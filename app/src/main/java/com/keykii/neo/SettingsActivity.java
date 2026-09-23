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
                "Customize colors, transparency, corners and background",
                true
        );

        addSection(page, "Base theme");

        addThemeChoice(page, "Glass Dark", "Dark translucent KeyKii panel", 0);
        addThemeChoice(page, "Morning Cream", "Warm cream floating keyboard", 1);
        addThemeChoice(page, "Clear Glass", "Darker transparent glass look", 2);

        addSection(page, "Customization");

        addChoiceRow(
                page,
                "Accent color",
                accentColorName(),
                new String[]{
                        "Blue",
                        "Rose",
                        "Purple",
                        "Teal",
                        "Green",
                        "Orange"
                },
                new int[]{
                        Color.rgb(93,118,171),
                        Color.rgb(210,91,113),
                        Color.rgb(142,96,190),
                        Color.rgb(57,145,151),
                        Color.rgb(85,145,91),
                        Color.rgb(217,133,62)
                },
                "accent_color",
                Color.rgb(93,118,171),
                this::showTheme
        );

        addChoiceRow(
                page,
                "Keyboard transparency",
                themeTransparencyName(),
                new String[]{
                        "More transparent",
                        "Transparent",
                        "Balanced",
                        "Solid"
                },
                new int[]{55,70,85,100},
                "theme_transparency",
                100,
                this::showTheme
        );

        addChoiceRow(
                page,
                "Key corner roundness",
                keyCornerName(),
                new String[]{
                        "Small",
                        "Medium",
                        "Default",
                        "Very round"
                },
                new int[]{6,11,15,22},
                "key_corner_radius",
                15,
                this::showTheme
        );

        addSection(page, "Light & dark appearance");

        addSwitchRow(
                page,
                "Automatic light/dark themes",
                "Use a different KeyKii theme for light and dark phone appearance",
                "theme_auto_day_night",
                false
        );

        if (prefs.getBoolean("theme_auto_day_night", false)) {
            addChoiceRow(
                    page,
                    "Light appearance",
                    themeDisplayName(prefs.getInt("theme_light", 1)),
                    new String[]{
                            "Glass Dark",
                            "Morning Cream",
                            "Clear Glass"
                    },
                    new int[]{0,1,2},
                    "theme_light",
                    1,
                    this::showTheme
            );

            addChoiceRow(
                    page,
                    "Dark appearance",
                    themeDisplayName(prefs.getInt("theme_dark", 0)),
                    new String[]{
                            "Glass Dark",
                            "Morning Cream",
                            "Clear Glass"
                    },
                    new int[]{0,1,2},
                    "theme_dark",
                    0,
                    this::showTheme
            );
        }

        addSection(page, "Background image");

        String imageUri = prefs.getString("theme_image_uri", "");

        addInfoCard(
                page,
                "Custom image",
                imageUri == null || imageUri.isEmpty()
                        ? "No image selected. Your normal KeyKii theme background is being used."
                        : "A custom image is active behind the keyboard. Your theme overlay keeps the keys readable."
        );

        addActionButton(page, "Choose background image", v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                intent.addFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION |
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                );
                startActivityForResult(intent, REQUEST_THEME_IMAGE);
            } catch (Exception e) {
                toast("Image picker is unavailable on this device.");
            }
        });

        if (imageUri != null && !imageUri.isEmpty()) {
            addActionButton(page, "Remove background image", v -> {
                prefs.edit()
                        .remove("theme_image_uri")
                        .apply();

                toast("Theme image removed");
                showTheme();
            });
        }

        addActionButton(page, "Reset theme customization", v -> {
            prefs.edit()
                    .remove("accent_color")
                    .remove("theme_transparency")
                    .remove("key_corner_radius")
                    .putBoolean("theme_auto_day_night", false)
                    .remove("theme_light")
                    .remove("theme_dark")
                    .apply();

            toast("Theme customization reset");
            showTheme();
        });

        addInfoCard(
                page,
                "Current appearance",
                themeName() + ". Changes apply the next time the keyboard view refreshes."
        );

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
