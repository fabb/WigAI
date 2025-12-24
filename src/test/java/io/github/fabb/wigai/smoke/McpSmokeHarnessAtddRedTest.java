package io.github.fabb.wigai.smoke;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("atdd_red")
class McpSmokeHarnessAtddRedTest {

    @Test
    void prints_resolved_mcp_url_and_mode() {
        McpSmokeHarnessArgs args = new McpSmokeHarnessArgs("localhost", 61169, "/mcp", false);

        ByteArrayOutputStream stdoutBytes = new ByteArrayOutputStream();
        ByteArrayOutputStream stderrBytes = new ByteArrayOutputStream();
        PrintStream stdout = new PrintStream(stdoutBytes, true, StandardCharsets.UTF_8);
        PrintStream stderr = new PrintStream(stderrBytes, true, StandardCharsets.UTF_8);

        McpClient client = new FakeMcpClient(allBaselineTools());
        McpSmokeHarness harness = new McpSmokeHarness();

        int exitCode = harness.run(args, client, stdout, stderr);

        String stdoutText = new String(stdoutBytes.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(stdoutText.contains(args.resolvedUrl()));
        assertEquals(0, exitCode);
    }

    @Test
    void performs_tools_list_and_asserts_full_baseline_per_AC2() {
        // AC2: asserts baseline tools exist - all read-only tools must be present
        McpSmokeHarnessArgs args = new McpSmokeHarnessArgs("localhost", 61169, "/mcp", false);

        ByteArrayOutputStream stdoutBytes = new ByteArrayOutputStream();
        ByteArrayOutputStream stderrBytes = new ByteArrayOutputStream();

        // Provide ALL baseline read-only tools as per AC2
        McpClient client = new FakeMcpClient(List.of(
                "status",
                "list_tracks",
                "get_track_details",
                "list_devices_on_track",
                "get_device_details",
                "list_scenes",
                "get_clips_in_scene",
                "get_selected_device_parameters"
        ));
        McpSmokeHarness harness = new McpSmokeHarness();

        int exitCode = harness.run(args, client, new PrintStream(stdoutBytes), new PrintStream(stderrBytes));

        assertEquals(0, exitCode, "Should pass when all baseline tools are present");
    }

    @Test
    void fails_when_baseline_tool_is_missing_per_AC2() {
        // AC2: asserts baseline tools exist - must fail if any baseline tool is missing
        McpSmokeHarnessArgs args = new McpSmokeHarnessArgs("localhost", 61169, "/mcp", false);

        ByteArrayOutputStream stderrBytes = new ByteArrayOutputStream();

        // Missing several baseline tools (only status and list_tracks present)
        McpClient client = new FakeMcpClient(List.of("status", "list_tracks"));
        McpSmokeHarness harness = new McpSmokeHarness();

        int exitCode = harness.run(args, client, System.out, new PrintStream(stderrBytes));

        String stderrText = new String(stderrBytes.toByteArray(), StandardCharsets.UTF_8);
        assertEquals(1, exitCode, "Should fail when baseline tools are missing");
        assertTrue(stderrText.contains("Baseline tool missing"), "Should report missing baseline tool");
    }

    @Test
    void safe_mode_never_calls_mutating_tools() {
        McpSmokeHarnessArgs args = new McpSmokeHarnessArgs("localhost", 61169, "/mcp", false);

        // Include all baseline tools plus a mutation tool to verify safe mode ignores it
        RecordingMcpClient client = new RecordingMcpClient(allBaselineTools("transport_start"));
        McpSmokeHarness harness = new McpSmokeHarness();

        int exitCode = harness.run(args, client, System.out, System.err);

        assertFalse(client.calledTools.contains("transport_start"));
        assertEquals(0, exitCode);
    }

    @Test
    void mutation_mode_calls_transport_start_then_stop() {
        McpSmokeHarnessArgs args = new McpSmokeHarnessArgs("localhost", 61169, "/mcp", true);

        // Mutation mode needs baseline tools plus transport tools
        RecordingMcpClient client = new RecordingMcpClient(allBaselineTools("transport_start", "transport_stop"));
        McpSmokeHarness harness = new McpSmokeHarness();

        int exitCode = harness.run(args, client, System.out, System.err);

        assertEquals(List.of("transport_start", "transport_stop"), client.calledTools);
        assertEquals(0, exitCode);
    }

    @Test
    void mutation_mode_fails_when_required_tools_missing() {
        // Mutation mode must fail if required mutation tools are missing, not silently skip
        McpSmokeHarnessArgs args = new McpSmokeHarnessArgs("localhost", 61169, "/mcp", true);

        ByteArrayOutputStream stderrBytes = new ByteArrayOutputStream();

        // Only baseline tools, no mutation tools
        McpClient client = new FakeMcpClient(allBaselineTools());
        McpSmokeHarness harness = new McpSmokeHarness();

        int exitCode = harness.run(args, client, System.out, new PrintStream(stderrBytes));

        String stderrText = new String(stderrBytes.toByteArray(), StandardCharsets.UTF_8);
        assertEquals(1, exitCode, "Mutation mode should fail when required tools are missing");
        assertTrue(stderrText.contains("transport_start") || stderrText.contains("transport_stop"),
                "Should report which mutation tool is missing");
    }

    @Test
    void no_device_selected_returns_typed_error_not_crash() {
        McpSmokeHarnessArgs args = new McpSmokeHarnessArgs("localhost", 61169, "/mcp", false);

        McpClient client = new FakeMcpClient(allBaselineTools()) {
            @Override
            public String callTool(String toolName, Map<String, Object> arguments) {
                if ("get_selected_device_parameters".equals(toolName)) {
                    return """
                        {"status":"error","error":{"code":"DEVICE_NOT_SELECTED","message":"No device selected","operation":"get_selected_device_parameters"}}
                        """.trim();
                }
                return super.callTool(toolName, arguments);
            }
        };

        McpSmokeHarness harness = new McpSmokeHarness();
        int exitCode = harness.run(args, client, System.out, System.err);

        assertEquals(0, exitCode);
    }

    /**
     * Returns list of all baseline read-only tools, optionally with additional tools.
     */
    private static List<String> allBaselineTools(String... additional) {
        List<String> tools = new java.util.ArrayList<>(List.of(
                "status",
                "list_tracks",
                "get_track_details",
                "list_devices_on_track",
                "get_device_details",
                "list_scenes",
                "get_clips_in_scene",
                "get_selected_device_parameters"
        ));
        tools.addAll(List.of(additional));
        return tools;
    }

    private static class FakeMcpClient implements McpClient {
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
        public String callTool(String toolName, Map<String, Object> arguments) {
            return """
                {"status":"success","data":{"tool":"%s"}}
                """.formatted(toolName).trim();
        }
    }

    private static class RecordingMcpClient extends FakeMcpClient {
        private final List<String> calledTools = new java.util.ArrayList<>();

        RecordingMcpClient(List<String> tools) {
            super(tools);
        }

        @Override
        public String callTool(String toolName, Map<String, Object> arguments) {
            calledTools.add(toolName);
            return super.callTool(toolName, arguments);
        }
    }
}
