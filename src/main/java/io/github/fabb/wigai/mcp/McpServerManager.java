package io.github.fabb.wigai.mcp;

import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.config.ConfigManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages the MCP server for the WigAI extension.
 * Responsible for starting, configuring, and stopping the embedded MCP HTTP server
 * that uses the Streamable HTTP/SSE transport.
 */
public class McpServerManager {
    private final Logger logger;
    private final String host;
    private final int port;

    private ExecutorService executor;
    private ServerSocket serverSocket;
    private volatile boolean isRunning;

    /**
     * Creates a new McpServerManager instance.
     *
     * @param logger       The logger to use for logging server events
     * @param configManager The configuration manager to get server settings from
     */
    public McpServerManager(Logger logger, ConfigManager configManager) {
        this.logger = logger;
        this.host = configManager.getMcpHost();
        this.port = configManager.getMcpPort();
    }

    /**
     * Starts the MCP server.
     *
     * This implementation creates a basic ServerSocket to handle incoming connections.
     * In a real implementation, this would be replaced with proper MCP Java SDK setup
     * to handle the MCP protocol and SSE streaming.
     */
    public void start() {
        if (isRunning) {
            logger.info("MCP Server is already running");
            return;
        }

        try {
            // Create a server socket and bind it to the configured host and port
            executor = Executors.newCachedThreadPool();
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(host, port));
            isRunning = true;

            logger.info(String.format("MCP Server started on http://%s:%d/mcp", host, port));

            // Start accepting connections in a separate thread
            executor.submit(() -> {
                while (isRunning) {
                    try {
                        // Accept incoming connections and handle them
                        // This is a stub implementation that just accepts and immediately closes connections
                        // to demonstrate that the server is running and accessible
                        var socket = serverSocket.accept();
                        logger.info("Received connection from " + socket.getInetAddress().getHostAddress());

                        // In a real implementation, this would handle the connection using the MCP SDK
                        // For now, we just respond with a basic HTTP response
                        executor.submit(() -> {
                            try (var out = socket.getOutputStream()) {
                                String response = "HTTP/1.1 200 OK\r\n" +
                                                 "Content-Type: application/json\r\n" +
                                                 "Connection: close\r\n" +
                                                 "\r\n" +
                                                 "{\"error\":\"Unknown command\",\"status\":\"error\"}";
                                out.write(response.getBytes());
                                socket.close();
                            } catch (IOException e) {
                                logger.error("Error handling client connection", e);
                            }
                        });
                    } catch (IOException e) {
                        if (isRunning) {
                            logger.error("Error accepting connection", e);
                        }
                        // If server socket is closed while accepting, this is expected during shutdown
                    }
                }
            });
        } catch (IOException e) {
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
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.error("Error closing server socket", e);
        }

        if (executor != null) {
            executor.shutdownNow();
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
