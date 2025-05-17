# WigAI MCP API Specification

Version: 0.2.0
Date: 2025-05-17

## 1. Introduction

This document specifies the Model Context Protocol (MCP) Application Programming Interface (API) for WigAI, a Bitwig Studio extension. WigAI enables external AI agents to interact with Bitwig Studio for various control and query tasks.

This API adheres to the official MCP specification (2025-03-26), utilizing the Streamable HTTP transport and implementing the tools interface. All requests and responses are JSON-RPC 2.0 formatted.

## 2. General Principles

* **Transport:** MCP Streamable HTTP. A single HTTP endpoint will be used for all communication. The server uses Server-Sent Events (SSE) for streaming responses or notifications over this connection.
* **Request Method:** Clients MUST use HTTP POST for sending MCP messages, with the JSON-RPC 2.0 message as the request body.
* **Content Type:** All JSON payloads MUST use `application/json`.
* **Endpoint:** A single configurable endpoint (e.g., `http://localhost:61169/mcp`). The port `61169` ("WIGAI") is the suggested default.
* **MCP Message Structure:**
    WigAI follows the JSON-RPC 2.0 format as specified by the official MCP protocol. All functionality is exposed through the tools interface.

    **Tools/List Request Example:**
    ```json
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "tools/list"
    }
    ```

    **Tools/List Response Example:**
    ```json
    {
      "jsonrpc": "2.0",
      "id": 1,
      "result": {
        "tools": [
          {
            "name": "ping",
            "description": "Verify connectivity and operational status of WigAI",
            "inputSchema": {
              "type": "object",
              "properties": {}
            }
          },
          {
            "name": "transport_start",
            "description": "Start Bitwig's main transport playback",
            "inputSchema": {
              "type": "object",
              "properties": {}
            }
          }
        ]
      }
    }
    ```

    **Tools/Call Request Example:**
    ```json
    {
      "jsonrpc": "2.0",
      "id": 2,
      "method": "tools/call",
      "params": {
        "name": "ping",
        "arguments": {}
      }
    }
    ```
    
    **Tools/Call Response Example:**
    ```json
    {
      "jsonrpc": "2.0",
      "id": 2,
      "result": {
        "content": [
          {
            "type": "text",
            "text": "pong (WigAI v0.2.0)"
          }
        ],
        "isError": false
      }
    }
    ```
    
    **Error Response Example:**
    ```json
    {
      "jsonrpc": "2.0",
      "id": 3,
      "error": {
        "code": -32601,
        "message": "Method not found"
      }
    }
    ```
    *(Note: The MCP Java SDK handles the protocol structure. WigAI implements the tool functionalities on top of this foundation.)*

## 3. Standard Endpoints and Error Handling

### 3.1 Standard MCP Endpoints

WigAI implements the standard MCP endpoints as defined in the official specification:

| Endpoint     | Method     | Description                                             |
| :----------- | :--------- | :------------------------------------------------------ |
| `tools/list` | List all available tools exposed by WigAI                            |
| `tools/call` | Call a specific tool with arguments                                  |

### 3.2 JSON-RPC 2.0 Error Codes

WigAI follows the standard JSON-RPC 2.0 error codes as defined in the MCP specification:

| Error Code | Message               | Description                                     |
| :--------- | :-------------------- | :---------------------------------------------- |
| -32700     | Parse error           | Invalid JSON was received                       |
| -32600     | Invalid request       | The JSON sent is not a valid request object     |
| -32601     | Method not found      | The method does not exist / is not available    |
| -32602     | Invalid params        | Invalid method parameter(s)                     |
| -32603     | Internal error        | Internal JSON-RPC error                         |

### 3.3 Tool-Specific Error Handling

In addition to standard JSON-RPC errors, tools may report execution errors through the `isError: true` field in their responses with specific messages:

| Error Type                | Description                                    | Reported Via Tool Response |
| :------------------------ | :--------------------------------------------- | :------------------------- |
| `BITWIG_ERROR`            | An error occurred during Bitwig API interaction | Tool Result with `isError: true` |
| `DEVICE_NOT_SELECTED`     | Operation requires a device to be selected     | Tool Result with `isError: true` |
| `INVALID_PARAMETER_INDEX` | Parameter index is out of bounds               | Tool Result with `isError: true` |
| `TRACK_NOT_FOUND`         | Specified track name not found                 | Tool Result with `isError: true` |
| `CLIP_INDEX_OUT_OF_BOUNDS`| Clip index out of bounds for the track         | Tool Result with `isError: true` |
| `SCENE_NOT_FOUND`         | Specified scene name or index not found        | Tool Result with `isError: true` |

## 4. WigAI Tools

### 4.1. System Tools

#### 4.1.1. ping

