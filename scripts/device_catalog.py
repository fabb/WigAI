#!/usr/bin/env python3
"""Build a catalog of every Bitwig device and plugin installed on this machine.

Two steps:

  inventory  What is installed, read from disk only: Bitwig's built-in devices (names and UUIDs the
             extension can insert) and every CLAP plugin's descriptor (id, name, vendor, description,
             feature tags), read from the plugin files themselves. LV2 plugins are listed for Carla.
  scan       What each device's controls are and what their values mean, measured in a running
             Bitwig through the WigAI MCP server: every remote-control page, each control's display
             at 0/25/50/75/100%, the options of switch-like controls, and the chains inside devices.
             Needs an EMPTY project open; it inserts and deletes devices on a scratch track.
  docs       Copy each Bitwig device's description from Bitwig's online user guide into the local catalog.
  render     Write the catalog as Markdown.

The catalog lives in ~/.local/share/wigai/ (override with WIGAI_CATALOG_DIR). The extension's
describe_device / list_installed_devices tools read it from there.

usage:
  python3 scripts/device_catalog.py inventory
  python3 scripts/device_catalog.py scan [--only NAME_SUBSTRING] [--kind bitwig|clap] [--rescan]
  python3 scripts/device_catalog.py docs       (copy Bitwig's device descriptions from its online user guide)
  python3 scripts/device_catalog.py render
  python3 scripts/device_catalog.py update-resource   (maintainers: refresh src/main/resources/bitwig-devices.txt)
"""
import argparse
import ctypes
import glob
import json
import os
import re
import struct
import subprocess
import sys
import time
import urllib.request

CATALOG_DIR = os.environ.get("WIGAI_CATALOG_DIR", os.path.expanduser("~/.local/share/wigai"))
CATALOG = os.path.join(CATALOG_DIR, "device-catalog.json")
MARKDOWN = os.path.join(CATALOG_DIR, "device-catalog.md")
REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BITWIG_DEVICES = os.path.join(REPO, "src/main/resources/bitwig-devices.txt")
BITWIG_ROOT = os.environ.get("BITWIG_ROOT", "/opt/bitwig-studio")
CLAP_DIRS = ["/usr/lib/clap", "/usr/local/lib/clap", os.path.expanduser("~/.clap")]
LV2_DIRS = ["/usr/lib/lv2", "/usr/local/lib/lv2", os.path.expanduser("~/.lv2")]
SWEEP = [0.0, 0.25, 0.5, 0.75, 1.0]
ENUM_SWEEP = [i / 16 for i in range(17)]
SCRATCH_TRACK = "wigai catalog scan"


# ---------------------------------------------------------------- catalog file

def load():
    if os.path.exists(CATALOG):
        with open(CATALOG, encoding="utf-8") as f:
            return json.load(f)
    return {"bitwig": {}, "clap": {}, "lv2": {}}


def save(cat):
    os.makedirs(CATALOG_DIR, exist_ok=True)
    tmp = CATALOG + ".tmp"
    with open(tmp, "w", encoding="utf-8") as f:
        json.dump(cat, f, indent=1, sort_keys=True)
    os.replace(tmp, CATALOG)


# ---------------------------------------------------------------- inventory

class ClapVersion(ctypes.Structure):
    _fields_ = [("major", ctypes.c_uint32), ("minor", ctypes.c_uint32), ("revision", ctypes.c_uint32)]


class ClapDescriptor(ctypes.Structure):
    _fields_ = [("clap_version", ClapVersion)] + [(n, ctypes.c_char_p) for n in
                ("id", "name", "vendor", "url", "manual_url", "support_url", "version", "description")] + \
               [("features", ctypes.POINTER(ctypes.c_char_p))]


class ClapFactory(ctypes.Structure):
    pass


ClapFactory._fields_ = [
    ("get_plugin_count", ctypes.CFUNCTYPE(ctypes.c_uint32, ctypes.POINTER(ClapFactory))),
    ("get_plugin_descriptor", ctypes.CFUNCTYPE(ctypes.POINTER(ClapDescriptor), ctypes.POINTER(ClapFactory), ctypes.c_uint32)),
]


