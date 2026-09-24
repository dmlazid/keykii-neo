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
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {

    private static final int REQUEST_THEME_IMAGE = 2160;
    private static final int REQUEST_RECORD_AUDIO = 2161;

    private static final int BG = Color.rgb(248, 246, 242);
    private static final int CARD = Color.WHITE;
    private static final int TEXT = Color.rgb(45, 43, 40);
    private static final int MUTED = Color.rgb(118, 113, 107);
    private static final int BORDER = Color.rgb(229, 224, 217);
    private static final int ACCENT = Color.rgb(239, 232, 221);
    private static final int SOFT = Color.rgb(245, 241, 235);

    private SharedPreferences prefs;
    private String screen = "home";

    private ScrollView themeScrollView;
    private int themeScrollY = 0;
    private String pendingThemeImageUri = "";

    // Keep every settings screen at the same scroll position when it
    // refreshes itself after a tap. This avoids the recurring jump-to-top
    // glitch across toolbar, preferences, shortcuts, themes, and future pages.
    private ScrollView activeSettingsScrollView;
    private String activeSettingsScrollScreen = "";
    private final java.util.HashMap<String,Integer> settingsScrollPositions =
            new java.util.HashMap<>();

    // Gboard-style theme picker draft state. Theme tiles only update this
    // preview; nothing is saved until Apply is pressed.
    private LinearLayout themePreviewSheet;
    private FrameLayout themePreviewFrame;
    private Switch themePreviewBorderSwitch;
    private final java.util.ArrayList<LinearLayout> themeSelectableTiles =
            new java.util.ArrayList<>();

    private int themeDraftSurfaceMode = 0;
    private int themeDraftTheme = 1;
    private int themeDraftStart = Color.rgb(93,118,171);
    private int themeDraftEnd = Color.rgb(93,118,171);
    private String themeDraftImageUri = "";
    private boolean themeDraftKeyBorders = true;
    private int themeDraftStylePack = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("keykii_prefs", MODE_PRIVATE);

        String openScreen=
                getIntent().getStringExtra(
                        "open_screen"
                );

        if ("shortcuts".equals(openScreen)) {
            showShortcuts();

            int shortcutIndex = getIntent().getIntExtra("shortcut_index", -1);
            if (shortcutIndex >= 0 && shortcutIndex < 12) {
                showShortcutEditor(shortcutIndex);
            }

        } else if ("theme".equals(openScreen)) {
            showTheme();

        } else if ("fonts".equals(openScreen)) {
            showFonts();

        } else if ("toolbar".equals(openScreen)) {
            showToolbar();

        } else if ("voice".equals(openScreen)) {
            showVoice();

        } else if ("languages".equals(openScreen)) {
            showLanguages();

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

            pendingThemeImageUri = uri.toString();

            themeDraftSurfaceMode = 3;
            themeDraftTheme = 0;
            themeDraftImageUri = pendingThemeImageUri;
            themeDraftKeyBorders = false;

            if ("theme".equals(screen) && themePreviewFrame != null) {
                showThemePreviewSheet();
                refreshThemeTileSelection();
            } else {
                showTheme();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {
        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if(requestCode==REQUEST_RECORD_AUDIO) {
            if(
                grantResults.length>0 &&
                grantResults[0]==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                toast("Microphone access allowed");
            } else {
                toast("Microphone access is needed for voice typing");
            }

            showVoice();
        }
    }


    @Override
    public void onBackPressed() {
        if ("photo_theme".equals(screen)) {
            showTheme();
            return;
        }

        if ("language_add".equals(screen)) {
            showLanguages();
            return;
        }

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
        addRow(page, "⌨", "Languages", "Add and switch KeyKii keyboard layouts", v -> showLanguages());
        addRow(page, "⚙", "Preferences", "Size, spacing, haptics and default width", v -> showPreferences());
        addRow(page, "◐", "Themes", "Backgrounds, colours and key shapes", v -> showTheme());
        addRow(page, "Aa", "Fonts", keyboardFontName(), v -> showFonts());
        addRow(page, "☰", "Toolbar buttons", "Choose which tools appear above the keys", v -> showToolbar());

        addSection(page, "Typing");
        addRow(
                page,
                "✓",
                "Smart typing",
                "Auto-capitalization, double-space period and key preview",
                v -> showSmartTyping()
        );
        addRow(
                page,
                "〰",
                "Glide typing",
                prefs.getBoolean("glide_typing", false)
                        ? "On • Swipe across letters to type"
                        : "Off • Swipe across letters to type",
                v -> showGlideTyping()
        );
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

    private final String[] keyboardLanguageCodes = {
            "en-US","en-GB","fil","ceb","es",
            "fr","de","tr","pt","it"
    };

    private final String[] keyboardLanguageNames = {
            "English (US)","English (UK)","Filipino",
            "Cebuano","Spanish","French","German",
            "Turkish","Portuguese","Italian"
    };

    private final String[] keyboardLanguageLayouts = {
            "QWERTY","QWERTY","QWERTY","QWERTY","QWERTY",
            "AZERTY","QWERTZ","Turkish QWERTY","QWERTY","QWERTY"
    };


    private java.util.ArrayList<String> enabledKeyboardLanguages() {
        String saved =
                prefs.getString(
                        "keyboard_languages",
                        "en-US"
                );

        java.util.ArrayList<String> out =
                new java.util.ArrayList<>();

        if (saved != null) {
            for (String part : saved.split(",")) {
                String code =
                        part == null
                                ? ""
                                : part.trim();

                if (
                        !code.isEmpty() &&
                        !out.contains(code)
                ) {
                    out.add(code);
                }
            }
        }

        if (out.isEmpty())
            out.add("en-US");

        return out;
    }


    private void saveEnabledKeyboardLanguages(
            java.util.ArrayList<String> languages
    ) {
        if (languages == null || languages.isEmpty()) {
            languages =
                    new java.util.ArrayList<>();
            languages.add("en-US");
        }

        String joined =
                android.text.TextUtils.join(
                        ",",
                        languages
                );

        String active =
                prefs.getString(
                        "keyboard_language_active",
                        languages.get(0)
                );

        if (!languages.contains(active))
            active = languages.get(0);

        prefs.edit()
                .putString(
                        "keyboard_languages",
                        joined
                )
                .putString(
                        "keyboard_language_active",
                        active
                )
                .apply();
    }


    private int keyboardLanguageIndex(
            String code
    ) {
        for (
                int i=0;
                i<keyboardLanguageCodes.length;
                i++
        ) {
            if (
                    keyboardLanguageCodes[i]
                            .equals(code)
            ) {
                return i;
            }
        }

        return 0;
    }


    private String keyboardLanguageName(
            String code
    ) {
        return keyboardLanguageNames[
                keyboardLanguageIndex(code)
        ];
    }


    private String keyboardLanguageLayout(
            String code
    ) {
        return keyboardLanguageLayouts[
                keyboardLanguageIndex(code)
        ];
    }


    private void showLanguages() {
        screen = "languages";

        java.util.ArrayList<String> enabled =
                enabledKeyboardLanguages();

        String active =
                prefs.getString(
                        "keyboard_language_active",
                        enabled.get(0)
                );

        if (!enabled.contains(active))
            active = enabled.get(0);

        LinearLayout page =
                page(
                        "Languages",
                        "Keyboard languages and layouts",
                        true
                );

        addInfoCard(
                page,
                "Switch languages",
                "Tap the 🌐 globe key on KeyKii to switch between the keyboards you add here. The active language is shown on the spacebar."
        );

        addSection(
                page,
                "Your keyboards"
        );

        for (String code : enabled) {
            final String languageCode = code;
            final boolean isActive =
                    code.equals(active);

            addRow(
                    page,
                    isActive ? "✓" : "⌨",
                    keyboardLanguageName(code),
                    keyboardLanguageLayout(code) +
                            (isActive ? " • Active" : ""),
                    v -> showKeyboardLanguageOptions(
                            languageCode
                    )
            );
        }

        addActionButton(
                page,
                "+ Add keyboard",
                v -> showAddKeyboard()
        );

        addInfoCard(
                page,
                "Typing",
                "Filipino and Cebuano use the familiar QWERTY layout. Spanish, French, German, Turkish, Portuguese and Italian include their common letters and accents. Glide typing currently stays English-only."
        );

        setContentView(
                wrap(page)
        );
    }


    private void showKeyboardLanguageOptions(
            String code
    ) {
        java.util.ArrayList<String> enabled =
                enabledKeyboardLanguages();

        String active =
                prefs.getString(
                        "keyboard_language_active",
                        enabled.get(0)
                );

        java.util.ArrayList<String> options =
                new java.util.ArrayList<>();

        options.add(
                code.equals(active)
                        ? "Active keyboard"
                        : "Set as active"
        );

        if (enabled.size()>1)
            options.add("Remove keyboard");

        new AlertDialog.Builder(this)
                .setTitle(
                        keyboardLanguageName(code)
                )
                .setItems(
                        options.toArray(
                                new String[0]
                        ),
                        (dialog,which) -> {
                            String choice =
                                    options.get(which);

                            if (
                                    choice.equals(
                                            "Set as active"
                                    )
                            ) {
                                prefs.edit()
                                        .putString(
                                                "keyboard_language_active",
                                                code
                                        )
                                        .apply();

                                toast(
                                        keyboardLanguageName(code) +
                                                " selected"
                                );

                                showLanguages();

                            } else if (
                                    choice.equals(
                                            "Remove keyboard"
                                    )
                            ) {
                                enabled.remove(code);
                                saveEnabledKeyboardLanguages(
                                        enabled
                                );
                                showLanguages();
                            }
                        }
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .show();
    }


    private void showAddKeyboard() {
        screen = "language_add";

        LinearLayout page =
                page(
                        "Add keyboard",
                        "Search or choose a language",
                        true
                );

        EditText search =
                new EditText(this);

        search.setHint(
                "Search language"
        );

        search.setSingleLine(true);
        search.setTextColor(TEXT);
        search.setHintTextColor(MUTED);
        search.setTextSize(17);

        search.setPadding(
                dp(18),
                dp(10),
                dp(18),
                dp(10)
        );

        search.setBackground(
                round(
                        CARD,
                        28
                )
        );

        LinearLayout.LayoutParams searchParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(56)
                );

        searchParams.setMargins(
                dp(4),
                dp(8),
                dp(4),
                dp(10)
        );

        page.addView(
                search,
                searchParams
        );

        addSection(
                page,
                "All languages"
        );

        LinearLayout results =
                new LinearLayout(this);

        results.setOrientation(
                LinearLayout.VERTICAL
        );

        page.addView(
                results,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        refreshAddKeyboardResults(
                results,
                ""
        );

        search.addTextChangedListener(
                new android.text.TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence text,
                            int start,
                            int count,
                            int after
                    ) {}

                    @Override
                    public void onTextChanged(
                            CharSequence text,
                            int start,
                            int before,
                            int count
                    ) {
                        refreshAddKeyboardResults(
                                results,
                                text == null
                                        ? ""
                                        : text.toString()
                        );
                    }

                    @Override
                    public void afterTextChanged(
                            android.text.Editable editable
                    ) {}
                }
        );

        setContentView(
                wrap(page)
        );
    }


    private void refreshAddKeyboardResults(
            LinearLayout results,
            String query
    ) {
        results.removeAllViews();

        java.util.ArrayList<String> enabled =
                enabledKeyboardLanguages();

        String q =
                query == null
                        ? ""
                        : query.trim()
                                .toLowerCase(
                                        java.util.Locale.ROOT
                                );

        int shown = 0;

        for (
                int i=0;
                i<keyboardLanguageCodes.length;
                i++
        ) {
            String code =
                    keyboardLanguageCodes[i];

            String name =
                    keyboardLanguageNames[i];

            if (enabled.contains(code))
                continue;

            if (
                    !q.isEmpty() &&
                    !name.toLowerCase(
                            java.util.Locale.ROOT
                    ).contains(q)
            ) {
                continue;
            }

            final String languageCode = code;

            addRow(
                    results,
                    "＋",
                    name,
                    keyboardLanguageLayouts[i],
                    v -> {
                        java.util.ArrayList<String> current =
                                enabledKeyboardLanguages();

                        if (
                                !current.contains(
                                        languageCode
                                )
                        ) {
                            current.add(
                                    languageCode
                            );

                            saveEnabledKeyboardLanguages(
                                    current
                            );

                            toast(
                                    keyboardLanguageName(
                                            languageCode
                                    ) +
                                            " added"
                            );
                        }

                        showLanguages();
                    }
            );

            shown++;
        }

        if (shown==0) {
            addInfoCard(
                    results,
                    "No languages found",
                    q.isEmpty()
                            ? "All available KeyKii languages are already added."
                            : "Try another language name."
            );
        }
    }


    private void showToolbar() {
        screen = "toolbar";

        LinearLayout page = page(
                "Toolbar buttons",
                "Show, hide and reorder tools above the keys",
                true
        );

        addInfoCard(
                page,
                "Keyboard button",
                "The ⌨ keyboard button always stays first and cannot be hidden."
        );

        addSection(page, "Show or hide");

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
                "🎙  Voice typing",
                "Show the microphone button",
                "toolbar_voice",
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
                "⛶  Full / wide",
                "Optional shortcut. Full / wide is also in the Tools panel",
                "toolbar_width",
                true
        );

        addSwitchRow(
                page,
                "◀  One-handed",
                "Optional shortcut. One-handed is also in the Tools panel",
                "toolbar_hand",
                true
        );

        addSection(page, "Order");

        addInfoCard(
                page,
                "Reorder toolbar",
                "Use ↑ and ↓ to choose the order. Hidden buttons keep their place and return there when enabled again."
        );

        java.util.ArrayList<String> order=
                toolbarOrder();

        for(int i=0;i<order.size();i++) {
            addToolbarOrderRow(
                    page,
                    order.get(i),
                    i,
                    order.size()
            );
        }

        addActionButton(
                page,
                "Restore default toolbar",
                v -> {
                    prefs.edit()
                            .putBoolean("toolbar_emoji", true)
                            .putBoolean("toolbar_clipboard", true)
                            .putBoolean("toolbar_actions", true)
                            .putBoolean("toolbar_voice", true)
                            .putBoolean("toolbar_theme", true)
                            .putBoolean("toolbar_width", false)
                            .putBoolean("toolbar_hand", false)
                            .putString(
                                    "toolbar_order",
                                    "emoji,clipboard,actions,voice,theme,width,hand"
                            )
                            .apply();

                    toast("Toolbar restored");
                    showToolbar();
                }
        );

        addInfoCard(
                page,
                "When changes appear",
                "Switch to another text field or reopen KeyKii to refresh the toolbar."
        );

        setContentView(wrap(page));
    }


    private java.util.ArrayList<String> toolbarOrder() {
        String stored=
                prefs.getString(
                        "toolbar_order",
                        "emoji,clipboard,actions,theme,width,hand"
                );

        java.util.LinkedHashSet<String> clean=
                new java.util.LinkedHashSet<>();

        if(stored!=null) {
            for(String id:stored.split(",")) {
                String item=id.trim();

                if(
                    item.equals("emoji") ||
                    item.equals("clipboard") ||
                    item.equals("actions") ||
                    item.equals("voice") ||
                    item.equals("theme") ||
                    item.equals("width") ||
                    item.equals("hand")
                ) {
                    clean.add(item);
                }
            }
        }

        clean.add("emoji");
        clean.add("clipboard");
        clean.add("actions");
        clean.add("voice");
        clean.add("theme");
        clean.add("width");
        clean.add("hand");

        return new java.util.ArrayList<>(
                clean
        );
    }


    private void saveToolbarOrder(
            java.util.ArrayList<String> order
    ) {
        StringBuilder value=
                new StringBuilder();

        for(int i=0;i<order.size();i++) {
            if(i>0)
                value.append(",");

            value.append(order.get(i));
        }

        prefs.edit()
                .putString(
                        "toolbar_order",
                        value.toString()
                )
                .apply();
    }


    private void moveToolbarItem(
            String id,
            int direction
    ) {
        java.util.ArrayList<String> order=
                toolbarOrder();

        int index=order.indexOf(id);

        if(index<0)
            return;

        int target=index+direction;

        if(
            target<0 ||
            target>=order.size()
        ) {
            return;
        }

        java.util.Collections.swap(
                order,
                index,
                target
        );

        saveToolbarOrder(order);
        showToolbar();
    }


    private void addToolbarOrderRow(
            LinearLayout page,
            String id,
            int index,
            int count
    ) {
        LinearLayout row=card();

        row.setGravity(
                Gravity.CENTER_VERTICAL
        );

        row.setPadding(
                dp(15),
                dp(10),
                dp(10),
                dp(10)
        );

        TextView icon=
                new TextView(this);

        icon.setText(
                toolbarIcon(id)
        );

        icon.setTextSize(23);
        icon.setTextColor(TEXT);
        icon.setGravity(Gravity.CENTER);

        row.addView(
                icon,
                new LinearLayout.LayoutParams(
                        dp(42),
                        dp(44)
                )
        );

        LinearLayout words=
                new LinearLayout(this);

        words.setOrientation(
                LinearLayout.VERTICAL
        );

        words.setPadding(
                dp(8),
                0,
                dp(8),
                0
        );

        TextView title=
                new TextView(this);

        title.setText(
                toolbarTitle(id)
        );

        title.setTextColor(TEXT);
        title.setTextSize(16);

        TextView subtitle=
                new TextView(this);

        subtitle.setText(
                "Position " +
                (index+1)
        );

        subtitle.setTextColor(MUTED);
        subtitle.setTextSize(11);

        words.addView(title);
        words.addView(subtitle);

        row.addView(
                words,
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                )
        );

        TextView up=
                textButton("↑");

        up.setTextSize(22);
        up.setAlpha(
                index==0
                        ? .3f
                        : 1f
        );

        up.setEnabled(index>0);

        up.setOnClickListener(v ->
                moveToolbarItem(
                        id,
                        -1
                ));

        row.addView(
                up,
                new LinearLayout.LayoutParams(
                        dp(46),
                        dp(44)
                )
        );

        TextView down=
                textButton("↓");

        down.setTextSize(22);
        down.setAlpha(
                index==count-1
                        ? .3f
                        : 1f
        );

        down.setEnabled(
                index<count-1
        );

        down.setOnClickListener(v ->
                moveToolbarItem(
                        id,
                        1
                ));

        LinearLayout.LayoutParams downParams=
                new LinearLayout.LayoutParams(
                        dp(46),
                        dp(44)
                );

        downParams.setMargins(
                dp(5),
                0,
                0,
                0
        );

        row.addView(
                down,
                downParams
        );

        page.addView(
                row,
                cardParams()
        );
    }


    private String toolbarIcon(
            String id
    ) {
        if(id.equals("emoji"))
            return "☺";

        if(id.equals("clipboard"))
            return "▣";

        if(id.equals("actions"))
            return "✎";

        if(id.equals("voice"))
            return "🎙";

        if(id.equals("theme"))
            return "◐";

        if(id.equals("width"))
            return "⛶";

        if(id.equals("hand"))
            return "◀";

        return "•";
    }


    private String toolbarTitle(
            String id
    ) {
        if(id.equals("emoji"))
            return "Emoji & kaomoji";

        if(id.equals("clipboard"))
            return "Clipboard";

        if(id.equals("actions"))
            return "Quick actions";

        if(id.equals("voice"))
            return "Voice typing";

        if(id.equals("theme"))
            return "Theme";

        if(id.equals("width"))
            return "Width";

        if(id.equals("hand"))
            return "One-handed";

        return id;
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
                "Key press sound",
                "Play a soft click when a key is pressed",
                "key_sound",
                false);

        addChoiceRow(
                page,
                "Key sound volume",
                keySoundVolumeName(),
                new String[]{"Low", "Medium", "High", "Full"},
                new int[]{25, 50, 75, 100},
                "key_sound_volume",
                50,
                this::showPreferences
        );

        addSwitchRow(page,
                "Wide keyboard by default",
                "Open KeyKii in wide mode",
                "wide_default",
                false);

        addChoiceRow(
                page,
                "One-handed mode",
                oneHandedName(),
                new String[]{"Off", "Left", "Right"},
                new int[]{0, 1, 2},
                "one_handed_default",
                0,
                this::showPreferences
        );

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
                    .setMessage("Theme is kept. Key size, gap, haptics, sound, number row, wide mode and one-handed mode will return to defaults.")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Reset", (d, which) -> {
                        prefs.edit()
                                .putInt("key_height", 46)
                                .putInt("float_gap", 96)
                                .putBoolean("haptic", false)
                                .putBoolean("key_sound", false)
                                .putInt("key_sound_volume", 50)
                                .putBoolean("wide_default", false)
                                .putInt("one_handed_default", 0)
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
        initThemeDraftFromPrefs();
        themeSelectableTiles.clear();

        boolean previewPending =
                pendingThemeImageUri != null &&
                !pendingThemeImageUri.isEmpty();

        if(previewPending) {
            themeDraftSurfaceMode = 3;
            themeDraftTheme = 0;
            themeDraftImageUri = pendingThemeImageUri;
            themeDraftKeyBorders = false;
        }

        LinearLayout page = page(
                "Themes",
                "Tap a design to preview it before applying",
                true
        );

        // Leave room for the fixed preview sheet, like Gboard.
        page.setPadding(
                dp(18),
                dp(18),
                dp(18),
                dp(285)
        );

        addThemeSection(page, "Style packs");

        addInfoCard(
                page,
                "Free + Pro preview",
                "All packs are unlocked in this test build. Packs marked PRO PREVIEW are the designs we can later lock behind KeyKii Pro with Google Play Billing."
        );

        addKeyboardStylePackGrid(page);

        addThemeSection(page, "My themes");
        addMyThemeTiles(page);

        addThemeSection(page, "Default");
        addDefaultThemeGallery(page);

        addThemeSection(page, "Colours");

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

        addThemeSection(page, "Light gradient");

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

        addThemeSection(page, "Dark gradient");

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

        addThemeSection(page, "More options");

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
                this::updateThemePreview
        );

        addChoiceRow(
                page,
                "Keyboard transparency",
                themeTransparencyName(),
                new String[]{"More transparent","Transparent","Balanced","Solid"},
                new int[]{55,70,85,100},
                "theme_transparency",
                100,
                this::updateThemePreview
        );

        addChoiceRow(
                page,
                "Key corner roundness",
                keyCornerName(),
                new String[]{"Small","Medium","Default","Very round"},
                new int[]{6,11,15,22},
                "key_corner_radius",
                15,
                this::updateThemePreview
        );

        FrameLayout root =
                new FrameLayout(this);

        root.setBackgroundColor(BG);

        ScrollView scroll = wrap(page);
        themeScrollView = scroll;

        root.addView(
                scroll,
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );

        themePreviewSheet =
                buildThemePreviewSheet();

        themePreviewSheet.setVisibility(
                previewPending
                        ? View.VISIBLE
                        : View.GONE
        );

        FrameLayout.LayoutParams sheetParams =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.BOTTOM
                );

        sheetParams.setMargins(
                dp(10),
                dp(10),
                dp(10),
                dp(18)
        );

        root.addView(
                themePreviewSheet,
                sheetParams
        );

        setContentView(root);

        updateThemePreview();
        refreshThemeTileSelection();
    }


    private String keyboardFontName() {
        int value=prefs.getInt("keyboard_font_style",0);

        switch(value) {
            case 1: return "Rounded";
            case 2: return "Serif";
            case 3: return "Mono";
            case 4: return "Condensed";
            case 5: return "Casual";
            case 6: return "Medium";
            case 100: return "Fredoka";
            case 101: return "DynaPuff";
            case 102: return "Rubik Bubbles";
            case 103: return "Patrick Hand";
            case 104: return "Lobster";
            case 105: return "Bungee";
            case 106: return "Press Start 2P";
            case 107: return "Cinzel Decorative";
            default: return "System";
        }
    }


    private Typeface settingsKeyboardTypeface(int style) {
        try {
            switch(style) {
                case 1:
                    return Typeface.create("sans-serif-rounded",Typeface.NORMAL);
                case 2:
                    return Typeface.create("serif",Typeface.NORMAL);
                case 3:
                    return Typeface.create("monospace",Typeface.NORMAL);
                case 4:
                    return Typeface.create("sans-serif-condensed",Typeface.NORMAL);
                case 5:
                    return Typeface.create("cursive",Typeface.NORMAL);
                case 6:
                    return Typeface.create("sans-serif-medium",Typeface.NORMAL);
                case 100:
                    return Typeface.createFromAsset(getAssets(),"fonts/fredoka.ttf");
                case 101:
                    return Typeface.createFromAsset(getAssets(),"fonts/dynapuff.ttf");
                case 102:
                    return Typeface.createFromAsset(getAssets(),"fonts/rubik_bubbles.ttf");
                case 103:
                    return Typeface.createFromAsset(getAssets(),"fonts/patrick_hand.ttf");
                case 104:
                    return Typeface.createFromAsset(getAssets(),"fonts/lobster.ttf");
                case 105:
                    return Typeface.createFromAsset(getAssets(),"fonts/bungee.ttf");
                case 106:
                    return Typeface.createFromAsset(getAssets(),"fonts/press_start_2p.ttf");
                case 107:
                    return Typeface.createFromAsset(getAssets(),"fonts/cinzel_decorative.ttf");
                default:
                    return Typeface.create("sans-serif",Typeface.NORMAL);
            }
        } catch(Exception ignored) {
            return Typeface.DEFAULT;
        }
    }


    private void showFonts() {
        screen="fonts";

        LinearLayout page=page(
                "Fonts",
                "Preview keyboard fonts before applying",
                true
        );

        addInfoCard(
                page,
                "Separate from Themes",
                "Themes never change your selected font. Fonts only change how the KeyKii keyboard letters and numbers look; the text you type stays normal."
        );

        addSection(page,"Free fonts");
        addFontStoreCard(page,0,"System","Clean Android keyboard lettering",false);
        addFontStoreCard(page,1,"Rounded","Soft and friendly system style",false);
        addFontStoreCard(page,2,"Serif","Classic system lettering",false);
        addFontStoreCard(page,3,"Mono","Simple fixed-width system style",false);

        addSection(page,"KeyKii Pro fonts");

        addInfoCard(
                page,
                "PRO PREVIEW",
                "Premium fonts are unlocked in this test build. Later these can be purchased with KeyKii Pro without changing the Font Store design."
        );

        addFontStoreCard(page,100,"Fredoka","Round, playful and bold",true);
        addFontStoreCard(page,101,"DynaPuff","Puffy hand-drawn display style",true);
        addFontStoreCard(page,102,"Rubik Bubbles","Bubble-outline lettering",true);
        addFontStoreCard(page,103,"Patrick Hand","Natural handwritten style",true);
        addFontStoreCard(page,104,"Lobster","Bold connected script",true);
        addFontStoreCard(page,105,"Bungee","Strong arcade display lettering",true);
        addFontStoreCard(page,106,"Press Start 2P","Retro pixel-game lettering",true);
        addFontStoreCard(page,107,"Cinzel Decorative","Elegant decorative lettering",true);

        addInfoCard(
                page,
                "Licensing",
                "These preview fonts are bundled from open-license font projects, and their license text is included in the app assets."
        );

        setContentView(wrap(page));
    }


    private void addFontStoreCard(
            LinearLayout page,
            int style,
            String name,
            String description,
            boolean pro
    ) {
        LinearLayout card=new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16),dp(13),dp(16),dp(13));

        GradientDrawable bg=round(CARD,20);
        bg.setStroke(dp(1),BORDER);
        card.setBackground(bg);
        card.setElevation(dp(1));

        TextView sample=new TextView(this);
        sample.setText("Aa Bb Cc  123");
        sample.setTextColor(TEXT);
        sample.setTextSize(style==106 ? 16 : 24);
        sample.setGravity(Gravity.CENTER);
        sample.setTypeface(settingsKeyboardTypeface(style));

        card.addView(
                sample,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(55)
                )
        );

        LinearLayout info=new LinearLayout(this);
        info.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout words=new LinearLayout(this);
        words.setOrientation(LinearLayout.VERTICAL);

        TextView title=new TextView(this);
        title.setText((pro ? "PRO PREVIEW • " : "FREE • ")+name);
        title.setTextColor(TEXT);
        title.setTextSize(16);

        TextView sub=new TextView(this);
        sub.setText(description);
        sub.setTextColor(MUTED);
        sub.setTextSize(11);

        words.addView(title);
        words.addView(sub);

        info.addView(
                words,
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                )
        );

        TextView arrow=new TextView(this);
        arrow.setText("›");
        arrow.setTextColor(MUTED);
        arrow.setTextSize(28);
        arrow.setGravity(Gravity.CENTER);

        info.addView(
                arrow,
                new LinearLayout.LayoutParams(dp(34),dp(42))
        );

        card.addView(info);

        card.setOnClickListener(v ->
                showFontPreview(style,name,pro)
        );

        LinearLayout.LayoutParams cp=cardParams();
        cp.setMargins(0,dp(6),0,dp(6));
        page.addView(card,cp);
    }


    private void showFontPreview(
            int style,
            String name,
            boolean pro
    ) {
        final android.app.Dialog dialog=
                new android.app.Dialog(this);

        LinearLayout sheet=new LinearLayout(this);
        sheet.setOrientation(LinearLayout.VERTICAL);
        sheet.setPadding(dp(18),dp(16),dp(18),dp(18));
        sheet.setBackground(round(Color.WHITE,26));

        TextView title=new TextView(this);
        title.setText((pro ? "PRO PREVIEW • " : "")+name);
        title.setTextColor(TEXT);
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        sheet.addView(title,new LinearLayout.LayoutParams(-1,dp(42)));

        TextView sample=new TextView(this);
        sample.setText("The quick brown fox  •  Aa Bb Cc 123");
        sample.setTextColor(TEXT);
        sample.setTextSize(style==106 ? 14 : 22);
        sample.setGravity(Gravity.CENTER);
        sample.setTypeface(settingsKeyboardTypeface(style));
        sheet.addView(sample,new LinearLayout.LayoutParams(-1,dp(62)));

        sheet.addView(
                buildFontKeyboardPreview(style),
                new LinearLayout.LayoutParams(-1,dp(210))
        );

        if(pro) {
            TextView note=new TextView(this);
            note.setText("Unlocked for testing in 2.40.0 • Future KeyKii Pro font");
            note.setTextColor(MUTED);
            note.setTextSize(11);
            note.setGravity(Gravity.CENTER);
            sheet.addView(note,new LinearLayout.LayoutParams(-1,dp(34)));
        }

        LinearLayout actions=new LinearLayout(this);
        actions.setGravity(Gravity.CENTER);

        TextView cancel=textButton("Cancel");
        TextView apply=textButton(pro ? "Apply for testing" : "Apply");
        cancel.setTextSize(16);
        apply.setTextSize(16);

        cancel.setOnClickListener(v -> dialog.dismiss());

        apply.setOnClickListener(v -> {
            prefs.edit()
                    .putInt("keyboard_font_style",style)
                    .apply();

            toast(name+" font applied");
            dialog.dismiss();
            showFonts();
        });

        LinearLayout.LayoutParams bp=
                new LinearLayout.LayoutParams(0,dp(56),1f);
        bp.setMargins(dp(4),dp(8),dp(4),0);

        actions.addView(cancel,bp);
        actions.addView(apply,bp);
        sheet.addView(actions);

        dialog.setContentView(sheet);
        dialog.setCancelable(true);

        dialog.setOnShowListener(d -> {
            android.view.Window w=dialog.getWindow();
            if(w!=null) {
                w.setBackgroundDrawable(
                        new android.graphics.drawable.ColorDrawable(
                                Color.TRANSPARENT
                        )
                );
                w.setDimAmount(.35f);
                w.addFlags(
                        android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND
                );
                w.setGravity(Gravity.BOTTOM);
                w.setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
            }
        });

        dialog.show();
    }


    private View buildFontKeyboardPreview(int style) {
        LinearLayout keyboard=new LinearLayout(this);
        keyboard.setOrientation(LinearLayout.VERTICAL);
        keyboard.setPadding(dp(9),dp(9),dp(9),dp(9));
        keyboard.setBackground(round(Color.rgb(39,45,55),20));

        Typeface typeface=settingsKeyboardTypeface(style);

        addFontPreviewRow(
                keyboard,
                new String[]{"q","w","e","r","t","y","u","i","o","p"},
                typeface
        );
        addFontPreviewRow(
                keyboard,
                new String[]{"a","s","d","f","g","h","j","k","l"},
                typeface
        );
        addFontPreviewRow(
                keyboard,
                new String[]{"⇧","z","x","c","v","b","n","m","⌫"},
                typeface
        );
        addFontPreviewRow(
                keyboard,
                new String[]{"?123",",","KeyKii",".","↵"},
                typeface
        );

        return keyboard;
    }


    private void addFontPreviewRow(
            LinearLayout parent,
            String[] values,
            Typeface typeface
    ) {
        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.CENTER);

        for(String value:values) {
            TextView key=new TextView(this);
            key.setText(value);
            key.setTextColor(Color.WHITE);
            key.setTextSize(value.length()>2 ? 9 : 13);
            key.setGravity(Gravity.CENTER);
            key.setTypeface(typeface);
            key.setBackground(round(Color.rgb(104,111,121),13));

            LinearLayout.LayoutParams p=
                    new LinearLayout.LayoutParams(
                            0,
                            dp(37),
                            value.equals("KeyKii") ? 2.6f : 1f
                    );
            p.setMargins(dp(2),dp(2),dp(2),dp(2));
            row.addView(key,p);
        }

        parent.addView(
                row,
                new LinearLayout.LayoutParams(-1,dp(41))
        );
    }


    private void addKeyboardStylePackGrid(LinearLayout page) {
        for(int pack=0;pack<12;pack+=2) {
            LinearLayout row=new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);

            addStylePackTileToRow(row,createStylePackTile(pack));

            if(pack+1<12) {
                addStylePackTileToRow(
                        row,
                        createStylePackTile(pack+1)
                );
            } else {
                addStylePackSpacer(row);
            }

            page.addView(row);
        }
    }


    private void addStylePackTileToRow(
            LinearLayout row,
            View tile
    ) {
        LinearLayout.LayoutParams p=
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                );
        p.setMargins(dp(4),dp(5),dp(4),dp(5));
        row.addView(tile,p);
    }


    private void addStylePackSpacer(LinearLayout row) {
        View spacer=new View(this);
        LinearLayout.LayoutParams p=
                new LinearLayout.LayoutParams(0,1,1f);
        p.setMargins(dp(4),0,dp(4),0);
        row.addView(spacer,p);
    }


    private LinearLayout createStylePackTile(int pack) {
        LinearLayout tile=new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setPadding(dp(7),dp(7),dp(7),dp(9));

        GradientDrawable cardBg=round(Color.WHITE,18);
        cardBg.setStroke(dp(1),BORDER);
        tile.setBackground(cardBg);

        tile.addView(
                buildStylePackMiniKeyboard(pack,false),
                new LinearLayout.LayoutParams(-1,dp(112))
        );

        TextView name=new TextView(this);
        name.setText(stylePackName(pack));
        name.setTextColor(TEXT);
        name.setTextSize(13);
        name.setGravity(Gravity.CENTER);
        name.setMaxLines(1);
        tile.addView(name,new LinearLayout.LayoutParams(-1,dp(25)));

        TextView badge=new TextView(this);
        badge.setText(stylePackPro(pack) ? "PRO PREVIEW" : "FREE");
        badge.setTextColor(
                stylePackPro(pack)
                        ? Color.rgb(145,83,173)
                        : Color.rgb(58,131,82)
        );
        badge.setTextSize(10);
        badge.setGravity(Gravity.CENTER);
        tile.addView(badge,new LinearLayout.LayoutParams(-1,dp(20)));

        tile.setOnClickListener(v -> showStylePackPreview(pack));
        return tile;
    }


    private LinearLayout buildStylePackMiniKeyboard(
            int pack,
            boolean large
    ) {
        int[] spec=stylePackSpec(pack);

        int start=spec[0];
        int end=spec[1];
        int accent=spec[2];
        int corner=spec[3];
        boolean borders=spec[5]==1;
        boolean dark=spec[6]==0;

        LinearLayout keyboard=new LinearLayout(this);
        keyboard.setOrientation(LinearLayout.VERTICAL);
        keyboard.setPadding(
                dp(large ? 9 : 6),
                dp(large ? 9 : 6),
                dp(large ? 9 : 6),
                dp(large ? 9 : 6)
        );

        GradientDrawable panelBg=
                new GradientDrawable(
                        GradientDrawable.Orientation.TL_BR,
                        new int[]{start,end}
                );
        panelBg.setCornerRadius(dp(large ? 22 : 15));
        keyboard.setBackground(panelBg);

        // Themes deliberately use the currently selected font.
        Typeface currentFont=settingsKeyboardTypeface(
                prefs.getInt("keyboard_font_style",0)
        );

        String[][] rows=new String[][]{
                {"q","w","e","r","t","y"},
                {"a","s","d","f","g","h"},
                {"⇧","z","x","c","v","⌫"},
                {"123",",","KeyKii",".","↵"}
        };

        for(int r=0;r<rows.length;r++) {
            LinearLayout row=new LinearLayout(this);
            row.setGravity(Gravity.CENTER);

            for(int k=0;k<rows[r].length;k++) {
                String value=rows[r][k];

                TextView key=new TextView(this);
                key.setText(value);
                key.setGravity(Gravity.CENTER);
                key.setTextSize(
                        large
                                ? (value.length()>2 ? 10 : 14)
                                : (value.length()>2 ? 7 : 10)
                );
                key.setTypeface(currentFont);

                boolean special=
                        r==2 && (k==0 || k==rows[r].length-1);

                key.setTextColor(
                        dark
                                ? Color.WHITE
                                : Color.rgb(53,53,57)
                );

                int keyFill=
                        special
                                ? Color.argb(
                                        large ? 220 : 205,
                                        Color.red(accent),
                                        Color.green(accent),
                                        Color.blue(accent)
                                )
                                : dark
                                        ? Color.argb(
                                                borders ? 145 : 72,
                                                255,255,255
                                        )
                                        : Color.argb(
                                                borders ? 214 : 116,
                                                255,255,255
                                        );

                GradientDrawable keyBg=round(
                        keyFill,
                        Math.max(5,large ? corner : corner-3)
                );

                if(borders) {
                    keyBg.setStroke(
                            dp(1),
                            dark
                                    ? Color.argb(65,255,255,255)
                                    : Color.argb(55,70,70,75)
                    );
                }

                key.setBackground(keyBg);

                LinearLayout.LayoutParams kp=
                        new LinearLayout.LayoutParams(
                                0,
                                dp(large ? 41 : 24),
                                value.equals("KeyKii") ? 2.5f : 1f
                        );
                kp.setMargins(
                        dp(large ? 2 : 1),
                        dp(large ? 2 : 1),
                        dp(large ? 2 : 1),
                        dp(large ? 2 : 1)
                );

                row.addView(key,kp);
            }

            keyboard.addView(
                    row,
                    new LinearLayout.LayoutParams(
                            -1,
                            dp(large ? 45 : 27)
                    )
            );
        }

        return keyboard;
    }


    private void showStylePackPreview(int pack) {
        final android.app.Dialog dialog=
                new android.app.Dialog(this);

        LinearLayout sheet=new LinearLayout(this);
        sheet.setOrientation(LinearLayout.VERTICAL);
        sheet.setPadding(dp(16),dp(15),dp(16),dp(18));
        sheet.setBackground(round(Color.WHITE,26));

        TextView title=new TextView(this);
        title.setText(stylePackName(pack));
        title.setTextColor(TEXT);
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        sheet.addView(title,new LinearLayout.LayoutParams(-1,dp(40)));

        TextView badge=new TextView(this);
        badge.setText(stylePackPro(pack) ? "PRO PREVIEW" : "FREE");
        badge.setTextColor(
                stylePackPro(pack)
                        ? Color.rgb(145,83,173)
                        : Color.rgb(58,131,82)
        );
        badge.setTextSize(11);
        badge.setGravity(Gravity.CENTER);
        sheet.addView(badge,new LinearLayout.LayoutParams(-1,dp(26)));

        sheet.addView(
                buildStylePackMiniKeyboard(pack,true),
                new LinearLayout.LayoutParams(-1,dp(208))
        );

        TextView sub=new TextView(this);
        sub.setText(stylePackSubtitle(pack)+"\nUses your current font");
        sub.setTextColor(MUTED);
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);
        sheet.addView(sub,new LinearLayout.LayoutParams(-1,dp(50)));

        LinearLayout buttons=new LinearLayout(this);

        TextView cancel=textButton("Cancel");
        TextView apply=textButton(
                stylePackPro(pack)
                        ? "Apply preview"
                        : "Apply"
        );
        cancel.setTextSize(16);
        apply.setTextSize(16);

        cancel.setOnClickListener(v -> dialog.dismiss());
        apply.setOnClickListener(v -> {
            applyKeyboardStylePack(pack);
            dialog.dismiss();
        });

        LinearLayout.LayoutParams p=
                new LinearLayout.LayoutParams(0,dp(56),1f);
        p.setMargins(dp(4),dp(8),dp(4),0);

        buttons.addView(cancel,p);
        buttons.addView(apply,p);
        sheet.addView(buttons);

        dialog.setContentView(sheet);
        dialog.setCancelable(true);

        dialog.setOnShowListener(d -> {
            android.view.Window w=dialog.getWindow();
            if(w!=null) {
                w.setBackgroundDrawable(
                        new android.graphics.drawable.ColorDrawable(
                                Color.TRANSPARENT
                        )
                );
                w.setDimAmount(.35f);
                w.addFlags(
                        android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND
                );
                w.setGravity(Gravity.BOTTOM);
                w.setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
            }
        });

        dialog.show();
    }


    private void applyKeyboardStylePack(int pack) {
        int[] spec=stylePackSpec(pack);

        prefs.edit()
                .putInt("theme_surface_mode",2)
                .putInt("theme",spec[6])
                .putInt("theme_custom_start",spec[0])
                .putInt("theme_custom_end",spec[1])
                .putInt("accent_color",spec[2])
                .putInt("key_corner_radius",spec[3])
                .putInt("theme_transparency",spec[4])
                .putBoolean("theme_key_borders",spec[5]==1)
                .putBoolean("photo_key_borders",spec[5]==1)
                .putBoolean("theme_auto_day_night",false)
                .putInt("keykii_style_pack",pack)
                // Themes must never overwrite keyboard_font_style.
                .apply();

        toast(stylePackName(pack)+" theme applied");
        showTheme();
    }


    private int[] stylePackSpec(int pack) {
        switch(pack) {
            case 0:
                return new int[]{
                        Color.rgb(217,238,255),
                        Color.rgb(249,252,255),
                        Color.rgb(81,128,204),
                        18,96,1,1
                };
            case 1:
                return new int[]{
                        Color.rgb(218,241,204),
                        Color.rgb(247,246,218),
                        Color.rgb(78,144,78),
                        22,96,1,1
                };
            case 2:
                return new int[]{
                        Color.rgb(33,48,74),
                        Color.rgb(10,17,29),
                        Color.rgb(98,149,229),
                        15,100,1,0
                };
            case 3:
                return new int[]{
                        Color.rgb(255,197,224),
                        Color.rgb(255,240,249),
                        Color.rgb(222,92,147),
                        24,96,1,1
                };
            case 4:
                return new int[]{
                        Color.rgb(60,16,92),
                        Color.rgb(3,30,50),
                        Color.rgb(44,223,235),
                        13,96,1,0
                };
            case 5:
                return new int[]{
                        Color.rgb(239,188,185),
                        Color.rgb(255,239,219),
                        Color.rgb(173,88,98),
                        19,98,1,1
                };
            case 6:
                return new int[]{
                        Color.rgb(172,235,241),
                        Color.rgb(207,225,255),
                        Color.rgb(32,146,170),
                        26,80,1,1
                };
            case 7:
                return new int[]{
                        Color.rgb(255,195,115),
                        Color.rgb(246,105,127),
                        Color.rgb(211,72,50),
                        20,95,1,1
                };
            case 8:
                return new int[]{
                        Color.rgb(220,201,255),
                        Color.rgb(252,229,249),
                        Color.rgb(132,92,194),
                        26,95,1,1
                };
            case 9:
                return new int[]{
                        Color.rgb(0,0,0),
                        Color.rgb(0,0,0),
                        Color.rgb(115,115,124),
                        11,100,0,0
                };
            case 10:
                return new int[]{
                        Color.rgb(24,24,27),
                        Color.rgb(2,2,3),
                        Color.rgb(226,44,55),
                        7,100,1,0
                };
            default:
                return new int[]{
                        Color.rgb(132,88,66),
                        Color.rgb(57,36,28),
                        Color.rgb(222,170,121),
                        17,98,1,0
                };
        }
    }


    private String stylePackName(int pack) {
        String[] names=new String[]{
                "Cloud Blue",
                "Matcha Cream",
                "Midnight",
                "Sakura Pink",
                "Neon Night",
                "Rose Gold",
                "Ocean Glass",
                "Sunset Pop",
                "Lavender Dream",
                "AMOLED Black",
                "Gaming Red",
                "Cocoa"
        };

        return names[Math.max(0,Math.min(names.length-1,pack))];
    }


    private String stylePackSubtitle(int pack) {
        String[] values=new String[]{
                "Clean blue and white",
                "Soft green and cream",
                "Deep navy night",
                "Cute pink pastel",
                "Purple and cyan glow",
                "Warm elegant rose",
                "Fresh aqua glass",
                "Bright coral sunset",
                "Soft purple pastel",
                "Pure black minimal",
                "Black and red gaming",
                "Warm coffee brown"
        };

        return values[Math.max(0,Math.min(values.length-1,pack))];
    }


    private boolean stylePackPro(int pack) {
        return pack>=3;
    }


    private void showSmartTyping() {
        screen = "smart_typing";

        LinearLayout page = page(
                "Smart typing",
                "Simple typing helpers that work directly on the keyboard",
                true
        );

        addSwitchRow(
                page,
                "Auto-capitalization",
                "Start sentences with a capital letter",
                "auto_capitalization",
                true
        );

        addSwitchRow(
                page,
                "Double-space period",
                "Press space twice to insert a period and a space",
                "double_space_period",
                true
        );

        addSwitchRow(
                page,
                "Key preview",
                "Show a small popup above a key while typing",
                "key_preview",
                true
        );

        addSwitchRow(
                page,
                "Swipe Backspace to delete word",
                "Swipe left on ⌫ to delete the previous word",
                "swipe_delete_word",
                true
        );

        addSwitchRow(
                page,
                "Quick punctuation",
                "Hold the period key to choose common punctuation",
                "quick_punctuation",
                true
        );

        addSwitchRow(
                page,
                "Word suggestions",
                "Show three offline word predictions above the English keyboard",
                "word_suggestions",
                true
        );

        addInfoCard(
                page,
                "Private suggestions",
                "KeyKii 2.38 uses its local English dictionary for suggestions. Typed text is not uploaded, and suggestions are hidden in password fields."
        );

        setContentView(
                wrap(page)
        );
    }


    private void showGlideTyping() {
        screen = "glide_typing";

        LinearLayout page = page(
                "Glide typing",
                "Swipe across letters to enter a word",
                true
        );

        addSection(
                page,
                "Typing"
        );

        addSwitchRow(
                page,
                "Enable glide typing",
                "Swipe from letter to letter instead of tapping each key",
                "glide_typing",
                false
        );

        addSwitchRow(
                page,
                "Show glide trail",
                "Show the letters KeyKii sees while your finger is moving",
                "glide_trail",
                true
        );

        addInfoCard(
                page,
                "Performance",
                "Glide recognition only runs after a swipe. Normal tap typing does not run the glide decoder in the background."
        );

        addInfoCard(
                page,
                "Language",
                "Glide typing currently uses KeyKii's local English dictionary and works offline."
        );

        setContentView(
                wrap(page)
        );
    }


    private void showVoice() {
        screen = "voice";

        LinearLayout page = page(
                "Voice typing",
                "Speak and insert text directly from the KeyKii microphone",
                true
        );

        boolean allowed =
                android.os.Build.VERSION.SDK_INT<23 ||
                checkSelfPermission(
                        android.Manifest.permission.RECORD_AUDIO
                )==
                android.content.pm.PackageManager.PERMISSION_GRANTED;

        addSection(
                page,
                "Voice typing"
        );

        addSwitchRow(
                page,
                "Voice typing",
                "Show the microphone and allow speech input",
                "voice_typing_enabled",
                true
        );

        addSection(
                page,
                "Microphone"
        );

        addInfoCard(
                page,
                "Microphone access",
                allowed
                        ? "Allowed. Tap the 🎙 microphone on the KeyKii toolbar to start voice typing."
                        : "KeyKii needs microphone access before the toolbar microphone can listen."
        );

        if(!allowed) {
            addActionButton(
                    page,
                    "Allow microphone",
                    v -> requestPermissions(
                            new String[]{
                                    android.Manifest.permission.RECORD_AUDIO
                            },
                            REQUEST_RECORD_AUDIO
                    )
            );
        }

        addInfoCard(
                page,
                "How it works",
                "Voice typing uses Android's installed speech-recognition service. Recognition only starts when you tap the microphone."
        );

        addActionButton(
                page,
                "Open Android voice input settings",
                v -> {
                    try {
                        startActivity(
                                new Intent(
                                        "android.settings.VOICE_INPUT_SETTINGS"
                                )
                        );
                    } catch (Exception e) {
                        toast(
                                "Voice input settings are unavailable on this device."
                        );
                    }
                }
        );

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
        LinearLayout page = page(
                "Emoji & kaomoji",
                "Manage favorites and recent emoji",
                true
        );

        String recent = getSharedPreferences("keykii_emoji", MODE_PRIVATE)
                .getString("recent", "");
        int count = recent.trim().isEmpty() ? 0 : recent.trim().split(" ").length;

        String favoriteRaw = prefs.getString(
                "emoji_favorites_v1",
                ""
        );
        int favoriteCount =
                favoriteRaw == null || favoriteRaw.isEmpty()
                        ? 0
                        : favoriteRaw.split("~~K~~").length;

        addInfoCard(
                page,
                "Favorites",
                favoriteCount +
                        (favoriteCount == 1
                                ? " favorite emoji is"
                                : " favorite emojis are") +
                        " saved. In the emoji panel, hold an emoji without skin-tone choices to add or remove it."
        );

        addInfoCard(page,
                "Recent emoji",
                count + (count == 1 ? " recent emoji is" : " recent emojis are") + " stored locally.");

        addInfoCard(page,
                "Skin tones",
                "On supported people and hand emoji, long-press and drag across the floating choices to select a skin tone.");

        addInfoCard(page,
                "Kaomoji",
                "The full KeyKii kaomoji library remains available from the :-) tab.");

        addActionButton(page, "Clear favorites", v -> {
            prefs.edit()
                    .remove("emoji_favorites_v1")
                    .apply();

            toast("Emoji favorites cleared");
            showEmoji();
        });

        addActionButton(page, "Clear recent emoji", v -> {
            getSharedPreferences("keykii_emoji", MODE_PRIVATE)
                    .edit()
                    .remove("recent")
                    .apply();

            getSharedPreferences("keykii_fast_emoji", MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply();

            prefs.edit()
                    .remove("fast_recent_v2")
                    .apply();

            toast("Recent emoji cleared");
            showEmoji();
        });

        setContentView(wrap(page));
    }

    private void showPrivacy() {
        screen = "privacy";
        LinearLayout page = page("Privacy", "Local KeyKii data controls", true);

        addSection(
                page,
                "Private typing"
        );

        addSwitchRow(
                page,
                "Incognito mode",
                "Pause clipboard history and emoji recents while typing privately",
                "incognito_mode",
                false
        );

        addInfoCard(
                page,
                "When Incognito is on",
                "KeyKii stops adding new clipboard-history items and emoji recents. Existing pinned clips, favorites, text shortcuts and keyboard preferences stay saved."
        );

        addSection(
                page,
                "Stored on this device"
        );

        addInfoCard(page,
                "Keyboard preferences",
                "Theme, keyboard size, haptics and width preferences are stored in KeyKii's local app data.");

        addInfoCard(page,
                "Clipboard",
                "KeyKii's clipboard panel stores recent and pinned copied text locally so it can be pasted again. Incognito mode pauses new clipboard-history saves.");

        addInfoCard(page,
                "Text shortcuts",
                "Saved text shortcuts are stored only in KeyKii's local app data and are pasted only when you tap them.");

        addInfoCard(page,
                "Emoji recents",
                "Recently used emoji are stored locally to build the Recent Emoji section. Incognito mode pauses new emoji recents.");

        addInfoCard(page,
                "Network",
                "Normal typing, emoji, kaomoji, calculator and text case work without internet. Translator uses Google ML Kit on-device translation for supported languages; Cebuano uses an online fallback when you tap Translate. Grammar Fix sends only the text in its box to the LanguageTool grammar service when you tap Check grammar.");

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
        // Save the old page position before replacing its view.
        if (
                activeSettingsScrollView != null &&
                activeSettingsScrollScreen != null &&
                !activeSettingsScrollScreen.isEmpty()
        ) {
            settingsScrollPositions.put(
                    activeSettingsScrollScreen,
                    activeSettingsScrollView.getScrollY()
            );
        }

        final String scrollScreen =
                screen == null ? "" : screen;

        final int restoreY =
                settingsScrollPositions.containsKey(scrollScreen)
                        ? settingsScrollPositions.get(scrollScreen)
                        : 0;

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

        // Android 15 draws behind system bars; keep settings clear of them.
        scroll.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(
                    insets.getSystemWindowInsetLeft(),
                    insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(),
                    insets.getSystemWindowInsetBottom()
            );
            return insets;
        });

        scroll.addView(
                content,
                new ScrollView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        scroll.setOnScrollChangeListener(
                (view, scrollX, scrollY, oldScrollX, oldScrollY) ->
                        settingsScrollPositions.put(
                                scrollScreen,
                                scrollY
                        )
        );

        activeSettingsScrollView = scroll;
        activeSettingsScrollScreen = scrollScreen;

        if(restoreY>0) {
            /*
             * Important: restore BEFORE Android draws the replacement page.
             * The old post() version drew frame 1 at the top and then moved
             * to the saved position on frame 2, which looked like a jump.
             */
            scroll.setScrollY(restoreY);

            final android.view.ViewTreeObserver.OnPreDrawListener[] holder =
                    new android.view.ViewTreeObserver.OnPreDrawListener[1];

            holder[0] =
                    new android.view.ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            if(
                                    scroll.getViewTreeObserver()
                                            .isAlive()
                            ) {
                                scroll.getViewTreeObserver()
                                        .removeOnPreDrawListener(
                                            holder[0]
                                        );
                            }

                            scroll.scrollTo(
                                    0,
                                    restoreY
                            );

                            // Cancel this draw. The next draw starts directly
                            // at the correct position, so there is no top flash.
                            return false;
                        }
                    };

            scroll.getViewTreeObserver()
                    .addOnPreDrawListener(
                        holder[0]
                    );
        }

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
            b.setOnClickListener(v -> {
                if ("photo_theme".equals(screen)) {
                    showTheme();
                } else {
                    showHome();
                }
            });
            top.addView(b, new LinearLayout.LayoutParams(dp(52), dp(52)));
        }

        if (!back && "home".equals(screen)) {
            ImageView logo = new ImageView(this);
            logo.setImageResource(R.drawable.keykii_official_color_icon);
            logo.setScaleType(ImageView.ScaleType.FIT_CENTER);

            LinearLayout.LayoutParams logoParams =
                    new LinearLayout.LayoutParams(
                            dp(64),
                            dp(64)
                    );

            logoParams.setMargins(
                    0,
                    0,
                    dp(12),
                    0
            );

            top.addView(
                    logo,
                    logoParams
            );
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

    private void addThemeSection(
            LinearLayout page,
            String text
    ) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextColor(MUTED);
        t.setTextSize(14);
        t.setPadding(
                dp(5),
                dp(18),
                dp(5),
                dp(8)
        );
        page.addView(t);
    }


    private void initThemeDraftFromPrefs() {
        themeDraftStylePack=-1;

        themeDraftSurfaceMode =
                prefs.getInt(
                        "theme_surface_mode",
                        0
                );

        themeDraftTheme =
                prefs.getInt(
                        "theme",
                        1
                );

        themeDraftStart =
                prefs.getInt(
                        "theme_custom_start",
                        Color.rgb(93,118,171)
                );

        themeDraftEnd =
                prefs.getInt(
                        "theme_custom_end",
                        themeDraftStart
                );

        themeDraftImageUri =
                prefs.getString(
                        "theme_image_uri",
                        ""
                );

        if(
                themeDraftSurfaceMode==3 &&
                (
                    themeDraftImageUri==null ||
                    themeDraftImageUri.isEmpty()
                )
        ) {
            themeDraftSurfaceMode=0;
        }

        if(prefs.contains("theme_key_borders")) {
            themeDraftKeyBorders =
                    prefs.getBoolean(
                            "theme_key_borders",
                            true
                    );
        } else if(themeDraftSurfaceMode==3) {
            themeDraftKeyBorders =
                    prefs.getBoolean(
                            "photo_key_borders",
                            false
                    );
        } else {
            themeDraftKeyBorders=true;
        }
    }


    private void addMyThemeTiles(
            LinearLayout page
    ) {
        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        LinearLayout addPhoto =
                createThemeTile(
                        "＋",
                        "Add photo"
                );

        addPhoto.setOnClickListener(v ->
                chooseThemeImage());

        addThemeTileToRow(
                row,
                addPhoto
        );

        LinearLayout custom =
                createThemeTile(
                        "🎨",
                        "Any color"
                );

        custom.setOnClickListener(v ->
                showCustomColorDialog());

        addThemeTileToRow(
                row,
                custom
        );

        String imageUri =
                prefs.getString(
                        "theme_image_uri",
                        ""
                );

        if(
                imageUri!=null &&
                !imageUri.isEmpty()
        ) {
            LinearLayout photo =
                    photoThemeThumbnail(
                            imageUri,
                            "Photo"
                    );

            registerThemeTile(
                    photo,
                    "3"
            );

            photo.setOnClickListener(v ->
                    selectPhotoThemeDraft(
                            imageUri
                    ));

            addThemeTileToRow(
                    row,
                    photo
            );

        } else {
            addThemeTileSpacer(row);
        }

        page.addView(row);
    }


    private LinearLayout photoThemeThumbnail(
            String uriText,
            String labelText
    ) {
        LinearLayout tile =
                new LinearLayout(this);

        tile.setOrientation(
                LinearLayout.VERTICAL
        );

        tile.setPadding(
                dp(3),
                dp(3),
                dp(3),
                dp(5)
        );

        ImageView preview =
                new ImageView(this);

        preview.setScaleType(
                ImageView.ScaleType.CENTER_CROP
        );

        try {
            preview.setImageURI(
                    Uri.parse(uriText)
            );
        } catch(Exception ignored) {
            preview.setBackgroundColor(
                    Color.rgb(60,60,64)
            );
        }

        GradientDrawable frame =
                new GradientDrawable();

        frame.setColor(
                Color.TRANSPARENT
        );

        frame.setCornerRadius(dp(15));
        preview.setBackground(frame);
        preview.setClipToOutline(true);

        tile.addView(
                preview,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(72)
                )
        );

        TextView label =
                new TextView(this);

        label.setText(labelText);
        label.setTextColor(TEXT);
        label.setTextSize(10);
        label.setGravity(Gravity.CENTER);

        tile.addView(
                label,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(22)
                )
        );

        return tile;
    }


    private void addDefaultThemeGallery(
            LinearLayout page
    ) {
        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        addDefaultThemeTile(
                row,
                "Dynamic",
                Color.rgb(247,245,242),
                1
        );

        addDefaultThemeTile(
                row,
                "Dark",
                Color.rgb(36,38,43),
                0
        );

        addDefaultThemeTile(
                row,
                "Clear",
                Color.rgb(58,61,67),
                2
        );

        page.addView(row);
    }


    private void addDefaultThemeTile(
            LinearLayout row,
            String name,
            int previewColor,
            int themeValue
    ) {
        LinearLayout tile =
                themeTile(
                        name,
                        new int[]{
                            previewColor
                        }
                );

        registerThemeTile(
                tile,
                "0:" + themeValue
        );

        tile.setOnClickListener(v ->
                selectBuiltInThemeDraft(
                        themeValue
                ));

        addThemeTileToRow(
                row,
                tile
        );
    }


    private void addColorThemeGrid(
            LinearLayout page,
            String[] names,
            int[] colors
    ) {
        for(int i=0;i<colors.length;i+=3) {
            LinearLayout row =
                    new LinearLayout(this);

            row.setOrientation(
                    LinearLayout.HORIZONTAL
            );

            for(int col=0;col<3;col++) {
                int index=i+col;

                if(index<colors.length) {
                    final int color =
                            colors[index];

                    final String name =
                            names[index];

                    LinearLayout tile =
                            themeTile(
                                    "",
                                    new int[]{
                                        color
                                    }
                            );

                    tile.setContentDescription(
                            name
                    );

                    registerThemeTile(
                            tile,
                            "1:" + color
                    );

                    tile.setOnClickListener(v ->
                            selectSolidThemeDraft(
                                    color
                            ));

                    addThemeTileToRow(
                            row,
                            tile
                    );

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
        int count =
                Math.min(
                        names.length,
                        pairs.length/2
                );

        for(int i=0;i<count;i+=3) {
            LinearLayout row =
                    new LinearLayout(this);

            row.setOrientation(
                    LinearLayout.HORIZONTAL
            );

            for(int col=0;col<3;col++) {
                int index=i+col;

                if(index<count) {
                    final int start =
                            pairs[index*2];

                    final int end =
                            pairs[index*2+1];

                    final String name =
                            names[index];

                    LinearLayout tile =
                            themeTile(
                                    "",
                                    new int[]{
                                        start,
                                        end
                                    }
                            );

                    tile.setContentDescription(
                            name
                    );

                    registerThemeTile(
                            tile,
                            "2:" + start + ":" + end
                    );

                    tile.setOnClickListener(v ->
                            selectGradientThemeDraft(
                                    start,
                                    end
                            ));

                    addThemeTileToRow(
                            row,
                            tile
                    );

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
        LinearLayout tile =
                new LinearLayout(this);

        tile.setOrientation(
                LinearLayout.VERTICAL
        );

        tile.setPadding(
                dp(3),
                dp(3),
                dp(3),
                dp(4)
        );

        TextView preview =
                new TextView(this);

        preview.setText("━━━━   ●");
        preview.setTextSize(14);
        preview.setGravity(
                Gravity.BOTTOM |
                Gravity.CENTER_HORIZONTAL
        );

        preview.setPadding(
                dp(5),
                dp(5),
                dp(5),
                dp(8)
        );

        int middle=colors[0];

        if(colors.length>1)
            middle=averageColor(
                    colors[0],
                    colors[colors.length-1]
            );

        preview.setTextColor(
                isLightThemeColor(middle)
                        ? Color.argb(
                            130,55,60,65
                        )
                        : Color.argb(
                            205,245,245,247
                        )
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

        bg.setCornerRadius(dp(15));
        bg.setStroke(dp(1),BORDER);

        preview.setBackground(bg);

        tile.addView(
                preview,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(72)
                )
        );

        TextView label =
                new TextView(this);

        label.setText(name);
        label.setTextColor(TEXT);
        label.setTextSize(10);
        label.setGravity(Gravity.CENTER);
        label.setSingleLine(true);

        tile.addView(
                label,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        name.isEmpty()
                                ? dp(8)
                                : dp(22)
                )
        );

        return tile;
    }


    private LinearLayout createThemeTile(
            String symbol,
            String labelText
    ) {
        LinearLayout tile =
                new LinearLayout(this);

        tile.setOrientation(
                LinearLayout.VERTICAL
        );

        tile.setPadding(
                dp(3),
                dp(3),
                dp(3),
                dp(5)
        );

        TextView preview =
                new TextView(this);

        preview.setText(symbol);
        preview.setTextSize(31);
        preview.setGravity(Gravity.CENTER);
        preview.setTextColor(
                Color.rgb(77,99,148)
        );

        GradientDrawable bg =
                new GradientDrawable();

        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(15));
        bg.setStroke(
                dp(2),
                Color.rgb(101,124,173)
        );

        preview.setBackground(bg);

        tile.addView(
                preview,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(72)
                )
        );

        TextView label =
                new TextView(this);

        label.setText(labelText);
        label.setTextColor(TEXT);
        label.setTextSize(10);
        label.setGravity(Gravity.CENTER);

        tile.addView(
                label,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(22)
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

        p.setMargins(
                dp(3),
                dp(3),
                dp(3),
                dp(4)
        );

        row.addView(tile,p);
    }


    private void addThemeTileSpacer(
            LinearLayout row
    ) {
        View spacer =
                new View(this);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        0,
                        dp(1),
                        1f
                );

        p.setMargins(
                dp(3),
                0,
                dp(3),
                0
        );

        row.addView(spacer,p);
    }


    private void registerThemeTile(
            LinearLayout tile,
            String tag
    ) {
        tile.setTag(tag);
        themeSelectableTiles.add(tile);
    }


    private String currentThemeDraftTag() {
        if(themeDraftSurfaceMode==0)
            return "0:" + themeDraftTheme;

        if(themeDraftSurfaceMode==1)
            return "1:" + themeDraftStart;

        if(themeDraftSurfaceMode==2)
            return "2:" +
                    themeDraftStart +
                    ":" +
                    themeDraftEnd;

        if(themeDraftSurfaceMode==3)
            return "3";

        return "";
    }


    private void refreshThemeTileSelection() {
        String selected =
                currentThemeDraftTag();

        for(LinearLayout tile:
                themeSelectableTiles) {
            String tag =
                    tile.getTag()==null
                            ? ""
                            : tile.getTag().toString();

            GradientDrawable frame =
                    new GradientDrawable();

            frame.setColor(
                    Color.TRANSPARENT
            );

            frame.setCornerRadius(
                    dp(17)
            );

            if(tag.equals(selected)) {
                frame.setStroke(
                        dp(3),
                        Color.rgb(
                            82,107,161
                        )
                );
            }

            tile.setBackground(frame);
        }
    }


    private void selectBuiltInThemeDraft(
            int themeValue
    ) {
        themeDraftStylePack=-1;

        themeDraftSurfaceMode=0;
        themeDraftTheme=themeValue;

        showThemePreviewSheet();
        refreshThemeTileSelection();
    }


    private void selectSolidThemeDraft(
            int color
    ) {
        themeDraftStylePack=-1;

        themeDraftSurfaceMode=1;
        themeDraftStart=color;
        themeDraftEnd=color;
        themeDraftTheme =
                isLightThemeColor(color)
                        ? 1
                        : 0;

        showThemePreviewSheet();
        refreshThemeTileSelection();
    }


    private void selectGradientThemeDraft(
            int start,
            int end
    ) {
        themeDraftStylePack=-1;

        themeDraftSurfaceMode=2;
        themeDraftStart=start;
        themeDraftEnd=end;

        themeDraftTheme =
                isLightThemeColor(
                        averageColor(
                                start,
                                end
                        )
                )
                        ? 1
                        : 0;

        showThemePreviewSheet();
        refreshThemeTileSelection();
    }


    private void selectPhotoThemeDraft(
            String uriText
    ) {
        themeDraftStylePack=-1;

        if(
                uriText==null ||
                uriText.isEmpty()
        ) {
            chooseThemeImage();
            return;
        }

        themeDraftSurfaceMode=3;
        themeDraftTheme=0;
        themeDraftImageUri=uriText;

        showThemePreviewSheet();
        refreshThemeTileSelection();
    }


    private LinearLayout buildThemePreviewSheet() {
        LinearLayout sheet =
                new LinearLayout(this);

        sheet.setOrientation(
                LinearLayout.VERTICAL
        );

        sheet.setPadding(
                dp(16),
                dp(14),
                dp(16),
                dp(12)
        );

        GradientDrawable bg =
                round(
                    Color.rgb(
                        252,250,246
                    ),
                    28
                );

        bg.setStroke(
                dp(1),
                BORDER
        );

        sheet.setBackground(bg);
        sheet.setElevation(dp(14));

        themePreviewFrame =
                new FrameLayout(this);

        GradientDrawable previewFrameBg =
                new GradientDrawable();

        previewFrameBg.setColor(
                Color.rgb(
                    35,37,42
                )
        );

        previewFrameBg.setCornerRadius(
                dp(20)
        );

        themePreviewFrame.setBackground(
                previewFrameBg
        );

        themePreviewFrame.setClipToOutline(
                true
        );

        sheet.addView(
                themePreviewFrame,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(165)
                )
        );

        LinearLayout borderRow =
                new LinearLayout(this);

        borderRow.setGravity(
                Gravity.CENTER_VERTICAL
        );

        borderRow.setPadding(
                dp(4),
                dp(9),
                dp(4),
                dp(5)
        );

        TextView borderLabel =
                new TextView(this);

        borderLabel.setText(
                "Key borders"
        );

        borderLabel.setTextColor(TEXT);
        borderLabel.setTextSize(15);

        borderRow.addView(
                borderLabel,
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                )
        );

        themePreviewBorderSwitch =
                new Switch(this);

        themePreviewBorderSwitch.setChecked(
                themeDraftKeyBorders
        );

        themePreviewBorderSwitch
                .setOnCheckedChangeListener(
                    (buttonView,isChecked) -> {
                        themeDraftKeyBorders =
                                isChecked;

                        updateThemePreview();
                    }
                );

        borderRow.addView(
                themePreviewBorderSwitch
        );

        sheet.addView(borderRow);

        LinearLayout actions =
                new LinearLayout(this);

        actions.setGravity(
                Gravity.CENTER
        );

        TextView cancel =
                textButton("Cancel");

        cancel.setTextSize(14);
        cancel.setOnClickListener(v -> {
            pendingThemeImageUri="";
            initThemeDraftFromPrefs();

            if(themePreviewBorderSwitch!=null)
                themePreviewBorderSwitch
                        .setChecked(
                            themeDraftKeyBorders
                        );

            updateThemePreview();
            refreshThemeTileSelection();

            if(themePreviewSheet!=null)
                themePreviewSheet.setVisibility(
                        View.GONE
                );
        });

        TextView apply =
                textButton("Apply");

        apply.setTextSize(14);
        apply.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        apply.setBackground(
                round(
                    ACCENT,
                    18
                )
        );

        apply.setOnClickListener(v ->
                applyThemeDraft());

        LinearLayout.LayoutParams actionParams =
                new LinearLayout.LayoutParams(
                        0,
                        dp(46),
                        1f
                );

        actionParams.setMargins(
                dp(4),
                dp(3),
                dp(4),
                0
        );

        actions.addView(
                cancel,
                actionParams
        );

        actions.addView(
                apply,
                actionParams
        );

        sheet.addView(actions);

        return sheet;
    }


    private void showThemePreviewSheet() {
        if(themePreviewSheet!=null)
            themePreviewSheet.setVisibility(
                    View.VISIBLE
            );

        if(themePreviewBorderSwitch!=null) {
            themePreviewBorderSwitch
                    .setOnCheckedChangeListener(
                        null
                    );

            themePreviewBorderSwitch.setChecked(
                    themeDraftKeyBorders
            );

            themePreviewBorderSwitch
                    .setOnCheckedChangeListener(
                        (buttonView,isChecked) -> {
                            themeDraftKeyBorders =
                                    isChecked;

                            updateThemePreview();
                        }
                    );
        }

        updateThemePreview();
    }


    private void updateThemePreview() {
        if(themePreviewFrame==null)
            return;

        themePreviewFrame.removeAllViews();

        int previewBaseColor;

        if(themeDraftSurfaceMode==3) {
            previewBaseColor =
                    Color.rgb(
                        42,42,46
                    );

            if(
                    themeDraftImageUri!=null &&
                    !themeDraftImageUri.isEmpty()
            ) {
                ImageView image =
                        new ImageView(this);

                image.setScaleType(
                        ImageView.ScaleType.CENTER_CROP
                );

                try {
                    image.setImageURI(
                            Uri.parse(
                                themeDraftImageUri
                            )
                    );
                } catch(Exception ignored) {
                    image.setBackgroundColor(
                            previewBaseColor
                    );
                }

                themePreviewFrame.addView(
                        image,
                        new FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                        )
                );
            }

            View shade =
                    new View(this);

            shade.setBackgroundColor(
                    Color.argb(
                        34,
                        0,0,0
                    )
            );

            themePreviewFrame.addView(
                    shade,
                    new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                    )
            );

        } else {
            GradientDrawable previewBg =
                    new GradientDrawable();

            if(themeDraftSurfaceMode==2) {
                previewBg =
                        new GradientDrawable(
                            GradientDrawable.Orientation.TL_BR,
                            new int[]{
                                themeDraftStart,
                                themeDraftEnd
                            }
                        );

                previewBaseColor =
                        averageColor(
                                themeDraftStart,
                                themeDraftEnd
                        );

            } else if(themeDraftSurfaceMode==1) {
                previewBg.setColor(
                        themeDraftStart
                );

                previewBaseColor =
                        themeDraftStart;

            } else {
                previewBaseColor =
                        themeDraftTheme==1
                                ? Color.rgb(
                                    239,243,244
                                )
                                : themeDraftTheme==2
                                    ? Color.rgb(
                                        44,49,55
                                    )
                                    : Color.rgb(
                                        31,38,48
                                    );

                previewBg.setColor(
                        previewBaseColor
                );
            }

            previewBg.setCornerRadius(
                    dp(20)
            );

            themePreviewFrame.setBackground(
                    previewBg
            );
        }

        boolean light =
                themeDraftSurfaceMode!=3 &&
                isLightThemeColor(
                    previewBaseColor
                );

        int keyTextColor =
                light
                        ? Color.rgb(
                            42,45,48
                        )
                        : Color.WHITE;

        LinearLayout keyboard =
                new LinearLayout(this);

        keyboard.setOrientation(
                LinearLayout.VERTICAL
        );

        keyboard.setPadding(
                dp(8),
                dp(6),
                dp(8),
                dp(7)
        );

        themePreviewFrame.addView(
                keyboard,
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );

        TextView toolbar =
                new TextView(this);

        toolbar.setText(
                "▦        ☺        ▣        ✎        ◐        ↔"
        );

        toolbar.setTextColor(
                keyTextColor
        );

        toolbar.setTextSize(13);
        toolbar.setGravity(
                Gravity.CENTER
        );

        keyboard.addView(
                toolbar,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0,
                        .8f
                )
        );

        addThemePreviewRow(
                keyboard,
                new String[]{
                    "q","w","e","r","t",
                    "y","u","i","o","p"
                },
                null,
                keyTextColor
        );

        addThemePreviewRow(
                keyboard,
                new String[]{
                    "a","s","d","f","g",
                    "h","j","k","l"
                },
                null,
                keyTextColor
        );

        addThemePreviewRow(
                keyboard,
                new String[]{
                    "⇧","z","x","c","v",
                    "b","n","m","⌫"
                },
                new boolean[]{
                    true,false,false,false,false,
                    false,false,false,true
                },
                keyTextColor
        );

        LinearLayout bottom =
                new LinearLayout(this);

        bottom.setGravity(
                Gravity.CENTER
        );

        bottom.addView(
                themePreviewKey(
                    "?123",
                    1.15f,
                    true,
                    keyTextColor,
                    light
                )
        );

        bottom.addView(
                themePreviewKey(
                    ",",
                    .65f,
                    false,
                    keyTextColor,
                    light
                )
        );

        bottom.addView(
                themePreviewKey(
                    "KeyKii",
                    2.55f,
                    true,
                    keyTextColor,
                    light
                )
        );

        bottom.addView(
                themePreviewKey(
                    ".",
                    .65f,
                    false,
                    keyTextColor,
                    light
                )
        );

        bottom.addView(
                themePreviewKey(
                    "↵",
                    1f,
                    true,
                    keyTextColor,
                    light
                )
        );

        keyboard.addView(
                bottom,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0,
                        1f
                )
        );
    }


    private void addThemePreviewRow(
            LinearLayout parent,
            String[] labels,
            boolean[] special,
            int textColor
    ) {
        boolean light =
                textColor != Color.WHITE;

        LinearLayout row =
                new LinearLayout(this);

        row.setGravity(
                Gravity.CENTER
        );

        for(int i=0;i<labels.length;i++) {
            boolean isSpecial =
                    special!=null &&
                    i<special.length &&
                    special[i];

            row.addView(
                    themePreviewKey(
                        labels[i],
                        1f,
                        isSpecial,
                        textColor,
                        light
                    )
            );
        }

        parent.addView(
                row,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0,
                        1f
                )
        );
    }


    private TextView themePreviewKey(
            String label,
            float weight,
            boolean special,
            int textColor,
            boolean light
    ) {
        TextView key =
                new TextView(this);

        key.setText(label);
        key.setTextColor(textColor);
        key.setTextSize(
                label.length()>2
                        ? 10
                        : 14
        );

        key.setGravity(
                Gravity.CENTER
        );

        if(
                themeDraftKeyBorders ||
                special
        ) {
            GradientDrawable bg =
                    new GradientDrawable();

            int alpha =
                    themeDraftKeyBorders
                            ? 95
                            : 56;

            if(light) {
                bg.setColor(
                        Color.argb(
                            alpha,
                            255,255,255
                        )
                );
            } else {
                bg.setColor(
                        Color.argb(
                            alpha,
                            235,238,242
                        )
                );
            }

            bg.setCornerRadius(
                    dp(
                        prefs.getInt(
                            "key_corner_radius",
                            15
                        )
                    )
            );

            if(themeDraftKeyBorders) {
                bg.setStroke(
                        dp(1),
                        light
                                ? Color.argb(
                                    80,50,55,60
                                )
                                : Color.argb(
                                    80,255,255,255
                                )
                );
            }

            key.setBackground(bg);
        }

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        weight
                );

        p.setMargins(
                dp(2),
                dp(2),
                dp(2),
                dp(2)
        );

        key.setLayoutParams(p);

        return key;
    }


    private void applyThemeDraft() {
        if(
                themeDraftSurfaceMode==3 &&
                (
                    themeDraftImageUri==null ||
                    themeDraftImageUri.isEmpty()
                )
        ) {
            toast("Choose a photo first");
            return;
        }

        SharedPreferences.Editor e =
                prefs.edit();

        e.putInt(
                "theme_surface_mode",
                themeDraftSurfaceMode
        );

        e.putInt(
                "theme",
                themeDraftTheme
        );

        e.putBoolean(
                "theme_key_borders",
                themeDraftKeyBorders
        );

        // Keep backward compatibility with the 2.17.1 photo setting.
        e.putBoolean(
                "photo_key_borders",
                themeDraftKeyBorders
        );

        e.putBoolean(
                "theme_auto_day_night",
                false
        );

        if(themeDraftStylePack>=0) {
            int[] pack=
                    stylePackSpec(
                            themeDraftStylePack
                    );

            e.putInt(
                    "accent_color",
                    pack[2]
            );

            e.putInt(
                    "key_corner_radius",
                    pack[3]
            );

            e.putInt(
                    "theme_transparency",
                    pack[4]
            );

            e.putInt(
                    "keykii_style_pack",
                    themeDraftStylePack
            );

            // Theme packs never change keyboard_font_style.
        }

        if(
                themeDraftSurfaceMode==1 ||
                themeDraftSurfaceMode==2
        ) {
            e.putInt(
                    "theme_custom_start",
                    themeDraftStart
            );

            e.putInt(
                    "theme_custom_end",
                    themeDraftEnd
            );
        }

        if(themeDraftSurfaceMode==3) {
            e.putString(
                    "theme_image_uri",
                    themeDraftImageUri
            );
        }

        e.apply();

        pendingThemeImageUri="";

        toast("Theme applied");

        refreshThemeTileSelection();

        if(themePreviewSheet!=null)
            themePreviewSheet.setVisibility(
                    View.GONE
            );
    }


    private void chooseThemeImage() {
        if(themeScrollView!=null)
            themeScrollY=themeScrollView.getScrollY();

        try {
            Intent intent =
                    new Intent(
                        Intent.ACTION_OPEN_DOCUMENT
                    );

            intent.addCategory(
                    Intent.CATEGORY_OPENABLE
            );

            intent.setType("image/*");

            intent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION |
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            );

            startActivityForResult(
                    intent,
                    REQUEST_THEME_IMAGE
            );

        } catch(Exception e) {
            toast(
                    "Image picker is unavailable on this device."
            );
        }
    }


    private int averageColor(
            int a,
            int b
    ) {
        return Color.rgb(
                (Color.red(a)+Color.red(b))/2,
                (Color.green(a)+Color.green(b))/2,
                (Color.blue(a)+Color.blue(b))/2
        );
    }


    private boolean isLightThemeColor(
            int color
    ) {
        int brightness =
                (
                    Color.red(color)*299 +
                    Color.green(color)*587 +
                    Color.blue(color)*114
                ) / 1000;

        return brightness>=155;
    }


    private void showCustomColorDialog() {
        int current =
                themeDraftSurfaceMode==1
                        ? themeDraftStart
                        : prefs.getInt(
                            "theme_custom_start",
                            Color.rgb(
                                93,118,171
                            )
                        );

        final int[] chosen =
                new int[]{
                    current
                };

        final int[] initial =
                new int[]{
                    Color.red(current),
                    Color.green(current),
                    Color.blue(current)
                };

        final SeekBar[] bars =
                new SeekBar[3];

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setPadding(
                dp(22),
                dp(8),
                dp(22),
                0
        );

        TextView preview =
                new TextView(this);

        preview.setTextSize(16);
        preview.setGravity(Gravity.CENTER);

        box.addView(
                preview,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(82)
                )
        );

        final String[] labels =
                new String[]{
                    "Red",
                    "Green",
                    "Blue"
                };

        Runnable refreshPreview=() -> {
            int red =
                    bars[0]==null
                            ? initial[0]
                            : bars[0].getProgress();

            int green =
                    bars[1]==null
                            ? initial[1]
                            : bars[1].getProgress();

            int blue =
                    bars[2]==null
                            ? initial[2]
                            : bars[2].getProgress();

            chosen[0]=Color.rgb(
                    red,
                    green,
                    blue
            );

            GradientDrawable d =
                    new GradientDrawable();

            d.setColor(chosen[0]);
            d.setCornerRadius(dp(18));
            preview.setBackground(d);

            preview.setTextColor(
                    isLightThemeColor(
                        chosen[0]
                    )
                            ? Color.rgb(
                                40,40,42
                            )
                            : Color.WHITE
            );

            preview.setText(
                    String.format(
                            "#%02X%02X%02X",
                            red,
                            green,
                            blue
                    )
            );
        };

        for(int i=0;i<3;i++) {
            final int channel=i;

            TextView label =
                    new TextView(this);

            label.setText(
                    labels[channel] +
                    "  " +
                    initial[channel]
            );

            label.setTextColor(TEXT);
            label.setTextSize(13);

            label.setPadding(
                    0,
                    dp(10),
                    0,
                    0
            );

            box.addView(label);

            SeekBar bar =
                    new SeekBar(this);

            bar.setMax(255);
            bar.setProgress(
                    initial[channel]
            );

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
                                    labels[channel] +
                                    "  " +
                                    progress
                            );

                            refreshPreview.run();
                        }

                        @Override
                        public void onStartTrackingTouch(
                                SeekBar seekBar
                        ) {
                        }

                        @Override
                        public void onStopTrackingTouch(
                                SeekBar seekBar
                        ) {
                        }
                    }
            );

            box.addView(bar);
        }

        refreshPreview.run();

        new AlertDialog.Builder(this)
                .setTitle("Create any color")
                .setView(box)
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Preview",
                        (dialog,which) ->
                                selectSolidThemeDraft(
                                        chosen[0]
                                )
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

    private String oneHandedName() {
        int value=
                prefs.getInt(
                        "one_handed_default",
                        0
                );

        if(value==1)
            return "Left";

        if(value==2)
            return "Right";

        return "Off";
    }


    private String keySoundVolumeName() {
        int value=
                prefs.getInt(
                        "key_sound_volume",
                        50
                );

        if(value<=25)
            return "Low";

        if(value>=100)
            return "Full";

        if(value>=75)
            return "High";

        return "Medium";
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
