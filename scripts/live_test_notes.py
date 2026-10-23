#!/usr/bin/env python3
"""Live test for note expressions and conditions, clip settings, scenes, cue mix, crossfade and arranger clips.
Runs only against an EMPTY project; creates and deletes its own track and scenes. The test track is silenced
before anything plays.

usage: python3 scripts/live_test_notes.py [--url http://localhost:61169/mcp]
"""
import os
import sys
import time

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from live_test import Mcp  # noqa: E402

url = sys.argv[sys.argv.index("--url") + 1] if "--url" in sys.argv else "http://localhost:61169/mcp"
m = Mcp(url)
P, F = [], []


def ok(tool_name, **a):
    return m.tool(tool_name, **a)
def check(label, cond, detail=""):
    (P if cond else F).append(label); print(("ok   " if cond else "FAIL ") + label + ("" if cond else f"  ({detail})"), flush=True)
def index_of(name):
    return [x["name"] for x in ok("list_tracks")].index(name)
def scene_count():
    return len(ok("list_scenes"))


regular = [t for t in ok("list_tracks") if t["type"] not in ("master", "effect")]
if len(regular) > 2:
    sys.exit(f"Refusing to run: the open project has {len(regular)} tracks. Open an empty project (File > New).")

ok("create_track", type="instrument", name="b3 Notes")
scenes_before = scene_count()
cue_before = ok("project_state")
try:
    t = index_of("b3 Notes")
    ok("set_track", track_index=t, volume=0.0)
    ok("insert_device", track_index=t, bitwig_device="Polysynth")
    ok("create_clip", track_index=t, slot_index=0, length_beats=4)
    ok("write_notes", track_index=t, slot_index=0, notes=[
        {"start": 0, "pitch": 60, "velocity": 100, "duration": 1}, {"start": 2, "pitch": 64, "velocity": 100, "duration": 1}])

    r = ok("edit_notes", track_index=t, slot_index=0, pitch=60, release_velocity=0.8, velocity_spread=0.25,
           occurrence="first", recurrence_length=4, recurrence_mask=5, repeats=3, repeat_curve=0.5,
           repeat_velocity_end=-0.5)
    check("edit_notes with expressions and conditions", r["notes_changed"] == 1, r)
    n = [x for x in ok("read_notes", track_index=t, slot_index=0)["notes"] if x["pitch"] == 60][0]
    print("   note:", n)
    check("release_velocity and velocity_spread", abs(n.get("release_velocity", 0) - 0.8) < 0.01
          and abs(n.get("velocity_spread", 0) - 0.25) < 0.01, n)
    check("occurrence", n.get("occurrence") == "first", n)
    check("recurrence", n.get("recurrence_length") == 4 and n.get("recurrence_mask") == 5, n)
    check("repeats with curve and velocity end", n.get("repeats") == 3 and abs(n.get("repeat_curve", 0) - 0.5) < 0.01
          and abs(n.get("repeat_velocity_end", 0) + 0.5) < 0.01, n)
    ok("edit_notes", track_index=t, slot_index=0, pitch=60, occurrence="off", recurrence_length=0, repeats=0)
    n = [x for x in ok("read_notes", track_index=t, slot_index=0)["notes"] if x["pitch"] == 60][0]
    check("conditions switch off", not any(k in n for k in ("occurrence", "recurrence_length", "repeats")), n)

    r = ok("set_clip", track_index=t, slot_index=0, shuffle=True, accent=0.5, loop_enabled=False,
           play_start_beats=1, color="#3080FF", name="b3 clip")
    print("   clip:", r)
    check("set_clip shuffle/loop/play start", r["shuffle"] is True and r["loop_enabled"] is False
          and abs(r["play_start_beats"] - 1) < 1e-6, r)
    check("set_clip accent", abs(r["accent"] - 0.5) < 0.01, r)
    ok("set_clip", track_index=t, slot_index=0, loop_enabled=True, play_start_beats=0)

    while scene_count() < 2:
        ok("create_scene"); time.sleep(0.3)
    n0 = scene_count()
    r = ok("duplicate_scene", scene_index=0)
    check("duplicate_scene adds a scene with the clip", len(r["scenes"]) == n0 + 1 and r["scenes"][1]["clip_count"] == 1, r)
    r = ok("move_scene", from_scene=1, to_scene=2)
    check("move_scene moves the clips", r["scenes"][1]["clip_count"] == 0 and r["scenes"][2]["clip_count"] == 1, r)
    r = ok("copy_scene", from_scene=2, to_scene=1)
    check("copy_scene copies the clips", r["scenes"][1]["clip_count"] == 1 and r["scenes"][2]["clip_count"] == 1, r)
    r = ok("set_scene", scene_index=1, name="b3 copy", color="#FF8000")
    check("set_scene name", r["name"] == "b3 copy", r)
    r = ok("delete_scene", scene_index=2)
    check("delete_scene removes a scene", len(r["scenes"]) == n0, r)

    r1 = ok("set_cue", volume=0.5, mix=0.25)
    r2 = ok("set_cue", volume=0.75, mix=0.5)
    check("set_cue changes the cue bus", r1["cue_volume"] != r2["cue_volume"] and r1["cue_mix"] != r2["cue_mix"], (r1, r2))

    r = ok("set_track", track_index=t, crossfade_mode="B")
    check("set_track crossfade_mode", r["crossfade_mode"] == "B", r)

    try:
        r = ok("read_notes", arranger=True)
        check("read_notes arranger reads the selected arranger clip", "notes" in r and r.get("arranger") is True, r)
    except RuntimeError as e:
        check("read_notes arranger explains when nothing is selected", "arranger" in str(e), e)
finally:
    ok("set_cue", volume=cue_before["cue_volume_raw"], mix=cue_before["cue_mix_raw"])
    names = [x["name"] for x in ok("list_tracks")]
    while "b3 Notes" in names:
        ok("delete_track", track_index=names.index("b3 Notes")); names = [x["name"] for x in ok("list_tracks")]
    while scene_count() > max(scenes_before, 1):
        ok("delete_scene", scene_index=scene_count() - 1)
    print("tracks after cleanup:", names, "scenes:", scene_count())
print(f"\n{len(P)} passed, {len(F)} failed", F)
sys.exit(1 if F else 0)
