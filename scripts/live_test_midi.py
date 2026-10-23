#!/usr/bin/env python3
"""Live test for playing notes, raw MIDI, modulation readout and show. Runs only against an EMPTY project;
creates and deletes its own track, which is silenced before anything plays. show switches Bitwig's panel
layout and leaves it on ARRANGE.

usage: python3 scripts/live_test_midi.py [--url http://localhost:61169/mcp]
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


regular = [t for t in ok("list_tracks") if t["type"] not in ("master", "effect")]
if len(regular) > 2:
    sys.exit(f"Refusing to run: the open project has {len(regular)} tracks. Open an empty project (File > New).")

t = ok("create_track", type="instrument", name="b4 MIDI")["track_index"]
try:
    ok("set_track", track_index=t, volume=0.0)
    ok("insert_device", track_index=t, bitwig_device="Polysynth")
    ok("set_engine_active", active=True)
    ok("play_notes", track_index=t, notes=[{"pitch": 60, "duration_ms": 1500}, {"pitch": 67, "duration_ms": 1500}])
    time.sleep(0.5)
    r = ok("get_playing_notes")
    mine = [x for x in r["tracks"] if x["track_index"] == t]
    check("get_playing_notes sees the auditioned chord", mine and sorted(n["pitch"] for n in mine[0]["notes"]) == [60, 67], r)
    time.sleep(1.5)
    r = ok("get_playing_notes")
    check("get_playing_notes empties after release", not [x for x in r["tracks"] if x["track_index"] == t], r)

    r = ok("send_midi", track_index=t, type="cc", number=1, value=100)
    check("send_midi cc", (r["status"], r["data1"], r["data2"]) == (0xB0, 1, 100), r)
    r = ok("send_midi", track_index=t, type="pitch_bend", value=0)
    check("send_midi pitch_bend center", (r["status"], r["data1"], r["data2"]) == (0xE0, 0, 64), r)
    try:
        ok("send_midi", track_index=t, type="cc", number=1, value=300)
        check("send_midi rejects out-of-range values", False)
    except RuntimeError as e:
        check("send_midi rejects out-of-range values", "0-127" in str(e), e)

    page = ok("get_device_parameters", track_index=t, device_index=0, page=0)["pages"][0]["parameters"]
    check("unmodulated controls have no modulated_value", all("modulated_value" not in p for p in page), page)

    r = ok("show", track_index=t, device_index=0, panel_layout="EDIT")
    check("show switches the layout and names the device", r["panel_layout"] == "EDIT" and r["device"] == "Polysynth", r)
    r = ok("show", panel_layout="ARRANGE")
    check("show back to ARRANGE", r["panel_layout"] == "ARRANGE", r)
finally:
    names = [x["name"] for x in ok("list_tracks")]
    while "b4 MIDI" in names:
        ok("delete_track", track_index=names.index("b4 MIDI")); names = [x["name"] for x in ok("list_tracks")]
    print("tracks after cleanup:", names)
print(f"\n{len(P)} passed, {len(F)} failed", F)
sys.exit(1 if F else 0)
