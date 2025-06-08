package io.github.fabb.wigai.mcp.tool;

import io.github.fabb.wigai.common.data.ParameterInfo;
import io.github.fabb.wigai.common.data.ParameterSetting;
import io.github.fabb.wigai.common.data.ParameterSettingResult;
import io.github.fabb.wigai.common.logging.StructuredLogger;
import io.github.fabb.wigai.common.validation.ParameterValidator;
import io.github.fabb.wigai.features.DeviceController;
import io.github.fabb.wigai.mcp.McpErrorHandler;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.function.BiFunction;

/**
 * MCP tool for device parameter operations in Bitwig using unified error handling architecture.
 */
public class DeviceParamTool {

    private static final String GET_PARAMETERS_TOOL = "get_selected_device_parameters";
    private static final String SET_PARAMETER_TOOL = "set_selected_device_parameter";
    private static final String SET_MULTIPLE_PARAMETERS_TOOL = "set_selected_device_parameters";

    /**
     * Creates a "get_selected_device_parameters" tool specification.
     *
     * @param deviceController The controller for device operations
     * @param logger           The structured logger for logging operations
     * @return A SyncToolSpecification for the "get_selected_device_parameters" tool
     */
    public static McpServerFeatures.SyncToolSpecification getSelectedDeviceParametersSpecification(
            DeviceController deviceController, StructuredLogger logger) {
        var schema = """
            {
              "type": "object",
              "properties": {}
            }""";
        var tool = new McpSchema.Tool(
            GET_PARAMETERS_TOOL,
            "Get the names and values of the eight addressable parameters of the user-selected device in Bitwig.",
            schema
        );

        BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> handler =
            (exchange, arguments) -> McpErrorHandler.executeWithErrorHandling(
                GET_PARAMETERS_TOOL,
                logger,
                new McpErrorHandler.ToolOperation() {
                    @Override
                    public Object execute() throws Exception {
                        DeviceController.DeviceParametersResult result = deviceController.getSelectedDeviceParameters();

                        // Create response data
                        Map<String, Object> responseData = new LinkedHashMap<>();
                        responseData.put("device_name", result.deviceName());

                        List<Map<String, Object>> parametersArray = new ArrayList<>();
                        for (ParameterInfo param : result.parameters()) {
                            Map<String, Object> paramMap = new LinkedHashMap<>();
                            paramMap.put("index", param.index());
                            paramMap.put("name", param.name());
                            paramMap.put("value", param.value());
                            paramMap.put("display_value", param.display_value());
                            parametersArray.add(paramMap);
                        }
                        responseData.put("parameters", parametersArray);

                        return McpErrorHandler.SuccessResponseBuilder.create()
                            .withAction("parameters_retrieved")
                            .withMessage("Device parameters retrieved successfully for: " + result.deviceName())
                            .withData(responseData)
                            .build();
                    }
                }
            );

        return new McpServerFeatures.SyncToolSpecification(tool, handler);
    }

    /**
     * Creates a "set_selected_device_parameter" tool specification.
     *
     * @param deviceController The controller for device operations
     * @param logger           The structured logger for logging operations
     * @return A SyncToolSpecification for the "set_selected_device_parameter" tool
     */
    public static McpServerFeatures.SyncToolSpecification setSelectedDeviceParameterSpecification(
            DeviceController deviceController, StructuredLogger logger) {
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
            SET_PARAMETER_TOOL,
            "Set a specific value for a single parameter (by its index 0-7) of the user-selected device in Bitwig.",
            schema
        );

        BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> handler =
            (exchange, arguments) -> McpErrorHandler.executeWithErrorHandling(
                SET_PARAMETER_TOOL,
                logger,
                new McpErrorHandler.ToolOperation() {
                    @Override
                    public Object execute() throws Exception {
                        // Parse and validate arguments
                        SetParameterArguments args = parseSetParameterArguments(arguments);

                        // Perform the parameter setting
                        deviceController.setSelectedDeviceParameter(args.parameterIndex(), args.value());

                        return McpErrorHandler.SuccessResponseBuilder.create()
                            .withAction("parameter_set")
                            .withMessage("Parameter " + args.parameterIndex() + " set to " + args.value() + ".")
                            .withData("parameter_index", args.parameterIndex())
                            .withData("new_value", args.value())
                            .build();
                    }
                }
            );

