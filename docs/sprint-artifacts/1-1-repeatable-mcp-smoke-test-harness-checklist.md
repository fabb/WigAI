# Story 1.1: Repeatable MCP Smoke Test Harness + Checklist

Status: Ready for Review

## Story

As a WigAI developer (Josh),
I want a repeatable smoke-test harness + checklist for the running Bitwig extension,
so that MCP regressions and integration issues are caught early before we build the MIDI workflow.

## Acceptance Criteria

1. **Given** Bitwig Studio is running with WigAI enabled and a known `host:port`  
   **When** I run a local harness (e.g., `./gradlew mcpSmokeTest -PmcpHost=localhost -PmcpPort=61169`)  
   **Then** it connects to `http://{host}:{port}/mcp` and reports a clear pass/fail with actionable diagnostics.
2. **Given** the harness is connected  
   **When** it performs MCP discovery (e.g., `tools/list`)  
   **Then** it asserts baseline tools exist and prints the full tool list it observed (including at minimum `status`).
3. **Given** the harness is run in default “safe mode”  
   **When** it executes its checks  
   **Then** it performs read-only validations only (e.g., `status`, `list_tracks`, `get_track_details`, `list_devices_on_track`, `get_device_details`, `list_scenes`, `get_clips_in_scene`, and any other non-mutating tools available).
4. **Given** the harness is run with an explicit mutation flag (e.g., `WIGAI_SMOKE_TEST_MUTATIONS=true`)  
   **When** it executes its mutation checks  
   **Then** it can exercise a minimal set of safe mutations and restore state where applicable (e.g., `transport_start` then `transport_stop`; optional device parameter round-trip).
5. **Given** no device is selected  
   **When** the harness invokes `get_selected_device_parameters`  
   **Then** it returns a typed/structured error (e.g., `DEVICE_NOT_SELECTED`) rather than an unhandled exception.

## Tasks / Subtasks

- [x] Implement a host-required smoke harness runnable via Gradle
  - [x] Add a dedicated CLI entrypoint (Java) that can connect to an MCP server over HTTP SSE/streamable transport
  - [x] Read `mcpHost`, `mcpPort`, optional `mcpEndpointPath` (default `/mcp`) and print the resolved URL
  - [x] Implement a strict timeout budget (connect timeout + per-operation timeout) and ensure the process exits with non-zero on failure
- [x] Implement "safe mode" (default) read-only checks
  - [x] Run MCP discovery and print the full observed `tools/list` output (or a summarized list + full JSON in a debug section)
  - [x] Assert baseline tools exist (minimum: `status`; also validate presence of current baseline set)
  - [x] Execute read-only tools and validate response envelopes are parseable and actionable
- [x] Implement mutation-gated checks (off by default)
  - [x] Gate mutations behind `WIGAI_SMOKE_TEST_MUTATIONS=true` (env var only; no accidental mutation via default args)
  - [x] Execute `transport_start` then `transport_stop` and validate results and that the extension remains responsive
  - [x] (Optional) Device parameter "round-trip" only if a device is selected (skip with explicit message otherwise)
- [x] Ensure negative-path "no device selected" is validated
  - [x] Call `get_selected_device_parameters` with no device selected and assert a typed error (no stack trace / no unhandled exception)
- [x] Add a human-readable checklist + runbook for Josh
  - [x] Document prerequisites (Bitwig running, WigAI enabled, host/port known)
  - [x] Provide commands for safe mode and mutation mode
  - [x] Provide troubleshooting guidance and "what to attach to a bug report" (logs + harness output)
- [x] Wire up `./gradlew mcpSmokeTest`
  - [x] Add a `mcpSmokeTest` Gradle task that runs the harness without requiring the full `test` suite
  - [x] Ensure it is NOT part of default CI gating (Bitwig host required)

### Review Follow-ups (AI)

