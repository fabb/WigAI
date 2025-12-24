package io.github.fabb.wigai.smoke;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for McpSmokeHarnessArgs and argument parsing.
 * These tests are CI-safe (no host required).
 */
class McpSmokeHarnessArgsTest {

    @Test
    void resolvedUrl_with_defaults() {
        McpSmokeHarnessArgs args = new McpSmokeHarnessArgs("localhost", 61169, "/mcp", false);
        assertEquals("http://localhost:61169/mcp", args.resolvedUrl());
    }

    @Test
    void resolvedUrl_with_custom_host_and_port() {
        McpSmokeHarnessArgs args = new McpSmokeHarnessArgs("192.168.1.100", 8080, "/mcp", false);
        assertEquals("http://192.168.1.100:8080/mcp", args.resolvedUrl());
    }

    @Test
    void resolvedUrl_normalizes_endpoint_path_without_leading_slash() {
        McpSmokeHarnessArgs args = new McpSmokeHarnessArgs("localhost", 61169, "mcp", false);
        assertEquals("http://localhost:61169/mcp", args.resolvedUrl());
    }

    @Test
    void resolvedUrl_with_custom_endpoint_path() {
        McpSmokeHarnessArgs args = new McpSmokeHarnessArgs("localhost", 61169, "/api/mcp", false);
        assertEquals("http://localhost:61169/api/mcp", args.resolvedUrl());
    }

    @Test
    void mutationsEnabled_defaults_to_false() {
        McpSmokeHarnessArgs args = new McpSmokeHarnessArgs("localhost", 61169, "/mcp", false);
        assertFalse(args.mutationsEnabled());
    }

    @Test
    void mutationsEnabled_can_be_true() {
        McpSmokeHarnessArgs args = new McpSmokeHarnessArgs("localhost", 61169, "/mcp", true);
        assertTrue(args.mutationsEnabled());
    }

    @Test
    void parseArgs_with_no_arguments_uses_defaults() {
        McpSmokeHarnessArgs args = McpSmokeHarnessMain.parseArgs(new String[]{});
        assertEquals("localhost", args.host());
        assertEquals(61169, args.port());
        assertEquals("/mcp", args.endpointPath());
        assertFalse(args.mutationsEnabled());
    }

    @Test
    void parseArgs_with_host_flag() {
        McpSmokeHarnessArgs args = McpSmokeHarnessMain.parseArgs(new String[]{"--host", "192.168.1.50"});
        assertEquals("192.168.1.50", args.host());
    }

    @Test
    void parseArgs_with_short_host_flag() {
        McpSmokeHarnessArgs args = McpSmokeHarnessMain.parseArgs(new String[]{"-h", "myhost"});
        assertEquals("myhost", args.host());
    }

    @Test
    void parseArgs_with_port_flag() {
        McpSmokeHarnessArgs args = McpSmokeHarnessMain.parseArgs(new String[]{"--port", "8080"});
        assertEquals(8080, args.port());
    }

    @Test
    void parseArgs_with_short_port_flag() {
        McpSmokeHarnessArgs args = McpSmokeHarnessMain.parseArgs(new String[]{"-p", "9999"});
        assertEquals(9999, args.port());
    }

    @Test
    void parseArgs_with_endpoint_flag() {
        McpSmokeHarnessArgs args = McpSmokeHarnessMain.parseArgs(new String[]{"--endpoint", "/api/v2/mcp"});
        assertEquals("/api/v2/mcp", args.endpointPath());
    }

    @Test
    void parseArgs_ignores_mutations_cli_flag_per_AC4() {
        // AC4: Mutations MUST be gated by env var ONLY - CLI flags must be ignored
        McpSmokeHarnessArgs args = McpSmokeHarnessMain.parseArgs(new String[]{"--mutations"});
        assertFalse(args.mutationsEnabled(), "CLI --mutations flag must be ignored per AC4 (env var only)");
    }

    @Test
    void parseArgs_ignores_short_mutations_cli_flag_per_AC4() {
        // AC4: Mutations MUST be gated by env var ONLY - CLI flags must be ignored
        McpSmokeHarnessArgs args = McpSmokeHarnessMain.parseArgs(new String[]{"-m"});
        assertFalse(args.mutationsEnabled(), "CLI -m flag must be ignored per AC4 (env var only)");
    }

    @Test
    void parseArgs_with_all_connection_flags() {
        // Note: --mutations is NOT included because AC4 requires env var only
        McpSmokeHarnessArgs args = McpSmokeHarnessMain.parseArgs(new String[]{
                "--host", "bitwig-host",
                "--port", "12345",
                "--endpoint", "/custom/mcp"
        });
        assertEquals("bitwig-host", args.host());
        assertEquals(12345, args.port());
        assertEquals("/custom/mcp", args.endpointPath());
        assertFalse(args.mutationsEnabled(), "Mutations should be off by default (env var only)");
    }

