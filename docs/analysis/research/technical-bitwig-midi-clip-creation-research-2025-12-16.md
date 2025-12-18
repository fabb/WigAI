---
stepsCompleted: [1, 2, 3, 4, 5]
inputDocuments:
  - docs/reference/bitwig-api/v19/com/bitwig/extension/controller/api/ClipLauncherSlot.md
  - docs/reference/bitwig-api/v19/com/bitwig/extension/controller/api/ClipLauncherSlotBank.md
  - docs/reference/bitwig-api/v19/com/bitwig/extension/controller/api/Track.md
  - docs/reference/bitwig-api/v19/com/bitwig/extension/controller/api/Clip.md
  - docs/reference/bitwig-api/v19/com/bitwig/extension/controller/api/CursorClip.md
  - docs/reference/component-architecture-deep-dive.md
  - src/main/java/io/github/fabb/wigai/bitwig/BitwigApiFacade.java
  - src/main/java/io/github/fabb/wigai/mcp/McpServerManager.java
workflowType: "research"
lastStep: 5
research_type: "technical"
research_topic: "Bitwig Extension API v19: MIDI clip creation (WigAI)"
research_goals: "Identify the best Bitwig API v19 approach to (1) create a new launcher MIDI clip on a target track/slot, (2) control its length, and (3) write MIDI notes into it (step-grid), in a way that fits WigAI’s MCP + BitwigApiFacade architecture."
user_name: "Josh"
date: "2025-12-16"
web_research_enabled: true
source_verification: true
---

# Research Report: technical

**Date:** 2025-12-16
**Author:** Josh
**Research Type:** technical

---

## Research Overview

This report focuses on implementing **MIDI clip creation** for WigAI using **Bitwig Extension API v19**:

- Creating empty launcher clips (session view)
- Selecting/locating the created clip reliably
- Writing notes into the clip via the step-grid APIs
- Designing a clean WigAI MCP tool surface and BitwigApiFacade methods to support this safely

---

## Technical Research Scope Confirmation

**Research Topic:** Bitwig Extension API v19: MIDI clip creation (WigAI)
**Research Goals:** Identify the best Bitwig API v19 approach to (1) create a new launcher MIDI clip on a target track/slot, (2) control its length, and (3) write MIDI notes into it (step-grid), in a way that fits WigAI’s MCP + BitwigApiFacade architecture.

**Technical Research Scope:**

- Architecture Analysis - design patterns, frameworks, system architecture
- Implementation Approaches - development methodologies, coding patterns
- Technology Stack - languages, frameworks, tools, platforms
- Integration Patterns - APIs, protocols, interoperability
- Performance Considerations - scalability, optimization, patterns

**Research Methodology:**

- Current web data with rigorous source verification
- Multi-source validation for critical technical claims
- Confidence level framework for uncertain information
- Comprehensive technical coverage with architecture-specific insights

**Scope Confirmed:** 2025-12-16

---

<!-- Content will be appended sequentially through research workflow steps -->

## Technology Stack Analysis

This section covers the core languages/libraries/tools involved in implementing **MIDI clip creation** inside a Bitwig Java extension and exposing it via WigAI’s MCP server.

### Programming Languages

- **Java (Bitwig extension runtime):** WigAI is implemented as a Java Bitwig Extension, so MIDI clip creation will be implemented in Java.  
  Source: https://openjdk.org/projects/jdk/21/
- **TypeScript/Node (offline tooling):** Your Bitwig API Javadoc scraping workflow is implemented in Node/TypeScript; this doesn’t affect runtime behavior in Bitwig, but it is a key developer tool to keep the API surface searchable.  
  Source: https://nodejs.org/en

### Development Frameworks and Libraries

- **Bitwig Extension API v19 (local, authoritative for this project):** This is the API surface WigAI calls to create and edit clips. For MIDI clip creation specifically, the local docs expose:
  - `Track.createNewLauncherClip(int,int)` / `Track.createNewLauncherClip(int)` for creating an empty launcher clip and selecting it  
    Local source: `docs/reference/bitwig-api/v19/com/bitwig/extension/controller/api/Track.md`
  - `ClipLauncherSlot.createEmptyClip(int)` / `ClipLauncherSlotBank.createEmptyClip(int,int)` for creating empty clips in a target slot  
    Local sources: `docs/reference/bitwig-api/v19/com/bitwig/extension/controller/api/ClipLauncherSlot.md`, `docs/reference/bitwig-api/v19/com/bitwig/extension/controller/api/ClipLauncherSlotBank.md`
  - `Clip.setStep(...)` / `CursorClip.setStep(...)` for writing notes via the step grid abstraction  
    Local sources: `docs/reference/bitwig-api/v19/com/bitwig/extension/controller/api/Clip.md`, `docs/reference/bitwig-api/v19/com/bitwig/extension/controller/api/CursorClip.md`

