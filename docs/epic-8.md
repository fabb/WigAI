# Epic 8: Device Information Retrieval

## 1. Goal

To provide MCP tools to list devices on a specific track (or the currently selected track) and to retrieve detailed information—including its remote controls (for the currently selected page, with normalized, raw, and display values) and the list of all remote control pages (indicating which is selected)—for a specific device (or the currently selected device).

## 2. Background

Building upon the track and clip information retrieval from Epics 6 and 7, and the selected device parameter summary in Epic 5, this epic focuses on comprehensive device-level data. AI agents need to inspect the full device chain of a track and get in-depth details of any device, including its 8 assignable remote controls and how they are organized into pages. This is essential for analysis, suggesting device manipulations, or building custom control interfaces for these primary controls. Direct device parameters and modulator parameters are out of scope for this epic.

## 3. User Stories

*   As an AI agent, I want to list all devices on a specific track (identified by name, index, or as the currently selected track), including their name, type, and key states (bypassed, expanded, window open, selected), so I can understand the signal chain of that track and identify the currently active device.
*   As an AI agent, I want to get detailed information for a specific device (identified by its track context + device index/name, or as the currently selected device in the project), including its full context, key states, its 8 remote controls (for the currently selected page, detailing existence, name, normalized value, raw value, display value), and its 8 remote control pages (listing all available pages and indicating which is selected), so I can deeply analyze or prepare to control that device via its main remote interface.

## 4. Proposed MCP Tools

### 4.1. `list_devices_on_track`

*   **Description:** Retrieves a list of all devices on a specified track.
*   **Request Parameters:**
    *   `track_index` (integer, optional): 0-based index of the track.
    *   `track_name` (string, optional): Name of the track.
    *   `get_for_selected_track` (boolean, optional): If `true` (and no `track_index` or `track_name` is provided), lists devices for the currently selected track. Defaults to `true` if no track identifier is specified.
*   **Response Body:** An array of device summary objects, each containing:
    *   `index` (integer): 0-based position in the track's device chain.
    *   `name` (string): Name of the device.
    *   `type` (string): Type of the device (e.g., "Instrument", "AudioFX", "NoteFX").
    *   `is_bypassed` (boolean): True if the device is currently bypassed.
    *   `is_expanded` (boolean): True if the device GUI is expanded within the device chain view.
    *   `is_window_open` (boolean): True if the plugin window (if it has one) is currently open.
    *   `is_selected` (boolean): True if this device is the currently selected device in Bitwig Studio.

### 4.2. `get_device_details`

*   **Description:** Retrieves detailed information for a specific device.
*   **Request Parameters:**
    *   `track_index` (integer, optional): 0-based index of the track containing the device.
    *   `track_name` (string, optional): Name of the track containing the device.
    *   `device_index` (integer, optional): 0-based index of the device on its track. (Required if `track_index` or `track_name` is specified and `device_name` is not, or for disambiguation).
    *   `device_name` (string, optional): Name of the device. (Can be used with `track_index` or `track_name` as an alternative to `device_index`. If multiple devices on the track share the same name, `device_index` is preferred).
    *   `get_for_selected_device` (boolean, optional): If `true` (and no other device/track identifiers are provided), retrieves details for the currently selected device in Bitwig Studio. Defaults to `true` if no other specifiers are given.
*   **Response Body:** A single device object containing:
    *   `track_index` (integer): 0-based index of the track this device is on.
    *   `track_name` (string): Name of the track this device is on.
    *   `index` (integer): 0-based position of the device in its track's device chain.
    *   `name` (string): Name of the device.
    *   `type` (string): Type of the device.
    *   `is_bypassed` (boolean): True if the device is currently bypassed.
    *   `is_expanded` (boolean): True if the device GUI is expanded within the device chain view.
    *   `is_window_open` (boolean): True if the plugin window (if it has one) is currently open.
    *   `is_selected` (boolean): True if this device is the currently selected device in Bitwig Studio.
    *   `remote_controls` (array of objects, for the 8 remote control slots **of the currently selected remote control page**):
        *   `index` (integer, 0-7): Index of the remote control slot on the current page.
        *   `exists` (boolean): True if a parameter is mapped to this remote slot on the current page.
        *   `name` (string, nullable): Name of the parameter mapped to this remote slot (if `exists`).
        *   `value` (float, nullable): Normalized value (0.0-1.0) of the mapped parameter (if `exists`).
        *   `raw_value` (float, nullable): Raw, unnormalized value of the mapped parameter in its native unit (if `exists`).
        *   `display_value` (string, nullable): Formatted string value of the mapped parameter (if `exists`).
    *   `remote_control_pages` (array of page objects, for the up to 8 remote-controllable parameter pages):
        *   `index` (integer, 0-7): Index of the remote control page slot.
        *   `exists` (boolean): True if a page is defined for this slot.
        *   `name` (string, nullable): Name of the remote control page (if `exists`).
        *   `is_selected` (boolean, nullable): True if this remote control page is currently selected (and thus its controls are listed in `remote_controls`). Only one page can be selected at a time.

## 5. Technical Considerations

*   `list_devices_on_track` will involve: identifying the target track (via `TrackBank` or `CursorTrack`), then accessing its `DeviceChain` and iterating through devices.
*   `get_device_details` will involve: identifying the target track, then the target device within that track's `DeviceChain` (or using `CursorDevice` if `get_for_selected_device` is true).
    *   Details for the `remote_controls` array will be sourced from the device's primary `RemoteControlBank` (e.g., `device.remoteControls()`), which represents the 8 remote controls of the **currently selected page**.
    *   Details for the `remote_control_pages` array will be sourced by iterating through the `RemoteControlPageBank` (e.g., `device.remoteControls().pageBank()`), checking each `RemoteControlPage` for its name and selection state (`isSelected()`).
    *   Individual remote control data (`name`, `value`, `raw_value`, `display_value`) is obtained from the `RemoteControl` objects within the primary `RemoteControlBank`.
*   The `is_selected` flag for a device in `list_devices_on_track` requires comparing each device to the global `CursorDevice`.
*   Error handling for invalid track/device identifiers or when a selected item is not available.
*   The `api-reference.md` will need to be updated.

## 6. Acceptance Criteria

*   `list_devices_on_track` MCP command returns an accurate array of all devices on the specified track (or selected track) with the specified summary fields, including correct `is_selected` status.
*   `get_device_details` MCP command returns accurate, detailed information for a device specified by track/device identifiers or as the currently selected device.
*   `get_device_details` includes correct `track_index`, `track_name`, device `index`, `name`, `type`, states (`is_bypassed`, `is_expanded`, `is_window_open`, `is_selected`).
*   `get_device_details` correctly lists up to 8 remote controls **for the currently selected page** with `index`, `exists`, `name`, `value` (normalized), `raw_value`, and `display_value`.
*   `get_device_details` correctly lists up to 8 remote control pages with `index`, `exists`, `name`, and `is_selected` status, ensuring only one page is marked as selected.
*   Both tools handle default behavior (targeting selected track/device) correctly when no specific identifiers are provided.
*   Both tools handle edge cases gracefully (e.g., track not found, device not found, no devices on track).
*   `api-reference.md` is updated to reflect the new tools.

## 7. Out of Scope for this Epic

*   Modifying device properties or remote controls (these would be separate tools/epics).
*   Listing *all* internal parameters of a device if it has more than the 8 remote-controllable ones (focus is on the common remote interface).
*   Listing or controlling modulator parameters or sources.
*   Real-time event streaming of device changes.
