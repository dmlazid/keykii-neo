package com.keykii.neo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Host regression gate for KeyKii Neo:
 * - Verifies keyboard layout geometry across 48 profiles, 11 editions, 10 languages, 3 modes
 * - Verifies 512 bulk Neo packs & 120-139 bulk illustrated packs remain removed
 * - Verifies all 30 active themes (100-119 original + 200-209 clean gradient)
 * - Verifies all 6 new stylish font assets (152-157) and shop metadata
 */
public final class NeoGeometryAudit {
    static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }

    static NeoGeometry.Key[][] keys(String language, boolean numbers, boolean symbols) {
        List<String[]> rows = new ArrayList<>();
        if (numbers || symbols) rows.add(new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"});
        if (symbols) {
            rows.add(new String[]{"@", "#", "$", "_", "&", "-", "+", "(", ")", "/"});
            rows.add(new String[]{"SYM2", "*", "\"", "'", ":", ";", "!", "?", "BACK"});
        } else {
            rows.add(NeoKeyboardLanguage.top(language));
            rows.add(NeoKeyboardLanguage.middle(language));
            String[] letters = NeoKeyboardLanguage.bottom(language);
            String[] third = new String[letters.length + 2];
            third[0] = "SHIFT";
            System.arraycopy(letters, 0, third, 1, letters.length);
            third[third.length - 1] = "BACK";
            rows.add(third);
        }
        rows.add(symbols ? new String[]{"ABC", ",", "SPACE", ".", "ENTER"} : new String[]{"123", ",", "LANG", "SPACE", ".", "ENTER"});
        NeoGeometry.Key[][] out = new NeoGeometry.Key[rows.size()][];
        for (int r = 0; r < rows.size(); r++) {
            String[] rr = rows.get(r);
            out[r] = new NeoGeometry.Key[rr.length];
            for (int k = 0; k < rr.length; k++) {
                String a = rr[k];
                float wt = r == rows.size() - 1
                        ? (a.equals("SPACE") ? 2.8f : a.equals(",") || a.equals(".") ? .58f : a.equals("LANG") ? .68f : 1.12f)
                        : (a.length() > 1 ? 1.05f : 1f);
                out[r][k] = new NeoGeometry.Key(a, wt, a.length() > 1 && !a.equals("SPACE"));
            }
        }
        return out;
    }

    private static String readText(Path path) throws IOException {
        check(Files.isRegularFile(path), "Missing required source file: " + path);
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    private static void verifyCatalogAndAssets() throws IOException {
        Path root = Paths.get("").toAbsolutePath();

        String neoCatalog = readText(root.resolve("app/src/main/java/com/keykii/neo/NeoThemeCatalog.java"));
        check(neoCatalog.contains("COUNT=0"), "NeoThemeCatalog.COUNT must be 0");
        check(neoCatalog.contains("static boolean isNeoPack(int p){return false;}"), "NeoThemeCatalog.isNeoPack must return false");

        String illAssets = readText(root.resolve("app/src/main/java/com/keykii/neo/IllustratedThemeAssets.java"));
        check(Pattern.compile("isNewPack\\(int\\s+pack\\)\\s*\\{\\s*return\\s+false;").matcher(illAssets).find(),
                "IllustratedThemeAssets.isNewPack must return false");

        String collection = readText(root.resolve("app/src/main/java/com/keykii/neo/KeyKiiThemeCollection.java"));
        check(collection.contains("(pack>=100&&pack<=119)||(pack>=200&&pack<=209)"),
                "KeyKiiThemeCollection.isThemePack must allow only 100-119 and 200-209");

        String access = readText(root.resolve("app/src/main/java/com/keykii/neo/KeyKiiAccess.java"));
        check(access.contains("(pack>=100&&pack<=119)||(pack>=200&&pack<=209)"),
                "KeyKiiAccess.themeNeedsAccess must match 30 active themes");
        check(access.contains("(style>=155&&style<=157)"),
                "KeyKiiAccess.fontNeedsAccess must include 155-157 PRO tier fonts");

        String settings = readText(root.resolve("app/src/main/java/com/keykii/neo/SettingsActivity.java"));
        check(!settings.contains("512 new theme compositions"),
                "SettingsActivity must not reference removed 512 new theme compositions");
        check(settings.contains("30 themes total"),
                "SettingsActivity Theme Shop banner must report 30 themes total");

        for (int p = 100; p <= 119; p++) {
            check(settings.contains("case " + p + ":"), "Original theme missing in SettingsActivity: " + p);
        }
        for (int p = 200; p <= 209; p++) {
            check(settings.contains("case " + p + ":"), "Clean gradient theme missing in SettingsActivity: " + p);
        }

        String service = readText(root.resolve("app/src/main/java/com/keykii/neo/KeyKiiService.java"));
        String[] newFontFiles = {
                "quicksand.ttf", "playfair_display.ttf", "space_grotesk.ttf",
                "righteous.ttf", "dancing_script.ttf", "comfortaa.ttf"
        };
        String[] newFontNames = {
                "Quicksand", "Playfair Display", "Space Grotesk",
                "Righteous", "Dancing Script", "Comfortaa"
        };
        for (int i = 0; i < 6; i++) {
            int styleId = 152 + i;
            String file = newFontFiles[i];
            String name = newFontNames[i];
            check(settings.contains("case " + styleId + ": return \"" + name + "\";"),
                    "Missing font label in SettingsActivity for " + styleId);
            check(settings.contains("fonts/" + file), "Missing font loader in SettingsActivity for " + file);
            check(service.contains("case " + styleId + ":") && service.contains("fonts/" + file),
                    "Missing font loader in KeyKiiService for " + styleId + " (" + file + ")");

            Path ttf = root.resolve("app/src/main/assets/fonts").resolve(file);
            check(Files.isRegularFile(ttf), "Missing font asset file: " + ttf);
            byte[] bytes = Files.readAllBytes(ttf);
            check(bytes.length > 10_000, "Font asset unexpectedly small: " + file + " (" + bytes.length + " bytes)");
            boolean validHeader =
                    (bytes[0] == 0x00 && bytes[1] == 0x01 && bytes[2] == 0x00 && bytes[3] == 0x00) ||
                    (bytes[0] == 'O' && bytes[1] == 'T' && bytes[2] == 'T' && bytes[3] == 'O') ||
                    (bytes[0] == 't' && bytes[1] == 'r' && bytes[2] == 'u' && bytes[3] == 'e');
            check(validHeader, "Invalid SFNT/TTF header in " + file);
        }
    }

    public static void main(String[] args) throws Exception {
        verifyCatalogAndAssets();

        int checks = 0;
        Set<String> layouts = new HashSet<>();
        String[] langs = {"en-US", "en-GB", "fil", "ceb", "es", "fr", "de", "tr", "pt", "it"};

        // Audit active theme geometry profile (family 0, composition 0) and all 48 geometry profiles x 11 editions.
        int[] activeThemes = new int[30];
        for (int i = 0; i < 20; i++) activeThemes[i] = 100 + i;
        for (int i = 0; i < 10; i++) activeThemes[20 + i] = 200 + i;
        check(activeThemes.length == 30, "Expected 30 active themes");

        for (int pack = 0; pack < NeoGeometry.FAMILY_COUNT * 11; pack++) {
            for (String language : langs) {
                for (int mode = 0; mode < 3; mode++) {
                    NeoGeometry.Key[][] keys = keys(language, mode == 1, mode == 2);
                    NeoGeometry.Box[] boxes = NeoGeometry.layout(keys, pack % NeoGeometry.FAMILY_COUNT, pack / NeoGeometry.FAMILY_COUNT);
                    int count = 0;
                    int spaces = 0;
                    for (NeoGeometry.Key[] row : keys) count += row.length;
                    check(boxes.length == count, "Missing key");
                    StringBuilder signature = new StringBuilder();
                    for (int i = 0; i < boxes.length; i++) {
                        NeoGeometry.Box b = boxes[i];
                        check(Float.isFinite(b.left) && Float.isFinite(b.top) && Float.isFinite(b.right) && Float.isFinite(b.bottom), "Nonfinite bounds");
                        check(b.left >= 0 && b.top >= 0 && b.right <= 1.00001f && b.bottom <= 1.00001f, "Out of bounds " + pack);
                        check((b.right - b.left) * 320 >= 12, "Unusable narrow target " + pack + " " + language);
                        check((b.bottom - b.top) * 180 >= 24, "Unusable short target " + pack);
                        if (keys[b.row][b.col].action.equals("SPACE")) {
                            spaces++;
                            check((b.right - b.left) * 320 > 65, "Spacebar too small");
                        }
                        signature.append(Math.round(b.left * 10000)).append(',')
                                .append(Math.round(b.top * 10000)).append(',')
                                .append(Math.round(b.right * 10000)).append(',')
                                .append(Math.round(b.bottom * 10000)).append(';');
                        for (int j = 0; j < i; j++) {
                            NeoGeometry.Box a = boxes[j];
                            float iw = Math.min(a.right, b.right) - Math.max(a.left, b.left);
                            float ih = Math.min(a.bottom, b.bottom) - Math.max(a.top, b.top);
                            check(iw < .00001f || ih < .00001f, "Overlapping touch targets " + pack);
                        }
                    }
                    check(spaces == 1, "Space action lost or duplicated");
                    if (language.equals("en-US") && mode == 0) layouts.add(signature.toString());
                    checks++;
                }
            }
        }
        check(layouts.size() >= NeoGeometry.FAMILY_COUNT, "Expected distinct layout profiles");
        System.out.println("PASS: 30 themes (100-119, 200-209), 6 new fonts (152-157), and " + checks + " keyboard geometry configurations verified (" + layouts.size() + " distinct layouts).");
    }
}
