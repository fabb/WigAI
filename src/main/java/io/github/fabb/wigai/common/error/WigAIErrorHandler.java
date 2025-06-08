package io.github.fabb.wigai.common.error;

import io.github.fabb.wigai.common.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;

/**
 * Centralized error handling utility for WigAI.
 * Provides consistent error processing, logging, and response formatting across all system layers.
 */
public class WigAIErrorHandler {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    /**
     * Processes a BitwigApiException and creates a standardized error response.
     *
     * @param exception The BitwigApiException to process
     * @param logger The logger for error recording
     * @return A standardized error response map
     */
    public static Map<String, Object> handleBitwigApiException(BitwigApiException exception, Logger logger) {
        // Log the error with full context
        logStructuredError(exception, logger);

        // Create standardized response
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("code", exception.getErrorCode().getCode());
        errorDetails.put("message", exception.getMessage());
        errorDetails.put("operation", exception.getOperation());
        errorDetails.put("timestamp", getCurrentTimestamp());

        if (exception.getContext() != null) {
            errorDetails.put("context", exception.getContext());
        }

        errorResponse.put("error", errorDetails);

        return errorResponse;
    }

    /**
     * Processes a generic exception and creates a standardized error response.
     *
     * @param exception The exception to process
     * @param operation The operation that failed
     * @param logger The logger for error recording
     * @return A standardized error response map
     */
    public static Map<String, Object> handleGenericException(Exception exception, String operation, Logger logger) {
        // Convert to BitwigApiException for consistent handling
        BitwigApiException bitwigException = BitwigApiException.fromException(operation, exception);
        return handleBitwigApiException(bitwigException, logger);
    }

    /**
     * Creates a standardized success response.
     *
     * @param data The success data to include
     * @return A standardized success response map
     */
    public static Map<String, Object> createSuccessResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", data);
        return response;
    }

    /**
     * Creates a standardized error response with custom error details.
     *
     * @param errorCode The error code
     * @param message The error message
     * @param operation The operation that failed
     * @return A standardized error response map
     */
    public static Map<String, Object> createErrorResponse(ErrorCode errorCode, String message, String operation) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("code", errorCode.getCode());
        errorDetails.put("message", message);
        errorDetails.put("operation", operation);
        errorDetails.put("timestamp", getCurrentTimestamp());

        errorResponse.put("error", errorDetails);

        return errorResponse;
    }

    /**
     * Converts a response map to JSON string for MCP tool responses.
     *
     * @param response The response map to convert
     * @return JSON string representation
     */
    public static String toJsonString(Map<String, Object> response) {
        try {
            return mapper.writeValueAsString(response);
        } catch (Exception e) {
            // Fallback error response if JSON serialization fails
            return "{\"status\":\"error\",\"error\":{\"code\":\"SERIALIZATION_ERROR\",\"message\":\"Failed to serialize response\",\"timestamp\":\"" + getCurrentTimestamp() + "\"}}";
        }
    }

    /**
     * Creates a JSON error response string directly.
     *
     * @param errorCode The error code
     * @param message The error message
     * @param operation The operation that failed
     * @return JSON string error response
     */
    public static String createJsonErrorResponse(ErrorCode errorCode, String message, String operation) {
        Map<String, Object> response = createErrorResponse(errorCode, message, operation);
        return toJsonString(response);
    }

    /**
     * Creates a JSON success response string directly.
     *
     * @param data The success data
     * @return JSON string success response
     */
    public static String createJsonSuccessResponse(Object data) {
        Map<String, Object> response = createSuccessResponse(data);
        return toJsonString(response);
    }

    /**
     * Determines if an exception should be considered a client error (4xx-style) or server error (5xx-style).
     *
     * @param errorCode The error code to classify
     * @return true if it's a client error, false if it's a server error
     */
    public static boolean isClientError(ErrorCode errorCode) {
        return switch (errorCode) {
            case INVALID_PARAMETER, INVALID_PARAMETER_INDEX, MISSING_REQUIRED_PARAMETER,
                 INVALID_PARAMETER_TYPE, INVALID_RANGE, EMPTY_PARAMETER,
                 DEVICE_NOT_SELECTED, TRACK_NOT_FOUND, SCENE_NOT_FOUND, CLIP_NOT_FOUND,
                 MCP_PARSING_ERROR -> true;
            default -> false;
        };
    }

    /**
     * Logs a structured error with full context information.
     *
     * @param exception The BitwigApiException to log
     * @param logger The logger instance
     */
    private static void logStructuredError(BitwigApiException exception, Logger logger) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("WigAI Error - ");
        logMessage.append("Code: ").append(exception.getErrorCode().getCode());
        logMessage.append(", Operation: ").append(exception.getOperation());
        logMessage.append(", Message: ").append(exception.getMessage());

        if (exception.getContext() != null) {
            logMessage.append(", Context: ").append(exception.getContext());
        }

        if (exception.getCause() != null) {
            logMessage.append(", Cause: ").append(exception.getCause().getClass().getSimpleName());
            logMessage.append(" - ").append(exception.getCause().getMessage());
        }

        // Use appropriate log level based on error type
        if (isClientError(exception.getErrorCode())) {
            logger.warn(logMessage.toString());
        } else {
            logger.error(logMessage.toString());
        }

        // Log stack trace for server errors
        if (!isClientError(exception.getErrorCode()) && exception.getCause() != null) {
            logger.error("Stack trace:", exception.getCause());
        }
    }

    /**
     * Gets the current timestamp in ISO format.
     *
     * @return Current timestamp string
     */
    private static String getCurrentTimestamp() {
        return Instant.now().atOffset(ZoneOffset.UTC).format(ISO_FORMATTER);
    }

    /**
     * Wraps an operation with error handling, converting any exceptions to BitwigApiException.
     *
     * @param <T> The return type of the operation
     * @param operation The operation name for error context
     * @param task The task to execute
     * @return The result of the task
     * @throws BitwigApiException if the task fails
     */
    public static <T> T executeWithErrorHandling(String operation, SupplierWithException<T> task) throws BitwigApiException {
        try {
            return task.get();
        } catch (BitwigApiException e) {
            // Re-throw BitwigApiException as-is
            throw e;
        } catch (Exception e) {
            // Convert other exceptions to BitwigApiException
            throw BitwigApiException.fromException(operation, e);
        }
    }

    /**
     * Wraps a void operation with error handling.
     *
     * @param operation The operation name for error context
     * @param task The task to execute
     * @throws BitwigApiException if the task fails
     */
    public static void executeWithErrorHandling(String operation, RunnableWithException task) throws BitwigApiException {
        try {
            task.run();
        } catch (BitwigApiException e) {
            // Re-throw BitwigApiException as-is
            throw e;
        } catch (Exception e) {
            // Convert other exceptions to BitwigApiException
            throw BitwigApiException.fromException(operation, e);
        }
    }

    /**
     * Functional interface for operations that return a value and may throw exceptions.
     */
    @FunctionalInterface
    public interface SupplierWithException<T> {
        T get() throws Exception;
    }

    /**
     * Functional interface for operations that don't return a value and may throw exceptions.
     */
    @FunctionalInterface
    public interface RunnableWithException {
        void run() throws Exception;
    }
}
