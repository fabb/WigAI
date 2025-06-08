package io.github.fabb.wigai.common.validation;

import io.github.fabb.wigai.common.error.BitwigApiException;
import io.github.fabb.wigai.common.error.ErrorCode;

import java.util.Map;
import java.util.function.Predicate;

/**
 * Reusable input validation framework for WigAI.
 * Provides consistent validation with clear error messages and proper error classification.
 */
public class ParameterValidator {

    /**
     * Validates that a required parameter exists in the arguments map.
     *
     * @param arguments The arguments map
     * @param parameterName The name of the required parameter
     * @param operation The operation context for error reporting
     * @return The parameter value
     * @throws BitwigApiException if the parameter is missing
     */
    public static Object validateRequired(Map<String, Object> arguments, String parameterName, String operation) {
        Object value = arguments.get(parameterName);
        if (value == null) {
            throw new BitwigApiException(
                ErrorCode.MISSING_REQUIRED_PARAMETER,
                operation,
                "Missing required parameter: " + parameterName
            );
        }
        return value;
    }

    /**
     * Validates that a parameter is of the expected type.
     *
     * @param <T> The expected type
     * @param value The parameter value
     * @param expectedType The expected class type
     * @param parameterName The parameter name for error reporting
     * @param operation The operation context for error reporting
     * @return The typed parameter value
     * @throws BitwigApiException if the parameter is not of the expected type
     */
    @SuppressWarnings("unchecked")
    public static <T> T validateType(Object value, Class<T> expectedType, String parameterName, String operation) {
        if (!expectedType.isInstance(value)) {
            throw new BitwigApiException(
                ErrorCode.INVALID_PARAMETER_TYPE,
                operation,
                parameterName + " must be a " + expectedType.getSimpleName().toLowerCase(),
                Map.of("parameter", parameterName, "expected_type", expectedType.getSimpleName(), "actual_type", value.getClass().getSimpleName())
            );
        }
        return (T) value;
    }

    /**
     * Validates and extracts a required string parameter.
     *
     * @param arguments The arguments map
     * @param parameterName The parameter name
     * @param operation The operation context
     * @return The string value
     * @throws BitwigApiException if the parameter is missing or not a string
     */
    public static String validateRequiredString(Map<String, Object> arguments, String parameterName, String operation) {
        Object value = validateRequired(arguments, parameterName, operation);
        return validateType(value, String.class, parameterName, operation);
    }

    /**
     * Validates and extracts a required integer parameter.
     *
     * @param arguments The arguments map
     * @param parameterName The parameter name
     * @param operation The operation context
     * @return The integer value
     * @throws BitwigApiException if the parameter is missing or not a number
     */
    public static int validateRequiredInteger(Map<String, Object> arguments, String parameterName, String operation) {
        Object value = validateRequired(arguments, parameterName, operation);
        if (!(value instanceof Number)) {
            throw new BitwigApiException(
                ErrorCode.INVALID_PARAMETER_TYPE,
                operation,
                parameterName + " must be an integer",
                Map.of("parameter", parameterName, "value", value)
            );
        }
        return ((Number) value).intValue();
    }

    /**
     * Validates and extracts a required double parameter.
     *
     * @param arguments The arguments map
     * @param parameterName The parameter name
     * @param operation The operation context
     * @return The double value
     * @throws BitwigApiException if the parameter is missing or not a number
     */
    public static double validateRequiredDouble(Map<String, Object> arguments, String parameterName, String operation) {
        Object value = validateRequired(arguments, parameterName, operation);
        if (!(value instanceof Number)) {
            throw new BitwigApiException(
                ErrorCode.INVALID_PARAMETER_TYPE,
                operation,
                parameterName + " must be a number",
                Map.of("parameter", parameterName, "value", value)
            );
        }
        return ((Number) value).doubleValue();
    }

    /**
     * Validates that a string parameter is not empty.
     *
     * @param value The string value to validate
     * @param parameterName The parameter name for error reporting
     * @param operation The operation context
     * @return The validated string
     * @throws BitwigApiException if the string is empty or only whitespace
     */
    public static String validateNotEmpty(String value, String parameterName, String operation) {
        if (value == null || value.trim().isEmpty()) {
            throw new BitwigApiException(
                ErrorCode.EMPTY_PARAMETER,
                operation,
                parameterName + " cannot be empty",
                Map.of("parameter", parameterName)
            );
        }
        return value.trim();
    }

    /**
     * Validates that an integer parameter is within a specified range.
     *
     * @param value The integer value to validate
     * @param min The minimum allowed value (inclusive)
     * @param max The maximum allowed value (inclusive)
     * @param parameterName The parameter name for error reporting
     * @param operation The operation context
     * @return The validated value
     * @throws BitwigApiException if the value is outside the valid range
     */
    public static int validateRange(int value, int min, int max, String parameterName, String operation) {
        if (value < min || value > max) {
            throw new BitwigApiException(
                ErrorCode.INVALID_RANGE,
                operation,
                parameterName + " must be between " + min + " and " + max + ", got: " + value,
                Map.of("parameter", parameterName, "value", value, "min", min, "max", max)
            );
        }
        return value;
    }

