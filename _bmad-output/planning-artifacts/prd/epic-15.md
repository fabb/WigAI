# Epic 15: Following the User

## 1. Goal

Let an agent follow what happens in Bitwig as it happens, instead of polling every tool (issue #3).

## 2. Background

Issue #3 asks for a way to listen for changes over the MCP stream. MCP Java SDK 0.11 sends progress and logging notifications during a call but does not handle resource subscriptions, so changes are offered as an event log that can be long-polled, and as a call that streams them.

Out of scope: MCP resource subscriptions (`resources/subscribe`), until the SDK supports them.

## 3. User Stories

*   As an AI agent, I want to get every change since I last looked, or wait for the next one.
*   As an AI agent, I want to receive changes as notifications while a call runs.

## 4. Proposed MCP Tools

| Story | Tools |
|---|---|
| [15.1](../stories/15.1.story.md) Event Log | `get_events` |
| [15.2](../stories/15.2.story.md) Streaming Changes | `watch` |

Request schemas are in [api-reference.md](../api-reference.md#epic-15-following-the-user).

## 5. Cross-cutting Rules

The rules of [Epic 9](./epic-9.md#5-cross-cutting-rules) apply: track targeting by `track_index` or `track_name`, asynchronous changes confirmed by polling, `setImmediately()` for value writes, the user's selection is never moved (except by `show`), and all Bitwig proxies are created at init.
