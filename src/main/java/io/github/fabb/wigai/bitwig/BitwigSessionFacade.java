package io.github.fabb.wigai.bitwig;

import com.bitwig.extension.controller.api.*;
import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.common.error.BitwigApiException;
import io.github.fabb.wigai.common.error.ErrorCode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.function.BooleanSupplier;

/**
 * Facade for listening to and auditioning a project: track levels, cue markers, the arranger loop and
 * time signature, reading and editing launcher clips, playing notes into a track, and scene names.
 * Like {@link BitwigEditFacade}, every Bitwig object is created in the constructor (during init()).
 */
public class BitwigSessionFacade {

    private static final class Constants {
        public static final int METER_RANGE = 1000;
        public static final int CUE_MARKERS = 64;
        public static final int SCENES = 128;
        public static final int CLIP_GRID_WIDTH = 16384;
        public static final int CLIP_GRID_HEIGHT = 128;
        public static final double CLIP_STEP_BEATS = 1.0 / 16.0;
        public static final long POLL_INTERVAL_MS = 20;
        public static final long CLIP_LOAD_MS = 500;

        private Constants() {}
    }

    private final ControllerHost host;
    private final Logger logger;
    private final Transport transport;
    private final TrackBank trackBank;
    private final MasterTrack master;
    private final AtomicIntegerArray peak;
    private final AtomicIntegerArray rms;
    private final int masterSlot;
    private final CueMarkerBank cueMarkers;
    private final SceneBank scenes;
    private final CursorTrack clipTrack;
    private final Clip clip;
    private final Map<Long, NoteStep> steps = new ConcurrentHashMap<>();
    private final Clip arrangerClip;
    private final Map<Long, NoteStep> arrangerSteps = new ConcurrentHashMap<>();
    private final long timeoutMs;

    public BitwigSessionFacade(ControllerHost host, Logger logger, Transport transport, TrackBank trackBank) {
        this(host, logger, transport, trackBank, 2000);
    }

    BitwigSessionFacade(ControllerHost host, Logger logger, Transport transport, TrackBank trackBank, long timeoutMs) {
        this.host = host;
        this.logger = logger;
        this.transport = transport;
        this.trackBank = trackBank;
        this.timeoutMs = timeoutMs;

        int tracks = trackBank.getSizeOfBank();
        this.masterSlot = tracks;
        this.peak = new AtomicIntegerArray(tracks + 1);
        this.rms = new AtomicIntegerArray(tracks + 1);
        for (int t = 0; t < tracks; t++) {
            observeMeter(trackBank.getItemAt(t), t);
            trackBank.getItemAt(t).playingNotes().markInterested();
        }
        this.master = host.createMasterTrack(0);
        master.name().markInterested();
        observeMeter(master, masterSlot);

        Arranger arranger = host.createArranger();
        this.cueMarkers = arranger.createCueMarkerBank(Constants.CUE_MARKERS);
        for (int i = 0; i < cueMarkers.getSizeOfBank(); i++) {
            CueMarker m = cueMarkers.getItemAt(i);
            m.exists().markInterested();
            m.getName().markInterested();
            m.position().markInterested();
        }

        this.scenes = host.createSceneBank(Constants.SCENES);
        for (int i = 0; i < scenes.getSizeOfBank(); i++) {
            scenes.getItemAt(i).exists().markInterested();
            scenes.getItemAt(i).name().markInterested();
        }

        transport.arrangerLoopStart().markInterested();
        transport.arrangerLoopDuration().markInterested();
        transport.timeSignature().markInterested();
        transport.timeSignature().numerator().markInterested();
        transport.timeSignature().denominator().markInterested();

        this.clipTrack = host.createCursorTrack("WIGAI_CLIP_TRACK", "WigAI clip track", 0, 0, false);
        clipTrack.name().markInterested();
        this.clip = clipTrack.createLauncherCursorClip(Constants.CLIP_GRID_WIDTH, Constants.CLIP_GRID_HEIGHT);
        observeClip(clip, steps);
        clip.launchQuantization().markInterested();
        clip.launchMode().markInterested();
        // Follows the clip selected in the arranger; the API cannot select an arranger clip itself.
        this.arrangerClip = host.createArrangerCursorClip(Constants.CLIP_GRID_WIDTH, Constants.CLIP_GRID_HEIGHT);
        observeClip(arrangerClip, arrangerSteps);
        arrangerClip.getTrack().name().markInterested();

        for (int i = 0; i < scenes.getSizeOfBank(); i++) {
            scenes.getItemAt(i).color().markInterested();
            scenes.getItemAt(i).clipCount().markInterested();
        }
    }

