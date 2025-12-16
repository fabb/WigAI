# Epic 1: Core Extension Setup, MCP Server Initialization, and Basic Transport Control

**Goal:** Establish the foundational Bitwig Java extension, implement the basic MCP server listener, and enable start/stop transport control via MCP commands. This proves the core communication pathway and fulfills PRD Objective 2.
**Architectural Alignment:** This epic leverages the `WigAIExtension`, `WigAIExtensionDefinition`, `Logger`, `ConfigManager`, `McpServerManager`, `RequestRouter`, `McpCommandParser`, `TransportController`, and `BitwigApiFacade` components. It also relies on `docs/project-structure.md`, `docs/coding-standards.md`, `docs/tech-stack.md`, and `docs/api-reference.md`.

## Story List

### Refined Story 1.1: Basic Bitwig Extension Project Setup
* **User Story / Goal:** As a WigAI Developer, I want a basic, runnable Bitwig Java extension project configured with the necessary dependencies (Bitwig API v19, MCP Java SDK) and build system (Gradle), adhering to the defined project structure and coding standards, so that I have a foundation for implementing WigAI features.
* **Detailed Requirements:**
    * Set up a new Java project using Gradle, targeting Java 21 LTS.
    * The root package shall be `io.github.fabb.wigai`.
    * Include `com.bitwig.extension-api:19` and the latest stable MCP Java SDK as dependencies in `build.gradle.kts`.
    * Configure the Gradle build script (`build.gradle.kts`) to compile the extension and package it into a `.bwextension` file (as detailed in technical story TS6).
    * Create initial `io.github.fabb.wigai.WigAIExtension.java` (extending `ControllerExtension`) and `io.github.fabb.wigai.WigAIExtensionDefinition.java` (extending `ControllerExtensionDefinition`) classes.
    * Implement basic `Logger` (`io.github.fabb.wigai.common.Logger.java`) and `ConfigManager` (`io.github.fabb.wigai.config.ConfigManager.java`) stubs as per technical story TS1.
    * The project structure must adhere to `docs/project-structure.md`.
    * The basic extension should successfully load into Bitwig Studio (5.2.7 or later, all platforms) without errors, identifiable in Bitwig's extension list with its name and version.
    * The extension should output a "WigAI Extension Loaded - Version X.Y.Z" message (including its version from `WigAIExtensionDefinition`) to the Bitwig extension console via the `Logger` on startup.
* **Acceptance Criteria (ACs):**
    * AC1: The project compiles successfully via Gradle into a `.bwextension` file.
    * AC2: The generated `.bwextension` file can be loaded into Bitwig Studio (5.2.7 or later) without errors.
    * AC3: The specified confirmation message with the correct version appears in the Bitwig extension console when the extension is activated.
    * AC4: The project structure and initial classes adhere to `docs/project-structure.md` and use the package `io.github.fabb.wigai`.
    * AC5: `Logger` and `ConfigManager` stubs are present and integrated into `WigAIExtension`.

---

### Refined Story 1.2: MCP Server Listener Implementation
* **User Story / Goal:** As the WigAI System, I want to initialize an MCP server listener (using the MCP Java SDK with SSE transport; Streamable HTTP planned) on a configurable local network port when the Bitwig extension starts, so that external AI agents can connect and send MCP commands.
* **Detailed Requirements:**
    * Implement an `McpServerManager` class within the `io.github.fabb.wigai.mcp` package.
    * This manager will use the MCP Java SDK to configure and start an embedded HTTP server supporting the MCP SSE transport (Streamable HTTP will be used once supported).
    * The listening IP address should default to `localhost`.
    * The port number should be retrieved from the `ConfigManager` (defaulting to `61169` as per `AppConstants`).
    * The server (managed by `McpServerManager`) should start when the `WigAIExtension`'s `init()` method is called and the extension is enabled.
    * The server should shut down gracefully when the `WigAIExtension`'s `exit()` method is called (extension is disabled or Bitwig closes).
    * Log server status (e.g., "MCP Server started on http://localhost:61169/mcp", "MCP Server stopped") to the Bitwig extension console via the `Logger` service.
    * The server should be able to accept incoming HTTP connections on its defined MCP endpoint (e.g., `/mcp`).
