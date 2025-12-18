# System-Level Test Design

**Date:** 2025-12-18T16:14:52Z  
**Author:** Josh  
**Status:** Draft

---

## Testability Assessment

- **Controllability:** **CONCERNS**
  - ✅ Clear tool/controller/facade layering enables extensive mocking in unit/integration tests (`src/test/java/...`).
  - ⚠️ True system state is Bitwig host state; fully deterministic automation in CI is not feasible without a “Bitwig host required” harness.
  - ⚠️ Timing-sensitive host behaviors (e.g., clip availability, selection state, scene scanning) increase the need for bounded retries + idempotency tests.

- **Observability:** **CONCERNS**
  - ✅ Structured, unified MCP error handling exists (`McpErrorHandler`) and tests validate response envelopes (`McpResponseTestUtils`).
  - ✅ PRD requires log discipline (no full note payloads unless debug).
  - ⚠️ No explicit metrics/tracing baselines defined yet (e.g., per-tool latency, retry counts, dedupe hits).

- **Reliability:** **PASS**
  - ✅ PRD explicitly calls for bounded retries and graceful failure without leaving Bitwig inconsistent.
  - ✅ Existing test suite covers validation/error-code behaviors and tool/controller flows.
  - ⚠️ Reliability for new “create → write → (optional) launch” workflow depends on robust idempotency (`request_id`) and safety defaults across all mutating tools.

---

## Architecturally Significant Requirements (ASRs)

Risk scoring uses Probability (1–3) × Impact (1–3) = Score (1–9). Scores ≥6 require explicit mitigation and automated validation where feasible.

| ASR ID | Category | Quality Requirement (Evidence) | Probability | Impact | Score | Validation Approach (Automatable First) |
| ------ | -------- | ------------------------------ | ----------- | ------ | ----- | --------------------------------------- |
| ASR-001 | PERF | End-to-end create→write→(optional) launch completes in **<10s** for normal payloads (PRD NFR1) | 2 | 3 | 6 | Add per-tool timing logs; add “host-required” smoke run checklist; add bounded retry + timeout unit tests. |
| ASR-002 | PERF | Avoid blocking Bitwig UI-sensitive paths (PRD NFR2) | 2 | 3 | 6 | Code review gate: no unbounded waits; unit tests around retry limits; host-required “responsiveness” check. |
| ASR-003 | OPS | WigAI must not crash Bitwig; failures return actionable MCP errors (PRD NFR3, NFR5) | 2 | 3 | 6 | Expand error-handling integration tests; enforce no uncaught exceptions in tool handlers. |
| ASR-004 | TECH | Retry transient host failures with **bounded** policy (PRD NFR4) | 2 | 2 | 4 | Unit tests for retry classifier/limits; deterministic controller tests with fake transient failures. |
| ASR-005 | SEC | Server binds loopback-only by default (PRD NFR6) | 1 | 3 | 3 | Unit tests for config parsing/validation; integration test on server binding config (no non-loopback unless explicit). |
| ASR-006 | TECH | MCP interface is stable; tool names/args snake_case; unified response envelope (PRD NFR7, `docs/project_context.md`) | 2 | 2 | 4 | Contract-like tests: schema/`@JsonProperty` coverage; `McpResponseTestUtils` assertions for every tool. |
| ASR-007 | DATA | Safety defaults: never overwrite/clear by default (PRD safety criteria) | 2 | 3 | 6 | Unit tests: overwrite/clear flags default false; refusal on destructive requests unless explicitly opted-in. |
| ASR-008 | DATA | Idempotency: mutating tools dedupe via `(tool_name, request_id)` (project context rule) | 2 | 3 | 6 | Controller-level tests that re-submit same request and verify single side-effect; verify TTL/size bounds. |
| ASR-009 | OPS | Logging discipline: do not log full note payload unless debug (PRD NFR10, project context rule) | 2 | 2 | 4 | Logger tests: only counts/shape logged by default; guard debug flag path separately. |

---

## Test Levels Strategy

This project is an in-process Bitwig extension; full E2E automation in CI is constrained by the host DAW. The strategy is therefore **“unit/integration heavy + explicit host-required smoke layer”**.

