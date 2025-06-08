package io.github.fabb.wigai.mcp.tool;

import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.common.data.ParameterInfo;
import io.github.fabb.wigai.common.data.ParameterSettingResult;
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

import java.util.Collections;
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
}
