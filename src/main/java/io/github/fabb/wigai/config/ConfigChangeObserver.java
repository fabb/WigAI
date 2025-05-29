package io.github.fabb.wigai.config;

/**
 * Observer interface for configuration changes in the WigAI extension.
 * Allows components to be notified when MCP server settings change.
 */
public interface ConfigChangeObserver {
    /**
     * Called when the MCP server host changes.
     *
     * @param oldHost The previous host value
     * @param newHost The new host value
     */
    void onHostChanged(String oldHost, String newHost);

    /**
     * Called when the MCP server port changes.
     *
     * @param oldPort The previous port value
     * @param newPort The new port value
     */
    void onPortChanged(int oldPort, int newPort);
}
