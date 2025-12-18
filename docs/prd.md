---
stepsCompleted: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
inputDocuments:
  - docs/project-brief.md
  - docs/analysis/research/technical-bitwig-midi-clip-creation-research-2025-12-16.md
  - docs/sprint-artifacts/archive/cycle-1-2025-12-15/index.md
  - docs/sprint-artifacts/archive/cycle-1-2025-12-15/epic-1.md
  - docs/sprint-artifacts/archive/cycle-1-2025-12-15/epic-2.md
  - docs/sprint-artifacts/archive/cycle-1-2025-12-15/epic-3.md
  - docs/sprint-artifacts/archive/cycle-1-2025-12-15/epic-4.md
  - docs/sprint-artifacts/archive/cycle-1-2025-12-15/epic-5.md
  - docs/sprint-artifacts/archive/cycle-1-2025-12-15/epic-6.md
  - docs/sprint-artifacts/archive/cycle-1-2025-12-15/epic-7.md
  - docs/sprint-artifacts/archive/cycle-1-2025-12-15/epic-8.md
  - docs/sprint-artifacts/archive/cycle-1-2025-12-15/prd.md
documentCounts:
  briefs: 1
  research: 1
  brainstorming: 0
  projectDocs: 10
workflowType: 'prd'
lastStep: 11
project_name: 'WigAI'
user_name: 'Josh'
date: '2025-12-17T05:26:14Z'
---

# Product Requirements Document - WigAI

**Author:** Josh
**Date:** 2025-12-17T05:26:14Z

## Executive Summary

WigAI is a Bitwig Studio Java extension that exposes DAW control via the Model Context Protocol (MCP) so an external AI agent can assist music production workflows. WigAI is an established, working system (brownfield) with the original MVP capabilities implemented and manually verified in Bitwig. The new product scope extends WigAI with a producer-focused composition workflow: the ability for an AI agent to create a new launcher MIDI clip, write a MIDI sequence into it, and launch the created clip slot safely and predictably.

WigAI does not generate musical notes itself. Instead, an external AI agent translates producer intent (e.g., “a 4‑bar chord progression in G minor”) into explicit, structured note/step data. WigAI’s job is to apply those instructions in Bitwig reliably, with guardrails that prevent destructive edits by default.

### What Makes This Special

WigAI turns “musical intent → actual Bitwig clip” through a DAW-native, tool-driven interface, without requiring the producer to manually step-enter patterns or click through clip slots. The differentiator is safety + control: explicit track targeting (by name or index), predictable placement behavior (next empty scene/slot by default), and explicit confirmation for overwrites. This enables fast iteration for studio producers while preserving trust that the DAW session won’t be unintentionally modified.

## Project Classification

**Technical Type:** desktop_app
**Domain:** general
**Complexity:** low
**Project Context:** Brownfield - extending existing system

This is a Bitwig Studio extension/plugin (running in-process) that exposes an MCP tool surface for an external AI agent. While the integration boundary is API-like (MCP), the product experience is anchored in a producer’s DAW workflow, with real-time and safety constraints typical of desktop creative tooling.

## Success Criteria

### User Success

- A studio producer can request a new launcher MIDI clip with a described musical intent (e.g., “4 bars, chord progression in G minor”) and receive a newly created clip with notes written and the created clip launched (auditioned), end-to-end in under 10 seconds.
- The created clip is immediately usable in the current Bitwig project workflow (clip exists in the launcher, contains the requested pattern, and is placed predictably).
- The producer can create multiple clips in a batch by setting `launch=false` (so clip creation/writing is decoupled from launching when desired).
- The system is trustworthy: it never overwrites an existing clip unless explicitly requested/confirmed.

### Business Success

- The extension is usable enough that the primary user (Josh) uses it at least weekly as part of real production work.
- Usability improves over time, evidenced by fewer failed/aborted requests and fewer manual “fix-ups” after clip creation (qualitative notes are acceptable for MVP).

### Technical Success

