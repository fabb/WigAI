# Epic 10: Device Chains, Presets and Containers

## 1. Goal

Let an agent work inside container devices and with Bitwig's sound library: the chains inside a device (a Vocoder's Modulator and Carrier, FX slots, layers), presets on disk and in Bitwig's browser, Drum Machine pads and the mixers of layer devices.

## 2. Background

Epic 9 edits devices on a track's own chain. Most interesting sounds live one level down: a Vocoder needs a carrier in its Carrier chain, a Drum Machine holds a device chain per pad, and Instrument/FX Layer mix several chains. Agents also need to start from presets rather than init patches. The Controller API exposes device slots (`CursorDeviceSlot`), layer and drum pad banks (which are `Channel`s), insertion points with `insertFile`, and the `PopupBrowser`.

Out of scope: Adding, duplicating or deleting layers and emptying a drum pad: `DeviceLayer.duplicateObject()`/`deleteObject()` and `DrumPad.deleteObject()` are ignored from an extension in Bitwig 6.1.3, and `host.deleteObjects(drumPad)` crashes Bitwig (ClassCastException on its GUI thread), so the tools never call it. Packaged presets are encrypted, so their device is not readable from disk.

## 3. User Stories

*   As an AI agent, I want to read and edit the chains inside a device (slots and layers), so I can build a vocoder or a layered sound.
*   As an AI agent, I want to load presets and search Bitwig's library, so I can start from a designed sound.
*   As an AI agent, I want to set up Drum Machine pads and balance the layers of a layer device.

## 4. Proposed MCP Tools

| Story | Tools |
|---|---|
| [10.1](../stories/10.1.story.md) Chains Inside Devices | `get_device_chains` |
| [10.2](../stories/10.2.story.md) Presets and Bitwig's Browser | `list_presets`, `replace_device`, `browser_open`, `browser_state`, `browser_filter`, `browser_commit`, `browser_cancel` |
| [10.3](../stories/10.3.story.md) Drum Machine Pads and Layer Mixers | `list_drum_pads`, `set_drum_pad`, `insert_into_drum_pad`, `clear_drum_pads`, `list_layers`, `set_layer` |

Request schemas are in [api-reference.md](../api-reference.md#epic-10-device-chains-presets-and-containers).

## 5. Cross-cutting Rules

The rules of [Epic 9](./epic-9.md#5-cross-cutting-rules) apply: track targeting by `track_index` or `track_name`, asynchronous changes confirmed by polling, `setImmediately()` for value writes, the user's selection is never moved (except by `show`), and all Bitwig proxies are created at init.
