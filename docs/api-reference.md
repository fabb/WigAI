# API Reference

## MCP API Specification

This document outlines the Model Context Protocol (MCP) API used for communication between the WigAI agent and the Bitwig Studio environment.

### Protocol Overview

Communication is message-based, typically using JSON-RPC or a similar structured format over a suitable transport layer (e.g., WebSockets, stdin/stdout). Each message will have a command/method name and associated parameters. Responses will indicate success or failure and may return data.

### Core Commands

#### `status`
*   **Description**: Get WigAI operational status, version information, current project name, audio engine status, and detailed transport information.
*   **Parameters**: None
*   **Returns**:
    ```json
    {
      "wigai_version": "x.y.z",
      "project_name": "Name of the project",
      "audio_engine_active": true,
      "transport": {
        "playing": false,
        "recording": false,
        "loop_active": false,
        "metronome_active": true,
        "current_tempo": 120.0,
        "time_signature": "4/4",
        "current_beat_str": "1.1.1:0",
        "current_time_str": "0:00.000"
      }
    }
    ```

*   **Notes**:
    - `current_beat_str`: Bitwig-style beat position format (measures.beats.sixteenths:ticks), e.g., "1.1.1:0"
    - `current_time_str`: Time format with milliseconds (MM:SS.mmm or HH:MM:SS.mmm), e.g., "0:12.345" or "1:23:45.678"

#### `transport_start`
*   **Description**: Start Bitwig's transport playback.
*   **Parameters**: None
*   **Returns**:
    ```json
    {
      "status": "success",
      "data": {
        "action": "transport_started",
        "message": "Bitwig transport started."
      }
    }
    ```

#### `transport_stop`
*   **Description**: Stop Bitwig's transport playback.
*   **Parameters**: None
*   **Returns**:
    ```json
    {
      "status": "success",
      "data": {
        "action": "transport_stopped",
        "message": "Bitwig transport stopped."
      }
    }
    ```
*   **Notes**:
    - The command is idempotent: calling it when playback is already stopped is handled gracefully.
    - All actions and responses are logged via the Logger service.

#### `project_getName`
*   **Description**: Get the name of the current Bitwig Studio project.
*   **Parameters**: None
*   **Returns**:
    ```json
    {
      "status": "success",
      "data": {
        "projectName": "Name of the project"
      }
    }
    ```

#### `get_selected_device_parameters`
*   **Description**: Get the names and values of the eight addressable parameters of the user-selected device in Bitwig.
*   **Parameters**: None
*   **Returns**:
    ```json
    {
      "status": "success",
      "data": {
        "device_name": "Name of the device or null",
        "parameters": [
          {
            "index": 0,
            "name": "Parameter Name",
            "value": 0.0, // Normalized value (0.0-1.0)
            "display_value": "Formatted Value"
          }
          // ... up to 8 parameters
        ]
      }
    }
    ```
*   **Errors**: None specific, `parameters` will be empty if no device or no parameters.

#### `set_selected_device_parameter`
*   **Description**: Set a specific value for a single parameter (by its index 0-7) of the user-selected device in Bitwig.
*   **Parameters**:
    ```json
    {
      "parameter_index": 0, // Integer (0-7)
      "value": 0.5 // Float/Double (0.0-1.0)
    }
    ```
*   **Returns**:
    ```json
    {
      "status": "success",
      "data": {
        "action": "parameter_set",
        "parameter_index": 0,
        "new_value": 0.5,
        "message": "Parameter 0 set to 0.5."
      }
    }
    ```
*   **Errors**:
    *   `DEVICE_NOT_SELECTED`
    *   `INVALID_PARAMETER_INDEX`
    *   `INVALID_PARAMETER` (for value out of range)
    *   `BITWIG_ERROR`

#### `set_selected_device_parameters`
*   **Description**: Set multiple parameter values (by index 0-7) of the user-selected device in Bitwig simultaneously.
*   **Parameters**:
    ```json
    {
      "parameters": [
        { "parameter_index": 0, "value": 0.25 },
        { "parameter_index": 1, "value": 0.80 }
        // ... more parameters
      ]
    }
    ```
*   **Returns**:
    ```json
    {
      "status": "success",
      "data": {
        "action": "multiple_parameters_set",
        "results": [
          {
            "parameter_index": 0,
            "status": "success", // or "error"
            "new_value": 0.25, // if success
            "error_code": "INVALID_PARAMETER_INDEX", // if error
            "message": "Optional message for this specific parameter" // if error
          }
          // ... results for each parameter
        ]
      }
    }
    ```
