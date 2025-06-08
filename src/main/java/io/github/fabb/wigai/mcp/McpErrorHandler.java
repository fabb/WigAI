package io.github.fabb.wigai.mcp;

import io.github.fabb.wigai.common.error.BitwigApiException;
import io.github.fabb.wigai.common.error.ErrorCode;
import io.github.fabb.wigai.common.error.WigAIErrorHandler;
import io.github.fabb.wigai.common.logging.StructuredLogger;
import io.modelcontextprotocol.spec.McpSchema;

import java.util.List;
import java.util.Map;

/**
 * Centralized MCP error handling utility for consistent tool response formatting.
 * Ensures all MCP tools return standardized JSON response format with proper error handling.
 */
public class McpErrorHandler {

    /**
     * Creates a standardized MCP success response.
     *
     * @param data The success data to include in the response
     * @return A McpSchema.CallToolResult with success response
     */
    public static McpSchema.CallToolResult createSuccessResponse(Object data) {
        Map<String, Object> response = WigAIErrorHandler.createSuccessResponse(data);
        String jsonResponse = WigAIErrorHandler.toJsonString(response);
        McpSchema.TextContent textContent = new McpSchema.TextContent(jsonResponse);
        return new McpSchema.CallToolResult(List.of(textContent), false);
    }

    /**
     * Creates a standardized MCP error response from a BitwigApiException.
     *
     * @param exception The BitwigApiException to convert
     * @param logger The structured logger for error recording
     * @return A McpSchema.CallToolResult with error response
     */
    public static McpSchema.CallToolResult createErrorResponse(BitwigApiException exception, StructuredLogger logger) {
        Map<String, Object> response = WigAIErrorHandler.handleBitwigApiException(exception, logger.getBaseLogger());
        String jsonResponse = WigAIErrorHandler.toJsonString(response);
        McpSchema.TextContent textContent = new McpSchema.TextContent(jsonResponse);
        return new McpSchema.CallToolResult(List.of(textContent), true);
    }

    /**
     * Creates a standardized MCP error response from a generic exception.
     *
     * @param exception The exception to convert
     * @param operation The operation that failed
     * @param logger The structured logger for error recording
     * @return A McpSchema.CallToolResult with error response
     */
    public static McpSchema.CallToolResult createErrorResponse(Exception exception, String operation, StructuredLogger logger) {
        BitwigApiException bitwigException = BitwigApiException.fromException(operation, exception);
        return createErrorResponse(bitwigException, logger);
    }

    /**
     * Creates a standardized MCP error response with custom error details.
     *
     * @param errorCode The error code
     * @param message The error message
     * @param operation The operation that failed
     * @return A McpSchema.CallToolResult with error response
     */
    public static McpSchema.CallToolResult createErrorResponse(ErrorCode errorCode, String message, String operation) {
        Map<String, Object> response = WigAIErrorHandler.createErrorResponse(errorCode, message, operation);
        String jsonResponse = WigAIErrorHandler.toJsonString(response);
        McpSchema.TextContent textContent = new McpSchema.TextContent(jsonResponse);
        return new McpSchema.CallToolResult(List.of(textContent), true);
    }

    /**
     * Executes a tool operation with standardized error handling and response formatting.
     *
     * @param operation The operation name for error context
     * @param logger The structured logger
     * @param task The tool operation to execute
     * @return A McpSchema.CallToolResult with success or error response
     */
    public static McpSchema.CallToolResult executeWithErrorHandling(String operation, StructuredLogger logger, ToolOperation task) {
        String operationId = logger.generateOperationId();
        StructuredLogger.TimedOperation timedOperation = logger.startTimedOperation(operationId, operation, null);

        try {
            Object result = task.execute();
            timedOperation.success(result);
            return createSuccessResponse(result);
        } catch (BitwigApiException e) {
            timedOperation.failure(e.getErrorCode(), e.getMessage());
            return createErrorResponse(e, logger);
        } catch (Exception e) {
            ErrorCode errorCode = ErrorCode.fromException(e);
            timedOperation.failure(errorCode, e.getMessage());
            return createErrorResponse(e, operation, logger);
        }
    }

