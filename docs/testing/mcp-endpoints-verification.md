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

### 4. Check Log Messages

After running these tests, check the Bitwig extension console to verify that appropriate log messages are being generated for each request and response.

## Troubleshooting

- If connections are refused, make sure the WigAI extension is loaded in Bitwig Studio and that the MCP server is running.
- Check the port configuration (default: 61169) if you can't connect to the server.
- Verify that the WigAI extension console shows log messages when requests are made.
