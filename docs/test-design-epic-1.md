# Test Design: Epic 1 - Reliable MCP Control Surface (Baseline Verification + Hardening)

**Date:** 2025-12-18T15:24:22-07:00
**Author:** Josh
**Status:** Draft

---

## Executive Summary

**Scope:** full test design for Epic 1

**Risk Summary:**

- Total risks identified: 8
- High-priority risks (≥6): 3
- Critical categories: SEC, PERF, OPS

**Coverage Summary:**

- P0 scenarios: 10 (20 hours)
- P1 scenarios: 8 (8 hours)
- P2/P3 scenarios: 10 (4 hours)
- **Total effort**: 32 hours (~4 days)

---

## Risk Assessment

### High-Priority Risks (Score ≥6)

| Risk ID | Category | Description | Probability | Impact | Score | Mitigation | Owner | Timeline |
| ------- | -------- | ----------- | ----------- | ------ | ----- | ---------- | ----- | -------- |
| R-001 | SEC | MVP exposure risk: accept non-loopback host binding (no auth) | 2 | 3 | 6 | Enforce loopback-only defaults + refuse non-loopback in prefs validation + add tests | Josh | Epic 1 |
| R-002 | OPS | Port bind failure (port in use) leads to crash/unreachable server | 2 | 3 | 6 | Graceful startup/restart failure path + actionable error + “still running” guarantee + test coverage | Josh | Epic 1 |
| R-003 | PERF | UI/host responsiveness regression: tool execution blocks Bitwig or hangs | 2 | 3 | 6 | Enforce bounded retries/timeouts + “no blocking” review gate + harness stress mode + log duration | Josh | Epic 1 |

### Medium-Priority Risks (Score 3-4)

| Risk ID | Category | Description | Probability | Impact | Score | Mitigation | Owner |
| ------- | -------- | ----------- | ----------- | ------ | ----- | ---------- | ----- |
| R-004 | TECH | Response envelope drift (double-wrapping; inconsistent `status/data/error`) breaks clients | 2 | 2 | 4 | Expand `McpResponseTestUtils` assertions across baseline tools + update `docs/reference/api-reference.md` | Josh | 
| R-005 | TECH | Lack of `request_id` correlation and standardized error-code logging slows incident triage | 2 | 2 | 4 | Add structured logging context for mutating tools + unit tests that assert request_id propagation | Josh |
| R-006 | TECH | “Retryable vs non-retryable” misclassification causes wasted retries or premature failure | 2 | 2 | 4 | Centralize retry classification + tests for “invalid params = no retry” and “timing = bounded retry” | Josh |

### Low-Priority Risks (Score 1-2)

| Risk ID | Category | Description | Probability | Impact | Score | Action |
| ------- | -------- | ----------- | ----------- | ------ | ----- | ------ |
| R-007 | OPS | Smoke harness accidentally performs mutations in “safe mode” | 1 | 2 | 2 | Monitor (prevent with explicit gating + CI guardrails) |
| R-008 | BUS | Poor diagnostics from harness reduces adoption and slows development feedback | 1 | 2 | 2 | Monitor (require actionable failure output) |

### Risk Category Legend

- **TECH**: Technical/Architecture (flaws, integration, scalability)
- **SEC**: Security (access controls, auth, data exposure)
- **PERF**: Performance (SLA violations, degradation, resource limits)
- **DATA**: Data Integrity (loss, corruption, inconsistency)
- **BUS**: Business Impact (UX harm, logic errors, revenue)
- **OPS**: Operations (deployment, config, monitoring)

---

## Test Coverage Plan

### P0 (Critical) - Run on every commit

**Criteria**: Blocks core journey + High risk (≥6) + No workaround

| Requirement | Test Level | Risk Link | Test Count | Owner | Notes |
| --- | --- | --- | ---: | --- | --- |
| Server binds loopback-only by default; refuse non-loopback host values (Story 1.2) | Unit/Integration | R-001 | 3 | Josh | Validate config sanitization + “refuse + revert” behavior |
| Port change restart is graceful; bind failure is actionable and non-crashing (Story 1.2) | Unit/Integration | R-002 | 2 | Josh | Ensure startup/restart errors surface cleanly |
| Baseline tools success/error responses match canonical envelope; no double-wrapping (Story 1.3) | Integration | R-004 | 3 | Josh | Expand `McpResponseTestUtils` coverage across baseline tools |
| Mutating tools accept optional `request_id` and include it in logging context (Story 1.4) | Unit/Integration | R-005 | 1 | Josh | Correlation must not break existing clients |
| Retry policy is bounded; invalid parameter errors are not retried; no hangs (Story 1.5) | Unit | R-003/R-006 | 1 | Josh | Validate max attempts/backoff and failure classification |

