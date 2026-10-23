package io.github.fabb.wigai.bitwig;

import com.bitwig.extension.controller.api.*;
import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.common.error.BitwigApiException;
import io.github.fabb.wigai.common.error.ErrorCode;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.BooleanSupplier;

/**
 * Facade for operations that change the project: creating tracks, inserting and editing devices,
 * mixer settings, clips, notes and application actions.
 *
 * All Bitwig objects are created in the constructor (Bitwig only allows that during init()).
 * Bitwig applies changes asynchronously, so mutating methods poll for the result before returning.
 * Device parameters are reached through a private cursor track and cursor device that do not
 * follow the user's selection, so reading or editing a device never moves the user's focus.
 */
public class BitwigEditFacade {

    private static final class Constants {
        public static final int REMOTE_CONTROL_COUNT = 8;
        public static final int CLIP_GRID_WIDTH = 16384;
        public static final int CLIP_GRID_HEIGHT = 128;
        public static final double CLIP_STEP_BEATS = 1.0 / 16.0; // 64th-note resolution
        public static final long POLL_INTERVAL_MS = 20;
        public static final long DEFAULT_TIMEOUT_MS = 2000;
        public static final long SETTLE_MS = 120;
        public static final String DEVICE_LIST_RESOURCE = "/bitwig-devices.txt";
        public static final int CHAIN_DEVICES = 32;
        public static final int LAYERS = 16;

        private Constants() {}
    }

    private final ControllerHost host;
    private final Application application;
    private final Transport transport;
    private final Logger logger;
    private final TrackBank trackBank;
    private final List<DeviceBank> trackDeviceBanks;
    private final CursorTrack editTrack;
    private final PinnableCursorDevice editDevice;
    private final ChainCursor primary;
    private final ChainCursor secondary;
    private final CursorRemoteControlsPage editPage;
    private final Clip editClip;
    private final Map<String, BitwigDevice> bitwigDevices;
    private final long timeoutMs;

    public BitwigEditFacade(ControllerHost host, Logger logger, Application application, Transport transport,
                            TrackBank trackBank, List<DeviceBank> trackDeviceBanks) {
        this(host, logger, application, transport, trackBank, trackDeviceBanks, Constants.DEFAULT_TIMEOUT_MS);
    }

    BitwigEditFacade(ControllerHost host, Logger logger, Application application, Transport transport,
                     TrackBank trackBank, List<DeviceBank> trackDeviceBanks, long timeoutMs) {
        this.host = host;
        this.logger = logger;
        this.application = application;
        this.transport = transport;
        this.trackBank = trackBank;
        this.trackDeviceBanks = trackDeviceBanks;
        this.timeoutMs = timeoutMs;
        this.bitwigDevices = loadBitwigDevices();

        this.editTrack = host.createCursorTrack("WIGAI_EDIT_TRACK", "WigAI edit track", 0, 0, false);
        this.editDevice = editTrack.createCursorDevice("WIGAI_EDIT_DEVICE", "WigAI edit device", 0,
            CursorDeviceFollowMode.FIRST_DEVICE);
        this.editPage = editDevice.createCursorRemoteControlsPage(Constants.REMOTE_CONTROL_COUNT);
        this.editClip = editTrack.createLauncherCursorClip(Constants.CLIP_GRID_WIDTH, Constants.CLIP_GRID_HEIGHT);
        this.primary = new ChainCursor(editDevice);
        this.secondary = new ChainCursor(editTrack.createCursorDevice("WIGAI_EDIT_DEVICE_2", "WigAI edit device 2", 0,
            CursorDeviceFollowMode.FIRST_DEVICE));

        application.panelLayout().markInterested();
        editTrack.position().markInterested();
        editTrack.name().markInterested();
        editDevice.exists().markInterested();
        editDevice.name().markInterested();
        editDevice.position().markInterested();
        editPage.pageNames().markInterested();
        editPage.selectedPageIndex().markInterested();
        for (int i = 0; i < editPage.getParameterCount(); i++) {
            RemoteControl p = editPage.getParameter(i);
            p.exists().markInterested();
            p.name().markInterested();
            p.value().markInterested();
            p.displayedValue().markInterested();
            p.modulatedValue().markInterested();
            p.modulatedValue().displayedValue().markInterested();
        }
        editClip.exists().markInterested();
        editClip.getLoopLength().markInterested();
        editClip.setStepSize(Constants.CLIP_STEP_BEATS);
        editClip.scrollToKey(0);
        editClip.scrollToStep(0);

        for (int t = 0; t < trackBank.getSizeOfBank(); t++) {
            Track track = trackBank.getItemAt(t);
            track.position().markInterested();
            track.trackType().markInterested();
            track.color().markInterested();
            track.isActivated().markInterested();
            track.solo().markInterested();
            track.arm().markInterested();
            track.mute().markInterested();
            track.volume().value().markInterested();
            track.volume().displayedValue().markInterested();
            track.pan().value().markInterested();
            track.pan().displayedValue().markInterested();
            track.monitorMode().markInterested();
            track.isGroup().markInterested();
            track.isGroupExpanded().markInterested();
            track.crossFadeMode().markInterested();
            SendBank sends = sendBankOrNull(track);
            for (int i = 0; sends != null && i < sends.getSizeOfBank(); i++) {
                Send send = sends.getItemAt(i);
                send.exists().markInterested();
                send.name().markInterested();
                send.value().markInterested();
                send.displayedValue().markInterested();
                send.isEnabled().markInterested();
            }
            ClipLauncherSlotBank slots = track.clipLauncherSlotBank();
            for (int s = 0; s < slots.getSizeOfBank(); s++) {
                slots.getItemAt(s).hasContent().markInterested();
            }
            DeviceBank bank = trackDeviceBanks.get(t);
            for (int d = 0; d < bank.getSizeOfBank(); d++) {
                bank.getItemAt(d).position().markInterested();
                bank.getItemAt(d).presetName().markInterested();
            }
        }
        transport.tempo().value().markInterested();
        transport.tempo().displayedValue().markInterested();
        transport.getPosition().markInterested();
        transport.isArrangerLoopEnabled().markInterested();
        transport.isMetronomeEnabled().markInterested();
        transport.isArrangerRecordEnabled().markInterested();
        transport.isArrangerOverdubEnabled().markInterested();
        transport.isAutomationOverrideActive().markInterested();
        transport.timeSignature().numerator().markInterested();
        transport.timeSignature().denominator().markInterested();
    }

    // ========================================
    // Lookup helpers
    // ========================================

    /** Resolves a track by index or exact name to its index in the flat track bank. */
    public int resolveTrackIndex(Integer trackIndex, String trackName, String operation) throws BitwigApiException {
        if (trackIndex != null) {
            if (trackIndex < 0 || trackIndex >= trackBank.getSizeOfBank() || !trackBank.getItemAt(trackIndex).exists().get()) {
                throw new BitwigApiException(ErrorCode.TRACK_NOT_FOUND, operation, "No track at index " + trackIndex);
            }
            return trackIndex;
        }
        if (trackName != null) {
            for (int i = 0; i < trackBank.getSizeOfBank(); i++) {
                Track t = trackBank.getItemAt(i);
                if (t.exists().get() && trackName.equals(t.name().get())) {
                    return i;
                }
            }
            throw new BitwigApiException(ErrorCode.TRACK_NOT_FOUND, operation, "No track named '" + trackName + "'");
        }
        throw new BitwigApiException(ErrorCode.MISSING_REQUIRED_PARAMETER, operation, "Provide track_index or track_name");
    }

