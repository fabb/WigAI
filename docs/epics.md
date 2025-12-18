---
stepsCompleted: [1, 2, 3, 4]
inputDocuments:
  - docs/prd.md
  - docs/architecture.md
  - docs/prd/index.md
---

# WigAI - Epic Breakdown

## Overview

This document provides the complete epic and story breakdown for WigAI, decomposing the requirements from the PRD, UX Design if it exists, and Architecture requirements into implementable stories.

## Requirements Inventory

### Functional Requirements

FR1: An external AI agent can connect to WigAI via MCP and invoke supported tools.
FR2: An external AI agent can receive structured success responses for each tool invocation.
FR3: An external AI agent can receive structured error responses when a request cannot be completed (e.g., missing target track, occupied slot without overwrite).
FR4: An external AI agent can invoke a batch operation that creates and writes multiple clips in one request.
FR5: An external AI agent can target a track by `track_index`.
FR6: An external AI agent can target a track by exact `track_name`.
FR7: If no track is specified, the system can target the currently selected Bitwig track.
FR8: If neither an explicit track nor a selected track is available, the system can refuse and request explicit track targeting.
FR9: An external AI agent can request track resolution by fuzzy name matching and receive candidate matches suitable for user confirmation.
FR10: The system can refuse to act when fuzzy matching is ambiguous unless the agent confirms the intended track explicitly.
FR11: An external AI agent can create a new launcher MIDI clip on a target track.
FR12: If `scene_index` is not specified, the system can place the new clip in the next empty launcher slot on that track (scanning forward as needed).
FR13: If `scene_index` is specified and the slot is occupied, the system can refuse unless overwrite is explicitly enabled.
FR14: The system can avoid overwriting existing clips by default (`overwrite=false`).
FR15: An external AI agent can explicitly enable overwriting an occupied target slot.
FR16: An external AI agent can name a clip explicitly at creation time.
FR17: An external AI agent can provide an “inferred” clip name (derived from the request), and the system can apply it.
FR18: An external AI agent can write explicit musical note data into a newly created launcher clip.
FR19: The system can support step-grid style note data as an input form for writing notes.
FR20: The system can support beat-time style note data as an input form for writing notes.
FR21: The system can validate note inputs (e.g., timing and pitch ranges) and refuse invalid payloads with actionable errors.
FR22: The system can support clearing/replacing prior clip content when explicitly requested as part of a write operation.
FR23: The system can report how many notes were written and how many were rejected/skipped (with reasons).
FR24: An external AI agent can launch a scene (launcher row) by index.
FR25: When creating a clip, the system can launch the resulting scene by default (`launch=true`).
FR26: When creating a clip, an external AI agent can suppress launching (`launch=false`) to support batching workflows.
FR27: An external AI agent can start Bitwig transport.
FR28: An external AI agent can stop Bitwig transport.
FR29: An external AI agent can launch an existing clip by specifying a target track and slot/scene index.
FR30: An external AI agent can read the 8 remote-control parameter values of the currently selected/cursor device.
FR31: An external AI agent can set one or more of the 8 remote-control parameter values of the currently selected/cursor device.

### NonFunctional Requirements

NFR1: For normal-sized requests, WigAI completes the end-to-end create→write→(optional) launch workflow in under 10 seconds.
NFR2: WigAI avoids blocking Bitwig Studio’s UI thread with long-running work to preserve a responsive DAW experience.
NFR3: WigAI does not crash Bitwig Studio; all tool failures are handled gracefully.
NFR4: WigAI retries transient failures (e.g., timing/availability issues in host state) before returning an error, using a bounded retry policy.
NFR5: WigAI returns clear error results when a request cannot be completed, without leaving Bitwig in an inconsistent or partially broken state.
NFR6: The MCP server binds to `localhost` by default for MVP.
NFR7: The MCP interface remains stable and backward-compatible within a release line so MCP clients can reliably call tools over time.
NFR8: No authentication is required for MVP, relying on localhost-only binding as the primary guardrail.
NFR9: WigAI logs each tool invocation and its outcome (success/failure and reason codes).
NFR10: WigAI avoids logging full note payloads by default; verbose logging (including note payloads) is enabled only via an explicit debug flag.

### Additional Requirements

