package io.github.fabb.wigai.mcp.tool;

import io.github.fabb.wigai.bitwig.BitwigContainerFacade;
import io.github.fabb.wigai.bitwig.BitwigEventStream;
import io.github.fabb.wigai.bitwig.BitwigEditFacade;
import io.github.fabb.wigai.bitwig.BitwigProjectFacade;
import io.github.fabb.wigai.bitwig.BitwigSessionFacade;
import io.github.fabb.wigai.bitwig.DeviceCatalog;
import io.github.fabb.wigai.common.error.BitwigApiException;
import io.github.fabb.wigai.common.logging.StructuredLogger;
import io.github.fabb.wigai.mcp.McpErrorHandler;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * MCP tools that change the project: tracks, mixer, devices, device parameters, clips, notes,
 * transport settings and Bitwig application actions. All of them go through {@link BitwigEditFacade}.
 */
public class EditTools {

    private static final String TRACK_PROPS = """
                "track_index": {"type": "integer", "minimum": 0, "description": "0-based track index (see list_tracks)"},
                "track_name": {"type": "string", "description": "Exact track name (alternative to track_index)"}""";

    private static final String DEVICE_PROPS = TRACK_PROPS + """
                ,
                "device_index": {"type": "integer", "minimum": 0, "description": "0-based device index on the track (see list_devices_on_track)"}""";

    private static final String CHAIN_PROPS = """
                "chain": {"type": "string", "description": "A chain inside the device, e.g. 'Modulator' or 'Carrier' (Vocoder), 'FX' (FX devices), or 'layer:0' / 'layer:Name' (layer devices). See get_device_chains."},
                "chain_device_index": {"type": "integer", "minimum": 0, "description": "Device index within that chain (required with chain)"}""";

    private static final String ARRANGER_PROP = """
                "arranger": {"type": "boolean", "description": "Use the clip selected in Bitwig's arranger instead of a launcher slot"}""";

    private static final String POSITION_PROPS = """
                "position": {"type": "string", "enum": ["start", "end"], "description": "Where in the chain to insert (default end)"},
                "after_device_index": {"type": "integer", "minimum": 0, "description": "Insert directly after this device instead"}""";

    @FunctionalInterface
    private interface Op {
        Object run(Args a) throws Exception;
    }

    /** A tool body that also needs the MCP exchange (to send notifications) and the request's progress token. */
    @FunctionalInterface
    private interface StreamingOp {
        Object run(Args a, McpSyncServerExchange exchange, Object progressToken) throws Exception;
    }

    /** Thin typed accessor over raw MCP arguments. */
    static final class Args {
        private final Map<String, Object> raw;
        private final BitwigEditFacade facade;
        private final String operation;

        Args(Map<String, Object> raw, BitwigEditFacade facade, String operation) {
            this.raw = raw == null ? Map.of() : raw;
            this.facade = facade;
            this.operation = operation;
        }

        Integer integer(String key) {
            Object v = raw.get(key);
            if (v == null) {
                return null;
            }
            if (!(v instanceof Number n)) {
                throw new IllegalArgumentException("Parameter '" + key + "' must be an integer");
            }
            return n.intValue();
        }

        int requiredInt(String key) {
            Integer v = integer(key);
            if (v == null) {
                throw new IllegalArgumentException("Missing required parameter '" + key + "'");
            }
            return v;
        }

        Double number(String key) {
            Object v = raw.get(key);
            if (v == null) {
                return null;
            }
            if (!(v instanceof Number n)) {
                throw new IllegalArgumentException("Parameter '" + key + "' must be a number");
            }
            return n.doubleValue();
        }

        String string(String key) {
            Object v = raw.get(key);
            if (v == null) {
                return null;
            }
            if (!(v instanceof String s)) {
                throw new IllegalArgumentException("Parameter '" + key + "' must be a string");
            }
            return s;
        }

        String requiredString(String key) {
            String v = string(key);
            if (v == null || v.isBlank()) {
                throw new IllegalArgumentException("Missing required parameter '" + key + "'");
            }
            return v;
        }

        Boolean bool(String key) {
            Object v = raw.get(key);
            if (v == null) {
                return null;
            }
            if (!(v instanceof Boolean b)) {
                throw new IllegalArgumentException("Parameter '" + key + "' must be a boolean");
            }
            return b;
        }

        boolean requiredBool(String key) {
            Boolean v = bool(key);
            if (v == null) {
                throw new IllegalArgumentException("Missing required parameter '" + key + "'");
            }
            return v;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list(String key) {
            Object v = raw.get(key);
            if (!(v instanceof List<?> l) || l.isEmpty()) {
                throw new IllegalArgumentException("Parameter '" + key + "' must be a non-empty array");
            }
            List<Map<String, Object>> out = new ArrayList<>();
            for (Object o : l) {
                if (!(o instanceof Map)) {
                    throw new IllegalArgumentException("Items of '" + key + "' must be objects");
                }
                out.add((Map<String, Object>) o);
            }
            return out;
        }

        Map<String, String> stringMap(String key) {
            Object v = raw.get(key);
            if (v == null) {
                return Map.of();
            }
            if (!(v instanceof Map<?, ?> m)) {
                throw new IllegalArgumentException("Parameter '" + key + "' must be an object");
            }
            Map<String, String> out = new java.util.LinkedHashMap<>();
            for (Map.Entry<?, ?> e : m.entrySet()) {
                if (!(e.getValue() instanceof String sv)) {
                    throw new IllegalArgumentException("Values of '" + key + "' must be strings");
                }
                out.put(String.valueOf(e.getKey()), sv);
            }
            return out;
        }

        /** The track for a launcher clip, or null when the arranger's selected clip is meant. */
        Integer clipTrack() throws BitwigApiException {
            return arranger() ? null : track();
        }

        boolean arranger() {
            return Boolean.TRUE.equals(bool("arranger"));
        }

        List<String> stringList(String key) {
            Object v = raw.get(key);
            if (v == null) {
                return List.of();
            }
            if (!(v instanceof List<?> l)) {
                throw new IllegalArgumentException("Parameter '" + key + "' must be an array of strings");
            }
            List<String> out = new ArrayList<>();
            for (Object o : l) {
                if (!(o instanceof String str)) {
                    throw new IllegalArgumentException("Items of '" + key + "' must be strings");
                }
                out.add(str);
            }
            return out;
        }

        int track() throws BitwigApiException {
            return facade.resolveTrackIndex(integer("track_index"), string("track_name"), operation);
        }

        int track(String indexKey, String nameKey) throws BitwigApiException {
            return facade.resolveTrackIndex(integer(indexKey), string(nameKey), operation);
        }
    }

    private static McpServerFeatures.SyncToolSpecification tool(String name, String description, String properties,
                                                                String required, BitwigEditFacade facade,
                                                                StructuredLogger logger, Op op) {
        String schema = "{\"type\": \"object\", \"properties\": {" + properties + "}"
            + (required == null ? "" : ", \"required\": [" + required + "]")
            + ", \"additionalProperties\": false}";
        var t = McpSchema.Tool.builder().name(name).description(description).inputSchema(schema).build();
        BiFunction<McpSyncServerExchange, CallToolRequest, McpSchema.CallToolResult> handler =
            (exchange, req) -> McpErrorHandler.executeWithErrorHandling(name, logger,
                () -> op.run(new Args(req.arguments(), facade, name)));
        return McpServerFeatures.SyncToolSpecification.builder().tool(t).callHandler(handler).build();
    }

