package io.github.fabb.wigai.mcp;

import io.github.fabb.wigai.WigAIExtensionDefinition;
import io.github.fabb.wigai.bitwig.BitwigApiFacade;
import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.config.ConfigChangeObserver;
import io.github.fabb.wigai.config.ConfigManager;
import io.github.fabb.wigai.features.TransportController;
import io.github.fabb.wigai.features.DeviceController;
import io.github.fabb.wigai.features.ClipSceneController;
import io.modelcontextprotocol.server.*;
import io.modelcontextprotocol.server.transport.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import io.github.fabb.wigai.mcp.tool.StatusTool;
import io.github.fabb.wigai.mcp.tool.TransportTool;
import io.github.fabb.wigai.mcp.tool.DeviceParamTool;
import io.github.fabb.wigai.mcp.tool.ClipTool;
import io.github.fabb.wigai.mcp.tool.SceneTool;
import io.modelcontextprotocol.spec.McpSchema;
import com.bitwig.extension.controller.api.ControllerHost;
import io.github.fabb.wigai.mcp.tool.SceneByNameTool;

/**
 * Manages the MCP server for the WigAI extension.
 * Responsible for starting, configuring, and stopping the embedded MCP HTTP server
 * that uses the Server-Sent Events (SSE) transport with the MCP Java SDK.
 *
 * This implementation:
 * - Sets up the MCP Java SDK with SSE transport
 * - Implements standard MCP ping functionality
 * - Registers custom tools like the "status" tool and "transport_start" tool
 * - Configures the appropriate error handling
 * - Provides logging for MCP requests and responses
 */
public class McpServerManager implements ConfigChangeObserver {
    private final Logger logger;
    private final ConfigManager configManager;
    private final WigAIExtensionDefinition extensionDefinition;
    private final ControllerHost controllerHost;

    private McpSyncServer mcpServer;
    private volatile boolean isRunning;
    private Server jettyServer;
    private HttpServletSseServerTransportProvider transportProvider;

    // Reusable controllers - initialized once during first start
    private BitwigApiFacade bitwigApiFacade;
    private TransportController transportController;
    private DeviceController deviceController;
    private ClipSceneController clipSceneController;

    /**
     * Creates a new McpServerManager instance.
     *
     * @param logger             The logger to use for logging server events
     * @param configManager      The configuration manager to get server settings from
     * @param extensionDefinition The extension definition to get version information
     */
    public McpServerManager(Logger logger, ConfigManager configManager, WigAIExtensionDefinition extensionDefinition) {
        this(logger, configManager, extensionDefinition, null);
    }

    /**
     * Creates a new McpServerManager instance with a controller host.
     *
     * @param logger             The logger to use for logging server events
     * @param configManager      The configuration manager to get server settings from
     * @param extensionDefinition The extension definition to get version information
     * @param controllerHost     The Bitwig controller host, or null if not available
     */
    public McpServerManager(Logger logger, ConfigManager configManager, WigAIExtensionDefinition extensionDefinition, ControllerHost controllerHost) {
        this.logger = logger;
        this.configManager = configManager;
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
     * Starts the MCP server using the MCP Java SDK.
     * Configures the server with the SSE transport and registers
     * the standard ping functionality and available tools.
     */
    public void start() {
        if (isRunning) {
            logger.info("MCP Server is already running");
            return;
        }

        try {
            // 1. Instantiate ObjectMapper
            ObjectMapper objectMapper = new ObjectMapper();

            // 2. Instantiate HttpServletSseServerTransportProvider
            this.transportProvider = new HttpServletSseServerTransportProvider(objectMapper, "/mcp", "/sse");

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
            } else {
                logger.info("McpServerManager: Reusing existing Bitwig API controllers");
            }

            this.mcpServer = McpServer.sync(this.transportProvider)
                .serverInfo("WigAI", extensionDefinition.getVersion())
                .capabilities(McpSchema.ServerCapabilities.builder()
                    .tools(true)
                    .logging()
                    .build())
                .tools(
                    StatusTool.specification(this.extensionDefinition, logger),
                    TransportTool.transportStartSpecification(transportController, logger),
                    TransportTool.transportStopSpecification(transportController, logger),
                    DeviceParamTool.getSelectedDeviceParametersSpecification(deviceController, logger),
                    DeviceParamTool.setSelectedDeviceParameterSpecification(deviceController, logger),
                    DeviceParamTool.setMultipleDeviceParametersSpecification(deviceController, logger),
                    ClipTool.launchClipSpecification(clipSceneController, logger),
                    SceneTool.launchSceneByIndexSpecification(clipSceneController, logger),
                    SceneByNameTool.launchSceneByNameSpecification(clipSceneController, logger)
                )
                .build();

            // 4. Servlet Container Setup (Embedded Jetty)
            this.jettyServer = new Server();
            ServerConnector connector = new ServerConnector(this.jettyServer);
            connector.setHost(configManager.getMcpHost());
            connector.setPort(configManager.getMcpPort());
            this.jettyServer.addConnector(connector);

            ServletContextHandler contextHandler = new ServletContextHandler();
            contextHandler.setContextPath("/");
            contextHandler.addServlet(new ServletHolder(this.transportProvider), "/*");
            this.jettyServer.setHandler(contextHandler);

            this.jettyServer.start();
            isRunning = true;
            logger.info(String.format("MCP Server started on http://%s:%d/mcp", configManager.getMcpHost(), configManager.getMcpPort()));
            logger.info(String.format("MCP SSE endpoint available at http://%s:%d/sse", configManager.getMcpHost(), configManager.getMcpPort()));

            // Show popup notification with connection info
            notifyServerStarted();
        } catch (Exception e) {
            isRunning = false;
            logger.error(String.format("Failed to start MCP Server on %s:%d", configManager.getMcpHost(), configManager.getMcpPort()), e);
        }
    }

