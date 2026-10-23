# Epic 14: Project-wide Control

## 1. Goal

Project-wide state and settings: stray solos and mutes, groove, transport options beyond play/stop, and the cue bus.

## 2. Background

A forgotten solo silences a whole project and is hard to spot from per-track tools. Groove, punch, pre-roll, metronome volume, launch quantization and the cue mix are project settings the API exposes through `Project`, `Groove` and `Transport`.

Out of scope: Opening projects from disk.

## 3. User Stories

*   As an AI agent, I want to see and clear solos, mutes and arms in one call.
*   As an AI agent, I want to set groove and transport options.

## 4. Proposed MCP Tools

| Story | Tools |
|---|---|
| [14.1](../stories/14.1.story.md) Project State and Reset | `project_state`, `reset_project` |
| [14.2](../stories/14.2.story.md) Groove, Transport Options and Cue Mix | `set_groove`, `set_transport_options`, `set_cue` |

Request schemas are in [api-reference.md](../api-reference.md#epic-14-project-wide-control).

## 5. Cross-cutting Rules

The rules of [Epic 9](./epic-9.md#5-cross-cutting-rules) apply: track targeting by `track_index` or `track_name`, asynchronous changes confirmed by polling, `setImmediately()` for value writes, the user's selection is never moved (except by `show`), and all Bitwig proxies are created at init.
