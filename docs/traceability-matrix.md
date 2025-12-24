# Traceability Matrix & Gate Decision - Story 1.1

**Story:** Repeatable MCP Smoke Test Harness
**Date:** 2025-12-24
**Evaluator:** Josh

---

## PHASE 1: REQUIREMENTS TRACEABILITY

### Coverage Summary

| Priority  | Total Criteria | FULL Coverage | Coverage % | Status            |
| --------- | -------------- | ------------- | ---------- | ----------------- |
| P0        | 0              | 0             | N/A        | ✅ PASS (N/A)     |
| P1        | 4              | 4             | 100%       | ✅ PASS           |
| P2        | 1              | 1             | 100%       | ✅ PASS           |
| P3        | 0              | 0             | N/A        | ✅ PASS (N/A)     |
| **Total** | **5**          | **5**         | **100%**   | **✅ PASS**       |

**Legend:**

- ✅ PASS - Coverage meets quality gate threshold
- ⚠️ WARN - Coverage below threshold but not critical
- ❌ FAIL - Coverage below minimum threshold (blocker)

---

### Detailed Mapping

#### AC-1: Connect to MCP endpoint and report diagnostics (P1)

- **Coverage:** FULL ✅
- **Tests:**
  - `1.1-ATDD-001` - src/test/java/io/github/fabb/wigai/smoke/McpSmokeHarnessAtddTest.java:22
    - **Given:** Safe mode harness run with host/port configured
    - **When:** The harness executes
    - **Then:** It prints the resolved MCP URL and mode, and exits successfully
  - `unit:resolvedUrl_with_custom_host_and_port` - src/test/java/io/github/fabb/wigai/smoke/McpSmokeHarnessArgsTest.java:23
    - **Given:** Custom host and port
    - **When:** resolvedUrl is computed
    - **Then:** The URL matches `http://{host}:{port}/mcp`
  - `unit:safeModeFailsOnTypedErrorForNonDeviceTools` - src/test/java/io/github/fabb/wigai/smoke/McpSmokeHarnessArgsTest.java:274
    - **Given:** A typed error on a non-device tool
    - **When:** Safe mode runs
    - **Then:** The harness fails with actionable diagnostics

- **Gaps:** None
- **Recommendation:** None

---

#### AC-2: tools/list baseline and full JSON output (P1)

- **Coverage:** FULL ✅
- **Tests:**
  - `1.1-ATDD-002` - src/test/java/io/github/fabb/wigai/smoke/McpSmokeHarnessAtddTest.java:35
    - **Given:** Full baseline tools are available
    - **When:** tools/list runs
    - **Then:** The harness passes baseline validation
  - `1.1-ATDD-003` - src/test/java/io/github/fabb/wigai/smoke/McpSmokeHarnessAtddTest.java:46
    - **Given:** A baseline tool is missing
    - **When:** tools/list runs
    - **Then:** The harness fails and reports the missing tool
  - `1.1-ATDD-013` - src/test/java/io/github/fabb/wigai/smoke/McpSmokeHarnessAtddTest.java:225
    - **Given:** A tools/list JSON response
    - **When:** The harness prints discovery output
    - **Then:** It includes the full tools/list JSON (not just names)

- **Gaps:** None
- **Recommendation:** None

---

#### AC-3: Safe mode read-only validations only (P1)

- **Coverage:** FULL ✅
- **Tests:**
  - `1.1-ATDD-004` - src/test/java/io/github/fabb/wigai/smoke/McpSmokeHarnessAtddTest.java:58
    - **Given:** Safe mode and a mutating tool is present
    - **When:** The harness runs
    - **Then:** Mutating tools are not called
  - `1.1-ATDD-005` - src/test/java/io/github/fabb/wigai/smoke/McpSmokeHarnessAtddTest.java:70
    - **Given:** A new read-only tool is discovered
    - **When:** Safe mode runs
    - **Then:** The harness calls all non-mutating tools
  - `unit:safeModeRejectsMutatingToolCall` - src/test/java/io/github/fabb/wigai/smoke/McpSmokeHarnessArgsTest.java:317
    - **Given:** Safe mode with both read-only and mutating tools
    - **When:** The harness executes
    - **Then:** No mutating tools are called

- **Gaps:** None
- **Recommendation:** None

---

