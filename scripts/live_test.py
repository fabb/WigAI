#!/usr/bin/env python3
"""Live smoke test for the WigAI MCP server against a running Bitwig Studio.

It creates its own temporary tracks at the end of the track list, exercises the editing tools
on them, and deletes them again. It never touches existing tracks except to read them.
Refuses to run unless the open project is empty (at most 2 tracks); --force overrides.

usage: python3 scripts/live_test.py [--url http://localhost:61169/mcp] [--keep] [--force]
"""
import argparse
import json
import os
import sys
import tempfile
import urllib.request

PASS, FAIL = [], []


class Mcp:
    def __init__(self, url):
        self.url, self.session, self.next_id = url, None, 1
        self.rpc("initialize", {"protocolVersion": "2025-03-26", "capabilities": {},
                                "clientInfo": {"name": "wigai-live-test", "version": "1"}})
        self.post({"jsonrpc": "2.0", "method": "notifications/initialized"})

    def post(self, body):
        req = urllib.request.Request(self.url, data=json.dumps(body).encode(), method="POST", headers={
            "Content-Type": "application/json", "Accept": "application/json, text/event-stream",
            **({"mcp-session-id": self.session} if self.session else {})})
        with urllib.request.urlopen(req, timeout=60) as r:
            self.session = r.headers.get("mcp-session-id") or self.session
            text = r.read().decode()
        for line in text.splitlines():  # SSE framing
            if line.startswith("data: "):
                return json.loads(line[6:])
        return json.loads(text) if text.strip() else None

    def rpc(self, method, params):
        self.next_id += 1
        resp = self.post({"jsonrpc": "2.0", "id": self.next_id, "method": method, "params": params})
        if "error" in resp:
            raise RuntimeError(resp["error"])
        return resp["result"]

    def tool(self, tool_name, **args):
        res = self.rpc("tools/call", {"name": tool_name, "arguments": args})
        body = json.loads(res["content"][0]["text"])
        if res.get("isError") or body.get("status") != "success":
            raise RuntimeError(f"{tool_name}: {body.get('error', body)}")
        return body["data"]


def check(label, cond, detail=""):
    (PASS if cond else FAIL).append(label)
    print(("  ok   " if cond else "  FAIL ") + label + (f"  ({detail})" if detail and not cond else ""))


def step(label, fn):
    try:
        return fn()
    except Exception as e:  # noqa: BLE001
        check(label, False, str(e))
        return None


