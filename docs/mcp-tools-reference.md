# MCP Tools Reference

This document provides detailed information about all available MCP tools in the WigAI extension.

## Core Status Tool

### `status`
Returns comprehensive operational status of WigAI and Bitwig Studio environment.

**Purpose:** Query the current state of the extension, transport, project, and selected track/device.

**Returns:** Complete system state including:
- WigAI version
- Project name
- Transport state (play, record, loop, tempo)
- Current time position
- Selected track information
- Selected device information
- Project parameters

---

## Transport Control Tools

### `transport_start`
Start playback in Bitwig Studio.

**Purpose:** Begin playing the Bitwig project from the current position.

**Returns:** Status message indicating playback started

**Error Handling:**
- Returns `TRANSPORT_ERROR` if playback cannot be started
- Logs detailed error messages for debugging

### `transport_stop`
Stop playback in Bitwig Studio.

**Purpose:** Stop playing the Bitwig project.

**Returns:** Status message indicating playback stopped

**Error Handling:**
- Returns `TRANSPORT_ERROR` if playback cannot be stopped
- Logs detailed error messages for debugging

---

## Device Parameter Tools

### `get_selected_device_parameters`
Retrieve all parameters from the currently selected device.

**Purpose:** List all available parameters of the selected device with their current values.

**Returns:**
- Device name
- Array of parameters, each containing:
  - Parameter index
  - Parameter name
  - Current value (0.0-1.0 range)
  - Display value (human-readable format)

**Error Handling:**
- Returns `NO_DEVICE_SELECTED` if no device is selected
- Returns `BITWIG_API_ERROR` if parameter retrieval fails

### `set_selected_device_parameter`
Set a single parameter on the currently selected device.

**Purpose:** Adjust one device parameter to a specific value.

**Parameters:**
- `parameter_index`: The parameter index (integer)
- `value`: New value (0.0-1.0 range)

**Returns:**
- Success/failure status
- New parameter value after setting

**Error Handling:**
- Returns `INVALID_PARAMETER_INDEX` if parameter doesn't exist
- Returns `NO_DEVICE_SELECTED` if no device is selected
- Returns `BITWIG_API_ERROR` if setting fails

### `set_multiple_device_parameters`
Set multiple parameters on the currently selected device in a single operation.

**Purpose:** Batch update multiple device parameters for efficiency.

**Parameters:**
- `parameters`: Array of {parameter_index, value} objects

**Returns:**
- Array of results for each parameter set
- Overall success/failure status

**Error Handling:**
- Returns `INVALID_PARAMETER_INDEX` for any invalid parameters
- Performs validation before applying any changes
- Returns detailed results for each parameter

### `get_device_details`
Get comprehensive information about the currently selected device.

**Purpose:** Retrieve device name, manufacturer, and complete parameter list with details.

**Returns:**
- Device name
- Device manufacturer
- Complete parameter list with:
  - Name
  - Current value
  - Min/max range
  - Modulation capability
  - Display value

**Error Handling:**
- Returns `NO_DEVICE_SELECTED` if no device is selected
- Returns `BITWIG_API_ERROR` if device info cannot be retrieved

---

## Clip & Scene Tools

### `launch_clip`
Launch a specific clip in the Bitwig project.

**Purpose:** Trigger playback of a specific clip by indices.

**Parameters:**
- `scene_index`: The scene (row) index
- `track_index`: The track (column) index

**Returns:** Status message indicating clip launched

**Error Handling:**
- Returns `INVALID_CLIP_INDEX` if indices are out of bounds
- Returns `NO_CLIP_AT_INDEX` if no clip exists at the position
- Returns `BITWIG_API_ERROR` if launch fails

### `launch_scene_by_index`
Launch a specific scene (row) in the Bitwig project.

**Purpose:** Trigger playback of all clips in a specific scene.

**Parameters:**
- `scene_index`: The scene (row) index

**Returns:** Status message indicating scene launched

**Error Handling:**
- Returns `INVALID_SCENE_INDEX` if index is out of bounds
- Returns `BITWIG_API_ERROR` if scene launch fails

