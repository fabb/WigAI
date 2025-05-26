package io.github.fabb.wigai.features;

import io.github.fabb.wigai.bitwig.BitwigApiFacade;
import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.common.data.ParameterInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the DeviceController class.
 */
public class DeviceControllerTest {

    @Mock
    private BitwigApiFacade mockBitwigApiFacade;

    @Mock
    private Logger mockLogger;

    private DeviceController deviceController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        deviceController = new DeviceController(mockBitwigApiFacade, mockLogger);
    }

    @Test
    void testGetSelectedDeviceParameters_WithDevice() {
        // Arrange
        String deviceName = "Test Device";
        List<ParameterInfo> parameters = Arrays.asList(
            new ParameterInfo(0, "Param 1", 0.5, "50%"),
            new ParameterInfo(1, "Param 2", 0.75, "75%")
        );

        when(mockBitwigApiFacade.getSelectedDeviceName()).thenReturn(deviceName);
        when(mockBitwigApiFacade.getSelectedDeviceParameters()).thenReturn(parameters);

        // Act
        DeviceController.DeviceParametersResult result = deviceController.getSelectedDeviceParameters();

        // Assert
        assertNotNull(result);
        assertEquals(deviceName, result.deviceName());
        assertEquals(parameters, result.parameters());
        assertEquals(2, result.parameters().size());

        // Verify logging
        verify(mockLogger).info("DeviceController: Getting selected device parameters");
        verify(mockLogger).info("DeviceController: Retrieved device '" + deviceName + "' with 2 parameters");
    }

    @Test
    void testGetSelectedDeviceParameters_NoDevice() {
        // Arrange
        when(mockBitwigApiFacade.getSelectedDeviceName()).thenReturn(null);
        when(mockBitwigApiFacade.getSelectedDeviceParameters()).thenReturn(Collections.emptyList());

        // Act
        DeviceController.DeviceParametersResult result = deviceController.getSelectedDeviceParameters();

        // Assert
        assertNotNull(result);
        assertNull(result.deviceName());
        assertTrue(result.parameters().isEmpty());

        // Verify logging
        verify(mockLogger).info("DeviceController: Getting selected device parameters");
        verify(mockLogger).info("DeviceController: Retrieved device 'null' with 0 parameters");
    }

    @Test
    void testGetSelectedDeviceParameters_ExceptionHandling() {
        // Arrange
        when(mockBitwigApiFacade.getSelectedDeviceName()).thenThrow(new RuntimeException("Test exception"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deviceController.getSelectedDeviceParameters();
        });

        assertEquals("Failed to get selected device parameters", exception.getMessage());
        assertEquals("Test exception", exception.getCause().getMessage());

        // Verify error logging
        verify(mockLogger).error("DeviceController: Error getting selected device parameters: Test exception");
    }

    @Test
    void testGetSelectedDeviceParameters_PartialException() {
        // Arrange - facade getName works but getParameters fails
        String deviceName = "Test Device";
        when(mockBitwigApiFacade.getSelectedDeviceName()).thenReturn(deviceName);
        when(mockBitwigApiFacade.getSelectedDeviceParameters()).thenThrow(new RuntimeException("Parameter error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deviceController.getSelectedDeviceParameters();
        });

        assertEquals("Failed to get selected device parameters", exception.getMessage());
        assertEquals("Parameter error", exception.getCause().getMessage());

        // Verify error logging
        verify(mockLogger).error("DeviceController: Error getting selected device parameters: Parameter error");
    }

    @Test
    void testSetSelectedDeviceParameter_Success() {
        // Arrange
        int parameterIndex = 3;
        double value = 0.75;

        // Act
        deviceController.setSelectedDeviceParameter(parameterIndex, value);

        // Assert
        verify(mockBitwigApiFacade).setSelectedDeviceParameter(parameterIndex, value);
        verify(mockLogger).info("DeviceController: Setting parameter " + parameterIndex + " to " + value);
        verify(mockLogger).info("DeviceController: Successfully set parameter " + parameterIndex + " to " + value);
    }

    @Test
    void testSetSelectedDeviceParameter_ValidationError() {
        // Arrange
        int parameterIndex = 8; // Invalid index
        double value = 0.5;

        doThrow(new IllegalArgumentException("Parameter index must be between 0-7, got: 8"))
            .when(mockBitwigApiFacade).setSelectedDeviceParameter(parameterIndex, value);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            deviceController.setSelectedDeviceParameter(parameterIndex, value);
        });

        assertEquals("Parameter index must be between 0-7, got: 8", exception.getMessage());

        // Verify logging
        verify(mockLogger).info("DeviceController: Setting parameter " + parameterIndex + " to " + value);
        verify(mockLogger).error("DeviceController: Validation error setting parameter " + parameterIndex + ": Parameter index must be between 0-7, got: 8");
    }

    @Test
    void testSetSelectedDeviceParameter_ValueValidationError() {
        // Arrange
        int parameterIndex = 0;
        double value = 1.5; // Invalid value

        doThrow(new IllegalArgumentException("Parameter value must be between 0.0-1.0, got: 1.5"))
            .when(mockBitwigApiFacade).setSelectedDeviceParameter(parameterIndex, value);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            deviceController.setSelectedDeviceParameter(parameterIndex, value);
        });

        assertEquals("Parameter value must be between 0.0-1.0, got: 1.5", exception.getMessage());

        // Verify logging
        verify(mockLogger).info("DeviceController: Setting parameter " + parameterIndex + " to " + value);
        verify(mockLogger).error("DeviceController: Validation error setting parameter " + parameterIndex + ": Parameter value must be between 0.0-1.0, got: 1.5");
    }

    @Test
    void testSetSelectedDeviceParameter_NoDeviceSelected() {
        // Arrange
        int parameterIndex = 0;
        double value = 0.5;

        doThrow(new RuntimeException("No device is currently selected"))
            .when(mockBitwigApiFacade).setSelectedDeviceParameter(parameterIndex, value);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deviceController.setSelectedDeviceParameter(parameterIndex, value);
        });

        assertEquals("Failed to set device parameter", exception.getMessage());
        assertEquals("No device is currently selected", exception.getCause().getMessage());

        // Verify logging
        verify(mockLogger).info("DeviceController: Setting parameter " + parameterIndex + " to " + value);
        verify(mockLogger).error("DeviceController: Error setting parameter " + parameterIndex + ": No device is currently selected");
    }

    @Test
    void testSetSelectedDeviceParameter_BitwigApiError() {
        // Arrange
        int parameterIndex = 0;
        double value = 0.5;

        doThrow(new RuntimeException("Bitwig API internal error"))
            .when(mockBitwigApiFacade).setSelectedDeviceParameter(parameterIndex, value);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deviceController.setSelectedDeviceParameter(parameterIndex, value);
        });

        assertEquals("Failed to set device parameter", exception.getMessage());
        assertEquals("Bitwig API internal error", exception.getCause().getMessage());

        // Verify logging
        verify(mockLogger).info("DeviceController: Setting parameter " + parameterIndex + " to " + value);
        verify(mockLogger).error("DeviceController: Error setting parameter " + parameterIndex + ": Bitwig API internal error");
    }

    @Test
    void testSetSelectedDeviceParameter_BoundaryValues() {
        // Test valid boundary values

        // Test minimum values
        deviceController.setSelectedDeviceParameter(0, 0.0);
        verify(mockBitwigApiFacade).setSelectedDeviceParameter(0, 0.0);

        // Test maximum values
        deviceController.setSelectedDeviceParameter(7, 1.0);
        verify(mockBitwigApiFacade).setSelectedDeviceParameter(7, 1.0);

        // Verify logging calls (reset before to count accurately)
        reset(mockLogger);

        deviceController.setSelectedDeviceParameter(3, 0.5);
        verify(mockLogger).info("DeviceController: Setting parameter 3 to 0.5");
        verify(mockLogger).info("DeviceController: Successfully set parameter 3 to 0.5");
    }
}
