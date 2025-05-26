package io.github.fabb.wigai.bitwig;

import com.bitwig.extension.controller.api.*;
import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.common.data.ParameterInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Facade for Bitwig API interactions.
 * This class abstracts the Bitwig API and provides simplified methods for common operations.
 */
public class BitwigApiFacade {
    private final ControllerHost host;
    private final Transport transport;
    private final Logger logger;
    private final CursorDevice cursorDevice;
    private final RemoteControlsPage deviceParameterBank;

    /**
     * Creates a new BitwigApiFacade instance.
     *
     * @param host   The Bitwig ControllerHost
     * @param logger The logger for logging operations
     */
    public BitwigApiFacade(ControllerHost host, Logger logger) {
        this.host = host;
        this.transport = host.createTransport();
        this.logger = logger;

        // Initialize device control - use CursorTrack.createCursorDevice() instead of deprecated host.createCursorDevice()
        CursorTrack cursorTrack = host.createCursorTrack(0, 0);
        this.cursorDevice = cursorTrack.createCursorDevice();
        this.deviceParameterBank = cursorDevice.createCursorRemoteControlsPage(8);

        // Mark interest in device properties to enable value access
        cursorDevice.exists().markInterested();
        cursorDevice.name().markInterested();

        // Mark interest in all parameter properties to enable value access
        for (int i = 0; i < 8; i++) {
            RemoteControl parameter = deviceParameterBank.getParameter(i);
            parameter.name().markInterested();
            parameter.value().markInterested();
            parameter.displayedValue().markInterested();
        }
    }

    /**
     * Starts the transport playback.
     */
    public void startTransport() {
        logger.info("BitwigApiFacade: Starting transport playback");
        transport.play();
    }

    /**
     * Stops the transport playback.
     */
    public void stopTransport() {
        logger.info("BitwigApiFacade: Stopping transport playback");
        transport.stop();
    }

    /**
     * Get the ControllerHost instance.
     *
     * @return The ControllerHost
     */
    public ControllerHost getHost() {
        return host;
    }

    /**
     * Checks if a device is currently selected.
     *
     * @return true if a device is selected, false otherwise
     */
    public boolean isDeviceSelected() {
        logger.info("BitwigApiFacade: Checking if device is selected");
        return cursorDevice.exists().get();
    }

    /**
     * Gets the name of the currently selected device.
     *
     * @return The device name or null if no device is selected
     */
    public String getSelectedDeviceName() {
        logger.info("BitwigApiFacade: Getting selected device name");
        if (!isDeviceSelected()) {
            return null;
        }
        return cursorDevice.name().get();
    }

    /**
     * Gets the parameters of the currently selected device.
     *
     * @return A list of ParameterInfo objects representing the 8 addressable parameters
     */
    public List<ParameterInfo> getSelectedDeviceParameters() {
        logger.info("BitwigApiFacade: Getting selected device parameters");
        List<ParameterInfo> parameters = new ArrayList<>();

        if (!isDeviceSelected()) {
            logger.info("BitwigApiFacade: No device selected, returning empty parameters list");
            return parameters;
        }

        for (int i = 0; i < 8; i++) {
            RemoteControl parameter = deviceParameterBank.getParameter(i);
            String name = parameter.name().get();
            double value = parameter.value().get();
            String displayValue = parameter.displayedValue().get();

            // Handle null or empty names
            if (name != null && name.trim().isEmpty()) {
                name = null;
            }

            parameters.add(new ParameterInfo(i, name, value, displayValue));
        }

        logger.info("BitwigApiFacade: Retrieved " + parameters.size() + " parameters");
        return parameters;
    }

    /**
     * Sets the value of a specific parameter for the currently selected device.
     *
     * @param parameterIndex The index of the parameter to set (0-7)
     * @param value          The value to set (0.0-1.0)
     * @throws IllegalArgumentException if parameterIndex is out of range or value is out of range
     * @throws RuntimeException        if no device is selected or Bitwig API error occurs
     */
    public void setSelectedDeviceParameter(int parameterIndex, double value) {
        logger.info("BitwigApiFacade: Setting parameter " + parameterIndex + " to " + value);

        // Validate parameter index
        if (parameterIndex < 0 || parameterIndex > 7) {
            String errorMsg = "Parameter index must be between 0-7, got: " + parameterIndex;
            logger.error("BitwigApiFacade: " + errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        // Validate value range
        if (value < 0.0 || value > 1.0) {
            String errorMsg = "Parameter value must be between 0.0-1.0, got: " + value;
            logger.error("BitwigApiFacade: " + errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        // Check if device is selected
        if (!isDeviceSelected()) {
            String errorMsg = "No device is currently selected";
            logger.error("BitwigApiFacade: " + errorMsg);
            throw new RuntimeException(errorMsg);
        }

        try {
            RemoteControl parameter = deviceParameterBank.getParameter(parameterIndex);
            parameter.value().set(value);
            logger.info("BitwigApiFacade: Successfully set parameter " + parameterIndex + " to " + value);
        } catch (Exception e) {
            String errorMsg = "Failed to set parameter " + parameterIndex + ": " + e.getMessage();
            logger.error("BitwigApiFacade: " + errorMsg);
            throw new RuntimeException(errorMsg, e);
        }
    }
}
