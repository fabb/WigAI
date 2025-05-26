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
 * MCP tool for device parameter operations in Bitwig.
 */
public class DeviceParamTool {
    // Store the handler functions so they can be accessed for testing
    private static BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> getParametersHandler;
    private static BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> setParameterHandler;

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

    /**
     * Creates a "set_selected_device_parameter" tool specification.
     *
     * @param deviceController The controller for device operations
     * @param logger           The logger for logging operations
     * @return A SyncToolSpecification for the "set_selected_device_parameter" tool
     */
    public static McpServerFeatures.SyncToolSpecification setSelectedDeviceParameterSpecification(
            DeviceController deviceController, Logger logger) {
        var schema = """
            {
              "type": "object",
              "properties": {
                "parameter_index": {
                  "type": "integer",
                  "minimum": 0,
                  "maximum": 7,
                  "description": "The index of the parameter to set (0-7)"
                },
                "value": {
                  "type": "number",
                  "minimum": 0.0,
                  "maximum": 1.0,
                  "description": "The value to set (0.0-1.0)"
                }
              },
              "required": ["parameter_index", "value"]
            }""";
        var tool = new McpSchema.Tool(
            "set_selected_device_parameter",
            "Set a specific value for a single parameter (by its index 0-7) of the user-selected device in Bitwig.",
            schema
        );

        // Create and store the handler function
        setParameterHandler = (exchange, arguments) -> {
            logger.info("Received 'set_selected_device_parameter' tool call with arguments: " + arguments);

            try {
                // Extract and validate arguments
                Object paramIndexObj = arguments.get("parameter_index");
                Object valueObj = arguments.get("value");

                if (paramIndexObj == null) {
                    throw new IllegalArgumentException("Missing required parameter: parameter_index");
                }
                if (valueObj == null) {
                    throw new IllegalArgumentException("Missing required parameter: value");
                }

                int parameterIndex;
                double value;

                // Convert parameter_index to int
                if (paramIndexObj instanceof Number) {
                    parameterIndex = ((Number) paramIndexObj).intValue();
                } else {
                    throw new IllegalArgumentException("parameter_index must be a number");
                }

                // Convert value to double
                if (valueObj instanceof Number) {
                    value = ((Number) valueObj).doubleValue();
                } else {
                    throw new IllegalArgumentException("value must be a number");
                }

                // Perform the parameter setting
                deviceController.setSelectedDeviceParameter(parameterIndex, value);

                // Create success response
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode responseRoot = mapper.createObjectNode();
                responseRoot.put("status", "success");

                ObjectNode dataNode = mapper.createObjectNode();
                dataNode.put("action", "parameter_set");
                dataNode.put("parameter_index", parameterIndex);
                dataNode.put("new_value", value);
                dataNode.put("message", "Parameter " + parameterIndex + " set to " + value + ".");

                responseRoot.set("data", dataNode);

                String jsonResponse = responseRoot.toString();
                McpSchema.TextContent textContent = new McpSchema.TextContent(jsonResponse);

                logger.info("Successfully set parameter " + parameterIndex + " to " + value);

                return new McpSchema.CallToolResult(List.of(textContent), false);

            } catch (IllegalArgumentException e) {
                String errorMessage = e.getMessage();
                String errorCode;

                if (errorMessage.contains("Parameter index must be between 0-7")) {
                    errorCode = "INVALID_PARAMETER_INDEX";
                } else if (errorMessage.contains("Parameter value must be between 0.0-1.0")) {
                    errorCode = "INVALID_PARAMETER";
                } else {
                    errorCode = "INVALID_PARAMETER";
                }

                logger.error("DeviceParamTool validation error: " + errorMessage);

                // Create JSON error response
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode errorResponse = mapper.createObjectNode();
                errorResponse.put("status", "error");
                errorResponse.put("error_code", errorCode);
                errorResponse.put("message", errorMessage);

                McpSchema.TextContent errorContent = new McpSchema.TextContent(errorResponse.toString());

                return new McpSchema.CallToolResult(List.of(errorContent), true);

            } catch (RuntimeException e) {
                String errorMessage = e.getMessage();
                String errorCode;

                if (errorMessage.contains("No device is currently selected")) {
                    errorCode = "DEVICE_NOT_SELECTED";
                } else {
                    errorCode = "BITWIG_ERROR";
                }

                logger.error("DeviceParamTool runtime error: " + errorMessage);

                // Create JSON error response
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode errorResponse = mapper.createObjectNode();
                errorResponse.put("status", "error");
                errorResponse.put("error_code", errorCode);
                errorResponse.put("message", errorMessage);

                McpSchema.TextContent errorContent = new McpSchema.TextContent(errorResponse.toString());

                return new McpSchema.CallToolResult(List.of(errorContent), true);

            } catch (Exception e) {
                String errorMessage = "Unexpected error setting device parameter: " + e.getMessage();
                logger.error("DeviceParamTool unexpected error: " + errorMessage);

                // Create JSON error response
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode errorResponse = mapper.createObjectNode();
                errorResponse.put("status", "error");
                errorResponse.put("error_code", "BITWIG_ERROR");
                errorResponse.put("message", errorMessage);

                McpSchema.TextContent errorContent = new McpSchema.TextContent(errorResponse.toString());

                return new McpSchema.CallToolResult(List.of(errorContent), true);
            }
        };

        return new McpServerFeatures.SyncToolSpecification(tool, setParameterHandler);
    }

    // Accessors for testing
    public static BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> getGetParametersHandler() {
        return getParametersHandler;
    }

    public static BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> getSetParameterHandler() {
        return setParameterHandler;
    }
}