**Total P0**: 10 scenarios, 20 hours

### P1 (High) - Run on PR to main

**Criteria**: Important features + Medium risk (3-4) + Common workflows

| Requirement | Test Level | Risk Link | Test Count | Owner | Notes |
| --- | --- | --- | ---: | --- | --- |
| Smoke harness can connect to `http://{host}:{port}/mcp` and report actionable pass/fail (Story 1.1) | Host-required E2E/Smoke | R-003/R-008 | 3 | Josh | Run on a Bitwig-enabled workstation; not in standard CI |
| Smoke harness discovery asserts baseline tools exist and prints observed tool list (Story 1.1) | Host-required E2E/Smoke | R-008 | 1 | Josh | Include at minimum `status` |
| Safe mode harness exercises read-only tools only; mutation flag gates mutations (Story 1.1) | Host-required E2E/Smoke | R-007 | 2 | Josh | Environment flag must be explicit and auditable |
| Operation logs include started/finished with `tool_name`, outcome, duration; errors include `ErrorCode` (Stories 1.4/1.5) | Unit/Integration | R-005/R-003 | 2 | Josh | Avoid logging full payloads; log shape/counts |

**Total P1**: 8 scenarios, 8 hours

### P2 (Medium) - Run nightly/weekly

**Criteria**: Secondary features + Low risk (1-2) + Edge cases

| Requirement | Test Level | Risk Link | Test Count | Owner | Notes |
| --- | --- | --- | ---: | --- | --- |
| `get_selected_device_parameters` returns structured error when no device selected (Story 1.1) | Integration | R-008 | 2 | Josh | Regression coverage for “no unhandled exception” |
| Timing stress mode runs bounded retries + reports retry counts and total duration (Story 1.5) | Host-required E2E/Smoke | R-003 | 2 | Josh | Run on workstation; nightly optional |
| Verify harness “safe mode” cannot mutate (defense-in-depth) (Story 1.1) | Unit | R-007 | 2 | Josh | CLI-level guard + runtime guard |

**Total P2**: 6 scenarios, 3 hours

### P3 (Low) - Run on-demand

**Criteria**: Nice-to-have + Exploratory + Performance benchmarks

| Requirement | Test Level | Test Count | Owner | Notes |
| --- | --- | ---: | --- | --- |
| Manual “operational runbook” validation of baseline tools on a real Bitwig project file | Host-required Manual | 2 | Josh | Complements automation when host timing is variable |
| Log output format review (readability + noise discipline) | Manual | 2 | Josh | Ensure “actionable diagnostics” bar is met |

**Total P3**: 4 scenarios, 1 hour

---

## Execution Order

### Smoke Tests (<5 min)

**Purpose**: Fast feedback, catch build-breaking issues

- [ ] Host smoke: connect to `http://{host}:{port}/mcp` (30s)
- [ ] Host smoke: `tools/list` includes baseline tools (45s)
- [ ] Host smoke: safe-mode read-only checks (2–3 min)
- [ ] Host smoke: mutation-gated `transport_start` → `transport_stop` (60s)

**Total**: 4 scenarios

### P0 Tests (<10 min)

**Purpose**: Critical path validation

- [ ] Loopback-only binding defaults + guardrails (Unit/Integration)
- [ ] Canonical response envelope (Integration)
- [ ] `request_id` accepted + correlated logging for mutating tools (Unit/Integration)
- [ ] Bounded retries + no-hang guarantees (Unit)

**Total**: 10 scenarios

### P1 Tests (<30 min)

**Purpose**: Important feature coverage

- [ ] Smoke harness UX: actionable diagnostics, clear summary (Host-required)
- [ ] Operation logging requirements (Unit/Integration)

**Total**: 8 scenarios

### P2/P3 Tests (<60 min)

**Purpose**: Full regression coverage

- [ ] Negative path: “no device selected” structured error (Integration)
- [ ] Timing stress mode (Host-required, optional)

**Total**: 10 scenarios

---

## Resource Estimates

### Test Development Effort

| Priority  | Count             | Hours/Test | Total Hours       | Notes                   |
| --------- | ----------------- | ---------- | ----------------- | ----------------------- |
| P0 | 10 | 2.0 | 20 | Config + envelope + retry invariants |
| P1 | 8 | 1.0 | 8 | Smoke harness + logging coverage |
| P2 | 6 | 0.5 | 3 | Negative paths + stress mode |
| P3 | 4 | 0.25 | 1 | Runbook + log review |
| **Total** | **28** | **-** | **32** | **~4 days** |