class ClapEntry(ctypes.Structure):
    _fields_ = [("clap_version", ClapVersion),
                ("init", ctypes.CFUNCTYPE(ctypes.c_bool, ctypes.c_char_p)),
                ("deinit", ctypes.CFUNCTYPE(None)),
                ("get_factory", ctypes.CFUNCTYPE(ctypes.c_void_p, ctypes.c_char_p))]


def clap_descriptors(path):
    """Runs in a child process (see read_clap) so a plugin that crashes on load cannot take us down."""
    lib = ctypes.CDLL(path, mode=ctypes.RTLD_LOCAL)
    entry = ClapEntry.in_dll(lib, "clap_entry")
    if not entry.init(path.encode()):
        return []
    try:
        ptr = entry.get_factory(b"clap.plugin-factory")
        if not ptr:
            return []
        factory = ctypes.cast(ptr, ctypes.POINTER(ClapFactory))
        out = []
        for i in range(factory.contents.get_plugin_count(factory)):
            d = factory.contents.get_plugin_descriptor(factory, i).contents
            feats, j = [], 0
            while d.features and d.features[j]:
                feats.append(d.features[j].decode(errors="replace"))
                j += 1
            s = lambda b: b.decode(errors="replace") if b else ""  # noqa: E731
            out.append({"id": s(d.id), "name": s(d.name), "vendor": s(d.vendor), "version": s(d.version),
                        "description": s(d.description), "url": s(d.url), "manual_url": s(d.manual_url),
                        "features": feats, "path": path})
        return out
    finally:
        entry.deinit()


def read_clap(path):
    code = ("import sys, json; sys.path.insert(0, %r); import device_catalog as d; "
            "print(json.dumps(d.clap_descriptors(%r)))") % (os.path.dirname(os.path.abspath(__file__)), path)
    try:
        r = subprocess.run([sys.executable, "-I", "-c", code], capture_output=True, text=True, timeout=60)
        lines = [l for l in r.stdout.splitlines() if l.startswith("[")]
        return json.loads(lines[-1]) if lines else []
    except Exception as e:  # noqa: BLE001
        print(f"  could not read {path}: {e}", file=sys.stderr)
        return []


def lv2_plugins():
    out = {}
    for base in LV2_DIRS:
        for bundle in sorted(glob.glob(os.path.join(base, "*.lv2"))):
            for ttl in glob.glob(os.path.join(bundle, "*.ttl")):
                text = open(ttl, encoding="utf-8", errors="replace").read()
                if "lv2:Plugin" not in text and "a lv2:Plugin" not in text:
                    continue
                for uri, body in re.findall(r"<([^>]+)>\s*\n?\s*a\s+[^;]*lv2:\w*Plugin(.*?)\s\.\s*$", text, re.S | re.M):
                    name = re.search(r'doap:name\s+"([^"]+)"', body)
                    comment = re.search(r'rdfs:comment\s+"""(.*?)"""|rdfs:comment\s+"([^"]*)"', body, re.S)
                    out[uri] = {"uri": uri, "name": name.group(1) if name else os.path.basename(bundle)[:-4],
                                "description": (comment.group(1) or comment.group(2)).strip() if comment else "",
                                "bundle": bundle}
    return out


def library_devices(root=BITWIG_ROOT):
    """Bitwig's own devices, from the metadata header of each default preset in its library:
    a length-prefixed key followed by 0x08 and a length-prefixed UTF-8 value."""
    out = {}
    for path in glob.glob(os.path.join(root, "Library/device-settings/*/Default.bwpreset")):
        with open(path, "rb") as f:
            head = f.read(4096)
        meta = {}
        for key in ("device_id", "device_name", "device_type", "device_category"):
            k = key.encode()
            i = head.find(struct.pack(">I", len(k)) + k)
            j = i + 4 + len(k)
            if i >= 0 and head[j] == 0x08:
                n = struct.unpack(">I", head[j + 1:j + 5])[0]
                meta[key] = head[j + 5:j + 5 + n].decode("utf-8", "replace")
        if meta.get("device_id") and meta.get("device_name"):
            out[meta["device_name"]] = meta
    return out