    private static McpServerFeatures.SyncToolSpecification streamingTool(String name, String description,
                                                                         String properties, BitwigEditFacade facade,
                                                                         StructuredLogger logger, StreamingOp op) {
        String schema = "{\"type\": \"object\", \"properties\": {" + properties + "}, \"additionalProperties\": false}";
        var t = McpSchema.Tool.builder().name(name).description(description).inputSchema(schema).build();
        BiFunction<McpSyncServerExchange, CallToolRequest, McpSchema.CallToolResult> handler =
            (exchange, req) -> McpErrorHandler.executeWithErrorHandling(name, logger, () -> op.run(
                new Args(req.arguments(), facade, name), exchange,
                req.meta() == null ? null : req.meta().get("progressToken")));
        return McpServerFeatures.SyncToolSpecification.builder().tool(t).callHandler(handler).build();
    }

    /** Following changes in Bitwig: a pollable event log, and a call that streams events as notifications. */
    public static List<McpServerFeatures.SyncToolSpecification> eventSpecifications(
            BitwigEditFacade f, BitwigEventStream events, StructuredLogger log) {
        List<McpServerFeatures.SyncToolSpecification> specs = new ArrayList<>();
        String typesProp = "\"types\": {\"type\": \"array\", \"items\": {\"type\": \"string\", \"enum\": "
            + "[\"transport\", \"project\", \"track\", \"clip\", \"selection\", \"parameter\"]}, "
            + "\"description\": \"Only these kinds of change (default all)\"}";

        specs.add(tool("get_events",
            "Changes in Bitwig since since_seq: transport (play, record, loop, tempo), project (switch, engine), "
                + "track (name, mute, solo, arm, volume, pan, added/removed), clip (launcher playback state, "
                + "clips created/deleted), selection (the user's selected track, device, preset) and parameter (the "
                + "selected device's knobs as the user turns them). Call without since_seq to get a starting point, "
                + "then pass next_seq back. wait_ms waits up to that long for the next change (long-poll). Rapid "
                + "changes to the same thing are merged into the latest value.",
            """
                "since_seq": {"type": "integer", "minimum": 0},
                "wait_ms": {"type": "integer", "minimum": 0, "maximum": 30000},
                """ + typesProp,
            null, f, log,
            a -> events.events(a.integer("since_seq") == null ? null : a.integer("since_seq").longValue(),
                a.stringList("types"), a.integer("wait_ms") == null ? 0 : a.integer("wait_ms"))));

        specs.add(streamingTool("watch",
            "Watch Bitwig for duration_ms (up to 60000) and stream each change as it happens: as an MCP progress "
                + "notification (when the call carries a progress token) and as a log message (logger "
                + "'wigai.events'). Returns every change seen, so clients that do not show notifications still get them.",
            """
                "duration_ms": {"type": "integer", "minimum": 100, "maximum": 60000},
                """ + typesProp,
            f, log,
            (a, exchange, token) -> {
                long duration = a.integer("duration_ms") == null ? 10000 : a.integer("duration_ms");
                List<String> types = a.stringList("types");
                long deadline = System.currentTimeMillis() + duration;
                long since = events.buffer().latestSeq();
                List<Map<String, Object>> seen = new ArrayList<>();
                var json = new com.fasterxml.jackson.databind.ObjectMapper();
                while (System.currentTimeMillis() < deadline) {
                    for (var e : events.buffer().since(since, types, deadline - System.currentTimeMillis())) {
                        since = e.seq();
                        Map<String, Object> m = e.toMap();
                        seen.add(m);
                        String text = json.writeValueAsString(m);
                        if (exchange != null) {
                            if (token != null) {
                                exchange.progressNotification(new McpSchema.ProgressNotification(String.valueOf(token),
                                    (double) seen.size(), null, text));
                            }
                            exchange.loggingNotification(new McpSchema.LoggingMessageNotification(
                                McpSchema.LoggingLevel.INFO, "wigai.events", text));
                        }
                    }
                }
                Map<String, Object> result = new java.util.LinkedHashMap<>();
                result.put("duration_ms", duration);
                result.put("events", seen);
                result.put("next_seq", since);
                result.put("now", events.now());
                return result;
            }));
        return specs;
    }

