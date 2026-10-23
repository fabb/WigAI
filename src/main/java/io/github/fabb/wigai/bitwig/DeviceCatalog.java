package io.github.fabb.wigai.bitwig;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.common.error.BitwigApiException;
import io.github.fabb.wigai.common.error.ErrorCode;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Knowledge about every device and plugin installed: what it is (from disk), what it is for (the bundled
 * device guide), and what its controls do (measured in Bitwig, on demand, then cached).
 *
 * The catalog file is shared with scripts/device_catalog.py, which writes the disk inventory (Bitwig
 * devices, CLAP descriptors, LV2 plugins) and can pre-fill scans in bulk. This class adds on-demand scans
 * of single devices and keeps a scan history per entry.
 */
public class DeviceCatalog {

    private static final double[] SWEEP = {0.0, 0.25, 0.5, 0.75, 1.0};
    private static final int OPTION_STEPS = 16;
    private static final int HISTORY_LIMIT = 20;
    private static final String SCRATCH_TRACK = "wigai scan (temporary)";
    private static final Pattern NUMBER = Pattern.compile("^\\s*([+\\-−]?\\d+(?:\\.\\d+)?)\\s*(.*)$");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Path file;
    private final BitwigEditFacade edit;
    private final Logger logger;
    private final String bitwigVersion;
    private final ObjectMapper json = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private final Map<String, Map<String, Object>> guide;
    private final Path pluginCacheDir;

    /** Catalog sections: Bitwig devices, plugins by format (from Bitwig's plugin cache), LV2 via Carla, other. */
    private static final List<String> SECTIONS = List.of("bitwig", "clap", "vst3", "vst2", "lv2", "other");

    public DeviceCatalog(Path file, BitwigEditFacade edit, Logger logger, String bitwigVersion) {
        this(file, edit, logger, bitwigVersion, PluginCache.defaultDir());
    }

    public DeviceCatalog(Path file, BitwigEditFacade edit, Logger logger, String bitwigVersion, Path pluginCacheDir) {
        this.pluginCacheDir = pluginCacheDir;
        this.file = file;
        this.edit = edit;
        this.logger = logger;
        this.bitwigVersion = bitwigVersion;
        this.guide = loadGuide(file.resolveSibling("device-guide.json"));
    }

    /** ~/.local/share/wigai/device-catalog.json, or $WIGAI_CATALOG_DIR/device-catalog.json. */
    public static Path defaultFile() {
        String dir = System.getenv("WIGAI_CATALOG_DIR");
        Path base = dir != null ? Path.of(dir) : Path.of(System.getProperty("user.home"), ".local", "share", "wigai");
        return base.resolve("device-catalog.json");
    }

    // ========================================
    // Storage
    // ========================================

    @SuppressWarnings("unchecked")
    synchronized Map<String, Object> load() {
        Map<String, Object> cat = new LinkedHashMap<>();
        if (Files.isRegularFile(file)) {
            try {
                cat = json.readValue(file.toFile(), new TypeReference<LinkedHashMap<String, Object>>() {});
            } catch (Exception e) {
                logger.error("DeviceCatalog: could not read " + file + ": " + e.getMessage());
            }
        }
        for (String section : SECTIONS) {
            cat.computeIfAbsent(section, k -> new LinkedHashMap<String, Object>());
        }
        for (PluginCache.Plugin p : PluginCache.read(pluginCacheDir)) {
            Map<String, Object> e = (Map<String, Object>) ((Map<String, Object>) cat.get(p.kind()))
                .computeIfAbsent(p.id(), k -> new LinkedHashMap<>());
            e.putIfAbsent("name", p.name());
            putIfPresent(e, "vendor", p.vendor());
            putIfPresent(e, "category", p.category());
            putIfPresent(e, "version", p.version());
            if (!p.features().isEmpty()) {
                e.putIfAbsent("features", p.features());
            }
            putIfPresent(e, "file", p.file());
        }
        Map<String, Object> bitwig = (Map<String, Object>) cat.get("bitwig");
        for (Map<String, Object> d : edit.listBitwigDevices(null)) {
            Map<String, Object> e = (Map<String, Object>) bitwig.computeIfAbsent((String) d.get("name"), k -> new LinkedHashMap<>());
            e.putIfAbsent("name", d.get("name"));
            e.putIfAbsent("type", d.get("type"));
            e.putIfAbsent("category", d.get("category"));
        }
        return cat;
    }

