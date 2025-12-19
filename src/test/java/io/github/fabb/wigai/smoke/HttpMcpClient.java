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
            JsonNode response = objectMapper.readTree(responseBody);

            JsonNode result = response.get("result");
            if (result != null && result.has("content")) {
                JsonNode content = result.get("content");
                if (content.isArray() && content.size() > 0) {
                    JsonNode firstContent = content.get(0);
                    if (firstContent.has("text")) {
                        return firstContent.get("text").asText();
                    }
                }
            }

            JsonNode error = response.get("error");
            if (error != null) {
                return objectMapper.writeValueAsString(error);
            }

            return responseBody;
        } catch (Exception e) {
            throw new RuntimeException("Failed to call tool " + toolName + ": " + e.getMessage(), e);
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
