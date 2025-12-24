# MCP Smoke Test Runbook

This document describes how to run the MCP smoke test harness to validate that WigAI's MCP server is functioning correctly.

## Prerequisites

1. **Bitwig Studio** is running
2. **WigAI extension** is installed and enabled in Bitwig
3. You know the MCP server host and port (default: `localhost:61169`)

## Quick Start

### Safe Mode (Read-Only)

```bash
./gradlew mcpSmokeTest
```

This runs read-only validations only. No mutations are performed.

### With Custom Host/Port

```bash
./gradlew mcpSmokeTest -PmcpHost=192.168.1.50 -PmcpPort=8080
```

### Mutation Mode

```bash
WIGAI_SMOKE_TEST_MUTATIONS=true ./gradlew mcpSmokeTest
```

This additionally tests mutation tools like `transport_start`/`transport_stop`.

## What Gets Tested

### Safe Mode (Default)

| Check | Description |
|-------|-------------|
| Connection | Connects to MCP endpoint at `http://{host}:{port}/mcp` |
| Discovery | Runs `tools/list` and prints all available tools |
| Baseline Tools | Asserts ALL baseline read-only tools exist (see list below) |
| Read-Only Tools | Calls each baseline tool and validates response envelopes |

Read-only tools tested:
- `status`
- `list_tracks`
- `get_track_details`
- `list_devices_on_track`
- `get_device_details`
- `list_scenes`
- `get_clips_in_scene`
- `get_selected_device_parameters`

### Mutation Mode

| Check | Description |
|-------|-------------|
| Transport Control | Calls `transport_start` then `transport_stop` |

## Exit Codes

| Code | Meaning |
|------|---------|
| 0 | All checks passed |
| 1 | One or more checks failed |

## Troubleshooting

### Connection Refused

**Symptom:** `FAIL: tools/list failed: Connection refused`

**Causes:**
- Bitwig Studio is not running
- WigAI extension is not enabled
- Wrong host or port specified

**Solution:**
1. Verify Bitwig is running
2. Check WigAI is listed in Bitwig's extension preferences
3. Verify the MCP port in WigAI settings (default: 61169)

### Timeout

**Symptom:** `FAIL: tools/list failed: Read timed out`

**Causes:**
- Bitwig is busy/unresponsive
- Network issues

**Solution:**
1. Wait for Bitwig to finish loading
2. Check if another MCP client is connected

### Baseline Tool Missing

**Symptom:** `FAIL: Baseline tool missing: <tool_name>`

(Where `<tool_name>` is any of the baseline tools: `status`, `list_tracks`, `get_track_details`, etc.)

**Causes:**
- WigAI version mismatch
- Extension not fully initialized

**Solution:**
1. Restart Bitwig
2. Check WigAI extension logs

### Typed Error (Expected)

**Symptom:** `status → typed error (expected in some states)`

This is **not a failure**. Some tools return typed errors when the DAW state doesn't match (e.g., no device selected). The harness validates that errors are structured, not unhandled exceptions.

## Bug Report Checklist

When filing a bug, please attach:

1. **Harness output** - Full stdout/stderr from the smoke test
2. **WigAI version** - Run `./gradlew properties | grep version`
3. **Bitwig version** - From Bitwig Studio → About
4. **OS** - macOS/Windows/Linux version
5. **Java version** - Run `java -version`
6. **Bitwig project state** - Empty project? Loaded project?

Example bug report command:
```bash
./gradlew mcpSmokeTest 2>&1 | tee smoke-test-output.txt
```

## Advanced Usage

### CLI Direct Execution

For more control, run the harness directly:

```bash
./gradlew testClasses
java -cp build/classes/java/test:build/classes/java/main:$(./gradlew -q dependencies --configuration testRuntimeClasspath | tr '\n' ':') \
  io.github.fabb.wigai.smoke.McpSmokeHarnessMain --help
```

### Environment Variables

| Variable | Description |
|----------|-------------|
| `WIGAI_SMOKE_TEST_MUTATIONS` | Set to `true` to enable mutation tests |

## CI/CD Notes

This harness is **NOT** suitable for CI/CD pipelines because it requires a running Bitwig instance. The harness is excluded from the default `test` task.

For CI-safe unit tests of the harness itself, run:
```bash
./gradlew test
```

This runs argument parsing and behavior tests using mock clients.
