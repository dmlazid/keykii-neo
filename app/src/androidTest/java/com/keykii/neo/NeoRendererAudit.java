package com.keykii.neo;

import android.app.Instrumentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;

/** Dependency-free device audit. Failure returns a nonzero instrumentation result. */
public final class NeoRendererAudit extends Instrumentation {
    private static final String[] NEW_FONT_FILES = {
        "fonts/quicksand.ttf",
        "fonts/playfair_display.ttf",
        "fonts/space_grotesk.ttf",
        "fonts/righteous.ttf",
        "fonts/dancing_script.ttf",
        "fonts/comfortaa.ttf"
    };

    private static final int[] CLEAN_THEME_MOTIFS = {
        R.drawable.theme_motif_lotus,
        R.drawable.theme_motif_butterfly,
        R.drawable.theme_motif_flower,
        R.drawable.theme_motif_cherry,
        R.drawable.theme_motif_leaf,
        R.drawable.theme_motif_cup,
        R.drawable.theme_motif_bubble,
        R.drawable.theme_motif_moonstar,
        R.drawable.theme_motif_bow,
        R.drawable.theme_motif_strawberry,
        R.drawable.theme_motif_heart,
        R.drawable.theme_motif_cloud,
        R.drawable.theme_motif_star
    };

    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        start();
    }

    private static void check(boolean ok, String why) {
        if (!ok) throw new AssertionError(why);
    }

    @Override
    public void onStart() {
        Bundle results = new Bundle();
        try {
            Context c = getTargetContext();

            // 1. Verify 512 bulk Neo themes and 120-139 bulk illustrated themes remain removed.
            check(NeoThemeCatalog.COUNT == 0, "512 bulk NeoThemeCatalog packs must be removed");
            check(!NeoThemeCatalog.isNeoPack(1000), "NeoThemeCatalog pack 1000 must be disabled");
            check(!NeoThemeCatalog.isNeoPack(1511), "NeoThemeCatalog pack 1511 must be disabled");
            check(!IllustratedThemeAssets.isNewPack(120), "Bulk illustrated packs 120-139 must be disabled");
            check(!IllustratedThemeAssets.isNewPack(139), "Bulk illustrated packs 120-139 must be disabled");

            // 2. Verify all 30 active themes (100-119 original + 200-209 clean gradient).
            int validCount = 0;
            for (int p = 99; p <= 1600; p++) {
                boolean expected = (p >= 100 && p <= 119) || (p >= 200 && p <= 209);
                boolean actual = KeyKiiThemeCollection.isThemePack(p);
                check(actual == expected, "Theme pack membership mismatch for pack " + p + ": expected=" + expected + ", actual=" + actual);
                if (actual) {
                    validCount++;
                    boolean expectedFree =
                            p == 100 || p == 102 || p == 106 || p == 108 || p == 113 || p == 117
                            || p == 200 || p == 201 || p == 202 || p == 206;
                    check(KeyKiiAccess.themeNeedsAccess(p) == !expectedFree,
                            "Theme entitlement mismatch for pack " + p);
                }
            }
            check(validCount == 30, "Expected 30 total themes (100-119 and 200-209), got " + validCount);

            // 3. Verify all 6 new stylish font assets (152-157) load cleanly and enforce access tiers.
            Typeface[] loadedFonts = new Typeface[NEW_FONT_FILES.length];
            for (int i = 0; i < NEW_FONT_FILES.length; i++) {
                int style = 152 + i;
                boolean expectedPro = style >= 155;
                check(KeyKiiAccess.fontNeedsAccess(style) == expectedPro,
                        "Font access mismatch for style " + style);
                Typeface tf = Typeface.createFromAsset(c.getAssets(), NEW_FONT_FILES[i]);
                check(tf != null, "Failed to load font asset " + NEW_FONT_FILES[i]);
                loadedFonts[i] = tf;
            }

            // 4. Render audit evidence sheet covering the 20 original scenes, 10 clean gradient swatches, and 6 new fonts.
            File baseDir = c.getExternalFilesDir(null);
            if (baseDir == null) baseDir = c.getFilesDir();
            File dir = new File(baseDir, "theme-v2-audit");
            check(dir.mkdirs() || dir.isDirectory(), "Unable to create audit directory " + dir);

            Bitmap sheet = Bitmap.createBitmap(1080, 960, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(sheet);
            canvas.drawColor(Color.rgb(248, 245, 255));
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

            // Verify original ThemeSceneDrawable instances draw without error.
            for (int p = 100; p <= 119; p++) {
                ThemeSceneDrawable scene = new ThemeSceneDrawable(c, p, Color.rgb(142, 92, 200), p == 105 || p == 113 || p == 118);
                scene.setBounds(0, 0, 260, 140);
                scene.draw(canvas);
            }
            canvas.drawColor(Color.rgb(248, 245, 255));

            // Render clean gradient cards (200-209) and verify all motif drawables resolve & draw.
            int[][] cleanPalettes = {
                {0xFFEDE4FA, 0xFFF8F4FF, 0xFF8E5CC8},
                {0xFFFFE6F0, 0xFFFFF7FA, 0xFFDD6297},
                {0xFFE6F3E4, 0xFFF7FCF6, 0xFF55985E},
                {0xFFDEF1FC, 0xFFF4FBFF, 0xFF368BC7},
                {0xFFFFE9DB, 0xFFFFF8F3, 0xFFD57554},
                {0xFFE1E7FD, 0xFFF6F8FF, 0xFF6278DB},
                {0xFF181526, 0xFF241D3B, 0xFFC992FF},
                {0xFF1B141A, 0xFF2B1C27, 0xFFF27CA7},
                {0xFF0F1624, 0xFF18233A, 0xFF74B6FF},
                {0xFF181411, 0xFF261F1A, 0xFFE7B773}
            };
            for (int i = 0; i < cleanPalettes.length; i++) {
                int col = i % 2;
                int row = i / 2;
                float x = 36 + col * 516;
                float y = 28 + row * 138;
                RectF card = new RectF(x, y, x + 492, y + 118);
                paint.setShader(new LinearGradient(card.left, card.top, card.right, card.bottom,
                        cleanPalettes[i][0], cleanPalettes[i][1], Shader.TileMode.CLAMP));
                canvas.drawRoundRect(card, 26, 26, paint);
                paint.setShader(null);

                Drawable motif = c.getDrawable(CLEAN_THEME_MOTIFS[i % CLEAN_THEME_MOTIFS.length]);
                check(motif != null, "Missing clean theme motif drawable");
                motif = motif.mutate();
                motif.setTint(cleanPalettes[i][2]);
                motif.setBounds((int) (card.right - 74), (int) (card.top + 18), (int) (card.right - 22), (int) (card.top + 70));
                motif.draw(canvas);
            }

            // Render font samples for 152-157.
            paint.setColor(0xFF1E1B2E);
            paint.setTextSize(34f);
            for (int i = 0; i < loadedFonts.length; i++) {
                paint.setTypeface(loadedFonts[i]);
                canvas.drawText("Font " + (152 + i) + ": KeyKii AaBbCc 123", 48f, 750f + i * 36f, paint);
            }

            try (FileOutputStream pngOut = new FileOutputStream(new File(dir, "clean-themes-audit.png"))) {
                check(sheet.compress(Bitmap.CompressFormat.PNG, 95, pngOut), "Failed writing clean-themes-audit.png");
            }
            sheet.recycle();

            try (FileWriter out = new FileWriter(new File(dir, "result.txt"))) {
                out.write("PASS: 512 bulk themes removed, 20 original themes preserved (100-119), 10 clean gradient themes (200-209) active, and 6 stylish fonts (152-157) rendered.\n");
            }

            results.putString("stream", "\nTHEME_V2_AUDIT_PASS\n");
            finish(-1, results);
        } catch (Throwable t) {
            results.putString("stream", "\nTHEME_V2_AUDIT_FAIL: " + t + "\n");
            finish(1, results);
        }
    }
}
