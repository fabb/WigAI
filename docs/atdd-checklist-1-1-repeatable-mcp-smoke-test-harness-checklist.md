# ATDD Checklist - Epic 1, Story 1.1: Repeatable MCP Smoke Test Harness + Checklist

**Date:** 2025-12-18T16:44:37-07:00
**Author:** Josh
**Primary Test Level:** Host-required smoke harness (client → MCP server) + CI-safe unit/integration invariants

---

## Story Summary

Story 1.1 introduces a repeatable, host-required smoke harness to validate a running Bitwig instance with the WigAI extension enabled. The harness exists to catch MCP regressions early (connectivity, tool discovery, response envelope sanity, and safe mutation gating) without trying to make Bitwig host behavior CI-deterministic.

**As a** WigAI developer (Josh)
**I want** a repeatable smoke-test harness + checklist for the running Bitwig extension
**So that** MCP regressions and integration issues are caught early before we build the MIDI workflow

---

## Acceptance Criteria

1. **Given** Bitwig Studio is running with WigAI enabled and a known `host:port` \
   **When** I run a local harness (e.g., `./gradlew mcpSmokeTest -PmcpHost=localhost -PmcpPort=61169`) \
   **Then** it connects to `http://{host}:{port}/mcp` and reports a clear pass/fail with actionable diagnostics.
2. **Given** the harness is connected \
   **When** it performs MCP discovery (e.g., `tools/list`) \
   **Then** it asserts baseline tools exist and prints the full tool list it observed (including at minimum `status`).
3. **Given** the harness is run in default “safe mode” \
   **When** it executes its checks \
   **Then** it performs read-only validations only (e.g., `status`, `list_tracks`, `get_track_details`, `list_devices_on_track`, `get_device_details`, `list_scenes`, `get_clips_in_scene`, and any other non-mutating tools available).
4. **Given** the harness is run with an explicit mutation flag (e.g., `WIGAI_SMOKE_TEST_MUTATIONS=true`) \
   **When** it executes its mutation checks \
   **Then** it can exercise a minimal set of safe mutations and restore state where applicable (e.g., `transport_start` then `transport_stop`; optional device parameter round-trip).
5. **Given** no device is selected \
   **When** the harness invokes `get_selected_device_parameters` \
   **Then** it returns a typed/structured error (e.g., `DEVICE_NOT_SELECTED`) rather than an unhandled exception.

---

## Failing Tests Created (RED Phase)

This repo is Java/JUnit-based (not Playwright/Cypress). For this story, “acceptance tests” map to:

- **Host-required smoke checks** run by a Gradle task (`mcpSmokeTest`) against a real Bitwig session.
- **CI-safe invariants** (argument parsing, mutation gating, response envelope parsing) as JUnit tests.

### E2E/Smoke Tests (5 tests)

**File:** `src/test/java/io/github/fabb/wigai/smoke/McpSmokeHarnessAtddRedTest.java` (~150 lines)

- ✅ **Test:** `1.1-E2E-001 prints_resolved_mcp_url_and_mode` \
  - **Status:** RED - harness runner not implemented (throws `UnsupportedOperationException`) \
  - **Verifies:** Output always includes resolved MCP URL and whether safe mode vs mutation mode ran.
- ✅ **Test:** `1.1-E2E-002 performs_tools_list_and_asserts_status_tool_present` \
  - **Status:** RED - harness runner not implemented \
  - **Verifies:** The harness calls discovery and asserts `status` exists in observed tool list.
- ✅ **Test:** `1.1-E2E-003 safe_mode_never_calls_mutating_tools` \
  - **Status:** RED - harness runner not implemented \
  - **Verifies:** Default run performs read-only tool calls only (no `transport_start`, `transport_stop`, etc.).
- ✅ **Test:** `1.1-E2E-004 mutation_mode_calls_transport_start_then_stop` \
  - **Status:** RED - harness runner not implemented \
  - **Verifies:** With explicit env opt-in, the harness performs the minimal mutation sequence and reports outcomes.
