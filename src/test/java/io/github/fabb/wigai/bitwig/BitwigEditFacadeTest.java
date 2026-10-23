package io.github.fabb.wigai.bitwig;

import com.bitwig.extension.controller.api.*;
import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.common.error.BitwigApiException;
import io.github.fabb.wigai.common.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class BitwigEditFacadeTest {

    private static final UUID EQ_PLUS = UUID.fromString("e4815188-ba6f-4d14-bcfc-2dcb8f778ccb");

    private ControllerHost host;
    private Application application;
    private TrackBank trackBank;
    private Track track0;
    private DeviceBank deviceBank0;
    private Device device0;
    private BitwigEditFacade facade;

    @BeforeEach
    void setUp() {
        host = mock(ControllerHost.class, RETURNS_DEEP_STUBS);
        application = mock(Application.class);
        when(application.panelLayout()).thenReturn(mock(StringValue.class));
        Transport transport = mock(Transport.class, RETURNS_DEEP_STUBS);
        trackBank = mock(TrackBank.class);
        track0 = mock(Track.class, RETURNS_DEEP_STUBS);
        deviceBank0 = mock(DeviceBank.class);
        device0 = mock(Device.class, RETURNS_DEEP_STUBS);

        when(trackBank.getSizeOfBank()).thenReturn(1);
        when(trackBank.getItemAt(0)).thenReturn(track0);
        when(track0.exists().get()).thenReturn(true);
        when(track0.name().get()).thenReturn("Up Vocals");
        when(track0.clipLauncherSlotBank().getSizeOfBank()).thenReturn(0);
        when(deviceBank0.getSizeOfBank()).thenReturn(1);
        when(deviceBank0.getItemAt(0)).thenReturn(device0);
        when(device0.exists().get()).thenReturn(true);
        when(device0.name().get()).thenReturn("Vocoder");

        facade = new BitwigEditFacade(host, mock(Logger.class), application, transport, trackBank,
            List.of(deviceBank0), 0);
    }

    @Test
    void resolvesTrackByNameAndIndex() throws Exception {
        assertEquals(0, facade.resolveTrackIndex(null, "Up Vocals", "op"));
        assertEquals(0, facade.resolveTrackIndex(0, null, "op"));
    }

    @Test
    void unknownTrackIsTrackNotFound() {
        BitwigApiException e = assertThrows(BitwigApiException.class,
            () -> facade.resolveTrackIndex(null, "Nope", "op"));
        assertEquals(ErrorCode.TRACK_NOT_FOUND, e.getErrorCode());
        assertThrows(BitwigApiException.class, () -> facade.resolveTrackIndex(5, null, "op"));
        assertThrows(BitwigApiException.class, () -> facade.resolveTrackIndex(null, null, "op"));
    }

    @Test
    void bundledDeviceListResolvesCaseInsensitively() {
        assertEquals(EQ_PLUS, facade.findBitwigDevice("eq+"));
        assertNotNull(facade.findBitwigDevice("Vocoder"));
        assertNotNull(facade.findBitwigDevice("Reverb"));
        assertNull(facade.findBitwigDevice("Not A Device"));
        assertTrue(facade.listBitwigDeviceNames().size() > 100);
    }

    @Test
    void readsDeviceMetadataFromBitwigPresetHeaders(@org.junit.jupiter.api.io.TempDir java.nio.file.Path dir) throws Exception {
        java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
        b.write("BtWg0003meta".getBytes());
        for (String[] kv : new String[][]{{"device_id", "01af068e-1e49-4777-a6e6-7f1dc679227a"},
                {"device_name", "EQ-2"}, {"device_type", "audio_to_audio"}, {"device_category", "EQ"}}) {
            b.write(new byte[]{0, 0, 0, (byte) kv[0].length()});
            b.write(kv[0].getBytes());
            b.write(new byte[]{8, 0, 0, 0, (byte) kv[1].length()});
            b.write(kv[1].getBytes());
            b.write(new byte[]{0, 0, 0, 1});
        }
        java.nio.file.Path d = java.nio.file.Files.createDirectories(dir.resolve("01af068e-1e49-4777-a6e6-7f1dc679227a"));
        java.nio.file.Files.write(d.resolve("Default.bwpreset"), b.toByteArray());
        Map<String, BitwigEditFacade.BitwigDevice> lib = BitwigEditFacade.readLibrary(dir);
        BitwigEditFacade.BitwigDevice eq = lib.get("EQ-2");
        assertEquals(UUID.fromString("01af068e-1e49-4777-a6e6-7f1dc679227a"), eq.id());
        assertEquals("audio effect", eq.type());
        assertEquals("EQ", eq.category());
        assertTrue(BitwigEditFacade.readLibrary(dir.resolve("missing")).isEmpty());
    }

    @Test
    void insertBitwigDeviceAtEndOfChain() throws Exception {
        InsertionPoint end = mock(InsertionPoint.class);
        when(track0.endOfDeviceChainInsertionPoint()).thenReturn(end);
        facade.insertDevice(0, "EQ+", null, null, null, null);
        verify(end).insertBitwigDevice(EQ_PLUS);
    }

    @Test
    void insertClapAfterDevice() throws Exception {
        InsertionPoint after = mock(InsertionPoint.class);
        when(device0.afterDeviceInsertionPoint()).thenReturn(after);
        facade.insertDevice(0, null, "org.surge-synth-team.surge-xt", null, null, 0);
        verify(after).insertCLAPDevice("org.surge-synth-team.surge-xt");
    }

    @Test
    void insertUnknownDeviceFails() {
        when(track0.endOfDeviceChainInsertionPoint()).thenReturn(mock(InsertionPoint.class));
        BitwigApiException e = assertThrows(BitwigApiException.class,
            () -> facade.insertDevice(0, "Nope", null, null, null, null));
        assertEquals(ErrorCode.DEVICE_NOT_FOUND, e.getErrorCode());
    }

    @Test
    void insertWithoutAnyIdFails() {
        when(track0.endOfDeviceChainInsertionPoint()).thenReturn(mock(InsertionPoint.class));
        assertThrows(BitwigApiException.class, () -> facade.insertDevice(0, null, null, null, null, null));
    }

    @Test
    void copyDeviceUsesInsertionPoint() throws Exception {
        InsertionPoint start = mock(InsertionPoint.class);
        when(track0.startOfDeviceChainInsertionPoint()).thenReturn(start);
        facade.copyOrMoveDevice(0, 0, 0, "start", null, false);
        verify(start).copyDevices(device0);
        facade.copyOrMoveDevice(0, 0, 0, "start", null, true);
        verify(start).moveDevices(device0);
    }

    @Test
    void missingDeviceIsDeviceNotFound() {
        BitwigApiException e = assertThrows(BitwigApiException.class, () -> facade.setDeviceEnabled(0, 3, true));
        assertEquals(ErrorCode.DEVICE_NOT_FOUND, e.getErrorCode());
    }

    @Test
    void setDeviceEnabledSetsValue() throws Exception {
        facade.setDeviceEnabled(0, 0, true);
        verify(device0.isEnabled()).set(true);
    }

    @Test
    void createTrackCallsApplication() throws Exception {
        assertThrows(BitwigApiException.class, () -> facade.createTrack("audio", null, null)); // never appears: timeout
        verify(application).createAudioTrack(1);
        assertThrows(BitwigApiException.class, () -> facade.createTrack("midi", null, null));
        assertThrows(BitwigApiException.class, () -> facade.createTrack("audio", 9, null));
    }

    @Test
    void setTrackPropertiesMapsPanAndColor() throws Exception {
        facade.setTrackProperties(0, "Stack", null, null, -1.0, true, null, null, null, "auto", "#FF8000");
        verify(track0.name()).set("Stack");
        verify(track0.pan().value()).setImmediately(0.0);
        verify(track0.mute()).set(true);
        verify(track0.monitorMode()).set("AUTO");
        verify(track0.color()).set(1.0f, 128 / 255f, 0.0f);
    }

    /** Regression: the master track sits at the end of the flat track list and must never be renamed or deleted. */
    @Test
    void masterTrackIsNeverTreatedAsANewTrack() throws Exception {
        Track master = mock(Track.class, RETURNS_DEEP_STUBS);
        when(master.exists().get()).thenReturn(true);
        when(master.trackType().get()).thenReturn("Master");
        when(master.name().get()).thenReturn("Master");
        when(master.clipLauncherSlotBank().getSizeOfBank()).thenReturn(0);
        when(trackBank.getSizeOfBank()).thenReturn(2);
        when(trackBank.getItemAt(1)).thenReturn(master);
        DeviceBank emptyBank = mock(DeviceBank.class);
        facade = new BitwigEditFacade(host, mock(Logger.class), application, mock(Transport.class, RETURNS_DEEP_STUBS),
            trackBank, List.of(deviceBank0, emptyBank), 0);

        assertEquals(1, facade.countTracks());
        // Bitwig hasn't shown the new track yet, so nothing may be renamed
        assertThrows(BitwigApiException.class, () -> facade.createTrack("audio", null, "New"));
        verify(application).createAudioTrack(1);
        verify(master.name(), never()).set(anyString());

        // Even if the count goes up, a track of the wrong type at the target index is not renamed
        when(track0.trackType().get()).thenReturn("Audio");
        Track effect = mock(Track.class, RETURNS_DEEP_STUBS);
        when(effect.exists().get()).thenReturn(true);
        when(effect.trackType().get()).thenReturn("Effect");
        when(trackBank.getSizeOfBank()).thenReturn(3);
        when(trackBank.getItemAt(1)).thenReturn(effect);
        when(trackBank.getItemAt(2)).thenReturn(master);
        assertEquals(1, facade.countTracks());
        assertEquals(1, facade.countEffectTracks());

        BitwigApiException e = assertThrows(BitwigApiException.class, () -> facade.deleteTrack(2));
        assertEquals(ErrorCode.INVALID_PARAMETER, e.getErrorCode());
        verify(master, never()).deleteObject();
        assertThrows(BitwigApiException.class, () -> facade.duplicateTrack(2));
    }

    @Test
    void mixerStateSurvivesMissingSendBank() {
        when(track0.sendBank()).thenThrow(new IllegalStateException("No send bank exists"));
        Map<String, Object> state = facade.trackMixerState(0);
        assertEquals(List.of(), state.get("sends"));
        assertThrows(BitwigApiException.class, () -> facade.setSend(0, 0, 0.5, null));
    }

    @Test
    void parsesDbDisplay() {
        assertEquals(-3.0, BitwigEditFacade.parseDb("-3.00 dB"));
        assertEquals(-3.0, BitwigEditFacade.parseDb("−3.00 dB"));
        assertEquals(Double.NEGATIVE_INFINITY, BitwigEditFacade.parseDb("-inf dB"));
        assertNull(BitwigEditFacade.parseDb("loud"));
        assertNull(BitwigEditFacade.parseDb(null));
    }

    @Test
    void parsesColor() throws Exception {
        assertArrayEquals(new int[]{255, 0, 16}, BitwigEditFacade.parseColor("#ff0010", "op"));
        assertThrows(BitwigApiException.class, () -> BitwigEditFacade.parseColor("red", "op"));
    }

    @Test
    void invokeActionRejectsUnknownId() throws Exception {
        assertThrows(BitwigApiException.class, () -> facade.invokeAction("nope"));
        Action action = mock(Action.class);
        when(action.getName()).thenReturn("Undo");
        when(application.getAction("undo")).thenReturn(action);
        Map<String, Object> r = facade.invokeAction("undo");
        verify(action).invoke();
        assertEquals("undo", r.get("invoked"));
    }

    @Test
    void listActionsFilters() {
        Action a = mock(Action.class, RETURNS_DEEP_STUBS);
        when(a.getId()).thenReturn("toggle_metronome");
        when(a.getName()).thenReturn("Toggle Metronome");
        Action b = mock(Action.class, RETURNS_DEEP_STUBS);
        when(b.getId()).thenReturn("undo");
        when(b.getName()).thenReturn("Undo");
        when(application.getActions()).thenReturn(new Action[]{a, b});
        assertEquals(1, facade.listActions("metro").size());
        assertEquals(2, facade.listActions(null).size());
    }

    @Test
    void insertFileRejectsMissingFile() {
        assertThrows(BitwigApiException.class, () -> facade.insertFile(0, 0, "/nonexistent/file.mid"));
    }
}