    private Device resolveDevice(int trackIndex, int deviceIndex, String operation) throws BitwigApiException {
        DeviceBank bank = trackDeviceBanks.get(trackIndex);
        if (deviceIndex < 0 || deviceIndex >= bank.getSizeOfBank() || !bank.getItemAt(deviceIndex).exists().get()) {
            throw new BitwigApiException(ErrorCode.DEVICE_NOT_FOUND, operation,
                "No device at index " + deviceIndex + " on track " + trackIndex);
        }
        return bank.getItemAt(deviceIndex);
    }

    /**
     * Number of regular tracks. The flat track list holds regular tracks first, then effect tracks,
     * then the master track; only regular tracks can be inserted between.
     */
    public int countTracks() {
        return countTracksOfKind(false);
    }

    public int countEffectTracks() {
        return countTracksOfKind(true);
    }

    private int countTracksOfKind(boolean effect) {
        int n = 0;
        for (int i = 0; i < trackBank.getSizeOfBank(); i++) {
            Track t = trackBank.getItemAt(i);
            if (t.exists().get() && !isMaster(t) && isEffect(t) == effect) {
                n++;
            }
        }
        return n;
    }

    static boolean isMaster(Track track) {
        return "Master".equalsIgnoreCase(track.trackType().get());
    }

    static boolean isEffect(Track track) {
        return "Effect".equalsIgnoreCase(track.trackType().get());
    }

    public int countDevices(int trackIndex) {
        DeviceBank bank = trackDeviceBanks.get(trackIndex);
        int n = 0;
        for (int i = 0; i < bank.getSizeOfBank(); i++) {
            if (bank.getItemAt(i).exists().get()) {
                n = i + 1;
            }
        }
        return n;
    }

