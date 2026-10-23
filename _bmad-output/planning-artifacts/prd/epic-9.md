# Epic 9: Project Editing

## 1. Goal

To provide MCP tools that let an AI agent change a Bitwig project, not only read it: create and configure tracks, build device chains, set parameters on any device, create clips and write notes, adjust transport settings, and run Bitwig's own application actions.

## 2. Background

Epics 1–8 give agents transport control, clip and scene launching, parameter control of the *selected* device, and read access to tracks, scenes and devices. An agent that is asked to "put a vocoder on the backing vocals and match their reverb" can describe what to do but cannot do it, because every change still needs the user's hands. The Bitwig Controller API (version 19) already exposes track creation, device insertion by UUID/CLAP/VST3 id, insertion points for copying and moving devices, remote-control pages on any device, launcher clips and notes, and the application action list. This epic exposes those capabilities through MCP.

Out of scope, because the Controller API does not expose them: audio routing and sidechain source selection, and placing clips on the arranger timeline (only launcher slots can be targeted).

## 3. User Stories

*   As an AI agent, I want to create, delete and duplicate tracks and set their mixer state (name, volume in dB, pan, mute, solo, arm, activation, monitor mode, color, sends), so I can lay out a project for the user.
*   As an AI agent, I want to insert Bitwig built-in devices by name and plugins by CLAP or VST3 id, and copy, move, bypass and delete devices, so I can build signal chains.
*   As an AI agent, I want to read and set the parameters of any device on any track, across all of its remote-control pages, without changing what the user has selected, so I can configure a chain while the user keeps working.
*   As an AI agent, I want to import audio and MIDI files into clip slots, create empty clips and write notes into them, so I can supply musical material.
*   As an AI agent, I want to set tempo, play position, loop, metronome, record and overdub, list and invoke Bitwig application actions, and undo/redo, so I can reach functionality that has no dedicated tool and recover from mistakes.

## 4. Proposed MCP Tools

| Story | Tools |
|---|---|
| [9.1](../stories/9.1.story.md) Tracks and mixer | `create_track`, `delete_track`, `duplicate_track`, `get_track_mixer`, `set_track`, `set_send` |
| [9.2](../stories/9.2.story.md) Device chain editing | `list_bitwig_devices`, `insert_device`, `copy_device`, `move_device`, `delete_device`, `set_device_enabled`, `set_device_window_open` |
| [9.3](../stories/9.3.story.md) Parameters of any device | `get_device_parameters`, `set_device_parameters` |
| [9.4](../stories/9.4.story.md) Clips and notes | `insert_file`, `create_clip`, `delete_clip`, `write_notes` |
| [9.5](../stories/9.5.story.md) Transport settings and application actions | `set_transport`, `list_actions`, `invoke_action`, `undo`, `redo` |
| [9.6](../stories/9.6.story.md) Live end-to-end test | `scripts/live_test.py` |

Request and response schemas are in [api-reference.md](../api-reference.md#project-editing-commands).

## 5. Cross-cutting Rules

*   **Targeting:** every track-scoped tool accepts `track_index` (0-based, as in `list_tracks`) or `track_name` (exact, case-sensitive). Device-scoped tools add `device_index` (as in `list_devices_on_track`).
*   **Asynchrony:** Bitwig applies changes asynchronously. Mutating tools poll (20 ms interval, 2 s timeout) until the change is visible and return the resulting state; a change that never appears returns `BITWIG_TIMEOUT`.
*   **Value writes:** Bitwig ignores `SettableRangedValue.set()` from an extension that has no bound hardware control. All value writes (device parameters, volume, pan, sends) use `setImmediately()`.
*   **Selection is never moved:** device parameters are reached through a dedicated cursor track (`followSelection = false`) and cursor device, not the user's cursor.
*   **Track list layout:** the flat track bank lists regular tracks, then effect tracks, then the master track. Regular and effect tracks are counted separately; a newly created track's type is checked before it is renamed; the master track cannot be deleted or duplicated.
*   **Objects are created at init:** all Bitwig proxies (cursor track/device, remote-control page, launcher cursor clip, send bank of 8) are created in the facade constructor, which runs during `init()`.
