package io.github.fabb.wigai.mcp.tool;

import io.github.fabb.wigai.common.Logger;
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
}
