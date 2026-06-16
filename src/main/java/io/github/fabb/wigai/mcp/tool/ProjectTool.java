package io.github.fabb.wigai.mcp.tool;

import io.github.fabb.wigai.common.logging.StructuredLogger;
import io.github.fabb.wigai.features.ProjectController;
import io.github.fabb.wigai.mcp.McpErrorHandler;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;

import java.util.Map;
import java.util.function.BiFunction;

/**
 * MCP tools for switching between open projects in Bitwig using the unified error handling architecture.
 *
 * <p>Bitwig's extension API only supports cycling through the projects that are already open
 * (Application#nextProject / Application#previousProject); it cannot open a project from disk or
 * jump to a specific project by name/index.
 */
public class ProjectTool {

    /**
     * Creates a "next_project" tool specification using the unified error handling system.
     *
     * @param projectController The controller for project operations
     * @param logger            The structured logger for logging operations
     * @return A SyncToolSpecification for the "next_project" tool
     */
    public static McpServerFeatures.SyncToolSpecification nextProjectSpecification(
            ProjectController projectController, StructuredLogger logger) {

        var schema = """
            {
              "type": "object",
              "properties": {}
            }""";
        var tool = McpSchema.Tool.builder()
            .name("next_project")
            .description("Switch to the next open project tab in Bitwig. Cycles through already-open projects; does not open projects from disk. With only one project open this is a no-op.")
            .inputSchema(schema)
            .build();

        BiFunction<McpSyncServerExchange, CallToolRequest, McpSchema.CallToolResult> handler =
            (exchange, req) -> McpErrorHandler.executeWithErrorHandling(
                "next_project",
                logger,
                () -> {
                    String resultMessage = projectController.nextProject();
                    return Map.of(
                        "action", "project_switched_next",
                        "message", resultMessage
                    );
                }
            );

        return McpServerFeatures.SyncToolSpecification.builder()
            .tool(tool)
            .callHandler(handler)
            .build();
    }

    /**
     * Creates a "previous_project" tool specification using the unified error handling system.
     *
     * @param projectController The controller for project operations
     * @param logger            The structured logger for logging operations
     * @return A SyncToolSpecification for the "previous_project" tool
     */
    public static McpServerFeatures.SyncToolSpecification previousProjectSpecification(
            ProjectController projectController, StructuredLogger logger) {

        var schema = """
            {
              "type": "object",
              "properties": {}
            }""";
        var tool = McpSchema.Tool.builder()
            .name("previous_project")
            .description("Switch to the previous open project tab in Bitwig. Cycles through already-open projects; does not open projects from disk. With only one project open this is a no-op.")
            .inputSchema(schema)
            .build();

        BiFunction<McpSyncServerExchange, CallToolRequest, McpSchema.CallToolResult> handler =
            (exchange, req) -> McpErrorHandler.executeWithErrorHandling(
                "previous_project",
                logger,
                () -> {
                    String resultMessage = projectController.previousProject();
                    return Map.of(
                        "action", "project_switched_previous",
                        "message", resultMessage
                    );
                }
            );

        return McpServerFeatures.SyncToolSpecification.builder()
            .tool(tool)
            .callHandler(handler)
            .build();
    }
}