### `launch_scene_by_name`
Launch a scene by its name.

**Purpose:** Trigger a specific scene using its human-readable name instead of index.

**Parameters:**
- `scene_name`: The scene name (case-insensitive search)

**Returns:** Status message and matched scene index

**Error Handling:**
- Returns `SCENE_NOT_FOUND` if no scene matches the name
- Returns `MULTIPLE_MATCHES` if multiple scenes have the same name
- Returns `BITWIG_API_ERROR` if scene launch fails

---

## Project Inquiry Tools

### `list_tracks`
Get a list of all tracks in the project.

**Purpose:** Retrieve all tracks with their properties and state.

**Returns:**
- Array of tracks, each containing:
  - Track index
  - Track name
  - Track type (audio/midi/group/hybrid)
  - Mute state
  - Solo state
  - Arm state (record ready)
  - Is group track flag

**Error Handling:**
- Returns `BITWIG_API_ERROR` if track listing fails

### `get_track_details`
Get comprehensive information about a specific track.

**Purpose:** Retrieve detailed information about a track's properties, routing, and parameters.

**Parameters:**
- `track_index`: The track index

**Returns:**
- Track index and name
- Track type
- Volume level
- Panorama (pan) position
- Mute/Solo/Arm state
- Is group track flag
- List of devices on the track

**Error Handling:**
- Returns `INVALID_TRACK_INDEX` if track doesn't exist
- Returns `BITWIG_API_ERROR` if details cannot be retrieved

### `list_devices_on_track`
Get all devices on a specific track.

**Purpose:** List all devices (plugins) loaded on a track.

**Parameters:**
- `track_index`: The track index

**Returns:**
- Array of devices, each containing:
  - Device index
  - Device name
  - Device type/category
  - Enabled state

**Error Handling:**
- Returns `INVALID_TRACK_INDEX` if track doesn't exist
- Returns `BITWIG_API_ERROR` if device listing fails

### `list_scenes`
Get a list of all scenes (rows) in the project.

**Purpose:** Retrieve all available scenes with their names and count.

**Returns:**
- Total scene count
- Array of scenes, each containing:
  - Scene index
  - Scene name

**Error Handling:**
- Returns `BITWIG_API_ERROR` if scene listing fails

### `get_clips_in_scene`
Get all clips in a specific scene.

**Purpose:** Retrieve all clips in a given scene across all tracks.

**Parameters:**
- `scene_index`: The scene index

**Returns:**
- Array of clips, each containing:
  - Track index
  - Clip name
  - Clip exists flag
  - Clip type (if exists)

**Error Handling:**
- Returns `INVALID_SCENE_INDEX` if scene doesn't exist
- Returns `BITWIG_API_ERROR` if clips cannot be retrieved

---

## Error Handling

All tools implement consistent error handling through the `ErrorCode` enum:

| Error Code | Meaning | HTTP Status |
|-----------|---------|------------|
| `TRANSPORT_ERROR` | Transport control operation failed | 500 |
| `NO_DEVICE_SELECTED` | No device is currently selected | 400 |
| `INVALID_PARAMETER_INDEX` | Parameter index doesn't exist | 400 |
| `NO_CLIP_AT_INDEX` | No clip exists at the given position | 404 |
| `INVALID_CLIP_INDEX` | Clip indices are out of bounds | 400 |
| `INVALID_SCENE_INDEX` | Scene index is out of bounds | 400 |
| `INVALID_TRACK_INDEX` | Track index is out of bounds | 400 |
| `SCENE_NOT_FOUND` | Scene name not found | 404 |
| `MULTIPLE_MATCHES` | Multiple scenes match the search | 409 |
| `BITWIG_API_ERROR` | Generic Bitwig API error | 500 |

---

## Tool Implementation Architecture

All tools follow a consistent pattern:

1. **Specification Definition** - Static method defining tool schema
2. **Parameter Validation** - Checked before API calls
3. **Bitwig API Interaction** - Via `BitwigApiFacade`
4. **Error Handling** - Structured exception handling with detailed logging
5. **Response Formatting** - Consistent JSON response structure
