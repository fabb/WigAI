# WigAI - Bitwig Studio Extension for AI Control

> [!WARNING]
> **This project is archived and no longer maintained.**
> WigAI was a working experiment that I have abandoned. The code is [MIT licensed](LICENSE) — feel free to fork, reuse and build on it.
>
> Forks with further development (as of October 2026):
> - [glassdimly/wigai-full](https://github.com/glassdimly/wigai-full) — full project editing: devices, presets, browser, notes, arranger, event streaming
> - [jmeachum/WigAI](https://github.com/jmeachum/WigAI) — continued development, clip slot status
> - [liqtranq/WigAI](https://github.com/liqtranq/WigAI) — 39 extra MCP tools, resources, project switching
> - [0reo/WigAI](https://github.com/0reo/WigAI) — track group hierarchy, arranger clip introspection
> - [csabacsaba/WigAI](https://github.com/csabacsaba/WigAI) — song structure / scene / cue builder, clip note writing
>
> I am not affiliated with these forks or their authors, have not reviewed their code, and do not endorse them. They are listed for reference only; use them at your own risk. I accept no responsibility or liability for them.

WigAI is a Model Context Protocol (MCP) server implemented as a Bitwig Studio extension. It provides an interface for external AI assistants to control Bitwig Studio through simple text-based commands.

## Overview

The extension enables external AI agents (e.g., IDE-based copilots, standalone AI assistants) to interact with and control Bitwig Studio using the Model Context Protocol. This allows for hands-free control via text commands interpreted by an AI assistant.

## Key Features

-   MCP server implementation
-   Transport control (start/stop playback)
-   Device parameter control for selected devices
-   Clip and scene launching
-   Project editing: create/delete/duplicate tracks; volume (exact dB), pan, mute, solo, arm, color, sends
-   Devices: insert Bitwig built-ins by name or plugins by CLAP/VST3 id; copy, move, bypass, delete
-   Parameters of any device on any track, by name or page, without moving the user's selection
-   Clips: import audio/MIDI files into launcher slots, create clips, write MIDI notes
-   Tempo, loop, metronome, record; run any Bitwig menu action; undo/redo
-   Device chains (Vocoder modulator/carrier, FX slots, layers), presets and Bitwig's browser, Drum Machine pads, layer mixers
-   Device knowledge: every installed Bitwig device and plugin (CLAP, VST3, VST2 from Bitwig's plugin cache), control scans on demand, Bitwig's own device descriptions
-   Note expressions and conditions, clip settings, scenes, arranger clips selected by the user, groove, cue mix
-   Following the user: an event log of changes (`get_events`, with long-poll) and `watch`, which streams them as MCP notifications
-   Listening: meters, playing notes, auditioning notes, sending MIDI

See [docs/AGENT-GUIDE.md](docs/AGENT-GUIDE.md) for how the tools fit together and [ROADMAP.md](ROADMAP.md) for what Bitwig's API cannot do.

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

## Testing

```bash
./gradlew test                            # unit tests
python3 scripts/live_test.py              # end-to-end against a running Bitwig with WigAI enabled
python3 scripts/live_test_project.py      # project-wide tools, groove, clip management
python3 scripts/live_test_container.py    # presets, browser, drum pads, layers
python3 scripts/live_test_notes.py        # note expressions, clip settings, scenes, cue mix
python3 scripts/live_test_midi.py         # playing notes, MIDI, modulation readout, show
python3 scripts/live_test_events.py       # plugin inventory, get_events, watch streaming
```

The live tests create, edit and delete their own tracks, so they refuse to run unless an empty
project is open (`live_test.py --force` overrides). Never run them against a song you care about.

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
