package io.github.fabb.wigai.mcp.tool;

import io.github.fabb.wigai.common.error.BitwigApiException;
import io.github.fabb.wigai.common.error.ErrorCode;
import io.github.fabb.wigai.common.logging.StructuredLogger;
import io.github.fabb.wigai.common.validation.ParameterValidator;
import io.github.fabb.wigai.features.ClipSceneController;
import io.github.fabb.wigai.mcp.McpErrorHandler;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.function.BiFunction;

/**
 * MCP tool for launching scenes by index using unified error handling architecture.
 * Implements the session_launchSceneByIndex MCP command as specified in the API reference.
 */
public class SceneTool {

    private static final String TOOL_NAME = "session_launchSceneByIndex";

    /**
     * Creates the MCP tool specification for scene launching.
     *
     * @param clipSceneController The controller for clip/scene operations
     * @param logger The structured logger for operation logging
     * @return MCP tool specification
     */
    public static McpServerFeatures.SyncToolSpecification launchSceneByIndexSpecification(ClipSceneController clipSceneController, StructuredLogger logger) {
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

        BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> handler =
            (exchange, arguments) -> McpErrorHandler.executeWithErrorHandling(
                TOOL_NAME,
                logger,
                new McpErrorHandler.ToolOperation() {
                    @Override
                    public Object execute() throws Exception {
                        // Parse and validate arguments
                        LaunchSceneArguments args = parseArguments(arguments);

                        // Perform scene launch operation
                        var result = clipSceneController.launchSceneByIndex(args.sceneIndex());

                        if (result.isSuccess()) {
                            return Map.of(
                                "action", "scene_launched",
                                "scene_index", args.sceneIndex(),
                                "message", result.getMessage()
                            );
                        } else {
                            throw new BitwigApiException(ErrorCode.OPERATION_FAILED, TOOL_NAME, result.getMessage());
                        }
                    }
                }
            );

        return new McpServerFeatures.SyncToolSpecification(tool, handler);
    }

    /**
     * Parses the MCP tool arguments into a structured format.
     *
     * @param arguments Raw arguments map from MCP request
     * @return Parsed and validated LaunchSceneArguments
     * @throws IllegalArgumentException if arguments are invalid
     */
    private static LaunchSceneArguments parseArguments(Map<String, Object> arguments) {
        // Validate required parameters
        int sceneIndex = ParameterValidator.validateRequiredInteger(arguments, "scene_index", TOOL_NAME);
        sceneIndex = ParameterValidator.validateSceneIndex(sceneIndex, TOOL_NAME);

        return new LaunchSceneArguments(sceneIndex);
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
