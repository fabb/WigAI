package io.github.fabb.wigai.bitwig;

import com.bitwig.extension.controller.api.*;
import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.common.error.BitwigApiException;
import io.github.fabb.wigai.common.error.ErrorCode;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Project-wide operations: solo/mute/arm resets, scenes, groove, transport settings beyond play/stop,
 * and copying, moving or duplicating launcher clips. Bitwig objects are created in
 * the constructor (during init()).
 */
public class BitwigProjectFacade {

    private final Logger logger;
    private final Project project;
    private final Groove groove;
    private final Transport transport;
    private final TrackBank trackBank;
    private final BitwigEditFacade edit;

    public BitwigProjectFacade(ControllerHost host, Logger logger, Transport transport, TrackBank trackBank,
                               BitwigEditFacade edit) {
        this.logger = logger;
        this.transport = transport;
        this.trackBank = trackBank;
        this.edit = edit;
        this.project = host.getProject();
        this.groove = host.createGroove();
        project.hasSoloedTracks().markInterested();
        project.hasMutedTracks().markInterested();
        project.hasArmedTracks().markInterested();
        project.isModified().markInterested();
        project.cueVolume().value().markInterested();
        project.cueVolume().displayedValue().markInterested();
        project.cueMix().value().markInterested();
        project.cueMix().displayedValue().markInterested();
        for (Parameter p : new Parameter[]{groove.getEnabled(), groove.getShuffleAmount(), groove.getShuffleRate(),
                groove.getAccentAmount(), groove.getAccentRate(), groove.getAccentPhase()}) {
            p.value().markInterested();
            p.displayedValue().markInterested();
            p.name().markInterested();
        }
        transport.isPunchInEnabled().markInterested();
        transport.isPunchOutEnabled().markInterested();
        transport.metronomeVolume().markInterested();
        transport.metronomeVolume().displayedValue().markInterested();
        transport.preRoll().markInterested();
        transport.automationWriteMode().markInterested();
        transport.isClipLauncherOverdubEnabled().markInterested();
        transport.defaultLaunchQuantization().markInterested();
        transport.clipLauncherPostRecordingAction().markInterested();
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ========================================
    // Project-wide state
    // ========================================

    public Map<String, Object> projectState() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("has_soloed_tracks", project.hasSoloedTracks().get());
        m.put("has_muted_tracks", project.hasMutedTracks().get());
        m.put("has_armed_tracks", project.hasArmedTracks().get());
        m.put("modified", project.isModified().get());
        m.put("cue_volume", project.cueVolume().displayedValue().get());
        m.put("cue_mix", project.cueMix().displayedValue().get());
        m.put("cue_volume_raw", project.cueVolume().value().get());
        m.put("cue_mix_raw", project.cueMix().value().get());
        return m;
    }

    /** Clears every solo, mute and/or arm in the project; a stray solo silences everything else. */
    public Map<String, Object> resetProject(boolean unsolo, boolean unmute, boolean unarm) {
        if (unsolo) {
            project.unsoloAll();
        }
        if (unmute) {
            project.unmuteAll();
        }
        if (unarm) {
            project.unarmAll();
        }
        sleep(200);
        return projectState();
    }

    public Map<String, Object> createScene() {
        project.createScene();
        sleep(200);
        return Map.of("action", "scene_created");
    }

    /** Cue (headphone) bus: volume and the cue/master mix, both 0-1. */
    public Map<String, Object> setCue(Double volume, Double mix) {
        if (volume != null) {
            project.cueVolume().value().setImmediately(volume);
        }
        if (mix != null) {
            project.cueMix().value().setImmediately(mix);
        }
        sleep(150);
        return projectState();
    }

    // ========================================
    // Groove
    // ========================================

    public Map<String, Object> setGroove(Boolean enabled, Double shuffleAmount, Double shuffleRate, Double accentAmount,
                                         Double accentRate, Double accentPhase) {
        if (enabled != null) {
            groove.getEnabled().value().setImmediately(enabled ? 1.0 : 0.0);
        }
        set(groove.getShuffleAmount(), shuffleAmount);
        set(groove.getShuffleRate(), shuffleRate);
        set(groove.getAccentAmount(), accentAmount);
        set(groove.getAccentRate(), accentRate);
        set(groove.getAccentPhase(), accentPhase);
        sleep(150);
        Map<String, Object> m = new LinkedHashMap<>();
        for (Parameter p : new Parameter[]{groove.getEnabled(), groove.getShuffleAmount(), groove.getShuffleRate(),
                groove.getAccentAmount(), groove.getAccentRate(), groove.getAccentPhase()}) {
            m.put(p.name().get(), Map.of("value", p.value().get(), "display", p.displayedValue().get()));
        }
        return m;
    }

    private static void set(Parameter p, Double v) {
        if (v != null) {
            p.value().setImmediately(v);
        }
    }

    // ========================================
    // Transport settings
    // ========================================