def write_midi(path):
    """One-bar C major chord, 480 ppq."""
    def vlq(n):
        out = [n & 0x7F]
        while n > 0x7F:
            n >>= 7
            out.insert(0, (n & 0x7F) | 0x80)
        return bytes(out)
    ev = b""
    for i, p in enumerate((60, 64, 67)):
        ev += vlq(0) + bytes([0x90, p, 100])
    for i, p in enumerate((60, 64, 67)):
        ev += vlq(1920 if i == 0 else 0) + bytes([0x80, p, 0])
    ev += vlq(0) + b"\xff\x2f\x00"
    with open(path, "wb") as f:
        f.write(b"MThd" + (6).to_bytes(4, "big") + (0).to_bytes(2, "big") + (1).to_bytes(2, "big") + (480).to_bytes(2, "big"))
        f.write(b"MTrk" + len(ev).to_bytes(4, "big") + ev)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--url", default="http://localhost:61169/mcp")
    ap.add_argument("--keep", action="store_true", help="leave the test tracks in place")
    ap.add_argument("--force", action="store_true", help="run even if the open project is not empty")
    args = ap.parse_args()
    m = Mcp(args.url)

    tools = {t["name"] for t in m.rpc("tools/list", {})["tools"]}
    check("new tools are registered", {"create_track", "insert_device", "set_device_parameters"} <= tools,
          f"missing {sorted({'create_track', 'insert_device', 'set_device_parameters'} - tools)}")

    existing = [t for t in m.tool("list_tracks") if t["type"] not in ("master", "effect")]
    if len(existing) > 2 and not args.force:
        print(f"Refusing to run: the open project has {len(existing)} tracks. This test creates, edits and deletes "
              "tracks and devices; open an empty project (File > New) or pass --force.")
        return 2
    n_before = len(existing)
    created = []

    print("tracks")
    t = step("create_track instrument", lambda: m.tool("create_track", type="instrument", name="WigAI test inst"))
    if t:
        created.append("WigAI test inst")
        check("create_track instrument", t.get("name") == "WigAI test inst", t)
    t = step("create_track audio", lambda: m.tool("create_track", type="audio", name="WigAI test audio"))
    if t:
        created.append("WigAI test audio")
        check("create_track audio", t.get("name") == "WigAI test audio", t)
    if not created:
        return finish()

    r = step("set_track volume_db/pan/mute", lambda: m.tool("set_track", track_name="WigAI test audio",
                                                         volume_db=-3.0, pan=-0.5, mute=True, color="#3366CC"))
    if r:
        check("volume_db lands on -3 dB", r["volume_display"].replace("−", "-").startswith("-3.0"), r["volume_display"])
        check("pan set", abs(r["pan"] + 0.5) < 0.02, r["pan"])
        check("mute set", r["mute"] is True)

    print("devices")
    r = step("list_bitwig_devices", lambda: m.tool("list_bitwig_devices"))
    check("list_bitwig_devices has EQ+", bool(r) and any(d.get("name") == "EQ+" for d in r))
    r = step("insert_device EQ+", lambda: m.tool("insert_device", track_name="WigAI test audio", bitwig_device="EQ+"))
    check("insert_device EQ+", bool(r) and r["inserted"] and r["devices"][-1]["name"] == "EQ+", r)
    r = step("insert_device Reverb after EQ+", lambda: m.tool("insert_device", track_name="WigAI test audio",
                                                              bitwig_device="Reverb", after_device_index=0))
    check("insert_device Reverb after EQ+", bool(r) and [d["name"] for d in r["devices"]] == ["EQ+", "Reverb"], r)

    r = step("get_device_parameters all pages", lambda: m.tool("get_device_parameters", track_name="WigAI test audio", device_index=1))
    if r:
        names = [p["name"] for pg in r["pages"] for p in pg["parameters"]]
        check("get_device_parameters returns pages", len(r["pages"]) >= 1 and len(names) > 0, r["page_names"])
        target = names[0]
        s = step("set_device_parameters by name", lambda: m.tool("set_device_parameters", track_name="WigAI test audio",
                                                                device_index=1, parameters=[{"name": target, "value": 0.25}]))
        check("set_device_parameters by name", bool(s) and s["results"][0]["status"] == "success"
              and abs(s["results"][0]["value"] - 0.25) < 0.01, s)
        s = step("set_device_parameters by page/index", lambda: m.tool("set_device_parameters", track_name="WigAI test audio",
                                                                      device_index=1, parameters=[{"page": 0, "index": 0, "value": 0.75}]))
        check("set_device_parameters by page/index", bool(s) and abs(s["results"][0]["value"] - 0.75) < 0.01, s)

    r = step("set_device_enabled false", lambda: m.tool("set_device_enabled", track_name="WigAI test audio", device_index=0, enabled=False))
    check("bypass device", bool(r) and r["enabled"] is False, r)
    r = step("set_device_enabled true", lambda: m.tool("set_device_enabled", track_name="WigAI test audio", device_index=0, enabled=True))
    check("un-bypass device", bool(r) and r["enabled"] is True, r)

    r = step("copy_device to other track", lambda: m.tool("copy_device", from_track_name="WigAI test audio", from_device_index=0,
                                                         to_track_name="WigAI test inst"))
    check("copy_device to other track", bool(r) and any(d["name"] == "EQ+" for d in r["target_track_devices"]), r)
    r = step("move_device within track", lambda: m.tool("move_device", from_track_name="WigAI test audio", from_device_index=1,
                                                       to_track_name="WigAI test audio", position="start"))
    check("move_device within track", bool(r) and [d["name"] for d in r["target_track_devices"]] == ["Reverb", "EQ+"], r)
    r = step("delete_device", lambda: m.tool("delete_device", track_name="WigAI test audio", device_index=0))
    check("delete_device", bool(r) and [d["name"] for d in r["devices"]] == ["EQ+"], r)

    print("clips")
    r = step("create_clip", lambda: m.tool("create_clip", track_name="WigAI test inst", slot_index=0, length_beats=4))
    check("create_clip", bool(r) and r["created"], r)
    r = step("write_notes", lambda: m.tool("write_notes", track_name="WigAI test inst", slot_index=0, clear_first=True,
                                          notes=[{"start": 0, "pitch": 60, "duration": 1}, {"start": 1, "pitch": 64, "velocity": 90, "duration": 1}]))
    check("write_notes", bool(r) and r["notes_written"] == 2, r)
    r = step("delete_clip", lambda: m.tool("delete_clip", track_name="WigAI test inst", slot_index=0))
    check("delete_clip", bool(r) and r["deleted"], r)

    midi = os.path.join(tempfile.mkdtemp(prefix="wigai-"), "chord.mid")
    write_midi(midi)
    r = step("insert_file midi into slot", lambda: m.tool("insert_file", track_name="WigAI test inst", slot_index=1, path=midi))
    check("insert_file midi into slot", bool(r) and r["inserted"], r)

    print("application")
    r = step("list_actions", lambda: m.tool("list_actions", filter="metronome"))
    check("list_actions filter", bool(r) and len(r) > 0, r)
    st = m.tool("status")["transport"]
    r = step("set_transport metronome toggle", lambda: m.tool("set_transport", metronome=not st["metronome_active"]))
    check("set_transport metronome", bool(r) and r["metronome"] == (not st["metronome_active"]), r)
    m.tool("set_transport", metronome=st["metronome_active"])

    if not args.keep:
        print("cleanup")
        for name in created:
            step(f"delete_track {name}", lambda name=name: m.tool("delete_track", track_name=name))
        n_after = len([t for t in m.tool("list_tracks") if t["type"] not in ("master", "effect")])
        check("track count restored", n_after == n_before, f"{n_before} -> {n_after}")
    return finish()


def finish():
    print(f"\n{len(PASS)} passed, {len(FAIL)} failed")
    for f in FAIL:
        print("  failed:", f)
    return 1 if FAIL else 0


if __name__ == "__main__":
    sys.exit(main())