    @Test
    void host_cannot_be_null() {
        assertThrows(NullPointerException.class, () ->
                new McpSmokeHarnessArgs(null, 61169, "/mcp", false));
    }

    @Test
    void endpointPath_cannot_be_null() {
        assertThrows(NullPointerException.class, () ->
                new McpSmokeHarnessArgs("localhost", 61169, null, false));
    }

    // --- Envelope Parsing Tests (CI-safe) ---

    @Test
    void parseEnvelope_success_response() {
        McpSmokeHarness harness = new McpSmokeHarness();
        String json = """
            {"status":"success","data":{"tool":"status"}}
            """;

        McpSmokeHarness.EnvelopeResult result = harness.parseEnvelope(json);

        assertTrue(result.isValidEnvelope(), "Should be valid envelope");
        assertFalse(result.isError(), "Should not be error");
        assertNull(result.errorCode(), "Should have no error code");
    }

    @Test
    void parseEnvelope_typed_error_response() {
        McpSmokeHarness harness = new McpSmokeHarness();
        String json = """
            {"status":"error","error":{"code":"DEVICE_NOT_SELECTED","message":"No device selected","operation":"get_selected_device_parameters"}}
            """;

        McpSmokeHarness.EnvelopeResult result = harness.parseEnvelope(json);

        assertTrue(result.isValidEnvelope(), "Should be valid envelope");
        assertTrue(result.isError(), "Should be error");
        assertEquals("DEVICE_NOT_SELECTED", result.errorCode());
        assertEquals("No device selected", result.errorMessage());
    }

    @Test
    void parseEnvelope_rejects_missing_status() {
        McpSmokeHarness harness = new McpSmokeHarness();
        String json = """
            {"data":{"tool":"status"}}
            """;

        McpSmokeHarness.EnvelopeResult result = harness.parseEnvelope(json);

        assertFalse(result.isValidEnvelope(), "Should be invalid - missing status");
        assertTrue(result.errorMessage().contains("status"), "Should mention missing status field");
    }

    @Test
    void parseEnvelope_rejects_error_without_code() {
        McpSmokeHarness harness = new McpSmokeHarness();
        String json = """
            {"status":"error","error":{"message":"Something failed"}}
            """;

        McpSmokeHarness.EnvelopeResult result = harness.parseEnvelope(json);

        assertFalse(result.isValidEnvelope(), "Should be invalid - error missing code");
        assertTrue(result.errorMessage().contains("code"), "Should mention missing code field");
    }

    @Test
    void parseEnvelope_rejects_success_without_data() {
        McpSmokeHarness harness = new McpSmokeHarness();
        String json = """
            {"status":"success"}
            """;

        McpSmokeHarness.EnvelopeResult result = harness.parseEnvelope(json);

        assertFalse(result.isValidEnvelope(), "Should be invalid - success missing data");
        assertTrue(result.errorMessage().contains("data"), "Should mention missing data field");
    }

    @Test
    void parseEnvelope_rejects_invalid_json() {
        McpSmokeHarness harness = new McpSmokeHarness();
        String json = "not json at all";

        McpSmokeHarness.EnvelopeResult result = harness.parseEnvelope(json);

        assertFalse(result.isValidEnvelope(), "Should be invalid - not JSON");
        assertTrue(result.errorMessage().contains("Invalid JSON"), "Should mention invalid JSON");
    }

    @Test
    void parseEnvelope_rejects_null_response() {
        McpSmokeHarness harness = new McpSmokeHarness();

        McpSmokeHarness.EnvelopeResult result = harness.parseEnvelope(null);

        assertFalse(result.isValidEnvelope(), "Should be invalid - null");
        assertTrue(result.errorMessage().contains("null") || result.errorMessage().contains("empty"),
                "Should mention null/empty");
    }

    @Test
    void parseEnvelope_rejects_empty_response() {
        McpSmokeHarness harness = new McpSmokeHarness();

        McpSmokeHarness.EnvelopeResult result = harness.parseEnvelope("   ");

        assertFalse(result.isValidEnvelope(), "Should be invalid - blank");
    }

    // --- Safe Mode Mutation Guard Tests (CI-safe) ---