    /**
     * Validates that a double parameter is within a specified range.
     *
     * @param value The double value to validate
     * @param min The minimum allowed value (inclusive)
     * @param max The maximum allowed value (inclusive)
     * @param parameterName The parameter name for error reporting
     * @param operation The operation context
     * @return The validated value
     * @throws BitwigApiException if the value is outside the valid range
     */
    public static double validateRange(double value, double min, double max, String parameterName, String operation) {
        if (value < min || value > max) {
            throw new BitwigApiException(
                ErrorCode.INVALID_RANGE,
                operation,
                parameterName + " must be between " + min + " and " + max + ", got: " + value,
                Map.of("parameter", parameterName, "value", value, "min", min, "max", max)
            );
        }
        return value;
    }

    /**
     * Validates that a parameter index is within the valid range for device parameters.
     *
     * @param parameterIndex The parameter index to validate
     * @param operation The operation context
     * @return The validated parameter index
     * @throws BitwigApiException if the index is outside the valid range (0-7)
     */
    public static int validateParameterIndex(int parameterIndex, String operation) {
        return validateRange(parameterIndex, 0, 7, "parameter_index", operation);
    }

    /**
     * Validates that a parameter value is within the valid range for device parameters.
     *
     * @param parameterValue The parameter value to validate
     * @param operation The operation context
     * @return The validated parameter value
     * @throws BitwigApiException if the value is outside the valid range (0.0-1.0)
     */
    public static double validateParameterValue(double parameterValue, String operation) {
        return validateRange(parameterValue, 0.0, 1.0, "value", operation);
    }

    /**
     * Validates that a clip index is non-negative.
     *
     * @param clipIndex The clip index to validate
     * @param operation The operation context
     * @return The validated clip index
     * @throws BitwigApiException if the index is negative
     */
    public static int validateClipIndex(int clipIndex, String operation) {
        if (clipIndex < 0) {
            throw new BitwigApiException(
                ErrorCode.INVALID_RANGE,
                operation,
                "clip_index must be non-negative, got: " + clipIndex,
                Map.of("parameter", "clip_index", "value", clipIndex)
            );
        }
        return clipIndex;
    }

    /**
     * Validates that a scene index is non-negative.
     *
     * @param sceneIndex The scene index to validate
     * @param operation The operation context
     * @return The validated scene index
     * @throws BitwigApiException if the index is negative
     */
    public static int validateSceneIndex(int sceneIndex, String operation) {
        if (sceneIndex < 0) {
            throw new BitwigApiException(
                ErrorCode.INVALID_RANGE,
                operation,
                "scene_index must be non-negative, got: " + sceneIndex,
                Map.of("parameter", "scene_index", "value", sceneIndex)
            );
        }
        return sceneIndex;
    }

    /**
     * Validates a parameter against a custom predicate.
     *
     * @param <T> The parameter type
     * @param value The value to validate
     * @param predicate The validation predicate
     * @param parameterName The parameter name for error reporting
     * @param operation The operation context
     * @param errorMessage The error message if validation fails
     * @return The validated value
     * @throws BitwigApiException if validation fails
     */
    public static <T> T validateCustom(T value, Predicate<T> predicate, String parameterName, String operation, String errorMessage) {
        if (!predicate.test(value)) {
            throw new BitwigApiException(
                ErrorCode.INVALID_PARAMETER,
                operation,
                errorMessage,
                Map.of("parameter", parameterName, "value", value)
            );
        }
        return value;
    }

    /**
     * Builder pattern for complex validation chains.
     */
    public static class ValidationBuilder<T> {
        private T value;
        private String parameterName;
        private String operation;

        private ValidationBuilder(T value, String parameterName, String operation) {
            this.value = value;
            this.parameterName = parameterName;
            this.operation = operation;
        }

        public static <T> ValidationBuilder<T> of(T value, String parameterName, String operation) {
            return new ValidationBuilder<>(value, parameterName, operation);
        }

        public ValidationBuilder<T> notNull() {
            if (value == null) {
                throw new BitwigApiException(
                    ErrorCode.MISSING_REQUIRED_PARAMETER,
                    operation,
                    "Missing required parameter: " + parameterName
                );
            }
            return this;
        }

        public ValidationBuilder<T> custom(Predicate<T> predicate, String errorMessage) {
            if (!predicate.test(value)) {
                throw new BitwigApiException(
                    ErrorCode.INVALID_PARAMETER,
                    operation,
                    errorMessage,
                    Map.of("parameter", parameterName, "value", value)
                );
            }
            return this;
        }

        public T get() {
            return value;
        }
    }
}
