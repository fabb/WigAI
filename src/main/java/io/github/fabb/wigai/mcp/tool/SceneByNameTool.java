package io.github.fabb.wigai.mcp.tool;

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
 * MCP tool for launching scenes by name using unified error handling architecture.
 * Implements the session_launchSceneByName MCP command as specified in the API reference.
 */
public class SceneByNameTool {

    private static final String TOOL_NAME = "session_launchSceneByName";

    /**
     * Creates the MCP tool specification for scene launching by name.
     *
     * @param clipSceneController The controller for clip/scene operations
     * @param logger The structured logger for operation logging
     * @return MCP tool specification
     */
    public static McpServerFeatures.SyncToolSpecification launchSceneByNameSpecification(ClipSceneController clipSceneController, StructuredLogger logger) {
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

        BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> handler =
            (exchange, arguments) -> McpErrorHandler.executeWithErrorHandling(
                TOOL_NAME,
                logger,
                new McpErrorHandler.ToolOperation() {
                    @Override
                    public Object execute() throws Exception {
                        // Parse and validate arguments
                        LaunchSceneByNameArguments args = parseArguments(arguments);

                        // Perform scene launch operation
                        var result = clipSceneController.launchSceneByName(args.sceneName());

                        if (result.isSuccess()) {
                            // Get the launched scene index for the response
                            int launchedIndex = clipSceneController.getBitwigApiFacade().findSceneByName(args.sceneName());

                            return Map.of(
                                "action", "scene_launched",
                                "scene_name", args.sceneName(),
                                "launched_scene_index", launchedIndex,
                                "message", result.getMessage()
                            );
                        } else {
                            throw new RuntimeException(result.getMessage());
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
     * @return Parsed and validated LaunchSceneByNameArguments
     * @throws IllegalArgumentException if arguments are invalid
     */
    private static LaunchSceneByNameArguments parseArguments(Map<String, Object> arguments) {
        // Validate required parameters
        String sceneName = ParameterValidator.validateRequiredString(arguments, "scene_name", TOOL_NAME);
        sceneName = ParameterValidator.validateNotEmpty(sceneName, "scene_name", TOOL_NAME);

        return new LaunchSceneByNameArguments(sceneName);
    }

    /**
     * Data record for validated launch scene by name arguments.
     *
     * @param sceneName The case-sensitive scene name
     */
    public record LaunchSceneByNameArguments(
        @JsonProperty("scene_name") String sceneName
    ) {}
}