- ✅ **Test:** `1.1-E2E-005 no_device_selected_returns_typed_error_not_crash` \
  - **Status:** RED - harness runner not implemented \
  - **Verifies:** The harness surfaces `DEVICE_NOT_SELECTED` from MCP error envelope (no stack trace / no unhandled exception).

### API Tests (0 tests)

Not applicable: WigAI is an in-process Bitwig extension, not an HTTP JSON REST API surface.

### Component Tests (0 tests)

Not applicable: no UI component layer in scope for Story 1.1.

---

## Data Factories Created

Not applicable for Story 1.1 (no persistent datastore / no domain entities being created).

---

## Fixtures Created

Not applicable for Story 1.1 (no Playwright/Cypress fixtures; JUnit tests are pure and isolated).

---

## Mock Requirements

No third-party external services required. For CI-safe tests, the harness should be designed to accept a stubbed `McpClient` implementation (pure-function-first pattern) so unit tests can simulate:

- tool discovery results (`tools/list`)
- success envelopes (`{"status":"success","data":...}`)
- error envelopes (`{"status":"error","error":{"code":"DEVICE_NOT_SELECTED",...}}`)

---

## Required data-testid Attributes

Not applicable (no browser UI tests in Story 1.1).

---

## Implementation Checklist

### Test: `1.1-E2E-001 prints_resolved_mcp_url_and_mode`

**File:** `src/test/java/io/github/fabb/wigai/smoke/McpSmokeHarnessAtddRedTest.java`

**Tasks to make this test pass:**

- [ ] Implement `McpSmokeHarnessArgs` (host/port/path defaults and formatting).
- [ ] Implement `McpSmokeHarness` runner to always print:
  - resolved MCP URL (default endpoint path is `/mcp`)
  - safe-mode vs mutation-mode indicator
- [ ] Run test: `./gradlew atddRedTest`
- [ ] ✅ Test passes (green phase)

**Estimated Effort:** 1.0–2.0 hours

---

### Test: `1.1-E2E-002 performs_tools_list_and_asserts_status_tool_present`

**File:** `src/test/java/io/github/fabb/wigai/smoke/McpSmokeHarnessAtddRedTest.java`

**Tasks to make this test pass:**

- [ ] Define `McpClient` interface with minimal operations needed by the harness (discovery + tool call).
- [ ] Implement “discovery step” in harness:
  - [ ] call `tools/list`
  - [ ] print the observed tool list (summarized + debug full JSON)
  - [ ] assert at least `status` is present
- [ ] Ensure errors are surfaced as actionable step failures (tool name + error.code + error.message).
- [ ] Run test: `./gradlew atddRedTest`
- [ ] ✅ Test passes (green phase)

**Estimated Effort:** 2.0–4.0 hours

---

### Test: `1.1-E2E-003 safe_mode_never_calls_mutating_tools`

**File:** `src/test/java/io/github/fabb/wigai/smoke/McpSmokeHarnessAtddRedTest.java`

**Tasks to make this test pass:**

- [ ] Implement “plan builder” that enumerates read-only checks for safe mode:
  - `status`
  - `list_tracks`
  - `get_track_details`
  - `list_devices_on_track`
  - `get_device_details`
  - `list_scenes`
  - `get_clips_in_scene`
- [ ] Define and enforce a strict “mutating tool allowlist” only when `WIGAI_SMOKE_TEST_MUTATIONS=true`.
- [ ] Run test: `./gradlew atddRedTest`
- [ ] ✅ Test passes (green phase)

**Estimated Effort:** 1.0–2.0 hours

---

### Test: `1.1-E2E-004 mutation_mode_calls_transport_start_then_stop`

**File:** `src/test/java/io/github/fabb/wigai/smoke/McpSmokeHarnessAtddRedTest.java`

**Tasks to make this test pass:**

- [ ] Implement mutation gating (env var only; default read-only).
- [ ] Implement mutation checks:
  - [ ] call `transport_start`
  - [ ] call `transport_stop` (always attempt stop if start succeeded)
  - [ ] print action results and durations
- [ ] Ensure the harness exits non-zero on failure and reports the failing step.
- [ ] Run test: `./gradlew atddRedTest`
- [ ] ✅ Test passes (green phase)

