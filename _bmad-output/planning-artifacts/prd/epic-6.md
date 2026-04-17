# Epic 6: Track Information Retrieval

## 1. Goal

To provide comprehensive MCP tools for listing all tracks within a Bitwig Studio project and retrieving detailed information about specific tracks, including the currently selected track. This will enable AI agents to have a deeper understanding of the project's track structure, individual track states, contained devices, and clip details.

## 2. Background

While the enhanced `status` command (Epic 5) provides information about the *selected* track, AI agents often need a broader view of all tracks or need to query specific tracks by name or index. This epic introduces dedicated tools for these purposes, drawing inspiration from the granular data available via OSC and the Bitwig Java API.

## 3. User Stories

*   As an AI agent, I want to list all tracks in the current project with summary information (name, type, selection state, basic device list), so I can understand the overall project structure.
*   As an AI agent, I want to get detailed information for a specific track (by index, name, or by targeting the currently selected track), including its parameters (volume, pan, mute, solo, arm), sends, and a list of its clips with their properties (name, color, length, loop status), so I can perform targeted analysis or suggest modifications.
*   As an AI agent, I want to know which track is currently selected when listing all tracks, so I can easily identify the user's current focus.
*   As an AI agent, I want to know the parent group of a track, so I can understand its hierarchical context.

## 4. Proposed MCP Tools

### 4.1. `list_tracks`

*   **Description:** Retrieves a list of all tracks in the project with summary information.
*   **Request Parameters:**
    *   `type` (string, optional): Filter by track type (e.g., "audio", "instrument", "group", "effect", "master").
*   **Response Body:** An array of track objects, each containing:
    *   `index` (integer): 0-based position in the project's main track list.
    *   `name` (string): Name of the track.
    *   `type` (string): Type of the track (e.g., "audio", "instrument", "hybrid", "group", "effect", "master").
    *   `is_group` (boolean): True if the track is a group track.
    *   `parent_group_index` (integer, nullable): 0-based index of the parent group track if this track is inside a group, `null` otherwise.
    *   `activated` (boolean): Is the track activated.
    *   `color` (string): Color of the track (e.g., "rgb(r,g,b)").
    *   `is_selected` (boolean): True if this is the currently selected track.
    *   `devices` (array of objects): Summary of devices on this track.
        *   `index` (integer): 0-based position in this track's device chain.
        *   `name` (string): Name of the device.
        *   `type` (string): Type of the device (e.g. "Instrument", "AudioFX", "NoteFX").

### 4.2. `get_track_details`

*   **Description:** Retrieves detailed information for a specific track.
*   **Request Parameters:**
    *   `track_index` (integer, optional): 0-based index of the track.
    *   `track_name` (string, optional): Name of the track.
    *   `get_selected` (boolean, optional): If true, retrieves details for the currently selected track. If no parameters are provided, this defaults to `true`.
    *   *(Note: Only one of `track_index`, `track_name`, or `get_selected` (or default behavior) should be used per call.)*
*   **Response Body:** A single track object containing:
    *   All fields from the `list_tracks` response object for this track.
    *   `volume` (float): Normalized volume (0.0-1.0).
    *   `volume_str` (string): Formatted volume string (e.g., "-6.0 dB").
    *   `pan` (float): Normalized pan (-1.0 to 1.0 or 0.0-1.0, needs API confirmation).
    *   `pan_str` (string): Formatted pan string (e.g., "L 50%").
    *   `muted` (boolean): Is the track muted.
    *   `soloed` (boolean): Is the track soloed.
    *   `armed` (boolean): Is the track armed for recording.
    *   `monitor_enabled` (boolean): Is monitoring enabled.
    *   `auto_monitor_enabled` (boolean): Is auto-monitoring enabled.
    *   `sends` (array of objects): List of sends for the track.
        *   `name` (string): Name of the send channel.
        *   `volume` (float): Normalized send level.
        *   `volume_str` (string): Formatted send level string.
        *   `activated` (boolean): Is the send activated.
    *   `clips` (array of objects): List of clips in the track's slots.
        *   `slot_index` (integer): 0-based index of the clip slot (corresponds to scene index).
        *   `scene_name` (string, nullable): Name of the scene this slot aligns with, `null` if scene is unnamed or not applicable.
        *   `has_content` (boolean): True if the slot contains a clip.
        *   `clip_name` (string, nullable): Name of the clip if `has_content` is true.
        *   `clip_color` (string, nullable): Color of the clip if `has_content` is true.
        *   `length` (string, nullable): Length of the clip (e.g., "4.0.0" for beats.beats.sixteenths or a duration format) if `has_content` is true.
        *   `is_looping` (boolean, nullable): True if the clip is set to loop, if `has_content` is true.
        *   `is_playing` (boolean, nullable): True if the clip in this slot is currently playing (if `has_content` is true).
        *   `is_recording` (boolean, nullable): True if the clip in this slot is currently recording (if `has_content` is true).
        *   `is_playback_queued` (boolean, nullable): True if playback is queued for the clip in this slot (if `has_content` is true).
        *   `is_recording_queued` (boolean, nullable): True if recording is queued for the clip in this slot (if `has_content` is true).
        *   `is_stop_queued` (boolean, nullable): True if a stop is queued for the clip in this slot (if `has_content` is true).

## 5. Technical Considerations

*   The implementation will primarily use the Bitwig Studio Java API, specifically `TrackBank` for `list_tracks` and `CursorTrack` (potentially moved to a specific track) for `get_track_details`.
*   Device listing within `list_tracks` will require iterating through each track's `DeviceChain`.
*   Clip information for `get_track_details` will involve accessing the track's `ClipLauncherSlotBank`.
*   Normalization of values (volume, pan) should be consistent with other MCP tools.
*   Error handling for invalid track identifiers (name/index) or when a selected track is not available.
*   The `api-reference.md` will need to be updated with these new tools and their detailed schemas.

## 6. Acceptance Criteria

*   `list_tracks` MCP command returns an accurate array of all tracks with the specified summary fields.
*   `list_tracks` correctly indicates the `is_selected` track.
*   `list_tracks` correctly lists summary device information for each track.
*   `get_track_details` MCP command returns accurate, detailed information for a track specified by index, name, or as the currently selected track.
*   `get_track_details` defaults to the selected track if no specifier is given.
*   `get_track_details` includes correct volume, pan, mute/solo/arm, sends, and detailed clip information (including length, loop status, and playback states).
*   Both tools handle edge cases gracefully (e.g., empty project, track not found).
*   `api-reference.md` is updated to reflect the new tools.

## 7. Out of Scope for this Epic

*   Modifying track properties (these would be separate tools/epics).
*   Listing detailed device parameters within `list_tracks` (device summaries are sufficient here; full device details are for Epic 8 or `get_device_details`).
*   Real-time event streaming of track changes (these tools are poll/request-response).
