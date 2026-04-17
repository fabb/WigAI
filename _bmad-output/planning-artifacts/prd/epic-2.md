# Epic 2: Device Parameter Read and Write Implementation

**Goal:** Enable reading and writing of the eight parameters for the currently selected Bitwig device via MCP commands, allowing the AI agent to both query device state and modify it. This fulfills PRD Objective 4.
**Architectural Alignment:** This epic utilizes the `RequestRouter`, `McpCommandParser`, a new `DeviceController` feature module, the `BitwigApiFacade`, and the `Logger`. It adheres to `docs/api-reference.md` for command/response structures and `docs/data-models.md` for DTOs.

## Story List

### Refined Story 2.1: Read Selected Device's Parameters via MCP
* **User Story / Goal:** As an External AI Agent, I want to request the current names and values of the eight addressable parameters of the user-selected device in Bitwig via an MCP command, so that I can understand the device's state for display or further manipulation.
* **Detailed Requirements:**
    * The WigAI MCP server (via `RequestRouter`, `McpCommandParser`, and the `DeviceController` feature module) must handle the "get_selected_device_parameters" command.
    * The command structure is `{"command": "get_selected_device_parameters"}` with no additional payload, as per `docs/api-reference.md`.
    * The `DeviceController` will use the `BitwigApiFacade` to:
        * Identify the currently selected device in Bitwig.
        * Access its primary macro parameter bank (typically 8 parameters).
        * For each of the 8 parameters, retrieve its name (if available), current normalized value (0.0-1.0), and its display value (string).
    * Return an MCP success response as specified in `docs/api-reference.md` and `docs/data-models.md` (structure `GetDeviceParametersResponseData` containing `device_name` and a list of `ParameterInfo` objects).
        * Example: `{"status": "success", "data": {"device_name": "Poly Synth", "parameters": [{"index": 0, "name": "OSC1 Shape", "value": 0.75, "display_value": "75.0 %"}, ...]}}`
    * If no device is selected, or the selected device has no such parameters, the `parameters` list in the response should be empty, and `device_name` should be `null`. The overall MCP status should still be "success" indicating the command was processed.
    * Log the command, action, and response.
* **Acceptance Criteria (ACs):**
    * AC1: Sending the "get_selected_device_parameters" MCP command returns an MCP response with `status: "success"` and a `data` payload containing the `device_name` and an array of `parameters` (each with `index`, `name`, `value`, `display_value`) for the currently selected Bitwig device.
    * AC2: The parameter names, values, and display values in the response accurately reflect their current state in Bitwig's selected device.
    * AC3: If a device has fewer than 8 addressable parameters, only the available ones are returned, or null/empty placeholders are used for the remaining as per Bitwig API behavior (the facade should normalize this).
    * AC4: If no device is selected in Bitwig, the response data contains `device_name: null` and `parameters: []`.
    * AC5: The `Logger` service logs the command receipt and the summarized response.

---

### Refined Story 2.2: Set a Single Parameter Value for Selected Device via MCP
* **User Story / Goal:** As an External AI Agent, I want to set a specific value for a single parameter (by its index 0-7) of the user-selected device in Bitwig via an MCP command, so that I can make precise adjustments.
* **Detailed Requirements:**
    * The WigAI MCP server (via `RequestRouter`, `McpCommandParser`, and the `DeviceController` feature module) must handle the "set_selected_device_parameter" command.
    * The command structure and payload (`SetDeviceParameterPayload` containing `parameter_index` and `value`) must adhere to `docs/api-reference.md` and `docs/data-models.md`.
        * Example request payload: `{"parameter_index": 0, "value": 0.65}`
    * The `McpCommandParser` must validate:
        * `parameter_index` is an integer between 0 and 7 (inclusive).
        * `value` is a float/double between 0.0 and 1.0 (inclusive).
    * The `DeviceController` will use the `BitwigApiFacade` to:
        * Identify the currently selected device in Bitwig.
        * Set the specified parameter at the given index to the provided normalized value.
    * Return an MCP success response as specified in `docs/api-reference.md` and `docs/data-models.md` (structure `SetDeviceParameterResponseData`).
        * Example: `{"status": "success", "data": {"action": "parameter_set", "parameter_index": 0, "new_value": 0.65, "message": "Parameter 0 set to 0.65."}}`
    * Handle errors gracefully:
        * If no device is selected, return MCP error `DEVICE_NOT_SELECTED`.
        * If `parameter_index` is invalid, return MCP error `INVALID_PARAMETER_INDEX`.
        * If `value` is out of range (0.0-1.0), return MCP error `INVALID_PARAMETER`.
        * If the Bitwig API call fails, return MCP error `BITWIG_ERROR`.
    * Log the command, action, and response/errors.