### Prerequisites

**Test Data:**

- A repeatable Bitwig test project file (tracks/scenes known; safe to mutate)
- A “no device selected” baseline scenario (cursor device unset)

**Tooling:**

- JUnit 5 + Mockito unit/integration tests (CI-safe)
- A dedicated `mcpSmokeTest` Gradle task / CLI harness (host-required)

**Environment:**

- CI: Java 21 + Gradle (no Bitwig required)
- Workstation: Bitwig Studio installed + WigAI extension enabled (for smoke)

---

## Quality Gate Criteria

### Pass/Fail Thresholds

- **P0 pass rate**: 100% (no exceptions)
- **P1 pass rate**: ≥95% (waivers required for failures)
- **P2/P3 pass rate**: ≥90% (informational)
- **High-risk mitigations**: 100% complete or approved waivers

### Coverage Targets

- **Critical paths**: ≥80%
- **Security scenarios**: 100%
- **Business logic**: ≥70%
- **Edge cases**: ≥50%

### Non-Negotiable Requirements

- [ ] All P0 tests pass
- [ ] No high-risk (≥6) items unmitigated
- [ ] Security tests (SEC category) pass 100%
- [ ] Performance targets met (PERF category)

---

## Mitigation Plans

### R-001: MVP exposure via non-loopback binding (Score: 6)

**Mitigation Strategy:** Default bind loopback-only; sanitize empty host to `localhost`; refuse non-loopback values in preferences for MVP (no-auth); log warning with remediation guidance.
**Owner:** Josh
**Timeline:** Epic 1
**Status:** Planned
**Verification:** Unit/integration tests for host validation + binding; manual smoke harness verifies endpoint is reachable only via loopback.

### R-002: Port bind failure crashes/unreachable server (Score: 6)

**Mitigation Strategy:** Implement graceful start/restart path: if bind fails, surface actionable error (port in use) and keep Bitwig stable; avoid partial initialization.
**Owner:** Josh
**Timeline:** Epic 1
**Status:** Planned
**Verification:** Unit tests for config validation + restart logic; harness asserts “clear failure” (not crash/hang) on bind failure.

### R-003: UI/host responsiveness regressions (Score: 6)

**Mitigation Strategy:** Enforce bounded retries/timeouts; log duration + retry counts; smoke harness includes “timing stress” mode that fails fast on hangs.
**Owner:** Josh
**Timeline:** Epic 1
**Status:** Planned
**Verification:** Unit tests for retry limits + “no retry on invalid params”; host-required smoke harness validates no hangs and reports total duration.

---

## Assumptions and Dependencies

### Assumptions

1. Baseline MCP tools remain available and backward compatible throughout Epic 1 (per PRD NFR7).
2. Host-required smoke runs are performed on a Bitwig-capable workstation (not standard CI).
3. Tool logging is structured enough to assert `tool_name`, `request_id` (when provided), and `ErrorCode` on failures.

### Dependencies

1. Implement/configure MCP host/port preferences (Story 1.2) - Required by Epic 1 completion
2. Smoke harness scaffolding (Story 1.1) - Required before Epic 2+ tool surface expansion

### Risks to Plan

- **Risk**: Host-required smoke runs become flaky due to DAW timing variability
  - **Impact**: false negatives slow development and erode trust in the gate
  - **Contingency**: keep host smoke small; add bounded retry + clear diagnostics; prioritize CI-safe unit/integration invariants

---

## Approval

**Test Design Approved By:**

- [ ] Product Manager: TBD Date: TBD
- [ ] Tech Lead: TBD Date: TBD
- [ ] QA Lead: TBD Date: TBD

**Comments:**

---

---

---

## Appendix

### Knowledge Base References

- `risk-governance.md` - Risk classification framework
- `probability-impact.md` - Risk scoring methodology
- `test-levels-framework.md` - Test level selection
- `test-priorities-matrix.md` - P0-P3 prioritization

### Related Documents

- PRD: `docs/prd.md`
- Epic: `docs/epics.md` (Epic 1)
- Architecture: `docs/architecture.md`
- System Test Design: `docs/test-design-system.md`

---

**Generated by**: BMad TEA Agent - Test Architect Module
**Workflow**: `.bmad/bmm/testarch/test-design`
**Version**: 4.0 (BMad v6)
