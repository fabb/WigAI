package io.github.fabb.wigai.mcp.tool;

import com.bitwig.extension.controller.api.Application;
import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.Project;
import com.bitwig.extension.controller.api.Transport;
import io.github.fabb.wigai.common.Logger;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.github.fabb.wigai.WigAIExtensionDefinition;

import java.util.LinkedHashMap;
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

    public static McpServerFeatures.SyncToolSpecification specification(WigAIExtensionDefinition extensionDefinition, Logger logger, ControllerHost host) {
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
            Project project = host.getProject();
            Application application = host.getApplication();

            String projectName = project.getName().get();
            boolean audioEngineActive = application.isEngineActive().get();
            String wigaiVersion = extensionDefinition.getVersion();

            Map<String, Object> responseMap = new LinkedHashMap<>();
            responseMap.put("wigai_version", wigaiVersion);
            responseMap.put("project_name", projectName);
            responseMap.put("audio_engine_active", audioEngineActive);

            Transport transport = host.getTransport();
            Map<String, Object> transportMap = new LinkedHashMap<>();
            transportMap.put("playing", transport.isPlaying().get());
            transportMap.put("recording", transport.isArrangerRecordEnabled().get());
            transportMap.put("repeat_active", transport.isArrangerLoopEnabled().get());
            transportMap.put("metronome_active", transport.isMetronomeEnabled().get());
            transportMap.put("current_tempo", transport.tempo().value().get());
            transportMap.put("time_signature", transport.timeSignature().get());
            transportMap.put("current_beat_str", transport.getPosition().get());
            transportMap.put("current_time_str", transport.getPosition().get());

            responseMap.put("transport", transportMap);

            logger.info("Received 'status' tool call");
            logger.info("Responding with: " + responseMap);
            return new McpSchema.CallToolResult(responseMap, false);
        };
        return new McpServerFeatures.SyncToolSpecification(tool, handlerFunction);
    }

    // Accessor for testing
    public static BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> getHandler() {
        return handlerFunction;
    }
}
