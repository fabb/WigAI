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

        McpClient client = new FakeMcpClient();
        McpSmokeHarness harness = new McpSmokeHarness();

        int exitCode = harness.run(args, client, stdout, stderr);

        String stdoutText = new String(stdoutBytes.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(stdoutText.contains(args.resolvedUrl()));
        assertEquals(0, exitCode);
    }

    @Test
    void performs_tools_list_and_asserts_status_tool_present() {
        McpSmokeHarnessArgs args = new McpSmokeHarnessArgs("localhost", 61169, "/mcp", false);

        ByteArrayOutputStream stdoutBytes = new ByteArrayOutputStream();
        ByteArrayOutputStream stderrBytes = new ByteArrayOutputStream();

        McpClient client = new FakeMcpClient(List.of("status", "list_tracks"));
        McpSmokeHarness harness = new McpSmokeHarness();

        int exitCode = harness.run(args, client, new PrintStream(stdoutBytes), new PrintStream(stderrBytes));

        assertEquals(0, exitCode);
    }

    @Test
    void safe_mode_never_calls_mutating_tools() {
        McpSmokeHarnessArgs args = new McpSmokeHarnessArgs("localhost", 61169, "/mcp", false);

        RecordingMcpClient client = new RecordingMcpClient(List.of("status", "list_tracks", "transport_start"));
        McpSmokeHarness harness = new McpSmokeHarness();

        int exitCode = harness.run(args, client, System.out, System.err);

        assertFalse(client.calledTools.contains("transport_start"));
        assertEquals(0, exitCode);
    }

    @Test
    void mutation_mode_calls_transport_start_then_stop() {
        McpSmokeHarnessArgs args = new McpSmokeHarnessArgs("localhost", 61169, "/mcp", true);

        RecordingMcpClient client = new RecordingMcpClient(List.of("status", "transport_start", "transport_stop"));
        McpSmokeHarness harness = new McpSmokeHarness();

        int exitCode = harness.run(args, client, System.out, System.err);

        assertEquals(List.of("transport_start", "transport_stop"), client.calledTools);
        assertEquals(0, exitCode);
    }

    @Test
    void no_device_selected_returns_typed_error_not_crash() {
        McpSmokeHarnessArgs args = new McpSmokeHarnessArgs("localhost", 61169, "/mcp", false);

        McpClient client = new FakeMcpClient(List.of("status", "get_selected_device_parameters")) {
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

    private static class FakeMcpClient implements McpClient {
        private final List<String> tools;

        FakeMcpClient() {
            this(List.of("status"));
        }

        FakeMcpClient(List<String> tools) {
            this.tools = tools;
        }

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
