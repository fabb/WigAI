package io.github.fabb.wigai.common.logging;

import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.common.error.ErrorCode;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Enhanced logging system with operation correlation IDs and structured error metadata.
 * Integrates with Bitwig's native logging system while providing additional structure for monitoring.
 */
public class StructuredLogger {
    private final Logger baseLogger;
    private final String component;
    private final Map<String, String> contextMetadata;
    private final AtomicLong operationIdCounter = new AtomicLong(0);

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    /**
     * Creates a new StructuredLogger instance.
     *
     * @param baseLogger The underlying Bitwig logger
     * @param component The component name for log categorization
     */
    public StructuredLogger(Logger baseLogger, String component) {
        this.baseLogger = baseLogger;
        this.component = component;
        this.contextMetadata = new ConcurrentHashMap<>();
    }

    /**
     * Gets the underlying base logger for integration with other systems.
     *
     * @return The base Logger instance
     */
    public Logger getBaseLogger() {
        return baseLogger;
    }

    /**
     * Adds persistent context metadata that will be included in all log entries.
     *
     * @param key The metadata key
     * @param value The metadata value
     */
    public void addContext(String key, String value) {
        contextMetadata.put(key, value);
    }

    /**
     * Removes context metadata.
     *
     * @param key The metadata key to remove
     */
    public void removeContext(String key) {
        contextMetadata.remove(key);
    }

    /**
     * Clears all context metadata.
     */
    public void clearContext() {
        contextMetadata.clear();
    }

    /**
     * Generates a unique operation correlation ID.
     *
     * @return A new operation ID
     */
    public String generateOperationId() {
        return component + "-" + System.currentTimeMillis() + "-" + operationIdCounter.incrementAndGet();
    }

    /**
     * Logs an info message with operation context.
     *
     * @param operationId The operation correlation ID
     * @param operation The operation name
     * @param message The log message
     */
    public void info(String operationId, String operation, String message) {
        String structuredMessage = formatMessage(LogLevel.INFO, operationId, operation, message, null, null);
        baseLogger.info(structuredMessage);
    }

    /**
     * Logs an info message without operation context.
     *
     * @param message The log message
     */
    public void info(String message) {
        info(null, null, message);
    }

    /**
     * Logs a warning message with operation context.
     *
     * @param operationId The operation correlation ID
     * @param operation The operation name
     * @param message The log message
     */
    public void warn(String operationId, String operation, String message) {
        String structuredMessage = formatMessage(LogLevel.WARN, operationId, operation, message, null, null);
        baseLogger.warn(structuredMessage);
    }

    /**
     * Logs a warning message without operation context.
     *
     * @param message The log message
     */
    public void warn(String message) {
        warn(null, null, message);
    }

    /**
     * Logs an error message with operation context.
     *
     * @param operationId The operation correlation ID
     * @param operation The operation name
     * @param message The log message
     */
    public void error(String operationId, String operation, String message) {
        String structuredMessage = formatMessage(LogLevel.ERROR, operationId, operation, message, null, null);
        baseLogger.error(structuredMessage);
    }

    /**
     * Logs an error message with exception details.
     *
     * @param operationId The operation correlation ID
     * @param operation The operation name
     * @param message The log message
     * @param throwable The exception to log
     */
    public void error(String operationId, String operation, String message, Throwable throwable) {
        String structuredMessage = formatMessage(LogLevel.ERROR, operationId, operation, message, null, throwable);
        baseLogger.error(structuredMessage, throwable);
    }

    /**
     * Logs an error message without operation context.
     *
     * @param message The log message
     */
    public void error(String message) {
        error(null, null, message);
    }

    /**
     * Logs an error message with exception and no operation context.
     *
     * @param message The log message
     * @param throwable The exception to log
     */
    public void error(String message, Throwable throwable) {
        error(null, null, message, throwable);
    }

    /**
     * Logs a structured error with error code metadata.
     *
     * @param operationId The operation correlation ID
     * @param operation The operation name
     * @param message The log message
     * @param errorCode The error code for categorization
     * @param metadata Additional error metadata
     */
    public void errorWithCode(String operationId, String operation, String message, ErrorCode errorCode, Map<String, Object> metadata) {
        String structuredMessage = formatMessage(LogLevel.ERROR, operationId, operation, message, errorCode, null);
        if (metadata != null && !metadata.isEmpty()) {
            structuredMessage += " | Metadata: " + metadata.toString();
        }
        baseLogger.error(structuredMessage);
    }

    /**
     * Logs a debug message with operation context.
     *
     * @param operationId The operation correlation ID
     * @param operation The operation name
     * @param message The log message
     */
    public void debug(String operationId, String operation, String message) {
        String structuredMessage = formatMessage(LogLevel.DEBUG, operationId, operation, message, null, null);
        baseLogger.info(structuredMessage); // Bitwig Logger doesn't have debug level, use info
    }

    /**
     * Logs a debug message without operation context.
     *
     * @param message The log message
     */
    public void debug(String message) {
        debug(null, null, message);
    }

