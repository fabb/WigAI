package io.github.fabb.wigai.common.data;

/**
 * Record representing a device parameter with its current state.
 * Used in MCP responses for device parameter queries.
 */
public record ParameterInfo(
    int index,           // 0-7
    String name,         // Nullable
    double value,        // 0.0-1.0 normalized
    String display_value // Bitwig's display string
) {}
