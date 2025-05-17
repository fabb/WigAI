# WigAI Coding Standards and Patterns

This document outlines the coding standards, design patterns, and best practices to be followed for the WigAI Bitwig Extension project. Adherence to these standards is crucial for maintaining code quality, readability, and ensuring effective collaboration, especially when leveraging AI-assisted development.

## 1. Architectural / Design Patterns Adopted

The following high-level architectural and design patterns have been adopted for WigAI, as detailed in `docs/architecture.md`:

* **Modular Monolith within Extension:** The extension is a single deployable unit with clear internal module boundaries.
* **MCP Java SDK for Protocol Handling:** Leveraging the official SDK for MCP logic.
* **Streamable HTTP Transport (utilizing SSE):** For MCP communication.
* **Facade Pattern (`BitwigApiFacade`):** To abstract Bitwig API interactions.
* **Dependency Injection (Implicit/Manual):** While not using a full DI framework like Spring for the core extension, dependencies will be manually injected or passed where appropriate (e.g., `Host` object from Bitwig, `Logger`, `ConfigManager` to components that need them) to promote loose coupling and testability.
* **Clear Separation of Concerns:**
    * Network/Protocol Layer (MCP Server, Request Router, Command Parser)
    * Business Logic/Feature Layer (Feature Modules)
    * DAW Interaction Layer (Bitwig API Facade)
    * Cross-Cutting Concerns (Lifecycle, Config, Logging)

## 2. Coding Standards

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

## 3. Error Handling Strategy

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

## 4. Security Best Practices (for a Local Extension)

While WigAI is a local extension without direct internet exposure beyond the local MCP port, basic security hygiene is important:

* **MCP Port:** The default port should be documented. Users should be advised about firewall configurations if they intend to control WigAI from other machines on their local network. No authentication is planned for MVP.
* **Input Sanitization/Validation:** As mentioned in Error Handling, validate incoming MCP data to prevent unexpected behavior, even if the threat model is low for a local extension.
* **Dependency Security:** Keep dependencies updated. Tools like Gradle can report known vulnerabilities in dependencies.
* **Principle of Least Privilege:** The extension should only request the API access it needs from Bitwig. (This is generally managed by the scope of the Bitwig API itself).

## 5. Testing

* Refer to `docs/testing-strategy.md` for detailed testing approaches.
* Write unit tests for business logic in feature modules, command parsers, and utility classes.
* Aim for good test coverage of critical components.

## Change Log

| Change        | Date       | Version | Description                  | Author              |
| ------------- | ---------- | ------- | ---------------------------- | ------------------- |
| Initial draft | 2025-05-16 | 0.1     | First draft of coding standards | 3-architect BMAD v2 |