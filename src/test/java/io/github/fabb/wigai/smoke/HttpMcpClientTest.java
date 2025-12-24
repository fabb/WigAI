package io.github.fabb.wigai.smoke;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HttpMcpClient parsing behavior.
 */
class HttpMcpClientTest {

    @DisplayName("1.1-UNIT-031 [P2] Given SSE data without space, when parsed, then extract JSON")
    @Test
    void httpMcpClient_parseSseDataWithoutSpace() throws Exception {
        HttpMcpClient client = new HttpMcpClient("http://localhost:1/mcp", Duration.ofSeconds(1));
        String json = "{\"jsonrpc\":\"2.0\",\"id\":1}";
        String sse = "event: message\n" +
                "data:" + json + "\n\n";

        Method method = HttpMcpClient.class.getDeclaredMethod("parseSSEResponse", String.class);
        method.setAccessible(true);
        String parsed = (String) method.invoke(client, sse);

        assertEquals(json, parsed);
    }

    @DisplayName("1.1-UNIT-032 [P2] Given text content, when extracting, then return text")
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

    @DisplayName("1.1-UNIT-033 [P2] Given multiple text content, when extracting, then concatenate")
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

    @DisplayName("1.1-UNIT-034 [P2] Given non-text content, when extracting, then indicate non-text")
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

    @DisplayName("1.1-UNIT-035 [P2] Given mixed content, when extracting, then return text")
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

    @DisplayName("1.1-UNIT-036 [P2] Given empty content, when extracting, then return non-null")
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

    @DisplayName("1.1-UNIT-037 [P2] Given JSON-RPC error, when extracting, then wrap in envelope")
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
        // Should wrap JSON-RPC error in status envelope for parseEnvelope compatibility
        assertTrue(extracted.contains("\"status\":\"error\""),
                "Should wrap in status envelope");
        assertTrue(extracted.contains("JSON_RPC_ERROR_-32600"),
                "Should include error code in wrapped format");
        assertTrue(extracted.contains("Invalid Request"),
                "Should preserve error message");
    }

    @DisplayName("1.1-UNIT-038 [P2] Given wrapped JSON-RPC error, when parsed, then valid envelope")
    @Test
    void httpMcpClient_jsonRpcErrorParsableByParseEnvelope() {
        String mcpResponse = """
            {
              "jsonrpc": "2.0",
              "id": 1,
              "error": {
                "code": -32601,
                "message": "Method not found"
              }
            }
            """;

        String extracted = HttpMcpClient.extractToolResult(mcpResponse);

        // The wrapped response should be parseable by parseEnvelope
        McpSmokeHarness harness = new McpSmokeHarness();
        McpSmokeHarness.EnvelopeResult envelope = harness.parseEnvelope(extracted);

        assertTrue(envelope.isValidEnvelope(), "Wrapped JSON-RPC error should be a valid envelope");
        assertTrue(envelope.isError(), "Should be recognized as error");
        assertEquals("JSON_RPC_ERROR_-32601", envelope.errorCode());
        assertTrue(envelope.errorMessage().contains("Method not found"));
    }

    @DisplayName("1.1-UNIT-039 [P2] Given control chars, when escaping, then normalize")
    @Test
    void httpMcpClient_escapeJsonString_handlesControlChars() {
        // Test escaping of various control characters
        assertEquals("line1\\nline2", HttpMcpClient.escapeJsonString("line1\nline2"));
        assertEquals("tab\\there", HttpMcpClient.escapeJsonString("tab\there"));
        assertEquals("carriage\\rreturn", HttpMcpClient.escapeJsonString("carriage\rreturn"));
        assertEquals("back\\bspace", HttpMcpClient.escapeJsonString("back\bspace"));
        assertEquals("form\\ffeed", HttpMcpClient.escapeJsonString("form\ffeed"));
    }

    @DisplayName("1.1-UNIT-040 [P2] Given quotes/backslash, when escaping, then safe")
    @Test
    void httpMcpClient_escapeJsonString_handlesQuotesAndBackslash() {
        assertEquals("say \\\"hello\\\"", HttpMcpClient.escapeJsonString("say \"hello\""));
        assertEquals("path\\\\to\\\\file", HttpMcpClient.escapeJsonString("path\\to\\file"));
        assertEquals("mixed \\\"quote\\\" and \\\\backslash",
                HttpMcpClient.escapeJsonString("mixed \"quote\" and \\backslash"));
    }

    @DisplayName("1.1-UNIT-041 [P2] Given null or normal text, when escaping, then safe")
    @Test
    void httpMcpClient_escapeJsonString_handlesNullAndNormalText() {
        assertEquals("", HttpMcpClient.escapeJsonString(null));
        assertEquals("normal text", HttpMcpClient.escapeJsonString("normal text"));
        assertEquals("123 numbers!", HttpMcpClient.escapeJsonString("123 numbers!"));
    }

    @DisplayName("1.1-UNIT-042 [P2] Given JSON-RPC error with control chars, when parsing, then valid")
    @Test
    void httpMcpClient_jsonRpcErrorWithControlCharsIsParseable() {
        // Verify that errors with newlines/special chars produce valid JSON envelopes
        String mcpResponse = """
            {
              "jsonrpc": "2.0",
              "id": 1,
              "error": {
                "code": -32000,
                "message": "Error on line 1\\nDetails on line 2\\twith tab"
              }
            }
            """;

        String extracted = HttpMcpClient.extractToolResult(mcpResponse);

        // The result should be valid JSON that can be parsed
        McpSmokeHarness harness = new McpSmokeHarness();
        McpSmokeHarness.EnvelopeResult envelope = harness.parseEnvelope(extracted);

        assertTrue(envelope.isValidEnvelope(), "Should produce valid JSON even with control chars");
        assertTrue(envelope.isError());
    }
}
