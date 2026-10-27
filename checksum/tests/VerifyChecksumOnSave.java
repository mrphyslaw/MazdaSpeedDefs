// SPDX-License-Identifier: MIT
// Copyright (c) 2026 mrphyslaw
import java.nio.file.*;
import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import com.romraider.maps.*;
import com.romraider.maps.checksum.ChecksumManager;
import com.romraider.swing.JProgressPane;
import com.romraider.util.SettingsManager;
import com.romraider.xml.DOMRomUnmarshaller;

/** Exercises the ordinary editor save path, never ChecksumManager.update(). */
public final class VerifyChecksumOnSave {
    static final int TARGET = 0x5AA55AA5;
    static int checks, edits, tables, correctedEdits, uncoveredEdits, diskCases;
    static Path xml, output;
    static Document document;
    static byte[] original;
    static int correction;
    static int[][] ranges;
    static java.lang.reflect.Field presets;

    static void need(boolean ok, String message) {
        if (!ok) throw new IllegalStateException(message);
        checks++;
    }
    static int word(byte[] bytes, int at, int width) {
        int value = 0;
        for (int i = 0; i < width; i++) value = (value << 8) | (bytes[at+i] & 255);
        return value;
    }
    static void put(byte[] bytes, int at, int width, int value) {
        for (int i = width-1; i >= 0; i--) { bytes[at+i] = (byte)value; value >>>= 8; }
    }
    // Explicit expected native ranges supplied by the independent test profile.
    static int sum(byte[] bytes) {
        long value = 0;
        for (int[] range : ranges)
            for (int at = range[0]; at < range[1]; at += 4)
                value += Integer.toUnsignedLong(word(bytes, at, 4));
        return (int)value;
    }
    static byte[] expected(byte[] edited) {
        byte[] result = edited.clone();
        int residual = TARGET - sum(result);
        put(result, correction, 4, word(result, correction, 4) + residual);
        return result;
    }
    static Rom open(byte[] bytes, boolean valid) throws Exception {
        DOMRomUnmarshaller reader = new DOMRomUnmarshaller();
        Node match = reader.checkDefinitionMatch(document.getDocumentElement(), bytes);
        need(match != null, "Definition does not match private reference image");
        Rom rom = reader.unmarshallXMLDefinition(xml.toFile(), document.getDocumentElement(),
                match, bytes.clone(), new JProgressPane());
        rom.populateTables(bytes.clone(), new JProgressPane());
        need(rom.getFaultyTables().isEmpty(), "Faulty tables: " + rom.getFaultyTables());
        need(rom.getNumChecksumsManagers() == 1 && rom.getTotalAmountOfChecksums() == 1,
                "Missing or unexpected checksum module");
        // Rom.validateChecksum() displays a warning dialog for invalid input.
        // Read that same installed manager directly for deliberately damaged
        // fixtures so unattended verification never opens a dialog.
        java.lang.reflect.Field managers = Rom.class.getDeclaredField("checksumManagers");
        managers.setAccessible(true);
        ChecksumManager manager = (ChecksumManager)((List<?>)managers.get(rom)).get(0);
        need(manager.validate(bytes) == (valid ? 1 : 0), "Editor checksum validation disagrees with native sum");
        if (valid) need(rom.validateChecksum() == 1, "Editor rejects valid checksum");
        return rom;
    }
    static DataCell cell(Table table) {
        return table instanceof Table3D ? ((Table3D)table).get3dData()[0][0] : table.getDataCell(0);
    }
    static int width(Table table) {
        return table.getStorageType() == com.romraider.Settings.STORAGE_TYPE_FLOAT ? 4 : table.getStorageType();
    }
    static byte[] save(Rom rom, byte[] edited, String context) {
        byte[] saved = rom.saveFile().clone();
        need(sum(saved) == TARGET, "AUTOMATIC_CHECKSUM_SAVE_FAILED: " + context);
        need(Arrays.equals(saved, expected(edited)), "Save changed unintended bytes: " + context);
        need(Arrays.equals(saved, rom.saveFile()), "Repeated save differs: " + context);
        need(rom.validateChecksum() == 1, "Editor reports saved checksum invalid: " + context);
        if (sum(edited) == TARGET) uncoveredEdits++; else correctedEdits++;
        edits++;
        return saved;
    }
    static void restore(Rom rom, String context) {
        need(Arrays.equals(original, rom.saveFile()), "Exact restoration failed: " + context);
    }
    static void numeric(Rom rom, Table table, String context) throws Exception {
        DataCell c = cell(table);
        double before = c.getBinValue();
        boolean floating = table.getStorageType() == com.romraider.Settings.STORAGE_TYPE_FLOAT;
        int size = width(table), at = table.getStorageAddress();
        double next = before + 1;
        if (!floating && (size == 1 && before >= 255 || size == 2 && before >= 65535)) next = before - 1;
        c.setBinValue(next);
        int bits = floating ? Float.floatToRawIntBits((float)next) : (int)next;
        byte[] edited = original.clone(); put(edited, at, size, bits);
        need(!Arrays.equals(edited, original), "Test edit did not change bytes: " + context);
        save(rom, edited, context);
        c.setBinValue(before); restore(rom, context);
    }
    static void checkAllTables() throws Exception {
        Rom rom = open(original, true);
        Element definition = (Element)document.getElementsByTagName("rom").item(0);
        Set<String> names = new HashSet<>();
        for (Node n = definition.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (!(n instanceof Element) || !n.getNodeName().equals("table")) continue;
            Element e = (Element)n;
            String name = e.getAttribute("name");
            need(names.add(name), "Duplicate table name: " + name);
            Table table = rom.getTableByName(name);
            need(table != null && !table.isLocked(), "Missing or locked control: " + name);
            tables++;
            int at = table.getStorageAddress(), size = width(table);
            int stock = word(original, at, size);
            if (table instanceof TableBitwiseSwitch) {
                PresetManager pm = (PresetManager)presets.get(table);
                NodeList bits = e.getElementsByTagName("bit");
                need(bits.getLength() == pm.getPresets().size(), "Bitfield preset count differs");
                for (int i = 0; i < bits.getLength(); i++) {
                    int mask = 1 << Integer.parseInt(((Element)bits.item(i)).getAttribute("position"));
                    boolean on = (stock & mask) != 0;
                    if (on) pm.clearPreset(pm.getPresets().get(i)); else pm.applyPreset(pm.getPresets().get(i));
                    byte[] edited = original.clone(); put(edited, at, size, stock ^ mask);
                    save(rom, edited, name + " bit " + i);
                    if (on) pm.applyPreset(pm.getPresets().get(i)); else pm.clearPreset(pm.getPresets().get(i));
                    restore(rom, name);
                }
            } else if (table instanceof TableSwitch) {
                PresetManager pm = (PresetManager)presets.get(table);
                NodeList states = e.getElementsByTagName("state");
                need(states.getLength() == pm.getPresets().size(), "Switch preset count differs");
                int stockIndex = -1;
                for (int i = 0; i < states.getLength(); i++) {
                    int value = (int)Long.parseLong(((Element)states.item(i)).getAttribute("data"), 16);
                    if (value == stock) stockIndex = i;
                    pm.applyPreset(pm.getPresets().get(i));
                    byte[] edited = original.clone(); put(edited, at, size, value);
                    save(rom, edited, name + " state " + i);
                }
                need(stockIndex >= 0, "Unknown stock switch state: " + name);
                pm.applyPreset(pm.getPresets().get(stockIndex)); restore(rom, name);
            } else numeric(rom, table, name);
        }
        need(tables == rom.getTables().size(), "Included controls were skipped");
    }
    static void diskRoundTrip(Rom rom, byte[] edited, String label) throws Exception {
        byte[] saved = save(rom, edited, label);
        Path file = Files.createTempFile(output, "checksum-save-", ".bin");
        try {
            Files.write(file, saved);
            byte[] read = Files.readAllBytes(file);
            need(Arrays.equals(saved, read), "Disk bytes differ");
            Rom reopened = open(read, true);
            need(Arrays.equals(saved, reopened.saveFile()), "Reopened save differs");
        } finally { Files.deleteIfExists(file); }
        diskCases++;
    }
    static void checkRegions(boolean negativeControl) throws Exception {
        int[] probes;
        String stem = xml.getFileName().toString();
        if (stem.equals("L3CGEE000.xml")) probes = new int[]{0x8000, 0x30000, 0x50000, 0xBFFF0};
        else probes = new int[]{0x50000, 0xFFF00};
        for (int probe : probes) {
            byte[] edited = original.clone(); edited[probe] ^= 1;
            need(sum(edited) != TARGET, "Covered checksum probe is outside checksum coverage");
            Rom rom = open(edited, false);
            if (negativeControl) {
                // The caller intentionally loads a validation-only replacement module.
                // Passing this branch would mean the release check failed to detect it.
                save(rom, edited, "validation-only negative control");
                throw new IllegalStateException("Validation-only replacement unexpectedly repaired the image");
            }
            diskRoundTrip(rom, edited, "covered region " + Integer.toHexString(probe));
            byte[] repaired = expected(edited);
            Rom reopened = open(repaired, true);
            reopened.getBinary()[probe] ^= 1;
            restore(reopened, "restore covered-region edit");
        }
        // Exercise an ordinary table edit together with an invalid code sum.
        byte[] edited = original.clone(); edited[probes[0]] ^= 1;
        Rom rom = open(edited, false);
        Table t = rom.getTableByName("Rev Limiter Normal");
        need(t != null, "Missing ordinary calibration probe");
        double old = cell(t).getBinValue(); cell(t).setBinValue(old + 1);
        put(edited, t.getStorageAddress(), width(t), Float.floatToRawIntBits((float)(old+1)));
        diskRoundTrip(rom, edited, "combined calibration and covered-region edit");
        cell(t).setBinValue(old); rom.getBinary()[probes[0]] ^= 1;
        restore(rom, "combined restoration");
        int[] outside = stem.equals("L3CGEE000.xml") ? new int[]{0x2000, 0x1D000, 0xC8000} : new int[]{0x6000};
        for (int probe : outside) {
            byte[] uncovered = original.clone(); uncovered[probe] ^= 1;
            need(sum(uncovered) == TARGET, "Uncovered checksum probe is inside checksum coverage");
            diskRoundTrip(open(uncovered, true), uncovered, "outside checksum coverage");
        }
    }
    static void setup() throws Exception {
        SettingsManager.setTesting(true);
        java.lang.reflect.Field settings = SettingsManager.class.getDeclaredField("settings");
        settings.setAccessible(true); settings.set(null, new com.romraider.Settings());
        SettingsManager.getSettings().setUserLevel(5);
        java.lang.reflect.Field headless = java.awt.GraphicsEnvironment.class.getDeclaredField("headless");
        headless.setAccessible(true); headless.set(null, Boolean.TRUE);
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        java.lang.reflect.Field field = unsafeClass.getDeclaredField("theUnsafe");
        field.setAccessible(true); Object unsafe = field.get(null);
        Class<?> editorClass = Class.forName("com.romraider.editor.ecu.ECUEditor");
        Object editor = unsafeClass.getMethod("allocateInstance", Class.class).invoke(unsafe, editorClass);
        java.lang.reflect.Field panel = editorClass.getDeclaredField("statusPanel");
        panel.setAccessible(true); panel.set(editor, new JProgressPane());
        java.lang.reflect.Field active = Class.forName("com.romraider.editor.ecu.ECUEditorManager").getDeclaredField("editor");
        active.setAccessible(true); active.set(null, editor);
        presets = Table.class.getDeclaredField("presetManager"); presets.setAccessible(true);
    }
    public static void main(String[] args) {
        try {
            setup();
            xml = Paths.get(args[0]); original = Files.readAllBytes(Paths.get(args[1]));
            output = Paths.get(args[2]); Files.createDirectories(output);
            correction = Integer.decode(args[3]);
            String[] parts = args[4].split(","); ranges = new int[parts.length][2];
            for (int i = 0; i < parts.length; i++) {
                String[] endpoints = parts[i].split(":");
                ranges[i] = new int[]{Integer.decode(endpoints[0]), Integer.decode(endpoints[1])};
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            document = factory.newDocumentBuilder().parse(xml.toFile());
            need(original.length == 0x100000 && sum(original) == TARGET, "Invalid private reference checksum");
            boolean negativeControl = args.length > 5 && args[5].equals("negative-control");
            if (!negativeControl) {
                Rom baseline = open(original, true);
                need(Arrays.equals(original, baseline.saveFile()), "No-edit save changed original bytes");
                checkAllTables();
            }
            checkRegions(negativeControl);
            need(Arrays.equals(original, Files.readAllBytes(Paths.get(args[1]))), "Private source image was modified");
            String report = "{\"passed\":true,\"tables\":"+tables+",\"saveCases\":"+edits
                +",\"correctedCases\":"+correctedEdits+",\"uncoveredOrUnchangedCases\":"+uncoveredEdits
                +",\"diskSaveReopenCases\":"+diskCases+",\"assertions\":"+checks
                +",\"automaticRepairThroughRomSaveFile\":true,\"sourceUnchanged\":true}\n";
            Files.writeString(output.resolve("result.json"), report);
            System.out.println(xml.getFileName()+": "+tables+" controls, "+edits+" saves, "+correctedEdits+" checksum corrections, "+diskCases+" disk round trips passed");
            System.exit(0);
        } catch (Throwable e) { e.printStackTrace(); System.exit(1); }
    }
}
