# API Reference

## MCP API Specification

This document outlines the Model Context Protocol (MCP) API used for communication between the WigAI agent and the Bitwig Studio environment.

### Protocol Overview

Communication is message-based, typically using JSON-RPC or a similar structured format over a suitable transport layer (e.g., WebSockets, stdin/stdout). Each message will have a command/method name and associated parameters. Responses will indicate success or failure and may return data.

### Core Commands

#### `status`
*   **Description**: Get WigAI operational status, version information, current project name, audio engine status, detailed transport information, project parameters, selected track details, and selected device information.
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
      },
      "project_parameters": [
        {
          "index": 0,
          "exists": true,
          "name": "Project Parameter Name",
          "value": 0.5,
          "display_value": "50%"
        }
      ],
      "selected_track": {
        "index": 0,
        "name": "Track Name",
        "type": "audio",
        "is_group": false,
        "muted": false,
        "soloed": false,
        "armed": true
      },
      "selected_device": {
        "track_name": "Track Name",
        "track_index": 0,
        "index": 0,
        "name": "Device Name",
        "bypassed": false,
        "parameters": [
          {
            "index": 0,
            "name": "Parameter Name",
            "value": 0.5,
            "display_value": "50%"
          }
        ]
      }
    }
    ```

*   **Notes**:
    - `current_beat_str`: Bitwig-style beat position format (measures.beats.sixteenths:ticks), e.g., "1.1.1:0"
    - `current_time_str`: Time format with milliseconds (MM:SS.mmm or HH:MM:SS.mmm), e.g., "0:12.345" or "1:23:45.678"
    - `project_parameters`: Array containing only parameters where `exists` is true (0-7 parameter indexes)
    - `selected_track`: Object containing currently selected track details, or `null` if no track is selected
    - `selected_track.type`: Track type (e.g., "audio", "instrument", "group", "hybrid", "effect", "master")
    - `selected_track.index`: 0-based index in the current track bank, or -1 if not found in visible tracks
    - `selected_device`: Object containing currently selected device details, or `null` if no device is selected
    - `selected_device.track_name`: Name of the track containing the selected device
    - `selected_device.track_index`: 0-based index of the track containing the device, or -1 if not found in visible tracks
    - `selected_device.index`: 0-based index of the device in the track's device chain
    - `selected_device.bypassed`: Boolean indicating if the device is bypassed (disabled)
    - `selected_device.parameters`: Array containing only accessible parameters with names (0-7 parameter indexes)

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
*   **Not implemented as a separate tool**: the project name is returned by `status` (`project_name`). Kept here for history.
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
    *   `BITWIG_API_ERROR`

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
    *   Per-item in `results`: `INVALID_PARAMETER_INDEX`, `INVALID_PARAMETER`, `BITWIG_API_ERROR`

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
    *   `BITWIG_API_ERROR`: Internal error occurred while launching clip

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
    *   `BITWIG_API_ERROR`

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
    *   `BITWIG_API_ERROR`

### Track Information Commands

#### `list_tracks`
*   **Description**: List all tracks in the current project with summary information including name, type, selection state, parent group, and basic device list.
*   **Parameters**:
    ```json
    {
      "type": "audio" // Optional filter by track type (e.g., "audio", "instrument", "group", "effect", "master", "hybrid")
    }
    ```
*   **Returns**:
    ```json
    {
      "status": "success",
      "data": [
        {
          "index": 0,
          "name": "Drums",
          "type": "audio",
          "is_group": false,
          "parent_group_index": null,
          "activated": true,
          "color": "rgb(255,128,0)",
          "is_selected": true,
          "devices": [
            {
              "index": 0,
              "name": "EQ Eight",
              "type": "AudioFX"
            },
            {
              "index": 1,
              "name": "Compressor",
              "type": "AudioFX"
            }
          ]
        },
        {
          "index": 1,
          "name": "Bass",
          "type": "instrument",
          "is_group": false,
          "parent_group_index": null,
          "activated": true,
          "color": "rgb(0,255,128)",
          "is_selected": false,
          "devices": [
            {
              "index": 0,
              "name": "Wavetable",
              "type": "Instrument"
            }
          ]
        }
      ]
    }
    ```
*   **Notes**:
    - `index`: 0-based position in the project's main track list
    - `type`: Track type (e.g., "audio", "instrument", "hybrid", "group", "effect", "master")
    - `is_group`: True if the track is a group track
    - `parent_group_index`: 0-based index of the parent group track if this track is inside a group, `null` otherwise
    - `activated`: Is the track activated
    - `color`: Color of the track in RGB format (e.g., "rgb(255,128,0)")
    - `is_selected`: True if this is the currently selected track
    - `devices`: Array of devices on this track with summary information
    - `devices[].index`: 0-based position in this track's device chain
    - `devices[].name`: Name of the device
    - `devices[].type`: Type of the device ("Instrument", "AudioFX", "NoteFX")
*   **Errors**:
    *   `INVALID_PARAMETER`: Invalid track type filter
    *   `BITWIG_API_ERROR`: Internal error occurred while retrieving tracks

#### `get_track_details`
*   **Description**: Retrieve detailed information for a specific track by index, name, or the currently selected track.
*   **Parameters**:
    ```json
    {
      "track_index": 3,
      "track_name": "Drums",
      "get_selected": true
    }
    ```
    Rules:
    - Exactly one of `track_index`, `track_name`, or `get_selected` may be provided. If none provided, behaves as `get_selected=true`.
    - Providing multiple results in `INVALID_PARAMETER`.
    - `track_name` match is case-sensitive.
*   **Returns**:
    ```json
    {
      "status": "success",
      "data": {
        "index": 0,
        "name": "Drums",
        "type": "audio",
        "is_group": false,
        "parent_group_index": null,
        "activated": true,
        "color": "rgb(255,128,0)",
        "is_selected": true,
        "devices": [
          { "index": 0, "name": "EQ Eight", "type": "AudioFX", "bypassed": false },
          { "index": 1, "name": "Compressor", "type": "AudioFX", "bypassed": false }
        ],
        "volume": 0.63,
        "volume_str": "-4.0 dB",
        "pan": 0.5,
        "pan_str": "C",
        "muted": false,
        "soloed": false,
        "armed": false,
        "monitor_enabled": true,
        "auto_monitor_enabled": false,
        "sends": [
          { "name": "A", "volume": 0.4, "volume_str": "-8.0 dB", "activated": true },
          { "name": "B", "volume": 0.0, "volume_str": "-inf", "activated": false }
        ],
        "clips": [
          {
            "slot_index": 0,
            "scene_name": "Intro",
            "has_content": true,
            "clip_name": "Beat 1",
            "clip_color": "rgb(255,128,0)",
            "is_playing": false,
            "is_recording": false,
            "is_playback_queued": false
          },
          {
            "slot_index": 1,
            "scene_name": "Verse 1",
            "has_content": false,
            "clip_name": null,
            "clip_color": null,
            "is_playing": null,
            "is_recording": null,
            "is_playback_queued": null
          }
        ]
      }
    }
    ```
*   **Notes**:
    - Uses the same track index semantics and device summary format as `list_tracks`.
    - Color values are returned as RGB strings (e.g., `rgb(255,128,0)`).
    - Clip slot provides clip name and state flags; clip length and loop status are not exposed via slot API and are omitted.
*   **Errors**:
    *   `INVALID_PARAMETER`: Invalid combination or types of parameters
    *   `TRACK_NOT_FOUND`: Target track not found or no track selected when required
    *   `BITWIG_API_ERROR`: Internal Bitwig API error

#### `list_devices_on_track`
*   **Description**: List devices on a specific track (by index, name, or currently selected track) with summary fields and selection state.
*   **Parameters**:
    ```json
    {
      "track_index": 3,
      "track_name": "Drums",
      "get_selected": true
    }
    ```
    Rules:
    - Exactly one of `track_index`, `track_name`, or `get_selected` may be provided. If none provided, behaves as `get_selected=true`.
    - `track_name` match is case-sensitive (exact).
    - Providing multiple results in `INVALID_PARAMETER`.
*   **Returns**:
    ```json
    {
      "status": "success",
      "data": [
        {
          "index": 0,
          "name": "EQ Eight",
          "type": "AudioFX",
          "bypassed": false,
          "is_selected": false
        },
        {
          "index": 1,
          "name": "Compressor",
          "type": "AudioFX",
          "bypassed": false,
          "is_selected": true
        }
      ]
    }
    ```
*   **Notes**:
    - `type`: One of "Instrument", "AudioFX", "NoteFX", or "Unknown".
    - Deterministic enumeration: results are ordered by 0-based device chain index; only top-level devices are included (no recursion into container devices).
    - Selection semantics:
      - If the target track is not the globally selected track, `is_selected` is `false` for all devices.
      - If the target track is the globally selected track, compare against the global CursorDevice; prefer same-track index match, otherwise name match on that track (first match if ambiguous).

*   **Examples**:

    Request by index:
    ```json
    { "track_index": 2 }
    ```

    Request by name:
    ```json
    { "track_name": "Drums" }
    ```

    Request selected track:
    ```json
    { "get_selected": true }
    ```

    Default (no parameters → behaves as get_selected=true):
    ```json
    { }
    ```

    Empty device list response:
    ```json
    {
      "status": "success",
      "data": []
    }
    ```

    Error response (standardized envelope):
    ```json
    {
      "status": "error",
      "error": {
        "code": "TRACK_NOT_FOUND",
        "message": "No track is currently selected",
        "operation": "list_devices_on_track"
      }
    }
    ```

*   **Errors**:
    *   `INVALID_PARAMETER`: Invalid combination or types of parameters
    *   `INVALID_RANGE`: Index outside of valid range
    *   `TRACK_NOT_FOUND`: Target track not found or no track selected when required
    *   `BITWIG_API_ERROR`: Internal Bitwig API error

### Scene Information Commands

#### `list_scenes`
*   **Description**: List all scenes in the current project with their name and color. Returns information about scene structure.
*   **Parameters**: None
*   **JSON Schema**:
    ```json
    {
      "type": "object",
      "properties": {},
      "additionalProperties": false
    }
    ```
*   **Returns**:
    ```json
    {
      "status": "success",
      "data": [
        {
          "index": 0,
          "name": "Intro",
          "color": "rgb(255,128,0)"
        },
        {
          "index": 1,
          "name": "Verse",
          "color": "rgb(0,180,255)"
        },
        {
          "index": 2,
          "name": "Chorus",
          "color": null
        }
      ]
    }
    ```
*   **Notes**:
    - `index`: 0-based index of the scene across the entire project (not limited to the visible window)
    - `name`: Name of the scene as displayed in Bitwig Studio
    - `color`: Scene color as an RGB string formatted exactly as `rgb(r,g,b)` where `r`, `g`, and `b` are integers in the range 0–255. Use `null` if the color is not available from the API
    - The tool queries the Bitwig API (SceneBank) to retrieve all scenes, correctly handling paged/scrolling access so that all scenes are returned, not just the currently visible window
    - The tool functions correctly in an empty project (returns `[]`)
*   **Errors**:
    *   `BITWIG_API_ERROR`: Internal error occurred while retrieving scenes or Bitwig API unavailable

#### `get_clips_in_scene`
*   **Description**: Get detailed information for all clips within a specific scene, including their track context, content properties (name, color, length, loop status), and playback states.
*   **Parameters**: One of `scene_index` or `scene_name` must be provided. If both are provided, `scene_name` takes precedence.
    *   `scene_index` (integer, optional): 0-based index of the scene. Must be >= 0.
    *   `scene_name` (string, optional): Name of the scene (case-insensitive, trimmed).
*   **JSON Schema**:
    ```json
    {
      "type": "object",
      "properties": {
        "scene_index": {
          "type": "integer",
          "description": "0-based index of the scene. Must be >= 0.",
          "minimum": 0
        },
        "scene_name": {
          "type": "string",
          "description": "Name of the scene (case-insensitive, trimmed)"
        }
      },
      "oneOf": [
        {"required": ["scene_index"]},
        {"required": ["scene_name"]},
        {"required": ["scene_index", "scene_name"]}
      ]
    }
    ```
*   **Returns**:
    ```json
    {
      "status": "success",
      "data": [
        {
          "track_index": 0,
          "track_name": "Bass",
          "has_content": true,
          "clip_name": "Bass Line",
          "clip_color": "#FF8000",
          "is_playing": false,
          "is_recording": false,
          "is_playback_queued": true,
          "is_recording_queued": false,
          "is_stop_queued": false
        },
        {
          "track_index": 1,
          "track_name": "Drums",
          "has_content": false,
          "clip_name": null,
          "clip_color": null,
          "is_playing": false,
          "is_recording": false,
          "is_playback_queued": false,
          "is_recording_queued": false,
          "is_stop_queued": false
        }
      ]
    }
    ```
*   **Notes**:
    - Returns an array of clip slot objects for all tracks at the specified scene index
    - `track_index`: 0-based index of the track this slot belongs to
    - `track_name`: Name of the track this slot belongs to
    - `has_content`: True if a clip exists in this slot
    - `clip_name`: Name of the clip if `has_content` is true; otherwise null
    - `clip_color`: Hex color in "#RRGGBB" format if `has_content` is true; otherwise null
    - `is_playing`: True if the clip in this slot is currently playing
    - `is_recording`: True if the clip in this slot is currently recording
    - `is_playback_queued`: True if playback is queued for the clip in this slot
    - `is_recording_queued`: True if recording is queued for the clip in this slot
    - `is_stop_queued`: True if a stop is queued for the clip in this slot
    - Scene name comparison is case-insensitive and trimmed
    - If multiple scenes share the same name, the first match by index is used
    - Values reflect a consistent snapshot at query time (single API tick)
*   **Validation Rules**:
    - At least one of `scene_index` or `scene_name` must be provided
    - `scene_index` must be >= 0 if provided
    - Invalid `scene_index` (negative or out of range) results in `INVALID_PARAMETER` error
*   **Errors**:
    *   `SCENE_NOT_FOUND`: Scene not found by index or name
    *   `INVALID_PARAMETER`: Invalid parameter value (e.g., negative scene_index)
    *   `MISSING_REQUIRED_PARAMETER`: Neither scene_index nor scene_name provided
    *   `BITWIG_API_ERROR`: Internal error occurred while retrieving clip information

### Device Information Commands

#### `get_device_details`
- Description: Retrieve detailed information for a specific device by track/device identifiers or for the currently selected device, including current remote control page parameters and all page names with selection state.
- Parameters:
```json
{
  "track_index": 1,
  "track_name": "Bass",
  "device_index": 0,
  "device_name": "EQ Eight",
  "get_for_selected_device": true
}
```
Rules:
- Two targeting modes:
  - Selected-device mode: if `get_for_selected_device=true` OR no identifiers provided, return the globally selected device.
  - Target-by-identifiers mode: if any of `track_index`, `track_name`, `device_index`, or `device_name` is provided, target that specific device.
- Constraints:
  - Providing `get_for_selected_device=true` together with any identifier → `INVALID_PARAMETER`.
  - Exactly one of `track_index` OR `track_name` must be provided in identifier mode; not both → `INVALID_PARAMETER`.
  - Exactly one of `device_index` OR `device_name` must be provided in identifier mode; not both → `INVALID_PARAMETER`.
  - `track_name` and `device_name` require case-sensitive exact matches.
  - If `get_for_selected_device=false` (or omitted) and no identifiers are provided → `INVALID_PARAMETER`.

- Returns:
```json
{
  "status": "success",
  "data": {
    "track_index": 0,
    "track_name": "Drums",
    "index": 1,
    "name": "Compressor",
    "type": "AudioFX", // "Instrument" | "AudioFX" | "NoteFX" | "Unknown"
    "is_bypassed": false,
    "is_selected": true,
    "remote_controls": [
      {
        "index": 0,
        "exists": true,
        "name": "Threshold",
        "value": 0.52,
        "raw_value": null,      // null if raw not available
        "display_value": "-12.0 dB"
      }
      // ... only existing controls are included
    ]
  }
}
```

- Notes:
  - `remote_controls` reflect the currently selected remote control page for the device (via `device.remoteControls()`).
  - `exists` for controls is `true` when the parameter name is non-empty; otherwise `false` (defined heuristic).
  - `value` is normalized (0.0-1.0). `raw_value` is provided only if the Controller API exposes a raw accessor; otherwise `null`.
  - Only existing controls are included in the response array.

- Errors:
  - `INVALID_PARAMETER`
  - `INVALID_RANGE`
  - `TRACK_NOT_FOUND`
  - `DEVICE_NOT_FOUND`
  - `DEVICE_NOT_SELECTED`
  - `BITWIG_API_ERROR`

### Project Editing Commands

Tools from [Epic 9](./prd/epic-9.md). Shared rules:

*   **Track targeting:** `track_index` (0-based, as in `list_tracks`) or `track_name` (exact, case-sensitive). Device-scoped tools add `device_index` (as in `list_devices_on_track`).
*   **Asynchrony:** mutating tools wait (up to 2 s) until Bitwig shows the change, then return the resulting state. A change that never appears returns `BITWIG_TIMEOUT`.
*   **Common errors:** `TRACK_NOT_FOUND`, `DEVICE_NOT_FOUND`, `MISSING_REQUIRED_PARAMETER`, `INVALID_PARAMETER` (bad argument type or value), `INVALID_RANGE`, `BITWIG_TIMEOUT`, `OPERATION_FAILED`.

#### `create_track`
*   **Description**: Create an audio, instrument or effect track.
*   **Parameters**:
    ```json
    {
      "type": "instrument",   // required: "audio" | "instrument" | "effect"
      "position": 3,          // optional: index among regular tracks (or among effect tracks for "effect"); default end
      "name": "Vocoder Chords" // optional
    }
    ```
*   **Returns**:
    ```json
    { "status": "success", "data": { "type": "instrument", "track_index": 3, "name": "Vocoder Chords" } }
    ```
*   **Notes**: The new track's type is checked before it is renamed; if it cannot be located, `OPERATION_FAILED` is returned and nothing is renamed.

#### `delete_track`
*   **Parameters**: track targeting.
*   **Returns**: `{ "deleted_track": "Audio 2", "track_index": 1 }`
*   **Errors**: `INVALID_PARAMETER` for the master track.

#### `duplicate_track`
*   **Parameters**: track targeting.
*   **Returns**: `{ "duplicated_track": "Lead", "new_track_index": 2 }`
*   **Errors**: `INVALID_PARAMETER` for the master track.

#### `get_track_mixer`
*   **Parameters**: track targeting.
*   **Returns** (the same object is returned by `set_track` and `set_send`):
    ```json
    {
      "status": "success",
      "data": {
        "track_index": 1,
        "name": "Lead Vox",
        "volume": 0.5407,           // normalized fader position
        "volume_display": "-10.0 dB",
        "pan": 0.0,                 // -1 (left) .. 1 (right)
        "pan_display": "0.00 %",
        "mute": false,
        "solo": false,
        "arm": false,
        "activated": true,
        "monitor_mode": "AUTO",
        "is_master": false,
        "sends": [ { "index": 0, "name": "FX 1", "value": 0.0, "display": "-Inf dB", "enabled": false } ]
      }
    }
    ```

#### `set_track`
*   **Description**: Change track settings; only given fields change.
*   **Parameters**:
    ```json
    {
      "track_name": "Lead Vox",
      "name": "Lead",          // optional
      "volume": 0.7,           // optional: normalized 0..1
      "volume_db": -3.0,       // optional: matched against Bitwig's dB display (max 6)
      "pan": -0.25,            // optional: -1..1
      "mute": false, "solo": false, "arm": true, "activated": true, // optional
      "monitor_mode": "AUTO",  // optional: "ON" | "OFF" | "AUTO"
      "color": "#3366CC"       // optional
    }
    ```
*   **Notes**: `volume_db` binary-searches the fader against the displayed value and keeps the closest position; the display is rounded to 0.1 dB.

#### `set_send`
*   **Parameters**: track targeting, `send_index` (required), `value` (0..1, optional), `enabled` (optional).
*   **Errors**: `INVALID_RANGE` if the send does not exist; `RESOURCE_UNAVAILABLE` if the track has no sends.

#### `list_bitwig_devices`
*   **Description**: Names of Bitwig's built-in devices accepted by `insert_device`.
*   **Returns**: `["Amp", "Audio Receiver", ..., "EQ+", ..., "Vocoder", ...]`

#### `insert_device`
*   **Parameters**:
    ```json
    {
      "track_name": "Lead",
      "bitwig_device": "EQ+",        // one of: bitwig_device (case-insensitive name),
      "clap_id": null,               //         clap_id (e.g. "org.surge-synth-team.surge-xt"),
      "vst3_id": null,               //         vst3_id (VST3 class id)
      "position": "end",             // optional: "start" | "end" (default "end")
      "after_device_index": 0        // optional: insert after this device instead
    }
    ```
*   **Returns**:
    ```json
    { "status": "success", "data": { "inserted": true, "devices": [ { "index": 0, "name": "Compressor", "enabled": true }, { "index": 1, "name": "EQ+", "enabled": true } ] } }
    ```
*   **Errors**: `DEVICE_NOT_FOUND` (unknown built-in name), `MISSING_REQUIRED_PARAMETER` (no id given).

#### `copy_device` / `move_device`
*   **Parameters**:
    ```json
    {
      "from_track_name": "Lead", "from_device_index": 1,  // or from_track_index
      "to_track_name": "Backing",                         // or to_track_index
      "position": "end",                                  // optional, or after_device_index
      "after_device_index": null
    }
    ```
*   **Returns**: `{ "target_track_devices": [ { "index": 0, "name": "EQ+", "enabled": true } ] }`
*   **Notes**: Copies keep all device settings.

#### `delete_device`
*   **Parameters**: track targeting, `device_index`.
*   **Returns**: `{ "deleted_device": "Reverb", "devices": [ ... ] }`

#### `set_device_enabled`
*   **Parameters**: track targeting, `device_index`, `enabled` (`false` = bypassed).
*   **Returns**: `{ "device": "Vocoder", "enabled": true }`

#### `set_device_window_open`
*   **Parameters**: track targeting, `device_index`, `open`.
*   **Returns**: `{ "device": "Surge XT", "window_open": true }`

#### `get_device_parameters`
*   **Description**: Read the remote-control parameters of any device without changing the user's selection.
*   **Parameters**: track targeting, `device_index`, `page` (optional; default all pages).
*   **Returns**:
    ```json
    {
      "status": "success",
      "data": {
        "track_index": 2,
        "device_index": 0,
        "device": "Reverb",
        "page_names": ["Main", "Tone", "Common"],
        "pages": [
          {
            "page": 0,
            "page_name": "Main",
            "parameters": [
              { "index": 0, "name": "Size", "value": 0.495, "display": "99.3 %" },
              { "index": 1, "name": "Pre-delay", "value": 0.04, "display": "4.00 ms" }
            ]
          }
        ]
      }
    }
    ```

#### `set_device_parameters`
*   **Description**: Set parameters on any device by name or by page+index. Values are normalized; check `display` for units.
*   **Parameters**:
    ```json
    {
      "track_index": 2,
      "device_index": 0,
      "parameters": [
        { "name": "Mix", "value": 0.35 },          // name: case-insensitive, searched across pages
        { "page": 0, "index": 1, "value": 0.9 }    // or page + index (0..7)
      ]
    }
    ```
*   **Returns**:
    ```json
    {
      "status": "success",
      "data": {
        "device": "Reverb",
        "results": [
          { "request": "Mix", "applied": true, "status": "success", "page": 0, "name": "Mix", "value": 0.35, "display": "35.0 %" },
          { "request": "page 9 index 0", "status": "error", "message": "Parameter not found" }
        ]
      }
    }
    ```
*   **Notes**: Each item succeeds or fails on its own. `applied` is `false` if Bitwig did not report the new value within the timeout.

#### `insert_file`
*   **Parameters**: track targeting, `path` (absolute, required), `slot_index` (optional).
*   **Returns**: with `slot_index`: `{ "track_index": 0, "slot_index": 1, "inserted": true }`; without: `{ "new_track_index": 1, "inserted": true }` (Bitwig inserts the file as a new track after the given one).
*   **Errors**: `INVALID_PARAMETER` if the file does not exist.
*   **Notes**: The Controller API cannot place clips on the arranger timeline. Bitwig splits a MIDI file with several tracks or channels into one new track each instead of filling the slot; the result then reports `new_tracks_created` and `inserted: false`.

#### `create_clip`
*   **Parameters**: track targeting, `slot_index`, `length_beats` (integer ≥ 1).
*   **Returns**: `{ "track_index": 0, "slot_index": 0, "length_beats": 4, "created": true }`
*   **Errors**: `INVALID_PARAMETER` if the slot already holds a clip.

#### `delete_clip`
*   **Parameters**: track targeting, `slot_index`.
*   **Returns**: `{ "track_index": 0, "slot_index": 0, "deleted": true }`

#### `write_notes`
*   **Parameters**:
    ```json
    {
      "track_index": 0,
      "slot_index": 0,
      "clear_first": true,   // optional, default false
      "notes": [
        { "start": 0, "pitch": 60, "velocity": 100, "duration": 1.0 },  // beats from clip start; velocity optional (100)
        { "start": 1.5, "pitch": 64, "duration": 0.5 }
      ]
    }
    ```
*   **Returns**: `{ "track_index": 0, "slot_index": 0, "notes_written": 2 }`
*   **Errors**: `CLIP_NOT_FOUND` if the slot is empty.
*   **Notes**: Start times snap to a 1/64-beat grid.

#### `set_transport`
*   **Parameters** (all optional): `tempo_bpm`, `position_beats`, `loop`, `metronome`, `record`, `overdub`.
*   **Returns**: `{ "tempo": "110.00 BPM", "position_beats": 0.0, "loop": false, "metronome": false, "record": false, "overdub": true }`

#### `list_actions`
*   **Parameters**: `filter` (optional, case-insensitive substring of id, name or category).
*   **Returns**: `[ { "id": "toggle_metronome", "name": "Toggle Metronome", "category": "Project" } ]`

#### `invoke_action`
*   **Parameters**: `id` (required, from `list_actions`).
*   **Returns**: `{ "invoked": "toggle_metronome", "name": "Toggle Metronome" }`
*   **Errors**: `INVALID_PARAMETER` for an unknown id.

#### `undo` / `redo`
*   **Parameters**: none.
*   **Returns**: `{ "action": "undo" }`

### Commands from Epics 10-15

Tools from Epics 10-15. The shared rules of the [Project Editing Commands](#project-editing-commands) apply. Every tool returns `{ "status": "success", "data": ... }`; the live tests in `scripts/live_test_*.py` show real responses for each tool.

#### Additions to Epic 9 tools

*   Device tools (`insert_device`, `copy_device`/`move_device`, `delete_device`, `set_device_enabled`, `get_device_parameters`, `set_device_parameters`) take `chain` and `chain_device_index` (copy/move: `from_chain`, `from_chain_device_index`, `to_device_index`, `to_chain`) to work inside a device's chains ([Story 10.1](./stories/10.1.story.md)).
*   `insert_device` also takes `vst2_id` and `preset_file`.
*   `get_device_parameters` adds `modulated_value` / `modulated_display` to controls a modulator is moving.
*   `set_track` adds `group_expanded` and `crossfade_mode`; `get_track_mixer` reports them.
*   Device lists report each device's loaded `preset`.

### Epic 10: Device Chains, Presets and Containers

#### `get_device_chains`
*   **Description**: List the chains inside a device and the devices in each: slots such as a Vocoder's 'Modulator' and 'Carrier' or an FX device's 'FX', and layers of FX/Instrument Layer devices.
*   **Parameters**:
    ```json
    {
      "track_index": 0,    // optional; integer >= 0; 0-based track index (see list_tracks)
      "track_name": "...", // optional; string; Exact track name (alternative to track_index)
      "device_index": 0    // required; integer >= 0; 0-based device index on the track (see list_devices_on_track)
    }
    ```

#### `list_presets`
*   **Description**: Search Bitwig preset files on disk (installed packages and the user library). Every word of query must match the name, collection, device or category. Load one with insert_device or replace_device (preset_file). Packaged presets do not reveal their device; browser_open with mode 'replace' lists the presets of a device.
*   **Parameters**:
    ```json
    {
      "query": "...", // optional; string
      "limit": 1,     // optional; integer >= 1
      "rescan": true  // optional; boolean; Re-read the disk (after installing packages)
    }
    ```

#### `replace_device`
*   **Description**: Replace a device (or a device inside a chain) in place with a Bitwig device, a plugin, or a preset file. Loading a preset onto an existing device is a replace with preset_file.
*   **Parameters**:
    ```json
    {
      "track_index": 0,        // optional; integer >= 0; 0-based track index (see list_tracks)
      "track_name": "...",     // optional; string; Exact track name (alternative to track_index)
      "device_index": 0,       // required; integer >= 0; 0-based device index on the track (see list_devices_on_track)
      "chain": "...",          // optional; string; A chain inside the device, e.g. 'Modulator' or 'Carrier' (Vocoder), 'FX' (FX devices), or 'layer:0' / 'layer:Name' (layer devices). See get_device_chains.
      "chain_device_index": 0, // optional; integer >= 0; Device index within that chain (required with chain)
      "bitwig_device": "...",  // optional; string
      "clap_id": "...",        // optional; string
      "vst3_id": "...",        // optional; string
      "vst2_id": 0,            // optional; integer
      "preset_file": "..."     // optional; string; Absolute path of a .bwpreset (or other device file)
    }
    ```

#### `browser_open`
*   **Description**: Open Bitwig's browser to load something at a place: mode 'replace' (a device; Bitwig shows that device's presets), 'after'/'before' (a device), 'start'/'end' (the track's chain). It shows in Bitwig's window until browser_commit or browser_cancel.
*   **Parameters**:
    ```json
    {
      "track_index": 0,      // optional; integer >= 0; 0-based track index (see list_tracks)
      "track_name": "...",   // optional; string; Exact track name (alternative to track_index)
      "device_index": 0,     // optional; integer >= 0
      "mode": "replace",     // optional; "replace" | "after" | "before" | "start" | "end"
      "content_type": "..."  // optional; string; e.g. Devices, Presets, Samples (see content_types)
    }
    ```

#### `browser_state`
*   **Description**: The open browser: content type, the selected item of each filter column, and a page of results (64 per page). list_column also returns every item of that column.
*   **Parameters**:
    ```json
    {
      "list_column": "collection", // optional; "collection" | "location" | "device" | "category" | "tag" | "device_type" | "file_type" | "creator"
      "results_page": 0            // optional; integer >= 0
    }
    ```

#### `browser_filter`
*   **Description**: Narrow the open browser: content_type, and filters as {column: item}, e.g. {"category": "Bass", "creator": "Bitwig"}. Columns: collection, location, device, category, tag, device_type, file_type, creator. An 'Any ...' item clears a column.
*   **Parameters**:
    ```json
    {
      "content_type": "...",               // optional; string
      "filters": { "category": "Reverb" }  // optional; object
    }
    ```

#### `browser_commit`
*   **Description**: Load a result of the open browser, by name or by index, and close it.
*   **Parameters**:
    ```json
    {
      "result": "...",   // optional; string
      "result_index": 0  // optional; integer >= 0
    }
    ```

#### `browser_cancel`
*   **Description**: Close the browser without loading anything.
*   **Parameters**: none.

#### `list_drum_pads`
*   **Description**: The pads of a Drum Machine by MIDI key: name, volume, pan, mute, solo and the devices on each pad. Empty pads are left out unless include_empty.
*   **Parameters**:
    ```json
    {
      "track_index": 0,      // optional; integer >= 0; 0-based track index (see list_tracks)
      "track_name": "...",   // optional; string; Exact track name (alternative to track_index)
      "device_index": 0,     // required; integer >= 0; 0-based device index on the track (see list_devices_on_track)
      "include_empty": true  // optional; boolean
    }
    ```

#### `set_drum_pad`
*   **Description**: Rename a Drum Machine pad or change its volume, pan, mute, solo or activation.
*   **Parameters**:
    ```json
    {
      "track_index": 0,    // optional; integer >= 0; 0-based track index (see list_tracks)
      "track_name": "...", // optional; string; Exact track name (alternative to track_index)
      "device_index": 0,   // required; integer >= 0; 0-based device index on the track (see list_devices_on_track)
      "pad": 0,            // required; integer 0..127; MIDI key of the pad (36 = C1, the usual kick)
      "name": "...",       // optional; string
      "volume": 0.5,       // optional; number 0..1
      "volume_db": 1.0,    // optional; number <= 6
      "pan": 0.5,          // optional; number -1..1
      "mute": true,        // optional; boolean
      "solo": true,        // optional; boolean
      "activated": true    // optional; boolean
    }
    ```

#### `insert_into_drum_pad`
*   **Description**: Add a device to a Drum Machine pad (after what is there): a Bitwig device, a plugin, a preset file, or a sample file (Bitwig creates a Sampler for it).
*   **Parameters**:
    ```json
    {
      "track_index": 0,       // optional; integer >= 0; 0-based track index (see list_tracks)
      "track_name": "...",    // optional; string; Exact track name (alternative to track_index)
      "device_index": 0,      // required; integer >= 0; 0-based device index on the track (see list_devices_on_track)
      "pad": 0,               // required; integer 0..127; MIDI key of the pad (36 = C1, the usual kick)
      "bitwig_device": "...", // optional; string
      "clap_id": "...",       // optional; string
      "vst3_id": "...",       // optional; string
      "file": "..."           // optional; string; Absolute path of a sample or preset
    }
    ```

#### `clear_drum_pads`
*   **Description**: Unmute and/or unsolo every pad of a Drum Machine.
*   **Parameters**:
    ```json
    {
      "track_index": 0,    // optional; integer >= 0; 0-based track index (see list_tracks)
      "track_name": "...", // optional; string; Exact track name (alternative to track_index)
      "device_index": 0,   // required; integer >= 0; 0-based device index on the track (see list_devices_on_track)
      "mutes": true,       // optional; boolean
      "solos": true        // optional; boolean
    }
    ```

#### `list_layers`
*   **Description**: The layers of a layer device (Instrument Layer, FX Layer, ...) with their mixer settings and devices. Devices inside a layer are edited with chain 'layer:N' on the device tools.
*   **Parameters**:
    ```json
    {
      "track_index": 0,    // optional; integer >= 0; 0-based track index (see list_tracks)
      "track_name": "...", // optional; string; Exact track name (alternative to track_index)
      "device_index": 0    // required; integer >= 0; 0-based device index on the track (see list_devices_on_track)
    }
    ```

#### `set_layer`
*   **Description**: Rename a layer or change its volume, pan, mute, solo or activation.
*   **Parameters**:
    ```json
    {
      "track_index": 0,    // optional; integer >= 0; 0-based track index (see list_tracks)
      "track_name": "...", // optional; string; Exact track name (alternative to track_index)
      "device_index": 0,   // required; integer >= 0; 0-based device index on the track (see list_devices_on_track)
      "layer": 0,          // required; integer >= 0
      "name": "...",       // optional; string
      "volume": 0.5,       // optional; number 0..1
      "volume_db": 1.0,    // optional; number <= 6
      "pan": 0.5,          // optional; number -1..1
      "mute": true,        // optional; boolean
      "solo": true,        // optional; boolean
      "activated": true    // optional; boolean
    }
    ```

### Epic 11: Device Knowledge

#### `list_installed_devices`
*   **Description**: Find devices and plugins installed on this machine (Bitwig built-ins, CLAP plugins, LV2 plugins usable through Carla). The query is matched against names, vendors, categories, descriptions and the device guide's 'use for' text, best matches first; e.g. 'vocal compressor', 'reverb', 'pitch'. Use it to pick candidates before recommending or inserting a device.
*   **Parameters**:
    ```json
    {
      "query": "...",   // optional; string
      "kind": "bitwig"  // optional; "bitwig" | "clap" | "lv2"
    }
    ```

#### `describe_device`
*   **Description**: Everything known about one device: what it is for and how to use it (device guide), its plugin description, its chains, and every control's real range, units and options. Controls are measured the first time a device is described and cached (with a scan history); pass track/device_index to measure a device already in the project (its values are restored), or just a name to measure it on a temporary track. Measuring is skipped while the transport plays.
*   **Parameters**:
    ```json
    {
      "track_index": 0,        // optional; integer >= 0; 0-based track index (see list_tracks)
      "track_name": "...",     // optional; string; Exact track name (alternative to track_index)
      "name": "...",           // optional; string; Bitwig device name, plugin name or CLAP id
      "device_index": 0,       // optional; integer >= 0
      "chain": "...",          // optional; string
      "chain_device_index": 0, // optional; integer >= 0
      "scan": true,            // optional; boolean; Measure controls if not cached (default true)
      "rescan": true           // optional; boolean; Measure again even if cached
    }
    ```

#### `catalog_status`
*   **Description**: Device catalog coverage and freshness: entries and scans per kind, the most recent scans with what changed, scans that failed, and scans made on an older Bitwig version.
*   **Parameters**: none.

#### `fetch_device_docs`
*   **Description**: Fetch Bitwig's own description of every built-in device from Bitwig's online user guide into the local catalog, so describe_device can include it (field bitwig_docs). Run once, and again after a Bitwig update; needs internet access.
*   **Parameters**: none.

### Epic 12: Listening and Auditioning

#### `set_engine_active`
*   **Description**: Activate or deactivate Bitwig's audio engine for the current project. Only one open project has the engine; without it nothing plays, meters or auditions (status shows audio_engine_active).
*   **Parameters**:
    ```json
    {
      "active": true  // required; boolean
    }
    ```

#### `get_levels`
*   **Description**: Current peak and RMS meter readings for every track and the master (0-1 of Bitwig's meter scale, not dB). Only meaningful while audio is playing.
*   **Parameters**: none.

#### `get_playing_notes`
*   **Description**: Which notes are sounding right now on each track (from clips, the user's input or play_notes).
*   **Parameters**: none.

#### `play_notes`
*   **Description**: Play notes into a track's instrument right now, without a clip, to audition a sound or chord. Times are in milliseconds from the call.
*   **Parameters**:
    ```json
    {
      "track_index": 0,                                                            // optional; integer >= 0; 0-based track index (see list_tracks)
      "track_name": "...",                                                         // optional; string; Exact track name (alternative to track_index)
      "notes": [ { "pitch": 0, "velocity": 1, "start_ms": 0, "duration_ms": 1 } ]  // required; array
    }
    ```

#### `send_midi`
*   **Description**: Send a MIDI message into a track's instrument: cc (number, value 0-127), pitch_bend (value -8192..8191, 0 = center), program (number), aftertouch (value), or raw (status 0x80-0xEF, number, value). channel 0-15, default 0. Use play_notes for notes.
*   **Parameters**:
    ```json
    {
      "track_index": 0,    // optional; integer >= 0; 0-based track index (see list_tracks)
      "track_name": "...", // optional; string; Exact track name (alternative to track_index)
      "type": "cc",        // required; "cc" | "pitch_bend" | "program" | "aftertouch" | "raw"
      "channel": 0,        // optional; integer 0..15
      "number": 0,         // optional; integer 0..127
      "value": 0,          // optional; integer
      "status": 0          // optional; integer
    }
    ```

#### `show`
*   **Description**: Bring something into view in Bitwig's window for the user: a panel layout (ARRANGE, MIX, EDIT), a track, a launcher clip in the editor (slot_index), or a device (device_index). Changes the user's selection.
*   **Parameters**:
    ```json
    {
      "track_index": 0,          // optional; integer >= 0; 0-based track index (see list_tracks)
      "track_name": "...",       // optional; string; Exact track name (alternative to track_index)
      "panel_layout": "ARRANGE", // optional; "ARRANGE" | "MIX" | "EDIT"
      "slot_index": 0,           // optional; integer >= 0
      "device_index": 0          // optional; integer >= 0
    }
    ```

### Epic 13: Arrangement, Scenes, Clips and Notes

#### `list_cue_markers`
*   **Description**: List arranger cue markers with their positions in beats.
*   **Parameters**: none.

#### `go_to_cue_marker`
*   **Description**: Jump playback to a cue marker by index or name.
*   **Parameters**:
    ```json
    {
      "index": 0,        // optional; integer >= 0
      "name": "...",     // optional; string
      "quantized": true  // optional; boolean; Wait for the launch quantization (default false)
    }
    ```

#### `set_arrangement`
*   **Description**: Set the arranger loop range (in beats) and/or the time signature, e.g. '6/8'.
*   **Parameters**:
    ```json
    {
      "loop_start_beats": 1.0,  // optional; number >= 0
      "loop_length_beats": 1.0, // optional; number
      "time_signature": "..."   // optional; string
    }
    ```

#### `stop_track`
*   **Description**: Stop a track's launcher clip, or return the track to arrangement playback.
*   **Parameters**:
    ```json
    {
      "track_index": 0,              // optional; integer >= 0; 0-based track index (see list_tracks)
      "track_name": "...",           // optional; string; Exact track name (alternative to track_index)
      "return_to_arrangement": true  // optional; boolean
    }
    ```

#### `create_scene`
*   **Description**: Add a new empty scene.
*   **Parameters**: none.

#### `rename_scene`
*   **Description**: Rename a scene.
*   **Parameters**:
    ```json
    {
      "scene_index": 0, // required; integer >= 0
      "name": "..."     // required; string
    }
    ```

#### `set_scene`
*   **Description**: Rename and/or color a scene.
*   **Parameters**:
    ```json
    {
      "scene_index": 0, // required; integer >= 0
      "name": "...",    // optional; string
      "color": "..."    // optional; string; #RRGGBB
    }
    ```

#### `duplicate_scene`
*   **Description**: Duplicate a scene and its clips; the copy goes below it.
*   **Parameters**:
    ```json
    {
      "scene_index": 0  // required; integer >= 0
    }
    ```

#### `copy_scene`
*   **Description**: Copy every clip of a scene onto another scene's row.
*   **Parameters**:
    ```json
    {
      "from_scene": 0, // required; integer >= 0
      "to_scene": 0    // required; integer >= 0
    }
    ```

#### `move_scene`
*   **Description**: Move every clip of a scene onto another scene's row.
*   **Parameters**:
    ```json
    {
      "from_scene": 0, // required; integer >= 0
      "to_scene": 0    // required; integer >= 0
    }
    ```

#### `delete_scene`
*   **Description**: Delete a scene and its clips.
*   **Parameters**:
    ```json
    {
      "scene_index": 0  // required; integer >= 0
    }
    ```

#### `copy_clip`
*   **Description**: Copy a launcher clip to an empty slot (any track).
*   **Parameters**:
    ```json
    {
      "from_track_index": 0,    // optional; integer >= 0
      "from_track_name": "...", // optional; string
      "from_slot": 0,           // required; integer >= 0
      "to_track_index": 0,      // optional; integer >= 0
      "to_track_name": "...",   // optional; string
      "to_slot": 0              // required; integer >= 0
    }
    ```

#### `move_clip`
*   **Description**: Move a launcher clip to an empty slot (any track).
*   **Parameters**:
    ```json
    {
      "from_track_index": 0,    // optional; integer >= 0
      "from_track_name": "...", // optional; string
      "from_slot": 0,           // required; integer >= 0
      "to_track_index": 0,      // optional; integer >= 0
      "to_track_name": "...",   // optional; string
      "to_slot": 0              // required; integer >= 0
    }
    ```

#### `duplicate_clip`
*   **Description**: Duplicate a launcher clip into the next free slot.
*   **Parameters**:
    ```json
    {
      "track_index": 0,    // optional; integer >= 0; 0-based track index (see list_tracks)
      "track_name": "...", // optional; string; Exact track name (alternative to track_index)
      "slot_index": 0      // required; integer >= 0
    }
    ```

#### `record_into_slot`
*   **Description**: Start recording into a launcher slot (the track must be armed).
*   **Parameters**:
    ```json
    {
      "track_index": 0,    // optional; integer >= 0; 0-based track index (see list_tracks)
      "track_name": "...", // optional; string; Exact track name (alternative to track_index)
      "slot_index": 0      // required; integer >= 0
    }
    ```

#### `set_clip`
*   **Description**: Clip settings for a launcher clip (or the arranger's selected clip with arranger: true): name, loop on/off, loop start and length, play start and stop (beats), shuffle on/off, accent (0-1, 0.5 = none), color.
*   **Parameters**:
    ```json
    {
      "track_index": 0,         // optional; integer >= 0; 0-based track index (see list_tracks)
      "track_name": "...",      // optional; string; Exact track name (alternative to track_index)
      "arranger": true,         // optional; boolean; Use the clip selected in Bitwig's arranger instead of a launcher slot
      "slot_index": 0,          // optional; integer >= 0
      "name": "...",            // optional; string
      "loop_enabled": true,     // optional; boolean
      "loop_start_beats": 1.0,  // optional; number >= 0
      "loop_length_beats": 1.0, // optional; number
      "play_start_beats": 1.0,  // optional; number >= 0
      "play_stop_beats": 1.0,   // optional; number >= 0
      "shuffle": true,          // optional; boolean
      "accent": 0.5,            // optional; number 0..1
      "color": "..."            // optional; string; #RRGGBB
    }
    ```

#### `transform_clip`
*   **Description**: Whole-clip operations on a launcher clip (or the arranger's selected clip with arranger: true): transpose (semitones), quantize (0-1 strength), duplicate its content, set launch quantization and launch mode (launcher only; Bitwig enum ids, the response shows the current ones).
*   **Parameters**:
    ```json
    {
      "track_index": 0,             // optional; integer >= 0; 0-based track index (see list_tracks)
      "track_name": "...",          // optional; string; Exact track name (alternative to track_index)
      "slot_index": 0,              // optional; integer >= 0
      "arranger": true,             // optional; boolean; Use the clip selected in Bitwig's arranger instead of a launcher slot
      "transpose": 0,               // optional; integer
      "quantize": 0.5,              // optional; number 0..1
      "duplicate_content": true,    // optional; boolean
      "launch_quantization": "...", // optional; string
      "launch_mode": "..."          // optional; string
    }
    ```

#### `read_notes`
*   **Description**: Read the MIDI notes of a launcher clip, or of the arranger's selected clip with arranger: true (beats from the clip start), with every expression and condition that is not at its default, and the clip settings.
*   **Parameters**:
    ```json
    {
      "track_index": 0,    // optional; integer >= 0; 0-based track index (see list_tracks)
      "track_name": "...", // optional; string; Exact track name (alternative to track_index)
      "slot_index": 0,     // optional; integer >= 0
      "arranger": true     // optional; boolean; Use the clip selected in Bitwig's arranger instead of a launcher slot
    }
    ```

#### `edit_notes`
*   **Description**: Change notes in a launcher clip (or the arranger's selected clip with arranger: true): all of them, or those matching a pitch and/or a start range in beats. velocity/pressure/chance/release_velocity/velocity_spread 0-1, duration in beats, pan and timbre -1..1, gain 0-1, transpose in semitones (micro-pitch, fractional allowed), muted, repeats (count, 0 = off) with repeat_curve and repeat_velocity_curve (-1..1) and repeat_velocity_end (-1..1). occurrence: play only on the first loop, not the first, when the previous note played, etc. ('off' clears it). recurrence: play on selected loop cycles, recurrence_length cycles (1-8, 0 = off) with recurrence_mask bits (bit 0 = first cycle).
*   **Parameters**:
    ```json
    {
      "track_index": 0,             // optional; integer >= 0; 0-based track index (see list_tracks)
      "track_name": "...",          // optional; string; Exact track name (alternative to track_index)
      "slot_index": 0,              // optional; integer >= 0
      "arranger": true,             // optional; boolean; Use the clip selected in Bitwig's arranger instead of a launcher slot
      "release_velocity": 0.5,      // optional; number 0..1
      "velocity_spread": 0.5,       // optional; number 0..1
      "occurrence": "off",          // optional; "off" | "always" | "first" | "not_first" | "prev" | "not_prev" | "prev_channel" | "not_prev_channel"
      "recurrence_length": 0,       // optional; integer 0..8
      "recurrence_mask": 0,         // optional; integer 0..255
      "repeat_curve": 0.5,          // optional; number -1..1
      "repeat_velocity_end": 0.5,   // optional; number -1..1
      "repeat_velocity_curve": 0.5, // optional; number -1..1
      "pitch": 0,                   // optional; integer 0..127
      "from_beats": 1.0,            // optional; number >= 0
      "to_beats": 1.0,              // optional; number >= 0
      "velocity": 0.5,              // optional; number 0..1
      "duration": 1.0,              // optional; number
      "gain": 0.5,                  // optional; number 0..1
      "pan": 0.5,                   // optional; number -1..1
      "timbre": 0.5,                // optional; number -1..1
      "pressure": 0.5,              // optional; number 0..1
      "transpose": 1.0,             // optional; number
      "chance": 0.5,                // optional; number 0..1
      "muted": true,                // optional; boolean
      "repeats": 0                  // optional; integer >= 0
    }
    ```

#### `write_arranger_notes`
*   **Description**: Write notes into the clip selected in Bitwig's arranger (the API cannot place or select arranger clips; the user selects one). Same note format as write_notes.
*   **Parameters**:
    ```json
    {
      "clear_first": true,                                                       // optional; boolean
      "notes": [ { "start": 1.0, "pitch": 0, "velocity": 1, "duration": 1.0 } ]  // required; array
    }
    ```

### Epic 14: Project-wide Control

#### `project_state`
*   **Description**: Whether any track is soloed, muted or armed, and whether the project has unsaved changes.
*   **Parameters**: none.

#### `reset_project`
*   **Description**: Clear every solo, mute and/or arm in the project. A forgotten solo silences all other tracks.
*   **Parameters**:
    ```json
    {
      "unsolo": true, // optional; boolean
      "unmute": true, // optional; boolean
      "unarm": true   // optional; boolean
    }
    ```

#### `set_groove`
*   **Description**: Set the global groove. Values are normalized 0-1; the response shows Bitwig's displayed values.
*   **Parameters**:
    ```json
    {
      "enabled": true,       // optional; boolean
      "shuffle_amount": 0.5, // optional; number 0..1
      "shuffle_rate": 0.5,   // optional; number 0..1
      "accent_amount": 0.5,  // optional; number 0..1
      "accent_rate": 0.5,    // optional; number 0..1
      "accent_phase": 0.5    // optional; number 0..1
    }
    ```

#### `set_transport_options`
*   **Description**: Transport settings beyond play/stop: punch in/out, metronome volume (0-1), pre-roll, automation write mode, launcher overdub, default launch quantization, post-recording action; or run a command (rewind, fast_forward, tap_tempo, continue, record). Enum values are Bitwig's ids; the response shows the current ones.
*   **Parameters**:
    ```json
    {
      "punch_in": true,                     // optional; boolean
      "punch_out": true,                    // optional; boolean
      "metronome_volume": 0.5,              // optional; number 0..1
      "pre_roll": "...",                    // optional; string
      "automation_write_mode": "...",       // optional; string
      "launcher_overdub": true,             // optional; boolean
      "default_launch_quantization": "...", // optional; string
      "post_recording_action": "...",       // optional; string
      "command": "rewind"                   // optional; "rewind" | "fast_forward" | "tap_tempo" | "continue" | "record"
    }
    ```

#### `set_cue`
*   **Description**: Set the cue (headphone) bus volume and its cue/master mix, both 0-1.
*   **Parameters**:
    ```json
    {
      "volume": 0.5, // optional; number 0..1
      "mix": 0.5     // optional; number 0..1
    }
    ```

### Epic 15: Following the User

#### `get_events`
*   **Description**: Changes in Bitwig since since_seq: transport (play, record, loop, tempo), project (switch, engine), track (name, mute, solo, arm, volume, pan, added/removed), clip (launcher playback state, clips created/deleted), selection (the user's selected track, device, preset) and parameter (the selected device's knobs as the user turns them). Call without since_seq to get a starting point, then pass next_seq back. wait_ms waits up to that long for the next change (long-poll). Rapid changes to the same thing are merged into the latest value.
*   **Parameters**:
    ```json
    {
      "since_seq": 0,         // optional; integer >= 0
      "wait_ms": 0,           // optional; integer 0..30000
      "types": ["transport"]  // optional; array; Only these kinds of change (default all)
    }
    ```

#### `watch`
*   **Description**: Watch Bitwig for duration_ms (up to 60000) and stream each change as it happens: as an MCP progress notification (when the call carries a progress token) and as a log message (logger 'wigai.events'). Returns every change seen, so clients that do not show notifications still get them.
*   **Parameters**:
    ```json
    {
      "duration_ms": 100,     // optional; integer 100..60000
      "types": ["transport"]  // optional; array; Only these kinds of change (default all)
    }
    ```

### Error Handling

All MCP tools use a standardized response envelope.

- Success:
```json
{
  "status": "success",
  "data": { }
}
```

- Error:
```json
{
  "status": "error",
  "error": {
    "code": "INVALID_PARAMETER",
    "message": "Human-readable error message",
    "operation": "tool_name"
  }
}
```

- error.code is one of the values defined in the ErrorCode enum.
- error.operation identifies the tool/operation that failed.

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
