package io.github.fabb.wigai.mcp.tool;

import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.common.data.ParameterInfo;
import io.github.fabb.wigai.features.DeviceController;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * MCP tool for device parameter retrieval in Bitwig.
 */
public class DeviceParamTool {
    // Store the handler function so it can be accessed for testing
    private static BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> getParametersHandler;

    /**
     * Creates a "get_selected_device_parameters" tool specification.
     *
     * @param deviceController The controller for device operations
     * @param logger           The logger for logging operations
     * @return A SyncToolSpecification for the "get_selected_device_parameters" tool
     */
    public static McpServerFeatures.SyncToolSpecification getSelectedDeviceParametersSpecification(
            DeviceController deviceController, Logger logger) {
        var schema = """
            {
              "type": "object",
              "properties": {}
            }""";
        var tool = new McpSchema.Tool(
            "get_selected_device_parameters",
            "Get the names and values of the eight addressable parameters of the user-selected device in Bitwig.",
            schema
        );

        // Create and store the handler function
        getParametersHandler = (exchange, arguments) -> {
            logger.info("Received 'get_selected_device_parameters' tool call");

            try {
                DeviceController.DeviceParametersResult result = deviceController.getSelectedDeviceParameters();

                // Create JSON response matching API specification
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode responseRoot = mapper.createObjectNode();
                responseRoot.put("status", "success");

                ObjectNode dataNode = mapper.createObjectNode();
                dataNode.put("device_name", result.deviceName());

                ArrayNode parametersArray = mapper.createArrayNode();
                for (ParameterInfo param : result.parameters()) {
                    ObjectNode paramNode = mapper.createObjectNode();
                    paramNode.put("index", param.index());
                    paramNode.put("name", param.name());
                    paramNode.put("value", param.value());
                    paramNode.put("display_value", param.display_value());
                    parametersArray.add(paramNode);
                }
                dataNode.set("parameters", parametersArray);
                responseRoot.set("data", dataNode);

                // Return JSON response as text content
                String jsonResponse = responseRoot.toString();
                McpSchema.TextContent textContent = new McpSchema.TextContent(jsonResponse);

                logger.info("Responding with device parameters for: " + result.deviceName());

                // Return successful result with properly formatted JSON response
                return new McpSchema.CallToolResult(List.of(textContent), false);

            } catch (Exception e) {
                String errorMessage = "Error getting selected device parameters: " + e.getMessage();
                logger.error("DeviceParamTool: " + errorMessage);

                // Create JSON error response matching API specification
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode errorResponse = mapper.createObjectNode();
                errorResponse.put("status", "error");
                errorResponse.put("message", errorMessage);

                McpSchema.TextContent errorContent = new McpSchema.TextContent(errorResponse.toString());

                // Return error result
                return new McpSchema.CallToolResult(List.of(errorContent), true);
            }
        };

        return new McpServerFeatures.SyncToolSpecification(tool, getParametersHandler);
    }

    // Accessor for testing
    public static BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> getGetParametersHandler() {
        return getParametersHandler;
    }
}
