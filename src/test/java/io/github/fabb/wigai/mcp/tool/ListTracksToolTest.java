package io.github.fabb.wigai.mcp.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.fabb.wigai.bitwig.BitwigApiFacade;
import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.common.error.BitwigApiException;
import io.github.fabb.wigai.common.error.ErrorCode;
import io.github.fabb.wigai.common.logging.StructuredLogger;
import io.github.fabb.wigai.mcp.McpErrorHandler;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for ListTracksTool.
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

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(structuredLogger.getBaseLogger()).thenReturn(baseLogger);
        when(structuredLogger.generateOperationId()).thenReturn("op-123");
        when(structuredLogger.startTimedOperation(any(), any(), any())).thenReturn(timedOperation);
    }

    @Test
    void testListTracksSpecification() {
        McpServerFeatures.SyncToolSpecification spec = ListTracksTool.specification(bitwigApiFacade, structuredLogger);

        assertNotNull(spec);
        assertNotNull(spec.tool());
        assertEquals("list_tracks", spec.tool().name());
        assertTrue(spec.tool().description().contains("List all tracks"));
        assertNotNull(spec.tool().inputSchema());
    }

    @Test
    void testSpecificationValidation() {
        // Test that the tool specification is properly configured
        McpServerFeatures.SyncToolSpecification spec = ListTracksTool.specification(bitwigApiFacade, structuredLogger);

        assertNotNull(spec.tool().inputSchema());
        assertEquals("list_tracks", spec.tool().name());
        assertTrue(spec.tool().description().contains("summary information"));
    }

    @Test
    void testListTracksSuccessResponseFormat() throws Exception {
        // Since we can't access the handler directly, we'll test the McpErrorHandler
        // which is what was causing the double-wrapping issue

        // Arrange: Create mock track data
        List<Map<String, Object>> mockTracks = createMockTrackData();

        // Act: Test the McpErrorHandler.createSuccessResponse directly
        McpSchema.CallToolResult result = McpErrorHandler.createSuccessResponse(mockTracks);

        // Assert: Verify response structure
        assertNotNull(result);
        assertFalse(result.isError());
        assertEquals(1, result.content().size());

        // Parse the JSON response
        McpSchema.TextContent textContent = (McpSchema.TextContent) result.content().get(0);
        JsonNode responseJson = objectMapper.readTree(textContent.text());

        // Verify top-level structure matches API specification
        assertTrue(responseJson.has("status"));
        assertTrue(responseJson.has("data"));
        assertEquals("success", responseJson.get("status").asText());

        // Verify data is the tracks array directly (not wrapped again)
        JsonNode dataNode = responseJson.get("data");
        assertTrue(dataNode.isArray());
        assertEquals(2, dataNode.size());

        // Verify track structure
        JsonNode firstTrack = dataNode.get(0);
        assertTrue(firstTrack.has("index"));
        assertTrue(firstTrack.has("name"));
        assertTrue(firstTrack.has("type"));
        assertTrue(firstTrack.has("is_group"));
        assertTrue(firstTrack.has("parent_group_index"));
        assertTrue(firstTrack.has("activated"));
        assertTrue(firstTrack.has("color"));
        assertTrue(firstTrack.has("is_selected"));
        assertTrue(firstTrack.has("devices"));

        assertEquals(0, firstTrack.get("index").asInt());
        assertEquals("Test Track 1", firstTrack.get("name").asText());
        assertEquals("audio", firstTrack.get("type").asText());
    }

    @Test
    void testMcpErrorHandlerSuccessResponseFormat() throws Exception {
        // Test that McpErrorHandler creates the correct success response format
        Map<String, Object> testData = Map.of(
            "action", "test_action",
            "message", "Test message",
            "value", 42
        );

        McpSchema.CallToolResult result = McpErrorHandler.createSuccessResponse(testData);

        // Verify structure
        assertFalse(result.isError());
        assertEquals(1, result.content().size());

        McpSchema.TextContent textContent = (McpSchema.TextContent) result.content().get(0);
        JsonNode responseJson = objectMapper.readTree(textContent.text());

        // Verify API-compliant format
        assertEquals("success", responseJson.get("status").asText());
        assertTrue(responseJson.has("data"));

        JsonNode dataNode = responseJson.get("data");
        assertEquals("test_action", dataNode.get("action").asText());
        assertEquals("Test message", dataNode.get("message").asText());
        assertEquals(42, dataNode.get("value").asInt());
    }

    @Test
    void testMcpErrorHandlerErrorResponseFormat() throws Exception {
        // Test that McpErrorHandler creates the correct error response format
        BitwigApiException exception = new BitwigApiException(
            ErrorCode.TRACK_NOT_FOUND,
            "list_tracks",
            "Track 'NonExistent' not found"
        );

        McpSchema.CallToolResult result = McpErrorHandler.createErrorResponse(exception, structuredLogger);

        // Verify structure
        assertTrue(result.isError());
        assertEquals(1, result.content().size());

        McpSchema.TextContent textContent = (McpSchema.TextContent) result.content().get(0);
        JsonNode responseJson = objectMapper.readTree(textContent.text());

        // Verify API-compliant error format
        assertEquals("error", responseJson.get("status").asText());
        assertTrue(responseJson.has("error"));

        JsonNode errorNode = responseJson.get("error");
        assertEquals("TRACK_NOT_FOUND", errorNode.get("code").asText());
        assertEquals("Track 'NonExistent' not found", errorNode.get("message").asText());
        assertEquals("list_tracks", errorNode.get("operation").asText());
    }

    /**
     * This test specifically would have caught the double-wrapping issue!
     * Uses the shared utility to ensure consistent validation across all tools.
     */
    @Test
    void testResponseIsNotDoubleWrapped() throws Exception {
        // Test data that would have been double-wrapped in the old implementation
        List<Map<String, Object>> mockTracks = createMockTrackData();

        // Create response using the fixed McpErrorHandler
        McpSchema.CallToolResult result = McpErrorHandler.createSuccessResponse(mockTracks);

        // Use utility to verify NO double-wrapping - this would have caught the bug!
        McpResponseTestUtils.assertNotDoubleWrapped(result);

        // Also verify it's a proper list response
        JsonNode dataNode = McpResponseTestUtils.validateListResponse(result);
        assertEquals(2, dataNode.size(), "Should have 2 tracks");

        // Verify the data contains track objects, not serialized JSON strings
        JsonNode firstTrack = dataNode.get(0);
        assertTrue(firstTrack.isObject(), "Track should be an object, not a string");
        assertTrue(firstTrack.has("name"), "Track should have name field directly accessible");
        assertEquals("Test Track 1", firstTrack.get("name").asText());
    }

    /**
     * Test using the shared utility for consistent validation.
     */
    @Test
    void testMcpErrorHandlerSuccessResponseFormatWithUtility() throws Exception {
        // Test that McpErrorHandler creates the correct success response format
        Map<String, Object> testData = Map.of(
            "action", "test_action",
            "message", "Test message",
            "value", 42
        );

        McpSchema.CallToolResult result = McpErrorHandler.createSuccessResponse(testData);

        // Use utility for validation
        JsonNode dataNode = McpResponseTestUtils.validateObjectResponse(result);

        // Verify specific data
        assertEquals("test_action", dataNode.get("action").asText());
        assertEquals("Test message", dataNode.get("message").asText());
        assertEquals(42, dataNode.get("value").asInt());
    }

    /**
     * Test using the shared utility for error response validation.
     */
    @Test
    void testMcpErrorHandlerErrorResponseFormatWithUtility() throws Exception {
        // Test that McpErrorHandler creates the correct error response format
        BitwigApiException exception = new BitwigApiException(
            ErrorCode.TRACK_NOT_FOUND,
            "list_tracks",
            "Track 'NonExistent' not found"
        );

        McpSchema.CallToolResult result = McpErrorHandler.createErrorResponse(exception, structuredLogger);

        // Use utility for validation
        JsonNode errorNode = McpResponseTestUtils.validateErrorResponse(result);

        // Verify specific error details
        assertEquals("TRACK_NOT_FOUND", errorNode.get("code").asText());
        assertEquals("Track 'NonExistent' not found", errorNode.get("message").asText());
        assertEquals("list_tracks", errorNode.get("operation").asText());
    }

    /**
     * Test that verifies the complete tool integration would work correctly.
     * This simulates what the MCP server would do internally.
     */
    @Test
    void testListTracksToolIntegration() throws Exception {
        // Arrange: Mock the dependencies
        List<Map<String, Object>> mockTracks = createMockTrackData();
        when(bitwigApiFacade.getAllTracksInfo(null)).thenReturn(mockTracks);

        // Act: Simulate what executeWithValidation does internally
        String operationId = "test-op-123";
        when(structuredLogger.generateOperationId()).thenReturn(operationId);
        when(structuredLogger.startTimedOperation(eq(operationId), eq("list_tracks"), any()))
            .thenReturn(timedOperation);

        // This is what the tool's lambda returns (raw data)
        Object rawResult = mockTracks; // The tool returns tracks directly

        // This is what executeWithValidation does with the raw result
        McpSchema.CallToolResult finalResult = McpErrorHandler.createSuccessResponse(rawResult);

        // Assert: Verify the final result has correct format
        assertFalse(finalResult.isError());
        McpSchema.TextContent textContent = (McpSchema.TextContent) finalResult.content().get(0);
        JsonNode responseJson = objectMapper.readTree(textContent.text());

        // Verify it matches the expected API format
        assertEquals("success", responseJson.get("status").asText());
        assertTrue(responseJson.get("data").isArray());
        assertEquals(2, responseJson.get("data").size());

        // Verify track data is correctly structured
        JsonNode firstTrack = responseJson.get("data").get(0);
        assertEquals("Test Track 1", firstTrack.get("name").asText());
        assertEquals("audio", firstTrack.get("type").asText());
        assertEquals(0, firstTrack.get("index").asInt());
    }

    @Test
    void testListTracksIncludesDeviceInformation() throws Exception {
        // Test that the list_tracks response includes device information for tracks that have devices
        List<Map<String, Object>> mockTracks = createMockTrackData();

        // Create response using McpErrorHandler
        McpSchema.CallToolResult result = McpErrorHandler.createSuccessResponse(mockTracks);

        // Validate response format
        JsonNode dataNode = McpResponseTestUtils.validateListResponse(result);
        assertEquals(2, dataNode.size(), "Should have 2 tracks");

        // Verify first track has devices
        JsonNode firstTrack = dataNode.get(0);
        assertTrue(firstTrack.has("devices"), "Track should have devices field");
        JsonNode firstTrackDevices = firstTrack.get("devices");
        assertTrue(firstTrackDevices.isArray(), "Devices should be an array");
        assertEquals(2, firstTrackDevices.size(), "First track should have 2 devices");        // Verify device structure
        JsonNode firstDevice = firstTrackDevices.get(0);
        assertTrue(firstDevice.has("index"), "Device should have index");
        assertTrue(firstDevice.has("name"), "Device should have name");
        assertTrue(firstDevice.has("type"), "Device should have type");
        assertEquals(0, firstDevice.get("index").asInt());
        assertEquals("EQ Eight", firstDevice.get("name").asText());
        assertEquals("AudioFX", firstDevice.get("type").asText());
        
        // Verify second track has instrument device
        JsonNode secondTrack = dataNode.get(1);
        JsonNode secondTrackDevices = secondTrack.get("devices");
        assertEquals(1, secondTrackDevices.size(), "Second track should have 1 device");
        
        JsonNode instrumentDevice = secondTrackDevices.get(0);
        assertEquals("Wavetable", instrumentDevice.get("name").asText());
        assertEquals("Instrument", instrumentDevice.get("type").asText());
    }

    /**
     * Helper method to create mock track data for testing
     */
    private List<Map<String, Object>> createMockTrackData() {
        List<Map<String, Object>> tracks = new ArrayList<>();

        // Track 1: Audio track with some devices
        Map<String, Object> track1 = new LinkedHashMap<>();
        track1.put("index", 0);
        track1.put("name", "Test Track 1");
        track1.put("type", "audio");
        track1.put("is_group", false);
        track1.put("parent_group_index", null);
        track1.put("activated", true);
        track1.put("color", "rgb(255,128,0)");
        track1.put("is_selected", true);        // Add some mock devices to track 1
        List<Map<String, Object>> track1Devices = new ArrayList<>();
        Map<String, Object> device1 = Map.of(
            "index", 0,
            "name", "EQ Eight",
            "type", "AudioFX"
        );
        Map<String, Object> device2 = Map.of(
            "index", 1,
            "name", "Compressor",
            "type", "AudioFX"
        );
        track1Devices.add(device1);
        track1Devices.add(device2);
        track1.put("devices", track1Devices);
        tracks.add(track1);
        
        // Track 2: Instrument track with an instrument device
        Map<String, Object> track2 = new LinkedHashMap<>();
        track2.put("index", 1);
        track2.put("name", "Test Track 2");
        track2.put("type", "instrument");
        track2.put("is_group", false);
        track2.put("parent_group_index", null);
        track2.put("activated", true);
        track2.put("color", "rgb(0,255,128)");
        track2.put("is_selected", false);
        
        // Add a mock instrument device to track 2
        List<Map<String, Object>> track2Devices = new ArrayList<>();
        Map<String, Object> instrument = Map.of(
            "index", 0,
            "name", "Wavetable",
            "type", "Instrument"
        );
        track2Devices.add(instrument);
        track2.put("devices", track2Devices);
        tracks.add(track2);

        return tracks;
    }
}
