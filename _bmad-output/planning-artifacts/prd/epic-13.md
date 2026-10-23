# Epic 13: Arrangement, Scenes, Clips and Notes

## 1. Goal

Complete the musical editing tools: arrangement markers and loop, scene management, clip management, reading and editing notes with all of Bitwig's per-note expressions and conditions, and the arranger clip the user selects.

## 2. Background

Epic 9 can create clips and write notes. Shaping music needs reading notes back, editing them in place (velocity, timing, expressions, chance, occurrence, recurrence, repeats), clip settings (loop, shuffle, accent) and moving material between slots and scenes.

Out of scope: Placing or selecting arranger clips (the arranger cursor clip follows the user's selection only) and capturing playing clips into a scene (`createSceneFromPlayingLauncherClips` does nothing from an extension in Bitwig 6.1.3).

## 3. User Stories

*   As an AI agent, I want to navigate and loop the arrangement.
*   As an AI agent, I want to manage scenes.
*   As an AI agent, I want to copy, move, duplicate and record clips.
*   As an AI agent, I want to read the notes of a clip and change them in place.
*   As an AI agent, I want to edit the arranger clip the user selected.

## 4. Proposed MCP Tools

| Story | Tools |
|---|---|
| [13.1](../stories/13.1.story.md) Arrangement and Markers | `list_cue_markers`, `go_to_cue_marker`, `set_arrangement`, `stop_track` |
| [13.2](../stories/13.2.story.md) Scenes | `create_scene`, `rename_scene`, `set_scene`, `duplicate_scene`, `copy_scene`, `move_scene`, `delete_scene` |
| [13.3](../stories/13.3.story.md) Clip Management | `copy_clip`, `move_clip`, `duplicate_clip`, `record_into_slot`, `set_clip`, `transform_clip` |
| [13.4](../stories/13.4.story.md) Reading and Editing Notes | `read_notes`, `edit_notes` |
| [13.5](../stories/13.5.story.md) Arranger Clips | `write_arranger_notes` |

Request schemas are in [api-reference.md](../api-reference.md#epic-13-arrangement-scenes-clips-and-notes).

## 5. Cross-cutting Rules

The rules of [Epic 9](./epic-9.md#5-cross-cutting-rules) apply: track targeting by `track_index` or `track_name`, asynchronous changes confirmed by polling, `setImmediately()` for value writes, the user's selection is never moved (except by `show`), and all Bitwig proxies are created at init.