*   **Errors**:
    *   Top-level: `DEVICE_NOT_SELECTED`, `INVALID_PARAMETER` (for overall payload issues)
    *   Per-item in `results`: `INVALID_PARAMETER_INDEX`, `INVALID_PARAMETER`, `BITWIG_ERROR`

### Session Control Commands

#### `launch_clip`
*   **Description**: Launch a specific clip in Bitwig by providing its track name and clip slot index.
*   **Parameters**:
    ```json
    {
      "track_name": "Drums", // Case-sensitive string
      "clip_index": 0 // Non-negative integer (0-based)
    }
    ```
*   **Returns**:
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
*   **Errors**:
    *   `INVALID_ARGUMENT`: Missing or invalid parameters (e.g., empty track_name, negative clip_index)
    *   `TRACK_NOT_FOUND`: The specified track name was not found
    *   `CLIP_INDEX_OUT_OF_BOUNDS`: The clip index is outside the valid range for the track
    *   `BITWIG_ERROR`: Internal error occurred while launching clip

#### `session_launchSceneByIndex`
*   **Description**: Launch an entire scene in Bitwig by providing its numerical index.
*   **Parameters**:
    ```json
    {
      "scene_index": 1 // Non-negative integer (0-based)
    }
    ```
*   **Returns**:
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
*   **Errors**:
    *   `SCENE_NOT_FOUND`
    *   `BITWIG_ERROR`

#### `session_launchSceneByName`
*   **Description**: Launch an entire scene in Bitwig by providing its name.
*   **Parameters**:
    ```json
    {
      "scene_name": "Verse 1" // Case-sensitive
    }
    ```
*   **Returns**:
    ```json
    {
      "status": "success",
      "data": {
        "action": "scene_launched",
        "scene_name": "Verse 1",
        // "launched_scene_index": 0, // Optional
        "message": "Scene 'Verse 1' launched."
      }
    }
    ```
*   **Errors**:
    *   `SCENE_NOT_FOUND`
    *   `BITWIG_ERROR`

*(Further commands related to track manipulation, clip launching, device control, etc., will be detailed here as they are defined and implemented.)*

### Error Handling

Errors will be communicated in the `status` field of the response, with additional details in the `message` field. Standard error codes may be introduced later.

## External APIs Consumed

WigAI does not consume any external APIs.

## MCP Tool Development Guidelines

These guidelines assist AI agents in effectively choosing and using MCP tools.

### 1. Tool Design & Scope
*   **Focus & Atomicity**: Tools should perform single, clear tasks. Avoid overly complex tools.
*   **Meaningful Workflows**: Expose tools representing goal-oriented tasks, not just raw API endpoints.
*   **Manageable Number**: Prefer a curated set. For larger sets, use:
    *   **Logical Grouping/Namespacing**: e.g., `userManagement_createUser`.
    *   **Abstraction Layers**: Offer high-level (common operations) and lower-level (fine-grained control) tools.
    *   **Phased Exposure**: Start with critical tools and iterate.

### 2. Naming Conventions
*   **Clear & Descriptive**: Short names indicating function.
*   **Consistent & Unique**: Use a standard convention (e.g., `snake_case`) and ensure uniqueness.
*   **Unambiguous**: Differentiate similar tools clearly.

### 3. Tool Descriptions
*   **Detailed & Unambiguous**: Crucial for AI selection. Explain:
    *   What the tool does.
    *   When to use it.
    *   Expected outcome.
*   **Examples**: Provide sample inputs/outputs.
*   **Markdown**: Use for readability.

### 4. Parameter Handling
*   **Clear Definitions**: For each parameter: name, description, data type, required/optional, constraints/formats.
*   **JSON Schema**: Use for structured, machine-readable definitions.
*   **"Think Like the Model"**: Descriptions should be obvious to a new developer.
*   **Input Validation**: Define validation rules clearly.

### 5. Output Handling
*   **Document Structure**: Clearly describe output structure, including variability based on inputs.
*   **Context for Interpretation**: Explain how to interpret outputs if needed.
*   **Deterministic Behavior**: State if output can be random or change with identical inputs.
*   **Error Reporting**: Describe how errors are communicated in the output.

### 6. General Best Practices
*   **Prioritize Safety**: Be cautious with data-modifying tools (PUT, DELETE). Prefer GET. Implement robust safety/consent mechanisms.
*   **User Consent & Control**: Align with MCP principles for data access and operations.
*   **Error Handling**: Implement proper error handling with clear messages (no sensitive internal details).
*   **Rich Metadata**: Comprehensive and accurate tool metadata is key for discovery and use.
*   **Test Thoroughly**: Observe AI interaction and iterate on definitions.
*   **Consistent Documentation**: Standardize format and structure.
