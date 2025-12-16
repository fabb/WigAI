# Component Architecture - Deep Dive

This document provides detailed architectural information about WigAI's core components and their interactions.

## Component Hierarchy

```
WigAIExtension (Main Entry Point)
├── Logger (Common)
├── ConfigManager (Configuration)
│   └── PreferencesBackedConfigManager
├── JettyServerManager (HTTP Server)
│   └── ServletHolder (MCP Servlet)
├── McpServerManager (MCP Protocol)
│   ├── BitwigApiFacade (Bitwig Integration)
│   ├── TransportController (Playback Control)
│   ├── DeviceController (Device Parameters)
│   ├── ClipSceneController (Clip/Scene Management)
│   └── [12 MCP Tools]
└── ConfigChangeObserver (Observer Pattern)
```

## Core Components

### 1. WigAIExtension (Main Controller)

**File:** `src/main/java/io/github/fabb/wigai/WigAIExtension.java`

**Responsibility:** Extension lifecycle management and component orchestration.

**Key Responsibilities:**
- Initialize extension when loaded in Bitwig Studio
- Create and own all primary components (Logger, ConfigManager, ServerManagers)
- Start/stop/restart HTTP and MCP servers
- Handle configuration changes
- Graceful shutdown

**Lifecycle:**
1. `init()` - Called when extension is enabled
   - Create Logger
   - Create ConfigManager (Preferences-backed)
   - Create JettyServerManager
   - Create McpServerManager
   - Register as ConfigChangeObserver
   - Start servers

2. `onHostChanged()` / `onPortChanged()` - Handle config changes
   - Trigger graceful server restart
   - Maintain state across restart

3. `exit()` - Called when extension is disabled
   - Stop server (includes MCP server)
   - Clean shutdown

4. `flush()` - GUI updates (currently unused)

**Key Methods:**
- `startServer()` - Create MCP servlet and start Jetty
- `stopServer()` - Stop Jetty and all servlets
- `restartServer()` - Gracefully restart with new config

---

### 2. ConfigManager (Configuration Management)

**Files:** 
- `src/main/java/io/github/fabb/wigai/config/ConfigManager.java` (Interface)
- `src/main/java/io/github/fabb/wigai/config/PreferencesBackedConfigManager.java` (Implementation)

**Responsibility:** Centralized configuration with observer notification.

**Configuration Properties:**
- `mcpHost` - MCP server host (default: "localhost")
- `mcpPort` - MCP server port (default: 8765)

**Observer Pattern:**
- Implements `addObserver(ConfigChangeObserver)`
- Notifies observers when configuration changes:
  - `onHostChanged(oldHost, newHost)`
  - `onPortChanged(oldPort, newPort)`

**Implementation Details:**
- `PreferencesBackedConfigManager` stores config in Bitwig preferences
- Persists settings across Bitwig sessions
- Thread-safe access to configuration values

---

### 3. McpServerManager (MCP Protocol Manager)

**File:** `src/main/java/io/github/fabb/wigai/mcp/McpServerManager.java`

**Responsibility:** Configure MCP protocol server and manage tool registration.

**Architecture:**
- Uses MCP Java SDK with Server-Sent Events (SSE) transport
- HTTP servlet-based implementation
- ObjectMapper for JSON serialization

**Key Methods:**
- `createMcpServlet(endpointPath)` - Configure and return MCP servlet
  - Creates HttpServletStreamableServerTransportProvider
  - Initializes Bitwig API controllers (on first start)
  - Registers all 12 MCP tools
  - Returns ServletHolder for Jetty integration

**Tool Registration:**
The McpServerManager registers 12 MCP tools:

1. **Status Tool** - System status
2. **Transport Tools** - Start/stop playback
3. **Device Parameter Tools** - Get/set device parameters
4. **Device Details Tool** - Retrieve device information
5. **Track Tools** - List tracks, get track details
6. **Device List Tool** - List devices on track
7. **Scene Tools** - List scenes, launch by index/name
8. **Clip Tools** - Launch clips, get clips in scene

**Controller Reuse:**
Controllers are initialized once during first servlet creation to avoid Bitwig API restrictions:
- `BitwigApiFacade` - Bitwig API access layer
- `TransportController` - Playback control
- `DeviceController` - Device parameter management
- `ClipSceneController` - Clip and scene management

---

### 4. BitwigApiFacade (Bitwig Integration Layer)

**File:** `src/main/java/io/github/fabb/wigai/bitwig/BitwigApiFacade.java`

**Responsibility:** Abstraction layer for Bitwig API interactions.

**Key Responsibilities:**
- Encapsulate Bitwig ControllerHost operations
- Provide typed methods for common operations
- Handle Bitwig API idiosyncrasies
- Consistent logging of API interactions

**Key Methods:**
- Transport control: `startTransport()`, `stopTransport()`
- Track management: `getTrackCount()`, `getTrack(index)`
- Device access: `getSelectedDevice()`, `getSelectedDeviceName()`
- Parameter operations: `getSelectedDeviceParameters()`, `setDeviceParameter()`
- Scene/Clip management: `getSceneCount()`, `getScene(index)`
- Project info: `getProjectName()`, `getVersion()`

**Design Pattern:** Facade pattern abstracting complex Bitwig API

---

### 5. Feature Controllers (Domain-Specific Logic)

#### TransportController
**File:** `src/main/java/io/github/fabb/wigai/features/TransportController.java`

**Responsibility:** Encapsulate transport control business logic.

**Methods:**
- `startTransport()` - Start playback
- `stopTransport()` - Stop playback

