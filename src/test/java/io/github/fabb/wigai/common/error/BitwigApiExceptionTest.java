package io.github.fabb.wigai.common.error;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BitwigApiException.
 */
class BitwigApiExceptionTest {

    @Test
    void testBasicConstructor() {
        BitwigApiException exception = new BitwigApiException(
            ErrorCode.DEVICE_NOT_SELECTED,
            "testOperation"
        );

        assertEquals(ErrorCode.DEVICE_NOT_SELECTED, exception.getErrorCode());
        assertEquals("testOperation", exception.getOperation());
        assertEquals("No device is currently selected", exception.getMessage());
        assertNull(exception.getContext());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithCustomMessage() {
        BitwigApiException exception = new BitwigApiException(
            ErrorCode.INVALID_PARAMETER,
            "testOperation",
            "Custom error message"
        );

        assertEquals(ErrorCode.INVALID_PARAMETER, exception.getErrorCode());
        assertEquals("testOperation", exception.getOperation());
        assertEquals("Custom error message", exception.getMessage());
        assertNull(exception.getContext());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithCause() {
        Exception cause = new RuntimeException("Original cause");
        BitwigApiException exception = new BitwigApiException(
            ErrorCode.BITWIG_API_ERROR,
            "testOperation",
            cause
        );

        assertEquals(ErrorCode.BITWIG_API_ERROR, exception.getErrorCode());
        assertEquals("testOperation", exception.getOperation());
        assertEquals("Bitwig API operation failed", exception.getMessage());
        assertNull(exception.getContext());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        Exception cause = new RuntimeException("Original cause");
        BitwigApiException exception = new BitwigApiException(
            ErrorCode.INTERNAL_ERROR,
            "testOperation",
            "Custom message",
            cause
        );

        assertEquals(ErrorCode.INTERNAL_ERROR, exception.getErrorCode());
        assertEquals("testOperation", exception.getOperation());
        assertEquals("Custom message", exception.getMessage());
        assertNull(exception.getContext());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testConstructorWithContext() {
        Object context = java.util.Map.of("parameter", "test", "value", 42);
        BitwigApiException exception = new BitwigApiException(
            ErrorCode.INVALID_RANGE,
            "testOperation",
            "Value out of range",
            context
        );

        assertEquals(ErrorCode.INVALID_RANGE, exception.getErrorCode());
        assertEquals("testOperation", exception.getOperation());
        assertEquals("Value out of range", exception.getMessage());
        assertEquals(context, exception.getContext());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithAllParameters() {
        Exception cause = new RuntimeException("Original cause");
        Object context = java.util.Map.of("parameter", "test", "value", 42);
        BitwigApiException exception = new BitwigApiException(
            ErrorCode.TRACK_NOT_FOUND,
            "testOperation",
            "Track not found message",
            context,
            cause
        );

        assertEquals(ErrorCode.TRACK_NOT_FOUND, exception.getErrorCode());
        assertEquals("testOperation", exception.getOperation());
        assertEquals("Track not found message", exception.getMessage());
        assertEquals(context, exception.getContext());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testFromExceptionWithGenericException() {
        Exception originalException = new RuntimeException("Something went wrong");
        BitwigApiException result = BitwigApiException.fromException("testOp", originalException);

        assertEquals(ErrorCode.OPERATION_FAILED, result.getErrorCode());
        assertEquals("testOp", result.getOperation());
        assertEquals("Something went wrong", result.getMessage());
        assertEquals(originalException, result.getCause());
    }

    @Test
    void testFromExceptionWithIllegalArgumentException() {
        IllegalArgumentException originalException = new IllegalArgumentException("Parameter index must be between 0-7");
        BitwigApiException result = BitwigApiException.fromException("testOp", originalException);

        assertEquals(ErrorCode.INVALID_PARAMETER, result.getErrorCode());
        assertEquals("testOp", result.getOperation());
        assertEquals("Parameter index must be between 0-7", result.getMessage());
        assertEquals(originalException, result.getCause());
    }

    @Test
    void testFromExceptionWithContext() {
        Object context = java.util.Map.of("param", "value");
        Exception originalException = new RuntimeException("Error occurred");
        BitwigApiException result = BitwigApiException.fromException("testOp", context, originalException);

        assertEquals(ErrorCode.OPERATION_FAILED, result.getErrorCode());
        assertEquals("testOp", result.getOperation());
        assertEquals("Error occurred", result.getMessage());
        assertEquals(context, result.getContext());
        assertEquals(originalException, result.getCause());
    }

    @Test
    void testToString() {
        Object context = java.util.Map.of("parameter", "test");
        BitwigApiException exception = new BitwigApiException(
            ErrorCode.INVALID_PARAMETER,
            "testOperation",
            "Test message",
            context
        );

        String result = exception.toString();
        assertTrue(result.contains("BitwigApiException"));
        assertTrue(result.contains("INVALID_PARAMETER"));
        assertTrue(result.contains("testOperation"));
        assertTrue(result.contains("Test message"));
        assertTrue(result.contains("parameter=test"));
    }

    @Test
    void testToStringWithoutContext() {
        BitwigApiException exception = new BitwigApiException(
            ErrorCode.DEVICE_NOT_SELECTED,
            "testOperation"
        );

        String result = exception.toString();
        assertTrue(result.contains("BitwigApiException"));
        assertTrue(result.contains("DEVICE_NOT_SELECTED"));
        assertTrue(result.contains("testOperation"));
        assertTrue(result.contains("No device is currently selected"));
        assertFalse(result.contains("context="));
    }

    @Test
    void testRethrowBitwigApiException() {
        BitwigApiException originalException = new BitwigApiException(
            ErrorCode.TRACK_NOT_FOUND,
            "originalOp"
        );

        BitwigApiException result = BitwigApiException.fromException("newOp", originalException);

        // ErrorCode.fromException analyzes the message "Specified track was not found"
        // and correctly identifies it as TRACK_NOT_FOUND
        assertEquals(ErrorCode.TRACK_NOT_FOUND, result.getErrorCode());
        assertEquals("newOp", result.getOperation());
        assertEquals(originalException, result.getCause());
    }
}