- Brownfield continuation: do not re-scaffold from a Bitwig extension template; avoid heavy frameworks (e.g., Spring).
- Transport: implement MCP SSE transport now; adopt Streamable HTTP when supported by the MCP Java SDK.
- Tools-as-API: expose functionality exclusively as MCP “tools” (SDK tool interfaces + registry), not bespoke command handlers.
- Layering: MCP tools parse/validate args → call controller/service layer → return structured response; no direct Bitwig API calls from MCP tool classes.
- Naming: tool names MUST be `snake_case` verbs; request fields MUST be `snake_case` (e.g., `track_name`, `track_index`, `scene_index`, `request_id`).
- Error handling: all tool handlers MUST run through the unified error handling path and use the existing error code taxonomy.
- Validation: perform strict input validation at the tool boundary (pitch/time/duration/velocity ranges; bounded request sizes) and return actionable errors.
- Reliability: classify host-state timing/availability errors as retryable vs fatal consistently and apply a bounded retry policy for transient failures.
- Observability: log every tool invocation with `tool_name`, outcome, and `request_id` (when provided).
- Log hygiene: do not log full note payloads by default; allow full payload logging only when an explicit debug mode/flag is enabled.

### FR Coverage Map

### FR Coverage Map

FR1: Epic 1 - MCP connectivity and baseline tool invocation
FR2: Epic 1 - Structured success responses across tools
FR3: Epic 1 - Structured error responses across tools
FR4: Epic 5 - Batch operation (create+write multiple clips)
FR5: Epic 2 - Track targeting by `track_index`
FR6: Epic 2 - Track targeting by exact `track_name`
FR7: Epic 2 - Default to currently selected Bitwig track when unspecified
FR8: Epic 2 - Refuse and request explicit targeting when no track context exists
FR9: Epic 2 - Fuzzy track resolution and candidate match responses
FR10: Epic 2 - Refuse to act when fuzzy match ambiguous without confirmation
FR11: Epic 3 - Create a new launcher MIDI clip on target track
FR12: Epic 3 - Next-empty-slot placement when `scene_index` omitted
FR13: Epic 3 - Occupied slot refusal unless overwrite explicitly enabled
FR14: Epic 3 - Non-destructive default (`overwrite=false`)
FR15: Epic 3 - Explicit overwrite enablement
FR16: Epic 3 - Explicit clip naming at creation
FR17: Epic 3 - Apply agent-provided inferred clip name
FR18: Epic 4 - Write explicit note data into newly created launcher clip
FR19: Epic 4 - Support step-grid note input form
FR20: Epic 4 - Support beat-time note input form
FR21: Epic 4 - Validate note inputs and return actionable errors
FR22: Epic 4 - Clear/replace prior clip content when explicitly requested
FR23: Epic 4 - Report counts written and rejected/skipped with reasons
FR24: Epic 3 - Launch a scene by index
FR25: Epic 3 - Default `launch=true` on clip creation
FR26: Epic 3 - Support `launch=false` for batching workflows
FR27: Epic 1 - Start Bitwig transport
FR28: Epic 1 - Stop Bitwig transport
FR29: Epic 3 - Launch an existing clip by track + slot/scene index
FR30: Epic 1 - Read 8 remote-control parameter values of cursor device
FR31: Epic 1 - Set one or more of the 8 remote-control parameter values

## Epic List

### Epic 1: Reliable MCP Control Surface (Baseline Verification + Hardening)

External AI agents can reliably connect to WigAI and invoke the existing tool surface with consistent success/error envelopes, logging, and safe runtime behavior (no Bitwig crashes; no UI-thread blocking) so we can build new capabilities on a stable foundation.

**FRs covered:** FR1, FR2, FR3, FR27, FR28, FR30, FR31

### Epic 2: Safe Track Targeting & Discovery

External AI agents can reliably target the intended track (by index/name/selected) with guardrails (explicit refusal when context is missing; fuzzy matching support; refusal on ambiguity) so actions occur on the correct track without surprises.

**FRs covered:** FR5, FR6, FR7, FR8, FR9, FR10

### Epic 3: Safe Launcher Clip Creation & Audition (Placement + Overwrite Guardrails + Launch)