**Error Handling:**
- Wraps errors in `BitwigApiException` with `TRANSPORT_ERROR` code
- Logs operations for debugging

---

#### DeviceController
**File:** `src/main/java/io/github/fabb/wigai/features/DeviceController.java`

**Responsibility:** Encapsulate device parameter management logic.

**Methods:**
- `getSelectedDeviceParameters()` - Retrieve all parameters
- `setSelectedDeviceParameter(index, value)` - Set single parameter
- `setMultipleDeviceParameters(List<ParameterSetting>)` - Batch set
- `getSelectedDeviceDetails()` - Get device info with parameters

**Data Structures:**
- `DeviceParametersResult` - Device name + parameter list
- `ParameterInfo` - Parameter metadata (index, name, value, range)
- `ParameterSetting` - Parameter update (index, new value)
- `ParameterSettingResult` - Update result (success, new value)

**Error Handling:**
- Validates indices before API calls
- Returns typed exceptions for different error cases
- Logs detailed error information

---

#### ClipSceneController
**File:** `src/main/java/io/github/fabb/wigai/features/ClipSceneController.java`

**Responsibility:** Encapsulate clip and scene management logic.

**Methods:**
- `launchClip(sceneIndex, trackIndex)` - Launch specific clip
- `launchSceneByIndex(sceneIndex)` - Launch scene by index
- `launchSceneByName(sceneName)` - Launch scene by name
- `getClipsInScene(sceneIndex)` - List clips in scene

**Error Handling:**
- Validates indices and scene names
- Handles case-insensitive scene name matching
- Returns detailed error codes for MCP responses

---

### 6. JettyServerManager (HTTP Server Management)

**File:** `src/main/java/io/github/fabb/wigai/server/JettyServerManager.java`

**Responsibility:** HTTP server lifecycle and servlet management.

**Key Methods:**
- `startServer(servletHolder, endpointPath)` - Start Jetty with servlet
- `stopServer()` - Stop Jetty server
- `restartServer(servletHolder, endpointPath)` - Gracefully restart

**Configuration:**
- Uses Jetty embedded server
- Configures port from ConfigManager
- Registers servlets with specified endpoint path
- Handles port binding failures gracefully

---

### 7. Logger (Common Logging)

**File:** `src/main/java/io/github/fabb/wigai/common/Logger.java`

**Responsibility:** Unified logging to Bitwig console.

**Methods:**
- `info(message)` - Info level
- `error(message, exception)` - Error level with exception
- `debug(message)` - Debug level (if enabled)

**Features:**
- Logs to Bitwig Studio console
- Includes timestamp and log level
- Format: `[LEVEL] [timestamp] [class] message`

---

### 8. StructuredLogger (Unified Error Handling)

**File:** `src/main/java/io/github/fabb/wigai/common/logging/StructuredLogger.java`

**Responsibility:** Consistent error handling and logging for MCP tools.

**Features:**
- Wraps Logger with context information
- Provides error code and category
- Standardized error response formatting
- Helps tools implement consistent error handling

---

## Data Flow Diagrams

### Startup Sequence
```
Bitwig Studio loads extension
    ↓
WigAIExtension.init()
    ↓
Create Logger, ConfigManager
    ↓
Create ConfigManager observers (extension itself)
    ↓
Create JettyServerManager, McpServerManager
    ↓
Call startServer()
    ↓
McpServerManager.createMcpServlet()
    → Initialize BitwigApiFacade
    → Create Controllers (Transport, Device, Clip/Scene)
    → Register 12 MCP tools with server
    → Return ServletHolder
    ↓
JettyServerManager.startServer(servletHolder)
    ↓
MCP Server ready on localhost:port
```

### MCP Tool Request Flow
```
Client sends MCP request (JSON-RPC)
    ↓
HttpServletStreamableServerTransport (SSE)
    ↓
MCP Server routes to appropriate tool
    ↓
Tool validates parameters
    ↓
Tool calls Feature Controller
    ↓
Feature Controller calls BitwigApiFacade
    ↓
BitwigApiFacade calls Bitwig ControllerHost
    ↓
Result returned up the chain
    ↓
MCP Server formats response
    ↓
SSE transport sends back to client
```

### Configuration Change Flow
```
User changes MCP host/port in preferences
    ↓
ConfigManager notifies observers
    ↓
WigAIExtension.onHostChanged() / onPortChanged()
    ↓
Call restartServer()
    ↓
Stop Jetty server
    ↓
Create new MCP servlet with new config
    ↓
Start Jetty on new port
    ↓
MCP Server ready on new address
```

## Design Patterns Used

| Pattern | Where | Purpose |
|---------|-------|---------|
| **Facade** | BitwigApiFacade | Simplify Bitwig API access |
| **Observer** | ConfigManager + Extension | Notify on config changes |
| **Singleton** | Controllers | Reuse across tool calls |
| **Dependency Injection** | All components | Flexibility and testability |
| **Strategy** | Feature Controllers | Encapsulate business logic |
| **Adapter** | MCP tools | Adapt domain logic for MCP |

## Concurrency Considerations

- Bitwig API is single-threaded; all API calls must originate from Bitwig thread
- HttpServletStreamableServerTransport handles thread safety
- ConfigManager should be thread-safe (verify in implementation)
- Controllers are stateless (safe for concurrent access)

## Extension Points

- **New Tools:** Add to McpServerManager tool registration
- **New Controllers:** Follow Feature Controller pattern, inject into McpServerManager
- **New Config Options:** Extend ConfigManager interface and PreferencesBackedConfigManager
- **Custom Logging:** Extend Logger class with additional methods
