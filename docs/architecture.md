# WigAI Architecture Document

## 1. Technical Summary

WigAI is a Bitwig Studio Java Extension that functions as a Model Context Protocol (MCP) server. It enables external AI agents to interact with and control Bitwig Studio for functionalities such as transport control (start/stop playback), clip and scene launching, and reading/writing parameters of the currently selected device. The architecture is designed to be lightweight and run efficiently within the Bitwig Studio environment. It utilizes Java 21 LTS, the official Bitwig Extension API v19, and the MCP Java SDK. WigAI implements the **MCP SSE transport** (Streamable HTTP planned), which uses a single HTTP endpoint for communication and leverages **Server-Sent Events (SSE)** for streaming server-to-client updates. The primary goal is to provide a stable and responsive bridge between AI agents and Bitwig's creative functionalities, adhering to the open-source and no-cost constraints of the project.

## 2. High-Level Overview

WigAI operates as an embedded server within the Bitwig Studio Java Extension. The primary architectural style is a **modular monolith** confined to the extension's process space. An external AI agent (e.g., a copilot in an IDE, a standalone AI assistant) acts as the client. This client sends MCP commands (as JSON payloads) to WigAI over a single HTTP connection, adhering to the **MCP SSE transport** specification (Streamable HTTP planned). WigAI then translates these commands into actions using the Bitwig Java Extension API. Responses and asynchronous updates are sent back to the AI agent over the same HTTP connection, with the MCP Java SDK utilizing **Server-Sent Events (SSE)** to enable the streaming of these server-to-client messages as part of the SSE transport. Streamable HTTP will be adopted once supported by the SDK.

The core interaction flow is:
1. External AI Agent (Client) establishes an HTTP connection to the WigAI MCP Server's single endpoint (SSE transport; Streamable HTTP planned) running within the Bitwig Extension.
2. Client sends an MCP request (e.g., "start playback", "get device parameters") to this endpoint.
3. WigAI Server (using MCP Java SDK components) parses the MCP request.
4. WigAI's core logic maps the MCP command to the appropriate Bitwig Java Extension API calls.
5. WigAI interacts with Bitwig Studio.
6. WigAI Server sends an MCP response back. For ongoing updates or multiple messages, the connection leverages SSE to stream these back to the Client over the same HTTP connection.

