# Epic 11: Device Knowledge

## 1. Goal

Give agents the facts they need to choose devices: everything installed, what each control does, and Bitwig's own description of each built-in device, without editorializing.

## 2. Background

`list_bitwig_devices` (Epic 9) only names devices. To recommend a device an agent needs the full inventory (built-ins and plugins), the controls with their real ranges, and a description. Bitwig keeps device metadata in its library and plugin metadata in its own cache, and the user guide describes every built-in device.

Out of scope: Recommendations themselves: the catalog stores facts; optional personal notes can sit next to it in `device-guide.json` and are returned as `user_notes`.

## 3. User Stories

*   As an AI agent, I want to search everything installed, built-in and plugin, so I know what I can use.
*   As an AI agent, I want to know what each control of a device does, measured rather than guessed.
*   As an AI agent, I want to read Bitwig's own description of each device.

## 4. Proposed MCP Tools

| Story | Tools |
|---|---|
| [11.1](../stories/11.1.story.md) Installed Devices and Plugins | `list_installed_devices` |
| [11.2](../stories/11.2.story.md) Describe a Device, Scanning on Demand | `describe_device`, `catalog_status` |
| [11.3](../stories/11.3.story.md) Bitwig's Device Descriptions | `fetch_device_docs` |

Request schemas are in [api-reference.md](../api-reference.md#epic-11-device-knowledge).

## 5. Cross-cutting Rules

The rules of [Epic 9](./epic-9.md#5-cross-cutting-rules) apply: track targeting by `track_index` or `track_name`, asynchronous changes confirmed by polling, `setImmediately()` for value writes, the user's selection is never moved (except by `show`), and all Bitwig proxies are created at init.
