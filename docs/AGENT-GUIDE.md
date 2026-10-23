# Using WigAI from an agent

How the tools fit together, for AI agents (and the people setting them up). Tool descriptions carry the details;
this page gives the workflows and the limits.

## Find your way around a project

1. `status` for the project, transport and selection; `list_tracks` for every track with its devices.
2. `get_track_mixer`, `get_device_chains` (slots such as a Vocoder's Modulator/Carrier, and layers) and
   `get_device_parameters` (every remote-control page) to read the details without moving the user's selection.
3. `project_state` tells you if a solo, mute or arm is hiding something; `reset_project` clears them.

Groups that are collapsed in Bitwig hide their tracks from the track list. `set_track` with `group_expanded`
shows them.

## Know what is installed, and what it does

- `list_installed_devices` searches Bitwig's devices and every plugin Bitwig has scanned (CLAP, VST3, VST2,
  read from Bitwig's own plugin cache in `~/.BitwigStudio/cache`), plus LV2 plugins usable through Carla when
  `scripts/device_catalog.py inventory` has been run.
- `describe_device` returns what is known about one device. Without a stored scan it measures the controls now
  (on a temporary track, or on an instance you name) and keeps a scan history.
- `fetch_device_docs` stores Bitwig's own user-guide description of each built-in device.
- The catalog is facts, not advice. Decide what to use from those facts; optional personal notes can live in
  `device-guide.json` next to the catalog file and come back as `user_notes`.

## Build sounds

- `insert_device` takes a Bitwig device name, a `clap_id`, `vst3_id` or `vst2_id` (ids from
  `list_installed_devices`), or a `preset_file`.
- `list_presets` searches the preset files on disk. Presets from installed packages are encrypted, so their
  device is unknown on disk; `browser_open` with mode `replace` on a device lists that device's presets.
- `browser_open` / `browser_filter` / `browser_commit` drive Bitwig's own browser (content types, categories,
  tags, creators). It shows in Bitwig's window until committed or cancelled.
- `replace_device` swaps a device for another device or a preset, in place.
- Drum Machines: `list_drum_pads`, `set_drum_pad`, `insert_into_drum_pad` (a sample file becomes a Sampler).
  Layer devices: `list_layers`, `set_layer`, and chain `layer:N` on the device tools.
- `set_device_parameters` sets any control by name or page and index; `play_notes` auditions a sound without
  writing a clip. `get_device_parameters` shows `modulated_value` for controls a modulator is moving.

## Write and shape music

- Launcher clips: `create_clip`, `write_notes`, `read_notes`, `edit_notes` (velocity, expressions, chance,
  occurrence, recurrence, repeats), `transform_clip`, `set_clip`, `copy_clip`, `move_clip`, `duplicate_clip`.
- Arranger clips: the API cannot place or select them. When the user selects one, `arranger: true` on the
  clip tools and `write_arranger_notes` work on it.
- Scenes: `create_scene`, `set_scene`, `duplicate_scene`, `copy_scene`, `move_scene`, `delete_scene`.

## Follow what the user does

- `get_events` returns changes since a sequence number: transport, project switches, track settings, clip
  launches, the user's selected track/device/preset, and the selected device's knobs as they move. Call it
  once without `since_seq` to get a starting point, then pass `next_seq` back; `wait_ms` waits for the next
  change instead of polling.
- `watch` streams changes for a while as MCP progress and log notifications, then returns them all.
- `get_playing_notes` and `get_levels` show what is sounding right now (the project needs the audio engine:
  `set_engine_active`).

## Work safely in someone's song

- Prefer reading tools until you know the project; every edit is undoable with `undo`, but a crash loses
  unsaved work, so suggest saving before larger changes.
- Do not experiment with untried tools in a real song. `scripts/live_test_*.py` run only against an empty
  project for that reason.

## Not possible through Bitwig's API

Routing (track inputs/outputs, Audio Receiver sources, sidechains), placing clips on the arranger timeline,
moving or copying tracks, adding modulators or automation points, adding or deleting layers, emptying a drum
pad, capturing playing clips into a scene, and opening projects from disk. Ask the user to do these by hand.
See [ROADMAP.md](../ROADMAP.md) for details.
