package io.github.fabb.wigai.smoke;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for envelope parsing behavior.
 */
class McpSmokeHarnessEnvelopeTest {

    @DisplayName("1.1-UNIT-019 [P2] Given success envelope, when parsed, then valid")
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

    @DisplayName("1.1-UNIT-020 [P2] Given typed error envelope, when parsed, then capture code/message")
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

    @DisplayName("1.1-UNIT-021 [P2] Given missing status, when parsed, then invalid")
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

    @DisplayName("1.1-UNIT-022 [P2] Given error without code, when parsed, then invalid")
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

    @DisplayName("1.1-UNIT-023 [P2] Given success without data, when parsed, then invalid")
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

    @DisplayName("1.1-UNIT-024 [P2] Given invalid JSON, when parsed, then invalid")
    @Test
    void parseEnvelope_rejects_invalid_json() {
        McpSmokeHarness harness = new McpSmokeHarness();
        String json = "not json at all";

        McpSmokeHarness.EnvelopeResult result = harness.parseEnvelope(json);

        assertFalse(result.isValidEnvelope(), "Should be invalid - not JSON");
        assertTrue(result.errorMessage().contains("Invalid JSON"), "Should mention invalid JSON");
    }

    @DisplayName("1.1-UNIT-025 [P2] Given null response, when parsed, then invalid")
    @Test
    void parseEnvelope_rejects_null_response() {
        McpSmokeHarness harness = new McpSmokeHarness();

        McpSmokeHarness.EnvelopeResult result = harness.parseEnvelope(null);

        assertFalse(result.isValidEnvelope(), "Should be invalid - null");
        assertTrue(result.errorMessage().contains("null") || result.errorMessage().contains("empty"),
                "Should mention null/empty");
    }

    @DisplayName("1.1-UNIT-026 [P2] Given empty response, when parsed, then invalid")
    @Test
    void parseEnvelope_rejects_empty_response() {
        McpSmokeHarness harness = new McpSmokeHarness();

        McpSmokeHarness.EnvelopeResult result = harness.parseEnvelope("   ");

        assertFalse(result.isValidEnvelope(), "Should be invalid - blank");
    }
}
