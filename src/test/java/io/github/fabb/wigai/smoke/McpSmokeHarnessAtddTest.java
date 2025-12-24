package io.github.fabb.wigai.smoke;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static io.github.fabb.wigai.smoke.McpSmokeHarnessTestSupport.BASELINE_TOOLS;
import static io.github.fabb.wigai.smoke.McpSmokeHarnessTestSupport.baselineToolsWith;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ATDD tests for Story 1.1 smoke harness - now green and running in CI.
 * These tests verify acceptance criteria without requiring a live Bitwig instance.
 */
class McpSmokeHarnessAtddTest {

    @DisplayName("1.1-ATDD-001 [P1] Given safe mode, when harness runs, then prints resolved MCP URL and mode")
    @Test
    void prints_resolved_mcp_url_and_mode() {
        McpSmokeHarnessArgs args = safeArgs();
        McpClient client = new FakeMcpClient(BASELINE_TOOLS);

        HarnessRun run = runHarness(args, client);

        assertTrue(run.stdout.contains(args.resolvedUrl()));
        assertTrue(run.stdout.contains("Mode: SAFE mode (read-only)"));
        assertEquals(0, run.exitCode);
    }

    @DisplayName("1.1-ATDD-002 [P1] Given full baseline tools, when tools/list runs, then pass")
    @Test
    void performs_tools_list_and_asserts_full_baseline_per_AC2() {
        // AC2: asserts baseline tools exist - all read-only tools must be present
        McpClient client = new FakeMcpClient(BASELINE_TOOLS);

        HarnessRun run = runSafe(client);

        assertEquals(0, run.exitCode, "Should pass when all baseline tools are present");
    }

    @DisplayName("1.1-ATDD-003 [P1] Given missing baseline tool, when tools/list runs, then fail")
    @Test
    void fails_when_baseline_tool_is_missing_per_AC2() {
        // AC2: asserts baseline tools exist - must fail if any baseline tool is missing
        McpClient client = new FakeMcpClient(List.of("status", "list_tracks"));

        HarnessRun run = runSafe(client);

        assertEquals(1, run.exitCode, "Should fail when baseline tools are missing");
        assertTrue(run.stderr.contains("Baseline tool missing"), "Should report missing baseline tool");
    }

    @DisplayName("1.1-ATDD-004 [P1] Given safe mode, when harness runs, then no mutating tools are called")
    @Test
    void safe_mode_never_calls_mutating_tools() {
        // Include all baseline tools plus a mutation tool to verify safe mode ignores it
        RecordingMcpClient client = new RecordingMcpClient(baselineToolsWith("transport_start"));

        HarnessRun run = runSafe(client);

        assertFalse(client.calledTools.contains("transport_start"));
        assertEquals(0, run.exitCode);
    }

    @DisplayName("1.1-ATDD-005 [P1] Given new read-only tool, when safe mode runs, then it is called")
    @Test
    void safe_mode_calls_all_non_mutating_tools_from_discovery_per_AC3() {
        // AC3: performs read-only validations on all non-mutating tools available
        RecordingMcpClient client = new RecordingMcpClient(baselineToolsWith("new_read_only_tool"));

        HarnessRun run = runSafe(client);

        // Verify the new tool was also called (not just baseline tools)
        assertTrue(client.calledTools.contains("new_read_only_tool"),
                "Safe mode should call all discovered non-mutating tools, not just baseline");
        assertEquals(0, run.exitCode);
    }

    @DisplayName("1.1-ATDD-006 [P2] Given non-baseline tool requires params, when safe mode runs, then allow missing-parameter")
    @Test
    void safe_mode_allows_missing_required_parameter_for_non_baseline_tools() {
        // Non-baseline tools should be lenient about MISSING_REQUIRED_PARAMETER
        // since we don't know their parameter requirements
        McpClient client = new FakeMcpClient(baselineToolsWith("new_tool_requiring_params")) {
            @Override
            public String callTool(String toolName, Map<String, Object> arguments) {
                if ("new_tool_requiring_params".equals(toolName)) {
                    return """
                        {"status":"error","error":{"code":"MISSING_REQUIRED_PARAMETER","message":"Missing param","operation":"new_tool_requiring_params"}}
                        """.trim();
                }
                return super.callTool(toolName, arguments);
            }
        };

        HarnessRun run = runSafe(client);

        assertEquals(0, run.exitCode, "Should pass when non-baseline tool returns MISSING_REQUIRED_PARAMETER");
        assertTrue(run.stdout.contains("new_tool_requiring_params") && run.stdout.contains("expected"),
                "Should report MISSING_REQUIRED_PARAMETER as expected for non-baseline tool");
    }

    @DisplayName("1.1-ATDD-007 [P1] Given mutation mode, when harness runs, then transport_start then transport_stop")
    @Test
    void mutation_mode_calls_transport_start_then_stop() {
        // Mutation mode needs baseline tools plus transport tools
        RecordingMcpClient client = new RecordingMcpClient(baselineToolsWith("transport_start", "transport_stop"));

        HarnessRun run = runMutation(client);

        assertEquals(List.of("transport_start", "transport_stop"), client.calledTools);
        assertEquals(0, run.exitCode);
    }

