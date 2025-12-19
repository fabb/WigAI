# Story 1.1: Repeatable MCP Smoke Test Harness + Checklist

Status: ready-for-dev

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

- [ ] Implement a host-required smoke harness runnable via Gradle
  - [ ] Add a dedicated CLI entrypoint (Java) that can connect to an MCP server over HTTP SSE/streamable transport
  - [ ] Read `mcpHost`, `mcpPort`, optional `mcpEndpointPath` (default `/mcp`) and print the resolved URL
  - [ ] Implement a strict timeout budget (connect timeout + per-operation timeout) and ensure the process exits with non-zero on failure
- [ ] Implement “safe mode” (default) read-only checks
  - [ ] Run MCP discovery and print the full observed `tools/list` output (or a summarized list + full JSON in a debug section)
  - [ ] Assert baseline tools exist (minimum: `status`; also validate presence of current baseline set)
  - [ ] Execute read-only tools and validate response envelopes are parseable and actionable
- [ ] Implement mutation-gated checks (off by default)
  - [ ] Gate mutations behind `WIGAI_SMOKE_TEST_MUTATIONS=true` (env var only; no accidental mutation via default args)
  - [ ] Execute `transport_start` then `transport_stop` and validate results and that the extension remains responsive
  - [ ] (Optional) Device parameter “round-trip” only if a device is selected (skip with explicit message otherwise)
- [ ] Ensure negative-path “no device selected” is validated
  - [ ] Call `get_selected_device_parameters` with no device selected and assert a typed error (no stack trace / no unhandled exception)
- [ ] Add a human-readable checklist + runbook for Josh
  - [ ] Document prerequisites (Bitwig running, WigAI enabled, host/port known)
  - [ ] Provide commands for safe mode and mutation mode
  - [ ] Provide troubleshooting guidance and “what to attach to a bug report” (logs + harness output)
- [ ] Wire up `./gradlew mcpSmokeTest`
  - [ ] Add a `mcpSmokeTest` Gradle task that runs the harness without requiring the full `test` suite
  - [ ] Ensure it is NOT part of default CI gating (Bitwig host required)

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

<!-- (Optional) add a follow-up story-context file if the team adopts separate context artifacts -->

### Agent Model Used

GPT-5.2

### Debug Log References

<!-- Add harness run output path(s) or pasted excerpts when running against Bitwig -->

### Completion Notes List

- Ultimate context engine analysis completed — comprehensive developer guide created.

### File List

- `docs/sprint-artifacts/1-1-repeatable-mcp-smoke-test-harness-checklist.md`