    private static void observeClip(Clip c, Map<Long, NoteStep> into) {
        c.exists().markInterested();
        c.getLoopStart().markInterested();
        c.getLoopLength().markInterested();
        c.getPlayStart().markInterested();
        c.getPlayStop().markInterested();
        c.isLoopEnabled().markInterested();
        c.getShuffle().markInterested();
        c.getAccent().markInterested();
        c.getAccent().displayedValue().markInterested();
        c.color().markInterested();
        c.setStepSize(Constants.CLIP_STEP_BEATS);
        c.scrollToKey(0);
        c.scrollToStep(0);
        c.addNoteStepObserver(step -> {
            long key = stepKey(step.channel(), step.x(), step.y());
            if (step.state() == NoteStep.State.NoteOn) {
                into.put(key, step);
            } else {
                into.remove(key);
            }
        });
    }

    /** A clip being worked on: the launcher cursor clip (after focusClip) or the arranger's selected clip. */
    private record ClipView(Clip clip, Map<Long, NoteStep> steps, Map<String, Object> where) {}

    private ClipView view(Integer trackIndex, Integer slotIndex, boolean arranger, String operation) throws BitwigApiException {
        if (arranger) {
            if (!waitFor(() -> arrangerClip.exists().get())) {
                throw new BitwigApiException(ErrorCode.CLIP_NOT_FOUND, operation,
                    "No arranger clip is selected. Click a clip in Bitwig's arranger first.");
            }
            sleep(timeoutMs > 0 ? Constants.CLIP_LOAD_MS : 0);
            Map<String, Object> where = new LinkedHashMap<>();
            where.put("arranger", true);
            where.put("track", arrangerClip.getTrack().name().get());
            return new ClipView(arrangerClip, arrangerSteps, where);
        }
        if (trackIndex == null || slotIndex == null) {
            throw new BitwigApiException(ErrorCode.MISSING_REQUIRED_PARAMETER, operation,
                "Give a track and slot_index, or arranger: true");
        }
        focusClip(trackIndex, slotIndex, operation);
        Map<String, Object> where = new LinkedHashMap<>();
        where.put("track_index", trackIndex);
        where.put("slot_index", slotIndex);
        return new ClipView(clip, steps, where);
    }

    private void observeMeter(Channel channel, int slot) {
        channel.addVuMeterObserver(Constants.METER_RANGE, -1, true, v -> peak.set(slot, v));
        channel.addVuMeterObserver(Constants.METER_RANGE, -1, false, v -> rms.set(slot, v));
    }

    private static long stepKey(int channel, int x, int y) {
        return ((long) channel << 40) | ((long) x << 8) | y;
    }

