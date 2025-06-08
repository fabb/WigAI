package io.github.fabb.wigai.mcp.tool;

import io.github.fabb.wigai.bitwig.BitwigApiFacade;
import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.common.data.ParameterInfo;
import io.github.fabb.wigai.common.logging.StructuredLogger;
import io.github.fabb.wigai.WigAIExtensionDefinition;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for StatusTool after migration to unified error handling architecture.
 */
class StatusToolTest {

    @Mock
    private WigAIExtensionDefinition extensionDefinition;
    @Mock
    private BitwigApiFacade bitwigApiFacade;
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
    void testStatusSpecification() {
        McpServerFeatures.SyncToolSpecification spec = StatusTool.specification(extensionDefinition, bitwigApiFacade, structuredLogger);

        assertNotNull(spec);
        assertNotNull(spec.tool());
        assertEquals("status", spec.tool().name());
        assertTrue(spec.tool().description().contains("status"));
        assertNotNull(spec.tool().inputSchema());
    }

    @Test
    void testSpecificationValidation() {
        // Test that the tool specification is properly configured
        McpServerFeatures.SyncToolSpecification spec = StatusTool.specification(extensionDefinition, bitwigApiFacade, structuredLogger);

        assertNotNull(spec.tool().inputSchema());
        assertEquals("status", spec.tool().name());
    }
}
