# Operational Guidelines

This document provides guidelines for coding, testing, and other operational aspects of the WigAI project.

## 1. Coding Standards and Patterns

This section outlines the coding standards, design patterns, and best practices to be followed for the WigAI Bitwig Extension project. Adherence to these standards is crucial for maintaining code quality, readability, and ensuring effective collaboration, especially when leveraging AI-assisted development.

### 1.1. Architectural / Design Patterns Adopted

(Details are in `docs/architecture.md` and `docs/component-view.md`)

### 1.2. Coding Standards

* **Primary Language:** Java
    * **Version:** LTS (Java 21) - Ensure code is compatible and leverages modern Java features where appropriate, keeping in mind the Bitwig extension environment.
* **Root Package Name:** `io.github.fabb.wigai`
* **Style Guide & Linter:**
    * **Style:** Google Java Style Guide ([https://google.github.io/styleguide/javaguide.html](https://google.github.io/styleguide/javaguide.html)) is recommended as a base.
    * **Formatter:** Use an automated formatter in the IDE (IntelliJ IDEA, VS Code) configured to align with the chosen style (e.g., Google Java Format). Consistent formatting is key.
    * **Static Analysis:** Consider tools like Checkstyle or PMD if a more rigorous automated check is desired, configured with a subset of rules focusing on maintainability and common pitfalls. For MVP, IDE-integrated checks might suffice.
* **Naming Conventions:**
    * **Packages:** `lowercase.with.dots` (e.g., `io.github.fabb.wigai.common`, `io.github.fabb.wigai.mcp.command`)
    * **Classes & Interfaces:** `PascalCase` (e.g., `WigAIExtension`, `McpServerManager`, `CommandParser`)
    * **Methods:** `camelCase` (e.g., `initializeServer()`, `parseCommand()`)
    * **Variables (local, instance):** `camelCase` (e.g., `host`, `mcpPort`, `currentTrack`)
    * **Constants (static final):** `UPPER_SNAKE_CASE` (e.g., `DEFAULT_MCP_PORT`, `MAX_PARAMETERS`)
    * **Enum Members:** `UPPER_SNAKE_CASE`
    * **Type Parameters (Generics):** Single uppercase letter (e.g., `T`, `E`, `K`, `V`) or descriptive `PascalCase` if more clarity is needed (e.g., `RequestType`).
    * **File Names:** Must match the public class/interface name they contain (e.g., `Logger.java` contains `public class Logger`).
* **File Structure:** Adhere to the layout defined in `docs/project-structure.md`.
* **Java Features:**
    * **Immutability:** Prefer immutable objects where possible, especially for DTOs and configuration. Use `final` for fields that should not change after construction.
    * **Records (Java 16+):** Use Java Records for simple data carrier classes (DTOs) where appropriate (e.g., for parsed MCP command payloads or responses).
        ```java
        // Example DTO using a Record
        package io.github.fabb.wigai.mcp.command.dto;

        public record TransportCommandPayload(String action) {}
        ```
    * **Switch Expressions (Java 14+):** Prefer switch expressions over traditional switch statements for conciseness and clarity when applicable.
    * **Text Blocks (Java 15+):** Use for multi-line string literals (e.g., long JSON examples in comments or test data) if it improves readability.
    * **`var` for Local Variables (Java 10+):** Use `var` for local variable type inference where it enhances readability without obscuring the type. Avoid using it if the inferred type is not immediately obvious from the right-hand side of the assignment.
    * **Streams API:** Use Streams for collection processing where it makes the code more declarative and readable than traditional loops. Avoid overly complex or long stream pipelines.
    * **Optional:** Use `java.util.Optional` appropriately to handle potentially absent values, avoiding null pointer exceptions. Do not overuse it for method parameters or fields where nullability is not the primary concern.
* **Comments & Documentation:**
    * **Javadoc:** All public classes, interfaces, methods, and important fields should have Javadoc comments explaining their purpose, parameters, return values, and any thrown exceptions.
    * **Implementation Comments:** Use comments (`//` or `/* ... */`) within methods to explain complex logic, non-obvious decisions, or workarounds. Avoid commenting on obvious code.
    * **TODOs:** Use `// TODO:` comments to mark areas that require future attention, improvements, or completion. Include a brief explanation or a reference to an issue tracker if possible.
    * **Clarity over cleverness:** Write code that is easy to understand.
* **Dependency Management:**
    * **Tool:** Gradle (as defined in `build.gradle.kts`).
    * **Policy:** Minimize external dependencies to keep the extension lightweight. Clearly justify any new dependency. Prefer libraries that are well-maintained and have compatible licenses (MIT, Apache 2.0).
    * **Versions:** Specify exact versions for dependencies in `build.gradle.kts` to ensure reproducible builds. Avoid dynamic versions (e.g., `+` or version ranges) for release builds.
* **Concurrency:**
    * Be mindful that Bitwig API calls might need to happen on specific threads or have concurrency constraints. For MVP, the MCP server will likely process commands sequentially.
    * If any asynchronous operations are introduced (e.g., background tasks within the extension, though unlikely for MVP core features), use standard Java concurrency utilities (`java.util.concurrent`) carefully.

### 1.3. Error Handling Strategy

* **General Approach:**
    * Use exceptions for exceptional conditions. Define custom exceptions specific to WigAI where appropriate (e.g., `McpParsingException`, `BitwigOperationException`) extending standard Java exceptions.
    * Catch specific exceptions rather than generic `Exception` or `Throwable`.
    * Ensure resources (like network connections if not managed by the MCP SDK) are properly closed in `finally` blocks or by using try-with-resources statements.
* **Logging:**
    * **Library/Method:** Use the centralized `Logger` service (wrapping `host.println()`).
    * **Levels (Conceptual):**
        * `INFO`: For general operational messages, MCP command received/processed.
        * `WARN`: For recoverable issues or potential problems that don't stop the current operation.
        * `ERROR`: For unrecoverable errors within an operation, or significant failures. Include stack traces where helpful.
        * `DEBUG` (Conditional): For verbose logging useful during development, potentially toggled by a flag if needed in the future (for MVP, always on DEBUG might be too noisy).
    * **Context:** Log messages should include relevant context (e.g., MCP command details, affected Bitwig entity).
* **MCP Responses for Errors:**
    * If an MCP command cannot be processed successfully (e.g., invalid parameters, Bitwig action fails), WigAI should attempt to return a structured MCP error response to the client, as defined in `docs/mcp-api-spec.md`. The MCP Java SDK might provide utilities for this.
    * Do not let exceptions propagate to the point where they crash the Bitwig extension or Bitwig Studio itself. The main extension class and MCP request handling entry points should have top-level error handling.
* **Input Validation:**
    * Validate incoming MCP command parameters (e.g., types, ranges, presence of required fields) in the `McpCommandParser` or at the beginning of `Feature Module` handlers.
    * Return an appropriate MCP error if validation fails.

### 1.4. Security Best Practices (for a Local Extension)

While WigAI is a local extension without direct internet exposure beyond the local MCP port, basic security hygiene is important:

* **MCP Port:** The default port should be documented. Users should be advised about firewall configurations if they intend to control WigAI from other machines on their local network. No authentication is planned for MVP.
* **Input Sanitization/Validation:** As mentioned in Error Handling, validate incoming MCP data to prevent unexpected behavior, even if the threat model is low for a local extension.
* **Dependency Security:** Keep dependencies updated. Tools like Gradle can report known vulnerabilities in dependencies.
* **Principle of Least Privilege:** The extension should only request the API access it needs from Bitwig. (This is generally managed by the scope of the Bitwig API itself).

## 2. Testing Strategy

### 2.1. Overall Philosophy & Goals

The testing strategy for WigAI aims to ensure the extension is reliable, correctly implements the specified MCP tools, and integrates seamlessly with Bitwig Studio without causing instability. We will focus on a combination of automated unit tests for isolated logic and manual/semi-automated integration and end-to-end tests due to the nature of interacting with a host DAW.

**Key Goals:**

* **Correctness:** Verify that all implemented MCP tools perform the correct actions in Bitwig Studio and return the specified responses.
* **Robustness:** Ensure the extension handles invalid inputs, unexpected Bitwig states, and errors gracefully without crashing itself or Bitwig Studio.
* **Reliability:** Confirm that features work consistently across different scenarios.
* **Maintainability:** Tests should be easy to understand, write, and maintain alongside the codebase.
* **Developer Confidence:** Enable developers to refactor and add new features with a safety net.

### 2.2. Testing Levels

#### 2.2.1. Unit Tests

* **Scope:** Test individual Java classes and methods in isolation, particularly focusing on:
    * MCP Tool implementations: Validation of MCP tool arguments and conversion to appropriate actions.
    * Logic within specific tool implementations (e.g., tools for transport control, device parameter management, scene launching) that doesn't directly depend on live Bitwig API objects. This includes parameter validation, state transitions, and response formatting.
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

#### 2.2.2. Integration Tests (Within WigAI Extension)

* **Scope:** Test the interaction between different components *within* the WigAI extension, but still potentially without a fully running Bitwig instance, or by using more complex mock setups for the Bitwig API. For example:
    * The MCP Java SDK's tool registry correctly routing tool calls to the appropriate tool implementations.
    * Interaction between tool implementations and a mocked `BitwigApiFacade`.
* **Tools:**
    * JUnit 5.
    * Mockito for parts of the Bitwig API that are complex to simulate live in this context.
* **Location:** Can also reside in `src/test/java/`, possibly in a sub-package like `integration`.
* **Execution:** Run as part of the Gradle build.
* **Expectations:** Verify contracts and collaborations between internal components. Slower than unit tests but faster than end-to-end tests.

#### 2.2.3. End-to-End (E2E) / Manual Acceptance Tests

* **Scope:** Test the entire system flow, from an external MCP client sending commands to WigAI running within a live Bitwig Studio instance, and verifying the actions in Bitwig and the MCP responses. This is the most crucial level for validating user-facing functionality.
* **Process:**
    1.  **Setup:**
        * Install the WigAI extension (`.bwextension` file) in Bitwig Studio.
        * Launch Bitwig Studio and ensure the WigAI extension is active and the MCP server is running (check Bitwig's extension console for logs).
        * Prepare a Bitwig project with necessary elements (e.g., tracks, clips, scenes, devices) for specific test scenarios.
    2.  **Execution:**
        * Use a simple MCP client (e.g., a Python script, Node.js script, `curl`, or a tool like Postman) to send MCP tool calls (as defined in `docs/mcp-api-spec.md`) to the WigAI server endpoint.
        * Observe the behavior in Bitwig Studio (e.g., does playback start/stop? Does the correct clip launch? Do device parameters change?).
        * Inspect the MCP responses received by the client for correctness (result content, error status).
    3.  **Verification:**
        * Compare actual outcomes against the Acceptance Criteria defined in the Epic stories.
* **Test Scenarios:** Based directly on the User Stories and Acceptance Criteria in `epic1.md`, `epic2.md`, and `epic3.md`. Examples:
    * Call the `ping` tool, verify `pong` response.
    * Call the `transport_start` tool, verify Bitwig plays.
    * Call the `transport_stop` tool, verify Bitwig stops.
    * Select a device in Bitwig, call the `get_selected_device_parameters` tool, verify response matches device state.
    * Call the `set_selected_device_parameter` tool, verify parameter changes in Bitwig and response is correct.
    * Call the `launch_clip` tool for a specific clip, verify it plays.
    * Test error conditions: non-existent track, invalid parameter index, etc., and verify appropriate error responses with `isError: true`.
* **Tools:**
    * **Bitwig Studio:** Essential for hosting the extension and observing effects.
    * **MCP Client:**
        * Simple scripts (Python with `requests`, Node.js with `axios`/`node-fetch`).
        * Command-line tools like `curl` (for simple POST requests).
        * API testing tools like Postman or Insomnia.
* **Automation Level:** Primarily manual or semi-automated (scripted client sending commands) for MVP. Full E2E automation is complex due to the need to control and observe the Bitwig UI/state programmatically beyond what the extension API might offer for verification.
* **Location of Test Cases/Scripts:** Test scenarios will be documented. Any client scripts used for testing can be stored in a `/test-clients` or `/scripts/test` directory in the project.
* **Expectations:** Cover all primary user workflows and error conditions described in the epics. These tests validate the actual user experience.

### 2.3. Test Data Management

* **Unit Tests:** Test data (e.g., sample JSON strings for parsing) will be embedded directly in test cases or loaded from resource files in `src/test/resources/`.
* **E2E Tests:** Requires setting up specific states within Bitwig Studio projects (e.g., named tracks, specific number of scenes, devices on tracks). These setups should be simple and reproducible. For MVP, these setups will be done manually before running E2E test scenarios.

### 2.4. Testing Across Platforms

* **PRD Requirement:** The extension must function on macOS, Windows, and Linux.
* **Strategy:**
    * Development and primary testing might occur on one OS.
    * Key E2E test scenarios should be manually executed on all three supported operating systems before considering a release or significant milestone. This is crucial as OS-level differences or Bitwig's behavior might vary slightly.

### 2.5. CI/CD Integration

* **Unit Tests:** Automatically executed by Gradle on every commit/push to the main branches via GitHub Actions (or similar CI service if set up for the hobby project). Build should fail if unit tests fail.
* **Integration Tests (within WigAI):** Also run as part of the automated build.
* **E2E Tests:** Not fully automated in CI for MVP due to the Bitwig Studio dependency. Will be a manual step.

### 2.6. Logging for Tests

* During E2E tests, WigAI's logs in the Bitwig extension console will be vital for diagnosing issues.
* The MCP test client should also log requests sent and responses received.

## Change Log

| Change        | Date       | Version | Description                  | Author              |
| ------------- | ---------- | ------- | ---------------------------- | ------------------- |
| Initial draft | 2025-05-21 | 1.0     | Combined coding standards and testing strategy | GitHub Copilot      |

