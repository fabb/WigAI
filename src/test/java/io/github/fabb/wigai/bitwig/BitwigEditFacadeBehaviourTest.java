package io.github.fabb.wigai.bitwig;

import com.bitwig.extension.controller.api.*;
import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.common.error.BitwigApiException;
import io.github.fabb.wigai.common.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Behaviour tests for BitwigEditFacade against small fakes of the Bitwig objects it drives:
 * a device with three remote-control pages, a fader with a dB display, and a launcher clip.
 * The fakes model the live quirks that broke these tools: values written with set() are
 * ignored (only setImmediately() applies) and displays are rounded.
 */
class BitwigEditFacadeBehaviourTest {

    private static final String[][] PAGES = {
        {"Size", "Pre-delay", "Diffusion", "R. Time", "Early/Late", "Buildup", "St. Width", "Mix"},
        {"Low Cut", "High Cut", "Damping"},
        {"Gain"}
    };

    private Track track;
    private Device device;
    private ClipLauncherSlot slot0;
    private PinnableCursorClip clip;
    private Transport transport;
    private BitwigEditFacade facade;

    private final AtomicInteger selectedPage = new AtomicInteger(0);
    private final double[][] values = new double[PAGES.length][8];
    private final AtomicReference<Double> fader = new AtomicReference<>(0.5);
    private final AtomicBoolean slotHasContent = new AtomicBoolean(false);

    /** A ranged value that ignores set() and applies setImmediately(), like Bitwig does for extensions. */
    private static SettableRangedValue rangedValue(java.util.function.DoubleSupplier read,
                                                   java.util.function.DoubleConsumer write) {
        SettableRangedValue v = mock(SettableRangedValue.class);
        when(v.get()).thenAnswer(i -> read.getAsDouble());
        doAnswer(i -> {
            write.accept(i.getArgument(0));
            return null;
        }).when(v).setImmediately(anyDouble());
        return v;
    }

    private static StringValue stringValue(java.util.function.Supplier<String> read) {
        StringValue s = mock(StringValue.class);
        when(s.get()).thenAnswer(i -> read.get());
        return s;
    }

    /** Fader display: 0 dB at 0.7937, 60 dB of range per unit, rounded to 0.1 dB like Bitwig. */
    static String faderDisplay(double v) {
        if (v <= 0) {
            return "-Inf dB";
        }
        double db = 60 * Math.log10(v / 0.7937) / Math.log10(2);
        return String.format(java.util.Locale.ROOT, "%.1f dB", db);
    }