External AI agents can create launcher MIDI clips with predictable placement defaults and explicit non-destructive overwrite behavior, and can launch scenes/clips for auditioning, enabling fast iteration without accidental destructive edits.

**FRs covered:** FR11, FR12, FR13, FR14, FR15, FR16, FR17, FR24, FR25, FR26, FR29

### Epic 4: Deterministic MIDI Writing (Step-Grid + Beat-Time + Validation)

External AI agents can write explicit MIDI note payloads into launcher clips deterministically (step-grid and beat-time forms), with strict validation, optional clear/replace semantics, and accurate reporting of applied vs rejected notes.

**FRs covered:** FR18, FR19, FR20, FR21, FR22, FR23

### Epic 5: Batch Clip Generation Workflow

External AI agents can perform batch operations that create and write multiple clips in a single request, supporting workflows that generate variations efficiently (typically with `launch=false` and later auditioning).

**FRs covered:** FR4

## Epic 1: Reliable MCP Control Surface (Baseline Verification + Hardening)

External AI agents can reliably connect to WigAI and invoke the existing tool surface with consistent success/error envelopes, logging, and safe runtime behavior (no Bitwig crashes; no UI-thread blocking) so we can build new capabilities on a stable foundation.

### Story 1.1: Repeatable MCP Smoke Test Harness + Checklist

As a WigAI developer (Josh),
I want a repeatable smoke-test harness + checklist for the running Bitwig extension,
So that MCP regressions and integration issues are caught early before we build the MIDI workflow.

**Acceptance Criteria:**

**Given** Bitwig Studio is running with WigAI enabled and a known `host:port`
**When** I run a local harness (e.g., `./gradlew mcpSmokeTest -PmcpHost=localhost -PmcpPort=61169`)
**Then** it connects to `http://{host}:{port}/mcp` and reports a clear pass/fail with actionable diagnostics.

**Given** the harness is connected
**When** it performs MCP discovery (e.g., `tools/list`)
**Then** it asserts baseline tools exist and prints the full tool list it observed (including at minimum `status`).

**Given** the harness is run in default “safe mode”
**When** it executes its checks
**Then** it performs read-only validations only (e.g., `status`, `list_tracks`, `get_track_details`, `list_devices_on_track`, `get_device_details`, `list_scenes`, `get_clips_in_scene`, and any other non-mutating tools available).

**Given** the harness is run with an explicit mutation flag (e.g., `WIGAI_SMOKE_TEST_MUTATIONS=true`)
**When** it executes its mutation checks
**Then** it can exercise a minimal set of safe mutations and restore state where applicable (e.g., `transport_start` then `transport_stop`; optional device parameter round-trip).

**Given** no device is selected
**When** the harness invokes `get_selected_device_parameters`
**Then** it returns a typed/structured error (e.g., `DEVICE_NOT_SELECTED`) rather than an unhandled exception.

### Story 1.2: Localhost Binding Defaults + Preferences Guardrails

As a WigAI user,
I want the MCP server to bind to `localhost` by default with strong preference/input guardrails,
So that WigAI isn’t accidentally exposed on the network (no-auth MVP) and connection details stay predictable.

**Acceptance Criteria:**

**Given** WigAI is enabled for the first time in Bitwig
**When** the MCP server starts
**Then** it binds to `localhost` on the default port `61169` and advertises `http://localhost:61169/mcp` in logs/notification.

**Given** the user edits “MCP Host” in Bitwig preferences
**When** the host value is empty or whitespace
**Then** it is sanitized to `localhost` and the server remains reachable at a loopback address.

**Given** the user attempts to set “MCP Host” to a non-loopback value (e.g., `0.0.0.0`, `192.168.x.x`, a public hostname)
**When** the setting is applied
**Then** WigAI refuses for MVP (no-auth) and reverts to `localhost`, logging a clear warning explaining why.

**Given** the user changes “MCP Port” to another valid port (1024–65535)
**When** the setting is applied
**Then** WigAI performs a graceful restart and the MCP endpoint is reachable at `http://localhost:{new_port}/mcp`.

**Given** the configured port cannot be bound (e.g., already in use)
**When** WigAI tries to start or restart the server
**Then** it reports a clear, actionable error (suggesting choosing another port) and does not crash Bitwig.

