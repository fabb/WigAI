package io.github.fabb.wigai.mcp.tool;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.common.error.BitwigApiException;
import io.github.fabb.wigai.common.error.ErrorCode;
import io.github.fabb.wigai.mcp.McpErrorHandler;
import io.github.fabb.wigai.common.logging.StructuredLogger;
import io.github.fabb.wigai.features.ProjectController;
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
 * Unit tests for ProjectTool using the unified error handling architecture.
 */
class ProjectToolTest {

    @Mock
    private ProjectController projectController;
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
    void testNextProjectSpecification() {
        McpServerFeatures.SyncToolSpecification spec = ProjectTool.nextProjectSpecification(projectController, structuredLogger);

        assertNotNull(spec);
        assertNotNull(spec.tool());
        assertEquals("next_project", spec.tool().name());
        assertTrue(spec.tool().description().contains("next"));
        assertNotNull(spec.tool().inputSchema());
    }

    @Test
    void testPreviousProjectSpecification() {
        McpServerFeatures.SyncToolSpecification spec = ProjectTool.previousProjectSpecification(projectController, structuredLogger);

        assertNotNull(spec);
        assertNotNull(spec.tool());
        assertEquals("previous_project", spec.tool().name());
        assertTrue(spec.tool().description().contains("previous"));
        assertNotNull(spec.tool().inputSchema());
    }

    @Test
    void testNextProjectSuccessResponseFormat() throws Exception {
        when(projectController.nextProject()).thenReturn("Switched to next project.");

        Map<String, Object> responseData = Map.of(
            "action", "project_switched_next",
            "message", "Switched to next project."
        );
        McpSchema.CallToolResult result = McpErrorHandler.createSuccessResponse(responseData);

        JsonNode dataNode = McpResponseTestUtils.validateActionResponse(result, "project_switched_next");
        assertEquals("Switched to next project.", dataNode.get("message").asText());
    }

    @Test
    void testPreviousProjectSuccessResponseFormat() throws Exception {
        when(projectController.previousProject()).thenReturn("Switched to previous project.");

        Map<String, Object> responseData = Map.of(
            "action", "project_switched_previous",
            "message", "Switched to previous project."
        );
        McpSchema.CallToolResult result = McpErrorHandler.createSuccessResponse(responseData);

        JsonNode dataNode = McpResponseTestUtils.validateActionResponse(result, "project_switched_previous");
        assertEquals("Switched to previous project.", dataNode.get("message").asText());
    }

    @Test
    void testProjectErrorResponseFormat() throws Exception {
        BitwigApiException exception = new BitwigApiException(
            ErrorCode.PROJECT_ERROR,
            "nextProject",
            "Project switching is not available"
        );

        McpSchema.CallToolResult result = McpErrorHandler.createErrorResponse(exception, structuredLogger);

        JsonNode errorNode = McpResponseTestUtils.validateErrorResponse(result);
        assertEquals("PROJECT_ERROR", errorNode.get("code").asText());
        assertEquals("Project switching is not available", errorNode.get("message").asText());
        assertEquals("nextProject", errorNode.get("operation").asText());
    }

    @Test
    void testProjectResponseNotDoubleWrapped() throws Exception {
        Map<String, Object> actionData = Map.of(
            "action", "project_switched_next",
            "message", "Switched to next project."
        );
        McpSchema.CallToolResult result = McpErrorHandler.createSuccessResponse(actionData);

        McpResponseTestUtils.assertNotDoubleWrapped(result);

        JsonNode dataNode = McpResponseTestUtils.validateActionResponse(result, "project_switched_next");
        assertEquals("Switched to next project.", dataNode.get("message").asText());
    }
}
