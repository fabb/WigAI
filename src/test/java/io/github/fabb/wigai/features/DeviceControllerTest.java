package io.github.fabb.wigai.features;

import io.github.fabb.wigai.bitwig.BitwigApiFacade;
import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.common.data.ParameterInfo;
import io.github.fabb.wigai.common.data.ParameterSetting;
import io.github.fabb.wigai.common.data.ParameterSettingResult;
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

    // =========================== BATCH PARAMETER SETTING TESTS ===========================

    @Test
    void testSetMultipleSelectedDeviceParameters_AllSuccess() {
        // Arrange
        List<ParameterSetting> parameters = Arrays.asList(
            new ParameterSetting(0, 0.25),
            new ParameterSetting(1, 0.75),
            new ParameterSetting(2, 0.5)
        );

        when(mockBitwigApiFacade.getSelectedDeviceName()).thenReturn("Test Device");

        // Act
        List<ParameterSettingResult> results = deviceController.setMultipleSelectedDeviceParameters(parameters);

        // Assert
        assertEquals(3, results.size());

        for (int i = 0; i < results.size(); i++) {
            ParameterSettingResult result = results.get(i);
            ParameterSetting param = parameters.get(i);

            assertEquals(param.parameter_index(), result.parameter_index());
            assertEquals("success", result.status());
            assertEquals(param.value(), result.new_value());
            assertNull(result.error_code());
            assertNull(result.message());
        }

        // Verify all parameters were set
        verify(mockBitwigApiFacade).setSelectedDeviceParameter(0, 0.25);
        verify(mockBitwigApiFacade).setSelectedDeviceParameter(1, 0.75);
        verify(mockBitwigApiFacade).setSelectedDeviceParameter(2, 0.5);

        // Verify logging
        verify(mockLogger).info("DeviceController: Setting 3 parameters");
        verify(mockLogger).info("DeviceController: Batch operation completed - 3 succeeded, 0 failed");
    }

    @Test
    void testSetMultipleSelectedDeviceParameters_NoDeviceSelected() {
        // Arrange
        List<ParameterSetting> parameters = Arrays.asList(
            new ParameterSetting(0, 0.25)
        );

        when(mockBitwigApiFacade.getSelectedDeviceName()).thenThrow(new RuntimeException("No device selected"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deviceController.setMultipleSelectedDeviceParameters(parameters);
        });

        assertEquals("No device is currently selected", exception.getMessage());

        // Verify no parameter setting was attempted
        verify(mockBitwigApiFacade, never()).setSelectedDeviceParameter(anyInt(), anyDouble());

        // Verify logging
        verify(mockLogger).info("DeviceController: Setting 1 parameters");
        verify(mockLogger).error("DeviceController: No device selected for batch parameter setting");
    }

    @Test
    void testSetMultipleSelectedDeviceParameters_PartialSuccess() {
        // Arrange
        List<ParameterSetting> parameters = Arrays.asList(
            new ParameterSetting(0, 0.25),    // Success
            new ParameterSetting(8, 0.75),    // Invalid index
            new ParameterSetting(2, 1.5),     // Invalid value
            new ParameterSetting(3, 0.5)      // Success
        );

        when(mockBitwigApiFacade.getSelectedDeviceName()).thenReturn("Test Device");

        // Mock successful parameter setting for valid parameters
        doNothing().when(mockBitwigApiFacade).setSelectedDeviceParameter(0, 0.25);
        doNothing().when(mockBitwigApiFacade).setSelectedDeviceParameter(3, 0.5);

        // Mock validation errors for invalid parameters
        doThrow(new IllegalArgumentException("Parameter index must be between 0-7"))
            .when(mockBitwigApiFacade).setSelectedDeviceParameter(8, 0.75);
        doThrow(new IllegalArgumentException("Parameter value must be between 0.0-1.0"))
            .when(mockBitwigApiFacade).setSelectedDeviceParameter(2, 1.5);

        // Act
        List<ParameterSettingResult> results = deviceController.setMultipleSelectedDeviceParameters(parameters);

        // Assert
        assertEquals(4, results.size());

        // Check first parameter (success)
        ParameterSettingResult result0 = results.get(0);
        assertEquals(0, result0.parameter_index());
        assertEquals("success", result0.status());
        assertEquals(0.25, result0.new_value());
        assertNull(result0.error_code());
        assertNull(result0.message());

        // Check second parameter (invalid index)
        ParameterSettingResult result1 = results.get(1);
        assertEquals(8, result1.parameter_index());
        assertEquals("error", result1.status());
        assertNull(result1.new_value());
        assertEquals("INVALID_PARAMETER_INDEX", result1.error_code());
        assertEquals("Parameter index must be between 0-7", result1.message());

        // Check third parameter (invalid value)
        ParameterSettingResult result2 = results.get(2);
        assertEquals(2, result2.parameter_index());
        assertEquals("error", result2.status());
        assertNull(result2.new_value());
        assertEquals("INVALID_PARAMETER", result2.error_code());
        assertEquals("Parameter value must be between 0.0-1.0", result2.message());

        // Check fourth parameter (success)
        ParameterSettingResult result3 = results.get(3);
        assertEquals(3, result3.parameter_index());
        assertEquals("success", result3.status());
        assertEquals(0.5, result3.new_value());
        assertNull(result3.error_code());
        assertNull(result3.message());

        // Verify logging
        verify(mockLogger).info("DeviceController: Setting 4 parameters");
        verify(mockLogger).info("DeviceController: Batch operation completed - 2 succeeded, 2 failed");
    }

    @Test
    void testSetMultipleSelectedDeviceParameters_AllValidationErrors() {
        // Arrange
        List<ParameterSetting> parameters = Arrays.asList(
            new ParameterSetting(8, 0.25),    // Invalid index
            new ParameterSetting(1, 1.5)      // Invalid value
        );

        when(mockBitwigApiFacade.getSelectedDeviceName()).thenReturn("Test Device");

        // Mock validation errors
        doThrow(new IllegalArgumentException("Parameter index must be between 0-7"))
            .when(mockBitwigApiFacade).setSelectedDeviceParameter(8, 0.25);
        doThrow(new IllegalArgumentException("Parameter value must be between 0.0-1.0"))
            .when(mockBitwigApiFacade).setSelectedDeviceParameter(1, 1.5);

        // Act
        List<ParameterSettingResult> results = deviceController.setMultipleSelectedDeviceParameters(parameters);

        // Assert
        assertEquals(2, results.size());

        for (ParameterSettingResult result : results) {
            assertEquals("error", result.status());
            assertNull(result.new_value());
            assertNotNull(result.error_code());
            assertNotNull(result.message());
        }

        // Verify logging
        verify(mockLogger).info("DeviceController: Setting 2 parameters");
        verify(mockLogger).info("DeviceController: Batch operation completed - 0 succeeded, 2 failed");
    }

    @Test
    void testSetMultipleSelectedDeviceParameters_BitwigErrors() {
        // Arrange
        List<ParameterSetting> parameters = Arrays.asList(
            new ParameterSetting(0, 0.25),    // Success
            new ParameterSetting(1, 0.75)     // Bitwig API error
        );

        when(mockBitwigApiFacade.getSelectedDeviceName()).thenReturn("Test Device");

        // Mock one success and one Bitwig error
        doNothing().when(mockBitwigApiFacade).setSelectedDeviceParameter(0, 0.25);
        doThrow(new RuntimeException("Bitwig internal error"))
            .when(mockBitwigApiFacade).setSelectedDeviceParameter(1, 0.75);

        // Act
        List<ParameterSettingResult> results = deviceController.setMultipleSelectedDeviceParameters(parameters);

        // Assert
        assertEquals(2, results.size());

        // Check first parameter (success)
        ParameterSettingResult result0 = results.get(0);
        assertEquals(0, result0.parameter_index());
        assertEquals("success", result0.status());
        assertEquals(0.25, result0.new_value());

        // Check second parameter (Bitwig error)
        ParameterSettingResult result1 = results.get(1);
        assertEquals(1, result1.parameter_index());
        assertEquals("error", result1.status());
        assertNull(result1.new_value());
        assertEquals("BITWIG_ERROR", result1.error_code());
        assertEquals("Error setting parameter: Bitwig internal error", result1.message());

        // Verify logging
        verify(mockLogger).info("DeviceController: Setting 2 parameters");
        verify(mockLogger).info("DeviceController: Batch operation completed - 1 succeeded, 1 failed");
    }

    @Test
    void testSetMultipleSelectedDeviceParameters_EmptyList() {
        // Arrange
        List<ParameterSetting> parameters = Collections.emptyList();

        when(mockBitwigApiFacade.getSelectedDeviceName()).thenReturn("Test Device");

        // Act
        List<ParameterSettingResult> results = deviceController.setMultipleSelectedDeviceParameters(parameters);

        // Assert
        assertTrue(results.isEmpty());

        // Verify no parameter setting was attempted
        verify(mockBitwigApiFacade, never()).setSelectedDeviceParameter(anyInt(), anyDouble());

        // Verify logging
        verify(mockLogger).info("DeviceController: Setting 0 parameters");
        verify(mockLogger).info("DeviceController: Batch operation completed - 0 succeeded, 0 failed");
    }

    @Test
    void testSetMultipleSelectedDeviceParameters_SingleParameter() {
        // Arrange
        List<ParameterSetting> parameters = Arrays.asList(
            new ParameterSetting(4, 0.8)
        );

        when(mockBitwigApiFacade.getSelectedDeviceName()).thenReturn("Test Device");

        // Act
        List<ParameterSettingResult> results = deviceController.setMultipleSelectedDeviceParameters(parameters);

        // Assert
        assertEquals(1, results.size());

        ParameterSettingResult result = results.get(0);
        assertEquals(4, result.parameter_index());
        assertEquals("success", result.status());
        assertEquals(0.8, result.new_value());

        // Verify parameter was set
        verify(mockBitwigApiFacade).setSelectedDeviceParameter(4, 0.8);

        // Verify logging
        verify(mockLogger).info("DeviceController: Setting 1 parameters");
        verify(mockLogger).info("DeviceController: Batch operation completed - 1 succeeded, 0 failed");
    }

    @Test
    void testSetMultipleSelectedDeviceParameters_BoundaryValues() {
        // Arrange - test all valid boundary parameter indices and values
        List<ParameterSetting> parameters = Arrays.asList(
            new ParameterSetting(0, 0.0),     // Min index, min value
            new ParameterSetting(7, 1.0)      // Max index, max value
        );

        when(mockBitwigApiFacade.getSelectedDeviceName()).thenReturn("Test Device");

        // Act
        List<ParameterSettingResult> results = deviceController.setMultipleSelectedDeviceParameters(parameters);

        // Assert
        assertEquals(2, results.size());

        for (ParameterSettingResult result : results) {
            assertEquals("success", result.status());
            assertNull(result.error_code());
        }

        // Verify both parameters were set
        verify(mockBitwigApiFacade).setSelectedDeviceParameter(0, 0.0);
        verify(mockBitwigApiFacade).setSelectedDeviceParameter(7, 1.0);
    }
}