### Story 1.3: Standardize Baseline Tool Response Envelopes (Align With `status` Tool + API Reference)

As an external AI agent developer,
I want every WigAI MCP tool to return responses in a consistent, documented envelope,
So that my client can parse success/error reliably across tools (including `status`) and handle failures predictably.

**Acceptance Criteria:**

**Given** any baseline tool is invoked successfully
**When** it returns a response
**Then** the response JSON (single text content payload) has `status: "success"`, contains a `data` field with the tool-specific payload, and is not double-wrapped.

**Given** any baseline tool fails
**When** it returns an error
**Then** the response JSON has `status: "error"`, an `error` object with `code` (from `ErrorCode`), `message` (actionable), and `operation` (equal to the invoked tool name).

**Given** the `status` tool is invoked
**When** it succeeds
**Then** `data` is an object containing at minimum: `wigai_version`, `project_name`, `audio_engine_active`, `transport`, `project_parameters`, `selected_track`, `selected_device`, and `selected_clip_slot`.

**Given** one or more `status` sub-fetches fail (e.g., transport or selected device lookup)
**When** `status` returns
**Then** it still returns `status: "success"` with best-effort defaults, and includes `partial_failures` (array of strings) plus `status_note` (human-readable summary).

**Given** unit tests exist for baseline tools
**When** they run
**Then** they assert envelope compliance for both success and error paths, including a regression check preventing double-wrapping.

**Given** `docs/reference/api-reference.md` documents tool responses
**When** Story 1.3 is complete
**Then** the `status` section matches the real envelope + payload shape (including partial failure fields/behavior).

### Story 1.4: Logging + `request_id` Correlation Hardening (Mutating Tools Only)

As a WigAI developer,
I want mutating tools to log each invocation with consistent correlation (`request_id` when provided) and outcome metadata,
So that I can reliably debug failures and performance issues without logging sensitive or large payloads.

**Acceptance Criteria:**

**Given** any MCP tool is invoked
**When** it executes
**Then** WigAI logs an “operation started” and “operation finished” entry including at minimum: `tool_name` and outcome (success or failure).

**Given** a client supplies `request_id` in the tool arguments for a mutating tool
**When** the tool executes
**Then** all logs for that invocation include the `request_id` value (so I can correlate client→server→error).

**Given** a baseline mutating tool is invoked (`transport_start`, `transport_stop`, `launch_clip`, `session_launchSceneByIndex`, `set_selected_device_parameter`, `set_selected_device_parameters`)
**When** it is called
**Then** it accepts an optional `request_id` field without breaking existing clients, and uses it for correlation in logs.

**Given** a mutating tool fails
**When** it returns an error response
**Then** logs include the standardized `ErrorCode` (same code as returned in the MCP envelope) and the `request_id` if present.

**Given** a tool has potentially large inputs (current or future)
**When** logging parameters
**Then** it logs summaries only (counts/shape) and does not log full payloads by default (especially future note payloads), aligning with the “no full payload logging unless debug” rule.

**Given** unit tests exist for baseline tools and/or the error handler
**When** tests run
**Then** at least one baseline mutating tool test asserts that providing `request_id` results in it being included in the structured logging parameters/context for that operation.

### Story 1.5: Non-Blocking Execution + Bounded Retry Verification (Baseline Tools)

As a WigAI user,
I want baseline tools to be responsive and resilient to transient Bitwig host-state timing issues,
So that WigAI remains trustworthy (no DAW freezes/crashes) and requests complete reliably within the expected time bounds.

**Acceptance Criteria:**

**Given** baseline tools are invoked under normal conditions
**When** they execute
**Then** they complete without blocking Bitwig responsiveness and typical invocations complete within the performance expectation for “normal-sized requests”.

**Given** a baseline tool encounters a transient, retryable Bitwig host-state timing/availability failure
**When** the operation is executed
**Then** WigAI retries using a bounded retry policy (max attempts + backoff) and either succeeds or returns a clear error without hanging indefinitely.

**Given** a baseline tool encounters a non-retryable failure (e.g., invalid parameters, missing target track/device)
**When** the operation is executed
**Then** WigAI does not retry and returns the standardized error response immediately with an actionable message.

