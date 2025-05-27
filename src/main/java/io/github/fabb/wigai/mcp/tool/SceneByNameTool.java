package io.github.fabb.wigai.mcp.tool;

import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.features.ClipSceneController;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * MCP tool for launching scenes by name.
 * Implements the launch_scene_by_name MCP command as specified in the API reference.
 */
public class SceneByNameTool {
    private static final String TOOL_NAME = "session_launchSceneByName";

    public static BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> handlerForTests(
            ClipSceneController clipSceneController, Logger logger) {
        return (exchange, arguments) -> handleLaunchSceneByName(arguments, clipSceneController, logger);
    }

    public static McpServerFeatures.SyncToolSpecification launchSceneByNameSpecification(ClipSceneController clipSceneController, Logger logger) {
        var schema = """
            {
              "type": "object",
              "properties": {
                "scene_name": {
                  "type": "string",
                  "minLength": 1,
                  "description": "Case-sensitive name of the scene to launch"
                }
              },
              "required": ["scene_name"]
            }""";

        var tool = new McpSchema.Tool(
            TOOL_NAME,
            "Launch a scene in Bitwig by providing its name (case-sensitive)",
            schema
        );

        BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> handler = (exchange, arguments) ->
            handleLaunchSceneByName(arguments, clipSceneController, logger);

        return new McpServerFeatures.SyncToolSpecification(tool, handler);
    }

    private static McpSchema.CallToolResult handleLaunchSceneByName(Map<String, Object> arguments, ClipSceneController clipSceneController, Logger logger) {
        try {
            String sceneName = (String) arguments.get("scene_name");
            logger.info("Received session_launchSceneByName command - Scene Name: " + sceneName);
            if (sceneName == null || sceneName.trim().isEmpty()) {
                logger.warn("Invalid scene_name: empty or null");
                return errorResponse("SCENE_NOT_FOUND", "scene_name must be a non-empty string");
            }
            var result = clipSceneController.launchSceneByName(sceneName);
            if (result.isSuccess()) {
                logger.info("Scene launch by name successful: " + result.getMessage());
                int launchedIndex = clipSceneController.getBitwigApiFacade().findSceneByName(sceneName);
                return successResponse(sceneName, launchedIndex, result.getMessage());
            } else {
                logger.error("Scene launch by name failed: " + result.getMessage());
                String code = "SCENE_NOT_FOUND".equals(result.getErrorCode()) ? "SCENE_NOT_FOUND" : "BITWIG_ERROR";
                return errorResponse(code, result.getMessage());
            }
        } catch (IllegalArgumentException e) {
            logger.error("Invalid arguments for launch_scene_by_name: " + e.getMessage());
            return errorResponse("SCENE_NOT_FOUND", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error in launch_scene_by_name: " + e.getMessage(), e);
            return errorResponse("BITWIG_ERROR", "Internal error occurred while launching scene");
        }
    }

    private static McpSchema.CallToolResult successResponse(String sceneName, int sceneIndex, String message) {
        String json = String.format(
            "{\"status\":\"success\",\"data\":{\"action\":\"scene_launched\",\"scene_name\":\"%s\",\"launched_scene_index\":%d,\"message\":\"%s\"}}",
            sceneName.replace("\"", "\\\""), sceneIndex, message.replace("\"", "\\\"")
        );
        McpSchema.TextContent textContent = new McpSchema.TextContent(json);
        return new McpSchema.CallToolResult(List.of(textContent), false);
    }

    private static McpSchema.CallToolResult errorResponse(String code, String message) {
        String json = String.format(
            "{\"isError\":true,\"errorCode\":\"%s\",\"message\":\"%s\"}",
            code, message.replace("\"", "\\\"")
        );
        McpSchema.TextContent textContent = new McpSchema.TextContent(json);
        return new McpSchema.CallToolResult(List.of(textContent), true);
    }
}
