# WigAI MCP API Specification

Version: 0.1.0
Date: 2025-05-16

## 1. Introduction

This document specifies the Model Context Protocol (MCP) Application Programming Interface (API) for WigAI, a Bitwig Studio extension. WigAI enables external AI agents to interact with Bitwig Studio for various control and query tasks.

This API adheres to the MCP principles, utilizing the Streamable HTTP transport. All requests and responses are JSON-formatted.

## 2. General Principles

* **Transport:** MCP Streamable HTTP. A single HTTP endpoint will be used for all communication. The server may use Server-Sent Events (SSE) for streaming responses or notifications over this connection.
* **Request Method:** Clients SHOULD use HTTP POST for sending MCP command messages, with the MCP message as the JSON body. Some simple status or discovery endpoints MIGHT use HTTP GET if appropriate and defined by MCP.
* **Content Type:** All JSON payloads MUST use `application/json`.
* **Endpoint:** A single configurable endpoint (e.g., `http://localhost:61169/mcp`). The port `61169` ("WIGAI") is the suggested default, as per initial project notes.
* **MCP Message Structure (General):**
    While MCP can be flexible, WigAI will adopt a common request/response pattern. The MCP Java SDK might enforce or suggest specific JSON-RPC-like structures. For WigAI's custom commands, we'll define a clear `command` field.

    **Generic Request Structure Example:**
    ```json
    {
      "mcp_version": "1.0", // Or as per MCP Java SDK requirements
      "message_id": "unique-message-id-123", // Optional, for tracking
      "command": "command_name",
      "payload": {
        // Command-specific parameters
      }
    }
    ```

    **Generic Success Response Structure Example:**
    ```json
    {
      "mcp_version": "1.0",
      "in_reply_to": "unique-message-id-123", // Optional, correlates to request
      "status": "success",
      "data": {
        // Command-specific response data
      }
    }
    ```

    **Generic Error Response Structure Example:**
    ```json
    {
      "mcp_version": "1.0",
      "in_reply_to": "unique-message-id-123", // Optional
      "status": "error",
      "error": {
        "code": "ERROR_CODE_STRING", // e.g., "INVALID_PARAMETER", "BITWIG_ERROR"
        "message": "A human-readable error message.",
        "details": { /* Optional additional details */ }
      }
    }
    ```
    *(Note: The exact top-level MCP wrapper structure might be dictated by the MCP Java SDK. The key for WigAI is the `command` and its `payload` / `data` content).*

## 3. Common Error Codes

| Error Code String           | Description                                   | HTTP Status (Suggest) |
| :-------------------------- | :-------------------------------------------- | :-------------------- |
| `INVALID_REQUEST`           | The request was malformed or unparseable.   | 400                   |
| `UNKNOWN_COMMAND`           | The specified command is not recognized.    | 404                   |
| `INVALID_PARAMETER`         | A parameter in the command payload is invalid. | 400                   |
| `MISSING_PARAMETER`         | A required parameter is missing.              | 400                   |
| `BITWIG_ERROR`              | An error occurred during interaction with Bitwig. | 500                   |
| `RESOURCE_NOT_FOUND`        | A specified Bitwig resource was not found.    | 404                   |
| `OPERATION_FAILED`          | The requested operation could not be completed. | 500                   |
| `DEVICE_NOT_SELECTED`       | Operation requires a device to be selected.   | 400                   |
| `INVALID_PARAMETER_INDEX`   | Parameter index is out of bounds.             | 400                   |
| `TRACK_NOT_FOUND`           | Specified track name not found.               | 404                   |
| `CLIP_INDEX_OUT_OF_BOUNDS`  | Clip index out of bounds for the track.       | 400                   |
| `SCENE_NOT_FOUND`           | Specified scene name or index not found.      | 404                   |

## 4. Commands

### 4.1. System Commands

#### 4.1.1. Ping

