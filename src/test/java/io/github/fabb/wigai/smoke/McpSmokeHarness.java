package io.github.fabb.wigai.smoke;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Story 1.1 host-required smoke harness runner (client-side).
 *
 * Validates a running Bitwig instance with WigAI extension by connecting to the MCP server.
 * Default behavior is read-only (safe mode). Mutations require explicit opt-in.
 *
 * Exit codes: 0 = pass, 1 = failure.
 */
public final class McpSmokeHarness {

    // AC2: Assert baseline tools exist - full read-only tool set per story requirements
    private static final Set<String> BASELINE_REQUIRED_TOOLS = Set.of(
            "status",
            "list_tracks",
            "get_track_details",
            "list_devices_on_track",
            "get_device_details",
            "list_scenes",
            "get_clips_in_scene",
            "get_selected_device_parameters"
    );

    // READ_ONLY_TOOLS is now the same as BASELINE_REQUIRED_TOOLS
    private static final Set<String> READ_ONLY_TOOLS = BASELINE_REQUIRED_TOOLS;

    // Mutating tools - aligned with actual MCP tool names from src/main/java/.../mcp/tool/
    private static final Set<String> MUTATING_TOOLS = Set.of(
            "transport_start",
            "transport_stop",
            "set_selected_device_parameter",      // DeviceParamTool.SET_PARAMETER_TOOL
            "set_selected_device_parameters",     // DeviceParamTool.SET_MULTIPLE_PARAMETERS_TOOL
            "session_launchSceneByIndex",         // SceneTool.TOOL_NAME
            "session_launchSceneByName",          // SceneByNameTool.TOOL_NAME
            "launch_clip"                         // ClipTool.TOOL_NAME
    );

