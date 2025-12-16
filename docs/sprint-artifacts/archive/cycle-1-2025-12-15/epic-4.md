# Epic 4: Extension UI Settings Integration

**Goal:** Replace the hardcoded ConfigManager with Bitwig Studio's Preferences API integration, providing users with a GUI for configuring MCP server settings and displaying connection information. This enhances usability by moving configuration from code to Bitwig's Controller Preferences panel.

**Architectural Alignment:** This epic modifies the `WigAIExtension`, `ConfigManager`, and `McpServerManager` components to integrate with Bitwig's `Preferences` API. It maintains backward compatibility while adding UI exposure and real-time configuration updates.

## Story List

### Story 4.1: Extract Config Interfaces

Refactors the configuration layer so every component depends on a shared `ConfigManager`/`ConfigChangeObserver` interface.  
**Full specification:** `docs/stories/4.1.story.md`

### Story 4.2: Implement Preferences-Backed Configuration

Introduces `PreferencesBackedConfigManager` with Bitwig Controller Preferences UI for MCP host/port editing and connection URL display.  
**Full specification:** `docs/stories/4.2.story.md`

### Story 4.3: Observer Wiring and MCP Server Restart

Adds observer notifications and MCP server restart logic whenever settings change, with robust logging and rollback handling.  
**Full specification:** `docs/stories/4.3.story.md`

### Story 4.4: WigAI Extension Integration & Help Surfacing

Wires the new configuration system into `WigAIExtension`, keeps the connection URL in sync across logs/UI, and ensures Bitwig’s Help button points at the README.  
**Full specification:** `docs/stories/4.4.story.md`

**Technical Implementation Details (Stories 4.1–4.4):**

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

### Story 4.5: Replace Static String Settings with Bitwig Notification System

Removes redundant string settings from the preferences UI and surfaces connection information via Bitwig popup notifications whenever the MCP server starts, stops, or restarts.  
**Full specification:** `docs/stories/4.5.story.md`

---

## Change Log

| Change                                | Date       | Version | Description                             | Author              |
| ------------------------------------- | ---------- | ------- | --------------------------------------- | ------------------- |
| Initial Epic 4 Creation              | 2025-05-29 | 0.1     | Comprehensive settings UI integration story | Mo (Architect)      |
| Help Documentation Integration        | 2025-05-29 | 0.2     | Added help documentation using getHelpFilePath() API | GitHub Copilot      |
| Added Story 4.2                      | 2025-05-29 | 0.3     | Replace static string settings with Bitwig notification system | Curly (PO)          |
