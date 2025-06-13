package io.github.fabb.wigai.mcp.tool;

import io.github.fabb.wigai.WigAIExtensionDefinition;
import io.github.fabb.wigai.bitwig.BitwigApiFacade;
import io.github.fabb.wigai.common.logging.StructuredLogger;
import io.github.fabb.wigai.mcp.McpErrorHandler;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Custom MCP "status" tool for WigAI using unified error handling architecture.
 * Returns version info and project status.
 */
public class StatusTool {

    public static McpServerFeatures.SyncToolSpecification specification(WigAIExtensionDefinition extensionDefinition, BitwigApiFacade bitwigApiFacade, StructuredLogger logger) {
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

        BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> handler =
            (exchange, arguments) -> McpErrorHandler.executeWithErrorHandling(
                "status",
                logger,
                new McpErrorHandler.ToolOperation() {
                    @Override
                    public Object execute() throws Exception {
                        String wigaiVersion = extensionDefinition.getVersion();

                        // Create the response structure
                        Map<String, Object> responseData = new LinkedHashMap<>();
                        responseData.put("wigai_version", wigaiVersion);

                        List<String> partialFailures = new ArrayList<>();

                        // Get project information with partial success handling
                        try {
                            String projectName = bitwigApiFacade.getProjectName();
                            responseData.put("project_name", projectName);
                        } catch (Exception e) {
                            responseData.put("project_name", "Unknown Project");
                            partialFailures.add("project_name: " + e.getMessage());
                        }

                        try {
                            boolean audioEngineActive = bitwigApiFacade.isAudioEngineActive();
                            responseData.put("audio_engine_active", audioEngineActive);
                        } catch (Exception e) {
                            responseData.put("audio_engine_active", false);
                            partialFailures.add("audio_engine_active: " + e.getMessage());
                        }

                        // Get transport information (already has internal partial success handling)
                        try {
                            Map<String, Object> transportMap = bitwigApiFacade.getTransportStatus();
                            responseData.put("transport", transportMap);
                        } catch (Exception e) {
                            responseData.put("transport", Map.of("error", "Transport status unavailable"));
                            partialFailures.add("transport: " + e.getMessage());
                        }

                        // Get project parameters with partial success handling
                        try {
                            List<io.github.fabb.wigai.common.data.ParameterInfo> projectParams = bitwigApiFacade.getProjectParameters();
                            List<Map<String, Object>> projectParametersArray = new ArrayList<>();
                            for (io.github.fabb.wigai.common.data.ParameterInfo param : projectParams) {
                                Map<String, Object> paramMap = new LinkedHashMap<>();
                                paramMap.put("index", param.index());
                                paramMap.put("exists", true); // Only existing parameters are returned
                                paramMap.put("name", param.name());
                                paramMap.put("value", param.value());
                                paramMap.put("display_value", param.display_value());
                                projectParametersArray.add(paramMap);
                            }
                            responseData.put("project_parameters", projectParametersArray);
                        } catch (Exception e) {
                            responseData.put("project_parameters", new ArrayList<>());
                            partialFailures.add("project_parameters: " + e.getMessage());
                        }

                        // Get selected track information with partial success handling
                        try {
                            Map<String, Object> selectedTrackInfo = bitwigApiFacade.getSelectedTrackInfo();
                            responseData.put("selected_track", selectedTrackInfo);
                        } catch (Exception e) {
                            responseData.put("selected_track", null);
                            partialFailures.add("selected_track: " + e.getMessage());
                        }

                        // Get selected device information with partial success handling
                        try {
                            Map<String, Object> selectedDeviceInfo = bitwigApiFacade.getSelectedDeviceInfo();
                            responseData.put("selected_device", selectedDeviceInfo);
                        } catch (Exception e) {
                            responseData.put("selected_device", null);
                            partialFailures.add("selected_device: " + e.getMessage());
                        }

                        // Add partial failure information if any
                        if (!partialFailures.isEmpty()) {
                            responseData.put("partial_failures", partialFailures);
                            responseData.put("status_note", "Status retrieved with " + partialFailures.size() + " partial failures");
                        }

                        // Return the responseData directly - executeWithErrorHandling will wrap it properly
                        return responseData;
                    }
                }
            );

        return new McpServerFeatures.SyncToolSpecification(tool, handler);
    }
}