    public int run(McpSmokeHarnessArgs args, McpClient client, PrintStream out, PrintStream err) {
        Objects.requireNonNull(args, "args");
        Objects.requireNonNull(client, "client");
        Objects.requireNonNull(out, "out");
        Objects.requireNonNull(err, "err");

        String mode = args.mutationsEnabled() ? "MUTATION mode" : "SAFE mode (read-only)";
        out.println("=== MCP Smoke Harness ===");
        out.println("MCP URL: " + args.resolvedUrl());
        out.println("Mode: " + mode);
        out.println();

        // Step 1: Discovery - tools/list
        List<String> tools;
        try {
            tools = client.listTools();
        } catch (Exception e) {
            err.println("FAIL: tools/list failed: " + e.getMessage());
            return 1;
        }

        out.println("Discovered tools: " + tools);

        // Step 2: Assert baseline tools exist
        for (String required : BASELINE_REQUIRED_TOOLS) {
            if (!tools.contains(required)) {
                err.println("FAIL: Baseline tool missing: " + required);
                return 1;
            }
        }
        out.println("✓ Baseline tools present");

        if (args.mutationsEnabled()) {
            // Mutation mode: only call mutation tools
            out.println();
            out.println("--- Mutation checks ---");

            // Fail if required mutation tools are missing (don't silently skip)
            if (!tools.contains("transport_start")) {
                err.println("FAIL: Mutation mode requires transport_start tool but it is missing");
                return 1;
            }
            if (!tools.contains("transport_stop")) {
                err.println("FAIL: Mutation mode requires transport_stop tool but it is missing");
                return 1;
            }

            // transport_start then transport_stop - with envelope validation
            try {
                String startResponse = client.callTool("transport_start", Map.of());
                EnvelopeResult startEnvelope = parseEnvelope(startResponse);
                if (!startEnvelope.isValidEnvelope()) {
                    err.println("FAIL: transport_start returned invalid envelope: " + startEnvelope.errorMessage());
                    return 1;
                }
                if (startEnvelope.isError()) {
                    err.println("FAIL: transport_start returned error: " + startEnvelope.errorCode());
                    return 1;
                }
                out.println("✓ transport_start → OK");

                String stopResponse = client.callTool("transport_stop", Map.of());
                EnvelopeResult stopEnvelope = parseEnvelope(stopResponse);
                if (!stopEnvelope.isValidEnvelope()) {
                    err.println("FAIL: transport_stop returned invalid envelope: " + stopEnvelope.errorMessage());
                    return 1;
                }
                if (stopEnvelope.isError()) {
                    err.println("FAIL: transport_stop returned error: " + stopEnvelope.errorCode());
                    return 1;
                }
                out.println("✓ transport_stop → OK");
            } catch (Exception e) {
                err.println("FAIL: transport mutation failed: " + e.getMessage());
                return 1;
            }

            if (tools.contains("get_selected_device_parameters")
                    && tools.contains("set_selected_device_parameter")) {
                try {
                    String response = client.callTool("get_selected_device_parameters", Map.of());
                    EnvelopeResult envelope = parseEnvelope(response);

                    if (!envelope.isValidEnvelope()) {
                        err.println("FAIL: get_selected_device_parameters returned invalid envelope: "
                                + envelope.errorMessage());
                        return 1;
                    }

                    if (envelope.isError()) {
                        if ("DEVICE_NOT_SELECTED".equals(envelope.errorCode())) {
                            out.println("Skipping device parameter round-trip: DEVICE_NOT_SELECTED");
                        } else {
                            err.println("FAIL: get_selected_device_parameters returned error: " + envelope.errorCode());
                            return 1;
                        }
                    } else {
                        JsonNode root = OBJECT_MAPPER.readTree(response);
                        JsonNode parameters = root.path("data").path("parameters");
                        if (parameters.isArray() && parameters.size() > 0) {
                            JsonNode firstParam = parameters.get(0);
                            if (firstParam.hasNonNull("index") && firstParam.hasNonNull("value")) {
                                int index = firstParam.get("index").asInt();
                                double value = firstParam.get("value").asDouble();
                                String setResponse = client.callTool("set_selected_device_parameter", Map.of(
                                        "parameter_index", index,
                                        "value", value
                                ));
                                EnvelopeResult setEnvelope = parseEnvelope(setResponse);
                                if (!setEnvelope.isValidEnvelope()) {
                                    err.println("FAIL: set_selected_device_parameter returned invalid envelope: "
                                            + setEnvelope.errorMessage());
                                    return 1;
                                }
                                if (setEnvelope.isError()) {
                                    err.println("FAIL: set_selected_device_parameter returned error: "
                                            + setEnvelope.errorCode());
                                    return 1;
                                }
                                out.println("✓ device parameter round-trip → OK");
                            } else {
                                out.println("Skipping device parameter round-trip: no parameter value available");
                            }
                        } else {
                            out.println("Skipping device parameter round-trip: no parameters returned");
                        }
                    }
                } catch (Exception e) {
                    err.println("FAIL: device parameter round-trip failed: " + e.getMessage());
                    return 1;
                }
            } else {
                out.println("Skipping device parameter round-trip: required tools missing");
            }
        } else {
            // Safe mode: only call read-only tools
            for (String tool : READ_ONLY_TOOLS) {
                if (tools.contains(tool)) {
                    // Explicit guard: ensure safe mode never calls mutating tools
                    assertToolIsSafeForSafeMode(tool);
                    try {
                        String result = client.callTool(tool, Map.of());
                        EnvelopeResult envelope = parseEnvelope(result);

                        if (!envelope.isValidEnvelope()) {
                            err.println("FAIL: " + tool + " returned invalid envelope: " + envelope.errorMessage());
                            return 1;
                        }

                        if (envelope.isError()) {
                            if ("get_selected_device_parameters".equals(tool)) {
                                if ("DEVICE_NOT_SELECTED".equals(envelope.errorCode())) {
                                    out.println("✓ " + tool + " → typed error [DEVICE_NOT_SELECTED] (expected when no device selected)");
                                } else {
                                    err.println("FAIL: " + tool + " returned unexpected error: " + envelope.errorCode());
                                    return 1;
                                }
                            } else {
                                // Typed error on other read-only tools indicates a problem - fail to keep pass/fail meaningful
                                err.println("FAIL: " + tool + " returned typed error: " + envelope.errorCode());
                                return 1;
                            }
                        } else {
                            out.println("✓ " + tool + " → OK");
                        }
                    } catch (Exception e) {
                        err.println("FAIL: " + tool + " threw exception: " + e.getMessage());
                        return 1;
                    }
                }
            }
        }

        out.println();
        out.println("=== PASS ===");
        return 0;
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Explicit guard: throws IllegalStateException if tool is in MUTATING_TOOLS.
     * This prevents safe mode from ever calling mutating tools, even if READ_ONLY_TOOLS
     * is accidentally modified to include a mutating tool.
     */
    void assertToolIsSafeForSafeMode(String toolName) {
        if (MUTATING_TOOLS.contains(toolName)) {
            throw new IllegalStateException(
                    "Safe mode attempted to call mutating tool: " + toolName +
                    ". This is a bug in the harness - safe mode must only call read-only tools.");
        }
    }

    /**
     * Represents the result of parsing a tool response envelope.
     */
    record EnvelopeResult(boolean isError, String errorCode, String errorMessage, boolean isValidEnvelope) {
        static EnvelopeResult success() {
            return new EnvelopeResult(false, null, null, true);
        }

        static EnvelopeResult typedError(String code, String message) {
            return new EnvelopeResult(true, code, message, true);
        }

        static EnvelopeResult invalid(String reason) {
            return new EnvelopeResult(false, null, reason, false);
        }
    }

    /**
     * Parses and validates a tool response envelope per API reference format.
     *
     * Success: {"status":"success","data":{...}}
     * Error: {"status":"error","error":{"code":"...","message":"...","operation":"..."}}
     */
    EnvelopeResult parseEnvelope(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.isBlank()) {
            return EnvelopeResult.invalid("Response is null or empty");
        }

        try {
            JsonNode root = OBJECT_MAPPER.readTree(jsonResponse);

            if (!root.has("status")) {
                return EnvelopeResult.invalid("Missing 'status' field");
            }

            String status = root.get("status").asText();

            if ("success".equals(status)) {
                if (!root.has("data")) {
                    return EnvelopeResult.invalid("Success response missing 'data' field");
                }
                return EnvelopeResult.success();
            } else if ("error".equals(status)) {
                if (!root.has("error")) {
                    return EnvelopeResult.invalid("Error response missing 'error' object");
                }

                JsonNode errorNode = root.get("error");

                // AC5: Validate typed error has code (not just an unhandled exception)
                if (!errorNode.has("code")) {
                    return EnvelopeResult.invalid("Error response missing 'error.code' - not a typed error");
                }

                String code = errorNode.get("code").asText();
                String message = errorNode.has("message") ? errorNode.get("message").asText() : null;

                return EnvelopeResult.typedError(code, message);
            } else {
                return EnvelopeResult.invalid("Unknown status: " + status);
            }
        } catch (Exception e) {
            return EnvelopeResult.invalid("Invalid JSON: " + e.getMessage());
        }
    }
}