- **MCP Java SDK (WigAI’s protocol boundary):** WigAI exposes actions as MCP tools; clip-creation will likely become one or more MCP tools calling into `BitwigApiFacade`.  
  Sources:
  - https://raw.githubusercontent.com/modelcontextprotocol/java-sdk/main/README.md
  - https://modelcontextprotocol.io/sdk/java/mcp-server

- **Jetty (embedded HTTP server):** WigAI runs an embedded server in-process; any new clip-creation endpoints/tools must be safe for this embedded environment (avoid blocking behaviors, keep request handlers small).  
  Source: https://jetty.org/docs/jetty/12/programming-guide/index.html

### Database and Storage Technologies

- **None required for MVP clip creation.** Creating launcher clips and writing notes is driven by Bitwig’s internal project state; WigAI does not need a database to create MIDI clips.

### Development Tools and Platforms

- **Gradle build tooling** (project build + packaging).  
  Source: https://docs.gradle.org/current/userguide/userguide.html
- **JUnit 5** (tests in this repo).  
  Source: https://junit.org/junit5/

### Cloud Infrastructure and Deployment

- **Not applicable for clip creation itself.** WigAI runs locally inside Bitwig Studio as an extension; clip creation happens against the user’s currently open Bitwig project.

### Technology Adoption Trends

- Not applicable to this narrowly-scoped technical research topic (we’re focused on concrete API usage patterns and safe integration in WigAI).

---

## Integration Patterns Analysis

### Web Verification Notes

To comply with the research workflow’s “web verification + citations” requirement, integration-pattern assertions below are cross-checked against the following public references:

- MCP architecture concepts: https://modelcontextprotocol.io/docs/concepts/architecture
- MCP Java SDK server documentation: https://modelcontextprotocol.io/sdk/java/mcp-server
- MCP Java SDK README: https://raw.githubusercontent.com/modelcontextprotocol/java-sdk/main/README.md
- JSON-RPC 2.0 specification: https://www.jsonrpc.org/specification
- Server-Sent Events overview (SSE): https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events
- Jetty programming guide: https://jetty.org/docs/jetty/12/programming-guide/index.html

### API Design Patterns

**Primary API surface for WigAI clip creation:** MCP “tools” that wrap `BitwigApiFacade` operations.

- **Protocol shape:** MCP uses a JSON-RPC style request/response model (tool calls map naturally to RPC methods).  
  Source: https://www.jsonrpc.org/specification  
  Source: https://modelcontextprotocol.io/docs/concepts/architecture
- **Recommended tool design:** keep tools small and composable:
  - `clip.create_launcher_midi_clip` → creates/selects the clip
  - `clip.write_notes_steps` → writes notes into the currently selected launcher clip (or accepts an explicit locator, if feasible)
  - `clip.clear` / `clip.quantize` / `clip.transpose` (optional follow-ons, all supported by `Clip` APIs locally)
- **Idempotency guidance:** clip creation is *inherently stateful* in Bitwig. Provide an optional client-supplied `client_request_id` and implement best-effort idempotency (e.g., “if slot already has content, fail unless overwrite=true”).

### Communication Protocols

WigAI’s integration path is: external client ↔ MCP server (Jetty) ↔ Bitwig extension API.

- **HTTP + streaming:** If you’re using MCP over SSE, it’s fundamentally HTTP with server push semantics.  
  Source: https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events
- **Embedded server constraints:** treat Jetty handlers as “thin controllers” and keep blocking work minimal; defer heavy logic to application code (your existing pattern with `BitwigApiFacade` is aligned).  
  Source: https://jetty.org/docs/jetty/12/programming-guide/index.html

### Data Formats and Standards

- **Request/response format:** MCP messages are JSON(-RPC). Tool arguments should be JSON-serializable and validateable (schema-first).  
  Source: https://www.jsonrpc.org/specification  
  Source: https://modelcontextprotocol.io/sdk/java/mcp-server
