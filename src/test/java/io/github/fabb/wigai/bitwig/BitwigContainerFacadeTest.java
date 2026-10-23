package io.github.fabb.wigai.bitwig;

import io.github.fabb.wigai.common.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class BitwigContainerFacadeTest {

    private static byte[] preset(String... kvs) throws Exception {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        b.write("BtWg0003meta".getBytes());
        for (int i = 0; i < kvs.length; i += 2) {
            b.write(new byte[]{0, 0, 0, (byte) kvs[i].length()});
            b.write(kvs[i].getBytes());
            b.write(new byte[]{8, 0, 0, 0, (byte) kvs[i + 1].length()});
            b.write(kvs[i + 1].getBytes());
            b.write(new byte[]{0, 0, 0, 1});
        }
        return b.toByteArray();
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> presets(Map<String, Object> result) {
        return (List<Map<String, Object>>) result.get("presets");
    }

    @Test
    void scansPresetsWithReadableAndEncryptedHeaders(@TempDir Path dir) throws Exception {
        Path pkg = Files.createDirectories(dir.resolve("packages/5.0/Bitwig/Bass and Guitar"));
        Files.write(pkg.resolve("Acoustic Bass Long.bwpreset"), new byte[]{1, 2, 3});
        Path user = Files.createDirectories(dir.resolve("user/Presets"));
        Files.write(user.resolve("Alien Choir.bwpreset"), preset("device_name", "Vocoder", "preset_category", "Vocal"));
        Files.write(user.resolve("notes.txt"), new byte[]{0});

        List<Map<String, Object>> index = BitwigContainerFacade.scanPresets(
            List.of(dir.resolve("packages"), dir.resolve("user"), dir.resolve("missing")), mock(Logger.class));

        assertEquals(2, index.size());
        Map<String, Object> bass = index.get(0);
        assertEquals("Acoustic Bass Long", bass.get("name"));
        assertEquals("5.0/Bitwig/Bass and Guitar", bass.get("collection"));
        assertFalse(bass.containsKey("device"), "encrypted presets have no readable device");
        Map<String, Object> choir = index.get(1);
        assertEquals("Vocoder", choir.get("device"));
        assertEquals("Vocal", choir.get("category"));
        assertTrue(((String) choir.get("path")).endsWith("Alien Choir.bwpreset"));
    }

    @Test
    void searchMatchesEveryWordAcrossFields() {
        List<Map<String, Object>> index = List.of(
            Map.of("name", "Acoustic Bass Long", "collection", "5.0/Bitwig/Bass and Guitar", "path", "a"),
            Map.of("name", "Alien Choir", "collection", "Presets", "device", "Vocoder", "category", "Vocal", "path", "b"),
            Map.of("name", "Deep Bass", "collection", "Presets", "device", "Polysynth", "path", "c"));

        assertEquals(2, BitwigContainerFacade.searchPresets(index, "bass", 10).get("matches"));
        assertEquals("Deep Bass", presets(BitwigContainerFacade.searchPresets(index, "BASS polysynth", 10)).get(0).get("name"));
        assertEquals("Alien Choir", presets(BitwigContainerFacade.searchPresets(index, "vocoder vocal", 10)).get(0).get("name"));
        assertEquals(0, BitwigContainerFacade.searchPresets(index, "bass vocoder", 10).get("matches"));

        Map<String, Object> all = BitwigContainerFacade.searchPresets(index, null, 1);
        assertEquals(3, all.get("indexed"));
        assertEquals(3, all.get("matches"));
        assertEquals(1, presets(all).size(), "limit caps the list but not the match count");
    }
}