    @BeforeEach
    void setUp() {
        ControllerHost host = mock(ControllerHost.class, RETURNS_DEEP_STUBS);
        Application application = mock(Application.class);
        when(application.panelLayout()).thenReturn(mock(StringValue.class));
        transport = mock(Transport.class, RETURNS_DEEP_STUBS);
        TrackBank trackBank = mock(TrackBank.class);
        track = mock(Track.class, RETURNS_DEEP_STUBS);
        DeviceBank deviceBank = mock(DeviceBank.class);
        device = mock(Device.class, RETURNS_DEEP_STUBS);

        when(trackBank.getSizeOfBank()).thenReturn(1);
        when(trackBank.getItemAt(0)).thenReturn(track);
        when(track.exists().get()).thenReturn(true);
        when(track.name().get()).thenReturn("Vox");
        when(track.trackType().get()).thenReturn("Audio");
        when(deviceBank.getSizeOfBank()).thenReturn(1);
        when(deviceBank.getItemAt(0)).thenReturn(device);
        when(device.exists().get()).thenReturn(true);
        when(device.name().get()).thenReturn("Reverb");

        // fader
        Parameter volume = mock(Parameter.class);
        SettableRangedValue volumeValue = rangedValue(fader::get, fader::set);
        when(volume.value()).thenReturn(volumeValue);
        StringValue volumeDisplay = stringValue(() -> faderDisplay(fader.get()));
        when(volume.displayedValue()).thenReturn(volumeDisplay);
        when(track.volume()).thenReturn(volume);

        // clip slot
        ClipLauncherSlotBank slots = mock(ClipLauncherSlotBank.class);
        slot0 = mock(ClipLauncherSlot.class, RETURNS_DEEP_STUBS);
        when(slots.getSizeOfBank()).thenReturn(4);
        when(slots.getItemAt(anyInt())).thenReturn(slot0);
        when(slot0.hasContent().get()).thenAnswer(i -> slotHasContent.get());
        doAnswer(i -> {
            slotHasContent.set(true);
            return null;
        }).when(slot0).createEmptyClip(anyInt());
        when(track.clipLauncherSlotBank()).thenReturn(slots);

        // private edit cursor: track, device, remote-control page, launcher clip
        CursorTrack editTrack = mock(CursorTrack.class, RETURNS_DEEP_STUBS);
        PinnableCursorDevice editDevice = mock(PinnableCursorDevice.class, RETURNS_DEEP_STUBS);
        CursorRemoteControlsPage page = mock(CursorRemoteControlsPage.class, RETURNS_DEEP_STUBS);
        clip = mock(PinnableCursorClip.class, RETURNS_DEEP_STUBS);
        when(host.createCursorTrack(anyString(), anyString(), anyInt(), anyInt(), anyBoolean())).thenReturn(editTrack);
        when(editTrack.createCursorDevice(anyString(), anyString(), anyInt(), any())).thenReturn(editDevice);
        when(editDevice.createCursorRemoteControlsPage(8)).thenReturn(page);
        when(editTrack.createLauncherCursorClip(anyInt(), anyInt())).thenReturn(clip);
        when(clip.exists().get()).thenReturn(true);

        when(editTrack.name().get()).thenReturn("Vox");
        when(editDevice.exists().get()).thenReturn(true);
        when(editDevice.position().get()).thenReturn(0);
        when(editDevice.name().get()).thenReturn("Reverb");

        String[] pageNames = {"Main", "Tone", "Common"};
        when(page.pageNames().get()).thenReturn(pageNames);
        SettableIntegerValue pageIndex = mock(SettableIntegerValue.class);
        when(pageIndex.get()).thenAnswer(i -> selectedPage.get());
        doAnswer(i -> {
            selectedPage.set(i.getArgument(0));
            return null;
        }).when(pageIndex).set(anyInt());
        when(page.selectedPageIndex()).thenReturn(pageIndex);
        when(page.getParameterCount()).thenReturn(8);
        for (int i = 0; i < 8; i++) {
            final int slot = i;
            RemoteControl rc = mock(RemoteControl.class, RETURNS_DEEP_STUBS);
            when(rc.exists().get()).thenAnswer(x -> slot < PAGES[selectedPage.get()].length);
            when(rc.name().get()).thenAnswer(x -> slot < PAGES[selectedPage.get()].length ? PAGES[selectedPage.get()][slot] : "");
            SettableRangedValue v = rangedValue(() -> values[selectedPage.get()][slot],
                d -> values[selectedPage.get()][slot] = d);
            when(rc.value()).thenReturn(v);
            StringValue display = stringValue(() -> String.format(java.util.Locale.ROOT, "%.1f %%", values[selectedPage.get()][slot] * 100));
            when(rc.displayedValue()).thenReturn(display);
            when(page.getParameter(i)).thenReturn(rc);
        }

        facade = new BitwigEditFacade(host, mock(Logger.class), application, transport, trackBank,
            List.of(deviceBank), 0);
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> list(Object o) {
        return (List<Map<String, Object>>) o;
    }

    @Test
    void readsEveryPage() throws Exception {
        Map<String, Object> r = facade.getDeviceParameters(0, 0, null);
        assertArrayEquals(new String[]{"Main", "Tone", "Common"}, (String[]) r.get("page_names"));
        List<Map<String, Object>> pages = list(r.get("pages"));
        assertEquals(3, pages.size());
        assertEquals(8, list(pages.get(0).get("parameters")).size());
        assertEquals(3, list(pages.get(1).get("parameters")).size());
        assertEquals("Gain", list(pages.get(2).get("parameters")).get(0).get("name"));
    }

    @Test
    void readsOnePage() throws Exception {
        Map<String, Object> r = facade.getDeviceParameters(0, 0, 1);
        List<Map<String, Object>> pages = list(r.get("pages"));
        assertEquals(1, pages.size());
        assertEquals("Tone", pages.get(0).get("page_name"));
        assertThrows(BitwigApiException.class, () -> facade.getDeviceParameters(0, 0, 3));
    }

    /** Regression: value().set() is silently ignored by Bitwig; the write must use setImmediately(). */
    @Test
    void setsByPageAndIndexAndActuallyApplies() throws Exception {
        Map<String, Object> r = facade.setDeviceParameters(0, 0,
            List.of(new BitwigEditFacade.ParamAssignment(0, 1, null, 0.9)));
        Map<String, Object> result = list(r.get("results")).get(0);
        assertEquals("success", result.get("status"));
        assertEquals(true, result.get("applied"));
        assertEquals("Pre-delay", result.get("name"));
        assertEquals(0.9, values[0][1], 1e-9);
    }

    @Test
    void setsByNameSearchingAcrossPages() throws Exception {
        Map<String, Object> r = facade.setDeviceParameters(0, 0,
            List.of(new BitwigEditFacade.ParamAssignment(null, null, "damping", 0.25)));
        Map<String, Object> result = list(r.get("results")).get(0);
        assertEquals(1, result.get("page"));
        assertEquals("Damping", result.get("name"));
        assertEquals(0.25, values[1][2], 1e-9);
    }

    @Test
    void eachAssignmentFailsOnItsOwn() throws Exception {
        Map<String, Object> r = facade.setDeviceParameters(0, 0, List.of(
            new BitwigEditFacade.ParamAssignment(null, null, "Nope", 0.5),
            new BitwigEditFacade.ParamAssignment(0, 7, null, 1.5),
            new BitwigEditFacade.ParamAssignment(2, 5, null, 0.5),
            new BitwigEditFacade.ParamAssignment(0, 7, null, 0.4)));
        List<Map<String, Object>> results = list(r.get("results"));
        assertEquals("error", results.get(0).get("status"));
        assertEquals("error", results.get(1).get("status"));
        assertEquals("error", results.get(2).get("status")); // page 2 has only one control
        assertEquals("success", results.get(3).get("status"));
        assertEquals(0.4, values[0][7], 1e-9);
    }

    @Test
    void focusFailsWhenCursorLandsOnAnotherDevice() {
        when(device.name().get()).thenReturn("EQ+"); // cursor still reports "Reverb"
        BitwigApiException e = assertThrows(BitwigApiException.class, () -> facade.getDeviceParameters(0, 0, null));
        assertEquals(ErrorCode.BITWIG_TIMEOUT, e.getErrorCode());
    }

    @Test
    void volumeDbLandsOnTheClosestDisplayedValue() throws Exception {
        for (double target : new double[]{-3.0, 0.0, -12.5, 4.0}) {
            facade.setTrackProperties(0, null, null, target, null, null, null, null, null, null, null);
            double shown = BitwigEditFacade.parseDb(faderDisplay(fader.get()));
            assertEquals(target, shown, 0.051, "target " + target);
        }
    }

    @Test
    void createClipThenWriteNotesOnTheGrid() throws Exception {
        assertEquals(true, facade.createClip(0, 0, 4).get("created"));
        assertThrows(BitwigApiException.class, () -> facade.createClip(0, 0, 4)); // occupied now

        Map<String, Object> r = facade.writeNotes(0, 0, List.of(
            new BitwigEditFacade.Note(0.0, 60, 100, 1.0),
            new BitwigEditFacade.Note(1.5, 64, 200, 0.5),   // velocity clamped
            new BitwigEditFacade.Note(0.25, 128, 100, 1.0)  // out-of-range pitch skipped
        ), true);
        assertEquals(2, r.get("notes_written"));
        verify(clip).clearSteps();
        verify(clip).setStep(0, 60, 100, 1.0);
        verify(clip).setStep(24, 64, 127, 0.5); // 1.5 beats / (1/16 beat)
        verify(slot0).select();
    }

    @Test
    void writeNotesNeedsAClip() {
        BitwigApiException e = assertThrows(BitwigApiException.class,
            () -> facade.writeNotes(0, 0, List.of(new BitwigEditFacade.Note(0, 60, 100, 1)), false));
        assertEquals(ErrorCode.CLIP_NOT_FOUND, e.getErrorCode());
    }

    @Test
    void insertFileIntoSlot(@TempDir Path dir) throws Exception {
        Path midi = Files.write(dir.resolve("chord.mid"), new byte[]{0x4d, 0x54, 0x68, 0x64});
        InsertionPoint ip = mock(InsertionPoint.class);
        when(slot0.replaceInsertionPoint()).thenReturn(ip);
        doAnswer(i -> {
            slotHasContent.set(true);
            return null;
        }).when(ip).insertFile(anyString());
        Map<String, Object> r = facade.insertFile(0, 2, midi.toString());
        verify(ip).insertFile(midi.toString());
        assertEquals(true, r.get("inserted"));
        assertThrows(BitwigApiException.class, () -> facade.insertFile(0, 99, midi.toString()));
        assertNull(r.get("new_tracks_created"));
    }

    @Test
    void setTransportOnlyTouchesGivenFields() {
        facade.setTransport(128.0, null, true, null, null, null);
        verify(transport.tempo()).setRaw(128.0);
        verify(transport.isArrangerLoopEnabled()).set(true);
        verify(transport.isMetronomeEnabled(), never()).set(anyBoolean());
        verify(transport.getPosition(), never()).set(anyDouble());
    }

    @Test
    void deleteDeviceAndDuplicateTrack() throws Exception {
        assertEquals("Reverb", facade.deleteDevice(0, 0).get("deleted_device"));
        verify(device).deleteObject();
        facade.duplicateTrack(0);
        verify(track).duplicate();
    }
}
