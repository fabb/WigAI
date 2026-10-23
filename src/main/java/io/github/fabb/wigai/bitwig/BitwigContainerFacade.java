package io.github.fabb.wigai.bitwig;

import com.bitwig.extension.controller.api.*;
import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.common.error.BitwigApiException;
import io.github.fabb.wigai.common.error.ErrorCode;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Inside container devices and Bitwig's library: Drum Machine pads, layer mixers (Instrument/FX Layer),
 * presets on disk, and the popup browser. Bitwig objects are created in the constructor (during init()).
 */
public class BitwigContainerFacade {

    private static final class Constants {
        static final int PADS = 128;
        static final int PAD_DEVICES = 8;
        static final int LAYERS = 16;
        static final int BROWSER_ITEMS = 64;
        static final int PRESET_HEADER_BYTES = 4096;
        private Constants() {}
    }

    private static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    private final Logger logger;
    private final BitwigEditFacade edit;
    private final PinnableCursorDevice device;
    private final DrumPadBank pads;
    private final List<DeviceBank> padDevices = new ArrayList<>();
    private final DeviceLayerBank layers;
    private final List<DeviceBank> layerDevices = new ArrayList<>();
    private final PopupBrowser browser;
    private final Map<String, BrowserFilterColumn> columns = new LinkedHashMap<>();
    private final Map<String, BrowserFilterItemBank> columnItems = new LinkedHashMap<>();
    private final Map<String, BrowserFilterItem> columnCursors = new LinkedHashMap<>();
    private final BrowserResultsItemBank results;
    private final List<Path> presetRoots;
    private List<Map<String, Object>> presetIndex;

    public BitwigContainerFacade(ControllerHost host, Logger logger, BitwigEditFacade edit, List<Path> presetRoots) {
        this.logger = logger;
        this.edit = edit;
        this.presetRoots = presetRoots;
        this.device = edit.editTrack().createCursorDevice("WIGAI_CONTAINER_DEVICE", "WigAI container device", 0,
            CursorDeviceFollowMode.FIRST_DEVICE);
        device.exists().markInterested();
        device.name().markInterested();
        device.hasDrumPads().markInterested();
        device.hasLayers().markInterested();
        device.isNested().markInterested();
        device.position().markInterested();
        device.presetName().markInterested();

        this.pads = device.createDrumPadBank(Constants.PADS);
        pads.scrollPosition().markInterested();
        pads.scrollPosition().set(0);
        pads.hasMutedPads().markInterested();
        pads.hasSoloedPads().markInterested();
        for (int i = 0; i < pads.getSizeOfBank(); i++) {
            padDevices.add(markChannel(pads.getItemAt(i)));
        }
        this.layers = device.createLayerBank(Constants.LAYERS);
        for (int i = 0; i < layers.getSizeOfBank(); i++) {
            layerDevices.add(markChannel(layers.getItemAt(i)));
        }

        this.browser = host.createPopupBrowser();
        browser.exists().markInterested();
        browser.title().markInterested();
        browser.contentTypeNames().markInterested();
        browser.selectedContentTypeName().markInterested();
        browser.selectedContentTypeIndex().markInterested();
        columns.put("collection", browser.smartCollectionColumn());
        columns.put("location", browser.locationColumn());
        columns.put("device", browser.deviceColumn());
        columns.put("category", browser.categoryColumn());
        columns.put("tag", browser.tagColumn());
        columns.put("device_type", browser.deviceTypeColumn());
        columns.put("file_type", browser.fileTypeColumn());
        columns.put("creator", browser.creatorColumn());
        for (Map.Entry<String, BrowserFilterColumn> e : columns.entrySet()) {
            BrowserFilterColumn column = e.getValue();
            column.exists().markInterested();
            column.entryCount().markInterested();
            BrowserFilterItemBank bank = column.createItemBank(Constants.BROWSER_ITEMS);
            bank.scrollPosition().markInterested();
            markItems(bank);
            columnItems.put(e.getKey(), bank);
            BrowserFilterItem cursor = column.createCursorItem();
            cursor.exists().markInterested();
            cursor.name().markInterested();
            columnCursors.put(e.getKey(), cursor);
        }
        BrowserResultsColumn resultsColumn = browser.resultsColumn();
        resultsColumn.entryCount().markInterested();
        this.results = resultsColumn.createItemBank(Constants.BROWSER_ITEMS);
        results.scrollPosition().markInterested();
        markItems(results);
    }

