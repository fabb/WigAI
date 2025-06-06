# Epic 5: Enhance MCP `status` Command

## 1. Goal

To significantly enhance the `status` MCP command by providing a more comprehensive snapshot of the current Bitwig Studio state. This will give AI agents richer context, enabling more informed decisions and sophisticated interactions with the Bitwig environment.

## 2. Background

The existing `status` command returns minimal information (WigAI version and operational status). To allow for more intelligent agent behavior, it needs to convey key aspects of the Bitwig project, transport, selected track, selected clip slot, and selected device, including their parameters. This enhancement is inspired by the detailed information available via Bitwig's OSC interface.

## 3. User Stories

This epic will be broken down into the following stories:

*   **Story 5.1: Core Project and Transport Status**
    *   As an AI agent, I want to retrieve the current project name and audio engine status, so I can confirm I'm working in the correct project and that audio is active.
    *   As an AI agent, I want to know the current transport state (playing, recording, tempo, time signature, beat/time position), so I can synchronize my actions with Bitwig's playback.
*   **Story 5.2: Selected Track and Project Parameters Status**
    *   As an AI agent, I want to access global project parameters, so I can read or potentially suggest modifications to them.
    *   As an AI agent, I want to get information about the currently selected track (index, name, type, mute/solo/arm status), so I can perform track-specific operations.
*   **Story 5.3: Selected Device Status and Parameters**
    *   As an AI agent, I want to get information about the currently selected device (track context, device index, name, bypass status, and its parameters), so I can understand and control the selected device.
*   **Story 5.4: Selected Clip Slot Status**
    *   As an AI agent, I want to get information about the currently selected clip slot (track context, slot/scene indices, names, content status, playback/recording states), so I can interact with clips in the session view.

## 4. Proposed Solution

Modify the `StatusTool.java` to gather additional information from the Bitwig Studio API and return it in an expanded JSON structure. The MCP `status` command will no longer have a top-level `status` or `message` field from the tool's direct response; these will be handled by the MCP protocol wrapper if necessary. The `wigai_version` will remain.

### Proposed JSON Response for `status` Command:

```json
{
  "wigai_version": "x.y.z",
  "project_name": "Name of the project",
  "audio_engine_active": true,
  "project_parameters": [
    {
      "index": 0, // 0-7, maps to OSC /project/param/{1-8}
      "exists": true, // From /project/param/{1-8}/exists
      "name": "Project Param 1 Name", // From /project/param/{1-8}/name
      "value": 0.5, // From /project/param/{1-8}/value (normalized 0.0-1.0 for consistency)
      "display_value": "50%" // From /project/param/{1-8}/valueStr
    }
    // ... up to 8 project parameters, only include if 'exists' is true
  ],
  "transport": {
    "playing": false,
    "recording": false,
    "loop_active": true,
    "metronome_active": false,
    "current_tempo": 120.0, // From /tempo/raw
    "time_signature": "4/4", // From /time/signature
    "current_beat_str": "1.1.1:0", // From /beat/str (Bitwig format: measures.beats.sixteenths:ticks)
    "current_time_str": "0.0.0:0" // From /time/str (e.g., H.M.S:ms or M.S.ms)
  },
  "selected_track": {
    "index": 0, // 0-based index of the track in the project
    "name": "Drums", // From /track/selected/name
    "type": "instrument", // From /track/selected/type (e.g., audio, instrument, hybrid, group, effect, master)
    "is_group": false, // From /track/selected/isGroup
    "muted": false, // From /track/selected/mute
    "soloed": false, // From /track/selected/solo
    "armed": true // From /track/selected/recarm
  },
  "selected_clip_slot": {
    "track_name": "Drums", // Name of the track this slot belongs to
    "track_index": 0, // Index of the track this slot belongs to
    "slot_index": 0, // 0-based index of the clip slot on its track
    "scene_index": 0, // 0-based index of the scene this slot aligns with
    "scene_name": "Verse 1", // Name of the scene, null if scene is unnamed or not applicable
    "has_content": true, // true if a clip exists in this slot, false otherwise
    "clip_name": "Drum Loop 1", // Name of the clip if has_content is true, null otherwise
    "is_playing": false,
    "is_recording": false,
    "is_playback_queued": false,
    "is_recording_queued": false,
    "is_stop_queued": false
  },
  "selected_device": {
    "track_name": "Drums", // Name of the track this device is on
    "track_index": 0, // Index of the track this device is on
    "index": 0, // 0-based index of the device in the track's device chain
    "name": "Polymer", // Name of the selected device
    "bypassed": false, // True if the device is currently bypassed
    "parameters": [ // Parameters of the selected device (reflects the 8 remote controls of the currently selected page)
      {
        "index": 0, // 0-7
        "exists": true, // True if a parameter is mapped to this remote slot on the current page
        "name": "Oscillator Mix", // From device.getRemoteControls().getParameter(i).name().get()
        "value": 0.75, // From device.getRemoteControls().getParameter(i).value().get() (normalized 0.0-1.0)
        "raw_value": 75.0, // From device.getRemoteControls().getParameter(i).value().getRaw() (native value)
        "display_value": "75%" // From device.getRemoteControls().getParameter(i).displayedValue().get()
      },
      {
        "index": 1,
        "exists": false,
        "name": null,
        "value": null,
        "raw_value": null,
        "display_value": null
      }
      // ... up to 8 device remote controls, mirroring structure of Epic 8's get_device_details.remote_controls
      // Only 'name', 'value', 'raw_value', 'display_value' are null if 'exists' is false.
    ]
  }
}
```

## 5. Technical Considerations

*   The `StatusTool.java` will need to interface with various parts of the Bitwig Studio Java API (Application, Project, Transport, Track, Device, ClipLauncherSlotBank, etc.) to retrieve the required information.
*   For `selected_device.parameters`, the structure should mirror the `remote_controls` array from Epic 8's `get_device_details` tool, providing an 8-element array where each element has an `exists` flag. This reflects the 8 remote controls of the currently selected page of the selected device. This includes adding `raw_value`.
*   Care must be taken to handle cases where information might be unavailable (e.g., no device selected, no clip slot selected, project parameters not configured). In such cases, relevant fields should be `null` or an empty array `[]` for lists.
*   The `value` for parameters should be normalized (0.0-1.0) where applicable, consistent with `get_selected_device_parameters`. `display_value` will provide the formatted string. `raw_value` provides the native, unnormalized value.
*   Indices (`track_index`, `slot_index`, `scene_index`, `device.index`) should be 0-based.
*   The `api-reference.md` will need to be updated to reflect these changes to the `status` command's response.

## 6. Acceptance Criteria

*   The `status` MCP command returns a JSON object matching the structure defined above.
*   All fields in the JSON response accurately reflect the current state of Bitwig Studio.
*   The `selected_device.parameters` array in the response correctly represents the 8 remote controls of the selected device's current page, including `exists`, `name`, `value`, `raw_value`, and `display_value` for each, aligning with the structure in Epic 8.
*   The `StatusTool.java` correctly queries the Bitwig API for all required data points.
*   The `api-reference.md` is updated with the new response format for the `status` command.
*   The changes are thoroughly tested for various Bitwig project states (e.g., empty project, project with content, different selections).
*   The tool gracefully handles scenarios where parts of the requested information are not available (e.g., no device selected, an empty clip slot selected).

## 7. Out of Scope for this Epic

*   Adding new MCP commands to modify these states (they are covered by other tools or future epics).
*   Fetching exhaustive lists of all tracks, scenes, or devices (these would be separate, more specific tools).
*   Real-time event streaming of state changes (this `status` command is a poll/request-response).
