# WigAI Project Overview

## Executive Summary

**WigAI** is a sophisticated Bitwig Studio extension that bridges creative music production with AI capabilities through the Model Context Protocol (MCP). The extension enables AI agents to interact with Bitwig's rich API, controlling tracks, devices, scenes, and clips programmatically.

**Project Type:** Bitwig Studio Extension (Backend/Java)  
**Repository Structure:** Monolithic Java/Gradle project  
**Primary Language:** Java 21 (LTS)  
**Build System:** Gradle (Kotlin DSL)  
**Target Platform:** Bitwig Studio 12+  

## Architecture Overview

### High-Level Components

```
WigAI Extension
├── MCP Server Layer (Model Context Protocol)
│   ├── McpServerManager - Orchestrates MCP server lifecycle
│   ├── Tool Registry - Dynamically registers available tools
│   └── Error Handling - Standardized error responses
│
├── HTTP/Server Layer
│   ├── JettyServerManager - Embedded HTTP server (SSE/Streamable HTTP)
│   └── Servlet Infrastructure - Request routing
│
├── Bitwig Integration Layer
│   ├── BitwigApiFacade - Abstraction over Bitwig's complex API
│   ├── Feature Controllers - Domain-specific controllers
│   │   ├── ClipSceneController
│   │   ├── DeviceController
│   │   ├── TransportController
│   │   └── SceneBankFacade
│   └── ControllerHost Integration - Direct API access
│
├── Configuration System
│   ├── ConfigManager (Interface) - Configuration contract
│   └── PreferencesBackedConfigManager - Bitwig preferences integration
│
└── Common Infrastructure
    ├── Logger - Unified logging
    ├── Error Handling - Exception hierarchy
    ├── Validation - Input validation utilities
    └── Data Models - Shared data structures
```

### Component Responsibilities

| Component | Purpose | Key Files |
|-----------|---------|-----------|
| **MCP Server** | Implements Model Context Protocol spec; exposes tools to AI agents | `mcp/` directory |
| **HTTP Server** | Hosts MCP endpoints via SSE (Server-Sent Events) | `server/JettyServerManager.java` |
| **Bitwig Facade** | Simplifies complex Bitwig API interactions; handles threading constraints | `bitwig/BitwigApiFacade.java` |
| **Feature Controllers** | Domain-specific business logic for clips, devices, scenes, transport | `features/` directory |
| **Config Manager** | Manages extension settings and Bitwig preferences integration | `config/` directory |
| **Extension Lifecycle** | Bootstrap, initialization, cleanup | `WigAIExtension.java` |

## Technology Stack

| Category | Technology | Version | Purpose |
|----------|-----------|---------|---------|
| **Language** | Java | 21 LTS | Mandated by Bitwig API |
| **Build** | Gradle | 8.x | Dependency management |
| **Core API** | Bitwig Extension API | 19 | DAW integration |
| **Protocol** | MCP Java SDK | 0.11.0+ | AI agent communication |
| **Server** | Jetty 11 | 11.0.20 | Embedded HTTP server |
| **Servlet API** | Jakarta EE | 6.0.0 | Web standards compliance |
| **Testing** | JUnit Jupiter | 5.10.0 | Unit testing framework |
| **Packaging** | Shadow JAR + .bwextension | N/A | Bitwig extension format |

## Key Features

### Model Context Protocol (MCP) Integration
- Full MCP 0.11.0 compliance
- Dynamic tool registration system
- JSON-RPC 2.0 message handling
- Streaming responses via SSE (Streamable HTTP planned)

