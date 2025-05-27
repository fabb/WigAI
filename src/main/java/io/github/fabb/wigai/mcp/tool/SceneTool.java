package io.github.fabb.wigai.mcp.tool;

import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.features.ClipSceneController;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.function.BiFunction;

/**
 * MCP tool for launching scenes by index.
 * Implements the launch_scene_by_index MCP command as specified in the API reference.
 */
public class SceneTool {

    private static final String TOOL_NAME = "session_launchSceneByIndex";

    /**
     * Creates the MCP tool specification for scene launching.
     *
     * @param clipSceneController The controller for clip/scene operations
     * @param logger The logger service for operation logging
     * @return MCP tool specification
     */
    public static McpServerFeatures.SyncToolSpecification launchSceneByIndexSpecification(ClipSceneController clipSceneController, Logger logger) {
        var schema = """
            {
              "type": "object",
              "properties": {
                "scene_index": {
                  "type": "integer",
                  "minimum": 0,
                  "description": "Zero-based index of the scene to launch"
                }
              },
              "required": ["scene_index"]
            }""";

        var tool = new McpSchema.Tool(
            TOOL_NAME,
            "Launch a scene in Bitwig by providing its zero-based index",
            schema
        );

        BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> handler = (exchange, arguments) ->
            handleLaunchSceneByIndex(arguments, clipSceneController, logger);

        return new McpServerFeatures.SyncToolSpecification(tool, handler);
    }

    /**
     * Handles the launch_scene_by_index MCP tool request.
     *
     * @param arguments The tool arguments containing scene_index
     * @param clipSceneController The controller for clip/scene operations
     * @param logger The logger service for operation logging
     * @return Tool result with success response or error details
     */
    private static McpSchema.CallToolResult handleLaunchSceneByIndex(Map<String, Object> arguments, ClipSceneController clipSceneController, Logger logger) {
        try {
            LaunchSceneArguments args = parseArguments(arguments);

            logger.info("Received session_launchSceneByIndex command - Scene Index: " + args.sceneIndex());

            if (args.sceneIndex() < 0) {
                logger.warn("Invalid scene_index: negative value " + args.sceneIndex());
                return errorResponse("SCENE_NOT_FOUND", "scene_index must be non-negative");
            }

            var result = clipSceneController.launchSceneByIndex(args.sceneIndex());

            if (result.isSuccess()) {
                logger.info("Scene launch successful: " + result.getMessage());
                return successResponse(args.sceneIndex(), result.getMessage());
            } else {
                logger.error("Scene launch failed: " + result.getMessage());
                String code = "SCENE_NOT_FOUND".equals(result.getErrorCode()) ? "SCENE_NOT_FOUND" : "BITWIG_ERROR";
                return errorResponse(code, result.getMessage());
            }

        } catch (IllegalArgumentException e) {
            logger.error("Invalid arguments for launch_scene_by_index: " + e.getMessage());
            return errorResponse("SCENE_NOT_FOUND", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error in launch_scene_by_index: " + e.getMessage(), e);
            return errorResponse("BITWIG_ERROR", "Internal error occurred while launching scene");
        }
    }

    private static LaunchSceneArguments parseArguments(Map<String, Object> arguments) {
        Object sceneIndexObj = arguments.get("scene_index");

        if (sceneIndexObj == null) {
            throw new IllegalArgumentException("Missing required parameter: scene_index");
        }

        int sceneIndex;
        if (sceneIndexObj instanceof Number) {
            sceneIndex = ((Number) sceneIndexObj).intValue();
        } else {
            throw new IllegalArgumentException("scene_index must be an integer");
        }

        return new LaunchSceneArguments(sceneIndex);
    }

    private static McpSchema.CallToolResult successResponse(int sceneIndex, String message) {
        String json = String.format(
            "{\"status\":\"success\",\"data\":{\"action\":\"scene_launched\",\"scene_index\":%d,\"message\":\"%s\"}}",
            sceneIndex, message.replace("\"", "\\\"")
        );
        return new McpSchema.CallToolResult(json, false);
    }

    private static McpSchema.CallToolResult errorResponse(String code, String message) {
        String json = String.format(
            "{\"isError\":true,\"errorCode\":\"%s\",\"message\":\"%s\"}",
            code, message.replace("\"", "\\\"")
        );
        return new McpSchema.CallToolResult(json, true);
    }

    /**
     * Data record for validated launch scene arguments.
     *
     * @param sceneIndex The zero-based scene index
     */
    public record LaunchSceneArguments(
        @JsonProperty("scene_index") int sceneIndex
    ) {}
}
