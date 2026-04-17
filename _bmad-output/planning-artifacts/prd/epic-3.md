# Epic 3: Clip and Scene Launching Implementation

**Goal:** Enable the triggering of individual clips (by track name and clip index/scene number) and entire scenes (by name or index) via MCP commands, expanding interactive control over Bitwig's session view. This fulfills PRD Objective 3.
**Architectural Alignment:** This epic involves the `RequestRouter`, `McpCommandParser`, a new `SceneController` (or similar feature module for session control), the `BitwigApiFacade`, and the `Logger`. It adheres to `docs/api-reference.md` for command/response formats and `docs/data-models.md` for DTOs.

## Story List

### Refined Story 3.1: Trigger Clip by Track Name and Clip Index via MCP
* **User Story / Goal:** As a Musician (via an External AI Agent), I want to command WigAI to launch a specific clip in Bitwig by providing its track name and clip slot index (scene number) through an MCP message, so that I can trigger specific musical phrases or loops.
* **Detailed Requirements:**
    * The WigAI MCP server (via `RequestRouter`, `McpCommandParser`, and the `SceneController` feature module) must handle the "launch_clip" command.
    * The command structure and payload (`LaunchClipPayload` containing `track_name` and `clip_index`) must adhere to `docs/api-reference.md` and `docs/data-models.md`.
        * Example request payload: `{"track_name": "Drums", "clip_index": 0}`
    * The `McpCommandParser` must validate:
        * `track_name` is a non-empty string.
        * `clip_index` is a non-negative integer.
    * The `SceneController` will use the `BitwigApiFacade` to:
        * Find the specified track by its name. The track name matching should be **case-sensitive** (MVP behavior to be documented).
        * Trigger the clip in the specified 0-based slot index on that track.
    * Return an MCP success response as specified in `docs/api-reference.md` (structure `LaunchClipResponseData`).
        * Example: `{"status": "success", "data": {"action": "clip_launched", "track_name": "Drums", "clip_index": 0, "message": "Clip at Drums[0] launched."}}`
    * Handle errors gracefully:
        * If `track_name` is not found, return MCP error `TRACK_NOT_FOUND`.
        * If `clip_index` is out of bounds for the found track, return MCP error `CLIP_INDEX_OUT_OF_BOUNDS`.
        * If the Bitwig API call fails, return MCP error `BITWIG_ERROR`.
    * Behavior for empty clip slots: If the specified slot is empty, WigAI should proceed with the launch (mirroring Bitwig's behavior which might stop track playback depending on settings) and return success. The response message can optionally note if the slot was empty if this information is easily retrievable.
    * Log the command, action, and response/errors.
* **Acceptance Criteria (ACs):**
    * AC1: Sending the "launch_clip" MCP command with a valid track name and clip index launches the corresponding clip in Bitwig.
    * AC2: WigAI returns a successful MCP response matching the specified JSON structure.
    * AC3: If the specified track is not found (respecting case sensitivity as decided), an MCP error response with code `TRACK_NOT_FOUND` is returned, and no clip is launched.
    * AC4: If the `clip_index` is out of bounds for the found track, an MCP error response with code `CLIP_INDEX_OUT_OF_BOUNDS` is returned, and no clip is launched.
    * AC5: Launching an empty clip slot behaves as Bitwig does and WigAI returns a success response.
    * AC6: The `Logger` service logs the command, action, and relevant details.

---

### Refined Story 3.2: Trigger Scene by Index via MCP
* **User Story / Goal:** As a Musician (via an External AI Agent), I want to command WigAI to launch an entire scene in Bitwig by providing its numerical index through an MCP message, so that I can trigger a collection of clips simultaneously.
* **Detailed Requirements:**
    * The WigAI MCP server (via `RequestRouter`, `McpCommandParser`, and the `SceneController` feature module) must handle the "launch_scene_by_index" command.
    * The command structure and payload (`LaunchSceneByIndexPayload` containing `scene_index`) must adhere to `docs/api-reference.md` and `docs/data-models.md`.
        * Example request payload: `{"scene_index": 1}`
    * The `McpCommandParser` must validate that `scene_index` is a non-negative integer.
    * The `SceneController` will use the `BitwigApiFacade` to:
        * Access the main scene list in Bitwig.
        * Launch the scene at the specified 0-based index.
    * Return an MCP success response as specified in `docs/api-reference.md` (structure `LaunchSceneByIndexResponseData`).
        * Example: `{"status": "success", "data": {"action": "scene_launched", "scene_index": 1, "message": "Scene 1 launched."}}`
    * Handle errors gracefully:
        * If `scene_index` is out of bounds for the available scenes in Bitwig, return MCP error `SCENE_NOT_FOUND`.
        * If the Bitwig API call fails, return MCP error `BITWIG_ERROR`.
    * Log the command, action, and response/errors.
* **Acceptance Criteria (ACs):**
    * AC1: Sending the "launch_scene_by_index" MCP command with a valid `scene_index` launches the corresponding scene in Bitwig.
    * AC2: WigAI returns a successful MCP response matching the specified JSON structure.
    * AC3: If the `scene_index` is out of bounds (e.g., too high, or negative if not caught by parser), an MCP error response with code `SCENE_NOT_FOUND` is returned, and no scene is launched.
    * AC4: The `Logger` service logs the command, action, and relevant details.

---

### Refined Story 3.3: Trigger Scene by Name via MCP
* **User Story / Goal:** As a Musician (via an External AI Agent), I want to command WigAI to launch an entire scene in Bitwig by providing its name through an MCP message, offering a more user-friendly way to trigger scenes.
* **Detailed Requirements:**
    * The WigAI MCP server (via `RequestRouter`, `McpCommandParser`, and the `SceneController` feature module) must handle the "launch_scene_by_name" command.
    * The command structure and payload (`LaunchSceneByNamePayload` containing `scene_name`) must adhere to `docs/api-reference.md` and `docs/data-models.md`.
        * Example request payload: `{"scene_name": "Verse 1"}`
    * The `McpCommandParser` must validate that `scene_name` is a non-empty string.
    * The `SceneController` will use the `BitwigApiFacade` to:
        * Iterate through the scenes in Bitwig's main scene list.
        * Find the scene matching the given `scene_name`.
            * **Case Sensitivity:** For MVP, scene name matching will be **case-sensitive**. This behavior should be documented.
        * If multiple scenes have the same name, WigAI will launch the first one found during its iteration. This behavior should also be documented.
        * If a matching scene is found, launch it.
    * Return an MCP success response as specified in `docs/api-reference.md` (structure `LaunchSceneByNameResponseData`).
        * Example: `{"status": "success", "data": {"action": "scene_launched", "scene_name": "Verse 1", "message": "Scene 'Verse 1' launched."}}`
        * The response can optionally include the `launched_scene_index` if the facade can easily provide it.
    * Handle errors gracefully:
        * If no scene matching `scene_name` is found, return MCP error `SCENE_NOT_FOUND`.
        * If the Bitwig API call fails during scene iteration or launch, return MCP error `BITWIG_ERROR`.
    * Log the command, search action, launch action, and response/errors.
* **Acceptance Criteria (ACs):**
    * AC1: Sending the "launch_scene_by_name" MCP command with a valid and existing (case-sensitive) scene name launches the corresponding scene in Bitwig.
    * AC2: WigAI returns a successful MCP response matching the specified JSON structure.
    * AC3: If the specified `scene_name` is not found (case-sensitive match), an MCP error response with code `SCENE_NOT_FOUND` is returned.
    * AC4: If multiple scenes share the same name, the first one found (based on Bitwig API's scene iteration order) is launched, and this specific behavior is documented for the user/developer.
    * AC5: The `Logger` service logs the command, search process (e.g., "Searching for scene 'Verse 1'"), and the outcome.

## Change Log

| Change                                | Date       | Version | Description                             | Author              |
| ------------------------------------- | ---------- | ------- | --------------------------------------- | ------------------- |
| Initial Draft                         | 2025-05-16 | 0.1     | First draft of Epic 3 stories           | 2-pm BMAD v2        |
| Architectural Refinement of Stories   | 2025-05-16 | 0.2     | Aligned stories with defined architecture | 3-architect BMAD v2 |
