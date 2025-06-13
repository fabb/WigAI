package io.github.fabb.wigai.mcp.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Utility class for testing MCP tool response formats.
 * This utility helps ensure all MCP tools return responses that conform to the API specification
 * and prevents issues like double-wrapping that occurred in the past.
 */
public class McpResponseTestUtils {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Validates that an MCP success response has the correct format according to the API specification.
     * 
     * Expected format:
     * {
     *   "status": "success",
     *   "data": <actual_data>
     * }
     * 
     * @param result The McpSchema.CallToolResult to validate
     * @return The parsed data JsonNode for further assertions
     * @throws Exception if JSON parsing fails
     */
    public static JsonNode validateSuccessResponse(McpSchema.CallToolResult result) throws Exception {
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isError(), "Result should not be an error");
        assertEquals(1, result.content().size(), "Result should have exactly one content item");
        
        McpSchema.TextContent textContent = (McpSchema.TextContent) result.content().get(0);
        JsonNode responseJson = objectMapper.readTree(textContent.text());
        
        // Verify basic structure
        assertTrue(responseJson.has("status"), "Response must have 'status' field");
        assertTrue(responseJson.has("data"), "Response must have 'data' field");
        assertEquals("success", responseJson.get("status").asText(), "Status must be 'success'");
        
        // Verify it's NOT double-wrapped (this would catch the old bug)
        assertFalse(responseJson.has("content"), 
                   "Response should not have 'content' field (indicates double-wrapping)");
        assertFalse(responseJson.has("isError"), 
                   "Response should not have 'isError' field (indicates double-wrapping)");
        
        return responseJson.get("data");
    }
    
    /**
     * Validates that an MCP error response has the correct format according to the API specification.
     * 
     * Expected format:
     * {
     *   "status": "error",
     *   "error": {
     *     "code": "ERROR_CODE",
     *     "message": "Error message",
     *     "operation": "operation_name"
     *   }
     * }
     * 
     * @param result The McpSchema.CallToolResult to validate
     * @return The parsed error JsonNode for further assertions
     * @throws Exception if JSON parsing fails
     */
    public static JsonNode validateErrorResponse(McpSchema.CallToolResult result) throws Exception {
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isError(), "Result should be an error");
        assertEquals(1, result.content().size(), "Result should have exactly one content item");
        
        McpSchema.TextContent textContent = (McpSchema.TextContent) result.content().get(0);
        JsonNode responseJson = objectMapper.readTree(textContent.text());
        
        // Verify basic structure
        assertTrue(responseJson.has("status"), "Response must have 'status' field");
        assertTrue(responseJson.has("error"), "Response must have 'error' field");
        assertEquals("error", responseJson.get("status").asText(), "Status must be 'error'");
        
        // Verify error structure
        JsonNode errorNode = responseJson.get("error");
        assertTrue(errorNode.has("code"), "Error must have 'code' field");
        assertTrue(errorNode.has("message"), "Error must have 'message' field");
        assertTrue(errorNode.has("operation"), "Error must have 'operation' field");
        
        return errorNode;
    }
    
    /**
     * Validates that a list response (like list_tracks) has the correct format.
     * The data should be an array of objects.
     * 
     * @param result The McpSchema.CallToolResult to validate
     * @return The data array as JsonNode for further assertions
     * @throws Exception if JSON parsing fails
     */
    public static JsonNode validateListResponse(McpSchema.CallToolResult result) throws Exception {
        JsonNode dataNode = validateSuccessResponse(result);
        assertTrue(dataNode.isArray(), "Data should be an array for list responses");
        return dataNode;
    }
    
    /**
     * Validates that an object response (like device parameters) has the correct format.
     * The data should be an object.
     * 
     * @param result The McpSchema.CallToolResult to validate
     * @return The data object as JsonNode for further assertions
     * @throws Exception if JSON parsing fails
     */
    public static JsonNode validateObjectResponse(McpSchema.CallToolResult result) throws Exception {
        JsonNode dataNode = validateSuccessResponse(result);
        assertTrue(dataNode.isObject(), "Data should be an object for object responses");
        return dataNode;
    }
    
    /**
     * Validates that an action response (like transport commands) has the correct format.
     * The data should be an object with at least "action" and "message" fields.
     * 
     * @param result The McpSchema.CallToolResult to validate
     * @param expectedAction The expected action value
     * @return The data object as JsonNode for further assertions
     * @throws Exception if JSON parsing fails
     */
    public static JsonNode validateActionResponse(McpSchema.CallToolResult result, String expectedAction) throws Exception {
        JsonNode dataNode = validateObjectResponse(result);
        assertTrue(dataNode.has("action"), "Action response must have 'action' field");
        assertTrue(dataNode.has("message"), "Action response must have 'message' field");
        
        if (expectedAction != null) {
            assertEquals(expectedAction, dataNode.get("action").asText(), 
                        "Action should match expected value");
        }
        
        return dataNode;
    }
    
    /**
     * Validates that a response does NOT have double-wrapping that was present in the old bug.
     * This can be used as a specific regression test.
     * 
     * @param result The McpSchema.CallToolResult to validate
     * @throws Exception if JSON parsing fails
     */
    public static void assertNotDoubleWrapped(McpSchema.CallToolResult result) throws Exception {
        McpSchema.TextContent textContent = (McpSchema.TextContent) result.content().get(0);
        JsonNode responseJson = objectMapper.readTree(textContent.text());
        
        // The old bug would create this structure:
        // {"data": {"content": [...], "isError": false}, "status": "success"}
        // instead of:
        // {"status": "success", "data": [...]}
        
        assertFalse(responseJson.has("content"), 
                   "Response should not have 'content' field (indicates old double-wrapping bug)");
        assertFalse(responseJson.has("isError"), 
                   "Response should not have 'isError' field (indicates old double-wrapping bug)");
        
        // Should have the correct top-level structure
        assertTrue(responseJson.has("status"), "Response should have 'status' at top level");
        assertTrue(responseJson.has("data") || responseJson.has("error"), 
                  "Response should have 'data' or 'error' at top level");
    }
    
    /**
     * Utility method to parse the JSON response from an MCP result for custom validation.
     * 
     * @param result The McpSchema.CallToolResult to parse
     * @return The parsed JsonNode
     * @throws Exception if JSON parsing fails
     */
    public static JsonNode parseResponse(McpSchema.CallToolResult result) throws Exception {
        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.content().size(), "Result should have exactly one content item");
        
        McpSchema.TextContent textContent = (McpSchema.TextContent) result.content().get(0);
        return objectMapper.readTree(textContent.text());
    }
}
