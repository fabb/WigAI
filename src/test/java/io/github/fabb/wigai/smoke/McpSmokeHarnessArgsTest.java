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
}
