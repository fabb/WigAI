package io.github.fabb.wigai.smoke;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for safe-mode guards and error handling.
 */
class McpSmokeHarnessSafeModeTest {

    @DisplayName("1.1-UNIT-027 [P1] Given read-only tools, when checked, then allowed")
    @Test
    void assertToolIsSafeForSafeMode_allowsReadOnlyTools() {
        McpSmokeHarness harness = new McpSmokeHarness();
        // Should not throw for read-only tools
        assertDoesNotThrow(() -> harness.assertToolIsSafeForSafeMode("status"));
        assertDoesNotThrow(() -> harness.assertToolIsSafeForSafeMode("list_tracks"));
        assertDoesNotThrow(() -> harness.assertToolIsSafeForSafeMode("get_track_details"));
    }

    @DisplayName("1.1-UNIT-028 [P1] Given mutating tools, when checked, then rejected")
    @Test
    void assertToolIsSafeForSafeMode_rejectsMutatingTools() {
        McpSmokeHarness harness = new McpSmokeHarness();
        // Should throw for mutating tools - using actual MCP tool names
        assertThrows(IllegalStateException.class, () -> harness.assertToolIsSafeForSafeMode("transport_start"));
        assertThrows(IllegalStateException.class, () -> harness.assertToolIsSafeForSafeMode("transport_stop"));
        assertThrows(IllegalStateException.class, () -> harness.assertToolIsSafeForSafeMode("set_selected_device_parameter"));
        assertThrows(IllegalStateException.class, () -> harness.assertToolIsSafeForSafeMode("set_selected_device_parameters"));
        assertThrows(IllegalStateException.class, () -> harness.assertToolIsSafeForSafeMode("session_launchSceneByIndex"));
        assertThrows(IllegalStateException.class, () -> harness.assertToolIsSafeForSafeMode("session_launchSceneByName"));
        assertThrows(IllegalStateException.class, () -> harness.assertToolIsSafeForSafeMode("launch_clip"));
    }

    @DisplayName("1.1-UNIT-029 [P1] Given typed error on non-device tool, when safe mode runs, then fail")
    @Test
    void safeModeFailsOnTypedErrorForNonDeviceTools() {
        // Safe mode should fail on typed errors for tools other than get_selected_device_parameters
        McpSmokeHarness harness = new McpSmokeHarness();
        McpSmokeHarnessArgs safeArgs = new McpSmokeHarnessArgs("localhost", 61169, "/mcp", false);

        McpClient errorClient = new McpClient() {
            @Override
            public void initialize() {}

            @Override
            public List<String> listTools() {
                return List.of("status", "list_tracks", "get_track_details",
                        "list_devices_on_track", "get_device_details", "list_scenes",
                        "get_clips_in_scene", "get_selected_device_parameters");
            }

            @Override
            public String listToolsRaw() {
                return toolsListJson(listTools());
            }

            @Override
            public String callTool(String name, java.util.Map<String, Object> args) {
                // Return typed error for list_tracks (should cause failure in safe mode)
                if ("list_tracks".equals(name)) {
                    return "{\"status\":\"error\",\"error\":{\"code\":\"SOME_ERROR\",\"message\":\"Test error\"}}";
                }
                return "{\"status\":\"success\",\"data\":{}}";
            }
        };

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();

        int exitCode = harness.run(safeArgs, errorClient, new java.io.PrintStream(out), new java.io.PrintStream(err));

        assertEquals(1, exitCode, "Safe mode should fail on typed error for non-device tools");
        assertTrue(err.toString().contains("FAIL"), "Should report failure");
        assertTrue(err.toString().contains("SOME_ERROR"), "Should include the error code");
        assertTrue(err.toString().contains("Test error"), "Should include the error message");
    }

    @DisplayName("1.1-UNIT-030 [P1] Given safe mode, when running, then mutating tools not called")
    @Test
    void safeModeRejectsMutatingToolCall() {
        // AC3: Safe mode must perform read-only validations only
        // This test verifies the harness has an explicit guard against calling mutating tools
        McpSmokeHarness harness = new McpSmokeHarness();
        McpSmokeHarnessArgs safeArgs = new McpSmokeHarnessArgs("localhost", 61169, "/mcp", false);

        // Create a mock client that tracks which tools are called
        java.util.Set<String> calledTools = new java.util.HashSet<>();
        McpClient trackingClient = new McpClient() {
            @Override
            public void initialize() {}

            @Override
            public List<String> listTools() {
                // Return all baseline + mutation tools as available (using actual MCP tool names)
                return List.of(
                        "status", "list_tracks", "get_track_details", "list_devices_on_track",
                        "get_device_details", "list_scenes", "get_clips_in_scene",
                        "get_selected_device_parameters",
                        "transport_start", "transport_stop",
                        "set_selected_device_parameter", "set_selected_device_parameters",
                        "session_launchSceneByIndex", "session_launchSceneByName", "launch_clip"
                );
            }

            @Override
            public String listToolsRaw() {
                return toolsListJson(listTools());
            }

            @Override
            public String callTool(String name, java.util.Map<String, Object> args) {
                calledTools.add(name);
                return "{\"status\":\"success\",\"data\":{}}";
            }
        };

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();

        harness.run(safeArgs, trackingClient, new java.io.PrintStream(out), new java.io.PrintStream(err));

        // Verify no mutating tools were called (using actual MCP tool names)
        Set<String> mutatingTools = Set.of(
                "transport_start", "transport_stop",
                "set_selected_device_parameter", "set_selected_device_parameters",
                "session_launchSceneByIndex", "session_launchSceneByName", "launch_clip"
        );
        for (String mutatingTool : mutatingTools) {
            assertFalse(calledTools.contains(mutatingTool),
                    "Safe mode must NOT call mutating tool: " + mutatingTool);
        }
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
}
