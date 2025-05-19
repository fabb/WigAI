package io.github.fabb.wigai.mcp.tool;

import io.github.fabb.wigai.common.Logger;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.github.fabb.wigai.WigAIExtensionDefinition;

import java.util.Map;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import java.util.function.BiFunction;

/**
 * Custom MCP "status" tool for WigAI, returns version info.
 */
public class StatusTool {
    // Store the handler function so it can be accessed for testing
    private static BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> handlerFunction;

    public static McpServerFeatures.SyncToolSpecification specification(WigAIExtensionDefinition extensionDefinition, Logger logger) {
        var schema = """
            {
              "type": "object",
              "properties": {}
            }""";
        var tool = new McpSchema.Tool(
            "status",
            "Get WigAI operational status and version information.",
            schema
        );
        // Create and store the handler function
        handlerFunction = (exchange, arguments) -> {
            String version = extensionDefinition.getVersion();
            String statusText = String.format("WigAI v%s is operational", version);
            logger.info("Received 'status' tool call");
            logger.info("Responding with: " + statusText);
            return new McpSchema.CallToolResult(statusText, false);
        };
        return new McpServerFeatures.SyncToolSpecification(tool, handlerFunction);
    }

    // Accessor for testing
    public static BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> getHandler() {
        return handlerFunction;
    }
}
