package io.github.fabb.wigai.mcp.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.fabb.wigai.bitwig.BitwigContainerFacade;
import io.github.fabb.wigai.bitwig.BitwigEditFacade;
import io.github.fabb.wigai.bitwig.BitwigProjectFacade;
import io.github.fabb.wigai.bitwig.BitwigSessionFacade;
import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.common.error.BitwigApiException;
import io.github.fabb.wigai.common.error.ErrorCode;
import io.github.fabb.wigai.common.logging.StructuredLogger;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EditToolsTest {

    private BitwigEditFacade facade;
    private List<McpServerFeatures.SyncToolSpecification> specs;

    @BeforeEach
    void setUp() {
        facade = mock(BitwigEditFacade.class);
        specs = EditTools.specifications(facade, new StructuredLogger(mock(Logger.class), "test"));
    }

    private McpSchema.CallToolResult call(String name, Map<String, Object> args) {
        var spec = specs.stream().filter(s -> s.tool().name().equals(name)).findFirst().orElseThrow();
        return spec.callHandler().apply(null, new McpSchema.CallToolRequest(name, args));
    }

    @Test
    void toolNamesAreUniqueAndSchemasAreValidJson() throws Exception {
        ObjectMapper om = new ObjectMapper();
        Set<String> names = new HashSet<>();
        for (var s : specs) {
            assertTrue(names.add(s.tool().name()), "duplicate tool " + s.tool().name());
            String json = om.writeValueAsString(s.tool().inputSchema());
            assertEquals("object", om.readTree(json).get("type").asText());
        }
        assertTrue(names.containsAll(List.of("create_track", "insert_device", "set_device_parameters",
            "get_device_parameters", "set_track", "write_notes", "invoke_action", "undo")));
    }

    @Test
    void createTrackPassesArguments() throws Exception {
        when(facade.createTrack("instrument", 2, "Chords")).thenReturn(Map.of("track_index", 2));
        var r = call("create_track", Map.of("type", "instrument", "position", 2, "name", "Chords"));
        assertEquals(2, McpResponseTestUtils.validateSuccessResponse(r).get("track_index").asInt());
    }

    @Test
    void missingRequiredArgumentIsAnError() {
        var r = call("create_track", Map.of());
        assertTrue(r.isError());
        verifyNoInteractions(facade);
    }

    @Test
    void wrongTypeIsAnError() {
        var r = call("create_track", Map.of("type", 5));
        assertTrue(r.isError());
    }

    @Test
    void trackNameResolvesThroughFacade() throws Exception {
        when(facade.resolveTrackIndex(null, "Up Vocals", "set_device_enabled")).thenReturn(13);
        when(facade.setDeviceEnabled(13, 4, null, null, true)).thenReturn(Map.of("enabled", true));
        var r = call("set_device_enabled", Map.of("track_name", "Up Vocals", "device_index", 4, "enabled", true));
        McpResponseTestUtils.validateSuccessResponse(r);
        verify(facade).setDeviceEnabled(13, 4, null, null, true);
    }

    @Test
    void facadeErrorsBecomeErrorResponses() throws Exception {
        when(facade.resolveTrackIndex(any(), any(), anyString()))
            .thenThrow(new BitwigApiException(ErrorCode.TRACK_NOT_FOUND, "op", "No track named 'x'"));
        var r = call("delete_track", Map.of("track_name", "x"));
        assertTrue(r.isError());
        String text = ((McpSchema.TextContent) r.content().get(0)).text();
        assertTrue(text.contains("TRACK_NOT_FOUND"));
    }

    @Test
    void setDeviceParametersBuildsAssignments() throws Exception {
        when(facade.resolveTrackIndex(1, null, "set_device_parameters")).thenReturn(1);
        when(facade.setDeviceParameters(eq(1), eq(0), isNull(), isNull(), anyList(), eq(true))).thenReturn(Map.of());
        var r = call("set_device_parameters", Map.of("track_index", 1, "device_index", 0,
            "parameters", List.of(Map.of("name", "Mix", "value", 0.35), Map.of("page", 1, "index", 2, "value", 1))));
        McpResponseTestUtils.validateSuccessResponse(r);
        verify(facade).setDeviceParameters(eq(1), eq(0), isNull(), isNull(), eq(List.of(
            new BitwigEditFacade.ParamAssignment(null, null, "Mix", 0.35),
            new BitwigEditFacade.ParamAssignment(1, 2, null, 1.0))), eq(true));
    }

    @Test
    void chainArgumentsReachTheFacade() throws Exception {
        when(facade.resolveTrackIndex(null, "Vocoder Stack", "insert_device")).thenReturn(14);
        when(facade.insertDevice(14, 0, "Modulator", "Audio Receiver", null, null, null, null, null, null)).thenReturn(Map.of());
        McpResponseTestUtils.validateSuccessResponse(call("insert_device", Map.of("track_name", "Vocoder Stack",
            "device_index", 0, "chain", "Modulator", "bitwig_device", "Audio Receiver")));

        when(facade.resolveTrackIndex(14, null, "move_device")).thenReturn(14);
        when(facade.copyOrMoveDevice(14, 0, "Modulator", 0, 14, 0, "Carrier", null, null, true)).thenReturn(Map.of());
        McpResponseTestUtils.validateSuccessResponse(call("move_device", Map.of("from_track_index", 14,
            "from_device_index", 0, "from_chain", "Modulator", "from_chain_device_index", 0,
            "to_track_index", 14, "to_device_index", 0, "to_chain", "Carrier")));

        when(facade.resolveTrackIndex(14, null, "get_device_parameters")).thenReturn(14);
        when(facade.getDeviceParameters(14, 0, "Carrier", 1, null)).thenReturn(Map.of());
        McpResponseTestUtils.validateSuccessResponse(call("get_device_parameters", Map.of("track_index", 14,
            "device_index", 0, "chain", "Carrier", "chain_device_index", 1)));
    }

    @Test
    void setDeviceParametersRequiresValue() {
        var r = call("set_device_parameters", Map.of("track_index", 1, "device_index", 0,
            "parameters", List.of(Map.of("name", "Mix"))));
        assertTrue(r.isError());
    }

    @Test
    void writeNotesDefaultsVelocity() throws Exception {
        when(facade.resolveTrackIndex(0, null, "write_notes")).thenReturn(0);
        when(facade.writeNotes(eq(0), eq(0), anyList(), eq(true))).thenReturn(Map.of());
        var r = call("write_notes", Map.of("track_index", 0, "slot_index", 0, "clear_first", true,
            "notes", List.of(Map.of("start", 0, "pitch", 60, "duration", 1.5))));
        McpResponseTestUtils.validateSuccessResponse(r);
        verify(facade).writeNotes(0, 0, List.of(new BitwigEditFacade.Note(0, 60, 100, 1.5)), true);
    }

    @Test
    void presetFileReachesInsertAndReplace() throws Exception {
        when(facade.resolveTrackIndex(0, null, "insert_device")).thenReturn(0);
        when(facade.insertDevice(0, null, null, null, null, null, null, "/p/Kit.bwpreset", null, null)).thenReturn(Map.of());
        McpResponseTestUtils.validateSuccessResponse(call("insert_device",
            Map.of("track_index", 0, "preset_file", "/p/Kit.bwpreset")));

        when(facade.resolveTrackIndex(1, null, "replace_device")).thenReturn(1);
        when(facade.replaceDevice(1, 2, "FX", 0, "Delay+", null, null, null, null)).thenReturn(Map.of());
        McpResponseTestUtils.validateSuccessResponse(call("replace_device", Map.of("track_index", 1, "device_index", 2,
            "chain", "FX", "chain_device_index", 0, "bitwig_device", "Delay+")));
        verify(facade).replaceDevice(1, 2, "FX", 0, "Delay+", null, null, null, null);
    }

    private McpSchema.CallToolResult callContainer(BitwigContainerFacade c, String name, Map<String, Object> args) {
        var spec = EditTools.containerSpecifications(facade, c, new StructuredLogger(mock(Logger.class), "test")).stream()
            .filter(s -> s.tool().name().equals(name)).findFirst().orElseThrow();
        return spec.callHandler().apply(null, new McpSchema.CallToolRequest(name, args));
    }

    @Test
    void containerToolsPassArguments() throws Exception {
        BitwigContainerFacade c = mock(BitwigContainerFacade.class);
        ObjectMapper om = new ObjectMapper();
        for (var s : EditTools.containerSpecifications(facade, c, new StructuredLogger(mock(Logger.class), "test"))) {
            assertEquals("object", om.readTree(om.writeValueAsString(s.tool().inputSchema())).get("type").asText());
        }
        when(facade.resolveTrackIndex(null, "Drums", "set_drum_pad")).thenReturn(3);
        when(c.setDrumPad(3, 0, 36, "Kick", null, -6.0, -0.5, true, null, null)).thenReturn(Map.of());
        McpResponseTestUtils.validateSuccessResponse(callContainer(c, "set_drum_pad", Map.of("track_name", "Drums",
            "device_index", 0, "pad", 36, "name", "Kick", "volume_db", -6, "pan", -0.5, "mute", true)));
        verify(c).setDrumPad(3, 0, 36, "Kick", null, -6.0, -0.5, true, null, null);

        when(c.filterBrowser("Bitwig Presets", Map.of("category", "Reverb"))).thenReturn(Map.of());
        McpResponseTestUtils.validateSuccessResponse(callContainer(c, "browser_filter",
            Map.of("content_type", "Bitwig Presets", "filters", Map.of("category", "Reverb"))));

        assertTrue(callContainer(c, "browser_filter", Map.of("filters", Map.of("category", 5))).isError());
        assertTrue(callContainer(c, "set_drum_pad", Map.of("track_index", 0, "device_index", 0)).isError());
    }

    private McpSchema.CallToolResult callNamed(List<McpServerFeatures.SyncToolSpecification> all, String name,
                                               Map<String, Object> args) {
        var spec = all.stream().filter(s -> s.tool().name().equals(name)).findFirst().orElseThrow();
        return spec.callHandler().apply(null, new McpSchema.CallToolRequest(name, args));
    }

    @Test
    void arrangerClipToolsSkipTrackResolution() throws Exception {
        BitwigSessionFacade s = mock(BitwigSessionFacade.class);
        var log = new StructuredLogger(mock(Logger.class), "test");
        var all = new java.util.ArrayList<>(EditTools.sessionSpecifications(facade, s, log));
        all.addAll(EditTools.projectSpecifications(facade, s, mock(BitwigProjectFacade.class), log));

        when(s.readNotes(null, null, true)).thenReturn(Map.of());
        McpResponseTestUtils.validateSuccessResponse(callNamed(all, "read_notes", Map.of("arranger", true)));
        verify(s).readNotes(null, null, true);
        verify(facade, never()).resolveTrackIndex(any(), any(), anyString());

        when(s.editNotes(eq(null), eq(null), eq(true), any(), any())).thenReturn(Map.of());
        McpResponseTestUtils.validateSuccessResponse(callNamed(all, "edit_notes", Map.of("arranger", true,
            "occurrence", "not_first", "recurrence_length", 4, "recurrence_mask", 5, "release_velocity", 0.8)));
        var changes = org.mockito.ArgumentCaptor.forClass(BitwigSessionFacade.NoteChanges.class);
        verify(s).editNotes(eq(null), eq(null), eq(true), any(), changes.capture());
        assertEquals("not_first", changes.getValue().occurrence());
        assertEquals(4, changes.getValue().recurrenceLength());
        assertEquals(5, changes.getValue().recurrenceMask());
        assertEquals(0.8, changes.getValue().releaseVelocity());
    }

    @Test
    void setClipBuildsSettings() throws Exception {
        BitwigSessionFacade s = mock(BitwigSessionFacade.class);
        var all = EditTools.sessionSpecifications(facade, s, new StructuredLogger(mock(Logger.class), "test"));
        when(facade.resolveTrackIndex(2, null, "set_clip")).thenReturn(2);
        when(s.setClip(eq(2), eq(0), eq(false), any())).thenReturn(Map.of());
        McpResponseTestUtils.validateSuccessResponse(callNamed(all, "set_clip", Map.of("track_index", 2, "slot_index", 0,
            "shuffle", true, "accent", 0.7, "loop_enabled", false)));
        verify(s).setClip(2, 0, false, new BitwigSessionFacade.ClipSettings(null, null, null, false, null, null, true, 0.7, null));
    }
}
