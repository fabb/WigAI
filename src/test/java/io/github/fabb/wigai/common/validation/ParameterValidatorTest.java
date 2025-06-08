package io.github.fabb.wigai.common.validation;

import io.github.fabb.wigai.common.error.BitwigApiException;
import io.github.fabb.wigai.common.error.ErrorCode;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ParameterValidator.
 */
class ParameterValidatorTest {

    @Test
    void testValidateRequired_Success() {
        Map<String, Object> arguments = Map.of("testParam", "testValue");

        Object result = ParameterValidator.validateRequired(arguments, "testParam", "testOp");

        assertEquals("testValue", result);
    }

    @Test
    void testValidateRequired_Missing() {
        Map<String, Object> arguments = new HashMap<>();

        BitwigApiException exception = assertThrows(BitwigApiException.class, () ->
            ParameterValidator.validateRequired(arguments, "testParam", "testOp")
        );

        assertEquals(ErrorCode.MISSING_REQUIRED_PARAMETER, exception.getErrorCode());
        assertEquals("testOp", exception.getOperation());
        assertTrue(exception.getMessage().contains("testParam"));
    }

    @Test
    void testValidateType_Success() {
        Object value = "testString";

        String result = ParameterValidator.validateType(value, String.class, "testParam", "testOp");

        assertEquals("testString", result);
    }

    @Test
    void testValidateType_WrongType() {
        Object value = 123;

        BitwigApiException exception = assertThrows(BitwigApiException.class, () ->
            ParameterValidator.validateType(value, String.class, "testParam", "testOp")
        );

        assertEquals(ErrorCode.INVALID_PARAMETER_TYPE, exception.getErrorCode());
        assertEquals("testOp", exception.getOperation());
        assertTrue(exception.getMessage().contains("testParam"));
        assertTrue(exception.getMessage().contains("string"));
    }

    @Test
    void testValidateRequiredString_Success() {
        Map<String, Object> arguments = Map.of("testParam", "testValue");

        String result = ParameterValidator.validateRequiredString(arguments, "testParam", "testOp");

        assertEquals("testValue", result);
    }

    @Test
    void testValidateRequiredString_NotAString() {
        Map<String, Object> arguments = Map.of("testParam", 123);

        BitwigApiException exception = assertThrows(BitwigApiException.class, () ->
            ParameterValidator.validateRequiredString(arguments, "testParam", "testOp")
        );

        assertEquals(ErrorCode.INVALID_PARAMETER_TYPE, exception.getErrorCode());
    }

    @Test
    void testValidateRequiredInteger_Success() {
        Map<String, Object> arguments = Map.of("testParam", 42);

        int result = ParameterValidator.validateRequiredInteger(arguments, "testParam", "testOp");

        assertEquals(42, result);
    }

    @Test
    void testValidateRequiredInteger_FromDouble() {
        Map<String, Object> arguments = Map.of("testParam", 42.0);

        int result = ParameterValidator.validateRequiredInteger(arguments, "testParam", "testOp");

        assertEquals(42, result);
    }

    @Test
    void testValidateRequiredInteger_NotANumber() {
        Map<String, Object> arguments = Map.of("testParam", "not a number");

        BitwigApiException exception = assertThrows(BitwigApiException.class, () ->
            ParameterValidator.validateRequiredInteger(arguments, "testParam", "testOp")
        );

        assertEquals(ErrorCode.INVALID_PARAMETER_TYPE, exception.getErrorCode());
    }

    @Test
    void testValidateRequiredDouble_Success() {
        Map<String, Object> arguments = Map.of("testParam", 42.5);

        double result = ParameterValidator.validateRequiredDouble(arguments, "testParam", "testOp");

        assertEquals(42.5, result, 0.001);
    }

    @Test
    void testValidateRequiredDouble_FromInteger() {
        Map<String, Object> arguments = Map.of("testParam", 42);

        double result = ParameterValidator.validateRequiredDouble(arguments, "testParam", "testOp");

        assertEquals(42.0, result, 0.001);
    }