### Bitwig Studio Integration
- Thread-safe API access (respects Bitwig's single-threaded constraint)
- Controllers for: Tracks, Clips, Scenes, Devices, Transport
- Real-time state access and manipulation
- Error recovery and validation

### Configuration & Preferences
- Seamless Bitwig preferences integration
- Runtime configuration changes
- Observer pattern for config updates

### Error Handling
- Standardized error codes (e.g., `BITWIG_API_ERROR`, `INVALID_PARAMETER`)
- Proper HTTP status mapping
- Detailed error logging

## Development Workflow

### Prerequisites
- Java 21 JDK installed
- Gradle 8.x (wrapper included)
- Bitwig Studio 12+ for testing
- IDE: IntelliJ IDEA or VS Code (with Java extensions)

### Building
```bash
./gradlew build          # Build and create .bwextension
./gradlew shadowJar      # Create fat JAR with dependencies
./gradlew bwextension    # Create Bitwig extension package
```

### Testing
```bash
./gradlew test           # Run JUnit tests
./gradlew check          # Run all checks (tests + linting)
```

### Extension Installation
The `.bwextension` file (created in `build/extensions/`) is copied to:
- **macOS:** `~/Library/Application Support/Bitwig Studio/Extensions/`
- **Windows:** `%APPDATA%\Bitwig Studio\Extensions\`
- **Linux:** `~/.config/Bitwig Studio/Extensions/`

## Project Status

### Completed
- ✅ Core MCP server implementation
- ✅ Bitwig API integration layer
- ✅ Transport control features
- ✅ Clip/Scene management
- ✅ Device parameter access
- ✅ Track information queries
- ✅ Comprehensive test coverage
- ✅ Full architecture documentation

### In Progress / Planned
- 🔄 Advanced device chain manipulation
- 🔄 Mixer scene automation
- 🔄 Performance optimization
- 🔄 Extended error scenarios handling

## Documentation Map

### Architecture & Design
- [**Architecture** (architecture.md)](../architecture.md) - System design and patterns
- [**Component Architecture Deep Dive** (component-architecture-deep-dive.md)](./component-architecture-deep-dive.md) - Detailed component breakdown
- [**Component View** (component-view.md)](./component-view.md) - Component interactions
- [**Sequence Diagrams** (sequence-diagrams.md)](./sequence-diagrams.md) - Key workflows

### API & Integration
- [**API Reference** (api-reference.md)](./api-reference.md) - Full MCP API specification
- [**MCP Tools Reference** (mcp-tools-reference.md)](../mcp-tools-reference.md) - Tool implementations
- [**Data Models** (data-models.md)](./data-models.md) - Request/response structures

### Requirements & Planning
- [**Product Requirements** (prd/)](../sprint-artifacts/archive/cycle-1-2025-12-15/index.md) - Complete PRD by epic (archived from cycle 1)
- [**Project Brief** (project-brief.md)](../project-brief.md) - High-level project overview
- [**Epics & Stories** (stories/)](../sprint-artifacts/archive/cycle-1-2025-12-15/) - Implementation stories (archived from cycle 1)

### Development
- [**Tech Stack** (tech-stack.md)](./tech-stack.md) - Technology decisions and justification
- [**Project Structure** (project-structure.md)](./project-structure.md) - Directory layout
- [**Operational Guidelines** (operational-guidelines.md)](./operational-guidelines.md) - Coding standards
- [**Environment Variables** (environment-vars.md)](./environment-vars.md) - Configuration guide
- [**Deployment** (infra-deployment.md)](./infra-deployment.md) - Distribution and deployment
- [**Semantic Versioning** (semantic-versioning-guide.md)](./semantic-versioning-guide.md) - Version management

### Workflow Tracking
- [**Workflow Status** (bmm-workflow-status.yaml)](./bmm-workflow-status.yaml) - BMM methodology progress

## Getting Started

### For New Developers
1. **Read:** [Project Brief](../project-brief.md) for context
2. **Review:** [Architecture](../architecture.md) for system understanding
3. **Setup:** Follow build instructions above
4. **Explore:** Check [archived stories](../sprint-artifacts/archive/cycle-1-2025-12-15/) for completed work items

### For Feature Development
1. **Check:** [API Reference](./api-reference.md) for existing endpoints
2. **Review:** Related story in [archived stories](../sprint-artifacts/archive/cycle-1-2025-12-15/)
3. **Reference:** [Component Architecture Deep Dive](./component-architecture-deep-dive.md) for implementation patterns
4. **Test:** Add tests following patterns in `src/test/java/`

### For API Integration
1. **Start:** [MCP Tools Reference](../mcp-tools-reference.md)
2. **Understand:** [Data Models](./data-models.md) for request/response formats
3. **Review:** [Sequence Diagrams](./sequence-diagrams.md) for interaction flows

## Key References

- **Bitwig Extension API:** https://resources.bitwig.com/studios/controller-api/
- **Model Context Protocol:** https://modelcontextprotocol.io/
- **MCP Java SDK:** https://github.com/modelcontextprotocol/java-sdk
- **Jakarta EE:** https://jakarta.ee/

---

**Last Updated:** 2025-12-13  
**Generated by:** BMM Document Project Workflow (Full Rescan)  
**Status:** Active Development
