package io.github.fabb.wigai.smoke;

import java.util.List;
import java.util.Map;

/**
 * Minimal client abstraction used by the smoke harness.
 * Implementations will translate these operations into MCP protocol calls over HTTP.
 */
public interface McpClient {

    List<String> listTools();

    /**
     * Calls a tool and returns the raw JSON text payload returned by WigAI (single text content).
     */
    String callTool(String toolName, Map<String, Object> arguments);
}

