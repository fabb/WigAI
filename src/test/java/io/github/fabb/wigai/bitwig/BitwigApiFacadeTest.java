package io.github.fabb.wigai.bitwig;

import com.bitwig.extension.controller.api.*;
import io.github.fabb.wigai.common.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the BitwigApiFacade class.
 */
public class BitwigApiFacadeTest {

    @Mock
    private ControllerHost mockHost;

    @Mock
    private Transport mockTransport;

    @Mock
    private CursorTrack mockCursorTrack;

    @Mock
    private PinnableCursorDevice mockCursorDevice;

    @Mock
    private CursorRemoteControlsPage mockParameterBank;

    @Mock
    private RemoteControl mockRemoteControl;

    @Mock
    private TrackBank mockTrackBank;

    @Mock
    private Track mockTrack;

    @Mock
    private ClipLauncherSlotBank mockClipLauncherSlotBank;

    @Mock
    private ClipLauncherSlot mockClipLauncherSlot;

    @Mock
    private SceneBank mockSceneBank;

    @Mock
    private Scene mockScene;

    @Mock
    private Application mockApplication;

    @Mock
    private MasterTrack mockMasterTrack;

    @Mock
    private CursorRemoteControlsPage mockProjectParameterBank;

    @Mock
    private RemoteControl mockProjectRemoteControl;

    @Mock
    private Logger mockLogger;

    private BitwigApiFacade bitwigApiFacade;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup basic mocks
        when(mockHost.createTransport()).thenReturn(mockTransport);
        when(mockHost.createApplication()).thenReturn(mockApplication);
        when(mockHost.createCursorTrack(0, 0)).thenReturn(mockCursorTrack);
        when(mockCursorTrack.createCursorDevice()).thenReturn(mockCursorDevice);
        when(mockCursorDevice.createCursorRemoteControlsPage(8)).thenReturn(mockParameterBank);

        // Setup new mocks for story 5.2
        when(mockHost.createMasterTrack(0)).thenReturn(mockMasterTrack);
        when(mockMasterTrack.createCursorRemoteControlsPage(8)).thenReturn(mockProjectParameterBank);

        // Setup TrackBank mocks (new for clip launching)
        when(mockHost.createTrackBank(8, 0, 8)).thenReturn(mockTrackBank);
        when(mockTrackBank.getItemAt(anyInt())).thenReturn(mockTrack);
        when(mockTrack.clipLauncherSlotBank()).thenReturn(mockClipLauncherSlotBank);
        when(mockClipLauncherSlotBank.getItemAt(anyInt())).thenReturn(mockClipLauncherSlot);

        // Setup SceneBank mocks (new for scene launching)
        when(mockHost.createSceneBank(8)).thenReturn(mockSceneBank);
        when(mockSceneBank.getItemAt(anyInt())).thenReturn(mockScene);

        // Setup parameter mocks with lenient stubbing to avoid NPEs
        lenient().when(mockParameterBank.getParameter(anyInt())).thenReturn(mockRemoteControl);
        lenient().when(mockProjectParameterBank.getParameter(anyInt())).thenReturn(mockProjectRemoteControl);

        // Use lenient mocking for the chain calls to avoid NPEs during construction
        lenient().when(mockCursorDevice.exists()).thenReturn(mock(com.bitwig.extension.controller.api.BooleanValue.class));
        lenient().when(mockCursorDevice.name()).thenReturn(mock(com.bitwig.extension.controller.api.SettableStringValue.class));
        lenient().when(mockRemoteControl.name()).thenReturn(mock(com.bitwig.extension.controller.api.SettableStringValue.class));
        lenient().when(mockRemoteControl.value()).thenReturn(mock(com.bitwig.extension.controller.api.SettableRangedValue.class));
        lenient().when(mockRemoteControl.displayedValue()).thenReturn(mock(com.bitwig.extension.controller.api.SettableStringValue.class));

        // Setup project parameter mocks
        lenient().when(mockProjectRemoteControl.exists()).thenReturn(mock(com.bitwig.extension.controller.api.BooleanValue.class));
        lenient().when(mockProjectRemoteControl.name()).thenReturn(mock(com.bitwig.extension.controller.api.SettableStringValue.class));
        lenient().when(mockProjectRemoteControl.value()).thenReturn(mock(com.bitwig.extension.controller.api.SettableRangedValue.class));
        lenient().when(mockProjectRemoteControl.displayedValue()).thenReturn(mock(com.bitwig.extension.controller.api.SettableStringValue.class));