* **Description:** Verifies connectivity and operational status of WigAI.
* **PRD Objective:** N/A (Implicit for server health)
* **Epic Story:** 1.3
* **Request:**
    ```json
    {
      "command": "ping"
    }
    ```
* **Success Response (`200 OK`):**
    ```json
    {
      "status": "success",
      "data": {
        "response": "pong",
        "wigai_version": "0.1.0" // Current WigAI version
      }
    }
    ```

### 4.2. Transport Control Commands

#### 4.2.1. Start Transport

* **Description:** Starts Bitwig's main transport playback.
* **PRD Objective:** 2
* **Epic Story:** 1.4
* **Request:**
    ```json
    {
      "command": "transport_start"
    }
    ```
* **Success Response (`200 OK`):**
    ```json
    {
      "status": "success",
      "data": {
        "action": "transport_started",
        "message": "Bitwig transport started."
      }
    }
    ```
* **Error Responses:** `BITWIG_ERROR`

#### 4.2.2. Stop Transport

* **Description:** Stops Bitwig's main transport playback.
* **PRD Objective:** 2
* **Epic Story:** 1.5
* **Request:**
    ```json
    {
      "command": "transport_stop"
    }
    ```
* **Success Response (`200 OK`):**
    ```json
    {
      "status": "success",
      "data": {
        "action": "transport_stopped",
        "message": "Bitwig transport stopped."
      }
    }
    ```
* **Error Responses:** `BITWIG_ERROR`

### 4.3. Device Parameter Commands (Selected Device)

#### 4.3.1. Get Selected Device Parameters

* **Description:** Reads the names and values of the eight addressable parameters of the currently selected device in Bitwig.
* **PRD Objective:** 4
* **Epic Story:** 2.1
* **Request:**
    ```json
    {
      "command": "get_selected_device_parameters"
    }
    ```
* **Success Response (`200 OK`):**
    ```json
    {
      "status": "success",
      "data": {
        "device_name": "Poly Synth", // Name of the selected device
        "parameters": [
          { "index": 0, "name": "OSC1 Shape", "value": 0.75, "display_value": "75.0 %" },
          { "index": 1, "name": "Filter Cutoff", "value": 0.5, "display_value": "500 Hz" },
          // ... up to 8 parameters
          // If a parameter slot is empty or not available, 'name' might be null or a placeholder.
          // 'value' is normalized (0.0-1.0). 'display_value' is the string Bitwig shows.
        ]
      }
    }
    ```
    If no device is selected, or it has no parameters:
    ```json
    {
      "status": "success", // Or potentially an error status like DEVICE_NOT_SELECTED
      "data": {
        "device_name": null,
        "parameters": []
      }
    }
    ```
* **Error Responses:** `BITWIG_ERROR`, `DEVICE_NOT_SELECTED`

#### 4.3.2. Set Selected Device Parameter

* **Description:** Sets a single parameter of the currently selected device by its index.
* **PRD Objective:** 4
* **Epic Story:** 2.2
* **Request:**
    ```json
    {
      "command": "set_selected_device_parameter",
      "payload": {
        "parameter_index": 0, // Integer, 0-7
        "value": 0.65         // Float, 0.0-1.0 (normalized)
      }
    }
    ```
* **Success Response (`200 OK`):**
    ```json
    {
      "status": "success",
      "data": {
        "action": "parameter_set",
        "parameter_index": 0,
        "new_value": 0.65, // Confirms the value set
        "message": "Parameter 0 set to 0.65."
      }
    }
    ```
* **Error Responses:** `BITWIG_ERROR`, `DEVICE_NOT_SELECTED`, `INVALID_PARAMETER_INDEX`, `INVALID_PARAMETER` (for value out of range).

#### 4.3.3. Set Multiple Selected Device Parameters

* **Description:** Sets multiple parameters of the currently selected device simultaneously.
* **PRD Objective:** 4
* **Epic Story:** 2.3
* **Request:**
    ```json
    {
      "command": "set_multiple_selected_device_parameters",
      "payload": {
        "parameters": [
          { "parameter_index": 0, "value": 0.25 },
          { "parameter_index": 1, "value": 0.80 }
          // ... other parameters
        ]
      }
    }
    ```
