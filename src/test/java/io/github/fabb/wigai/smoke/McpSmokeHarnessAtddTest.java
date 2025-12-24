package io.github.fabb.wigai.smoke;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ATDD tests for Story 1.1 smoke harness - now green and running in CI.
 * These tests verify acceptance criteria without requiring a live Bitwig instance.
 */
class McpSmokeHarnessAtddTest {

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

    @Test
    void missing_required_parameter_expected_only_for_param_requiring_tools() {
        // MISSING_REQUIRED_PARAMETER should be expected for tools that require params
        // but should FAIL for tools that don't require params (like status, list_tracks)
        McpSmokeHarnessArgs args = new McpSmokeHarnessArgs("localhost", 61169, "/mcp", false);

        ByteArrayOutputStream stderrBytes = new ByteArrayOutputStream();
        PrintStream stderr = new PrintStream(stderrBytes, true, StandardCharsets.UTF_8);

        // Create a client where 'status' returns MISSING_REQUIRED_PARAMETER (which is wrong - status has no params)
        McpClient client = new FakeMcpClient(allBaselineTools()) {
            @Override
            public String callTool(String toolName, Map<String, Object> arguments) {
                if ("status".equals(toolName)) {
                    // This should NOT be treated as expected - status doesn't require params
                    return """
                        {"status":"error","error":{"code":"MISSING_REQUIRED_PARAMETER","message":"Missing param","operation":"status"}}
                        """.trim();
                }
                return super.callTool(toolName, arguments);
            }
        };

        McpSmokeHarness harness = new McpSmokeHarness();
        int exitCode = harness.run(args, client, System.out, stderr);

        // Should fail because status should never return MISSING_REQUIRED_PARAMETER
        assertEquals(1, exitCode, "Should fail when non-param tool returns MISSING_REQUIRED_PARAMETER");
        String stderrText = new String(stderrBytes.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(stderrText.contains("status") && stderrText.contains("typed error"),
                "Should report status tool returned unexpected typed error");
    }

    @Test
    void missing_required_parameter_fails_for_defaultable_tools() {
        // Defaultable tools (like get_track_details) should not return MISSING_REQUIRED_PARAMETER
        McpSmokeHarnessArgs args = new McpSmokeHarnessArgs("localhost", 61169, "/mcp", false);

        ByteArrayOutputStream stderrBytes = new ByteArrayOutputStream();
        PrintStream stderr = new PrintStream(stderrBytes, true, StandardCharsets.UTF_8);

        McpClient client = new FakeMcpClient(allBaselineTools()) {
            @Override
            public String callTool(String toolName, Map<String, Object> arguments) {
                if ("get_track_details".equals(toolName)) {
                    return """
                        {"status":"error","error":{"code":"MISSING_REQUIRED_PARAMETER","message":"Missing param","operation":"get_track_details"}}
                        """.trim();
                }
                return super.callTool(toolName, arguments);
            }
        };

        McpSmokeHarness harness = new McpSmokeHarness();
        int exitCode = harness.run(args, client, System.out, stderr);

        assertEquals(1, exitCode, "Should fail when defaultable tool returns MISSING_REQUIRED_PARAMETER");
        String stderrText = new String(stderrBytes.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(stderrText.contains("get_track_details") && stderrText.contains("typed error"),
                "Should report get_track_details returned unexpected typed error");
    }

    @Test
    void missing_required_parameter_accepted_for_param_requiring_tools() {
        // MISSING_REQUIRED_PARAMETER should be accepted for tools like get_track_details
        McpSmokeHarnessArgs args = new McpSmokeHarnessArgs("localhost", 61169, "/mcp", false);

        ByteArrayOutputStream stdoutBytes = new ByteArrayOutputStream();
        PrintStream stdout = new PrintStream(stdoutBytes, true, StandardCharsets.UTF_8);

        McpClient client = new FakeMcpClient(allBaselineTools()) {
            @Override
            public String callTool(String toolName, Map<String, Object> arguments) {
                if ("get_clips_in_scene".equals(toolName)) {
                    // This IS expected - get_clips_in_scene requires scene_index or scene_name
                    return """
                        {"status":"error","error":{"code":"MISSING_REQUIRED_PARAMETER","message":"Missing scene_index","operation":"get_clips_in_scene"}}
                        """.trim();
                }
                return super.callTool(toolName, arguments);
            }
        };

        McpSmokeHarness harness = new McpSmokeHarness();
        int exitCode = harness.run(args, client, stdout, System.err);

        // Should pass because get_clips_in_scene requires params
        assertEquals(0, exitCode, "Should pass when param-requiring tool returns MISSING_REQUIRED_PARAMETER");
        String stdoutText = new String(stdoutBytes.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(stdoutText.contains("get_clips_in_scene") && stdoutText.contains("expected"),
                "Should report MISSING_REQUIRED_PARAMETER as expected for get_clips_in_scene");
    }

    @Test
    void prints_full_tools_list_json_to_stdout_per_AC2() {
        // AC2: prints the full tool list observed (including at minimum `status`)
        McpSmokeHarnessArgs args = new McpSmokeHarnessArgs("localhost", 61169, "/mcp", false);

        ByteArrayOutputStream stdoutBytes = new ByteArrayOutputStream();
        PrintStream stdout = new PrintStream(stdoutBytes, true, StandardCharsets.UTF_8);

        // Create a client that returns realistic JSON structure
        String fullToolsJson = """
            {"jsonrpc":"2.0","id":2,"result":{"tools":[
                {"name":"status","description":"Get status"},
                {"name":"list_tracks","description":"List tracks"},
                {"name":"get_track_details","description":"Get track details"},
                {"name":"list_devices_on_track","description":"List devices"},
                {"name":"get_device_details","description":"Get device details"},
                {"name":"list_scenes","description":"List scenes"},
                {"name":"get_clips_in_scene","description":"Get clips"},
                {"name":"get_selected_device_parameters","description":"Get device params"}
            ]}}
            """.trim();

        McpClient client = new FakeMcpClient(allBaselineTools()) {
            @Override
            public String listToolsRaw() {
                return fullToolsJson;
            }
        };

        McpSmokeHarness harness = new McpSmokeHarness();
        int exitCode = harness.run(args, client, stdout, System.err);

        String stdoutText = new String(stdoutBytes.toByteArray(), StandardCharsets.UTF_8);

        // Verify full JSON is printed (not just tool names)
        assertTrue(stdoutText.contains("--- Full tools/list JSON ---"), "Should have JSON section header");
        assertTrue(stdoutText.contains("--- End tools/list JSON ---"), "Should have JSON section footer");
        assertTrue(stdoutText.contains("\"name\""), "Should contain JSON with name fields");
        assertTrue(stdoutText.contains("\"description\""), "Should contain JSON with description fields");
        assertTrue(stdoutText.contains("\"status\""), "Should contain status tool in JSON");
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

    private static String toolsListJson(List<String> tools) {
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
        public String listToolsRaw() {
            return toolsListJson(tools);
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
