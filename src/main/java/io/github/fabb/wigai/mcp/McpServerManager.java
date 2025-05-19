package io.github.fabb.wigai.mcp;

import io.github.fabb.wigai.WigAIExtensionDefinition;
import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.config.ConfigManager;
import io.modelcontextprotocol.server.*;
import io.modelcontextprotocol.server.transport.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import io.github.fabb.wigai.mcp.tool.StatusTool;
import io.modelcontextprotocol.spec.McpSchema;

/**
 * Manages the MCP server for the WigAI extension.
 * Responsible for starting, configuring, and stopping the embedded MCP HTTP server
 * that uses the Server-Sent Events (SSE) transport with the MCP Java SDK.
 *
 * This implementation:
 * - Sets up the MCP Java SDK with SSE transport
 * - Implements standard MCP ping functionality
 * - Registers custom tools like the "status" tool
 * - Configures the appropriate error handling
 * - Provides logging for MCP requests and responses
 */
public class McpServerManager {
    private final Logger logger;
    private final String host;
    private final int port;
    private final WigAIExtensionDefinition extensionDefinition;

    private McpSyncServer mcpServer;
    private volatile boolean isRunning;
    private Server jettyServer;
    private HttpServletSseServerTransportProvider transportProvider;

    /**
     * Creates a new McpServerManager instance.
     *
     * @param logger             The logger to use for logging server events
     * @param configManager      The configuration manager to get server settings from
     * @param extensionDefinition The extension definition to get version information
     */
    public McpServerManager(Logger logger, ConfigManager configManager, WigAIExtensionDefinition extensionDefinition) {
        this.logger = logger;
        this.host = configManager.getMcpHost();
        this.port = configManager.getMcpPort();
        this.extensionDefinition = extensionDefinition;
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
            this.mcpServer = McpServer.sync(this.transportProvider)
                .serverInfo("WigAI", extensionDefinition.getVersion())
                .capabilities(McpSchema.ServerCapabilities.builder()
                    .tools(true)
                    .logging()
                    .build())
                .tools(
                    StatusTool.specification(this.extensionDefinition, logger)
                )
                .build();

            // 4. Servlet Container Setup (Embedded Jetty)
            this.jettyServer = new Server();
            ServerConnector connector = new ServerConnector(this.jettyServer);
            connector.setHost(this.host);
            connector.setPort(this.port);
            this.jettyServer.addConnector(connector);

            ServletContextHandler contextHandler = new ServletContextHandler();
            contextHandler.setContextPath("/");
            contextHandler.addServlet(new ServletHolder(this.transportProvider), "/*");
            this.jettyServer.setHandler(contextHandler);

            this.jettyServer.start();
            isRunning = true;
            logger.info(String.format("MCP Server started on http://%s:%d/mcp", host, port));
            logger.info(String.format("MCP SSE endpoint available at http://%s:%d/sse", host, port));
        } catch (Exception e) {
            isRunning = false;
            logger.error(String.format("Failed to start MCP Server on %s:%d", host, port), e);
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
    }

    /**
     * Checks if the server is currently running.
     *
     * @return true if the server is running, false otherwise
     */
    public boolean isRunning() {
        return isRunning;
    }
}
