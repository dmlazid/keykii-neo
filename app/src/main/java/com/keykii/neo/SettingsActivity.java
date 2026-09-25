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

    private static final int BG = Color.rgb(252, 248, 253);
    private static final int CARD = Color.WHITE;
    private static final int TEXT = Color.rgb(55, 45, 62);
    private static final int MUTED = Color.rgb(126, 111, 133);
    private static final int BORDER = Color.rgb(236, 226, 241);
    private static final int ACCENT = Color.rgb(240, 225, 251);
    private static final int SOFT = Color.rgb(249, 241, 251);

    private static final int PASTEL_PINK = Color.rgb(255, 229, 241);
    private static final int PASTEL_PURPLE = Color.rgb(237, 227, 255);
    private static final int PASTEL_BLUE = Color.rgb(226, 240, 255);
    private static final int PASTEL_MINT = Color.rgb(225, 246, 238);
    private static final int PASTEL_PEACH = Color.rgb(255, 238, 220);

    private SharedPreferences prefs;
    private KeyKiiAds ads;
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

    private int remoteFontShownCount = 24;
    private int colorFontShownCount = 30;
    private String fontBrowseMode = "all";
    private String themeBrowseMode = "all";
    private int neoThemeShownCount = NeoThemeCatalog.PAGE_SIZE;

    private LinearLayout themeCategoryContent;
    private android.widget.HorizontalScrollView themeFilterScroll;
    private final java.util.ArrayList<TextView> themeFilterChips =
            new java.util.ArrayList<>();
    private int themeRenderGeneration = 0;

    private LinearLayout fontCategoryContent;
    private android.widget.HorizontalScrollView fontFilterScroll;
    private LinearLayout fontFilterRow;
    private final java.util.ArrayList<TextView> fontFilterChips =
            new java.util.ArrayList<>();
    private final java.util.concurrent.ExecutorService fontDownloadExecutor =
            java.util.concurrent.Executors.newSingleThreadExecutor();

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
        KeyKiiAccess.enforceExpiredSelections(this);
        ads=new KeyKiiAds(this,prefs);
        ads.start();
        themeBrowseMode="all";
        fontBrowseMode="all";
        prefs.edit()
                .remove("theme_browse_mode")
                .remove("font_browse_mode")
                .apply();

        String openScreen=
                getIntent().getStringExtra(
                        "open_screen"
                );

        if(
                openScreen==null &&
                prefs.getInt("app_intro_version_seen",0)<254
        ) {
            showIntro();
            return;
        }

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
            showTheme();
        }
    }

    private void hideSoftKeyboardNow() {
        try {
            View current=getCurrentFocus();
            if(current!=null) current.clearFocus();

            View decor=getWindow().getDecorView();
            InputMethodManager imm=
                    (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);

            if(imm!=null && decor!=null) {
                imm.hideSoftInputFromWindow(
                        decor.getWindowToken(),
                        0
                );
            }

            getWindow().setSoftInputMode(
                    android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN |
                    android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            );
        } catch(Exception ignored) {
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
        if("intro".equals(screen) || "intro_language".equals(screen)) {
            return;
        }
        if("app_language".equals(screen)) {
            showHome();
            return;
        }
        if("fonts".equals(screen) || "mine".equals(screen)) {
            showTheme();
            return;
        }
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
        hideSoftKeyboardNow();

        LinearLayout page=page(
                "Settings",
                "Manage KeyKii, your keyboard and the app",
                false
        );
        page.setPadding(dp(18),dp(18),dp(18),dp(120));

        addSettingsGroupTitle(page,"Tutorial");
        LinearLayout tutorial=settingsGroup();
        addSettingsActionRow(tutorial,"?","How to change keyboard",v -> showHelp());
        addSettingsDivider(tutorial);
        addSettingsActionRow(tutorial,"✨","How to get Custom Keyboard",v -> showTheme());
        page.addView(tutorial,settingsGroupParams());

        addSettingsGroupTitle(page,"Keyboard");
        LinearLayout keyboard=settingsGroup();
        addSettingsActionRow(keyboard,"⌨","Change Keyboard",v -> {
            InputMethodManager imm=(InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            if(imm!=null) imm.showInputMethodPicker();
        });
        addSettingsDivider(keyboard);
        addSettingsActionRow(keyboard,"文A","Keyboard Languages",v -> showLanguages());
        addSettingsDivider(keyboard);
        addSettingsActionRow(keyboard,"⚙","Preferences",v -> showPreferences());
        addSettingsDivider(keyboard);
        addSettingsActionRow(keyboard,"☰","Toolbar",v -> showToolbar());
        addSettingsDivider(keyboard);
        addSettingsActionRow(keyboard,"✨","Smart typing",v -> showSmartTyping());
        addSettingsDivider(keyboard);
        addSettingsActionRow(keyboard,"〰","Glide typing",v -> showGlideTyping());
        addSettingsDivider(keyboard);
        addSettingsActionRow(keyboard,"🎙","Voice typing",v -> showVoice());
        page.addView(keyboard,settingsGroupParams());

        addSettingsGroupTitle(page,"App");
        LinearLayout app=settingsGroup();
        addSettingsActionRow(app,"🌐","Language",v -> showAppLanguagePicker(false));
        addSettingsDivider(app);
        addSettingsActionRow(app,"🎨","Theme Shop",v -> showTheme());
        addSettingsDivider(app);
        addSettingsActionRow(app,"Aa","Font Shop",v -> showFonts());
        addSettingsDivider(app);
        addSettingsActionRow(app,"▣","Clipboard",v -> showClipboard());
        addSettingsDivider(app);
        addSettingsActionRow(app,"⚡","Text shortcuts",v -> showShortcuts());
        addSettingsDivider(app);
        addSettingsActionRow(app,"Aa","Dictionary",v -> showDictionary());
        addSettingsDivider(app);
        addSettingsActionRow(app,"☺","Emoji & kaomoji",v -> showEmoji());
        addSettingsDivider(app);
        addSettingsActionRow(app,"🔒","Privacy",v -> showPrivacy());
        addSettingsDivider(app);
        addSettingsActionRow(app,"ⓘ","About KeyKii",v -> showAbout());
        addSettingsDivider(app);
        addSettingsActionRow(app,"?","Help & feedback",v -> showHelp());
        addSettingsDivider(app);
        addSettingsValueRow(app,"!","App version",appVersion());
        page.addView(app,settingsGroupParams());

        setContentView(shopRoot(wrap(page),"settings"));
    }

    private void addSettingsGroupTitle(LinearLayout page,String title) {
        TextView label=new TextView(this);
        label.setText(ui(title));
        label.setTextColor(Color.rgb(153,91,194));
        label.setTextSize(14);
        label.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        label.setPadding(dp(18),dp(22),dp(8),dp(3));
        page.addView(label);
    }

    private LinearLayout settingsGroup() {
        LinearLayout group=new LinearLayout(this);
        group.setOrientation(LinearLayout.VERTICAL);
        group.setPadding(dp(12),dp(7),dp(12),dp(7));
        GradientDrawable bg=round(Color.WHITE,24);
        bg.setStroke(dp(2),Color.rgb(154,83,198));
        group.setBackground(bg);
        return group;
    }

    private LinearLayout.LayoutParams settingsGroupParams() {
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(dp(2),dp(2),dp(2),dp(8));
        return p;
    }

    private void addSettingsDivider(LinearLayout parent) {
        View line=new View(this);
        line.setBackgroundColor(Color.rgb(235,231,237));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(1));
        p.setMargins(dp(56),0,dp(18),0);
        parent.addView(line,p);
    }

    private void addSettingsActionRow(LinearLayout parent,String icon,String title,View.OnClickListener listener) {
        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10),dp(9),dp(8),dp(9));
        row.setOnClickListener(listener);

        TextView iconView=new TextView(this);
        iconView.setText(icon);
        iconView.setTextColor(Color.rgb(176,96,229));
        iconView.setTextSize(20);
        iconView.setGravity(Gravity.CENTER);
        row.addView(iconView,new LinearLayout.LayoutParams(dp(48),dp(48)));

        TextView titleView=new TextView(this);
        titleView.setText(ui(title));
        titleView.setTextColor(Color.rgb(27,25,29));
        titleView.setTextSize(16);
        titleView.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(titleView,new LinearLayout.LayoutParams(0,dp(48),1f));

        TextView arrow=new TextView(this);
        arrow.setText("›");
        arrow.setTextColor(Color.BLACK);
        arrow.setTextSize(31);
        arrow.setGravity(Gravity.CENTER);
        row.addView(arrow,new LinearLayout.LayoutParams(dp(38),dp(48)));
        parent.addView(row,new LinearLayout.LayoutParams(-1,dp(66)));
    }

    private void addSettingsValueRow(LinearLayout parent,String icon,String title,String value) {
        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10),dp(9),dp(8),dp(9));

        TextView iconView=new TextView(this);
        iconView.setText(icon);
        iconView.setTextColor(Color.rgb(176,96,229));
        iconView.setTextSize(20);
        iconView.setGravity(Gravity.CENTER);
        row.addView(iconView,new LinearLayout.LayoutParams(dp(48),dp(48)));

        TextView titleView=new TextView(this);
        titleView.setText(ui(title)+": "+value);
        titleView.setTextColor(Color.rgb(27,25,29));
        titleView.setTextSize(16);
        titleView.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(titleView,new LinearLayout.LayoutParams(0,dp(48),1f));
        parent.addView(row,new LinearLayout.LayoutParams(-1,dp(66)));
    }

    private void addHomeHero(
            LinearLayout page
    ) {
        LinearLayout hero =
                new LinearLayout(this);

        hero.setOrientation(
                LinearLayout.VERTICAL
        );

        hero.setPadding(
                dp(20),
                dp(19),
                dp(20),
                dp(18)
        );

        GradientDrawable bg =
                new GradientDrawable(
                        GradientDrawable.Orientation.TL_BR,
                        new int[]{
                            Color.rgb(245,225,255),
                            Color.rgb(255,230,242),
                            Color.rgb(226,241,255)
                        }
                );

        bg.setCornerRadius(dp(28));
        hero.setBackground(bg);
        hero.setElevation(dp(2));

        TextView badge =
                new TextView(this);

        badge.setText("  ✦  NEW IN KEYKII  ");
        badge.setTextColor(
                Color.rgb(123,77,151)
        );
        badge.setTextSize(11);
        badge.setGravity(Gravity.CENTER);
        badge.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        GradientDrawable badgeBg =
                round(
                        Color.argb(160,255,255,255),
                        14
                );

        badge.setBackground(badgeBg);

        LinearLayout.LayoutParams badgeParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        dp(28)
                );

        hero.addView(
                badge,
                badgeParams
        );

        TextView title =
                new TextView(this);

        title.setText(
                "Make your keyboard yours ✨"
        );

        title.setTextColor(TEXT);
        title.setTextSize(24);
        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        title.setPadding(
                0,
                dp(13),
                0,
                dp(5)
        );

        hero.addView(title);

        TextView sub =
                new TextView(this);

        sub.setText(
                "Mix beautiful themes with cute fonts, then keep all your smart typing tools."
        );

        sub.setTextColor(
                Color.rgb(92,78,100)
        );

        sub.setTextSize(13);

        hero.addView(sub);

        LinearLayout pills =
                new LinearLayout(this);

        pills.setPadding(
                0,
                dp(13),
                0,
                0
        );

        pills.addView(
                homeHeroPill(
                        "🎨 Themes",
                        v -> showTheme()
                ),
                new LinearLayout.LayoutParams(
                        0,
                        dp(42),
                        1f
                )
        );

        LinearLayout.LayoutParams fp =
                new LinearLayout.LayoutParams(
                        0,
                        dp(42),
                        1f
                );

        fp.setMargins(
                dp(8),0,0,0
        );

        pills.addView(
                homeHeroPill(
                        "Aa  Fonts",
                        v -> showFonts()
                ),
                fp
        );

        hero.addView(pills);

        LinearLayout.LayoutParams hp =
                new LinearLayout.LayoutParams(
                        -1,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        hp.setMargins(
                0,
                dp(2),
                0,
                dp(9)
        );

        page.addView(hero,hp);
    }


    private TextView homeHeroPill(
            String text,
            View.OnClickListener listener
    ) {
        TextView pill =
                new TextView(this);

        pill.setText(text);
        pill.setTextColor(TEXT);
        pill.setTextSize(13);
        pill.setGravity(Gravity.CENTER);
        pill.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        GradientDrawable bg =
                round(
                        Color.argb(205,255,255,255),
                        16
                );

        bg.setStroke(
                dp(1),
                Color.argb(
                        75,
                        130,95,150
                )
        );

        pill.setBackground(bg);
        pill.setOnClickListener(listener);

        return pill;
    }


    private void addHomeCustomizeCards(
            LinearLayout page
    ) {
        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        LinearLayout themes =
                homeFeatureCard(
                        "🎨",
                        "Themes",
                        "Pastel, dark, gaming & more",
                        PASTEL_PINK,
                        v -> showTheme()
                );

        LinearLayout fonts =
                homeFeatureCard(
                        "Aa",
                        "Fonts",
                        "Cute, script, bubble & retro",
                        PASTEL_BLUE,
                        v -> showFonts()
                );

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        0,
                        dp(146),
                        1f
                );

        p.setMargins(
                0,
                dp(4),
                dp(5),
                dp(4)
        );

        row.addView(themes,p);

        LinearLayout.LayoutParams p2 =
                new LinearLayout.LayoutParams(
                        0,
                        dp(146),
                        1f
                );

        p2.setMargins(
                dp(5),
                dp(4),
                0,
                dp(4)
        );

        row.addView(fonts,p2);

        page.addView(row);
    }


    private LinearLayout homeFeatureCard(
            String icon,
            String title,
            String subtitle,
            int color,
            View.OnClickListener listener
    ) {
        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setGravity(
                Gravity.CENTER_HORIZONTAL
        );

        card.setPadding(
                dp(13),
                dp(15),
                dp(13),
                dp(12)
        );

        GradientDrawable bg =
                round(color,24);

        bg.setStroke(
                dp(1),
                Color.argb(
                        55,
                        130,105,145
                )
        );

        card.setBackground(bg);
        card.setOnClickListener(listener);

        TextView iconView =
                new TextView(this);

        iconView.setText(icon);
        iconView.setTextSize(
                "Aa".equals(icon)
                        ? 22
                        : 27
        );

        iconView.setTextColor(TEXT);
        iconView.setGravity(Gravity.CENTER);
        iconView.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        iconView.setBackground(
                round(
                        Color.argb(
                                205,
                                255,255,255
                        ),
                        18
                )
        );

        card.addView(
                iconView,
                new LinearLayout.LayoutParams(
                        dp(54),
                        dp(54)
                )
        );

        TextView titleView =
                new TextView(this);

        titleView.setText(title);
        titleView.setTextColor(TEXT);
        titleView.setTextSize(17);
        titleView.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        titleView.setGravity(
                Gravity.CENTER
        );

        titleView.setPadding(
                0,
                dp(7),
                0,
                dp(2)
        );

        card.addView(titleView);

        TextView subView =
                new TextView(this);

        subView.setText(subtitle);
        subView.setTextColor(MUTED);
        subView.setTextSize(10);
        subView.setGravity(
                Gravity.CENTER
        );

        card.addView(subView);

        return card;
    }


    private void addHomeProBanner(
            LinearLayout page
    ) {
        LinearLayout banner =
                new LinearLayout(this);

        banner.setGravity(
                Gravity.CENTER_VERTICAL
        );

        banner.setPadding(
                dp(16),
                dp(15),
                dp(14),
                dp(15)
        );

        GradientDrawable bg =
                new GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT,
                        new int[]{
                            Color.rgb(246,218,255),
                            Color.rgb(255,223,236),
                            Color.rgb(255,237,210)
                        }
                );

        bg.setCornerRadius(dp(24));
        banner.setBackground(bg);

        TextView star =
                new TextView(this);

        star.setText("✦");
        star.setTextSize(26);
        star.setTextColor(
                Color.rgb(151,83,171)
        );
        star.setGravity(Gravity.CENTER);

        banner.addView(
                star,
                new LinearLayout.LayoutParams(
                        dp(48),
                        dp(48)
                )
        );

        LinearLayout words =
                new LinearLayout(this);

        words.setOrientation(
                LinearLayout.VERTICAL
        );

        TextView title =
                new TextView(this);

        title.setText(
                "KeyKii Pro Preview"
        );

        title.setTextColor(TEXT);
        title.setTextSize(16);
        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        TextView sub =
                new TextView(this);

        sub.setText(
                "Premium themes + expressive fonts"
        );

        sub.setTextColor(MUTED);
        sub.setTextSize(11);

        words.addView(title);
        words.addView(sub);

        banner.addView(
                words,
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                )
        );

        TextView arrow =
                new TextView(this);

        arrow.setText("›");
        arrow.setTextSize(28);
        arrow.setTextColor(
                Color.rgb(129,96,140)
        );
        arrow.setGravity(Gravity.CENTER);

        banner.addView(
                arrow,
                new LinearLayout.LayoutParams(
                        dp(32),
                        dp(48)
                )
        );

        banner.setOnClickListener(
                v -> showFonts()
        );

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        -1,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        p.setMargins(
                0,
                dp(7),
                0,
                dp(6)
        );

        page.addView(banner,p);
    }


    private void addHomeSetupButtons(
            LinearLayout page
    ) {
        LinearLayout row =
                new LinearLayout(this);

        TextView enable =
                homeSetupButton(
                        "✓  Enable KeyKii",
                        PASTEL_MINT,
                        v -> {
                            try {
                                startActivity(
                                        new Intent(
                                                Settings.ACTION_INPUT_METHOD_SETTINGS
                                        )
                                );
                            } catch(Exception e) {
                                toast(
                                        "Android keyboard settings are unavailable on this device."
                                );
                            }
                        }
                );

        TextView choose =
                homeSetupButton(
                        "⌨  Choose Keyboard",
                        PASTEL_PEACH,
                        v -> {
                            InputMethodManager imm =
                                    (InputMethodManager)
                                            getSystemService(
                                                    INPUT_METHOD_SERVICE
                                            );

                            if(imm!=null)
                                imm.showInputMethodPicker();
                        }
                );

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        0,
                        dp(54),
                        1f
                );

        p.setMargins(
                0,
                dp(4),
                dp(5),
                dp(4)
        );

        row.addView(enable,p);

        LinearLayout.LayoutParams p2 =
                new LinearLayout.LayoutParams(
                        0,
                        dp(54),
                        1f
                );

        p2.setMargins(
                dp(5),
                dp(4),
                0,
                dp(4)
        );

        row.addView(choose,p2);

        page.addView(row);
    }


    private TextView homeSetupButton(
            String text,
            int color,
            View.OnClickListener listener
    ) {
        TextView button =
                new TextView(this);

        button.setText(text);
        button.setTextColor(TEXT);
        button.setTextSize(12);
        button.setGravity(Gravity.CENTER);
        button.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        GradientDrawable bg =
                round(color,18);

        bg.setStroke(
                dp(1),
                BORDER
        );

        button.setBackground(bg);
        button.setOnClickListener(listener);

        return button;
    }



    private void showIntro() {
        screen="intro";
        hideSoftKeyboardNow();

        LinearLayout page=new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setGravity(Gravity.CENTER_HORIZONTAL);
        page.setPadding(dp(24),dp(34),dp(24),dp(32));
        page.setBackgroundColor(Color.WHITE);

        ImageView logo=new ImageView(this);
        logo.setImageResource(R.drawable.keykii_official_color_icon);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        page.addView(logo,new LinearLayout.LayoutParams(dp(180),dp(180)));

        TextView title=new TextView(this);
        title.setText("KeyKii Keyboard");
        title.setTextColor(TEXT);
        title.setTextSize(31);
        title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        page.addView(title);

        TextView sub=new TextView(this);
        sub.setText(ui("Cute, smart and made for you ✨"));
        sub.setTextColor(MUTED);
        sub.setTextSize(15);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0,dp(5),0,dp(22));
        page.addView(sub);

        LinearLayout featureRow=new LinearLayout(this);
        featureRow.setOrientation(LinearLayout.HORIZONTAL);
        featureRow.setGravity(Gravity.CENTER);

        featureRow.addView(
                introFeatureCard("🎨","Themes","Cute, dark & dreamy",Color.rgb(255,232,243)),
                new LinearLayout.LayoutParams(0,dp(116),1f)
        );
        featureRow.addView(
                introFeatureCard("Aa","Fonts","1,000+ styles",Color.rgb(230,240,255)),
                new LinearLayout.LayoutParams(0,dp(116),1f)
        );
        featureRow.addView(
                introFeatureCard("🌈","Color","300 color fonts",Color.rgb(239,229,255)),
                new LinearLayout.LayoutParams(0,dp(116),1f)
        );
        page.addView(featureRow,new LinearLayout.LayoutParams(-1,dp(116)));

        TextView language=new TextView(this);
        language.setText("🌐  "+ui("App language")+"  •  "+appLanguageName()+"   ›");
        language.setTextColor(TEXT);
        language.setTextSize(15);
        language.setGravity(Gravity.CENTER_VERTICAL);
        language.setPadding(dp(18),0,dp(18),0);
        GradientDrawable languageBg=round(Color.rgb(249,244,252),22);
        languageBg.setStroke(dp(1),Color.rgb(224,208,234));
        language.setBackground(languageBg);
        language.setOnClickListener(v -> showAppLanguagePicker(true));

        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(58));
        lp.setMargins(0,dp(24),0,0);
        page.addView(language,lp);

        TextView continueButton=new TextView(this);
        continueButton.setText(ui("Get started"));
        continueButton.setTextColor(Color.WHITE);
        continueButton.setTextSize(17);
        continueButton.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        continueButton.setGravity(Gravity.CENTER);

        GradientDrawable continueBg=new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        Color.rgb(183,101,236),
                        Color.rgb(238,104,184),
                        Color.rgb(255,170,118)
                }
        );
        continueBg.setCornerRadius(dp(27));
        continueButton.setBackground(continueBg);
        continueButton.setOnClickListener(v -> {
            prefs.edit()
                    .putBoolean("app_intro_seen",true)
                    .putInt("app_intro_version_seen",254)
                    .apply();
            showTheme();
        });

        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,dp(58));
        cp.setMargins(0,dp(16),0,0);
        page.addView(continueButton,cp);

        TextView note=new TextView(this);
        note.setText(ui("You can change the app language later in Settings."));
        note.setTextColor(MUTED);
        note.setTextSize(11);
        note.setGravity(Gravity.CENTER);
        note.setPadding(0,dp(12),0,0);
        page.addView(note);

        setContentView(wrap(page));
    }

    private LinearLayout introFeatureCard(
            String icon,
            String title,
            String subtitle,
            int color
    ) {
        LinearLayout card=new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(5),dp(8),dp(5),dp(8));

        LinearLayout.LayoutParams margin=new LinearLayout.LayoutParams(-1,-1);
        margin.setMargins(dp(4),0,dp(4),0);
        card.setLayoutParams(margin);

        GradientDrawable bg=round(color,24);
        bg.setStroke(dp(1),Color.rgb(233,225,235));
        card.setBackground(bg);

        TextView iconView=new TextView(this);
        iconView.setText(icon);
        iconView.setTextSize(24);
        iconView.setGravity(Gravity.CENTER);
        card.addView(iconView,new LinearLayout.LayoutParams(-1,dp(40)));

        TextView titleView=new TextView(this);
        titleView.setText(ui(title));
        titleView.setTextColor(TEXT);
        titleView.setTextSize(13);
        titleView.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        titleView.setGravity(Gravity.CENTER);
        card.addView(titleView,new LinearLayout.LayoutParams(-1,dp(28)));

        TextView subView=new TextView(this);
        subView.setText(ui(subtitle));
        subView.setTextColor(MUTED);
        subView.setTextSize(9);
        subView.setGravity(Gravity.CENTER);
        subView.setMaxLines(2);
        card.addView(subView,new LinearLayout.LayoutParams(-1,dp(32)));

        return card;
    }

    private final String[] appLanguageCodes={"en","fil","ceb","es"};
    private final String[] appLanguageNames={"English","Filipino","Cebuano","Español"};

    private String appLanguageCode(){return prefs.getString("app_language","en");}

    private String appLanguageName(){
        String code=appLanguageCode();
        for(int i=0;i<appLanguageCodes.length;i++)
            if(appLanguageCodes[i].equals(code))return appLanguageNames[i];
        return "English";
    }

    private void showAppLanguagePicker(boolean onboarding){
        screen=onboarding?"intro_language":"app_language";
        hideSoftKeyboardNow();
        LinearLayout page=page("App language","Choose the language used across KeyKii",!onboarding);
        page.setFocusableInTouchMode(true);
        page.requestFocus();
        String current=appLanguageCode();

        for(int i=0;i<appLanguageCodes.length;i++){
            final String code=appLanguageCodes[i];
            final boolean selected=code.equals(current);
            addRow(page,selected?"✓":"🌐",appLanguageNames[i],
                    selected?"Selected":"Tap to use this language",v -> {
                        hideSoftKeyboardNow();
                        prefs.edit().putString("app_language",code).apply();
                        settingsScrollPositions.clear();
                        showAppLanguagePicker(onboarding);
                    });
        }

        addActionButton(page,onboarding?"Continue":"Done",v -> {
            if(onboarding){
                prefs.edit()
                        .putBoolean("app_intro_seen",true)
                        .putInt("app_intro_version_seen",254)
                        .apply();
                showTheme();
            }else showHome();
        });
        setContentView(wrap(page));
    }

    private String ui(String english){
        if(english==null)return "";
        String code=appLanguageCode();
        if("fil".equals(code))return uiFil(english);
        if("ceb".equals(code))return uiCeb(english);
        if("es".equals(code))return uiEs(english);
        return english;
    }

    private String uiFil(String x){
        switch(x){
            case "Settings":return "Mga Setting";
            case "Manage KeyKii, your keyboard and the app":return "Pamahalaan ang KeyKii, keyboard at app";
            case "Tutorial":return "Gabay";
            case "How to change keyboard":return "Paano magpalit ng keyboard";
            case "How to get Custom Keyboard":return "Paano kumuha ng Custom Keyboard";
            case "Change Keyboard":return "Palitan ang Keyboard";
            case "Keyboard Languages":return "Mga Wika ng Keyboard";
            case "Preferences":return "Mga Kagustuhan";
            case "Smart typing":return "Smart na pag-type";
            case "Language":return "Wika";
            case "Dictionary":return "Diksyunaryo";
            case "Emoji & kaomoji":return "Emoji at kaomoji";
            case "About KeyKii":return "Tungkol sa KeyKii";
            case "Help & feedback":return "Tulong at feedback";
            case "App version":return "Bersyon ng app";
            case "Themes":return "Mga Tema";
            case "Fonts":return "Mga Font";
            case "Mine":return "Akin";
            case "All":return "Lahat";
            case "Selected":return "Napili";
            case "Free":return "Libre";
            case "App language":return "Wika ng app";
            case "Choose the language used across KeyKii":return "Piliin ang wikang gagamitin sa buong KeyKii";
            case "Continue":return "Magpatuloy";
            case "Done":return "Tapos";
            case "Make your keyboard yours":return "Gawing iyo ang keyboard mo";
            case "You can change the app language later in Settings.":return "Maaari mong palitan ang wika sa Mga Setting.";
            case "Test your new keyboard here!":return "Subukan ang bago mong keyboard dito!";
            case "Your collection":return "Iyong koleksyon";
            case "Discover themes":return "Tuklasin ang mga tema";
            case "Choose an alphabet style for your keyboard and combine it with any KeyKii theme.":return "Pumili ng estilo ng letra at ihalo ito sa kahit anong KeyKii theme.";
            case "Get started":return "Magsimula";
            case "Cute, smart and made for you ✨":return "Cute, smart at ginawa para sa iyo ✨";
            case "Cute":return "Cute";
            case "Aesthetic":return "Aesthetic";
            case "Dreamy":return "Dreamy";
            case "Dark":return "Madilim";
            case "Nature":return "Kalikasan";
            case "Seasonal":return "Seasonal";
            default:return x;
        }
    }

    private String uiCeb(String x){
        switch(x){
            case "Settings":return "Mga Setting";
            case "Manage KeyKii, your keyboard and the app":return "Dumalaa ang KeyKii, keyboard ug app";
            case "Tutorial":return "Giya";
            case "How to change keyboard":return "Unsaon pag-ilis sa keyboard";
            case "How to get Custom Keyboard":return "Unsaon pagkuha sa Custom Keyboard";
            case "Change Keyboard":return "Ilisi ang Keyboard";
            case "Keyboard Languages":return "Mga Pinulongan sa Keyboard";
            case "Preferences":return "Mga Kagustuhan";
            case "Language":return "Pinulongan";
            case "Dictionary":return "Diksyonaryo";
            case "Emoji & kaomoji":return "Emoji ug kaomoji";
            case "About KeyKii":return "Mahitungod sa KeyKii";
            case "Help & feedback":return "Tabang ug feedback";
            case "App version":return "Bersyon sa app";
            case "Themes":return "Mga Tema";
            case "Fonts":return "Mga Font";
            case "Mine":return "Akoa";
            case "All":return "Tanan";
            case "Selected":return "Napili";
            case "Free":return "Libre";
            case "App language":return "Pinulongan sa app";
            case "Choose the language used across KeyKii":return "Pilia ang pinulongan nga gamiton sa KeyKii";
            case "Continue":return "Padayon";
            case "Done":return "Human";
            case "Make your keyboard yours":return "Himoang imo ang imong keyboard";
            case "You can change the app language later in Settings.":return "Mahimo nimo usbon ang pinulongan sa Settings.";
            case "Test your new keyboard here!":return "Sulayi dinhi ang imong bag-ong keyboard!";
            case "Your collection":return "Imong koleksyon";
            case "Discover themes":return "Tan-awa ang mga tema";
            case "Choose an alphabet style for your keyboard and combine it with any KeyKii theme.":return "Pilia ang estilo sa letra ug ipares sa bisan unsang KeyKii theme.";
            case "Get started":return "Sugdi";
            case "Cute, smart and made for you ✨":return "Cute, smart ug gihimo para nimo ✨";
            case "Dark":return "Ngitngit";
            case "Nature":return "Kinaiyahan";
            default:return x;
        }
    }

    private String uiEs(String x){
        switch(x){
            case "Settings":return "Ajustes";
            case "Manage KeyKii, your keyboard and the app":return "Gestiona KeyKii, tu teclado y la app";
            case "How to change keyboard":return "Cómo cambiar el teclado";
            case "How to get Custom Keyboard":return "Cómo obtener un teclado personalizado";
            case "Change Keyboard":return "Cambiar teclado";
            case "Keyboard Languages":return "Idiomas del teclado";
            case "Preferences":return "Preferencias";
            case "Smart typing":return "Escritura inteligente";
            case "Voice typing":return "Escritura por voz";
            case "Language":return "Idioma";
            case "Theme Shop":return "Tienda de temas";
            case "Font Shop":return "Tienda de fuentes";
            case "Clipboard":return "Portapapeles";
            case "Dictionary":return "Diccionario";
            case "Privacy":return "Privacidad";
            case "About KeyKii":return "Acerca de KeyKii";
            case "Help & feedback":return "Ayuda y comentarios";
            case "App version":return "Versión de la app";
            case "Themes":return "Temas";
            case "Fonts":return "Fuentes";
            case "Mine":return "Mío";
            case "All":return "Todo";
            case "Selected":return "Seleccionado";
            case "Free":return "Gratis";
            case "Trending":return "Tendencias";
            case "Color Fonts":return "Fuentes de color";
            case "App language":return "Idioma de la app";
            case "Choose the language used across KeyKii":return "Elige el idioma de toda la app KeyKii";
            case "Continue":return "Continuar";
            case "Done":return "Listo";
            case "Make your keyboard yours":return "Haz tuyo tu teclado";
            case "You can change the app language later in Settings.":return "Puedes cambiar el idioma más tarde en Ajustes.";
            case "Test your new keyboard here!":return "¡Prueba aquí tu nuevo teclado!";
            case "Your collection":return "Tu colección";
            case "Discover themes":return "Descubrir temas";
            case "Choose an alphabet style for your keyboard and combine it with any KeyKii theme.":return "Elige un estilo de letras y combínalo con cualquier tema de KeyKii.";
            case "Get started":return "Empezar";
            case "Cute, smart and made for you ✨":return "Bonito, inteligente y hecho para ti ✨";
            case "Cute":return "Tierno";
            case "Aesthetic":return "Estético";
            case "Dreamy":return "Soñador";
            case "Dark":return "Oscuro";
            case "Nature":return "Naturaleza";
            case "Seasonal":return "Temporada";
            default:return x;
        }
    }

    private void showMine(){
        screen="mine";
        hideSoftKeyboardNow();
        LinearLayout page=page("Mine","Your keyboard, saved themes and quick test",false);
        page.setPadding(dp(18),dp(18),dp(18),dp(120));

        LinearLayout tester=new LinearLayout(this);
        tester.setOrientation(LinearLayout.VERTICAL);
        tester.setPadding(dp(18),dp(18),dp(18),dp(18));
        GradientDrawable testerBg=round(Color.WHITE,24);
        testerBg.setStroke(dp(2),Color.rgb(191,139,225));
        tester.setBackground(testerBg);

        TextView testerTitle=new TextView(this);
        testerTitle.setText(ui("Test your new keyboard here!"));
        testerTitle.setTextColor(TEXT);
        testerTitle.setTextSize(18);
        testerTitle.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        testerTitle.setGravity(Gravity.CENTER);
        tester.addView(testerTitle);

        EditText input=new EditText(this);
        input.setHint("Enter something");
        input.setTextColor(TEXT);
        input.setHintTextColor(Color.rgb(185,180,188));
        input.setTextSize(17);
        input.setPadding(dp(14),dp(6),dp(14),dp(6));
        GradientDrawable inputBg=round(Color.WHITE,20);
        inputBg.setStroke(dp(2),Color.rgb(220,216,222));
        input.setBackground(inputBg);
        LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(-1,dp(66));
        ip.setMargins(0,dp(14),0,0);
        tester.addView(input,ip);
        page.addView(tester);

        addSection(page,"Access");
        addAdAccessCard(page);

        addSection(page,"Your collection");
        addRow(page,"🎨","Current theme",themeName(),v -> showTheme());
        addRow(page,"Aa","Current font",keyboardFontName(),v -> showFonts());
        addRow(
                page,
                "♥",
                "Saved themes",
                KeyKiiThemeCollection.favoriteCount(this)+" favorites",
                v -> showTheme()
        );

        int[] favorites=KeyKiiThemeCollection.favorites(this,12);
        addThemeSection(page,"♥ FAVORITE THEMES");
        if(favorites.length==0){
            addInfoCard(
                    page,
                    "No favorites yet",
                    "Open any theme preview and tap ♡ Save. Your saved themes will appear here."
            );
        }else{
            addStylePackGrid(page,favorites);
        }

        int[] recents=KeyKiiThemeCollection.recents(this,8);
        addThemeSection(page,"↻ RECENTLY USED");
        if(recents.length==0){
            addInfoCard(
                    page,
                    "No recent themes yet",
                    "Themes you apply will appear here automatically."
            );
        }else{
            addStylePackGrid(page,recents);
        }

        addActionButton(page,"Discover more themes",v -> showTheme());
        setContentView(shopRoot(wrap(page),"mine"));
    }

    private View shopRoot(ScrollView scroll,String active){
        FrameLayout root=new FrameLayout(this);
        root.setBackgroundColor(BG);
        root.addView(scroll,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout nav=buildShopBottomNav(active);
        FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(-1,dp(82),Gravity.BOTTOM);
        p.setMargins(dp(10),0,dp(10),dp(10));
        root.addView(nav,p);
        return root;
    }

    private LinearLayout buildShopBottomNav(String active){
        LinearLayout nav=new LinearLayout(this);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(5),dp(6),dp(5),dp(6));
        GradientDrawable bg=round(Color.WHITE,32);
        bg.setStroke(dp(1),Color.rgb(235,230,237));
        nav.setBackground(bg);
        nav.setElevation(dp(10));
        nav.addView(shopNavItem("🎨","Themes","themes",active,v -> showTheme()),new LinearLayout.LayoutParams(0,-1,1f));
        nav.addView(shopNavItem("A","Fonts","fonts",active,v -> showFonts()),new LinearLayout.LayoutParams(0,-1,1f));
        nav.addView(shopNavItem("▣","Mine","mine",active,v -> showMine()),new LinearLayout.LayoutParams(0,-1,1f));
        nav.addView(shopNavItem("⚙","Settings","settings",active,v -> showHome()),new LinearLayout.LayoutParams(0,-1,1f));
        return nav;
    }

    private LinearLayout shopNavItem(String icon,String label,String id,String active,View.OnClickListener listener){
        boolean selected=id.equals(active);
        LinearLayout item=new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        item.setOnClickListener(listener);
        TextView iconView=new TextView(this);
        iconView.setText(icon);
        iconView.setTextSize(20);
        iconView.setGravity(Gravity.CENTER);
        iconView.setTextColor(selected?Color.rgb(178,91,231):Color.rgb(110,108,112));
        TextView textView=new TextView(this);
        textView.setText(ui(label));
        textView.setTextSize(11);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(selected?Color.rgb(178,91,231):Color.rgb(62,60,64));
        item.addView(iconView,new LinearLayout.LayoutParams(-1,dp(36)));
        item.addView(textView,new LinearLayout.LayoutParams(-1,dp(26)));
        return item;
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
        themeBrowseMode="all";
        neoThemeShownCount=NeoThemeCatalog.PAGE_SIZE;
        hideSoftKeyboardNow();
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
                false
        );

        // Leave room for the fixed preview sheet, like Gboard.
        page.setPadding(
                dp(18),
                dp(18),
                dp(18),
                dp(370)
        );

        addThemeStoreHero(page);
        addThemePricingCard(page);
        addAdAccessCard(page);

        addThemeFilterBar(page);

        themeCategoryContent=new LinearLayout(this);
        themeCategoryContent.setOrientation(LinearLayout.VERTICAL);
        page.addView(
                themeCategoryContent,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        renderThemeCategoryContent();

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
                dp(102)
        );

        root.addView(themePreviewSheet,sheetParams);

        LinearLayout shopNav=buildShopBottomNav("themes");
        FrameLayout.LayoutParams shopNavParams=new FrameLayout.LayoutParams(-1,dp(82),Gravity.BOTTOM);
        shopNavParams.setMargins(dp(10),0,dp(10),dp(10));
        root.addView(shopNav,shopNavParams);

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
            case 108: return "Pacifico";
            case 109: return "Caveat";
            case 110: return "Berkshire Swash";
            case 111: return "Kaushan Script";
            case 112: return "Fascinate Inline";
            case 113: return "Monoton";
            case 114: return "Frijole";
            case 115: return "Barrio";
            case 116: return "Knewave";
            case 117: return "Rye";
            case 118: return "Creepster";
            case 119: return "Baloo 2";
            case 120: return "Modak";
            case 121: return "Fredericka the Great";
            case 122: return "Gluten";
            case 123: return "Londrina Sketch";
            case 124: return "Black Ops One";
            case 125: return "Bowlby One SC";
            case 126: return "Bubblegum Sans";
            case 127: return "Cherry Bomb One";
            case 128: return "Codystar";
            case 129: return "Diplomata SC";
            case 130: return "Emblema One";
            case 131: return "Ewert";
            case 132: return "Faster One";
            case 133: return "Finger Paint";
            case 134: return "Geostar";
            case 135: return "Geostar Fill";
            case 136: return "Gravitas One";
            case 137: return "Henny Penny";
            case 138: return "Jolly Lodger";
            case 139: return "Kablammo";
            case 140: return "Kirang Haerang";
            case 141: return "Lacquer";
            case 142: return "Limelight";
            case 143: return "Metal Mania";
            case 144: return "Mogra";
            case 145: return "Nosifer";
            case 146: return "Rampart One";
            case 147: return "Ribeye";
            case 148: return "Rubik Beastly";
            case 149: return "Rubik Moonrocks";
            case 150: return "Train One";
            case 151: return "Unifraktur Cook";

            default:
                if(ColorFontCatalog.isColorStyle(value)) {
                    ColorFontCatalog.Entry e=ColorFontCatalog.find(value);
                    if(e!=null) return e.name;
                }
                if(value>=RemoteFontCatalog.FIRST_STYLE) {
                    RemoteFontCatalog.Entry e=RemoteFontCatalog.find(value);
                    if(e!=null) return e.name;
                }
                return "System";
        }
    }


    private Typeface settingsKeyboardTypeface(int style) {
        try {
            if(ColorFontCatalog.isColorStyle(style)) {
                ColorFontCatalog.Entry e=ColorFontCatalog.find(style);
                if(e!=null) return settingsKeyboardTypeface(e.baseStyle);
            }
            if(style>=RemoteFontCatalog.FIRST_STYLE) {
                java.io.File remote=remoteFontFile(style);
                if(remote.exists())
                    return Typeface.createFromFile(remote);
                return Typeface.create("sans-serif",Typeface.NORMAL);
            }

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
                case 108:
                    return Typeface.createFromAsset(getAssets(),"fonts/pacifico.ttf");
                case 109:
                    return Typeface.createFromAsset(getAssets(),"fonts/caveat.ttf");
                case 110:
                    return Typeface.createFromAsset(getAssets(),"fonts/berkshire_swash.ttf");
                case 111:
                    return Typeface.createFromAsset(getAssets(),"fonts/kaushan_script.ttf");
                case 112:
                    return Typeface.createFromAsset(getAssets(),"fonts/fascinate_inline.ttf");
                case 113:
                    return Typeface.createFromAsset(getAssets(),"fonts/monoton.ttf");
                case 114:
                    return Typeface.createFromAsset(getAssets(),"fonts/frijole.ttf");
                case 115:
                    return Typeface.createFromAsset(getAssets(),"fonts/barrio.ttf");
                case 116:
                    return Typeface.createFromAsset(getAssets(),"fonts/knewave.ttf");
                case 117:
                    return Typeface.createFromAsset(getAssets(),"fonts/rye.ttf");
                case 118:
                    return Typeface.createFromAsset(getAssets(),"fonts/creepster.ttf");
                case 119:
                    return Typeface.createFromAsset(getAssets(),"fonts/baloo2.ttf");
                case 120:
                    return Typeface.createFromAsset(getAssets(),"fonts/modak.ttf");
                case 121:
                    return Typeface.createFromAsset(getAssets(),"fonts/fredericka_the_great.ttf");
                case 122:
                    return Typeface.createFromAsset(getAssets(),"fonts/gluten.ttf");
                case 123:
                    return Typeface.createFromAsset(getAssets(),"fonts/londrina_sketch.ttf");
                case 124:
                    return Typeface.createFromAsset(getAssets(),"fonts/black_ops_one.ttf");
                case 125:
                    return Typeface.createFromAsset(getAssets(),"fonts/bowlby_one_sc.ttf");
                case 126:
                    return Typeface.createFromAsset(getAssets(),"fonts/bubblegum_sans.ttf");
                case 127:
                    return Typeface.createFromAsset(getAssets(),"fonts/cherry_bomb_one.ttf");
                case 128:
                    return Typeface.createFromAsset(getAssets(),"fonts/codystar.ttf");
                case 129:
                    return Typeface.createFromAsset(getAssets(),"fonts/diplomata_sc.ttf");
                case 130:
                    return Typeface.createFromAsset(getAssets(),"fonts/emblema_one.ttf");
                case 131:
                    return Typeface.createFromAsset(getAssets(),"fonts/ewert.ttf");
                case 132:
                    return Typeface.createFromAsset(getAssets(),"fonts/faster_one.ttf");
                case 133:
                    return Typeface.createFromAsset(getAssets(),"fonts/finger_paint.ttf");
                case 134:
                    return Typeface.createFromAsset(getAssets(),"fonts/geostar.ttf");
                case 135:
                    return Typeface.createFromAsset(getAssets(),"fonts/geostar_fill.ttf");
                case 136:
                    return Typeface.createFromAsset(getAssets(),"fonts/gravitas_one.ttf");
                case 137:
                    return Typeface.createFromAsset(getAssets(),"fonts/henny_penny.ttf");
                case 138:
                    return Typeface.createFromAsset(getAssets(),"fonts/jolly_lodger.ttf");
                case 139:
                    return Typeface.createFromAsset(getAssets(),"fonts/kablammo.ttf");
                case 140:
                    return Typeface.createFromAsset(getAssets(),"fonts/kirang_haerang.ttf");
                case 141:
                    return Typeface.createFromAsset(getAssets(),"fonts/lacquer.ttf");
                case 142:
                    return Typeface.createFromAsset(getAssets(),"fonts/limelight.ttf");
                case 143:
                    return Typeface.createFromAsset(getAssets(),"fonts/metal_mania.ttf");
                case 144:
                    return Typeface.createFromAsset(getAssets(),"fonts/mogra.ttf");
                case 145:
                    return Typeface.createFromAsset(getAssets(),"fonts/nosifer.ttf");
                case 146:
                    return Typeface.createFromAsset(getAssets(),"fonts/rampart_one.ttf");
                case 147:
                    return Typeface.createFromAsset(getAssets(),"fonts/ribeye.ttf");
                case 148:
                    return Typeface.createFromAsset(getAssets(),"fonts/rubik_beastly.ttf");
                case 149:
                    return Typeface.createFromAsset(getAssets(),"fonts/rubik_moonrocks.ttf");
                case 150:
                    return Typeface.createFromAsset(getAssets(),"fonts/train_one.ttf");
                case 151:
                    return Typeface.createFromAsset(getAssets(),"fonts/unifraktur_cook.ttf");
                default:
                    return Typeface.create("sans-serif",Typeface.NORMAL);
            }
        } catch(Exception ignored) {
            return Typeface.DEFAULT;
        }
    }


    private int fontDisplayTextSize(int style) {
        if(ColorFontCatalog.isColorStyle(style)) {
            ColorFontCatalog.Entry e=ColorFontCatalog.find(style);
            if(e!=null) return fontDisplayTextSize(e.baseStyle);
        }
        switch(style) {
            case 106:
                return 13;
            case 105:
            case 112:
            case 113:
            case 114:
            case 118:
            case 120:
            case 123:
                return 18;
            default:
                return 23;
        }
    }


    private void showFonts(){
        screen="fonts";
        fontBrowseMode="all";
        remoteFontShownCount=24;
        colorFontShownCount=30;
        hideSoftKeyboardNow();

        LinearLayout page=page(
                "Fonts",
                "Choose a font style for your KeyKii keyboard",
                false
        );
        page.setPadding(dp(18),dp(18),dp(18),dp(120));

        LinearLayout hero=new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(dp(18),dp(16),dp(18),dp(15));

        GradientDrawable heroBg=new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{
                        Color.rgb(255,244,235),
                        Color.rgb(246,248,239),
                        Color.rgb(250,242,250)
                }
        );
        heroBg.setCornerRadius(dp(24));
        heroBg.setStroke(dp(1),Color.rgb(238,229,224));
        hero.setBackground(heroBg);

        TextView heroTitle=new TextView(this);
        heroTitle.setText("1,383 keyboard font styles");
        heroTitle.setTextColor(TEXT);
        heroTitle.setTextSize(19);
        heroTitle.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        hero.addView(heroTitle);

        TextView heroSub=new TextView(this);
        heroSub.setText(
                ui("Choose an alphabet style for your keyboard and combine it with any KeyKii theme.")
        );
        heroSub.setTextColor(MUTED);
        heroSub.setTextSize(11);
        heroSub.setPadding(0,dp(5),0,0);
        hero.addView(heroSub);

        LinearLayout.LayoutParams hp=
                new LinearLayout.LayoutParams(
                        -1,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        hp.setMargins(0,dp(6),0,dp(12));
        page.addView(hero,hp);
        addAdAccessCard(page);

        addFontFilterBar(page);

        fontCategoryContent=new LinearLayout(this);
        fontCategoryContent.setOrientation(LinearLayout.VERTICAL);
        page.addView(
                fontCategoryContent,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        renderFontCategoryContent();

        setContentView(
                shopRoot(
                        wrap(page),
                        "fonts"
                )
        );
    }

    private void renderFontCategoryContent() {
        if(fontCategoryContent==null)
            return;

        LinearLayout page=fontCategoryContent;
        page.removeAllViews();

        if("selected".equals(fontBrowseMode)) {
            addSection(page,"Selected");
            addCurrentlySelectedFontCard(page);

        } else if("free".equals(fontBrowseMode)) {
            addSection(page,"Free");

            int[] base={
                    0,1,2,3,4,5,6,
                    124,125,126,127,128,129,130,131,132,133,134,135
            };
            String[] names={
                    "System","Rounded","Serif","Mono","Condensed","Casual","Medium",
                    "Black Ops One","Bowlby One SC","Bubblegum Sans","Cherry Bomb One",
                    "Codystar","Diplomata SC","Emblema One","Ewert","Faster One",
                    "Finger Paint","Geostar","Geostar Fill"
            };

            for(int i=0;i<base.length;i++)
                addFontStoreCard(
                        page,
                        base[i],
                        names[i],
                        "Free keyboard alphabet",
                        false
                );

            addSection(page,"Free / Watch Ad");
            addRemoteFontAccessCollection(page,false);

            addSection(page,"Color Fonts • Free / Watch Ad");
            addColorFontCollection(page,false);

        } else if("pro".equals(fontBrowseMode)) {
            addSection(page,"Pro");

            int[] pro={
                    100,101,102,103,104,105,106,107,108,109,110,111,
                    112,113,114,115,116,117,118,119,120,121,122,123,
                    136,137,138,139,140,141,142,143,144,145,146,147,
                    148,149,150,151
            };

            for(int style:pro)
                addFontStoreCard(
                        page,
                        style,
                        fontNameForStyle(style),
                        "Premium keyboard alphabet",
                        true
                );

            addSection(page,"More Pro fonts");
            addRemoteFontAccessCollection(page,true);

            addSection(page,"Color Fonts • Pro");
            addColorFontCollection(page,true);

        } else if("trending".equals(fontBrowseMode)) {
            addSection(page,"Trending");

            int[] trend={
                    100,101,102,103,104,105,106,
                    108,109,119,126,127,139,149
            };

            for(int style:trend)
                addFontStoreCard(
                        page,
                        style,
                        fontNameForStyle(style),
                        "Popular keyboard alphabet",
                        (style>=100&&style<=123)||style>=136
                );

            addSection(page,"Trending color fonts");

            for(int i=0;i<24;i++)
                addColorFontStoreCard(
                        page,
                        ColorFontCatalog.find(
                                ColorFontCatalog.FIRST_STYLE+i
                        )
                );

        } else if("color".equals(fontBrowseMode)) {
            addSection(page,"300 Color Fonts");

            addInfoCard(
                    page,
                    "Color Fonts",
                    "These styles color the letters and numbers on your KeyKii keyboard. They type normal text into apps and can be mixed with any theme."
            );

            addColorFontCollection(page,null);

        } else {
            addSection(page,"Free collection");
            addFontStoreCard(page,0,"System","Clean Android",false);
            addFontStoreCard(page,1,"Rounded","Soft rounded",false);
            addFontStoreCard(page,2,"Serif","Classic book",false);
            addFontStoreCard(page,3,"Mono","Fixed width",false);
            addFontStoreCard(page,4,"Condensed","Slim modern",false);
            addFontStoreCard(page,5,"Casual","Relaxed handwriting",false);
            addFontStoreCard(page,6,"Medium","Clean bold",false);

            addSection(page,"Popular font styles");

            for(int style=100;style<=123;style++)
                addFontStoreCard(
                        page,
                        style,
                        fontNameForStyle(style),
                        "Premium keyboard alphabet",
                        true
                );

            addSection(page,"New featured fonts");

            for(int style=124;style<=151;style++)
                addFontStoreCard(
                        page,
                        style,
                        fontNameForStyle(style),
                        "Featured keyboard alphabet",
                        style>=136
                );

            addSection(page,"300 Color Fonts");

            for(int i=0;i<12;i++)
                addColorFontStoreCard(
                        page,
                        ColorFontCatalog.find(
                                ColorFontCatalog.FIRST_STYLE+i
                        )
                );

            TextView colors=textButton("Browse all 300 Color Fonts");
            colors.setTextSize(14);
            colors.setOnClickListener(v -> {
                fontBrowseMode="color";
                colorFontShownCount=30;
                refreshFontFilterStyles();
                keepActiveFontFilterVisible();
                renderFontCategoryContent();
            });

            LinearLayout.LayoutParams cp=
                    new LinearLayout.LayoutParams(-1,dp(50));
            cp.setMargins(0,dp(6),0,dp(10));
            page.addView(colors,cp);

            addSection(page,"1,024 more fonts");
            addRemoteFontCollection(page);
        }

        addInfoCard(
                page,
                "Font behavior",
                "The selected font changes the alphabet design shown on the KeyKii keyboard. Text sent into apps remains normal."
        );
    }


    private String fontNameForStyle(int style){
        String[] n={"Fredoka","DynaPuff","Rubik Bubbles","Patrick Hand","Lobster","Bungee","Press Start 2P",
                "Cinzel Decorative","Pacifico","Caveat","Berkshire Swash","Kaushan Script","Fascinate Inline","Monoton",
                "Frijole","Barrio","Knewave","Rye","Creepster","Baloo 2","Modak","Fredericka the Great","Gluten",
                "Londrina Sketch","Black Ops One","Bowlby One SC","Bubblegum Sans","Cherry Bomb One","Codystar",
                "Diplomata SC","Emblema One","Ewert","Faster One","Finger Paint","Geostar","Geostar Fill","Gravitas One",
                "Henny Penny","Jolly Lodger","Kablammo","Kirang Haerang","Lacquer","Limelight","Metal Mania","Mogra",
                "Nosifer","Rampart One","Ribeye","Rubik Beastly","Rubik Moonrocks","Train One","Unifraktur Cook"};
        if(style>=100&&style<=151)return n[style-100];
        return "System";
    }

    private void addFontFilterBar(LinearLayout page){
        fontFilterChips.clear();

        fontFilterScroll=
                new android.widget.HorizontalScrollView(this);
        fontFilterScroll.setHorizontalScrollBarEnabled(false);
        fontFilterScroll.setFillViewport(false);

        fontFilterRow=new LinearLayout(this);
        fontFilterRow.setGravity(Gravity.CENTER_VERTICAL);
        fontFilterRow.setPadding(dp(2),dp(2),dp(2),dp(6));

        addFontFilterChip(fontFilterRow,"All","all");
        addFontFilterChip(fontFilterRow,"Selected","selected");
        addFontFilterChip(fontFilterRow,"Free","free");
        addFontFilterChip(fontFilterRow,"Pro","pro");
        addFontFilterChip(fontFilterRow,"Trending","trending");
        addFontFilterChip(fontFilterRow,"Color Fonts","color");

        fontFilterScroll.addView(fontFilterRow);
        page.addView(
                fontFilterScroll,
                new LinearLayout.LayoutParams(-1,dp(58))
        );

        keepActiveFontFilterVisible();
    }

    private void addFontFilterChip(
            LinearLayout row,
            String label,
            String mode
    ) {
        TextView chip=new TextView(this);
        chip.setText(ui(label));
        chip.setTag(mode);
        chip.setTextSize(12);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(dp(18),0,dp(18),0);

        fontFilterChips.add(chip);
        styleFontFilterChip(
                chip,
                mode.equals(fontBrowseMode)
        );

        chip.setOnClickListener(v -> {
            if(mode.equals(fontBrowseMode)) {
                keepActiveFontFilterVisible();
                return;
            }

            fontBrowseMode=mode;
            remoteFontShownCount=24;
            colorFontShownCount=30;

            refreshFontFilterStyles();
            keepActiveFontFilterVisible();
            renderFontCategoryContent();
        });

        LinearLayout.LayoutParams p=
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        dp(42)
                );
        p.setMargins(dp(3),0,dp(3),0);
        row.addView(chip,p);
    }

    private void styleFontFilterChip(
            TextView chip,
            boolean selected
    ) {
        chip.setTextColor(
                selected
                        ? Color.rgb(120,66,149)
                        : Color.rgb(88,80,92)
        );

        chip.setTypeface(
                Typeface.DEFAULT,
                selected ? Typeface.BOLD : Typeface.NORMAL
        );

        GradientDrawable bg=round(
                selected
                        ? Color.rgb(247,224,251)
                        : Color.WHITE,
                20
        );
        bg.setStroke(
                dp(selected ? 2 : 1),
                selected
                        ? Color.rgb(193,132,215)
                        : Color.rgb(232,226,235)
        );
        chip.setBackground(bg);
    }

    private void refreshFontFilterStyles() {
        for(TextView chip:fontFilterChips) {
            Object tag=chip.getTag();
            styleFontFilterChip(
                    chip,
                    tag!=null &&
                    fontBrowseMode.equals(
                            String.valueOf(tag)
                    )
            );
        }
    }

    private void keepActiveFontFilterVisible() {
        if(fontFilterScroll==null)
            return;

        fontFilterScroll.post(() -> {
            TextView selected=null;

            for(TextView chip:fontFilterChips) {
                Object tag=chip.getTag();
                if(
                        tag!=null &&
                        fontBrowseMode.equals(
                                String.valueOf(tag)
                        )
                ) {
                    selected=chip;
                    break;
                }
            }

            if(selected==null)
                return;

            int viewport=fontFilterScroll.getWidth();
            int target=
                    selected.getLeft() -
                    Math.max(
                            dp(12),
                            (viewport-selected.getWidth())/2
                    );

            fontFilterScroll.smoothScrollTo(
                    Math.max(0,target),
                    0
            );
        });
    }


    private void addRemoteFontAccessCollection(LinearLayout page,boolean pro){
        int shown=0,available=0;
        for(RemoteFontCatalog.Entry entry:RemoteFontCatalog.ITEMS){
            if(entry.pro!=pro)continue;
            available++;
            if(shown<remoteFontShownCount){addRemoteFontStoreCard(page,entry);shown++;}
        }
        if(shown<available){
            TextView more=textButton("Show 24 more  •  "+shown+"/"+available);
            more.setOnClickListener(v->{remoteFontShownCount+=24;renderFontCategoryContent();});
            page.addView(more,new LinearLayout.LayoutParams(-1,dp(50)));
        }
    }

    private void addColorFontCollection(LinearLayout page,Boolean proFilter){
        int shown=0,available=0;
        for(int i=0;i<ColorFontCatalog.COUNT;i++){
            ColorFontCatalog.Entry entry=ColorFontCatalog.find(ColorFontCatalog.FIRST_STYLE+i);
            if(proFilter!=null&&entry.pro!=proFilter.booleanValue())continue;
            available++;
            if(shown<colorFontShownCount){addColorFontStoreCard(page,entry);shown++;}
        }
        if(shown<available){
            TextView more=textButton("Show 30 more  •  "+shown+"/"+available);
            more.setOnClickListener(v->{colorFontShownCount+=30;renderFontCategoryContent();});
            page.addView(more,new LinearLayout.LayoutParams(-1,dp(50)));
        }
    }

    private void addColorFontStoreCard(LinearLayout page,ColorFontCatalog.Entry entry){
        if(entry==null)return;
        boolean selected=prefs.getInt("keyboard_font_style",0)==entry.style;
        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16),dp(7),dp(10),dp(7));
        GradientDrawable bg=round(selected?Color.rgb(247,239,252):Color.WHITE,13);
        bg.setStroke(dp(selected?2:1),selected?Color.rgb(181,143,204):Color.rgb(238,232,227));
        row.setBackground(bg);

        LinearLayout words=new LinearLayout(this);
        words.setOrientation(LinearLayout.VERTICAL);
        words.setGravity(Gravity.CENTER);
        TextView sample=new TextView(this);
        sample.setTextSize(fontDisplayTextSize(entry.baseStyle));
        sample.setGravity(Gravity.CENTER);
        sample.setSingleLine(true);
        sample.setTypeface(settingsKeyboardTypeface(entry.baseStyle));
        applyColorFontText(sample,entry.name,entry.style,TEXT);

        TextView sub=new TextView(this);
        sub.setText(
                hasAdAccess()
                        ? "Color alphabet • 24h access active"
                        : (entry.pro?"Color alphabet • PRO / AD":"Color alphabet • FREE / AD")
        );
        sub.setTextColor(MUTED);
        sub.setTextSize(9);
        sub.setGravity(Gravity.CENTER);
        words.addView(sample,new LinearLayout.LayoutParams(-1,dp(32)));
        words.addView(sub,new LinearLayout.LayoutParams(-1,dp(16)));
        row.addView(words,new LinearLayout.LayoutParams(0,dp(50),1f));

        TextView badge=new TextView(this);
        badge.setText(selected?"✓":(hasAdAccess()?"24H":(entry.pro?"PRO":"AD")));
        badge.setTextSize(selected?18:10);
        badge.setGravity(Gravity.CENTER);
        badge.setTextColor(selected?Color.rgb(114,79,133):(entry.pro?Color.rgb(184,95,115):Color.rgb(80,145,96)));
        badge.setBackground(round(selected?Color.rgb(239,224,248):(entry.pro?Color.rgb(253,239,240):Color.rgb(233,247,235)),11));
        row.addView(badge,new LinearLayout.LayoutParams(dp(52),dp(28)));
        row.setOnClickListener(v->showFontPreview(entry.style,entry.name,entry.pro));

        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(66));
        p.setMargins(0,dp(3),0,dp(3));
        page.addView(row,p);
    }

    private void applyColorFontText(TextView view,String text,int style,int fallback){
        if(!ColorFontCatalog.isColorStyle(style)){view.setText(text);view.setTextColor(fallback);return;}
        android.text.SpannableString span=new android.text.SpannableString(text);
        for(int i=0;i<text.length();i++)
            span.setSpan(new android.text.style.ForegroundColorSpan(
                    ColorFontCatalog.colorFor(style,String.valueOf(text.charAt(i))+i,fallback)),
                    i,i+1,android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        view.setText(span);
    }

    private void addRemoteFontSlice(LinearLayout page,int start,int count){
        int end=Math.min(RemoteFontCatalog.ITEMS.length,start+count);
        for(int i=Math.max(0,start);i<end;i++)addRemoteFontStoreCard(page,RemoteFontCatalog.ITEMS[i]);
    }

    private void addCurrentlySelectedFontCard(LinearLayout page){
        int style=prefs.getInt("keyboard_font_style",0);
        if(ColorFontCatalog.isColorStyle(style)){addColorFontStoreCard(page,ColorFontCatalog.find(style));return;}
        if(style>=RemoteFontCatalog.FIRST_STYLE){
            RemoteFontCatalog.Entry entry=RemoteFontCatalog.find(style);
            if(entry!=null){addRemoteFontStoreCard(page,entry);return;}
        }
        boolean pro=(style>=100&&style<=123)||(style>=136&&style<=151);
        addFontStoreCard(page,style,keyboardFontName(),"Currently selected keyboard alphabet",pro);
    }

    private void addFontStoreCard(
            LinearLayout page,
            int style,
            String name,
            String description,
            boolean pro
    ) {
        boolean selected=
                prefs.getInt("keyboard_font_style",0)==style;

        LinearLayout row=new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16),dp(7),dp(10),dp(7));

        int surface;
        switch(Math.abs(style)%5) {
            case 0:
                surface=Color.rgb(248,249,244);
                break;
            case 1:
                surface=Color.rgb(250,247,240);
                break;
            case 2:
                surface=Color.rgb(247,249,245);
                break;
            case 3:
                surface=Color.rgb(250,245,246);
                break;
            default:
                surface=Color.rgb(248,246,250);
                break;
        }

        if(selected)
            surface=Color.rgb(247,239,252);

        GradientDrawable bg=round(surface,13);
        bg.setStroke(
                dp(selected ? 2 : 1),
                selected
                        ? Color.rgb(181,143,204)
                        : Color.rgb(238,232,227)
        );
        row.setBackground(bg);

        LinearLayout words=new LinearLayout(this);
        words.setOrientation(LinearLayout.VERTICAL);
        words.setGravity(Gravity.CENTER);
        words.setPadding(0,0,dp(8),0);

        TextView sample=new TextView(this);
        sample.setText(name);
        sample.setTextColor(TEXT);
        sample.setTextSize(fontDisplayTextSize(style));
        sample.setGravity(Gravity.CENTER);
        sample.setSingleLine(true);
        sample.setTypeface(settingsKeyboardTypeface(style));

        TextView sub=new TextView(this);
        sub.setText(description);
        sub.setTextColor(MUTED);
        sub.setTextSize(9);
        sub.setGravity(Gravity.CENTER);
        sub.setSingleLine(true);

        words.addView(
                sample,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(32)
                )
        );
        words.addView(
                sub,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(16)
                )
        );

        row.addView(
                words,
                new LinearLayout.LayoutParams(
                        0,
                        dp(50),
                        1f
                )
        );

        TextView badge=new TextView(this);
        badge.setText(
                selected
                        ? "✓"
                        : (fontNeedsAdAccess(style,pro)
                            ? (hasAdAccess() ? "24H" : (pro ? "PRO" : "AD"))
                            : "FREE")
        );
        badge.setTextSize(selected ? 18 : 10);
        badge.setGravity(Gravity.CENTER);
        badge.setTextColor(
                selected
                        ? Color.rgb(114,79,133)
                        : (
                            pro
                                ? Color.rgb(184,95,115)
                                : Color.rgb(80,145,96)
                          )
        );

        GradientDrawable badgeBg=
                round(
                        selected
                                ? Color.rgb(239,224,248)
                                : (
                                    pro
                                        ? Color.rgb(253,239,240)
                                        : Color.rgb(233,247,235)
                                  ),
                        11
                );

        badge.setBackground(badgeBg);

        row.addView(
                badge,
                new LinearLayout.LayoutParams(
                        dp(52),
                        dp(28)
                )
        );

        row.setOnClickListener(v ->
                showFontPreview(style,name,pro)
        );

        LinearLayout.LayoutParams rp=
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(66)
                );
        rp.setMargins(0,dp(3),0,dp(3));
        page.addView(row,rp);
    }


    private java.io.File remoteFontFile(int style) {
        java.io.File dir=new java.io.File(getFilesDir(),"keykii_fonts");
        if(!dir.exists()) dir.mkdirs();
        return new java.io.File(dir,style+".ttf");
    }

    private String remoteFontUrl(RemoteFontCatalog.Entry entry) {
        return "https://raw.githubusercontent.com/google/fonts/" +
                RemoteFontCatalog.GOOGLE_FONTS_REV + "/" +
                android.net.Uri.encode(entry.path,"/");
    }

    private boolean downloadRemoteFont(RemoteFontCatalog.Entry entry) {
        java.io.File target=remoteFontFile(entry.style);
        if(target.exists() && target.length()>1024) return true;

        java.io.File part=new java.io.File(target.getAbsolutePath()+".part");
        java.net.HttpURLConnection conn=null;

        try {
            java.net.URL url=new java.net.URL(remoteFontUrl(entry));
            conn=(java.net.HttpURLConnection)url.openConnection();
            conn.setConnectTimeout(12000);
            conn.setReadTimeout(20000);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("User-Agent","KeyKii-Neo/2.51");

            int code=conn.getResponseCode();
            if(code<200 || code>=300) return false;

            java.io.InputStream in=conn.getInputStream();
            java.io.FileOutputStream out=new java.io.FileOutputStream(part);
            byte[] buffer=new byte[8192];
            int read;
            long total=0;

            while((read=in.read(buffer))!=-1) {
                total+=read;
                if(total>12000000L) {
                    out.close();
                    in.close();
                    part.delete();
                    return false;
                }
                out.write(buffer,0,read);
            }

            out.flush();
            out.close();
            in.close();

            if(part.length()<1024) {
                part.delete();
                return false;
            }

            if(target.exists()) target.delete();
            return part.renameTo(target);

        } catch(Exception ignored) {
            part.delete();
            return false;

        } finally {
            if(conn!=null) conn.disconnect();
        }
    }

    private void queueRemoteFontCardPreview(
            RemoteFontCatalog.Entry entry,
            TextView sample
    ) {
        java.io.File file=remoteFontFile(entry.style);

        if(file.exists()) {
            try {
                sample.setTypeface(Typeface.createFromFile(file));
                sample.setAlpha(1f);
            } catch(Exception ignored) {}
            return;
        }

        sample.setAlpha(.62f);
        sample.setTag(entry.style);

        fontDownloadExecutor.execute(() -> {
            boolean ok=downloadRemoteFont(entry);
            if(!ok) return;

            runOnUiThread(() -> {
                Object tag=sample.getTag();
                if(!(tag instanceof Integer) || ((Integer)tag)!=entry.style)
                    return;

                try {
                    sample.setTypeface(
                            Typeface.createFromFile(remoteFontFile(entry.style))
                    );
                    sample.setAlpha(1f);
                } catch(Exception ignored) {}
            });
        });
    }

    private void openRemoteFontPreview(RemoteFontCatalog.Entry entry) {
        java.io.File file=remoteFontFile(entry.style);

        if(file.exists()) {
            showFontPreview(entry.style,entry.name,entry.pro);
            return;
        }

        toast("Downloading "+entry.name+" preview…");

        new Thread(() -> {
            boolean ok=downloadRemoteFont(entry);

            runOnUiThread(() -> {
                if(ok) {
                    showFontPreview(entry.style,entry.name,entry.pro);
                } else {
                    toast("Could not download this font. Check your internet connection.");
                }
            });
        },"KeyKii-Font-Preview").start();
    }

    private void addRemoteFontCollection(LinearLayout page) {
        int count=Math.min(remoteFontShownCount,RemoteFontCatalog.ITEMS.length);

        for(int i=0;i<count;i++)
            addRemoteFontStoreCard(page,RemoteFontCatalog.ITEMS[i]);

        if(count<RemoteFontCatalog.ITEMS.length) {
            TextView more=textButton(
                    "Show 24 more  •  "+count+"/"+RemoteFontCatalog.ITEMS.length
            );
            more.setTextSize(14);
            more.setOnClickListener(v -> {
                remoteFontShownCount=Math.min(
                        RemoteFontCatalog.ITEMS.length,
                        remoteFontShownCount+24
                );
                renderFontCategoryContent();
            });

            LinearLayout.LayoutParams mp=
                    new LinearLayout.LayoutParams(-1,dp(52));
            mp.setMargins(0,dp(8),0,dp(10));
            page.addView(more,mp);
        }
    }

    private void addRemoteFontStoreCard(
            LinearLayout page,
            RemoteFontCatalog.Entry entry
    ) {
        boolean selected=
                prefs.getInt("keyboard_font_style",0)==entry.style;

        LinearLayout row=new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16),dp(7),dp(10),dp(7));

        int surface;
        switch(entry.style%5) {
            case 0: surface=Color.rgb(248,249,244); break;
            case 1: surface=Color.rgb(250,247,240); break;
            case 2: surface=Color.rgb(247,249,245); break;
            case 3: surface=Color.rgb(250,245,246); break;
            default: surface=Color.rgb(248,246,250); break;
        }

        if(selected) surface=Color.rgb(247,239,252);

        GradientDrawable bg=round(surface,13);
        bg.setStroke(
                dp(selected ? 2 : 1),
                selected ? Color.rgb(181,143,204) : Color.rgb(238,232,227)
        );
        row.setBackground(bg);

        LinearLayout words=new LinearLayout(this);
        words.setOrientation(LinearLayout.VERTICAL);
        words.setGravity(Gravity.CENTER);
        words.setPadding(0,0,dp(8),0);

        TextView sample=new TextView(this);
        sample.setText(entry.name);
        sample.setTextColor(TEXT);
        sample.setTextSize(20);
        sample.setGravity(Gravity.CENTER);
        sample.setSingleLine(true);
        sample.setTypeface(settingsKeyboardTypeface(entry.style));

        TextView sub=new TextView(this);
        sub.setText(
                hasAdAccess()
                        ? "24h access active • tap for live preview"
                        : (entry.pro
                            ? "Pro font • watch ad for 24h access"
                            : "Free with rewarded ad • 24h access")
        );
        sub.setTextColor(MUTED);
        sub.setTextSize(9);
        sub.setGravity(Gravity.CENTER);
        sub.setSingleLine(true);

        words.addView(sample,new LinearLayout.LayoutParams(-1,dp(32)));
        words.addView(sub,new LinearLayout.LayoutParams(-1,dp(16)));

        row.addView(words,new LinearLayout.LayoutParams(0,dp(50),1f));

        TextView badge=new TextView(this);
        badge.setText(selected ? "✓" : (hasAdAccess() ? "24H" : (entry.pro ? "PRO" : "AD")));
        badge.setTextSize(selected ? 18 : 10);
        badge.setGravity(Gravity.CENTER);
        badge.setTextColor(
                selected
                        ? Color.rgb(114,79,133)
                        : (entry.pro
                            ? Color.rgb(184,95,115)
                            : Color.rgb(80,145,96))
        );
        badge.setBackground(
                round(
                        selected
                                ? Color.rgb(239,224,248)
                                : (entry.pro
                                    ? Color.rgb(253,239,240)
                                    : Color.rgb(233,247,235)),
                        11
                )
        );

        row.addView(badge,new LinearLayout.LayoutParams(dp(52),dp(28)));
        row.setOnClickListener(v -> openRemoteFontPreview(entry));

        LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(66));
        rp.setMargins(0,dp(3),0,dp(3));
        page.addView(row,rp);

        queueRemoteFontCardPreview(entry,sample);
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
        title.setTextSize(fontDisplayTextSize(style)+2);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(settingsKeyboardTypeface(style));
        applyColorFontText(title,name,style,TEXT);
        sheet.addView(title,new LinearLayout.LayoutParams(-1,dp(48)));

        TextView sample=new TextView(this);
        sample.setTextSize(style==106 ? 13 : 20);
        sample.setGravity(Gravity.CENTER);
        sample.setTypeface(settingsKeyboardTypeface(style));
        applyColorFontText(sample,"The quick brown fox  •  Aa Bb Cc 123",style,TEXT);
        sheet.addView(sample,new LinearLayout.LayoutParams(-1,dp(58)));

        sheet.addView(
                buildFontKeyboardPreview(style),
                new LinearLayout.LayoutParams(-1,dp(210))
        );

        final boolean adGated=fontNeedsAdAccess(style,pro);

        if(adGated) {
            TextView note=new TextView(this);
            note.setText(
                    hasAdAccess()
                            ? "✓ 24-hour access active • "+KeyKiiAccess.remainingLabel(this)
                            : "Watch one rewarded ad to use PRO/ad fonts and PRO themes for 24 hours"
            );
            note.setTextColor(hasAdAccess()?Color.rgb(62,139,82):Color.rgb(169,92,181));
            note.setTextSize(10);
            note.setGravity(Gravity.CENTER);
            sheet.addView(note,new LinearLayout.LayoutParams(-1,dp(38)));
        }

        LinearLayout actions=new LinearLayout(this);
        actions.setGravity(Gravity.CENTER);

        TextView cancel=textButton("Cancel");
        TextView apply=textButton(adGated&&!hasAdAccess() ? "Watch ad • 24h" : "Apply font");
        cancel.setTextSize(16);
        apply.setTextSize(16);

        cancel.setOnClickListener(v -> dialog.dismiss());

        apply.setOnClickListener(v -> {
            Runnable applyFont=() -> {
                prefs.edit()
                        .putInt("keyboard_font_style",style)
                        .apply();

                toast(name+" applied to KeyKii");
                dialog.dismiss();
                renderFontCategoryContent();
            };

            if(adGated&&!hasAdAccess()){
                request24HourAdAccess(applyFont);
                return;
            }

            applyFont.run();
            if(ads!=null)ads.recordNaturalBreak();
        });

        LinearLayout.LayoutParams bp=
                new LinearLayout.LayoutParams(0,dp(56),1f);
        bp.setMargins(dp(4),dp(8),dp(4),0);

        actions.addView(cancel,bp);
        actions.addView(apply,bp);
        sheet.addView(actions);

        dialog.setContentView(sheet);
        dialog.setCancelable(true);

        android.view.Window window=
                dialog.getWindow();

        if(window!=null) {
            window.setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(
                            Color.TRANSPARENT
                    )
            );

            window.setDimAmount(.35f);
            window.addFlags(
                    android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND
            );

            android.view.WindowManager.LayoutParams lp=
                    window.getAttributes();

            lp.width=
                    ViewGroup.LayoutParams.MATCH_PARENT;

            lp.height=
                    ViewGroup.LayoutParams.WRAP_CONTENT;

            lp.gravity=Gravity.BOTTOM;

            window.setAttributes(lp);
        }

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
                typeface,
                style
        );
        addFontPreviewRow(
                keyboard,
                new String[]{"a","s","d","f","g","h","j","k","l"},
                typeface,
                style
        );
        addFontPreviewRow(
                keyboard,
                new String[]{"⇧","z","x","c","v","b","n","m","⌫"},
                typeface,
                style
        );
        addFontPreviewRow(
                keyboard,
                new String[]{"?123",",","KeyKii",".","↵"},
                typeface,
                style
        );

        return keyboard;
    }


    private void addFontPreviewRow(
            LinearLayout parent,
            String[] values,
            Typeface typeface,
            int style
    ) {
        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.CENTER);

        for(String value:values) {
            TextView key=new TextView(this);
            key.setText(value);
            key.setTextColor(ColorFontCatalog.colorFor(style,value,Color.WHITE));
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


    private void addThemeFilterBar(LinearLayout page) {
        themeFilterChips.clear();

        android.widget.HorizontalScrollView scroll=
                new android.widget.HorizontalScrollView(this);
        themeFilterScroll=scroll;
        scroll.setHorizontalScrollBarEnabled(false);
        scroll.setFillViewport(false);

        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(2),dp(3),dp(2),dp(7));

        addThemeFilterChip(row,"All","all");
        addThemeFilterChip(row,"New","new");
        addThemeFilterChip(row,"Free","free");
        addThemeFilterChip(row,"Pro","pro");
        addThemeFilterChip(row,"Cute","cute");
        addThemeFilterChip(row,"Kawaii","kawaii");
        addThemeFilterChip(row,"Aesthetic","aesthetic");
        addThemeFilterChip(row,"Anime-inspired","anime-inspired");
        addThemeFilterChip(row,"Dreamy","dreamy");
        addThemeFilterChip(row,"Dark","dark");
        addThemeFilterChip(row,"Gaming","gaming");
        addThemeFilterChip(row,"Nature","nature");
        addThemeFilterChip(row,"Floral","floral");
        addThemeFilterChip(row,"Food","food");
        addThemeFilterChip(row,"Retro","retro");
        addThemeFilterChip(row,"Y2K","y2k");
        addThemeFilterChip(row,"Minimal","minimal");
        addThemeFilterChip(row,"Luxury","luxury");
        addThemeFilterChip(row,"Space","space");
        addThemeFilterChip(row,"Ocean","ocean");
        addThemeFilterChip(row,"City","city");
        addThemeFilterChip(row,"Seasonal","seasonal");

        scroll.addView(row);
        page.addView(scroll,new LinearLayout.LayoutParams(-1,dp(58)));
        scrollThemeFilterToActive(false);
    }

    private void addThemeFilterChip(
            LinearLayout row,
            String label,
            String mode
    ) {
        TextView chip=new TextView(this);
        chip.setText(ui(label));
        chip.setTag(mode);
        chip.setTextSize(12);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(dp(18),0,dp(18),0);

        themeFilterChips.add(chip);
        styleThemeFilterChip(chip,mode.equals(themeBrowseMode));

        chip.setOnClickListener(v -> {
            if(mode.equals(themeBrowseMode)) {
                scrollThemeFilterToActive(true);
                return;
            }

            themeBrowseMode=mode;
            neoThemeShownCount=NeoThemeCatalog.PAGE_SIZE;

            for(TextView item:themeFilterChips) {
                Object tag=item.getTag();
                styleThemeFilterChip(
                        item,
                        tag!=null && mode.equals(tag.toString())
                );
            }

            scrollThemeFilterToActive(true);

            if(themePreviewSheet!=null)
                themePreviewSheet.setVisibility(View.GONE);

            renderThemeCategoryContent();
        });

        LinearLayout.LayoutParams p=
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        dp(42)
                );
        p.setMargins(dp(3),0,dp(3),0);
        row.addView(chip,p);
    }

    private void styleThemeFilterChip(
            TextView chip,
            boolean selected
    ) {
        chip.setTextColor(
                selected
                        ? Color.rgb(122,66,151)
                        : Color.rgb(88,80,92)
        );
        chip.setTypeface(
                Typeface.DEFAULT,
                selected
                        ? Typeface.BOLD
                        : Typeface.NORMAL
        );

        GradientDrawable bg=round(
                selected
                        ? Color.rgb(249,226,247)
                        : Color.WHITE,
                20
        );
        bg.setStroke(
                dp(selected ? 2 : 1),
                selected
                        ? Color.rgb(202,137,215)
                        : Color.rgb(232,226,235)
        );
        chip.setBackground(bg);
    }

    private void scrollThemeFilterToActive(boolean smooth) {
        if(themeFilterScroll==null)
            return;

        themeFilterScroll.post(() -> {
            TextView selected=null;

            for(TextView chip:themeFilterChips) {
                Object tag=chip.getTag();
                if(
                        tag!=null &&
                        themeBrowseMode.equals(tag.toString())
                ) {
                    selected=chip;
                    break;
                }
            }

            if(selected==null)
                return;

            int target=Math.max(
                    0,
                    selected.getLeft()-dp(18)
            );

            if(smooth)
                themeFilterScroll.smoothScrollTo(target,0);
            else
                themeFilterScroll.scrollTo(target,0);
        });
    }

    private void renderThemeCategoryContent() {
        if(themeCategoryContent==null)
            return;

        final int generation=++themeRenderGeneration;

        themeCategoryContent.removeAllViews();
        themeSelectableTiles.clear();
        themeCategoryContent.setAlpha(1f);

        final int totalNew=
                NeoThemeCatalog.count(
                        themeBrowseMode
                );

        final int[] visibleNew=
                NeoThemeCatalog.first(
                        themeBrowseMode,
                        neoThemeShownCount
                );

        Runnable legacyAndTools=() -> {
            if(generation!=themeRenderGeneration)
                return;

            int[] legacy=
                    legacyThemePacksForMode(
                            themeBrowseMode
                    );

            if(legacy.length>0) {
                addThemeSection(
                        themeCategoryContent,
                        "CURRENT FAVORITES • "+legacy.length
                );

                addStylePackGridIncremental(
                        themeCategoryContent,
                        legacy,
                        generation,
                        () -> addThemeFineTuneIfNeeded(generation)
                );
            } else {
                addThemeFineTuneIfNeeded(generation);
            }
        };

        if(totalNew>0) {
            addThemeSection(themeCategoryContent,"✦ NEW THEME COLLECTION • "+totalNew+" THEMES");
            LinearLayout newGrid=new LinearLayout(this);
            newGrid.setOrientation(LinearLayout.VERTICAL);
            themeCategoryContent.addView(newGrid);
            TextView more=textButton("Show more themes");
            more.setTextSize(13);
            more.setVisibility(View.GONE);
            themeCategoryContent.addView(more,new LinearLayout.LayoutParams(-1,dp(52)));
            Runnable updateMore=() -> {
                if(generation!=themeRenderGeneration)return;
                more.setText("Show more themes  •  "+neoThemeShownCount+" / "+totalNew);
                more.setEnabled(true);
                more.setVisibility(neoThemeShownCount<totalNew?View.VISIBLE:View.GONE);
            };
            more.setOnClickListener(v -> {
                if(generation!=themeRenderGeneration)return;
                more.setEnabled(false);
                int[] next=NeoThemeCatalog.page(themeBrowseMode,neoThemeShownCount,NeoThemeCatalog.PAGE_SIZE);
                neoThemeShownCount=Math.min(totalNew,neoThemeShownCount+next.length);
                addStylePackGridIncremental(newGrid,next,generation,updateMore);
            });
            addStylePackGridIncremental(newGrid,visibleNew,generation,() -> {
                if(generation!=themeRenderGeneration)return;
                neoThemeShownCount=visibleNew.length;
                updateMore.run();legacyAndTools.run();
            });
            return;
        }

        legacyAndTools.run();
    }


    private int[] legacyThemePacksForMode(
            String mode
    ) {
        if("new".equals(mode))
            return new int[0];

        if("free".equals(mode))
            return new int[]{100,102,106,108,113,117,120,122,126,128,133,137};

        if("pro".equals(mode))
            return new int[]{
                    101,103,104,105,107,109,110,111,112,114,115,116,
                    118,119,121,123,124,125,127,129,130,131,132,134,
                    135,136,138,139
            };

        if("cute".equals(mode))
            return new int[]{
                    100,102,103,104,106,108,109,110,113,114,
                    117,120,122,123,127,131,133,134,136,137
            };

        if("aesthetic".equals(mode))
            return new int[]{
                    100,103,104,110,115,116,117,120,124,130,
                    131,133,135,139
            };

        if("dreamy".equals(mode))
            return new int[]{
                    101,107,110,112,116,119,121,124,125,128,
                    129,133,138
            };

        if("dark".equals(mode))
            return new int[]{105,107,118,125,132,139};

        if("nature".equals(mode))
            return new int[]{102,108,111,115,116,122,126,129,130,133,135};

        if("gaming".equals(mode))
            return new int[]{105};

        if("retro".equals(mode))
            return new int[]{105,118,132,137,139};

        if("minimal".equals(mode))
            return new int[]{115,117,130,135};

        if("luxury".equals(mode))
            return new int[]{115,116,118,135,139};

        if("space".equals(mode))
            return new int[]{101,105,107,112,121,125,138};

        if("ocean".equals(mode))
            return new int[]{101,112,121,125,129};

        if("food".equals(mode))
            return new int[]{102,103,106,109,111,113,114,122,123,127,131,132,134,136,137};

        if("seasonal".equals(mode))
            return new int[]{100,106,111,119,120,128,131};

        int[] all=new int[40];

        for(int i=0;i<40;i++)
            all[i]=100+i;

        return all;
    }


    private void addThemeFineTuneIfNeeded(
            int generation
    ) {
        if(generation!=themeRenderGeneration)
            return;

        if(!"all".equals(themeBrowseMode))
            return;

        addThemeSection(
                themeCategoryContent,
                "My themes"
        );

        addMyThemeTiles(
                themeCategoryContent
        );

        addThemeSection(
                themeCategoryContent,
                "Fine tune"
        );

        addChoiceRow(
                themeCategoryContent,
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
                themeCategoryContent,
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
                55,
                this::updateThemePreview
        );

        addChoiceRow(
                themeCategoryContent,
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
                this::updateThemePreview
        );
    }

    private void addStylePackGridIncremental(
            LinearLayout page,
            int[] packs,
            int generation,
            Runnable onComplete
    ) {
        if(packs==null || packs.length==0) {
            if(onComplete!=null) onComplete.run();
            return;
        }

        final int[] index={0};

        Runnable addNextRow=new Runnable() {
            @Override
            public void run() {
                if(generation!=themeRenderGeneration)
                    return;

                if(index[0]>=packs.length) {
                    refreshThemeTileSelection();
                    if(onComplete!=null)
                        onComplete.run();
                    return;
                }

                LinearLayout row=new LinearLayout(SettingsActivity.this);
                row.setOrientation(LinearLayout.HORIZONTAL);

                addStylePackTileToRow(
                        row,
                        createStylePackTile(packs[index[0]])
                );
                index[0]++;

                if(index[0]<packs.length) {
                    addStylePackTileToRow(
                            row,
                            createStylePackTile(packs[index[0]])
                    );
                    index[0]++;
                } else {
                    addStylePackSpacer(row);
                }

                page.addView(row);
                refreshThemeTileSelection();

                page.postDelayed(this,8);
            }
        };

        page.post(addNextRow);
    }


    private void addThemeStoreHero(
            LinearLayout page
    ) {
        LinearLayout hero=
                new LinearLayout(this);

        hero.setOrientation(
                LinearLayout.VERTICAL
        );

        hero.setPadding(
                dp(18),
                dp(17),
                dp(18),
                dp(16)
        );

        GradientDrawable bg=
                new GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT,
                        new int[]{
                            Color.rgb(246,221,255),
                            Color.rgb(255,225,239),
                            Color.rgb(224,242,255)
                        }
                );

        bg.setCornerRadius(dp(25));
        hero.setBackground(bg);

        TextView title=
                new TextView(this);

        title.setText(
                "✨ KeyKii Theme Shop"
        );

        title.setTextColor(TEXT);
        title.setTextSize(20);
        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        hero.addView(title);

        TextView sub=
                new TextView(this);

        sub.setText(
                "Explore 512 new theme compositions and your 40 original favorites. FREE themes stay free, while PRO themes now show their launch price before you preview them."
        );

        sub.setTextColor(MUTED);
        sub.setTextSize(12);
        sub.setPadding(
                0,
                dp(5),
                0,
                0
        );

        hero.addView(sub);

        LinearLayout.LayoutParams p=
                new LinearLayout.LayoutParams(
                        -1,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        p.setMargins(
                0,
                dp(2),
                0,
                dp(6)
        );

        page.addView(hero,p);
    }

    private void addThemePricingCard(LinearLayout page) {
        LinearLayout card=new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16),dp(13),dp(16),dp(13));
        GradientDrawable bg=round(Color.WHITE,20);
        bg.setStroke(dp(1),Color.rgb(229,217,236));
        card.setBackground(bg);

        TextView title=new TextView(this);
        title.setText("Launch pricing");
        title.setTextColor(TEXT);
        title.setTextSize(15);
        title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        card.addView(title);

        TextView prices=new TextView(this);
        prices.setText("PRO themes ₱19–₱29  •  5-theme pack ₱79\nCategory pack ₱99  •  Lifetime PRO ₱249\nOr watch 1 rewarded ad for 24h access");
        prices.setTextColor(Color.rgb(126,78,151));
        prices.setTextSize(12);
        prices.setPadding(0,dp(4),0,0);
        card.addView(prices);

        TextView note=new TextView(this);
        note.setText("Preview first. Rewarded access covers PRO themes and ad-gated fonts for 24 hours. Purchases will use Google Play when billing is connected.");
        note.setTextColor(MUTED);
        note.setTextSize(10);
        note.setPadding(0,dp(4),0,0);
        card.addView(note);

        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(0,dp(2),0,dp(5));
        page.addView(card,p);
    }


    private boolean hasAdAccess(){
        return KeyKiiAccess.canUsePremium(this);
    }

    private boolean fontNeedsAdAccess(int style,boolean pro){
        return pro||KeyKiiAccess.fontNeedsAccess(style);
    }

    private void request24HourAdAccess(Runnable afterUnlock){
        if(hasAdAccess()){
            if(afterUnlock!=null)afterUnlock.run();
            return;
        }
        if(ads==null){
            toast("Ads are not ready yet. Try again in a moment.");
            return;
        }
        toast("Loading rewarded ad…");
        ads.showRewardedFor24Hours(
                () -> {
                    toast("Unlocked PRO themes + fonts for 24 hours ✨");
                    if(afterUnlock!=null)afterUnlock.run();
                },
                () -> toast("Ad was not completed or is unavailable. No access was changed.")
        );
    }

    private void addAdAccessCard(LinearLayout page){
        LinearLayout card=new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16),dp(13),dp(16),dp(13));

        boolean active=hasAdAccess();
        GradientDrawable bg=round(
                active ? Color.rgb(235,249,239) : Color.rgb(251,242,255),
                20
        );
        bg.setStroke(dp(1),active ? Color.rgb(164,211,175) : Color.rgb(224,199,238));
        card.setBackground(bg);

        TextView title=new TextView(this);
        title.setText(active ? "✓ 24-hour access active" : "▶ Watch ad • Unlock for 24 hours");
        title.setTextColor(active ? Color.rgb(54,126,75) : Color.rgb(137,72,169));
        title.setTextSize(14);
        title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        card.addView(title);

        TextView sub=new TextView(this);
        sub.setText(
                active
                        ? KeyKiiAccess.remainingLabel(this)+" • PRO themes + ad-gated fonts"
                        : "Complete one rewarded ad to use PRO themes and ad-gated fonts free for 24 hours."
        );
        sub.setTextColor(MUTED);
        sub.setTextSize(10);
        sub.setPadding(0,dp(4),0,0);
        card.addView(sub);

        if(!active){
            TextView button=textButton(ads!=null&&ads.isTestMode() ? "Watch TEST ad • 24h" : "Watch ad • 24h");
            button.setTextSize(13);
            button.setOnClickListener(v -> request24HourAdAccess(() -> {
                if("theme".equals(screen))showTheme();
                else if("fonts".equals(screen))showFonts();
            }));
            LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(46));
            bp.setMargins(0,dp(8),0,0);
            card.addView(button,bp);
        }

        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,ViewGroup.LayoutParams.WRAP_CONTENT);
        cp.setMargins(0,dp(4),0,dp(8));
        page.addView(card,cp);
    }


    private void addStylePackGrid(
            LinearLayout page,
            int[] packs
    ) {
        for(int index=0;index<packs.length;index+=2) {
            LinearLayout row=
                    new LinearLayout(this);

            row.setOrientation(
                    LinearLayout.HORIZONTAL
            );

            addStylePackTileToRow(
                    row,
                    createStylePackTile(
                            packs[index]
                    )
            );

            if(index+1<packs.length) {
                addStylePackTileToRow(
                        row,
                        createStylePackTile(
                                packs[index+1]
                        )
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

        p.setMargins(
                dp(4),
                dp(5),
                dp(4),
                dp(5)
        );

        row.addView(tile,p);
    }


    private void addStylePackSpacer(
            LinearLayout row
    ) {
        View spacer=
                new View(this);

        LinearLayout.LayoutParams p=
                new LinearLayout.LayoutParams(
                        0,
                        1,
                        1f
                );

        p.setMargins(
                dp(4),
                0,
                dp(4),
                0
        );

        row.addView(spacer,p);
    }


    private LinearLayout createStylePackTile(
            int pack
    ) {
        LinearLayout tile=
                new LinearLayout(this);

        tile.setOrientation(
                LinearLayout.VERTICAL
        );

        tile.setPadding(
                dp(7),
                dp(7),
                dp(7),
                dp(9)
        );

        GradientDrawable cardBg=
                round(
                        Color.WHITE,
                        20
                );

        cardBg.setStroke(
                dp(1),
                BORDER
        );

        tile.setBackground(cardBg);
        tile.setElevation(dp(1));

        tile.addView(
                buildStylePackMiniKeyboard(
                        pack,
                        false
                ),
                new LinearLayout.LayoutParams(
                        -1,
                        dp(132)
                )
        );

        TextView name=
                new TextView(this);

        name.setText(
                (KeyKiiThemeCollection.isFavorite(this,pack) ? "♥ " : "")+
                stylePackName(pack)
        );

        name.setTextColor(TEXT);
        name.setTextSize(13);
        name.setGravity(Gravity.CENTER);
        name.setMaxLines(1);

        tile.addView(
                name,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(25)
                )
        );

        TextView badge=
                new TextView(this);

        badge.setText(
                stylePackPro(pack)
                        ? (hasAdAccess() ? "✓ 24H ACCESS" : stylePackPriceLabel(pack)+" • PRO")
                        : "FREE"
        );

        badge.setTextColor(
                stylePackPro(pack)
                        ? Color.rgb(149,76,177)
                        : Color.rgb(58,131,82)
        );

        badge.setTextSize(10);
        badge.setGravity(Gravity.CENTER);

        tile.addView(
                badge,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(20)
                )
        );

        tile.setOnClickListener(
                v -> showStylePackPreview(pack)
        );

        return tile;
    }


    private LinearLayout buildStylePackMiniKeyboard(
            int pack,
            boolean large
    ) {
        if(NeoThemeCatalog.isNeoPack(pack)) {
            LinearLayout host=new LinearLayout(this);
            host.setOrientation(LinearLayout.VERTICAL);
            host.addView(new NeoThemePreview(this,pack,
                    settingsKeyboardTypeface(prefs.getInt("keyboard_font_style",0)),
                    prefs.getBoolean("number_row",false)),
                    new LinearLayout.LayoutParams(-1,-1));
            return host;
        }

        int[] spec=
                stylePackSpec(pack);

        int start=spec[0];
        int end=spec[1];
        int accent=spec[2];
        int corner=Math.min(
                spec[3],
                large ? 10 : 8
        );
        boolean borders=spec[5]==1;
        boolean dark=spec[6]==0;
        int decor=spec[7];

        LinearLayout keyboard=
                new LinearLayout(this);

        keyboard.setOrientation(
                LinearLayout.VERTICAL
        );

        keyboard.setPadding(
                dp(large ? 7 : 4),
                dp(large ? 7 : 4),
                dp(large ? 7 : 4),
                dp(large ? 7 : 4)
        );

        GradientDrawable panelBg=
                new GradientDrawable(
                        GradientDrawable.Orientation.TL_BR,
                        new int[]{start,end}
                );

        panelBg.setCornerRadius(
                dp(large ? 20 : 14)
        );

        android.graphics.drawable.Drawable background=
                panelBg;

        if(NeoThemeCatalog.isNeoPack(pack)) {
            background=
                    new android.graphics.drawable.LayerDrawable(
                            new android.graphics.drawable.Drawable[]{
                                panelBg,
                                new NeoThemeDrawable(this,pack)
                            }
                    );
        } else if(IllustratedThemeAssets.isNewPack(pack)) {
            int bgRes=
                    IllustratedThemeAssets.background(pack);

            if(bgRes!=0) {
                android.graphics.drawable.Drawable assetBg=
                        getDrawable(bgRes);

                if(assetBg!=null) {
                    assetBg=assetBg.mutate();

                    background=
                            new android.graphics.drawable.LayerDrawable(
                                    new android.graphics.drawable.Drawable[]{
                                        panelBg,
                                        assetBg
                                    }
                            );
                }
            }
        } else if(decor>0) {
            android.graphics.drawable.Drawable themeArt=
                    pack==116
                            ? new ThemePreviewDecorDrawable(
                                    decor,
                                    accent,
                                    dark,
                                    pack
                              )
                            : new ThemeSceneDrawable(
                                    this,
                                    pack,
                                    accent,
                                    dark
                              );

            background=
                    new android.graphics.drawable.LayerDrawable(
                            new android.graphics.drawable.Drawable[]{
                                panelBg,
                                themeArt
                            }
                    );
        }

        keyboard.setBackground(background);

        Typeface currentFont=
                settingsKeyboardTypeface(
                        prefs.getInt(
                                "keyboard_font_style",
                                0
                        )
                );

        String[][] rows=
                large
                        ? new String[][]{
                            {"1","2","3","4","5","6","7","8","9","0"},
                            {"q","w","e","r","t","y","u","i","o","p"},
                            {"a","s","d","f","g","h","j","k","l"},
                            {"⇧","z","x","c","v","b","n","m","⌫"},
                            {"123","☺",",","KeyKii",".","↵"}
                        }
                        : new String[][]{
                            {"q","w","e","r","t","y","u","i","o","p"},
                            {"a","s","d","f","g","h","j","k","l"},
                            {"⇧","z","x","c","v","b","n","m","⌫"},
                            {"123","☺",",","KeyKii",".","↵"}
                        };

        int rowHeight=
                large
                        ? dp(39)
                        : dp(29);

        int keyHeight=
                large
                        ? dp(35)
                        : dp(25);

        for(int r=0;r<rows.length;r++) {
            LinearLayout row=
                    new LinearLayout(this);

            row.setGravity(Gravity.CENTER);

            String[] values=rows[r];

            for(int k=0;k<values.length;k++) {
                String value=values[k];

                TextView key=
                        new TextView(this);

                key.setText(value);
                key.setGravity(Gravity.CENTER);

                key.setTextSize(
                        large
                                ? (
                                    value.length()>2
                                            ? 8
                                            : 11
                                )
                                : (
                                    value.length()>2
                                            ? 5
                                            : 7
                                )
                );

                key.setTypeface(currentFont);

                boolean special=
                        value.equals("⇧") ||
                        value.equals("⌫") ||
                        value.equals("123") ||
                        value.equals("↵") ||
                        value.equals("KeyKii");

                key.setTextColor(
                        dark
                                ? Color.WHITE
                                : Color.rgb(53,53,57)
                );

                key.setBackground(
                        previewThemeKeyBackground(
                                spec[8],
                                accent,
                                corner,
                                dark,
                                special,
                                value.equals("KeyKii"),
                                borders,
                                spec[4],
                                pack
                        )
                );

                int stickerRes=
                        previewKeyStickerRes(
                                pack,
                                value
                        );

                if(stickerRes!=0) {
                    android.graphics.drawable.Drawable sticker=
                            getDrawable(stickerRes);

                    if(sticker!=null) {
                        sticker=sticker.mutate();
                        sticker.setTint(accent);

                        int stickerSize=
                                dp(
                                    large
                                        ? 9
                                        : 6
                                );

                        sticker.setBounds(
                                0,0,
                                stickerSize,
                                stickerSize
                        );

                        key.setCompoundDrawablePadding(
                                large
                                    ? dp(1)
                                    : 0
                        );

                        key.setCompoundDrawables(
                                null,
                                sticker,
                                null,
                                null
                        );
                    }
                }

                float weight=
                        value.equals("KeyKii")
                                ? 3.6f
                                : (
                                    value.equals("123") ||
                                    value.equals("↵")
                                        ? 1.35f
                                        : 1f
                                  );

                LinearLayout.LayoutParams kp=
                        new LinearLayout.LayoutParams(
                                0,
                                keyHeight,
                                weight
                        );

                kp.setMargins(
                        dp(1),
                        dp(1),
                        dp(1),
                        dp(1)
                );

                row.addView(key,kp);
            }

            keyboard.addView(
                    row,
                    new LinearLayout.LayoutParams(
                            -1,
                            rowHeight
                    )
            );
        }

        // Asset-pack artwork is deliberately behind the keys. This keeps
        // labels readable while still giving the board a full wallpaper /
        // sticker-kit look like the reference keyboards.
        keyboard.setForeground(null);

        return keyboard;
    }


    private void showStylePackPreview(
            int pack
    ) {
        final android.app.Dialog dialog=
                new android.app.Dialog(this);

        LinearLayout sheet=
                new LinearLayout(this);

        sheet.setOrientation(
                LinearLayout.VERTICAL
        );

        sheet.setPadding(
                dp(16),
                dp(15),
                dp(16),
                dp(18)
        );

        sheet.setBackground(
                round(
                        Color.WHITE,
                        26
                )
        );

        TextView title=
                new TextView(this);

        title.setText(
                stylePackName(pack)
        );

        title.setTextColor(TEXT);
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);

        sheet.addView(
                title,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(40)
                )
        );

        TextView badge=
                new TextView(this);

        badge.setText(
                stylePackPro(pack)
                        ? (hasAdAccess()
                            ? "✓ 24H ACCESS ACTIVE"
                            : "KEYKII PRO • "+stylePackPriceLabel(pack)+" • OR WATCH AD")
                        : "FREE THEME"
        );

        badge.setTextColor(
                stylePackPro(pack)
                        ? Color.rgb(149,76,177)
                        : Color.rgb(58,131,82)
        );

        badge.setTextSize(11);
        badge.setGravity(Gravity.CENTER);

        sheet.addView(
                badge,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(26)
                )
        );

        sheet.addView(
                buildStylePackMiniKeyboard(
                        pack,
                        true
                ),
                new LinearLayout.LayoutParams(
                        -1,
                        dp(228)
                )
        );

        TextView sub=
                new TextView(this);

        sub.setText(
                stylePackSubtitle(pack)+
                (stylePackPro(pack)
                        ? (hasAdAccess()
                            ? "\n24-hour access active • Apply now"
                            : "\nWatch one rewarded ad to unlock all PRO themes + ad-gated fonts for 24h")
                        : "\nFree theme • Uses your current font")
        );

        sub.setTextColor(MUTED);
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);

        sheet.addView(
                sub,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(52)
                )
        );

        LinearLayout buttons=
                new LinearLayout(this);

        TextView cancel=
                textButton("Cancel");

        TextView favorite=
                textButton(
                        KeyKiiThemeCollection.isFavorite(this,pack)
                                ? "♥ Saved"
                                : "♡ Save"
                );

        TextView apply=
                textButton(
                        stylePackPro(pack)
                                ? (hasAdAccess() ? "Apply" : "Watch ad • 24h")
                                : "Apply"
                );

        cancel.setTextSize(14);
        favorite.setTextSize(14);
        apply.setTextSize(14);

        cancel.setOnClickListener(
                v -> dialog.dismiss()
        );

        favorite.setOnClickListener(v -> {
            boolean saved=KeyKiiThemeCollection.toggleFavorite(this,pack);
            favorite.setText(saved ? "♥ Saved" : "♡ Save");
            toast(saved ? "Saved to Mine" : "Removed from favorites");
        });

        apply.setOnClickListener(v -> {
            if(stylePackPro(pack)&&!hasAdAccess()){
                request24HourAdAccess(() -> {
                    applyKeyboardStylePack(pack);
                    dialog.dismiss();
                    renderThemeCategoryContent();
                });
                return;
            }

            applyKeyboardStylePack(pack);
            dialog.dismiss();
            if(ads!=null)ads.recordNaturalBreak();
        });

        LinearLayout.LayoutParams p=
                new LinearLayout.LayoutParams(
                        0,
                        dp(56),
                        1f
                );

        p.setMargins(
                dp(4),
                dp(8),
                dp(4),
                0
        );

        buttons.addView(cancel,p);
        buttons.addView(favorite,p);
        buttons.addView(apply,p);
        sheet.addView(buttons);

        dialog.setContentView(sheet);
        dialog.setCancelable(true);

        android.view.Window window=
                dialog.getWindow();

        if(window!=null) {
            window.setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(
                            Color.TRANSPARENT
                    )
            );

            window.setDimAmount(.35f);
            window.addFlags(
                    android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND
            );

            android.view.WindowManager.LayoutParams lp=
                    window.getAttributes();

            lp.width=
                    ViewGroup.LayoutParams.MATCH_PARENT;

            lp.height=
                    ViewGroup.LayoutParams.WRAP_CONTENT;

            lp.gravity=Gravity.BOTTOM;

            window.setAttributes(lp);
        }

        dialog.show();
    }


    private void applyKeyboardStylePack(
            int pack
    ) {
        int[] spec=
                stylePackSpec(pack);

        prefs.edit()
                .putInt(
                        "theme_surface_mode",
                        2
                )
                .putInt(
                        "theme",
                        spec[6]
                )
                .putInt(
                        "theme_custom_start",
                        spec[0]
                )
                .putInt(
                        "theme_custom_end",
                        spec[1]
                )
                .putInt(
                        "accent_color",
                        spec[2]
                )
                .putInt(
                        "key_corner_radius",
                        spec[3]
                )
                .putInt(
                        "theme_transparency",
                        spec[4]
                )
                .putBoolean(
                        "theme_key_borders",
                        spec[5]==1
                )
                .putBoolean(
                        "photo_key_borders",
                        spec[5]==1
                )
                .putInt(
                        "theme_decor_style",
                        spec[7]
                )
                .putInt(
                        "theme_key_style",
                        spec[8]
                )
                .putBoolean(
                        "theme_auto_day_night",
                        false
                )
                .putInt(
                        "keykii_style_pack",
                        pack
                )
                .apply();

        KeyKiiThemeCollection.rememberApplied(this,pack);

        toast(
                stylePackName(pack)+
                " theme applied"
        );

        showTheme();
    }


    private android.graphics.drawable.Drawable previewThemeKeyBackground(
            int style,
            int accent,
            int corner,
            boolean dark,
            boolean special,
            boolean wideKey,
            boolean borders,
            int transparency,
            int pack
    ) {
        if(NeoThemeCatalog.isNeoPack(pack)) {
            return NeoThemeKeyDrawable.state(
                    this,
                    pack,
                    special,
                    wideKey
            );
        }

        if(IllustratedThemeAssets.isNewPack(pack)) {
            int res=
                    wideKey
                        ? IllustratedThemeAssets.space(pack)
                        : (
                            special
                                ? IllustratedThemeAssets.special(pack)
                                : IllustratedThemeAssets.key(pack)
                          );

            if(res!=0) {
                android.graphics.drawable.Drawable exact=
                        getDrawable(res);

                if(exact!=null)
                    return exact.mutate();
            }
        }

        int white=Color.WHITE;
        int black=Color.rgb(10,10,14);

        int start;
        int end;
        int stroke;
        int strokeWidth=1;

        switch(style) {
            case 1:
                start=special || wideKey
                        ? Color.argb(
                            205,
                            Color.red(accent),
                            Color.green(accent),
                            Color.blue(accent)
                        )
                        : Color.argb(190,12,14,28);

                end=Color.argb(
                        150,
                        Color.red(accent),
                        Color.green(accent),
                        Color.blue(accent)
                );

                stroke=accent;
                strokeWidth=2;
                break;

            case 2:
                start=Color.argb(
                        special || wideKey
                                ? 175
                                : 112,
                        255,255,255
                );

                end=Color.argb(
                        special || wideKey
                                ? 110
                                : 62,
                        Color.red(accent),
                        Color.green(accent),
                        Color.blue(accent)
                );

                stroke=Color.argb(215,255,255,255);
                break;

            case 3:
                start=blendPreviewColor(
                        accent,
                        white,
                        special || wideKey
                                ? 42
                                : 72
                );

                end=blendPreviewColor(
                        accent,
                        white,
                        special || wideKey
                                ? 20
                                : 54
                );

                stroke=Color.WHITE;
                strokeWidth=2;
                break;

            case 4:
                start=special || wideKey
                        ? accent
                        : Color.rgb(18,18,24);

                end=Color.rgb(4,4,7);
                stroke=accent;
                strokeWidth=2;
                break;

            case 5:
                start=special || wideKey
                        ? blendPreviewColor(
                            accent,
                            black,
                            28
                        )
                        : Color.rgb(25,22,26);

                end=Color.rgb(4,4,7);
                stroke=accent;
                strokeWidth=2;
                break;

            case 6:
                start=Color.rgb(235,249,255);
                end=blendPreviewColor(
                        accent,
                        white,
                        62
                );
                stroke=Color.WHITE;
                strokeWidth=2;
                break;

            case 7:
                start=Color.argb(
                        special || wideKey
                                ? 215
                                : 175,
                        27,18,58
                );

                end=Color.argb(
                        180,
                        Color.red(accent),
                        Color.green(accent),
                        Color.blue(accent)
                );

                stroke=accent;
                strokeWidth=2;
                break;

            case 8:
                start=blendPreviewColor(
                        accent,
                        Color.rgb(255,235,246),
                        special || wideKey
                                ? 35
                                : 67
                );

                end=blendPreviewColor(
                        accent,
                        white,
                        special || wideKey
                                ? 18
                                : 52
                );

                stroke=Color.WHITE;
                strokeWidth=2;
                break;

            default:
                start=dark
                        ? Color.argb(
                            borders ? 150 : 85,
                            255,255,255
                        )
                        : Color.argb(
                            borders ? 225 : 140,
                            255,255,255
                        );
                end=start;
                stroke=borders
                        ? (
                            dark
                                    ? Color.argb(75,255,255,255)
                                    : Color.argb(55,60,60,68)
                        )
                        : Color.TRANSPARENT;
        }

        // The 19 non-Lotus aesthetic themes use the same light visual
        // weight as the approved Lotus reference: translucent fills and a
        // single thin outline. Purple Lotus (116) is intentionally untouched.
        if(pack>=100 && pack<=119 && pack!=116) {
            int percent=
                    Math.max(
                            45,
                            Math.min(100, transparency)
                    );

            start=scalePreviewColorAlpha(start,percent);
            end=scalePreviewColorAlpha(end,percent);

            if(stroke!=Color.TRANSPARENT) {
                stroke=scalePreviewColorAlpha(
                        stroke,
                        Math.min(72,percent+12)
                );
            }

            strokeWidth=1;
        }

        GradientDrawable bg=
                new GradientDrawable(
                        GradientDrawable.Orientation.TL_BR,
                        new int[]{start,end}
                );

        bg.setCornerRadius(
                dp(
                    Math.max(
                        5,
                        corner
                    )
                )
        );

        if(stroke!=Color.TRANSPARENT) {
            bg.setStroke(
                    dp(strokeWidth),
                    stroke
            );
        }

        return bg;
    }


    private int scalePreviewColorAlpha(
            int color,
            int percent
    ) {
        int p=Math.max(0,Math.min(100,percent));
        int sourceAlpha=Color.alpha(color);

        if(sourceAlpha==0 && color!=Color.TRANSPARENT)
            sourceAlpha=255;

        return Color.argb(
                Math.max(
                        0,
                        Math.min(
                                255,
                                Math.round(sourceAlpha*p/100f)
                        )
                ),
                Color.red(color),
                Color.green(color),
                Color.blue(color)
        );
    }


    private int blendPreviewColor(
            int a,
            int b,
            int bPercent
    ) {
        int p=Math.max(
                0,
                Math.min(
                        100,
                        bPercent
                )
        );

        int ap=100-p;

        return Color.rgb(
                (
                    Color.red(a)*ap+
                    Color.red(b)*p
                )/100,
                (
                    Color.green(a)*ap+
                    Color.green(b)*p
                )/100,
                (
                    Color.blue(a)*ap+
                    Color.blue(b)*p
                )/100
        );
    }


    private int[] stylePackSpec(
            int pack
    ) {
        int[] neo=NeoThemeCatalog.spec(pack);
        if(neo!=null)
            return neo;

        switch(pack) {
            case 100:
                return new int[]{
                        Color.rgb(255,184,211),
                        Color.rgb(255,235,244),
                        Color.rgb(230,90,145),
                        12,55,1,1,20,8
                };

            case 101:
                return new int[]{
                        Color.rgb(124,147,255),
                        Color.rgb(208,218,255),
                        Color.rgb(92,111,234),
                        12,55,1,1,21,6
                };

            case 102:
                return new int[]{
                        Color.rgb(203,231,179),
                        Color.rgb(244,247,211),
                        Color.rgb(91,150,75),
                        13,55,1,1,22,3
                };

            case 103:
                return new int[]{
                        Color.rgb(255,195,175),
                        Color.rgb(255,235,226),
                        Color.rgb(210,113,87),
                        12,55,1,1,23,3
                };

            case 104:
                return new int[]{
                        Color.rgb(215,191,255),
                        Color.rgb(244,233,255),
                        Color.rgb(135,91,194),
                        12,55,1,1,24,3
                };

            case 105:
                return new int[]{
                        Color.rgb(37,13,72),
                        Color.rgb(5,8,24),
                        Color.rgb(218,52,255),
                        8,55,1,0,25,1
                };

            case 106:
                return new int[]{
                        Color.rgb(255,171,204),
                        Color.rgb(255,233,241),
                        Color.rgb(229,80,132),
                        13,55,1,1,26,8
                };

            case 107:
                return new int[]{
                        Color.rgb(89,110,201),
                        Color.rgb(209,221,255),
                        Color.rgb(117,132,220),
                        12,55,1,1,27,6
                };

            case 108:
                return new int[]{
                        Color.rgb(185,229,181),
                        Color.rgb(236,249,225),
                        Color.rgb(72,151,83),
                        13,55,1,1,28,3
                };

            case 109:
                return new int[]{
                        Color.rgb(226,184,173),
                        Color.rgb(248,224,218),
                        Color.rgb(171,105,90),
                        12,55,1,1,29,3
                };

            case 110:
                return new int[]{
                        Color.rgb(210,191,250),
                        Color.rgb(248,238,255),
                        Color.rgb(135,93,190),
                        12,55,1,1,30,3
                };

            case 111:
                return new int[]{
                        Color.rgb(255,183,204),
                        Color.rgb(255,232,225),
                        Color.rgb(225,80,126),
                        12,55,1,1,31,8
                };

            case 112:
                return new int[]{
                        Color.rgb(83,171,246),
                        Color.rgb(185,230,255),
                        Color.rgb(50,143,226),
                        12,55,1,1,32,6
                };

            case 113:
                return new int[]{
                        Color.rgb(126,84,62),
                        Color.rgb(205,164,133),
                        Color.rgb(188,128,84),
                        11,55,1,0,33,5
                };

            case 114:
                return new int[]{
                        Color.rgb(255,173,211),
                        Color.rgb(255,231,244),
                        Color.rgb(226,80,150),
                        13,55,1,1,34,8
                };

            case 115:
                return new int[]{
                        Color.rgb(203,220,255),
                        Color.rgb(247,250,255),
                        Color.rgb(66,108,181),
                        10,55,1,1,35,6
                };

            case 116:
                return new int[]{
                        Color.rgb(219,202,246),
                        Color.rgb(247,239,255),
                        Color.rgb(127,83,171),
                        10,96,1,1,36,2
                };

            case 117:
                return new int[]{
                        Color.rgb(241,229,215),
                        Color.rgb(255,249,242),
                        Color.rgb(168,126,98),
                        10,55,1,1,37,3
                };

            case 118:
                return new int[]{
                        Color.rgb(46,32,28),
                        Color.rgb(8,7,7),
                        Color.rgb(204,142,83),
                        9,55,1,0,38,5
                };

            case 119:
                return new int[]{
                        Color.rgb(210,235,255),
                        Color.rgb(245,239,255),
                        Color.rgb(224,88,119),
                        10,55,1,1,39,6
                };

            case 120: return new int[]{Color.rgb(255,212,229),Color.rgb(255,247,251),Color.rgb(228,93,149),14,55,1,1,0,8};
            case 121: return new int[]{Color.rgb(111,130,232),Color.rgb(221,230,255),Color.rgb(86,110,217),16,55,1,1,0,6};
            case 122: return new int[]{Color.rgb(220,239,185),Color.rgb(247,245,220),Color.rgb(101,155,85),18,55,1,1,0,3};
            case 123: return new int[]{Color.rgb(255,202,182),Color.rgb(255,241,232),Color.rgb(217,125,103),20,55,1,1,0,3};
            case 124: return new int[]{Color.rgb(220,199,244),Color.rgb(250,242,255),Color.rgb(138,98,190),10,55,1,1,0,2};
            case 125: return new int[]{Color.rgb(52,77,152),Color.rgb(184,200,247),Color.rgb(111,114,217),18,55,1,0,0,7};
            case 126: return new int[]{Color.rgb(191,232,189),Color.rgb(238,248,222),Color.rgb(85,151,91),14,55,1,1,0,3};
            case 127: return new int[]{Color.rgb(231,192,188),Color.rgb(249,231,226),Color.rgb(173,108,104),16,55,1,1,0,3};
            case 128: return new int[]{Color.rgb(207,234,255),Color.rgb(249,242,255),Color.rgb(216,90,131),14,55,1,1,0,6};
            case 129: return new int[]{Color.rgb(103,191,229),Color.rgb(215,244,255),Color.rgb(51,143,192),16,55,1,1,0,6};
            case 130: return new int[]{Color.rgb(241,235,216),Color.rgb(251,248,238),Color.rgb(139,159,114),11,55,1,1,0,2};
            case 131: return new int[]{Color.rgb(255,192,212),Color.rgb(255,240,246),Color.rgb(227,83,130),14,55,1,1,0,8};
            case 132: return new int[]{Color.rgb(142,96,72),Color.rgb(213,174,137),Color.rgb(181,122,82),16,55,1,1,0,3};
            case 133: return new int[]{Color.rgb(216,198,241),Color.rgb(248,240,255),Color.rgb(137,96,185),10,55,1,1,0,2};
            case 134: return new int[]{Color.rgb(255,183,208),Color.rgb(255,240,247),Color.rgb(232,94,141),18,55,1,1,0,8};
            case 135: return new int[]{Color.rgb(215,229,248),Color.rgb(252,254,255),Color.rgb(69,111,175),9,55,1,1,0,2};
            case 136: return new int[]{Color.rgb(255,208,226),Color.rgb(255,244,248),Color.rgb(229,94,154),20,55,1,1,0,8};
            case 137: return new int[]{Color.rgb(217,179,143),Color.rgb(242,220,197),Color.rgb(155,105,70),16,55,1,1,0,3};
            case 138: return new int[]{Color.rgb(185,162,230),Color.rgb(238,231,255),Color.rgb(122,99,182),14,55,1,1,0,2};
            case 139: return new int[]{Color.rgb(46,32,25),Color.rgb(10,9,9),Color.rgb(209,160,94),10,55,1,0,0,5};

            default:
                return stylePackSpec(100);
        }
    }


    private String stylePackName(
            int pack
    ) {
        NeoThemeCatalog.Entry neo=NeoThemeCatalog.get(pack);
        if(neo!=null)
            return neo.name;

        switch(pack) {
            case 100: return "Cherry Blossom Love";
            case 101: return "Blueberry Jelly Sky";
            case 102: return "Matcha Bunny Café";
            case 103: return "Peach Teddy Dessert";
            case 104: return "Lilac Butterfly Diary";
            case 105: return "Midnight Neon Arcade";
            case 106: return "Strawberry Ribbon Milk";
            case 107: return "Cloudy Moon Sleep";
            case 108: return "Mint Frog Garden";
            case 109: return "Rosy Bear Picnic";
            case 110: return "Lavender Lace Dream";
            case 111: return "Sakura Cherry Soda";
            case 112: return "Ocean Jelly Star";
            case 113: return "Cozy Cocoa Bunny";
            case 114: return "Pink Kitty Bow";
            case 115: return "Blue Porcelain Bloom";
            case 116: return "Purple Lotus Watercolor";
            case 117: return "Cream Heart Minimal";
            case 118: return "Brown Butterfly Noir";
            case 119: return "Snowy Pastel Christmas";
            case 120: return "Sakura Ribbon Picnic";
            case 121: return "Blueberry Dream Clouds";
            case 122: return "Bunny Matcha Garden";
            case 123: return "Peach Cream Pudding";
            case 124: return "Violet Lace Letter";
            case 125: return "Moonlight Jelly Sleep";
            case 126: return "Mint Frog Forest";
            case 127: return "Rosy Teddy Bakery";
            case 128: return "Snow Gift Wonderland";
            case 129: return "Ocean Pearl Jelly";
            case 130: return "Daisy Milk Garden";
            case 131: return "Cherry Soda Bow";
            case 132: return "Cozy Cocoa Bunny";
            case 133: return "Butterfly Diary Bloom";
            case 134: return "Strawberry Candy Milk";
            case 135: return "Porcelain Blossom Blue";
            case 136: return "Pastel Kitty Dessert";
            case 137: return "Caramel Bear Café";
            case 138: return "Lilac Star Pajama";
            case 139: return "Golden Noir Butterfly";
            default: return "Aesthetic Theme";
        }
    }


    private String stylePackSubtitle(
            int pack
    ) {
        NeoThemeCatalog.Entry neo=NeoThemeCatalog.get(pack);
        if(neo!=null)
            return neo.subtitle;

        switch(pack) {
            case 100: return "Cherry blossoms, ribbons and soft love details";
            case 101: return "Blue jelly sky with clouds, stars and moon";
            case 102: return "Matcha café greens with bunny and leaf accents";
            case 103: return "Peach dessert palette with teddy details";
            case 104: return "Lilac diary style with butterflies and lace mood";
            case 105: return "Dark arcade keyboard with electric neon accents";
            case 106: return "Strawberry milk pink with bows and hearts";
            case 107: return "Sleepy clouds, moon and dreamy night stars";
            case 108: return "Fresh mint garden with frog and leaf details";
            case 109: return "Rosy picnic browns with bears, ribbons and hearts";
            case 110: return "Lavender lace with butterflies and tiny blooms";
            case 111: return "Sakura pink with cherry soda accents";
            case 112: return "Ocean blue jelly look with bubbles and stars";
            case 113: return "Warm cocoa café with bunny and bear details";
            case 114: return "Cute pink cat-bow keyboard with glossy keys";
            case 115: return "Blue porcelain floral keyboard with elegant details";
            case 116: return "Purple lotus watercolor with butterfly accents";
            case 117: return "Cream minimal keyboard with hearts and bows";
            case 118: return "Dark brown noir with gold butterfly details";
            case 119: return "Snowy pastel holiday keyboard with winter details";
            case 120: return "Sakura wallpaper with striped picnic texture, blossoms, bows and falling petals";
            case 121: return "Dreamy blue sky wallpaper with clouds, moon, stars and blueberry clusters";
            case 122: return "Matcha garden wallpaper with bunny stickers, café cups, leaves and soft checks";
            case 123: return "Peach pudding palette with teddy, cake, strawberry and tiny heart details";
            case 124: return "Lilac diary-paper theme with lace edges, ribbons, flowers and sticker-tape details";
            case 125: return "Sleepy blue night with moon, clouds, tiny stars and floating jellyfish";
            case 126: return "Mint forest grid with frog characters, leaves and small daisy accents";
            case 127: return "Rosy bakery checks with teddy stickers, ribbons and cookie details";
            case 128: return "Pastel winter wallpaper with snow, gifts and soft snowflake decoration";
            case 129: return "Ocean wallpaper with waves, jellyfish, bubbles, shells and pearl tones";
            case 130: return "Cream-and-mint garden with daisies, milk-carton stickers and light plaid";
            case 131: return "Cherry soda stripes with bows, cherries, bubbles and café drink details";
            case 132: return "Warm cocoa café wallpaper with bunny, cup, cookies and chocolate checks";
            case 133: return "Lilac scrapbook grid with sticker tape, butterflies, bows and flower stamps";
            case 134: return "Strawberry milk wallpaper with candy stripes, berries, bows and milk details";
            case 135: return "Blue porcelain paper with painted floral branches and fine tile lines";
            case 136: return "Cute original kitty dessert wallpaper with paws, cake, bows and pink checks";
            case 137: return "Caramel café wallpaper with bear stickers, coffee, cookies and warm plaid";
            case 138: return "Lilac sleepover wallpaper with stars, moon, clouds and pajama-check details";
            case 139: return "Dark boutique wallpaper with thin gold framing, stars, moon and butterfly bows";
            default: return "Illustrated KeyKii aesthetic theme";
        }
    }


    private boolean stylePackPro(
            int pack
    ) {
        NeoThemeCatalog.Entry neo=NeoThemeCatalog.get(pack);
        if(neo!=null)
            return neo.pro;

        switch(pack) {
            case 100:
            case 102:
            case 106:
            case 108:
            case 113:
            case 117:
            case 120:
            case 122:
            case 126:
            case 128:
            case 133:
            case 137:
                return false;

            default:
                return pack>=100 && pack<=139;
        }
    }

    private int stylePackPricePeso(int pack) {
        if(!stylePackPro(pack)) return 0;
        NeoThemeCatalog.Entry neo=NeoThemeCatalog.get(pack);
        if(neo!=null) return NeoThemeCatalog.pricePeso(pack);
        return NeoThemeCatalog.PRICE_REGULAR;
    }

    private String stylePackPriceLabel(int pack) {
        int price=stylePackPricePeso(pack);
        return price<=0 ? "FREE" : "₱"+price;
    }


    private int previewThemePrimaryAssetRes(
            int pack
    ) {
        switch(pack) {
            case 100: return R.drawable.theme_motif_flower;
            case 101: return R.drawable.theme_motif_moonstar;
            case 102: return R.drawable.theme_motif_bunny;
            case 103: return R.drawable.theme_motif_bear;
            case 104: return R.drawable.theme_motif_butterfly;
            case 105: return R.drawable.theme_motif_star;
            case 106: return R.drawable.theme_motif_strawberry;
            case 107: return R.drawable.theme_motif_cloud;
            case 108: return R.drawable.theme_motif_frog;
            case 109: return R.drawable.theme_motif_bear;
            case 110: return R.drawable.theme_motif_butterfly;
            case 111: return R.drawable.theme_motif_flower;
            case 112: return R.drawable.theme_motif_bubble;
            case 113: return R.drawable.theme_motif_bunny;
            case 114: return R.drawable.theme_motif_cat;
            case 115: return R.drawable.theme_motif_flower;
            case 116: return R.drawable.theme_motif_lotus;
            case 117: return R.drawable.theme_motif_heart;
            case 118: return R.drawable.theme_motif_butterfly;
            case 119: return R.drawable.theme_motif_snow;
        }

        return 0;
    }


    private int previewThemeSecondaryAssetRes(
            int pack
    ) {
        switch(pack) {
            case 100: return R.drawable.theme_motif_cherry;
            case 101: return R.drawable.theme_motif_cloud;
            case 102: return R.drawable.theme_motif_leaf;
            case 103: return R.drawable.theme_motif_cake;
            case 104: return R.drawable.theme_motif_bow;
            case 105: return R.drawable.theme_motif_moonstar;
            case 106: return R.drawable.theme_motif_bow;
            case 107: return R.drawable.theme_motif_moonstar;
            case 108: return R.drawable.theme_motif_leaf;
            case 109: return R.drawable.theme_motif_bow;
            case 110: return R.drawable.theme_motif_flower;
            case 111: return R.drawable.theme_motif_cherry;
            case 112: return R.drawable.theme_motif_star;
            case 113: return R.drawable.theme_motif_bear;
            case 114: return R.drawable.theme_motif_bow;
            case 115: return R.drawable.theme_motif_lotus;
            case 116: return R.drawable.theme_motif_butterfly;
            case 117: return R.drawable.theme_motif_bow;
            case 118: return R.drawable.theme_motif_star;
            case 119: return R.drawable.theme_motif_gift;
        }

        return 0;
    }


    private int previewThemeTertiaryAssetRes(
            int pack
    ) {
        switch(pack) {
            case 100: return R.drawable.theme_motif_heart;
            case 101: return R.drawable.theme_motif_star;
            case 102: return R.drawable.theme_motif_cup;
            case 103: return R.drawable.theme_motif_heart;
            case 104: return R.drawable.theme_motif_flower;
            case 105: return R.drawable.theme_motif_bubble;
            case 106: return R.drawable.theme_motif_heart;
            case 107: return R.drawable.theme_motif_star;
            case 108: return R.drawable.theme_motif_flower;
            case 109: return R.drawable.theme_motif_heart;
            case 110: return R.drawable.theme_motif_bow;
            case 111: return R.drawable.theme_motif_cup;
            case 112: return R.drawable.theme_motif_moonstar;
            case 113: return R.drawable.theme_motif_cup;
            case 114: return R.drawable.theme_motif_heart;
            case 115: return R.drawable.theme_motif_star;
            case 116: return R.drawable.theme_motif_lotus;
            case 117: return R.drawable.theme_motif_flower;
            case 118: return R.drawable.theme_motif_heart;
            case 119: return R.drawable.theme_motif_bow;
        }

        return 0;
    }


    private int previewKeyStickerRes(
            int pack,
            String value
    ) {
        boolean left=
                value.equals("q") ||
                value.equals("1") ||
                value.equals("⇧");

        boolean right=
                value.equals("p") ||
                value.equals("0") ||
                value.equals("⌫");

        boolean center=
                value.equals("KeyKii") ||
                value.equals("5");

        if(
                !left &&
                !right &&
                !center
        ) {
            return 0;
        }

        if(IllustratedThemeAssets.isNewPack(pack)) {
            if(value.equals("⇧"))
                return IllustratedThemeAssets.primarySticker(pack);

            if(value.equals("⌫"))
                return IllustratedThemeAssets.secondarySticker(pack);

            if(value.equals("KeyKii"))
                return IllustratedThemeAssets.tertiarySticker(pack);

            return 0;
        }

        if(pack==116) {
            if(right)
                return R.drawable.theme_motif_butterfly;

            return R.drawable.theme_motif_lotus;
        }

        if(left)
            return previewThemePrimaryAssetRes(pack);

        if(right)
            return previewThemeSecondaryAssetRes(pack);

        return previewThemeTertiaryAssetRes(pack);
    }


    class ThemePreviewDecorDrawable
        extends android.graphics.drawable.Drawable {

        final int style;
        final int accent;
        final boolean dark;
        final int pack;

        final android.graphics.Paint paint=
                new android.graphics.Paint(
                        android.graphics.Paint.ANTI_ALIAS_FLAG
                );

        ThemePreviewDecorDrawable(
                int style,
                int accent,
                boolean dark,
                int pack
        ) {
            this.style=style;
            this.accent=accent;
            this.dark=dark;
            this.pack=pack;
        }


        @Override
        public void draw(
                android.graphics.Canvas canvas
        ) {
            android.graphics.Rect b=
                    getBounds();

            float w=b.width();
            float h=b.height();

            if(w<=0 || h<=0)
                return;

            int accentSoft=
                    Color.argb(
                            48,
                            Color.red(accent),
                            Color.green(accent),
                            Color.blue(accent)
                    );

            int whiteSoft=
                    Color.argb(
                            dark ? 48 : 88,
                            255,255,255
                    );

            paint.setStyle(
                    android.graphics.Paint.Style.FILL
            );

            if(
                    style>=20 &&
                    style<=39
            ) {
                if(pack==116) {
                    int res=
                            R.drawable.theme_motif_lotus;

                    android.graphics.drawable.Drawable motif=
                            SettingsActivity.this.getDrawable(res);

                    if(motif!=null) {
                        motif=motif.mutate();
                        motif.setTint(accent);
                        motif.setAlpha(180);

                        drawPreviewAsset(
                                canvas,
                                motif,
                                w*.03f,
                                h*.04f,
                                previewAssetSize(w,h,false)
                        );

                        drawPreviewAsset(
                                canvas,
                                motif,
                                w*.78f,
                                h*.04f,
                                previewAssetSize(w,h,true)
                        );

                        drawPreviewAsset(
                                canvas,
                                motif,
                                w*.72f,
                                h*.69f,
                                previewAssetSize(w,h,false)
                        );

                        return;
                    }
                }

                int primary=
                        previewThemePrimaryAssetRes(pack);

                int secondary=
                        previewThemeSecondaryAssetRes(pack);

                int tertiary=
                        previewThemeTertiaryAssetRes(pack);

                drawPreviewTintedAsset(
                        canvas,
                        primary,
                        accent,
                        dark ? 210 : 185,
                        w*.02f,
                        h*.04f,
                        previewAssetSize(w,h,false)
                );

                drawPreviewTintedAsset(
                        canvas,
                        secondary,
                        blendPreviewColor(
                                accent,
                                Color.WHITE,
                                dark ? 45 : 58
                        ),
                        dark ? 220 : 195,
                        w*.78f,
                        h*.03f,
                        previewAssetSize(w,h,true)
                );

                drawPreviewTintedAsset(
                        canvas,
                        tertiary,
                        blendPreviewColor(
                                accent,
                                Color.WHITE,
                                dark ? 20 : 35
                        ),
                        dark ? 200 : 175,
                        w*.70f,
                        h*.70f,
                        previewAssetSize(w,h,false)
                );

                drawSmallPreviewAccent(
                        canvas,
                        w,
                        h
                );

                return;
            }

            switch(style) {
                case 1:
                    paint.setColor(accentSoft);
                    previewHeart(canvas,w*.12f,h*.22f,dp(8));
                    previewHeart(canvas,w*.87f,h*.25f,dp(7));
                    previewHeart(canvas,w*.74f,h*.80f,dp(10));
                    break;

                case 2:
                    paint.setColor(whiteSoft);
                    previewSpark(canvas,w*.12f,h*.20f,dp(7));
                    previewSpark(canvas,w*.86f,h*.22f,dp(10));
                    previewSpark(canvas,w*.72f,h*.80f,dp(7));
                    break;

                case 3:
                    paint.setStyle(
                            android.graphics.Paint.Style.STROKE
                    );
                    paint.setStrokeWidth(dp(1));
                    paint.setColor(whiteSoft);
                    canvas.drawCircle(w*.11f,h*.22f,dp(10),paint);
                    canvas.drawCircle(w*.86f,h*.23f,dp(15),paint);
                    canvas.drawCircle(w*.72f,h*.80f,dp(12),paint);
                    paint.setStyle(
                            android.graphics.Paint.Style.FILL
                    );
                    break;

                case 4:
                    paint.setColor(accentSoft);
                    paint.setStrokeWidth(dp(2));
                    for(int i=0;i<5;i++) {
                        float y=h*(.12f+i*.19f);
                        canvas.drawLine(w*.02f,y,w*.20f,y-dp(8),paint);
                        canvas.drawLine(w*.80f,y+dp(6),w*.98f,y-dp(2),paint);
                    }
                    break;

                case 5:
                    paint.setColor(whiteSoft);
                    canvas.drawCircle(w*.10f,h*.18f,dp(25),paint);
                    canvas.drawCircle(w*.91f,h*.30f,dp(34),paint);
                    canvas.drawCircle(w*.63f,h*.88f,dp(38),paint);
                    break;

                case 6:
                    paint.setStrokeWidth(dp(2));
                    for(int i=0;i<10;i++) {
                        paint.setColor(
                                i%2==0
                                        ? accentSoft
                                        : whiteSoft
                        );
                        float x=w*(.05f+(i%5)*.22f);
                        float y=h*(i<5 ? .15f : .83f);
                        canvas.drawLine(x,y,x+dp(5),y+dp(7),paint);
                    }
                    break;

                case 7:
                    paint.setColor(whiteSoft);
                    previewStar(canvas,w*.12f,h*.20f,dp(8));
                    previewStar(canvas,w*.86f,h*.23f,dp(11));
                    previewStar(canvas,w*.72f,h*.80f,dp(7));
                    break;

                case 8:
                    paint.setColor(accentSoft);
                    for(int i=0;i<4;i++) {
                        float x=w*(.14f+i*.24f);
                        android.graphics.RectF oval=
                                new android.graphics.RectF(
                                        x-dp(4),
                                        h*.12f-dp(8),
                                        x+dp(4),
                                        h*.12f+dp(8)
                                );
                        canvas.drawOval(oval,paint);
                    }
                    break;

                case 9:
                    paint.setColor(whiteSoft);
                    previewSpark(canvas,w*.14f,h*.20f,dp(7));
                    previewSpark(canvas,w*.84f,h*.23f,dp(10));
                    break;

                case 10:
                    paint.setColor(accentSoft);
                    paint.setStrokeWidth(dp(5));
                    for(float x=-w*.10f;x<w*1.1f;x+=dp(30)) {
                        canvas.drawLine(x,h,x+w*.28f,0,paint);
                    }
                    break;

                case 11:
                    paint.setColor(whiteSoft);
                    paint.setStrokeWidth(dp(1));
                    float cx=w*.86f;
                    float cy=h*.19f;
                    for(int i=0;i<8;i++) {
                        double a=Math.PI*2*i/8.0;
                        canvas.drawLine(
                                cx,cy,
                                cx+(float)Math.cos(a)*dp(22),
                                cy+(float)Math.sin(a)*dp(22),
                                paint
                        );
                    }
                    break;

                case 20:
                case 26:
                case 31:
                    paint.setColor(accentSoft);
                    previewFlower(canvas,w*.10f,h*.19f,dp(8));
                    previewFlower(canvas,w*.88f,h*.22f,dp(10));
                    previewHeart(canvas,w*.75f,h*.80f,dp(7));
                    break;

                case 21:
                case 27:
                    paint.setColor(whiteSoft);
                    previewCloud(canvas,w*.12f,h*.20f,dp(9));
                    previewCloud(canvas,w*.84f,h*.22f,dp(11));
                    previewStar(canvas,w*.73f,h*.80f,dp(7));
                    break;

                case 22:
                case 28:
                    paint.setColor(accentSoft);
                    previewLeaf(canvas,w*.10f,h*.20f,dp(9));
                    previewLeaf(canvas,w*.88f,h*.22f,dp(10));
                    previewFlower(canvas,w*.75f,h*.80f,dp(7));
                    break;

                case 23:
                case 29:
                case 33:
                    paint.setColor(accentSoft);
                    previewBear(canvas,w*.12f,h*.20f,dp(9));
                    previewBear(canvas,w*.86f,h*.22f,dp(10));
                    previewHeart(canvas,w*.74f,h*.80f,dp(7));
                    break;

                case 24:
                case 30:
                case 36:
                case 38:
                    paint.setColor(accentSoft);
                    previewButterfly(canvas,w*.11f,h*.20f,dp(9));
                    previewButterfly(canvas,w*.87f,h*.23f,dp(10));
                    previewSpark(canvas,w*.74f,h*.80f,dp(6));
                    break;

                case 25:
                    paint.setColor(accentSoft);
                    previewSpark(canvas,w*.11f,h*.19f,dp(8));
                    previewStar(canvas,w*.87f,h*.22f,dp(10));
                    paint.setStrokeWidth(dp(2));
                    canvas.drawLine(w*.70f,h*.82f,w*.92f,h*.74f,paint);
                    break;

                case 32:
                    paint.setStyle(android.graphics.Paint.Style.STROKE);
                    paint.setStrokeWidth(dp(1));
                    paint.setColor(whiteSoft);
                    canvas.drawCircle(w*.12f,h*.20f,dp(9),paint);
                    canvas.drawCircle(w*.87f,h*.22f,dp(12),paint);
                    canvas.drawCircle(w*.75f,h*.80f,dp(7),paint);
                    paint.setStyle(android.graphics.Paint.Style.FILL);
                    break;

                case 34:
                    paint.setColor(accentSoft);
                    previewBow(canvas,w*.11f,h*.20f,dp(9));
                    previewBow(canvas,w*.87f,h*.22f,dp(10));
                    previewHeart(canvas,w*.75f,h*.80f,dp(7));
                    break;

                case 35:
                    paint.setColor(accentSoft);
                    previewFlower(canvas,w*.10f,h*.20f,dp(9));
                    previewFlower(canvas,w*.88f,h*.22f,dp(10));
                    previewFlower(canvas,w*.75f,h*.80f,dp(7));
                    break;

                case 37:
                    paint.setColor(accentSoft);
                    previewHeart(canvas,w*.11f,h*.20f,dp(8));
                    previewHeart(canvas,w*.88f,h*.22f,dp(9));
                    previewBow(canvas,w*.75f,h*.80f,dp(7));
                    break;

                case 39:
                    paint.setColor(whiteSoft);
                    previewSnow(canvas,w*.11f,h*.20f,dp(8));
                    previewSnow(canvas,w*.87f,h*.22f,dp(10));
                    previewSnow(canvas,w*.75f,h*.80f,dp(7));
                    break;
            }
        }


        void drawPreviewTintedAsset(
                android.graphics.Canvas canvas,
                int res,
                int tint,
                int alpha,
                float x,
                float y,
                int size
        ) {
            if(res==0)
                return;

            android.graphics.drawable.Drawable drawable=
                    SettingsActivity.this.getDrawable(res);

            if(drawable==null)
                return;

            drawable=drawable.mutate();
            drawable.setTint(tint);
            drawable.setAlpha(
                    Math.max(
                            0,
                            Math.min(
                                    255,
                                    alpha
                            )
                    )
            );

            drawPreviewAsset(
                    canvas,
                    drawable,
                    x,
                    y,
                    size
            );
        }


        void drawSmallPreviewAccent(
                android.graphics.Canvas canvas,
                float w,
                float h
        ) {
            if(pack==116)
                return;

            int soft=
                    blendPreviewColor(
                            accent,
                            Color.WHITE,
                            dark ? 38 : 58
                    );

            int a=0;
            int b=0;

            switch(pack) {
                case 100: a=R.drawable.theme_motif_flower; b=R.drawable.theme_motif_cherry; break;
                case 101: a=R.drawable.theme_motif_star; b=R.drawable.theme_motif_star; break;
                case 102: a=R.drawable.theme_motif_leaf; b=R.drawable.theme_motif_leaf; break;
                case 103: a=R.drawable.theme_motif_heart; b=R.drawable.theme_motif_cake; break;
                case 104: a=R.drawable.theme_motif_flower; b=R.drawable.theme_motif_butterfly; break;
                case 105: a=R.drawable.theme_motif_star; b=R.drawable.theme_motif_bubble; break;
                case 106: a=R.drawable.theme_motif_heart; b=R.drawable.theme_motif_strawberry; break;
                case 107: a=R.drawable.theme_motif_star; b=R.drawable.theme_motif_cloud; break;
                case 108: a=R.drawable.theme_motif_leaf; b=R.drawable.theme_motif_flower; break;
                case 109: a=R.drawable.theme_motif_bow; b=R.drawable.theme_motif_heart; break;
                case 110: a=R.drawable.theme_motif_flower; b=R.drawable.theme_motif_bow; break;
                case 111: a=R.drawable.theme_motif_cherry; b=R.drawable.theme_motif_cup; break;
                case 112: a=R.drawable.theme_motif_star; b=R.drawable.theme_motif_bubble; break;
                case 113: a=R.drawable.theme_motif_heart; b=R.drawable.theme_motif_cup; break;
                case 114: a=R.drawable.theme_motif_bow; b=R.drawable.theme_motif_heart; break;
                case 115: a=R.drawable.theme_motif_flower; b=R.drawable.theme_motif_lotus; break;
                case 117: a=R.drawable.theme_motif_heart; b=R.drawable.theme_motif_bow; break;
                case 118: a=R.drawable.theme_motif_star; b=R.drawable.theme_motif_butterfly; break;
                case 119: a=R.drawable.theme_motif_snow; b=R.drawable.theme_motif_gift; break;
            }

            if(a!=0) {
                drawPreviewTintedAsset(
                        canvas,
                        a,
                        soft,
                        dark ? 165 : 145,
                        w*.30f,
                        h*.03f,
                        Math.max(dp(12),previewAssetSize(w,h,false)*2/3)
                );
            }

            if(b!=0) {
                drawPreviewTintedAsset(
                        canvas,
                        b,
                        accent,
                        dark ? 175 : 150,
                        w*.60f,
                        h*.76f,
                        Math.max(dp(13),previewAssetSize(w,h,false)*2/3)
                );
            }
        }


        int previewAssetSize(
                float w,
                float h,
                boolean hero
        ) {
            float base=Math.min(w,h);

            return Math.max(
                    dp(18),
                    Math.round(
                            base*
                            (
                                hero
                                    ? .25f
                                    : .19f
                            )
                    )
            );
        }


        void drawPreviewAsset(
                android.graphics.Canvas canvas,
                android.graphics.drawable.Drawable drawable,
                float x,
                float y,
                int size
        ) {
            int left=Math.round(x);
            int top=Math.round(y);

            drawable.setBounds(
                    left,
                    top,
                    left+size,
                    top+size
            );

            drawable.draw(canvas);
        }


        void previewFlower(
                android.graphics.Canvas canvas,
                float cx,
                float cy,
                float size
        ) {
            for(int i=0;i<5;i++) {
                double a=
                        -Math.PI/2+
                        i*Math.PI*2/5;

                canvas.drawCircle(
                        cx+(float)Math.cos(a)*size*.55f,
                        cy+(float)Math.sin(a)*size*.55f,
                        size*.36f,
                        paint
                );
            }

            canvas.drawCircle(
                    cx,cy,
                    size*.23f,
                    paint
            );
        }


        void previewCloud(
                android.graphics.Canvas canvas,
                float cx,
                float cy,
                float size
        ) {
            canvas.drawCircle(cx-size*.45f,cy,size*.42f,paint);
            canvas.drawCircle(cx,cy-size*.18f,size*.56f,paint);
            canvas.drawCircle(cx+size*.46f,cy,size*.38f,paint);
            canvas.drawRect(
                    cx-size*.76f,
                    cy,
                    cx+size*.78f,
                    cy+size*.34f,
                    paint
            );
        }


        void previewLeaf(
                android.graphics.Canvas canvas,
                float cx,
                float cy,
                float size
        ) {
            android.graphics.RectF oval=
                    new android.graphics.RectF(
                            cx-size*.40f,
                            cy-size,
                            cx+size*.40f,
                            cy+size
                    );

            canvas.save();
            canvas.rotate(-32,cx,cy);
            canvas.drawOval(oval,paint);
            canvas.restore();
        }


        void previewBear(
                android.graphics.Canvas canvas,
                float cx,
                float cy,
                float size
        ) {
            canvas.drawCircle(cx,cy,size*.66f,paint);
            canvas.drawCircle(cx-size*.52f,cy-size*.48f,size*.30f,paint);
            canvas.drawCircle(cx+size*.52f,cy-size*.48f,size*.30f,paint);
        }


        void previewButterfly(
                android.graphics.Canvas canvas,
                float cx,
                float cy,
                float size
        ) {
            canvas.drawOval(
                    new android.graphics.RectF(
                            cx-size,
                            cy-size*.65f,
                            cx-size*.08f,
                            cy+size*.22f
                    ),
                    paint
            );

            canvas.drawOval(
                    new android.graphics.RectF(
                            cx+size*.08f,
                            cy-size*.65f,
                            cx+size,
                            cy+size*.22f
                    ),
                    paint
            );

            canvas.drawRect(
                    cx-size*.06f,
                    cy-size*.36f,
                    cx+size*.06f,
                    cy+size*.55f,
                    paint
            );
        }


        void previewBow(
                android.graphics.Canvas canvas,
                float cx,
                float cy,
                float size
        ) {
            android.graphics.Path left=
                    new android.graphics.Path();

            left.moveTo(cx,cy);
            left.lineTo(cx-size,cy-size*.55f);
            left.lineTo(cx-size*.82f,cy+size*.55f);
            left.close();
            canvas.drawPath(left,paint);

            android.graphics.Path right=
                    new android.graphics.Path();

            right.moveTo(cx,cy);
            right.lineTo(cx+size,cy-size*.55f);
            right.lineTo(cx+size*.82f,cy+size*.55f);
            right.close();
            canvas.drawPath(right,paint);

            canvas.drawCircle(cx,cy,size*.24f,paint);
        }


        void previewSnow(
                android.graphics.Canvas canvas,
                float cx,
                float cy,
                float size
        ) {
            paint.setStyle(android.graphics.Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1));

            for(int i=0;i<3;i++) {
                double a=i*Math.PI/3;
                float dx=(float)Math.cos(a)*size;
                float dy=(float)Math.sin(a)*size;
                canvas.drawLine(cx-dx,cy-dy,cx+dx,cy+dy,paint);
            }

            paint.setStyle(android.graphics.Paint.Style.FILL);
        }


        void previewHeart(
                android.graphics.Canvas canvas,
                float cx,
                float cy,
                float size
        ) {
            android.graphics.Path path=
                    new android.graphics.Path();

            path.moveTo(cx,cy+size*.7f);
            path.cubicTo(cx-size,cy,cx-size*.65f,cy-size*.75f,cx,cy-size*.22f);
            path.cubicTo(cx+size*.65f,cy-size*.75f,cx+size,cy,cx,cy+size*.7f);
            canvas.drawPath(path,paint);
        }


        void previewSpark(
                android.graphics.Canvas canvas,
                float cx,
                float cy,
                float size
        ) {
            android.graphics.Path path=
                    new android.graphics.Path();

            path.moveTo(cx,cy-size);
            path.lineTo(cx+size*.22f,cy-size*.22f);
            path.lineTo(cx+size,cy);
            path.lineTo(cx+size*.22f,cy+size*.22f);
            path.lineTo(cx,cy+size);
            path.lineTo(cx-size*.22f,cy+size*.22f);
            path.lineTo(cx-size,cy);
            path.lineTo(cx-size*.22f,cy-size*.22f);
            path.close();
            canvas.drawPath(path,paint);
        }


        void previewStar(
                android.graphics.Canvas canvas,
                float cx,
                float cy,
                float radius
        ) {
            android.graphics.Path path=
                    new android.graphics.Path();

            for(int i=0;i<10;i++) {
                double a=-Math.PI/2+i*Math.PI/5;
                float r=i%2==0 ? radius : radius*.42f;
                float x=cx+(float)Math.cos(a)*r;
                float y=cy+(float)Math.sin(a)*r;

                if(i==0)
                    path.moveTo(x,y);
                else
                    path.lineTo(x,y);
            }

            path.close();
            canvas.drawPath(path,paint);
        }


        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
        }


        @Override
        public void setColorFilter(
                android.graphics.ColorFilter filter
        ) {
            paint.setColorFilter(filter);
        }


        @Override
        public int getOpacity() {
            return android.graphics.PixelFormat.TRANSLUCENT;
        }
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

        java.util.ArrayList<String> words =
                PersonalDictionary.load(this);

        LinearLayout page = page(
                "Dictionary",
                "Personal words used by KeyKii suggestions",
                true
        );

        addInfoCard(
                page,
                "KeyKii personal dictionary",
                words.size() +
                        (words.size() == 1 ? " word is" : " words are") +
                        " saved locally. Personal words are checked before KeyKii's built-in English dictionary."
        );

        addActionButton(
                page,
                "Add personal word",
                v -> showPersonalWordEditor(null)
        );

        if (words.isEmpty()) {
            addInfoCard(
                    page,
                    "No personal words yet",
                    "Add names, places, slang, brand names or other words you type often. They will appear in KeyKii's suggestion strip when they match what you type."
            );
        } else {
            addSection(page, "Saved words");

            for (String savedWord : words) {
                final String word = savedWord;

                addRow(
                        page,
                        "Aa",
                        word,
                        "Tap to edit or delete",
                        v -> showPersonalWordEditor(word)
                );
            }
        }

        addSection(page, "Android dictionary");

        addInfoCard(
                page,
                "System personal dictionary",
                "Android's own personal dictionary stays separate. KeyKii's saved words are stored only inside KeyKii."
        );

        addActionButton(page, "Open Android personal dictionary", v -> {
            try {
                startActivity(new Intent(Settings.ACTION_USER_DICTIONARY_SETTINGS));
            } catch (Exception e) {
                toast("Personal dictionary settings are unavailable.");
            }
        });

        setContentView(wrap(page));
    }


    private void showPersonalWordEditor(String existingWord) {
        java.util.ArrayList<String> current =
                PersonalDictionary.load(this);

        if (
                existingWord == null &&
                current.size() >= PersonalDictionary.MAX_WORDS
        ) {
            toast("Personal dictionary limit reached");
            return;
        }

        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint("Word");
        input.setText(existingWord == null ? "" : existingWord);
        input.setSelection(input.getText().length());

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(22), dp(8), dp(22), 0);
        box.addView(input);

        AlertDialog.Builder builder =
                new AlertDialog.Builder(this)
                        .setTitle(
                                existingWord == null
                                        ? "Add personal word"
                                        : "Edit personal word"
                        )
                        .setView(box)
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Save", null);

        if (existingWord != null) {
            builder.setNeutralButton(
                    "Delete",
                    (dialog, which) -> {
                        PersonalDictionary.remove(
                                this,
                                existingWord
                        );
                        toast("Personal word deleted");
                        showDictionary();
                    }
            );
        }

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(d ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(v -> {
                            String cleaned =
                                    PersonalDictionary.normalize(
                                            input.getText().toString()
                                    );

                            if (cleaned.isEmpty()) {
                                input.setError(
                                        "Use letters and apostrophes only"
                                );
                                return;
                            }

                            java.util.ArrayList<String> saved =
                                    PersonalDictionary.load(this);

                            for (String other : saved) {
                                if (
                                        other.equalsIgnoreCase(cleaned) &&
                                        (
                                                existingWord == null ||
                                                !other.equalsIgnoreCase(existingWord)
                                        )
                                ) {
                                    input.setError(
                                            "This word is already saved"
                                    );
                                    return;
                                }
                            }

                            boolean ok =
                                    existingWord == null
                                            ? PersonalDictionary.add(
                                                    this,
                                                    cleaned
                                            )
                                            : PersonalDictionary.replace(
                                                    this,
                                                    existingWord,
                                                    cleaned
                                            );

                            if (!ok) {
                                toast("Could not save personal word");
                                return;
                            }

                            dialog.dismiss();
                            toast("Personal word saved");
                            showDictionary();
                        })
        );

        dialog.show();
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

        addSection(page,"Ads & rewards");
        addInfoCard(
                page,
                "Ad-supported access",
                "A completed rewarded ad can unlock PRO themes and ad-gated fonts for 24 hours. Occasional full-screen ads are limited to natural breaks inside the KeyKii app and are never shown inside the keyboard while you type."
        );
        if(ads!=null&&ads.privacyOptionsRequired()) {
            addActionButton(page,"Ad privacy choices",v ->
                    ads.showPrivacyOptions(() -> toast("Ad privacy choices updated"))
            );
        }

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
        t.setText(ui(title));
        t.setTextColor(TEXT);
        t.setTextSize(27);
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        TextView s = new TextView(this);
        s.setText(ui(subtitle));
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
        t.setText(ui(text));
        t.setTextColor(TEXT);
        t.setTextSize(15);
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setPadding(dp(6), dp(19), dp(6), dp(8));
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
        i.setTextSize(22);
        i.setGravity(Gravity.CENTER);
        i.setTextColor(TEXT);

        i.setBackground(
                round(
                        iconBubbleColor(title),
                        15
                )
        );

        LinearLayout.LayoutParams iconParams =
                new LinearLayout.LayoutParams(
                        dp(44),
                        dp(44)
                );

        iconParams.setMargins(
                0,0,
                dp(2),0
        );

        row.addView(i,iconParams);

        LinearLayout words = new LinearLayout(this);
        words.setOrientation(LinearLayout.VERTICAL);
        words.setPadding(dp(10), 0, dp(8), 0);

        TextView a = new TextView(this);
        a.setText(ui(title));
        a.setTextColor(TEXT);
        a.setTextSize(17);

        TextView b = new TextView(this);
        b.setText(ui(subtitle));
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

            e.putInt(
                    "theme_decor_style",
                    pack[7]
            );

            e.putInt(
                    "theme_key_style",
                    pack[8]
            );

            // Theme packs never change keyboard_font_style.
        } else {
            e.putInt(
                    "theme_decor_style",
                    0
            );

            e.putInt(
                    "theme_key_style",
                    0
            );
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
        b.setText(ui(message));
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

    private int iconBubbleColor(
            String title
    ) {
        if(title==null)
            return PASTEL_PURPLE;

        int bucket=
                Math.abs(
                        title.hashCode()
                ) % 5;

        switch(bucket) {
            case 0:
                return PASTEL_PINK;
            case 1:
                return PASTEL_PURPLE;
            case 2:
                return PASTEL_BLUE;
            case 3:
                return PASTEL_MINT;
            default:
                return PASTEL_PEACH;
        }
    }


    private LinearLayout card() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        GradientDrawable bg = round(CARD, 22);
        bg.setStroke(dp(1), BORDER);
        row.setBackground(bg);
        row.setElevation(dp(2));

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
        int value = prefs.getInt("theme_transparency", 55);

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
