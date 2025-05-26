package io.github.fabb.wigai.features;

import io.github.fabb.wigai.bitwig.BitwigApiFacade;
import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.common.data.ParameterInfo;

import java.util.List;

/**
 * Controller class for device parameter control features.
 * Bridges between MCP tools and Bitwig API operations for device parameter control.
 */
public class DeviceController {
    private final BitwigApiFacade bitwigApiFacade;
    private final Logger logger;

    /**
     * Creates a new DeviceController instance.
     *
     * @param bitwigApiFacade The facade for Bitwig API interactions
     * @param logger          The logger for logging operations
     */
    public DeviceController(BitwigApiFacade bitwigApiFacade, Logger logger) {
        this.bitwigApiFacade = bitwigApiFacade;
        this.logger = logger;
    }

    /**
     * Gets the parameters of the currently selected device.
     *
     * @return A DeviceParametersResult containing the device name and parameters
     */
    public DeviceParametersResult getSelectedDeviceParameters() {
        try {
            logger.info("DeviceController: Getting selected device parameters");

            String deviceName = bitwigApiFacade.getSelectedDeviceName();
            List<ParameterInfo> parameters = bitwigApiFacade.getSelectedDeviceParameters();

            logger.info("DeviceController: Retrieved device '" + deviceName + "' with " + parameters.size() + " parameters");

            return new DeviceParametersResult(deviceName, parameters);

        } catch (Exception e) {
            logger.error("DeviceController: Error getting selected device parameters: " + e.getMessage());
            throw new RuntimeException("Failed to get selected device parameters", e);
        }
    }

    /**
     * Sets a specific parameter value for the currently selected device.
     *
     * @param parameterIndex The index of the parameter to set (0-7)
     * @param value          The value to set (0.0-1.0)
     * @throws IllegalArgumentException if parameterIndex is out of range or value is out of range
     * @throws RuntimeException        if no device is selected or Bitwig API error occurs
     */
    public void setSelectedDeviceParameter(int parameterIndex, double value) {
        try {
            logger.info("DeviceController: Setting parameter " + parameterIndex + " to " + value);

            // Use BitwigApiFacade to perform the actual parameter setting
            // This will handle all validation (parameter index, value range, device selection)
            bitwigApiFacade.setSelectedDeviceParameter(parameterIndex, value);

            logger.info("DeviceController: Successfully set parameter " + parameterIndex + " to " + value);

        } catch (IllegalArgumentException e) {
            logger.error("DeviceController: Validation error setting parameter " + parameterIndex + ": " + e.getMessage());
            throw e; // Re-throw validation errors as-is
        } catch (Exception e) {
            logger.error("DeviceController: Error setting parameter " + parameterIndex + ": " + e.getMessage());
            throw new RuntimeException("Failed to set device parameter", e);
        }
    }

    /**
     * Result record for device parameter queries.
     */
    public record DeviceParametersResult(
        String deviceName,           // Nullable
        List<ParameterInfo> parameters
    ) {}
}