        // Setup TrackBank related mocks with lenient stubbing
        lenient().when(mockTrack.name()).thenReturn(mock(com.bitwig.extension.controller.api.SettableStringValue.class));
        lenient().when(mockTrack.exists()).thenReturn(mock(com.bitwig.extension.controller.api.BooleanValue.class));
        lenient().when(mockClipLauncherSlot.hasContent()).thenReturn(mock(com.bitwig.extension.controller.api.BooleanValue.class));
        lenient().when(mockClipLauncherSlot.isPlaying()).thenReturn(mock(com.bitwig.extension.controller.api.BooleanValue.class));

        // Setup SceneBank related mocks with lenient stubbing
        lenient().when(mockScene.name()).thenReturn(mock(com.bitwig.extension.controller.api.SettableStringValue.class));
        lenient().when(mockScene.exists()).thenReturn(mock(com.bitwig.extension.controller.api.BooleanValue.class));

        // Setup Application mocks for story 5.2
        lenient().when(mockApplication.projectName()).thenReturn(mock(com.bitwig.extension.controller.api.SettableStringValue.class));
        lenient().when(mockApplication.hasActiveEngine()).thenReturn(mock(com.bitwig.extension.controller.api.BooleanValue.class));

        // Setup Transport mocks for story 5.2
        lenient().when(mockTransport.isPlaying()).thenReturn(mock(com.bitwig.extension.controller.api.SettableBooleanValue.class));
        lenient().when(mockTransport.isArrangerRecordEnabled()).thenReturn(mock(com.bitwig.extension.controller.api.SettableBooleanValue.class));
        lenient().when(mockTransport.isArrangerLoopEnabled()).thenReturn(mock(com.bitwig.extension.controller.api.SettableBooleanValue.class));
        lenient().when(mockTransport.isMetronomeEnabled()).thenReturn(mock(com.bitwig.extension.controller.api.SettableBooleanValue.class));
        lenient().when(mockTransport.tempo()).thenReturn(mock(com.bitwig.extension.controller.api.Parameter.class));
        lenient().when(mockTransport.timeSignature()).thenReturn(mock(com.bitwig.extension.controller.api.TimeSignatureValue.class));
        lenient().when(mockTransport.getPosition()).thenReturn(mock(com.bitwig.extension.controller.api.SettableBeatTimeValue.class));
        lenient().when(mockTransport.playPositionInSeconds()).thenReturn(mock(com.bitwig.extension.controller.api.SettableDoubleValue.class));

        // Setup CursorTrack mocks for story 5.2
        lenient().when(mockCursorTrack.trackType()).thenReturn(mock(com.bitwig.extension.controller.api.SettableStringValue.class));
        lenient().when(mockCursorTrack.isGroup()).thenReturn(mock(com.bitwig.extension.controller.api.BooleanValue.class));
        lenient().when(mockCursorTrack.mute()).thenReturn(mock(com.bitwig.extension.controller.api.SettableBooleanValue.class));
        lenient().when(mockCursorTrack.solo()).thenReturn(mock(com.bitwig.extension.controller.api.SoloValue.class));
        lenient().when(mockCursorTrack.arm()).thenReturn(mock(com.bitwig.extension.controller.api.SettableBooleanValue.class));