* **Acceptance Criteria (ACs):**
    * AC1: Sending the "set_selected_device_parameter" MCP command with a valid `parameter_index` (0-7) and `value` (0.0-1.0) changes the corresponding parameter of the selected device in Bitwig.
    * AC2: WigAI returns a successful MCP response matching the specified JSON structure, confirming the change.
    * AC3: Sending the command with an invalid `parameter_index` (e.g., 8, -1) results in an MCP error response with code `INVALID_PARAMETER_INDEX` and no change in Bitwig.
    * AC4: Sending the command with a `value` outside the 0.0-1.0 range results in an MCP error response with code `INVALID_PARAMETER` and no change in Bitwig.
    * AC5: Attempting to set a parameter if no device is selected in Bitwig results in an MCP error response with code `DEVICE_NOT_SELECTED`.
    * AC6: The `Logger` service logs the command, any parameter validation issues, the invocation of the Bitwig parameter set action, and the response or error.

---

### Refined Story 2.3: Set Multiple Parameter Values for Selected Device via MCP
* **User Story / Goal:** As an External AI Agent, I want to set multiple parameter values (by index 0-7) of the user-selected device in Bitwig simultaneously via a single MCP command, so that I can apply complex changes efficiently.
* **Detailed Requirements:**
    * The WigAI MCP server (via `RequestRouter`, `McpCommandParser`, and the `DeviceController` feature module) must handle the "set_multiple_selected_device_parameters" command.
    * The command structure and payload (`SetMultipleDeviceParametersPayload` containing a list of `ParameterSetting` objects, each with `parameter_index` and `value`) must adhere to `docs/api-reference.md` and `docs/data-models.md`.
        * Example request payload: `{"parameters": [{"parameter_index": 0, "value": 0.25}, {"parameter_index": 1, "value": 0.80}]}`
    * The `McpCommandParser` must validate for each item in the "parameters" array:
        * `parameter_index` is an integer between 0 and 7 (inclusive).
        * `value` is a float/double between 0.0 and 1.0 (inclusive).
    * The `DeviceController` will use the `BitwigApiFacade` to:
        * Identify the currently selected device in Bitwig.
        * Iterate through the provided parameter changes and apply each valid one.
    * Return an MCP success response as specified in `docs/api-reference.md` and `docs/data-models.md` (structure `SetMultipleDeviceParametersResponseData` containing a list of `ParameterSettingResult` objects).
        * This response should detail the outcome of each requested parameter change (success or error with code/message for that specific parameter).
        * Example: `{"status": "success", "data": {"action": "multiple_parameters_set", "results": [{"parameter_index": 0, "status": "success", "new_value": 0.25}, ...]}}`
    * Handle errors gracefully:
        * If no device is selected, return a top-level MCP error `DEVICE_NOT_SELECTED`. (No partial processing).
        * If the overall payload structure is invalid (e.g., "parameters" array is missing), return MCP error `INVALID_PARAMETER`.
        * For individual parameter settings within the array:
            * If a `parameter_index` is invalid, report it as an error for that item in the `results` array with code `INVALID_PARAMETER_INDEX`, but attempt to process other valid items in the batch.
            * If a `value` is out of range, report it as an error for that item with code `INVALID_PARAMETER`.
            * If a Bitwig API call fails for a specific parameter, report it with `BITWIG_ERROR` for that item.
    * Log the command, actions taken for each parameter, and the overall response/errors.
* **Acceptance Criteria (ACs):**
    * AC1: Sending the "set_multiple_selected_device_parameters" MCP command with a list of valid parameter indices and values changes all corresponding parameters of the selected device in Bitwig.
    * AC2: WigAI returns an MCP response with `status: "success"` and a `data.results` array detailing the individual outcome (success or error code/message) of each requested parameter change, as per `docs/api-reference.md`.
    * AC3: If an invalid index or value is included for one item in the "parameters" list, that specific parameter change fails (and is reported with its error status in the `results` array), but other valid changes in the same command are still successfully applied to Bitwig.
    * AC4: Attempting to set parameters if no device is selected in Bitwig results in a top-level MCP error response with code `DEVICE_NOT_SELECTED` (before processing individual items).
    * AC5: The `Logger` service logs the command, the processing of each parameter update, and the response.

## Change Log

| Change                                | Date       | Version | Description                             | Author              |
| ------------------------------------- | ---------- | ------- | --------------------------------------- | ------------------- |
| Initial Draft                         | 2025-05-16 | 0.1     | First draft of Epic 2 stories           | 2-pm BMAD v2        |
| Architectural Refinement of Stories   | 2025-05-16 | 0.2     | Aligned stories with defined architecture | 3-architect BMAD v2 |