**Given** retries are performed
**When** WigAI logs the operation
**Then** logs clearly indicate retry attempts, final outcome (success/failure), and total duration for the invocation.

**Given** the smoke test harness from Story 1.1 is available
**When** it is run in a “timing stress” mode (manual or scripted)
**Then** it can validate that tools do not hang and that failures are surfaced as bounded, actionable errors rather than Bitwig instability.

## Epic 2: Safe Track Targeting & Discovery

External AI agents can reliably target the intended track (by index/name/selected) with guardrails (explicit refusal when context is missing; fuzzy matching support; refusal on ambiguity) so actions occur on the correct track without surprises.

### Story 2.1: Standard Track Targeting Contract (Index/Exact Name/Selected Default)

As an external AI agent developer,
I want a consistent way to target tracks across WigAI tools,
So that I can reliably act on the intended track without custom per-tool rules.

**Acceptance Criteria:**

**Given** a tool supports track targeting
**When** the request includes `track_index`
**Then** WigAI targets the track by 0-based index and returns `INVALID_RANGE` if the index is out of bounds.

**Given** a tool supports track targeting
**When** the request includes `track_name`
**Then** WigAI targets the track by exact, case-insensitive name match (after trimming) and returns `TRACK_NOT_FOUND` if no exact match exists.

**Given** a tool supports track targeting
**When** neither `track_index` nor `track_name` is provided
**Then** WigAI targets the currently selected Bitwig track.

**Given** a tool supports track targeting
**When** neither `track_index` nor `track_name` is provided and no Bitwig track is currently selected
**Then** WigAI refuses the request with `TRACK_NOT_FOUND` and a message instructing the client to provide `track_index` or `track_name`.

**Given** a request provides both `track_index` and `track_name`
**When** the request is validated
**Then** WigAI refuses with `INVALID_PARAMETER` (exactly one targeting mode must be used).

### Story 2.2: `resolve_track` Tool (Deterministic “Fuzzy” Matching → Candidate List + Ambiguity)

As an external AI agent developer,
I want WigAI to resolve a fuzzy track query into candidate tracks,
So that I can ask the user to confirm the intended track before executing actions.

**Acceptance Criteria:**

**Given** the client calls `resolve_track` with a required `query` string
**When** WigAI searches tracks
**Then** matching is deterministic and uses simple, predictable rules: case-insensitive exact match, then case-insensitive prefix match, then case-insensitive substring match.

**Given** `resolve_track` finds one or more matches
**When** it returns success
**Then** `data` includes an ordered `candidates` list where each entry includes at minimum `track_index`, `track_name`, and `match_type` (`exact`, `prefix`, or `substring`).

**Given** `resolve_track` finds multiple plausible matches
**When** it returns success
**Then** it clearly signals ambiguity (e.g., `ambiguous=true`) and does not select a track implicitly.

**Given** multiple tracks share the same exact `track_name`
**When** `resolve_track` returns
**Then** it is treated as ambiguous and the client must confirm using `track_index` (even if the name matches exactly).

**Given** `resolve_track` finds no matches
**When** it returns
**Then** it returns a standardized error with `TRACK_NOT_FOUND` and an actionable message (e.g., suggesting `list_tracks`).

### Story 2.3: Apply Track Targeting Contract to Existing Mutating Tools + Documentation

As an external AI agent developer,
I want mutating tools to support the standard track targeting contract and have examples of the “resolve → confirm → act” flow,
So that automation remains safe and predictable as the tool surface expands.

**Acceptance Criteria:**

**Given** an existing mutating tool operates on a track (e.g., `launch_clip`)
**When** the client provides `track_index`
**Then** the tool accepts it as an alternative to `track_name` without breaking existing clients that still send `track_name`.

**Given** a mutating tool supports both `track_index` and `track_name`
**When** both are provided in the same request
**Then** the tool refuses with `INVALID_PARAMETER`.

**Given** Epic 2 is implemented
**When** documentation is updated
**Then** `docs/reference/api-reference.md` includes a clear example showing the recommended workflow:
1. Call `resolve_track` with a fuzzy query to get candidates.
2. Ask the user to confirm the intended track.
3. Call the mutating tool using `track_index` for unambiguous targeting.

## Epic 3: Safe Launcher Clip Creation & Audition (Placement + Overwrite Guardrails + Launch)

