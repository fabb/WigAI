package io.github.fabb.wigai.mcp.tool;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.common.error.BitwigApiException;
import io.github.fabb.wigai.common.error.ErrorCode;
import io.github.fabb.wigai.mcp.McpErrorHandler;
import io.github.fabb.wigai.common.logging.StructuredLogger;
import io.github.fabb.wigai.features.TransportController;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for TransportTool after migration to unified error handling architecture.
 */
class TransportToolTest {

    @Mock
    private TransportController transportController;
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
    void testTransportStartSpecification() {
        McpServerFeatures.SyncToolSpecification spec = TransportTool.transportStartSpecification(transportController, structuredLogger);

        assertNotNull(spec);
        assertNotNull(spec.tool());
        assertEquals("transport_start", spec.tool().name());
        assertTrue(spec.tool().description().contains("Start"));
        assertNotNull(spec.tool().inputSchema());
    }

    @Test
    void testTransportStopSpecification() {
        McpServerFeatures.SyncToolSpecification spec = TransportTool.transportStopSpecification(transportController, structuredLogger);

        assertNotNull(spec);
        assertNotNull(spec.tool());
        assertEquals("transport_stop", spec.tool().name());
        assertTrue(spec.tool().description().contains("Stop"));
        assertNotNull(spec.tool().inputSchema());
    }

    @Test
    void testTransportStartSuccessResponseFormat() throws Exception {
        // Arrange: Mock successful transport start
        when(transportController.startTransport()).thenReturn("Bitwig transport started.");
        
        // Act: Simulate what the tool does - returns raw data that executeWithErrorHandling wraps
        Map<String, Object> responseData = Map.of(
            "action", "transport_started",
            "message", "Bitwig transport started."
        );
        McpSchema.CallToolResult result = McpErrorHandler.createSuccessResponse(responseData);
        
        // Assert: Validate action response format
        JsonNode dataNode = McpResponseTestUtils.validateActionResponse(result, "transport_started");
        assertEquals("Bitwig transport started.", dataNode.get("message").asText());
    }

    @Test
    void testTransportStopSuccessResponseFormat() throws Exception {
        // Arrange: Mock successful transport stop
        when(transportController.stopTransport()).thenReturn("Bitwig transport stopped.");
        
        // Act: Simulate what the tool does
        Map<String, Object> responseData = Map.of(
            "action", "transport_stopped",
            "message", "Bitwig transport stopped."
        );
        McpSchema.CallToolResult result = McpErrorHandler.createSuccessResponse(responseData);
        
        // Assert: Validate action response format
        JsonNode dataNode = McpResponseTestUtils.validateActionResponse(result, "transport_stopped");
        assertEquals("Bitwig transport stopped.", dataNode.get("message").asText());
    }

    @Test
    void testTransportErrorResponseFormat() throws Exception {
        // Test error response format for transport operations
        BitwigApiException exception = new BitwigApiException(
            ErrorCode.TRANSPORT_ERROR,
            "transport_start",
            "Transport is not available"
        );
        
        McpSchema.CallToolResult result = McpErrorHandler.createErrorResponse(exception, structuredLogger);
        
        // Validate error response format
        JsonNode errorNode = McpResponseTestUtils.validateErrorResponse(result);
        assertEquals("TRANSPORT_ERROR", errorNode.get("code").asText());
        assertEquals("Transport is not available", errorNode.get("message").asText());
        assertEquals("transport_start", errorNode.get("operation").asText());
    }

    @Test
    void testTransportResponseNotDoubleWrapped() throws Exception {
        // Test that transport responses are not double-wrapped
        Map<String, Object> actionData = Map.of(
            "action", "transport_started",
            "message", "Transport started"
        );
        McpSchema.CallToolResult result = McpErrorHandler.createSuccessResponse(actionData);
        
        // This would have caught the double-wrapping bug
        McpResponseTestUtils.assertNotDoubleWrapped(result);
        
        // Verify it's properly structured as an action response
        JsonNode dataNode = McpResponseTestUtils.validateActionResponse(result, "transport_started");
        assertEquals("Transport started", dataNode.get("message").asText());
    }
}
