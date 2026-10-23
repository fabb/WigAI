# WigAI API roadmap

What the Bitwig Controller API (v19) can do that WigAI does not expose yet, ordered by how much it helps an agent produce music. The raw method-by-method list is generated into [docs/api-coverage.md](docs/api-coverage.md) by `scripts/api_coverage.py`; this file is the curated version.

## Done

| Area | Tools |
|---|---|
| Read the project | `status`, `list_tracks`, `get_track_details`, `list_devices_on_track`, `get_device_details`, `list_scenes`, `get_clips_in_scene` (upstream) |
| Transport and launching | `transport_start`, `transport_stop`, `launch_clip`, scene launching (upstream), `set_transport`, `stop_track` |
| Tracks and mixer | `create_track`, `delete_track`, `duplicate_track`, `get_track_mixer`, `set_track`, `set_send` |
| Devices | `list_bitwig_devices`, `insert_device`, `copy_device`, `move_device`, `delete_device`, `set_device_enabled`, `set_device_window_open` |
| Chains inside devices | `get_device_chains`, plus `chain` / `chain_device_index` on the device tools (slots such as a Vocoder's Modulator/Carrier, FX slots, layers) |
| Parameters of any device | `get_device_parameters`, `set_device_parameters` (all remote-control pages, by name or page+index) |
| Clips and notes | `insert_file`, `create_clip`, `delete_clip`, `write_notes`, `read_notes`, `set_clip` |
| Listening | `set_engine_active`, `get_levels`, `play_notes` |
| Arrangement | `list_cue_markers`, `go_to_cue_marker`, `set_arrangement` (loop range, time signature), `rename_scene` |
| Actions | `list_actions`, `invoke_action`, `undo`, `redo` |
| Project-wide | `project_state`, `reset_project` (unsolo/unmute/unarm all), `create_scene`, `set_groove`, `set_transport_options` (punch, metronome volume, pre-roll, automation write mode, launcher overdub, launch quantization, post-recording action, rewind/fast-forward/tap tempo) |
| Clip management and note editing | `copy_clip`, `move_clip`, `duplicate_clip`, `record_into_slot`, `edit_notes` (velocity, length, gain, pan, timbre, pressure, micro-pitch, chance, mute, repeats), `transform_clip` (transpose, quantize, duplicate content, launch quantization/mode) |
| Presets and the library | `list_presets` (every .bwpreset on disk, searchable), `preset_file` on `insert_device`, `replace_device` (device, plugin or preset in place), `browser_open` / `browser_state` / `browser_filter` / `browser_commit` / `browser_cancel` (Bitwig's own browser: content types, every filter column, paged results; mode `replace` lists a device's presets) |
| Drum Machine and layers | `list_drum_pads`, `set_drum_pad` (name, volume, pan, mute, solo), `insert_into_drum_pad` (device, plugin, preset, or a sample, which becomes a Sampler), `clear_drum_pads`, `list_layers`, `set_layer`; device lists now show each device's loaded preset |
| Note expressions, clip settings, scenes, arranger clips | `edit_notes` adds release velocity, velocity spread, occurrence, recurrence and repeat curves; `read_notes` reports every non-default expression; `set_clip` adds loop on/off, play start/stop, shuffle, accent, color; `arranger: true` on `read_notes` / `edit_notes` / `set_clip` / `transform_clip` and `write_arranger_notes` work on the arranger clip the user selected; `set_scene`, `delete_scene`, `duplicate_scene`, `copy_scene`, `move_scene`; `set_cue` (cue volume and mix); `set_track` adds group expand/collapse and crossfade side |
| Listening, MIDI and the UI | `get_playing_notes` (notes sounding per track), `send_midi` (CC, pitch bend, program, aftertouch, raw), modulated values in `get_device_parameters`, `show` (panel layout, track, clip in editor, device) |
| Following the user, and plugins | `get_events` (event log of transport, project, track, clip, selection and parameter changes, with long-poll), `watch` (streams changes as MCP progress and log notifications, [fabb/WigAI#3](https://github.com/fabb/WigAI/issues/3)); `list_installed_devices` reads CLAP, VST3 and VST2 plugins from Bitwig's own plugin cache; `vst2_id` on `insert_device` / `replace_device`; server instructions for agents and [docs/AGENT-GUIDE.md](docs/AGENT-GUIDE.md) |
| Device knowledge | `list_installed_devices`, `describe_device` (on-demand control scans with history), `fetch_device_docs`, `catalog_status` |

## Next

Everything the Controller API offers that helps an agent make music is now exposed. Left:

1. **Resource subscriptions.** Expose project state as MCP resources with `resources/updated` notifications once the MCP Java SDK supports subscriptions (0.11 does not); `get_events` and `watch` cover the need meanwhile.
2. **UI automation for what the API cannot do** (routing, arranger placement): would need driving Bitwig's window from outside, which is fragile and takes over the user's screen. Not planned.

## Not possible with the Controller API

These need changes in Bitwig itself; tools should tell the user to do them by hand.

- **Audio and MIDI routing:** track inputs/outputs, sidechain sources, an Audio Receiver's source. `Track.sourceSelector` is read-only.
- **Placing clips on the arranger timeline**, or reading where arranger clips are.
- **Adding or removing modulators** (LFOs, envelopes) and modulation routings. Their effect can be read: `get_device_parameters` reports a control's modulated value.
- **Writing automation points.** Only by recording parameter changes with automation write enabled.
- **Parameters that are not on a remote-control page** ([fabb/WigAI#15](https://github.com/fabb/WigAI/issues/15)).
- **Rendering, bouncing or exporting audio**, other than through application actions where Bitwig offers one.
- **Moving or copying tracks.** Bitwig 6.1.3 accepts `InsertionPoint.moveTracks/copyTracks` from an extension and silently does nothing, on the request thread and on the control thread alike (tested 2026-10-17). `duplicate_track` does work for copies.
- **Opening a project from disk.** Only cycling through projects that are already open.
- **Adding, removing or duplicating layers, and emptying a drum pad.** `DeviceLayer.duplicate()` / `duplicateObject()` / `deleteObject()`, `DrumPad.deleteObject()` and `host.duplicateObjects(layer)` are silently ignored in Bitwig 6.1.3. **`host.deleteObjects(drumPad)` crashes Bitwig** (ClassCastException on its GUI thread), taking every open project with it; never call it. Delete the devices on a pad or layer with `delete_device` and chain `layer:N` instead.
- **Capturing playing clips into a scene.** `Project.createSceneFromPlayingLauncherClips()` and the "Create Scene From Playing Launcher Clips" action do nothing when called from an extension, even with clips playing (tested 2026-10-17).
- **Selecting an arranger clip.** The arranger cursor clip follows the user's selection only; tools with `arranger: true` work on whatever clip the user clicked.
- **Reading the device of a packaged preset from disk.** Presets from installed packages are encrypted; `browser_open` with mode `replace` shows which presets belong to a device.