* **Success Response (`200 OK`):**
    Contains a result for each attempted parameter change.
    ```json
    {
      "status": "success", // Overall status; individual changes reported in results
      "data": {
        "action": "multiple_parameters_set",
        "results": [
          { "parameter_index": 0, "status": "success", "new_value": 0.25 },
          { "parameter_index": 1, "status": "success", "new_value": 0.80 },
          { "parameter_index": 8, "status": "error", "error_code": "INVALID_PARAMETER_INDEX", "message": "Index 8 out of bounds." } // Example of partial failure
        ]
      }
    }
    ```
* **Error Responses:** `BITWIG_ERROR`, `DEVICE_NOT_SELECTED`, `INVALID_PARAMETER` (if overall payload structure is bad). Individual parameter errors reported in `results` array.

### 4.4. Clip and Scene Launching Commands

#### 4.4.1. Launch Clip

* **Description:** Launches a specific clip by track name and clip slot index (scene number).
* **PRD Objective:** 3
* **Epic Story:** 3.1
* **Request:**
    ```json
    {
      "command": "launch_clip",
      "payload": {
        "track_name": "Drums",   // String, name of the track
        "clip_index": 0         // Integer, 0-based index of the clip slot (scene)
      }
    }
    ```
* **Success Response (`200 OK`):**
    ```json
    {
      "status": "success",
      "data": {
        "action": "clip_launched",
        "track_name": "Drums",
        "clip_index": 0,
        "message": "Clip at Drums[0] launched."
      }
    }
    ```
* **Error Responses:** `BITWIG_ERROR`, `TRACK_NOT_FOUND`, `CLIP_INDEX_OUT_OF_BOUNDS`.

#### 4.4.2. Launch Scene by Index

* **Description:** Launches an entire scene by its numerical index.
* **PRD Objective:** 3
* **Epic Story:** 3.2
* **Request:**
    ```json
    {
      "command": "launch_scene_by_index",
      "payload": {
        "scene_index": 1 // Integer, 0-based index of the scene
      }
    }
    ```
* **Success Response (`200 OK`):**
    ```json
    {
      "status": "success",
      "data": {
        "action": "scene_launched",
        "scene_index": 1,
        "message": "Scene 1 launched."
      }
    }
    ```
* **Error Responses:** `BITWIG_ERROR`, `SCENE_NOT_FOUND` (if index out of bounds).

#### 4.4.3. Launch Scene by Name

* **Description:** Launches an entire scene by its name.
* **PRD Objective:** 3
* **Epic Story:** 3.3
* **Request:**
    ```json
    {
      "command": "launch_scene_by_name",
      "payload": {
        "scene_name": "Verse 1" // String, name of the scene (case sensitivity TBD, default to case-sensitive)
      }
    }
    ```
* **Success Response (`200 OK`):**
    ```json
    {
      "status": "success",
      "data": {
        "action": "scene_launched",
        "scene_name": "Verse 1",
        "message": "Scene 'Verse 1' launched."
        // Optionally, include launched_scene_index if found
      }
    }
    ```
* **Error Responses:** `BITWIG_ERROR`, `SCENE_NOT_FOUND`.

## 5. Future Considerations / Expansion

* More granular error reporting.
* Notifications from WigAI to the client (e.g., when a device is selected in Bitwig, track changes, etc.). This is a natural fit for the SSE aspect of Streamable HTTP.
* Discovery mechanisms (e.g., list available tracks, scenes, devices).

## Change Log

| Change        | Date       | Version | Description                       | Author              |
| ------------- | ---------- | ------- | --------------------------------- | ------------------- |
| Initial draft | 2025-05-16 | 0.1.0   | First draft of MCP API commands. | 3-architect BMAD v2 |