# Epic 12: Listening and Auditioning

## 1. Goal

Let an agent hear (by meter) what it changed, audition sounds without writing clips, and show the user what it is working on.

## 2. Background

After an edit an agent cannot tell whether anything sounds. Bitwig offers VU meters, the notes playing on each channel, direct note and MIDI input to a track, and calls that bring tracks, clips and devices into view.

Out of scope: Rendering or recording audio for analysis.

## 3. User Stories

*   As an AI agent, I want to know whether and what a track is playing.
*   As an AI agent, I want to play notes and send MIDI to an instrument to try a sound.
*   As an AI agent, I want to bring what I changed into view for the user.

## 4. Proposed MCP Tools

| Story | Tools |
|---|---|
| [12.1](../stories/12.1.story.md) Engine, Meters and Playing Notes | `set_engine_active`, `get_levels`, `get_playing_notes` |
| [12.2](../stories/12.2.story.md) Audition and MIDI | `play_notes`, `send_midi` |
| [12.3](../stories/12.3.story.md) Show the User | `show` |

Request schemas are in [api-reference.md](../api-reference.md#epic-12-listening-and-auditioning).

## 5. Cross-cutting Rules

The rules of [Epic 9](./epic-9.md#5-cross-cutting-rules) apply: track targeting by `track_index` or `track_name`, asynchronous changes confirmed by polling, `setImmediately()` for value writes, the user's selection is never moved (except by `show`), and all Bitwig proxies are created at init.
