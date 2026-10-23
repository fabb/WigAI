#!/usr/bin/env python3
"""Live test for the plugin inventory (Bitwig's plugin cache, VST2/VST3 insertion by id) and for following changes
(get_events long-poll, watch streaming notifications). Runs only against an EMPTY project; creates and deletes
its own track.

usage: python3 scripts/live_test_events.py [--url http://localhost:61169/mcp]
"""
import json
import os
import sys
import threading
import time
import urllib.request

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


def stream_watch(duration_ms, types):
    """Calls watch with a progress token and returns (seconds after start, message) for every SSE message."""
    m.next_id += 1
    body = {"jsonrpc": "2.0", "id": m.next_id, "method": "tools/call",
            "params": {"name": "watch", "arguments": {"duration_ms": duration_ms, "types": types},
                       "_meta": {"progressToken": "live-test"}}}
    req = urllib.request.Request(m.url, data=json.dumps(body).encode(), method="POST", headers={
        "Content-Type": "application/json", "Accept": "application/json, text/event-stream", "mcp-session-id": m.session})
    out, start = [], time.time()
    with urllib.request.urlopen(req, timeout=duration_ms / 1000 + 30) as r:
        for raw in r:
            line = raw.decode().rstrip()
            if line.startswith("data: "):
                out.append((time.time() - start, json.loads(line[6:])))
    return out


regular = [t for t in ok("list_tracks") if t["type"] not in ("master", "effect")]
if len(regular) > 2:
    sys.exit(f"Refusing to run: the open project has {len(regular)} tracks. Open an empty project (File > New).")

ok("create_track", type="audio", name="b5 Events")
try:
    counts = {k: ok("list_installed_devices", kind=k)["count"] for k in ("clap", "vst3", "vst2")}
    print("   plugins:", counts)
    check("plugins come from Bitwig's plugin cache", sum(counts.values()) > 0, counts)
    t = index_of("b5 Events")
    vst3 = [d for d in ok("list_installed_devices", kind="vst3")["devices"] if "Reverb" in d["name"] or "EQ" in d["type"]]
    if vst3:
        r = ok("insert_device", track_index=t, vst3_id=vst3[0]["id"])
        check("insert_device by vst3_id from the inventory", r["inserted"], r)
    vst2 = [d for d in ok("list_installed_devices", kind="vst2")["devices"] if not d["name"].startswith("Carla")]
    if vst2:
        r = ok("insert_device", track_index=t, vst2_id=int(vst2[0]["id"]))
        check("insert_device by vst2_id from the inventory", r["inserted"], r)

    start = ok("get_events")
    check("get_events without since_seq gives a starting point", start["events"] == [] and "next_seq" in start, start)
    ok("set_track", track_index=t, mute=True)
    r = ok("get_events", since_seq=start["next_seq"], wait_ms=2000, types=["track"])
    check("get_events reports a mute", any(e.get("field") == "mute" and e.get("value") is True for e in r["events"]), r)

    since = r["next_seq"]
    threading.Thread(target=lambda: (time.sleep(1.0), m.tool("set_track", track_index=t, mute=False))).start()
    t0 = time.time()
    r = ok("get_events", since_seq=since, wait_ms=8000, types=["track"])
    waited = time.time() - t0
    check("get_events long-poll returns when the change happens", r["events"] and 0.7 < waited < 4, (waited, r))

    since = r["next_seq"]
    ok("set_engine_active", active=True); time.sleep(0.5)
    since = ok("get_events")["next_seq"]
    ok("transport_start"); time.sleep(0.6); ok("transport_stop")
    seen, deadline = [], time.time() + 4
    while time.time() < deadline and not any(e.get("playing") is False for e in seen):
        r = ok("get_events", since_seq=since, wait_ms=1000, types=["transport"])
        seen += r["events"]; since = r["next_seq"]
    check("transport events (play then stop)", any(e.get("playing") is True for e in seen)
          and any(e.get("playing") is False for e in seen), seen)

    threading.Thread(target=lambda: (time.sleep(1.0), m.tool("set_track", track_index=t, solo=True),
                                     time.sleep(0.6), m.tool("set_track", track_index=t, solo=False))).start()
    msgs = stream_watch(3000, ["track"])
    notes = [(s, d) for s, d in msgs if d.get("method") == "notifications/progress"]
    logs = [d for _, d in msgs if d.get("method") == "notifications/message"]
    final = [d for _, d in msgs if "result" in d]
    print("   watch:", [(round(s, 2), d["method"]) for s, d in msgs if "method" in d])
    check("watch streams progress notifications while it runs", len(notes) >= 2 and notes[0][0] < 2.5, notes)
    check("watch also sends log messages", len(logs) == len(notes), (len(logs), len(notes)))
    body = json.loads(final[0]["result"]["content"][0]["text"])["data"] if final else {}
    check("watch returns every change it saw", len(body.get("events", [])) == len(notes), body)
finally:
    names = [x["name"] for x in ok("list_tracks")]
    while "b5 Events" in names:
        ok("delete_track", track_index=names.index("b5 Events")); names = [x["name"] for x in ok("list_tracks")]
    print("tracks after cleanup:", names)
print(f"\n{len(P)} passed, {len(F)} failed", F)
sys.exit(1 if F else 0)