- [x] [AI-Review][Critical] Remove CLI `--mutations` flag or enforce env-var-only gating per AC4 [src/test/java/io/github/fabb/wigai/smoke/McpSmokeHarnessMain.java:62]
- [x] [AI-Review][Critical] Validate full baseline tool set (not just `status`) per AC2 [src/test/java/io/github/fabb/wigai/smoke/McpSmokeHarness.java:19]
- [x] [AI-Review][Critical] Parse/validate response envelopes instead of substring check for typed errors [src/test/java/io/github/fabb/wigai/smoke/McpSmokeHarness.java:113]
- [x] [AI-Review][High] Fail mutation mode when required mutation tools are missing instead of skipping [src/test/java/io/github/fabb/wigai/smoke/McpSmokeHarness.java:78]
- [x] [AI-Review][High] Fix Story File List to match git changes (remove unchanged files) [docs/sprint-artifacts/1-1-repeatable-mcp-smoke-test-harness-checklist.md:161]
- [x] [AI-Review][Medium] Add CI-safe tests for envelope parsing/error surfacing [src/test/java/io/github/fabb/wigai/smoke/McpSmokeHarnessArgsTest.java]
- [x] [AI-Review][High] Add explicit guard to ensure safe mode never calls mutating tools [src/test/java/io/github/fabb/wigai/smoke/McpSmokeHarness.java:103]
- [x] [AI-Review][High] Handle non-text or multi-content tool responses in HttpMcpClient to avoid false envelope failures [src/test/java/io/github/fabb/wigai/smoke/HttpMcpClient.java:83]
- [x] [AI-Review][Medium] De-duplicate resolved URL printing between CLI and harness output [src/test/java/io/github/fabb/wigai/smoke/McpSmokeHarnessMain.java:29]
- [x] [AI-Review][Medium] Update runbook baseline-tool assertion to match full baseline set [docs/engineering/mcp-smoke-test-runbook.md:43]
- [x] [AI-Review][High] Align MUTATING_TOOLS and safe-mode guard with actual tool names (`set_selected_device_parameter`, `session_launchSceneByIndex`, `session_launchSceneByName`) [src/test/java/io/github/fabb/wigai/smoke/McpSmokeHarness.java:37]
- [x] [AI-Review][High] Validate mutation-mode tool responses via envelope parsing (transport + parameter set) instead of assuming OK [src/test/java/io/github/fabb/wigai/smoke/McpSmokeHarness.java:93]
- [x] [AI-Review][High] Fix File List to reflect current git changes (remove stale entries) [docs/sprint-artifacts/1-1-repeatable-mcp-smoke-test-harness-checklist.md:190]
- [x] [AI-Review][Critical] Update File List to include files from last 8 commits (docs/atdd-checklist-1-1-repeatable-mcp-smoke-test-harness-checklist.md, docs/sprint-artifacts/validation-report-2025-12-18T15-37-47-07-00.md, docs/test-design-epic-1.md, docs/test-review.md) [docs/sprint-artifacts/1-1-repeatable-mcp-smoke-test-harness-checklist.md:198]
- [x] [AI-Review][High] Fail safe mode on any typed error other than DEVICE_NOT_SELECTED to keep pass/fail meaningful [src/test/java/io/github/fabb/wigai/smoke/McpSmokeHarness.java:197]
- [x] [AI-Review][Medium] Wrap JSON-RPC error responses in a {status:"error"} envelope so parseEnvelope reports actionable code/message [src/test/java/io/github/fabb/wigai/smoke/HttpMcpClient.java:102]
- [x] [AI-Review][Critical] Fix File List to include missing last-10-commit docs (docs/atdd-checklist-1-1-repeatable-mcp-smoke-test-harness-checklist.md, docs/sprint-artifacts/validation-report-2025-12-18T15-37-47-07-00.md, docs/test-design-epic-1.md) [docs/sprint-artifacts/1-1-repeatable-mcp-smoke-test-harness-checklist.md:208]
- [x] [AI-Review][High] Print full tools/list JSON to stdout (not just tool names) to satisfy AC2 [src/test/java/io/github/fabb/wigai/smoke/McpSmokeHarness.java:69]
- [x] [AI-Review][Medium] Update runbook typed-error guidance to reflect safe-mode failures on non-device tool errors [docs/engineering/mcp-smoke-test-runbook.md:111]
- [x] [AI-Review][Medium] Escape JSON-RPC error messages safely when wrapping in status envelope (handle newline/control chars) [src/test/java/io/github/fabb/wigai/smoke/HttpMcpClient.java:105]
- [x] [AI-Review][Medium] Promote green ATDD tests (remove atdd_red tag or adjust filters so they run in CI) [src/test/java/io/github/fabb/wigai/smoke/McpSmokeHarnessAtddRedTest.java:14]

## Dev Notes

### Developer Context (Guardrails)

- This is a **host-required smoke harness**. It validates a *running* Bitwig instance with the WigAI extension enabled. Do not attempt to make this pass in CI.
- Default behavior must be **read-only**. Mutations require an explicit opt-in.
- Output must be **actionable**:
  - Always print the resolved MCP URL and whether safe-mode vs mutation-mode ran.
  - On failure, print which step failed, the MCP method/tool name, and any returned `error.code`/`error.message`.
  - Exit codes: `0` on pass, `1` on any failed assertion/timeout/unreachable server.
- Use the **MCP Java SDK client utilities** if available; do not hand-roll protocol framing unless necessary.