TYPES = {"audio_to_audio": "audio effect", "note_to_audio": "instrument", "note_to_note": "note effect"}


def update_resource():
    """Regenerates the bundled UUID list the extension falls back to when Bitwig's library is unreadable."""
    devices = library_devices()
    with open(BITWIG_DEVICES, "w", encoding="utf-8") as f:
        f.write("# Bitwig built-in devices (Name$uuid), generated from Bitwig Studio's Library/device-settings by\n"
                "# scripts/device_catalog.py update-resource. The extension also reads that library at startup.\n")
        for name in sorted(devices, key=str.lower):
            f.write(f"{name}${devices[name]['device_id']}\n")
    print(f"wrote {len(devices)} devices to {BITWIG_DEVICES}")


def inventory():
    cat = load()
    for name, meta in sorted(library_devices().items()):
        e = cat["bitwig"].setdefault(name, {})
        e.update({"name": name, "uuid": meta["device_id"], "insertable": True,
                  "type": TYPES.get(meta.get("device_type"), meta.get("device_type")),
                  "category": meta.get("device_category", "")})
    for base in CLAP_DIRS:
        for path in sorted(glob.glob(os.path.join(base, "*.clap"))):
            for d in read_clap(path):
                cat["clap"].setdefault(d["id"], {}).update(d)
    cat["lv2"] = lv2_plugins()
    cat["inventory_time"] = time.strftime("%Y-%m-%d %H:%M:%S")
    save(cat)
    print(f"bitwig devices: {len(cat['bitwig'])} ({sum(1 for e in cat['bitwig'].values() if e['insertable'])} insertable), "
          f"clap plugins: {len(cat['clap'])}, lv2 plugins (Carla): {len(cat['lv2'])}")
    print("written:", CATALOG)


# ---------------------------------------------------------------- Bitwig's own device descriptions

USERGUIDE = "https://www.bitwig.com/userguide/latest/"
STOP_AT = {"modulator", "modulators", "grid_modules", "the_grid", "legacy_devices", "credits"}


def html_text(fragment):
    import html
    return html.unescape(re.sub(r"\s+", " ", re.sub("<[^>]+>", " ", fragment))).strip()


def fetch_docs():
    """Copies each device's description from Bitwig's online user guide into the local catalog.

    The text is Bitwig's; it is fetched onto the user's machine for reference and not redistributed."""
    cat = load()
    index = urllib.request.urlopen(USERGUIDE + "device_descriptions", timeout=60).read().decode("utf-8", "replace")
    links = re.findall(r'href="/userguide/latest/([^"#]+)"', index)
    pages = []
    for link in links[links.index("device_descriptions") + 1:]:
        if link in STOP_AT:
            break
        if link not in pages:
            pages.append(link)
    found = 0
    for page in pages:
        text = urllib.request.urlopen(USERGUIDE + page, timeout=60).read().decode("utf-8", "replace")
        body = text[text.find("<main"):] if "<main" in text else text
        for part in re.split(r"<h3[^>]*>", body)[1:]:
            name = re.sub(r"^[\d.\s\xa0]+", "", html_text(part.split("</h3>")[0]))
            paras = [html_text(x) for x in re.findall(r"<p[^>]*>(.*?)</p>", part, re.S)]
            paras = [x for x in paras if x and "Was this helpful" not in x]
            if name in cat["bitwig"] and paras:
                cat["bitwig"][name]["bitwig_docs"] = {"text": " ".join(paras[:6])[:1500], "url": USERGUIDE + page}
                found += 1
        time.sleep(0.3)
    cat["docs_time"] = time.strftime("%Y-%m-%d %H:%M:%S")
    save(cat)
    missing = [n for n, e in cat["bitwig"].items() if "bitwig_docs" not in e]
    print(f"descriptions for {found} Bitwig devices from {len(pages)} user-guide pages; without one: {', '.join(missing) or 'none'}")