    @Test
    void testValidateRequiredDouble_NotANumber() {
        Map<String, Object> arguments = Map.of("testParam", "not a number");

        BitwigApiException exception = assertThrows(BitwigApiException.class, () ->
            ParameterValidator.validateRequiredDouble(arguments, "testParam", "testOp")
        );

        assertEquals(ErrorCode.INVALID_PARAMETER_TYPE, exception.getErrorCode());
    }

    @Test
    void testValidateNotEmpty_Success() {
        String result = ParameterValidator.validateNotEmpty("test value", "testParam", "testOp");

        assertEquals("test value", result);
    }

    @Test
    void testValidateNotEmpty_TrimsWhitespace() {
        String result = ParameterValidator.validateNotEmpty("  test value  ", "testParam", "testOp");

        assertEquals("test value", result);
    }

    @Test
    void testValidateNotEmpty_EmptyString() {
        BitwigApiException exception = assertThrows(BitwigApiException.class, () ->
            ParameterValidator.validateNotEmpty("", "testParam", "testOp")
        );

        assertEquals(ErrorCode.EMPTY_PARAMETER, exception.getErrorCode());
    }

    @Test
    void testValidateNotEmpty_WhitespaceOnly() {
        BitwigApiException exception = assertThrows(BitwigApiException.class, () ->
            ParameterValidator.validateNotEmpty("   ", "testParam", "testOp")
        );

        assertEquals(ErrorCode.EMPTY_PARAMETER, exception.getErrorCode());
    }

    @Test
    void testValidateNotEmpty_NullString() {
        BitwigApiException exception = assertThrows(BitwigApiException.class, () ->
            ParameterValidator.validateNotEmpty(null, "testParam", "testOp")
        );

        assertEquals(ErrorCode.EMPTY_PARAMETER, exception.getErrorCode());
    }

    @Test
    void testValidateRangeInt_Success() {
        int result = ParameterValidator.validateRange(5, 0, 10, "testParam", "testOp");

        assertEquals(5, result);
    }

    @Test
    void testValidateRangeInt_EdgeCases() {
        assertEquals(0, ParameterValidator.validateRange(0, 0, 10, "testParam", "testOp"));
        assertEquals(10, ParameterValidator.validateRange(10, 0, 10, "testParam", "testOp"));
    }

    @Test
    void testValidateRangeInt_TooLow() {
        BitwigApiException exception = assertThrows(BitwigApiException.class, () ->
            ParameterValidator.validateRange(-1, 0, 10, "testParam", "testOp")
        );

        assertEquals(ErrorCode.INVALID_RANGE, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("0"));
        assertTrue(exception.getMessage().contains("10"));
        assertTrue(exception.getMessage().contains("-1"));
    }

    @Test
    void testValidateRangeInt_TooHigh() {
        BitwigApiException exception = assertThrows(BitwigApiException.class, () ->
            ParameterValidator.validateRange(11, 0, 10, "testParam", "testOp")
        );

        assertEquals(ErrorCode.INVALID_RANGE, exception.getErrorCode());
    }

    @Test
    void testValidateRangeDouble_Success() {
        double result = ParameterValidator.validateRange(0.5, 0.0, 1.0, "testParam", "testOp");

        assertEquals(0.5, result, 0.001);
    }

    @Test
    void testValidateRangeDouble_EdgeCases() {
        assertEquals(0.0, ParameterValidator.validateRange(0.0, 0.0, 1.0, "testParam", "testOp"), 0.001);
        assertEquals(1.0, ParameterValidator.validateRange(1.0, 0.0, 1.0, "testParam", "testOp"), 0.001);
    }

    @Test
    void testValidateRangeDouble_TooLow() {
        BitwigApiException exception = assertThrows(BitwigApiException.class, () ->
            ParameterValidator.validateRange(-0.1, 0.0, 1.0, "testParam", "testOp")
        );

        assertEquals(ErrorCode.INVALID_RANGE, exception.getErrorCode());
    }

    @Test
    void testValidateParameterIndex_Success() {
        int result = ParameterValidator.validateParameterIndex(3, "testOp");

        assertEquals(3, result);
    }

    @Test
    void testValidateParameterIndex_EdgeCases() {
        assertEquals(0, ParameterValidator.validateParameterIndex(0, "testOp"));
        assertEquals(7, ParameterValidator.validateParameterIndex(7, "testOp"));
    }

