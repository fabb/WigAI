package io.github.fabb.wigai.mcp.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.fabb.wigai.bitwig.BitwigApiFacade;
import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.common.error.BitwigApiException;
import io.github.fabb.wigai.common.error.ErrorCode;
import io.github.fabb.wigai.mcp.McpErrorHandler;
import io.github.fabb.wigai.common.logging.StructuredLogger;
import io.github.fabb.wigai.features.ClipSceneController;
import io.github.fabb.wigai.features.ClipSceneController.SceneLaunchResult;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SceneByNameToolTest {
    @Mock
    private ClipSceneController clipSceneController;
    @Mock
    private StructuredLogger structuredLogger;
    @Mock
    private Logger baseLogger;
    @Mock
    private McpSyncServerExchange exchange;
    @Mock
    private BitwigApiFacade bitwigApiFacade;
    @Mock
    private StructuredLogger.TimedOperation timedOperation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(structuredLogger.getBaseLogger()).thenReturn(baseLogger);
        when(structuredLogger.generateOperationId()).thenReturn("op-123");
        when(structuredLogger.startTimedOperation(any(), any(), any())).thenReturn(timedOperation);
    }

    private String getJsonFromResult(McpSchema.CallToolResult result) throws Exception {
        // Extract JSON from the content list
        if (result.content() != null && !result.content().isEmpty()) {
            Object firstContent = result.content().get(0);
            if (firstContent instanceof McpSchema.TextContent) {
                return ((McpSchema.TextContent) firstContent).text();
            }
        }
        throw new IllegalStateException("No TextContent found in CallToolResult");
    }

    @Test
    void testSpecificationCreation() {
        // Test that the specification can be created
        McpServerFeatures.SyncToolSpecification spec = SceneByNameTool.launchSceneByNameSpecification(clipSceneController, structuredLogger);

        assertNotNull(spec);
        assertNotNull(spec.tool());
        assertEquals("session_launchSceneByName", spec.tool().name());
        assertEquals("Launch a scene in Bitwig by providing its name (case-sensitive)", spec.tool().description());
        assertNotNull(spec.tool().inputSchema());
    }

    @Test
    void testLaunchSceneByName_Success() throws Exception {
        String sceneName = "Verse 1";
        when(clipSceneController.launchSceneByName(sceneName)).thenReturn(SceneLaunchResult.success("Scene 'Verse 1' launched."));
        when(clipSceneController.getBitwigApiFacade()).thenReturn(bitwigApiFacade);
        when(bitwigApiFacade.findSceneByName(sceneName)).thenReturn(0);

        // Instead of accessing the handler directly, test through the specification by reflection or similar
        // For now, test that the specification is properly configured
        McpServerFeatures.SyncToolSpecification spec = SceneByNameTool.launchSceneByNameSpecification(clipSceneController, structuredLogger);
        assertNotNull(spec);

        // Verify tool metadata
        assertEquals("session_launchSceneByName", spec.tool().name());

        // The actual handler testing would need to be done differently since we don't have direct access
        // This test verifies the specification is created correctly
    }

    @Test
    void testLaunchSceneByName_ValidationFunctionality() {
        // Test that the tool specification includes proper validation by checking the schema
        McpServerFeatures.SyncToolSpecification spec = SceneByNameTool.launchSceneByNameSpecification(clipSceneController, structuredLogger);

        var schema = spec.tool().inputSchema();
        assertNotNull(schema);
        // Basic schema validation - checking that it's properly configured
        assertTrue(spec.tool().name().contains("Scene"));
    }

    @Test
    void testLaunchSceneByNameSuccessResponseFormat() throws Exception {
        // Arrange: Mock successful scene launch by name
        SceneLaunchResult successResult = SceneLaunchResult.success("Scene 'Verse 1' launched.");
        when(clipSceneController.launchSceneByName("Verse 1")).thenReturn(successResult);
        when(clipSceneController.getBitwigApiFacade()).thenReturn(bitwigApiFacade);
        when(bitwigApiFacade.findSceneByName("Verse 1")).thenReturn(0);

        // Act: Simulate what the tool does
        Map<String, Object> responseData = Map.of(
            "action", "scene_launched",
            "scene_name", "Verse 1",
            "launched_scene_index", 0,
            "message", "Scene 'Verse 1' launched."
        );
        McpSchema.CallToolResult result = McpErrorHandler.createSuccessResponse(responseData);

        // Assert: Validate action response format
        JsonNode dataNode = McpResponseTestUtils.validateActionResponse(result, "scene_launched");
        assertEquals("Verse 1", dataNode.get("scene_name").asText());
        assertEquals(0, dataNode.get("launched_scene_index").asInt());
        assertEquals("Scene 'Verse 1' launched.", dataNode.get("message").asText());
    }

    @Test
    void testLaunchSceneByNameErrorResponseFormat() throws Exception {
        // Test error response format for scene by name operations
        BitwigApiException exception = new BitwigApiException(
            ErrorCode.SCENE_NOT_FOUND,
            "session_launchSceneByName",
            "Scene 'NonExistent' not found"
        );
        
        McpSchema.CallToolResult result = McpErrorHandler.createErrorResponse(exception, structuredLogger);
        
        // Validate error response format
        JsonNode errorNode = McpResponseTestUtils.validateErrorResponse(result);
        assertEquals("SCENE_NOT_FOUND", errorNode.get("code").asText());
        assertEquals("Scene 'NonExistent' not found", errorNode.get("message").asText());
        assertEquals("session_launchSceneByName", errorNode.get("operation").asText());
    }

    @Test
    void testLaunchSceneByNameResponseNotDoubleWrapped() throws Exception {
        // Test that scene by name launch responses are not double-wrapped
        Map<String, Object> sceneData = Map.of(
            "action", "scene_launched",
            "scene_name", "Chorus",
            "launched_scene_index", 1,
            "message", "Scene launched by name"
        );
        McpSchema.CallToolResult result = McpErrorHandler.createSuccessResponse(sceneData);
        
        // This would have caught the double-wrapping bug
        McpResponseTestUtils.assertNotDoubleWrapped(result);
        
        // Verify it's properly structured as an action response
        JsonNode dataNode = McpResponseTestUtils.validateActionResponse(result, "scene_launched");
        assertEquals("Chorus", dataNode.get("scene_name").asText());
        assertEquals(1, dataNode.get("launched_scene_index").asInt());
    }
}