- **Clip note payload (recommended):** represent notes as a list of objects with:
  - `channel` (0–15), `key` (0–127), `start_step` (int), `duration_steps` (int or double beats), `velocity` (0–127)
  - optional `step_size_beats` to define the meaning of “step”

### System Interoperability Approaches

Interoperability here means “AI client can create clips reliably across different Bitwig project states.”

- **Selection-based interoperability (pragmatic):** prefer APIs that explicitly say the new clip will be selected (so the “launcher cursor clip” view follows it). In Bitwig API v19 local docs, `Track.createNewLauncherClip(...)` states it selects the new clip; this reduces ambiguity compared to trying to infer selection state.  
  Local source: `docs/reference/bitwig-api/v19/com/bitwig/extension/controller/api/Track.md`
- **Locator-based interoperability (more robust, but limited by API):** if Bitwig does not expose a stable “selected slot cursor” for inactive slots (documented in your prior story), you may not be able to address “the currently selected slot” precisely; design tools to accept explicit `track` + `slotIndex` when creating a clip.  
  Local source: `docs/sprint-artifacts/archive/cycle-1-2025-12-15/5-4-selected-clip-slot-status.md`

### Microservices Integration Patterns

Not applicable. WigAI is an embedded server inside a Bitwig extension, not a distributed microservice environment. Keep the system monolithic/modular and minimize moving parts.

### Event-Driven Integration

- **Within Bitwig:** you can observe playback/slot state via observers (already used in your facade for `ClipLauncherSlot` state).  
  Local sources: `docs/reference/bitwig-api/v19/com/bitwig/extension/controller/api/ClipLauncherSlot.md`, `docs/reference/bitwig-api/v19/com/bitwig/extension/controller/api/ClipLauncherSlotBank.md`
- **To clients:** SSE can stream status updates/notifications (if your MCP transport uses SSE).  
  Source: https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events

### Integration Security Patterns

Clip creation is powerful (it mutates the user’s project). Even without doing web research on Bitwig specifics, the general integration security patterns apply:

- **Local-only binding by default** (e.g., bind to loopback, require explicit user opt-in to expose externally).
- **Auth token** for non-local connections; consider a short-lived token or a per-session token surfaced in Bitwig UI/logs.
- **Safety rails in the API design:** `dry_run` flags, explicit `overwrite` boolean, and conservative defaults.

Source (MCP docs include a security section; treat as starting point): https://modelcontextprotocol.io/docs/concepts/architecture

---

## Architectural Patterns and Design

### Web Verification Notes

The general architecture patterns and principles cited here are verified via:

- SOLID overview: https://en.wikipedia.org/wiki/SOLID
- Domain-Driven Design overview: https://martinfowler.com/bliki/DomainDrivenDesign.html
- The Twelve-Factor App (ops/deploy principles): https://12factor.net/
- Jetty programming guide: https://jetty.org/docs/jetty/12/programming-guide/index.html

Bitwig-specific architectural conclusions are derived from local API docs and the existing WigAI codebase.

### System Architecture Patterns

**Pattern observed in WigAI today:** a modular monolith embedded in the Bitwig extension process:

- `JettyServerManager` hosts an embedded HTTP server.
- `McpServerManager` registers MCP tools and wires controllers.
- Controllers call `BitwigApiFacade` (facade pattern over Bitwig API).
  Local sources:
  - `docs/reference/component-architecture-deep-dive.md`
  - `src/main/java/io/github/fabb/wigai/mcp/McpServerManager.java`
  - `src/main/java/io/github/fabb/wigai/bitwig/BitwigApiFacade.java`

**Recommended pattern for MIDI clip creation:** keep the same layering:

- Add facade methods to create a launcher clip and write notes.
- Add a feature controller (e.g., `MidiClipController`) that enforces business rules (validation, defaults, safety rails).
- Add MCP tools that do minimal mapping/validation and call the controller.

This aligns with “separation of concerns” and reduces the risk of Jetty request handlers becoming stateful/complex.

### Design Principles and Best Practices

- **SOLID / Single Responsibility:** clip creation (mutating project state) should not be mixed into “status / query” code paths. Split into its own controller + tools.  
  Source: https://en.wikipedia.org/wiki/SOLID