    private List<Map<String, Object>> deviceSummaries(int trackIndex) {
        List<Map<String, Object>> out = new ArrayList<>();
        DeviceBank bank = trackDeviceBanks.get(trackIndex);
        for (int i = 0; i < bank.getSizeOfBank(); i++) {
            Device d = bank.getItemAt(i);
            if (!d.exists().get()) {
                continue;
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("index", i);
            m.put("name", d.name().get());
            m.put("enabled", d.isEnabled().get());
            putPreset(m, d);
            out.add(m);
        }
        return out;
    }

    private static void putPreset(Map<String, Object> m, Device d) {
        String preset = d.presetName().get();
        if (preset != null && !preset.isEmpty() && !"init".equals(preset)) {
            m.put("preset", preset);
        }
    }

    boolean waitFor(BooleanSupplier condition) {
        return waitFor(condition, timeoutMs);
    }

    boolean waitFor(BooleanSupplier condition, long limitMs) {
        long deadline = System.currentTimeMillis() + Math.min(limitMs, timeoutMs);
        while (System.currentTimeMillis() < deadline) {
            if (condition.getAsBoolean()) {
                return true;
            }
            sleep(Constants.POLL_INTERVAL_MS);
        }
        return condition.getAsBoolean();
    }

    void settle() {
        if (timeoutMs > 0) {
            sleep(Constants.SETTLE_MS);
        }
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ========================================
    // Tracks
    // ========================================

    public Map<String, Object> createTrack(String type, Integer position, String name) throws BitwigApiException {
        final String operation = "createTrack";
        boolean effect = "effect".equals(type);
        int regular = countTracks();
        int effects = countEffectTracks();
        int before = effect ? effects : regular;
        int pos = position == null ? before : position;
        if (pos < 0 || pos > before) {
            throw new BitwigApiException(ErrorCode.INVALID_RANGE, operation,
                "position must be 0.." + before + (effect ? " (among effect tracks)" : ""));
        }
        switch (type) {
            case "audio" -> application.createAudioTrack(pos);
            case "instrument" -> application.createInstrumentTrack(pos);
            case "effect" -> application.createEffectTrack(pos);
            default -> throw new BitwigApiException(ErrorCode.INVALID_PARAMETER, operation,
                "type must be audio, instrument or effect");
        }
        if (!waitFor(() -> (effect ? countEffectTracks() : countTracks()) > before)) {
            throw new BitwigApiException(ErrorCode.BITWIG_TIMEOUT, operation, "Track was not created in time");
        }
        int flatIndex = effect ? countTracks() + pos : pos;
        Track created = trackBank.getItemAt(flatIndex);
        String expectedType = effect ? "Effect" : type;
        if (!created.exists().get() || isMaster(created) || !expectedType.equalsIgnoreCase(created.trackType().get())) {
            throw new BitwigApiException(ErrorCode.OPERATION_FAILED, operation,
                "Track was created but could not be located at index " + flatIndex + "; not renaming anything");
        }
        if (name != null) {
            created.name().set(name);
            waitFor(() -> name.equals(created.name().get()));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", type);
        result.put("track_index", flatIndex);
        result.put("name", created.name().get());
        return result;
    }

    public Map<String, Object> deleteTrack(int trackIndex) throws BitwigApiException {
        Track track = trackBank.getItemAt(trackIndex);
        if (isMaster(track)) {
            throw new BitwigApiException(ErrorCode.INVALID_PARAMETER, "deleteTrack", "Refusing to delete the master track");
        }
        String name = track.name().get();
        int before = countTracks();
        track.deleteObject();
        waitFor(() -> countTracks() < before);
        return Map.of("deleted_track", name, "track_index", trackIndex);
    }

    public Map<String, Object> duplicateTrack(int trackIndex) throws BitwigApiException {
        Track track = trackBank.getItemAt(trackIndex);
        if (isMaster(track)) {
            throw new BitwigApiException(ErrorCode.INVALID_PARAMETER, "duplicateTrack", "The master track cannot be duplicated");
        }
        int before = countTracks();
        track.duplicate();
        waitFor(() -> countTracks() > before);
        return Map.of("duplicated_track", track.name().get(), "new_track_index", trackIndex + 1);
    }

    /**
     * Applies any non-null mixer/track setting. volumeDb is matched against Bitwig's displayed dB value,
     * so it is exact regardless of the fader curve.
     */
    public Map<String, Object> setTrackProperties(int trackIndex, String name, Double volume, Double volumeDb, Double pan,
                                                  Boolean mute, Boolean solo, Boolean arm, Boolean activated,
                                                  String monitorMode, String color) throws BitwigApiException {
        return setTrackProperties(trackIndex, name, volume, volumeDb, pan, mute, solo, arm, activated, monitorMode, color,
            null, null);
    }

    public Map<String, Object> setTrackProperties(int trackIndex, String name, Double volume, Double volumeDb, Double pan,
                                                  Boolean mute, Boolean solo, Boolean arm, Boolean activated,
                                                  String monitorMode, String color, Boolean groupExpanded,
                                                  String crossfadeMode) throws BitwigApiException {
        final String operation = "setTrackProperties";
        Track track = trackBank.getItemAt(trackIndex);
        if (groupExpanded != null) {
            if (!track.isGroup().get()) {
                throw new BitwigApiException(ErrorCode.INVALID_PARAMETER, operation, "Track is not a group");
            }
            track.isGroupExpanded().set(groupExpanded);
        }
        if (crossfadeMode != null) {
            track.crossFadeMode().set(crossfadeMode.toUpperCase(Locale.ROOT));
        }
        if (name != null) {
            track.name().set(name);
        }
        if (volume != null) {
            track.volume().value().setImmediately(volume);
        }
        if (volumeDb != null) {
            setVolumeDb(track.volume(), volumeDb);
        }
        if (pan != null) {
            track.pan().value().setImmediately((pan + 1.0) / 2.0);
        }
        if (mute != null) {
            track.mute().set(mute);
        }
        if (solo != null) {
            track.solo().set(solo);
        }
        if (arm != null) {
            track.arm().set(arm);
        }
        if (activated != null) {
            track.isActivated().set(activated);
        }
        if (monitorMode != null) {
            track.monitorMode().set(monitorMode.toUpperCase(Locale.ROOT));
        }
        if (color != null) {
            int[] rgb = parseColor(color, operation);
            track.color().set(rgb[0] / 255f, rgb[1] / 255f, rgb[2] / 255f);
        }
        settle();
        return trackMixerState(trackIndex);
    }

    /** Sets a volume parameter (track, drum pad or layer) to a dB value by searching against its display. */
    void setVolumeDb(Parameter vol, double targetDb) {
        double lo = 0.0;
        double hi = 1.0;
        double best = 0.5;
        double bestError = Double.MAX_VALUE;
        for (int i = 0; i < 16; i++) {
            double mid = (lo + hi) / 2;
            String before = vol.displayedValue().get();
            vol.value().setImmediately(mid);
            waitFor(() -> Math.abs(vol.value().get() - mid) < 1e-6);
            waitForChange(() -> vol.displayedValue().get(), before);
            Double shown = parseDb(vol.displayedValue().get());
            if (shown != null && Math.abs(shown - targetDb) < bestError) {
                bestError = Math.abs(shown - targetDb);
                best = mid;
            }
            if (shown == null || shown < targetDb) {
                lo = mid;
            } else {
                hi = mid;
            }
        }
        // The display is rounded, so keep the position whose displayed value was closest to the target.
        double finalValue = best;
        vol.value().setImmediately(finalValue);
        waitFor(() -> Math.abs(vol.value().get() - finalValue) < 1e-6);
    }

    /** Waits briefly for a displayed string to change; displays update after the raw value. */
    private void waitForChange(java.util.function.Supplier<String> display, String before) {
        if (timeoutMs <= 0) {
            return;
        }
        long deadline = System.currentTimeMillis() + 300;
        while (System.currentTimeMillis() < deadline && java.util.Objects.equals(display.get(), before)) {
            sleep(Constants.POLL_INTERVAL_MS);
        }
        sleep(Constants.POLL_INTERVAL_MS);
    }

    static Double parseDb(String display) {
        if (display == null) {
            return null;
        }
        String s = display.replace("dB", "").replace('−', '-').trim();
        if (s.toLowerCase(Locale.ROOT).contains("inf")) {
            return Double.NEGATIVE_INFINITY;
        }
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    static int[] parseColor(String color, String operation) throws BitwigApiException {
        String hex = color.startsWith("#") ? color.substring(1) : color;
        if (hex.length() != 6) {
            throw new BitwigApiException(ErrorCode.INVALID_PARAMETER, operation, "color must be #RRGGBB");
        }
        try {
            return new int[]{
                Integer.parseInt(hex.substring(0, 2), 16),
                Integer.parseInt(hex.substring(2, 4), 16),
                Integer.parseInt(hex.substring(4, 6), 16)
            };
        } catch (NumberFormatException e) {
            throw new BitwigApiException(ErrorCode.INVALID_PARAMETER, operation, "color must be #RRGGBB");
        }
    }

    public Map<String, Object> trackMixerState(int trackIndex) {
        Track track = trackBank.getItemAt(trackIndex);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("track_index", trackIndex);
        m.put("name", track.name().get());
        m.put("volume", track.volume().value().get());
        m.put("volume_display", track.volume().displayedValue().get());
        m.put("pan", track.pan().value().get() * 2.0 - 1.0);
        m.put("pan_display", track.pan().displayedValue().get());
        m.put("mute", track.mute().get());
        m.put("solo", track.solo().get());
        m.put("arm", track.arm().get());
        m.put("activated", track.isActivated().get());
        m.put("monitor_mode", track.monitorMode().get());
        m.put("crossfade_mode", track.crossFadeMode().get());
        if (track.isGroup().get()) {
            m.put("group_expanded", track.isGroupExpanded().get());
        }
        m.put("is_master", isMaster(track));
        List<Map<String, Object>> sends = new ArrayList<>();
        SendBank sendBank = sendBankOrNull(track);
        for (int i = 0; sendBank != null && i < sendBank.getSizeOfBank(); i++) {
            Send send = sendBank.getItemAt(i);
            if (!send.exists().get()) {
                continue;
            }
            Map<String, Object> s = new LinkedHashMap<>();
            s.put("index", i);
            s.put("name", send.name().get());
            s.put("value", send.value().get());
            s.put("display", send.displayedValue().get());
            s.put("enabled", send.isEnabled().get());
            sends.add(s);
        }
        m.put("sends", sends);
        return m;
    }

    /** The track bank is created without sends, in which case Bitwig throws on sendBank(). */
    private static SendBank sendBankOrNull(Track track) {
        try {
            SendBank bank = track.sendBank();
            bank.getSizeOfBank();
            return bank;
        } catch (RuntimeException e) {
            return null;
        }
    }

    public Map<String, Object> setSend(int trackIndex, int sendIndex, Double value, Boolean enabled) throws BitwigApiException {
        Track track = trackBank.getItemAt(trackIndex);
        SendBank sendBank = sendBankOrNull(track);
        if (sendBank == null) {
            throw new BitwigApiException(ErrorCode.RESOURCE_UNAVAILABLE, "setSend", "Sends are not available");
        }
        if (sendIndex < 0 || sendIndex >= sendBank.getSizeOfBank() || !sendBank.getItemAt(sendIndex).exists().get()) {
            throw new BitwigApiException(ErrorCode.INVALID_RANGE, "setSend", "No send at index " + sendIndex);
        }
        Send send = sendBank.getItemAt(sendIndex);
        if (value != null) {
            send.value().setImmediately(value);
        }
        if (enabled != null) {
            send.isEnabled().set(enabled);
        }
        settle();
        return trackMixerState(trackIndex);
    }

    // ========================================
    // Device chains (slots and layers inside a device)
    // ========================================

    /**
     * A cursor device plus views into its nested chains. Slots (e.g. a Vocoder's "Modulator" and
     * "Carrier", an FX device's "FX") are browsed one at a time through the cursor slot; layers
     * (FX Layer, Instrument Layer) each get their own device bank.
     */
    private static final class ChainCursor {
        final PinnableCursorDevice device;
        final DeviceSlot slot;
        final DeviceBank slotDevices;
        final DeviceLayerBank layers;
        final List<DeviceBank> layerDevices = new ArrayList<>();

        ChainCursor(PinnableCursorDevice device) {
            this.device = device;
            this.slot = device.getCursorSlot();
            this.slotDevices = slot.createDeviceBank(Constants.CHAIN_DEVICES);
            this.layers = device.createLayerBank(Constants.LAYERS);
            device.exists().markInterested();
            device.name().markInterested();
            device.position().markInterested();
            device.slotNames().markInterested();
            device.hasSlots().markInterested();
            device.hasLayers().markInterested();
            device.hasDrumPads().markInterested();
            device.isNested().markInterested();
            slot.name().markInterested();
            markDevices(slotDevices);
            for (int i = 0; i < layers.getSizeOfBank(); i++) {
                DeviceLayer layer = layers.getItemAt(i);
                layer.exists().markInterested();
                layer.name().markInterested();
                DeviceBank bank = layer.createDeviceBank(Constants.CHAIN_DEVICES);
                markDevices(bank);
                layerDevices.add(bank);
            }
        }

        private static void markDevices(DeviceBank bank) {
            for (int i = 0; i < bank.getSizeOfBank(); i++) {
                Device d = bank.getItemAt(i);
                d.exists().markInterested();
                d.name().markInterested();
                d.isEnabled().markInterested();
                d.deviceType().markInterested();
                d.presetName().markInterested();
            }
        }

        void selectSlot(String name) {
            if (slot instanceof CursorDeviceSlot cursorSlot) {
                cursorSlot.selectSlot(name);
            }
        }
    }

    /** One chain inside a device: its display name, the chain itself (for insertion) and its devices. */
    private record ChainView(String name, DeviceChain chain, DeviceBank devices) {}

    /** Selects a chain on a cursor that already points at the containing device. */
    private ChainView selectChain(ChainCursor c, String chain, String operation) throws BitwigApiException {
        String wanted = chain.trim();
        if (wanted.toLowerCase(Locale.ROOT).startsWith("layer:")) {
            String key = wanted.substring("layer:".length()).trim();
            for (int i = 0; i < c.layers.getSizeOfBank(); i++) {
                DeviceLayer layer = c.layers.getItemAt(i);
                if (layer.exists().get() && (key.equals(String.valueOf(i)) || key.equalsIgnoreCase(layer.name().get()))) {
                    return new ChainView("layer:" + i, layer, c.layerDevices.get(i));
                }
            }
            throw new BitwigApiException(ErrorCode.DEVICE_NOT_FOUND, operation, "No layer '" + key + "' in this device");
        }
        String[] names = c.device.slotNames().get();
        String match = null;
        for (String n : names) {
            if (n.equalsIgnoreCase(wanted)) {
                match = n;
            }
        }
        if (match == null) {
            throw new BitwigApiException(ErrorCode.INVALID_PARAMETER, operation,
                "Device has no chain '" + wanted + "'. Chains: " + String.join(", ", names)
                    + (c.device.hasLayers().get() ? " (and layers: use 'layer:N')" : ""));
        }
        String exact = match;
        c.selectSlot(exact);
        waitFor(() -> exact.equalsIgnoreCase(c.slot.name().get()));
        settle();
        return new ChainView(exact, c.slot, c.slotDevices);
    }

    private Device chainDevice(ChainView view, int index, String operation) throws BitwigApiException {
        if (index < 0 || index >= view.devices().getSizeOfBank() || !view.devices().getItemAt(index).exists().get()) {
            throw new BitwigApiException(ErrorCode.DEVICE_NOT_FOUND, operation,
                "No device at index " + index + " in chain '" + view.name() + "'");
        }
        return view.devices().getItemAt(index);
    }

    private static int countBank(DeviceBank bank) {
        int n = 0;
        for (int i = 0; i < bank.getSizeOfBank(); i++) {
            if (bank.getItemAt(i).exists().get()) {
                n = i + 1;
            }
        }
        return n;
    }

    private static List<Map<String, Object>> summaries(DeviceBank bank) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (int i = 0; i < bank.getSizeOfBank(); i++) {
            Device d = bank.getItemAt(i);
            if (!d.exists().get()) {
                continue;
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("index", i);
            m.put("name", d.name().get());
            m.put("enabled", d.isEnabled().get());
            m.put("type", d.deviceType().get());
            putPreset(m, d);
            out.add(m);
        }
        return out;
    }

    /** Lists the chains (slots and layers) inside a device and the devices in each. */
    public Map<String, Object> getDeviceChains(int trackIndex, int deviceIndex) throws BitwigApiException {
        final String operation = "getDeviceChains";
        focusDevice(primary.device, trackIndex, deviceIndex, operation);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("track_index", trackIndex);
        result.put("device_index", deviceIndex);
        result.put("device", primary.device.name().get());
        List<Map<String, Object>> chains = new ArrayList<>();
        for (String name : primary.device.slotNames().get()) {
            ChainView v = selectChain(primary, name, operation);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("chain", v.name());
            m.put("devices", summaries(v.devices()));
            chains.add(m);
        }
        if (primary.device.hasLayers().get()) {
            for (int i = 0; i < primary.layers.getSizeOfBank(); i++) {
                DeviceLayer layer = primary.layers.getItemAt(i);
                if (!layer.exists().get()) {
                    continue;
                }
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("chain", "layer:" + i);
                m.put("layer_name", layer.name().get());
                m.put("devices", summaries(primary.layerDevices.get(i)));
                chains.add(m);
            }
        }
        result.put("chains", chains);
        result.put("has_drum_pads", primary.device.hasDrumPads().get());
        result.put("slot_switching", primary.slot instanceof CursorDeviceSlot);
        return result;
    }

    /**
     * Points the primary cursor at a device, or at a device inside one of its chains when chain is given.
     * The remote-control page then shows that device's parameters.
     */
    private void focusTarget(int trackIndex, int deviceIndex, String chain, Integer chainDeviceIndex, String operation)
            throws BitwigApiException {
        focusDevice(primary.device, trackIndex, deviceIndex, operation);
        if (chain == null) {
            return;
        }
        if (chainDeviceIndex == null) {
            throw new BitwigApiException(ErrorCode.MISSING_REQUIRED_PARAMETER, operation,
                "chain_device_index is required with chain");
        }
        ChainView view = selectChain(primary, chain, operation);
        Device nested = chainDevice(view, chainDeviceIndex, operation);
        String expected = nested.name().get();
        primary.device.selectDevice(nested);
        if (!waitFor(() -> primary.device.isNested().get() && expected.equals(primary.device.name().get()))) {
            throw new BitwigApiException(ErrorCode.BITWIG_TIMEOUT, operation,
                "Could not focus '" + expected + "' in chain '" + view.name() + "'");
        }
        settle();
    }

    /** Resolves a top-level device, or a device inside a chain of it. */
    private Device resolveTarget(int trackIndex, int deviceIndex, String chain, Integer chainDeviceIndex, String operation)
            throws BitwigApiException {
        if (chain == null) {
            return resolveDevice(trackIndex, deviceIndex, operation);
        }
        if (chainDeviceIndex == null) {
            throw new BitwigApiException(ErrorCode.MISSING_REQUIRED_PARAMETER, operation,
                "chain_device_index is required with chain");
        }
        focusDevice(primary.device, trackIndex, deviceIndex, operation);
        return chainDevice(selectChain(primary, chain, operation), chainDeviceIndex, operation);
    }

    /** Where a device should go: a track's chain, or a chain inside a device (via the given cursor). */
    private record Destination(InsertionPoint point, DeviceBank devices, String chainName) {}

    private Destination destination(ChainCursor cursor, int trackIndex, Integer deviceIndex, String chain,
                                    String position, Integer afterIndex, String operation) throws BitwigApiException {
        if (chain == null) {
            return new Destination(insertionPoint(trackIndex, position, afterIndex, operation),
                trackDeviceBanks.get(trackIndex), null);
        }
        if (deviceIndex == null) {
            throw new BitwigApiException(ErrorCode.MISSING_REQUIRED_PARAMETER, operation,
                "device_index (the device that contains the chain) is required with chain");
        }
        focusDevice(cursor.device, trackIndex, deviceIndex, operation);
        ChainView view = selectChain(cursor, chain, operation);
        InsertionPoint ip;
        if (afterIndex != null) {
            ip = chainDevice(view, afterIndex, operation).afterDeviceInsertionPoint();
        } else if ("start".equals(position)) {
            ip = view.chain().startOfDeviceChainInsertionPoint();
        } else if (position == null || "end".equals(position)) {
            ip = view.chain().endOfDeviceChainInsertionPoint();
        } else {
            throw new BitwigApiException(ErrorCode.INVALID_PARAMETER, operation, "position must be 'start' or 'end'");
        }
        return new Destination(ip, view.devices(), view.name());
    }

    // ========================================
    // Devices
    // ========================================

    public List<String> listBitwigDeviceNames() {
        return new ArrayList<>(bitwigDevices.keySet());
    }

    /** Built-in devices with type and category, optionally filtered by a substring of name, type or category. */
    public List<Map<String, Object>> listBitwigDevices(String filter) {
        String f = filter == null ? null : filter.toLowerCase(Locale.ROOT);
        List<Map<String, Object>> out = new ArrayList<>();
        for (BitwigDevice d : bitwigDevices.values()) {
            if (f != null && !(d.name() + " " + d.type() + " " + d.category()).toLowerCase(Locale.ROOT).contains(f)) {
                continue;
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", d.name());
            m.put("type", d.type());
            m.put("category", d.category());
            out.add(m);
        }
        out.sort((a, b) -> String.valueOf(a.get("name")).compareToIgnoreCase(String.valueOf(b.get("name"))));
        return out;
    }

    private InsertionPoint insertionPoint(int trackIndex, String position, Integer afterDeviceIndex, String operation)
            throws BitwigApiException {
        Track track = trackBank.getItemAt(trackIndex);
        if (afterDeviceIndex != null) {
            return resolveDevice(trackIndex, afterDeviceIndex, operation).afterDeviceInsertionPoint();
        }
        if (position == null || "end".equals(position)) {
            return track.endOfDeviceChainInsertionPoint();
        }
        if ("start".equals(position)) {
            return track.startOfDeviceChainInsertionPoint();
        }
        throw new BitwigApiException(ErrorCode.INVALID_PARAMETER, operation, "position must be 'start' or 'end'");
    }

    public Map<String, Object> insertDevice(int trackIndex, String bitwigDevice, String clapId, String vst3Id,
                                            String position, Integer afterDeviceIndex) throws BitwigApiException {
        return insertDevice(trackIndex, null, null, bitwigDevice, clapId, vst3Id, position, afterDeviceIndex);
    }

    public Map<String, Object> insertDevice(int trackIndex, Integer deviceIndex, String chain, String bitwigDevice,
                                            String clapId, String vst3Id, String position, Integer afterDeviceIndex)
            throws BitwigApiException {
        return insertDevice(trackIndex, deviceIndex, chain, bitwigDevice, clapId, vst3Id, null, position, afterDeviceIndex);
    }

    public Map<String, Object> insertDevice(int trackIndex, Integer deviceIndex, String chain, String bitwigDevice,
                                            String clapId, String vst3Id, String presetFile, String position,
                                            Integer afterDeviceIndex) throws BitwigApiException {
        return insertDevice(trackIndex, deviceIndex, chain, bitwigDevice, clapId, vst3Id, null, presetFile, position,
            afterDeviceIndex);
    }

    public Map<String, Object> insertDevice(int trackIndex, Integer deviceIndex, String chain, String bitwigDevice,
                                            String clapId, String vst3Id, Integer vst2Id, String presetFile,
                                            String position, Integer afterDeviceIndex) throws BitwigApiException {
        final String operation = "insertDevice";
        Destination dest = destination(primary, trackIndex, deviceIndex, chain, position, afterDeviceIndex, operation);
        int before = countBank(dest.devices());
        insertInto(dest.point(), bitwigDevice, clapId, vst3Id, vst2Id, presetFile, operation);
        boolean added = waitFor(() -> countBank(dest.devices()) > before);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("inserted", added);
        if (dest.chainName() != null) {
            result.put("chain", dest.chainName());
        }
        result.put("devices", dest.chainName() != null ? summaries(dest.devices()) : deviceSummaries(trackIndex));
        return result;
    }

    /** Inserts a Bitwig device, a plugin, or a file (.bwpreset, .bwdevice, a plugin preset, or a sample) at a point. */
    void insertInto(InsertionPoint ip, String bitwigDevice, String clapId, String vst3Id, String file, String operation)
            throws BitwigApiException {
        insertInto(ip, bitwigDevice, clapId, vst3Id, null, file, operation);
    }

    void insertInto(InsertionPoint ip, String bitwigDevice, String clapId, String vst3Id, Integer vst2Id, String file,
                    String operation) throws BitwigApiException {
        if (bitwigDevice != null) {
            UUID id = findBitwigDevice(bitwigDevice);
            if (id == null) {
                throw new BitwigApiException(ErrorCode.DEVICE_NOT_FOUND, operation,
                    "Unknown Bitwig device '" + bitwigDevice + "'. Use list_bitwig_devices.");
            }
            ip.insertBitwigDevice(id);
        } else if (clapId != null) {
            ip.insertCLAPDevice(clapId);
        } else if (vst3Id != null) {
            ip.insertVST3Device(vst3Id);
        } else if (vst2Id != null) {
            ip.insertVST2Device(vst2Id);
        } else if (file != null) {
            if (!Files.isRegularFile(Path.of(file))) {
                throw new BitwigApiException(ErrorCode.INVALID_PARAMETER, operation, "No such file: " + file);
            }
            ip.insertFile(file);
        } else {
            throw new BitwigApiException(ErrorCode.MISSING_REQUIRED_PARAMETER, operation,
                "Provide bitwig_device, clap_id, vst3_id, vst2_id or preset_file");
        }
    }

    /**
     * Replaces a device (or a device inside a chain) with another device or a preset file. Loading a preset
     * this way is how a device's sound is swapped while keeping its place in the chain.
     */
    public Map<String, Object> replaceDevice(int trackIndex, int deviceIndex, String chain, Integer chainDeviceIndex,
                                             String bitwigDevice, String clapId, String vst3Id, String presetFile)
            throws BitwigApiException {
        return replaceDevice(trackIndex, deviceIndex, chain, chainDeviceIndex, bitwigDevice, clapId, vst3Id, null,
            presetFile);
    }

    public Map<String, Object> replaceDevice(int trackIndex, int deviceIndex, String chain, Integer chainDeviceIndex,
                                             String bitwigDevice, String clapId, String vst3Id, Integer vst2Id,
                                             String presetFile) throws BitwigApiException {
        final String operation = "replaceDevice";
        Device device = resolveTarget(trackIndex, deviceIndex, chain, chainDeviceIndex, operation);
        String oldName = device.name().get();
        String oldPreset = device.presetName().get();
        insertInto(device.replaceDeviceInsertionPoint(), bitwigDevice, clapId, vst3Id, vst2Id, presetFile, operation);
        waitFor(() -> !oldName.equals(device.name().get()) || !oldPreset.equals(device.presetName().get()), 4000);
        settle();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("replaced", oldName);
        result.put("device", device.name().get());
        result.put("preset", device.presetName().get());
        result.put("devices", chain == null ? deviceSummaries(trackIndex) : summaries(
            selectChain(primary, chain, operation).devices()));
        return result;
    }

    UUID findBitwigDevice(String name) {
        for (Map.Entry<String, BitwigDevice> e : bitwigDevices.entrySet()) {
            if (e.getKey().equalsIgnoreCase(name.trim())) {
                return e.getValue().id();
            }
        }
        return null;
    }

    public Map<String, Object> copyOrMoveDevice(int fromTrack, int fromDevice, int toTrack, String position,
                                                Integer afterDeviceIndex, boolean move) throws BitwigApiException {
        return copyOrMoveDevice(fromTrack, fromDevice, null, null, toTrack, null, null, position, afterDeviceIndex, move);
    }

    /**
     * Copies or moves a device. Source and destination can each be a track's chain or a chain inside a
     * device; two independent cursors are used so both can point into chains of the same device.
     */
    public Map<String, Object> copyOrMoveDevice(int fromTrack, int fromDevice, String fromChain,
                                                Integer fromChainDeviceIndex, int toTrack, Integer toDevice,
                                                String toChain, String position, Integer afterDeviceIndex,
                                                boolean move) throws BitwigApiException {
        final String operation = move ? "moveDevice" : "copyDevice";
        // A chain source is held by the primary cursor itself, not by a slot-bank entry: the selected slot
        // belongs to the device, so pointing the second cursor at another slot of the same device would
        // repoint a bank entry to a different device.
        Device source;
        if (fromChain != null) {
            focusTarget(fromTrack, fromDevice, fromChain, fromChainDeviceIndex, operation);
            source = primary.device;
        } else {
            source = resolveDevice(fromTrack, fromDevice, operation);
        }
        String sourceName = source.name().get();
        Destination dest = destination(secondary, toTrack, toDevice, toChain, position, afterDeviceIndex, operation);
        int before = countBank(dest.devices());
        if (move) {
            dest.point().moveDevices(source);
        } else {
            dest.point().copyDevices(source);
        }
        if (!waitFor(() -> countBank(dest.devices()) > before)) {
            settle();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("device", sourceName);
        if (dest.chainName() != null) {
            result.put("target_chain", dest.chainName());
            result.put("target_chain_devices", summaries(dest.devices()));
        } else {
            result.put("target_track_devices", deviceSummaries(toTrack));
        }
        return result;
    }

    public Map<String, Object> deleteDevice(int trackIndex, int deviceIndex) throws BitwigApiException {
        return deleteDevice(trackIndex, deviceIndex, null, null);
    }

    public Map<String, Object> deleteDevice(int trackIndex, int deviceIndex, String chain, Integer chainDeviceIndex)
            throws BitwigApiException {
        final String operation = "deleteDevice";
        Device device = resolveTarget(trackIndex, deviceIndex, chain, chainDeviceIndex, operation);
        String name = device.name().get();
        DeviceBank bank = chain == null ? trackDeviceBanks.get(trackIndex)
            : (chain.toLowerCase(Locale.ROOT).startsWith("layer:")
                ? primary.layerDevices.get(layerIndexOf(device)) : primary.slotDevices);
        int before = countBank(bank);
        device.deleteObject();
        waitFor(() -> countBank(bank) < before);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("deleted_device", name);
        result.put("devices", chain == null ? deviceSummaries(trackIndex) : summaries(bank));
        return result;
    }

    private int layerIndexOf(Device device) {
        for (int i = 0; i < primary.layerDevices.size(); i++) {
            DeviceBank bank = primary.layerDevices.get(i);
            for (int d = 0; d < bank.getSizeOfBank(); d++) {
                if (bank.getItemAt(d) == device) {
                    return i;
                }
            }
        }
        return 0;
    }

    public Map<String, Object> setDeviceEnabled(int trackIndex, int deviceIndex, boolean enabled) throws BitwigApiException {
        return setDeviceEnabled(trackIndex, deviceIndex, null, null, enabled);
    }

    public Map<String, Object> setDeviceEnabled(int trackIndex, int deviceIndex, String chain, Integer chainDeviceIndex,
                                                boolean enabled) throws BitwigApiException {
        Device device = resolveTarget(trackIndex, deviceIndex, chain, chainDeviceIndex, "setDeviceEnabled");
        device.isEnabled().set(enabled);
        waitFor(() -> device.isEnabled().get() == enabled);
        return Map.of("device", device.name().get(), "enabled", device.isEnabled().get());
    }

    public Map<String, Object> setDeviceWindowOpen(int trackIndex, int deviceIndex, boolean open) throws BitwigApiException {
        Device device = resolveDevice(trackIndex, deviceIndex, "setDeviceWindowOpen");
        device.isWindowOpen().set(open);
        return Map.of("device", device.name().get(), "window_open", open);
    }

    /** Points the private edit cursor at a device without touching the user's selection. */
    void focusDevice(PinnableCursorDevice cursor, int trackIndex, int deviceIndex, String operation)
            throws BitwigApiException {
        Track track = trackBank.getItemAt(trackIndex);
        Device device = resolveDevice(trackIndex, deviceIndex, operation);
        String expectedName = device.name().get();
        editTrack.selectChannel(track);
        waitFor(() -> track.name().get().equals(editTrack.name().get()));
        cursor.selectDevice(device);
        boolean ok = waitFor(() -> cursor.exists().get()
            && !cursor.isNested().get()
            && cursor.position().get() == deviceIndex
            && expectedName.equals(cursor.name().get()));
        if (!ok) {
            throw new BitwigApiException(ErrorCode.BITWIG_TIMEOUT, operation,
                "Could not focus device " + deviceIndex + " on track " + trackIndex);
        }
        settle();
    }

    private boolean selectPage(int page) {
        if (editPage.selectedPageIndex().get() == page) {
            return true;
        }
        editPage.selectedPageIndex().set(page);
        boolean ok = waitFor(() -> editPage.selectedPageIndex().get() == page);
        settle();
        return ok;
    }

    private List<Map<String, Object>> readPage() {
        List<Map<String, Object>> params = new ArrayList<>();
        for (int i = 0; i < editPage.getParameterCount(); i++) {
            RemoteControl p = editPage.getParameter(i);
            if (!p.exists().get()) {
                continue;
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("index", i);
            m.put("name", p.name().get());
            m.put("value", p.value().get());
            m.put("display", p.displayedValue().get());
            // Modulators (LFOs, envelopes, macros) move the effective value away from the knob position.
            double modulated = p.modulatedValue().get();
            if (Math.abs(modulated - p.value().get()) > 1e-4) {
                m.put("modulated_value", modulated);
                m.put("modulated_display", p.modulatedValue().displayedValue().get());
            }
            params.add(m);
        }
        return params;
    }

    /** Reads one page, or every page when page is null. */
    public Map<String, Object> getDeviceParameters(int trackIndex, int deviceIndex, Integer page) throws BitwigApiException {
        return getDeviceParameters(trackIndex, deviceIndex, null, null, page);
    }

    public Map<String, Object> getDeviceParameters(int trackIndex, int deviceIndex, String chain, Integer chainDeviceIndex,
                                                   Integer page) throws BitwigApiException {
        final String operation = "getDeviceParameters";
        focusTarget(trackIndex, deviceIndex, chain, chainDeviceIndex, operation);
        String[] pageNames = editPage.pageNames().get();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("track_index", trackIndex);
        result.put("device_index", deviceIndex);
        if (chain != null) {
            result.put("chain", chain);
            result.put("chain_device_index", chainDeviceIndex);
        }
        result.put("device", editDevice.name().get());
        result.put("page_names", pageNames);
        List<Map<String, Object>> pages = new ArrayList<>();
        int first = page == null ? 0 : page;
        int last = page == null ? pageNames.length - 1 : page;
        if (page != null && (page < 0 || page >= Math.max(1, pageNames.length))) {
            throw new BitwigApiException(ErrorCode.INVALID_RANGE, operation, "page must be 0.." + (pageNames.length - 1));
        }
        for (int p = first; p <= Math.max(first, last); p++) {
            if (pageNames.length > 0) {
                selectPage(p);
            }
            Map<String, Object> pm = new LinkedHashMap<>();
            pm.put("page", p);
            pm.put("page_name", pageNames.length > p ? pageNames[p] : "");
            pm.put("parameters", readPage());
            pages.add(pm);
        }
        result.put("pages", pages);
        return result;
    }

    /** A single parameter assignment: by page + index, or by name (searched across all pages). */
    public record ParamAssignment(Integer page, Integer index, String name, double value) {}

    public Map<String, Object> setDeviceParameters(int trackIndex, int deviceIndex, List<ParamAssignment> assignments)
            throws BitwigApiException {
        return setDeviceParameters(trackIndex, deviceIndex, null, null, assignments);
    }

    public Map<String, Object> setDeviceParameters(int trackIndex, int deviceIndex, String chain, Integer chainDeviceIndex,
                                                   List<ParamAssignment> assignments) throws BitwigApiException {
        return setDeviceParameters(trackIndex, deviceIndex, chain, chainDeviceIndex, assignments, true);
    }

    /**
     * With verify=false values are written without waiting for each to be confirmed (one short settle per
     * page instead), for bulk writes such as sweeping every control of a device.
     */
    public Map<String, Object> setDeviceParameters(int trackIndex, int deviceIndex, String chain, Integer chainDeviceIndex,
                                                   List<ParamAssignment> assignments, boolean verify)
            throws BitwigApiException {
        final String operation = "setDeviceParameters";
        focusTarget(trackIndex, deviceIndex, chain, chainDeviceIndex, operation);
        String[] pageNames = editPage.pageNames().get();
        int pageCount = Math.max(1, pageNames.length);
        List<Map<String, Object>> results = new ArrayList<>();
        for (ParamAssignment a : assignments) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("request", a.name() != null ? a.name() : ("page " + a.page() + " index " + a.index()));
            if (a.value() < 0.0 || a.value() > 1.0) {
                r.put("status", "error");
                r.put("message", "value must be 0.0-1.0");
                results.add(r);
                continue;
            }
            RemoteControl target = null;
            int foundPage = -1;
            if (a.name() != null) {
                for (int p = 0; p < pageCount && target == null; p++) {
                    selectPage(p);
                    for (int i = 0; i < editPage.getParameterCount(); i++) {
                        RemoteControl rc = editPage.getParameter(i);
                        if (rc.exists().get() && a.name().equalsIgnoreCase(rc.name().get())) {
                            target = rc;
                            foundPage = p;
                            break;
                        }
                    }
                }
            } else if (a.index() != null) {
                int p = a.page() == null ? 0 : a.page();
                if (p >= 0 && p < pageCount && a.index() >= 0 && a.index() < editPage.getParameterCount()) {
                    selectPage(p);
                    RemoteControl rc = editPage.getParameter(a.index());
                    if (rc.exists().get()) {
                        target = rc;
                        foundPage = p;
                    }
                }
            }
            if (target == null) {
                r.put("status", "error");
                r.put("message", "Parameter not found");
                results.add(r);
                continue;
            }
            RemoteControl t = target;
            // Bitwig ignores set() on remote controls from extensions with no bound hardware; setImmediately() applies.
            double before = t.value().get();
            t.value().setImmediately(a.value());
            if (verify) {
                // Stepped controls snap to the nearest step, so "applied" also accepts any change.
                boolean applied = waitFor(() -> Math.abs(t.value().get() - a.value()) < 1e-3
                    || Math.abs(t.value().get() - before) > 1e-6, 400);
                settle();
                r.put("applied", applied || Math.abs(before - a.value()) < 1e-3);
            }
            r.put("status", "success");
            r.put("page", foundPage);
            r.put("name", t.name().get());
            r.put("value", t.value().get());
            r.put("display", t.displayedValue().get());
            results.add(r);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("device", editDevice.name().get());
        result.put("results", results);
        return result;
    }

    // ========================================
    // Clips and files
    // ========================================

    public Map<String, Object> insertFile(int trackIndex, Integer slotIndex, String path) throws BitwigApiException {
        final String operation = "insertFile";
        if (path == null || !Files.isRegularFile(Path.of(path))) {
            throw new BitwigApiException(ErrorCode.INVALID_PARAMETER, operation, "File not found: " + path);
        }
        Track track = trackBank.getItemAt(trackIndex);
        Map<String, Object> result = new LinkedHashMap<>();
        if (slotIndex != null) {
            ClipLauncherSlot slot = slotAt(track, slotIndex, operation);
            int tracksBefore = countTracks();
            slot.replaceInsertionPoint().insertFile(path);
            boolean ok = waitFor(() -> slot.hasContent().get() || countTracks() > tracksBefore);
            settle();
            result.put("track_index", trackIndex);
            result.put("slot_index", slotIndex);
            result.put("inserted", slot.hasContent().get());
            int created = countTracks() - tracksBefore;
            if (created > 0) {
                // A MIDI file with several tracks or channels is split by Bitwig into one new track each.
                result.put("new_tracks_created", created);
                result.put("note", "Bitwig split the file into new tracks instead of filling the slot; "
                    + "merge the MIDI into one track on one channel to import it as a single clip");
            }
        } else {
            int before = countTracks();
            track.afterTrackInsertionPoint().insertFile(path);
            boolean ok = waitFor(() -> countTracks() > before);
            result.put("new_track_index", ok ? trackIndex + 1 : null);
            result.put("inserted", ok);
        }
        return result;
    }

    private ClipLauncherSlot slotAt(Track track, int slotIndex, String operation) throws BitwigApiException {
        ClipLauncherSlotBank slots = track.clipLauncherSlotBank();
        if (slotIndex < 0 || slotIndex >= slots.getSizeOfBank()) {
            throw new BitwigApiException(ErrorCode.INVALID_RANGE, operation, "slot_index must be 0.." + (slots.getSizeOfBank() - 1));
        }
        return slots.getItemAt(slotIndex);
    }

    public Map<String, Object> createClip(int trackIndex, int slotIndex, int lengthBeats) throws BitwigApiException {
        final String operation = "createClip";
        ClipLauncherSlot slot = slotAt(trackBank.getItemAt(trackIndex), slotIndex, operation);
        if (slot.hasContent().get()) {
            throw new BitwigApiException(ErrorCode.INVALID_PARAMETER, operation, "Slot already has a clip");
        }
        slot.createEmptyClip(lengthBeats);
        boolean ok = waitFor(() -> slot.hasContent().get());
        return Map.of("track_index", trackIndex, "slot_index", slotIndex, "length_beats", lengthBeats, "created", ok);
    }

    public Map<String, Object> deleteClip(int trackIndex, int slotIndex) throws BitwigApiException {
        ClipLauncherSlot slot = slotAt(trackBank.getItemAt(trackIndex), slotIndex, "deleteClip");
        slot.deleteObject();
        waitFor(() -> !slot.hasContent().get());
        return Map.of("track_index", trackIndex, "slot_index", slotIndex, "deleted", !slot.hasContent().get());
    }

    /** A MIDI note in beats from the clip start. */
    public record Note(double startBeats, int pitch, int velocity, double durationBeats) {}

    public Map<String, Object> writeNotes(int trackIndex, int slotIndex, List<Note> notes, boolean clearFirst)
            throws BitwigApiException {
        final String operation = "writeNotes";
        Track track = trackBank.getItemAt(trackIndex);
        ClipLauncherSlot slot = slotAt(track, slotIndex, operation);
        if (!slot.hasContent().get()) {
            throw new BitwigApiException(ErrorCode.CLIP_NOT_FOUND, operation, "Slot is empty; create_clip first");
        }
        editTrack.selectChannel(track);
        waitFor(() -> track.name().get().equals(editTrack.name().get()));
        slot.select();
        if (!waitFor(() -> editClip.exists().get())) {
            throw new BitwigApiException(ErrorCode.BITWIG_TIMEOUT, operation, "Could not focus the clip");
        }
        settle();
        if (clearFirst) {
            editClip.clearSteps();
        }
        int written = 0;
        for (Note n : notes) {
            int step = (int) Math.round(n.startBeats() / Constants.CLIP_STEP_BEATS);
            if (step < 0 || step >= Constants.CLIP_GRID_WIDTH || n.pitch() < 0 || n.pitch() > 127) {
                continue;
            }
            editClip.setStep(step, n.pitch(), Math.max(1, Math.min(127, n.velocity())), n.durationBeats());
            written++;
        }
        return Map.of("track_index", trackIndex, "slot_index", slotIndex, "notes_written", written);
    }

    // ========================================
    // Transport
    // ========================================

    public Map<String, Object> setTransport(Double tempoBpm, Double positionBeats, Boolean loop, Boolean metronome,
                                            Boolean record, Boolean overdub) {
        if (tempoBpm != null) {
            transport.tempo().setRaw(tempoBpm);
        }
        if (positionBeats != null) {
            transport.getPosition().set(positionBeats);
        }
        if (loop != null) {
            transport.isArrangerLoopEnabled().set(loop);
        }
        if (metronome != null) {
            transport.isMetronomeEnabled().set(metronome);
        }
        if (record != null) {
            transport.isArrangerRecordEnabled().set(record);
        }
        if (overdub != null) {
            transport.isArrangerOverdubEnabled().set(overdub);
        }
        settle();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("tempo", transport.tempo().displayedValue().get());
        m.put("position_beats", transport.getPosition().get());
        m.put("loop", transport.isArrangerLoopEnabled().get());
        m.put("metronome", transport.isMetronomeEnabled().get());
        m.put("record", transport.isArrangerRecordEnabled().get());
        m.put("overdub", transport.isArrangerOverdubEnabled().get());
        return m;
    }

    // ========================================
    // Application actions
    // ========================================

    Track trackAt(int trackIndex) {
        return trackBank.getItemAt(trackIndex);
    }

    /**
     * Brings things into view in Bitwig's window: a panel layout (ARRANGE, MIX, EDIT), a track, a launcher clip in
     * the editor, or a device. This changes what the user sees and selects; it is for working alongside them.
     */
    public Map<String, Object> show(String panelLayout, Integer trackIndex, Integer slotIndex, Integer deviceIndex)
            throws BitwigApiException {
        final String operation = "show";
        Map<String, Object> m = new LinkedHashMap<>();
        if (panelLayout != null) {
            String layout = panelLayout.toUpperCase(Locale.ROOT);
            if (!List.of("ARRANGE", "MIX", "EDIT").contains(layout)) {
                throw new BitwigApiException(ErrorCode.INVALID_PARAMETER, operation, "panel_layout must be ARRANGE, MIX or EDIT");
            }
            application.setPanelLayout(layout);
            waitFor(() -> layout.equalsIgnoreCase(application.panelLayout().get()));
            m.put("panel_layout", application.panelLayout().get());
        }
        if (trackIndex != null) {
            Track track = trackBank.getItemAt(trackIndex);
            track.selectInMixer();
            track.makeVisibleInArranger();
            track.makeVisibleInMixer();
            m.put("track", track.name().get());
            if (slotIndex != null) {
                ClipLauncherSlot slot = slotAt(track, slotIndex, operation);
                if (!slot.hasContent().get()) {
                    throw new BitwigApiException(ErrorCode.CLIP_NOT_FOUND, operation, "Slot " + slotIndex + " is empty");
                }
                slot.select();
                slot.showInEditor();
                m.put("clip_slot", slotIndex);
            }
            if (deviceIndex != null) {
                Device device = resolveDevice(trackIndex, deviceIndex, operation);
                device.selectInEditor();
                m.put("device", device.name().get());
            }
        } else if (slotIndex != null || deviceIndex != null) {
            throw new BitwigApiException(ErrorCode.MISSING_REQUIRED_PARAMETER, operation, "A clip or device needs its track");
        }
        settle();
        return m;
    }

    /** The private cursor track other facades attach their own cursor devices to (during init). */
    CursorTrack editTrack() {
        return editTrack;
    }

    public Application application() {
        return application;
    }

    public boolean isPlaying() {
        return transport.isPlaying().get();
    }

    public List<Map<String, Object>> listActions(String filter) {
        List<Map<String, Object>> out = new ArrayList<>();
        String f = filter == null ? null : filter.toLowerCase(Locale.ROOT);
        for (Action a : application.getActions()) {
            String id = a.getId();
            String name = a.getName();
            String cat = a.getCategory() != null ? a.getCategory().getName() : "";
            if (f != null && !(id + " " + name + " " + cat).toLowerCase(Locale.ROOT).contains(f)) {
                continue;
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", id);
            m.put("name", name);
            m.put("category", cat);
            out.add(m);
        }
        return out;
    }

    public Map<String, Object> invokeAction(String id) throws BitwigApiException {
        Action action = application.getAction(id);
        if (action == null) {
            throw new BitwigApiException(ErrorCode.INVALID_PARAMETER, "invokeAction", "Unknown action id '" + id + "'. Use list_actions.");
        }
        action.invoke();
        return Map.of("invoked", id, "name", action.getName());
    }

    public Map<String, Object> undo(boolean redo) {
        if (redo) {
            application.redo();
        } else {
            application.undo();
        }
        settle();
        return Map.of("action", redo ? "redo" : "undo");
    }

    // ========================================
    // Device list resource
    // ========================================

    /** A Bitwig built-in device: name, UUID, and (when read from Bitwig's library) type and category. */
    public record BitwigDevice(String name, UUID id, String type, String category) {}

    /**
     * Bitwig's built-in devices. Read from Bitwig's own library (Library/device-settings under the
     * installationRoot Bitwig passes to the JVM) so every device of the running version is insertable,
     * falling back to the bundled list when that is unavailable.
     */
    private static Map<String, BitwigDevice> loadBitwigDevices() {
        Map<String, BitwigDevice> map = new LinkedHashMap<>();
        try (InputStream in = BitwigEditFacade.class.getResourceAsStream(Constants.DEVICE_LIST_RESOURCE)) {
            if (in != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    int sep = line.lastIndexOf('$');
                    if (line.isEmpty() || line.startsWith("#") || sep < 0) {
                        continue;
                    }
                    String name = line.substring(0, sep);
                    map.put(name, new BitwigDevice(name, UUID.fromString(line.substring(sep + 1)), "", ""));
                }
            }
        } catch (Exception e) {
            // Keep whatever was read; the library scan below may still fill the map.
        }
        String root = System.getProperty("installationRoot");
        if (root != null) {
            map.putAll(readLibrary(Path.of(root, "Library", "device-settings")));
        }
        return map;
    }

    static Map<String, BitwigDevice> readLibrary(Path deviceSettings) {
        Map<String, BitwigDevice> map = new LinkedHashMap<>();
        if (!Files.isDirectory(deviceSettings)) {
            return map;
        }
        try (var dirs = Files.list(deviceSettings)) {
            for (Path dir : (Iterable<Path>) dirs::iterator) {
                Path preset = dir.resolve("Default.bwpreset");
                if (!Files.isRegularFile(preset)) {
                    continue;
                }
                byte[] head;
                try (InputStream in = Files.newInputStream(preset)) {
                    head = in.readNBytes(4096);
                }
                String id = presetMeta(head, "device_id");
                String name = presetMeta(head, "device_name");
                if (id == null || name == null) {
                    continue;
                }
                String type = switch (String.valueOf(presetMeta(head, "device_type"))) {
                    case "audio_to_audio" -> "audio effect";
                    case "note_to_audio" -> "instrument";
                    case "note_to_note" -> "note effect";
                    default -> "";
                };
                String category = presetMeta(head, "device_category");
                map.put(name, new BitwigDevice(name, UUID.fromString(id), type, category == null ? "" : category));
            }
        } catch (Exception e) {
            // An unreadable library leaves the bundled list in place.
        }
        return map;
    }

    /** Preset metadata: a 4-byte big-endian length and the key, then 0x08 and a length-prefixed UTF-8 value. */
    static String presetMeta(byte[] head, String key) {
        byte[] k = key.getBytes(StandardCharsets.UTF_8);
        outer:
        for (int i = 0; i + 4 + k.length + 5 <= head.length; i++) {
            if (head[i] != 0 || head[i + 1] != 0 || head[i + 2] != 0 || head[i + 3] != k.length) {
                continue;
            }
            for (int j = 0; j < k.length; j++) {
                if (head[i + 4 + j] != k[j]) {
                    continue outer;
                }
            }
            int v = i + 4 + k.length;
            if (head[v] != 0x08) {
                return null;
            }
            int n = ((head[v + 1] & 0xff) << 24) | ((head[v + 2] & 0xff) << 16) | ((head[v + 3] & 0xff) << 8) | (head[v + 4] & 0xff);
            if (n < 0 || v + 5 + n > head.length) {
                return null;
            }
            return new String(head, v + 5, n, StandardCharsets.UTF_8);
        }
        return null;
    }
}