- Performance: end-to-end completion time for the create→write→(optional) launch flow is <10s for normal-sized requests.
- Correctness: notes written into the clip match the external AI agent’s explicit structured payload exactly (note timing, pitch, duration, velocity), and the overall musical intent (key/scale constraints) is satisfied by virtue of the agent’s payload.
- Safety: `overwrite=false` by default; overwriting only occurs when explicitly enabled/confirmed by the user/agent context.
- Placement: if a target scene/slot is not specified, WigAI finds the next empty launcher slot for the target track by scanning forward as needed (Bitwig supports effectively unlimited scenes).
- Stability: WigAI does not crash Bitwig; failures are handled gracefully and returned as clear MCP errors (or logs) without leaving Bitwig in a broken state.
- Compatibility: feature works on macOS/Windows/Linux wherever Bitwig Studio is supported.

### Measurable Outcomes

- Success rate: percentage of valid requests that result in (a) clip created, (b) notes written, (c) created clip launched when `launch=true`.
- Latency: end-to-end time distribution (at least average + worst-case observed during manual testing).
- Safety incidents: number of unintended overwrites (target = 0).

## Product Scope

### MVP - Minimum Viable Product

- Create a new launcher MIDI clip on a specified or inferred track, with:
  - Track selection by `track_index` or `track_name`.
  - If no track specified: use selected track; if no selected track: return an error requesting explicit track.
  - If no scene/slot specified: create in next empty slot (scan forward).
  - If scene/slot specified and occupied: refuse unless `overwrite=true` (or an explicit confirm flow is satisfied).
  - Clip naming: accept an explicit name, or allow the external agent to provide a reasonable inferred name.
- Write MIDI notes into the created clip using explicit structured data from the external AI agent:
  - Support both step-grid style data and beat-time style data (as long as it is explicit and unambiguous).
- Launch the created clip (slot) for auditioning:
  - `launch=true` by default for the create flow (launches the created clip slot, not the full scene row).
  - Allow `launch=false` for batching/advanced workflows.
- Support launching scenes separately (e.g., `launch_scene`) when the user wants to audition a full row across tracks.
- Expose the above via MCP tools (multiple tools is acceptable for composability).

### Growth Features (Post-MVP)

- More flexible launch controls (e.g., launch/stop policies, quantization options, stop-all, etc.).
- Editing/augmenting existing clips (append vs replace policies, clearer targeting).
- Richer metadata and helpers (e.g., returning clip/slot identifiers for follow-up operations).

### Vision (Future)

- More advanced composition workflows (e.g., multi-track arrangement generation, iterative variations, constraint-driven transformations), while keeping WigAI’s core role as a safe executor and leaving creative generation to external agents.

## User Journeys

### Journey 1 — Studio Producer (Core Success Path): “Intent → Clip → Hear It”

Kai is producing a track in Bitwig and wants a fast way to audition musical ideas without manually step-entering notes. Kai has a Bitwig project open, selects the target track in the launcher (e.g., “Drums”), and opens an MCP-capable agent (typically Claude Desktop, but any MCP client works).

Kai asks the agent: “Create a 4‑bar drum pattern in 16th notes, punchy, and name it ‘Verse Drums 1’.”
The agent converts that intent into explicit structured note data and calls WigAI via MCP.

WigAI:

- Targets the selected Bitwig track (since no track was specified explicitly).
- Finds the next empty launcher slot (scene row) on that track.
- Creates a new launcher MIDI clip in that slot.
- Writes the provided notes into the clip.
- Sets the clip name (explicit name provided, or a reasonable inferred name supplied by the agent).
- Launches the created clip slot by default (`launch=true`) so Kai immediately hears the result.

Kai hears the pattern playing, sees the newly created clip selected, and can iterate: “Make the hats sparser” → agent sends an updated note payload → WigAI creates another clip in the next empty slot (non-destructive by default).

**Key decision points:**

- Track targeting: explicit `track_name`/`track_index` wins; otherwise selected track is used; if neither is available WigAI must request clarification.
- Placement: default is next empty slot (scan forward as needed).

### Journey 2 — Studio Producer (Edge Case): “Specified Scene Is Occupied”

Mina is arranging variations and wants a clip in a specific scene row for structure. Mina asks: “On ‘Bass’ track, create a 4‑bar bassline in G# minor in scene 8.”

The agent calls WigAI with explicit `track_name="Bass"` and `scene_index=8`.
WigAI checks the target slot and finds an existing clip already in scene 8 on that track.

