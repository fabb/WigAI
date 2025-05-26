package io.github.fabb.wigai.features;

import io.github.fabb.wigai.bitwig.BitwigApiFacade;
import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.common.data.ParameterInfo;
import io.github.fabb.wigai.common.data.ParameterSetting;
import io.github.fabb.wigai.common.data.ParameterSettingResult;

import java.util.List;
import java.util.ArrayList;

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
     * Sets multiple parameter values for the currently selected device.
     * Processes each parameter independently, returning structured results for each.
     * Supports partial success - some parameters may succeed while others fail.
     *
     * @param parameters List of parameter settings to apply
     * @return List of results indicating success/failure for each parameter
     * @throws RuntimeException if no device is selected (top-level error)
     */
    public List<ParameterSettingResult> setMultipleSelectedDeviceParameters(List<ParameterSetting> parameters) {
        logger.info("DeviceController: Setting " + parameters.size() + " parameters");

        // First, validate device selection (top-level validation)
        try {
            bitwigApiFacade.getSelectedDeviceName(); // This will throw if no device selected
        } catch (Exception e) {
            logger.error("DeviceController: No device selected for batch parameter setting");
            throw new RuntimeException("No device is currently selected", e);
        }

        List<ParameterSettingResult> results = new ArrayList<>();

        for (ParameterSetting param : parameters) {
            try {
                logger.info("DeviceController: Processing parameter " + param.parameter_index() + " = " + param.value());

                // Use existing single parameter setting method which handles validation
                bitwigApiFacade.setSelectedDeviceParameter(param.parameter_index(), param.value());

                // Create success result
                ParameterSettingResult result = new ParameterSettingResult(
                    param.parameter_index(),
                    "success",
                    param.value(),
                    null,
                    null
                );
                results.add(result);

                logger.info("DeviceController: Successfully set parameter " + param.parameter_index() + " to " + param.value());

            } catch (IllegalArgumentException e) {
                // Parameter validation failed
                String errorCode;
                if (e.getMessage().contains("Parameter index must be between 0-7")) {
                    errorCode = "INVALID_PARAMETER_INDEX";
                } else if (e.getMessage().contains("Parameter value must be between 0.0-1.0")) {
                    errorCode = "INVALID_PARAMETER";
                } else {
                    errorCode = "INVALID_PARAMETER";
                }

                ParameterSettingResult result = new ParameterSettingResult(
                    param.parameter_index(),
                    "error",
                    null,
                    errorCode,
                    e.getMessage()
                );
                results.add(result);

                logger.error("DeviceController: Validation error for parameter " + param.parameter_index() + ": " + e.getMessage());

            } catch (Exception e) {
                // Other errors (likely Bitwig API errors)
                ParameterSettingResult result = new ParameterSettingResult(
                    param.parameter_index(),
                    "error",
                    null,
                    "BITWIG_ERROR",
                    "Error setting parameter: " + e.getMessage()
                );
                results.add(result);

                logger.error("DeviceController: Error setting parameter " + param.parameter_index() + ": " + e.getMessage());
            }
        }

        long successCount = results.stream().filter(r -> "success".equals(r.status())).count();
        long errorCount = results.size() - successCount;
        logger.info("DeviceController: Batch operation completed - " + successCount + " succeeded, " + errorCount + " failed");

        return results;
    }

    /**
     * Result record for device parameter queries.
     */
    public record DeviceParametersResult(
        String deviceName,           // Nullable
        List<ParameterInfo> parameters
    ) {}
}