    private boolean waitFor(BooleanSupplier condition) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (condition.getAsBoolean()) {
                return true;
            }
            sleep(Constants.POLL_INTERVAL_MS);
        }
        return condition.getAsBoolean();
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ========================================
    // Audio engine
    // ========================================

    /**
     * Bitwig runs the audio engine for one open project at a time; a project without it cannot play,
     * meter or audition. Activating it here takes it from whichever project had it.
     */
    public Map<String, Object> setEngineActive(Application application, boolean active) {
        if (active) {
            application.activateEngine();
        } else {
            application.deactivateEngine();
        }
        waitFor(() -> application.hasActiveEngine().get() == active);
        return Map.of("engine_active", application.hasActiveEngine().get());
    }

    // ========================================
    // Levels
    // ========================================

    /**
     * Current meter readings. Values are Bitwig's meter scale normalized to 0-1 (not dB); they are only
     * meaningful while the transport is playing or audio is otherwise flowing.
     */
    public Map<String, Object> getLevels() {
        List<Map<String, Object>> tracks = new ArrayList<>();
        for (int t = 0; t < trackBank.getSizeOfBank(); t++) {
            Track track = trackBank.getItemAt(t);
            if (!track.exists().get() || "Master".equalsIgnoreCase(track.trackType().get())) {
                continue;
            }
            tracks.add(level(t, track.name().get(), t));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("playing", transport.isPlaying().get());
        result.put("scale", "0-1 of Bitwig's meter range (not dB)");
        result.put("master", level(-1, master.name().get(), masterSlot));
        result.put("tracks", tracks);
        return result;
    }

    private Map<String, Object> level(int index, String name, int slot) {
        Map<String, Object> m = new LinkedHashMap<>();
        if (index >= 0) {
            m.put("track_index", index);
        }
        m.put("name", name);
        m.put("peak", Math.round(peak.get(slot) * 1000.0 / Constants.METER_RANGE) / 1000.0);
        m.put("rms", Math.round(rms.get(slot) * 1000.0 / Constants.METER_RANGE) / 1000.0);
        return m;
    }

    // ========================================
    // Cue markers, loop, time signature
    // ========================================

    public List<Map<String, Object>> listCueMarkers() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (int i = 0; i < cueMarkers.getSizeOfBank(); i++) {
            CueMarker m = cueMarkers.getItemAt(i);
            if (!m.exists().get()) {
                continue;
            }
            Map<String, Object> e = new LinkedHashMap<>();
            e.put("index", i);
            e.put("name", m.getName().get());
            e.put("position_beats", m.position().get());
            out.add(e);
        }
        return out;
    }

    public Map<String, Object> goToCueMarker(Integer index, String name, boolean quantized) throws BitwigApiException {
        for (int i = 0; i < cueMarkers.getSizeOfBank(); i++) {
            CueMarker m = cueMarkers.getItemAt(i);
            if (m.exists().get() && ((index != null && index == i) || (name != null && name.equalsIgnoreCase(m.getName().get())))) {
                m.launch(quantized);
                return Map.of("cue_marker", m.getName().get(), "position_beats", m.position().get());
            }
        }
        throw new BitwigApiException(ErrorCode.INVALID_PARAMETER, "goToCueMarker", "No such cue marker");
    }

    public Map<String, Object> setArrangement(Double loopStartBeats, Double loopLengthBeats, String timeSignature) {
        if (loopStartBeats != null) {
            transport.arrangerLoopStart().set(loopStartBeats);
        }
        if (loopLengthBeats != null) {
            transport.arrangerLoopDuration().set(loopLengthBeats);
        }
        if (timeSignature != null) {
            transport.timeSignature().set(timeSignature);
        }
        sleep(timeoutMs > 0 ? 120 : 0);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("loop_start_beats", transport.arrangerLoopStart().get());
        m.put("loop_length_beats", transport.arrangerLoopDuration().get());
        m.put("time_signature", transport.timeSignature().get());
        return m;
    }

    // ========================================
    // Scenes
    // ========================================

    public Map<String, Object> renameScene(int sceneIndex, String name) throws BitwigApiException {
        if (sceneIndex < 0 || sceneIndex >= scenes.getSizeOfBank() || !scenes.getItemAt(sceneIndex).exists().get()) {
            throw new BitwigApiException(ErrorCode.SCENE_NOT_FOUND, "renameScene", "No scene at index " + sceneIndex);
        }
        Scene scene = scenes.getItemAt(sceneIndex);
        scene.name().set(name);
        waitFor(() -> name.equals(scene.name().get()));
        return Map.of("scene_index", sceneIndex, "name", scene.name().get());
    }

    private Scene scene(int index, String operation) throws BitwigApiException {
        if (index < 0 || index >= scenes.getSizeOfBank() || !scenes.getItemAt(index).exists().get()) {
            throw new BitwigApiException(ErrorCode.SCENE_NOT_FOUND, operation, "No scene at index " + index);
        }
        return scenes.getItemAt(index);
    }

    private int sceneCount() {
        int n = 0;
        for (int i = 0; i < scenes.getSizeOfBank(); i++) {
            if (scenes.getItemAt(i).exists().get()) {
                n = i + 1;
            }
        }
        return n;
    }

    public List<Map<String, Object>> sceneSummaries() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (int i = 0; i < sceneCount(); i++) {
            Scene sc = scenes.getItemAt(i);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("scene_index", i);
            m.put("name", sc.name().get());
            m.put("clip_count", sc.clipCount().get());
            out.add(m);
        }
        return out;
    }

    /** Renames and/or colors a scene. */
    public Map<String, Object> setScene(int sceneIndex, String name, String color) throws BitwigApiException {
        Scene sc = scene(sceneIndex, "setScene");
        if (name != null) {
            sc.name().set(name);
            waitFor(() -> name.equals(sc.name().get()));
        }
        if (color != null) {
            int[] rgb = BitwigEditFacade.parseColor(color, "setScene");
            sc.color().set(rgb[0] / 255f, rgb[1] / 255f, rgb[2] / 255f);
            sleep(timeoutMs > 0 ? 100 : 0);
        }
        return Map.of("scene_index", sceneIndex, "name", sc.name().get());
    }

    /** Deletes or duplicates a scene with all its clips. */
    public Map<String, Object> deleteOrDuplicateScene(int sceneIndex, boolean delete) throws BitwigApiException {
        final String operation = delete ? "deleteScene" : "duplicateScene";
        Scene sc = scene(sceneIndex, operation);
        int before = sceneCount();
        if (delete) {
            sc.deleteObject();
            waitFor(() -> sceneCount() < before);
        } else {
            sc.duplicateObject();
            waitFor(() -> sceneCount() > before);
        }
        sleep(timeoutMs > 0 ? 150 : 0);
        return Map.of("scene_count_before", before, "scenes", sceneSummaries());
    }

    /** Copies or moves all clips of a scene onto another scene's row. */
    public Map<String, Object> copyOrMoveScene(int fromIndex, int toIndex, boolean move) throws BitwigApiException {
        final String operation = move ? "moveScene" : "copyScene";
        Scene from = scene(fromIndex, operation);
        Scene to = scene(toIndex, operation);
        if (move) {
            to.replaceInsertionPoint().moveSlotsOrScenes(from);
        } else {
            to.replaceInsertionPoint().copySlotsOrScenes(from);
        }
        int clips = from.clipCount().get();
        waitFor(() -> to.clipCount().get() >= clips);
        sleep(timeoutMs > 0 ? 150 : 0);
        return Map.of("action", move ? "moved" : "copied", "scenes", sceneSummaries());
    }

    // ========================================
    // Tracks: stop, return to arrangement, audition notes
    // ========================================

    public Map<String, Object> stopTrack(int trackIndex, boolean returnToArrangement) {
        Track track = trackBank.getItemAt(trackIndex);
        if (returnToArrangement) {
            track.returnToArrangement();
        } else {
            track.stop();
        }
        return Map.of("track", track.name().get(), "action", returnToArrangement ? "return_to_arrangement" : "stop");
    }

    /** A note to audition: MIDI pitch, velocity, start offset and length in milliseconds. */
    public record AuditionNote(int pitch, int velocity, long startMs, long durationMs) {}

    /**
     * Plays notes into a track's instrument right now, without a clip. Each note is started and stopped
     * on Bitwig's control thread by scheduled tasks, so the call returns immediately.
     */
    public Map<String, Object> playNotes(int trackIndex, List<AuditionNote> notes) {
        Track track = trackBank.getItemAt(trackIndex);
        long end = 0;
        for (AuditionNote n : notes) {
            int pitch = Math.max(0, Math.min(127, n.pitch()));
            int velocity = Math.max(1, Math.min(127, n.velocity()));
            host.scheduleTask(() -> track.startNote(pitch, velocity), Math.max(0, n.startMs()));
            host.scheduleTask(() -> track.stopNote(pitch, 0), Math.max(0, n.startMs()) + Math.max(1, n.durationMs()));
            end = Math.max(end, n.startMs() + n.durationMs());
        }
        return Map.of("track", track.name().get(), "notes", notes.size(), "total_ms", end);
    }

    /** The notes sounding right now on every track that has any (from clips, input or play_notes). */
    public Map<String, Object> playingNotes() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (int t = 0; t < trackBank.getSizeOfBank(); t++) {
            Track track = trackBank.getItemAt(t);
            if (!track.exists().get()) {
                continue;
            }
            PlayingNote[] notes = track.playingNotes().get();
            if (notes == null || notes.length == 0) {
                continue;
            }
            List<Map<String, Object>> list = new ArrayList<>();
            for (PlayingNote n : notes) {
                list.add(Map.of("pitch", n.pitch(), "velocity", n.velocity()));
            }
            out.add(Map.of("track_index", t, "track", track.name().get(), "notes", list));
        }
        return Map.of("playing", transport.isPlaying().get(), "tracks", out);
    }

    /**
     * Sends a MIDI message into a track's instrument: cc (number, value 0-127), pitch_bend (value -8192..8191),
     * program (number), aftertouch (value), or raw (status, number = data1, value = data2). channel is 0-15.
     */
    public Map<String, Object> sendMidi(int trackIndex, String type, int channel, Integer number, Integer value,
                                        Integer status) throws BitwigApiException {
        final String operation = "sendMidi";
        if (channel < 0 || channel > 15) {
            throw new BitwigApiException(ErrorCode.INVALID_RANGE, operation, "channel must be 0-15");
        }
        int[] msg = midiMessage(type, channel, number, value, status, operation);
        Track track = trackBank.getItemAt(trackIndex);
        track.sendMidi(msg[0], msg[1], msg[2]);
        return Map.of("track", track.name().get(), "status", msg[0], "data1", msg[1], "data2", msg[2]);
    }

    static int[] midiMessage(String type, int channel, Integer number, Integer value, Integer status, String operation)
            throws BitwigApiException {
        String t = type == null ? "" : type.toLowerCase(Locale.ROOT);
        return switch (t) {
            case "cc" -> new int[]{0xB0 | channel, data(number, "number", operation), data(value, "value", operation)};
            case "program" -> new int[]{0xC0 | channel, data(number, "number", operation), 0};
            case "aftertouch" -> new int[]{0xD0 | channel, data(value, "value", operation), 0};
            case "pitch_bend" -> {
                if (value == null || value < -8192 || value > 8191) {
                    throw new BitwigApiException(ErrorCode.INVALID_RANGE, operation, "pitch_bend value must be -8192..8191");
                }
                int v = value + 8192;
                yield new int[]{0xE0 | channel, v & 0x7F, (v >> 7) & 0x7F};
            }
            case "raw" -> {
                if (status == null || status < 0x80 || status > 0xEF) {
                    throw new BitwigApiException(ErrorCode.INVALID_RANGE, operation, "raw needs a channel-voice status 0x80-0xEF");
                }
                yield new int[]{status, data(number, "number", operation), value == null ? 0 : data(value, "value", operation)};
            }
            default -> throw new BitwigApiException(ErrorCode.INVALID_PARAMETER, operation,
                "type must be cc, pitch_bend, program, aftertouch or raw");
        };
    }

    private static int data(Integer v, String name, String operation) throws BitwigApiException {
        if (v == null || v < 0 || v > 127) {
            throw new BitwigApiException(ErrorCode.INVALID_RANGE, operation, name + " must be 0-127");
        }
        return v;
    }

    // ========================================
    // Launcher clips: read notes and properties
    // ========================================

    private ClipLauncherSlot focusClip(int trackIndex, int slotIndex, String operation) throws BitwigApiException {
        Track track = trackBank.getItemAt(trackIndex);
        ClipLauncherSlotBank slots = track.clipLauncherSlotBank();
        if (slotIndex < 0 || slotIndex >= slots.getSizeOfBank()) {
            throw new BitwigApiException(ErrorCode.INVALID_RANGE, operation, "slot_index must be 0.." + (slots.getSizeOfBank() - 1));
        }
        ClipLauncherSlot slot = slots.getItemAt(slotIndex);
        if (!slot.hasContent().get()) {
            throw new BitwigApiException(ErrorCode.CLIP_NOT_FOUND, operation, "Slot " + slotIndex + " is empty");
        }
        // The note map is kept current by the step observer, which reports every cell that changes as
        // the cursor clip moves; clearing it here would drop notes delivered by selectChannel already.
        clipTrack.selectChannel(track);
        waitFor(() -> track.name().get().equals(clipTrack.name().get()));
        slot.select();
        if (!waitFor(() -> clip.exists().get())) {
            throw new BitwigApiException(ErrorCode.BITWIG_TIMEOUT, operation, "Could not focus the clip");
        }
        sleep(timeoutMs > 0 ? Constants.CLIP_LOAD_MS : 0);
        return slot;
    }

    /** Reads the notes of a launcher clip (beats from the clip start, on a 1/16-beat grid). */
    public Map<String, Object> readNotes(int trackIndex, int slotIndex) throws BitwigApiException {
        return readNotes(trackIndex, slotIndex, false);
    }

    public Map<String, Object> readNotes(Integer trackIndex, Integer slotIndex, boolean arranger) throws BitwigApiException {
        ClipView v = view(trackIndex, slotIndex, arranger, "readNotes");
        List<NoteStep> found = new ArrayList<>(v.steps().values());
        found.sort(Comparator.comparingInt(NoteStep::x).thenComparingInt(NoteStep::y));
        List<Map<String, Object>> notes = new ArrayList<>();
        for (NoteStep s : found) {
            notes.add(describeNote(s));
        }
        Map<String, Object> result = new LinkedHashMap<>(v.where());
        result.putAll(clipProperties(v.clip()));
        result.put("note_count", notes.size());
        result.put("notes", notes);
        return result;
    }

    /** A note's position and velocity, plus every expression or condition that is not at its default. */
    static Map<String, Object> describeNote(NoteStep s) {
        Map<String, Object> n = new LinkedHashMap<>();
        n.put("start", s.x() * Constants.CLIP_STEP_BEATS);
        n.put("pitch", s.y());
        n.put("velocity", (int) Math.round(s.velocity() * 127));
        n.put("duration", s.duration());
        n.put("channel", s.channel());
        putIf(n, "release_velocity", s.releaseVelocity(), 0.5);
        putIf(n, "velocity_spread", s.velocitySpread(), 0);
        putIf(n, "gain", s.gain(), 0);
        putIf(n, "pan", s.pan(), 0);
        putIf(n, "timbre", s.timbre(), 0);
        putIf(n, "pressure", s.pressure(), 0);
        putIf(n, "transpose", s.transpose(), 0);
        if (s.isChanceEnabled() && s.chance() < 1.0 - 1e-6) {
            n.put("chance", s.chance());
        }
        if (s.isOccurrenceEnabled()) {
            n.put("occurrence", s.occurrence().name().toLowerCase(Locale.ROOT));
        }
        if (s.isRecurrenceEnabled()) {
            n.put("recurrence_length", s.recurrenceLength());
            n.put("recurrence_mask", s.recurrenceMask());
        }
        if (s.isRepeatEnabled()) {
            n.put("repeats", s.repeatCount());
            putIf(n, "repeat_curve", s.repeatCurve(), 0);
            putIf(n, "repeat_velocity_end", s.repeatVelocityEnd(), 0);
            putIf(n, "repeat_velocity_curve", s.repeatVelocityCurve(), 0);
        }
        if (s.isMuted()) {
            n.put("muted", true);
        }
        return n;
    }

    private static void putIf(Map<String, Object> m, String key, double value, double dflt) {
        if (Math.abs(value - dflt) > 1e-6) {
            m.put(key, value);
        }
    }

    public Map<String, Object> setClip(int trackIndex, int slotIndex, String name, Double loopStartBeats,
                                       Double loopLengthBeats) throws BitwigApiException {
        return setClip(trackIndex, slotIndex, false, new ClipSettings(name, loopStartBeats, loopLengthBeats, null,
            null, null, null, null, null));
    }

    /** Clip settings; null fields are left alone. accent is 0-1. */
    public record ClipSettings(String name, Double loopStartBeats, Double loopLengthBeats, Boolean loopEnabled,
                               Double playStartBeats, Double playStopBeats, Boolean shuffle, Double accent,
                               String color) {}

    public Map<String, Object> setClip(Integer trackIndex, Integer slotIndex, boolean arranger, ClipSettings c)
            throws BitwigApiException {
        final String operation = "setClip";
        ClipView v = view(trackIndex, slotIndex, arranger, operation);
        Clip target = v.clip();
        if (c.name() != null) {
            target.setName(c.name());
        }
        if (c.loopStartBeats() != null) {
            target.getLoopStart().set(c.loopStartBeats());
        }
        if (c.loopLengthBeats() != null) {
            target.getLoopLength().set(c.loopLengthBeats());
        }
        if (c.loopEnabled() != null) {
            target.isLoopEnabled().set(c.loopEnabled());
        }
        if (c.playStartBeats() != null) {
            target.getPlayStart().set(c.playStartBeats());
        }
        if (c.playStopBeats() != null) {
            target.getPlayStop().set(c.playStopBeats());
        }
        if (c.shuffle() != null) {
            target.getShuffle().set(c.shuffle());
        }
        if (c.accent() != null) {
            target.getAccent().setImmediately(c.accent());
        }
        if (c.color() != null) {
            int[] rgb = BitwigEditFacade.parseColor(c.color(), operation);
            target.color().set(rgb[0] / 255f, rgb[1] / 255f, rgb[2] / 255f);
        }
        sleep(timeoutMs > 0 ? 150 : 0);
        Map<String, Object> result = new LinkedHashMap<>(v.where());
        result.putAll(clipProperties(target));
        return result;
    }

    /** Which notes to change: optional pitch, and a start range in beats from the clip start. */
    public record NoteFilter(Integer pitch, Double fromBeats, Double toBeats) {
        boolean matches(NoteStep s) {
            double start = s.x() * Constants.CLIP_STEP_BEATS;
            return (pitch == null || s.y() == pitch) && (fromBeats == null || start >= fromBeats - 1e-9)
                && (toBeats == null || start < toBeats - 1e-9);
        }
    }

    /** Per-note changes; null fields are left alone. Ranges follow Bitwig's NoteStep API. */
    public record NoteChanges(Double velocity, Double duration, Double gain, Double pan, Double timbre,
                              Double pressure, Double transpose, Double chance, Boolean muted, Integer repeats,
                              Double releaseVelocity, Double velocitySpread, String occurrence,
                              Integer recurrenceLength, Integer recurrenceMask, Double repeatCurve,
                              Double repeatVelocityEnd, Double repeatVelocityCurve) {
        public NoteChanges(Double velocity, Double duration, Double gain, Double pan, Double timbre, Double pressure,
                           Double transpose, Double chance, Boolean muted, Integer repeats) {
            this(velocity, duration, gain, pan, timbre, pressure, transpose, chance, muted, repeats,
                null, null, null, null, null, null, null, null);
        }
    }

    /** Parses an occurrence condition name; "off" (or "always") disables the condition. */
    static NoteOccurrence occurrence(String name, String operation) throws BitwigApiException {
        String key = name.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        for (NoteOccurrence o : NoteOccurrence.values()) {
            if (o.name().equals(key)) {
                return o;
            }
        }
        throw new BitwigApiException(ErrorCode.INVALID_PARAMETER, operation,
            "occurrence must be off, always, first, not_first, prev, not_prev, prev_channel or not_prev_channel");
    }

    public Map<String, Object> editNotes(int trackIndex, int slotIndex, NoteFilter filter, NoteChanges c)
            throws BitwigApiException {
        return editNotes(trackIndex, slotIndex, false, filter, c);
    }

    public Map<String, Object> editNotes(Integer trackIndex, Integer slotIndex, boolean arranger, NoteFilter filter,
                                         NoteChanges c) throws BitwigApiException {
        final String operation = "editNotes";
        boolean occurrenceOff = c.occurrence() != null
            && List.of("off", "none", "always").contains(c.occurrence().trim().toLowerCase(Locale.ROOT));
        NoteOccurrence occ = c.occurrence() == null || occurrenceOff ? null : occurrence(c.occurrence(), operation);
        if ((c.recurrenceLength() == null) != (c.recurrenceMask() == null) && !Integer.valueOf(0).equals(c.recurrenceLength())) {
            throw new BitwigApiException(ErrorCode.INVALID_PARAMETER, operation,
                "recurrence_length and recurrence_mask go together (length 0 turns recurrence off)");
        }
        ClipView v = view(trackIndex, slotIndex, arranger, operation);
        int changed = 0;
        for (NoteStep s : new ArrayList<>(v.steps().values())) {
            if (!filter.matches(s)) {
                continue;
            }
            if (c.velocity() != null) {
                s.setVelocity(c.velocity());
            }
            if (c.duration() != null) {
                s.setDuration(c.duration());
            }
            if (c.gain() != null) {
                s.setGain(c.gain());
            }
            if (c.pan() != null) {
                s.setPan(c.pan());
            }
            if (c.timbre() != null) {
                s.setTimbre(c.timbre());
            }
            if (c.pressure() != null) {
                s.setPressure(c.pressure());
            }
            if (c.transpose() != null) {
                s.setTranspose(c.transpose());
            }
            if (c.chance() != null) {
                s.setIsChanceEnabled(c.chance() < 1.0);
                s.setChance(c.chance());
            }
            if (c.muted() != null) {
                s.setIsMuted(c.muted());
            }
            if (c.repeats() != null) {
                s.setIsRepeatEnabled(c.repeats() > 0);
                s.setRepeatCount(Math.max(0, c.repeats()));
            }
            if (c.releaseVelocity() != null) {
                s.setReleaseVelocity(c.releaseVelocity());
            }
            if (c.velocitySpread() != null) {
                s.setVelocitySpread(c.velocitySpread());
            }
            if (occurrenceOff) {
                s.setIsOccurrenceEnabled(false);
            } else if (occ != null) {
                s.setOccurrence(occ);
                s.setIsOccurrenceEnabled(true);
            }
            if (c.recurrenceLength() != null) {
                if (c.recurrenceLength() <= 0) {
                    s.setIsRecurrenceEnabled(false);
                } else {
                    s.setRecurrence(c.recurrenceLength(), c.recurrenceMask());
                    s.setIsRecurrenceEnabled(true);
                }
            }
            if (c.repeatCurve() != null) {
                s.setRepeatCurve(c.repeatCurve());
            }
            if (c.repeatVelocityEnd() != null) {
                s.setRepeatVelocityEnd(c.repeatVelocityEnd());
            }
            if (c.repeatVelocityCurve() != null) {
                s.setRepeatVelocityCurve(c.repeatVelocityCurve());
            }
            changed++;
        }
        sleep(timeoutMs > 0 ? 200 : 0);
        Map<String, Object> result = new LinkedHashMap<>(v.where());
        result.put("notes_changed", changed);
        return result;
    }

    /** Writes notes into the arranger's selected clip (launcher clips use write_notes on the edit facade). */
    public Map<String, Object> writeArrangerNotes(List<BitwigEditFacade.Note> notes, boolean clearFirst)
            throws BitwigApiException {
        ClipView v = view(null, null, true, "writeNotes");
        if (clearFirst) {
            v.clip().clearSteps();
        }
        int written = 0;
        for (BitwigEditFacade.Note n : notes) {
            int step = (int) Math.round(n.startBeats() / Constants.CLIP_STEP_BEATS);
            if (step < 0 || step >= Constants.CLIP_GRID_WIDTH || n.pitch() < 0 || n.pitch() > 127) {
                continue;
            }
            v.clip().setStep(step, n.pitch(), Math.max(1, Math.min(127, n.velocity())), n.durationBeats());
            written++;
        }
        Map<String, Object> result = new LinkedHashMap<>(v.where());
        result.put("notes_written", written);
        return result;
    }

    /** Whole-clip operations: transpose by semitones, quantize (0-1 strength), duplicate content, launch settings. */
    public Map<String, Object> transformClip(int trackIndex, int slotIndex, Integer transpose, Double quantize,
                                             Boolean duplicateContent, String launchQuantization, String launchMode)
            throws BitwigApiException {
        return transformClip(trackIndex, slotIndex, false, transpose, quantize, duplicateContent, launchQuantization,
            launchMode);
    }

    public Map<String, Object> transformClip(Integer trackIndex, Integer slotIndex, boolean arranger, Integer transpose,
                                             Double quantize, Boolean duplicateContent, String launchQuantization,
                                             String launchMode) throws BitwigApiException {
        ClipView v = view(trackIndex, slotIndex, arranger, "transformClip");
        Clip clip = v.clip();
        if (transpose != null) {
            clip.transpose(transpose);
        }
        if (quantize != null) {
            clip.quantize(quantize);
        }
        if (Boolean.TRUE.equals(duplicateContent)) {
            clip.duplicateContent();
        }
        if (launchQuantization != null) {
            clip.launchQuantization().set(launchQuantization);
        }
        if (launchMode != null) {
            clip.launchMode().set(launchMode);
        }
        sleep(timeoutMs > 0 ? 300 : 0);
        Map<String, Object> result = new LinkedHashMap<>(v.where());
        result.putAll(clipProperties(clip));
        if (!arranger) {
            result.put("launch_quantization", clip.launchQuantization().get());
            result.put("launch_mode", clip.launchMode().get());
        }
        return result;
    }

    private static Map<String, Object> clipProperties(Clip c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("loop_enabled", c.isLoopEnabled().get());
        m.put("loop_start_beats", c.getLoopStart().get());
        m.put("loop_length_beats", c.getLoopLength().get());
        m.put("play_start_beats", c.getPlayStart().get());
        m.put("play_stop_beats", c.getPlayStop().get());
        m.put("shuffle", c.getShuffle().get());
        m.put("accent", c.getAccent().get());
        m.put("accent_display", c.getAccent().displayedValue().get());
        return m;
    }
}