- **Unit (≈65%)**
  - Validate argument parsing, `ParameterValidator` rules, edge cases, error codes, and controller policy decisions (retry/idempotency/safety defaults).
  - Fast, deterministic, Bitwig-host-free.

- **Integration (≈30%)**
  - Verify MCP tool specifications, JSON schema + snake_case mapping, unified error envelope, and tool→controller wiring.
  - Use `io.modelcontextprotocol.sdk:mcp-test` utilities where applicable.

- **Host-Required E2E/Smoke (≈5%)**
  - A small, curated suite run on a developer machine with Bitwig installed (or a dedicated workstation), validating the real DAW interaction for the highest-risk workflows:
    - Create launcher MIDI clip in next empty slot
    - Write notes (atomic, validated)
    - Launch scene (optional)
    - Negative paths: occupied slot + overwrite=false; invalid note payload; ambiguous track targeting

---

## NFR Testing Approach

### Security (Local-First)

- **Primary gate:** loopback-only binding defaults; refuse/require explicit opt-in for non-loopback configs.
- **Tests:** configuration validation + server binding assertions (unit/integration).
- **Scope note:** No external auth for MVP (per PRD). Threat model is “local process boundary”; treat any future remote binding as a separate security epic.

### Performance

- Establish a **latency budget per workflow step** (create, write, launch) and measure in logs (min/p50/p95) during host-required smoke runs.
- Add unit tests that prevent performance regressions from logic-level issues:
  - bounded retry loops
  - bounded scene scanning (with explicit max / clear error)
  - payload size limits (notes count, bars, etc.) as validation rules

### Reliability

- Expand integration tests around `McpErrorHandler` and tool handlers to ensure:
  - no double-wrapped responses
  - consistent error code mapping
  - failures are atomic (no partial writes when validation fails)
- Add controller tests for idempotency (`request_id`) and destructive-operation opt-ins.

### Maintainability

- Keep tests deterministic and short; prefer focused unit tests + small integration tests over heavy “do everything” tests.
- Ensure new tools/controllers follow existing patterns (tool→controller→`BitwigApiFacade`) to keep testability stable.

---

## Test Environment Requirements

- **CI (default):** Java 21 + Gradle; runs all unit + integration tests without Bitwig installed.
- **Host-required station (optional but recommended):**
  - Bitwig installed and configured
  - WigAI extension installed
  - A small test Bitwig project file for repeatable scenarios (tracks/scenes pre-created)
  - A scripted MCP client (or manual runbook) to exercise P0 smoke scenarios

---

## Testability Concerns (Informing Solutioning Gate)

1. **Host dependency for true E2E**: CI cannot fully validate Bitwig behaviors; mitigation is a tiny “host-required” smoke suite + strict unit/integration gates.
2. **Timing variability**: Host state may be eventually consistent; mitigation is bounded retries + idempotency + explicit targeting (avoid relying on selection state).
3. **Limited observability baselines**: No current, enforced latency or retry-count thresholds; mitigation is structured per-tool timing logs and a lightweight gate.

**Gate Recommendation:** **PASS with CONCERNS** (address ASR-001/002/008 with explicit tests + runbook before shipping the new MIDI workflow).

---

## Quality Gate Criteria (Before Shipping MIDI Workflow)

- All unit + integration tests pass in CI (`./gradlew test`).
- No **Score ≥6** ASRs remain unmitigated (ASR-001/002/003/007/008 have concrete tests + owners).
- Host-required smoke runbook passes for:
  - create launcher MIDI clip (next-empty + explicit slot refusal)
  - write notes (atomic, validated, no payload logging by default)
  - optional launch behavior (`launch=true|false`)
- Error handling remains unified (no double-wrapping; actionable error codes/messages).

---

## Recommendations for Sprint 0

1. Define and implement **idempotency/dedupe** policy for all new mutating tools (`request_id`), with controller-level tests.
2. Add/standardize **payload size limits** for note writes (count/timing bounds), with validation tests.
3. Add **host-required smoke runbook** (or a dedicated Gradle test task) for create/write/launch workflows.
4. Add basic **latency instrumentation** per tool (timed operation logging) and document acceptable thresholds.
5. Ensure “destructive defaults” remain non-destructive (`overwrite=false`, `clear_existing=false`) and are tested as invariants.
