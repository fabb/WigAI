package io.github.fabb.wigai.mcp;

import io.github.fabb.wigai.WigAIExtensionDefinition;
import io.github.fabb.wigai.bitwig.BitwigApiFacade;
import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.common.logging.StructuredLogger;
import io.github.fabb.wigai.config.ConfigManager;
import io.github.fabb.wigai.features.TransportController;
import io.github.fabb.wigai.features.DeviceController;
import io.github.fabb.wigai.features.ClipSceneController;
import io.github.fabb.wigai.features.ProjectController;
import io.modelcontextprotocol.server.*;
import io.modelcontextprotocol.server.transport.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.servlet.ServletHolder;
import io.github.fabb.wigai.mcp.tool.StatusTool;
import io.github.fabb.wigai.mcp.tool.TransportTool;
import io.github.fabb.wigai.mcp.tool.ProjectTool;
import io.github.fabb.wigai.mcp.tool.DeviceParamTool;
import io.github.fabb.wigai.mcp.tool.ClipTool;
import io.github.fabb.wigai.mcp.tool.SceneTool;
import io.github.fabb.wigai.mcp.tool.ListTracksTool;
import io.github.fabb.wigai.mcp.tool.ListDevicesOnTrackTool;
import io.github.fabb.wigai.mcp.tool.GetTrackDetailsTool;
import io.github.fabb.wigai.mcp.tool.GetDeviceDetailsTool;
import io.github.fabb.wigai.mcp.tool.ListScenesTool;
import io.github.fabb.wigai.mcp.tool.GetClipsInSceneTool;
import io.modelcontextprotocol.spec.McpSchema;
import com.bitwig.extension.controller.api.ControllerHost;
import io.github.fabb.wigai.mcp.tool.SceneByNameTool;

/**
 * Manages the MCP server for the WigAI extension.
 * Responsible for configuring and managing the MCP HTTP servlet
 * that uses the Server-Sent Events (SSE) transport with the MCP Java SDK.
 *
 * This implementation:
 * - Sets up the MCP Java SDK with SSE transport
 * - Implements standard MCP ping functionality
 * - Registers custom tools like the "status" tool and "transport_start" tool
 * - Configures the appropriate error handling
 * - Provides logging for MCP requests and responses
 * - Registers the MCP servlet with the provided ServletContextHandler
 */
public class McpServerManager {
    private final Logger logger;
    private final WigAIExtensionDefinition extensionDefinition;
    private final ControllerHost controllerHost;

    private HttpServletStreamableServerTransportProvider transportProvider;

    // Reusable controllers - initialized once during first start
    private BitwigApiFacade bitwigApiFacade;
    private TransportController transportController;
    private DeviceController deviceController;
    private ClipSceneController clipSceneController;
    private ProjectController projectController;

    /**
     * Creates a new McpServerManager instance.
     *
     * @param logger             The logger to use for logging server events
     * @param configManager      The configuration manager (kept for API compatibility)
     * @param extensionDefinition The extension definition to get version information
     */
    public McpServerManager(Logger logger, ConfigManager configManager, WigAIExtensionDefinition extensionDefinition) {
        this(logger, configManager, extensionDefinition, null);
    }

    /**
     * Creates a new McpServerManager instance with a controller host.
     *
     * @param logger             The logger to use for logging server events
     * @param configManager      The configuration manager (kept for API compatibility)
     * @param extensionDefinition The extension definition to get version information
     * @param controllerHost     The Bitwig controller host, or null if not available
     */
    public McpServerManager(Logger logger, ConfigManager configManager, WigAIExtensionDefinition extensionDefinition, ControllerHost controllerHost) {
        this.logger = logger;
        this.extensionDefinition = extensionDefinition;
        this.controllerHost = controllerHost;
    }

    /**
     * Gets the Bitwig controller host.
     *
     * @return The controller host
     * @throws IllegalStateException if the controller host is not available
     */
    public ControllerHost getHost() {
        if (controllerHost == null) {
            throw new IllegalStateException("Controller host is not available");
        }
        return controllerHost;
    }

    /**
     * Creates and returns the MCP servlet.
     * Configures the server with the SSE transport and registers
     * the standard ping functionality and available tools.
     *
     * @param endpointPath The endpoint path for the MCP servlet
     * @return The configured MCP servlet
     * @throws Exception if servlet creation fails
     */
    public ServletHolder createMcpServlet(String endpointPath) throws Exception {
        // 1. Instantiate ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();

        // 2. Instantiate HttpServletStreamableServerTransportProvider
        this.transportProvider = HttpServletStreamableServerTransportProvider
            .builder()
            .objectMapper(objectMapper)
            .mcpEndpoint(endpointPath)
            .build();

        // 3. Configure the MCP server with tools
        // Note: Direct request/response logging with onRequest/onResponse methods is not
        // supported by the MCP Java SDK. The StatusTool implementation handles its own
        // logging. If more detailed logging is needed, we should investigate alternative
        // approaches with the MCP SDK.

        // Initialize controllers only once during first start to avoid Bitwig API restrictions
        if (bitwigApiFacade == null) {
            logger.info("McpServerManager: Initializing Bitwig API controllers");
            bitwigApiFacade = new BitwigApiFacade(getHost(), logger);
            transportController = new TransportController(bitwigApiFacade, logger);
            deviceController = new DeviceController(bitwigApiFacade, logger);
            clipSceneController = new ClipSceneController(bitwigApiFacade, logger);
            projectController = new ProjectController(bitwigApiFacade, logger);
        } else {
            logger.info("McpServerManager: Reusing existing Bitwig API controllers");
        }

        // Create StructuredLogger for tools that have been migrated to unified error handling
        StructuredLogger structuredLogger = new StructuredLogger(logger, "MCP-Tools");

        McpServer.sync(this.transportProvider)
            .serverInfo("WigAI", extensionDefinition.getVersion())
            .capabilities(McpSchema.ServerCapabilities.builder()
                .tools(true)
                .logging()
                .build())
            .tools(
                StatusTool.specification(this.extensionDefinition, bitwigApiFacade, structuredLogger),
                TransportTool.transportStartSpecification(transportController, structuredLogger),
                TransportTool.transportStopSpecification(transportController, structuredLogger),
                ProjectTool.nextProjectSpecification(projectController, structuredLogger),
                ProjectTool.previousProjectSpecification(projectController, structuredLogger),
                ClipTool.launchClipSpecification(clipSceneController, structuredLogger),
                SceneTool.launchSceneByIndexSpecification(clipSceneController, structuredLogger),
                SceneByNameTool.launchSceneByNameSpecification(clipSceneController, structuredLogger),
                DeviceParamTool.getSelectedDeviceParametersSpecification(deviceController, structuredLogger),
                DeviceParamTool.setSelectedDeviceParameterSpecification(deviceController, structuredLogger),
                DeviceParamTool.setMultipleDeviceParametersSpecification(deviceController, structuredLogger),
                GetDeviceDetailsTool.getDeviceDetailsSpecification(deviceController, structuredLogger),
                ListTracksTool.specification(bitwigApiFacade, structuredLogger),
                ListDevicesOnTrackTool.specification(bitwigApiFacade, structuredLogger),
                GetTrackDetailsTool.specification(bitwigApiFacade, structuredLogger),
                ListScenesTool.specification(bitwigApiFacade, structuredLogger),
                GetClipsInSceneTool.getClipsInSceneSpecification(clipSceneController, structuredLogger)
            )
            .build();

        // 4. Return the MCP servlet
        return new ServletHolder(this.transportProvider);
    }
}