External AI agents can create launcher MIDI clips with predictable placement defaults and explicit non-destructive overwrite behavior, and can audition results reliably without accidental destructive edits.

### Story 3.1: Resolve Target Track + Slot for Launcher Clip Creation

As an external AI agent developer,
I want WigAI to deterministically resolve the target track and launcher slot for clip creation,
So that clip placement is predictable and safe by default.

**Acceptance Criteria:**

**Given** a create-clip request provides `track_index` or `track_name` (or neither)
**When** WigAI resolves track targeting
**Then** it follows the standard targeting contract (track by `track_index`, else exact `track_name` case-insensitive, else selected track) and refuses with `TRACK_NOT_FOUND` when no track context exists.

**Given** the request provides `scene_index`
**When** WigAI resolves the target slot
**Then** it targets that exact launcher slot index and does not auto-relocate to a different slot.

**Given** the request omits `scene_index`
**When** WigAI resolves the target slot
**Then** it scans forward from `start_scene_index` (default 0) to find the next empty launcher slot on the target track.

**Given** the request provides `scene_index` and the slot is occupied
**When** `overwrite=false` (default)
**Then** WigAI refuses with a standardized error explaining the slot is occupied and that the client can re-run with `overwrite=true` or omit `scene_index` to use next-empty-slot placement.

**Given** the request provides `scene_index` and the slot is occupied
**When** `overwrite=true`
**Then** WigAI proceeds using that exact slot and marks the operation as an overwrite in the eventual result metadata.

### Story 3.2: Create Launcher MIDI Clip With Naming + Overwrite Guardrails

As an external AI agent,
I want WigAI to create a new launcher MIDI clip in the resolved slot with safe overwrite behavior and naming support,
So that the clip is ready for deterministic note writing in the next epic.

**Acceptance Criteria:**

**Given** a target track and target slot have been resolved
**When** WigAI creates the launcher MIDI clip
**Then** a new MIDI clip exists in that slot after the operation completes.

**Given** the target slot is occupied
**When** `overwrite=false`
**Then** WigAI does not modify the existing clip and returns a standardized error.

**Given** the target slot is occupied
**When** `overwrite=true`
**Then** WigAI replaces the existing clip content in a controlled way and returns result metadata indicating an overwrite occurred.

**Given** the request includes `clip_name`
**When** the clip is created
**Then** WigAI sets the created clip’s name to `clip_name`.

**Given** the request omits `clip_name` but includes `inferred_clip_name`
**When** the clip is created
**Then** WigAI sets the created clip’s name to `inferred_clip_name`.

**Given** the request omits both `clip_name` and `inferred_clip_name`
**When** the clip is created
**Then** WigAI leaves the clip name unchanged (Bitwig default) and returns the resulting name if available.

**Given** the request omits `clip_length_beats`
**When** the clip is created
**Then** WigAI uses a default length of 16 beats (4 bars at 4/4) for the created clip.

### Story 3.3: `create_launcher_midi_clip` MCP Tool (Composable Primitive)

As an external AI agent developer,
I want a single MCP tool that creates a launcher MIDI clip safely with predictable placement and optional audition behavior,
So that I can implement the end-to-end “intent → clip” workflow using composable primitives.

**Acceptance Criteria:**

**Given** the client calls `create_launcher_midi_clip`
**When** the request is validated
**Then** all request fields are `snake_case` and the tool supports:
- Track targeting (`track_index` or `track_name`, else selected track)
- Slot targeting (`scene_index` optional, else next-empty via scan from `start_scene_index` default 0)
- Overwrite control (`overwrite` default false)
- Naming (`clip_name` optional, `inferred_clip_name` optional)
- Length (`clip_length_beats` optional, default 16)
- Audition (`launch` default true)

**Given** `launch=true`
**When** clip creation succeeds
**Then** WigAI launches the created clip slot for auditioning (not the full scene row across all tracks).

**Given** `launch=false`
**When** clip creation succeeds
**Then** WigAI does not launch anything (supports batching workflows).

**Given** the tool returns success
**When** the response is inspected
**Then** it returns structured metadata sufficient for the client to explain what happened, including at minimum: `track_index`, `track_name`, `scene_index` (or `slot_index`), `clip_name`, `overwrite` (whether an overwrite occurred), and `launched` (true/false).

