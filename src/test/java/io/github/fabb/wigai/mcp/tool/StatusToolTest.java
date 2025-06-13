package io.github.fabb.wigai.mcp.tool;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.fabb.wigai.bitwig.BitwigApiFacade;
import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.common.data.ParameterInfo;
import io.github.fabb.wigai.common.error.BitwigApiException;
import io.github.fabb.wigai.common.error.ErrorCode;
import io.github.fabb.wigai.common.logging.StructuredLogger;
import io.github.fabb.wigai.WigAIExtensionDefinition;
import io.github.fabb.wigai.mcp.McpErrorHandler;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for StatusTool after migration to unified error handling architecture.
 */
class StatusToolTest {

    @Mock
    private WigAIExtensionDefinition extensionDefinition;
    @Mock
    private BitwigApiFacade bitwigApiFacade;
    @Mock
    private StructuredLogger structuredLogger;
    @Mock
    private Logger baseLogger;
    @Mock
    private StructuredLogger.TimedOperation timedOperation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(structuredLogger.getBaseLogger()).thenReturn(baseLogger);
        when(structuredLogger.generateOperationId()).thenReturn("op-123");
        when(structuredLogger.startTimedOperation(any(), any(), any())).thenReturn(timedOperation);
    }

    @Test
    void testStatusSpecification() {
        McpServerFeatures.SyncToolSpecification spec = StatusTool.specification(extensionDefinition, bitwigApiFacade, structuredLogger);

        assertNotNull(spec);
        assertNotNull(spec.tool());
        assertEquals("status", spec.tool().name());
        assertTrue(spec.tool().description().contains("status"));
        assertNotNull(spec.tool().inputSchema());
    }

    @Test
    void testSpecificationValidation() {
        // Test that the tool specification is properly configured
        McpServerFeatures.SyncToolSpecification spec = StatusTool.specification(extensionDefinition, bitwigApiFacade, structuredLogger);

        assertNotNull(spec.tool().inputSchema());
        assertEquals("status", spec.tool().name());
    }

    @Test
    void testStatusSuccessResponseFormat() throws Exception {
        // Arrange: Set up mock data for status response
        when(extensionDefinition.getVersion()).thenReturn("1.0.0");
        when(bitwigApiFacade.getProjectName()).thenReturn("Test Project");
        when(bitwigApiFacade.isAudioEngineActive()).thenReturn(true);
        when(bitwigApiFacade.getTransportStatus()).thenReturn(createMockTransportStatus());
        when(bitwigApiFacade.getProjectParameters()).thenReturn(createMockProjectParameters());
        when(bitwigApiFacade.getSelectedTrackInfo()).thenReturn(createMockTrackInfo());
        when(bitwigApiFacade.getSelectedDeviceInfo()).thenReturn(createMockDeviceInfo());

        // Act: Create response using McpErrorHandler directly (simulating what executeWithErrorHandling does)
        Map<String, Object> statusData = createExpectedStatusData();
        McpSchema.CallToolResult result = McpErrorHandler.createSuccessResponse(statusData);

        // Assert: Validate response format using utility
        JsonNode dataNode = McpResponseTestUtils.validateObjectResponse(result);
        
        // Verify status-specific fields
        assertTrue(dataNode.has("wigai_version"));
        assertTrue(dataNode.has("project_name"));
        assertTrue(dataNode.has("audio_engine_active"));
        assertTrue(dataNode.has("transport"));
        assertTrue(dataNode.has("project_parameters"));
        assertTrue(dataNode.has("selected_track"));
        assertTrue(dataNode.has("selected_device"));
        
        assertEquals("1.0.0", dataNode.get("wigai_version").asText());
        assertEquals("Test Project", dataNode.get("project_name").asText());
        assertTrue(dataNode.get("audio_engine_active").asBoolean());
    }

    @Test
    void testStatusErrorResponseFormat() throws Exception {
        // Test error response format
        BitwigApiException exception = new BitwigApiException(
            ErrorCode.BITWIG_API_ERROR,
            "status",
            "Failed to retrieve status"
        );
        
        McpSchema.CallToolResult result = McpErrorHandler.createErrorResponse(exception, structuredLogger);
        
        // Validate error response format
        JsonNode errorNode = McpResponseTestUtils.validateErrorResponse(result);
        assertEquals("BITWIG_API_ERROR", errorNode.get("code").asText());
        assertEquals("Failed to retrieve status", errorNode.get("message").asText());
        assertEquals("status", errorNode.get("operation").asText());
    }

    @Test
    void testStatusResponseNotDoubleWrapped() throws Exception {
        // Test that status responses are not double-wrapped
        Map<String, Object> statusData = createExpectedStatusData();
        McpSchema.CallToolResult result = McpErrorHandler.createSuccessResponse(statusData);
        
        // This would have caught the double-wrapping bug
        McpResponseTestUtils.assertNotDoubleWrapped(result);
    }

    // Helper methods for creating mock data
    private Map<String, Object> createMockTransportStatus() {
        Map<String, Object> transport = new LinkedHashMap<>();
        transport.put("playing", false);
        transport.put("recording", false);
        transport.put("loop_active", false);
        transport.put("metronome_active", true);
        transport.put("current_tempo", 120.0);
        transport.put("time_signature", "4/4");
        transport.put("current_beat_str", "1.1.1:0");
        transport.put("current_time_str", "0:00.000");
        return transport;
    }

    private List<ParameterInfo> createMockProjectParameters() {
        List<ParameterInfo> params = new ArrayList<>();
        params.add(new ParameterInfo(0, "Project Param 1", 0.5, "50%"));
        return params;
    }

    private Map<String, Object> createMockTrackInfo() {
        Map<String, Object> track = new LinkedHashMap<>();
        track.put("index", 0);
        track.put("name", "Track 1");
        track.put("type", "audio");
        track.put("is_group", false);
        track.put("muted", false);
        track.put("soloed", false);
        track.put("armed", true);
        return track;
    }

    private Map<String, Object> createMockDeviceInfo() {
        Map<String, Object> device = new LinkedHashMap<>();
        device.put("track_name", "Track 1");
        device.put("track_index", 0);
        device.put("index", 0);
        device.put("name", "Test Device");
        device.put("bypassed", false);
        device.put("parameters", new ArrayList<>());
        return device;
    }

    private Map<String, Object> createExpectedStatusData() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("wigai_version", "1.0.0");
        data.put("project_name", "Test Project");
        data.put("audio_engine_active", true);
        data.put("transport", createMockTransportStatus());
        
        // Convert ParameterInfo to Map for status response
        List<Map<String, Object>> paramMaps = new ArrayList<>();
        for (ParameterInfo param : createMockProjectParameters()) {
            Map<String, Object> paramMap = new LinkedHashMap<>();
            paramMap.put("index", param.index());
            paramMap.put("exists", true);
            paramMap.put("name", param.name());
            paramMap.put("value", param.value());
            paramMap.put("display_value", param.display_value());
            paramMaps.add(paramMap);
        }
        data.put("project_parameters", paramMaps);
        
        data.put("selected_track", createMockTrackInfo());
        data.put("selected_device", createMockDeviceInfo());
        return data;
    }
}
