# MCP Endpoints Verification Guide

This document provides instructions for testing and verifying the MCP server endpoints implemented in WigAI.

## Prerequisites

- WigAI extension running in Bitwig Studio
- `curl` command-line tool for sending HTTP requests
- JQ (optional, for prettier JSON formatting): `brew install jq` on macOS

## Testing MCP Endpoints

### 1. Verify the `tools/list` Endpoint

The `tools/list` endpoint should return a list of available tools the server supports.

```bash
curl -X POST http://localhost:61169/mcp \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":1,"method":"tools/list"}' | jq
```

Expected response format:
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
      }
      // Other tools will appear here as they are added
    ]
  }
}
```

### 2. Verify the `tools/call` Endpoint with the "ping" Tool

The `tools/call` endpoint should allow calling the "ping" tool and receive a "pong" response with the current WigAI version.

```bash
curl -X POST http://localhost:61169/mcp \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":1,"method":"tools/call","params":{"name":"ping","arguments":{}}}' | jq
```

Expected response format:
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

### 3. Test Error Cases

#### Invalid Method
```bash
curl -X POST http://localhost:61169/mcp \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":1,"method":"invalid_method"}' | jq
```

Expected: Method not found error.

#### Invalid Tool Name
```bash
curl -X POST http://localhost:61169/mcp \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":1,"method":"tools/call","params":{"name":"nonexistent_tool","arguments":{}}}' | jq
```

Expected: Tool not found error.

#### Malformed JSON
```bash
curl -X POST http://localhost:61169/mcp \
  -H "Content-Type: application/json" \
  -d '{malformed json}' | jq
```

Expected: Parse error.

### 4. Test the `list_scenes` Tool

The `list_scenes` tool should return all scenes in the current project with their details.

```bash
curl -X POST http://localhost:61169/mcp \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":1,"method":"tools/call","params":{"name":"list_scenes","arguments":{}}}' | jq
```

Expected response format:
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "content": [
      {
        "type": "text",
        "text": "{\"status\":\"success\",\"data\":[{\"index\":0,\"name\":\"Intro\",\"color\":\"rgb(255,128,0)\"},{\"index\":1,\"name\":\"Verse\",\"color\":\"rgb(0,180,255)\"},{\"index\":2,\"name\":\"Chorus\",\"color\":null}]}"
      }
    ],
    "isError": false
  }
}
```

#### Test Cases for `list_scenes`:

1. **Empty Project**: Create a new empty project in Bitwig and verify the response is an empty array:
   ```bash
   # Expected: {"status":"success","data":[]}
   ```

2. **Multiple Scenes with Colors**: Create several scenes with different colors in Bitwig and verify:
   - All scenes are returned in correct order (index 0, 1, 2, ...)
   - Scene names match exactly what's shown in Bitwig
   - Colors are formatted as `rgb(r,g,b)` strings with values 0-255
   - Scenes without colors show `color: null`

3. **Scene Selection**:
   - The tool no longer returns scene selection information
   - This simplifies the API and focuses on structural information only

4. **Large Number of Scenes**: Create a project with many scenes (>10) and verify:
   - All scenes are returned (not just visible ones)
   - Pagination is handled correctly
   - Performance is reasonable

#### Manual Testing Setup:

1. **Create Test Project in Bitwig**:
   - Open Bitwig Studio
   - Create a new project
   - Add at least 3 scenes with different names ("Intro", "Verse", "Chorus")
   - Set different colors for scenes (right-click on scene → Set Color)
   - Leave one scene without a color

2. **Test Scene Selection**:
   - Scene selection is no longer tracked or returned by this tool
   - Focus on verifying scene names and colors are correctly returned

3. **Verify Field Values**:
   - `index`: Should be 0-based, sequential, matching scene order in Bitwig
   - `name`: Should exactly match scene names as displayed in Bitwig
   - `color`: Should be `rgb(r,g,b)` format or `null` for scenes without colors

### 5. Check Log Messages

After running these tests, check the Bitwig extension console to verify that appropriate log messages are being generated for each request and response.

## Troubleshooting

- If connections are refused, make sure the WigAI extension is loaded in Bitwig Studio and that the MCP server is running.
- Check the port configuration (default: 61169) if you can't connect to the server.
- Verify that the WigAI extension console shows log messages when requests are made.
