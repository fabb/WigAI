package io.github.fabb.wigai.mcp.tool;

import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.features.TransportController;
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
 * Unit tests for the TransportTool class.
 */
public class TransportToolTest {

    @Mock
    private TransportController mockTransportController;

    @Mock
    private Logger mockLogger;

    @Mock
    private McpSyncServerExchange mockExchange;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockTransportController.startTransport()).thenReturn("Transport playback started.");
    }

    @Test
    void testTransportStartToolSpecification() {
        // Get the tool specification
        McpServerFeatures.SyncToolSpecification toolSpec =
            TransportTool.transportStartSpecification(mockTransportController, mockLogger);

        // Verify the tool properties
        assertNotNull(toolSpec);
        assertEquals("transport_start", toolSpec.tool().name());
        assertEquals("Start Bitwig's transport playback.", toolSpec.tool().description());
    }

    @Test
    void testTransportStartToolHandler() {
        // Initialize the handler by calling specification
        TransportTool.transportStartSpecification(mockTransportController, mockLogger);

        // Execute the handler with an empty arguments map
        Map<String, Object> args = Collections.emptyMap();
        McpSchema.CallToolResult result = TransportTool.getTransportStartHandler().apply(mockExchange, args);

        // Verify the controller was called
        verify(mockTransportController).startTransport();

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
        assertEquals("Transport playback started.", textContent.text());

        // Verify logging
        verify(mockLogger).info("Received 'transport_start' tool call");
        verify(mockLogger).info("Responding with: Transport playback started.");
    }

    @Test
    void testTransportStartToolHandlerWithError() {
        // Setup the mock to throw an exception
        when(mockTransportController.startTransport()).thenThrow(new RuntimeException("Test exception"));

        // Initialize the handler by calling specification
        TransportTool.transportStartSpecification(mockTransportController, mockLogger);

        // Execute the handler with an empty arguments map
        Map<String, Object> args = Collections.emptyMap();
        McpSchema.CallToolResult result = TransportTool.getTransportStartHandler().apply(mockExchange, args);

        // Verify the result indicates an error
        assertNotNull(result);
        assertTrue(result.isError());

        // Check content structure
        List<?> content = result.content();
        assertNotNull(content);
        assertEquals(1, content.size());

        // Check the text content contains error message
        Object first = content.get(0);
        assertTrue(first instanceof McpSchema.TextContent);
        McpSchema.TextContent textContent = (McpSchema.TextContent) first;
        assertTrue(textContent.text().contains("Error starting transport"));

        // Verify logging
        verify(mockLogger).info("Received 'transport_start' tool call");
        verify(mockLogger).info(contains("Responding with error"));
    }

    @Test
    void testTransportStopToolSpecification() {
        // Get the tool specification
        McpServerFeatures.SyncToolSpecification toolSpec =
            TransportTool.transportStopSpecification(mockTransportController, mockLogger);

        // Verify the tool properties
        assertNotNull(toolSpec);
        assertEquals("transport_stop", toolSpec.tool().name());
        assertEquals("Stop Bitwig's transport playback.", toolSpec.tool().description());
    }

    @Test
    void testTransportStopToolHandler() {
        // Initialize the handler by calling specification
        TransportTool.transportStopSpecification(mockTransportController, mockLogger);

        // Execute the handler with an empty arguments map
        Map<String, Object> args = Collections.emptyMap();
        when(mockTransportController.stopTransport()).thenReturn("Transport playback stopped.");
        McpSchema.CallToolResult result = TransportTool.getTransportStopHandler().apply(mockExchange, args);

        // Verify the controller was called
        verify(mockTransportController).stopTransport();

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
        assertEquals("Transport playback stopped.", textContent.text());

        // Verify logging
        verify(mockLogger).info("Received 'transport_stop' tool call");
        verify(mockLogger).info("Responding with: Transport playback stopped.");
    }

    @Test
    void testTransportStopToolHandlerWithError() {
        // Setup the mock to throw an exception
        when(mockTransportController.stopTransport()).thenThrow(new RuntimeException("Test exception"));

        // Initialize the handler by calling specification
        TransportTool.transportStopSpecification(mockTransportController, mockLogger);

        // Execute the handler with an empty arguments map
        Map<String, Object> args = Collections.emptyMap();
        McpSchema.CallToolResult result = TransportTool.getTransportStopHandler().apply(mockExchange, args);

        // Verify the result indicates an error
        assertNotNull(result);
        assertTrue(result.isError());

        // Check content structure
        List<?> content = result.content();
        assertNotNull(content);
        assertEquals(1, content.size());

        // Check the text content contains error message
        Object first = content.get(0);
        assertTrue(first instanceof McpSchema.TextContent);
        McpSchema.TextContent textContent = (McpSchema.TextContent) first;
        assertTrue(textContent.text().contains("Error stopping transport"));

        // Verify logging
        verify(mockLogger).info("Received 'transport_stop' tool call");
        verify(mockLogger).info(contains("Responding with error"));
    }
}
