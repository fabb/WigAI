# Epic 4: Extension UI Settings Integration

**Goal:** Replace the hardcoded ConfigManager with Bitwig Studio's Preferences API integration, providing users with a GUI for configuring MCP server settings and displaying connection information. This enhances usability by moving configuration from code to Bitwig's Controller Preferences panel.

**Architectural Alignment:** This epic modifies the `WigAIExtension`, `ConfigManager`, and `McpServerManager` components to integrate with Bitwig's `Preferences` API. It maintains backward compatibility while adding UI exposure and real-time configuration updates.

## Story List

### Story 4.1: Replace ConfigManager with Bitwig Preferences UI Integration

**User Story:** As a WigAI user, I want to configure MCP server settings (host and port) through Bitwig Studio's Controller Preferences panel, so that I can easily adjust network settings and see the connection URL without editing code.

**Detailed Requirements:**

**Phase 1: Interface Extraction & Migration Preparation**
* Extract `ConfigManager` interface from the current `ConfigManager` class to enable polymorphic implementation
* Interface must include methods: `getMcpHost()`, `getMcpPort()`, `setMcpHost(String)`, `setMcpPort(int)`, and `addObserver(ConfigChangeObserver)`
* Remove current `ConfigManager` class (replaced by new preferences-backed implementation)
* Ensure clean migration to new configuration system

**Phase 2: Bitwig Preferences Integration**
* Implement `PreferencesBackedConfigManager` that implements `ConfigManager`
* Create Bitwig preferences settings in "Network Settings" category:
  - **MCP Host**: String setting (max 50 chars, default "localhost")  
  - **MCP Port**: Number setting (range 1024-65535, default 61169)
  - **Connection URL**: Read-only info display showing dynamic URL (e.g., "http://localhost:61169/sse")
* Settings must appear in Bitwig Studio → Preferences → Controllers → WigAI
* Include brief instructional text: "Configure MCP server connection. Use the Connection URL below to connect external AI agents to WigAI."

**Phase 3: Observer Pattern Implementation**
* Create `ConfigChangeObserver` interface with methods: `onHostChanged(String oldHost, String newHost)` and `onPortChanged(int oldPort, int newPort)`
* Implement observer registration/notification system in `PreferencesBackedConfigManager`
* Modify `McpServerManager` to implement `ConfigChangeObserver` interface
* Add graceful MCP server restart logic when host or port changes via preferences
* Implement real-time connection URL updates when host/port settings change

**Phase 4: WigAIExtension Integration**
* Modify `WigAIExtension.init()` to instantiate `PreferencesBackedConfigManager` instead of legacy `ConfigManager`
* Add observer registration: `configManager.addObserver(mcpServerManager)`
* Ensure constructor signature change: `new PreferencesBackedConfigManager(logger, host)`
* Maintain all existing functionality and method calls unchanged

**Phase 5: Connection Info Display Enhancement**
* Dynamic connection URL must update immediately when host or port changes
* URL format: `http://{host}:{port}/sse` (matching mcp.json structure)
* Display as read-only field in preferences UI
* Include helpful text explaining how to use this URL with MCP clients

**Phase 6: Help Documentation Integration**
* Update `WigAIExtensionDefinition.getHelpFilePath()` to return GitHub README URL
* Bitwig Studio will automatically provide "Help" button in Controller Preferences panel
* Help button will open the project's GitHub README.md in user's default browser
* Ensures users always access the most up-to-date documentation and project information

**Technical Implementation Details:**

**ConfigChangeObserver Interface:**
```java
public interface ConfigChangeObserver {
    void onHostChanged(String oldHost, String newHost);
    void onPortChanged(int oldPort, int newPort);
}
```

**ConfigManager Interface:**
```java
public interface ConfigManager {
    String getMcpHost();
    int getMcpPort();
    void setMcpHost(String host);
    void setMcpPort(int port);
    void addObserver(ConfigChangeObserver observer);
}
```