- **DDD-lite for boundaries:** even without full-blown DDD, treat “MIDI clip creation” as its own subdomain with a clear API (`create`, `writeNotes`, `clear`, `quantize`), to keep Bitwig quirks contained.  
  Source: https://martinfowler.com/bliki/DomainDrivenDesign.html

### Scalability and Performance Patterns

Scale here is mostly about *responsiveness* inside Bitwig’s extension environment:

- Batch note writes where possible (e.g., compute step positions in memory, then apply in a tight loop).
- Keep grid sizes reasonable (avoid enormous `gridWidth/gridHeight` unless needed).
- Avoid excessive observers during write operations unless you need feedback.

Local sources (note writing primitives):
- `docs/reference/bitwig-api/v19/com/bitwig/extension/controller/api/Clip.md`
- `docs/reference/bitwig-api/v19/com/bitwig/extension/controller/api/CursorClip.md`

### Integration and Communication Patterns

- **Tool-based RPC boundary (MCP):** MCP tools form a stable boundary between AI clients and the mutable Bitwig project. Keep tool schemas explicit and validated.  
  Source: https://modelcontextprotocol.io/sdk/java/mcp-server
- **Embedded server patterns (Jetty):** favor short request lifecycles; avoid long-running operations in request threads; prefer background work with explicit progress/status tools if needed.  
  Source: https://jetty.org/docs/jetty/12/programming-guide/index.html

### Security Architecture Patterns

Clip creation can destructively change a user’s project. Recommended guardrails:

- Require explicit `track` + `slotIndex` for “create clip” (avoid ambiguous selection inference).
- Require explicit `overwrite=true` to replace existing clip content; default is safe “fail if occupied”.
- Log all mutations with enough detail for audit and debugging.

(MCP security guidance is relevant at the transport boundary.)  
Source: https://modelcontextprotocol.io/docs/concepts/architecture

### Data Architecture Patterns

No database required. Data is:

- Inputs: JSON tool args (notes, target track/slot, length).
- Outputs: structured JSON results (created slot, selected clip context, counts).
- Persistence: Bitwig project itself.

### Deployment and Operations Architecture

WigAI runs as a Bitwig extension, but the general operational guidance still applies:

- Prefer config via environment/preferences (already present via `ConfigManager`).
- Keep logs structured enough to debug production use.
- Avoid hidden coupling on startup order; make server lifecycle restart-safe.

Source: https://12factor.net/

---

## Implementation Approaches and Technology Adoption

### Web Verification Notes

This section uses general (non-Bitwig) software engineering references for workflow/testing/build/tooling claims:

- Continuous Integration: https://martinfowler.com/articles/continuousIntegration.html
- Gradle user manual: https://docs.gradle.org/current/userguide/userguide.html
- JUnit 5: https://junit.org/junit5/
- Twelve-Factor App: https://12factor.net/

Bitwig-specific implementation steps are derived from local API docs and WigAI’s existing code architecture.

### Technology Adoption Strategies

**Adopt MIDI clip creation incrementally** to minimize risk inside the Bitwig extension runtime:

1. Add “create empty launcher clip” capability first (no note writing yet).
2. Add “write a single note” into the selected launcher clip (smallest end-to-end proof).
3. Add batch note writing + clear/overwrite policies.
4. Add higher-level musical abstractions (patterns, chords, quantize) only after the primitives are stable.

This is a typical low-risk adoption pattern: ship a minimal vertical slice, then iterate.

### Development Workflows and Tooling

- **CI discipline:** keep changes small and verifiable; adopt a practice of “always green” mainline to avoid regressions in the extension runtime.  
  Source: https://martinfowler.com/articles/continuousIntegration.html
- **Build tooling:** continue using Gradle for repeatable builds; for new functionality, add focused unit tests around argument validation and facade/controller behavior.  
  Source: https://docs.gradle.org/current/userguide/userguide.html

### Testing and Quality Assurance

Recommended test layers for MIDI clip creation:

- **Unit tests (fast, deterministic):**
  - Validate request parsing (MCP tool args → domain args).
  - Validate argument constraints (track selection rules, slot index, note ranges).
  - Validate error mapping (`BitwigApiException` → MCP error response).
  - This matches your existing pattern of mocking Bitwig API objects in `BitwigApiFacadeTest`.
