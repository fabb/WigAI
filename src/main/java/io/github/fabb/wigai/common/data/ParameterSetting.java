package io.github.fabb.wigai.common.data;

/**
 * Record representing a parameter setting request for batch operations.
 * Used for setting multiple device parameters simultaneously.
 */
public record ParameterSetting(
    int parameter_index,     // 0-7
    double value            // 0.0-1.0 normalized
) {}
