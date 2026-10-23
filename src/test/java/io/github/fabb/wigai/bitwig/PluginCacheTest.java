package io.github.fabb.wigai.bitwig;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PluginCacheTest {

    /** Builds cache bytes in Bitwig's tagged format: 4-byte tag, type byte, then the value. */
    private static final class Cache {
        final ByteArrayOutputStream b = new ByteArrayOutputStream();

        Cache raw(byte... bytes) {
            b.writeBytes(bytes);
            return this;
        }

        Cache record() {
            return raw(new byte[]{0, 0, 0, 1}).raw("BtWg0001000200000000003900".getBytes(StandardCharsets.US_ASCII));
        }

        Cache str(int tag, String v) {
            byte[] s = v.getBytes(StandardCharsets.UTF_8);
            return raw(i(tag)).raw((byte) 0x08).raw(i(s.length)).raw(s);
        }

        Cache integer(int tag, int v) {
            return raw(i(tag)).raw((byte) 0x03).raw(i(v));
        }

        Cache strings(int tag, String... vs) {
            raw(i(tag)).raw((byte) 0x19).raw(i(vs.length));
            for (String v : vs) {
                byte[] s = v.getBytes(StandardCharsets.UTF_8);
                raw(i(s.length)).raw(s);
            }
            return this;
        }

        static byte[] i(int v) {
            return new byte[]{(byte) (v >> 24), (byte) (v >> 16), (byte) (v >> 8), (byte) v};
        }

        byte[] bytes() {
            return b.toByteArray();
        }
    }

    @Test
    void parsesClapFilesWithSeveralPlugins() {
        byte[] data = new Cache()
            .record().str(0x3eb, "lsp").str(0x3ec, "/usr/lib/clap/lsp-plugins.clap").raw(new byte[]{0, 0, 3, (byte) 0xef, 4})
            .str(0x3e3, "Dynamics").str(0x3e4, "Compressor Mono").str(0x3e6, "LSP").str(0x2ec4, "in.lsp-plug.comp_mono")
            .str(0x2ec5, "1.0.37").strings(0x34f8, "audio-effect", "compressor", "mono")
            .str(0x3e3, "EQ").str(0x3e4, "Graphic EQ").str(0x3e6, "LSP").str(0x2ec4, "in.lsp-plug.geq")
            .str(0x2ec5, "").strings(0x34f8, "audio-effect", "equalizer")
            .record().str(0x3ec, "/usr/lib/clap/Surge XT.clap")
            .str(0x3e3, "Synth").str(0x3e4, "Surge XT").str(0x3e6, "Surge Synth Team")
            .str(0x2ec4, "org.surge-synth-team.surge-xt").strings(0x34f8, "instrument", "synthesizer")
            .bytes();

        List<PluginCache.Plugin> ps = PluginCache.parse("clap", data);
        assertEquals(3, ps.size());
        PluginCache.Plugin comp = ps.get(0);
        assertEquals("in.lsp-plug.comp_mono", comp.id());
        assertEquals("Compressor Mono", comp.name());
        assertEquals("Dynamics", comp.category());
        assertEquals("1.0.37", comp.version());
        assertEquals(List.of("audio-effect", "compressor", "mono"), comp.features());
        assertEquals("/usr/lib/clap/lsp-plugins.clap", comp.file());
        assertNull(ps.get(1).version(), "empty version is left out");
        assertEquals("/usr/lib/clap/lsp-plugins.clap", ps.get(1).file());
        assertEquals("/usr/lib/clap/Surge XT.clap", ps.get(2).file());
        assertEquals(List.of("instrument", "synthesizer"), ps.get(2).features());
    }

    @Test
    void parsesVst3StringIdsAndVst2IntegerIds() {
        byte[] vst3 = new Cache().record().str(0x3ec, "/usr/lib/vst3/ZaMaximX2.vst3")
            .str(0x3e3, "Audio FX").str(0x3e4, "ZaMaximX2").str(0x3e6, "Damien Zammit")
            .str(0x3e2, "2046504473616C6332584D5A00000000").str(0xc58, "4.5.0").bytes();
        PluginCache.Plugin p3 = PluginCache.parse("vst3", vst3).get(0);
        assertEquals("2046504473616C6332584D5A00000000", p3.id());
        assertEquals("4.5.0", p3.version());

        byte[] vst2 = new Cache().record().str(0x3ec, "/usr/lib/vst/ZaMaximX2-vst.so")
            .str(0x3e3, "Audio FX").str(0x3e4, "ZaMaximX2").str(0x3e6, "Damien Zammit")
            .integer(0x3e2, 1515018290).integer(0xc58, 263424).bytes();
        PluginCache.Plugin p2 = PluginCache.parse("vst2", vst2).get(0);
        assertEquals("1515018290", p2.id());
        assertNull(p2.version());
    }

    @Test
    void entriesWithoutAnIdAreSkippedAndGarbageIsTolerated() {
        byte[] data = new Cache().raw(new byte[]{9, 9, 9}).record().str(0x3e3, "Synth").str(0x3e4, "No Id")
            .raw(new byte[]{0, 0, 3, (byte) 0xe4, 8, 0x7f, 0x7f, 0x7f, 0x7f}).bytes();
        assertTrue(PluginCache.parse("clap", data).isEmpty());
        assertTrue(PluginCache.parse("clap", new byte[0]).isEmpty());
    }

    @Test
    void readsTheNewestCacheFileOfEachKind(@TempDir Path dir) throws Exception {
        byte[] old = new Cache().record().str(0x3e3, "Synth").str(0x3e4, "Old").str(0x2ec4, "old.id").bytes();
        byte[] cur = new Cache().record().str(0x3e3, "Synth").str(0x3e4, "New").str(0x2ec4, "new.id").bytes();
        Files.write(dir.resolve("clap-metadata-9"), old);
        Files.write(dir.resolve("clap-metadata-13"), cur);
        Files.write(dir.resolve("vst-metadata-27"), new Cache().record().str(0x3e3, "Audio FX").str(0x3e4, "V2")
            .integer(0x3e2, 42).bytes());
        List<PluginCache.Plugin> ps = PluginCache.read(dir);
        assertEquals(List.of("new.id", "42"), ps.stream().map(PluginCache.Plugin::id).toList());
        assertEquals(List.of("clap", "vst2"), ps.stream().map(PluginCache.Plugin::kind).toList());
        assertTrue(PluginCache.read(dir.resolve("missing")).isEmpty());
    }

    @Test
    void readsThisMachinesCacheWhenPresent() {
        Path real = PluginCache.defaultDir();
        if (PluginCache.newest(real, "clap-metadata-") == null) {
            return;
        }
        List<PluginCache.Plugin> ps = PluginCache.read(real);
        assertFalse(ps.isEmpty());
        assertTrue(ps.stream().allMatch(p -> !p.id().isBlank() && !p.name().isBlank()));
    }
}