* **Description:** Verifies connectivity and operational status of WigAI.
* **PRD Objective:** N/A (Implicit for server health)
* **Epic Story:** 1.3
* **Tool Schema:**
    ```json
    {
      "name": "ping",
      "description": "Verify connectivity and operational status of WigAI",
      "inputSchema": {
        "type": "object",
        "properties": {}
      }
    }
    ```
* **Request:**
    ```json
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "tools/call",
      "params": {
        "name": "ping",
        "arguments": {}
      }
    }
    ```
* **Success Response (`200 OK`):**
    ```json
    {
      "jsonrpc": "2.0",
      "id": 1,
      "result": {
        "content": [
          {
            "type": "text",
            "text": "pong (WigAI v0.2.0)"
          }
        ],
        "isError": false
      }
    }
    ```

### 4.2. Transport Control Tools

#### 4.2.1. transport_start

* **Description:** Starts Bitwig's main transport playback.
* **PRD Objective:** 2
* **Epic Story:** 1.4
* **Tool Schema:**
    ```json
    {
      "name": "transport_start",
      "description": "Start Bitwig's main transport playback",
      "inputSchema": {
        "type": "object",
        "properties": {}
      }
    }
    ```
* **Request:**
    ```json
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "tools/call",
      "params": {
        "name": "transport_start",
        "arguments": {}
      }
    }
    ```
* **Success Response (`200 OK`):**
    ```json
    {
      "jsonrpc": "2.0",
      "id": 1,
      "result": {
        "content": [
          {
            "type": "text",
            "text": "Bitwig transport started."
          }
        ],
        "isError": false
      }
    }
    ```
* **Error Response:**
    ```json
    {
      "jsonrpc": "2.0",
      "id": 1,
      "result": {
        "content": [
          {
            "type": "text",
            "text": "Failed to start transport: [error details]"
          }
        ],
        "isError": true
      }
    }
    ```

#### 4.2.2. transport_stop

* **Description:** Stops Bitwig's main transport playback.
* **PRD Objective:** 2
* **Epic Story:** 1.5
* **Tool Schema:**
    ```json
    {
      "name": "transport_stop",
      "description": "Stop Bitwig's main transport playback",
      "inputSchema": {
        "type": "object",
        "properties": {}
      }
    }
    ```
* **Request:**
    ```json
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "tools/call",
      "params": {
        "name": "transport_stop",
        "arguments": {}
      }
    }
    ```
* **Success Response (`200 OK`):**
    ```json
    {
      "jsonrpc": "2.0",
      "id": 1,
      "result": {
        "content": [
          {
            "type": "text",
            "text": "Bitwig transport stopped."
          }
        ],
        "isError": false
      }
    }
    ```
* **Error Response:**
    ```json
    {
      "jsonrpc": "2.0",
      "id": 1,
      "result": {
        "content": [
          {
            "type": "text",
            "text": "Failed to stop transport: [error details]"
          }
        ],
        "isError": true
      }
    }
    ```

### 4.3. Device Parameter Tools (Selected Device)

#### 4.3.1. get_selected_device_parameters

* **Description:** Reads the names and values of the eight addressable parameters of the currently selected device in Bitwig.
* **PRD Objective:** 4
* **Epic Story:** 2.1
* **Tool Schema:**
    ```json
    {
      "name": "get_selected_device_parameters",
      "description": "Get parameters of the currently selected device in Bitwig",
      "inputSchema": {
        "type": "object",
        "properties": {}
      }
    }
    ```
* **Request:**
    ```json
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "tools/call",
      "params": {
        "name": "get_selected_device_parameters",
        "arguments": {}
      }
    }
    ```
* **Success Response (`200 OK`):**
    ```json
    {
      "jsonrpc": "2.0",
      "id": 1,
      "result": {
        "content": [
          {
            "type": "text",
            "text": "Device: Poly Synth\nParameters:\n- OSC1 Shape: 75.0% (0.75)\n- Filter Cutoff: 500 Hz (0.5)\n..."
          }
        ],
        "isError": false
      }
    }
    ```
    Or with more structured return data:
    ```json
    {
      "jsonrpc": "2.0",
      "id": 1,
      "result": {
        "content": [
          {
            "type": "resource",
            "resource": {
              "uri": "resource://device-parameters",
              "mimeType": "application/json",
              "data": {
                "device_name": "Poly Synth",
                "parameters": [
                  { "index": 0, "name": "OSC1 Shape", "value": 0.75, "display_value": "75.0 %" },
                  { "index": 1, "name": "Filter Cutoff", "value": 0.5, "display_value": "500 Hz" }
                  // ... up to 8 parameters
                ]
              }
            }
          }
        ],
        "isError": false
      }
    }
    ```
* **Error Response:**
    ```json
    {
      "jsonrpc": "2.0",
      "id": 1,
      "result": {
        "content": [
          {
            "type": "text",
            "text": "Error: No device is currently selected."
          }
        ],
        "isError": true
      }
    }
    ```

