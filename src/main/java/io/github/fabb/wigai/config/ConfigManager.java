package io.github.fabb.wigai.config;

import io.github.fabb.wigai.common.AppConstants;
import io.github.fabb.wigai.common.Logger;

/**
 * Configuration manager for the WigAI extension.
 * For the initial version, this provides basic configuration options with
 * default values.
 */
public class ConfigManager {
    private final Logger logger;
    private int mcpPort = AppConstants.DEFAULT_MCP_PORT;
    private String mcpHost = "localhost";

    /**
     * Creates a new ConfigManager instance.
     *
     * @param logger The logger to use for logging configuration events
     */
    public ConfigManager(Logger logger) {
        this.logger = logger;
        logger.info("ConfigManager initialized with default settings");
    }

    /**
     * Gets the configured MCP server host.
     *
     * @return The MCP server host
     */
    public String getMcpHost() {
        return mcpHost;
    }

    /**
     * Gets the configured MCP server port.
     *
     * @return The MCP server port
     */
    public int getMcpPort() {
        return mcpPort;
    }

    /**
     * Sets the MCP server port.
     *
     * @param port The port to use for the MCP server
     */
    public void setMcpPort(int port) {
        if (port <= 0 || port > 65535) {
            logger.warn("Invalid port number: " + port + ". Using default: " + AppConstants.DEFAULT_MCP_PORT);
            this.mcpPort = AppConstants.DEFAULT_MCP_PORT;
        } else {
            this.mcpPort = port;
            logger.info("MCP server port set to: " + port);
        }
    }

    /**
     * Sets the MCP server host.
     *
     * @param host The host to use for the MCP server
     */
    public void setMcpHost(String host) {
        if (host == null || host.isEmpty()) {
            logger.warn("Invalid host: " + host + ". Using default: localhost");
            this.mcpHost = "localhost";
        } else {
            this.mcpHost = host;
            logger.info("MCP server host set to: " + host);
        }
    }
}
