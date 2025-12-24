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
 * Communicates with the WigAI MCP server using JSON-RPC over HTTP
 * with Streamable HTTP transport (requires session management).
 */
public final class HttpMcpClient implements McpClient {

    private static final String ACCEPT_HEADER = "text/event-stream";
    private static final String SESSION_HEADER = "mcp-session-id";

    private final String mcpUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final AtomicInteger requestIdCounter;
    private final Duration timeout;
    private String sessionId;

    public HttpMcpClient(String mcpUrl, Duration timeout) {
        this.mcpUrl = mcpUrl;
        this.timeout = timeout;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .build();
        this.objectMapper = new ObjectMapper();
        this.requestIdCounter = new AtomicInteger(1);
        this.sessionId = null;
    }

    /**
     * Initializes the MCP session by performing the initialize handshake.
     * This must be called before any other operations.
     *
     * @throws RuntimeException if initialization fails
     */
    public void initialize() {
        try {
            ObjectNode request = objectMapper.createObjectNode();
            request.put("jsonrpc", "2.0");
            request.put("id", requestIdCounter.getAndIncrement());
            request.put("method", "initialize");

            ObjectNode params = objectMapper.createObjectNode();
            ObjectNode clientInfo = objectMapper.createObjectNode();
            clientInfo.put("name", "MCP Smoke Harness");
            clientInfo.put("version", "1.0.0");
            params.set("clientInfo", clientInfo);
            params.put("protocolVersion", "2024-11-05");
            ObjectNode capabilities = objectMapper.createObjectNode();
            params.set("capabilities", capabilities);
            request.set("params", params);

            // Send initialize request and capture session ID from response header
            // For initialize, accept both JSON and SSE as server may respond with either
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(mcpUrl))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json, " + ACCEPT_HEADER)
                    .timeout(timeout)
                    .POST(HttpRequest.BodyPublishers.ofString(request.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Initialize failed with HTTP " + response.statusCode() + ": " + response.body());
            }

            // Extract session ID from response header
            this.sessionId = response.headers().firstValue(SESSION_HEADER).orElse(null);
            if (this.sessionId == null) {
                throw new RuntimeException("Server did not return session ID in " + SESSION_HEADER + " header");
            }

            // Parse response - may be JSON or SSE format depending on server
            String responseBody = response.body();
            String contentType = response.headers().firstValue("Content-Type").orElse("");
            if (contentType.contains("text/event-stream")) {
                responseBody = parseSSEResponse(responseBody);
            }
            JsonNode jsonResponse = objectMapper.readTree(responseBody);
            if (jsonResponse.has("error")) {
                throw new RuntimeException("Initialize returned error: " + jsonResponse.get("error"));
            }

            // Send initialized notification
            sendInitializedNotification();

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize MCP session: " + e.getMessage(), e);
        }
    }

    private void sendInitializedNotification() throws Exception {
        ObjectNode notification = objectMapper.createObjectNode();
        notification.put("jsonrpc", "2.0");
        notification.put("method", "notifications/initialized");
        // Notifications don't have an id field

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(mcpUrl))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, " + ACCEPT_HEADER)
                .header(SESSION_HEADER, sessionId)
                .timeout(timeout)
                .POST(HttpRequest.BodyPublishers.ofString(notification.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        // Notifications may return 200, 202 (accepted), or 204 (no content)
        if (response.statusCode() != 200 && response.statusCode() != 202 && response.statusCode() != 204) {
            throw new RuntimeException("Initialized notification failed with HTTP " + response.statusCode());
        }
    }

    /**
     * Parses a Server-Sent Events (SSE) response body to extract the JSON data.
     * SSE format has lines like "event: message" and "data: {...json...}".
     *
     * @param sseBody the raw SSE response body
     * @return the extracted JSON data string
     */
    private String parseSSEResponse(String sseBody) {
        if (sseBody == null || sseBody.isBlank()) {
            return "{}";
        }

        StringBuilder jsonData = new StringBuilder();
        for (String line : sseBody.split("\n")) {
            if (line.startsWith("data:")) {
                String data = line.substring(5); // Remove "data:" prefix
                if (data.startsWith(" ")) {
                    data = data.substring(1);
                }
                if (!data.isBlank()) {
                    if (jsonData.length() > 0) {
                        jsonData.append("\n");
                    }
                    jsonData.append(data);
                }
            }
        }

        return jsonData.length() > 0 ? jsonData.toString() : sseBody;
    }

    @Override
    public List<String> listTools() {
        try {
            String responseBody = listToolsRaw();
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
    public String listToolsRaw() {
        try {
            ObjectNode request = objectMapper.createObjectNode();
            request.put("jsonrpc", "2.0");
            request.put("id", requestIdCounter.getAndIncrement());
            request.put("method", "tools/list");
            request.set("params", objectMapper.createObjectNode());

            return sendRequest(request.toString());
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
                String escapedMessage = escapeJsonString(errorMessage);
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

    /**
     * Escapes a string for safe inclusion in a JSON string value.
     * Handles backslashes, quotes, and control characters (newlines, tabs, etc.).
     */
    static String escapeJsonString(String input) {
        if (input == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"' -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                default -> {
                    if (c < 0x20) {
                        // Other control characters: use unicode escape
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    private String sendRequest(String jsonBody) throws Exception {
        if (sessionId == null) {
            throw new IllegalStateException("MCP session not initialized. Call initialize() first.");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(mcpUrl))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, " + ACCEPT_HEADER)
                .header(SESSION_HEADER, sessionId)
                .timeout(timeout)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
        }

        // Parse response - may be JSON or SSE format depending on server
        String responseBody = response.body();
        String contentType = response.headers().firstValue("Content-Type").orElse("");
        if (contentType.contains("text/event-stream")) {
            return parseSSEResponse(responseBody);
        }
        return responseBody;
    }
}
