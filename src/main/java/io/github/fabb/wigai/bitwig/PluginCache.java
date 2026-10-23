package io.github.fabb.wigai.bitwig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Reads the plugin metadata Bitwig caches after scanning plugins ({@code ~/.BitwigStudio/cache/clap-metadata-N},
 * {@code vst3-metadata-N}, {@code vst-metadata-N}), so the installed plugins are known without loading any of them.
 *
 * <p>Each plugin file is a "BtWg" record of tagged fields: a 4-byte tag, a type byte (0x08 string, 0x19 string
 * array, 0x03 int) and the value. One plugin file can describe several plugins; each one starts with its
 * category field. The format is Bitwig's own and undocumented, so unknown bytes are skipped.
 */
public final class PluginCache {

    public record Plugin(String kind, String id, String name, String vendor, String category, String version,
                         List<String> features, String file) {}

    private static final int TAG_PATH = 0x3ec;
    private static final int TAG_CATEGORY = 0x3e3;
    private static final int TAG_NAME = 0x3e4;
    private static final int TAG_VENDOR = 0x3e6;
    private static final int TAG_VST_ID = 0x3e2;
    private static final int TAG_VST_VERSION = 0xc58;
    private static final int TAG_CLAP_ID = 0x2ec4;
    private static final int TAG_CLAP_VERSION = 0x2ec5;
    private static final int TAG_CLAP_FEATURES = 0x34f8;
    private static final int TYPE_INT = 0x03;
    private static final int TYPE_STRING = 0x08;
    private static final int TYPE_STRINGS = 0x19;
    private static final int MAX_STRING = 4096;
    private static final byte[] RECORD = "BtWg".getBytes(StandardCharsets.US_ASCII);

    /** Cache file prefix per plugin kind; the newest numbered file of each is read. */
    private static final Map<String, String> FILES = Map.of("clap", "clap-metadata-", "vst3", "vst3-metadata-",
        "vst2", "vst-metadata-");

    private PluginCache() {}

    public static Path defaultDir() {
        return Path.of(System.getProperty("user.home"), ".BitwigStudio", "cache");
    }

    /** Every plugin in the newest cache file of each kind; kinds without a cache file are skipped. */
    public static List<Plugin> read(Path dir) {
        List<Plugin> out = new ArrayList<>();
        for (String kind : List.of("clap", "vst3", "vst2")) {
            Path f = newest(dir, FILES.get(kind));
            if (f != null) {
                try {
                    out.addAll(parse(kind, Files.readAllBytes(f)));
                } catch (IOException e) {
                    // An unreadable cache just means those plugins are not listed.
                }
            }
        }
        return out;
    }

    static Path newest(Path dir, String prefix) {
        if (!Files.isDirectory(dir)) {
            return null;
        }
        Pattern p = Pattern.compile(Pattern.quote(prefix) + "(\\d+)");
        Path best = null;
        int bestN = -1;
        try (Stream<Path> files = Files.list(dir)) {
            for (Path f : (Iterable<Path>) files::iterator) {
                Matcher m = p.matcher(f.getFileName().toString());
                if (m.matches() && Integer.parseInt(m.group(1)) > bestN) {
                    bestN = Integer.parseInt(m.group(1));
                    best = f;
                }
            }
        } catch (IOException e) {
            return null;
        }
        return best;
    }

    /** One plugin under construction; fields are filled in the order the cache lists them. */
    private static final class Builder {
        final String file;
        final String category;
        String id;
        String name;
        String vendor;
        String version;
        List<String> features = List.of();

        Builder(String file, String category) {
            this.file = file;
            this.category = category;
        }
    }

    static List<Plugin> parse(String kind, byte[] b) {
        List<Plugin> out = new ArrayList<>();
        for (int[] range : records(b)) {
            String path = null;
            Builder cur = null;
            int i = range[0];
            while (i + 9 <= range[1]) {
                int tag = readInt(b, i);
                int type = b[i + 4] & 0xff;
                int n = readInt(b, i + 5);
                if (type == TYPE_STRING && isTag(tag) && n >= 0 && n < MAX_STRING && i + 9 + n <= range[1]) {
                    String v = new String(b, i + 9, n, StandardCharsets.UTF_8);
                    switch (tag) {
                        case TAG_PATH -> path = v;
                        case TAG_CATEGORY -> {
                            add(out, kind, cur);
                            cur = new Builder(path, v);
                        }
                        case TAG_NAME -> { if (cur != null && cur.name == null) cur.name = v; }
                        case TAG_VENDOR -> { if (cur != null && cur.vendor == null) cur.vendor = v; }
                        case TAG_CLAP_ID, TAG_VST_ID -> { if (cur != null && cur.id == null) cur.id = v; }
                        case TAG_CLAP_VERSION, TAG_VST_VERSION -> { if (cur != null && cur.version == null) cur.version = v; }
                        default -> { }
                    }
                    i += 9 + n;
                    continue;
                }
                if (type == TYPE_INT && tag == TAG_VST_ID && cur != null && cur.id == null) {
                    cur.id = String.valueOf(n);
                    i += 9;
                    continue;
                }
                if (type == TYPE_STRINGS && tag == TAG_CLAP_FEATURES && cur != null && n > 0 && n < 128) {
                    List<String> list = new ArrayList<>();
                    int j = i + 9;
                    for (int k = 0; k < n && j + 4 <= range[1]; k++) {
                        int len = readInt(b, j);
                        if (len < 0 || len > MAX_STRING || j + 4 + len > range[1]) {
                            break;
                        }
                        list.add(new String(b, j + 4, len, StandardCharsets.UTF_8));
                        j += 4 + len;
                    }
                    cur.features = List.copyOf(list);
                    i = j;
                    continue;
                }
                i++;
            }
            add(out, kind, cur);
        }
        return out;
    }

    private static boolean isTag(int tag) {
        return tag == TAG_PATH || tag == TAG_CATEGORY || tag == TAG_NAME || tag == TAG_VENDOR || tag == TAG_VST_ID
            || tag == TAG_VST_VERSION || tag == TAG_CLAP_ID || tag == TAG_CLAP_VERSION;
    }

    private static void add(List<Plugin> out, String kind, Builder p) {
        if (p != null && p.id != null && !p.id.isEmpty() && p.name != null) {
            out.add(new Plugin(kind, p.id, p.name, p.vendor, p.category, p.version == null || p.version.isEmpty()
                ? null : p.version, p.features, p.file));
        }
    }

    /** Byte ranges between successive "BtWg" markers. */
    private static List<int[]> records(byte[] b) {
        List<Integer> starts = new ArrayList<>();
        outer:
        for (int i = 0; i + RECORD.length <= b.length; i++) {
            for (int j = 0; j < RECORD.length; j++) {
                if (b[i + j] != RECORD[j]) {
                    continue outer;
                }
            }
            starts.add(i + RECORD.length);
        }
        List<int[]> ranges = new ArrayList<>();
        for (int k = 0; k < starts.size(); k++) {
            int end = k + 1 < starts.size() ? starts.get(k + 1) - RECORD.length : b.length;
            ranges.add(new int[]{starts.get(k), end});
        }
        return ranges;
    }

    private static int readInt(byte[] b, int i) {
        return ((b[i] & 0xff) << 24) | ((b[i + 1] & 0xff) << 16) | ((b[i + 2] & 0xff) << 8) | (b[i + 3] & 0xff);
    }
}