```mermaid
graph TD
    subgraph External Environment
        AI_Agent["External AI Agent (Client)"]
    end

    subgraph "Bitwig Studio"
        subgraph "WigAI Extension (MCP Server)"
            MCP_Server_Endpoint["MCP SSE Endpoint (utilizing SSE for streaming; Streamable HTTP planned)"]
            MCP_Handler["MCP Request/Response Handler (MCP Java SDK)"]
            WigAI_Core_Logic["WigAI Core Logic"]
            Bitwig_API_Adapter["Bitwig API Adapter"]
        end
        Bitwig_Host["Bitwig Studio Host Application & API"]
    end

    AI_Agent -- "MCP JSON via SSE (SSE for streaming; Streamable HTTP planned)" --> MCP_Server_Endpoint
    MCP_Server_Endpoint --> MCP_Handler
    MCP_Handler --> WigAI_Core_Logic
    WigAI_Core_Logic --> Bitwig_API_Adapter
    Bitwig_API_Adapter -- "Bitwig API Calls" --> Bitwig_Host
    Bitwig_Host -- "API Responses/Events" --> Bitwig_API_Adapter
    Bitwig_API_Adapter --> WigAI_Core_Logic
    WigAI_Core_Logic --> MCP_Handler
    MCP_Handler -- "MCP JSON via SSE (SSE for streaming; Streamable HTTP planned)" --> AI_Agent

    style WigAI_Extension fill:#f9f,stroke:#333,stroke-width:2px
    style Bitwig_Studio fill:#ccf,stroke:#333,stroke-width:2px
````

## 3. Component View

The WigAI Bitwig Extension consists of several key internal components that work together to provide the MCP server functionality. These components reside entirely within the Bitwig Extension environment.

```mermaid
graph TD
    subgraph WigAI_Extension ["WigAI Extension (Java)"]
        direction LR

        MCP_Server_Node["MCP Server (MCP Java SDK - SSE Transport; Streamable HTTP planned)"]
        Request_Router_Node["Request Router and Dispatcher"]
        Command_Parser_Node["MCP Command Parser"]
        
        subgraph Feature_Modules_Group ["Feature Modules"]
            direction TB
            Transport_Module_Node["Transport Control Module"]
            Device_Module_Node["Device Parameter Module"]
            Clip_Scene_Module_Node["Clip/Scene Launch Module"]
        end

        Bitwig_API_Facade_Node["Bitwig API Facade"]
        Extension_Lifecycle_Node["Extension Lifecycle Manager (Initialization/Shutdown)"]
        Config_Manager_Node["Configuration Manager (Port, etc.)"]
        Logger_Node["Logging Service (host.println)"]

    end

    MCP_Server_Node -->|"MCP Requests"| Request_Router_Node
    Request_Router_Node -->|"Parsed Commands"| Command_Parser_Node
    Command_Parser_Node -->|"Validated Commands"| Feature_Modules_Group
    
    Feature_Modules_Group -->|"Bitwig Actions"| Bitwig_API_Facade_Node
    Bitwig_API_Facade_Node -->|"Bitwig API Events/Data"| Feature_Modules_Group
    
    Extension_Lifecycle_Node -->|"Manages"| MCP_Server_Node
    Extension_Lifecycle_Node -->|"Manages"| Config_Manager_Node
    Extension_Lifecycle_Node -->|"Manages"| Logger_Node

    Config_Manager_Node -->|"Config Data"| MCP_Server_Node
    Config_Manager_Node -->|"Config Data"| Logger_Node

    Feature_Modules_Group -->|"Log Messages"| Logger_Node
    MCP_Server_Node -->|"Log Messages"| Logger_Node
    Request_Router_Node -->|"Log Messages"| Logger_Node
    Bitwig_API_Facade_Node -->|"Log Messages"| Logger_Node
    
    style WigAI_Extension fill:#f9f,stroke:#333,stroke-width:2px
    style Feature_Modules_Group fill:#lightgrey,stroke:#333,stroke-width:1px
