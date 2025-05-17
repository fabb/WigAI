# WigAI Testing Strategy

Version: 0.1.0
Date: 2025-05-16

## 1. Overall Philosophy & Goals

The testing strategy for WigAI aims to ensure the extension is reliable, correctly implements the specified MCP commands, and integrates seamlessly with Bitwig Studio without causing instability. We will focus on a combination of automated unit tests for isolated logic and manual/semi-automated integration and end-to-end tests due to the nature of interacting with a host DAW.

**Key Goals:**

* **Correctness:** Verify that all implemented MCP commands perform the correct actions in Bitwig Studio and return the specified responses.
* **Robustness:** Ensure the extension handles invalid inputs, unexpected Bitwig states, and errors gracefully without crashing itself or Bitwig Studio.
* **Reliability:** Confirm that features work consistently across different scenarios.
* **Maintainability:** Tests should be easy to understand, write, and maintain alongside the codebase.
* **Developer Confidence:** Enable developers to refactor and add new features with a safety net.

## 2. Testing Levels

### 2.1. Unit Tests

* **Scope:** Test individual Java classes and methods in isolation, particularly focusing on:
    * `McpCommandParser`: Parsing and validation of MCP command JSON payloads into DTOs.
    * Logic within `Feature Modules` (e.g., `TransportController`, `DeviceController`, `SceneController`) that doesn't directly depend on live Bitwig API objects. This includes parameter validation, state transitions, and response formatting.
    * Utility classes in `common/` and `config/`.
    * Abstracted logic within `BitwigApiFacade` that can be tested without a live Bitwig `Host` instance (e.g., data transformation).
* **Tools:**
    * **JUnit 5 (Jupiter):** The primary framework for writing unit tests.
    * **Mockito (or similar):** For creating mock objects to isolate components and simulate dependencies (e.g., mocking parts of the Bitwig API or other internal components).
* **Mocking/Stubbing:**
    * Bitwig API interfaces (like `Host`, `Application`, `Transport`, `Device`, etc.) will be mocked where necessary to simulate Bitwig's behavior or responses for unit tests.
    * Internal dependencies between WigAI components will also be mocked.
* **Location:** `src/test/java/io/github/fabb/wigai/` (mirroring the main source structure).
* **Execution:** Automatically run as part of the Gradle build process (`./gradlew test`).
* **Expectations:**
    * High code coverage for critical logic (parsers, non-Bitwig-dependent feature logic).
    * Tests should be fast and self-contained.
    * Focus on testing different paths, edge cases, and boundary conditions for data processing and command validation.

### 2.2. Integration Tests (Within WigAI Extension)

* **Scope:** Test the interaction between different components *within* the WigAI extension, but still potentially without a fully running Bitwig instance, or by using more complex mock setups for the Bitwig API. For example:
    * `RequestRouter` correctly dispatching to the `McpCommandParser` and then to the appropriate `FeatureModule`.
    * Interaction between `FeatureModules` and a mocked `BitwigApiFacade`.
* **Tools:**
    * JUnit 5.
    * Mockito for parts of the Bitwig API that are complex to simulate live in this context.
* **Location:** Can also reside in `src/test/java/`, possibly in a sub-package like `integration`.
* **Execution:** Run as part of the Gradle build.
* **Expectations:** Verify contracts and collaborations between internal components. Slower than unit tests but faster than end-to-end tests.

### 2.3. End-to-End (E2E) / Manual Acceptance Tests

* **Scope:** Test the entire system flow, from an external MCP client sending commands to WigAI running within a live Bitwig Studio instance, and verifying the actions in Bitwig and the MCP responses. This is the most crucial level for validating user-facing functionality.
* **Process:**
    1.  **Setup:**
        * Install the WigAI extension (`.bwextension` file) in Bitwig Studio.
        * Launch Bitwig Studio and ensure the WigAI extension is active and the MCP server is running (check Bitwig's extension console for logs).
        * Prepare a Bitwig project with necessary elements (e.g., tracks, clips, scenes, devices) for specific test scenarios.
    2.  **Execution:**
        * Use a simple MCP client (e.g., a Python script, Node.js script, `curl`, or a tool like Postman) to send MCP commands (as defined in `docs/mcp-api-spec.md`) to the WigAI server endpoint.
        * Observe the behavior in Bitwig Studio (e.g., does playback start/stop? Does the correct clip launch? Do device parameters change?).
        * Inspect the MCP responses received by the client for correctness (status, data payload, error messages).
    3.  **Verification:**
        * Compare actual outcomes against the Acceptance Criteria defined in the Epic stories.
* **Test Scenarios:** Based directly on the User Stories and Acceptance Criteria in `epic1.md`, `epic2.md`, and `epic3.md`. Examples:
    * Send `ping` command, verify `pong` response.
    * Send `transport_start`, verify Bitwig plays.
    * Send `transport_stop`, verify Bitwig stops.
    * Select a device in Bitwig, send `get_selected_device_parameters`, verify response matches device state.
    * Send `set_selected_device_parameter`, verify parameter changes in Bitwig and response is correct.
    * Send `launch_clip` for a specific clip, verify it plays.
    * Test error conditions: non-existent track, invalid parameter index, etc., and verify appropriate error responses.
* **Tools:**
    * **Bitwig Studio:** Essential for hosting the extension and observing effects.
    * **MCP Client:**
        * Simple scripts (Python with `requests`, Node.js with `axios`/`node-fetch`).
        * Command-line tools like `curl` (for simple POST requests).
        * API testing tools like Postman or Insomnia.
* **Automation Level:** Primarily manual or semi-automated (scripted client sending commands) for MVP. Full E2E automation is complex due to the need to control and observe the Bitwig UI/state programmatically beyond what the extension API might offer for verification.
* **Location of Test Cases/Scripts:** Test scenarios will be documented. Any client scripts used for testing can be stored in a `/test-clients` or `/scripts/test` directory in the project.
* **Expectations:** Cover all primary user workflows and error conditions described in the epics. These tests validate the actual user experience.

## 3. Test Data Management

* **Unit Tests:** Test data (e.g., sample JSON strings for parsing) will be embedded directly in test cases or loaded from resource files in `src/test/resources/`.
* **E2E Tests:** Requires setting up specific states within Bitwig Studio projects (e.g., named tracks, specific number of scenes, devices on tracks). These setups should be simple and reproducible. For MVP, these setups will be done manually before running E2E test scenarios.

## 4. Testing Across Platforms

* **PRD Requirement:** The extension must function on macOS, Windows, and Linux.
* **Strategy:**
    * Development and primary testing might occur on one OS.
    * Key E2E test scenarios should be manually executed on all three supported operating systems before considering a release or significant milestone. This is crucial as OS-level differences or Bitwig's behavior might vary slightly.

## 5. CI/CD Integration

* **Unit Tests:** Automatically executed by Gradle on every commit/push to the main branches via GitHub Actions (or similar CI service if set up for the hobby project). Build should fail if unit tests fail.
* **Integration Tests (within WigAI):** Also run as part of the automated build.
* **E2E Tests:** Not fully automated in CI for MVP due to the Bitwig Studio dependency. Will be a manual step.

## 6. Logging for Tests

* During E2E tests, WigAI's logs in the Bitwig extension console will be vital for diagnosing issues.
* The MCP test client should also log requests sent and responses received.

## Change Log

| Change        | Date       | Version | Description                  | Author              |
| ------------- | ---------- | ------- | ---------------------------- | ------------------- |
| Initial draft | 2025-05-16 | 0.1.0   | First draft of testing strategy. | 3-architect BMAD v2 |