**Bitwig Preferences Setup:**
```java
// In PreferencesBackedConfigManager constructor
Preferences preferences = host.getPreferences();

SettableStringValue hostSetting = preferences.getStringSetting(
    "MCP Host", "Network Settings", 50, "localhost");
    
SettableRangedValue portSetting = preferences.getNumberSetting(
    "MCP Port", "Network Settings", 1024, 65535, 1, "", 61169);
    
// Read-only connection info display
preferences.getStringSetting("Connection URL", "Network Settings", 
    200, buildConnectionUrl()).setEnabled(false);
```

**Help Documentation Integration:**
```java
// In WigAIExtensionDefinition.java
@Override
public String getHelpFilePath() {
    return "https://github.com/fabb/WigAI/blob/main/README.md";
}
```

**MCP Server Restart Logic:**
* When settings change, gracefully stop current MCP server
* Start new MCP server with updated host/port configuration
* Log restart events for debugging
* Handle restart failures gracefully with appropriate error logging

**Acceptance Criteria:**

* **AC1**: Bitwig Studio Controller Preferences panel displays "Network Settings" section for WigAI with MCP Host and MCP Port input fields
* **AC2**: Settings default to "localhost" and 61169 respectively, matching current AppConstants
* **AC3**: Connection URL field displays current connection string (e.g., "http://localhost:61169/sse") and updates immediately when host or port changes
* **AC4**: Changing MCP host or port in preferences triggers graceful MCP server restart with new settings
* **AC5**: All existing ConfigManager method calls (`getMcpHost()`, `getMcpPort()`) continue working without modification
* **AC6**: Settings persist across Bitwig Studio sessions
* **AC7**: Invalid host/port values are handled gracefully with appropriate validation and fallback to defaults
* **AC8**: Logger records all settings changes and MCP server restart events
* **AC9**: UI includes brief instructional text about MCP connection usage
* **AC10**: Help button is automatically provided by Bitwig Studio in Controller Preferences panel
* **AC11**: Help button opens GitHub README.md in user's default browser for up-to-date documentation

**Technical Components Modified:**
- `io.github.fabb.wigai.config.ConfigManager` (new interface)
- `io.github.fabb.wigai.config.PreferencesBackedConfigManager` (new implementation)
- `io.github.fabb.wigai.config.ConfigChangeObserver` (new interface)
- `io.github.fabb.wigai.config.ConfigManager` (removed - replaced by PreferencesBackedConfigManager)
- `io.github.fabb.wigai.mcp.McpServerManager` (modified to implement observer)
- `io.github.fabb.wigai.WigAIExtension` (modified initialization)

**Testing Requirements:**
- Unit tests for PreferencesBackedConfigManager observer notifications
- Integration tests for settings persistence and MCP server restart
- Manual testing of Bitwig preferences UI functionality
- Validation of connection URL format and real-time updates
- Test help documentation access via Bitwig's Help button
- Validate documentation content accuracy and completeness

---

### Story 4.2: Replace Static String Settings with Bitwig Notification System

**User Story:** As a WigAI user, I want to receive dynamic popup notifications about MCP server status and connection information instead of static text fields in preferences, so that I get real-time visual feedback about the extension's state without cluttering the preferences UI.

**Detailed Requirements:**

**Phase 1: Remove Static String Settings**
* Remove the static `"Connection URL"` string setting from `PreferencesBackedConfigManager`
* Remove the static `"Instructions"` string setting from preferences UI
* Clean up related code for maintaining these static display fields
* Ensure preferences UI becomes more streamlined with only the interactive host/port settings

**Phase 2: Implement Direct Popup Notifications**
* Add direct popup notification calls to `McpServerManager` lifecycle methods
* Use `host.showPopupNotification(String)` method directly for immediate feedback
* Create notification messages for:
  - **MCP Server Started**: "WigAI MCP Server started. Connect AI agents to: http://{host}:{port}/sse"
  - **MCP Server Stopped**: "WigAI MCP Server stopped"
  - **Configuration Changed**: "WigAI MCP Server restarted. Connect AI agents to: http://{host}:{port}/sse"

