package io.github.fabb.wigai.mcp.tool;

import io.github.fabb.wigai.WigAIExtensionDefinition;
import io.github.fabb.wigai.common.Logger;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the StatusTool class.
 */
public class StatusToolTest {

    @Mock
    private WigAIExtensionDefinition mockExtensionDefinition;

    @Mock
    private Logger mockLogger;

    @Mock
    private McpSyncServerExchange mockExchange;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockExtensionDefinition.getVersion()).thenReturn("0.2.0");
    }

    @Test
    void testStatusToolSpecification() {
        // Get the tool specification
        McpServerFeatures.SyncToolSpecification toolSpec =
            StatusTool.specification(mockExtensionDefinition, mockLogger);

        // Verify the tool properties
        assertNotNull(toolSpec);
        assertEquals("status", toolSpec.tool().name());
        assertEquals("Get WigAI operational status and version information.",
                     toolSpec.tool().description());
    }

    @Test
    void testStatusToolHandler() {
        // Initialize the handler by calling specification
        StatusTool.specification(mockExtensionDefinition, mockLogger);

        // Execute the handler with an empty arguments map
        Map<String, Object> args = Collections.emptyMap();
        McpSchema.CallToolResult result = StatusTool.getHandler().apply(mockExchange, args);

        // Verify the result
        assertNotNull(result);
        assertFalse(result.isError());

        // Check content structure
        List<?> content = result.content();
        assertNotNull(content);
        assertEquals(1, content.size());

        // Check the text content
        Object first = content.get(0);
        assertTrue(first instanceof McpSchema.TextContent);
        McpSchema.TextContent textContent = (McpSchema.TextContent) first;
        assertEquals("WigAI v0.2.0 is operational", textContent.text());

        // Verify logging
        verify(mockLogger).info("Received 'status' tool call");
        verify(mockLogger).info("Responding with: WigAI v0.2.0 is operational");
    }
}
