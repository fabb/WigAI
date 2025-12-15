package io.github.fabb.wigai.mcp.tool;

import io.github.fabb.wigai.WigAIExtensionDefinition;
import io.github.fabb.wigai.bitwig.BitwigApiFacade;
import io.github.fabb.wigai.common.logging.StructuredLogger;
import io.github.fabb.wigai.mcp.McpErrorHandler;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;

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
        var tool = McpSchema.Tool.builder()
            .name("status")
            .description("Get WigAI operational status, version information, current project name, audio engine status, and detailed transport information.")
            .inputSchema(schema)
            .build();

        BiFunction<McpSyncServerExchange, CallToolRequest, McpSchema.CallToolResult> handler =
            (exchange, req) -> McpErrorHandler.executeWithErrorHandling(
                "status",
                logger,
                new McpErrorHandler.ToolOperation() {
                    @Override
                    public Object execute() throws Exception {
                        String wigaiVersion = extensionDefinition.getVersion();
                        Map<String, Object> responseData = new LinkedHashMap<>();
                        responseData.put("wigai_version", wigaiVersion);
                        List<String> partialFailures = new ArrayList<>();
                        try { responseData.put("project_name", bitwigApiFacade.getProjectName()); } catch (Exception e) { responseData.put("project_name", "Unknown Project"); partialFailures.add("project_name: " + e.getMessage()); }
                        try { responseData.put("audio_engine_active", bitwigApiFacade.isAudioEngineActive()); } catch (Exception e) { responseData.put("audio_engine_active", false); partialFailures.add("audio_engine_active: " + e.getMessage()); }
                        try { responseData.put("transport", bitwigApiFacade.getTransportStatus()); } catch (Exception e) { responseData.put("transport", Map.of("error", "Transport status unavailable")); partialFailures.add("transport: " + e.getMessage()); }
                        try {
                            List<io.github.fabb.wigai.common.data.ParameterInfo> projectParams = bitwigApiFacade.getProjectParameters();
                            List<Map<String, Object>> projectParametersArray = new ArrayList<>();
                            for (io.github.fabb.wigai.common.data.ParameterInfo param : projectParams) {
                                Map<String, Object> paramMap = new LinkedHashMap<>();
                                paramMap.put("index", param.index());
                                paramMap.put("name", param.name());
                                paramMap.put("value", param.value());
                                paramMap.put("display_value", param.display_value());
                                projectParametersArray.add(paramMap);
                            }
                            responseData.put("project_parameters", projectParametersArray);
                        } catch (Exception e) { responseData.put("project_parameters", new ArrayList<>()); partialFailures.add("project_parameters: " + e.getMessage()); }
                        try { responseData.put("selected_track", bitwigApiFacade.getSelectedTrackInfo()); } catch (Exception e) { responseData.put("selected_track", null); partialFailures.add("selected_track: " + e.getMessage()); }
                        try { responseData.put("selected_device", bitwigApiFacade.getSelectedDeviceInfo()); } catch (Exception e) { responseData.put("selected_device", null); partialFailures.add("selected_device: " + e.getMessage()); }
                        try { responseData.put("selected_clip_slot", bitwigApiFacade.getSelectedClipSlotInfo()); } catch (Exception e) { responseData.put("selected_clip_slot", null); partialFailures.add("selected_clip_slot: " + e.getMessage()); }
                        if (!partialFailures.isEmpty()) { responseData.put("partial_failures", partialFailures); responseData.put("status_note", "Status retrieved with " + partialFailures.size() + " partial failures"); }
                        return responseData;
                    }
                }
            );
        return McpServerFeatures.SyncToolSpecification.builder()
            .tool(tool)
            .callHandler(handler)
            .build();
    }
}
