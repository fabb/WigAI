package io.github.fabb.wigai.config;

/**
 * Interface for configuration management in the WigAI extension.
 * Provides a contract for retrieving and modifying MCP server settings,
 * enabling polymorphic implementations (e.g., preferences-backed, file-based).
 */
public interface ConfigManager {
    /**
     * Gets the configured MCP server host.
     *
     * @return The MCP server host
     */
    String getMcpHost();

    /**
     * Gets the configured MCP server port.
     *
     * @return The MCP server port
     */
    int getMcpPort();

    /**
     * Sets the MCP server host.
     *
     * @param host The host to use for the MCP server
     */
    void setMcpHost(String host);

    /**
     * Sets the MCP server port.
     *
     * @param port The port to use for the MCP server
     */
    void setMcpPort(int port);

    /**
     * Adds an observer to be notified when configuration changes.
     *
     * @param observer The observer to add
     */
    void addObserver(ConfigChangeObserver observer);
}