    public static List<McpServerFeatures.SyncToolSpecification> specifications(BitwigEditFacade f, StructuredLogger log) {
        List<McpServerFeatures.SyncToolSpecification> specs = new ArrayList<>();

        // ---- tracks
        specs.add(tool("create_track",
            "Create a new audio, instrument or effect track, optionally at a position and with a name.",
            """
                "type": {"type": "string", "enum": ["audio", "instrument", "effect"]},
                "position": {"type": "integer", "minimum": 0, "description": "Track index to insert at (default: end)"},
                "name": {"type": "string"}""",
            "\"type\"", f, log,
            a -> f.createTrack(a.requiredString("type"), a.integer("position"), a.string("name"))));

        specs.add(tool("delete_track", "Delete a track.", TRACK_PROPS, null, f, log,
            a -> f.deleteTrack(a.track())));

        specs.add(tool("duplicate_track", "Duplicate a track with its devices and clips.", TRACK_PROPS, null, f, log,
            a -> f.duplicateTrack(a.track())));

        specs.add(tool("get_track_mixer", "Read a track's mixer state: volume, pan, mute, solo, arm, monitor mode, sends.",
            TRACK_PROPS, null, f, log, a -> f.trackMixerState(a.track())));

        specs.add(tool("set_track",
            "Change track settings. Only given fields change. volume_db is matched exactly against Bitwig's dB display; "
                + "volume is the raw 0-1 fader position. pan is -1 (left) to 1 (right).",
            TRACK_PROPS + """
                ,
                "name": {"type": "string"},
                "volume": {"type": "number", "minimum": 0, "maximum": 1},
                "volume_db": {"type": "number", "maximum": 6},
                "pan": {"type": "number", "minimum": -1, "maximum": 1},
                "mute": {"type": "boolean"},
                "solo": {"type": "boolean"},
                "arm": {"type": "boolean"},
                "activated": {"type": "boolean"},
                "monitor_mode": {"type": "string", "enum": ["ON", "OFF", "AUTO"]},
                "color": {"type": "string", "description": "#RRGGBB"},
                "group_expanded": {"type": "boolean", "description": "Expand or collapse a group track"},
                "crossfade_mode": {"type": "string", "enum": ["A", "B", "AB"]}""",
            null, f, log,
            a -> f.setTrackProperties(a.track(), a.string("name"), a.number("volume"), a.number("volume_db"),
                a.number("pan"), a.bool("mute"), a.bool("solo"), a.bool("arm"), a.bool("activated"),
                a.string("monitor_mode"), a.string("color"), a.bool("group_expanded"), a.string("crossfade_mode"))));

        specs.add(tool("set_send", "Set a track's send level (0-1) and/or enable it.",
            TRACK_PROPS + """
                ,
                "send_index": {"type": "integer", "minimum": 0},
                "value": {"type": "number", "minimum": 0, "maximum": 1},
                "enabled": {"type": "boolean"}""",
            "\"send_index\"", f, log,
            a -> f.setSend(a.track(), a.requiredInt("send_index"), a.number("value"), a.bool("enabled"))));

        // ---- devices
        specs.add(tool("list_bitwig_devices",
            "List Bitwig's built-in devices usable with insert_device, with type (instrument, audio effect, "
                + "note effect) and category. Optional filter matches name, type or category.",
            "\"filter\": {\"type\": \"string\"}", null, f, log, a -> f.listBitwigDevices(a.string("filter"))));

        specs.add(tool("insert_device",
            "Insert a device on a track: a Bitwig built-in by name (e.g. 'EQ+', 'Audio Receiver', 'Reverb'), "
                + "a plugin by CLAP id (e.g. 'org.surge-synth-team.surge-xt'), VST3 class id or VST2 id "
                + "(see list_installed_devices), "
                + "or a preset file (.bwpreset path, see list_presets). "
                + "To insert inside a device's chain, give device_index (the container) and chain; "
                + "after_device_index then refers to a device in that chain.",
            TRACK_PROPS + ",\n" + POSITION_PROPS + """
                ,
                "device_index": {"type": "integer", "minimum": 0, "description": "Container device, only with chain"},
                "chain": {"type": "string", "description": "Chain inside device_index, e.g. 'Modulator'. See get_device_chains."},
                "bitwig_device": {"type": "string"},
                "clap_id": {"type": "string"},
                "vst3_id": {"type": "string"},
                "vst2_id": {"type": "integer"},
                "preset_file": {"type": "string", "description": "Absolute path of a .bwpreset (or other device file)"}""",
            null, f, log,
            a -> f.insertDevice(a.track(), a.integer("device_index"), a.string("chain"), a.string("bitwig_device"),
                a.string("clap_id"), a.string("vst3_id"), a.integer("vst2_id"), a.string("preset_file"),
                a.string("position"), a.integer("after_device_index"))));

        specs.add(tool("replace_device",
            "Replace a device (or a device inside a chain) in place with a Bitwig device, a plugin, or a preset file. "
                + "Loading a preset onto an existing device is a replace with preset_file.",
            DEVICE_PROPS + ",\n" + CHAIN_PROPS + """
                ,
                "bitwig_device": {"type": "string"},
                "clap_id": {"type": "string"},
                "vst3_id": {"type": "string"},
                "vst2_id": {"type": "integer"},
                "preset_file": {"type": "string", "description": "Absolute path of a .bwpreset (or other device file)"}""",
            "\"device_index\"", f, log,
            a -> f.replaceDevice(a.track(), a.requiredInt("device_index"), a.string("chain"), a.integer("chain_device_index"),
                a.string("bitwig_device"), a.string("clap_id"), a.string("vst3_id"), a.integer("vst2_id"),
                a.string("preset_file"))));

        specs.add(tool("get_device_chains",
            "List the chains inside a device and the devices in each: slots such as a Vocoder's 'Modulator' and "
                + "'Carrier' or an FX device's 'FX', and layers of FX/Instrument Layer devices.",
            DEVICE_PROPS, "\"device_index\"", f, log,
            a -> f.getDeviceChains(a.track(), a.requiredInt("device_index"))));

        String copyProps = """
                "from_track_index": {"type": "integer", "minimum": 0},
                "from_track_name": {"type": "string"},
                "from_device_index": {"type": "integer", "minimum": 0},
                "from_chain": {"type": "string", "description": "Source is inside this chain of from_device_index"},
                "from_chain_device_index": {"type": "integer", "minimum": 0},
                "to_track_index": {"type": "integer", "minimum": 0},
                "to_track_name": {"type": "string"},
                "to_device_index": {"type": "integer", "minimum": 0, "description": "Container device, only with to_chain"},
                "to_chain": {"type": "string", "description": "Destination chain inside to_device_index"},
                """ + POSITION_PROPS;
        specs.add(tool("copy_device", "Copy a device (with all its settings) to another position or track.",
            copyProps, "\"from_device_index\"", f, log,
            a -> f.copyOrMoveDevice(a.track("from_track_index", "from_track_name"), a.requiredInt("from_device_index"),
                a.string("from_chain"), a.integer("from_chain_device_index"),
                a.track("to_track_index", "to_track_name"), a.integer("to_device_index"), a.string("to_chain"),
                a.string("position"), a.integer("after_device_index"), false)));
        specs.add(tool("move_device", "Move a device to another position or track.",
            copyProps, "\"from_device_index\"", f, log,
            a -> f.copyOrMoveDevice(a.track("from_track_index", "from_track_name"), a.requiredInt("from_device_index"),
                a.string("from_chain"), a.integer("from_chain_device_index"),
                a.track("to_track_index", "to_track_name"), a.integer("to_device_index"), a.string("to_chain"),
                a.string("position"), a.integer("after_device_index"), true)));

        specs.add(tool("delete_device", "Delete a device from a track, or from a chain inside a device.",
            DEVICE_PROPS + ",\n" + CHAIN_PROPS, "\"device_index\"", f, log,
            a -> f.deleteDevice(a.track(), a.requiredInt("device_index"), a.string("chain"), a.integer("chain_device_index"))));

        specs.add(tool("set_device_enabled", "Enable (un-bypass) or bypass a device, or a device inside a chain.",
            DEVICE_PROPS + ",\n" + CHAIN_PROPS + ",\n\"enabled\": {\"type\": \"boolean\"}", "\"device_index\", \"enabled\"", f, log,
            a -> f.setDeviceEnabled(a.track(), a.requiredInt("device_index"), a.string("chain"), a.integer("chain_device_index"),
                a.requiredBool("enabled"))));

        specs.add(tool("set_device_window_open", "Open or close a plugin's editor window.",
            DEVICE_PROPS + ",\n\"open\": {\"type\": \"boolean\"}", "\"device_index\", \"open\"", f, log,
            a -> f.setDeviceWindowOpen(a.track(), a.requiredInt("device_index"), a.requiredBool("open"))));

        specs.add(tool("get_device_parameters",
            "Read a device's parameters on any track without changing the user's selection. "
                + "Returns all remote-control pages, or one page if 'page' is given. A control that a modulator "
                + "(LFO, envelope, macro) is moving also has modulated_value/modulated_display, its effective value now.",
            DEVICE_PROPS + ",\n" + CHAIN_PROPS + ",\n\"page\": {\"type\": \"integer\", \"minimum\": 0}", "\"device_index\"", f, log,
            a -> f.getDeviceParameters(a.track(), a.requiredInt("device_index"), a.string("chain"),
                a.integer("chain_device_index"), a.integer("page"))));

        specs.add(tool("set_device_parameters",
            "Set parameters on any device by name (case-insensitive, searched across pages) or by page+index. "
                + "Values are normalized 0-1; the response shows Bitwig's displayed value so you can check units.",
            DEVICE_PROPS + ",\n" + CHAIN_PROPS + """
                ,
                "verify": {"type": "boolean", "description": "Wait for each value to be confirmed (default true); false is faster for bulk writes"},
                "parameters": {"type": "array", "items": {"type": "object", "properties": {
                    "name": {"type": "string"},
                    "page": {"type": "integer", "minimum": 0},
                    "index": {"type": "integer", "minimum": 0, "maximum": 7},
                    "value": {"type": "number", "minimum": 0, "maximum": 1}},
                  "required": ["value"]}}""",
            "\"device_index\", \"parameters\"", f, log,
            a -> {
                List<BitwigEditFacade.ParamAssignment> list = new ArrayList<>();
                for (Map<String, Object> p : a.list("parameters")) {
                    Args pa = new Args(p, f, "set_device_parameters");
                    Double v = pa.number("value");
                    if (v == null) {
                        throw new IllegalArgumentException("Each parameter needs a 'value'");
                    }
                    list.add(new BitwigEditFacade.ParamAssignment(pa.integer("page"), pa.integer("index"), pa.string("name"), v));
                }
                return f.setDeviceParameters(a.track(), a.requiredInt("device_index"), a.string("chain"),
                    a.integer("chain_device_index"), list, !Boolean.FALSE.equals(a.bool("verify")));
            }));

        // ---- clips and files
        specs.add(tool("insert_file",
            "Import an audio or MIDI file. With slot_index it goes into that clip-launcher slot of the track; "
                + "without, Bitwig inserts it as a new track after the given track.",
            TRACK_PROPS + """
                ,
                "path": {"type": "string", "description": "Absolute path to a .wav/.mid/etc file"},
                "slot_index": {"type": "integer", "minimum": 0}""",
            "\"path\"", f, log,
            a -> f.insertFile(a.track(), a.integer("slot_index"), a.requiredString("path"))));

        specs.add(tool("create_clip", "Create an empty launcher clip in a slot.",
            TRACK_PROPS + """
                ,
                "slot_index": {"type": "integer", "minimum": 0},
                "length_beats": {"type": "integer", "minimum": 1}""",
            "\"slot_index\", \"length_beats\"", f, log,
            a -> f.createClip(a.track(), a.requiredInt("slot_index"), a.requiredInt("length_beats"))));

        specs.add(tool("delete_clip", "Delete the launcher clip in a slot.",
            TRACK_PROPS + ",\n\"slot_index\": {\"type\": \"integer\", \"minimum\": 0}", "\"slot_index\"", f, log,
            a -> f.deleteClip(a.track(), a.requiredInt("slot_index"))));

        specs.add(tool("write_notes",
            "Write MIDI notes into an existing launcher clip. Times are in beats from the clip start (1/64-beat grid).",
            TRACK_PROPS + """
                ,
                "slot_index": {"type": "integer", "minimum": 0},
                "clear_first": {"type": "boolean", "description": "Remove existing notes first (default false)"},
                "notes": {"type": "array", "items": {"type": "object", "properties": {
                    "start": {"type": "number", "minimum": 0},
                    "pitch": {"type": "integer", "minimum": 0, "maximum": 127},
                    "velocity": {"type": "integer", "minimum": 1, "maximum": 127},
                    "duration": {"type": "number", "exclusiveMinimum": 0}},
                  "required": ["start", "pitch", "duration"]}}""",
            "\"slot_index\", \"notes\"", f, log,
            a -> {
                List<BitwigEditFacade.Note> notes = new ArrayList<>();
                for (Map<String, Object> n : a.list("notes")) {
                    Args na = new Args(n, f, "write_notes");
                    Integer vel = na.integer("velocity");
                    notes.add(new BitwigEditFacade.Note(na.number("start"), na.requiredInt("pitch"),
                        vel == null ? 100 : vel, na.number("duration")));
                }
                Boolean clear = a.bool("clear_first");
                return f.writeNotes(a.track(), a.requiredInt("slot_index"), notes, Boolean.TRUE.equals(clear));
            }));

        // ---- transport
        specs.add(tool("set_transport", "Set tempo, play position (beats), arranger loop, metronome, record or overdub.",
            """
                "tempo_bpm": {"type": "number", "minimum": 20, "maximum": 666},
                "position_beats": {"type": "number", "minimum": 0},
                "loop": {"type": "boolean"},
                "metronome": {"type": "boolean"},
                "record": {"type": "boolean"},
                "overdub": {"type": "boolean"}""",
            null, f, log,
            a -> f.setTransport(a.number("tempo_bpm"), a.number("position_beats"), a.bool("loop"),
                a.bool("metronome"), a.bool("record"), a.bool("overdub"))));

        // ---- application
        specs.add(tool("list_actions",
            "List Bitwig application actions (menu commands), optionally filtered by a substring of id/name/category.",
            "\"filter\": {\"type\": \"string\"}", null, f, log, a -> f.listActions(a.string("filter"))));

        specs.add(tool("invoke_action", "Run a Bitwig application action by id (see list_actions).",
            "\"id\": {\"type\": \"string\"}", "\"id\"", f, log, a -> f.invokeAction(a.requiredString("id"))));

        specs.add(tool("undo", "Undo the last change in Bitwig.", "", null, f, log, a -> f.undo(false)));
        specs.add(tool("redo", "Redo the last undone change in Bitwig.", "", null, f, log, a -> f.undo(true)));

        return specs;
    }

