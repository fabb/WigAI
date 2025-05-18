# WigAI Technology Stack

## Technology Choices

| Category             | Technology              | Version / Details        | Description / Purpose                                  | Justification (Optional)                                                                 |
| :------------------- | :---------------------- | :----------------------- | :----------------------------------------------------- | :--------------------------------------------------------------------------------------- |
| **Languages** | Java                    | LTS (21)                 | Primary language for Bitwig extension development        | Mandatory by Bitwig API; LTS for stability and long-term support.                        |
| **Runtime** | Bitwig Studio JVM       | As provided by Bitwig    | Execution environment within Bitwig Studio             | Requirement for Bitwig extensions.                                                       |
| **Build Tool** | Gradle                  | Latest stable (e.g., 8.x) | Dependency management and build automation             | Common for Java projects; used by reference projects like "DrivenByMoss".                |
| **Bitwig API** | Bitwig Extension API    | 19                       | Interface for interacting with Bitwig Studio           | Specified in PRD; core requirement for functionality.                                    |
| **MCP Implementation** | MCP Java SDK            | 0.8.0+ (Latest stable)  | For MCP server logic and message handling              | Official SDK for MCP; provides spec-compliant components including JSON-RPC 2.0 processing, tool registry, and server transport. |
| **HTTP Server (MCP)**| MCP Java SDK's built-in Streamable HTTP/SSE transport | Chosen by MCP Java SDK | Hosts the MCP server endpoint for Streamable HTTP (SSE) | The MCP Java SDK provides transport implementations with Server-Sent Events support for streaming responses. |
| **MCP Transport** | Streamable HTTP (SSE)   | MCP Specification        | Protocol for communication between AI agent and WigAI    | Modern MCP standard; supports real-time, bi-directional communication needs for control. |
| **Testing** | JUnit                   | 5.x (Jupiter)            | Unit testing Java components                           | Standard Java testing framework.                                                         |
| **IDE** | IntelliJ IDEA CE / VS Code | Latest                   | Development Environment                                | Standard Java IDEs with good Gradle support.                                             |
| **Operating Systems**| macOS, Windows, Linux   | Various                  | Target platforms for the extension                     | As per Bitwig Studio compatibility.                                                      |
| **Output Format** | .bwextension            | N/A                      | Bitwig Extension package format                        | Standard deployment format for Bitwig.                                                   |

## Notes

- The specific embedded HTTP server component (e.g., Jetty, Tomcat, Undertow if used by the MCP Java SDK's Servlet transport) will be determined by the dependencies of the MCP Java SDK's chosen server transport. The goal is to remain lightweight and avoid full application server footprints if possible.
- Version for Gradle and MCP Java SDK will be the latest stable at the time of development initiation, to be locked in the `build.gradle` file.

## Change Log

| Change        | Date       | Version | Description                  | Author         |
| ------------- | ---------- | ------- | ---------------------------- | -------------- |
| Initial draft | 2025-05-16 | 0.1     | First draft based on research and decisions | 3-architect BMAD v2 |
| Update        | 2025-05-16 | 0.2     | Added VS Code as IDE option  | 3-architect BMAD v2 |
| Update        | 2025-05-18 | 0.3     | Updated MCP Java SDK version to 0.8.0+ and clarified transport implementation | Architect Agent |