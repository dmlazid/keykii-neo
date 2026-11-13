package com.keykii.neo;

import org.junit.Test;

import static org.junit.Assert.*;

/** JVM unit tests verifying the 40 active themes, 14 bundled new fonts, and keyboard layout geometry. */
public final class ThemeAndFontCatalogTest {

    @Test
    public void removedBulkThemesStayDisabled() {
        assertEquals(0, NeoThemeCatalog.COUNT);
        assertEquals(0, NeoThemeCatalog.count("all"));
        assertFalse(NeoThemeCatalog.isNeoPack(1000));
        assertFalse(NeoThemeCatalog.isNeoPack(1511));
        assertNull(NeoThemeCatalog.get(1000));

        for (int p = 120; p <= 139; p++) {
            assertFalse("Legacy illustrated pack " + p + " should be disabled", IllustratedThemeAssets.isNewPack(p));
            assertEquals(0, IllustratedThemeAssets.background(p));
        }
    }

    @Test
    public void exactlyFortyThemesAreActiveWithExpectedTiers() {
        int activeCount = 0;
        for (int p = 0; p <= 1600; p++) {
            boolean expected = (p >= 100 && p <= 119) || (p >= 200 && p <= 219);
            assertEquals("Pack " + p + " membership mismatch", expected, KeyKiiThemeCollection.isThemePack(p));
            if (expected) {
                activeCount++;
                boolean expectedFree =
                        p == 100 || p == 102 || p == 106 || p == 108 || p == 113 || p == 117
                        || p == 200 || p == 201 || p == 202 || p == 206
                        || p == 210 || p == 212 || p == 214 || p == 219;
                assertEquals("Tier mismatch for pack " + p, !expectedFree, KeyKiiAccess.themeNeedsAccess(p));
            }
        }
        assertEquals(40, activeCount);
    }

    @Test
    public void bundledNewFontsHaveExpectedAccessTiers() {
        for (int style = 152; style <= 154; style++) {
            assertFalse("New font " + style + " should be free", KeyKiiAccess.fontNeedsAccess(style));
        }
        for (int style = 155; style <= 157; style++) {
            assertTrue("New font " + style + " should require Pro access", KeyKiiAccess.fontNeedsAccess(style));
        }
        for (int style = 158; style <= 159; style++) {
            assertFalse("New font " + style + " should be free", KeyKiiAccess.fontNeedsAccess(style));
        }
        for (int style = 160; style <= 161; style++) {
            assertTrue("New font " + style + " should require Pro access", KeyKiiAccess.fontNeedsAccess(style));
        }
        for (int style = 162; style <= 163; style++) {
            assertFalse("New font " + style + " should be free", KeyKiiAccess.fontNeedsAccess(style));
        }
        for (int style = 164; style <= 165; style++) {
            assertTrue("New font " + style + " should require Pro access", KeyKiiAccess.fontNeedsAccess(style));
        }
    }

    @Test
    public void geometryLayoutsRetainAllKeysAndSpacebar() {
        String[] languages = {"en-US", "en-GB", "fil", "ceb", "es", "fr", "de", "tr", "pt", "it"};
        for (String lang : languages) {
            String[] top = NeoKeyboardLanguage.top(lang);
            String[] mid = NeoKeyboardLanguage.middle(lang);
            String[] bot = NeoKeyboardLanguage.bottom(lang);
            NeoGeometry.Key[][] rows = new NeoGeometry.Key[4][];
            rows[0] = toKeys(top);
            rows[1] = toKeys(mid);
            String[] third = new String[bot.length + 2];
            third[0] = "SHIFT";
            System.arraycopy(bot, 0, third, 1, bot.length);
            third[third.length - 1] = "BACK";
            rows[2] = toKeys(third);
            rows[3] = new NeoGeometry.Key[]{
                    new NeoGeometry.Key("123", 1.12f, true),
                    new NeoGeometry.Key(",", .58f, false),
                    new NeoGeometry.Key("LANG", .68f, true),
                    new NeoGeometry.Key("SPACE", 2.8f, false),
                    new NeoGeometry.Key(".", .58f, false),
                    new NeoGeometry.Key("ENTER", 1.12f, true)
            };

            NeoGeometry.Box[] boxes = NeoGeometry.layout(rows, 0, 0);
            int expectedTotal = rows[0].length + rows[1].length + rows[2].length + rows[3].length;
            assertEquals(expectedTotal, boxes.length);
            for (NeoGeometry.Box box : boxes) {
                assertTrue(box.left >= 0f && box.right <= 1.00001f);
                assertTrue(box.top >= 0f && box.bottom <= 1.00001f);
                assertTrue(box.right > box.left);
                assertTrue(box.bottom > box.top);
            }
        }
    }

    private static NeoGeometry.Key[] toKeys(String[] labels) {
        NeoGeometry.Key[] out = new NeoGeometry.Key[labels.length];
        for (int i = 0; i < labels.length; i++) {
            String a = labels[i];
            out[i] = new NeoGeometry.Key(a, a.length() > 1 ? 1.05f : 1f, a.length() > 1);
        }
        return out;
    }
}
