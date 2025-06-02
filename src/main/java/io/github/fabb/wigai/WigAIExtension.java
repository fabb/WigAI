package io.github.fabb.wigai;

import com.bitwig.extension.controller.ControllerExtension;
import com.bitwig.extension.controller.api.ControllerHost;
import io.github.fabb.wigai.common.AppConstants; // Added import
import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.config.ConfigManager;
import io.github.fabb.wigai.config.PreferencesBackedConfigManager;
import io.github.fabb.wigai.mcp.McpServerManager;

/**
 * Main extension class for the WigAI extension.
 * Handles lifecycle events (init, exit) and owns the primary components.
 */
public class WigAIExtension extends ControllerExtension {
    private Logger logger;
    private ConfigManager configManager;
    private McpServerManager mcpServerManager;

    /**
     * Creates a new WigAIExtension instance.
     *
     * @param definition The extension definition
     * @param host       The Bitwig ControllerHost
     */
    protected WigAIExtension(final WigAIExtensionDefinition definition, final ControllerHost host) {
        super(definition, host);
    }

    /**
     * Initialize the extension.
     * This is called when the extension is enabled in Bitwig Studio.
     */
    @Override
    public void init() {
        // Initialize AppConstants version FIRST
        // AppConstants.initVersion(); // Removed call to initVersion

        final ControllerHost host = getHost();

        // Initialize the logger
        logger = new Logger(host);

        // Initialize the config manager with Bitwig preferences integration
        configManager = new PreferencesBackedConfigManager(logger, host);

        // Initialize and start the MCP server
        mcpServerManager = new McpServerManager(logger, configManager, (WigAIExtensionDefinition)getExtensionDefinition(), host);

        // Register MCP server as a configuration change observer
        configManager.addObserver(mcpServerManager);

        // Start the MCP server
        mcpServerManager.start();

        // Log startup message
        logger.info(String.format("WigAI Extension Loaded - Version %s", getExtensionDefinition().getVersion()));
    }

    /**
     * Clean up when the extension is closed.
     * This is called when the extension is disabled in Bitwig Studio or when Bitwig
     * Studio is closed.
     */
    @Override
    public void exit() {
        if (logger != null) {
            logger.info("WigAI Extension shutting down");
        }

        // Stop the MCP server if it exists
        if (mcpServerManager != null) {
            mcpServerManager.stop();
        }
    }

    /**
     * Called when GUI updates should be performed.
     */
    @Override
    public void flush() {
        // No GUI updates needed for now
    }
}
