#!/usr/bin/env python3
"""Live test for container and library tools (drum pads, layer mixers, presets on disk, replace_device, the popup
browser). Runs only against an EMPTY project; creates and deletes its own tracks. The browser opens briefly
in Bitwig's window.

usage: python3 scripts/live_test_container.py [--url http://localhost:61169/mcp]
"""
import math
import os
import struct
import sys
import tempfile
import time
import wave

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


regular = [t for t in ok("list_tracks") if t["type"] not in ("master", "effect")]
if len(regular) > 2:
    sys.exit(f"Refusing to run: the open project has {len(regular)} tracks. Open an empty project (File > New).")

sample = os.path.join(tempfile.mkdtemp(prefix="wigai-"), "blip.wav")
with wave.open(sample, "wb") as w:
    w.setnchannels(1); w.setsampwidth(2); w.setframerate(44100)
    w.writeframes(b"".join(struct.pack("<h", int(8000 * math.sin(2 * math.pi * 440 * i / 44100))) for i in range(4410)))

ok("create_track", type="instrument", name="b2 Drums")
ok("create_track", type="audio", name="b2 FX")
try:
    r = ok("list_presets", query="drum machine kit", limit=5)
    check("list_presets finds presets on disk", r["indexed"] > 0, r["indexed"])
    kits = r["presets"]
    d = index_of("b2 Drums")
    if kits:
        r = ok("insert_device", track_index=d, preset_file=kits[0]["path"])
        check("insert_device loads a preset file", r["inserted"] and r["devices"][0]["name"] == "Drum Machine", r)
    else:
        ok("insert_device", track_index=d, bitwig_device="Drum Machine")

    r = ok("insert_into_drum_pad", track_index=d, device_index=0, pad=60, file=sample)
    check("insert_into_drum_pad with a sample makes a Sampler", r["inserted"] and r["devices"][0]["name"] == "Sampler", r)
    r = ok("insert_into_drum_pad", track_index=d, device_index=0, pad=60, bitwig_device="Bit-8")
    check("insert_into_drum_pad appends a device", [x["name"] for x in r["devices"]] == ["Sampler", "Bit-8"], r)
    r = ok("set_drum_pad", track_index=d, device_index=0, pad=60, name="Blip", volume_db=-6, pan=-0.5, mute=True)
    check("set_drum_pad name/volume/pan/mute", r["name"] == "Blip" and r["volume"] == "-6.0 dB"
          and r["pan"].startswith("-50") and r["mute"] is True, r)
    ok("set_drum_pad", track_index=d, device_index=0, pad=60, solo=True)
    pads = ok("list_drum_pads", track_index=d, device_index=0)
    check("list_drum_pads reports mute and solo", pads["has_muted_pads"] and pads["has_soloed_pads"]
          and any(p["pad"] == 60 and p["note"] == "C3" for p in pads["pads"]), pads)
    r = ok("clear_drum_pads", track_index=d, device_index=0, mutes=True, solos=True)
    check("clear_drum_pads", not r["has_muted_pads"] and not r["has_soloed_pads"], r)

    fx = index_of("b2 FX")
    ok("insert_device", track_index=fx, bitwig_device="FX Layer")
    r = ok("set_layer", track_index=fx, device_index=0, layer=0, name="Wet", volume_db=-3, pan=0.25, mute=True)
    check("set_layer name/volume/pan/mute", r["name"] == "Wet" and r["volume"] == "-3.0 dB" and r["mute"] is True, r)
    ok("insert_device", track_index=fx, device_index=0, chain="layer:0", bitwig_device="Chorus+")
    r = ok("list_layers", track_index=fx, device_index=0)
    check("list_layers shows the layer's devices", r["layers"][0]["devices"][0]["name"] == "Chorus+", r)

    ok("insert_device", track_index=fx, bitwig_device="Delay+")
    r = ok("replace_device", track_index=fx, device_index=1, bitwig_device="Convolution")
    check("replace_device swaps in place", r["replaced"] == "Delay+" and r["device"] == "Convolution", r)

    r = ok("browser_open", track_index=fx, device_index=1, mode="after")
    check("browser_open lists content types and results", "Bitwig Presets" in r["content_types"] and r["result_count"] > 0, r)
    r = ok("browser_state", list_column="category")
    check("browser_state lists a column", "Reverb" in r["category_items"], r.get("category_items"))
    r = ok("browser_filter", content_type="Devices", filters={"category": "Reverb"})
    check("browser_filter narrows results", r["filters"]["category"]["selected"] == "Reverb" and "Reverb" in r["results"], r)
    r = ok("browser_commit", result="Reverb")
    check("browser_commit loads the result", r["loaded"] == "Reverb" and not r["browser_open"], r)
    names = [x["name"] for x in ok("list_devices_on_track", track_index=fx)]
    check("browser inserted after the device", names == ["FX Layer", "Convolution", "Reverb"], names)

    r = ok("browser_open", track_index=fx, device_index=1, mode="replace")
    check("browser_open replace shows the device's presets", r["result_count"] > 0, r)
    preset = r["results"][0]
    r = ok("browser_commit", result_index=0)
    check("browser_commit by index loads a preset", r["loaded"] == preset, r)
    ok("browser_open", track_index=fx, mode="end")
    check("browser_cancel closes", ok("browser_cancel")["browser_open"] is False)
    names = [x["name"] for x in ok("list_devices_on_track", track_index=fx)]
    check("cancel loads nothing", names == ["FX Layer", "Convolution", "Reverb"], names)
finally:
    ok("browser_cancel")
    for name in ("b2 Drums", "b2 FX"):
        names = [x["name"] for x in ok("list_tracks")]
        while name in names:
            ok("delete_track", track_index=names.index(name)); names = [x["name"] for x in ok("list_tracks")]
    os.remove(sample)
    print("tracks after cleanup:", [x["name"] for x in ok("list_tracks")])
print(f"\n{len(P)} passed, {len(F)} failed", F)
sys.exit(1 if F else 0)
