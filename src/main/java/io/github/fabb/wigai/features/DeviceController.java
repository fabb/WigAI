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
     * Result record for device parameter queries.
     */
    public record DeviceParametersResult(
        String deviceName,           // Nullable
        List<ParameterInfo> parameters
    ) {}
}
