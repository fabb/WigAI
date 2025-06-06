package io.github.fabb.wigai.mcp.tool;

import io.github.fabb.wigai.WigAIExtensionDefinition;
import io.github.fabb.wigai.bitwig.BitwigApiFacade;
import io.github.fabb.wigai.common.Logger;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import com.bitwig.extension.controller.api.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Custom MCP "status" tool for WigAI, returns version info and project status.
 */
public class StatusTool {
    // Store the handler function so it can be accessed for testing
    private static BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> handlerFunction;

    public static McpServerFeatures.SyncToolSpecification specification(WigAIExtensionDefinition extensionDefinition, BitwigApiFacade bitwigApiFacade, Logger logger) {
        var schema = """
            {
              "type": "object",
              "properties": {}
            }""";
        var tool = new McpSchema.Tool(
            "status",
            "Get WigAI operational status, version information, current project name, audio engine status, and detailed transport information.",
            schema
        );

        // Create and store the handler function
        handlerFunction = (exchange, arguments) -> {
            logger.info("Received 'status' tool call");

            try {
                String wigaiVersion = extensionDefinition.getVersion();

                // Create the response structure
                Map<String, Object> responseMap = new LinkedHashMap<>();
                responseMap.put("wigai_version", wigaiVersion);

                // Get project information from BitwigApiFacade
                String projectName = bitwigApiFacade.getProjectName();
                boolean audioEngineActive = bitwigApiFacade.isAudioEngineActive();

                responseMap.put("project_name", projectName);
                responseMap.put("audio_engine_active", audioEngineActive);

                // Get transport information from BitwigApiFacade
                Map<String, Object> transportMap = bitwigApiFacade.getTransportStatus();
                responseMap.put("transport", transportMap);

                // Convert response to JSON string for text content
                ObjectMapper objectMapper = new ObjectMapper();
                String jsonResponse = objectMapper.writeValueAsString(responseMap);

                logger.info("Responding with: " + jsonResponse);

                // Create text content for the response
                McpSchema.TextContent textContent = new McpSchema.TextContent(jsonResponse);

                // Return successful result
                return new McpSchema.CallToolResult(List.of(textContent), false);
            } catch (Exception e) {
                String errorMessage = "Error getting status: " + e.getMessage();
                logger.error("StatusTool error: " + errorMessage, e);

                // Create text content for the error response
                McpSchema.TextContent errorContent = new McpSchema.TextContent(errorMessage);

                // Return error result
                return new McpSchema.CallToolResult(List.of(errorContent), true);
            }
        };

        return new McpServerFeatures.SyncToolSpecification(tool, handlerFunction);
    }

    // Accessor for testing
    public static BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> getHandler() {
        return handlerFunction;
    }
}
