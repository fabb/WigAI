# WigAI - Bitwig Studio Extension for AI Control

WigAI is a Model Context Protocol (MCP) server implemented as a Bitwig Studio extension. It provides an interface for external AI assistants to control Bitwig Studio through simple text-based commands.

## Overview

The extension enables external AI agents (e.g., IDE-based copilots, standalone AI assistants) to interact with and control Bitwig Studio using the Model Context Protocol. This allows for hands-free control via text commands interpreted by an AI assistant.

## Key Features

-   MCP server implementation
-   Transport control (start/stop playback)
-   Device parameter control for selected devices
-   Clip and scene launching

## Requirements

-   Bitwig Studio (5.2.7 or later)
-   Java 21 LTS
-   External AI agent supporting Model Context Protocol

## Building

To build the extension:

```bash
./gradlew build
```

This will generate a `.bwextension` file in the `build/extensions` directory.

## Installation

1. Copy the `WigAI.bwextension` file from `build/extensions` to your Bitwig Studio extensions directory.
2. Launch Bitwig Studio
3. Open Bitwig preferences, navigate to Extensions, and activate WigAI

## Usage

Once the extension is activated in Bitwig Studio, the MCP server will be available at `http://localhost:61169/mcp`. External AI agents can connect to this endpoint to send commands and receive responses.

## Development

This project is developed using the [BMAD v2 method](https://github.com/bmadcode/BMAD-METHOD) with AI Agents. The files in folders `agents`, `ai` and `docs` used for this development method.

## Releases

This project uses [Nyx](https://github.com/mooltiverse/nyx) for semantic versioning and automated releases. The release process follows [Conventional Commits](https://www.conventionalcommits.org/) specification:

- `feat:` - new features (minor version bump)
- `fix:` - bug fixes (patch version bump)
- `feat!:` or `BREAKING CHANGE:` - breaking changes (major version bump)
- `chore:`, `docs:`, `style:`, `refactor:`, `test:` - no version bump

Releases are automatically created on pushes to the `main` branch when commits follow the conventional commit format.

## License

[MIT License](LICENSE)

## Author

fabb
