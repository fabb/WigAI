package io.github.fabb.wigai.mcp.tool;

import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.common.data.ParameterInfo;
import io.github.fabb.wigai.common.data.ParameterSetting;
import io.github.fabb.wigai.common.data.ParameterSettingResult;
import io.github.fabb.wigai.features.DeviceController;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the DeviceParamTool class.
 */
public class DeviceParamToolTest {

    @Mock
    private DeviceController mockDeviceController;

    @Mock
    private Logger mockLogger;

    @Mock
    private McpSyncServerExchange mockExchange;

    private McpServerFeatures.SyncToolSpecification toolSpec;
    private BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        toolSpec = DeviceParamTool.getSelectedDeviceParametersSpecification(mockDeviceController, mockLogger);
        handler = DeviceParamTool.getGetParametersHandler();
    }

    @Test
    void testToolSpecification() {
        // Verify tool specification
        assertNotNull(toolSpec);
        assertNotNull(toolSpec.tool());
        assertEquals("get_selected_device_parameters", toolSpec.tool().name());
        assertEquals("Get the names and values of the eight addressable parameters of the user-selected device in Bitwig.",
                     toolSpec.tool().description());
        assertNotNull(toolSpec.tool().inputSchema());
    }

    @Test
    void testGetSelectedDeviceParameters_WithDevice() {
        // Arrange
        String deviceName = "Test Synth";
        List<ParameterInfo> parameters = Arrays.asList(
            new ParameterInfo(0, "OSC1 Shape", 0.75, "75.0 %"),
            new ParameterInfo(1, "Filter Cutoff", 0.5, "50.0 %"),
            new ParameterInfo(2, null, 0.0, "0.0 %") // Unnamed parameter
        );

        DeviceController.DeviceParametersResult result =
            new DeviceController.DeviceParametersResult(deviceName, parameters);

        when(mockDeviceController.getSelectedDeviceParameters()).thenReturn(result);

        // Act
        McpSchema.CallToolResult toolResult = handler.apply(mockExchange, Collections.emptyMap());

        // Assert
        assertNotNull(toolResult);
        assertFalse(toolResult.isError());
        assertEquals(1, toolResult.content().size());

        // Verify JSON response matches API specification
        McpSchema.TextContent textContent = (McpSchema.TextContent) toolResult.content().get(0);
        String responseText = textContent.text();

        // Check JSON structure
        assertTrue(responseText.contains("\"status\":\"success\""));
        assertTrue(responseText.contains("\"data\":{"));
        assertTrue(responseText.contains("\"device_name\":\"Test Synth\""));
        assertTrue(responseText.contains("\"parameters\":["));
        assertTrue(responseText.contains("\"name\":\"OSC1 Shape\""));
        assertTrue(responseText.contains("\"name\":\"Filter Cutoff\""));
        assertTrue(responseText.contains("\"index\":0"));
        assertTrue(responseText.contains("\"index\":1"));
        assertTrue(responseText.contains("\"index\":2"));
        assertTrue(responseText.contains("\"value\":0.75"));
        assertTrue(responseText.contains("\"value\":0.5"));
        assertTrue(responseText.contains("\"display_value\":\"75.0 %\""));

        // Verify logging
        verify(mockLogger).info("Received 'get_selected_device_parameters' tool call");
        verify(mockLogger).info("Responding with device parameters for: Test Synth");
    }

    @Test
    void testGetSelectedDeviceParameters_NoDevice() {
        // Arrange
        DeviceController.DeviceParametersResult result =
            new DeviceController.DeviceParametersResult(null, Collections.emptyList());

        when(mockDeviceController.getSelectedDeviceParameters()).thenReturn(result);

        // Act
        McpSchema.CallToolResult toolResult = handler.apply(mockExchange, Collections.emptyMap());

        // Assert
        assertNotNull(toolResult);
        assertFalse(toolResult.isError());
        assertEquals(1, toolResult.content().size());

        // Verify JSON response shows no device
        McpSchema.TextContent textContent = (McpSchema.TextContent) toolResult.content().get(0);
        String responseText = textContent.text();

        // Check JSON structure
        assertTrue(responseText.contains("\"status\":\"success\""));
        assertTrue(responseText.contains("\"data\":{"));
        assertTrue(responseText.contains("\"device_name\":null"));
        assertTrue(responseText.contains("\"parameters\":[]"));

        // Verify logging
        verify(mockLogger).info("Received 'get_selected_device_parameters' tool call");
        verify(mockLogger).info("Responding with device parameters for: null");
    }

    @Test
    void testGetSelectedDeviceParameters_Exception() {
        // Arrange
        when(mockDeviceController.getSelectedDeviceParameters())
            .thenThrow(new RuntimeException("Device access error"));

        // Act
        McpSchema.CallToolResult toolResult = handler.apply(mockExchange, Collections.emptyMap());

        // Assert
        assertNotNull(toolResult);
        assertTrue(toolResult.isError());
        assertEquals(1, toolResult.content().size());

        // Verify JSON error response
        McpSchema.TextContent errorContent = (McpSchema.TextContent) toolResult.content().get(0);
        String errorText = errorContent.text();
        assertTrue(errorText.contains("\"status\":\"error\""));
        assertTrue(errorText.contains("\"message\":\"Error getting selected device parameters: Device access error\""));

        // Verify error logging
        verify(mockLogger).info("Received 'get_selected_device_parameters' tool call");
        verify(mockLogger).error("DeviceParamTool: Error getting selected device parameters: Device access error");
    }

    @Test
    void testGetSelectedDeviceParameters_EmptyParametersList() {
        // Arrange - device exists but has no parameters
        String deviceName = "Empty Device";
        DeviceController.DeviceParametersResult result =
            new DeviceController.DeviceParametersResult(deviceName, Collections.emptyList());

        when(mockDeviceController.getSelectedDeviceParameters()).thenReturn(result);

        // Act
        McpSchema.CallToolResult toolResult = handler.apply(mockExchange, Collections.emptyMap());

        // Assert
        assertNotNull(toolResult);
        assertFalse(toolResult.isError());
        assertEquals(1, toolResult.content().size());

        // Verify JSON response shows device with no parameters
        McpSchema.TextContent textContent = (McpSchema.TextContent) toolResult.content().get(0);
        String responseText = textContent.text();

        // Check JSON structure
        assertTrue(responseText.contains("\"status\":\"success\""));
        assertTrue(responseText.contains("\"data\":{"));
        assertTrue(responseText.contains("\"device_name\":\"Empty Device\""));
        assertTrue(responseText.contains("\"parameters\":[]"));
    }

    @Test
    void testSetSelectedDeviceParameter_ToolSpecification() {
        // Arrange & Act
        McpServerFeatures.SyncToolSpecification setToolSpec =
            DeviceParamTool.setSelectedDeviceParameterSpecification(mockDeviceController, mockLogger);

        // Assert
        assertNotNull(setToolSpec);
        assertNotNull(setToolSpec.tool());
        assertEquals("set_selected_device_parameter", setToolSpec.tool().name());
        assertEquals("Set a specific value for a single parameter (by its index 0-7) of the user-selected device in Bitwig.",
                     setToolSpec.tool().description());
        assertNotNull(setToolSpec.tool().inputSchema());

        // Verify schema contains required fields
        String schema = setToolSpec.tool().inputSchema().toString();
        assertTrue(schema.contains("parameter_index"));
        assertTrue(schema.contains("value"));
        assertTrue(schema.contains("required"));
    }

    @Test
    void testSetSelectedDeviceParameter_Success() {
        // Arrange
        Map<String, Object> arguments = Map.of(
            "parameter_index", 3,
            "value", 0.75
        );

        BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> setHandler =
            DeviceParamTool.getSetParameterHandler();

        // Mock the setSelectedDeviceParameterSpecification to initialize the handler
        DeviceParamTool.setSelectedDeviceParameterSpecification(mockDeviceController, mockLogger);
        setHandler = DeviceParamTool.getSetParameterHandler();

        // Act
        McpSchema.CallToolResult toolResult = setHandler.apply(mockExchange, arguments);

        // Assert
        assertNotNull(toolResult);
        assertFalse(toolResult.isError());
        assertEquals(1, toolResult.content().size());

        // Verify device controller was called
        verify(mockDeviceController).setSelectedDeviceParameter(3, 0.75);

        // Verify JSON response
        McpSchema.TextContent textContent = (McpSchema.TextContent) toolResult.content().get(0);
        String responseText = textContent.text();

        assertTrue(responseText.contains("\"status\":\"success\""));
        assertTrue(responseText.contains("\"action\":\"parameter_set\""));
        assertTrue(responseText.contains("\"parameter_index\":3"));
        assertTrue(responseText.contains("\"new_value\":0.75"));
        assertTrue(responseText.contains("\"message\":\"Parameter 3 set to 0.75.\""));

        // Verify logging
        verify(mockLogger).info(contains("Received 'set_selected_device_parameter' tool call with arguments:"));
        verify(mockLogger).info("Successfully set parameter 3 to 0.75");
    }

    @Test
    void testSetSelectedDeviceParameter_MissingParameterIndex() {
        // Arrange
        Map<String, Object> arguments = Map.of("value", 0.5);

        // Initialize handler
        DeviceParamTool.setSelectedDeviceParameterSpecification(mockDeviceController, mockLogger);
        BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> setHandler =
            DeviceParamTool.getSetParameterHandler();

        // Act
        McpSchema.CallToolResult toolResult = setHandler.apply(mockExchange, arguments);

        // Assert
        assertNotNull(toolResult);
        assertTrue(toolResult.isError());
        assertEquals(1, toolResult.content().size());

        // Verify error response
        McpSchema.TextContent errorContent = (McpSchema.TextContent) toolResult.content().get(0);
        String errorText = errorContent.text();
        assertTrue(errorText.contains("\"status\":\"error\""));
        assertTrue(errorText.contains("\"error_code\":\"INVALID_PARAMETER\""));
        assertTrue(errorText.contains("Missing required parameter: parameter_index"));

        // Verify device controller was not called
        verify(mockDeviceController, never()).setSelectedDeviceParameter(anyInt(), anyDouble());
    }

    @Test
    void testSetSelectedDeviceParameter_MissingValue() {
        // Arrange
        Map<String, Object> arguments = Map.of("parameter_index", 0);

        // Initialize handler
        DeviceParamTool.setSelectedDeviceParameterSpecification(mockDeviceController, mockLogger);
        BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> setHandler =
            DeviceParamTool.getSetParameterHandler();

        // Act
        McpSchema.CallToolResult toolResult = setHandler.apply(mockExchange, arguments);

        // Assert
        assertNotNull(toolResult);
        assertTrue(toolResult.isError());
        assertEquals(1, toolResult.content().size());

        // Verify error response
        McpSchema.TextContent errorContent = (McpSchema.TextContent) toolResult.content().get(0);
        String errorText = errorContent.text();
        assertTrue(errorText.contains("\"status\":\"error\""));
        assertTrue(errorText.contains("\"error_code\":\"INVALID_PARAMETER\""));
        assertTrue(errorText.contains("Missing required parameter: value"));

        // Verify device controller was not called
        verify(mockDeviceController, never()).setSelectedDeviceParameter(anyInt(), anyDouble());
    }

    @Test
    void testSetSelectedDeviceParameter_InvalidParameterIndex() {
        // Arrange
        Map<String, Object> arguments = Map.of(
            "parameter_index", 8, // Invalid index
            "value", 0.5
        );

        // Mock controller to throw validation error
        doThrow(new IllegalArgumentException("Parameter index must be between 0-7, got: 8"))
            .when(mockDeviceController).setSelectedDeviceParameter(8, 0.5);

        // Initialize handler
        DeviceParamTool.setSelectedDeviceParameterSpecification(mockDeviceController, mockLogger);
        BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> setHandler =
            DeviceParamTool.getSetParameterHandler();

        // Act
        McpSchema.CallToolResult toolResult = setHandler.apply(mockExchange, arguments);

        // Assert
        assertNotNull(toolResult);
        assertTrue(toolResult.isError());
        assertEquals(1, toolResult.content().size());

        // Verify error response
        McpSchema.TextContent errorContent = (McpSchema.TextContent) toolResult.content().get(0);
        String errorText = errorContent.text();
        assertTrue(errorText.contains("\"status\":\"error\""));
        assertTrue(errorText.contains("\"error_code\":\"INVALID_PARAMETER_INDEX\""));
        assertTrue(errorText.contains("Parameter index must be between 0-7, got: 8"));

        // Verify device controller was called and threw the error
        verify(mockDeviceController).setSelectedDeviceParameter(8, 0.5);
    }

    @Test
    void testSetSelectedDeviceParameter_InvalidValue() {
        // Arrange
        Map<String, Object> arguments = Map.of(
            "parameter_index", 0,
            "value", 1.5 // Invalid value
        );

        // Mock controller to throw validation error
        doThrow(new IllegalArgumentException("Parameter value must be between 0.0-1.0, got: 1.5"))
            .when(mockDeviceController).setSelectedDeviceParameter(0, 1.5);

        // Initialize handler
        DeviceParamTool.setSelectedDeviceParameterSpecification(mockDeviceController, mockLogger);
        BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> setHandler =
            DeviceParamTool.getSetParameterHandler();

        // Act
        McpSchema.CallToolResult toolResult = setHandler.apply(mockExchange, arguments);

        // Assert
        assertNotNull(toolResult);
        assertTrue(toolResult.isError());
        assertEquals(1, toolResult.content().size());

        // Verify error response
        McpSchema.TextContent errorContent = (McpSchema.TextContent) toolResult.content().get(0);
        String errorText = errorContent.text();
        assertTrue(errorText.contains("\"status\":\"error\""));
        assertTrue(errorText.contains("\"error_code\":\"INVALID_PARAMETER\""));
        assertTrue(errorText.contains("Parameter value must be between 0.0-1.0, got: 1.5"));
    }

    @Test
    void testSetSelectedDeviceParameter_NoDeviceSelected() {
        // Arrange
        Map<String, Object> arguments = Map.of(
            "parameter_index", 0,
            "value", 0.5
        );

        // Mock controller to throw device not selected error
        doThrow(new RuntimeException("No device is currently selected"))
            .when(mockDeviceController).setSelectedDeviceParameter(0, 0.5);

        // Initialize handler
        DeviceParamTool.setSelectedDeviceParameterSpecification(mockDeviceController, mockLogger);
        BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> setHandler =
            DeviceParamTool.getSetParameterHandler();

        // Act
        McpSchema.CallToolResult toolResult = setHandler.apply(mockExchange, arguments);

        // Assert
        assertNotNull(toolResult);
        assertTrue(toolResult.isError());
        assertEquals(1, toolResult.content().size());

        // Verify error response
        McpSchema.TextContent errorContent = (McpSchema.TextContent) toolResult.content().get(0);
        String errorText = errorContent.text();
        assertTrue(errorText.contains("\"status\":\"error\""));
        assertTrue(errorText.contains("\"error_code\":\"DEVICE_NOT_SELECTED\""));
        assertTrue(errorText.contains("No device is currently selected"));
    }

    @Test
    void testSetSelectedDeviceParameter_BitwigError() {
        // Arrange
        Map<String, Object> arguments = Map.of(
            "parameter_index", 0,
            "value", 0.5
        );

        // Mock controller to throw Bitwig API error
        doThrow(new RuntimeException("Bitwig API internal error"))
            .when(mockDeviceController).setSelectedDeviceParameter(0, 0.5);

        // Initialize handler
        DeviceParamTool.setSelectedDeviceParameterSpecification(mockDeviceController, mockLogger);
        BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> setHandler =
            DeviceParamTool.getSetParameterHandler();

        // Act
        McpSchema.CallToolResult toolResult = setHandler.apply(mockExchange, arguments);

        // Assert
        assertNotNull(toolResult);
        assertTrue(toolResult.isError());
        assertEquals(1, toolResult.content().size());

        // Verify error response
        McpSchema.TextContent errorContent = (McpSchema.TextContent) toolResult.content().get(0);
        String errorText = errorContent.text();
        assertTrue(errorText.contains("\"status\":\"error\""));
        assertTrue(errorText.contains("\"error_code\":\"BITWIG_ERROR\""));
        assertTrue(errorText.contains("Bitwig API internal error"));
    }

    @Test
    void testSetSelectedDeviceParameter_BoundaryValues() {
        // Initialize handler
        DeviceParamTool.setSelectedDeviceParameterSpecification(mockDeviceController, mockLogger);
        BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> setHandler =
            DeviceParamTool.getSetParameterHandler();

        // Test minimum boundary values
        Map<String, Object> minArgs = Map.of("parameter_index", 0, "value", 0.0);
        McpSchema.CallToolResult minResult = setHandler.apply(mockExchange, minArgs);
        assertFalse(minResult.isError());
        verify(mockDeviceController).setSelectedDeviceParameter(0, 0.0);

        // Test maximum boundary values
        Map<String, Object> maxArgs = Map.of("parameter_index", 7, "value", 1.0);
        McpSchema.CallToolResult maxResult = setHandler.apply(mockExchange, maxArgs);
        assertFalse(maxResult.isError());
        verify(mockDeviceController).setSelectedDeviceParameter(7, 1.0);
    }

    @Test
    void testSetSelectedDeviceParameter_InvalidArgumentTypes() {
        // Initialize handler
        DeviceParamTool.setSelectedDeviceParameterSpecification(mockDeviceController, mockLogger);
        BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> setHandler =
            DeviceParamTool.getSetParameterHandler();

        // Test non-numeric parameter_index
        Map<String, Object> invalidIndexArgs = Map.of(
            "parameter_index", "not_a_number",
            "value", 0.5
        );
        McpSchema.CallToolResult result1 = setHandler.apply(mockExchange, invalidIndexArgs);
        assertTrue(result1.isError());

        // Test non-numeric value
        Map<String, Object> invalidValueArgs = Map.of(
            "parameter_index", 0,
            "value", "not_a_number"
        );
        McpSchema.CallToolResult result2 = setHandler.apply(mockExchange, invalidValueArgs);
        assertTrue(result2.isError());
    }

    // =========================== BATCH PARAMETER SETTING TESTS ===========================

    @Test
    void testSetMultipleDeviceParameters_ToolSpecification() {
        // Arrange & Act
        McpServerFeatures.SyncToolSpecification multiToolSpec =
            DeviceParamTool.setMultipleDeviceParametersSpecification(mockDeviceController, mockLogger);

        // Assert
        assertNotNull(multiToolSpec);
        assertNotNull(multiToolSpec.tool());
        assertEquals("set_selected_device_parameters", multiToolSpec.tool().name());
        assertEquals("Set multiple parameter values (by index 0-7) of the user-selected device in Bitwig simultaneously.",
                     multiToolSpec.tool().description());
        assertNotNull(multiToolSpec.tool().inputSchema());

        // Verify schema contains required fields
        String schema = multiToolSpec.tool().inputSchema().toString();
        assertTrue(schema.contains("parameters"));
        assertTrue(schema.contains("parameter_index"));
        assertTrue(schema.contains("value"));
        assertTrue(schema.contains("required"));
        assertTrue(schema.contains("minItems"));
    }

    @Test
    void testSetMultipleDeviceParameters_AllSuccess() {
        // Arrange
        List<Object> parametersArray = Arrays.asList(
            Map.of("parameter_index", 0, "value", 0.25),
            Map.of("parameter_index", 1, "value", 0.75),
            Map.of("parameter_index", 2, "value", 0.5)
        );
        Map<String, Object> arguments = Map.of("parameters", parametersArray);

        List<ParameterSettingResult> mockResults = Arrays.asList(
            new ParameterSettingResult(0, "success", 0.25, null, null),
            new ParameterSettingResult(1, "success", 0.75, null, null),
            new ParameterSettingResult(2, "success", 0.5, null, null)
        );

        when(mockDeviceController.setMultipleSelectedDeviceParameters(any())).thenReturn(mockResults);

        // Initialize handler
        DeviceParamTool.setMultipleDeviceParametersSpecification(mockDeviceController, mockLogger);
        BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> multiHandler =
            DeviceParamTool.getSetMultipleParametersHandler();

        // Act
        McpSchema.CallToolResult toolResult = multiHandler.apply(mockExchange, arguments);

        // Assert
        assertNotNull(toolResult);
        assertFalse(toolResult.isError());
        assertEquals(1, toolResult.content().size());

        // Verify JSON response
        McpSchema.TextContent textContent = (McpSchema.TextContent) toolResult.content().get(0);
        String responseText = textContent.text();

        assertTrue(responseText.contains("\"status\":\"success\""));
        assertTrue(responseText.contains("\"action\":\"multiple_parameters_set\""));
        assertTrue(responseText.contains("\"results\":["));
        assertTrue(responseText.contains("\"parameter_index\":0"));
        assertTrue(responseText.contains("\"parameter_index\":1"));
        assertTrue(responseText.contains("\"parameter_index\":2"));
        assertTrue(responseText.contains("\"new_value\":0.25"));
        assertTrue(responseText.contains("\"new_value\":0.75"));
        assertTrue(responseText.contains("\"new_value\":0.5"));

        // Verify device controller was called with correct parameters
        verify(mockDeviceController).setMultipleSelectedDeviceParameters(any());

        // Verify logging
        verify(mockLogger).info(contains("Received 'set_selected_device_parameters' tool call with arguments:"));
        verify(mockLogger).info("Parsed 3 parameter settings for batch operation");
        verify(mockLogger).info("Batch operation completed: 3 succeeded, 0 failed");
    }

    @Test
    void testSetMultipleDeviceParameters_PartialSuccess() {
        // Arrange
        List<Object> parametersArray = Arrays.asList(
            Map.of("parameter_index", 0, "value", 0.25),    // Success
            Map.of("parameter_index", 8, "value", 0.75),    // Invalid index
            Map.of("parameter_index", 2, "value", 0.5)      // Success
        );
        Map<String, Object> arguments = Map.of("parameters", parametersArray);

        List<ParameterSettingResult> mockResults = Arrays.asList(
            new ParameterSettingResult(0, "success", 0.25, null, null),
            new ParameterSettingResult(8, "error", null, "INVALID_PARAMETER_INDEX", "Parameter index must be between 0-7"),
            new ParameterSettingResult(2, "success", 0.5, null, null)
        );

        when(mockDeviceController.setMultipleSelectedDeviceParameters(any())).thenReturn(mockResults);

        // Initialize handler
        DeviceParamTool.setMultipleDeviceParametersSpecification(mockDeviceController, mockLogger);
        BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> multiHandler =
            DeviceParamTool.getSetMultipleParametersHandler();

        // Act
        McpSchema.CallToolResult toolResult = multiHandler.apply(mockExchange, arguments);

        // Assert
        assertNotNull(toolResult);
        assertFalse(toolResult.isError());
        assertEquals(1, toolResult.content().size());

        // Verify JSON response contains both success and error results
        McpSchema.TextContent textContent = (McpSchema.TextContent) toolResult.content().get(0);
        String responseText = textContent.text();

        assertTrue(responseText.contains("\"status\":\"success\"")); // Overall success
        assertTrue(responseText.contains("\"action\":\"multiple_parameters_set\""));
        assertTrue(responseText.contains("\"results\":["));

        // Check success entries
        assertTrue(responseText.contains("\"parameter_index\":0"));
        assertTrue(responseText.contains("\"new_value\":0.25"));
        assertTrue(responseText.contains("\"parameter_index\":2"));
        assertTrue(responseText.contains("\"new_value\":0.5"));

        // Check error entry
        assertTrue(responseText.contains("\"parameter_index\":8"));
        assertTrue(responseText.contains("\"error_code\":\"INVALID_PARAMETER_INDEX\""));
        assertTrue(responseText.contains("Parameter index must be between 0-7"));

        // Verify logging
        verify(mockLogger).info("Batch operation completed: 2 succeeded, 1 failed");
    }

    @Test
    void testSetMultipleDeviceParameters_NoDeviceSelected() {
        // Arrange
        List<Object> parametersArray = Arrays.asList(
            Map.of("parameter_index", 0, "value", 0.25)
        );
        Map<String, Object> arguments = Map.of("parameters", parametersArray);

        when(mockDeviceController.setMultipleSelectedDeviceParameters(any()))
            .thenThrow(new RuntimeException("No device is currently selected"));

        // Initialize handler
        DeviceParamTool.setMultipleDeviceParametersSpecification(mockDeviceController, mockLogger);
        BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> multiHandler =
            DeviceParamTool.getSetMultipleParametersHandler();

        // Act
        McpSchema.CallToolResult toolResult = multiHandler.apply(mockExchange, arguments);

        // Assert
        assertNotNull(toolResult);
        assertTrue(toolResult.isError());
        assertEquals(1, toolResult.content().size());

        // Verify error response
        McpSchema.TextContent errorContent = (McpSchema.TextContent) toolResult.content().get(0);
        String errorText = errorContent.text();
        assertTrue(errorText.contains("\"status\":\"error\""));
        assertTrue(errorText.contains("\"error_code\":\"DEVICE_NOT_SELECTED\""));
        assertTrue(errorText.contains("No device is currently selected"));
    }

    @Test
    void testSetMultipleDeviceParameters_MissingParametersArray() {
        // Arrange
        Map<String, Object> arguments = Map.of("other_field", "value");

        // Initialize handler
        DeviceParamTool.setMultipleDeviceParametersSpecification(mockDeviceController, mockLogger);
        BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> multiHandler =
            DeviceParamTool.getSetMultipleParametersHandler();

        // Act
        McpSchema.CallToolResult toolResult = multiHandler.apply(mockExchange, arguments);

        // Assert
        assertNotNull(toolResult);
        assertTrue(toolResult.isError());
        assertEquals(1, toolResult.content().size());

        // Verify error response
        McpSchema.TextContent errorContent = (McpSchema.TextContent) toolResult.content().get(0);
        String errorText = errorContent.text();
        assertTrue(errorText.contains("\"status\":\"error\""));
        assertTrue(errorText.contains("\"error_code\":\"INVALID_PARAMETER\""));
        assertTrue(errorText.contains("Missing required parameter: parameters"));

        // Verify device controller was not called
        verify(mockDeviceController, never()).setMultipleSelectedDeviceParameters(any());
    }

    @Test
    void testSetMultipleDeviceParameters_EmptyParametersArray() {
        // Arrange
        Map<String, Object> arguments = Map.of("parameters", Collections.emptyList());

        // Initialize handler
        DeviceParamTool.setMultipleDeviceParametersSpecification(mockDeviceController, mockLogger);
        BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> multiHandler =
            DeviceParamTool.getSetMultipleParametersHandler();

        // Act
        McpSchema.CallToolResult toolResult = multiHandler.apply(mockExchange, arguments);

        // Assert
        assertNotNull(toolResult);
        assertTrue(toolResult.isError());
        assertEquals(1, toolResult.content().size());

        // Verify error response
        McpSchema.TextContent errorContent = (McpSchema.TextContent) toolResult.content().get(0);
        String errorText = errorContent.text();
        assertTrue(errorText.contains("\"status\":\"error\""));
        assertTrue(errorText.contains("\"error_code\":\"INVALID_PARAMETER\""));
        assertTrue(errorText.contains("'parameters' array cannot be empty"));

        // Verify device controller was not called
        verify(mockDeviceController, never()).setMultipleSelectedDeviceParameters(any());
    }

    @Test
    void testSetMultipleDeviceParameters_InvalidParametersType() {
        // Arrange - parameters is not an array
        Map<String, Object> arguments = Map.of("parameters", "not_an_array");

        // Initialize handler
        DeviceParamTool.setMultipleDeviceParametersSpecification(mockDeviceController, mockLogger);
        BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> multiHandler =
            DeviceParamTool.getSetMultipleParametersHandler();

        // Act
        McpSchema.CallToolResult toolResult = multiHandler.apply(mockExchange, arguments);

        // Assert
        assertNotNull(toolResult);
        assertTrue(toolResult.isError());
        assertEquals(1, toolResult.content().size());

        // Verify error response
        McpSchema.TextContent errorContent = (McpSchema.TextContent) toolResult.content().get(0);
        String errorText = errorContent.text();
        assertTrue(errorText.contains("\"status\":\"error\""));
        assertTrue(errorText.contains("\"error_code\":\"INVALID_PARAMETER\""));
        assertTrue(errorText.contains("'parameters' must be an array"));
    }

    @Test
    void testSetMultipleDeviceParameters_InvalidParameterEntry() {
        // Arrange - parameter entry is not an object
        List<Object> parametersArray = Arrays.asList("not_an_object");
        Map<String, Object> arguments = Map.of("parameters", parametersArray);

        // Initialize handler
        DeviceParamTool.setMultipleDeviceParametersSpecification(mockDeviceController, mockLogger);
        BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> multiHandler =
            DeviceParamTool.getSetMultipleParametersHandler();

        // Act
        McpSchema.CallToolResult toolResult = multiHandler.apply(mockExchange, arguments);

        // Assert
        assertNotNull(toolResult);
        assertTrue(toolResult.isError());
        assertEquals(1, toolResult.content().size());

        // Verify error response
        McpSchema.TextContent errorContent = (McpSchema.TextContent) toolResult.content().get(0);
        String errorText = errorContent.text();
        assertTrue(errorText.contains("\"status\":\"error\""));
        assertTrue(errorText.contains("\"error_code\":\"INVALID_PARAMETER\""));
        assertTrue(errorText.contains("Each parameter entry must be an object"));
    }

    @Test
    void testSetMultipleDeviceParameters_MissingParameterFields() {
        // Test missing parameter_index
        List<Object> parametersArray1 = Arrays.asList(
            Map.of("value", 0.5) // Missing parameter_index
        );
        Map<String, Object> arguments1 = Map.of("parameters", parametersArray1);

        // Initialize handler
        DeviceParamTool.setMultipleDeviceParametersSpecification(mockDeviceController, mockLogger);
        BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> multiHandler =
            DeviceParamTool.getSetMultipleParametersHandler();

        // Act & Assert for missing parameter_index
        McpSchema.CallToolResult toolResult1 = multiHandler.apply(mockExchange, arguments1);
        assertTrue(toolResult1.isError());
        String errorText1 = ((McpSchema.TextContent) toolResult1.content().get(0)).text();
        assertTrue(errorText1.contains("Missing required field: parameter_index"));

        // Test missing value
        List<Object> parametersArray2 = Arrays.asList(
            Map.of("parameter_index", 0) // Missing value
        );
        Map<String, Object> arguments2 = Map.of("parameters", parametersArray2);

        // Act & Assert for missing value
        McpSchema.CallToolResult toolResult2 = multiHandler.apply(mockExchange, arguments2);
        assertTrue(toolResult2.isError());
        String errorText2 = ((McpSchema.TextContent) toolResult2.content().get(0)).text();
        assertTrue(errorText2.contains("Missing required field: value"));
    }

    @Test
    void testSetMultipleDeviceParameters_InvalidParameterFieldTypes() {
        // Test non-numeric parameter_index
        List<Object> parametersArray1 = Arrays.asList(
            Map.of("parameter_index", "not_a_number", "value", 0.5)
        );
        Map<String, Object> arguments1 = Map.of("parameters", parametersArray1);

        // Initialize handler
        DeviceParamTool.setMultipleDeviceParametersSpecification(mockDeviceController, mockLogger);
        BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> multiHandler =
            DeviceParamTool.getSetMultipleParametersHandler();

        // Act & Assert for invalid parameter_index type
        McpSchema.CallToolResult toolResult1 = multiHandler.apply(mockExchange, arguments1);
        assertTrue(toolResult1.isError());
        String errorText1 = ((McpSchema.TextContent) toolResult1.content().get(0)).text();
        assertTrue(errorText1.contains("parameter_index must be a number"));

        // Test non-numeric value
        List<Object> parametersArray2 = Arrays.asList(
            Map.of("parameter_index", 0, "value", "not_a_number")
        );
        Map<String, Object> arguments2 = Map.of("parameters", parametersArray2);

        // Act & Assert for invalid value type
        McpSchema.CallToolResult toolResult2 = multiHandler.apply(mockExchange, arguments2);
        assertTrue(toolResult2.isError());
        String errorText2 = ((McpSchema.TextContent) toolResult2.content().get(0)).text();
        assertTrue(errorText2.contains("value must be a number"));
    }

    @Test
    void testSetMultipleDeviceParameters_SingleParameter() {
        // Arrange
        List<Object> parametersArray = Arrays.asList(
            Map.of("parameter_index", 4, "value", 0.8)
        );
        Map<String, Object> arguments = Map.of("parameters", parametersArray);

        List<ParameterSettingResult> mockResults = Arrays.asList(
            new ParameterSettingResult(4, "success", 0.8, null, null)
        );

        when(mockDeviceController.setMultipleSelectedDeviceParameters(any())).thenReturn(mockResults);

        // Initialize handler
        DeviceParamTool.setMultipleDeviceParametersSpecification(mockDeviceController, mockLogger);
        BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> multiHandler =
            DeviceParamTool.getSetMultipleParametersHandler();

        // Act
        McpSchema.CallToolResult toolResult = multiHandler.apply(mockExchange, arguments);

        // Assert
        assertNotNull(toolResult);
        assertFalse(toolResult.isError());
        assertEquals(1, toolResult.content().size());

        // Verify JSON response
        McpSchema.TextContent textContent = (McpSchema.TextContent) toolResult.content().get(0);
        String responseText = textContent.text();

        assertTrue(responseText.contains("\"status\":\"success\""));
        assertTrue(responseText.contains("\"parameter_index\":4"));
        assertTrue(responseText.contains("\"new_value\":0.8"));

        // Verify logging
        verify(mockLogger).info("Parsed 1 parameter settings for batch operation");
        verify(mockLogger).info("Batch operation completed: 1 succeeded, 0 failed");
    }

    @Test
    void testSetMultipleDeviceParameters_BoundaryValues() {
        // Arrange - test boundary parameter indices and values
        List<Object> parametersArray = Arrays.asList(
            Map.of("parameter_index", 0, "value", 0.0),     // Min index, min value
            Map.of("parameter_index", 7, "value", 1.0)      // Max index, max value
        );
        Map<String, Object> arguments = Map.of("parameters", parametersArray);

        List<ParameterSettingResult> mockResults = Arrays.asList(
            new ParameterSettingResult(0, "success", 0.0, null, null),
            new ParameterSettingResult(7, "success", 1.0, null, null)
        );

        when(mockDeviceController.setMultipleSelectedDeviceParameters(any())).thenReturn(mockResults);

        // Initialize handler
        DeviceParamTool.setMultipleDeviceParametersSpecification(mockDeviceController, mockLogger);
        BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> multiHandler =
            DeviceParamTool.getSetMultipleParametersHandler();

        // Act
        McpSchema.CallToolResult toolResult = multiHandler.apply(mockExchange, arguments);

        // Assert
        assertNotNull(toolResult);
        assertFalse(toolResult.isError());

        // Verify both boundary values are handled correctly
        McpSchema.TextContent textContent = (McpSchema.TextContent) toolResult.content().get(0);
        String responseText = textContent.text();

        assertTrue(responseText.contains("\"parameter_index\":0"));
        assertTrue(responseText.contains("\"new_value\":0.0"));
        assertTrue(responseText.contains("\"parameter_index\":7"));
        assertTrue(responseText.contains("\"new_value\":1.0"));
    }
}