    private static DeviceBank markChannel(Channel c) {
        c.exists().markInterested();
        c.name().markInterested();
        c.isActivated().markInterested();
        c.mute().markInterested();
        c.solo().markInterested();
        c.isMutedBySolo().markInterested();
        c.volume().value().markInterested();
        c.volume().displayedValue().markInterested();
        c.pan().value().markInterested();
        c.pan().displayedValue().markInterested();
        DeviceBank bank = c.createDeviceBank(Constants.PAD_DEVICES);
        for (int d = 0; d < bank.getSizeOfBank(); d++) {
            bank.getItemAt(d).exists().markInterested();
            bank.getItemAt(d).name().markInterested();
            bank.getItemAt(d).presetName().markInterested();
        }
        return bank;
    }

    private static void markItems(BrowserItemBank<?> bank) {
        for (int i = 0; i < bank.getSizeOfBank(); i++) {
            BrowserItem item = bank.getItemAt(i);
            item.exists().markInterested();
            item.name().markInterested();
            item.isSelected().markInterested();
        }
    }

    private static String noteName(int key) {
        return NOTE_NAMES[key % 12] + (key / 12 - 2);
    }

    private static List<Map<String, Object>> deviceList(DeviceBank bank) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (int d = 0; d < bank.getSizeOfBank(); d++) {
            Device dev = bank.getItemAt(d);
            if (dev.exists().get()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("index", d);
                m.put("name", dev.name().get());
                String preset = dev.presetName().get();
                if (preset != null && !preset.isEmpty()) {
                    m.put("preset", preset);
                }
                out.add(m);
            }
        }
        return out;
    }

    private static Map<String, Object> channelState(Channel c, DeviceBank devices) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", c.name().get());
        m.put("volume", c.volume().displayedValue().get());
        m.put("pan", c.pan().displayedValue().get());
        m.put("mute", c.mute().get());
        m.put("solo", c.solo().get());
        if (c.isMutedBySolo().get()) {
            m.put("muted_by_solo", true);
        }
        if (!c.isActivated().get()) {
            m.put("activated", false);
        }
        m.put("devices", deviceList(devices));
        return m;
    }

    private void applyMix(Channel c, String name, Double volume, Double volumeDb, Double pan, Boolean mute, Boolean solo,
                          Boolean activated) {
        if (name != null) {
            c.name().set(name);
        }
        if (volume != null) {
            c.volume().value().setImmediately(volume);
        }
        if (volumeDb != null) {
            edit.setVolumeDb(c.volume(), volumeDb);
        }
        if (pan != null) {
            c.pan().value().setImmediately((pan + 1.0) / 2.0);
        }
        if (mute != null) {
            c.mute().set(mute);
        }
        if (solo != null) {
            c.solo().set(solo);
        }
        if (activated != null) {
            c.isActivated().set(activated);
        }
        edit.settle();
    }

    // ========================================
    // Drum Machine pads
    // ========================================

    private void focusDrumMachine(int trackIndex, int deviceIndex, String operation) throws BitwigApiException {
        edit.focusDevice(device, trackIndex, deviceIndex, operation);
        if (!edit.waitFor(() -> device.hasDrumPads().get(), 500)) {
            throw new BitwigApiException(ErrorCode.INVALID_PARAMETER, operation,
                "Device '" + device.name().get() + "' has no drum pads (use a Drum Machine)");
        }
        edit.waitFor(() -> pads.scrollPosition().get() == 0, 500);
    }

    private DrumPad pad(int key, String operation) throws BitwigApiException {
        if (key < 0 || key >= pads.getSizeOfBank()) {
            throw new BitwigApiException(ErrorCode.INVALID_RANGE, operation, "pad must be a MIDI key 0-127");
        }
        return pads.getItemAt(key);
    }

    private Map<String, Object> padState(int key) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("pad", key);
        m.put("note", noteName(key));
        m.putAll(channelState(pads.getItemAt(key), padDevices.get(key)));
        return m;
    }

    /** The pads of a Drum Machine, keyed by MIDI note. Empty pads are left out unless includeEmpty. */
    public Map<String, Object> listDrumPads(int trackIndex, int deviceIndex, boolean includeEmpty) throws BitwigApiException {
        focusDrumMachine(trackIndex, deviceIndex, "listDrumPads");
        List<Map<String, Object>> list = new ArrayList<>();
        for (int k = 0; k < pads.getSizeOfBank(); k++) {
            DrumPad p = pads.getItemAt(k);
            boolean filled = p.exists().get() && padDevices.get(k).getItemAt(0).exists().get();
            if (filled || includeEmpty) {
                list.add(padState(k));
            }
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("device", device.name().get());
        m.put("has_muted_pads", pads.hasMutedPads().get());
        m.put("has_soloed_pads", pads.hasSoloedPads().get());
        m.put("pads", list);
        return m;
    }

    public Map<String, Object> setDrumPad(int trackIndex, int deviceIndex, int key, String name, Double volume,
                                          Double volumeDb, Double pan, Boolean mute, Boolean solo, Boolean activated)
            throws BitwigApiException {
        focusDrumMachine(trackIndex, deviceIndex, "setDrumPad");
        applyMix(pad(key, "setDrumPad"), name, volume, volumeDb, pan, mute, solo, activated);
        return padState(key);
    }

    /** Puts a device, plugin, preset or sample (Bitwig makes a Sampler for it) on a pad, after what is there. */
    public Map<String, Object> insertIntoDrumPad(int trackIndex, int deviceIndex, int key, String bitwigDevice,
                                                 String clapId, String vst3Id, String file) throws BitwigApiException {
        final String operation = "insertIntoDrumPad";
        focusDrumMachine(trackIndex, deviceIndex, operation);
        DrumPad p = pad(key, operation);
        DeviceBank bank = padDevices.get(key);
        int before = deviceList(bank).size();
        edit.insertInto(p.insertionPoint(), bitwigDevice, clapId, vst3Id, file, operation);
        boolean added = edit.waitFor(() -> deviceList(bank).size() > before, 4000);
        edit.settle();
        Map<String, Object> m = padState(key);
        m.put("inserted", added);
        return m;
    }

    public Map<String, Object> clearDrumPads(int trackIndex, int deviceIndex, boolean mutes, boolean solos)
            throws BitwigApiException {
        focusDrumMachine(trackIndex, deviceIndex, "clearDrumPads");
        if (mutes) {
            pads.clearMutedPads();
        }
        if (solos) {
            pads.clearSoloedPads();
        }
        edit.settle();
        return Map.of("has_muted_pads", pads.hasMutedPads().get(), "has_soloed_pads", pads.hasSoloedPads().get());
    }

    // ========================================
    // Layer mixers (Instrument Layer, FX Layer, ...)
    // ========================================

    private void focusLayered(int trackIndex, int deviceIndex, String operation) throws BitwigApiException {
        edit.focusDevice(device, trackIndex, deviceIndex, operation);
        if (!edit.waitFor(() -> device.hasLayers().get(), 500)) {
            throw new BitwigApiException(ErrorCode.INVALID_PARAMETER, operation,
                "Device '" + device.name().get() + "' has no layers (use Instrument Layer, FX Layer, ...)");
        }
    }

    private Map<String, Object> layerState(int i) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("layer", i);
        m.putAll(channelState(layers.getItemAt(i), layerDevices.get(i)));
        return m;
    }

    public Map<String, Object> listLayers(int trackIndex, int deviceIndex) throws BitwigApiException {
        focusLayered(trackIndex, deviceIndex, "listLayers");
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < layers.getSizeOfBank(); i++) {
            if (layers.getItemAt(i).exists().get()) {
                list.add(layerState(i));
            }
        }
        return Map.of("device", device.name().get(), "layers", list);
    }

    public Map<String, Object> setLayer(int trackIndex, int deviceIndex, int layer, String name, Double volume,
                                        Double volumeDb, Double pan, Boolean mute, Boolean solo, Boolean activated)
            throws BitwigApiException {
        focusLayered(trackIndex, deviceIndex, "setLayer");
        applyMix(layer(layer, "setLayer"), name, volume, volumeDb, pan, mute, solo, activated);
        return layerState(layer);
    }

    private DeviceLayer layer(int i, String operation) throws BitwigApiException {
        if (i < 0 || i >= layers.getSizeOfBank() || !layers.getItemAt(i).exists().get()) {
            throw new BitwigApiException(ErrorCode.INVALID_RANGE, operation, "No layer " + i + " in this device");
        }
        return layers.getItemAt(i);
    }

    // ========================================
    // Presets on disk
    // ========================================

    /** Default places Bitwig keeps presets: installed packages and the user library. */
    public static List<Path> defaultPresetRoots() {
        Path home = Path.of(System.getProperty("user.home"));
        return List.of(home.resolve(".BitwigStudio").resolve("installed-packages"), home.resolve("Bitwig Studio"));
    }

    private synchronized List<Map<String, Object>> presetIndex() {
        if (presetIndex == null) {
            presetIndex = scanPresets(presetRoots, logger);
        }
        return presetIndex;
    }

    /** Every .bwpreset under the roots, with the device and category when the file is not encrypted. */
    static List<Map<String, Object>> scanPresets(List<Path> roots, Logger logger) {
        List<Map<String, Object>> index = new ArrayList<>();
        for (Path root : roots) {
            if (!Files.isDirectory(root)) {
                continue;
            }
            try (Stream<Path> files = Files.walk(root)) {
                files.filter(p -> p.getFileName().toString().endsWith(".bwpreset")).sorted().forEach(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    String file = p.getFileName().toString();
                    m.put("name", file.substring(0, file.length() - ".bwpreset".length()));
                    m.put("collection", root.relativize(p.getParent()).toString());
                    byte[] head = readHead(p);
                    String dev = BitwigEditFacade.presetMeta(head, "device_name");
                    if (dev != null) {
                        m.put("device", dev);
                    }
                    String category = BitwigEditFacade.presetMeta(head, "preset_category");
                    if (category != null && !category.isEmpty()) {
                        m.put("category", category);
                    }
                    m.put("path", p.toString());
                    index.add(m);
                });
            } catch (IOException e) {
                logger.warn("Could not scan presets under " + root + ": " + e.getMessage());
            }
        }
        return index;
    }

    private static byte[] readHead(Path p) {
        try (InputStream in = Files.newInputStream(p)) {
            return in.readNBytes(Constants.PRESET_HEADER_BYTES);
        } catch (IOException e) {
            return new byte[0];
        }
    }

    /**
     * Presets found on disk. Every word of the query must appear in the name, collection, device or category.
     * Packaged presets are encrypted, so their device is only known from the browser (browse_presets).
     */
    public Map<String, Object> listPresets(String query, Integer limit, boolean rescan) {
        if (rescan) {
            synchronized (this) {
                presetIndex = null;
            }
        }
        return searchPresets(presetIndex(), query, limit == null ? 100 : limit);
    }

    static Map<String, Object> searchPresets(List<Map<String, Object>> all, String query, int max) {
        String[] words = query == null ? new String[0] : query.toLowerCase(Locale.ROOT).trim().split("\\s+");
        List<Map<String, Object>> hits = new ArrayList<>();
        int total = 0;
        for (Map<String, Object> m : all) {
            String hay = (m.get("name") + " " + m.get("collection") + " " + m.getOrDefault("device", "") + " "
                + m.getOrDefault("category", "")).toLowerCase(Locale.ROOT);
            boolean match = true;
            for (String w : words) {
                if (!w.isEmpty() && !hay.contains(w)) {
                    match = false;
                    break;
                }
            }
            if (match) {
                total++;
                if (hits.size() < max) {
                    hits.add(m);
                }
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("indexed", all.size());
        out.put("matches", total);
        out.put("presets", hits);
        return out;
    }

    // ========================================
    // Popup browser
    // ========================================

    /**
     * Opens Bitwig's browser for an insertion point. mode: replace (the device; Bitwig filters to its presets),
     * after/before (the device), start/end (the track's chain).
     */
    public Map<String, Object> openBrowser(int trackIndex, Integer deviceIndex, String mode, String contentType)
            throws BitwigApiException {
        final String operation = "openBrowser";
        if (browser.exists().get()) {
            browser.cancel();
            edit.waitFor(() -> !browser.exists().get());
        }
        String m = mode == null ? (deviceIndex == null ? "end" : "replace") : mode.toLowerCase(Locale.ROOT);
        InsertionPoint ip;
        if (m.equals("start") || m.equals("end")) {
            Track track = edit.trackAt(trackIndex);
            ip = m.equals("start") ? track.startOfDeviceChainInsertionPoint() : track.endOfDeviceChainInsertionPoint();
        } else {
            if (deviceIndex == null) {
                throw new BitwigApiException(ErrorCode.MISSING_REQUIRED_PARAMETER, operation,
                    "device_index is required for mode " + m);
            }
            edit.focusDevice(device, trackIndex, deviceIndex, operation);
            ip = switch (m) {
                case "replace" -> device.replaceDeviceInsertionPoint();
                case "after" -> device.afterDeviceInsertionPoint();
                case "before" -> device.beforeDeviceInsertionPoint();
                default -> throw new BitwigApiException(ErrorCode.INVALID_PARAMETER, operation,
                    "mode must be replace, after, before, start or end");
            };
        }
        ip.browse();
        if (!edit.waitFor(() -> browser.exists().get(), 4000)) {
            throw new BitwigApiException(ErrorCode.BITWIG_TIMEOUT, operation, "The browser did not open");
        }
        edit.settle();
        if (contentType != null) {
            selectContentType(contentType, operation);
        }
        return browserState(null, 0);
    }

    private void requireOpen(String operation) throws BitwigApiException {
        if (!browser.exists().get()) {
            throw new BitwigApiException(ErrorCode.INVALID_PARAMETER, operation, "The browser is not open (browser_open)");
        }
    }

    private void selectContentType(String name, String operation) throws BitwigApiException {
        String[] names = browser.contentTypeNames().get();
        for (int i = 0; i < names.length; i++) {
            if (names[i].equalsIgnoreCase(name.trim())) {
                int idx = i;
                browser.selectedContentTypeIndex().set(idx);
                edit.waitFor(() -> browser.selectedContentTypeIndex().get() == idx);
                edit.settle();
                return;
            }
        }
        throw new BitwigApiException(ErrorCode.INVALID_PARAMETER, operation,
            "No content type '" + name + "'. Types: " + String.join(", ", names));
    }

    private static List<String> names(BrowserItemBank<?> bank) {
        List<String> out = new ArrayList<>();
        for (int i = 0; i < bank.getSizeOfBank(); i++) {
            BrowserItem item = bank.getItemAt(i);
            if (item.exists().get()) {
                out.add(item.name().get());
            }
        }
        return out;
    }

    private static void scrollTo(BitwigEditFacade edit, BrowserItemBank<?> bank, int position) {
        if (bank.scrollPosition().get() != position) {
            bank.scrollPosition().set(position);
            edit.waitFor(() -> bank.scrollPosition().get() == position);
            edit.settle();
        }
    }

    /**
     * The browser's content type, the selected item of each filter column, and one page of results.
     * With listColumn, also every item of that column (to pick a filter from).
     */
    public Map<String, Object> browserState(String listColumn, int resultsPage) throws BitwigApiException {
        requireOpen("browserState");
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("title", browser.title().get());
        m.put("content_types", List.of(browser.contentTypeNames().get()));
        String[] types = browser.contentTypeNames().get();
        int typeIndex = browser.selectedContentTypeIndex().get();
        m.put("content_type", typeIndex >= 0 && typeIndex < types.length ? types[typeIndex] : browser.selectedContentTypeName().get());
        Map<String, Object> filters = new LinkedHashMap<>();
        for (Map.Entry<String, BrowserFilterColumn> e : columns.entrySet()) {
            if (!e.getValue().exists().get()) {
                continue;
            }
            BrowserFilterItem cursor = columnCursors.get(e.getKey());
            filters.put(e.getKey(), Map.of("selected", cursor.exists().get() ? cursor.name().get() : "",
                "items", e.getValue().entryCount().get()));
        }
        m.put("filters", filters);
        if (listColumn != null) {
            m.put(listColumn + "_items", columnItemNames(listColumn, "browserState"));
        }
        scrollTo(edit, results, Math.max(0, resultsPage) * Constants.BROWSER_ITEMS);
        m.put("result_count", browser.resultsColumn().entryCount().get());
        m.put("results_page", resultsPage);
        m.put("results", names(results));
        return m;
    }

    private BrowserFilterColumn column(String name, String operation) throws BitwigApiException {
        BrowserFilterColumn c = columns.get(name == null ? "" : name.toLowerCase(Locale.ROOT));
        if (c == null) {
            throw new BitwigApiException(ErrorCode.INVALID_PARAMETER, operation,
                "column must be one of " + String.join(", ", columns.keySet()));
        }
        return c;
    }

    private List<String> columnItemNames(String name, String operation) throws BitwigApiException {
        BrowserFilterColumn c = column(name, operation);
        BrowserFilterItemBank bank = columnItems.get(name.toLowerCase(Locale.ROOT));
        List<String> out = new ArrayList<>();
        int count = c.entryCount().get();
        for (int pos = 0; pos < Math.max(count, 1); pos += Constants.BROWSER_ITEMS) {
            scrollTo(edit, bank, pos);
            out.addAll(names(bank));
        }
        scrollTo(edit, bank, 0);
        return out;
    }

    /** Finds an item by exact (case-insensitive) name, paging through the bank, and selects it. */
    private boolean selectByName(BrowserItemBank<?> bank, int count, String wanted) {
        for (int pos = 0; pos < Math.max(count, 1); pos += Constants.BROWSER_ITEMS) {
            scrollTo(edit, bank, pos);
            for (int i = 0; i < bank.getSizeOfBank(); i++) {
                BrowserItem item = bank.getItemAt(i);
                if (item.exists().get() && item.name().get().equalsIgnoreCase(wanted.trim())) {
                    item.isSelected().set(true);
                    edit.waitFor(() -> item.isSelected().get());
                    edit.settle();
                    return true;
                }
            }
        }
        return false;
    }

    /** Sets filters by column name and item name (e.g. {"category": "Bass"}); "Any ..." items clear a column. */
    public Map<String, Object> filterBrowser(String contentType, Map<String, String> filters) throws BitwigApiException {
        final String operation = "filterBrowser";
        requireOpen(operation);
        if (contentType != null) {
            selectContentType(contentType, operation);
        }
        for (Map.Entry<String, String> e : filters.entrySet()) {
            BrowserFilterColumn c = column(e.getKey(), operation);
            String key = e.getKey().toLowerCase(Locale.ROOT);
            if (!selectByName(columnItems.get(key), c.entryCount().get(), e.getValue())) {
                throw new BitwigApiException(ErrorCode.INVALID_PARAMETER, operation,
                    "No '" + e.getValue() + "' in the " + key + " column. Items: "
                        + String.join(", ", columnItemNames(key, operation)));
            }
        }
        return browserState(null, 0);
    }

    /** Selects a result by name (or index into all results) and loads it; cancel closes without loading. */
    public Map<String, Object> commitBrowser(String result, Integer resultIndex) throws BitwigApiException {
        final String operation = "commitBrowser";
        requireOpen(operation);
        String loaded = null;
        if (result != null) {
            if (!selectByName(results, browser.resultsColumn().entryCount().get(), result)) {
                throw new BitwigApiException(ErrorCode.INVALID_PARAMETER, operation, "No result named '" + result + "'");
            }
            loaded = result;
        } else if (resultIndex != null) {
            int page = resultIndex / Constants.BROWSER_ITEMS * Constants.BROWSER_ITEMS;
            scrollTo(edit, results, page);
            BrowserResultsItem item = results.getItemAt(resultIndex - page);
            if (!item.exists().get()) {
                throw new BitwigApiException(ErrorCode.INVALID_RANGE, operation, "No result at index " + resultIndex);
            }
            loaded = item.name().get();
            item.isSelected().set(true);
            edit.waitFor(() -> item.isSelected().get());
            edit.settle();
        }
        browser.commit();
        edit.waitFor(() -> !browser.exists().get(), 4000);
        edit.settle();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("committed", true);
        m.put("loaded", loaded == null ? "(the selected result)" : loaded);
        m.put("browser_open", browser.exists().get());
        return m;
    }

    public Map<String, Object> cancelBrowser() {
        if (browser.exists().get()) {
            browser.cancel();
            edit.waitFor(() -> !browser.exists().get());
        }
        return Map.of("browser_open", browser.exists().get());
    }
}
