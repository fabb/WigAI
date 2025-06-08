package io.github.fabb.wigai.common.error;

/**
 * Structured exception class for Bitwig API operations.
 * Provides consistent error information with context for debugging and client responses.
 */
public class BitwigApiException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String operation;
    private final Object context;

    /**
     * Creates a BitwigApiException with error code and operation context.
     *
     * @param errorCode The error classification
     * @param operation The operation that failed
     */
    public BitwigApiException(ErrorCode errorCode, String operation) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.operation = operation;
        this.context = null;
    }

    /**
     * Creates a BitwigApiException with error code, operation context, and custom message.
     *
     * @param errorCode The error classification
     * @param operation The operation that failed
     * @param message Custom error message
     */
    public BitwigApiException(ErrorCode errorCode, String operation, String message) {
        super(message);
        this.errorCode = errorCode;
        this.operation = operation;
        this.context = null;
    }

    /**
     * Creates a BitwigApiException with error code, operation context, and root cause.
     *
     * @param errorCode The error classification
     * @param operation The operation that failed
     * @param cause The underlying exception that caused this error
     */
    public BitwigApiException(ErrorCode errorCode, String operation, Throwable cause) {
        super(errorCode.getDefaultMessage(), cause);
        this.errorCode = errorCode;
        this.operation = operation;
        this.context = null;
    }

    /**
     * Creates a BitwigApiException with error code, operation context, custom message, and root cause.
     *
     * @param errorCode The error classification
     * @param operation The operation that failed
     * @param message Custom error message
     * @param cause The underlying exception that caused this error
     */
    public BitwigApiException(ErrorCode errorCode, String operation, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.operation = operation;
        this.context = null;
    }

    /**
     * Creates a BitwigApiException with error code, operation context, custom message, and additional context.
     *
     * @param errorCode The error classification
     * @param operation The operation that failed
     * @param message Custom error message
     * @param context Additional context information (e.g., parameter values, indices)
     */
    public BitwigApiException(ErrorCode errorCode, String operation, String message, Object context) {
        super(message);
        this.errorCode = errorCode;
        this.operation = operation;
        this.context = context;
    }

    /**
     * Creates a BitwigApiException with all context information.
     *
     * @param errorCode The error classification
     * @param operation The operation that failed
     * @param message Custom error message
     * @param context Additional context information
     * @param cause The underlying exception that caused this error
     */
    public BitwigApiException(ErrorCode errorCode, String operation, String message, Object context, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.operation = operation;
        this.context = context;
    }

    /**
     * Gets the error code classification.
     * @return The error code
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * Gets the operation that failed.
     * @return The operation name
     */
    public String getOperation() {
        return operation;
    }

    /**
     * Gets additional context information about the error.
     * @return The context object, or null if none provided
     */
    public Object getContext() {
        return context;
    }

    /**
     * Creates a BitwigApiException from a generic exception with operation context.
     * Analyzes the exception to determine appropriate error code.
     *
     * @param operation The operation that failed
     * @param cause The original exception
     * @return A new BitwigApiException with appropriate error classification
     */
    public static BitwigApiException fromException(String operation, Exception cause) {
        ErrorCode errorCode = ErrorCode.fromException(cause);
        return new BitwigApiException(errorCode, operation, cause.getMessage(), cause);
    }

    /**
     * Creates a BitwigApiException from a generic exception with operation context and additional context.
     *
     * @param operation The operation that failed
     * @param context Additional context information
     * @param cause The original exception
     * @return A new BitwigApiException with appropriate error classification
     */
    public static BitwigApiException fromException(String operation, Object context, Exception cause) {
        ErrorCode errorCode = ErrorCode.fromException(cause);
        return new BitwigApiException(errorCode, operation, cause.getMessage(), context, cause);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("BitwigApiException{");
        sb.append("errorCode=").append(errorCode.getCode());
        sb.append(", operation='").append(operation).append('\'');
        if (context != null) {
            sb.append(", context=").append(context);
        }
        sb.append(", message='").append(getMessage()).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
