# WigAI Project Structure

This document defines the standard directory and file layout for the WigAI Bitwig Extension project. The structure is based on common Java and Gradle conventions, tailored for a Bitwig extension and optimized for AI-assisted development. The chosen root package name for the Java source code is `io.github.fabb.wigai`.

## Root Directory Layout

```plaintext
WigAI/
├── agents/                     # Agent prompts and instructions
│   ├── architect-agent.md      # Architect agent prompt
│   ├── dev-agent.md            # Developer agent prompt
│   └── other agent prompts...  # Various AI agent prompts
├── ai/                         # AI-related resources
│   └── stories/                # User stories for implementation
├── build/                      # Compiled output and build artifacts (git-ignored)
├── docs/                       # Project documentation
│   ├── architecture.md         # Architecture document
│   ├── mcp-api-spec.md         # MCP API specification
│   ├── project-structure.md    # This file
│   ├── tech-stack.md           # Technology stack document
│   ├── epic1.md, epic2.md...   # Epic documentation
│   └── templates/              # Documentation templates
├── gradle/                     # Gradle wrapper files
├── src/                        # Source code
│   ├── main/                   # Main application source
│   │   ├── java/               # Java source files
│   │   │   └── io/github/fabb/wigai/  # Root package (abbreviated)
│   │   │       ├── WigAIExtension.java         # Main extension class
│   │   │       ├── WigAIExtensionDefinition.java # Extension definition
│   │   │       ├── common/                     # Shared utilities, constants
│   │   │       ├── config/                     # Configuration management
│   │   │       ├── mcp/                        # MCP Server implementation
│   │   │       │   ├── McpServerManager.java   # Manages MCP server lifecycle
│   │   │       │   └── tool/                   # MCP tool implementations
│   │   │       │       ├── StatusTool.java     # Status tool implementation
│   │   │       │       ├── TransportTool.java  # Transport control tools (start/stop)
│   │   │       │       ├── DeviceParamTool.java # Device parameter management tools
│   │   │       │       ├── ClipTool.java       # Clip launching tools
│   │   │       │       ├── SceneTool.java      # Scene launching tools
│   │   │       │       └── BaseTool.java       # Base abstract class for tool implementations
│   │   │       ├── features/                   # Feature modules for Bitwig control
│   │   │       └── bitwig/                     # Bitwig API Facade
│   │   └── resources/          # Resources (e.g., extension metadata)
│   └── test/                   # Test source code
│       ├── java/               # Java test files (mirroring main structure)
│       └── resources/          # Test resources
├── .gitignore                  # Specifies intentionally untracked files for Git
├── build.gradle.kts            # Gradle build script
├── gradlew & gradlew.bat       # Gradle wrapper scripts
├── settings.gradle.kts         # Gradle settings
└── README.md                   # Project overview, setup instructions
````

## Key Directory & File Descriptions

  * **`WigAI/`**: The root directory of the project.
      * **`.github/copilot_instructions.md`**: Provides context and guidelines for GitHub Copilot to improve code suggestions and adherence to project standards. This file can reference key documents in `docs/` and specify the root package `io.github.fabb.wigai`.
      * **`.idea/.junie/guidelines.md`**: (If using IntelliJ IDEA) Provides context and guidelines for the JetBrains AI Assistant (Junie), similar to the Copilot instructions, referencing `io.github.fabb.wigai`.
      * **`.vscode/settings.json`**: Workspace-specific settings for VS Code developers, can include Java formatter preferences, linter settings, etc.
      * **`.gradle/`**: Gradle's internal cache and files.
      * **`build/`**: Output directory for compiled classes, the `.bwextension` file, test reports, and other build artifacts. This directory is typically git-ignored.
      * **`docs/`**: Contains all project documentation.
      * **`gradle/wrapper/`**: Contains the Gradle Wrapper files.
      * **`src/`**: Contains all source code and resources.
          * **`src/main/java/io/github/fabb/wigai/`**: This is where the core Java source code for the extension resides, under the root package `io.github.fabb.wigai`.
              * `WigAIExtension.java`: The main entry point class, extending `ControllerExtension`.
              * `WigAIExtensionDefinition.java`: Defines metadata for the extension, extending `ControllerExtensionDefinition`.
              * `common/`: Utility classes, constants, and shared services like `Logger.java`.
              * `config/`: Configuration management, like `ConfigManager.java`.
              * `mcp/`: Components related to the MCP server.
              * `features/`: Sub-packages for each major feature area.
              * `bitwig/`: Facade for Bitwig API interactions.
          * **`src/main/resources/`**: Non-code resources.
          * **`src/test/java/io/github/fabb/wigai/`**: Source code for unit tests, mirroring the main package structure.
          * **`src/test/resources/`**: Resources for tests.
      * **`.gitignore`**: Standard Git ignore file.
      * **`build.gradle.kts`**: The Gradle build script (Kotlin DSL).
      * **`gradlew` & `gradlew.bat`**: Gradle wrapper executable scripts.
      * **`LICENSE`**: Project's open-source license.
      * **`README.md`**: Project overview and instructions.

## Notes

  * The root package name for all Java source code is **`io.github.fabb.wigai`**.
  * This structure promotes modularity and separation of concerns, making the codebase easier to understand, maintain, test, and for AI assistants to work with effectively.
  * The `build.gradle.kts` file will be crucial for defining how these components are built into the final `.bwextension` file.
  * The contents of `.github/copilot_instructions.md` and `.idea/.junie/guidelines.md` will be developed to point AI assistants to relevant architectural documents, coding standards (using `io.github.fabb.wigai` as the root package), and API specifications within the `docs/` folder.

## Change Log

| Change        | Date       | Version | Description                                       | Author              |
| ------------- | ---------- | ------- | ------------------------------------------------- | ------------------- |
| Initial draft | 2025-05-16 | 0.1     | First draft of project structure                  | 3-architect BMAD v2 |
| Update        | 2025-05-16 | 0.2     | Added AI assistant instruction/guideline files. | 3-architect BMAD v2 |
| Update        | 2025-05-16 | 0.3     | Set root package name to `io.github.fabb.wigai`.  | 3-architect BMAD v2 |
| Update        | 2025-05-18 | 0.4     | Updated for MCP Java SDK integration. Under `mcp/` package, replace `RequestRouter.java` and command handling with `tool/` package containing tool implementations like `PingTool.java`.  | Architect Agent    |
| Update        | 2025-05-18 | 0.5     | Corrected root directory name from "wigai-bitwig-extension" to "WigAI" for consistency. | GitHub Copilot    |
| Update        | 2025-05-18 | 0.6     | Added more detailed descriptions of specific tool implementation files in the MCP tool package. | GitHub Copilot    |
| Update        | 2025-05-18 | 0.7     | Updated tool implementations to replace PingTool with StatusTool to follow the MCP specification which already includes a native ping functionality. | Technical Scrum Master Agent |
