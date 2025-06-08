package io.github.fabb.wigai.mcp.tool;

import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.common.logging.StructuredLogger;
import io.github.fabb.wigai.features.TransportController;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for TransportTool after migration to unified error handling architecture.
 */
class TransportToolTest {

    @Mock
    private TransportController transportController;
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
    void testTransportStartSpecification() {
        McpServerFeatures.SyncToolSpecification spec = TransportTool.transportStartSpecification(transportController, structuredLogger);

        assertNotNull(spec);
        assertNotNull(spec.tool());
        assertEquals("transport_start", spec.tool().name());
        assertTrue(spec.tool().description().contains("Start"));
        assertNotNull(spec.tool().inputSchema());
    }

    @Test
    void testTransportStopSpecification() {
        McpServerFeatures.SyncToolSpecification spec = TransportTool.transportStopSpecification(transportController, structuredLogger);

        assertNotNull(spec);
        assertNotNull(spec.tool());
        assertEquals("transport_stop", spec.tool().name());
        assertTrue(spec.tool().description().contains("Stop"));
        assertNotNull(spec.tool().inputSchema());
    }
}