Because the user/agent explicitly specified the scene, WigAI refuses to proceed unless overwrite is explicitly allowed. WigAI responds with an actionable error (or confirmation request) that scene 8 is occupied and requires `overwrite=true` (or an explicit confirmation flow) to replace it.

Mina chooses one of two safe paths:

- Re-run with `overwrite=true` (explicitly accepting destruction), or
- Re-run without `scene_index` so WigAI places the new clip in the next empty slot instead.

**Key decision points:**

- If `scene_index` is specified and occupied: refuse and ask (do not auto-relocate).
- Overwrite only happens with explicit intent.

### Journey 3 — Power User (Batching): “Create Many Clips, Launch Later”

Noah is building an arrangement quickly and wants multiple clips created across tracks without interrupting playback or launching each one immediately.

Noah asks the agent: “Create three 4‑bar chord progression variations in G minor on ‘Keys’ track, name them ‘Keys A/B/C’, don’t launch yet.”
The agent generates three explicit note payloads and calls WigAI multiple times with `launch=false`.

WigAI:

- Targets the explicitly named track (“Keys”).
- Creates each new clip in the next empty slot, scanning forward as needed.
- Writes the notes and sets the names.
- Does not launch (because `launch=false`), allowing uninterrupted batching.

After clips exist, Noah asks: “Launch scene 12.” WigAI launches the specified scene (MVP includes launching scenes), letting Noah audition a specific row when ready.

**Key decision points:**

- `launch` defaults true, but batch workflows depend on `launch=false`.
- Scene launching is supported independently so users can audition later.

### Journey 4 — Live Performer (Secondary Persona): “Rehearsal Variation Without Risk”

Rae is rehearsing a live set and wants a quick variation clip without risking overwriting existing performance clips. Rae selects the target track in Bitwig and asks the agent: “Create a 2‑bar fill variation and don’t overwrite anything.”

The agent sends explicit note data and does not request overwrite. WigAI creates the clip in the next empty slot and (by default) launches the created clip slot so Rae can hear it immediately. If Rae is preparing multiple ideas, Rae (or the agent) sets `launch=false` to create several variations first, then launches the preferred scene later.

**Key decision points:**

- Non-destructive defaults protect performance sets.
- Next-empty-slot placement avoids surprises mid-set.
- Optional `launch=false` supports prep workflows.

### Journey Requirements Summary

These journeys imply the system must provide:

- MCP tools for: create launcher MIDI clip (optionally launches the created clip), write notes to clip, launch clip, launch scene.
- Track targeting: accept `track_index` or `track_name`; otherwise use selected track; if no selected track and none provided → request clarification.
- Slot targeting: support optional `scene_index`; if omitted, scan forward to find next empty slot.
- Occupancy safety: if `scene_index` specified and occupied → refuse unless `overwrite=true` (or explicit confirmation is satisfied).
- Non-destructive defaults: `overwrite=false` by default.
- Launch behavior: `launch=true` launches the created clip slot by default; allow `launch=false` for batching.
- Clip naming: accept explicit name; otherwise allow agent-provided inferred name.
- Note writing: accept explicit structured notes (step-grid and/or beat-time representations) and apply them exactly.
- Clear outcomes: return enough metadata (track/scene chosen, created/overwritten status, launch performed) for the agent to explain what happened to the user.
- Error handling: actionable errors for missing track context, occupied target slots, invalid ranges, or API limitations.

## Innovation & Novel Patterns

### Detected Innovation Areas

- **MCP as a universal control port for Bitwig Studio:** WigAI exposes DAW actions as MCP tools, making any MCP-capable AI client (e.g., Claude Desktop) able to control Bitwig reliably through a consistent protocol boundary.
- **Producer-first “intent → clip” workflow:** A producer describes what they want musically, an external AI agent generates explicit structured note data, and WigAI materializes it as a new launcher MIDI clip inside Bitwig (optionally launching the created clip slot by default).
- **Safety-oriented automation:** Non-destructive defaults (`overwrite=false`, next-empty-slot placement) make automation usable in real creative projects without fear of silent destructive edits.

### Market Context & Competitive Landscape