**Estimated Effort:** 2.0–4.0 hours

---

### Test: `1.1-E2E-005 no_device_selected_returns_typed_error_not_crash`

**File:** `src/test/java/io/github/fabb/wigai/smoke/McpSmokeHarnessAtddRedTest.java`

**Tasks to make this test pass:**

- [ ] Ensure harness calls `get_selected_device_parameters` during safe checks.
- [ ] Parse tool responses using the canonical envelope:
  - success: `{"status":"success","data":...}`
  - error: `{"status":"error","error":{"code","message","operation"}}`
- [ ] If `DEVICE_NOT_SELECTED`, treat as an expected negative-path check (report as “validated”).
- [ ] Run test: `./gradlew atddRedTest`
- [ ] ✅ Test passes (green phase)

**Estimated Effort:** 1.0–2.0 hours

---

## Running Tests

```bash
# CI-safe unit/integration tests (does NOT include ATDD red-tagged tests)
./gradlew test

# Run ATDD RED tests for this story only (expected to fail until implemented)
./gradlew atddRedTest

# Run the host-required smoke harness against a real Bitwig session (once implemented)
./gradlew mcpSmokeTest -PmcpHost=localhost -PmcpPort=61169

# Enable mutation checks explicitly (once implemented)
WIGAI_SMOKE_TEST_MUTATIONS=true ./gradlew mcpSmokeTest -PmcpHost=localhost -PmcpPort=61169
```

---

## Red-Green-Refactor Workflow

### RED Phase (Complete) ✅

**TEA Agent Responsibilities:**

- ✅ Acceptance criteria mapped to atomic tests
- ✅ Test strategy selected (host-required smoke + CI-safe invariants)
- ✅ Implementation checklist created

**Verification:**

- Run `./gradlew atddRedTest` and confirm failures are due to missing harness behavior (not test bugs).

---

### GREEN Phase (DEV Team - Next Steps)

1. Implement the harness runner (`McpSmokeHarness`) and the `McpClient` abstraction.
2. Make `atddRedTest` go green one test at a time, in order (001 → 005).
3. Wire a `JavaExec` Gradle task `mcpSmokeTest` that runs the harness against a real Bitwig instance.
4. Keep the default behavior strictly read-only; mutations require env opt-in.

---

### REFACTOR Phase (DEV Team - After All Tests Pass)

1. Refactor harness code for clarity and diagnostics quality (step IDs, durations, clear failure messages).
2. Keep operations bounded (timeouts per step; no hangs).
3. Ensure log output avoids noisy payloads (summaries only).

---

## Next Steps

1. Review this checklist in planning/standup.
2. Implement `mcpSmokeTest` harness to make `atddRedTest` green.
3. After harness exists, run it against Bitwig and attach output to story completion notes.

---

## Knowledge Base References Applied

This ATDD workflow consulted the following fragments (adapted for a Java/JUnit project):

- `test-quality.md` - Determinism, isolation, explicit assertions, bounded runtime
- `test-levels-framework.md` - Unit vs integration vs E2E selection (host-required E2E kept minimal)
- `test-healing-patterns.md` - Actionable failures and anti-flakiness mindset
- `timing-debugging.md` - Replace time-based waits with event-based signals (adapted: timeouts + bounded retries)
- `fixture-architecture.md` - Pure-function-first abstraction (adapted: `McpClient` interface injection)

---

## Test Execution Evidence

### Initial Test Run (RED Phase Verification)

**Command:** `./gradlew atddRedTest`

**Results:**

Not executed in this run (host-required development flow). When implementing Story 1.1, paste the failing output here to document RED phase verification.

---

## Notes

- MCP endpoint path is `/mcp` (see `src/main/java/io/github/fabb/wigai/WigAIExtension.java`).
- Default port is `61169` (see `src/main/java/io/github/fabb/wigai/common/AppConstants.java`).
- Baseline tools are registered in `src/main/java/io/github/fabb/wigai/mcp/McpServerManager.java`.
- Canonical response envelope is validated by `src/test/java/io/github/fabb/wigai/mcp/tool/McpResponseTestUtils.java`.