    /** Device knowledge: what is installed, what it is for, and what its controls do. */
    public static List<McpServerFeatures.SyncToolSpecification> catalogSpecifications(
            BitwigEditFacade f, DeviceCatalog c, StructuredLogger log) {
        List<McpServerFeatures.SyncToolSpecification> specs = new ArrayList<>();

        specs.add(tool("list_installed_devices",
            "Find devices and plugins installed on this machine (Bitwig built-ins, CLAP plugins, LV2 plugins usable "
                + "through Carla). The query is matched against names, vendors, categories, descriptions and the "
                + "device guide's 'use for' text, best matches first; e.g. 'vocal compressor', 'reverb', 'pitch'. "
                + "Use it to pick candidates before recommending or inserting a device.",
            """
                "query": {"type": "string"},
                "kind": {"type": "string", "enum": ["bitwig", "clap", "lv2"]}""",
            null, f, log, a -> c.listInstalled(a.string("query"), a.string("kind"))));

        specs.add(tool("describe_device",
            "Everything known about one device: what it is for and how to use it (device guide), its plugin "
                + "description, its chains, and every control's real range, units and options. Controls are measured "
                + "the first time a device is described and cached (with a scan history); pass track/device_index to "
                + "measure a device already in the project (its values are restored), or just a name to measure it on "
                + "a temporary track. Measuring is skipped while the transport plays.",
            TRACK_PROPS + """
                ,
                "name": {"type": "string", "description": "Bitwig device name, plugin name or CLAP id"},
                "device_index": {"type": "integer", "minimum": 0},
                "chain": {"type": "string"},
                "chain_device_index": {"type": "integer", "minimum": 0},
                "scan": {"type": "boolean", "description": "Measure controls if not cached (default true)"},
                "rescan": {"type": "boolean", "description": "Measure again even if cached"}""",
            null, f, log,
            a -> {
                Integer track = a.integer("track_index") != null || a.string("track_name") != null ? a.track() : null;
                return c.describe(a.string("name"), track, a.integer("device_index"), a.string("chain"),
                    a.integer("chain_device_index"), !Boolean.FALSE.equals(a.bool("scan")), Boolean.TRUE.equals(a.bool("rescan")));
            }));

        specs.add(tool("fetch_device_docs",
            "Fetch Bitwig's own description of every built-in device from Bitwig's online user guide into the local "
                + "catalog, so describe_device can include it (field bitwig_docs). Run once, and again after a Bitwig "
                + "update; needs internet access.",
            "", null, f, log, a -> c.fetchDocs()));

        specs.add(tool("catalog_status",
            "Device catalog coverage and freshness: entries and scans per kind, the most recent scans with what "
                + "changed, scans that failed, and scans made on an older Bitwig version.",
            "", null, f, log, a -> c.status()));

        return specs;
    }

