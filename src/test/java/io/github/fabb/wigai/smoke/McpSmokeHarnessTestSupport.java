package io.github.fabb.wigai.smoke;

import java.util.List;
import java.util.Map;

final class McpSmokeHarnessTestSupport {
    static final List<String> BASELINE_TOOLS = List.of(
            "status",
            "list_tracks",
            "get_track_details",
            "list_devices_on_track",
            "get_device_details",
            "list_scenes",
            "get_clips_in_scene",
            "get_selected_device_parameters"
    );

    private McpSmokeHarnessTestSupport() {}

    static List<String> baselineToolsWith(String... additional) {
        List<String> tools = new java.util.ArrayList<>(BASELINE_TOOLS);
        tools.addAll(List.of(additional));
        return tools;
    }

    static String toolsListJson(List<String> tools) {
        StringBuilder json = new StringBuilder();
        json.append("{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":{\"tools\":[");
        for (int i = 0; i < tools.size(); i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append("{\"name\":\"").append(tools.get(i)).append("\"}");
        }
        json.append("]}}");
        return json.toString();
    }
}

class FakeMcpClient implements McpClient {
    private final List<String> tools;

    FakeMcpClient() {
        this(List.of("status"));
    }

    FakeMcpClient(List<String> tools) {
        this.tools = tools;
    }

    @Override
    public void initialize() {}

    @Override
    public List<String> listTools() {
        return tools;
    }

    @Override
    public String listToolsRaw() {
        return McpSmokeHarnessTestSupport.toolsListJson(tools);
    }

    @Override
    public String callTool(String toolName, Map<String, Object> arguments) {
        return """
            {"status":"success","data":{"tool":"%s"}}
            """.formatted(toolName).trim();
    }
}

class RecordingMcpClient extends FakeMcpClient {
    final List<String> calledTools = new java.util.ArrayList<>();

    RecordingMcpClient(List<String> tools) {
        super(tools);
    }

    @Override
    public String callTool(String toolName, Map<String, Object> arguments) {
        calledTools.add(toolName);
        return super.callTool(toolName, arguments);
    }
}
