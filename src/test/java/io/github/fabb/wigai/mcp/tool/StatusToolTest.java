package io.github.fabb.wigai.mcp.tool;

import io.github.fabb.wigai.WigAIExtensionDefinition;
import io.github.fabb.wigai.bitwig.BitwigApiFacade;
import io.github.fabb.wigai.common.Logger;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StatusToolTest {

    @Mock
    private WigAIExtensionDefinition mockExtensionDefinition;
    @Mock
    private BitwigApiFacade mockBitwigApiFacade;
    @Mock
    private Logger mockLogger;
    @Mock
    private McpSyncServerExchange mockExchange;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetStatusReturnsCorrectInformation() {
        // Define expected values
        String expectedVersion = "1.0.0";
        String expectedProjectName = "Test Project";
        boolean expectedEngineActive = true;

        // Create expected transport data
        Map<String, Object> expectedTransportMap = new LinkedHashMap<>();
        expectedTransportMap.put("playing", false);
        expectedTransportMap.put("recording", false);
        expectedTransportMap.put("loop_active", true);
        expectedTransportMap.put("metronome_active", true);
        expectedTransportMap.put("current_tempo", 125.0);
        expectedTransportMap.put("time_signature", "3/4");
        expectedTransportMap.put("current_beat_str", "2.1.1:0");
        expectedTransportMap.put("current_time_str", "2.1.1:0");

        // Stub mock methods to return these expected values
        when(mockExtensionDefinition.getVersion()).thenReturn(expectedVersion);
        when(mockBitwigApiFacade.getProjectName()).thenReturn(expectedProjectName);
        when(mockBitwigApiFacade.isAudioEngineActive()).thenReturn(expectedEngineActive);
        when(mockBitwigApiFacade.getTransportStatus()).thenReturn(expectedTransportMap);

        // Get the StatusTool specification and handler
        McpServerFeatures.SyncToolSpecification spec = StatusTool.specification(mockExtensionDefinition, mockBitwigApiFacade, mockLogger);
        var handler = StatusTool.getHandler();

        // Call the handler
        McpSchema.CallToolResult result = handler.apply(mockExchange, Map.of());

        // Assert the results
        assertFalse(result.isError());
        assertNotNull(result.content());
        assertTrue(result.content() instanceof List);

        @SuppressWarnings("unchecked")
        List<McpSchema.Content> responseList = (List<McpSchema.Content>) result.content();
        assertEquals(1, responseList.size());
        assertTrue(responseList.get(0) instanceof McpSchema.TextContent);

        McpSchema.TextContent textContent = (McpSchema.TextContent) responseList.get(0);
        String jsonResponse = textContent.text();

        // The response should be a JSON string containing the expected structure
        assertTrue(jsonResponse.contains("\"wigai_version\":\"" + expectedVersion + "\""));
        assertTrue(jsonResponse.contains("\"project_name\":\"" + expectedProjectName + "\""));
        assertTrue(jsonResponse.contains("\"audio_engine_active\":" + expectedEngineActive));
        assertTrue(jsonResponse.contains("\"transport\":{"));
        assertTrue(jsonResponse.contains("\"playing\":false"));
        assertTrue(jsonResponse.contains("\"recording\":false"));
        assertTrue(jsonResponse.contains("\"loop_active\":true"));
        assertTrue(jsonResponse.contains("\"current_tempo\":125.0"));
        assertTrue(jsonResponse.contains("\"time_signature\":\"3/4\""));

        // Verify logger interactions
        verify(mockLogger, times(1)).info("Received 'status' tool call");
        verify(mockLogger, times(1)).info(startsWith("Responding with: {"));

        // Verify BitwigApiFacade interactions
        verify(mockBitwigApiFacade, times(1)).getProjectName();
        verify(mockBitwigApiFacade, times(1)).isAudioEngineActive();
        verify(mockBitwigApiFacade, times(1)).getTransportStatus();
    }
}