    @Test
    void testValidateParameterIndex_OutOfRange() {
        BitwigApiException exception = assertThrows(BitwigApiException.class, () ->
            ParameterValidator.validateParameterIndex(8, "testOp")
        );

        assertEquals(ErrorCode.INVALID_RANGE, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("parameter_index"));
    }

    @Test
    void testValidateParameterValue_Success() {
        double result = ParameterValidator.validateParameterValue(0.5, "testOp");

        assertEquals(0.5, result, 0.001);
    }

    @Test
    void testValidateParameterValue_EdgeCases() {
        assertEquals(0.0, ParameterValidator.validateParameterValue(0.0, "testOp"), 0.001);
        assertEquals(1.0, ParameterValidator.validateParameterValue(1.0, "testOp"), 0.001);
    }

    @Test
    void testValidateParameterValue_OutOfRange() {
        BitwigApiException exception = assertThrows(BitwigApiException.class, () ->
            ParameterValidator.validateParameterValue(1.1, "testOp")
        );

        assertEquals(ErrorCode.INVALID_RANGE, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("value"));
    }

    @Test
    void testValidateClipIndex_Success() {
        int result = ParameterValidator.validateClipIndex(5, "testOp");

        assertEquals(5, result);
    }

    @Test
    void testValidateClipIndex_Zero() {
        int result = ParameterValidator.validateClipIndex(0, "testOp");

        assertEquals(0, result);
    }

    @Test
    void testValidateClipIndex_Negative() {
        BitwigApiException exception = assertThrows(BitwigApiException.class, () ->
            ParameterValidator.validateClipIndex(-1, "testOp")
        );

        assertEquals(ErrorCode.INVALID_RANGE, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("clip_index"));
    }

    @Test
    void testValidateSceneIndex_Success() {
        int result = ParameterValidator.validateSceneIndex(3, "testOp");

        assertEquals(3, result);
    }

    @Test
    void testValidateSceneIndex_Negative() {
        BitwigApiException exception = assertThrows(BitwigApiException.class, () ->
            ParameterValidator.validateSceneIndex(-1, "testOp")
        );

        assertEquals(ErrorCode.INVALID_RANGE, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("scene_index"));
    }

    @Test
    void testValidateCustom_Success() {
        String result = ParameterValidator.validateCustom(
            "test",
            s -> s.length() > 2,
            "testParam",
            "testOp",
            "String must be longer than 2 characters"
        );

        assertEquals("test", result);
    }

    @Test
    void testValidateCustom_Fails() {
        BitwigApiException exception = assertThrows(BitwigApiException.class, () ->
            ParameterValidator.validateCustom(
                "ab",
                s -> s.length() > 2,
                "testParam",
                "testOp",
                "String must be longer than 2 characters"
            )
        );

        assertEquals(ErrorCode.INVALID_PARAMETER, exception.getErrorCode());
        assertEquals("String must be longer than 2 characters", exception.getMessage());
    }

    @Test
    void testValidationBuilder_Success() {
        String result = ParameterValidator.ValidationBuilder
            .of("test value", "testParam", "testOp")
            .notNull()
            .custom(s -> s.length() > 5, "String too short")
            .get();

        assertEquals("test value", result);
    }

    @Test
    void testValidationBuilder_FailsOnNull() {
        BitwigApiException exception = assertThrows(BitwigApiException.class, () ->
            ParameterValidator.ValidationBuilder
                .of(null, "testParam", "testOp")
                .notNull()
                .get()
        );

        assertEquals(ErrorCode.MISSING_REQUIRED_PARAMETER, exception.getErrorCode());
    }

    @Test
    void testValidationBuilder_FailsOnCustom() {
        BitwigApiException exception = assertThrows(BitwigApiException.class, () ->
            ParameterValidator.ValidationBuilder
                .of("ab", "testParam", "testOp")
                .notNull()
                .custom(s -> s.length() > 5, "String too short")
                .get()
        );

        assertEquals(ErrorCode.INVALID_PARAMETER, exception.getErrorCode());
        assertEquals("String too short", exception.getMessage());
    }
}