### Technical Requirements

- **Endpoint**: WigAI registers MCP servlet at `"/mcp"` (see `src/main/java/io/github/fabb/wigai/WigAIExtension.java`).
- **Default port**: `61169` (see `src/main/java/io/github/fabb/wigai/common/AppConstants.java`).
- **Transport**: Server uses MCP Java SDK + Jetty, currently configured for the MCP servlet transport provider (see `src/main/java/io/github/fabb/wigai/mcp/McpServerManager.java` and `src/main/java/io/github/fabb/wigai/server/JettyServerManager.java`).
- **Dependencies**: Project uses Java 21, Gradle Kotlin DSL, MCP BOM `0.11.0`, Jetty `11.0.20` (see `build.gradle.kts` and `docs/architecture.md`).
- **No network beyond localhost**: This harness must be able to run in offline environments (local-only).

### Architecture Compliance

- Treat this harness as an **external client** of the MCP server boundary.
- Do not add Bitwig API dependencies to the harness beyond what’s already in the repo; the harness must not rely on Bitwig internal state except via MCP tool calls.
- Do not add new MCP tools in this story; this story validates the existing baseline tool surface and its stability.

### Library / Framework Requirements

- Prefer the existing MCP SDK ecosystem (`io.modelcontextprotocol.sdk:*`) for client/transport where possible (already in dependencies).
- Prefer standard Java HTTP client if the SDK does not provide a client transport usable for host-required smoke.

### File Structure Requirements

- Keep harness code in a clearly named package under test tooling (recommended: `src/test/java/.../smoke/` or `src/test/java/.../tools/`) and run via a dedicated `JavaExec` Gradle task.
- Do not mix host-required smoke with CI-safe unit tests; keep them separate by task and naming.
- Place human checklist/runbook in `docs/engineering/` or `docs/sprint-artifacts/` (choose one and link it from the story).

### Testing Requirements

- Add CI-safe unit tests for:
  - argument parsing (host/port defaults, endpoint path default)
  - mutation gating behavior (safe mode must never call mutating tools)
  - response envelope parsing and error surfacing
- Host-required smoke itself is validated by running `./gradlew mcpSmokeTest` against a real Bitwig session.

### Previous Story Intelligence

- N/A (first story in Epic 1).

### Git Intelligence Summary

- MCP server entrypoint is `src/main/java/io/github/fabb/wigai/WigAIExtension.java` (endpoint path constant: `/mcp`).
- The tool surface is registered in `src/main/java/io/github/fabb/wigai/mcp/McpServerManager.java` (baseline tools include `status`, transport start/stop, track/device/scene/clip reads and clip/scene launch actions).
- Jetty lifecycle and binding is managed by `src/main/java/io/github/fabb/wigai/server/JettyServerManager.java` and config via `src/main/java/io/github/fabb/wigai/config/*`.
- There is existing unit/integration test scaffolding under `src/test/java/io/github/fabb/wigai/` (use it as patterns for logging + envelope assertions).

### Latest Technical Information

- Network access is restricted for this run; do not rely on fetching external docs at runtime.
- If MCP SDK client/transport behavior is unclear, confirm by inspecting the SDK JARs already present in the Gradle cache and/or reading upstream docs during implementation time.

### Project Context Reference

- Epic 1 test design (risk + coverage expectations): `docs/test-design-epic-1.md`

### Story Completion Status

- Set `development_status[1-1-repeatable-mcp-smoke-test-harness-checklist] = ready-for-dev` in `docs/sprint-artifacts/sprint-status.yaml`.
- Completion note: “Ultimate context engine analysis completed — comprehensive developer guide created.”

## References

- Epic + acceptance criteria: `docs/epics.md`
- Test design for Epic 1: `docs/test-design-epic-1.md`
- Architecture overview + transport notes: `docs/architecture.md`
- API reference (baseline tool names): `docs/reference/api-reference.md`
- MCP server + tools registration: `src/main/java/io/github/fabb/wigai/mcp/McpServerManager.java`
- Jetty server lifecycle/binding: `src/main/java/io/github/fabb/wigai/server/JettyServerManager.java`
- MCP endpoint path constant: `src/main/java/io/github/fabb/wigai/WigAIExtension.java`
- Build + dependency versions: `build.gradle.kts`

## Dev Agent Record

### Context Reference

- Runbook: `docs/engineering/mcp-smoke-test-runbook.md`

### Agent Model Used

Claude Opus 4.5

### Debug Log References

<!-- Add harness run output path(s) or pasted excerpts when running against Bitwig -->

### Completion Notes List

