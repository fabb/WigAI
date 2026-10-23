package io.github.fabb.wigai.bitwig;

import com.bitwig.extension.callback.IntegerValueChangedCallback;
import com.bitwig.extension.controller.api.*;
import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.common.error.BitwigApiException;
import io.github.fabb.wigai.common.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BitwigSessionFacadeTest {

    private ControllerHost host;
    private Track track;
    private BitwigSessionFacade facade;
    private final ArgumentCaptor<IntegerValueChangedCallback> meters = ArgumentCaptor.forClass(IntegerValueChangedCallback.class);

    @BeforeEach
    void setUp() {
        host = mock(ControllerHost.class, RETURNS_DEEP_STUBS);
        TrackBank trackBank = mock(TrackBank.class);
        track = mock(Track.class, RETURNS_DEEP_STUBS);
        when(trackBank.getSizeOfBank()).thenReturn(1);
        when(trackBank.getItemAt(0)).thenReturn(track);
        when(track.exists().get()).thenReturn(true);
        when(track.name().get()).thenReturn("Vox");
        when(track.trackType().get()).thenReturn("Audio");
        when(host.createArranger().createCueMarkerBank(anyInt()).getSizeOfBank()).thenReturn(0);
        when(host.createSceneBank(anyInt()).getSizeOfBank()).thenReturn(0);
        facade = new BitwigSessionFacade(host, mock(Logger.class), mock(Transport.class, RETURNS_DEEP_STUBS), trackBank, 0);
        verify(track, times(2)).addVuMeterObserver(eq(1000), eq(-1), anyBoolean(), meters.capture());
    }

    @Test
    @SuppressWarnings("unchecked")
    void levelsAreNormalizedFromMeterCallbacks() {
        meters.getAllValues().get(0).valueChanged(500);  // peak
        meters.getAllValues().get(1).valueChanged(250);  // rms
        Map<String, Object> levels = facade.getLevels();
        Map<String, Object> vox = ((List<Map<String, Object>>) levels.get("tracks")).get(0);
        assertEquals("Vox", vox.get("name"));
        assertEquals(0.5, vox.get("peak"));
        assertEquals(0.25, vox.get("rms"));
    }

    @Test
    void playNotesSchedulesStartAndStopForEachNote() {
        Map<String, Object> r = facade.playNotes(0, List.of(
            new BitwigSessionFacade.AuditionNote(60, 100, 0, 500),
            new BitwigSessionFacade.AuditionNote(64, 200, 100, 500)));
        verify(host).scheduleTask(any(Runnable.class), eq(0L));
        verify(host).scheduleTask(any(Runnable.class), eq(500L));
        verify(host).scheduleTask(any(Runnable.class), eq(100L));
        verify(host).scheduleTask(any(Runnable.class), eq(600L));
        assertEquals(600L, r.get("total_ms"));
    }

    @Test
    void stopTrackOrReturnToArrangement() {
        facade.stopTrack(0, false);
        verify(track).stop();
        facade.stopTrack(0, true);
        verify(track).returnToArrangement();
    }

    @Test
    void unknownSceneAndCueMarkerAreErrors() {
        BitwigApiException e = assertThrows(BitwigApiException.class, () -> facade.renameScene(3, "Chorus"));
        assertEquals(ErrorCode.SCENE_NOT_FOUND, e.getErrorCode());
        assertThrows(BitwigApiException.class, () -> facade.goToCueMarker(0, null, false));
    }

    @Test
    void readNotesNeedsAClip() {
        when(track.clipLauncherSlotBank().getSizeOfBank()).thenReturn(4);
        when(track.clipLauncherSlotBank().getItemAt(0).hasContent().get()).thenReturn(false);
        BitwigApiException e = assertThrows(BitwigApiException.class, () -> facade.readNotes(0, 0));
        assertEquals(ErrorCode.CLIP_NOT_FOUND, e.getErrorCode());
    }

    @Test
    void midiMessagesAreEncoded() throws Exception {
        assertArrayEquals(new int[]{0xB2, 1, 100}, BitwigSessionFacade.midiMessage("cc", 2, 1, 100, null, "op"));
        assertArrayEquals(new int[]{0xE0, 0, 64}, BitwigSessionFacade.midiMessage("pitch_bend", 0, null, 0, null, "op"));
        assertArrayEquals(new int[]{0xE0, 0x7F, 0x7F}, BitwigSessionFacade.midiMessage("pitch_bend", 0, null, 8191, null, "op"));
        assertArrayEquals(new int[]{0xE0, 0, 0}, BitwigSessionFacade.midiMessage("pitch_bend", 0, null, -8192, null, "op"));
        assertArrayEquals(new int[]{0xC0, 5, 0}, BitwigSessionFacade.midiMessage("program", 0, 5, null, null, "op"));
        assertArrayEquals(new int[]{0x90, 60, 0}, BitwigSessionFacade.midiMessage("raw", 0, 60, null, 0x90, "op"));
        assertThrows(BitwigApiException.class, () -> BitwigSessionFacade.midiMessage("cc", 0, 1, 128, null, "op"));
        assertThrows(BitwigApiException.class, () -> BitwigSessionFacade.midiMessage("pitch_bend", 0, null, 9000, null, "op"));
        assertThrows(BitwigApiException.class, () -> BitwigSessionFacade.midiMessage("raw", 0, 1, 1, 0xF0, "op"));
        assertThrows(BitwigApiException.class, () -> BitwigSessionFacade.midiMessage("sysex", 0, 1, 1, null, "op"));
    }
}
