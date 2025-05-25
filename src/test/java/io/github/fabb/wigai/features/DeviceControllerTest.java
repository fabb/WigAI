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
}
