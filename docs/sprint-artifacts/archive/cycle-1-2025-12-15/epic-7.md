# Epic 7: Scene and Clip Information Retrieval

## 1. Goal

To provide MCP tools for listing all scenes in a Bitwig Studio project and for retrieving detailed information about all clips within a specific scene (across all tracks). This complements Epic 6 (Track Information Retrieval) by offering a scene-centric view of clip data.

## 2. Background

While Epic 6 allows detailed track and clip information retrieval on a per-track basis, users (via AI agents) also need to understand and interact with the project from a scene perspective. This is crucial for managing song structure and triggering scene-based playback. This epic provides the tools to list all available scenes and to get a comprehensive overview of all clips that constitute a particular scene.

## 3. User Stories

*   As an AI agent, I want to list all scenes in the current project, so I can understand the song's structure and available sections.
*   As an AI agent, I want to get detailed information for all clips within a specific scene (by index or name), including their track context, content properties, and playback states, so I can analyze or prepare a scene for launching.
*   As an AI agent, I want to know if a scene is currently selected/cued when listing all scenes.

## 4. Proposed MCP Tools

### 4.1. `list_scenes`

*   **Description:** Retrieves a list of all scenes in the project.
*   **Request Parameters:** (None)
*   **Response Body:** An array of scene objects, each containing:
    *   `index` (integer): 0-based index of the scene.
    *   `name` (string): Name of the scene.
    *   `color` (string, nullable): Color of the scene (e.g., "rgb(r,g,b)").
    *   `is_selected` (boolean): True if this scene is currently selected/cued in the launcher (global scene selection).

### 4.2. `get_clips_in_scene`

*   **Description:** Retrieves detailed information for all clip slots that are part of a specific scene, across all relevant tracks.
*   **Request Parameters:**
    *   `scene_index` (integer, optional): 0-based index of the scene.
    *   `scene_name` (string, optional): Name of the scene.
    *   *(At least one of `scene_index` or `scene_name` must be provided)*
*   **Response Body:** An array of clip slot objects for the specified scene. Each object represents a slot on a particular track within that scene and contains:
    *   `track_index` (integer): 0-based index of the track this slot belongs to.
    *   `track_name` (string): Name of the track this slot belongs to.
    *   `has_content` (boolean): True if a clip exists in this slot.
    *   `clip_name` (string, nullable): Name of the clip if `has_content` is true.
    *   `clip_color` (string, nullable): Color of the clip if `has_content` is true.
    *   `length` (string, nullable): Length of the clip (e.g., "4.0.0" or a duration format) if `has_content` is true.
    *   `is_looping` (boolean, nullable): True if the clip is set to loop, if `has_content` is true.
    *   `is_playing` (boolean): True if the clip in this slot is currently playing.
    *   `is_recording` (boolean): True if the clip in this slot is currently recording.
    *   `is_playback_queued` (boolean): True if playback is queued for the clip in this slot.
    *   `is_recording_queued` (boolean): True if recording is queued for the clip in this slot.
    *   `is_stop_queued` (boolean): True if a stop is queued for the clip in this slot.

## 5. Technical Considerations

*   `list_scenes` will use the Bitwig API to iterate through the project's `SceneBank` or equivalent.
*   `get_clips_in_scene` will require identifying the target scene, then iterating through all tracks (`TrackBank`) and for each track, accessing its `ClipLauncherSlotBank` at the specified scene's index to retrieve clip details.
*   The definition of a "selected" scene (`is_selected` in `list_scenes`) needs to be confirmed with Bitwig API capabilities (e.g., if it refers to the scene pointed to by the global scene launch cursor).
*   Error handling for invalid scene identifiers (name/index).
*   The `api-reference.md` will need to be updated with these new tools.

## 6. Acceptance Criteria

*   `list_scenes` MCP command returns an accurate array of all scenes with their `index`, `name`, `color`, and `is_selected` status.
*   `get_clips_in_scene` MCP command returns an accurate array of clip slot objects for the specified scene, with each object containing `track_index`, `track_name`, and the full set of clip properties (content, color, length, loop, playback states).
*   Both tools handle requests with valid scene identifiers (index or name).
*   Both tools handle edge cases gracefully (e.g., empty project, scene not found).
*   `api-reference.md` is updated to reflect the new tools.

## 7. Out of Scope for this Epic

*   Modifying scene or clip properties (these would be separate tools/epics).
*   Launching scenes or clips (covered in Epic 3).
*   Retrieving clips for a specific track (this is covered by `get_track_details` in Epic 6).
