# Agent Instructions for WigAI Project

For details refer to these documents:

-   [Project Brief](/_bmad-output/planning-artifacts/project-brief.md)
-   [PRD](/_bmad-output/planning-artifacts/prd.md)
-   [Architecture](/_bmad-output/planning-artifacts/architecture.md)

## Commands

```bash
./gradlew build        # compile + shadowJar + WigAI.bwextension → build/extensions/WigAI.bwextension
./gradlew test         # run unit tests (JUnit Jupiter)
```

## Architecture

Java 21 Gradle project. Source root: `src/main/java/io/github/fabb/wigai/`

| Package | Purpose |
|---------|---------|
| `bitwig/` | `BitwigApiFacade` — wraps all Bitwig Extension API calls |
| `features/` | Domain controllers (`TransportController`, `DeviceController`, `ClipSceneController`) |
| `mcp/tool/` | One class per MCP tool (registered via MCP Java SDK) |
| `mcp/` | `McpServerManager`, `McpErrorHandler` |
| `config/` | `ConfigManager` interface + `PreferencesBackedConfigManager` |
| `common/` | `AppConstants`, `Logger`, error types, validators |
| `server/` | `JettyServerManager` — embedded Jetty 11 |

Entry point: `WigAIExtension.java` / `WigAIExtensionDefinition.java`

## Key Facts

- MCP server runs at `http://localhost:61169/mcp` (default port in `AppConstants.DEFAULT_MCP_PORT`)
- Logging: `host.println()` → visible in Bitwig's extension console (not stdout)
- MCP Java SDK docs: use `context7` tool (also noted in `build.gradle.kts`)
- Bitwig extension API: `com.bitwig:extension-api:19`

## Rules

Never read `bitwig-api-doc-scraper/bitwig-api-documentation.md` fully into context since it's too big. Rather only read parts of it using code search tools.