    /**
     * Logs the start of an operation.
     *
     * @param operationId The operation correlation ID
     * @param operation The operation name
     * @param parameters Optional operation parameters
     */
    public void logOperationStart(String operationId, String operation, Map<String, Object> parameters) {
        StringBuilder message = new StringBuilder("Operation started: ").append(operation);
        if (parameters != null && !parameters.isEmpty()) {
            message.append(" | Parameters: ").append(parameters.toString());
        }
        info(operationId, operation, message.toString());
    }

    /**
     * Logs the successful completion of an operation.
     *
     * @param operationId The operation correlation ID
     * @param operation The operation name
     * @param durationMs The operation duration in milliseconds
     * @param result Optional operation result
     */
    public void logOperationSuccess(String operationId, String operation, long durationMs, Object result) {
        StringBuilder message = new StringBuilder("Operation completed successfully: ").append(operation);
        message.append(" | Duration: ").append(durationMs).append("ms");
        if (result != null) {
            message.append(" | Result: ").append(result.toString());
        }
        info(operationId, operation, message.toString());
    }

    /**
     * Logs the failure of an operation.
     *
     * @param operationId The operation correlation ID
     * @param operation The operation name
     * @param durationMs The operation duration in milliseconds
     * @param errorCode The error code
     * @param errorMessage The error message
     */
    public void logOperationFailure(String operationId, String operation, long durationMs, ErrorCode errorCode, String errorMessage) {
        StringBuilder message = new StringBuilder("Operation failed: ").append(operation);
        message.append(" | Duration: ").append(durationMs).append("ms");
        message.append(" | Error: ").append(errorMessage);
        errorWithCode(operationId, operation, message.toString(), errorCode, null);
    }

    /**
     * Creates a timed operation logger that automatically logs start/finish.
     *
     * @param operationId The operation correlation ID
     * @param operation The operation name
     * @param parameters Optional operation parameters
     * @return A TimedOperation instance for tracking
     */
    public TimedOperation startTimedOperation(String operationId, String operation, Map<String, Object> parameters) {
        logOperationStart(operationId, operation, parameters);
        return new TimedOperation(this, operationId, operation, System.currentTimeMillis());
    }

    /**
     * Formats a log message with structured metadata.
     */
    private String formatMessage(LogLevel level, String operationId, String operation, String message, ErrorCode errorCode, Throwable throwable) {
        StringBuilder formatted = new StringBuilder();

        // Timestamp
        formatted.append("[").append(getCurrentTimestamp()).append("]");

        // Log level
        formatted.append(" [").append(level.name()).append("]");

        // Component
        formatted.append(" [").append(component).append("]");

        // Operation ID if available
        if (operationId != null) {
            formatted.append(" [").append(operationId).append("]");
        }

        // Operation name if available
        if (operation != null) {
            formatted.append(" [").append(operation).append("]");
        }

        // Error code if available
        if (errorCode != null) {
            formatted.append(" [").append(errorCode.getCode()).append("]");
        }

        // Main message
        formatted.append(" ").append(message);

        // Context metadata
        if (!contextMetadata.isEmpty()) {
            formatted.append(" | Context: ").append(contextMetadata.toString());
        }

        return formatted.toString();
    }

    private String getCurrentTimestamp() {
        return Instant.now().atOffset(ZoneOffset.UTC).format(ISO_FORMATTER);
    }

    /**
     * Log levels for structured logging.
     */
    public enum LogLevel {
        DEBUG, INFO, WARN, ERROR
    }

    /**
     * Helper class for timing operations and automatic logging.
     */
    public static class TimedOperation {
        private final StructuredLogger logger;
        private final String operationId;
        private final String operation;
        private final long startTime;

        TimedOperation(StructuredLogger logger, String operationId, String operation, long startTime) {
            this.logger = logger;
            this.operationId = operationId;
            this.operation = operation;
            this.startTime = startTime;
        }

        /**
         * Logs successful completion of the operation.
         *
         * @param result Optional operation result
         */
        public void success(Object result) {
            long duration = System.currentTimeMillis() - startTime;
            logger.logOperationSuccess(operationId, operation, duration, result);
        }

        /**
         * Logs successful completion without result.
         */
        public void success() {
            success(null);
        }

        /**
         * Logs failure of the operation.
         *
         * @param errorCode The error code
         * @param errorMessage The error message
         */
        public void failure(ErrorCode errorCode, String errorMessage) {
            long duration = System.currentTimeMillis() - startTime;
            logger.logOperationFailure(operationId, operation, duration, errorCode, errorMessage);
        }

        /**
         * Gets the operation duration so far.
         *
         * @return Duration in milliseconds
         */
        public long getDuration() {
            return System.currentTimeMillis() - startTime;
        }

        /**
         * Gets the operation ID.
         *
         * @return The operation correlation ID
         */
        public String getOperationId() {
            return operationId;
        }

        /**
         * Gets the operation name.
         *
         * @return The operation name
         */
        public String getOperation() {
            return operation;
        }
    }
}