# ---------------------------------------------------------------- live scan

class Mcp:
    def __init__(self, url):
        self.url, self.session, self.n = url, None, 0
        self.call("initialize", {"protocolVersion": "2025-03-26", "capabilities": {},
                                 "clientInfo": {"name": "device-catalog", "version": "1"}})
        self.post({"jsonrpc": "2.0", "method": "notifications/initialized"})

    def post(self, body):
        req = urllib.request.Request(self.url, json.dumps(body).encode(), method="POST", headers={
            "Content-Type": "application/json", "Accept": "application/json, text/event-stream",
            **({"mcp-session-id": self.session} if self.session else {})})
        with urllib.request.urlopen(req, timeout=120) as r:
            self.session = r.headers.get("mcp-session-id") or self.session
            text = r.read().decode()
        data = [l[6:] for l in text.splitlines() if l.startswith("data: ")]
        return json.loads(data[0]) if data else (json.loads(text) if text.strip() else None)

    def call(self, method, params):
        self.n += 1
        r = self.post({"jsonrpc": "2.0", "id": self.n, "method": method, "params": params})
        if "error" in r:
            raise RuntimeError(r["error"])
        return r["result"]

    def tool(self, tool_name, **args):
        res = self.call("tools/call", {"name": tool_name, "arguments": args})
        body = json.loads(res["content"][0]["text"])
        if body.get("status") != "success":
            raise RuntimeError(f"{tool_name}: {body.get('error', body)}")
        return body["data"]


NUM = re.compile(r"^\s*([+\-−]?\d+(?:\.\d+)?)\s*(.*)$")


def numeric(display):
    m = NUM.match(display or "")
    return (float(m.group(1).replace("−", "-")), m.group(2).strip()) if m else None


def describe_control(samples):
    """samples: [(value, display)] -> summary of range, unit and options."""
    displays = [d for _, d in samples]
    nums = [numeric(d) for d in displays]
    if all(n is not None for n in nums):
        units = sorted({u for _, u in nums if u})
        return {"kind": "continuous", "min": displays[0], "max": displays[-1],
                "curve": [d for d in displays], "units": units}
    options = []
    for d in displays:
        if d not in options:
            options.append(d)
    return {"kind": "options", "options": options}