#### AC-4: Mutation mode gated by explicit flag (P1)

- **Coverage:** FULL ✅
- **Tests:**
  - `1.1-ATDD-007` - src/test/java/io/github/fabb/wigai/smoke/McpSmokeHarnessAtddTest.java:108
    - **Given:** Mutation mode enabled
    - **When:** The harness runs
    - **Then:** It calls `transport_start` then `transport_stop`
  - `1.1-ATDD-008` - src/test/java/io/github/fabb/wigai/smoke/McpSmokeHarnessAtddTest.java:120
    - **Given:** Mutation mode is missing required tools
    - **When:** The harness runs
    - **Then:** It fails and reports missing mutation tools
  - `unit:parseArgs_ignores_mutations_cli_flag_per_AC4` - src/test/java/io/github/fabb/wigai/smoke/McpSmokeHarnessArgsTest.java:109
    - **Given:** A CLI mutations flag
    - **When:** Args are parsed
    - **Then:** Mutations remain disabled (env-var only gating)

- **Gaps:** None
- **Recommendation:** None

---

#### AC-5: Typed error when no device is selected (P2)

- **Coverage:** FULL ✅
- **Tests:**
  - `1.1-ATDD-009` - src/test/java/io/github/fabb/wigai/smoke/McpSmokeHarnessAtddTest.java:133
    - **Given:** No device is selected
    - **When:** get_selected_device_parameters is invoked
    - **Then:** A typed error is returned without crashing
  - `unit:parseEnvelope_typed_error_response` - src/test/java/io/github/fabb/wigai/smoke/McpSmokeHarnessArgsTest.java:165
    - **Given:** A typed error envelope
    - **When:** The envelope is parsed
    - **Then:** The code and message are surfaced correctly

- **Gaps:** None
- **Recommendation:** None

---

### Gap Analysis

#### Critical Gaps (BLOCKER) ❌

0 gaps found. No release blockers.

---

#### High Priority Gaps (PR BLOCKER) ⚠️

0 gaps found. No PR blockers.

---

#### Medium Priority Gaps (Nightly) ⚠️

0 gaps found. No nightly gaps.

---

#### Low Priority Gaps (Optional) ℹ️

0 gaps found. No optional gaps.

---

### Quality Assessment

#### Tests with Issues

**BLOCKER Issues** ❌

- None

**WARNING Issues** ⚠️

- `McpSmokeHarnessArgsTest.java` - 592 lines (exceeds 300 line limit) - Split into smaller focused test files

**INFO Issues** ℹ️

- `McpSmokeHarnessArgsTest.java` - Unit tests lack ID/priority markers - Optional for full traceability parity

---

#### Tests Passing Quality Gates

**54/55 tests (98%) meet all quality criteria** ✅

---

### Duplicate Coverage Analysis

#### Acceptable Overlap (Defense in Depth)

- AC-1: Tested at ATDD and unit levels ✅
- AC-3: Tested at ATDD and unit levels ✅
- AC-4: Tested at ATDD and unit levels ✅
- AC-5: Tested at ATDD and unit levels ✅

#### Unacceptable Duplication ⚠️

- None

---

### Coverage by Test Level

| Test Level | Tests | Criteria Covered                     | Coverage % |
| ---------- | ----- | ------------------------------------ | ---------- |
| E2E        | 13    | AC-1, AC-2, AC-3, AC-4, AC-5 (5/5)   | 100%       |
| API        | 0     | None (0/5)                           | 0%         |
| Component  | 0     | None (0/5)                           | 0%         |
| Unit       | 42    | AC-1, AC-3, AC-4, AC-5 (4/5)         | 80%        |
| **Total**  | **55**| **5/5**                              | **100%**   |

---

### Traceability Recommendations

#### Immediate Actions (Before PR Merge)

1. None required. Coverage targets and critical criteria are fully met.

#### Short-term Actions (This Sprint)

1. Split `src/test/java/io/github/fabb/wigai/smoke/McpSmokeHarnessArgsTest.java` into smaller focused test files (<300 lines each).
2. (Optional) Add ID/priority markers to unit tests for uniform traceability.

#### Long-term Actions (Backlog)

1. None

---

## PHASE 2: QUALITY GATE DECISION

**Gate Type:** story
**Decision Mode:** deterministic
**Status:** Skipped (enable_gate_decision=false)

