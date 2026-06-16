# Agent Instructions for WigAI Project

For details refer to these documents:

-   [Project Brief](_bmad-output/planning-artifacts/project-brief.md)
-   [PRD](_bmad-output/planning-artifacts/prd/index.md)
-   [Architecture](_bmad-output/planning-artifacts/architecture.md)

## Commands

```bash
# Build requires a real JDK 21 — the system `java` is a JRE (no javac) and no
# Gradle toolchain is configured, so JAVA_HOME must point at a JDK 21 install.
JAVA_HOME=/path/to/jdk-21 ./gradlew build   # → build/extensions/WigAI.bwextension
JAVA_HOME=/path/to/jdk-21 ./gradlew test    # JUnit Jupiter unit tests

# Deploy to the running Bitwig, then toggle the WigAI controller off/on
# (or restart Bitwig) to load the new build:
cp build/extensions/WigAI.bwextension ~/"Bitwig Studio/Extensions/"
```

## Architecture

Java 21 Gradle project. Source root: `src/main/java/io/github/fabb/wigai/`

| Package | Purpose |
|---------|---------|
| `bitwig/` | `BitwigApiFacade` — wraps all Bitwig Extension API calls |
| `features/` | Domain controllers (`TransportController`, `DeviceController`, `ClipSceneController`, `ProjectController`) |
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
- The project targets API **v19**, but the host Bitwig may run a newer level (e.g. v24) — newer host, older compile target.
- **Verify API behavior against a *running* Bitwig, not mocks.** Mocked unit tests validate your *model* of the async push/flush API, not the API itself — a passing mock test has shipped live-broken behavior here before.

## Rules

Never read the scraped API doc (`bitwig-api-doc-scraper/bitwig-api-documentation.md`, a **gitignored, generated** artifact — run the scraper to produce it) fully into context; it's too big. Search it with code-search tools instead.