This is intentionally built for personal use rather than competitive positioning. The differentiator that matters is practical: a producer can use any MCP-capable agent to create and audition musical ideas directly inside Bitwig without manual clip/step entry.

### Validation Approach

- **Producer “wow” demo:** Tell an MCP-capable agent what you want musically and observe the result created inside Bitwig as a new launcher clip.
- **Time-boxed usefulness test:** Create 5 usable clip ideas/variations in <5 minutes via the agent-to-WigAI workflow (with `launch=true` for rapid auditioning, or `launch=false` for batching).

### Risk Mitigation

This is a pet project for personal use, so risk tolerance is high. If the end-to-end “intent → clip” experience is inconsistent, the system should still remain useful in a degraded mode:

- **Degraded mode:** WigAI only performs deterministic execution (create clip, write explicit notes, launch created clip) based on explicit structured payloads; no reliance on WigAI-side inference.

## desktop_app Specific Requirements

### Project-Type Overview

WigAI runs as a Bitwig Studio extension/plugin (in-process) and exposes an MCP tool surface to an external AI agent. While it is not a standalone desktop application, the dominant constraints and expectations align with desktop software: cross-platform compatibility, low-latency interaction, and safe behavior within the host app.

### Technical Architecture Considerations

- The extension must run stably inside Bitwig Studio without crashing the host, and must degrade gracefully on errors.
- The MCP server boundary should remain consistent so multiple MCP-capable clients can interact with WigAI over time.

### Platform Support

- Support all Bitwig Studio supported operating systems: macOS, Windows, and Linux.

### Update Strategy

- Updates are manual: users install/update by replacing the `.bwextension` (no auto-update mechanism required for MVP).

### System Integration

- No additional system integration requirements for MVP beyond operating within Bitwig Studio and exposing MCP tools.

### Offline Capabilities

- WigAI should be compatible with offline workflows in principle: if the external AI agent is local (e.g., a local LLM client) and can call MCP tools, the create/write/launch flow should not require internet connectivity.

## Project Scoping & Phased Development

### MVP Strategy & Philosophy

**MVP Approach:** Experience MVP (deliver the “intent → clip in Bitwig” end-to-end experience)
**Resource Requirements:** Solo developer (Josh) with AI assistance; manual testing inside Bitwig Studio

### MVP Feature Set (Phase 1)

**Core User Journeys Supported:**

- Studio Producer: intent → new launcher MIDI clip → notes written → created clip launched
- Studio Producer (edge case): specified scene occupied → refuse unless overwrite explicitly enabled
- Power user batching: create multiple clips with `launch=false`, launch scene later

**Must-Have Capabilities:**

- MCP tools that cover:
  - Create launcher MIDI clip on a target track
  - Write explicit MIDI notes into the created clip
  - Launch a scene (row) in the launcher
- Safe targeting defaults:
  - Track: accept `track_index` or `track_name`; else selected track; else error requesting explicit track
  - Slot: next empty slot by default (scan forward); if `scene_index` specified and occupied → refuse unless `overwrite=true`
- Non-destructive behavior by default (`overwrite=false`)
- Clip naming support (explicit name, or agent-provided inferred name)
- `launch=true` by default with `launch=false` for batching
- Clear error reporting and results metadata for the agent (chosen track/scene, created vs overwritten, launched vs not)

### Post-MVP Features

**Phase 2 (Post-MVP):**

- Launch individual clips (not just scenes)
- Editing existing clips (append/replace policies) with explicit targeting
- Richer response metadata and helper queries (e.g., discover empty slots, list scenes/clips)

**Phase 3 (Expansion):**

- Higher-level compositional workflows (multi-track generation, variations, constraint transformations) while keeping WigAI as deterministic executor

### Risk Mitigation Strategy

**Technical Risks:** Targeting and determinism inside Bitwig (selection state, slot discovery, timing). Mitigate with explicit track/scene addressing rules, safe defaults, and graceful failure modes.
**Market Risks:** Not applicable (personal/pet project); validation is based on weekly real usage and time-boxed “5 usable clips in <5 minutes” tests.
**Resource Risks:** Solo bandwidth. Keep the MVP narrowly focused on end-to-end create→write→(optional) launch with strict non-destructive defaults; defer editing/advanced browsing/automation to Phase 2.

