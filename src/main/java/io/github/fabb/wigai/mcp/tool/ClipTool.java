package io.github.fabb.wigai.mcp.tool;

import io.github.fabb.wigai.common.error.BitwigApiException;
import io.github.fabb.wigai.common.error.ErrorCode;
import io.github.fabb.wigai.common.logging.StructuredLogger;
import io.github.fabb.wigai.common.validation.ParameterValidator;
import io.github.fabb.wigai.features.ClipSceneController;
import io.github.fabb.wigai.features.ClipSceneController.ClipLaunchResult;
import io.github.fabb.wigai.mcp.McpErrorHandler;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.function.BiFunction;

/**
 * MCP tool for launching clips by track name and clip index using unified error handling architecture.
 * Implements the launch_clip MCP command as specified in the API reference.
 */
public class ClipTool {

    private static final String TOOL_NAME = "launch_clip";

    /**
     * Creates the MCP tool specification for clip launching.
     *
     * @param clipSceneController The controller for clip/scene operations
     * @param logger The structured logger for operation logging
     * @return MCP tool specification
     */
    public static McpServerFeatures.SyncToolSpecification launchClipSpecification(ClipSceneController clipSceneController, StructuredLogger logger) {
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

        BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> handler =
            (exchange, arguments) -> McpErrorHandler.executeWithErrorHandling(
                TOOL_NAME,
                logger,
                () -> {
                    // Parse and validate arguments
                    LaunchClipArguments args = parseArguments(arguments);

                    // Perform clip launch operation
                    ClipLaunchResult result = clipSceneController.launchClip(args.trackName(), args.clipIndex());

                    if (result.isSuccess()) {
                        return McpErrorHandler.SuccessResponseBuilder.create()
                            .withAction("clip_launched")
                            .withMessage(result.getMessage())
                            .withData("track_name", args.trackName())
                            .withData("clip_index", args.clipIndex())
                            .build();
                    } else {
                        throw new BitwigApiException(ErrorCode.OPERATION_FAILED, TOOL_NAME, result.getMessage());
                    }
                }
            );

        return new McpServerFeatures.SyncToolSpecification(tool, handler);
    }

    /**
     * Parses the MCP tool arguments into a structured format.
     *
     * @param arguments Raw arguments map from MCP request
     * @return Parsed and validated LaunchClipArguments
     * @throws IllegalArgumentException if arguments are invalid
     */
    private static LaunchClipArguments parseArguments(Map<String, Object> arguments) {
        // Validate required parameters
        String trackName = ParameterValidator.validateRequiredString(arguments, "track_name", TOOL_NAME);
        trackName = ParameterValidator.validateNotEmpty(trackName, "track_name", TOOL_NAME);

        int clipIndex = ParameterValidator.validateRequiredInteger(arguments, "clip_index", TOOL_NAME);
        clipIndex = ParameterValidator.validateClipIndex(clipIndex, TOOL_NAME);

        return new LaunchClipArguments(trackName, clipIndex);
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