    synchronized void save(Map<String, Object> cat) {
        try {
            Files.createDirectories(file.getParent());
            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            json.writeValue(tmp.toFile(), cat);
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception e) {
            logger.error("DeviceCatalog: could not write " + file + ": " + e.getMessage());
        }
    }

    private record Found(String section, String key, Map<String, Object> entry) {}

    private static void putIfPresent(Map<String, Object> m, String key, String value) {
        if (value != null && !value.isEmpty()) {
            m.putIfAbsent(key, value);
        }
    }

    @SuppressWarnings("unchecked")
    private static Found find(Map<String, Object> cat, String query) {
        String q = query.trim().toLowerCase(Locale.ROOT);
        for (String section : SECTIONS) {
            Map<String, Object> entries = (Map<String, Object>) cat.get(section);
            for (Map.Entry<String, Object> e : entries.entrySet()) {
                Map<String, Object> entry = (Map<String, Object>) e.getValue();
                if (e.getKey().equalsIgnoreCase(q) || q.equalsIgnoreCase(String.valueOf(entry.get("name")))) {
                    return new Found(section, e.getKey(), entry);
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    /** Optional user-supplied notes per device, next to the catalog; nothing editorial ships with the extension. */
    private static Map<String, Map<String, Object>> loadGuide(Path guideFile) {
        if (!Files.isRegularFile(guideFile)) {
            return Map.of();
        }
        try (InputStream in = Files.newInputStream(guideFile)) {
            Map<String, Object> raw = new ObjectMapper().readValue(in, new TypeReference<LinkedHashMap<String, Object>>() {});
            Map<String, Map<String, Object>> out = new LinkedHashMap<>();
            raw.forEach((k, v) -> {
                if (!k.startsWith("_")) {
                    out.put(k.toLowerCase(Locale.ROOT), (Map<String, Object>) v);
                }
            });
            return out;
        } catch (Exception e) {
            return Map.of();
        }
    }

    private Map<String, Object> guideFor(String section, String key, Map<String, Object> entry) {
        Map<String, Object> g = guide.get(key.toLowerCase(Locale.ROOT));
        if (g == null && entry.get("name") != null) {
            g = guide.get(String.valueOf(entry.get("name")).toLowerCase(Locale.ROOT));
        }
        return g;
    }

    // ========================================
    // Listing and searching
    // ========================================

    /**
     * Everything installed, optionally filtered by words matched against name, vendor, type, category,
     * description, feature tags and the device guide. Use it to pick candidates for a job.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> listInstalled(String query, String kind) {
        Map<String, Object> cat = load();
        List<String> words = query == null ? List.of() : List.of(query.toLowerCase(Locale.ROOT).split("\\s+"));
        List<Map<String, Object>> out = new ArrayList<>();
        for (String section : SECTIONS) {
            if (kind != null && !kind.equalsIgnoreCase(section)) {
                continue;
            }
            for (Map.Entry<String, Object> e : ((Map<String, Object>) cat.get(section)).entrySet()) {
                Map<String, Object> entry = (Map<String, Object>) e.getValue();
                Map<String, Object> g = guideFor(section, e.getKey(), entry);
                String text = String.join(" ", e.getKey(), str(entry.get("name")), str(entry.get("vendor")),
                    str(entry.get("type")), str(entry.get("category")), str(entry.get("description")),
                    str(entry.get("features")), g == null ? "" : str(g.get("use_for")) + " " + str(g.get("tags")))
                    .toLowerCase(Locale.ROOT);
                int score = 0;
                for (String w : words) {
                    if (text.contains(w)) {
                        score++;
                    }
                }
                if (!words.isEmpty() && score == 0) {
                    continue;
                }
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("kind", section);
                m.put("name", entry.getOrDefault("name", e.getKey()));
                if (!"bitwig".equals(section)) {
                    m.put("id", e.getKey());
                    m.put("vendor", entry.get("vendor"));
                }
                m.put("type", section.equals("bitwig") ? entry.get("type")
                    : entry.get("features") != null ? str(entry.get("features")) : entry.get("category"));
                if (g != null) {
                    m.put("use_for", g.get("use_for"));
                } else if (entry.get("description") != null && !str(entry.get("description")).isBlank()) {
                    m.put("description", truncate(str(entry.get("description")), 160));
                }
                m.put("scanned", entry.get("scan") instanceof Map<?, ?> s && !s.containsKey("error"));
                m.put("score", score);
                out.add(m);
            }
        }
        out.sort(Comparator.comparingInt((Map<String, Object> m) -> -(int) m.get("score"))
            .thenComparing(m -> !"bitwig".equals(m.get("kind")))
            .thenComparing(m -> str(m.get("name")).toLowerCase(Locale.ROOT)));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("count", out.size());
        result.put("devices", out.size() > 150 ? out.subList(0, 150) : out);
        if (Stream.of("clap", "vst3", "vst2").allMatch(k -> ((Map<String, Object>) cat.get(k)).isEmpty())) {
            result.put("note", "No plugins found in Bitwig's plugin cache (" + pluginCacheDir
                + "). Bitwig fills it when it scans plugin folders (Settings > Locations).");
        }
        return result;
    }

    // ========================================
    // Describing (and scanning on demand)
    // ========================================

    /**
     * Everything known about a device: what it is, what it is for, and its controls. Without a cached scan
     * (or with rescan), the controls are measured now: on the given device instance when track/device are
     * given (values are restored afterwards), otherwise on a temporary track that is deleted again.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> describe(String name, Integer trackIndex, Integer deviceIndex, String chain,
                                        Integer chainDeviceIndex, boolean scanIfMissing, boolean rescan)
            throws BitwigApiException {
        final String operation = "describeDevice";
        Map<String, Object> cat = load();
        String instanceName = null;
        if (trackIndex != null && deviceIndex != null) {
            instanceName = (String) edit.getDeviceParameters(trackIndex, deviceIndex, chain, chainDeviceIndex, 0).get("device");
        }
        String query = name != null ? name : instanceName;
        if (query == null) {
            throw new BitwigApiException(ErrorCode.MISSING_REQUIRED_PARAMETER, operation, "Give name, or track and device_index");
        }
        Found found = find(cat, query);
        if (found == null) {
            Map<String, Object> other = (Map<String, Object>) cat.get("other");
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("name", query);
            other.put(query, entry);
            found = new Found("other", query, entry);
        }
        Map<String, Object> entry = found.entry();
        Object scan = entry.get("scan");
        boolean haveScan = scan instanceof Map<?, ?> s && !s.containsKey("error") && ((Map<?, ?>) s).containsKey("pages");
        String scanNote = null;
        if ((rescan || !haveScan) && scanIfMissing && !"lv2".equals(found.section())) {
            if (edit.isPlaying()) {
                scanNote = "Not scanned: the transport is playing and scanning moves every control. Stop playback and ask again.";
            } else {
                Map<String, Object> result = trackIndex != null && deviceIndex != null
                    ? scanInstance(trackIndex, deviceIndex, chain, chainDeviceIndex)
                    : scanFresh(found);
                recordScan(entry, result, trackIndex != null ? "on-demand (instance)" : "on-demand (temporary track)");
                save(cat);
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("kind", found.section());
        out.put("key", found.key());
        out.putAll(entry);
        Map<String, Object> g = guideFor(found.section(), found.key(), entry);
        if (g != null) {
            out.put("user_notes", g);
        }
        if (scanNote != null) {
            out.put("scan_note", scanNote);
        }
        if (out.get("scan_history") instanceof List<?> h && h.size() > 5) {
            out.put("scan_history", h.subList(h.size() - 5, h.size()));
        }
        return out;
    }

    private Map<String, Object> scanFresh(Found found) throws BitwigApiException {
        final String operation = "describeDevice";
        String bitwigName = "bitwig".equals(found.section()) ? str(found.entry().get("name")) : null;
        String clapId = "clap".equals(found.section()) ? found.key() : null;
        String vst3Id = "vst3".equals(found.section()) ? found.key() : null;
        Integer vst2Id = "vst2".equals(found.section()) ? Integer.valueOf(found.key()) : null;
        if (bitwigName == null && clapId == null && vst3Id == null && vst2Id == null) {
            throw new BitwigApiException(ErrorCode.DEVICE_NOT_FOUND, operation,
                "'" + found.key() + "' can only be scanned on an instance: insert it and pass track and device_index");
        }
        int track = (int) edit.createTrack("instrument", null, SCRATCH_TRACK).get("track_index");
        try {
            Map<String, Object> ins = edit.insertDevice(track, null, null, bitwigName, clapId, vst3Id, vst2Id, null,
                "end", null);
            if (!Boolean.TRUE.equals(ins.get("inserted"))) {
                Map<String, Object> err = new LinkedHashMap<>();
                err.put("error", "Bitwig did not insert it");
                return err;
            }
            for (int i = 0; i < 8; i++) { // plugins publish their pages after loading
                Map<String, Object> probe = edit.getDeviceParameters(track, 0, null, null, null);
                if (((String[]) probe.get("page_names")).length > 0 || clapId == null) {
                    break;
                }
                sleep(500);
            }
            return scanInstance(track, 0, null, null);
        } finally {
            edit.deleteTrack(track);
        }
    }

    /** Sweeps every control of one device instance and restores the values it had. */
    @SuppressWarnings("unchecked")
    Map<String, Object> scanInstance(int track, int device, String chain, Integer chainDevice) throws BitwigApiException {
        Map<String, Object> base = edit.getDeviceParameters(track, device, chain, chainDevice, null);
        Map<String, Object> scan = new LinkedHashMap<>();
        scan.put("page_names", base.get("page_names"));
        if (chain == null) {
            try {
                List<Map<String, Object>> chains = new ArrayList<>();
                for (Map<String, Object> c : (List<Map<String, Object>>) edit.getDeviceChains(track, device).get("chains")) {
                    List<Object> names = new ArrayList<>();
                    for (Map<String, Object> d : (List<Map<String, Object>>) c.get("devices")) {
                        names.add(d.get("name"));
                    }
                    chains.add(Map.of("chain", c.get("chain"), "default_devices", names));
                }
                scan.put("chains", chains);
            } catch (BitwigApiException e) {
                scan.put("chains", List.of());
            }
        }
        record Control(int page, int index, String name, double value, String display) {}
        List<Control> controls = new ArrayList<>();
        for (Map<String, Object> pg : (List<Map<String, Object>>) base.get("pages")) {
            for (Map<String, Object> p : (List<Map<String, Object>>) pg.get("parameters")) {
                controls.add(new Control((int) pg.get("page"), (int) p.get("index"), str(p.get("name")),
                    ((Number) p.get("value")).doubleValue(), str(p.get("display"))));
            }
        }
        if (controls.isEmpty()) {
            scan.put("pages", List.of());
            scan.put("note", "Bitwig exposes no remote-control pages for this device, so its parameters cannot be "
                + "reached through the API (plugins only get pages when they define them).");
            return scan;
        }
        Map<String, List<String>> samples = new LinkedHashMap<>();
        try {
            for (double x : SWEEP) {
                sweep(track, device, chain, chainDevice, controls.stream().map(c -> new int[]{c.page(), c.index()}).toList(), x, samples);
            }
            List<int[]> optionControls = new ArrayList<>();
            for (Control c : controls) {
                if (!isContinuous(samples.get(c.page() + ":" + c.index()))) {
                    optionControls.add(new int[]{c.page(), c.index()});
                    samples.get(c.page() + ":" + c.index()).clear();
                }
            }
            if (!optionControls.isEmpty()) {
                for (int i = 0; i <= OPTION_STEPS; i++) {
                    sweep(track, device, chain, chainDevice, optionControls, (double) i / OPTION_STEPS, samples);
                }
            }
        } finally {
            List<BitwigEditFacade.ParamAssignment> restore = new ArrayList<>();
            for (Control c : controls) {
                restore.add(new BitwigEditFacade.ParamAssignment(c.page(), c.index(), null, c.value()));
            }
            edit.setDeviceParameters(track, device, chain, chainDevice, restore, false);
        }
        Map<Integer, List<Map<String, Object>>> byPage = new LinkedHashMap<>();
        Set<String> seen = new LinkedHashSet<>();
        for (Control c : controls) {
            List<String> s = samples.get(c.page() + ":" + c.index());
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("index", c.index());
            m.put("name", c.name());
            m.put("default", c.display());
            m.put("default_value", Math.round(c.value() * 10000) / 10000.0);
            if (isContinuous(s)) {
                m.put("kind", "continuous");
                m.put("min", s.get(0));
                m.put("max", s.get(s.size() - 1));
                m.put("curve", s);
            } else {
                m.put("kind", "options");
                m.put("options", new ArrayList<>(new LinkedHashSet<>(s)));
            }
            m.put("also_on_other_page", !seen.add(c.name()));
            byPage.computeIfAbsent(c.page(), k -> new ArrayList<>()).add(m);
        }
        String[] pageNames = (String[]) base.get("page_names");
        List<Map<String, Object>> pages = new ArrayList<>();
        byPage.forEach((pg, list) -> pages.add(Map.of("page", pg, "page_name", pg < pageNames.length ? pageNames[pg] : "",
            "controls", list)));
        scan.put("pages", pages);
        return scan;
    }

    @SuppressWarnings("unchecked")
    private void sweep(int track, int device, String chain, Integer chainDevice, List<int[]> targets, double x,
                       Map<String, List<String>> samples) throws BitwigApiException {
        List<BitwigEditFacade.ParamAssignment> set = new ArrayList<>();
        Set<Integer> pages = new LinkedHashSet<>();
        for (int[] t : targets) {
            set.add(new BitwigEditFacade.ParamAssignment(t[0], t[1], null, x));
            pages.add(t[0]);
        }
        edit.setDeviceParameters(track, device, chain, chainDevice, set, false);
        Set<String> wanted = new LinkedHashSet<>();
        for (int[] t : targets) {
            wanted.add(t[0] + ":" + t[1]);
        }
        Map<String, Object> now = edit.getDeviceParameters(track, device, chain, chainDevice, pages.size() == 1 ? pages.iterator().next() : null);
        for (Map<String, Object> pg : (List<Map<String, Object>>) now.get("pages")) {
            for (Map<String, Object> p : (List<Map<String, Object>>) pg.get("parameters")) {
                String k = pg.get("page") + ":" + p.get("index");
                if (wanted.contains(k)) {
                    samples.computeIfAbsent(k, kk -> new ArrayList<>()).add(str(p.get("display")));
                }
            }
        }
    }

    /** A control is continuous when every sampled display is a number ("-Inf dB" counts as one). */
    static boolean isContinuous(List<String> displays) {
        if (displays == null || displays.isEmpty()) {
            return false;
        }
        for (String d : displays) {
            String s = d.replace('−', '-').trim();
            if (s.toLowerCase(Locale.ROOT).startsWith("-inf") || s.toLowerCase(Locale.ROOT).startsWith("inf")) {
                continue;
            }
            Matcher m = NUMBER.matcher(s);
            if (!m.matches()) {
                return false;
            }
        }
        return true;
    }

    // ========================================
    // Bitwig's own descriptions (fetched from its online user guide)
    // ========================================

    private static final String USERGUIDE = "https://www.bitwig.com/userguide/latest/";
    private static final Set<String> GUIDE_STOP = Set.of("modulator", "modulators", "grid_modules", "the_grid",
        "legacy_devices", "credits");

    /**
     * Copies each Bitwig device's description from Bitwig's online user guide into the local catalog
     * (field bitwig_docs). The text is Bitwig's; it is fetched onto the user's machine, not redistributed.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> fetchDocs() throws BitwigApiException {
        java.net.http.HttpClient http = java.net.http.HttpClient.newBuilder()
            .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
            .connectTimeout(java.time.Duration.ofSeconds(20)).build();
        Map<String, Object> cat = load();
        Map<String, Object> bitwig = (Map<String, Object>) cat.get("bitwig");
        List<String> pages = new ArrayList<>();
        int found = 0;
        try {
            String index = get(http, USERGUIDE + "device_descriptions");
            Matcher links = Pattern.compile("href=\"/userguide/latest/([^\"#]+)\"").matcher(index);
            boolean inChapter = false;
            while (links.find()) {
                String link = links.group(1);
                if (link.equals("device_descriptions")) {
                    inChapter = true;
                } else if (inChapter) {
                    if (GUIDE_STOP.contains(link)) {
                        break;
                    }
                    if (!pages.contains(link)) {
                        pages.add(link);
                    }
                }
            }
            for (String page : pages) {
                String html = get(http, USERGUIDE + page);
                int main = html.indexOf("<main");
                String body = main >= 0 ? html.substring(main) : html;
                String[] parts = body.split("<h3[^>]*>");
                for (int i = 1; i < parts.length; i++) {
                    String name = htmlText(parts[i].split("</h3>")[0]).replaceFirst("^[\\d.\\s\\u00a0]+", "");
                    List<String> paras = new ArrayList<>();
                    Matcher p = Pattern.compile("<p[^>]*>(.*?)</p>", Pattern.DOTALL).matcher(parts[i]);
                    while (p.find() && paras.size() < 6) {
                        String t = htmlText(p.group(1));
                        if (!t.isEmpty() && !t.contains("Was this helpful")) {
                            paras.add(t);
                        }
                    }
                    if (bitwig.get(name) instanceof Map<?, ?> entry && !paras.isEmpty()) {
                        String text = String.join(" ", paras);
                        ((Map<String, Object>) entry).put("bitwig_docs",
                            Map.of("text", text.length() > 1500 ? text.substring(0, 1500) : text, "url", USERGUIDE + page));
                        found++;
                    }
                }
            }
        } catch (Exception e) {
            throw new BitwigApiException(ErrorCode.OPERATION_FAILED, "fetchDeviceDocs",
                "Could not read Bitwig's user guide: " + e.getMessage());
        }
        cat.put("docs_time", LocalDateTime.now().format(TIME));
        save(cat);
        List<String> missing = new ArrayList<>();
        bitwig.forEach((k, v) -> {
            if (!(v instanceof Map<?, ?> m && m.containsKey("bitwig_docs"))) {
                missing.add(k);
            }
        });
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("pages_read", pages.size());
        out.put("devices_described", found);
        out.put("without_description", missing);
        return out;
    }

    private static String get(java.net.http.HttpClient http, String url) throws Exception {
        java.net.http.HttpResponse<String> r = http.send(
            java.net.http.HttpRequest.newBuilder(java.net.URI.create(url)).timeout(java.time.Duration.ofSeconds(60)).GET().build(),
            java.net.http.HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() != 200) {
            throw new IllegalStateException(url + " returned " + r.statusCode());
        }
        return r.body();
    }

    static String htmlText(String fragment) {
        String t = fragment.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
        return t.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"")
            .replace("&#39;", "'").replace("&nbsp;", " ").replace("&#8217;", "’");
    }

    // ========================================
    // Scan history
    // ========================================

    @SuppressWarnings("unchecked")
    void recordScan(Map<String, Object> entry, Map<String, Object> scan, String source) {
        String now = LocalDateTime.now().format(TIME);
        scan.put("time", now);
        scan.put("bitwig_version", bitwigVersion);
        scan.put("source", source);
        if (entry.get("version") != null) {
            scan.put("plugin_version", entry.get("version"));
        }
        Map<String, Object> previous = entry.get("scan") instanceof Map<?, ?> p ? (Map<String, Object>) p : null;
        Map<String, Object> h = new LinkedHashMap<>();
        h.put("time", now);
        h.put("source", source);
        h.put("bitwig_version", bitwigVersion);
        if (scan.containsKey("error")) {
            h.put("result", "error: " + scan.get("error"));
        } else {
            Map<String, String> after = controlSummary(scan);
            h.put("result", after.size() + " controls");
            if (previous != null && previous.containsKey("pages")) {
                Map<String, String> before = controlSummary(previous);
                List<String> changes = new ArrayList<>();
                after.forEach((k, v) -> {
                    if (!before.containsKey(k)) {
                        changes.add("added " + k);
                    } else if (!before.get(k).equals(v)) {
                        changes.add("changed " + k + ": " + before.get(k) + " -> " + v);
                    }
                });
                before.keySet().stream().filter(k -> !after.containsKey(k)).forEach(k -> changes.add("removed " + k));
                h.put("changes", changes.isEmpty() ? List.of("none") : changes);
            }
        }
        List<Object> history = entry.get("scan_history") instanceof List<?> l ? new ArrayList<>(l) : new ArrayList<>();
        history.add(h);
        while (history.size() > HISTORY_LIMIT) {
            history.remove(0);
        }
        entry.put("scan_history", history);
        entry.put("scan", scan);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> controlSummary(Map<String, Object> scan) {
        Map<String, String> out = new LinkedHashMap<>();
        for (Map<String, Object> pg : (List<Map<String, Object>>) scan.getOrDefault("pages", List.of())) {
            for (Map<String, Object> c : (List<Map<String, Object>>) pg.get("controls")) {
                String range = "options".equals(c.get("kind")) ? str(c.get("options")) : c.get("min") + ".." + c.get("max");
                out.putIfAbsent(str(c.get("name")), range);
            }
        }
        return out;
    }

    /** Coverage and freshness: how much is scanned, what failed, and what was scanned on an older Bitwig. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> status() {
        Map<String, Object> cat = load();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("catalog_file", file.toString());
        out.put("bitwig_version", bitwigVersion);
        out.put("inventory_time", cat.get("inventory_time"));
        List<Map<String, Object>> recent = new ArrayList<>();
        List<String> stale = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        for (String section : SECTIONS) {
            Map<String, Object> entries = (Map<String, Object>) cat.get(section);
            int scanned = 0;
            for (Map.Entry<String, Object> e : entries.entrySet()) {
                Map<String, Object> entry = (Map<String, Object>) e.getValue();
                if (entry.get("scan") instanceof Map<?, ?> s) {
                    if (s.containsKey("error")) {
                        failed.add(section + ":" + e.getKey());
                    } else {
                        scanned++;
                        if (bitwigVersion != null && s.get("bitwig_version") != null && !bitwigVersion.equals(s.get("bitwig_version"))) {
                            stale.add(section + ":" + e.getKey() + " (scanned on " + s.get("bitwig_version") + ")");
                        }
                    }
                }
                if (entry.get("scan_history") instanceof List<?> h) {
                    for (Object o : h) {
                        Map<String, Object> m = new LinkedHashMap<>((Map<String, Object>) o);
                        m.put("device", entry.getOrDefault("name", e.getKey()));
                        recent.add(m);
                    }
                }
            }
            out.put(section, Map.of("entries", entries.size(), "scanned", scanned));
        }
        recent.sort(Comparator.comparing((Map<String, Object> m) -> str(m.get("time"))).reversed());
        out.put("recent_scans", recent.size() > 15 ? recent.subList(0, 15) : recent);
        out.put("stale", stale);
        out.put("failed", failed);
        out.put("guide_entries", guide.size());
        return out;
    }

    private static String str(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private static String truncate(String s, int n) {
        return s.length() <= n ? s : s.substring(0, n - 1) + "…";
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
