package io.github.fabb.wigai.mcp.tool;

import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.features.ClipSceneController;
import io.github.fabb.wigai.features.ClipSceneController.ClipLaunchResult;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * MCP tool for launching clips by track name and clip index.
 * Implements the launch_clip MCP command as specified in the API reference.
 */
public class ClipTool {

    private static final String TOOL_NAME = "launch_clip";

    // Store the handler function so it can be accessed for testing
    private static BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> handlerFunction;

    /**
     * Creates the MCP tool specification for clip launching.
     *
     * @param clipSceneController The controller for clip/scene operations
     * @param logger The logger service for operation logging
     * @return MCP tool specification
     */
    public static McpServerFeatures.SyncToolSpecification launchClipSpecification(ClipSceneController clipSceneController, Logger logger) {
        var schema = """
            {
              "type": "object",
              "properties": {
                "track_name": {
                  "type": "string",
                  "description": "Name of the track containing the clip (case-sensitive)"
                },
                "clip_index": {
                  "type": "integer",
                  "minimum": 0,
                  "description": "Zero-based index of the clip slot to launch"
                }
              },
              "required": ["track_name", "clip_index"]
            }""";

        var tool = new McpSchema.Tool(
            TOOL_NAME,
            "Launch a specific clip in Bitwig by providing track name and clip slot index",
            schema
        );

        // Create and store the handler function
        handlerFunction = (exchange, arguments) -> handleLaunchClip(arguments, clipSceneController, logger);

        return new McpServerFeatures.SyncToolSpecification(tool, handlerFunction);
    }

    /**
     * Handles the launch_clip MCP tool request.
     *
     * @param arguments The tool arguments containing track_name and clip_index
     * @param clipSceneController The controller for clip/scene operations
     * @param logger The logger service for operation logging
     * @return Tool result with success response or error details
     */
    private static McpSchema.CallToolResult handleLaunchClip(Map<String, Object> arguments, ClipSceneController clipSceneController, Logger logger) {
        try {
            // Parse and validate arguments
            LaunchClipArguments args = parseArguments(arguments);

            logger.info("Received launch_clip command - Track: '" + args.trackName() + "', Index: " + args.clipIndex());

            // Validate input parameters
            if (args.trackName().trim().isEmpty()) {
                logger.warn("Invalid track_name: empty string provided");
                return new McpSchema.CallToolResult("Error: track_name cannot be empty", true);
            }

            if (args.clipIndex() < 0) {
                logger.warn("Invalid clip_index: negative value " + args.clipIndex());
                return new McpSchema.CallToolResult("Error: clip_index must be non-negative", true);
            }

            // Perform clip launch operation
            ClipLaunchResult result = clipSceneController.launchClip(args.trackName(), args.clipIndex());

            if (result.isSuccess()) {
                logger.info("Clip launch successful: " + result.getMessage());
                return new McpSchema.CallToolResult(result.getMessage(), false);
            } else {
                logger.error("Clip launch failed: " + result.getMessage());
                return new McpSchema.CallToolResult("Error: " + result.getMessage(), true);
            }

        } catch (IllegalArgumentException e) {
            logger.error("Invalid arguments for launch_clip: " + e.getMessage());
            return new McpSchema.CallToolResult("Error: " + e.getMessage(), true);
        } catch (Exception e) {
            logger.error("Unexpected error in launch_clip: " + e.getMessage(), e);
            return new McpSchema.CallToolResult("Error: Internal error occurred while launching clip", true);
        }
    }

    /**
     * Parses the MCP tool arguments into a structured format.
     *
     * @param arguments Raw arguments map from MCP request
     * @return Parsed and validated LaunchClipArguments
     * @throws IllegalArgumentException if arguments are invalid
     */
    private static LaunchClipArguments parseArguments(Map<String, Object> arguments) {
        Object trackNameObj = arguments.get("track_name");
        Object clipIndexObj = arguments.get("clip_index");

        if (trackNameObj == null) {
            throw new IllegalArgumentException("Missing required parameter: track_name");
        }

        if (clipIndexObj == null) {
            throw new IllegalArgumentException("Missing required parameter: clip_index");
        }

        if (!(trackNameObj instanceof String)) {
            throw new IllegalArgumentException("track_name must be a string");
        }

        String trackName = (String) trackNameObj;

        int clipIndex;
        if (clipIndexObj instanceof Number) {
            clipIndex = ((Number) clipIndexObj).intValue();
        } else {
            throw new IllegalArgumentException("clip_index must be an integer");
        }

        return new LaunchClipArguments(trackName, clipIndex);
    }

    // Accessor for testing
    public static BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> getHandler() {
        return handlerFunction;
    }

    /**
     * Data record for validated launch clip arguments.
     *
     * @param trackName The name of the track (case-sensitive)
     * @param clipIndex The zero-based clip slot index
     */
    public record LaunchClipArguments(
        @JsonProperty("track_name") String trackName,
        @JsonProperty("clip_index") int clipIndex
    ) {}
}