def scan_device(m, track, kind, key, entry, rescan):
    if entry.get("scan") and "error" not in entry["scan"] and not rescan:
        return False
    args = {"track_index": track}
    if kind == "bitwig":
        args["bitwig_device"] = entry["name"]
    else:
        args["clap_id"] = key
    r = m.tool("insert_device", position="end", **args)
    if not r["inserted"]:
        entry["scan"] = {"error": "Bitwig did not insert it", "time": time.strftime("%Y-%m-%d %H:%M:%S")}
        return True
    devices = m.tool("list_devices_on_track", track_index=track)
    di = len(devices) - 1
    entry["device_type"] = devices[di]["type"]
    scan = {"time": time.strftime("%Y-%m-%d %H:%M:%S"), "pages": []}
    try:
        try:
            chains = m.tool("get_device_chains", track_index=track, device_index=di)["chains"]
            scan["chains"] = [{"chain": c["chain"], "default_devices": [d["name"] for d in c["devices"]]} for c in chains]
        except RuntimeError:
            scan["chains"] = []
        base = m.tool("get_device_parameters", track_index=track, device_index=di)
        deadline = time.time() + (4 if kind == "clap" else 0)
        while not base["page_names"] and time.time() < deadline:  # plugins publish their pages after loading
            time.sleep(0.5)
            base = m.tool("get_device_parameters", track_index=track, device_index=di)
        scan["page_names"] = base["page_names"]
        controls = [(pg["page"], p["index"], p["name"], p["value"], p["display"])
                    for pg in base["pages"] for p in pg["parameters"]]
        samples = {(pg, ix): [] for pg, ix, *_ in controls}
        if not controls:
            scan["note"] = ("Bitwig exposes no remote-control pages for this device, so its parameters cannot be "
                            "reached through the API (plugins only get pages when they define them).")
            entry["scan"] = scan
            return True
        for x in SWEEP:
            m.tool("set_device_parameters", track_index=track, device_index=di, verify=False,
                   parameters=[{"page": pg, "index": ix, "value": x} for pg, ix, *_ in controls])
            now = m.tool("get_device_parameters", track_index=track, device_index=di)
            for pg in now["pages"]:
                for p in pg["parameters"]:
                    if (pg["page"], p["index"]) in samples:
                        samples[(pg["page"], p["index"])].append((x, p["display"]))
        pages_with_options = {pg for (pg, ix), s in samples.items() if describe_control(s)["kind"] == "options"}
        for pg_index in sorted(pages_with_options):
            page_controls = [(pg, ix) for pg, ix, *_ in controls if pg == pg_index
                             and describe_control(samples[(pg, ix)])["kind"] == "options"]
            dense = {k: [] for k in page_controls}
            for x in ENUM_SWEEP:
                m.tool("set_device_parameters", track_index=track, device_index=di, verify=False,
                       parameters=[{"page": pg, "index": ix, "value": x} for pg, ix in page_controls])
                now = m.tool("get_device_parameters", track_index=track, device_index=di, page=pg_index)
                for p in now["pages"][0]["parameters"]:
                    if (pg_index, p["index"]) in dense:
                        dense[(pg_index, p["index"])].append((x, p["display"]))
            samples.update(dense)
        by_page = {}
        seen = set()
        for pg, ix, name, value, display in controls:
            c = {"index": ix, "name": name, "default_value": round(value, 4), "default": display}
            c.update(describe_control(samples[(pg, ix)]))
            c["also_on_other_page"] = name in seen
            seen.add(name)
            by_page.setdefault(pg, []).append(c)
        scan["pages"] = [{"page": pg, "page_name": scan["page_names"][pg] if pg < len(scan["page_names"]) else "",
                          "controls": by_page[pg]} for pg in sorted(by_page)]
    finally:
        m.tool("delete_device", track_index=track, device_index=di)
    entry["scan"] = scan
    return True


def scan(args):
    cat = load()
    if not cat["bitwig"] and not cat["clap"]:
        sys.exit("run 'inventory' first")
    m = Mcp(args.url)
    tracks = [t for t in m.tool("list_tracks") if t["type"] not in ("master", "effect")]
    scratch = [t for t in tracks if t["name"] == SCRATCH_TRACK]
    if not scratch and len(tracks) > 2 and not args.force:
        sys.exit(f"Refusing to scan: the open project has {len(tracks)} tracks. Open an empty project (File > New).")
    if scratch:
        track = scratch[0]["index"]
    else:
        track = m.tool("create_track", type="instrument", name=SCRATCH_TRACK)["track_index"]
    for existing in reversed(m.tool("list_devices_on_track", track_index=track)):
        m.tool("delete_device", track_index=track, device_index=existing["index"])
    jobs = []
    if args.kind in (None, "bitwig"):
        jobs += [("bitwig", k, e) for k, e in sorted(cat["bitwig"].items()) if e.get("insertable")]
    if args.kind in (None, "clap"):
        jobs += [("clap", k, e) for k, e in sorted(cat["clap"].items())]
    if args.only:
        jobs = [j for j in jobs if args.only.lower() in (j[2].get("name") or j[1]).lower()]
    start = time.time()
    for n, (kind, key, entry) in enumerate(jobs, 1):
        label = entry.get("name") or key
        t0 = time.time()
        try:
            did = scan_device(m, track, kind, key, entry, args.rescan)
        except Exception as e:  # noqa: BLE001
            entry["scan"] = {"error": str(e)[:500], "time": time.strftime("%Y-%m-%d %H:%M:%S")}
            did = True
            for existing in reversed(m.tool("list_devices_on_track", track_index=track)):
                m.tool("delete_device", track_index=track, device_index=existing["index"])
        if did:
            save(cat)
            status = "error: " + entry["scan"]["error"][:80] if "error" in entry["scan"] else \
                f"{sum(len(p['controls']) for p in entry['scan']['pages'])} controls"
            print(f"[{n}/{len(jobs)}] {kind} {label}: {status} ({time.time() - t0:.0f}s, {(time.time() - start) / 60:.0f} min total)",
                  flush=True)
    m.tool("delete_track", track_index=track)
    print("done:", CATALOG)


