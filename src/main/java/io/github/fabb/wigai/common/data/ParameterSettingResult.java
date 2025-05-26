package io.github.fabb.wigai.common.data;

/**
 * Record representing the result of setting a single parameter in a batch operation.
 * Used for structured response formatting in multiple parameter setting operations.
 */
public record ParameterSettingResult(
    int parameter_index,     // The parameter index that was attempted
    String status,           // "success" or "error"
    Double new_value,        // The value that was set (null for errors)
    String error_code,       // Error code if status is "error" (null for success)
    String message          // Success message or error description
) {}