### Story 3.4: `launch_scene` MCP Tool (Snake-Case Alias for Scene Row Launch)

As an external AI agent developer,
I want a snake_case `launch_scene` tool for launching a scene by index,
So that the API aligns with naming conventions while maintaining backward compatibility with existing tools.

**Acceptance Criteria:**

**Given** the client calls `launch_scene` with `scene_index`
**When** the request succeeds
**Then** WigAI launches the full scene row across tracks and returns a standardized success response.

**Given** an older tool name exists for the same behavior (e.g., `session_launchSceneByIndex`)
**When** the system is upgraded
**Then** the old tool remains available for backward compatibility and both tools produce consistent response envelopes.

### Story 3.5: Update API Reference for New Clip-Creation and Scene-Launch Tools

As an external AI agent developer,
I want `docs/reference/api-reference.md` to match the implemented tools and targeting rules,
So that I can build against WigAI without reading its source code.

**Acceptance Criteria:**

**Given** Epic 3 tools are implemented
**When** documentation is updated
**Then** `docs/reference/api-reference.md` includes `create_launcher_midi_clip` and `launch_scene` with accurate schemas, examples, defaults, and error cases (including occupied-slot refusal messaging and the meaning of `launch=true`).

## Epic 4: Deterministic MIDI Writing (Step-Grid + Beat-Time + Validation)

External AI agents can write explicit MIDI note payloads into launcher clips deterministically (step-grid and beat-time forms), with strict validation, optional clear/replace semantics, and accurate reporting of applied vs rejected notes.

### Story 4.1: Define Deterministic Note Payload Contracts (Beat-Time + Step-Grid)

As an external AI agent developer,
I want a clear, stable note payload schema for both beat-time and step-grid representations,
So that I can generate deterministic note instructions without ambiguity.

**Acceptance Criteria:**

**Given** a note payload is provided in beat-time form
**When** it is validated
**Then** each note includes at minimum: `start_beat`, `duration_beats`, `pitch`, and `velocity`, and all fields are `snake_case`.

**Given** a note payload is provided in step-grid form
**When** it is validated
**Then** each note includes at minimum: `step_index`, `duration_steps`, `pitch`, and `velocity`, and the request defines `steps_per_beat`.

**Given** velocity is provided
**When** it is validated
**Then** velocity is interpreted as MIDI integer `0–127`.

### Story 4.2: Strict Validation + Atomic Write Semantics (No Partial Writes)

As a WigAI user,
I want note writing to be strict and atomic,
So that invalid payloads never result in partially-written clips or unpredictable edits.

**Acceptance Criteria:**

**Given** a write request contains any invalid note(s) (e.g., out-of-range pitch, negative timing, invalid velocity, invalid duration)
**When** WigAI validates the request
**Then** WigAI refuses the request with a standardized error and writes no notes to the target clip.

**Given** a write request is valid
**When** WigAI executes the write
**Then** it writes all provided notes exactly as specified (timing, duration, pitch, velocity).

**Given** a write request targets a clip that already has note content
**When** `clear_existing=false` (default)
**Then** WigAI refuses with a standardized error instructing the client to re-run with `clear_existing=true`.

### Story 4.3: `write_launcher_clip_notes` MCP Tool (Composable Primitive)

As an external AI agent developer,
I want a single MCP tool to write notes deterministically into a launcher clip using the approved schemas,
So that I can compose “create clip” + “write notes” + “(optional) launch” workflows safely.

**Acceptance Criteria:**

**Given** the client calls `write_launcher_clip_notes`
**When** the request is validated
**Then** it supports targeting the clip via `track_index`/`track_name` + `scene_index` (slot), and refuses if targeting is ambiguous or missing.

**Given** the request includes beat-time notes
**When** validation passes
**Then** WigAI writes the notes into the target clip using `start_beat` and `duration_beats`.

**Given** the request includes step-grid notes
**When** validation passes
**Then** WigAI converts `step_index`/`duration_steps` using `steps_per_beat` into beat-time and writes the notes accordingly.

**Given** the request omits `clear_existing`
**When** the tool runs
**Then** `clear_existing` defaults to `false` and the tool refuses if the target clip already contains notes.

