# Story 4.1: Extract Config Interfaces

Status: ready-for-dev

## Story

As a WigAI developer, I need a stable `ConfigManager`/`ConfigChangeObserver` abstraction so we can plug in future configuration sources (Bitwig Preferences, CLI, presets) without touching the MCP server lifecycle logic or risking regressions in transport, device, or clip tooling.

## Acceptance Criteria

1. `ConfigManager` interface exists under `io.github.fabb.wigai.config` with `getMcpHost()`, `setMcpHost(String host)`, `getMcpPort()`, `setMcpPort(int port)`, and `addObserver(ConfigChangeObserver observer)` methods plus lightweight JavaDoc describing expectations (thread-safety, validation, defaults).
2. `ConfigChangeObserver` interface (same package) defines `onHostChanged(String oldHost, String newHost)` and `onPortChanged(int oldPort, int newPort)` callbacks; it is registered and invoked by the config provider whenever persisted values change (no-op behavior is acceptable for now but the hooks must exist).
3. All existing components (`WigAIExtension`, `JettyServerManager`, `McpServerManager`, downstream controllers/tools, and supporting tests) depend only on the new interfaces—no class reaches into implementation-specific details or static constants beyond the interface contract.
4. The current configuration source (Legacy configuration class) implements `ConfigManager`, keeps existing defaults (`localhost`, `AppConstants.DEFAULT_MCP_PORT`), validates input (host string non-empty, port range 1024–65535), and safely notifies observers when setters mutate state without triggering any new Bitwig UI / restart logic.
5. Regression guardrails exist: unit/integration tests (or updated existing tests) prove that host/port read/write behavior, server startup, and logging remain unchanged, and manual verification steps are documented so a developer can run WigAI in Bitwig to double-check.
6. No Bitwig Preferences API usage, observer-driven restarts, or other behavior demanded by stories 4.2+ is introduced—the deliverable is a pure refactor that unblocks those later stories while maintaining parity with today’s runtime.

## Tasks / Subtasks

- [ ] Design `ConfigManager` and `ConfigChangeObserver` interfaces with JavaDoc describing thread expectations and validation responsibilities.
- [ ] Update the existing configuration provider to implement `ConfigManager`, persist defaults, and notify observers from setters (wrap notifications with try/catch + logging).
- [ ] Refactor `WigAIExtension`, `JettyServerManager`, `McpServerManager`, and any helper classes/tests to accept `ConfigManager` via constructor injection instead of concrete classes or static calls.
- [ ] Ensure observer registration happens once during extension startup and that callback bodies simply log intentions (actual restart logic arrives later).
- [ ] Expand or add tests covering host/port getters/setters, observer notification wiring, and server startup using the interface (e.g., Mockito or lightweight fakes).
- [ ] Document manual verification (run `./gradlew build`, launch extension inside Bitwig, confirm CLI logs show existing host/port) and capture notes in `Dev Agent Record` after implementation.

## Dev Notes

- This is the foundation for Epic 4 Stories 4.2–4.4; bias toward clean interfaces, granular validation, and zero functional drift so the next stories can simply introduce new implementations.
- The observer model should be resilient: do not allow a single observer failure to stop others; log via `Logger` with class context.
- Keep logging consistent with `Logger`/`StructuredLogger` patterns described in `docs/component-architecture-deep-dive.md`; no System.out statements.
- Explicitly forbid Bitwig Preference APIs, UI strings, or restart logic—they belong to later stories.

### Project Structure Notes

- Source files live under `src/main/java/io/github/fabb/wigai/...` following `docs/project-structure.md`.
- Interfaces: `src/main/java/io/github/fabb/wigai/config/ConfigManager.java` and `ConfigChangeObserver.java`.
- Config implementation: existing class inside `io.github.fabb.wigai.config` (legacy) should move behind the interface; no new packages.
- Tests belong under `src/test/java/io/github/fabb/wigai/config/`.

### References

- `docs/prd/epic-4.md` – Epic objectives, acceptance criteria, and dependencies across Stories 4.1–4.4.
- `docs/architecture.md` – Overall system architecture, error handling, and component responsibilities.
- `docs/component-architecture-deep-dive.md` – Detailed component relationships (ConfigManager, JettyServerManager, McpServerManager, observers).
- `docs/project-structure.md` – Source tree conventions.
- `docs/tech-stack.md` – Approved language/runtime/library versions (Java 21, Bitwig API v19, MCP Java SDK 0.9.0+).
- `docs/stories/4.2.story.md` and `docs/stories/4.3.story.md` – Downstream stories to understand dependencies and boundary conditions for this refactor.

## Developer Context

Epic 4 shifts configuration from hardcoded constants to Bitwig Preferences UI. Story 4.1 provides the seam: every component that currently reaches directly into a configuration implementation must instead consume a `ConfigManager` interface, and configuration changes must surface through an observer contract. Without this, later stories risk rewiring the entire server lifecycle when they introduce UI-backed preferences, restart hooks, and logging improvements. This story therefore focuses entirely on abstractions, validation, and dependency cleanup so that future work only needs to supply a new implementation. Keep the MCP server, Jetty bootstrap, and tool registration untouched apart from their constructor signatures—tests and manual verification must demonstrate complete behavioral parity so we do not jeopardize existing MCP tool coverage from Epics 1–3.

## Technical Requirements

- `ConfigManager` methods:
  - `String getMcpHost()` returns the currently effective host (never null, trimmed).
  - `void setMcpHost(String host)` validates non-empty, fallback to `"localhost"` via `AppConstants` when invalid, notifies observers.
  - `int getMcpPort()` returns the effective port (within 1024–65535 inclusive).
  - `void setMcpPort(int port)` validates numeric range, falls back to `AppConstants.DEFAULT_MCP_PORT` when invalid, notifies observers.
  - `void addObserver(ConfigChangeObserver observer)` adds observers exactly once; duplicate registrations are ignored or clearly documented.