## Functional Requirements

### MCP Interaction & Tool Execution

- FR1: An external AI agent can connect to WigAI via MCP and invoke supported tools.
- FR2: An external AI agent can receive structured success responses for each tool invocation.
- FR3: An external AI agent can receive structured error responses when a request cannot be completed (e.g., missing target track, occupied slot without overwrite).
- FR4: An external AI agent can invoke a batch operation that creates and writes multiple clips in one request.

### Targeting & Discoverability

- FR5: An external AI agent can target a track by `track_index`.
- FR6: An external AI agent can target a track by exact `track_name`.
- FR7: If no track is specified, the system can target the currently selected Bitwig track.
- FR8: If neither an explicit track nor a selected track is available, the system can refuse and request explicit track targeting.
- FR9: An external AI agent can request track resolution by fuzzy name matching and receive candidate matches suitable for user confirmation.
- FR10: The system can refuse to act when fuzzy matching is ambiguous unless the agent confirms the intended track explicitly.

### Clip Creation (Launcher)

- FR11: An external AI agent can create a new launcher MIDI clip on a target track.
- FR12: If `scene_index` is not specified, the system can place the new clip in the next empty launcher slot on that track (scanning forward as needed).
- FR13: If `scene_index` is specified and the slot is occupied, the system can refuse unless overwrite is explicitly enabled.
- FR14: The system can avoid overwriting existing clips by default (`overwrite=false`).
- FR15: An external AI agent can explicitly enable overwriting an occupied target slot.
- FR16: An external AI agent can name a clip explicitly at creation time.
- FR17: An external AI agent can provide an “inferred” clip name (derived from the request), and the system can apply it.

### Note Writing (Deterministic Execution)

- FR18: An external AI agent can write explicit musical note data into a newly created launcher clip.
- FR19: The system can support step-grid style note data as an input form for writing notes.
- FR20: The system can support beat-time style note data as an input form for writing notes.
- FR21: The system can validate note inputs (e.g., timing and pitch ranges) and refuse invalid payloads with actionable errors.
- FR22: The system can support clearing/replacing prior clip content when explicitly requested as part of a write operation.
- FR23: The system can report how many notes were written and how many were rejected/skipped (with reasons).

### Launching & Auditioning

- FR24: An external AI agent can launch a scene (launcher row) by index.
- FR25: When creating a clip, the system can launch the created clip slot by default (`launch=true`).
- FR26: When creating a clip, an external AI agent can suppress launching (`launch=false`) to support batching workflows.

### Baseline (Existing Brownfield Capabilities)

- FR27: An external AI agent can start Bitwig transport.
- FR28: An external AI agent can stop Bitwig transport.
- FR29: An external AI agent can launch an existing clip by specifying a target track and slot/scene index.
- FR30: An external AI agent can read the 8 remote-control parameter values of the currently selected/cursor device.
- FR31: An external AI agent can set one or more of the 8 remote-control parameter values of the currently selected/cursor device.

## Non-Functional Requirements

### Performance

- NFR1: For normal-sized requests, WigAI completes the end-to-end create→write→(optional) launch workflow in under 10 seconds.
- NFR2: WigAI avoids blocking Bitwig Studio’s UI thread with long-running work to preserve a responsive DAW experience.

### Reliability & Stability

- NFR3: WigAI does not crash Bitwig Studio; all tool failures are handled gracefully.
- NFR4: WigAI retries transient failures (e.g., timing/availability issues in host state) before returning an error, using a bounded retry policy.
- NFR5: WigAI returns clear error results when a request cannot be completed, without leaving Bitwig in an inconsistent or partially broken state.

### Integration & Networking

- NFR6: The MCP server binds to `localhost` by default for MVP.
- NFR7: The MCP interface remains stable and backward-compatible within a release line so MCP clients can reliably call tools over time.

### Security (Local-First)

- NFR8: No authentication is required for MVP, relying on localhost-only binding as the primary guardrail.

### Observability & Diagnostics

- NFR9: WigAI logs each tool invocation and its outcome (success/failure and reason codes).
- NFR10: WigAI avoids logging full note payloads by default; verbose logging (including note payloads) is enabled only via an explicit debug flag.