#### 4.3.2. set_selected_device_parameter

* **Description:** Sets a single parameter of the currently selected device by its index.
* **PRD Objective:** 4
* **Epic Story:** 2.2
* **Tool Schema:**
    ```json
    {
      "name": "set_selected_device_parameter",
      "description": "Set a single parameter of the currently selected device by its index",
      "inputSchema": {
        "type": "object",
        "properties": {
          "parameter_index": {
            "type": "integer",
            "description": "Index of the parameter (0-7)",
            "minimum": 0,
            "maximum": 7
          },
          "value": {
            "type": "number",
            "description": "Normalized value for the parameter (0.0-1.0)",
            "minimum": 0.0,
            "maximum": 1.0
          }
        },
        "required": ["parameter_index", "value"]
      }
    }
    ```
* **Request:**
    ```json
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "tools/call",
      "params": {
        "name": "set_selected_device_parameter",
        "arguments": {
          "parameter_index": 0,
          "value": 0.65
        }
      }
    }
    ```
* **Success Response (`200 OK`):**
    ```json
    {
      "jsonrpc": "2.0",
      "id": 1,
      "result": {
        "content": [
          {
            "type": "text",
            "text": "Parameter 0 set to 0.65."
          }
        ],
        "isError": false
      }
    }
    ```
* **Error Response:**
    ```json
    {
      "jsonrpc": "2.0",
      "id": 1,
      "result": {
        "content": [
          {
            "type": "text",
            "text": "Error: Invalid parameter index or no device selected."
          }
        ],
        "isError": true
      }
    }
    ```

#### 4.3.3. set_multiple_device_parameters

* **Description:** Sets multiple parameters of the currently selected device simultaneously.
* **PRD Objective:** 4
* **Epic Story:** 2.3
* **Tool Schema:**
    ```json
    {
      "name": "set_multiple_device_parameters",
      "description": "Set multiple parameters of the currently selected device simultaneously",
      "inputSchema": {
        "type": "object",
        "properties": {
          "parameters": {
            "type": "array",
            "description": "List of parameters to change",
            "items": {
              "type": "object",
              "properties": {
                "parameter_index": {
                  "type": "integer",
                  "description": "Index of the parameter (0-7)",
                  "minimum": 0,
                  "maximum": 7
                },
                "value": {
                  "type": "number",
                  "description": "Normalized value for the parameter (0.0-1.0)",
                  "minimum": 0.0,
                  "maximum": 1.0
                }
              },
              "required": ["parameter_index", "value"]
            }
          }
        },
        "required": ["parameters"]
      }
    }
    ```
* **Request:**
    ```json
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "tools/call",
      "params": {
        "name": "set_multiple_device_parameters",
        "arguments": {
          "parameters": [
            { "parameter_index": 0, "value": 0.25 },
            { "parameter_index": 1, "value": 0.80 }
          ]
        }
      }
    }
    ```
* **Success Response (`200 OK`):**
    ```json
    {
      "jsonrpc": "2.0",
      "id": 1,
      "result": {
        "content": [
          {
            "type": "text",
            "text": "Multiple parameters set successfully:\n- Parameter 0: set to 0.25\n- Parameter 1: set to 0.80"
          }
        ],
        "isError": false
      }
    }
    ```
* **Partial Success/Error Response:**
    ```json
    {
      "jsonrpc": "2.0",
      "id": 1,
      "result": {
        "content": [
          {
            "type": "text",
            "text": "Parameters partially set:\n- Parameter 0: set to 0.25\n- Parameter 1: set to 0.80\n- Parameter 8: FAILED (Index out of bounds)"
          }
        ],
        "isError": false
      }
    }
    ```
* **Error Response:**
    ```json
    {
      "jsonrpc": "2.0",
      "id": 1,
      "result": {
        "content": [
          {
            "type": "text",
            "text": "Error: No device selected or invalid parameters format."
          }
        ],
        "isError": true
      }
    }
    ```

### 4.4. Clip and Scene Launching Tools

#### 4.4.1. launch_clip

* **Description:** Launches a specific clip by track name and clip slot index (scene number).
* **PRD Objective:** 3
* **Epic Story:** 3.1
* **Tool Schema:**
    ```json
    {
      "name": "launch_clip",
      "description": "Launch a specific clip by track name and clip slot index",
      "inputSchema": {
        "type": "object",
        "properties": {
          "track_name": {
            "type": "string",
            "description": "Name of the track containing the clip"
          },
          "clip_index": {
            "type": "integer",
            "description": "0-based index of the clip slot (scene)",
            "minimum": 0
          }
        },
        "required": ["track_name", "clip_index"]
      }
    }
    ```
