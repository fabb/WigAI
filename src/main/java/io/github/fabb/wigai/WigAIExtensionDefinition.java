package io.github.fabb.wigai;

import com.bitwig.extension.api.PlatformType;
import com.bitwig.extension.controller.ControllerExtensionDefinition;
import com.bitwig.extension.controller.api.ControllerHost;
import io.github.fabb.wigai.common.AppConstants;

import java.util.UUID;

/**
 * Definition class for the WigAI extension.
 * This defines metadata such as name, author, version, etc.
 */
public class WigAIExtensionDefinition extends ControllerExtensionDefinition {
    private static final UUID DRIVER_ID = UUID.fromString("f379badd-ba64-4234-90e9-5fd6bfe8c7c3");

    @Override
    public String getName() {
        return AppConstants.APP_NAME;
    }

    @Override
    public String getAuthor() {
        return AppConstants.APP_AUTHOR;
    }

    @Override
    public String getVersion() {
        return AppConstants.APP_VERSION;
    }

    @Override
    public UUID getId() {
        return DRIVER_ID;
    }

    @Override
    public String getHardwareVendor() {
        return "fabb";
    }

    @Override
    public String getHardwareModel() {
        return "WigAI MCP Server";
    }

    @Override
    public int getRequiredAPIVersion() {
        return 19;
    }

    @Override
    public int getNumMidiInPorts() {
        return 0;
    }

    @Override
    public int getNumMidiOutPorts() {
        return 0;
    }

    @Override
    public String getHelpFilePath() {
        return "";
    }

    // This method may not be part of the current API version
    public boolean isUsingBitwigMidiAPI() {
        return false;
    }

    @Override
    public void listAutoDetectionMidiPortNames(
            final com.bitwig.extension.controller.AutoDetectionMidiPortNamesList list,
            final PlatformType platformType) {
        // No MIDI auto-detection needed
    }

    @Override
    public WigAIExtension createInstance(final ControllerHost host) {
        return new WigAIExtension(this, host);
    }
}
