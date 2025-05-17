# WigAI Project Structure

This document defines the standard directory and file layout for the WigAI Bitwig Extension project. The structure is based on common Java and Gradle conventions, tailored for a Bitwig extension and optimized for AI-assisted development. The chosen root package name for the Java source code is `io.github.fabb.wigai`.

## Root Directory Layout

```plaintext
wigai-bitwig-extension/
├── .github/                    # GitHub specific files
│   └── copilot_instructions.md # Instructions and context for GitHub Copilot
├── .gradle/                    # Gradle-specific files (usually git-ignored)
├── .idea/                      # IntelliJ IDEA specific files (if used, git-ignored)
│   └── .junie/                 # JetBrains AI Assistant (Junie) specific settings
│       └── guidelines.md       # Guidelines and context for Junie
├── .vscode/                    # VS Code specific files (if used, git-ignored)
│   └── settings.json           # VS Code workspace settings
├── build/                      # Compiled output and build artifacts (git-ignored)
├── docs/                       # Project documentation (PRD, Architecture, etc.)
│   ├── architecture.md
│   ├── tech-stack.md
│   ├── project-structure.md
│   ├── coding-standards.md
│   ├── mcp-api-spec.md
│   ├── data-models.md
│   ├── environment-vars.md
│   ├── testing-strategy.md
│   ├── prd.md
│   ├── epic1.md
│   ├── epic2.md
│   ├── epic3.md
│   └── project-brief.md
├── gradle/                     # Gradle wrapper files
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── src/                        # Source code
│   ├── main/                   # Main application source
│   │   ├── java/               # Java source files
│   │   │   └── io/             # Root package structure
│   │   │       └── github/
│   │   │           └── fabb/
│   │   │               └── wigai/  # Project root package: io.github.fabb.wigai
│   │   │                   ├── WigaiExtension.java             # Main extension class
│   │   │                   ├── WigaiExtensionDefinition.java   # Extension definition class
│   │   │                   ├── common/                         # Shared utilities, constants
│   │   │                   │   └── Logger.java
│   │   │                   │   └── AppConstants.java
│   │   │                   ├── config/                         # Configuration management
│   │   │                   │   └── ConfigManager.java
│   │   │                   ├── mcp/                            # MCP Server and protocol handling
│   │   │                   │   ├── McpServerManager.java       # Manages MCP server lifecycle
│   │   │                   │   ├── RequestRouter.java
│   │   │                   │   └── command/
│   │   │                   │       └── McpCommandParser.java
│   │   │                   │       └── McpCommands.java        # Enum or constants for command names
│   │   │                   │       └── dto/                    # Data Transfer Objects for commands
│   │   │                   ├── features/                       # Feature-specific modules
│   │   │                   │   ├── transport/
│   │   │                   │   │   └── TransportController.java
│   │   │                   │   ├── device/
│   │   │                   │   │   └── DeviceController.java
│   │   │                   │   └── scene/
│   │   │                   │       └── SceneController.java
│   │   │                   └── bitwig/                         # Bitwig API Facade and interaction
│   │   │                       └── BitwigApiFacade.java
│   │   └── resources/          # Resources (e.g., extension icon - if any)
│   └── test/                   # Test source code
│       ├── java/               # Java test files
│       │   └── io/
│       │       └── github/
│       │           └── fabb/
│       │               └── wigai/  # Mirroring main package structure
│       │                   ├── mcp/
│       │                   │   └── McpCommandParserTest.java
│       │                   └── features/
│       │                       └── transport/
│       │                           └── TransportControllerTest.java
│       └── resources/          # Test resources
├── .gitignore                  # Specifies intentionally untracked files for Git
├── build.gradle.kts            # Gradle build script (Kotlin DSL)
├── gradlew                     # Gradle wrapper script (Linux/macOS)
├── gradlew.bat                 # Gradle wrapper script (Windows)
├── LICENSE                     # Project license file (e.g., MIT, Apache 2.0)
└── README.md                   # Project overview, setup, and usage instructions
````

## Key Directory & File Descriptions

  * **`wigai-bitwig-extension/`**: The root directory of the project.
      * **`.github/copilot_instructions.md`**: Provides context and guidelines for GitHub Copilot to improve code suggestions and adherence to project standards. This file can reference key documents in `docs/` and specify the root package `io.github.fabb.wigai`.
      * **`.idea/.junie/guidelines.md`**: (If using IntelliJ IDEA) Provides context and guidelines for the JetBrains AI Assistant (Junie), similar to the Copilot instructions, referencing `io.github.fabb.wigai`.
      * **`.vscode/settings.json`**: Workspace-specific settings for VS Code developers, can include Java formatter preferences, linter settings, etc.
      * **`.gradle/`**: Gradle's internal cache and files.
      * **`build/`**: Output directory for compiled classes, the `.bwextension` file, test reports, and other build artifacts. This directory is typically git-ignored.
      * **`docs/`**: Contains all project documentation.
      * **`gradle/wrapper/`**: Contains the Gradle Wrapper files.
      * **`src/`**: Contains all source code and resources.
          * **`src/main/java/io/github/fabb/wigai/`**: This is where the core Java source code for the extension resides, under the root package `io.github.fabb.wigai`.
              * `WigaiExtension.java`: The main entry point class, extending `ControllerExtension`.
              * `WigaiExtensionDefinition.java`: Defines metadata for the extension, extending `ControllerExtensionDefinition`.
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