    /** Project-wide, transport, track-order and clip-management tools. */
    public static List<McpServerFeatures.SyncToolSpecification> projectSpecifications(
            BitwigEditFacade f, BitwigSessionFacade s, BitwigProjectFacade p, StructuredLogger log) {
        List<McpServerFeatures.SyncToolSpecification> specs = new ArrayList<>();

        specs.add(tool("project_state", "Whether any track is soloed, muted or armed, and whether the project has unsaved changes.",
            "", null, f, log, a -> p.projectState()));

        specs.add(tool("reset_project",
            "Clear every solo, mute and/or arm in the project. A forgotten solo silences all other tracks.",
            """
                "unsolo": {"type": "boolean"},
                "unmute": {"type": "boolean"},
                "unarm": {"type": "boolean"}""",
            null, f, log, a -> p.resetProject(Boolean.TRUE.equals(a.bool("unsolo")), Boolean.TRUE.equals(a.bool("unmute")),
                Boolean.TRUE.equals(a.bool("unarm")))));

        specs.add(tool("create_scene", "Add a new empty scene.", "", null, f, log, a -> p.createScene()));

        specs.add(tool("set_groove", "Set the global groove. Values are normalized 0-1; the response shows Bitwig's displayed values.",
            """
                "enabled": {"type": "boolean"},
                "shuffle_amount": {"type": "number", "minimum": 0, "maximum": 1},
                "shuffle_rate": {"type": "number", "minimum": 0, "maximum": 1},
                "accent_amount": {"type": "number", "minimum": 0, "maximum": 1},
                "accent_rate": {"type": "number", "minimum": 0, "maximum": 1},
                "accent_phase": {"type": "number", "minimum": 0, "maximum": 1}""",
            null, f, log, a -> p.setGroove(a.bool("enabled"), a.number("shuffle_amount"), a.number("shuffle_rate"),
                a.number("accent_amount"), a.number("accent_rate"), a.number("accent_phase"))));

        specs.add(tool("set_transport_options",
            "Transport settings beyond play/stop: punch in/out, metronome volume (0-1), pre-roll, automation write mode, "
                + "launcher overdub, default launch quantization, post-recording action; or run a command "
                + "(rewind, fast_forward, tap_tempo, continue, record). Enum values are Bitwig's ids; the response shows the current ones.",
            """
                "punch_in": {"type": "boolean"},
                "punch_out": {"type": "boolean"},
                "metronome_volume": {"type": "number", "minimum": 0, "maximum": 1},
                "pre_roll": {"type": "string"},
                "automation_write_mode": {"type": "string"},
                "launcher_overdub": {"type": "boolean"},
                "default_launch_quantization": {"type": "string"},
                "post_recording_action": {"type": "string"},
                "command": {"type": "string", "enum": ["rewind", "fast_forward", "tap_tempo", "continue", "record"]}""",
            null, f, log, a -> p.setTransportOptions(a.bool("punch_in"), a.bool("punch_out"), a.number("metronome_volume"),
                a.string("pre_roll"), a.string("automation_write_mode"), a.bool("launcher_overdub"),
                a.string("default_launch_quantization"), a.string("post_recording_action"), a.string("command"))));

        String clipMoveProps = """
                "from_track_index": {"type": "integer", "minimum": 0},
                "from_track_name": {"type": "string"},
                "from_slot": {"type": "integer", "minimum": 0},
                "to_track_index": {"type": "integer", "minimum": 0},
                "to_track_name": {"type": "string"},
                "to_slot": {"type": "integer", "minimum": 0}""";
        specs.add(tool("copy_clip", "Copy a launcher clip to an empty slot (any track).", clipMoveProps, "\"from_slot\", \"to_slot\"", f, log,
            a -> p.copyOrMoveClip(a.track("from_track_index", "from_track_name"), a.requiredInt("from_slot"),
                a.track("to_track_index", "to_track_name"), a.requiredInt("to_slot"), false)));
        specs.add(tool("move_clip", "Move a launcher clip to an empty slot (any track).", clipMoveProps, "\"from_slot\", \"to_slot\"", f, log,
            a -> p.copyOrMoveClip(a.track("from_track_index", "from_track_name"), a.requiredInt("from_slot"),
                a.track("to_track_index", "to_track_name"), a.requiredInt("to_slot"), true)));

        String slotProps = TRACK_PROPS + ",\n\"slot_index\": {\"type\": \"integer\", \"minimum\": 0}";
        specs.add(tool("duplicate_clip", "Duplicate a launcher clip into the next free slot.", slotProps, "\"slot_index\"", f, log,
            a -> p.duplicateClip(a.track(), a.requiredInt("slot_index"))));
        specs.add(tool("record_into_slot", "Start recording into a launcher slot (the track must be armed).", slotProps, "\"slot_index\"", f, log,
            a -> p.recordIntoSlot(a.track(), a.requiredInt("slot_index"))));

        String clipProps = slotProps + ",\n" + ARRANGER_PROP;
        specs.add(tool("edit_notes",
            "Change notes in a launcher clip (or the arranger's selected clip with arranger: true): all of them, or "
                + "those matching a pitch and/or a start range in beats. velocity/pressure/chance/release_velocity/"
                + "velocity_spread 0-1, duration in beats, pan and timbre -1..1, gain 0-1, transpose in semitones "
                + "(micro-pitch, fractional allowed), muted, repeats (count, 0 = off) with repeat_curve and "
                + "repeat_velocity_curve (-1..1) and repeat_velocity_end (-1..1). occurrence: play only on the first "
                + "loop, not the first, when the previous note played, etc. ('off' clears it). recurrence: play on "
                + "selected loop cycles, recurrence_length cycles (1-8, 0 = off) with recurrence_mask bits (bit 0 = "
                + "first cycle).",
            clipProps + """
                ,
                "release_velocity": {"type": "number", "minimum": 0, "maximum": 1},
                "velocity_spread": {"type": "number", "minimum": 0, "maximum": 1},
                "occurrence": {"type": "string", "enum": ["off", "always", "first", "not_first", "prev", "not_prev", "prev_channel", "not_prev_channel"]},
                "recurrence_length": {"type": "integer", "minimum": 0, "maximum": 8},
                "recurrence_mask": {"type": "integer", "minimum": 0, "maximum": 255},
                "repeat_curve": {"type": "number", "minimum": -1, "maximum": 1},
                "repeat_velocity_end": {"type": "number", "minimum": -1, "maximum": 1},
                "repeat_velocity_curve": {"type": "number", "minimum": -1, "maximum": 1},
                "pitch": {"type": "integer", "minimum": 0, "maximum": 127},
                "from_beats": {"type": "number", "minimum": 0},
                "to_beats": {"type": "number", "minimum": 0},
                "velocity": {"type": "number", "minimum": 0, "maximum": 1},
                "duration": {"type": "number", "exclusiveMinimum": 0},
                "gain": {"type": "number", "minimum": 0, "maximum": 1},
                "pan": {"type": "number", "minimum": -1, "maximum": 1},
                "timbre": {"type": "number", "minimum": -1, "maximum": 1},
                "pressure": {"type": "number", "minimum": 0, "maximum": 1},
                "transpose": {"type": "number"},
                "chance": {"type": "number", "minimum": 0, "maximum": 1},
                "muted": {"type": "boolean"},
                "repeats": {"type": "integer", "minimum": 0}""",
            null, f, log,
            a -> s.editNotes(a.clipTrack(), a.integer("slot_index"), a.arranger(),
                new BitwigSessionFacade.NoteFilter(a.integer("pitch"), a.number("from_beats"), a.number("to_beats")),
                new BitwigSessionFacade.NoteChanges(a.number("velocity"), a.number("duration"), a.number("gain"),
                    a.number("pan"), a.number("timbre"), a.number("pressure"), a.number("transpose"), a.number("chance"),
                    a.bool("muted"), a.integer("repeats"), a.number("release_velocity"), a.number("velocity_spread"),
                    a.string("occurrence"), a.integer("recurrence_length"), a.integer("recurrence_mask"),
                    a.number("repeat_curve"), a.number("repeat_velocity_end"), a.number("repeat_velocity_curve")))));

        specs.add(tool("transform_clip",
            "Whole-clip operations on a launcher clip (or the arranger's selected clip with arranger: true): transpose "
                + "(semitones), quantize (0-1 strength), duplicate its content, set launch quantization and launch mode "
                + "(launcher only; Bitwig enum ids, the response shows the current ones).",
            clipProps + """
                ,
                "transpose": {"type": "integer"},
                "quantize": {"type": "number", "minimum": 0, "maximum": 1},
                "duplicate_content": {"type": "boolean"},
                "launch_quantization": {"type": "string"},
                "launch_mode": {"type": "string"}""",
            null, f, log,
            a -> s.transformClip(a.clipTrack(), a.integer("slot_index"), a.arranger(), a.integer("transpose"),
                a.number("quantize"), a.bool("duplicate_content"), a.string("launch_quantization"), a.string("launch_mode"))));

        specs.add(tool("write_arranger_notes",
            "Write notes into the clip selected in Bitwig's arranger (the API cannot place or select arranger clips; "
                + "the user selects one). Same note format as write_notes.",
            """
                "clear_first": {"type": "boolean"},
                "notes": {"type": "array", "items": {"type": "object", "properties": {
                    "start": {"type": "number", "minimum": 0},
                    "pitch": {"type": "integer", "minimum": 0, "maximum": 127},
                    "velocity": {"type": "integer", "minimum": 1, "maximum": 127},
                    "duration": {"type": "number", "exclusiveMinimum": 0}},
                  "required": ["start", "pitch", "duration"]}}""",
            "\"notes\"", f, log,
            a -> {
                List<BitwigEditFacade.Note> notes = new ArrayList<>();
                for (Map<String, Object> n : a.list("notes")) {
                    Args na = new Args(n, f, "write_arranger_notes");
                    Integer vel = na.integer("velocity");
                    notes.add(new BitwigEditFacade.Note(na.number("start"), na.requiredInt("pitch"), vel == null ? 100 : vel,
                        na.number("duration")));
                }
                return s.writeArrangerNotes(notes, Boolean.TRUE.equals(a.bool("clear_first")));
            }));

        specs.add(tool("set_scene", "Rename and/or color a scene.",
            """
                "scene_index": {"type": "integer", "minimum": 0},
                "name": {"type": "string"},
                "color": {"type": "string", "description": "#RRGGBB"}""",
            "\"scene_index\"", f, log,
            a -> s.setScene(a.requiredInt("scene_index"), a.string("name"), a.string("color"))));
        String sceneProp = "\"scene_index\": {\"type\": \"integer\", \"minimum\": 0}";
        specs.add(tool("delete_scene", "Delete a scene and its clips.", sceneProp, "\"scene_index\"", f, log,
            a -> s.deleteOrDuplicateScene(a.requiredInt("scene_index"), true)));
        specs.add(tool("duplicate_scene", "Duplicate a scene and its clips; the copy goes below it.", sceneProp,
            "\"scene_index\"", f, log,
            a -> s.deleteOrDuplicateScene(a.requiredInt("scene_index"), false)));
        String sceneMoveProps = """
                "from_scene": {"type": "integer", "minimum": 0},
                "to_scene": {"type": "integer", "minimum": 0}""";
        specs.add(tool("copy_scene", "Copy every clip of a scene onto another scene's row.", sceneMoveProps,
            "\"from_scene\", \"to_scene\"", f, log,
            a -> s.copyOrMoveScene(a.requiredInt("from_scene"), a.requiredInt("to_scene"), false)));
        specs.add(tool("move_scene", "Move every clip of a scene onto another scene's row.", sceneMoveProps,
            "\"from_scene\", \"to_scene\"", f, log,
            a -> s.copyOrMoveScene(a.requiredInt("from_scene"), a.requiredInt("to_scene"), true)));
        specs.add(tool("set_cue", "Set the cue (headphone) bus volume and its cue/master mix, both 0-1.",
            """
                "volume": {"type": "number", "minimum": 0, "maximum": 1},
                "mix": {"type": "number", "minimum": 0, "maximum": 1}""",
            null, f, log, a -> p.setCue(a.number("volume"), a.number("mix"))));

        return specs;
    }