* **Acceptance Criteria (ACs):**
    * AC1: The `McpServerManager` successfully starts the embedded MCP server on the configured port (e.g., 61169) when the extension loads.
    * AC2: A log message from the `Logger` service confirms the MCP server has started, including the listening address and port.
    * AC3: The MCP server stops gracefully when the extension is disabled or Bitwig exits, confirmed by a log message.
    * AC4: An external tool (e.g., `curl` or a simple HTTP client) can make a basic HTTP GET or POST request to the MCP endpoint (e.g., `http://localhost:61169/mcp`) and receive an HTTP response (even if it's an MCP error for an unknown command at this stage, the HTTP connection itself should work).

---

### Refined Story 1.3: Implement MCP "Ping" Command
* **User Story / Goal:** As an External AI Agent Developer, I want to send a "ping" command via MCP to the WigAI server and receive a success response as defined in the API specification, so that I can verify connectivity and that WigAI is operational.
* **Detailed Requirements:**
    * The WigAI MCP server (specifically the `RequestRouter` and `McpCommandParser` components) must be able to receive and parse an MCP command for "ping".
    * The command structure is `{"command": "ping"}` with no additional payload, as per `docs/api-reference.md`.
    * Upon receiving a valid "ping" command:
        * WigAI should not need to interact with the Bitwig API for this command.
        * It should construct a success response as specified in `docs/api-reference.md`:
          ```json
          {
            "status": "success",
            "data": {
              "response": "pong",
              "wigai_version": "0.1.0" // Fetched from WigAIExtensionDefinition
            }
          }
          ```
    * The version reported in `wigai_version` should be dynamically retrieved from the `WigAIExtensionDefinition` (or a constant reflecting it).
    * The `RequestRouter` should log the receipt of the "ping" command and the response sent.
* **Acceptance Criteria (ACs):**
    * AC1: Sending a valid "ping" MCP command (e.g., `{"command": "ping"}`) to the WigAI server's MCP endpoint results in a successful MCP response with `Content-Type: application/json` and the body matching the specified JSON structure (including "pong" and the current WigAI version).
    * AC2: The `Logger` service logs the receipt of the "ping" command and the content of the response.
    * AC3: Invalid or malformed "ping" commands (e.g., incorrect JSON, missing `command` field) result in an appropriate MCP error response (e.g., `INVALID_REQUEST` as per `docs/api-reference.md`) or are safely ignored and logged if error response generation is not yet fully implemented for this specific case.
    * AC4: The `wigai_version` in the response accurately reflects the version set in `WigAIExtensionDefinition`.

---

### Refined Story 1.4: Implement MCP Transport Start Command
* **User Story / Goal:** As a Musician (via an External AI Agent), I want to command WigAI to start Bitwig's playback via an MCP message, so that I can control transport hands-free.
* **Detailed Requirements:**
    * The WigAI MCP server (via `RequestRouter`, `McpCommandParser`, and the `TransportController` feature module) must handle the "transport_start" command.
    * The command structure is `{"command": "transport_start"}` with no additional payload, as per `docs/api-reference.md`.
    * Upon receiving this command, the `TransportController` will use the `BitwigApiFacade` to call the appropriate Bitwig API method to start the main transport playback (e.g., `host.transport().play()`).
    * Return an MCP success response as specified in `docs/api-reference.md`:
      ```json
      {
        "status": "success",
        "data": {
          "action": "transport_started",
          "message": "Bitwig transport started."
        }
      }
      ```
    * Log the command receipt, the action taken, and the response.
* **Acceptance Criteria (ACs):**
    * AC1: Sending the "transport_start" MCP command to WigAI's MCP endpoint causes Bitwig's main transport playback to start.
    * AC2: WigAI returns a successful MCP response matching the specified JSON structure.
    * AC3: If playback is already playing, the command is gracefully handled (Bitwig API typically handles this; WigAI should still return success).
    * AC4: The `Logger` service logs the command, the invocation of the Bitwig play action, and the response.

---

### Refined Story 1.5: Implement MCP Transport Stop Command
* **User Story / Goal:** As a Musician (via an External AI Agent), I want to command WigAI to stop Bitwig's playback via an MCP message, so that I can control transport hands-free.
* **Detailed Requirements:**
    * The WigAI MCP server (via `RequestRouter`, `McpCommandParser`, and the `TransportController` feature module) must handle the "transport_stop" command.
    * The command structure is `{"command": "transport_stop"}` with no additional payload, as per `docs/api-reference.md`.
    * Upon receiving this command, the `TransportController` will use the `BitwigApiFacade` to call the appropriate Bitwig API method to stop the main transport playback (e.g., `host.transport().stop()`).
    * Return an MCP success response as specified in `docs/api-reference.md`:
      ```json
      {
        "status": "success",
        "data": {
          "action": "transport_stopped",
          "message": "Bitwig transport stopped."
        }
      }
      ```
    * Log the command receipt, the action taken, and the response.
* **Acceptance Criteria (ACs):**
    * AC1: Sending the "transport_stop" MCP command to WigAI's MCP endpoint causes Bitwig's main transport playback to stop.
    * AC2: WigAI returns a successful MCP response matching the specified JSON structure.
    * AC3: If playback is already stopped, the command is gracefully handled (Bitwig API typically handles this; WigAI should still return success).
    * AC4: The `Logger` service logs the command, the invocation of the Bitwig stop action, and the response.

## Change Log

| Change                                | Date       | Version | Description                             | Author              |
| ------------------------------------- | ---------- | ------- | --------------------------------------- | ------------------- |
| Initial Draft                         | 2025-05-16 | 0.1     | First draft of Epic 1 stories           | 2-pm BMAD v2        |
| Architectural Refinement of Stories   | 2025-05-16 | 0.2     | Aligned stories with defined architecture | 3-architect BMAD v2 |
