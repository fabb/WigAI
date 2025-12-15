package io.github.fabb.wigai.mcp.tool;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.fabb.wigai.bitwig.BitwigApiFacade;
import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.common.logging.StructuredLogger;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ListTracksTool exercising the real handler.
 */
class ListTracksToolTest {

    @Mock
    private BitwigApiFacade bitwigApiFacade;
    @Mock
    private StructuredLogger structuredLogger;
    @Mock
    private Logger baseLogger;
    @Mock
    private StructuredLogger.TimedOperation timedOperation;
    @Mock
    private McpSyncServerExchange exchange;

    private McpServerFeatures.SyncToolSpecification specification;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(structuredLogger.getBaseLogger()).thenReturn(baseLogger);
        when(structuredLogger.generateOperationId()).thenReturn("op-123");
        when(structuredLogger.startTimedOperation(any(), any(), any())).thenReturn(timedOperation);
        specification = ListTracksTool.specification(bitwigApiFacade, structuredLogger);
    }

    @Test
    void testSpecificationMetadata() {
        assertNotNull(specification);
        assertEquals("list_tracks", specification.tool().name());
        assertTrue(specification.tool().description().contains("List all tracks"));
        assertNotNull(specification.tool().inputSchema());
        assertNotNull(specification.callHandler());
    }

    @Test
    void testHandlerReturnsTrackData() throws Exception {
        when(bitwigApiFacade.getAllTracksInfo(null)).thenReturn(createMockTrackData());

        McpSchema.CallToolResult result = specification.callHandler().apply(
            exchange,
            buildRequest(Map.of())
        );

        JsonNode data = McpResponseTestUtils.validateListResponse(result);
        assertEquals(2, data.size());
        assertEquals("Test Track 1", data.get(0).get("name").asText());
        assertEquals("audio", data.get(0).get("type").asText());
        verify(bitwigApiFacade).getAllTracksInfo(null);
    }

    @Test
    void testHandlerPassesTypeFilter() throws Exception {
        when(bitwigApiFacade.getAllTracksInfo("audio")).thenReturn(createMockTrackData());

        McpSchema.CallToolResult result = specification.callHandler().apply(
            exchange,
            buildRequest(Map.of("type", "audio"))
        );

        JsonNode data = McpResponseTestUtils.validateListResponse(result);
        assertEquals(2, data.size());
        verify(bitwigApiFacade).getAllTracksInfo("audio");
    }

    @Test
    void testHandlerRejectsInvalidType() throws Exception {
        McpSchema.CallToolResult result = specification.callHandler().apply(
            exchange,
            buildRequest(Map.of("type", "invalid"))
        );

        JsonNode error = McpResponseTestUtils.validateErrorResponse(result);
        assertEquals("INVALID_PARAMETER", error.get("code").asText());
        assertEquals("list_tracks", error.get("operation").asText());
        verify(bitwigApiFacade, never()).getAllTracksInfo(any());
    }

    private McpSchema.CallToolRequest buildRequest(Map<String, Object> arguments) {
        return McpSchema.CallToolRequest.builder()
            .name("list_tracks")
            .arguments(arguments)
            .build();
    }

    /**
     * Helper method to create mock track data for testing.
     */
    private List<Map<String, Object>> createMockTrackData() {
        List<Map<String, Object>> tracks = new ArrayList<>();

        Map<String, Object> track1 = new LinkedHashMap<>();
        track1.put("index", 0);
        track1.put("name", "Test Track 1");
        track1.put("type", "audio");
        track1.put("is_group", false);
        track1.put("parent_group_index", null);
        track1.put("activated", true);
        track1.put("color", "rgb(255,128,0)");
        track1.put("is_selected", true);
        track1.put("devices", List.of(Map.of(
            "index", 0,
            "name", "EQ Eight",
            "type", "AudioFX"
        )));
        tracks.add(track1);

        Map<String, Object> track2 = new LinkedHashMap<>();
        track2.put("index", 1);
        track2.put("name", "Test Track 2");
        track2.put("type", "instrument");
        track2.put("is_group", false);
        track2.put("parent_group_index", null);
        track2.put("activated", true);
        track2.put("color", "rgb(0,255,128)");
        track2.put("is_selected", false);
        track2.put("devices", List.of(Map.of(
            "index", 0,
            "name", "Wavetable",
            "type", "Instrument"
        )));
        tracks.add(track2);

        return tracks;
    }
}
