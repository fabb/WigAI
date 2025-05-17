# WigAI Data Models

Version: 0.1.0
Date: 2025-05-16

## 1. Introduction

This document describes the primary data models and structures used within the WigAI Bitwig Extension, particularly those related to the MCP API request and response payloads. For MVP, WigAI does not have its own persistent database or state files; all state is either transient or directly reflects the state within Bitwig Studio.

The data structures defined here will primarily be implemented as Java Records for immutability and conciseness, serving as Data Transfer Objects (DTOs) for handling MCP command payloads and constructing responses.

## 2. Core Application Entities / Domain Objects

For the MVP, there are no complex internal domain objects that are distinctly separate from the API payload structures. The "domain" largely consists of interacting with Bitwig entities (tracks, devices, parameters, clips, scenes) via the Bitwig API and translating these interactions to/from MCP messages.

## 3. MCP API Payload Schemas

These schemas define the structure of data sent to and received from the WigAI MCP API. They correspond to the JSON examples provided in `docs/mcp-api-spec.md`. Below are conceptual representations, which will translate to Java Records or classes.

### 3.1. Generic MCP Structures

As outlined in the `mcp-api-spec.md`, requests and responses will follow a general MCP pattern. The focus here is on the `payload` of requests and the `data` field of successful responses.

* **Generic Request:**
    * `command`: String (e.g., "ping", "transport_start")
    * `payload`: Object (command-specific, see below)
* **Generic Success Response:**
    * `status`: "success"
    * `data`: Object (command-specific, see below)
* **Generic Error Response:**
    * `status`: "error"
    * `error`: Object { `code`: String, `message`: String, `details?`: Object }

### 3.2. Command-Specific Payloads and Data Structures

#### 3.2.1. Ping
* **Request Payload:** (No payload)
* **Response Data (`PingResponseData`):**
    * `response`: String (Value: "pong")
    * `wigai_version`: String (e.g., "0.1.0")

#### 3.2.2. Transport Control
* **`transport_start` Request Payload:** (No payload)
* **`transport_start` Response Data (`TransportActionData`):**
    * `action`: String (Value: "transport_started")
    * `message`: String
* **`transport_stop` Request Payload:** (No payload)
* **`transport_stop` Response Data (`TransportActionData`):** (Same structure as above)
    * `action`: String (Value: "transport_stopped")
    * `message`: String

#### 3.2.3. Device Parameters

* **`ParameterInfo` (Used in `GetDeviceParametersResponseData`):**
    * `index`: Integer (0-7)
    * `name`: String (Nullable)
    * `value`: Double (0.0-1.0, normalized)
    * `display_value`: String (Bitwig's display string for the value)
* **`get_selected_device_parameters` Request Payload:** (No payload)
* **`get_selected_device_parameters` Response Data (`GetDeviceParametersResponseData`):**
    * `device_name`: String (Nullable, name of the selected Bitwig device)
    * `parameters`: List<`ParameterInfo`>
* **`set_selected_device_parameter` Request Payload (`SetDeviceParameterPayload`):**
    * `parameter_index`: Integer (0-7)
    * `value`: Double (0.0-1.0)
* **`set_selected_device_parameter` Response Data (`SetDeviceParameterResponseData`):**
    * `action`: String (Value: "parameter_set")
    * `parameter_index`: Integer
    * `new_value`: Double
    * `message`: String
* **`ParameterSetting` (Used in `SetMultipleDeviceParametersPayload`):**
    * `parameter_index`: Integer (0-7)
    * `value`: Double (0.0-1.0)
* **`set_multiple_selected_device_parameters` Request Payload (`SetMultipleDeviceParametersPayload`):**
    * `parameters`: List<`ParameterSetting`>
* **`ParameterSettingResult` (Used in `SetMultipleDeviceParametersResponseData`):**
    * `parameter_index`: Integer
    * `status`: String ("success" or "error")
    * `new_value?`: Double (if success)
    * `error_code?`: String (if error)
    * `message?`: String (if error)
* **`set_multiple_selected_device_parameters` Response Data (`SetMultipleDeviceParametersResponseData`):**
    * `action`: String (Value: "multiple_parameters_set")
    * `results`: List<`ParameterSettingResult`>

#### 3.2.4. Clip and Scene Launching

* **`launch_clip` Request Payload (`LaunchClipPayload`):**
    * `track_name`: String
    * `clip_index`: Integer (0-based)
* **`launch_clip` Response Data (`LaunchClipResponseData`):**
    * `action`: String (Value: "clip_launched")
    * `track_name`: String
    * `clip_index`: Integer
    * `message`: String
* **`launch_scene_by_index` Request Payload (`LaunchSceneByIndexPayload`):**
    * `scene_index`: Integer (0-based)
* **`launch_scene_by_index` Response Data (`LaunchSceneByIndexResponseData`):**
    * `action`: String (Value: "scene_launched")
    * `scene_index`: Integer
    * `message`: String
* **`launch_scene_by_name` Request Payload (`LaunchSceneByNamePayload`):**
    * `scene_name`: String
* **`launch_scene_by_name` Response Data (`LaunchSceneByNameResponseData`):**
    * `action`: String (Value: "scene_launched")
    * `scene_name`: String
    * `message`: String
    * `launched_scene_index?`: Integer (Optional, actual index if found by name)

## 4. Database Schemas

Not applicable for MVP. WigAI does not use a database.

## 5. State File Schemas

Not applicable for MVP. WigAI does not use external state files for its core operation. Extension settings (like port) are managed internally for MVP.

## Change Log

| Change        | Date       | Version | Description                  | Author              |
| ------------- | ---------- | ------- | ---------------------------- | ------------------- |
| Initial draft | 2025-05-16 | 0.1.0   | First draft of data models. | 3-architect BMAD v2 |