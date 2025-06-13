package io.github.fabb.wigai.bitwig;

import com.bitwig.extension.controller.api.*;
import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.common.error.BitwigApiException;
import io.github.fabb.wigai.common.error.ErrorCode;
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

    @Mock
    private DeviceBank mockDeviceBank;

    @Mock
    private Device mockDevice;

    private BitwigApiFacade bitwigApiFacade;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup basic mocks
        when(mockHost.createTransport()).thenReturn(mockTransport);
        when(mockHost.createApplication()).thenReturn(mockApplication);
        when(mockHost.createCursorTrack(0, 0)).thenReturn(mockCursorTrack);
        when(mockCursorTrack.createCursorDevice()).thenReturn(mockCursorDevice);
        when(mockCursorDevice.createCursorRemoteControlsPage(128)).thenReturn(mockParameterBank);

        // Setup new mocks for story 5.2
        when(mockHost.createMasterTrack(0)).thenReturn(mockMasterTrack);
        when(mockMasterTrack.createCursorRemoteControlsPage(128)).thenReturn(mockProjectParameterBank);

        // Setup TrackBank mocks (new for clip launching) - use smaller sizes for testing
        when(mockHost.createTrackBank(128, 0, 128)).thenReturn(mockTrackBank);
        when(mockTrackBank.getSizeOfBank()).thenReturn(8); // Reduced from 128 to 8 for testing
        when(mockTrackBank.getItemAt(anyInt())).thenReturn(mockTrack);
        when(mockTrack.clipLauncherSlotBank()).thenReturn(mockClipLauncherSlotBank);
        when(mockClipLauncherSlotBank.getSizeOfBank()).thenReturn(8); // Reduced from 128 to 8 for testing
        when(mockClipLauncherSlotBank.getItemAt(anyInt())).thenReturn(mockClipLauncherSlot);

        // Setup SceneBank mocks (new for scene launching) - use smaller sizes for testing
        when(mockHost.createSceneBank(128)).thenReturn(mockSceneBank);
        when(mockSceneBank.getItemAt(anyInt())).thenReturn(mockScene);
        when(mockSceneBank.getSizeOfBank()).thenReturn(8); // Reduced from 128 to 8 for testing

        // Setup parameter mocks with lenient stubbing to avoid NPEs
        lenient().when(mockParameterBank.getParameter(anyInt())).thenReturn(mockRemoteControl);
        lenient().when(mockProjectParameterBank.getParameter(anyInt())).thenReturn(mockProjectRemoteControl);

        // Use lenient mocking for the chain calls to avoid NPEs during construction
        lenient().when(mockCursorDevice.exists()).thenReturn(mock(com.bitwig.extension.controller.api.BooleanValue.class));
        lenient().when(mockCursorDevice.name()).thenReturn(mock(com.bitwig.extension.controller.api.SettableStringValue.class));
        lenient().when(mockCursorDevice.isEnabled()).thenReturn(mock(com.bitwig.extension.controller.api.SettableBooleanValue.class));
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
        lenient().when(mockTrack.trackType()).thenReturn(mock(com.bitwig.extension.controller.api.SettableStringValue.class));
        lenient().when(mockTrack.isGroup()).thenReturn(mock(com.bitwig.extension.controller.api.BooleanValue.class));
        lenient().when(mockTrack.isActivated()).thenReturn(mock(com.bitwig.extension.controller.api.SettableBooleanValue.class));
        lenient().when(mockTrack.color()).thenReturn(mock(com.bitwig.extension.controller.api.SettableColorValue.class));

        // Setup device bank mocks for tracks - use smaller sizes for testing
        lenient().when(mockTrack.createDeviceBank(128)).thenReturn(mockDeviceBank);
        lenient().when(mockDeviceBank.getSizeOfBank()).thenReturn(8);
        lenient().when(mockDeviceBank.getItemAt(anyInt())).thenReturn(mockDevice);
        lenient().when(mockDevice.exists()).thenReturn(mock(com.bitwig.extension.controller.api.BooleanValue.class));
        lenient().when(mockDevice.name()).thenReturn(mock(com.bitwig.extension.controller.api.SettableStringValue.class));
        lenient().when(mockDevice.isEnabled()).thenReturn(mock(com.bitwig.extension.controller.api.SettableBooleanValue.class));
        lenient().when(mockDevice.deviceType()).thenReturn(mock(com.bitwig.extension.controller.api.EnumValue.class));

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
        com.bitwig.extension.controller.api.Parameter mockTempo = mock(com.bitwig.extension.controller.api.Parameter.class);
        lenient().when(mockTempo.value()).thenReturn(mock(com.bitwig.extension.controller.api.SettableRangedValue.class));
        lenient().when(mockTransport.tempo()).thenReturn(mockTempo);
        lenient().when(mockTransport.timeSignature()).thenReturn(mock(com.bitwig.extension.controller.api.TimeSignatureValue.class));
        lenient().when(mockTransport.getPosition()).thenReturn(mock(com.bitwig.extension.controller.api.SettableBeatTimeValue.class));
        lenient().when(mockTransport.playPositionInSeconds()).thenReturn(mock(com.bitwig.extension.controller.api.SettableDoubleValue.class));

        // Setup CursorTrack mocks for story 5.2
        lenient().when(mockCursorTrack.exists()).thenReturn(mock(com.bitwig.extension.controller.api.BooleanValue.class));
        lenient().when(mockCursorTrack.name()).thenReturn(mock(com.bitwig.extension.controller.api.SettableStringValue.class));
        lenient().when(mockCursorTrack.trackType()).thenReturn(mock(com.bitwig.extension.controller.api.SettableStringValue.class));
        lenient().when(mockCursorTrack.isGroup()).thenReturn(mock(com.bitwig.extension.controller.api.BooleanValue.class));
        lenient().when(mockCursorTrack.mute()).thenReturn(mock(com.bitwig.extension.controller.api.SettableBooleanValue.class));
        lenient().when(mockCursorTrack.solo()).thenReturn(mock(com.bitwig.extension.controller.api.SoloValue.class));
        lenient().when(mockCursorTrack.arm()).thenReturn(mock(com.bitwig.extension.controller.api.SettableBooleanValue.class));

        // Setup parameter count mocks
        when(mockParameterBank.getParameterCount()).thenReturn(8);  // Default to 8 for device parameters
        when(mockProjectParameterBank.getParameterCount()).thenReturn(8);  // Default to 8 for project parameters

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
        // Mock device exists for this test
        com.bitwig.extension.controller.api.BooleanValue mockExists = mock(com.bitwig.extension.controller.api.BooleanValue.class);
        when(mockExists.get()).thenReturn(true);
        when(mockCursorDevice.exists()).thenReturn(mockExists);

        // Test invalid parameter index (too high)
        BitwigApiException exception = assertThrows(BitwigApiException.class, () -> {
            bitwigApiFacade.setSelectedDeviceParameter(8, 0.5);
        });

        assertEquals(ErrorCode.INVALID_RANGE, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("parameter_index must be between 0 and 7, got: 8"));

        // Test invalid parameter index (negative)
        BitwigApiException exception2 = assertThrows(BitwigApiException.class, () -> {
            bitwigApiFacade.setSelectedDeviceParameter(-1, 0.5);
        });

        assertEquals(ErrorCode.INVALID_RANGE, exception2.getErrorCode());
        assertTrue(exception2.getMessage().contains("parameter_index must be between 0 and 7, got: -1"));
    }

    @Test
    void testSetSelectedDeviceParameter_InvalidValue() {
        // Mock device exists for this test
        com.bitwig.extension.controller.api.BooleanValue mockExists = mock(com.bitwig.extension.controller.api.BooleanValue.class);
        when(mockExists.get()).thenReturn(true);
        when(mockCursorDevice.exists()).thenReturn(mockExists);

        // Test value too high
        BitwigApiException exception = assertThrows(BitwigApiException.class, () -> {
            bitwigApiFacade.setSelectedDeviceParameter(0, 1.5);
        });

        assertEquals(ErrorCode.INVALID_RANGE, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("value must be between 0.0 and 1.0, got: 1.5"));

        // Test negative value
        BitwigApiException exception2 = assertThrows(BitwigApiException.class, () -> {
            bitwigApiFacade.setSelectedDeviceParameter(0, -0.1);
        });

        assertEquals(ErrorCode.INVALID_RANGE, exception2.getErrorCode());
        assertTrue(exception2.getMessage().contains("value must be between 0.0 and 1.0, got: -0.1"));
    }

    @Test
    void testSetSelectedDeviceParameter_NoDeviceSelected() {
        // Arrange
        com.bitwig.extension.controller.api.BooleanValue mockExists = mock(com.bitwig.extension.controller.api.BooleanValue.class);
        when(mockExists.get()).thenReturn(false);
        when(mockCursorDevice.exists()).thenReturn(mockExists);

        // Act & Assert
        BitwigApiException exception = assertThrows(BitwigApiException.class, () -> {
            bitwigApiFacade.setSelectedDeviceParameter(0, 0.5);
        });

        assertEquals(ErrorCode.DEVICE_NOT_SELECTED, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("No device is currently selected"));
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
        BitwigApiException exception = assertThrows(BitwigApiException.class, () -> {
            bitwigApiFacade.setSelectedDeviceParameter(parameterIndex, value);
        });

        // Current implementation returns OPERATION_FAILED for all RuntimeException
        assertEquals(ErrorCode.OPERATION_FAILED, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Bitwig API error"));
        assertEquals("Bitwig API error", exception.getCause().getMessage());
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

    @Test
    void testGetSelectedDeviceInfo_WithDeviceSelected() {
        // Arrange
        // Mock device exists
        com.bitwig.extension.controller.api.BooleanValue mockDeviceExists = mock(com.bitwig.extension.controller.api.BooleanValue.class);
        when(mockDeviceExists.get()).thenReturn(true);
        when(mockCursorDevice.exists()).thenReturn(mockDeviceExists);

        // Mock device name
        com.bitwig.extension.controller.api.SettableStringValue mockDeviceName = mock(com.bitwig.extension.controller.api.SettableStringValue.class);
        when(mockDeviceName.get()).thenReturn("Test Device");
        when(mockCursorDevice.name()).thenReturn(mockDeviceName);

        // Mock device enabled status
        com.bitwig.extension.controller.api.SettableBooleanValue mockDeviceEnabled = mock(com.bitwig.extension.controller.api.SettableBooleanValue.class);
        when(mockDeviceEnabled.get()).thenReturn(true);
        when(mockCursorDevice.isEnabled()).thenReturn(mockDeviceEnabled);

        // Mock cursor track name and exists
        com.bitwig.extension.controller.api.SettableStringValue mockTrackName = mock(com.bitwig.extension.controller.api.SettableStringValue.class);
        when(mockTrackName.get()).thenReturn("Test Track");
        when(mockCursorTrack.name()).thenReturn(mockTrackName);

        // Mock track bank for finding track index
        when(mockTrackBank.getSizeOfBank()).thenReturn(8);
        com.bitwig.extension.controller.api.BooleanValue mockTrackExists = mock(com.bitwig.extension.controller.api.BooleanValue.class);
        when(mockTrackExists.get()).thenReturn(true);
        when(mockTrack.exists()).thenReturn(mockTrackExists);

        com.bitwig.extension.controller.api.SettableStringValue mockBankTrackName = mock(com.bitwig.extension.controller.api.SettableStringValue.class);
        when(mockBankTrackName.get()).thenReturn("Test Track");
        when(mockTrack.name()).thenReturn(mockBankTrackName);

        // Mock device parameters
        for (int i = 0; i < 8; i++) {
            RemoteControl mockParam = mock(RemoteControl.class);

            com.bitwig.extension.controller.api.SettableStringValue mockParamName = mock(com.bitwig.extension.controller.api.SettableStringValue.class);
            com.bitwig.extension.controller.api.SettableRangedValue mockParamValue = mock(com.bitwig.extension.controller.api.SettableRangedValue.class);
            com.bitwig.extension.controller.api.SettableStringValue mockParamDisplay = mock(com.bitwig.extension.controller.api.SettableStringValue.class);

            if (i == 0) {
                when(mockParamName.get()).thenReturn("Cutoff");
                when(mockParamValue.get()).thenReturn(0.7);
                when(mockParamDisplay.get()).thenReturn("70%");
            } else if (i == 2) {
                when(mockParamName.get()).thenReturn("Resonance");
                when(mockParamValue.get()).thenReturn(0.3);
                when(mockParamDisplay.get()).thenReturn("30%");
            } else {
                when(mockParamName.get()).thenReturn("");  // Empty name for unused parameters
                when(mockParamValue.get()).thenReturn(0.0);
                when(mockParamDisplay.get()).thenReturn("0%");
            }

            when(mockParam.name()).thenReturn(mockParamName);
            when(mockParam.value()).thenReturn(mockParamValue);
            when(mockParam.displayedValue()).thenReturn(mockParamDisplay);
            when(mockParameterBank.getParameter(i)).thenReturn(mockParam);
        }

        // Act
        java.util.Map<String, Object> result = bitwigApiFacade.getSelectedDeviceInfo();

        // Assert
        assertNotNull(result);
        assertEquals("Test Track", result.get("track_name"));
        assertEquals(0, result.get("track_index"));  // Found at index 0
        assertEquals(0, result.get("index"));  // Device index in chain
        assertEquals("Test Device", result.get("name"));
        assertEquals(false, result.get("bypassed"));  // Device is enabled, so not bypassed

        @SuppressWarnings("unchecked")
        java.util.List<java.util.Map<String, Object>> parameters = (java.util.List<java.util.Map<String, Object>>) result.get("parameters");
        assertEquals(2, parameters.size());  // Only 2 parameters with non-empty names

        java.util.Map<String, Object> param0 = parameters.get(0);
        assertEquals(0, param0.get("index"));
        assertEquals("Cutoff", param0.get("name"));
        assertEquals(0.7, param0.get("value"));
        assertEquals("70%", param0.get("display_value"));

        java.util.Map<String, Object> param2 = parameters.get(1);
        assertEquals(2, param2.get("index"));
        assertEquals("Resonance", param2.get("name"));
        assertEquals(0.3, param2.get("value"));
        assertEquals("30%", param2.get("display_value"));

        verify(mockLogger).info("BitwigApiFacade: Getting selected device information");
        verify(mockLogger).info("BitwigApiFacade: Retrieved selected device info: Test Device");
    }

    @Test
    void testGetSelectedDeviceInfo_NoDeviceSelected() {
        // Arrange
        com.bitwig.extension.controller.api.BooleanValue mockDeviceExists = mock(com.bitwig.extension.controller.api.BooleanValue.class);
        when(mockDeviceExists.get()).thenReturn(false);
        when(mockCursorDevice.exists()).thenReturn(mockDeviceExists);

        // Act
        java.util.Map<String, Object> result = bitwigApiFacade.getSelectedDeviceInfo();

        // Assert
        assertNull(result);
        verify(mockLogger).info("BitwigApiFacade: Getting selected device information");
        verify(mockLogger).info("BitwigApiFacade: No device selected");
    }

    @Test
    void testGetSelectedDeviceInfo_TrackNotFoundInBank() {
        // Arrange
        // Mock device exists
        com.bitwig.extension.controller.api.BooleanValue mockDeviceExists = mock(com.bitwig.extension.controller.api.BooleanValue.class);
        when(mockDeviceExists.get()).thenReturn(true);
        when(mockCursorDevice.exists()).thenReturn(mockDeviceExists);

        // Mock device properties
        com.bitwig.extension.controller.api.SettableStringValue mockDeviceName = mock(com.bitwig.extension.controller.api.SettableStringValue.class);
        when(mockDeviceName.get()).thenReturn("Test Device");
        when(mockCursorDevice.name()).thenReturn(mockDeviceName);

        com.bitwig.extension.controller.api.SettableBooleanValue mockDeviceEnabled = mock(com.bitwig.extension.controller.api.SettableBooleanValue.class);
        when(mockDeviceEnabled.get()).thenReturn(false);  // Device is bypassed
        when(mockCursorDevice.isEnabled()).thenReturn(mockDeviceEnabled);

        // Mock cursor track name
        com.bitwig.extension.controller.api.SettableStringValue mockTrackName = mock(com.bitwig.extension.controller.api.SettableStringValue.class);
        when(mockTrackName.get()).thenReturn("Unknown Track");
        when(mockCursorTrack.name()).thenReturn(mockTrackName);

        // Mock track bank - no matching tracks
        when(mockTrackBank.getSizeOfBank()).thenReturn(8);
        com.bitwig.extension.controller.api.BooleanValue mockTrackExists = mock(com.bitwig.extension.controller.api.BooleanValue.class);
        when(mockTrackExists.get()).thenReturn(true);
        when(mockTrack.exists()).thenReturn(mockTrackExists);

        com.bitwig.extension.controller.api.SettableStringValue mockBankTrackName = mock(com.bitwig.extension.controller.api.SettableStringValue.class);
        when(mockBankTrackName.get()).thenReturn("Different Track");  // Different name
        when(mockTrack.name()).thenReturn(mockBankTrackName);

        // Mock empty device parameters
        for (int i = 0; i < 8; i++) {
            RemoteControl mockParam = mock(RemoteControl.class);

            com.bitwig.extension.controller.api.SettableStringValue mockParamName = mock(com.bitwig.extension.controller.api.SettableStringValue.class);
            when(mockParamName.get()).thenReturn("");  // Empty names

            when(mockParam.name()).thenReturn(mockParamName);
            when(mockParameterBank.getParameter(i)).thenReturn(mockParam);
        }

        // Act
        java.util.Map<String, Object> result = bitwigApiFacade.getSelectedDeviceInfo();

        // Assert
        assertNotNull(result);
        assertEquals("Unknown Track", result.get("track_name"));
        assertEquals(-1, result.get("track_index"));  // Not found in bank
        assertEquals(0, result.get("index"));
        assertEquals("Test Device", result.get("name"));
        assertEquals(true, result.get("bypassed"));  // Device is disabled, so bypassed

        @SuppressWarnings("unchecked")
        java.util.List<java.util.Map<String, Object>> parameters = (java.util.List<java.util.Map<String, Object>>) result.get("parameters");
        assertEquals(0, parameters.size());  // No parameters with non-empty names
    }

    @Test
    void testGetAllTracksInfo_WithFilterAndActivation() {
        // Arrange - setup tracks with proper activation status
        when(mockTrackBank.getSizeOfBank()).thenReturn(3);

        // Mock cursor track for selection detection
        com.bitwig.extension.controller.api.BooleanValue mockCursorExists = mock(com.bitwig.extension.controller.api.BooleanValue.class);
        when(mockCursorExists.get()).thenReturn(true);
        when(mockCursorTrack.exists()).thenReturn(mockCursorExists);

        com.bitwig.extension.controller.api.SettableStringValue mockCursorName = mock(com.bitwig.extension.controller.api.SettableStringValue.class);
        when(mockCursorName.get()).thenReturn("Track 2");
        when(mockCursorTrack.name()).thenReturn(mockCursorName);

        // Setup 3 different tracks
        Track[] tracks = new Track[3];
        for (int i = 0; i < 3; i++) {
            tracks[i] = mock(Track.class);

            // Track exists
            com.bitwig.extension.controller.api.BooleanValue mockExists = mock(com.bitwig.extension.controller.api.BooleanValue.class);
            when(mockExists.get()).thenReturn(true);
            when(tracks[i].exists()).thenReturn(mockExists);

            // Track name
            com.bitwig.extension.controller.api.SettableStringValue mockName = mock(com.bitwig.extension.controller.api.SettableStringValue.class);
            when(mockName.get()).thenReturn("Track " + (i + 1));
            when(tracks[i].name()).thenReturn(mockName);

            // Track type
            com.bitwig.extension.controller.api.SettableStringValue mockType = mock(com.bitwig.extension.controller.api.SettableStringValue.class);
            String trackType = (i == 0) ? "AUDIO" : (i == 1) ? "INSTRUMENT" : "GROUP";
            when(mockType.get()).thenReturn(trackType);
            when(tracks[i].trackType()).thenReturn(mockType);

            // Track group status
            com.bitwig.extension.controller.api.BooleanValue mockIsGroup = mock(com.bitwig.extension.controller.api.BooleanValue.class);
            when(mockIsGroup.get()).thenReturn(i == 2); // Only track 3 is a group
            when(tracks[i].isGroup()).thenReturn(mockIsGroup);

            // Track activation status - test the new functionality
            com.bitwig.extension.controller.api.SettableBooleanValue mockActivated = mock(com.bitwig.extension.controller.api.SettableBooleanValue.class);
            when(mockActivated.get()).thenReturn(i != 1); // Track 2 is deactivated
            when(tracks[i].isActivated()).thenReturn(mockActivated);

            // Track color
            com.bitwig.extension.controller.api.SettableColorValue mockColor = mock(com.bitwig.extension.controller.api.SettableColorValue.class);
            when(tracks[i].color()).thenReturn(mockColor);

            // No parent track for this test
            when(tracks[i].createParentTrack(0, 0)).thenReturn(null);

            // Mock device bank for each track
            DeviceBank mockDeviceBank = mock(DeviceBank.class);
            when(tracks[i].createDeviceBank(8)).thenReturn(mockDeviceBank);
            when(mockDeviceBank.getSizeOfBank()).thenReturn(0); // No devices for simplicity

            when(mockTrackBank.getItemAt(i)).thenReturn(tracks[i]);
        }

        // Act - get all tracks without filter
        java.util.List<java.util.Map<String, Object>> allTracks = bitwigApiFacade.getAllTracksInfo(null);

        // Act - get tracks with audio filter
        java.util.List<java.util.Map<String, Object>> audioTracks = bitwigApiFacade.getAllTracksInfo("audio");

        // Assert - all tracks
        assertEquals(3, allTracks.size());

        // Verify Track 1 (Audio, activated)
        java.util.Map<String, Object> track1 = allTracks.get(0);
        assertEquals(0, track1.get("index"));
        assertEquals("Track 1", track1.get("name"));
        assertEquals("audio", track1.get("type"));
        assertEquals(false, track1.get("is_group"));
        assertEquals(null, track1.get("parent_group_index"));
        assertEquals(true, track1.get("activated")); // Activated
        assertEquals("rgb(128,128,128)", track1.get("color"));
        assertEquals(false, track1.get("is_selected"));

        // Verify Track 2 (Instrument, deactivated, selected)
        java.util.Map<String, Object> track2 = allTracks.get(1);
        assertEquals(1, track2.get("index"));
        assertEquals("Track 2", track2.get("name"));
        assertEquals("instrument", track2.get("type"));
        assertEquals(false, track2.get("is_group"));
        assertEquals(null, track2.get("parent_group_index"));
        assertEquals(false, track2.get("activated")); // Deactivated
        assertEquals("rgb(128,128,128)", track2.get("color"));
        assertEquals(true, track2.get("is_selected")); // Selected

        // Verify Track 3 (Group, activated)
        java.util.Map<String, Object> track3 = allTracks.get(2);
        assertEquals(2, track3.get("index"));
        assertEquals("Track 3", track3.get("name"));
        assertEquals("group", track3.get("type"));
        assertEquals(true, track3.get("is_group"));
        assertEquals(null, track3.get("parent_group_index"));
        assertEquals(true, track3.get("activated")); // Activated
        assertEquals("rgb(128,128,128)", track3.get("color"));
        assertEquals(false, track3.get("is_selected"));

        // Assert - filtered tracks (only audio)
        assertEquals(1, audioTracks.size());
        java.util.Map<String, Object> filteredTrack = audioTracks.get(0);
        assertEquals("Track 1", filteredTrack.get("name"));
        assertEquals("audio", filteredTrack.get("type"));

        // Verify logging
        verify(mockLogger, times(2)).info(contains("Getting all tracks info"));
    }
}