    public Map<String, Object> setTransportOptions(Boolean punchIn, Boolean punchOut, Double metronomeVolume,
                                                   String preRoll, String automationWriteMode, Boolean launcherOverdub,
                                                   String defaultLaunchQuantization, String postRecordingAction,
                                                   String command) throws BitwigApiException {
        if (punchIn != null) {
            transport.isPunchInEnabled().set(punchIn);
        }
        if (punchOut != null) {
            transport.isPunchOutEnabled().set(punchOut);
        }
        if (metronomeVolume != null) {
            transport.metronomeVolume().setImmediately(metronomeVolume);
        }
        if (preRoll != null) {
            transport.preRoll().set(preRoll);
        }
        if (automationWriteMode != null) {
            transport.automationWriteMode().set(automationWriteMode);
        }
        if (launcherOverdub != null) {
            transport.isClipLauncherOverdubEnabled().set(launcherOverdub);
        }
        if (defaultLaunchQuantization != null) {
            transport.defaultLaunchQuantization().set(defaultLaunchQuantization);
        }
        if (postRecordingAction != null) {
            transport.clipLauncherPostRecordingAction().set(postRecordingAction);
        }
        if (command != null) {
            switch (command.toLowerCase(Locale.ROOT)) {
                case "rewind" -> transport.rewind();
                case "fast_forward" -> transport.fastForward();
                case "tap_tempo" -> transport.tapTempo();
                case "continue" -> transport.continuePlayback();
                case "record" -> transport.record();
                default -> throw new BitwigApiException(ErrorCode.INVALID_PARAMETER, "setTransportOptions",
                    "command must be rewind, fast_forward, tap_tempo, continue or record");
            }
        }
        sleep(150);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("punch_in", transport.isPunchInEnabled().get());
        m.put("punch_out", transport.isPunchOutEnabled().get());
        m.put("metronome_volume", transport.metronomeVolume().displayedValue().get());
        m.put("pre_roll", transport.preRoll().get());
        m.put("automation_write_mode", transport.automationWriteMode().get());
        m.put("launcher_overdub", transport.isClipLauncherOverdubEnabled().get());
        m.put("default_launch_quantization", transport.defaultLaunchQuantization().get());
        m.put("post_recording_action", transport.clipLauncherPostRecordingAction().get());
        return m;
    }

    // ========================================
    // Launcher clips: duplicate, copy, move, record
    // ========================================

    private ClipLauncherSlot slot(int trackIndex, int slotIndex, String operation) throws BitwigApiException {
        ClipLauncherSlotBank slots = trackBank.getItemAt(trackIndex).clipLauncherSlotBank();
        if (slotIndex < 0 || slotIndex >= slots.getSizeOfBank()) {
            throw new BitwigApiException(ErrorCode.INVALID_RANGE, operation, "slot_index must be 0.." + (slots.getSizeOfBank() - 1));
        }
        return slots.getItemAt(slotIndex);
    }

    public Map<String, Object> copyOrMoveClip(int fromTrack, int fromSlot, int toTrack, int toSlot, boolean move)
            throws BitwigApiException {
        final String operation = move ? "moveClip" : "copyClip";
        ClipLauncherSlot source = slot(fromTrack, fromSlot, operation);
        ClipLauncherSlot target = slot(toTrack, toSlot, operation);
        if (!source.hasContent().get()) {
            throw new BitwigApiException(ErrorCode.CLIP_NOT_FOUND, operation, "Source slot is empty");
        }
        if (target.hasContent().get()) {
            throw new BitwigApiException(ErrorCode.INVALID_PARAMETER, operation, "Target slot already has a clip");
        }
        if (move) {
            target.replaceInsertionPoint().moveSlotsOrScenes(source);
        } else {
            target.replaceInsertionPoint().copySlotsOrScenes(source);
        }
        edit.waitFor(() -> target.hasContent().get());
        return Map.of("action", move ? "moved" : "copied", "target_has_clip", target.hasContent().get(),
            "source_has_clip", source.hasContent().get());
    }

    public Map<String, Object> duplicateClip(int trackIndex, int slotIndex) throws BitwigApiException {
        ClipLauncherSlot s = slot(trackIndex, slotIndex, "duplicateClip");
        if (!s.hasContent().get()) {
            throw new BitwigApiException(ErrorCode.CLIP_NOT_FOUND, "duplicateClip", "Slot is empty");
        }
        s.duplicateClip();
        sleep(300);
        return Map.of("action", "duplicated", "note", "Bitwig places the copy in the next free slot below");
    }

    public Map<String, Object> recordIntoSlot(int trackIndex, int slotIndex) throws BitwigApiException {
        ClipLauncherSlot s = slot(trackIndex, slotIndex, "recordIntoSlot");
        s.record();
        return Map.of("action", "recording_queued", "note", "The track must be armed; stop with stop_track or transport_stop");
    }
}
