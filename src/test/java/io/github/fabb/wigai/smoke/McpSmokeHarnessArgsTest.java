package io.github.fabb.wigai.smoke;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for McpSmokeHarnessArgs and argument parsing.
 * These tests are CI-safe (no host required).
 */
class McpSmokeHarnessArgsTest {

    @DisplayName("1.1-UNIT-001 [P1] Given defaults, when resolvedUrl, then uses localhost:61169/mcp")
    @Test
    void resolvedUrl_with_defaults() {
        McpSmokeHarnessArgs args = new McpSmokeHarnessArgs("localhost", 61169, "/mcp", false);
        assertEquals("http://localhost:61169/mcp", args.resolvedUrl());
    }

    @DisplayName("1.1-UNIT-002 [P1] Given custom host/port, when resolvedUrl, then uses host/port")
    @Test
    void resolvedUrl_with_custom_host_and_port() {
        McpSmokeHarnessArgs args = new McpSmokeHarnessArgs("192.168.1.100", 8080, "/mcp", false);
        assertEquals("http://192.168.1.100:8080/mcp", args.resolvedUrl());
    }

    @DisplayName("1.1-UNIT-003 [P1] Given endpoint without slash, when resolvedUrl, then normalizes path")
    @Test
    void resolvedUrl_normalizes_endpoint_path_without_leading_slash() {
        McpSmokeHarnessArgs args = new McpSmokeHarnessArgs("localhost", 61169, "mcp", false);
        assertEquals("http://localhost:61169/mcp", args.resolvedUrl());
    }

    @DisplayName("1.1-UNIT-004 [P1] Given custom endpoint, when resolvedUrl, then uses endpoint")
    @Test
    void resolvedUrl_with_custom_endpoint_path() {
        McpSmokeHarnessArgs args = new McpSmokeHarnessArgs("localhost", 61169, "/api/mcp", false);
        assertEquals("http://localhost:61169/api/mcp", args.resolvedUrl());
    }

    @DisplayName("1.1-UNIT-005 [P1] Given defaults, when mutations flag, then disabled")
    @Test
    void mutationsEnabled_defaults_to_false() {
        McpSmokeHarnessArgs args = new McpSmokeHarnessArgs("localhost", 61169, "/mcp", false);
        assertFalse(args.mutationsEnabled());
    }

    @DisplayName("1.1-UNIT-006 [P1] Given env var true, when mutations flag, then enabled")
    @Test
    void mutationsEnabled_can_be_true() {
        McpSmokeHarnessArgs args = new McpSmokeHarnessArgs("localhost", 61169, "/mcp", true);
        assertTrue(args.mutationsEnabled());
    }

    @DisplayName("1.1-UNIT-007 [P1] Given no args, when parseArgs, then defaults apply")
    @Test
    void parseArgs_with_no_arguments_uses_defaults() {
        McpSmokeHarnessArgs args = McpSmokeHarnessMain.parseArgs(new String[]{});
        assertEquals("localhost", args.host());
        assertEquals(61169, args.port());
        assertEquals("/mcp", args.endpointPath());
        assertFalse(args.mutationsEnabled());
    }

    @DisplayName("1.1-UNIT-008 [P1] Given --host, when parseArgs, then host is set")
    @Test
    void parseArgs_with_host_flag() {
        McpSmokeHarnessArgs args = McpSmokeHarnessMain.parseArgs(new String[]{"--host", "192.168.1.50"});
        assertEquals("192.168.1.50", args.host());
    }

    @DisplayName("1.1-UNIT-009 [P1] Given -h, when parseArgs, then host is set")
    @Test
    void parseArgs_with_short_host_flag() {
        McpSmokeHarnessArgs args = McpSmokeHarnessMain.parseArgs(new String[]{"-h", "myhost"});
        assertEquals("myhost", args.host());
    }

    @DisplayName("1.1-UNIT-010 [P1] Given --port, when parseArgs, then port is set")
    @Test
    void parseArgs_with_port_flag() {
        McpSmokeHarnessArgs args = McpSmokeHarnessMain.parseArgs(new String[]{"--port", "8080"});
        assertEquals(8080, args.port());
    }

    @DisplayName("1.1-UNIT-011 [P1] Given -p, when parseArgs, then port is set")
    @Test
    void parseArgs_with_short_port_flag() {
        McpSmokeHarnessArgs args = McpSmokeHarnessMain.parseArgs(new String[]{"-p", "9999"});
        assertEquals(9999, args.port());
    }

    @DisplayName("1.1-UNIT-012 [P2] Given non-numeric port, when parseArgs, then warn and fall back")
    @Test
    void parseArgs_with_non_numeric_port_exits_with_message() {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        try {
            System.setErr(new PrintStream(err));
            McpSmokeHarnessArgs args = McpSmokeHarnessMain.parseArgs(new String[]{"--port", "not-a-number"});

            assertEquals(61169, args.port(), "Should fall back to default port");
            String errText = err.toString();
            assertTrue(errText.contains("Invalid port"), "Should explain invalid port");
            assertTrue(errText.contains("not-a-number"), "Should echo invalid port value");
        } finally {
            System.setErr(originalErr);
        }
    }

    @DisplayName("1.1-UNIT-013 [P1] Given --endpoint, when parseArgs, then endpoint is set")
    @Test
    void parseArgs_with_endpoint_flag() {
        McpSmokeHarnessArgs args = McpSmokeHarnessMain.parseArgs(new String[]{"--endpoint", "/api/v2/mcp"});
        assertEquals("/api/v2/mcp", args.endpointPath());
    }

    @DisplayName("1.1-UNIT-014 [P1] Given --mutations, when parseArgs, then ignore (env var only)")
    @Test
    void parseArgs_ignores_mutations_cli_flag_per_AC4() {
        // AC4: Mutations MUST be gated by env var ONLY - CLI flags must be ignored
        McpSmokeHarnessArgs args = McpSmokeHarnessMain.parseArgs(new String[]{"--mutations"});
        assertFalse(args.mutationsEnabled(), "CLI --mutations flag must be ignored per AC4 (env var only)");
    }

    @DisplayName("1.1-UNIT-015 [P1] Given -m, when parseArgs, then ignore (env var only)")
    @Test
    void parseArgs_ignores_short_mutations_cli_flag_per_AC4() {
        // AC4: Mutations MUST be gated by env var ONLY - CLI flags must be ignored
        McpSmokeHarnessArgs args = McpSmokeHarnessMain.parseArgs(new String[]{"-m"});
        assertFalse(args.mutationsEnabled(), "CLI -m flag must be ignored per AC4 (env var only)");
    }

    @DisplayName("1.1-UNIT-016 [P1] Given all flags, when parseArgs, then apply host/port/endpoint")
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

    @DisplayName("1.1-UNIT-017 [P2] Given null host, when constructing, then throw")
    @Test
    void host_cannot_be_null() {
        assertThrows(NullPointerException.class, () ->
                new McpSmokeHarnessArgs(null, 61169, "/mcp", false));
    }

    @DisplayName("1.1-UNIT-018 [P2] Given null endpoint, when constructing, then throw")
    @Test
    void endpointPath_cannot_be_null() {
        assertThrows(NullPointerException.class, () ->
                new McpSmokeHarnessArgs("localhost", 61169, null, false));
    }
}