- **Manual integration validation (inside Bitwig):**
  - Verify clip creation in an empty slot and in an occupied slot.
  - Verify note writing with different step sizes and velocities.

JUnit 5 is the expected unit-test framework.  
Source: https://junit.org/junit5/

### Deployment and Operations Practices

Even though WigAI is an extension (not a cloud service), the operational principles still help:

- **Config separation:** keep host/port and any future “unsafe operations” flags configurable (already in your config manager).
- **Observability:** log all mutations (create clip, overwrite, number of notes written) with enough data to reproduce issues.

Source: https://12factor.net/

### Team Organization and Skills

This work benefits from:

- Familiarity with Bitwig’s clip launcher mental model (tracks × scenes/slots).
- Comfort with Java API integration and testing via mocks.
- Comfort designing “safe” external tool APIs (schema-driven, validated, conservative defaults).

### Cost Optimization and Resource Management

No extra infrastructure cost is required. Resource management focus is on Bitwig runtime health:

- Avoid huge note grids or excessive observers.
- Prefer bounded loops and input limits (e.g., max notes per request).

### Risk Assessment and Mitigation

Key risks and mitigations:

- **Risk: ambiguous selection / wrong clip edited.**  
  Mitigation: prefer APIs that explicitly select the created clip; accept explicit `track` + `slotIndex`; do not rely on “user selected slot” when Bitwig API can’t reveal it.
- **Risk: overwriting user content.**  
  Mitigation: default to `overwrite=false`, and require explicit opt-in to clear/replace clip contents.
- **Risk: responsiveness / blocking in Jetty request threads.**  
  Mitigation: keep handlers thin; if operations become heavy, add async execution + status polling tools.

## Technical Research Recommendations

### Implementation Roadmap

1. Implement `create_launcher_clip`:
   - Inputs: `trackName` (or track index), `slotIndex`, optional `lengthInBeats`.
   - Behavior: create a new launcher clip (preferring the API that guarantees selection), return created slot metadata.
2. Implement `write_launcher_clip_notes`:
   - Inputs: `gridWidth`, `gridHeight` (or fixed defaults), `stepSizeBeats`, `notes[]`.
   - Behavior: target the currently selected launcher clip (created in prior step) and write notes via `setStep`.
3. Add `overwrite` policy:
   - `overwrite=false` fails if slot has content.
   - `overwrite=true` clears/overwrites then writes.
4. Add convenience musical helpers (optional):
   - quantize/transpose after writing.

### Technology Stack Recommendations

- Keep using your current layering (`Tool` → `Controller` → `BitwigApiFacade`) to integrate MIDI clip creation.
- Prefer local Bitwig API docs as truth for API calls; use web citations only for general architectural principles and MCP/Jetty/JSON-RPC references.

### Skill Development Requirements

- Bitwig clip launcher API fluency (`Track`, `ClipLauncherSlot`, `CursorClip`).
- Safe external API design: schemas, validation, conservative defaults.

### Success Metrics and KPIs

- Can create an empty launcher clip in a specified track/slot reliably.
- Can write a deterministic note pattern into the created clip.
- No unintended overwrites with default settings.
- Clear error reporting when target track/slot is invalid or occupied.

---

## Technical Synthesis and Conclusions

### Executive Summary

WigAI is well-positioned to add **MIDI clip creation** as a first-class MCP capability because the codebase already has the right architectural seams: an embedded HTTP server (Jetty), an MCP tool layer (MCP Java SDK), feature controllers, and a `BitwigApiFacade` abstraction over the Bitwig Extension API. The core work is to add a small, safe set of primitives:

1. Create/select a new launcher clip in an explicit track + slot.
2. Write notes into the selected launcher clip using the step grid API.
3. Wrap both operations behind schema-validated MCP tools with conservative defaults (`overwrite=false`, bounded note counts).

This keeps clip creation predictable, debuggable, and safe for end users while enabling higher-level “musical intent” tools later.

### Table of Contents

1. Research Introduction and Methodology
2. Technology Stack Analysis
3. Integration Patterns Analysis
4. Architectural Patterns and Design
5. Implementation Approaches and Technology Adoption
6. Technical Synthesis and Conclusions

### Research Introduction and Methodology

This report synthesizes:

- Local Bitwig API v19 documentation (authoritative for API surface)
- Existing WigAI architecture and code patterns
- Web-verified references for MCP/JSON-RPC/SSE/Jetty and general engineering principles, per the `*research` workflow requirements