        return new McpServerFeatures.SyncToolSpecification(tool, handler);
    }

    /**
     * Creates a "set_selected_device_parameters" tool specification.
     *
     * @param deviceController The controller for device operations
     * @param logger           The structured logger for logging operations
     * @return A SyncToolSpecification for the "set_selected_device_parameters" tool
     */
    public static McpServerFeatures.SyncToolSpecification setMultipleDeviceParametersSpecification(
            DeviceController deviceController, StructuredLogger logger) {
        var schema = """
            {
              "type": "object",
              "properties": {
                "parameters": {
                  "type": "array",
                  "minItems": 1,
                  "items": {
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
                  },
                  "description": "List of parameter settings to apply"
                }
              },
              "required": ["parameters"]
            }""";
        var tool = new McpSchema.Tool(
            SET_MULTIPLE_PARAMETERS_TOOL,
            "Set multiple parameter values (by index 0-7) of the user-selected device in Bitwig simultaneously.",
            schema
        );

        BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> handler =
            (exchange, arguments) -> McpErrorHandler.executeWithErrorHandling(
                SET_MULTIPLE_PARAMETERS_TOOL,
                logger,
                new McpErrorHandler.ToolOperation() {
                    @Override
                    public Object execute() throws Exception {
                        // Parse and validate arguments
                        SetMultipleParametersArguments args = parseSetMultipleParametersArguments(arguments);

                        // Perform the batch parameter setting
                        List<ParameterSettingResult> results = deviceController.setMultipleSelectedDeviceParameters(args.parameters());

                        // Build response data
                        List<Map<String, Object>> resultsArray = new ArrayList<>();
                        for (ParameterSettingResult result : results) {
                            Map<String, Object> resultMap = new LinkedHashMap<>();
                            resultMap.put("parameter_index", result.parameter_index());
                            resultMap.put("status", result.status());

                            if ("success".equals(result.status())) {
                                resultMap.put("new_value", result.new_value());
                            } else {
                                resultMap.put("error_code", result.error_code());
                                resultMap.put("message", result.message());
                            }

                            resultsArray.add(resultMap);
                        }

                        long successCount = results.stream().filter(r -> "success".equals(r.status())).count();
                        long errorCount = results.size() - successCount;

                        return McpErrorHandler.SuccessResponseBuilder.create()
                            .withAction("multiple_parameters_set")
                            .withMessage("Batch operation completed: " + successCount + " succeeded, " + errorCount + " failed")
                            .withData("results", resultsArray)
                            .build();
                    }
                }
            );

        return new McpServerFeatures.SyncToolSpecification(tool, handler);
    }

    /**
     * Parses the arguments for setting a single parameter.
     */
    private static SetParameterArguments parseSetParameterArguments(Map<String, Object> arguments) {
        int parameterIndex = ParameterValidator.validateRequiredInteger(arguments, "parameter_index", SET_PARAMETER_TOOL);
        parameterIndex = ParameterValidator.validateParameterIndex(parameterIndex, SET_PARAMETER_TOOL);

        double value = ParameterValidator.validateRequiredDouble(arguments, "value", SET_PARAMETER_TOOL);
        value = ParameterValidator.validateParameterValue(value, SET_PARAMETER_TOOL);

        return new SetParameterArguments(parameterIndex, value);
    }

    /**
     * Parses the arguments for setting multiple parameters.
     */
    @SuppressWarnings("unchecked")
    private static SetMultipleParametersArguments parseSetMultipleParametersArguments(Map<String, Object> arguments) {
        Object parametersObj = ParameterValidator.validateRequired(arguments, "parameters", SET_MULTIPLE_PARAMETERS_TOOL);

        if (!(parametersObj instanceof List)) {
            throw new IllegalArgumentException("'parameters' must be an array");
        }

        List<Object> parametersArray = (List<Object>) parametersObj;
        if (parametersArray.isEmpty()) {
            throw new IllegalArgumentException("'parameters' array cannot be empty");
        }

        List<ParameterSetting> parameterSettings = new ArrayList<>();
        for (Object paramObj : parametersArray) {
            if (!(paramObj instanceof Map)) {
                throw new IllegalArgumentException("Each parameter entry must be an object");
            }

            Map<String, Object> paramMap = (Map<String, Object>) paramObj;

            int parameterIndex = ParameterValidator.validateRequiredInteger(paramMap, "parameter_index", SET_MULTIPLE_PARAMETERS_TOOL);
            parameterIndex = ParameterValidator.validateParameterIndex(parameterIndex, SET_MULTIPLE_PARAMETERS_TOOL);

            double value = ParameterValidator.validateRequiredDouble(paramMap, "value", SET_MULTIPLE_PARAMETERS_TOOL);
            value = ParameterValidator.validateParameterValue(value, SET_MULTIPLE_PARAMETERS_TOOL);

            parameterSettings.add(new ParameterSetting(parameterIndex, value));
        }

        return new SetMultipleParametersArguments(parameterSettings);
    }

    /**
     * Data record for validated set parameter arguments.
     */
    public record SetParameterArguments(
        @JsonProperty("parameter_index") int parameterIndex,
        @JsonProperty("value") double value
    ) {}

    /**
     * Data record for validated set multiple parameters arguments.
     */
    public record SetMultipleParametersArguments(
        @JsonProperty("parameters") List<ParameterSetting> parameters
    ) {}
}