* **Request:**
    ```json
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "tools/call",
      "params": {
        "name": "launch_clip",
        "arguments": {
          "track_name": "Drums",
          "clip_index": 0
        }
      }
    }
    ```
* **Success Response (`200 OK`):**
    ```json
    {
      "jsonrpc": "2.0",
      "id": 1,
      "result": {
        "content": [
          {
            "type": "text",
            "text": "Clip at Drums[0] launched."
          }
        ],
        "isError": false
      }
    }
    ```
* **Error Response:**
    ```json
    {
      "jsonrpc": "2.0",
      "id": 1,
      "result": {
        "content": [
          {
            "type": "text",
            "text": "Error: Track 'Drums' not found or clip index out of bounds."
          }
        ],
        "isError": true
      }
    }
    ```

#### 4.4.2. launch_scene_by_index

* **Description:** Launches an entire scene by its numerical index.
* **PRD Objective:** 3
* **Epic Story:** 3.2
* **Tool Schema:**
    ```json
    {
      "name": "launch_scene_by_index",
      "description": "Launch an entire scene by its numerical index",
      "inputSchema": {
        "type": "object",
        "properties": {
          "scene_index": {
            "type": "integer",
            "description": "0-based index of the scene to launch",
            "minimum": 0
          }
        },
        "required": ["scene_index"]
      }
    }
    ```
* **Request:**
    ```json
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "tools/call",
      "params": {
        "name": "launch_scene_by_index",
        "arguments": {
          "scene_index": 1
        }
      }
    }
    ```
* **Success Response (`200 OK`):**
    ```json
    {
      "jsonrpc": "2.0",
      "id": 1,
      "result": {
        "content": [
          {
            "type": "text",
            "text": "Scene 1 launched."
          }
        ],
        "isError": false
      }
    }
    ```
* **Error Response:**
    ```json
    {
      "jsonrpc": "2.0",
      "id": 1,
      "result": {
        "content": [
          {
            "type": "text",
            "text": "Error: Scene not found at index 1."
          }
        ],
        "isError": true
      }
    }
    ```

#### 4.4.3. launch_scene_by_name

* **Description:** Launches an entire scene by its name.
* **PRD Objective:** 3
* **Epic Story:** 3.3
* **Tool Schema:**
    ```json
    {
      "name": "launch_scene_by_name",
      "description": "Launch an entire scene by its name",
      "inputSchema": {
        "type": "object",
        "properties": {
          "scene_name": {
            "type": "string",
            "description": "Name of the scene to launch (case-sensitive)"
          }
        },
        "required": ["scene_name"]
      }
    }
    ```
* **Request:**
    ```json
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "tools/call",
      "params": {
        "name": "launch_scene_by_name",
        "arguments": {
          "scene_name": "Verse 1"
        }
      }
    }
    ```
* **Success Response (`200 OK`):**
    ```json
    {
      "jsonrpc": "2.0",
      "id": 1,
      "result": {
        "content": [
          {
            "type": "text",
            "text": "Scene 'Verse 1' launched."
          }
        ],
        "isError": false
      }
    }
    ```
* **Error Response:**
    ```json
    {
      "jsonrpc": "2.0",
      "id": 1,
      "result": {
        "content": [
          {
            "type": "text",
            "text": "Error: Scene named 'Verse 1' not found."
          }
        ],
        "isError": true
      }
    }
    ```

## 5. Standard MCP Features

### 5.1. Tool Discovery with tools/list

As per the MCP specification, WigAI supports tool discovery through the standard `tools/list` endpoint:

**Request:**
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "tools/list"
}
```

**Response:**
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "tools": [
      {
        "name": "ping",
        "description": "Verify connectivity and operational status of WigAI",
        "inputSchema": {...}
      },
      {
        "name": "transport_start",
        "description": "Start Bitwig's main transport playback",
        "inputSchema": {...}
      },
      // Other tools...
    ]
  }
}
```

### 5.2. Capabilities Declaration

WigAI declares its supported capabilities in response to capability queries:

```json
{
  "capabilities": {
    "tools": {
      "listChanged": false
    }
  }
}
```

## 6. Future Considerations / Expansion

* Implement notifications from WigAI to the client (e.g., when a device is selected in Bitwig, track changes, etc.) using the SSE aspect of Streamable HTTP.
* Add more discovery mechanisms (e.g., list available tracks, scenes, devices) as additional tools.
* Support tool list change notifications when tools are dynamically added.

## Change Log

| Change                      | Date       | Version | Description                                          | Author              |
| --------------------------- | ---------- | ------- | ---------------------------------------------------- | ------------------- |
| Initial draft              | 2025-05-16 | 0.1.0   | First draft of MCP API commands.                     | 3-architect BMAD v2 |
| Updated to official MCP spec | 2025-05-17 | 0.2.0   | Converted command API to tools-based API per MCP spec. | GitHub Copilot      |