Key references:

- MCP architecture: https://modelcontextprotocol.io/docs/concepts/architecture
- MCP Java SDK server docs: https://modelcontextprotocol.io/sdk/java/mcp-server
- JSON-RPC 2.0: https://www.jsonrpc.org/specification
- SSE overview: https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events
- Jetty programming guide: https://jetty.org/docs/jetty/12/programming-guide/index.html

### Core Implementation Strategy (Bitwig API v19 + WigAI layering)

**Create an empty launcher MIDI clip**

Preferred approach (explicit selection semantics):

- Use `Track.createNewLauncherClip(slotIndex, lengthInBeats)` or `Track.createNewLauncherClip(slotIndex)` which (per local docs) creates an empty clip at or after the absolute slot index and selects it.
  Local source: `docs/reference/bitwig-api/v19/com/bitwig/extension/controller/api/Track.md`

Alternative approach (direct slot API):

- Use `ClipLauncherSlot.createEmptyClip(lengthInBeats)` or `ClipLauncherSlotBank.createEmptyClip(slot, lengthInBeats)` if you are already targeting a specific bank/slot instance.
  Local sources: `docs/reference/bitwig-api/v19/com/bitwig/extension/controller/api/ClipLauncherSlot.md`, `docs/reference/bitwig-api/v19/com/bitwig/extension/controller/api/ClipLauncherSlotBank.md`

**Write notes into the clip**

- Represent the selected launcher clip as a `CursorClip` (or pinnable cursor clip) with a configured grid.
- Write notes via `setStep(channel, x, y, velocity, duration)` (or the appropriate overload) where:
  - `x` = step/time position within the grid
  - `y` = MIDI key (0–127)
  - `duration` = beats (per docs) and step size can be controlled via `setStepSize(double)`
  Local sources: `docs/reference/bitwig-api/v19/com/bitwig/extension/controller/api/CursorClip.md`, `docs/reference/bitwig-api/v19/com/bitwig/extension/controller/api/Clip.md`

**Important limitation to design around**

Your prior work already documents that Bitwig API v19 does not let you reliably identify the user-selected (inactive) launcher slot (“selected clip slot”) in all cases. This reinforces the design choice to require explicit `track` + `slotIndex` for creation and to rely on “new clip will be selected” semantics when you need to edit it immediately afterward.

Local source: `docs/sprint-artifacts/archive/cycle-1-2025-12-15/5-4-selected-clip-slot-status.md`

### Recommended MCP Tool Surface (minimal, safe, composable)

1. `create_launcher_midi_clip`
   - Inputs: `track_name`, `slot_index`, optional `length_in_beats`, optional `overwrite`
   - Output: created clip metadata (track/slot), and whether selection now points to it
2. `write_launcher_clip_notes`
   - Inputs: `step_size_beats`, `grid_width`, `grid_height`, `notes[]`, optional `clear_existing`
   - Output: counts (notes_written, notes_skipped), plus any validation warnings

This matches the existing WigAI pattern: tool → controller → facade.

### Practical Roadmap (next engineering steps)

1. Add a facade method: `createNewLauncherClip(trackName, slotIndex, lengthInBeats?)`
2. Add a facade method (or a new helper component) to get a launcher `CursorClip` and write steps.
3. Add a new controller `MidiClipController` that:
   - validates note ranges and limits
   - enforces overwrite/clear policy
4. Add new MCP tools and register them in `McpServerManager`.
5. Add unit tests around validation and error mapping; validate end-to-end manually inside Bitwig.

### Open Questions (worth deciding before coding)

- Should “write notes” target the *currently selected launcher clip* only, or accept explicit track/slot and internally select it first?
- What note representation do you want in the MCP tool: step-grid (x/y) vs beat-time (startBeat, durationBeats)?
- What should happen when the target slot already has content: fail, overwrite, or “append”?

### Conclusions

The Bitwig API surface you already have locally supports MIDI clip creation and step-grid note writing. The primary engineering challenge is not the API calls themselves but ensuring **correct targeting and safety** (explicit slot addressing, overwrite policies, bounded inputs) through WigAI’s MCP tools. With WigAI’s existing architecture, this feature can be implemented as a small, well-contained extension that preserves stability and user trust.
