package io.github.fabb.wigai.smoke;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * HTTP-based MCP client for the smoke harness.
 * Communicates with the WigAI MCP server using JSON-RPC over HTTP.
 */
public final class HttpMcpClient implements McpClient {

    private final String mcpUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final AtomicInteger requestIdCounter;
    private final Duration timeout;

    public HttpMcpClient(String mcpUrl, Duration timeout) {
        this.mcpUrl = mcpUrl;
        this.timeout = timeout;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .build();
        this.objectMapper = new ObjectMapper();
        this.requestIdCounter = new AtomicInteger(1);
    }

    @Override
    public List<String> listTools() {
        try {
            ObjectNode request = objectMapper.createObjectNode();
            request.put("jsonrpc", "2.0");
            request.put("id", requestIdCounter.getAndIncrement());
            request.put("method", "tools/list");
            request.set("params", objectMapper.createObjectNode());

            String responseBody = sendRequest(request.toString());
            JsonNode response = objectMapper.readTree(responseBody);

            List<String> toolNames = new ArrayList<>();
            JsonNode result = response.get("result");
            if (result != null && result.has("tools")) {
                for (JsonNode tool : result.get("tools")) {
                    if (tool.has("name")) {
                        toolNames.add(tool.get("name").asText());
                    }
                }
            }
            return toolNames;
        } catch (Exception e) {
            throw new RuntimeException("Failed to list tools: " + e.getMessage(), e);
        }
    }

    @Override
    public String callTool(String toolName, Map<String, Object> arguments) {
        try {
            ObjectNode request = objectMapper.createObjectNode();
            request.put("jsonrpc", "2.0");
            request.put("id", requestIdCounter.getAndIncrement());
            request.put("method", "tools/call");

            ObjectNode params = objectMapper.createObjectNode();
            params.put("name", toolName);
            params.set("arguments", objectMapper.valueToTree(arguments));
            request.set("params", params);

            String responseBody = sendRequest(request.toString());
            return extractToolResult(responseBody);
        } catch (Exception e) {
            throw new RuntimeException("Failed to call tool " + toolName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Extracts the tool result from an MCP JSON-RPC response.
     * Handles text content, multiple content items, non-text content, and errors.
     *
     * @param responseBody the raw JSON-RPC response body
     * @return the extracted text content or error information
     */
    static String extractToolResult(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "{\"status\":\"error\",\"error\":{\"code\":\"EMPTY_RESPONSE\",\"message\":\"Empty response from server\"}}";
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode response = mapper.readTree(responseBody);

            // Handle JSON-RPC error - wrap in status envelope for parseEnvelope compatibility
            JsonNode error = response.get("error");
            if (error != null) {
                int errorCode = error.has("code") ? error.get("code").asInt() : -1;
                String errorMessage = error.has("message") ? error.get("message").asText() : "Unknown JSON-RPC error";
                // Escape quotes and backslashes in error message for valid JSON
                String escapedMessage = errorMessage.replace("\\", "\\\\").replace("\"", "\\\"");
                return "{\"status\":\"error\",\"error\":{\"code\":\"JSON_RPC_ERROR_" + errorCode + "\",\"message\":\"" +
                        escapedMessage + "\"}}";
            }

            JsonNode result = response.get("result");
            if (result == null) {
                return responseBody; // Return raw if no result
            }

            if (!result.has("content")) {
                return responseBody; // Return raw if no content field
            }

            JsonNode content = result.get("content");
            if (!content.isArray()) {
                return responseBody; // Return raw if content is not an array
            }

            if (content.size() == 0) {
                return ""; // Empty content array
            }

            // Process all content items, collecting text and noting non-text types
            StringBuilder textBuilder = new StringBuilder();
            List<String> nonTextTypes = new ArrayList<>();

            for (JsonNode item : content) {
                String type = item.has("type") ? item.get("type").asText() : "unknown";

                if ("text".equals(type) && item.has("text")) {
                    if (textBuilder.length() > 0) {
                        textBuilder.append("\n");
                    }
                    textBuilder.append(item.get("text").asText());
                } else {
                    nonTextTypes.add(type);
                }
            }

            // If we got text content, return it
            if (textBuilder.length() > 0) {
                return textBuilder.toString();
            }

            // No text content - return indicator of what content types were received
            if (!nonTextTypes.isEmpty()) {
                return "{\"status\":\"error\",\"error\":{\"code\":\"NON_TEXT_CONTENT\"," +
                        "\"message\":\"Response contained non-text content types: " +
                        String.join(", ", nonTextTypes) + "\"}}";
            }

            return responseBody; // Fallback
        } catch (Exception e) {
            return responseBody; // Return raw on parse error
        }
    }

    private String sendRequest(String jsonBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(mcpUrl))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .timeout(timeout)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
        }

        return response.body();
    }
}