- Ultimate context engine analysis completed — comprehensive developer guide created.
- Implemented MCP smoke harness with ATDD red-green-refactor cycle
- Created `McpSmokeHarness` with safe mode (read-only) and mutation mode
- Created `HttpMcpClient` for HTTP-based MCP JSON-RPC communication
- Created `McpSmokeHarnessMain` CLI entrypoint with argument parsing
- Added `mcpSmokeTest` Gradle task for host-required smoke testing
- Created comprehensive runbook at `docs/engineering/mcp-smoke-test-runbook.md`
- All 5 ATDD tests pass, all CI-safe unit tests pass

**Code Review Follow-ups (2025-12-18):**
- Removed CLI `--mutations` flag to enforce env-var-only gating (AC4 compliance)
- Expanded baseline tool validation to full read-only tool set (AC2 compliance)
- Replaced substring-based error detection with proper JSON envelope parsing
- Made mutation mode fail-fast when required tools are missing
- Fixed File List to include only actually changed files
- Added 8 CI-safe envelope parsing tests
- All 25 CI-safe tests pass, all 7 ATDD tests pass

**Code Review Follow-ups Round 2 (2025-12-23):**
- Added explicit `assertToolIsSafeForSafeMode()` guard with 2 unit tests
- Implemented `extractToolResult()` in HttpMcpClient to handle non-text and multi-content responses with 6 unit tests
- Removed duplicate URL printing from CLI (harness already prints it)
- Updated runbook baseline-tool description to reflect full tool set validation
- All 37 CI-safe tests pass

**Code Review Follow-ups Round 3 (2025-12-23):**
- Aligned MUTATING_TOOLS with actual MCP tool names from source (`set_selected_device_parameter`, `set_selected_device_parameters`, `session_launchSceneByIndex`, `session_launchSceneByName`, `launch_clip`)
- Added envelope validation for mutation-mode transport_start/transport_stop and set_selected_device_parameter responses
- Fixed File List to include all files created for this story (McpClient.java, McpSmokeHarnessArgs.java)
- Updated tests to use correct tool names
- All 37 CI-safe tests pass

**Code Review Follow-ups Round 4 (2025-12-23):**
- Safe mode now fails on typed errors for tools other than `get_selected_device_parameters` to keep pass/fail meaningful
- JSON-RPC error responses now wrapped in `{status:"error"}` envelope for parseEnvelope compatibility
- Added `docs/test-review.md` to File List
- Added 2 new tests: `safeModeFailsOnTypedErrorForNonDeviceTools` and `httpMcpClient_jsonRpcErrorParsableByParseEnvelope`
- All 40 CI-safe tests pass

**Code Review Follow-ups Round 5 (2025-12-23):**
- Added `listToolsRaw()` to McpClient interface for full tools/list JSON output (AC2 compliance)
- Updated harness to print pretty-printed full tools/list JSON to stdout
- Updated runbook typed-error section to document safe-mode failure behavior
- Added `escapeJsonString()` helper for safe JSON string escaping in error envelopes
- Promoted ATDD tests to CI: renamed `McpSmokeHarnessAtddRedTest` → `McpSmokeHarnessAtddTest`, removed `@Tag("atdd_red")`
- Added 4 new escape-related tests
- All 51 CI-safe tests pass (including 7 promoted ATDD tests)

### File List

- `src/test/java/io/github/fabb/wigai/smoke/McpClient.java` (new)
- `src/test/java/io/github/fabb/wigai/smoke/McpSmokeHarness.java` (new)
- `src/test/java/io/github/fabb/wigai/smoke/McpSmokeHarnessArgs.java` (new)
- `src/test/java/io/github/fabb/wigai/smoke/McpSmokeHarnessArgsTest.java` (new)
- `src/test/java/io/github/fabb/wigai/smoke/McpSmokeHarnessAtddTest.java` (new, renamed from McpSmokeHarnessAtddRedTest.java)
- `src/test/java/io/github/fabb/wigai/smoke/McpSmokeHarnessMain.java` (new)
- `src/test/java/io/github/fabb/wigai/smoke/HttpMcpClient.java` (new)
- `build.gradle.kts` (modified - added mcpSmokeTest task)
- `docs/engineering/mcp-smoke-test-runbook.md` (new)
- `docs/sprint-artifacts/sprint-status.yaml` (modified)
- `docs/sprint-artifacts/1-1-repeatable-mcp-smoke-test-harness-checklist.md` (new)
- `docs/test-review.md` (new)
- `docs/atdd-checklist-1-1-repeatable-mcp-smoke-test-harness-checklist.md` (new)
- `docs/sprint-artifacts/validation-report-2025-12-18T15-37-47-07-00.md` (new)
- `docs/test-design-epic-1.md` (new)
