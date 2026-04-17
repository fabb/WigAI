# Infrastructure and Deployment Overview

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
