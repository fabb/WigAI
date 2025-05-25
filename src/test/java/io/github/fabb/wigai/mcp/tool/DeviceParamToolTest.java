package io.github.fabb.wigai.mcp.tool;

import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.common.data.ParameterInfo;
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
}
