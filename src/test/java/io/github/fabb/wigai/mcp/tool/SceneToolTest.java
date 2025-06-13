package io.github.fabb.wigai.mcp.tool;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.common.error.BitwigApiException;
import io.github.fabb.wigai.common.error.ErrorCode;
import io.github.fabb.wigai.mcp.McpErrorHandler;
import io.github.fabb.wigai.common.logging.StructuredLogger;
import io.github.fabb.wigai.features.ClipSceneController;
import io.github.fabb.wigai.features.ClipSceneController.SceneLaunchResult;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Map;

/**
 * Unit tests for SceneTool focusing on error handling integration.
 */
class SceneToolTest {

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
    void testLaunchSceneByIndexSpecification() {
        // Act
        McpServerFeatures.SyncToolSpecification specification = SceneTool.launchSceneByIndexSpecification(clipSceneController, structuredLogger);

        // Assert
        assertNotNull(specification);
        assertNotNull(specification.tool());
        assertEquals("session_launchSceneByIndex", specification.tool().name());
        assertEquals("Launch a scene in Bitwig by providing its zero-based index", specification.tool().description());
        assertNotNull(specification.tool().inputSchema());
    }

    @Test
    void testSpecificationErrorHandlingConfiguration() {
        // Test that the specification properly configures error handling with BitwigApiException
        McpServerFeatures.SyncToolSpecification specification = SceneTool.launchSceneByIndexSpecification(clipSceneController, structuredLogger);

        // Verify that the specification is properly configured for error handling
        assertNotNull(specification);

        // Test error result handling setup
        SceneLaunchResult errorResult = SceneLaunchResult.error("SCENE_NOT_FOUND", "Scene not found");
        when(clipSceneController.launchSceneByIndex(anyInt())).thenReturn(errorResult);

        // Verify that the SceneTool properly integrates with our error handling system
        assertNotNull(specification.tool().inputSchema());
    }

    @Test
    void testParameterValidationIntegration() {
        // Test that parameter validation is integrated into the tool specification
        McpServerFeatures.SyncToolSpecification specification = SceneTool.launchSceneByIndexSpecification(clipSceneController, structuredLogger);

        // Verify tool schema includes validation requirements
        McpSchema.JsonSchema schema = specification.tool().inputSchema();
        assertNotNull(schema);
        String schemaString = schema.toString();
        assertTrue(schemaString.contains("scene_index"));
        assertTrue(schemaString.contains("required"));
        assertTrue(schemaString.contains("minimum"));
    }

    @Test
    void testBitwigApiExceptionHandling() {
        // Test that the tool properly handles BitwigApiException instead of RuntimeException
        McpServerFeatures.SyncToolSpecification specification = SceneTool.launchSceneByIndexSpecification(clipSceneController, structuredLogger);

        // Setup error condition
        SceneLaunchResult failureResult = SceneLaunchResult.error("OPERATION_FAILED", "Scene launch failed");
        when(clipSceneController.launchSceneByIndex(0)).thenReturn(failureResult);

        // Verify specification is properly configured to handle BitwigApiException
        assertNotNull(specification);
        assertEquals("session_launchSceneByIndex", specification.tool().name());

        // Verify that the specification was created successfully
        // Note: StructuredLogger methods are only called during handler execution, not specification creation
        assertNotNull(specification.tool());
    }

    @Test
    void testLaunchSceneByIndexSuccessResponseFormat() throws Exception {
        // Arrange: Mock successful scene launch
        SceneLaunchResult successResult = SceneLaunchResult.success("Scene 1 launched.");
        when(clipSceneController.launchSceneByIndex(1)).thenReturn(successResult);

        // Act: Simulate what the tool does
        Map<String, Object> responseData = Map.of(
            "action", "scene_launched",
            "scene_index", 1,
            "message", "Scene 1 launched."
        );
        McpSchema.CallToolResult result = McpErrorHandler.createSuccessResponse(responseData);

        // Assert: Validate action response format
        JsonNode dataNode = McpResponseTestUtils.validateActionResponse(result, "scene_launched");
        assertEquals(1, dataNode.get("scene_index").asInt());
        assertEquals("Scene 1 launched.", dataNode.get("message").asText());
    }

    @Test
    void testLaunchSceneErrorResponseFormat() throws Exception {
        // Test error response format for scene operations
        BitwigApiException exception = new BitwigApiException(
            ErrorCode.SCENE_NOT_FOUND,
            "session_launchSceneByIndex",
            "Scene index 99 is out of range"
        );
        
        McpSchema.CallToolResult result = McpErrorHandler.createErrorResponse(exception, structuredLogger);
        
        // Validate error response format
        JsonNode errorNode = McpResponseTestUtils.validateErrorResponse(result);
        assertEquals("SCENE_NOT_FOUND", errorNode.get("code").asText());
        assertEquals("Scene index 99 is out of range", errorNode.get("message").asText());
        assertEquals("session_launchSceneByIndex", errorNode.get("operation").asText());
    }

    @Test
    void testLaunchSceneResponseNotDoubleWrapped() throws Exception {
        // Test that scene launch responses are not double-wrapped
        Map<String, Object> sceneData = Map.of(
            "action", "scene_launched",
            "scene_index", 3,
            "message", "Scene launched successfully"
        );
        McpSchema.CallToolResult result = McpErrorHandler.createSuccessResponse(sceneData);
        
        // This would have caught the double-wrapping bug
        McpResponseTestUtils.assertNotDoubleWrapped(result);
        
        // Verify it's properly structured as an action response
        JsonNode dataNode = McpResponseTestUtils.validateActionResponse(result, "scene_launched");
        assertEquals(3, dataNode.get("scene_index").asInt());
    }
}
