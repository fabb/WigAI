package io.github.fabb.wigai.mcp.tool;

import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.features.TransportController;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * MCP tools for transport control in Bitwig.
 */
public class TransportTool {
    // Store the handler function so it can be accessed for testing
    private static BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> transportStartHandler;
    private static BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> transportStopHandler;

    /**
     * Creates a "transport_start" tool specification.
     *
     * @param transportController The controller for transport operations
     * @param logger              The logger for logging operations
     * @return A SyncToolSpecification for the "transport_start" tool
     */
    public static McpServerFeatures.SyncToolSpecification transportStartSpecification(
            TransportController transportController, Logger logger) {
        var schema = """
            {
              "type": "object",
              "properties": {}
            }""";
        var tool = new McpSchema.Tool(
            "transport_start",
            "Start Bitwig's transport playback.",
            schema
        );

        // Create and store the handler function
        transportStartHandler = (exchange, arguments) -> {
            logger.info("Received 'transport_start' tool call");

            String resultMessage;
            try {
                resultMessage = transportController.startTransport();
                logger.info("Responding with: " + resultMessage);

                // Create text content for the response
                McpSchema.TextContent textContent = new McpSchema.TextContent(resultMessage);

                // Return successful result
                return new McpSchema.CallToolResult(List.of(textContent), false);
            } catch (Exception e) {
                String errorMessage = "Error starting transport: " + e.getMessage();
                logger.info("Responding with error: " + errorMessage);

                // Create text content for the error response
                McpSchema.TextContent errorContent = new McpSchema.TextContent(errorMessage);

                // Return error result
                return new McpSchema.CallToolResult(List.of(errorContent), true);
            }
        };

        return new McpServerFeatures.SyncToolSpecification(tool, transportStartHandler);
    }

    /**
     * Creates a "transport_stop" tool specification.
     *
     * @param transportController The controller for transport operations
     * @param logger              The logger for logging operations
     * @return A SyncToolSpecification for the "transport_stop" tool
     */
    public static McpServerFeatures.SyncToolSpecification transportStopSpecification(
            TransportController transportController, Logger logger) {
        var schema = """
            {
              "type": "object",
              "properties": {}
            }""";
        var tool = new McpSchema.Tool(
            "transport_stop",
            "Stop Bitwig's transport playback.",
            schema
        );

        // Create and store the handler function
        transportStopHandler = (exchange, arguments) -> {
            logger.info("Received 'transport_stop' tool call");

            String resultMessage;
            try {
                resultMessage = transportController.stopTransport();
                logger.info("Responding with: " + resultMessage);

                // Create text content for the response
                McpSchema.TextContent textContent = new McpSchema.TextContent(resultMessage);

                // Return successful result
                return new McpSchema.CallToolResult(List.of(textContent), false);
            } catch (Exception e) {
                String errorMessage = "Error stopping transport: " + e.getMessage();
                logger.info("Responding with error: " + errorMessage);

                // Create text content for the error response
                McpSchema.TextContent errorContent = new McpSchema.TextContent(errorMessage);

                // Return error result
                return new McpSchema.CallToolResult(List.of(errorContent), true);
            }
        };

        return new McpServerFeatures.SyncToolSpecification(tool, transportStopHandler);
    }

    // Accessor for testing
    public static BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> getTransportStartHandler() {
        return transportStartHandler;
    }

    // Accessor for testing
    public static BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> getTransportStopHandler() {
        return transportStopHandler;
    }
}
