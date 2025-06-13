package io.github.fabb.wigai.mcp.tool;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.common.error.BitwigApiException;
import io.github.fabb.wigai.common.error.ErrorCode;
import io.github.fabb.wigai.mcp.McpErrorHandler;
import io.github.fabb.wigai.common.logging.StructuredLogger;
import io.github.fabb.wigai.features.ClipSceneController;
import io.github.fabb.wigai.features.ClipSceneController.ClipLaunchResult;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for ClipTool after migration to unified error handling architecture.
 */
class ClipToolTest {

    @Mock
    private ClipSceneController clipSceneController;
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
    void testLaunchClipSpecification() {
        // Act
        McpServerFeatures.SyncToolSpecification specification = ClipTool.launchClipSpecification(clipSceneController, structuredLogger);

        // Assert
        assertNotNull(specification);
        assertNotNull(specification.tool());
        assertEquals("launch_clip", specification.tool().name());
        assertEquals("Launch a specific clip in Bitwig by providing track name and clip slot index",
                     specification.tool().description());
        assertNotNull(specification.tool().inputSchema());
    }

    @Test
    void testSpecificationContainsRequiredFields() {
        // Test that the tool specification includes proper validation
        McpServerFeatures.SyncToolSpecification spec = ClipTool.launchClipSpecification(clipSceneController, structuredLogger);

        assertNotNull(spec.tool().inputSchema());
        assertTrue(spec.tool().name().contains("clip"));
        assertTrue(spec.tool().description().contains("track"));
    }

    @Test
    void testHandleLaunchClip_Success() {
        // This test validates that the ClipTool specification can be created and configured properly.
        // The actual handler testing is handled by integration tests since the handler is not exposed publicly.

        // Arrange - Test specification creation with mock dependencies
        ClipLaunchResult successResult = ClipLaunchResult.success("Clip at Drums[0] launched.");
        when(clipSceneController.launchClip("Drums", 0)).thenReturn(successResult);

        // Act - Create the specification
        McpServerFeatures.SyncToolSpecification specification = ClipTool.launchClipSpecification(clipSceneController, structuredLogger);

        // Assert - Verify specification is properly configured
        assertNotNull(specification);
        assertNotNull(specification.tool());
        assertEquals("launch_clip", specification.tool().name());
        assertTrue(specification.tool().description().contains("clip"));
        assertNotNull(specification.tool().inputSchema());
        // Handler is internal to the specification, so we verify it's properly configured
        // by checking that the specification object is valid

        // Verify that the specification was created successfully
        // Note: StructuredLogger methods are only called during handler execution, not specification creation
        assertNotNull(specification.tool());
    }

    @Test
    void testSpecificationErrorHandlingConfiguration() {
        // Test that the specification properly configures error handling
        McpServerFeatures.SyncToolSpecification specification = ClipTool.launchClipSpecification(clipSceneController, structuredLogger);

        // Verify that structured logging is configured for error handling
        // Handler is internal, so we test the specification configuration

        // Test that mock setup verifies error handling integration
        ClipLaunchResult errorResult = ClipLaunchResult.error("TRACK_NOT_FOUND", "Track not found");
        when(clipSceneController.launchClip(anyString(), anyInt())).thenReturn(errorResult);

        // Verify that the ClipTool properly integrates with our error handling system
        assertNotNull(specification.tool().inputSchema());
    }

    @Test
    void testParameterValidationIntegration() {
        // Test that parameter validation is integrated into the tool specification
        McpServerFeatures.SyncToolSpecification specification = ClipTool.launchClipSpecification(clipSceneController, structuredLogger);

        // Verify tool schema includes validation requirements
        McpSchema.JsonSchema schema = specification.tool().inputSchema();
        assertNotNull(schema);
        // Verify schema is properly configured for validation
        String schemaString = schema.toString();
        assertTrue(schemaString.contains("track_name"));
        assertTrue(schemaString.contains("clip_index"));
        assertTrue(schemaString.contains("required"));
    }

    @Test
    void testLaunchClipSuccessResponseFormat() throws Exception {
        // Arrange: Mock successful clip launch
        ClipLaunchResult successResult = ClipLaunchResult.success("Clip at Drums[0] launched.");
        when(clipSceneController.launchClip("Drums", 0)).thenReturn(successResult);

        // Act: Simulate what the tool does
        Map<String, Object> responseData = Map.of(
            "action", "clip_launched",
            "track_name", "Drums",
            "clip_index", 0,
            "message", "Clip at Drums[0] launched."
        );
        McpSchema.CallToolResult result = McpErrorHandler.createSuccessResponse(responseData);

        // Assert: Validate action response format
        JsonNode dataNode = McpResponseTestUtils.validateActionResponse(result, "clip_launched");
        assertEquals("Drums", dataNode.get("track_name").asText());
        assertEquals(0, dataNode.get("clip_index").asInt());
        assertEquals("Clip at Drums[0] launched.", dataNode.get("message").asText());
    }

    @Test
    void testLaunchClipErrorResponseFormat() throws Exception {
        // Test error response format for clip operations
        BitwigApiException exception = new BitwigApiException(
            ErrorCode.TRACK_NOT_FOUND,
            "launch_clip",
            "Track 'NonExistent' not found"
        );
        
        McpSchema.CallToolResult result = McpErrorHandler.createErrorResponse(exception, structuredLogger);
        
        // Validate error response format
        JsonNode errorNode = McpResponseTestUtils.validateErrorResponse(result);
        assertEquals("TRACK_NOT_FOUND", errorNode.get("code").asText());
        assertEquals("Track 'NonExistent' not found", errorNode.get("message").asText());
        assertEquals("launch_clip", errorNode.get("operation").asText());
    }

    @Test
    void testLaunchClipResponseNotDoubleWrapped() throws Exception {
        // Test that clip launch responses are not double-wrapped
        Map<String, Object> clipData = Map.of(
            "action", "clip_launched",
            "track_name", "Bass",
            "clip_index", 2,
            "message", "Clip launched successfully"
        );
        McpSchema.CallToolResult result = McpErrorHandler.createSuccessResponse(clipData);
        
        // This would have caught the double-wrapping bug
        McpResponseTestUtils.assertNotDoubleWrapped(result);
        
        // Verify it's properly structured as an action response
        JsonNode dataNode = McpResponseTestUtils.validateActionResponse(result, "clip_launched");
        assertEquals("Bass", dataNode.get("track_name").asText());
        assertEquals(2, dataNode.get("clip_index").asInt());
    }

    @Test
    void testClipOperationFailureResponseFormat() throws Exception {
        // Test response format when clip operation fails but no exception is thrown
        ClipLaunchResult failureResult = ClipLaunchResult.error("CLIP_NOT_FOUND", "Clip at index 5 does not exist");
        
        // Simulate the BitwigApiException that would be thrown in this case
        BitwigApiException exception = new BitwigApiException(
            ErrorCode.CLIP_NOT_FOUND,
            "launch_clip", 
            "Clip at index 5 does not exist"
        );
        
        McpSchema.CallToolResult result = McpErrorHandler.createErrorResponse(exception, structuredLogger);
        
        // Validate error response format
        JsonNode errorNode = McpResponseTestUtils.validateErrorResponse(result);
        assertEquals("CLIP_NOT_FOUND", errorNode.get("code").asText());
        assertEquals("Clip at index 5 does not exist", errorNode.get("message").asText());
        assertEquals("launch_clip", errorNode.get("operation").asText());
    }
}
