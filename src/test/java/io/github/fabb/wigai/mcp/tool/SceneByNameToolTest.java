package io.github.fabb.wigai.mcp.tool;

import io.github.fabb.wigai.common.Logger;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.fabb.wigai.bitwig.BitwigApiFacade;

class SceneByNameToolTest {
    @Mock
    private ClipSceneController clipSceneController;
    @Mock
    private Logger logger;
    @Mock
    private McpSyncServerExchange exchange;
    @Mock
    private BitwigApiFacade bitwigApiFacade;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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
    void testLaunchSceneByName_Success() throws Exception {
        String sceneName = "Verse 1";
        when(clipSceneController.launchSceneByName(sceneName)).thenReturn(SceneLaunchResult.success("Scene 'Verse 1' launched."));
        when(clipSceneController.getBitwigApiFacade()).thenReturn(bitwigApiFacade);
        when(bitwigApiFacade.findSceneByName(sceneName)).thenReturn(0);
        var handler = SceneByNameTool.handlerForTests(clipSceneController, logger);
        Map<String, Object> args = Map.of("scene_name", sceneName);
        var result = handler.apply(exchange, args);
        assertFalse(result.isError());
        ObjectMapper mapper = new ObjectMapper();
        var jsonNode = mapper.readTree(getJsonFromResult(result));
        assertEquals("success", jsonNode.get("status").asText());
        var data = jsonNode.get("data");
        assertEquals("scene_launched", data.get("action").asText());
        assertEquals(sceneName, data.get("scene_name").asText());
        assertEquals(0, data.get("launched_scene_index").asInt());
    }

    @Test
    void testLaunchSceneByName_SceneNotFound() throws Exception {
        String sceneName = "NonExistent";
        when(clipSceneController.launchSceneByName(sceneName)).thenReturn(SceneLaunchResult.error("SCENE_NOT_FOUND", "Scene not found"));
        var handler = SceneByNameTool.handlerForTests(clipSceneController, logger);
        Map<String, Object> args = Map.of("scene_name", sceneName);
        var result = handler.apply(exchange, args);
        assertTrue(result.isError());
        ObjectMapper mapper = new ObjectMapper();
        var jsonNode = mapper.readTree(getJsonFromResult(result));
        assertEquals("SCENE_NOT_FOUND", jsonNode.get("errorCode").asText());
    }

    @Test
    void testLaunchSceneByName_EmptyName() throws Exception {
        var handler = SceneByNameTool.handlerForTests(clipSceneController, logger);
        Map<String, Object> args = Map.of("scene_name", "");
        var result = handler.apply(exchange, args);
        assertTrue(result.isError());
        ObjectMapper mapper = new ObjectMapper();
        var jsonNode = mapper.readTree(getJsonFromResult(result));
        assertEquals("SCENE_NOT_FOUND", jsonNode.get("errorCode").asText());
    }
}
