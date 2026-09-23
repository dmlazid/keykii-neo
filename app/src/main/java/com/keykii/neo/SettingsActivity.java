package com.keykii.neo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {

    private static final int REQUEST_THEME_IMAGE = 2160;

    private static final int BG = Color.rgb(248, 246, 242);
    private static final int CARD = Color.WHITE;
    private static final int TEXT = Color.rgb(45, 43, 40);
    private static final int MUTED = Color.rgb(118, 113, 107);
    private static final int BORDER = Color.rgb(229, 224, 217);
    private static final int ACCENT = Color.rgb(239, 232, 221);
    private static final int SOFT = Color.rgb(245, 241, 235);

    private SharedPreferences prefs;
    private String screen = "home";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("keykii_prefs", MODE_PRIVATE);

        if ("shortcuts".equals(getIntent().getStringExtra("open_screen"))) {
            showShortcuts();

            int shortcutIndex = getIntent().getIntExtra("shortcut_index", -1);
            if (shortcutIndex >= 0 && shortcutIndex < 12) {
                showShortcutEditor(shortcutIndex);
            }
        } else {
            showHome();
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {
        super.onActivityResult(requestCode, resultCode, data);

        if (
                requestCode == REQUEST_THEME_IMAGE &&
                resultCode == RESULT_OK &&
                data != null &&
                data.getData() != null
        ) {
            Uri uri = data.getData();

            try {
                final int flags =
                        data.getFlags() &
                                (Intent.FLAG_GRANT_READ_URI_PERMISSION |
                                 Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                getContentResolver()
                        .takePersistableUriPermission(uri, flags);
            } catch (Exception ignored) {
            }

            prefs.edit()
                    .putString("theme_image_uri", uri.toString())
                    .putInt("theme_surface_mode", 3)
                    .apply();

            toast("Theme image selected");
            showTheme();
        }
    }


    @Override
    public void onBackPressed() {
        if (!"home".equals(screen)) {
            showHome();
            return;
        }
        super.onBackPressed();
    }

    private void showHome() {
        screen = "home";
        LinearLayout page = page("KeyKii settings", "KeyKii Neo " + appVersion(), false);

        addSection(page, "Set up keyboard");
        addActionButton(page, "Enable KeyKii", v -> {
            try {
                startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
            } catch (Exception e) {
                toast("Android keyboard settings are unavailable on this device.");
            }
        });
        addActionButton(page, "Choose Keyboard", v -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.showInputMethodPicker();
        });

        addSection(page, "Keyboard");
        addRow(page, "⌨", "Languages", "Keyboard language and Android input settings", v -> showLanguages());
        addRow(page, "⚙", "Preferences", "Size, spacing, haptics and default width", v -> showPreferences());
        addRow(page, "◐", "Theme", themeName(), v -> showTheme());
        addRow(page, "☰", "Toolbar buttons", "Choose which tools appear above the keys", v -> showToolbar());

        addSection(page, "Typing");
        addRow(page, "✓", "Corrections & suggestions", "Suggestion engine roadmap", v -> showComing(
                "Corrections & suggestions",
                "KeyKii currently sends text directly without a prediction engine. Auto-correction, suggestions and spell tools will be added in a later 2.10.x update."
        ));
        addRow(page, "〰", "Glide typing", "Swipe typing is not enabled yet", v -> showComing(
                "Glide typing",
                "Glide typing needs a gesture decoder and language model. The setting is shown here now so the structure is ready, but it is not enabled yet."
        ));
        addRow(page, "🎙", "Voice typing", "Android voice input", v -> showVoice());

        addSection(page, "Data & content");
        addRow(page, "▣", "Clipboard", "Manage KeyKii clipboard history", v -> showClipboard());
        addRow(page, "⚡", "Text shortcuts", "Save reusable text and paste it from KeyKii", v -> showShortcuts());
        addRow(page, "Aa", "Dictionary", "Open Android personal dictionary", v -> showDictionary());
        addRow(page, "☺", "Emoji & kaomoji", "Recents and emoji behavior", v -> showEmoji());

        addSection(page, "General");
        addRow(page, "🔒", "Privacy", "What KeyKii stores on this device", v -> showPrivacy());
        addRow(page, "ⓘ", "About", "Version and keyboard information", v -> showAbout());
        addRow(page, "?", "Help & feedback", "Quick troubleshooting", v -> showHelp());

        setContentView(wrap(page));
    }

    private void showLanguages() {
        screen = "languages";
        LinearLayout page = page("Languages", "Current KeyKii layout: English QWERTY", true);

        addInfoCard(page,
                "English (United States)",
                "KeyKii currently uses the English QWERTY layout. More KeyKii language layouts can be added later without changing the keyboard design.");

        addActionButton(page, "Open Android keyboard settings", v -> {
            try {
                startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
            } catch (Exception e) {
                toast("Android keyboard settings are unavailable on this device.");
            }
        });

        setContentView(wrap(page));
    }

    private void showToolbar() {
        screen = "toolbar";
        LinearLayout page = page(
                "Toolbar buttons",
                "Choose which tools appear above the keys",
                true
        );

        addInfoCard(
                page,
                "Keyboard button",
                "The ⌨ keyboard button always stays visible so you can always return to normal typing."
        );

        addSwitchRow(
                page,
                "☺  Emoji & kaomoji",
                "Show the emoji button",
                "toolbar_emoji",
                true
        );

        addSwitchRow(
                page,
                "▣  Clipboard",
                "Show clipboard and text shortcuts",
                "toolbar_clipboard",
                true
        );

        addSwitchRow(
                page,
                "✎  Quick actions",
                "Show editing and cursor tools",
                "toolbar_actions",
                true
        );

        addSwitchRow(
                page,
                "◐  Theme",
                "Show the quick theme switch button",
                "toolbar_theme",
                true
        );

        addSwitchRow(
                page,
                "↔  Width",
                "Show the normal/wide keyboard toggle",
                "toolbar_width",
                true
        );

        addActionButton(page, "Restore all toolbar buttons", v -> {
            prefs.edit()
                    .putBoolean("toolbar_emoji", true)
                    .putBoolean("toolbar_clipboard", true)
                    .putBoolean("toolbar_actions", true)
                    .putBoolean("toolbar_theme", true)
                    .putBoolean("toolbar_width", true)
                    .apply();

            toast("Toolbar restored");
            showToolbar();
        });

        addInfoCard(
                page,
                "When changes appear",
                "Close and reopen KeyKii, or switch to another text field, to refresh the toolbar."
        );

        setContentView(wrap(page));
    }


    private void showPreferences() {
        screen = "preferences";
        LinearLayout page = page("Preferences", "Changes apply the next time KeyKii opens", true);

        addSwitchRow(page,
                "Haptic feedback",
                "Vibrate lightly when a key is pressed",
                "haptic",
                false);

        addSwitchRow(page,
                "Wide keyboard by default",
                "Open KeyKii in wide mode",
                "wide_default",
                false);

        addSwitchRow(page,
                "Number row",
                "Show 1–0 above the letter keys",
                "number_row",
                false);

        addChoiceRow(page,
                "Key height",
                keyHeightName(),
                new String[]{"Compact", "Default", "Large", "Extra large"},
                new int[]{42, 46, 52, 58},
                "key_height",
                46,
                this::showPreferences);

        addChoiceRow(page,
                "Floating bottom gap",
                floatGapName(),
                new String[]{"Low", "Default", "High"},
                new int[]{56, 96, 128},
                "float_gap",
                96,
                this::showPreferences);

        addActionButton(page, "Reset keyboard preferences", v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Reset preferences?")
                    .setMessage("Theme is kept. Key size, gap, haptics, number row and wide mode will return to defaults.")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Reset", (d, which) -> {
                        prefs.edit()
                                .putInt("key_height", 46)
                                .putInt("float_gap", 96)
                                .putBoolean("haptic", false)
                                .putBoolean("wide_default", false)
                                .putBoolean("number_row", false)
                                .apply();
                        toast("Preferences reset");
                        showPreferences();
                    })
                    .show();
        });

        setContentView(wrap(page));
    }

    private void showTheme() {
        screen = "theme";

        LinearLayout page = page(
                "Theme",
                "Pick a style, color, gradient or your own image",
                true
        );

        addSection(page, "My themes");
        addMyThemeTiles(page);

        addSection(page, "Default");
        addDefaultThemeGallery(page);

        addSection(page, "Colours");

        String[] colorNames = new String[]{
                "Snow","Silver","Stone",
                "Charcoal","Black","Navy",
                "Royal","Blue","Sky",
                "Cyan","Aqua","Teal",
                "Emerald","Green","Lime",
                "Olive","Yellow","Gold",
                "Amber","Orange","Coral",
                "Red","Crimson","Wine",
                "Rose","Pink","Blush",
                "Magenta","Purple","Violet",
                "Indigo","Brown","Cocoa",
                "Sand","Cream","Mint"
        };

        int[] colors = new int[]{
                Color.rgb(248,249,250), Color.rgb(213,219,224), Color.rgb(151,157,162),
                Color.rgb(55,61,66), Color.rgb(12,12,14), Color.rgb(22,45,78),

                Color.rgb(42,105,196), Color.rgb(48,126,226), Color.rgb(105,190,235),
                Color.rgb(55,202,222), Color.rgb(100,218,222), Color.rgb(19,126,116),

                Color.rgb(36,145,83), Color.rgb(64,168,75), Color.rgb(150,198,64),
                Color.rgb(113,124,48), Color.rgb(247,205,70), Color.rgb(219,166,48),

                Color.rgb(237,174,57), Color.rgb(230,132,52), Color.rgb(240,112,90),
                Color.rgb(210,45,48), Color.rgb(174,31,55), Color.rgb(111,29,47),

                Color.rgb(207,65,101), Color.rgb(235,112,164), Color.rgb(244,171,190),
                Color.rgb(194,32,128), Color.rgb(128,60,181), Color.rgb(108,72,201),

                Color.rgb(64,55,172), Color.rgb(103,69,56), Color.rgb(113,77,63),
                Color.rgb(210,190,166), Color.rgb(246,238,218), Color.rgb(157,222,191)
        };

        addColorThemeGrid(page,colorNames,colors);

        addSection(page, "Light gradients");

        addGradientThemeGrid(
                page,
                new String[]{
                        "Cotton candy","Sea glass","Lavender",
                        "Peach sky","Lemon mint","Ocean pearl",
                        "Pink cloud","Blue blush","Spring",
                        "Sunrise","Ice","Pastel"
                },
                new int[]{
                        Color.rgb(255,184,196), Color.rgb(207,231,255),
                        Color.rgb(218,235,227), Color.rgb(199,226,220),
                        Color.rgb(225,209,247), Color.rgb(245,225,239),

                        Color.rgb(255,205,177), Color.rgb(255,238,201),
                        Color.rgb(241,229,129), Color.rgb(190,234,188),
                        Color.rgb(195,225,244), Color.rgb(236,238,255),

                        Color.rgb(251,193,213), Color.rgb(255,229,237),
                        Color.rgb(206,224,251), Color.rgb(250,209,226),
                        Color.rgb(211,239,189), Color.rgb(179,226,205),

                        Color.rgb(255,205,151), Color.rgb(250,231,166),
                        Color.rgb(202,232,255), Color.rgb(221,246,248),
                        Color.rgb(255,209,223), Color.rgb(207,236,255)
                }
        );

        addSection(page, "Dark gradients");

        addGradientThemeGrid(
                page,
                new String[]{
                        "Midnight","Graphite","Coffee",
                        "Deep ocean","Forest night","Electric",
                        "Plum night","Storm","Black glass",
                        "Burgundy","Deep teal","Indigo dusk"
                },
                new int[]{
                        Color.rgb(56,20,103), Color.rgb(164,54,141),
                        Color.rgb(78,84,88), Color.rgb(10,12,14),
                        Color.rgb(91,59,48), Color.rgb(40,24,22),

                        Color.rgb(36,71,86), Color.rgb(14,31,40),
                        Color.rgb(30,92,76), Color.rgb(12,42,37),
                        Color.rgb(21,88,190), Color.rgb(16,48,103),

                        Color.rgb(96,31,91), Color.rgb(32,19,52),
                        Color.rgb(77,92,106), Color.rgb(28,35,43),
                        Color.rgb(42,42,44), Color.rgb(4,4,5),

                        Color.rgb(105,30,49), Color.rgb(45,15,23),
                        Color.rgb(17,111,105), Color.rgb(9,45,49),
                        Color.rgb(70,62,145), Color.rgb(28,26,65)
                }
        );

        addSection(page, "Background image");

        String imageUri = prefs.getString("theme_image_uri", "");
        boolean imageActive = prefs.getInt("theme_surface_mode",0)==3;

        addPhotoThemeTile(page,imageUri,imageActive);

        addSection(page, "Fine tuning");

        addChoiceRow(
                page,
                "Accent color",
                accentColorName(),
                new String[]{
                        "Blue","Rose","Purple","Teal",
                        "Green","Orange","Gold","Cyan",
                        "Magenta","Red","Black","White"
                },
                new int[]{
                        Color.rgb(93,118,171),
                        Color.rgb(210,91,113),
                        Color.rgb(142,96,190),
                        Color.rgb(57,145,151),
                        Color.rgb(85,145,91),
                        Color.rgb(217,133,62),
                        Color.rgb(214,166,46),
                        Color.rgb(61,183,211),
                        Color.rgb(194,32,128),
                        Color.rgb(208,48,52),
                        Color.rgb(35,35,38),
                        Color.rgb(240,240,242)
                },
                "accent_color",
                Color.rgb(93,118,171),
                this::showTheme
        );

        addChoiceRow(
                page,
                "Keyboard transparency",
                themeTransparencyName(),
                new String[]{"More transparent","Transparent","Balanced","Solid"},
                new int[]{55,70,85,100},
                "theme_transparency",
                100,
                this::showTheme
        );

        addChoiceRow(
                page,
                "Key corner roundness",
                keyCornerName(),
                new String[]{"Small","Medium","Default","Very round"},
                new int[]{6,11,15,22},
                "key_corner_radius",
                15,
                this::showTheme
        );

        addActionButton(page, "Reset theme customization", v -> {
            prefs.edit()
                    .remove("accent_color")
                    .remove("theme_transparency")
                    .remove("key_corner_radius")
                    .remove("theme_custom_start")
                    .remove("theme_custom_end")
                    .putInt("theme_surface_mode", 0)
                    .apply();

            toast("Theme customization reset");
            showTheme();
        });

        setContentView(wrap(page));
    }


    private void showVoice() {
        screen = "voice";
        LinearLayout page = page("Voice typing", "Voice input is provided by Android", true);

        addInfoCard(page,
                "Voice input",
                "KeyKii does not run its own speech-recognition service yet. You can use a voice input service installed on Android.");

        addActionButton(page, "Open Android voice input settings", v -> {
            try {
                startActivity(new Intent("android.settings.VOICE_INPUT_SETTINGS"));
            } catch (Exception e) {
                toast("Voice input settings are unavailable on this device.");
            }
        });

        setContentView(wrap(page));
    }

    private void showClipboard() {
        screen = "clipboard";
        LinearLayout page = page("Clipboard", "KeyKii keeps up to six recent copied text items", true);

        SharedPreferences clips = getSharedPreferences("keykii_clipboard", MODE_PRIVATE);
        int count = 0;
        for (int i = 0; i < 6; i++) {
            if (!clips.getString("clip" + i, "").trim().isEmpty()) count++;
        }

        addInfoCard(page,
                "Saved clipboard items",
                count + (count == 1 ? " item is" : " items are") + " currently stored in KeyKii's local app data.");

        addActionButton(page, "Clear KeyKii clipboard history", v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Clear clipboard history?")
                    .setMessage("This removes KeyKii's saved clipboard items from this device.")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Clear", (d, which) -> {
                        getSharedPreferences("keykii_clipboard", MODE_PRIVATE).edit().clear().apply();
                        toast("Clipboard history cleared");
                        showClipboard();
                    })
                    .show();
        });

        setContentView(wrap(page));
    }

    private void showShortcuts() {
        screen = "shortcuts";
        LinearLayout page = page(
                "Text shortcuts",
                "Save reusable text for quick paste from KeyKii",
                true
        );

        SharedPreferences shortcuts =
                getSharedPreferences("keykii_shortcuts", MODE_PRIVATE);

        int count = 0;

        for (int i = 0; i < 12; i++) {
            String value = shortcuts.getString("shortcut_text" + i, "");
            if (value != null && !value.trim().isEmpty()) count++;
        }

        addInfoCard(
                page,
                "Saved shortcuts",
                count + (count == 1 ? " shortcut is" : " shortcuts are") +
                        " stored locally. Tap one below to edit or delete it. On the keyboard, tap to paste or hold to edit."
        );

        for (int i = 0; i < 12; i++) {
            String value = shortcuts.getString("shortcut_text" + i, "");

            if (value == null || value.trim().isEmpty())
                continue;

            String label = shortcuts.getString("shortcut_label" + i, "");
            String preview = value.replace("\n", " ").replace("\r", " ");

            if (preview.length() > 54)
                preview = preview.substring(0, 54) + "…";

            final int index = i;

            addRow(
                    page,
                    "⚡",
                    label == null || label.trim().isEmpty() ? "Shortcut" : label,
                    "Pastes: " + preview,
                    v -> showShortcutEditor(index)
            );
        }

        if (count < 12) {
            addActionButton(
                    page,
                    "Add text shortcut",
                    v -> showShortcutEditor(-1)
            );
        } else {
            addInfoCard(
                    page,
                    "Shortcut limit reached",
                    "KeyKii currently supports up to 12 saved text shortcuts."
            );
        }

        setContentView(wrap(page));
    }


    private int nextShortcutIndex(SharedPreferences shortcuts) {
        for (int i = 0; i < 12; i++) {
            String value = shortcuts.getString("shortcut_text" + i, "");
            if (value == null || value.trim().isEmpty())
                return i;
        }

        return -1;
    }


    private void compactShortcuts(SharedPreferences shortcuts) {
        java.util.ArrayList<String> labels =
                new java.util.ArrayList<>();
        java.util.ArrayList<String> values =
                new java.util.ArrayList<>();

        for (int i = 0; i < 12; i++) {
            String value = shortcuts.getString("shortcut_text" + i, "");

            if (value == null || value.trim().isEmpty())
                continue;

            labels.add(shortcuts.getString("shortcut_label" + i, ""));
            values.add(value);
        }

        SharedPreferences.Editor e = shortcuts.edit();

        for (int i = 0; i < 12; i++) {
            e.remove("shortcut_label" + i);
            e.remove("shortcut_text" + i);
        }

        for (int i = 0; i < values.size(); i++) {
            e.putString("shortcut_label" + i, labels.get(i));
            e.putString("shortcut_text" + i, values.get(i));
        }

        e.apply();
    }


    private void showShortcutEditor(int existingIndex) {
        SharedPreferences shortcuts =
                getSharedPreferences("keykii_shortcuts", MODE_PRIVATE);

        int index = existingIndex >= 0
                ? existingIndex
                : nextShortcutIndex(shortcuts);

        if (index < 0) {
            toast("Shortcut limit reached");
            return;
        }

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(22), dp(8), dp(22), 0);

        EditText label = new EditText(this);
        label.setHint("Name, e.g. Email");
        label.setSingleLine(true);
        label.setText(shortcuts.getString("shortcut_label" + index, ""));

        EditText text = new EditText(this);
        text.setHint("Text to paste");
        text.setMinLines(3);
        text.setGravity(Gravity.TOP | Gravity.START);
        text.setText(shortcuts.getString("shortcut_text" + index, ""));

        box.addView(label);
        box.addView(text);

        AlertDialog.Builder builder =
                new AlertDialog.Builder(this)
                        .setTitle(existingIndex >= 0 ? "Edit shortcut" : "Add shortcut")
                        .setView(box)
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Save", null);

        if (existingIndex >= 0) {
            builder.setNeutralButton(
                    "Delete",
                    (dialog, which) -> {
                        shortcuts.edit()
                                .remove("shortcut_label" + index)
                                .remove("shortcut_text" + index)
                                .apply();

                        compactShortcuts(shortcuts);
                        toast("Shortcut deleted");
                        showShortcuts();
                    }
            );
        }

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(d ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(v -> {
                            String value = text.getText().toString();

                            if (value.trim().isEmpty()) {
                                text.setError("Enter text to save");
                                return;
                            }

                            String name = label.getText().toString().trim();

                            if (name.isEmpty()) {
                                String oneLine = value
                                        .replace("\n", " ")
                                        .replace("\r", " ")
                                        .trim();

                                name = oneLine.length() > 24
                                        ? oneLine.substring(0, 24) + "…"
                                        : oneLine;
                            }

                            shortcuts.edit()
                                    .putString("shortcut_label" + index, name)
                                    .putString("shortcut_text" + index, value)
                                    .apply();

                            dialog.dismiss();
                            toast("Shortcut saved");
                            showShortcuts();
                        })
        );

        dialog.show();
    }


    private void showDictionary() {
        screen = "dictionary";
        LinearLayout page = page("Dictionary", "Personal words are managed by Android", true);

        addInfoCard(page,
                "Personal dictionary",
                "KeyKii does not have a separate dictionary database yet. You can manage Android's personal dictionary from here.");

        addActionButton(page, "Open personal dictionary", v -> {
            try {
                startActivity(new Intent(Settings.ACTION_USER_DICTIONARY_SETTINGS));
            } catch (Exception e) {
                toast("Personal dictionary settings are unavailable.");
            }
        });

        setContentView(wrap(page));
    }

    private void showEmoji() {
        screen = "emoji";
        LinearLayout page = page("Emoji & kaomoji", "Manage recent emoji data", true);

        String recent = getSharedPreferences("keykii_emoji", MODE_PRIVATE)
                .getString("recent", "");
        int count = recent.trim().isEmpty() ? 0 : recent.trim().split(" ").length;

        addInfoCard(page,
                "Recent emoji",
                count + (count == 1 ? " recent emoji is" : " recent emojis are") + " stored locally.");

        addInfoCard(page,
                "Skin tones",
                "On supported people and hand emoji, long-press and drag across the floating choices to select a skin tone.");

        addInfoCard(page,
                "Kaomoji",
                "The full KeyKii kaomoji library remains available from the :-) tab.");

        addActionButton(page, "Clear recent emoji", v -> {
            getSharedPreferences("keykii_emoji", MODE_PRIVATE)
                    .edit()
                    .remove("recent")
                    .apply();

            // Also clear the newer fast-recents store if present.
            getSharedPreferences("keykii_fast_emoji", MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply();

            toast("Recent emoji cleared");
            showEmoji();
        });

        setContentView(wrap(page));
    }

    private void showPrivacy() {
        screen = "privacy";
        LinearLayout page = page("Privacy", "Local KeyKii data controls", true);

        addInfoCard(page,
                "Keyboard preferences",
                "Theme, keyboard size, haptics and width preferences are stored in KeyKii's local app data.");

        addInfoCard(page,
                "Clipboard",
                "KeyKii's clipboard panel stores recent and pinned copied text locally so it can be pasted again. You can clear it from Clipboard settings.");

        addInfoCard(page,
                "Text shortcuts",
                "Saved text shortcuts are stored only in KeyKii's local app data and are pasted only when you tap them.");

        addInfoCard(page,
                "Emoji recents",
                "Recently used emoji are stored locally to build the Recent Emoji section. You can clear them from Emoji & kaomoji settings.");

        addInfoCard(page,
                "Network",
                "The current keyboard engine does not need a network connection for normal typing, emoji or kaomoji.");

        setContentView(wrap(page));
    }

    private void showAbout() {
        screen = "about";
        LinearLayout page = page("About", "KeyKii Neo", true);

        addInfoCard(page, "Version", appVersion());
        addInfoCard(page, "Package", "com.keykii.neo");
        addInfoCard(page,
                "Keyboard",
                "Custom Android input method with floating layouts, emoji search, kaomoji, clipboard tools, themes and long-press alternatives.");

        setContentView(wrap(page));
    }

    private void showHelp() {
        screen = "help";
        LinearLayout page = page("Help & feedback", "Quick checks for common problems", true);

        addInfoCard(page,
                "Settings did not apply",
                "Close and reopen the keyboard. KeyKii reloads its saved settings whenever a new input view starts.");

        addInfoCard(page,
                "Keyboard is missing",
                "Open Android keyboard settings and make sure KeyKii Neo is enabled.");

        addInfoCard(page,
                "Switch keyboards",
                "Long-press the KeyKii spacebar to open Android's keyboard chooser.");

        addActionButton(page, "Open Android keyboard settings", v -> {
            try {
                startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
            } catch (Exception e) {
                toast("Android keyboard settings are unavailable.");
            }
        });

        setContentView(wrap(page));
    }

    private void showComing(String title, String message) {
        screen = "coming";
        LinearLayout page = page(title, "Planned for the KeyKii 2.10 series", true);
        addInfoCard(page, "Not enabled yet", message);
        setContentView(wrap(page));
    }

    private String appVersion() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            return "Unknown";
        }
    }

    private ScrollView wrap(LinearLayout content) {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        // Android 15 draws behind system bars; keep settings clear of them.
        scroll.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets;
        });
        scroll.addView(content, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return scroll;
    }

    private LinearLayout page(String title, String subtitle, boolean back) {
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(18), dp(18), dp(18), dp(30));
        page.setBackgroundColor(BG);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);

        if (back) {
            TextView b = textButton("‹");
            b.setTextSize(38);
            b.setOnClickListener(v -> showHome());
            top.addView(b, new LinearLayout.LayoutParams(dp(52), dp(52)));
        }

        LinearLayout names = new LinearLayout(this);
        names.setOrientation(LinearLayout.VERTICAL);

        TextView t = new TextView(this);
        t.setText(title);
        t.setTextColor(TEXT);
        t.setTextSize(27);
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        TextView s = new TextView(this);
        s.setText(subtitle);
        s.setTextColor(MUTED);
        s.setTextSize(13);

        names.addView(t);
        names.addView(s);

        top.addView(names, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        page.addView(top);

        View gap = new View(this);
        page.addView(gap, new LinearLayout.LayoutParams(1, dp(18)));

        return page;
    }

    private void addSection(LinearLayout page, String text) {
        TextView t = new TextView(this);
        t.setText(text.toUpperCase());
        t.setTextColor(MUTED);
        t.setTextSize(12);
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setPadding(dp(8), dp(16), dp(8), dp(7));
        page.addView(t);
    }

    private void addRow(LinearLayout page, String icon, String title, String subtitle,
                        View.OnClickListener listener) {

        LinearLayout row = card();
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(15), dp(13), dp(15), dp(13));
        row.setOnClickListener(listener);

        TextView i = new TextView(this);
        i.setText(icon);
        i.setTextSize(24);
        i.setGravity(Gravity.CENTER);
        i.setTextColor(TEXT);
        row.addView(i, new LinearLayout.LayoutParams(dp(44), dp(44)));

        LinearLayout words = new LinearLayout(this);
        words.setOrientation(LinearLayout.VERTICAL);
        words.setPadding(dp(10), 0, dp(8), 0);

        TextView a = new TextView(this);
        a.setText(title);
        a.setTextColor(TEXT);
        a.setTextSize(17);

        TextView b = new TextView(this);
        b.setText(subtitle);
        b.setTextColor(MUTED);
        b.setTextSize(12);

        words.addView(a);
        words.addView(b);
        row.addView(words, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView arrow = new TextView(this);
        arrow.setText("›");
        arrow.setTextSize(30);
        arrow.setTextColor(MUTED);
        arrow.setGravity(Gravity.CENTER);
        row.addView(arrow, new LinearLayout.LayoutParams(dp(30), dp(44)));

        page.addView(row, cardParams());
    }

    private void addSwitchRow(LinearLayout page, String title, String subtitle,
                              String key, boolean def) {

        LinearLayout row = card();
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(14), dp(12), dp(14));

        LinearLayout words = new LinearLayout(this);
        words.setOrientation(LinearLayout.VERTICAL);

        TextView a = new TextView(this);
        a.setText(title);
        a.setTextColor(TEXT);
        a.setTextSize(17);

        TextView b = new TextView(this);
        b.setText(subtitle);
        b.setTextColor(MUTED);
        b.setTextSize(12);

        words.addView(a);
        words.addView(b);
        row.addView(words, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        Switch sw = new Switch(this);
        sw.setChecked(prefs.getBoolean(key, def));
        sw.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean(key, isChecked).apply());

        row.addView(sw);
        page.addView(row, cardParams());
    }

    private void addChoiceRow(LinearLayout page, String title, String current,
                              String[] labels, int[] values, String prefKey,
                              int defaultValue, Runnable refresh) {

        addRow(page, "›", title, current, v -> {
            int stored = prefs.getInt(prefKey, defaultValue);
            int checked = 0;
            for (int i = 0; i < values.length; i++) {
                if (values[i] == stored) checked = i;
            }

            new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                        prefs.edit().putInt(prefKey, values[which]).apply();
                        dialog.dismiss();
                        refresh.run();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void addMyThemeTiles(LinearLayout page) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout create = createThemeTile("＋","Create");
        create.setOnClickListener(v -> showCustomColorDialog());
        addThemeTileToRow(row,create);

        int saved = prefs.getInt(
                "theme_custom_start",
                Color.rgb(93,118,171)
        );

        LinearLayout custom = themeTile(
                "Custom",
                new int[]{saved}
        );

        custom.setOnClickListener(v ->
                applySolidTheme("Custom",saved));

        addThemeTileToRow(row,custom);

        String imageUri = prefs.getString(
                "theme_image_uri",
                ""
        );

        LinearLayout photo = createThemeTile(
                imageUri==null || imageUri.isEmpty() ? "▧" : "▣",
                "Photo"
        );

        photo.setOnClickListener(v -> {
            if(imageUri!=null && !imageUri.isEmpty()) {
                prefs.edit()
                        .putInt("theme_surface_mode",3)
                        .apply();
                toast("Photo theme selected");
                showTheme();
            } else {
                chooseThemeImage();
            }
        });

        addThemeTileToRow(row,photo);
        page.addView(row);
    }


    private void addDefaultThemeGallery(LinearLayout page) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        addDefaultThemeTile(row,"Cream",Color.rgb(247,245,242),1);
        addDefaultThemeTile(row,"Glass",Color.rgb(45,48,54),0);
        addDefaultThemeTile(row,"Clear",Color.rgb(30,32,37),2);

        page.addView(row);
    }


    private void addDefaultThemeTile(
            LinearLayout row,
            String name,
            int previewColor,
            int themeValue
    ) {
        LinearLayout tile = themeTile(
                name,
                new int[]{previewColor}
        );

        boolean selected =
                prefs.getInt("theme_surface_mode",0)==0 &&
                prefs.getInt("theme",1)==themeValue;

        markThemeTileSelected(tile,selected);

        tile.setOnClickListener(v -> {
            prefs.edit()
                    .putInt("theme",themeValue)
                    .putInt("theme_surface_mode",0)
                    .putBoolean("theme_auto_day_night",false)
                    .apply();

            toast(name+" selected");
            showTheme();
        });

        addThemeTileToRow(row,tile);
    }


    private void addColorThemeGrid(
            LinearLayout page,
            String[] names,
            int[] colors
    ) {
        for(int i=0;i<colors.length;i+=3) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);

            for(int col=0;col<3;col++) {
                int index=i+col;

                if(index<colors.length) {
                    final int color=colors[index];
                    final String name=names[index];

                    LinearLayout tile = themeTile(
                            name,
                            new int[]{color}
                    );

                    boolean selected =
                            prefs.getInt("theme_surface_mode",0)==1 &&
                            prefs.getInt("theme_custom_start",0)==color;

                    markThemeTileSelected(tile,selected);

                    tile.setOnClickListener(v ->
                            applySolidTheme(name,color));

                    addThemeTileToRow(row,tile);
                } else {
                    addThemeTileSpacer(row);
                }
            }

            page.addView(row);
        }
    }


    private void addGradientThemeGrid(
            LinearLayout page,
            String[] names,
            int[] pairs
    ) {
        int count=Math.min(names.length,pairs.length/2);

        for(int i=0;i<count;i+=3) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);

            for(int col=0;col<3;col++) {
                int index=i+col;

                if(index<count) {
                    final int start=pairs[index*2];
                    final int end=pairs[index*2+1];
                    final String name=names[index];

                    LinearLayout tile = themeTile(
                            name,
                            new int[]{start,end}
                    );

                    boolean selected =
                            prefs.getInt("theme_surface_mode",0)==2 &&
                            prefs.getInt("theme_custom_start",0)==start &&
                            prefs.getInt("theme_custom_end",0)==end;

                    markThemeTileSelected(tile,selected);

                    tile.setOnClickListener(v ->
                            applyGradientTheme(name,start,end));

                    addThemeTileToRow(row,tile);
                } else {
                    addThemeTileSpacer(row);
                }
            }

            page.addView(row);
        }
    }


    private LinearLayout themeTile(
            String name,
            int[] colors
    ) {
        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setPadding(dp(3),dp(3),dp(3),dp(6));

        TextView preview = new TextView(this);
        preview.setText("━━━━  ●");
        preview.setTextSize(15);
        preview.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        preview.setPadding(dp(5),dp(6),dp(5),dp(8));

        int middle=colors[0];
        if(colors.length>1)
            middle=averageColor(colors[0],colors[colors.length-1]);

        preview.setTextColor(
                isLightThemeColor(middle)
                        ? Color.argb(155,55,60,65)
                        : Color.argb(205,245,245,247)
        );

        GradientDrawable bg;

        if(colors.length>1) {
            bg=new GradientDrawable(
                    GradientDrawable.Orientation.TL_BR,
                    colors
            );
        } else {
            bg=new GradientDrawable();
            bg.setColor(colors[0]);
        }

        bg.setCornerRadius(dp(16));
        bg.setStroke(dp(1),BORDER);
        preview.setBackground(bg);

        tile.addView(
                preview,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(68)
                )
        );

        TextView label = new TextView(this);
        label.setText(name);
        label.setTextColor(TEXT);
        label.setTextSize(10);
        label.setGravity(Gravity.CENTER);
        label.setSingleLine(true);
        label.setPadding(0,dp(4),0,0);

        tile.addView(
                label,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(25)
                )
        );

        return tile;
    }


    private LinearLayout createThemeTile(
            String symbol,
            String labelText
    ) {
        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setPadding(dp(3),dp(3),dp(3),dp(6));

        TextView preview = new TextView(this);
        preview.setText(symbol);
        preview.setTextSize(34);
        preview.setGravity(Gravity.CENTER);
        preview.setTextColor(Color.rgb(77,99,148));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(16));
        bg.setStroke(dp(2),Color.rgb(101,124,173));
        preview.setBackground(bg);

        tile.addView(
                preview,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(68)
                )
        );

        TextView label = new TextView(this);
        label.setText(labelText);
        label.setTextColor(TEXT);
        label.setTextSize(10);
        label.setGravity(Gravity.CENTER);
        label.setPadding(0,dp(4),0,0);

        tile.addView(
                label,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(25)
                )
        );

        return tile;
    }


    private void addThemeTileToRow(
            LinearLayout row,
            View tile
    ) {
        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                );

        p.setMargins(dp(3),dp(3),dp(3),dp(5));
        row.addView(tile,p);
    }


    private void addThemeTileSpacer(LinearLayout row) {
        View spacer=new View(this);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        0,
                        dp(1),
                        1f
                );

        p.setMargins(dp(3),0,dp(3),0);
        row.addView(spacer,p);
    }


    private void markThemeTileSelected(
            LinearLayout tile,
            boolean selected
    ) {
        if(!selected) return;

        GradientDrawable frame = new GradientDrawable();
        frame.setColor(Color.TRANSPARENT);
        frame.setCornerRadius(dp(18));
        frame.setStroke(dp(3),Color.rgb(82,107,161));
        tile.setBackground(frame);
    }


    private void applySolidTheme(
            String name,
            int color
    ) {
        prefs.edit()
                .putInt("theme_custom_start",color)
                .putInt("theme_custom_end",color)
                .putInt("theme_surface_mode",1)
                .putInt("theme",isLightThemeColor(color) ? 1 : 0)
                .putBoolean("theme_auto_day_night",false)
                .apply();

        toast(name+" selected");
        showTheme();
    }


    private void applyGradientTheme(
            String name,
            int start,
            int end
    ) {
        int average=averageColor(start,end);

        prefs.edit()
                .putInt("theme_custom_start",start)
                .putInt("theme_custom_end",end)
                .putInt("theme_surface_mode",2)
                .putInt("theme",isLightThemeColor(average) ? 1 : 0)
                .putBoolean("theme_auto_day_night",false)
                .apply();

        toast(name+" selected");
        showTheme();
    }


    private void addPhotoThemeTile(
            LinearLayout page,
            String imageUri,
            boolean active
    ) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout choose = createThemeTile(
                "▧",
                imageUri==null || imageUri.isEmpty()
                        ? "Choose photo"
                        : "Change photo"
        );

        choose.setOnClickListener(v -> chooseThemeImage());
        addThemeTileToRow(row,choose);

        if(imageUri!=null && !imageUri.isEmpty()) {
            LinearLayout use=createThemeTile(
                    active ? "✓" : "▣",
                    active ? "Photo active" : "Use photo"
            );

            use.setOnClickListener(v -> {
                prefs.edit()
                        .putInt("theme_surface_mode",3)
                        .apply();

                toast("Photo theme selected");
                showTheme();
            });

            addThemeTileToRow(row,use);

            LinearLayout remove=createThemeTile("×","Remove");

            remove.setOnClickListener(v -> {
                prefs.edit()
                        .remove("theme_image_uri")
                        .putInt("theme_surface_mode",0)
                        .apply();

                toast("Theme image removed");
                showTheme();
            });

            addThemeTileToRow(row,remove);
        } else {
            addThemeTileSpacer(row);
            addThemeTileSpacer(row);
        }

        page.addView(row);
    }


    private void chooseThemeImage() {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            intent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION |
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            );

            startActivityForResult(intent,REQUEST_THEME_IMAGE);
        } catch(Exception e) {
            toast("Image picker is unavailable on this device.");
        }
    }


    private int averageColor(int a,int b) {
        return Color.rgb(
                (Color.red(a)+Color.red(b))/2,
                (Color.green(a)+Color.green(b))/2,
                (Color.blue(a)+Color.blue(b))/2
        );
    }


    private boolean isLightThemeColor(int color) {
        int brightness =
                (Color.red(color)*299 +
                 Color.green(color)*587 +
                 Color.blue(color)*114) / 1000;

        return brightness>=155;
    }


    private void showCustomColorDialog() {
        int current = prefs.getInt(
                "theme_custom_start",
                Color.rgb(93,118,171)
        );

        final int[] chosen=new int[]{current};
        final int[] initial=new int[]{
                Color.red(current),
                Color.green(current),
                Color.blue(current)
        };
        final SeekBar[] bars=new SeekBar[3];

        LinearLayout box=new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(22),dp(8),dp(22),0);

        TextView preview=new TextView(this);
        preview.setTextSize(16);
        preview.setGravity(Gravity.CENTER);

        box.addView(
                preview,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(82)
                )
        );

        final String[] labels=new String[]{"Red","Green","Blue"};

        Runnable refreshPreview=() -> {
            int red=bars[0]==null ? initial[0] : bars[0].getProgress();
            int green=bars[1]==null ? initial[1] : bars[1].getProgress();
            int blue=bars[2]==null ? initial[2] : bars[2].getProgress();

            chosen[0]=Color.rgb(red,green,blue);

            GradientDrawable d=new GradientDrawable();
            d.setColor(chosen[0]);
            d.setCornerRadius(dp(18));
            preview.setBackground(d);

            preview.setTextColor(
                    isLightThemeColor(chosen[0])
                            ? Color.rgb(40,40,42)
                            : Color.WHITE
            );

            preview.setText(
                    String.format("#%02X%02X%02X",red,green,blue)
            );
        };

        for(int i=0;i<3;i++) {
            final int channel=i;

            TextView label=new TextView(this);
            label.setText(labels[channel]+"  "+initial[channel]);
            label.setTextColor(TEXT);
            label.setTextSize(13);
            label.setPadding(0,dp(10),0,0);
            box.addView(label);

            SeekBar bar=new SeekBar(this);
            bar.setMax(255);
            bar.setProgress(initial[channel]);
            bars[channel]=bar;

            bar.setOnSeekBarChangeListener(
                    new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(
                                SeekBar seekBar,
                                int progress,
                                boolean fromUser
                        ) {
                            label.setText(
                                    labels[channel]+"  "+progress
                            );
                            refreshPreview.run();
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {}

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {}
                    }
            );

            box.addView(bar);
        }

        refreshPreview.run();

        new AlertDialog.Builder(this)
                .setTitle("Create any color")
                .setView(box)
                .setNegativeButton("Cancel",null)
                .setPositiveButton(
                        "Use color",
                        (dialog,which) ->
                                applySolidTheme("Custom",chosen[0])
                )
                .show();
    }


    private void addThemeChoice(LinearLayout page, String title, String subtitle, int value) {
        int current;

        if (prefs.getBoolean("theme_auto_day_night", false)) {
            int nightMode =
                    getResources().getConfiguration().uiMode &
                    android.content.res.Configuration.UI_MODE_NIGHT_MASK;

            current = nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
                    ? prefs.getInt("theme_dark", 0)
                    : prefs.getInt("theme_light", 1);
        } else {
            current = prefs.getInt("theme", 1);
        }

        LinearLayout row = card();
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(15), dp(16), dp(15));

        TextView dot = new TextView(this);
        dot.setText(current == value ? "●" : "○");
        dot.setTextSize(26);
        dot.setTextColor(current == value ? Color.rgb(93, 118, 171) : MUTED);
        dot.setGravity(Gravity.CENTER);
        row.addView(dot, new LinearLayout.LayoutParams(dp(40), dp(44)));

        LinearLayout words = new LinearLayout(this);
        words.setOrientation(LinearLayout.VERTICAL);

        TextView a = new TextView(this);
        a.setText(title);
        a.setTextColor(TEXT);
        a.setTextSize(17);

        TextView b = new TextView(this);
        b.setText(subtitle);
        b.setTextColor(MUTED);
        b.setTextSize(12);

        words.addView(a);
        words.addView(b);
        row.addView(words, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        row.setOnClickListener(v -> {
            SharedPreferences.Editor e = prefs.edit();

            if (prefs.getBoolean("theme_auto_day_night", false)) {
                int nightMode =
                        getResources().getConfiguration().uiMode &
                        android.content.res.Configuration.UI_MODE_NIGHT_MASK;

                if (nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
                    e.putInt("theme_dark", value);
                } else {
                    e.putInt("theme_light", value);
                }
            } else {
                e.putInt("theme", value);
            }

            e.apply();
            toast(title + " selected");
            showTheme();
        });

        page.addView(row, cardParams());
    }

    private void addInfoCard(LinearLayout page, String title, String message) {
        LinearLayout box = card();
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(17), dp(15), dp(17), dp(15));

        TextView a = new TextView(this);
        a.setText(title);
        a.setTextColor(TEXT);
        a.setTextSize(16);
        a.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        TextView b = new TextView(this);
        b.setText(message);
        b.setTextColor(MUTED);
        b.setTextSize(13);
        b.setPadding(0, dp(5), 0, 0);

        box.addView(a);
        box.addView(b);
        page.addView(box, cardParams());
    }

    private void addActionButton(LinearLayout page, String text, View.OnClickListener listener) {
        TextView b = new TextView(this);
        b.setText(text);
        b.setTextColor(TEXT);
        b.setTextSize(15);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(14), dp(14), dp(14), dp(14));
        b.setBackground(round(ACCENT, 18));
        b.setOnClickListener(listener);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, dp(9), 0, dp(2));
        page.addView(b, p);
    }

    private LinearLayout card() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        GradientDrawable bg = round(CARD, 20);
        bg.setStroke(dp(1), BORDER);
        row.setBackground(bg);
        row.setElevation(dp(1));

        return row;
    }

    private LinearLayout.LayoutParams cardParams() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, dp(5), 0, dp(5));
        return p;
    }

    private TextView textButton(String text) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextColor(TEXT);
        v.setGravity(Gravity.CENTER);
        v.setBackground(round(SOFT, 16));
        return v;
    }

    private GradientDrawable round(int color, int radiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radiusDp));
        return d;
    }

    private String themeDisplayName(int value) {
        if (value == 1) return "Morning Cream";
        if (value == 2) return "Clear Glass";
        return "Glass Dark";
    }


    private String accentColorName() {
        int value = prefs.getInt(
                "accent_color",
                Color.rgb(93,118,171)
        );

        if (value == Color.rgb(210,91,113)) return "Rose";
        if (value == Color.rgb(142,96,190)) return "Purple";
        if (value == Color.rgb(57,145,151)) return "Teal";
        if (value == Color.rgb(85,145,91)) return "Green";
        if (value == Color.rgb(217,133,62)) return "Orange";
        if (value == Color.rgb(214,166,46)) return "Gold";
        if (value == Color.rgb(61,183,211)) return "Cyan";
        if (value == Color.rgb(194,32,128)) return "Magenta";
        if (value == Color.rgb(208,48,52)) return "Red";
        if (value == Color.rgb(35,35,38)) return "Black";
        if (value == Color.rgb(240,240,242)) return "White";
        return "Blue";
    }

    private String themeTransparencyName() {
        int value = prefs.getInt("theme_transparency", 100);

        if (value <= 55) return "More transparent";
        if (value <= 70) return "Transparent";
        if (value <= 85) return "Balanced";
        return "Solid";
    }


    private String keyCornerName() {
        int value = prefs.getInt("key_corner_radius", 15);

        if (value <= 6) return "Small";
        if (value <= 11) return "Medium";
        if (value >= 22) return "Very round";
        return "Default";
    }


    private String themeName() {
        int theme;

        if (prefs.getBoolean("theme_auto_day_night", false)) {
            int nightMode =
                    getResources().getConfiguration().uiMode &
                    android.content.res.Configuration.UI_MODE_NIGHT_MASK;

            theme = nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
                    ? prefs.getInt("theme_dark", 0)
                    : prefs.getInt("theme_light", 1);
        } else {
            theme = prefs.getInt("theme", 1);
        }

        return themeDisplayName(theme);
    }

    private String keyHeightName() {
        int value = prefs.getInt("key_height", 46);
        if (value <= 42) return "Compact";
        if (value >= 58) return "Extra large";
        if (value >= 52) return "Large";
        return "Default";
    }

    private String floatGapName() {
        int value = prefs.getInt("float_gap", 96);
        if (value <= 56) return "Low";
        if (value >= 128) return "High";
        return "Default";
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
