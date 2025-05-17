# WigAI Product Requirements Document (PRD)

## Intro

WigAI is a software component designed to enhance the creative workflow for Bitwig Studio musicians. It functions as a **Model Context Protocol (MCP) server** implemented as a **Bitwig Java Extension**. This allows external AI agents (e.g., IDE-based copilots, standalone AI assistants) to interact with and control Bitwig Studio via text-based commands translated into MCP messages. The Minimum Viable Product (MVP) focuses on enabling core functionalities: AI-driven playback control, clip/scene triggering, and manipulation of device parameters, all initiated by an external AI agent interpreting user's text commands. This project aims to provide a new, intelligent, and hands-free way to interact with Bitwig, fostering creativity and aiding live performance.

## Goals and Context

-   **Project Objectives:**
    1.  To develop a Bitwig Java extension that acts as an MCP server, exposing control over specific Bitwig functionalities.
    2.  Enable external AI agents to control Bitwig's transport (start/stop playback) via MCP commands.
    3.  Enable external AI agents to trigger user-specified clips (by track name and clip index/scene number) and entire scenes in Bitwig via MCP commands.
    4.  Enable external AI agents to read and set the eight (8) parameters of the currently selected device in Bitwig via MCP commands.
    5.  To achieve this using exclusively open-source tools and the official Bitwig Java Extension API (version 22).
-   **Measurable Outcomes:**
    1.  Successful and reliable starting and stopping of Bitwig playback initiated by an MCP command.
    2.  Successful and reliable triggering of specified clips and scenes in Bitwig initiated by an MCP command.
    3.  Successful and reliable reading and setting of the 8 parameters for the currently selected Bitwig device initiated by MCP commands.
-   **Success Criteria:**
    * The WigAI Bitwig extension installs and runs stably on all Bitwig-supported platforms (macOS, Windows, Linux).
    * All three MVP functionalities (transport, clip/scene launch, device parameter control) are demonstrably working as per the defined MCP interactions.
    * The system can handle valid MCP requests for the implemented features without crashing Bitwig or the extension.
    * Basic error states (e.g., clip not found, invalid parameter index) are communicated back via MCP if the protocol supports it, or logged appropriately.
-   **Key Performance Indicators (KPIs):**
    * Number of successful MCP commands processed for each core feature (transport, clip/scene launch, parameter control).
    * (Post-MVP/User Adoption) Number of unique Bitwig parameters successfully controlled via WigAI (as an indicator of utility and potential expansion).

## Scope and Requirements (MVP / Current Version)

### Functional Requirements (High-Level)

1.  **MCP Server Implementation (Bitwig Extension):**
    * Implement a server within the Bitwig Java Extension that listens for and processes incoming MCP messages on a configurable local network port.
    * Adhere to basic MCP principles for request/response handling for the scope of implemented features.
2.  **Transport Control:**
    * Accept MCP commands to start Bitwig's playback.
    * Accept MCP commands to stop Bitwig's playback.
3.  **Clip and Scene Launching:**
    * Accept MCP commands to launch a specific clip, identified by track name and clip index (slot/scene number).
    * Accept MCP commands to launch an entire scene, identified by its name or index.
    * Provide feedback on the success or failure of the launch command (e.g., clip/scene not found).
4.  **Device Parameter Control (Selected Device):**
    * Accept MCP commands to read the current values of the eight (8) addressable parameters of the device currently selected by the user in Bitwig's UI.
    * Return the parameter names (if available via API) and their current values in an MCP response.
    * Accept MCP commands to set specific values for one or more of the eight (8) addressable parameters of the currently selected device.
    * The external AI agent is responsible for translating user's natural language (e.g., "make parameter 1 higher", "randomize parameters slightly") into specific parameter indices and values for the MCP command. WigAI MVP will not perform natural language interpretation for parameter changes.

### Non-Functional Requirements (NFRs)