        bitwigApiFacade = new BitwigApiFacade(mockHost, mockLogger);
    }

    @Test
    void testStartTransport() {
        // Call the method
        bitwigApiFacade.startTransport();

        // Verify the transport's play method was called
        verify(mockTransport).play();

        // Verify logging
        verify(mockLogger).info("BitwigApiFacade: Starting transport playback");
    }

    @Test
    void testStopTransport() {
        // Execute the facade method
        bitwigApiFacade.stopTransport();

        // Verify the transport.stop() was called
        verify(mockTransport).stop();

        // Verify logging
        verify(mockLogger).info("BitwigApiFacade: Stopping transport playback");
    }

    @Test
    void testSetSelectedDeviceParameter_Success() {
        // Arrange
        int parameterIndex = 3;
        double value = 0.75;

        // Mock device exists
        com.bitwig.extension.controller.api.BooleanValue mockExists = mock(com.bitwig.extension.controller.api.BooleanValue.class);
        when(mockExists.get()).thenReturn(true);
        when(mockCursorDevice.exists()).thenReturn(mockExists);

        // Mock parameter value setter
        com.bitwig.extension.controller.api.SettableRangedValue mockValueSetter = mock(com.bitwig.extension.controller.api.SettableRangedValue.class);
        when(mockRemoteControl.value()).thenReturn(mockValueSetter);
        when(mockParameterBank.getParameter(parameterIndex)).thenReturn(mockRemoteControl);

        // Act
        bitwigApiFacade.setSelectedDeviceParameter(parameterIndex, value);

        // Assert
        verify(mockValueSetter).set(value);
        verify(mockLogger).info("BitwigApiFacade: Setting parameter " + parameterIndex + " to " + value);
        verify(mockLogger).info("BitwigApiFacade: Successfully set parameter " + parameterIndex + " to " + value);
    }

    @Test
    void testSetSelectedDeviceParameter_InvalidParameterIndex() {
        // Test invalid parameter index (too high)
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bitwigApiFacade.setSelectedDeviceParameter(8, 0.5);
        });

        assertEquals("Parameter index must be between 0-7, got: 8", exception.getMessage());
        verify(mockLogger).error("BitwigApiFacade: Parameter index must be between 0-7, got: 8");

        // Test invalid parameter index (negative)
        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () -> {
            bitwigApiFacade.setSelectedDeviceParameter(-1, 0.5);
        });

        assertEquals("Parameter index must be between 0-7, got: -1", exception2.getMessage());
        verify(mockLogger).error("BitwigApiFacade: Parameter index must be between 0-7, got: -1");
    }

    @Test
    void testSetSelectedDeviceParameter_InvalidValue() {
        // Test value too high
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bitwigApiFacade.setSelectedDeviceParameter(0, 1.5);
        });

        assertEquals("Parameter value must be between 0.0-1.0, got: 1.5", exception.getMessage());
        verify(mockLogger).error("BitwigApiFacade: Parameter value must be between 0.0-1.0, got: 1.5");

        // Test negative value
        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () -> {
            bitwigApiFacade.setSelectedDeviceParameter(0, -0.1);
        });

        assertEquals("Parameter value must be between 0.0-1.0, got: -0.1", exception2.getMessage());
        verify(mockLogger).error("BitwigApiFacade: Parameter value must be between 0.0-1.0, got: -0.1");
    }

    @Test
    void testSetSelectedDeviceParameter_NoDeviceSelected() {
        // Arrange
        com.bitwig.extension.controller.api.BooleanValue mockExists = mock(com.bitwig.extension.controller.api.BooleanValue.class);
        when(mockExists.get()).thenReturn(false);
        when(mockCursorDevice.exists()).thenReturn(mockExists);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bitwigApiFacade.setSelectedDeviceParameter(0, 0.5);
        });

        assertEquals("No device is currently selected", exception.getMessage());
        verify(mockLogger).error("BitwigApiFacade: No device is currently selected");
    }

    @Test
    void testSetSelectedDeviceParameter_BitwigApiError() {
        // Arrange
        int parameterIndex = 0;
        double value = 0.5;

        // Mock device exists
        com.bitwig.extension.controller.api.BooleanValue mockExists = mock(com.bitwig.extension.controller.api.BooleanValue.class);
        when(mockExists.get()).thenReturn(true);
        when(mockCursorDevice.exists()).thenReturn(mockExists);

        // Mock parameter access that throws exception
        when(mockParameterBank.getParameter(parameterIndex)).thenThrow(new RuntimeException("Bitwig API error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bitwigApiFacade.setSelectedDeviceParameter(parameterIndex, value);
        });

        assertEquals("Failed to set parameter " + parameterIndex + ": Bitwig API error", exception.getMessage());
        assertEquals("Bitwig API error", exception.getCause().getMessage());
        verify(mockLogger).error("BitwigApiFacade: Failed to set parameter " + parameterIndex + ": Bitwig API error");
    }

    @Test
    void testSetSelectedDeviceParameter_BoundaryValues() {
        // Arrange
        com.bitwig.extension.controller.api.BooleanValue mockExists = mock(com.bitwig.extension.controller.api.BooleanValue.class);
        when(mockExists.get()).thenReturn(true);
        when(mockCursorDevice.exists()).thenReturn(mockExists);

        com.bitwig.extension.controller.api.SettableRangedValue mockValueSetter = mock(com.bitwig.extension.controller.api.SettableRangedValue.class);
        when(mockRemoteControl.value()).thenReturn(mockValueSetter);
        when(mockParameterBank.getParameter(anyInt())).thenReturn(mockRemoteControl);

        // Test minimum boundary values
        bitwigApiFacade.setSelectedDeviceParameter(0, 0.0);
        verify(mockValueSetter).set(0.0);

        // Test maximum boundary values
        bitwigApiFacade.setSelectedDeviceParameter(7, 1.0);
        verify(mockValueSetter).set(1.0);

        // Verify parameter bank access for boundary indices
        verify(mockParameterBank, times(2)).getParameter(0);
        verify(mockParameterBank, times(2)).getParameter(7);
    }
}