    @Test
    void assertToolIsSafeForSafeMode_allowsReadOnlyTools() {
        McpSmokeHarness harness = new McpSmokeHarness();
        // Should not throw for read-only tools
        assertDoesNotThrow(() -> harness.assertToolIsSafeForSafeMode("status"));
        assertDoesNotThrow(() -> harness.assertToolIsSafeForSafeMode("list_tracks"));
        assertDoesNotThrow(() -> harness.assertToolIsSafeForSafeMode("get_track_details"));
    }

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
            public java.util.List<String> listTools() {
                // Return all baseline + mutation tools as available (using actual MCP tool names)
                return java.util.List.of(
                        "status", "list_tracks", "get_track_details", "list_devices_on_track",
                        "get_device_details", "list_scenes", "get_clips_in_scene",
                        "get_selected_device_parameters",
                        "transport_start", "transport_stop",
                        "set_selected_device_parameter", "set_selected_device_parameters",
                        "session_launchSceneByIndex", "session_launchSceneByName", "launch_clip"
                );
            }

            @Override
            public String callTool(String name, java.util.Map<String, Object> args) {
                calledTools.add(name);
                return "{\"status\":\"success\",\"data\":{}}";
            }
        };

        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        java.io.ByteArrayOutputStream err = new java.io.ByteArrayOutputStream();

        harness.run(safeArgs, trackingClient, new java.io.PrintStream(out), new java.io.PrintStream(err));

        // Verify no mutating tools were called (using actual MCP tool names)
        java.util.Set<String> mutatingTools = java.util.Set.of(
                "transport_start", "transport_stop",
                "set_selected_device_parameter", "set_selected_device_parameters",
                "session_launchSceneByIndex", "session_launchSceneByName", "launch_clip"
        );
        for (String mutatingTool : mutatingTools) {
            assertFalse(calledTools.contains(mutatingTool),
                    "Safe mode must NOT call mutating tool: " + mutatingTool);
        }
    }

    // --- HttpMcpClient Response Parsing Tests (CI-safe) ---

    @Test
    void httpMcpClient_extractsTextFromSingleTextContent() {
        String mcpResponse = """
            {
              "jsonrpc": "2.0",
              "id": 1,
              "result": {
                "content": [
                  {"type": "text", "text": "{\\"status\\":\\"success\\",\\"data\\":{}}"}
                ]
              }
            }
            """;

        String extracted = HttpMcpClient.extractToolResult(mcpResponse);
        assertEquals("{\"status\":\"success\",\"data\":{}}", extracted);
    }

    @Test
    void httpMcpClient_concatenatesMultipleTextContents() {
        String mcpResponse = """
            {
              "jsonrpc": "2.0",
              "id": 1,
              "result": {
                "content": [
                  {"type": "text", "text": "first"},
                  {"type": "text", "text": "second"}
                ]
              }
            }
            """;

        String extracted = HttpMcpClient.extractToolResult(mcpResponse);
        assertTrue(extracted.contains("first"), "Should contain first text");
        assertTrue(extracted.contains("second"), "Should contain second text");
    }

    @Test
    void httpMcpClient_handlesNonTextContentGracefully() {
        String mcpResponse = """
            {
              "jsonrpc": "2.0",
              "id": 1,
              "result": {
                "content": [
                  {"type": "image", "data": "base64data", "mimeType": "image/png"}
                ]
              }
            }
            """;

        String extracted = HttpMcpClient.extractToolResult(mcpResponse);
        // Should not return the raw JSON or throw - should return an indication of non-text content
        assertTrue(extracted.contains("non-text") || extracted.contains("image"),
                "Should indicate non-text content was received");
    }

    @Test
    void httpMcpClient_handlesMixedContentTypes() {
        String mcpResponse = """
            {
              "jsonrpc": "2.0",
              "id": 1,
              "result": {
                "content": [
                  {"type": "text", "text": "{\\"status\\":\\"success\\",\\"data\\":{}}"},
                  {"type": "resource", "uri": "file://path"}
                ]
              }
            }
            """;

        String extracted = HttpMcpClient.extractToolResult(mcpResponse);
        // Should extract the text content
        assertTrue(extracted.contains("success"), "Should extract text content");
    }

    @Test
    void httpMcpClient_handlesEmptyContentArray() {
        String mcpResponse = """
            {
              "jsonrpc": "2.0",
              "id": 1,
              "result": {
                "content": []
              }
            }
            """;

        String extracted = HttpMcpClient.extractToolResult(mcpResponse);
        // Should return empty or indication of no content
        assertNotNull(extracted, "Should not return null for empty content");
    }

    @Test
    void httpMcpClient_handlesJsonRpcError() {
        String mcpResponse = """
            {
              "jsonrpc": "2.0",
              "id": 1,
              "error": {
                "code": -32600,
                "message": "Invalid Request"
              }
            }
            """;

        String extracted = HttpMcpClient.extractToolResult(mcpResponse);
        // Should return the error as JSON for caller to handle
        assertTrue(extracted.contains("error") || extracted.contains("-32600"),
                "Should return error information");
    }
}
