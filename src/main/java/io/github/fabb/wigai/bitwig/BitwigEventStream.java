package io.github.fabb.wigai.bitwig;

import com.bitwig.extension.controller.api.*;
import io.github.fabb.wigai.common.Logger;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Turns Bitwig's change callbacks into a numbered event log that MCP clients can follow (get_events, watch):
 * transport state, the project, each track's name/mute/solo/arm/volume/pan, launcher clip playback, the user's
 * selected track and device, and the controls of the selected device's remote-control page. Observers are
 * registered in the constructor (during init()); the burst of initial values Bitwig sends then is ignored.
 */
public class BitwigEventStream {

    private static final class Constants {
        static final int CAPACITY = 2000;
        static final long COALESCE_MS = 250;
        static final long SETTLE_AFTER_INIT_MS = 2000;
        static final int REMOTE_CONTROLS = 8;
        private Constants() {}
    }

    public static final List<String> TYPES = List.of("transport", "project", "track", "clip", "selection", "parameter");

    private static final String[] CLIP_STATES = {"stopped", "playing", "recording"};

    private final EventBuffer buffer = new EventBuffer(Constants.CAPACITY, Constants.COALESCE_MS);
    private final Transport transport;
    private final TrackBank trackBank;
    private final CursorTrack selectedTrack;
    private final PinnableCursorDevice selectedDevice;
    private final CursorRemoteControlsPage selectedPage;
    private volatile boolean live;

    public BitwigEventStream(ControllerHost host, Logger logger, Application application, Transport transport,
                             TrackBank trackBank) {
        this.transport = transport;
        this.trackBank = trackBank;

        transport.isPlaying().addValueObserver(v -> emit("transport", "transport:playing", "playing", v));
        transport.isArrangerRecordEnabled().addValueObserver(v -> emit("transport", "transport:recording", "recording", v));
        transport.isArrangerLoopEnabled().addValueObserver(v -> emit("transport", "transport:loop", "loop", v));
        transport.isMetronomeEnabled().addValueObserver(v -> emit("transport", "transport:metronome", "metronome", v));
        transport.tempo().displayedValue().addValueObserver(v -> emit("transport", "transport:tempo", "tempo", v));
        transport.timeSignature().addValueObserver(v -> emit("transport", "transport:time_signature", "time_signature", v));
        transport.getPosition().markInterested();

        application.projectName().addValueObserver(v -> emit("project", "project:name", "project", v));
        application.hasActiveEngine().addValueObserver(v -> emit("project", "project:engine", "audio_engine_active", v));

        for (int t = 0; t < trackBank.getSizeOfBank(); t++) {
            observeTrack(trackBank.getItemAt(t), t);
        }

        this.selectedTrack = host.createCursorTrack("WIGAI_EVENTS_TRACK", "WigAI events (selection)", 0, 0, true);
        selectedTrack.name().addValueObserver(v -> emit("selection", "selection:track", "track", v));
        this.selectedDevice = selectedTrack.createCursorDevice("WIGAI_EVENTS_DEVICE", "WigAI events device", 0,
            CursorDeviceFollowMode.FOLLOW_SELECTION);
        selectedDevice.name().addValueObserver(v -> emit("selection", "selection:device", "device", v));
        selectedDevice.presetName().addValueObserver(v -> emit("selection", "selection:preset", "preset", v));
        this.selectedPage = selectedDevice.createCursorRemoteControlsPage(Constants.REMOTE_CONTROLS);
        selectedPage.getName().markInterested();
        for (int i = 0; i < selectedPage.getParameterCount(); i++) {
            RemoteControl p = selectedPage.getParameter(i);
            p.name().markInterested();
            int index = i;
            p.displayedValue().addValueObserver(v -> {
                if (!p.name().get().isEmpty()) {
                    Map<String, Object> d = new LinkedHashMap<>();
                    d.put("device", selectedDevice.name().get());
                    d.put("page", selectedPage.getName().get());
                    d.put("index", index);
                    d.put("name", p.name().get());
                    d.put("value", v);
                    emitData("parameter", "parameter:" + index, d);
                }
            });
        }

        host.scheduleTask(() -> {
            live = true;
            logger.info("BitwigEventStream: recording changes");
        }, Constants.SETTLE_AFTER_INIT_MS);
    }

    private void observeTrack(Track track, int t) {
        String k = "track:" + t + ":";
        track.name().addValueObserver(v -> emitTrack(t, k + "name", "name", v));
        track.mute().addValueObserver(v -> emitTrack(t, k + "mute", "mute", v));
        track.solo().addValueObserver(v -> emitTrack(t, k + "solo", "solo", v));
        track.arm().addValueObserver(v -> emitTrack(t, k + "arm", "arm", v));
        track.volume().displayedValue().addValueObserver(v -> emitTrack(t, k + "volume", "volume", v));
        track.pan().displayedValue().addValueObserver(v -> emitTrack(t, k + "pan", "pan", v));
        track.exists().addValueObserver(v -> emitTrack(t, k + "exists", "exists", v));
        track.clipLauncherSlotBank().addPlaybackStateObserver((slot, state, queued) -> {
            Map<String, Object> d = new LinkedHashMap<>();
            d.put("track_index", t);
            d.put("track", track.name().get());
            d.put("slot_index", slot);
            d.put("state", state >= 0 && state < CLIP_STATES.length ? CLIP_STATES[state] : String.valueOf(state));
            d.put("queued", queued);
            emitData("clip", "clip:" + t + ":" + slot, d);
        });
        track.clipLauncherSlotBank().addHasContentObserver((slot, has) -> {
            Map<String, Object> d = new LinkedHashMap<>();
            d.put("track_index", t);
            d.put("track", track.name().get());
            d.put("slot_index", slot);
            d.put("has_clip", has);
            emitData("clip", "clip_content:" + t + ":" + slot, d);
        });
    }

    private void emitTrack(int t, String key, String field, Object value) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("track_index", t);
        d.put("track", trackBank.getItemAt(t).name().get());
        d.put("field", field);
        d.put("value", value);
        emitData("track", key, d);
    }

    private void emit(String type, String key, String field, Object value) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put(field, value);
        emitData(type, key, d);
    }

    private void emitData(String type, String key, Map<String, Object> data) {
        if (live) {
            buffer.add(type, key, data);
        }
    }

    public EventBuffer buffer() {
        return buffer;
    }

    /** What is true right now, for context next to a batch of events. */
    public Map<String, Object> now() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("playing", transport.isPlaying().get());
        m.put("position_beats", transport.getPosition().get());
        m.put("selected_track", selectedTrack.name().get());
        m.put("selected_device", selectedDevice.name().get());
        return m;
    }

    /**
     * Events after {@code since} (types filter optional), waiting up to {@code waitMs} for one. Without
     * {@code since}, nothing is returned and next_seq marks the starting point.
     */
    public Map<String, Object> events(Long since, Collection<String> types, long waitMs) throws InterruptedException {
        long from = since == null ? buffer.latestSeq() : since;
        List<EventBuffer.Event> found = since == null ? List.of() : buffer.since(from, types, waitMs);
        long next = found.isEmpty() ? from : found.get(found.size() - 1).seq();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("events", found.stream().map(EventBuffer.Event::toMap).toList());
        m.put("next_seq", next);
        if (since != null && buffer.missedSince(since)) {
            m.put("missed_events", true);
        }
        m.put("now", now());
        return m;
    }
}