**Given** the request sets `clear_existing=true`
**When** the tool runs
**Then** WigAI clears existing note content first and then writes the full validated note set.

**Given** the tool succeeds
**When** it returns
**Then** it reports `notes_written` and returns enough metadata for the client to confirm the target and outcome.

### Story 4.4: Result Reporting + Error Diagnostics (Actionable, Not Noisy)

As an external AI agent developer,
I want detailed but safe outcome reporting for note writes,
So that I can explain results to the user and troubleshoot failures without leaking large payloads into logs.

**Acceptance Criteria:**

**Given** a write request succeeds
**When** it returns
**Then** the response includes: target identifiers (`track_index`, `track_name`, `scene_index`), `notes_written`, and (if applicable) `cleared_existing=true`.

**Given** a write request fails validation
**When** it returns an error
**Then** the error message is actionable (points to which field/rule failed) and no notes are written.

**Given** WigAI logs the operation
**When** logging parameters for the note payload
**Then** it does not log full note payloads by default; it logs only summaries (e.g., counts and shape) unless an explicit debug mode is enabled.

### Story 4.5: Update API Reference for Note Writing Tool

As an external AI agent developer,
I want `docs/reference/api-reference.md` to document the note writing tool accurately,
So that I can generate valid payloads and understand strict/atomic behavior.

**Acceptance Criteria:**

**Given** Epic 4 is implemented
**When** documentation is updated
**Then** `docs/reference/api-reference.md` includes `write_launcher_clip_notes` with schemas for beat-time + step-grid, examples, defaults (`clear_existing=false`), and strict validation/atomicity rules.

## Epic 5: Batch Clip Generation Workflow

External AI agents can perform batch operations that create and write multiple clips in a single request, supporting workflows that generate variations efficiently (typically with `launch=false` and later auditioning).

### Story 5.1: `batch_create_write_launcher_clips` MCP Tool (Atomic Batch With Per-Item Results)

As an external AI agent developer,
I want a batch tool that can create and write multiple launcher clips in one request,
So that I can generate several variations efficiently while keeping outcomes explicit and safe.

**Acceptance Criteria:**

**Given** the client calls `batch_create_write_launcher_clips`
**When** the request is validated
**Then** it supports an array of clip instructions where each item contains at minimum: track targeting, optional `scene_index` (or next-empty rules), naming fields, clip length, and a note payload (beat-time or step-grid).

**Given** the batch request is valid
**When** it executes
**Then** WigAI processes items sequentially and returns a per-item result array that includes created slot indices and note write counts.

**Given** `launch` behavior is present in the batch tool
**When** the batch executes
**Then** it defaults to `launch=false` at the batch level (batching should not interrupt playback), but allows explicit per-item override when needed.

**Given** any item fails validation
**When** strict batch mode is enabled (default)
**Then** the batch fails atomically and no clips are created or modified.

**Given** partial batch mode is explicitly enabled (opt-in)
**When** an item fails
**Then** WigAI can continue processing subsequent items and reports failures per-item without hiding errors.

### Story 5.2: Guardrails for Batch Safety + Bounded Work

As a WigAI user,
I want clear guardrails on batch operations,
So that batch requests cannot overwhelm Bitwig or cause unintended destructive edits.

**Acceptance Criteria:**

**Given** a batch request exceeds configured limits (e.g., too many clips, too many notes, or too-large payloads)
**When** WigAI validates the request
**Then** it refuses with a standardized error explaining the limit that was exceeded.

**Given** overwrite/clear behavior is involved in a batch
**When** defaults apply
**Then** `overwrite=false` and `clear_existing=false` remain the defaults; destructive actions are only performed when explicitly enabled per item.

### Story 5.3: Update API Reference for Batch Tool + Recommended Workflows

As an external AI agent developer,
I want the API reference to document the batch tool and best-practice usage patterns,
So that I can implement batch variation workflows without trial-and-error.

**Acceptance Criteria:**

**Given** Epic 5 is implemented
**When** documentation is updated
**Then** `docs/reference/api-reference.md` documents `batch_create_write_launcher_clips`, shows examples for `launch=false` batching, and explains strict (atomic) vs partial (opt-in) modes.
