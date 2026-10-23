#!/usr/bin/env python3
"""Live test for the project tools (project state/reset, scenes, groove, transport options, clip copy/move/
duplicate, note editing, clip transforms). Runs only against an EMPTY project; creates and deletes its own tracks.

usage: python3 scripts/live_test_project.py [--url http://localhost:61169/mcp]
"""
import json
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
regular = [t for t in ok("list_tracks") if t["type"] not in ("master", "effect")]
if len(regular) > 2:
    sys.exit(f"Refusing to run: the open project has {len(regular)} tracks. Open an empty project (File > New).")
ok("set_engine_active", active=True)
a = ok("create_track", type="instrument", name="b1 A")["track_index"]
b = ok("create_track", type="instrument", name="b1 B")["track_index"]
try:
    ok("set_track", track_index=a, solo=True); time.sleep(0.3)
    check("project_state sees a solo", ok("project_state")["has_soloed_tracks"] is True)
    r = ok("reset_project", unsolo=True)
    check("reset_project unsolo", r["has_soloed_tracks"] is False, r)
    n0 = len(ok("list_scenes")); ok("create_scene"); time.sleep(0.4)
    check("create_scene adds a scene", len(ok("list_scenes")) == n0 + 1, (n0, len(ok("list_scenes"))))
    g = ok("set_groove", shuffle_amount=0.75); print("   groove:", json.dumps(g)[:250])
    check("set_groove shuffle", any(abs(v["value"] - 0.75) < 0.01 for v in g.values()), g)
    ok("set_groove", shuffle_amount=0.5)
    t = ok("set_transport_options", metronome_volume=0.4, punch_in=True); print("   transport:", t)
    check("set_transport_options punch_in", t["punch_in"] is True, t)
    ok("set_transport_options", punch_in=False)
    names = [x["name"] for x in ok("list_tracks")]
    a, b = names.index("b1 A"), names.index("b1 B")
    ok("insert_device", track_index=a, bitwig_device="Polysynth")
    ok("create_clip", track_index=a, slot_index=0, length_beats=4)
    ok("write_notes", track_index=a, slot_index=0, notes=[{"start": 0, "pitch": 60, "velocity": 100, "duration": 1}, {"start": 2, "pitch": 64, "velocity": 100, "duration": 1}])
    r = ok("copy_clip", from_track_index=a, from_slot=0, to_track_index=b, to_slot=0)
    check("copy_clip to other track", r["target_has_clip"] and r["source_has_clip"], r)
    r = ok("move_clip", from_track_index=b, from_slot=0, to_track_index=b, to_slot=2)
    check("move_clip within track", r["target_has_clip"] and not r["source_has_clip"], r)
    ok("duplicate_clip", track_index=a, slot_index=0); time.sleep(0.3)
    slots = [c["slot_index"] for c in ok("get_track_details", track_index=a)["clips"] if c["has_content"]]
    check("duplicate_clip fills next slot", 1 in slots, slots)
    r = ok("edit_notes", track_index=a, slot_index=0, pitch=60, velocity=0.3, duration=0.5)
    n60 = [n for n in ok("read_notes", track_index=a, slot_index=0)["notes"] if n["pitch"] == 60]
    check("edit_notes velocity/duration on pitch 60", r["notes_changed"] == 1 and n60 and abs(n60[0]["velocity"] - 38) <= 1 and abs(n60[0]["duration"] - 0.5) < 1e-6, (r, n60))
    r = ok("transform_clip", track_index=a, slot_index=0, transpose=2); print("   transform:", r)
    pitches = sorted(n["pitch"] for n in ok("read_notes", track_index=a, slot_index=0)["notes"])
    check("transform_clip transpose +2", pitches == [62, 66], pitches)
finally:
    for name in ("b1 A", "b1 B"):
        names = [x["name"] for x in ok("list_tracks")]
        while name in names:
            ok("delete_track", track_index=names.index(name)); names = [x["name"] for x in ok("list_tracks")]
    print("tracks after cleanup:", [x["name"] for x in ok("list_tracks")])
print(f"\n{len(P)} passed, {len(F)} failed", F)
sys.exit(1 if F else 0)