    /** Drum pads, layer mixers, presets and the browser, backed by {@link BitwigContainerFacade}. */
    public static List<McpServerFeatures.SyncToolSpecification> containerSpecifications(
            BitwigEditFacade f, BitwigContainerFacade c, StructuredLogger log) {
        List<McpServerFeatures.SyncToolSpecification> specs = new ArrayList<>();
        String mixProps = """
                "name": {"type": "string"},
                "volume": {"type": "number", "minimum": 0, "maximum": 1},
                "volume_db": {"type": "number", "maximum": 6},
                "pan": {"type": "number", "minimum": -1, "maximum": 1},
                "mute": {"type": "boolean"},
                "solo": {"type": "boolean"},
                "activated": {"type": "boolean"}""";
        String padProp = "\"pad\": {\"type\": \"integer\", \"minimum\": 0, \"maximum\": 127, "
            + "\"description\": \"MIDI key of the pad (36 = C1, the usual kick)\"}";

        specs.add(tool("list_drum_pads",
            "The pads of a Drum Machine by MIDI key: name, volume, pan, mute, solo and the devices on each pad. "
                + "Empty pads are left out unless include_empty.",
            DEVICE_PROPS + ",\n\"include_empty\": {\"type\": \"boolean\"}", "\"device_index\"", f, log,
            a -> c.listDrumPads(a.track(), a.requiredInt("device_index"), Boolean.TRUE.equals(a.bool("include_empty")))));

        specs.add(tool("set_drum_pad", "Rename a Drum Machine pad or change its volume, pan, mute, solo or activation.",
            DEVICE_PROPS + ",\n" + padProp + ",\n" + mixProps, "\"device_index\", \"pad\"", f, log,
            a -> c.setDrumPad(a.track(), a.requiredInt("device_index"), a.requiredInt("pad"), a.string("name"),
                a.number("volume"), a.number("volume_db"), a.number("pan"), a.bool("mute"), a.bool("solo"),
                a.bool("activated"))));

        specs.add(tool("insert_into_drum_pad",
            "Add a device to a Drum Machine pad (after what is there): a Bitwig device, a plugin, a preset file, "
                + "or a sample file (Bitwig creates a Sampler for it).",
            DEVICE_PROPS + ",\n" + padProp + """
                ,
                "bitwig_device": {"type": "string"},
                "clap_id": {"type": "string"},
                "vst3_id": {"type": "string"},
                "file": {"type": "string", "description": "Absolute path of a sample or preset"}""",
            "\"device_index\", \"pad\"", f, log,
            a -> c.insertIntoDrumPad(a.track(), a.requiredInt("device_index"), a.requiredInt("pad"),
                a.string("bitwig_device"), a.string("clap_id"), a.string("vst3_id"), a.string("file"))));

        specs.add(tool("clear_drum_pads", "Unmute and/or unsolo every pad of a Drum Machine.",
            DEVICE_PROPS + """
                ,
                "mutes": {"type": "boolean"},
                "solos": {"type": "boolean"}""",
            "\"device_index\"", f, log,
            a -> c.clearDrumPads(a.track(), a.requiredInt("device_index"), Boolean.TRUE.equals(a.bool("mutes")),
                Boolean.TRUE.equals(a.bool("solos")))));

        specs.add(tool("list_layers",
            "The layers of a layer device (Instrument Layer, FX Layer, ...) with their mixer settings and devices. "
                + "Devices inside a layer are edited with chain 'layer:N' on the device tools.",
            DEVICE_PROPS, "\"device_index\"", f, log,
            a -> c.listLayers(a.track(), a.requiredInt("device_index"))));

        specs.add(tool("set_layer", "Rename a layer or change its volume, pan, mute, solo or activation.",
            DEVICE_PROPS + ",\n\"layer\": {\"type\": \"integer\", \"minimum\": 0},\n" + mixProps,
            "\"device_index\", \"layer\"", f, log,
            a -> c.setLayer(a.track(), a.requiredInt("device_index"), a.requiredInt("layer"), a.string("name"),
                a.number("volume"), a.number("volume_db"), a.number("pan"), a.bool("mute"), a.bool("solo"),
                a.bool("activated"))));

        specs.add(tool("list_presets",
            "Search Bitwig preset files on disk (installed packages and the user library). Every word of query must "
                + "match the name, collection, device or category. Load one with insert_device or replace_device "
                + "(preset_file). Packaged presets do not reveal their device; browser_open with mode 'replace' "
                + "lists the presets of a device.",
            """
                "query": {"type": "string"},
                "limit": {"type": "integer", "minimum": 1},
                "rescan": {"type": "boolean", "description": "Re-read the disk (after installing packages)"}""",
            null, f, log,
            a -> c.listPresets(a.string("query"), a.integer("limit"), Boolean.TRUE.equals(a.bool("rescan")))));

        specs.add(tool("browser_open",
            "Open Bitwig's browser to load something at a place: mode 'replace' (a device; Bitwig shows that "
                + "device's presets), 'after'/'before' (a device), 'start'/'end' (the track's chain). It shows in "
                + "Bitwig's window until browser_commit or browser_cancel.",
            TRACK_PROPS + """
                ,
                "device_index": {"type": "integer", "minimum": 0},
                "mode": {"type": "string", "enum": ["replace", "after", "before", "start", "end"]},
                "content_type": {"type": "string", "description": "e.g. Devices, Presets, Samples (see content_types)"}""",
            null, f, log,
            a -> c.openBrowser(a.track(), a.integer("device_index"), a.string("mode"), a.string("content_type"))));

        specs.add(tool("browser_state",
            "The open browser: content type, the selected item of each filter column, and a page of results "
                + "(64 per page). list_column also returns every item of that column.",
            """
                "list_column": {"type": "string", "enum": ["collection", "location", "device", "category", "tag", "device_type", "file_type", "creator"]},
                "results_page": {"type": "integer", "minimum": 0}""",
            null, f, log,
            a -> c.browserState(a.string("list_column"), a.integer("results_page") == null ? 0 : a.integer("results_page"))));

        specs.add(tool("browser_filter",
            "Narrow the open browser: content_type, and filters as {column: item}, e.g. {\"category\": \"Bass\", "
                + "\"creator\": \"Bitwig\"}. Columns: collection, location, device, category, tag, device_type, "
                + "file_type, creator. An 'Any ...' item clears a column.",
            """
                "content_type": {"type": "string"},
                "filters": {"type": "object", "additionalProperties": {"type": "string"}}""",
            null, f, log,
            a -> c.filterBrowser(a.string("content_type"), a.stringMap("filters"))));

        specs.add(tool("browser_commit", "Load a result of the open browser, by name or by index, and close it.",
            """
                "result": {"type": "string"},
                "result_index": {"type": "integer", "minimum": 0}""",
            null, f, log,
            a -> c.commitBrowser(a.string("result"), a.integer("result_index"))));

        specs.add(tool("browser_cancel", "Close the browser without loading anything.", "", null, f, log,
            a -> c.cancelBrowser()));

        return specs;
    }