```

**Component Descriptions:**

  * **`Extension Lifecycle Manager (Extension_Lifecycle_Node)`**:

      * **Responsibility:** Manages the startup and shutdown of the WigAI extension. Initializes and terminates other components, including the MCP Server. This corresponds to the main class extending `ControllerExtension`.
      * **Interactions:** Initializes `Config_Manager_Node`, `Logger_Node`, and `MCP_Server_Node`. Hooks into Bitwig's extension lifecycle methods (`init()`, `exit()`).

  * **`Config Manager (Config_Manager_Node)`**:

      * **Responsibility:** Handles configuration for the extension, primarily the network port for the MCP server. For MVP, this might be hardcoded with clear documentation, but designed for future configurability.
      * **Interactions:** Provides configuration data (e.g., port number) to the `MCP_Server_Node`.

  * **`Logger (Logger_Node)`**:

      * **Responsibility:** Provides a centralized logging facility for all components. Wraps Bitwig's `host.println()` for outputting messages to the Bitwig extension console.
      * **Interactions:** Used by most other components to log information, warnings, and errors.

  * **`MCP Server (MCP_Server_Node)`**:

      * **Responsibility:** Leverages the MCP Java SDK to configure and run an MCP-compliant server with the SSE transport (Streamable HTTP planned). This includes setting up the server with proper capabilities, registering tools, and handling the server lifecycle. Uses the SDK's built-in JSON-RPC 2.0 message processing, tool registry, and transport implementation.
      * **Interactions:** Manages the MCP server lifecycle. Configures and initializes the SDK's server components. Registers tool implementations. Managed by `Extension_Lifecycle_Node`. Uses `Config_Manager_Node` for configuration.

  * **`MCP SDK Tool Registry (Request_Router_Node)`**:

      * **Responsibility:** Leverages the MCP Java SDK's built-in tool registry system to register, manage, and route requests to the appropriate tool implementation. Replaces the need for a custom request router and command parser with the SDK's tool routing capabilities.
      * **Interactions:** Integrated with the MCP Java SDK's server components. Routes tool calls to the appropriate tool implementation classes.

  * **`MCP Tool Implementations (Command_Parser_Node)`**:

      * **Responsibility:** Implements the MCP Java SDK's tool interfaces for each supported tool (ping, transport_start, etc.). Each tool implementation defines its schema, validates inputs using the SDK's facilities, and handles the tool-specific business logic.
      * **Interactions:** Receives tool calls from the MCP SDK's routing system. Interacts with the appropriate `Feature Module` within `Feature_Modules_Group`.

  * **`Feature Modules (Feature_Modules_Group)`**: This group encapsulates the logic for handling specific categories of MCP commands.

      * **`Transport Control Module (Transport_Module_Node)`**: Handles commands like `transport_start`, `transport_stop`.
      * **`Device Parameter Module (Device_Module_Node)`**: Handles commands like `get_selected_device_parameters`, `set_selected_device_parameter`.
      * **`Clip/Scene Launch Module (Clip_Scene_Module_Node)`**: Handles commands like `launch_clip`, `launch_scene_by_index`, `launch_scene_by_name`.
      * **Responsibility (Overall):** Execute the business logic for their respective features by interacting with the `Bitwig_API_Facade_Node`. Formulate MCP responses.
      * **Interactions:** Receive parsed commands from `Command_Parser_Node`. Interact with `Bitwig_API_Facade_Node`. Send response data back towards the `MCP_Server_Node`.

  * **`Bitwig API Facade (Bitwig_API_Facade_Node)`**:

      * **Responsibility:** Provides a simplified and tailored interface to the Bitwig Java Extension API. It abstracts the complexities of the Bitwig API, offering clear methods for actions required by the `Feature Modules` (e.g., `startPlayback()`, `getSelectedDeviceParameters()`, `launchClip(trackName, clipIndex)`). It also handles callbacks or listeners from the Bitwig API if needed for observing state changes.
      * **Interactions:** Called by `Feature Modules`. Interacts directly with the Bitwig `Host` object and other Bitwig API objects.

## 4. Key Architectural Decisions & Patterns

This section highlights the significant architectural choices made for WigAI and the reasoning behind them.

  * **Modular Monolith within Extension:**
      * **Decision:** WigAI is designed as a single, deployable Bitwig Extension (`.bwextension` file) but with a clear internal modular structure (as shown in the Component View).
      * **Justification:** Bitwig extensions are inherently monolithic in their deployment. A modular internal design promotes separation of concerns, testability, and maintainability within this constraint. This approach avoids the complexity of inter-process communication for a system that naturally runs embedded within Bitwig.
  * **MCP Java SDK for Core Protocol Handling:**
      * **Decision:** Utilize the official MCP Java SDK (0.8.0+) for implementing the MCP server logic, including JSON-RPC 2.0 message processing, tool registration and validation, and standard endpoint handling.
      * **Justification:** This SDK is specifically designed for the Model Context Protocol, ensuring spec compliance and reducing the boilerplate code needed for protocol handling. It provides built-in support for necessary transport mechanisms like SSE (Streamable HTTP planned), as well as comprehensive tool registration, validation, and request handling components.
  * **SSE Transport:**
    * **Purpose:** Provides streaming server-to-client updates for MCP. Streamable HTTP will be adopted once supported by the SDK.
  * **Facade Pattern for Bitwig API Interaction (`Bitwig API Facade`):**
      * **Decision:** Introduce a facade component to abstract direct interactions with the Bitwig Java Extension API.
      * **Justification:** This simplifies the code in the `Feature Modules` by providing a cleaner, more domain-specific interface to Bitwig functionalities. It also centralizes Bitwig API knowledge, making it easier to adapt to potential Bitwig API changes in the future.
  * **Tool-Based Interface Pattern:**
      * **Decision:** Implement all WigAI functionality as MCP "tools" using the SDK's tool interfaces and registry system rather than custom command handlers.
      * **Justification:** The MCP Java SDK provides a standardized way to define tools with schemas, handle their registration, and process requests. This approach simplifies implementation, ensures proper validation, and enables automatic discovery through the standard `tools/list` endpoint.

  * **Lightweight Embedded Server:**
      * **Decision:** The MCP server (provided by or configured through the MCP Java SDK) will be lightweight and embedded directly within the Java extension.
      * **Justification:** To minimize resource footprint and avoid unnecessary complexity within the Bitwig Studio environment. We aim to avoid pulling in heavy frameworks like a full Spring Boot application context if the MCP Java SDK's server components can be used more directly.
  * **Configuration via Constants (MVP):**
      * **Decision:** For the MVP, essential configurations like the listening port will be managed via internal constants within the code, clearly documented.
      * **Justification:** Simplifies initial development. The `Config Manager` component is designed to allow for more sophisticated configuration methods (e.g., files, UI in Bitwig) in future iterations if needed.
  * **Logging via `host.println()`:**
      * **Decision:** All logging will be directed through a simple `Logger` service that uses Bitwig's native `host.println()` method.
      * **Justification:** This integrates seamlessly with Bitwig's built-in extension logging console, making it easy for users and developers to view diagnostic information without external tools.

## 5. Infrastructure and Deployment Overview

  * **Hosting/Cloud Provider(s):** Not applicable. WigAI is a local Bitwig Studio extension and runs entirely within the user's Bitwig Studio environment.
  * **Core Services Used:**
      * Bitwig Studio Java Extension API (version 19)
      * MCP Java SDK (for MCP server implementation)
      * Embedded HTTP server components as utilized by the MCP Java SDK's transport layer (e.g., Jetty, if the Servlet-based transport is used).
  * **Infrastructure as Code (IaC):** Not applicable.
  * **Deployment Strategy:**
      * **Output:** A single `.bwextension` file.
      * **Process:** The user manually copies the `.bwextension` file into their Bitwig Studio `Extensions` folder. Bitwig Studio then loads the extension.
      * **Build Tools:** Gradle will be used to compile the Java code and package it into the `.bwextension` file.
  * **Environments:**
      * **Development:** Local machine with Bitwig Studio, a Java IDE (IntelliJ IDEA CE / VS Code), and Gradle.
      * **Testing:** Similar to development, using Bitwig Studio for integration testing and a separate MCP client (e.g., a script) for end-to-end command testing.
      * **Production:** The user's Bitwig Studio installation on macOS, Windows, or Linux.
  * **(See `docs/environment-vars.md` for any potential runtime configuration details, though MVP primarily uses internal constants).**
  * **(See `docs/project-structure.md` for code organization and build output paths).**

## 6. Key Reference Documents

This architecture document should be read in conjunction with the following detailed specifications and guidelines:

  * `docs/prd.md`: Product Requirements Document, outlining the goals, scope, and features of WigAI.
  * `docs/epic1.md`, `docs/epic2.md`, `docs/epic3.md`: Detailed functional requirements and user stories for each epic.
  * `docs/tech-stack.md`: Specifies all technologies, frameworks, and library versions used in the project.
  * `docs/project-structure.md`: Defines the organization of directories and files within the project. (To be created)
  * `docs/coding-standards.md`: Outlines coding conventions, patterns, and best practices to be followed. (To be created)
  * `docs/api-reference.md` (or `docs/mcp-api-spec.md`): Details the specific MCP commands, their JSON structures, parameters, and expected responses that WigAI will support. (To be created)
  * `docs/data-models.md`: Describes any significant data structures or objects used internally or in API communication. (To be created, if necessary beyond API spec)
  * `docs/environment-vars.md`: Documents any environment variables used for configuration (though MVP aims for minimal). (To be created)
  * `docs/testing-strategy.md`: Outlines the approach to unit, integration, and end-to-end testing. (To be created)
  * Bitwig Java Extension API Documentation (Version 19): External reference for interacting with Bitwig Studio.
  * Model Context Protocol (MCP) Specification: External reference for MCP principles and the Streamable HTTP transport. ([https://modelcontextprotocol.io/](https://modelcontextprotocol.io/))
  * MCP Java SDK Documentation: External reference for the chosen Java SDK.

## 7. Change Log

| Change        | Date       | Version | Description                                      | Author              |
| ------------- | ---------- | ------- | ------------------------------------------------ | ------------------- |
| Initial draft | 2025-05-16 | 0.1     | Initial draft of architecture document sections. | 3-architect BMAD v2 |
| Update        | 2025-05-18 | 0.2     | Updated component descriptions to align with MCP Java SDK 0.8.0+ integration. Revised MCP components to reflect the SDK's tool-based approach. | Architect Agent    |