# ---------------------------------------------------------------- markdown

def render():
    cat = load()
    out = ["# Installed devices and plugins", "",
           f"Generated by `scripts/device_catalog.py` (inventory {cat.get('inventory_time', '?')}). "
           "Values are what Bitwig displays for each control at 0/25/50/75/100% of its range.", ""]

    def section(title, entries, header_fn):
        out.extend([f"## {title}", ""])
        for key, e in entries:
            out.append(f"### {e.get('name') or key}")
            out.extend(header_fn(key, e))
            s = e.get("scan")
            if not s:
                out.extend(["*Not scanned yet.*", ""])
                continue
            if "error" in s:
                out.extend([f"*Scan failed: {s['error']}*", ""])
                continue
            if e.get("device_type"):
                out.append(f"Type: {e['device_type']}")
            for c in s.get("chains", []):
                out.append(f"Chain **{c['chain']}**" + (f" (default: {', '.join(c['default_devices'])})" if c["default_devices"] else ""))
            out.append("")
            for pg in s["pages"]:
                out.append(f"**{pg['page_name'] or 'Page ' + str(pg['page'])}**")
                for c in pg["controls"]:
                    if c.get("also_on_other_page"):
                        continue
                    if c["kind"] == "continuous":
                        out.append(f"- {c['name']}: {c['min']} to {c['max']} (default {c['default']}; curve {' / '.join(c['curve'])})")
                    else:
                        out.append(f"- {c['name']}: {', '.join(c['options'])} (default {c['default']})")
                out.append("")
    section("Bitwig devices", sorted((k, e) for k, e in cat["bitwig"].items() if e.get("insertable")),
            lambda k, e: [])
    section("CLAP plugins", sorted(cat["clap"].items(), key=lambda kv: (kv[1].get("vendor", ""), kv[1].get("name", ""))),
            lambda k, e: [f"{e.get('vendor', '')} {e.get('version', '')} · `{k}` · {', '.join(e.get('features', []))}",
                          "", e.get("description", "") or ""])
    out.extend(["## LV2 plugins (usable through Carla)", ""])
    for uri, e in sorted(cat.get("lv2", {}).items(), key=lambda kv: kv[1]["name"]):
        out.append(f"- **{e['name']}** — {e['description'][:200]}" if e["description"] else f"- **{e['name']}**")
    os.makedirs(CATALOG_DIR, exist_ok=True)
    with open(MARKDOWN, "w", encoding="utf-8") as f:
        f.write("\n".join(out) + "\n")
    print("written:", MARKDOWN)


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = ap.add_subparsers(dest="cmd", required=True)
    sub.add_parser("inventory")
    sc = sub.add_parser("scan")
    sc.add_argument("--url", default="http://localhost:61169/mcp")
    sc.add_argument("--only")
    sc.add_argument("--kind", choices=["bitwig", "clap"])
    sc.add_argument("--rescan", action="store_true")
    sc.add_argument("--force", action="store_true")
    sub.add_parser("render")
    sub.add_parser("update-resource")
    sub.add_parser("docs")
    a = ap.parse_args()
    {"inventory": inventory, "scan": lambda: scan(a), "render": render, "update-resource": update_resource,
     "docs": fetch_docs}[a.cmd]()


if __name__ == "__main__":
    main()