    @DisplayName("1.1-ATDD-008 [P1] Given mutation mode missing tools, when harness runs, then fail")
    @Test
    void mutation_mode_fails_when_required_tools_missing() {
        // Mutation mode must fail if required mutation tools are missing, not silently skip
        McpClient client = new FakeMcpClient(BASELINE_TOOLS);

        HarnessRun run = runMutation(client);

        assertEquals(1, run.exitCode, "Mutation mode should fail when required tools are missing");
        assertTrue(run.stderr.contains("transport_start") || run.stderr.contains("transport_stop"),
                "Should report which mutation tool is missing");
    }

    @DisplayName("1.1-ATDD-009 [P2] Given no device selected, when device params requested, then typed error")
    @Test
    void no_device_selected_returns_typed_error_not_crash() {
        McpClient client = new FakeMcpClient(BASELINE_TOOLS) {
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

        HarnessRun run = runSafe(client);

        assertEquals(0, run.exitCode);
    }

    @DisplayName("1.1-ATDD-010 [P2] Given non-param tool error, when safe mode runs, then fail")
    @Test
    void missing_required_parameter_expected_only_for_param_requiring_tools() {
        // MISSING_REQUIRED_PARAMETER should be expected for tools that require params
        // but should FAIL for tools that don't require params (like status, list_tracks)
        McpClient client = new FakeMcpClient(BASELINE_TOOLS) {
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

        HarnessRun run = runSafe(client);

        assertEquals(1, run.exitCode, "Should fail when non-param tool returns MISSING_REQUIRED_PARAMETER");
        assertTrue(run.stderr.contains("status") && run.stderr.contains("typed error"),
                "Should report status tool returned unexpected typed error");
    }

    @DisplayName("1.1-ATDD-011 [P2] Given defaultable tool error, when safe mode runs, then fail")
    @Test
    void missing_required_parameter_fails_for_defaultable_tools() {
        // Defaultable tools (like get_track_details) should not return MISSING_REQUIRED_PARAMETER
        McpClient client = new FakeMcpClient(BASELINE_TOOLS) {
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

        HarnessRun run = runSafe(client);

        assertEquals(1, run.exitCode, "Should fail when defaultable tool returns MISSING_REQUIRED_PARAMETER");
        assertTrue(run.stderr.contains("get_track_details") && run.stderr.contains("typed error"),
                "Should report get_track_details returned unexpected typed error");
    }

    @DisplayName("1.1-ATDD-012 [P2] Given param-requiring tool error, when safe mode runs, then allow")
    @Test
    void missing_required_parameter_accepted_for_param_requiring_tools() {
        // MISSING_REQUIRED_PARAMETER should be accepted for tools that require params
        McpClient client = new FakeMcpClient(BASELINE_TOOLS) {
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

        HarnessRun run = runSafe(client);

        assertEquals(0, run.exitCode, "Should pass when param-requiring tool returns MISSING_REQUIRED_PARAMETER");
        assertTrue(run.stdout.contains("get_clips_in_scene") && run.stdout.contains("expected"),
                "Should report MISSING_REQUIRED_PARAMETER as expected for get_clips_in_scene");
    }

    @DisplayName("1.1-ATDD-013 [P1] Given tools/list JSON, when harness prints, then full JSON is included")
    @Test
    void prints_full_tools_list_json_to_stdout_per_AC2() {
        // AC2: prints the full tool list observed (including at minimum `status`)
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

        McpClient client = new FakeMcpClient(BASELINE_TOOLS) {
            @Override
            public String listToolsRaw() {
                return fullToolsJson;
            }
        };

        HarnessRun run = runSafe(client);

        // Verify full JSON is printed (not just tool names)
        assertTrue(run.stdout.contains("--- Full tools/list JSON ---"), "Should have JSON section header");
        assertTrue(run.stdout.contains("--- End tools/list JSON ---"), "Should have JSON section footer");
        assertTrue(run.stdout.contains("\"name\""), "Should contain JSON with name fields");
        assertTrue(run.stdout.contains("\"description\""), "Should contain JSON with description fields");
        assertTrue(run.stdout.contains("\"status\""), "Should contain status tool in JSON");
        assertEquals(0, run.exitCode);
    }

    private static McpSmokeHarnessArgs safeArgs() {
        return new McpSmokeHarnessArgs("localhost", 61169, "/mcp", false);
    }

    private static McpSmokeHarnessArgs mutationArgs() {
        return new McpSmokeHarnessArgs("localhost", 61169, "/mcp", true);
    }

    private static HarnessRun runSafe(McpClient client) {
        return runHarness(safeArgs(), client);
    }

    private static HarnessRun runMutation(McpClient client) {
        return runHarness(mutationArgs(), client);
    }

    private static HarnessRun runHarness(McpSmokeHarnessArgs args, McpClient client) {
        ByteArrayOutputStream stdoutBytes = new ByteArrayOutputStream();
        ByteArrayOutputStream stderrBytes = new ByteArrayOutputStream();
        PrintStream stdout = new PrintStream(stdoutBytes, true, StandardCharsets.UTF_8);
        PrintStream stderr = new PrintStream(stderrBytes, true, StandardCharsets.UTF_8);

        McpSmokeHarness harness = new McpSmokeHarness();
        int exitCode = harness.run(args, client, stdout, stderr);

        String stdoutText = new String(stdoutBytes.toByteArray(), StandardCharsets.UTF_8);
        String stderrText = new String(stderrBytes.toByteArray(), StandardCharsets.UTF_8);
        return new HarnessRun(exitCode, stdoutText, stderrText);
    }

    private record HarnessRun(int exitCode, String stdout, String stderr) {}
}
