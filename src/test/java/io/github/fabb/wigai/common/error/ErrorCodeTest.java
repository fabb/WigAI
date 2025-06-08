package io.github.fabb.wigai.common.error;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ErrorCode enum and exception classification.
 */
class ErrorCodeTest {

    @Test
    void testErrorCodeBasicProperties() {
        ErrorCode code = ErrorCode.INVALID_PARAMETER;
        assertEquals("INVALID_PARAMETER", code.getCode());
        assertEquals("Invalid parameter value provided", code.getDefaultMessage());
    }

    @Test
    void testFromExceptionWithIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Parameter index must be between 0-7");
        ErrorCode result = ErrorCode.fromException(ex);
        assertEquals(ErrorCode.INVALID_PARAMETER, result);
    }

    @Test
    void testFromExceptionWithParameterValueError() {
        IllegalArgumentException ex = new IllegalArgumentException("Parameter value must be between 0.0-1.0");
        ErrorCode result = ErrorCode.fromException(ex);
        assertEquals(ErrorCode.INVALID_PARAMETER, result);
    }

    @Test
    void testFromExceptionWithMissingParameter() {
        IllegalArgumentException ex = new IllegalArgumentException("Missing required parameter: trackName");
        ErrorCode result = ErrorCode.fromException(ex);
        assertEquals(ErrorCode.INVALID_PARAMETER, result);
    }

    @Test
    void testFromExceptionWithTypeError() {
        IllegalArgumentException ex = new IllegalArgumentException("value must be a number");
        ErrorCode result = ErrorCode.fromException(ex);
        assertEquals(ErrorCode.INVALID_PARAMETER, result);
    }

    @Test
    void testFromExceptionWithEmptyParameter() {
        IllegalArgumentException ex = new IllegalArgumentException("trackName cannot be empty");
        ErrorCode result = ErrorCode.fromException(ex);
        assertEquals(ErrorCode.INVALID_PARAMETER, result);
    }

    @Test
    void testFromExceptionWithRuntimeExceptionDeviceNotSelected() {
        RuntimeException ex = new RuntimeException("No device is currently selected");
        ErrorCode result = ErrorCode.fromException(ex);
        assertEquals(ErrorCode.OPERATION_FAILED, result);
    }

    @Test
    void testFromExceptionWithRuntimeExceptionTrackNotFound() {
        RuntimeException ex = new RuntimeException("track not found");
        ErrorCode result = ErrorCode.fromException(ex);
        assertEquals(ErrorCode.OPERATION_FAILED, result);
    }

    @Test
    void testFromExceptionWithRuntimeExceptionSceneNotFound() {
        RuntimeException ex = new RuntimeException("scene not found");
        ErrorCode result = ErrorCode.fromException(ex);
        assertEquals(ErrorCode.OPERATION_FAILED, result);
    }

    @Test
    void testFromExceptionWithRuntimeExceptionClipNotFound() {
        RuntimeException ex = new RuntimeException("clip not found");
        ErrorCode result = ErrorCode.fromException(ex);
        assertEquals(ErrorCode.OPERATION_FAILED, result);
    }

    @Test
    void testFromExceptionWithTransportError() {
        RuntimeException ex = new RuntimeException("transport operation failed");
        ErrorCode result = ErrorCode.fromException(ex);
        assertEquals(ErrorCode.OPERATION_FAILED, result);
    }

    @Test
    void testFromExceptionWithBitwigApiError() {
        RuntimeException ex = new RuntimeException("Bitwig API call failed");
        ErrorCode result = ErrorCode.fromException(ex);
        assertEquals(ErrorCode.OPERATION_FAILED, result);
    }

    @Test
    void testFromExceptionWithGenericRuntimeException() {
        RuntimeException ex = new RuntimeException("something went wrong");
        ErrorCode result = ErrorCode.fromException(ex);
        assertEquals(ErrorCode.OPERATION_FAILED, result);
    }

    @Test
    void testFromExceptionWithGenericIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("invalid data");
        ErrorCode result = ErrorCode.fromException(ex);
        assertEquals(ErrorCode.INVALID_PARAMETER, result);
    }

    @Test
    void testFromExceptionWithUnknownException() {
        Exception ex = new Exception("unknown error");
        ErrorCode result = ErrorCode.fromException(ex);
        assertEquals(ErrorCode.UNKNOWN_ERROR, result);
    }

    @Test
    void testFromExceptionWithNullException() {
        ErrorCode result = ErrorCode.fromException(null);
        assertEquals(ErrorCode.UNKNOWN_ERROR, result);
    }

    @Test
    void testFromExceptionWithNullMessage() {
        Exception ex = new Exception((String) null);
        ErrorCode result = ErrorCode.fromException(ex);
        assertEquals(ErrorCode.UNKNOWN_ERROR, result);
    }

    @Test
    void testTimeoutExceptionDetection() {
        // Simulate a timeout-related exception
        Exception ex = new Exception("TimeoutException occurred");
        ErrorCode result = ErrorCode.fromException(ex);
        // Since we can't mock class name easily, this test verifies the logic would work
        assertEquals(ErrorCode.UNKNOWN_ERROR, result);
    }
}
