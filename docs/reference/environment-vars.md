# WigAI Environment Variables

Version: 0.1.0
Date: 2025-05-16

## 1. Introduction

This document outlines any environment variables or externalized configuration parameters used by the WigAI Bitwig Extension. For the Minimum Viable Product (MVP), WigAI aims for minimal external configuration to simplify deployment and usage.

## 2. Configuration Loading Mechanism

* **Local Development / Runtime:** Configuration values are primarily managed as internal constants within the Java code (e.g., in an `AppConstants.java` file or within the `ConfigManager` component).
* **Future Enhancements:** Post-MVP, mechanisms for user-configuration (e.g., via a settings file within the Bitwig user directory, or potentially through a UI within Bitwig if the API allows) could be considered for parameters like the MCP server port.

## 3. Environment Variables / Configuration Parameters

For the MVP, WigAI does not rely on traditional system environment variables for its configuration. The key configurable aspect is the network port for the MCP server.

| Parameter Name (Conceptual) | Internal Constant (Example) | Description                                      | Default Value | Required for MVP? | Settable by User (MVP)? |
| :-------------------------- | :-------------------------- | :----------------------------------------------- | :------------ | :---------------- | :---------------------- |
| MCP Server Port             | `DEFAULT_MCP_PORT`          | The local network port the MCP server listens on. | `61169`       | Yes (as default)  | No (Hardcoded default)  |
| Log Level                   | `LOG_LEVEL` (conceptual)    | Controls the verbosity of logging.               | `INFO`        | Yes (as default)  | No (Hardcoded default)  |

**Details:**

* **MCP Server Port (`DEFAULT_MCP_PORT`):**
    * **Description:** This is the TCP port number that the embedded MCP server will use to listen for incoming connections from external AI agents.
    * **Default Value:** `61169` (as suggested in initial project requirements, mnemonic "WIGAI").
    * **MVP Implementation:** This will be a hardcoded constant within the `ConfigManager` or an `AppConstants` class. It will be clearly documented for users.
    * **Future Consideration:** This is the most likely candidate for user-configurability post-MVP.
* **Log Level (`LOG_LEVEL`):**
    * **Description:** Conceptually, this would control the detail of messages output to the Bitwig extension console via `host.println()`.
    * **Default Value:** For MVP, logging will likely include INFO and ERROR level messages. A more granular, configurable system (`DEBUG`, `WARN`, etc.) might be an enhancement if needed.
    * **MVP Implementation:** Logging behavior will be directly implemented; a formal "log level" constant might not be explicitly used to switch verbosity in MVP but is a common pattern if more control is needed.

## 4. `.env` Files

Not applicable for this project, as it's a Bitwig Java Extension and does not typically load configuration from `.env` files in the same way a standalone backend service might.

## 5. Notes

* **Simplicity for MVP:** The goal for MVP is to have the extension work out-of-the-box with sensible defaults.
* **User Documentation:** Any fixed default values (like the port) will be clearly stated in the `README.md` and user-facing documentation so users know how to connect their AI agents.
* The `ConfigManager` component in the architecture is designed with the idea that more sophisticated configuration methods could be added in the future without major refactoring of components that consume configuration values.

## Change Log

| Change        | Date       | Version | Description                                      | Author              |
| ------------- | ---------- | ------- | ------------------------------------------------ | ------------------- |
| Initial draft | 2025-05-16 | 0.1.0   | First draft of environment variables document. | 3-architect BMAD v2 |