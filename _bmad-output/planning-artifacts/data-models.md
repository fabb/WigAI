# WigAI Data Models

Version: 0.2.0
Date: 2025-05-17

## 1. Introduction

This document describes the primary data models and structures used within the WigAI Bitwig Extension, particularly those related to the MCP API request and response payloads. For MVP, WigAI does not have its own persistent database or state files; all state is either transient or directly reflects the state within Bitwig Studio.

The data structures defined here will primarily be implemented as Java Records for immutability and conciseness, serving as Data Transfer Objects (DTOs) for handling MCP tool arguments and constructing JSON-RPC 2.0 responses.

## 2. Core Application Entities / Domain Objects

For the MVP, there are no complex internal domain objects that are distinctly separate from the API payload structures. The "domain" largely consists of interacting with Bitwig entities (tracks, devices, parameters, clips, scenes) via the Bitwig API and translating these interactions to/from MCP messages.

## 3. MCP API Data Structures

These structures define the data formats used by the WigAI MCP API implementation, following the JSON-RPC 2.0 format as specified in the official MCP standard. They correspond to the JSON examples provided in `docs/api-reference.md`. Below are conceptual representations, which will translate to Java Records or classes.

### 3.1. MCP Protocol Structures

The MCP Java SDK handles the protocol-level structures for JSON-RPC 2.0. WigAI implements the tools and their input/output schemas on top of this foundation.

* **Standard JSON-RPC 2.0 Request Structure:**
    * `jsonrpc`: String (Value: "2.0")
    * `id`: Number or String (unique request identifier)
    * `method`: String (e.g., "ping", "tools/list", "tools/call")
    * `params`: Object (method-specific parameters, not used for "ping")
* **Standard Tool Call Request Structure:**
    * `name`: String (tool name, e.g., "ping", "transport_start")
    * `arguments`: Object (tool-specific arguments)
* **Standard Tool Result Structure:**
    * `content`: Array of content items with different types
    * `isError`: Boolean (indicates whether the tool execution resulted in an error)
* **Standard Error Response Structure:**
    * `code`: Number (standard JSON-RPC error code)
    * `message`: String (error description)
    * `data`: Object (optional, additional error information)

### 3.2. Tool-Specific Data Structures

#### 3.2.1. Status Tool
* **Tool Name:** `status`
* **Input Schema:** Empty object (no arguments required)
* **Output Schema (`StatusToolResult`):**
    * `type`: String (Value: "text")
    * `text`: String (Value: "WigAI v0.2.0 is operational")

#### 3.2.2. Transport Control Tools
* **Tool Name:** `transport_start`
* **Input Schema:** Empty object (no arguments required)
* **Output Schema (`TransportStartToolResult`):**
    * `type`: String (Value: "text")
    * `text`: String (Value: "Bitwig transport started.")

* **Tool Name:** `transport_stop`
* **Input Schema:** Empty object (no arguments required)
* **Output Schema (`TransportStopToolResult`):**
    * `type`: String (Value: "text")
    * `text`: String (Value: "Bitwig transport stopped.")

#### 3.2.3. Device Parameter Tools

* **Common Structure: `ParameterInfo`**
    * `index`: Integer (0-7)
    * `name`: String (Nullable)
    * `value`: Double (0.0-1.0, normalized)
    * `display_value`: String (Bitwig's display string for the value)

* **Tool Name:** `get_selected_device_parameters`
* **Input Schema:** Empty object (no arguments required)
* **Output Schema - Text Response:**
    * `type`: String (Value: "text")
    * `text`: String (formatted device parameters information)
* **Output Schema - Resource Response:**
    * `type`: String (Value: "resource")
    * `resource`: Object
        * `uri`: String (Value: "resource://device-parameters")
        * `mimeType`: String (Value: "application/json")
        * `data`: Object
            * `device_name`: String (Nullable, name of the selected device)
            * `parameters`: Array of `ParameterInfo` objects

* **Tool Name:** `set_selected_device_parameter`
* **Input Schema (`SetDeviceParameterArguments`):**
    * `parameter_index`: Integer (0-7)
    * `value`: Double (0.0-1.0)
* **Output Schema:**
    * `type`: String (Value: "text")
    * `text`: String (e.g., "Parameter 0 set to 0.65.")

* **Tool Name:** `set_multiple_device_parameters`
* **Input Schema (`SetMultipleDeviceParametersArguments`):**
    * `parameters`: Array of Objects
        * `parameter_index`: Integer (0-7)
        * `value`: Double (0.0-1.0)
* **Output Schema:**
    * `type`: String (Value: "text")
    * `text`: String (summary of parameter setting results)

#### 3.2.4. Clip and Scene Launching Tools

* **Tool Name:** `launch_clip`
* **Input Schema (`LaunchClipArguments`):**
    * `track_name`: String (name of the track containing the clip)
    * `clip_index`: Integer (0-based index of the clip slot)
* **Output Schema:**
    * `type`: String (Value: "text")
    * `text`: String (e.g., "Clip at Drums[0] launched.")

* **Tool Name:** `launch_scene_by_index`
* **Input Schema (`LaunchSceneByIndexArguments`):**
    * `scene_index`: Integer (0-based index of the scene)
* **Output Schema:**
    * `type`: String (Value: "text")
    * `text`: String (e.g., "Scene 1 launched.")

* **Tool Name:** `launch_scene_by_name`
* **Input Schema (`LaunchSceneByNameArguments`):**
    * `scene_name`: String (name of the scene to launch)
* **Output Schema:**
    * `type`: String (Value: "text")
    * `text`: String (e.g., "Scene 'Verse 1' launched.")

## 4. Database Schemas

Not applicable for MVP. WigAI does not use a database.

## 5. State File Schemas

Not applicable for MVP. WigAI does not use external state files for its core operation. Extension settings (like port) are managed internally for MVP.

## Change Log

| Change                      | Date       | Version | Description                                             | Author              |
| --------------------------- | ---------- | ------- | ------------------------------------------------------- | ------------------- |
| Initial draft               | 2025-05-16 | 0.1.0   | First draft of data models.                            | 3-architect BMAD v2 |
| Updated to MCP tools format | 2025-05-17 | 0.2.0   | Converted to official MCP tools and JSON-RPC 2.0 format | GitHub Copilot      |
| Updated tool names          | 2025-05-18 | 0.2.1   | Replaced custom PingTool with StatusTool and added support for standard MCP ping method | Technical Scrum Master Agent |