**Phase 3: Notification Trigger Implementation**
* Modify `McpServerManager` to trigger notifications on server lifecycle events
* Add notification calls to `ConfigChangeObserver` methods (`onHostChanged`, `onPortChanged`)
* Implement startup notification when MCP server initializes successfully
* Add shutdown notification when extension is disabled or server stops
* Ensure notifications appear at appropriate times without being overly verbose

**Phase 4: Notification Content & Timing**
* **Startup Notification**: Show comprehensive message with connection info when MCP server starts successfully
* **Settings Change Notification**: Combined restart + connection info notification when host/port changes and server restarts
* **Shutdown Notification**: Simple message when server stops
* **Error Notifications**: When server fails to start or restart

**Technical Implementation Details:**

**Direct Popup Notification Implementation:**
```java
// In McpServerManager - single comprehensive notification approach
private void notifyServerStarted() {
    String connectionUrl = String.format("http://%s:%d/sse", 
        configManager.getMcpHost(), configManager.getMcpPort());
    String message = String.format("WigAI MCP Server started. Connect AI agents to: %s", 
        connectionUrl);
    host.showPopupNotification(message);
}

private void notifyServerRestarted() {
    String connectionUrl = String.format("http://%s:%d/sse", 
        configManager.getMcpHost(), configManager.getMcpPort());
    String message = String.format("WigAI MCP Server restarted. Connect AI agents to: %s", 
        connectionUrl);
    host.showPopupNotification(message);
}

private void notifyServerStopped() {
    host.showPopupNotification("WigAI MCP Server stopped");
}
```

**Acceptance Criteria:**

* **AC1**: Static "Connection URL" and "Instructions" string settings are removed from Bitwig preferences UI
* **AC2**: Preferences panel shows only interactive MCP Host and MCP Port fields (cleaner UI)
* **AC3**: Dynamic popup notification appears when MCP server starts, showing current connection info
* **AC4**: Brief notification appears when MCP server restarts due to host/port changes
* **AC5**: Notifications include properly formatted connection URL (e.g., "http://localhost:61169/sse")
* **AC6**: Notifications do not interfere with normal Bitwig Studio operation
* **AC7**: Notification content is concise and informative without being verbose
* **AC8**: Notifications appear immediately when triggered without dependency on notification settings
* **AC9**: Server startup/shutdown events trigger appropriate notifications
* **AC10**: All existing functionality continues to work unchanged (host/port settings, server restart logic)

**Technical Components Modified:**
- `io.github.fabb.wigai.config.PreferencesBackedConfigManager` (remove static settings)
- `io.github.fabb.wigai.mcp.McpServerManager` (add direct popup notification calls)
- `io.github.fabb.wigai.WigAIExtension` (pass host reference to McpServerManager for notifications)

**Testing Requirements:**
- Unit tests for popup notification triggers in server lifecycle methods
- Integration tests for notification timing and content accuracy
- Manual testing of popup notification behavior and timing
- Validation that existing preferences and server restart functionality remains unchanged
- Test direct popup notifications work reliably across different scenarios

---

## Change Log

| Change                                | Date       | Version | Description                             | Author              |
| ------------------------------------- | ---------- | ------- | --------------------------------------- | ------------------- |
| Initial Epic 4 Creation              | 2025-05-29 | 0.1     | Comprehensive settings UI integration story | Mo (Architect)      |
| Help Documentation Integration        | 2025-05-29 | 0.2     | Added help documentation using getHelpFilePath() API | GitHub Copilot      |
| Added Story 4.2                      | 2025-05-29 | 0.3     | Replace static string settings with Bitwig notification system | Curly (PO)          |