    /**
     * Executes a tool operation with parameter validation and standardized error handling.
     *
     * @param operation The operation name for error context
     * @param arguments The tool arguments to validate
     * @param logger The structured logger
     * @param validator The parameter validation function
     * @param task The tool operation to execute with validated parameters
     * @return A McpSchema.CallToolResult with success or error response
     */
    public static <T> McpSchema.CallToolResult executeWithValidation(
            String operation,
            Map<String, Object> arguments,
            StructuredLogger logger,
            ParameterValidator<T> validator,
            ToolOperationWithParams<T> task) {

        String operationId = logger.generateOperationId();
        StructuredLogger.TimedOperation timedOperation = logger.startTimedOperation(operationId, operation, arguments);

        try {
            // Validate parameters
            T validatedParams = validator.validate(arguments, operation);

            // Execute operation with validated parameters
            Object result = task.execute(validatedParams);

            timedOperation.success(result);
            return createSuccessResponse(result);
        } catch (BitwigApiException e) {
            timedOperation.failure(e.getErrorCode(), e.getMessage());
            return createErrorResponse(e, logger);
        } catch (Exception e) {
            ErrorCode errorCode = ErrorCode.fromException(e);
            timedOperation.failure(errorCode, e.getMessage());
            return createErrorResponse(e, operation, logger);
        }
    }

    /**
     * Creates a legacy compatibility response for tools that need to maintain backward compatibility.
     * This method should be used sparingly and only during transition periods.
     *
     * @param success Whether the operation was successful
     * @param message The response message
     * @param isError Whether this is an error response
     * @return A McpSchema.CallToolResult in legacy format
     */
    @Deprecated
    public static McpSchema.CallToolResult createLegacyResponse(boolean success, String message, boolean isError) {
        McpSchema.TextContent textContent = new McpSchema.TextContent(message);
        return new McpSchema.CallToolResult(List.of(textContent), isError);
    }

    /**
     * Converts a legacy error response to the new standardized format.
     *
     * @param errorMessage The legacy error message
     * @param operation The operation that failed
     * @return A standardized error response
     */
    public static McpSchema.CallToolResult upgradeLegacyErrorResponse(String errorMessage, String operation) {
        ErrorCode errorCode = ErrorCode.UNKNOWN_ERROR;

        // Try to determine error code from message
        if (errorMessage.toLowerCase().contains("not found")) {
            if (errorMessage.toLowerCase().contains("track")) {
                errorCode = ErrorCode.TRACK_NOT_FOUND;
            } else if (errorMessage.toLowerCase().contains("scene")) {
                errorCode = ErrorCode.SCENE_NOT_FOUND;
            } else if (errorMessage.toLowerCase().contains("clip")) {
                errorCode = ErrorCode.CLIP_NOT_FOUND;
            }
        } else if (errorMessage.toLowerCase().contains("invalid") || errorMessage.toLowerCase().contains("must be")) {
            errorCode = ErrorCode.INVALID_PARAMETER;
        } else if (errorMessage.toLowerCase().contains("device") && errorMessage.toLowerCase().contains("selected")) {
            errorCode = ErrorCode.DEVICE_NOT_SELECTED;
        }

        return createErrorResponse(errorCode, errorMessage, operation);
    }

    /**
     * Functional interface for tool operations that return a result.
     */
    @FunctionalInterface
    public interface ToolOperation {
        Object execute() throws Exception;
    }

    /**
     * Functional interface for tool operations that take validated parameters.
     */
    @FunctionalInterface
    public interface ToolOperationWithParams<T> {
        Object execute(T validatedParams) throws Exception;
    }

    /**
     * Functional interface for parameter validation.
     */
    @FunctionalInterface
    public interface ParameterValidator<T> {
        T validate(Map<String, Object> arguments, String operation) throws BitwigApiException;
    }

    /**
     * Helper class for building complex success responses.
     */
    public static class SuccessResponseBuilder {
        private final Map<String, Object> data = new java.util.HashMap<>();

        public static SuccessResponseBuilder create() {
            return new SuccessResponseBuilder();
        }

        public SuccessResponseBuilder withAction(String action) {
            data.put("action", action);
            return this;
        }

        public SuccessResponseBuilder withMessage(String message) {
            data.put("message", message);
            return this;
        }

        public SuccessResponseBuilder withData(String key, Object value) {
            data.put(key, value);
            return this;
        }

        public SuccessResponseBuilder withData(Map<String, Object> additionalData) {
            data.putAll(additionalData);
            return this;
        }

        public McpSchema.CallToolResult build() {
            return createSuccessResponse(data);
        }
    }
}
