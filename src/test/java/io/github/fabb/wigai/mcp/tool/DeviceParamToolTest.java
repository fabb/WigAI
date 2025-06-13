package io.github.fabb.wigai.mcp.tool;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.common.data.ParameterInfo;
import io.github.fabb.wigai.common.data.ParameterSettingResult;
import io.github.fabb.wigai.common.error.BitwigApiException;
import io.github.fabb.wigai.common.error.ErrorCode;
import io.github.fabb.wigai.mcp.McpErrorHandler;
import io.github.fabb.wigai.common.logging.StructuredLogger;
import io.github.fabb.wigai.features.DeviceController;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Unit tests for DeviceParamTool after migration to unified error handling architecture.
 */
class DeviceParamToolTest {

    @Mock
    private DeviceController deviceController;
    @Mock
    private StructuredLogger structuredLogger;
    @Mock
    private Logger baseLogger;
    @Mock
    private StructuredLogger.TimedOperation timedOperation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(structuredLogger.getBaseLogger()).thenReturn(baseLogger);
        when(structuredLogger.generateOperationId()).thenReturn("op-123");
        when(structuredLogger.startTimedOperation(any(), any(), any())).thenReturn(timedOperation);
    }

    @Test
    void testGetSelectedDeviceParametersSpecification() {
        McpServerFeatures.SyncToolSpecification spec = DeviceParamTool.getSelectedDeviceParametersSpecification(deviceController, structuredLogger);

        assertNotNull(spec);
        assertNotNull(spec.tool());
        assertEquals("get_selected_device_parameters", spec.tool().name());
        assertTrue(spec.tool().description().contains("device"));
        assertNotNull(spec.tool().inputSchema());
    }

    @Test
    void testSetSelectedDeviceParameterSpecification() {
        McpServerFeatures.SyncToolSpecification spec = DeviceParamTool.setSelectedDeviceParameterSpecification(deviceController, structuredLogger);

        assertNotNull(spec);
        assertNotNull(spec.tool());
        assertEquals("set_selected_device_parameter", spec.tool().name());
        assertTrue(spec.tool().description().contains("parameter"));
        assertNotNull(spec.tool().inputSchema());
    }

    @Test
    void testSetMultipleDeviceParametersSpecification() {
        McpServerFeatures.SyncToolSpecification spec = DeviceParamTool.setMultipleDeviceParametersSpecification(deviceController, structuredLogger);

        assertNotNull(spec);
        assertNotNull(spec.tool());
        assertEquals("set_selected_device_parameters", spec.tool().name());
        assertTrue(spec.tool().description().contains("multiple"));
        assertNotNull(spec.tool().inputSchema());
    }

    @Test
    void testGetDeviceParametersSuccessResponseFormat() throws Exception {
        // Arrange: Mock device parameters response
        DeviceController.DeviceParametersResult mockResult = new DeviceController.DeviceParametersResult(
            "Test Device",
            List.of(
                new ParameterInfo(0, "Param 1", 0.5, "50%"),
                new ParameterInfo(1, "Param 2", 0.8, "80%")
            )
        );
        when(deviceController.getSelectedDeviceParameters()).thenReturn(mockResult);

        // Act: Simulate what the tool does - creates response data structure
        Map<String, Object> responseData = new LinkedHashMap<>();
        responseData.put("device_name", mockResult.deviceName());
        
        List<Map<String, Object>> parametersArray = new ArrayList<>();
        for (ParameterInfo param : mockResult.parameters()) {
            Map<String, Object> paramMap = new LinkedHashMap<>();
            paramMap.put("index", param.index());
            paramMap.put("name", param.name());
            paramMap.put("value", param.value());
            paramMap.put("display_value", param.display_value());
            parametersArray.add(paramMap);
        }
        responseData.put("parameters", parametersArray);
        
        McpSchema.CallToolResult result = McpErrorHandler.createSuccessResponse(responseData);

        // Assert: Validate object response format
        JsonNode dataNode = McpResponseTestUtils.validateObjectResponse(result);
        
        // Verify device parameter response structure
        assertTrue(dataNode.has("device_name"));
        assertTrue(dataNode.has("parameters"));
        assertEquals("Test Device", dataNode.get("device_name").asText());
        
        JsonNode parametersNode = dataNode.get("parameters");
        assertTrue(parametersNode.isArray());
        assertEquals(2, parametersNode.size());
        
        JsonNode firstParam = parametersNode.get(0);
        assertEquals(0, firstParam.get("index").asInt());
        assertEquals("Param 1", firstParam.get("name").asText());
        assertEquals(0.5, firstParam.get("value").asDouble(), 0.001);
        assertEquals("50%", firstParam.get("display_value").asText());
    }

    @Test
    void testSetDeviceParameterSuccessResponseFormat() throws Exception {
        // Arrange: Mock successful parameter setting
        doNothing().when(deviceController).setSelectedDeviceParameter(0, 0.75);

        // Act: Simulate what the tool does
        Map<String, Object> responseData = Map.of(
            "action", "parameter_set",
            "parameter_index", 0,
            "new_value", 0.75,
            "message", "Parameter 0 set to 0.75."
        );
        McpSchema.CallToolResult result = McpErrorHandler.createSuccessResponse(responseData);

        // Assert: Validate action response format
        JsonNode dataNode = McpResponseTestUtils.validateActionResponse(result, "parameter_set");
        assertEquals(0, dataNode.get("parameter_index").asInt());
        assertEquals(0.75, dataNode.get("new_value").asDouble(), 0.001);
        assertEquals("Parameter 0 set to 0.75.", dataNode.get("message").asText());
    }

    @Test
    void testSetMultipleParametersSuccessResponseFormat() throws Exception {
        // Arrange: Mock multiple parameter setting results
        List<ParameterSettingResult> mockResults = List.of(
            new ParameterSettingResult(0, "success", 0.5, null, "Parameter set successfully"),
            new ParameterSettingResult(1, "error", null, "INVALID_PARAMETER_INDEX", "Parameter index out of range")
        );

        // Act: Simulate what the tool does
        List<Map<String, Object>> resultsArray = new ArrayList<>();
        for (ParameterSettingResult result : mockResults) {
            Map<String, Object> resultMap = new LinkedHashMap<>();
            resultMap.put("parameter_index", result.parameter_index());
            resultMap.put("status", result.status());
            if ("error".equals(result.status())) {
                resultMap.put("error_code", result.error_code());
                resultMap.put("message", result.message());
            } else {
                resultMap.put("new_value", result.new_value());
            }
            resultsArray.add(resultMap);
        }
        
        long successCount = mockResults.stream().filter(r -> "success".equals(r.status())).count();
        long errorCount = mockResults.size() - successCount;
        
        Map<String, Object> responseData = Map.of(
            "action", "multiple_parameters_set",
            "results", resultsArray,
            "message", "Batch operation completed: " + successCount + " succeeded, " + errorCount + " failed"
        );
        McpSchema.CallToolResult result = McpErrorHandler.createSuccessResponse(responseData);

        // Assert: Validate action response format
        JsonNode dataNode = McpResponseTestUtils.validateActionResponse(result, "multiple_parameters_set");
        assertTrue(dataNode.has("results"));
        
        JsonNode resultsNode = dataNode.get("results");
        assertTrue(resultsNode.isArray());
        assertEquals(2, resultsNode.size());
        
        // Check first result (success)
        JsonNode firstResult = resultsNode.get(0);
        assertEquals(0, firstResult.get("parameter_index").asInt());
        assertEquals("success", firstResult.get("status").asText());
        
        // Check second result (error)
        JsonNode secondResult = resultsNode.get(1);
        assertEquals(1, secondResult.get("parameter_index").asInt());
        assertEquals("error", secondResult.get("status").asText());
        assertEquals("INVALID_PARAMETER_INDEX", secondResult.get("error_code").asText());
    }

    @Test
    void testDeviceParameterErrorResponseFormat() throws Exception {
        // Test error response format for device parameter operations
        BitwigApiException exception = new BitwigApiException(
            ErrorCode.DEVICE_NOT_SELECTED,
            "get_selected_device_parameters",
            "No device is currently selected"
        );
        
        McpSchema.CallToolResult result = McpErrorHandler.createErrorResponse(exception, structuredLogger);
        
        // Validate error response format
        JsonNode errorNode = McpResponseTestUtils.validateErrorResponse(result);
        assertEquals("DEVICE_NOT_SELECTED", errorNode.get("code").asText());
        assertEquals("No device is currently selected", errorNode.get("message").asText());
        assertEquals("get_selected_device_parameters", errorNode.get("operation").asText());
    }

    @Test
    void testDeviceParameterResponseNotDoubleWrapped() throws Exception {
        // Test that device parameter responses are not double-wrapped
        Map<String, Object> deviceData = Map.of(
            "device_name", "Test Device",
            "parameters", List.of()
        );
        McpSchema.CallToolResult result = McpErrorHandler.createSuccessResponse(deviceData);
        
        // This would have caught the double-wrapping bug
        McpResponseTestUtils.assertNotDoubleWrapped(result);
        
        // Verify it's properly structured as an object response
        JsonNode dataNode = McpResponseTestUtils.validateObjectResponse(result);
        assertEquals("Test Device", dataNode.get("device_name").asText());
        assertTrue(dataNode.get("parameters").isArray());
    }
}
