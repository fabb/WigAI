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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import io.github.fabb.wigai.common.data.ParameterInfo;

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

        // Create expected project parameters
        List<ParameterInfo> expectedProjectParams = new ArrayList<>();
        expectedProjectParams.add(new ParameterInfo(0, "Project Param 1", 0.5, "50%"));
        expectedProjectParams.add(new ParameterInfo(2, "Project Param 3", 0.8, "80%"));

        // Create expected selected track info
        Map<String, Object> expectedSelectedTrack = new LinkedHashMap<>();
        expectedSelectedTrack.put("index", 1);
        expectedSelectedTrack.put("name", "Lead Synth");
        expectedSelectedTrack.put("type", "instrument");
        expectedSelectedTrack.put("is_group", false);
        expectedSelectedTrack.put("muted", false);
        expectedSelectedTrack.put("soloed", true);
        expectedSelectedTrack.put("armed", false);

        // Create expected selected device info
        Map<String, Object> expectedSelectedDevice = new LinkedHashMap<>();
        expectedSelectedDevice.put("track_name", "Lead Synth");
        expectedSelectedDevice.put("track_index", 1);
        expectedSelectedDevice.put("index", 0);
        expectedSelectedDevice.put("name", "Operator");
        expectedSelectedDevice.put("bypassed", false);

        List<Map<String, Object>> expectedDeviceParams = new ArrayList<>();
        Map<String, Object> deviceParam1 = new LinkedHashMap<>();
        deviceParam1.put("index", 0);
        deviceParam1.put("name", "Cutoff");
        deviceParam1.put("value", 0.7);
        deviceParam1.put("display_value", "70%");
        expectedDeviceParams.add(deviceParam1);

        Map<String, Object> deviceParam2 = new LinkedHashMap<>();
        deviceParam2.put("index", 2);
        deviceParam2.put("name", "Resonance");
        deviceParam2.put("value", 0.3);
        deviceParam2.put("display_value", "30%");
        expectedDeviceParams.add(deviceParam2);

        expectedSelectedDevice.put("parameters", expectedDeviceParams);

        // Stub mock methods to return these expected values
        when(mockExtensionDefinition.getVersion()).thenReturn(expectedVersion);
        when(mockBitwigApiFacade.getProjectName()).thenReturn(expectedProjectName);
        when(mockBitwigApiFacade.isAudioEngineActive()).thenReturn(expectedEngineActive);
        when(mockBitwigApiFacade.getTransportStatus()).thenReturn(expectedTransportMap);
        when(mockBitwigApiFacade.getProjectParameters()).thenReturn(expectedProjectParams);
        when(mockBitwigApiFacade.getSelectedTrackInfo()).thenReturn(expectedSelectedTrack);
        when(mockBitwigApiFacade.getSelectedDeviceInfo()).thenReturn(expectedSelectedDevice);

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

        // Test new project parameters fields
        assertTrue(jsonResponse.contains("\"project_parameters\":["));
        assertTrue(jsonResponse.contains("\"Project Param 1\""));
        assertTrue(jsonResponse.contains("\"Project Param 3\""));
        assertTrue(jsonResponse.contains("\"exists\":true"));

        // Test new selected track fields
        assertTrue(jsonResponse.contains("\"selected_track\":{"));
        assertTrue(jsonResponse.contains("\"name\":\"Lead Synth\""));
        assertTrue(jsonResponse.contains("\"type\":\"instrument\""));
        assertTrue(jsonResponse.contains("\"is_group\":false"));
        assertTrue(jsonResponse.contains("\"soloed\":true"));

        // Test new selected device fields
        assertTrue(jsonResponse.contains("\"selected_device\":{"));
        assertTrue(jsonResponse.contains("\"track_name\":\"Lead Synth\""));
        assertTrue(jsonResponse.contains("\"track_index\":1"));
        assertTrue(jsonResponse.contains("\"index\":0"));
        assertTrue(jsonResponse.contains("\"name\":\"Operator\""));
        assertTrue(jsonResponse.contains("\"bypassed\":false"));
        assertTrue(jsonResponse.contains("\"parameters\":["));
        assertTrue(jsonResponse.contains("\"Cutoff\""));
        assertTrue(jsonResponse.contains("\"Resonance\""));

        // Verify logger interactions
        verify(mockLogger, times(1)).info("Received 'status' tool call");
        verify(mockLogger, times(1)).info(startsWith("Responding with: {"));

        // Verify BitwigApiFacade interactions
        verify(mockBitwigApiFacade, times(1)).getProjectName();
        verify(mockBitwigApiFacade, times(1)).isAudioEngineActive();
        verify(mockBitwigApiFacade, times(1)).getTransportStatus();
        verify(mockBitwigApiFacade, times(1)).getProjectParameters();
        verify(mockBitwigApiFacade, times(1)).getSelectedTrackInfo();
        verify(mockBitwigApiFacade, times(1)).getSelectedDeviceInfo();
    }

    @Test
    void testGetStatusWithNoTrackSelectedAndNoProjectParameters() {
        // Define expected values
        String expectedVersion = "1.0.0";
        String expectedProjectName = "Empty Project";
        boolean expectedEngineActive = false;

        // Create expected transport data
        Map<String, Object> expectedTransportMap = new LinkedHashMap<>();
        expectedTransportMap.put("playing", false);
        expectedTransportMap.put("recording", false);
        expectedTransportMap.put("loop_active", false);
        expectedTransportMap.put("metronome_active", false);
        expectedTransportMap.put("current_tempo", 120.0);
        expectedTransportMap.put("time_signature", "4/4");
        expectedTransportMap.put("current_beat_str", "1.1.1:0");
        expectedTransportMap.put("current_time_str", "0:00.000");

        // Empty project parameters and no selected track
        List<ParameterInfo> emptyProjectParams = new ArrayList<>();

        // Stub mock methods to return these expected values
        when(mockExtensionDefinition.getVersion()).thenReturn(expectedVersion);
        when(mockBitwigApiFacade.getProjectName()).thenReturn(expectedProjectName);
        when(mockBitwigApiFacade.isAudioEngineActive()).thenReturn(expectedEngineActive);
        when(mockBitwigApiFacade.getTransportStatus()).thenReturn(expectedTransportMap);
        when(mockBitwigApiFacade.getProjectParameters()).thenReturn(emptyProjectParams);
        when(mockBitwigApiFacade.getSelectedTrackInfo()).thenReturn(null);
        when(mockBitwigApiFacade.getSelectedDeviceInfo()).thenReturn(null);

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

        // Test that project_parameters is an empty array
        assertTrue(jsonResponse.contains("\"project_parameters\":[]"));

        // Test that selected_track is null
        assertTrue(jsonResponse.contains("\"selected_track\":null"));

        // Test that selected_device is null
        assertTrue(jsonResponse.contains("\"selected_device\":null"));

        // Verify BitwigApiFacade interactions
        verify(mockBitwigApiFacade, times(1)).getProjectParameters();
        verify(mockBitwigApiFacade, times(1)).getSelectedTrackInfo();
        verify(mockBitwigApiFacade, times(1)).getSelectedDeviceInfo();
    }
}