    /** Listening and auditioning tools backed by {@link BitwigSessionFacade}. */
    public static List<McpServerFeatures.SyncToolSpecification> sessionSpecifications(
            BitwigEditFacade f, BitwigSessionFacade s, StructuredLogger log) {
        List<McpServerFeatures.SyncToolSpecification> specs = new ArrayList<>();

        specs.add(tool("set_engine_active",
            "Activate or deactivate Bitwig's audio engine for the current project. Only one open project has the "
                + "engine; without it nothing plays, meters or auditions (status shows audio_engine_active).",
            "\"active\": {\"type\": \"boolean\"}", "\"active\"", f, log,
            a -> s.setEngineActive(f.application(), a.requiredBool("active"))));

        specs.add(tool("get_levels",
            "Current peak and RMS meter readings for every track and the master (0-1 of Bitwig's meter scale, "
                + "not dB). Only meaningful while audio is playing.",
            "", null, f, log, a -> s.getLevels()));

        specs.add(tool("list_cue_markers", "List arranger cue markers with their positions in beats.",
            "", null, f, log, a -> s.listCueMarkers()));

        specs.add(tool("go_to_cue_marker", "Jump playback to a cue marker by index or name.",
            """
                "index": {"type": "integer", "minimum": 0},
                "name": {"type": "string"},
                "quantized": {"type": "boolean", "description": "Wait for the launch quantization (default false)"}""",
            null, f, log,
            a -> s.goToCueMarker(a.integer("index"), a.string("name"), Boolean.TRUE.equals(a.bool("quantized")))));

        specs.add(tool("set_arrangement", "Set the arranger loop range (in beats) and/or the time signature, e.g. '6/8'.",
            """
                "loop_start_beats": {"type": "number", "minimum": 0},
                "loop_length_beats": {"type": "number", "exclusiveMinimum": 0},
                "time_signature": {"type": "string"}""",
            null, f, log,
            a -> s.setArrangement(a.number("loop_start_beats"), a.number("loop_length_beats"), a.string("time_signature"))));

        specs.add(tool("rename_scene", "Rename a scene.",
            """
                "scene_index": {"type": "integer", "minimum": 0},
                "name": {"type": "string"}""",
            "\"scene_index\", \"name\"", f, log,
            a -> s.renameScene(a.requiredInt("scene_index"), a.requiredString("name"))));

        specs.add(tool("stop_track", "Stop a track's launcher clip, or return the track to arrangement playback.",
            TRACK_PROPS + ",\n\"return_to_arrangement\": {\"type\": \"boolean\"}", null, f, log,
            a -> s.stopTrack(a.track(), Boolean.TRUE.equals(a.bool("return_to_arrangement")))));

        specs.add(tool("play_notes",
            "Play notes into a track's instrument right now, without a clip, to audition a sound or chord. "
                + "Times are in milliseconds from the call.",
            TRACK_PROPS + """
                ,
                "notes": {"type": "array", "items": {"type": "object", "properties": {
                    "pitch": {"type": "integer", "minimum": 0, "maximum": 127},
                    "velocity": {"type": "integer", "minimum": 1, "maximum": 127},
                    "start_ms": {"type": "integer", "minimum": 0},
                    "duration_ms": {"type": "integer", "minimum": 1}},
                  "required": ["pitch", "duration_ms"]}}""",
            "\"notes\"", f, log,
            a -> {
                List<BitwigSessionFacade.AuditionNote> notes = new ArrayList<>();
                for (Map<String, Object> n : a.list("notes")) {
                    Args na = new Args(n, f, "play_notes");
                    Integer vel = na.integer("velocity");
                    Integer start = na.integer("start_ms");
                    notes.add(new BitwigSessionFacade.AuditionNote(na.requiredInt("pitch"), vel == null ? 100 : vel,
                        start == null ? 0 : start, na.requiredInt("duration_ms")));
                }
                return s.playNotes(a.track(), notes);
            }));

        specs.add(tool("get_playing_notes",
            "Which notes are sounding right now on each track (from clips, the user's input or play_notes).",
            "", null, f, log, a -> s.playingNotes()));

        specs.add(tool("send_midi",
            "Send a MIDI message into a track's instrument: cc (number, value 0-127), pitch_bend (value -8192..8191, "
                + "0 = center), program (number), aftertouch (value), or raw (status 0x80-0xEF, number, value). "
                + "channel 0-15, default 0. Use play_notes for notes.",
            TRACK_PROPS + """
                ,
                "type": {"type": "string", "enum": ["cc", "pitch_bend", "program", "aftertouch", "raw"]},
                "channel": {"type": "integer", "minimum": 0, "maximum": 15},
                "number": {"type": "integer", "minimum": 0, "maximum": 127},
                "value": {"type": "integer"},
                "status": {"type": "integer"}""",
            "\"type\"", f, log,
            a -> s.sendMidi(a.track(), a.requiredString("type"), a.integer("channel") == null ? 0 : a.integer("channel"),
                a.integer("number"), a.integer("value"), a.integer("status"))));

        specs.add(tool("show",
            "Bring something into view in Bitwig's window for the user: a panel layout (ARRANGE, MIX, EDIT), a track, "
                + "a launcher clip in the editor (slot_index), or a device (device_index). Changes the user's selection.",
            TRACK_PROPS + """
                ,
                "panel_layout": {"type": "string", "enum": ["ARRANGE", "MIX", "EDIT"]},
                "slot_index": {"type": "integer", "minimum": 0},
                "device_index": {"type": "integer", "minimum": 0}""",
            null, f, log,
            a -> f.show(a.string("panel_layout"),
                a.integer("track_index") != null || a.string("track_name") != null ? a.track() : null,
                a.integer("slot_index"), a.integer("device_index"))));

        specs.add(tool("read_notes",
            "Read the MIDI notes of a launcher clip, or of the arranger's selected clip with arranger: true (beats from "
                + "the clip start), with every expression and condition that is not at its default, and the clip settings.",
            TRACK_PROPS + ",\n\"slot_index\": {\"type\": \"integer\", \"minimum\": 0},\n" + ARRANGER_PROP, null, f, log,
            a -> s.readNotes(a.clipTrack(), a.integer("slot_index"), a.arranger())));

        specs.add(tool("set_clip",
            "Clip settings for a launcher clip (or the arranger's selected clip with arranger: true): name, loop "
                + "on/off, loop start and length, play start and stop (beats), shuffle on/off, accent (0-1, 0.5 = none), color.",
            TRACK_PROPS + ",\n" + ARRANGER_PROP + """
                ,
                "slot_index": {"type": "integer", "minimum": 0},
                "name": {"type": "string"},
                "loop_enabled": {"type": "boolean"},
                "loop_start_beats": {"type": "number", "minimum": 0},
                "loop_length_beats": {"type": "number", "exclusiveMinimum": 0},
                "play_start_beats": {"type": "number", "minimum": 0},
                "play_stop_beats": {"type": "number", "minimum": 0},
                "shuffle": {"type": "boolean"},
                "accent": {"type": "number", "minimum": 0, "maximum": 1},
                "color": {"type": "string", "description": "#RRGGBB"}""",
            null, f, log,
            a -> s.setClip(a.clipTrack(), a.integer("slot_index"), a.arranger(), new BitwigSessionFacade.ClipSettings(
                a.string("name"), a.number("loop_start_beats"), a.number("loop_length_beats"), a.bool("loop_enabled"),
                a.number("play_start_beats"), a.number("play_stop_beats"), a.bool("shuffle"), a.number("accent"),
                a.string("color")))));

        return specs;
    }
}