    /**
     * Stops the MCP server.
     */
    public void stop() {
        if (!isRunning) {
            logger.info("MCP Server is not running");
            return;
        }

        isRunning = false;

        try {
            if (jettyServer != null && jettyServer.isRunning()) {
                logger.info("Stopping Jetty server...");
                jettyServer.stop();
                logger.info("Jetty server stopped.");
            }
            if (mcpServer != null) {
                mcpServer.close();
                logger.info("MCP Server closed");
            }
        } catch (Exception e) {
            logger.error("Error stopping MCP Server", e);
        }

        logger.info("MCP Server stopped");

        // Show popup notification
        notifyServerStopped();
    }

    /**
     * Checks if the server is currently running.
     *
     * @return true if the server is running, false otherwise
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Called when the MCP server host changes.
     * Triggers a graceful restart of the MCP server with the new host.
     *
     * @param oldHost The previous host value
     * @param newHost The new host value
     */
    @Override
    public void onHostChanged(String oldHost, String newHost) {
        logger.info("McpServerManager: Host changed from '" + oldHost + "' to '" + newHost + "', restarting MCP server");
        restartServer();
    }

    /**
     * Called when the MCP server port changes.
     * Triggers a graceful restart of the MCP server with the new port.
     *
     * @param oldPort The previous port value
     * @param newPort The new port value
     */
    @Override
    public void onPortChanged(int oldPort, int newPort) {
        logger.info("McpServerManager: Port changed from " + oldPort + " to " + newPort + ", restarting MCP server");
        restartServer();
    }

    /**
     * Gracefully restarts the MCP server.
     * Stops the current server and starts a new one with updated configuration.
     */
    private void restartServer() {
        try {
            logger.info("McpServerManager: Beginning graceful restart");

            // Stop the current server if running
            if (isRunning()) {
                logger.info("McpServerManager: Stopping current server for restart");
                stop();
            }

            // Small delay to ensure clean shutdown
            Thread.sleep(500);

            // Start the server with new configuration
            logger.info("McpServerManager: Starting server with updated configuration");
            start();

            logger.info("McpServerManager: Restart completed successfully");

            // Show restart notification with new connection info
            notifyServerRestarted();

        } catch (Exception e) {
            logger.error("McpServerManager: Error during server restart", e);
        }
    }

    /**
     * Shows a popup notification when the MCP server starts successfully.
     * Includes both status and connection information for user convenience.
     */
    private void notifyServerStarted() {
        if (controllerHost == null) {
            logger.debug("McpServerManager: Controller host not available, skipping startup notification");
            return;
        }

        String connectionUrl = String.format("http://%s:%d/sse",
            configManager.getMcpHost(), configManager.getMcpPort());
        String message = String.format("WigAI MCP Server started. Connect AI agents to: %s",
            connectionUrl);

        try {
            controllerHost.showPopupNotification(message);
            logger.info("McpServerManager: Displayed startup notification: " + message);
        } catch (Exception e) {
            logger.error("McpServerManager: Error showing startup notification", e);
        }
    }

    /**
     * Shows a popup notification when the MCP server is restarted due to configuration changes.
     * Combines restart status with new connection information.
     */
    private void notifyServerRestarted() {
        if (controllerHost == null) {
            logger.debug("McpServerManager: Controller host not available, skipping restart notification");
            return;
        }

        String connectionUrl = String.format("http://%s:%d/sse",
            configManager.getMcpHost(), configManager.getMcpPort());
        String message = String.format("WigAI MCP Server restarted. Connect AI agents to: %s",
            connectionUrl);

        try {
            controllerHost.showPopupNotification(message);
            logger.info("McpServerManager: Displayed restart notification: " + message);
        } catch (Exception e) {
            logger.error("McpServerManager: Error showing restart notification", e);
        }
    }

    /**
     * Shows a popup notification when the MCP server stops.
     */
    private void notifyServerStopped() {
        if (controllerHost == null) {
            logger.debug("McpServerManager: Controller host not available, skipping stop notification");
            return;
        }

        String message = "WigAI MCP Server stopped";

        try {
            controllerHost.showPopupNotification(message);
            logger.info("McpServerManager: Displayed stop notification: " + message);
        } catch (Exception e) {
            logger.error("McpServerManager: Error showing stop notification", e);
        }
    }
}