-   **Performance:**
    * MCP command processing should be near real-time to ensure a responsive user experience when interacting via the AI agent. (e.g., <100ms processing time within the extension for most commands, excluding Bitwig's own processing time).
-   **Scalability:**
    * The extension should handle sequential MCP commands robustly. (Concurrent command handling is not an MVP focus but the server should not break if multiple connections are attempted; it can process one at a time).
-   **Reliability/Availability:**
    * The Bitwig extension must be stable and not cause Bitwig Studio to crash.
    * Basic error handling for invalid MCP messages or failed Bitwig API calls should be implemented, with errors logged within Bitwig's extension console.
-   **Security:**
    * The MCP server will listen on a local network port. No specific authentication mechanisms for incoming MCP connections are required for MVP, as it's intended for local control. Users should be aware of this if exposing the port more widely.
-   **Maintainability:**
    * Java code should be well-organized, commented, and follow standard Java conventions to facilitate understanding and future development (especially important for an open-source hobby project).
-   **Usability/Accessibility:**
    * Not directly applicable to WigAI itself, as it is a server component without a direct UI. The usability and accessibility pertain to the external AI agent and its interface.
-   **Compatibility:**
    * Must function on all operating systems supported by Bitwig Studio (macOS, Windows, Linux).
    * Must use and be compatible with the official Bitwig Java Extension API version 22.
-   **Resource Usage:**
    * The extension should be mindful of CPU and memory usage and not unduly burden the Bitwig Studio process.
-   **Open Source:**
    * All tools, libraries, and code developed must be compatible with open-source licensing. (Specific license to be decided, e.g., MIT, Apache 2.0).

### User Experience (UX) Requirements (High-Level)

-   There is no direct UI for WigAI itself. All user interaction is mediated by an external AI agent (e.g., VS Code Copilot, Claude Desktop) which takes text input from the user.
-   The interaction model assumes the user types commands to the AI agent, and the AI agent translates these into MCP messages to be sent to WigAI.

### Integration Requirements (High-Level)

1.  **External AI Agent Integration:**
    * WigAI will expose an MCP server endpoint (configurable local IP and port) for external AI agents to connect and send MCP messages.
    * The specific MCP message schema (JSON structure, command names, parameters) for each supported function (transport, clip/scene launch, device parameters) needs to be defined and documented for AI agent developers.
2.  **Bitwig Studio Integration:**
    * WigAI will use the official Bitwig Java Extension API (version 22) to interact with Bitwig Studio for all its functionalities.

### Testing Requirements (High-Level)

-   Unit tests for individual Java classes and methods, particularly those handling MCP message parsing and Bitwig API interactions.
-   Manual end-to-end testing using a simple script-based or command-line MCP client to simulate an external AI agent and verify all supported functionalities within Bitwig.
-   Testing across all supported operating systems (macOS, Windows, Linux) is desired, if feasible for the hobbyist developer.

## Epic Overview (MVP / Current Version)

-   **Epic 1: Core Extension Setup, MCP Server Initialization, and Basic Transport Control**
    * **Goal:** Establish the foundational Bitwig Java extension, implement the basic MCP server listener, and enable start/stop transport control via MCP commands. This proves the core communication pathway.
-   **Epic 2: Device Parameter Read and Write Implementation**
    * **Goal:** Enable reading and writing of the eight parameters for the currently selected Bitwig device via MCP commands, allowing the AI agent to both query device state and modify it.
-   **Epic 3: Clip and Scene Launching Implementation**
    * **Goal:** Enable the triggering of individual clips (by track name and clip index/scene) and entire scenes (by name or index) via MCP commands, expanding interactive control.

## Key Reference Documents

-   `docs/project-brief.md`
-   `docs/architecture.md` (To be created by Architect)
-   `docs/epic1.md`, `docs/epic2.md`, `docs/epic3.md` (To be created)
-   `docs/tech-stack.md` (To be created by Architect, will primarily list Java, Bitwig API, MCP)
-   `docs/mcp-api-spec.md` (To be created, detailing MCP commands for WigAI)
-   `docs/testing-strategy.md` (To be created by Architect/Dev)

## Post-MVP / Future Enhancements

-   Support for a wider range of Bitwig API functionalities (e.g., track creation, device insertion, note manipulation).
-   More sophisticated MCP query capabilities (e.g., listing available tracks, scenes, devices, parameters).
-   Direct handling of "creative" commands within WigAI with more built-in logic (e.g., "randomize parameters with X characteristic"), reducing reliance on external AI agent's interpretation for common creative gestures.
-   Support for asynchronous operations and notifications from WigAI to the MCP client (e.g., when a new track is added manually in Bitwig).
-   Built-in mapping capabilities for users to customize how AI commands map to Bitwig actions beyond the selected device.
-   Exploration of simpler setup/discovery mechanisms for the MCP connection.

## Change Log

| Change        | Date       | Version | Description                  | Author         |
| ------------- | ---------- | ------- | ---------------------------- | -------------- |
| Initial Draft | 2025-05-16 | 0.1     | First draft of the PRD       | 2-pm BMAD v2   |

## Initial Architect Prompt

### Technical Infrastructure

-   **Starter Project/Template:** None explicitly required, but standard Java project structure for a Bitwig Extension should be followed. Examples like "DrivenByMoss" can serve as a reference for structure and API usage.
-   **Hosting/Cloud Provider:** Not applicable. WigAI is a local Bitwig Studio extension.
-   **Frontend Platform:** Not applicable.
-   **Backend Platform:** The "backend" is the WigAI Java extension itself, running within the Bitwig Studio process. It will host an MCP server (e.g., using a lightweight HTTP server library in Java if MCP is over HTTP, or a raw socket listener if appropriate for MCP).
-   **Database Requirements:** Not applicable for MVP.

### Technical Constraints

-   **Mandatory Technology:** Java (language level compatible with Bitwig Extension API v22 requirements, likely Java 8 or higher). Official Bitwig Java Extension API (version 22: `https://maven.bitwig.com/com/bitwig/extension-api/22/`).
-   **Model Context Protocol (MCP):** The extension must correctly implement server-side handling for MCP messages related to the defined MVP features. The exact MCP message structures need to be defined.
-   **Operating System Compatibility:** The extension must be compatible with macOS, Windows, and Linux (wherever Bitwig Studio runs). Code should be platform-agnostic.
-   **Budget:** $0. All libraries and tools used must be open-source and free of charge.
-   **Resource Management:** The extension must be efficient and not cause performance degradation or instability in Bitwig Studio.

### Deployment Considerations

-   **Deployment:** The output will be a `.bwextension` file, deployed by the user into their Bitwig Studio extensions folder.
-   **CI/CD:** Not an MVP requirement for this hobby project, but a simple build script (e.g., Gradle, Maven) for compiling and packaging the extension is expected.
-   **Environment Requirements:** Runs within the Bitwig Studio Java Virtual Machine environment.

### Local Development & Testing Requirements

-   **Development Environment:** Standard Java IDE (e.g., IntelliJ IDEA Community Edition, Eclipse) with appropriate build tools (Maven or Gradle, as used by Bitwig extension examples).
-   **Testing Tools:**
    * JUnit for unit testing Java components.
    * A simple MCP client (e.g., a Python script, Node.js script, or a command-line tool like `curl` if MCP is HTTP-based) for sending test messages to WigAI running in Bitwig.
    * Bitwig Studio itself is required for integration testing.
-   **Logging:** The extension should use Bitwig's provided extension logging mechanisms (`host.println()`) for diagnostics.

### Other Technical Considerations

-   **API Evolution:** Be mindful that Bitwig's API can evolve. Design for reasonable separation of concerns to ease adaptation to future API versions.
-   **MCP Specification:** Closely follow the MCP specification (from `https://modelcontextprotocol.io/`) for the parts being implemented. If the MCP spec is abstract, concrete message formats for WigAI will need to be defined.
-   **Error Handling:** Robust error handling is important. The extension should not crash Bitwig. Errors in processing MCP requests should be logged, and where appropriate, an error response sent back via MCP (if the protocol supports this).
-   **Simplicity for MVP:** Focus on achieving the core MVP features with minimal complexity. The external AI agent handles complex interpretations; WigAI handles concrete MCP commands.