- `ConfigChangeObserver` callback methods must document that they run on the Bitwig controller thread and should complete quickly; they must receive the previous values for symmetry with future restart logic.
- Legacy implementation should remain thread-safe (use `synchronized` or concurrent collections as needed) because Jetty and MCP initialization run concurrently with Bitwig events.
- Observer notification order is not guaranteed but failure of one observer may not interrupt others; wrap each callback in try/catch and log with `logger.error`.
- Constructor injection:
  - `WigAIExtension` constructs the config implementation once, registers itself as observer, and passes only the interface down to `JettyServerManager` and `McpServerManager`.
  - `JettyServerManager` and `McpServerManager` use `configManager.getMcpPort()` / `getMcpHost()` when binding Jetty/MCP transport; no other components should read `AppConstants.DEFAULT_MCP_PORT` directly.
- Maintain existing default host/port (localhost:61169) so CLI scripts and docs remain accurate.
- Document these contracts with JavaDoc so future stories (4.2–4.4) have a single source of truth.

## Architecture Compliance

- Honor the modular structure outlined in `docs/architecture.md`:
  - Keep `ConfigManager` within the Configuration module, separate from the MCP server and feature controllers.
  - Observer notifications flow into `WigAIExtension`, which remains the orchestrator responsible for restarts.
  - Jetty and MCP server managers stay decoupled via constructor injection; do not introduce service locators or static globals.
- Follow the error-handling strategy (structured logging, no silent failures, propagate via exceptions).
- Ensure the observer pattern aligns with the architecture doc’s design patterns table (Observer + Dependency Injection).

## Library & Framework Requirements

- Java 21 LTS with Gradle 8.x per `docs/tech-stack.md`.
- Bitwig Extension API v19 (extension lifecycle, controller host access).
- MCP Java SDK 0.9.0+ for SSE transport; this story must not change transport selection.
- Jetty remains the embedded HTTP server; this work must not swap server frameworks.

## File & Code Structure Requirements

- Keep all new interfaces/classes in `io.github.fabb.wigai.config`.
- Update constructors and fields in:
  - `io.github.fabb.wigai.WigAIExtension`
  - `io.github.fabb.wigai.server.JettyServerManager`
  - `io.github.fabb.wigai.mcp.McpServerManager`
  - Any helper/service that previously imported the concrete config class
- Tests belong in the mirrored `src/test/java/io/github/fabb/wigai/...` packages and should use descriptive class names (e.g., `ConfigManagerTest`).
- Do not relocate or rename existing packages; follow `docs/project-structure.md`.

## Testing Requirements

- Unit tests verifying:
  - Default host/port values are returned when no overrides are set.
  - Invalid host/port inputs fallback and emit logs without throwing.
  - Observer registration and invocation occurs exactly once per setter call (use mocks/fakes).
- Integration or component-level test to confirm `JettyServerManager` consumes port/host via the interface (a mock config can confirm binding).
- Manual steps (documented in implementation PR or completion notes):
  - Run `./gradlew build` to ensure compilation/test coverage.
  - Launch WigAI inside Bitwig Studio with the legacy config and confirm startup logs still announce the same host/port.

## Previous Story Intelligence

- This is the first story inside Epic 4, so there are no prior Epic 4 implementation learnings.
- Reference Epics 1–3 commits to understand testing and log styles; continue using the `feat: implement story X.Y` convention when committing code.

## Git Intelligence

- Latest commits (`git log -5 --oneline`):
  - `3257236 fix: simplify code`
  - `2eb29c4 feat: implement story 8.2 and add get_device_details tool`
  - `031206d feat: implement story 8.1 and add list_devices_on_track tool`
  - `f8f8e3d feat: implement story 7.2 and add get_clips_in_scene tool`
  - `01de677 feat: implement story 7.1 and add list_scenes tool`
- Patterns to follow:
  - Each story commit explicitly references the story ID in the subject.
  - Feature additions pair code and tool registration updates—mirror that discipline when updating config wiring so changes remain localized and reviewable.

## Latest Technical Information

- According to `docs/tech-stack.md`, target stack remains Java 21, Bitwig API v19, and MCP Java SDK 0.9.0+ (SSE transport only for now). No newer SDK version is approved without architect review.
- Default MCP server port stays aligned with `AppConstants.DEFAULT_MCP_PORT (61169)`; keep watchers for future updates but do not change the constant.
- Logging continues through `Logger` / `StructuredLogger`; no new logging libraries or frameworks are permitted.

## Project Context Reference

- No `project-context.md` file exists in this repository. Use the documents referenced above plus this story as the authoritative context for development.

## Story Completion Status

- Story ID: 4.1
- Story Key: 4-1-extract-config-interfaces
- Story File: `docs/sprint-artifacts/4-1-extract-config-interfaces.md`
- Status: ready-for-dev — configuration interfaces, observer contract, and guardrails are fully specified for development handoff.

## Dev Agent Record

### Context Reference

- Story context: `docs/sprint-artifacts/4-1-extract-config-interfaces.md`
- Supporting docs: `docs/prd/epic-4.md`, `docs/architecture.md`, `docs/component-architecture-deep-dive.md`

### Agent Model Used

- Codex GPT-5 (Scrum Master Bob) via *create-story workflow

### Debug Log References

- Populate during implementation; no runtime logs generated for this drafting step.

### Completion Notes List

- Story drafted via *create-story workflow on the current sprint cycle; ready for developer pick-up once sprint planning confirms priority.

### File List

- To be completed by the development agent after implementing the story (expected files: configuration interfaces, legacy config implementation updates, server managers, associated tests).
