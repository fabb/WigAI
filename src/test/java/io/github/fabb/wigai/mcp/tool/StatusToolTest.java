package io.github.fabb.wigai.mcp.tool;

import com.bitwig.extension.controller.api.*;
import io.github.fabb.wigai.WigAIExtensionDefinition;
import io.github.fabb.wigai.common.Logger;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StatusToolTest {

    @Mock
    private WigAIExtensionDefinition mockExtensionDefinition;
    @Mock
    private Logger mockLogger;
    @Mock
    private ControllerHost mockHost;
    @Mock
    private McpSyncServerExchange mockExchange;

    // Mocks for Bitwig API objects that ControllerHost would return
    @Mock
    private Project mockProject;
    @Mock
    private Application mockApplication;
    @Mock
    private Transport mockTransport;

    // Mocks for Bitwig Value objects
    @Mock
    private StringValue mockProjectNameValue;
    @Mock
    private BooleanValue mockEngineActiveValue;
    @Mock
    private BooleanValue mockPlayingValue;
    @Mock
    private BooleanValue mockRecordingValue;
    @Mock
    private BooleanValue mockRepeatActiveValue;
    @Mock
    private BooleanValue mockMetronomeActiveValue;
    @Mock
    private SettableBeatTimeValue mockTempoValue; // This is the type for transport.tempo()
    @Mock
    private Parameter mockTempoParameter; // This is the type for tempo().value()
    @Mock
    private StringValue mockTimeSignatureValue;
    @Mock
    private BeatTimeValue mockPositionValue; // For both current_beat_str and current_time_str


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup ControllerHost mocks to return other Bitwig API mocks
        when(mockHost.getProject()).thenReturn(mockProject);
        when(mockHost.getApplication()).thenReturn(mockApplication);
        when(mockHost.getTransport()).thenReturn(mockTransport);

        // Setup Bitwig API object mocks to return Value mocks
        when(mockProject.getName()).thenReturn(mockProjectNameValue);
        when(mockApplication.isEngineActive()).thenReturn(mockEngineActiveValue);
        when(mockTransport.isPlaying()).thenReturn(mockPlayingValue);
        when(mockTransport.isArrangerRecordEnabled()).thenReturn(mockRecordingValue);
        when(mockTransport.isArrangerLoopEnabled()).thenReturn(mockRepeatActiveValue);
        when(mockTransport.isMetronomeEnabled()).thenReturn(mockMetronomeActiveValue);
        when(mockTransport.tempo()).thenReturn(mockTempoValue);
        when(mockTempoValue.value()).thenReturn(mockTempoParameter); // tempo().value()
        when(mockTransport.timeSignature()).thenReturn(mockTimeSignatureValue);
        when(mockTransport.getPosition()).thenReturn(mockPositionValue);
    }

    @Test
    void testGetStatusReturnsCorrectInformation() {
        // Define expected values
        String expectedVersion = "1.0.0";
        String expectedProjectName = "Test Project";
        boolean expectedEngineActive = true;
        boolean expectedPlaying = false;
        boolean expectedRecording = false;
        boolean expectedRepeatActive = true;
        boolean expectedMetronomeActive = true;
        double expectedTempo = 125.0;
        String expectedTimeSignature = "3/4";
        String expectedBeatStr = "2.1.1:0";

        // Stub mock methods to return these expected values
        when(mockExtensionDefinition.getVersion()).thenReturn(expectedVersion);
        when(mockProjectNameValue.get()).thenReturn(expectedProjectName);
        when(mockEngineActiveValue.get()).thenReturn(expectedEngineActive);
        when(mockPlayingValue.get()).thenReturn(expectedPlaying);
        when(mockRecordingValue.get()).thenReturn(expectedRecording);
        when(mockRepeatActiveValue.get()).thenReturn(expectedRepeatActive);
        when(mockMetronomeActiveValue.get()).thenReturn(expectedMetronomeActive);
        when(mockTempoParameter.get()).thenReturn(expectedTempo); // tempo().value().get()
        when(mockTimeSignatureValue.get()).thenReturn(expectedTimeSignature);
        when(mockPositionValue.get()).thenReturn(expectedBeatStr);

        // Get the StatusTool specification and handler
        McpServerFeatures.SyncToolSpecification spec = StatusTool.specification(mockExtensionDefinition, mockLogger, mockHost);
        var handler = spec.handler();

        // Call the handler
        McpSchema.CallToolResult result = handler.apply(mockExchange, Map.of());

        // Assert the results
        assertFalse(result.isError());
        assertNotNull(result.response());
        assertTrue(result.response() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, Object> responseMap = (Map<String, Object>) result.response();
        assertEquals(expectedVersion, responseMap.get("wigai_version"));
        assertEquals(expectedProjectName, responseMap.get("project_name"));
        assertEquals(expectedEngineActive, responseMap.get("audio_engine_active"));

        assertTrue(responseMap.get("transport") instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> transportMap = (Map<String, Object>) responseMap.get("transport");
        assertEquals(expectedPlaying, transportMap.get("playing"));
        assertEquals(expectedRecording, transportMap.get("recording"));
        assertEquals(expectedRepeatActive, transportMap.get("repeat_active"));
        assertEquals(expectedMetronomeActive, transportMap.get("metronome_active"));
        assertEquals(expectedTempo, transportMap.get("current_tempo"));
        assertEquals(expectedTimeSignature, transportMap.get("time_signature"));
        assertEquals(expectedBeatStr, transportMap.get("current_beat_str"));
        assertEquals(expectedBeatStr, transportMap.get("current_time_str")); // As per story current_time_str is same as current_beat_str

        // Verify logger interactions
        verify(mockLogger, times(1)).info("Received 'status' tool call");
        verify(mockLogger, times(1)).info(startsWith("Responding with: {"));
    }
}
