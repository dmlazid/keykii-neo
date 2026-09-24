package com.keykii.neo;

import android.content.*;
import android.graphics.Color;
import android.graphics.drawable.*;
import android.inputmethodservice.InputMethodService;
import android.util.DisplayMetrics;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;

public class KeyKiiService extends InputMethodService {
    String emojiSearchQuery="";
    boolean emojiSearchMode=false;
    boolean kaomojiMode=false;
    int kaomojiCategory=0;
    TextView emojiSearchField=null;
    int emojiSearchCursor=0;
    int emojiSearchGestureStart=0;
    HorizontalScrollView emojiSearchResultsScroll=null;
    LinearLayout emojiSearchResultsRow=null;

    PopupWindow keyPreviewPopup=null;
    TextView keyPreviewText=null;

    // Gboard-style drag selector used by long-press alternatives.
    PopupWindow dragChoicePopup=null;
    java.util.ArrayList<TextView> dragChoiceViews=
        new java.util.ArrayList<>();
    java.util.ArrayList<String> dragChoiceValues=
        new java.util.ArrayList<>();
    int dragChoiceIndex=-1;
    boolean dragChoiceActive=false;
    String dragChoiceMode="";

    // Spacebar cursor control: slide left/right to move the caret.
    android.os.Handler spaceGestureHandler=
        new android.os.Handler(android.os.Looper.getMainLooper());
    float spaceDownX=0f;
    int spaceStartSelection=-1;
    int spaceMinSelection=0;
    int spaceMaxSelection=0;
    int spaceFallbackStep=0;
    boolean spaceCursorDragging=false;
    boolean spacePickerShown=false;


    LinearLayout root, panel, body;

    ClipboardManager clipboardManager;
    ClipboardManager.OnPrimaryClipChangedListener clipboardListener;
    android.media.AudioManager audioManager;

    // Voice typing only runs when the mic is tapped, so normal typing stays fast.
    android.speech.SpeechRecognizer voiceRecognizer=null;
    boolean voiceListening=false;
    boolean voiceProcessing=false;
    int voiceSessionId=0;
    LinearLayout voiceStatusRow=null;
    LinearLayout toolbarRow=null;
    TextView voiceStatusText=null;
    TextView voiceStatusMic=null;
    boolean voicePermissionPromptOpen=false;
    BroadcastReceiver voicePermissionReceiver=null;

    boolean clipboardShortcutMode=false;

    boolean shift=false;
    boolean capsLock=false;
    long lastShiftTap=0L;
    boolean symbols=false;
    boolean floating=true;
    boolean wideMode=false;
    boolean numberRow=false;

    int symbolPage=1;
    int page=0;
    int theme=0;
    int hand=0;
    int emojiCategory=0;
    java.util.LinkedHashMap<
        String,
        java.util.ArrayList<String>
    > emojiGroupsCache=null;

    int keyHeight=46;
    int floatGap=96;

    boolean haptic=false;
    boolean keySound=false;
    int keySoundVolume=50;
    boolean autoCapitalization=true;
    boolean doubleSpacePeriod=true;
    boolean keyPreviewEnabled=true;
    boolean swipeDeleteWord=true;
    boolean quickPunctuation=true;

    // 2.29.0 Glide typing. Off by default so normal tap typing keeps the
    // exact lightweight path used by the stable 2.27.4 keyboard.
    boolean glideTyping=false;
    boolean glideTrailEnabled=true;
    boolean glideTracking=false;
    boolean glideActive=false;
    boolean glideStartShift=false;
    float glideDownX=0f;
    float glideDownY=0f;
    long lastGlideEndTime=0L;
    int glideDecodeSession=0;
    StringBuilder glideGestureLetters=
        new StringBuilder();
    java.util.ArrayList<View> glideLetterViews=
        new java.util.ArrayList<>();
    java.util.ArrayList<String> glideLetterActions=
        new java.util.ArrayList<>();
    java.util.ArrayList<String> glideDictionary=null;
    final Object glideDictionaryLock=
        new Object();
    PopupWindow glideTrailPopup=null;
    TextView glideTrailText=null;
    View glideHighlightedKey=null;

    long lastSpaceTap=0L;

    float backspaceGestureStartX=0f;
    boolean backspaceSwipeActive=false;

    android.os.Handler repeatBackspaceHandler=
        new android.os.Handler(
            android.os.Looper.getMainLooper()
        );

    boolean backspaceRepeating=false;
    boolean suppressBackspaceClick=false;

    Runnable repeatBackspaceRunnable=
        new Runnable() {

            @Override
            public void run() {

                if(page==1 && emojiSearchMode) {
                    eraseEmojiSearchChar();
                    refreshEmojiSearchResults();
                } else {
                    InputConnection ic=
                        getCurrentInputConnection();
                    if(ic!=null)
                        deleteOneBeforeCursor(ic);
                }

                backspaceRepeating=true;

                repeatBackspaceHandler
                    .postDelayed(
                        this,
                        55
                    );
            }
        };


    @Override
    public void onCreate() {

        super.onCreate();

        clipboardManager=
            (ClipboardManager)
            getSystemService(
                CLIPBOARD_SERVICE
            );

        audioManager=
            (android.media.AudioManager)
            getSystemService(
                AUDIO_SERVICE
            );

        clipboardListener=()->captureClipboard();

        if(clipboardManager!=null)
            clipboardManager
                .addPrimaryClipChangedListener(
                    clipboardListener
                );

        voicePermissionReceiver=
            new BroadcastReceiver() {
                @Override
                public void onReceive(
                    Context context,
                    Intent intent
                ) {
                    if(
                        intent==null ||
                        !"com.keykii.neo.VOICE_PERMISSION_RESULT"
                            .equals(intent.getAction())
                    ) {
                        return;
                    }

                    voicePermissionPromptOpen=false;

                    boolean granted=
                        intent.getBooleanExtra(
                            "granted",
                            false
                        );

                    if(granted) {
                        // Start listening immediately after the user taps Allow,
                        // so the first mic tap behaves like Gboard.
                        new android.os.Handler(
                            android.os.Looper.getMainLooper()
                        ).postDelayed(
                            () -> toggleVoiceTyping(),
                            120
                        );
                    } else {
                        voiceToast(
                            "Microphone access was not allowed."
                        );
                    }
                }
            };

        IntentFilter voicePermissionFilter=
            new IntentFilter(
                "com.keykii.neo.VOICE_PERMISSION_RESULT"
            );

        if(android.os.Build.VERSION.SDK_INT>=33) {
            registerReceiver(
                voicePermissionReceiver,
                voicePermissionFilter,
                Context.RECEIVER_NOT_EXPORTED
            );
        } else {
            registerReceiver(
                voicePermissionReceiver,
                voicePermissionFilter
            );
        }
    }

    @Override
    public void onDestroy() {

        if(
            clipboardManager!=null &&
            clipboardListener!=null
        ){
            clipboardManager
                .removePrimaryClipChangedListener(
                    clipboardListener
                );
        }

        if(voiceRecognizer!=null) {
            try {
                voiceRecognizer.cancel();
                voiceRecognizer.destroy();
            } catch(Exception ignored) {
            }

            voiceRecognizer=null;
            voiceSessionId++;
            voiceListening=false;
            voiceProcessing=false;
        }

        if(voicePermissionReceiver!=null) {
            try {
                unregisterReceiver(
                    voicePermissionReceiver
                );
            } catch(Exception ignored) {
            }

            voicePermissionReceiver=null;
        }

        hideGlideTrail();

        super.onDestroy();
    }

    void captureClipboard(){

        if(
            clipboardManager==null ||
            !clipboardManager.hasPrimaryClip()
        ) return;

        ClipData clip=
            clipboardManager.getPrimaryClip();

        if(
            clip==null ||
            clip.getItemCount()==0
        ) return;

        CharSequence text=
            clip.getItemAt(0)
                .coerceToText(this);

        if(text!=null)
            rememberClip(
                text.toString()
            );
    }

    @Override
    public View onCreateInputView() {

        SharedPreferences keykiiPrefs=
            getSharedPreferences(
                "keykii_prefs",
                MODE_PRIVATE
            );

        keyHeight=keykiiPrefs.getInt(
            "key_height",
            46
        );

        haptic=keykiiPrefs.getBoolean(
            "haptic",
            false
        );

        keySound=keykiiPrefs.getBoolean(
            "key_sound",
            false
        );

        keySoundVolume=keykiiPrefs.getInt(
            "key_sound_volume",
            50
        );

        autoCapitalization=keykiiPrefs.getBoolean(
            "auto_capitalization",
            true
        );

        doubleSpacePeriod=keykiiPrefs.getBoolean(
            "double_space_period",
            true
        );

        keyPreviewEnabled=keykiiPrefs.getBoolean(
            "key_preview",
            true
        );

        swipeDeleteWord=keykiiPrefs.getBoolean(
            "swipe_delete_word",
            true
        );

        quickPunctuation=keykiiPrefs.getBoolean(
            "quick_punctuation",
            true
        );

        glideTyping=keykiiPrefs.getBoolean(
            "glide_typing",
            false
        );

        glideTrailEnabled=keykiiPrefs.getBoolean(
            "glide_trail",
            true
        );

        if(glideTyping)
            ensureGlideDictionaryAsync();

        numberRow=keykiiPrefs.getBoolean(
            "number_row",
            false
        );

        theme=resolvedTheme(keykiiPrefs);

        root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.TRANSPARENT);

        buildShell();

        return root;
    }


    @Override
    public boolean onEvaluateFullscreenMode() {
        return false;
    }


    @Override
    public void onComputeInsets(Insets outInsets) {
        super.onComputeInsets(outInsets);

        if(!isFullscreenMode()) {
            outInsets.contentTopInsets=
                outInsets.visibleTopInsets;
        }
    }


    void loadKeyKiiSettings() {

        SharedPreferences p=
            getSharedPreferences(
                "keykii_prefs",
                MODE_PRIVATE
            );

        if(!p.getBoolean("tools_panel_2192_migrated",false)) {
            p.edit()
             .putBoolean("toolbar_width",false)
             .putBoolean("toolbar_hand",false)
             .putBoolean("tools_panel_2192_migrated",true)
             .apply();
        }

        theme=resolvedTheme(p);
        keyHeight=p.getInt("key_height",46);
        floatGap=p.getInt("float_gap",96);
        haptic=p.getBoolean("haptic",false);
        keySound=p.getBoolean("key_sound",false);
        keySoundVolume=p.getInt(
            "key_sound_volume",
            50
        );
        numberRow=p.getBoolean("number_row",false);
        autoCapitalization=p.getBoolean(
            "auto_capitalization",
            true
        );
        doubleSpacePeriod=p.getBoolean(
            "double_space_period",
            true
        );
        keyPreviewEnabled=p.getBoolean(
            "key_preview",
            true
        );

        swipeDeleteWord=p.getBoolean(
            "swipe_delete_word",
            true
        );

        quickPunctuation=p.getBoolean(
            "quick_punctuation",
            true
        );

        glideTyping=p.getBoolean(
            "glide_typing",
            false
        );

        glideTrailEnabled=p.getBoolean(
            "glide_trail",
            true
        );

        if(glideTyping)
            ensureGlideDictionaryAsync();

        wideMode=p.getBoolean(
            "wide_default",
            false
        );

        hand=p.getInt(
            "one_handed_default",
            0
        );

        if(hand<0 || hand>2)
            hand=0;

        if(wideMode)
            hand=0;
    }

    @Override
    public void onStartInputView(
        EditorInfo info,
        boolean restarting
    ) {

        super.onStartInputView(
            info,
            restarting
        );

        loadKeyKiiSettings();
        shift=autoCapitalization;
        capsLock=false;
        lastShiftTap=0L;
        lastSpaceTap=0L;

        if(root!=null)
            buildShell();
    }

    void buildShell() {

        root.removeAllViews();

        if(wideMode)
            hand=0;

        root.setGravity(
            Gravity.CENTER_HORIZONTAL
        );

        int sidePadding=
            wideMode ? 3 : 10;

        int bottomPadding=
            wideMode
            ? 30
            : hand!=0
                ? Math.min(floatGap,48)
                : Math.min(floatGap,52);

        root.setPadding(
            dp(sidePadding),
            dp(4),
            dp(sidePadding),
            dp(bottomPadding)
        );

        panel=new LinearLayout(this);
        panel.setOrientation(
            LinearLayout.VERTICAL
        );

        int panelPadX=
            wideMode
            ? 8
            : hand!=0
                ? 6
                : 7;

        int panelPadBottom=
            wideMode
            ? 8
            : hand!=0
                ? 6
                : 7;

        panel.setPadding(
            dp(panelPadX),
            dp(4),
            dp(panelPadX),
            dp(panelPadBottom)
        );

        panel.setElevation(dp(10));

        applyPanelThemeBackground();

        DisplayMetrics d=
            getResources()
                .getDisplayMetrics();

        float ratio=
            wideMode
            ? .985f
            : hand!=0
                ? .82f
                : .91f;

        LinearLayout.LayoutParams panelParams=
            new LinearLayout.LayoutParams(
                (int)(d.widthPixels*ratio),
                LinearLayout.LayoutParams.WRAP_CONTENT
            );

        if(hand==0) {
            panelParams.gravity=
                Gravity.CENTER_HORIZONTAL;

            root.addView(
                panel,
                panelParams
            );

        } else {
            LinearLayout dock=
                new LinearLayout(this);

            dock.setOrientation(
                LinearLayout.HORIZONTAL
            );

            dock.setGravity(
                Gravity.CENTER_VERTICAL |
                (
                    hand==1
                    ? Gravity.START
                    : Gravity.END
                )
            );

            LinearLayout rail=
                buildOneHandRail();

            LinearLayout.LayoutParams railParams=
                new LinearLayout.LayoutParams(
                    dp(54),
                    LinearLayout.LayoutParams.WRAP_CONTENT
                );

            if(hand==1) {
                dock.addView(
                    panel,
                    panelParams
                );

                dock.addView(
                    rail,
                    railParams
                );
            } else {
                dock.addView(
                    rail,
                    railParams
                );

                dock.addView(
                    panel,
                    panelParams
                );
            }

            root.addView(
                dock,
                new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            );
        }

        addHandle();
        addVoiceStatusRow();
        addToolbar();
        updateVoiceStatusUi();

        body=new LinearLayout(this);
        body.setOrientation(
            LinearLayout.VERTICAL
        );

        panel.addView(
            body,
            new LinearLayout.LayoutParams(
                -1,
                -2
            )
        );

        showPage();
    }


    LinearLayout buildOneHandRail() {

        LinearLayout rail=
            new LinearLayout(this);

        rail.setOrientation(
            LinearLayout.VERTICAL
        );

        rail.setGravity(
            Gravity.CENTER
        );

        rail.setPadding(
            dp(3),
            dp(4),
            dp(3),
            dp(4)
        );

        TextView switchSide=
            oneHandRailButton(
                hand==1
                ? "▶"
                : "◀"
            );

        switchSide.setOnClickListener(v -> {
            hand=
                hand==1
                ? 2
                : 1;

            getSharedPreferences(
                "keykii_prefs",
                MODE_PRIVATE
            ).edit()
             .putInt(
                 "one_handed_default",
                 hand
             )
             .putBoolean(
                 "wide_default",
                 false
             )
             .apply();

            page=0;
            buildShell();
        });

        TextView expand=
            oneHandRailButton(
                "⛶\nWide"
            );

        expand.setTextSize(11);
        expand.setLineSpacing(0f,.9f);

        expand.setOnClickListener(v -> {
            // Leave one-handed completely and return to the real,
            // full-width docked keyboard.
            hand=0;
            wideMode=true;
            floating=false;
            page=0;
            symbols=false;
            symbolPage=1;
            shift=false;
            capsLock=false;

            getSharedPreferences(
                "keykii_prefs",
                MODE_PRIVATE
            ).edit()
             .putInt(
                 "one_handed_default",
                 0
             )
             .putBoolean(
                 "wide_default",
                 true
             )
             .apply();

            buildShell();
        });

        rail.addView(
            switchSide,
            new LinearLayout.LayoutParams(
                dp(47),
                dp(70)
            )
        );

        View gap=
            new View(this);

        rail.addView(
            gap,
            new LinearLayout.LayoutParams(
                1,
                dp(8)
            )
        );

        rail.addView(
            expand,
            new LinearLayout.LayoutParams(
                dp(47),
                dp(70)
            )
        );

        return rail;
    }


    TextView oneHandRailButton(
        String label
    ) {
        TextView v=
            new TextView(this);

        v.setText(label);
        v.setTextSize(20);
        v.setTextColor(textColor());
        v.setGravity(Gravity.CENTER);

        v.setBackground(
            round(
                keyColor(true),
                18,
                borderColor()
            )
        );

        return v;
    }


    void addHandle() {

        LinearLayout r=
            new LinearLayout(this);

        r.setGravity(Gravity.CENTER);

        View v=new View(this);

        v.setBackground(
            round(
                Color.argb(
                    105,
                    160,
                    160,
                    160
                ),
                4,
                Color.TRANSPARENT
            )
        );

        LinearLayout.LayoutParams p=
            new LinearLayout.LayoutParams(
                dp(38),
                dp(3)
            );

        p.setMargins(
            0,
            0,
            0,
            dp(3)
        );

        r.addView(v,p);
        panel.addView(r);
    }


    void addVoiceStatusRow() {

        LinearLayout row=
            new LinearLayout(this);

        voiceStatusRow=row;
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(
            dp(6),
            0,
            dp(6),
            0
        );

        row.setBackground(
            round(
                keyColor(false),
                18,
                borderColor()
            )
        );

        TextView back=
            new TextView(this);

        back.setText("←");
        back.setTextSize(26);
        back.setTextColor(textColor());
        back.setGravity(Gravity.CENTER);
        back.setOnClickListener(v -> cancelVoiceTyping());

        voiceStatusText=
            new TextView(this);

        voiceStatusText.setText("Speak now");
        voiceStatusText.setTextSize(18);
        voiceStatusText.setTextColor(textColor());
        voiceStatusText.setGravity(Gravity.CENTER);
        voiceStatusText.setTypeface(
            android.graphics.Typeface.DEFAULT,
            android.graphics.Typeface.BOLD
        );

        voiceStatusMic=
            new TextView(this);

        voiceStatusMic.setText("🎙");
        voiceStatusMic.setTextSize(22);
        voiceStatusMic.setTextColor(textColor());
        voiceStatusMic.setGravity(Gravity.CENTER);
        voiceStatusMic.setBackground(
            round(
                accentFillColor(),
                18,
                accentColor()
            )
        );

        voiceStatusMic.setOnClickListener(v -> {
            if(voiceListening) {
                try {
                    if(voiceRecognizer!=null)
                        voiceRecognizer.stopListening();
                } catch(Exception ignored) {
                }

                voiceListening=false;
                voiceProcessing=true;
                updateVoiceStatusUi();

            } else if(voiceProcessing) {
                cancelVoiceTyping();

            } else {
                toggleVoiceTyping();
            }
        });

        row.addView(
            back,
            new LinearLayout.LayoutParams(
                dp(48),
                dp(44)
            )
        );

        row.addView(
            voiceStatusText,
            new LinearLayout.LayoutParams(
                0,
                dp(44),
                1
            )
        );

        LinearLayout.LayoutParams micParams=
            new LinearLayout.LayoutParams(
                dp(48),
                dp(40)
            );

        micParams.setMargins(
            dp(2),
            dp(2),
            dp(2),
            dp(2)
        );

        row.addView(
            voiceStatusMic,
            micParams
        );

        panel.addView(
            row,
            new LinearLayout.LayoutParams(
                -1,
                dp(46)
            )
        );
    }


    void updateVoiceStatusUi() {
        boolean active=
            voiceListening ||
            voiceProcessing;

        if(voiceStatusRow!=null) {
            voiceStatusRow.setVisibility(
                active
                ? View.VISIBLE
                : View.GONE
            );
        }

        if(toolbarRow!=null) {
            toolbarRow.setVisibility(
                active
                ? View.GONE
                : View.VISIBLE
            );
        }

        if(voiceStatusText!=null) {
            voiceStatusText.setText(
                voiceProcessing
                ? "Processing…"
                : "Speak now"
            );
        }

        if(voiceStatusMic!=null) {
            voiceStatusMic.setAlpha(
                active
                ? 1f
                : .75f
            );
        }
    }


    void releaseVoiceRecognizer() {
        // Invalidate callbacks from any previous recognizer session.
        voiceSessionId++;

        android.speech.SpeechRecognizer old=
            voiceRecognizer;

        voiceRecognizer=null;

        if(old!=null) {
            try {
                old.cancel();
            } catch(Exception ignored) {
            }

            try {
                old.destroy();
            } catch(Exception ignored) {
            }
        }
    }


    void cancelVoiceTyping() {
        releaseVoiceRecognizer();
        voiceListening=false;
        voiceProcessing=false;
        updateVoiceStatusUi();
    }


    void addToolbar() {

        LinearLayout r=
            new LinearLayout(this);

        toolbarRow=r;
        r.setGravity(Gravity.CENTER);

        SharedPreferences toolbarPrefs=
            getSharedPreferences(
                "keykii_prefs",
                MODE_PRIVATE
            );

        // Gboard-style tools entry on the normal keyboard.
        // On other panels this becomes the keyboard/back button.
        if(page==0)
            tool(r,"▦",7);
        else
            tool(r,"⌨",0);

        String orderText=
            toolbarPrefs.getString(
                "toolbar_order",
                "emoji,clipboard,actions,voice,theme,width,hand"
            );

        java.util.LinkedHashSet<String> order=
            new java.util.LinkedHashSet<>();

        if(orderText!=null) {
            for(String id:orderText.split(",")) {
                String clean=id.trim();

                if(
                    clean.equals("emoji") ||
                    clean.equals("clipboard") ||
                    clean.equals("actions") ||
                    clean.equals("voice") ||
                    clean.equals("theme") ||
                    clean.equals("width") ||
                    clean.equals("hand")
                ) {
                    order.add(clean);
                }
            }
        }

        // Add anything missing so old/corrupt preferences can never
        // make a toolbar item disappear from the ordering system.
        order.add("emoji");
        order.add("clipboard");
        order.add("actions");
        order.add("voice");
        order.add("theme");
        order.add("width");
        order.add("hand");

        for(String id:order) {

            if(
                id.equals("emoji") &&
                toolbarPrefs.getBoolean(
                    "toolbar_emoji",
                    true
                )
            ) {
                tool(r,"☺",1);

            } else if(
                id.equals("clipboard") &&
                toolbarPrefs.getBoolean(
                    "toolbar_clipboard",
                    true
                )
            ) {
                tool(r,"▣",2);

            } else if(
                id.equals("actions") &&
                toolbarPrefs.getBoolean(
                    "toolbar_actions",
                    true
                )
            ) {
                tool(r,"✎",3);

            } else if(
                id.equals("voice") &&
                toolbarPrefs.getBoolean(
                    "voice_typing_enabled",
                    true
                ) &&
                toolbarPrefs.getBoolean(
                    "toolbar_voice",
                    true
                )
            ) {
                tool(r,"🎙",8);

            } else if(
                id.equals("theme") &&
                toolbarPrefs.getBoolean(
                    "toolbar_theme",
                    true
                )
            ) {
                tool(r,"◐",4);

            } else if(
                id.equals("width") &&
                toolbarPrefs.getBoolean(
                    "toolbar_width",
                    true
                )
            ) {
                // Separate full/wide control from one-handed mode.
                tool(r,"⛶",5);

            } else if(
                id.equals("hand") &&
                toolbarPrefs.getBoolean(
                    "toolbar_hand",
                    true
                )
            ) {
                // Center -> left -> right -> center.
                // Icon shows what the next tap will do.
                tool(
                    r,
                    hand==0
                        ? "◀"
                        : hand==1
                            ? "▶"
                            : "⛶",
                    6
                );
            }
        }

        int toolbarHeight=
            wideMode
            ? 42
            : hand!=0
                ? 40
                : 40;

        panel.addView(
            r,
            new LinearLayout.LayoutParams(
                -1,
                dp(toolbarHeight)
            )
        );
    }


    void tool(
        LinearLayout r,
        String text,
        int action
    ) {

        TextView v=new TextView(this);

        v.setText(text);
        v.setTextColor(textColor());
        v.setTextSize(18);
        v.setGravity(Gravity.CENTER);
        v.setClickable(true);

        v.setOnClickListener(x -> {

            if(action<=3) {

                page=action;

                if(action==1)
                    emojiCategory=0;

                // Rebuild on keyboard/back so the toolbar itself also changes.
                if(action==0)
                    buildShell();
                else
                    showPage();

            } else if(action==4) {

                theme=(theme+1)%3;

                SharedPreferences themePrefs=
                    getSharedPreferences(
                        "keykii_prefs",
                        MODE_PRIVATE
                    );

                SharedPreferences.Editor themeEdit=
                    themePrefs.edit();

                if(
                    themePrefs.getBoolean(
                        "theme_auto_day_night",
                        false
                    )
                ) {
                    int nightMode=
                        getResources()
                            .getConfiguration()
                            .uiMode &
                        android.content.res.Configuration.UI_MODE_NIGHT_MASK;

                    if(
                        nightMode==
                        android.content.res.Configuration.UI_MODE_NIGHT_YES
                    ) {
                        themeEdit.putInt(
                            "theme_dark",
                            theme
                        );
                    } else {
                        themeEdit.putInt(
                            "theme_light",
                            theme
                        );
                    }
                } else {
                    themeEdit.putInt(
                        "theme",
                        theme
                    );
                }

                themeEdit.apply();
                buildShell();

            } else if(action==5) {

                wideMode=!wideMode;

                SharedPreferences sizePrefs=
                    getSharedPreferences(
                        "keykii_prefs",
                        MODE_PRIVATE
                    );

                if(wideMode) {
                    hand=0;
                } else {
                    hand=sizePrefs.getInt(
                        "one_handed_default",
                        0
                    );

                    if(hand<0 || hand>2)
                        hand=0;
                }

                sizePrefs.edit()
                 .putBoolean(
                     "wide_default",
                     wideMode
                 )
                 .apply();

                page=0;
                buildShell();

            } else if(action==6) {

                wideMode=false;

                hand=
                    hand==0
                    ? 1
                    : hand==1
                        ? 2
                        : 0;

                getSharedPreferences(
                    "keykii_prefs",
                    MODE_PRIVATE
                ).edit()
                 .putInt(
                     "one_handed_default",
                     hand
                 )
                 .putBoolean(
                     "wide_default",
                     false
                 )
                 .apply();

                page=0;
                buildShell();

            } else if(action==7) {

                // Toggle Tools <-> Keyboard even if the toolbar has not
                // yet been rebuilt. This fixes the stuck-back-button issue.
                if(page==4 || page==5 || page==3) {
                    page=0;
                } else {
                    page=4;
                }

                buildShell();

            } else if(action==8) {

                toggleVoiceTyping();
            }
        });;

        r.addView(
            v,
            new LinearLayout.LayoutParams(
                0,
                dp(44),
                1
            )
        );
    }


    void voiceToast(String text) {
        android.widget.Toast.makeText(
            this,
            text,
            android.widget.Toast.LENGTH_SHORT
        ).show();
    }


    void openVoiceSettingsForPermission() {
        try {
            Intent intent=
                new Intent(
                    this,
                    SettingsActivity.class
                );

            intent.putExtra(
                "open_screen",
                "voice"
            );

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            );

            startActivity(intent);

        } catch(Exception ignored) {
            voiceToast(
                "Open KeyKii settings and allow microphone access."
            );
        }
    }


    void toggleVoiceTyping() {

        if(
            !getSharedPreferences(
                "keykii_prefs",
                MODE_PRIVATE
            ).getBoolean(
                "voice_typing_enabled",
                true
            )
        ) {
            cancelVoiceTyping();
            voiceToast(
                "Voice typing is turned off in KeyKii settings."
            );
            return;
        }

        if(voiceListening) {
            try {
                if(voiceRecognizer!=null)
                    voiceRecognizer.stopListening();
            } catch(Exception ignored) {
            }

            voiceListening=false;
            voiceProcessing=true;
            updateVoiceStatusUi();
            return;
        }

        if(
            android.os.Build.VERSION.SDK_INT>=23 &&
            checkSelfPermission(
                android.Manifest.permission.RECORD_AUDIO
            )!=
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            if(voicePermissionPromptOpen)
                return;

            voicePermissionPromptOpen=true;

            try {
                Intent permissionIntent=
                    new Intent(
                        this,
                        VoicePermissionActivity.class
                    );

                permissionIntent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_NO_ANIMATION
                );

                startActivity(
                    permissionIntent
                );

            } catch(Exception e) {
                voicePermissionPromptOpen=false;
                openVoiceSettingsForPermission();
            }

            return;
        }

        if(
            !android.speech.SpeechRecognizer
                .isRecognitionAvailable(this)
        ) {
            voiceToast(
                "Android speech recognition is unavailable on this device."
            );
            return;
        }

        try {
            // Completely release the previous recognizer first. Some Android
            // speech services can send a late callback from the old session;
            // the session id below prevents it from cancelling the new one.
            releaseVoiceRecognizer();

            final int session=
                ++voiceSessionId;

            voiceRecognizer=
                android.speech.SpeechRecognizer
                    .createSpeechRecognizer(this);

            final android.speech.SpeechRecognizer sessionRecognizer=
                voiceRecognizer;

            voiceRecognizer.setRecognitionListener(
                new android.speech.RecognitionListener() {

                    @Override
                    public void onReadyForSpeech(
                        android.os.Bundle params
                    ) {
                        if(session!=voiceSessionId)
                            return;

                        voiceListening=true;
                        voiceProcessing=false;
                        updateVoiceStatusUi();
                    }

                    @Override
                    public void onBeginningOfSpeech() {
                        if(session!=voiceSessionId)
                            return;

                        voiceListening=true;
                        voiceProcessing=false;
                        updateVoiceStatusUi();
                    }

                    @Override
                    public void onRmsChanged(float rmsdB) {
                    }

                    @Override
                    public void onBufferReceived(byte[] buffer) {
                    }

                    @Override
                    public void onEndOfSpeech() {
                        if(session!=voiceSessionId)
                            return;

                        voiceListening=false;
                        voiceProcessing=true;
                        updateVoiceStatusUi();
                    }

                    @Override
                    public void onError(int error) {
                        if(session!=voiceSessionId)
                            return;

                        voiceListening=false;
                        voiceProcessing=false;
                        updateVoiceStatusUi();

                        // Release this finished recognizer so the next mic tap
                        // always starts from a clean speech session.
                        if(voiceRecognizer==sessionRecognizer) {
                            voiceRecognizer=null;
                            try {
                                sessionRecognizer.destroy();
                            } catch(Exception ignored) {
                            }
                        }

                        if(
                            error!=
                            android.speech.SpeechRecognizer.ERROR_NO_MATCH &&
                            error!=
                            android.speech.SpeechRecognizer.ERROR_SPEECH_TIMEOUT
                        ) {
                            voiceToast(
                                "Voice typing stopped. Tap the mic to try again."
                            );
                        }
                    }

                    @Override
                    public void onResults(
                        android.os.Bundle results
                    ) {
                        if(session!=voiceSessionId)
                            return;

                        voiceListening=false;
                        voiceProcessing=false;
                        updateVoiceStatusUi();

                        if(voiceRecognizer==sessionRecognizer) {
                            voiceRecognizer=null;
                            try {
                                sessionRecognizer.destroy();
                            } catch(Exception ignored) {
                            }
                        }

                        java.util.ArrayList<String> matches=
                            results.getStringArrayList(
                                android.speech.SpeechRecognizer
                                    .RESULTS_RECOGNITION
                            );

                        if(
                            matches==null ||
                            matches.isEmpty()
                        ) {
                            return;
                        }

                        String spoken=matches.get(0);

                        if(
                            spoken==null ||
                            spoken.trim().isEmpty()
                        ) {
                            return;
                        }

                        InputConnection ic=
                            getCurrentInputConnection();

                        if(ic!=null)
                            ic.commitText(
                                spoken.trim(),
                                1
                            );
                    }

                    @Override
                    public void onPartialResults(
                        android.os.Bundle partialResults
                    ) {
                    }

                    @Override
                    public void onEvent(
                        int eventType,
                        android.os.Bundle params
                    ) {
                    }
                }
            );

            Intent listenIntent=
                new Intent(
                    android.speech.RecognizerIntent
                        .ACTION_RECOGNIZE_SPEECH
                );

            listenIntent.putExtra(
                android.speech.RecognizerIntent
                    .EXTRA_LANGUAGE_MODEL,
                android.speech.RecognizerIntent
                    .LANGUAGE_MODEL_FREE_FORM
            );

            listenIntent.putExtra(
                android.speech.RecognizerIntent
                    .EXTRA_PARTIAL_RESULTS,
                false
            );

            listenIntent.putExtra(
                android.speech.RecognizerIntent
                    .EXTRA_MAX_RESULTS,
                3
            );

            listenIntent.putExtra(
                android.speech.RecognizerIntent
                    .EXTRA_LANGUAGE,
                java.util.Locale.getDefault()
                    .toLanguageTag()
            );

            voiceListening=true;
            voiceProcessing=false;
            updateVoiceStatusUi();

            sessionRecognizer.startListening(
                listenIntent
            );

        } catch(Exception e) {
            releaseVoiceRecognizer();
            voiceListening=false;
            voiceProcessing=false;
            updateVoiceStatusUi();
            voiceToast(
                "Unable to start voice typing."
            );
        }
    }


    void showPage() {

        body.removeAllViews();

        if(page==0)
            buildKeyboard();

        else if(page==1)
            buildEmoji();

        else if(page==2)
            buildClipboard();

        else if(page==3)
            buildEditing();

        else if(page==4)
            buildToolsPanel();

        else if(page==5)
            buildResizePanel();

        else
            buildKeyboard();
    }

    void ensureGlideDictionaryAsync() {
        if(!glideTyping || glideDictionary!=null)
            return;

        new Thread(
            () -> loadGlideDictionaryBlocking(),
            "KeyKii-Glide-Dictionary"
        ).start();
    }


    java.util.ArrayList<String> loadGlideDictionaryBlocking() {
        synchronized(glideDictionaryLock) {
            if(glideDictionary!=null)
                return glideDictionary;

            java.util.ArrayList<String> words=
                new java.util.ArrayList<>();

            try {
                java.io.BufferedReader reader=
                    new java.io.BufferedReader(
                        new java.io.InputStreamReader(
                            getAssets().open(
                                "keykii-english-10000.txt"
                            ),
                            "UTF-8"
                        )
                    );

                String line;

                while((line=reader.readLine())!=null) {
                    String word=
                        line.trim()
                            .toLowerCase(
                                java.util.Locale.ROOT
                            );

                    if(
                        word.length()<2 ||
                        word.length()>24
                    ) {
                        continue;
                    }

                    boolean valid=true;

                    for(int i=0;i<word.length();i++) {
                        if(!Character.isLetter(word.charAt(i))) {
                            valid=false;
                            break;
                        }
                    }

                    if(valid)
                        words.add(word);
                }

                reader.close();

            } catch(Exception ignored) {
            }

            glideDictionary=words;
            return glideDictionary;
        }
    }


    boolean isGlideLetterAction(String action) {
        return
            glideTyping &&
            page==0 &&
            !symbols &&
            action!=null &&
            action.length()==1 &&
            Character.isLetter(
                action.charAt(0)
            );
    }


    String glideLetterAt(
        float rawX,
        float rawY
    ) {
        int[] location=
            new int[2];

        for(int i=0;i<glideLetterViews.size();i++) {
            View key=
                glideLetterViews.get(i);

            if(
                key==null ||
                key.getVisibility()!=View.VISIBLE
            ) {
                continue;
            }

            key.getLocationOnScreen(location);

            if(
                rawX>=location[0] &&
                rawX<=location[0]+key.getWidth() &&
                rawY>=location[1] &&
                rawY<=location[1]+key.getHeight()
            ) {
                return glideLetterActions.get(i);
            }
        }

        return "";
    }


    void appendGlideLetter(String letter) {
        if(
            letter==null ||
            letter.length()!=1
        ) {
            return;
        }

        String lower=
            letter.toLowerCase(
                java.util.Locale.ROOT
            );

        int length=
            glideGestureLetters.length();

        if(
            length==0 ||
            glideGestureLetters.charAt(length-1)!=
                lower.charAt(0)
        ) {
            glideGestureLetters.append(lower);
            updateGlideTrail();
        }
    }


    void resetGlideKeyVisuals() {
        glideHighlightedKey=null;

        for(View key:glideLetterViews) {
            if(key==null)
                continue;

            key.animate()
                .cancel();

            key.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(55)
                .start();

            key.setPressed(false);

            key.post(() -> {
                key.setPressed(false);
                key.jumpDrawablesToCurrentState();
            });
        }
    }


    void highlightGlideLetter(String letter) {
        if(
            letter==null ||
            letter.isEmpty()
        ) {
            return;
        }

        View next=null;

        for(int i=0;i<glideLetterActions.size();i++) {
            if(
                letter.equals(
                    glideLetterActions.get(i)
                )
            ) {
                next=glideLetterViews.get(i);
                break;
            }
        }

        if(next==glideHighlightedKey)
            return;

        if(glideHighlightedKey!=null) {
            View previous=glideHighlightedKey;

            previous.animate()
                .cancel();

            previous.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(45)
                .start();

            previous.setPressed(false);
            previous.jumpDrawablesToCurrentState();
        }

        glideHighlightedKey=next;

        if(next!=null) {
            next.setPressed(true);

            next.animate()
                .cancel();

            next.animate()
                .scaleX(1.09f)
                .scaleY(1.09f)
                .alpha(.88f)
                .setDuration(45)
                .start();
        }
    }


    void showGlideTrail(
        float rawX,
        float rawY
    ) {
        if(
            !glideTrailEnabled ||
            root==null
        ) {
            return;
        }

        hideGlideTrail();

        glideTrailText=
            new TextView(this);

        glideTrailText.setTextSize(14);
        glideTrailText.setTextColor(textColor());
        glideTrailText.setGravity(Gravity.CENTER);
        glideTrailText.setPadding(
            dp(14),
            dp(5),
            dp(14),
            dp(5)
        );

        glideTrailText.setElevation(
            dp(8)
        );

        glideTrailText.setBackground(
            round(
                accentFillColor(),
                20,
                accentColor()
            )
        );

        glideTrailPopup=
            new PopupWindow(
                glideTrailText,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dp(40),
                false
            );

        glideTrailPopup.setClippingEnabled(false);
        glideTrailPopup.setOutsideTouchable(false);
        glideTrailPopup.setTouchable(false);

        try {
            glideTrailPopup.showAtLocation(
                root,
                Gravity.TOP |
                Gravity.LEFT,
                (int)rawX+dp(18),
                Math.max(
                    dp(12),
                    (int)rawY-dp(72)
                )
            );

            glideTrailText.setScaleX(.78f);
            glideTrailText.setScaleY(.78f);
            glideTrailText.setAlpha(.15f);

            glideTrailText.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(110)
                .start();

        } catch(Exception ignored) {
        }

        updateGlideTrail();
    }


    void moveGlideTrail(
        float rawX,
        float rawY
    ) {
        if(
            glideTrailPopup==null ||
            !glideTrailPopup.isShowing()
        ) {
            return;
        }

        try {
            glideTrailPopup.update(
                (int)rawX+dp(18),
                Math.max(
                    dp(12),
                    (int)rawY-dp(72)
                ),
                -1,
                dp(40)
            );
        } catch(Exception ignored) {
        }
    }


    void updateGlideTrail() {
        if(
            glideTrailText==null ||
            glideGestureLetters.length()==0
        ) {
            return;
        }

        String value=
            glideGestureLetters.toString();

        if(glideStartShift && value.length()>0) {
            value=
                value.substring(0,1)
                    .toUpperCase(
                        java.util.Locale.ROOT
                    ) +
                value.substring(1);
        }

        glideTrailText.setText(
            "〰  " + value + "  ✦"
        );

        glideTrailText.animate()
            .cancel();

        glideTrailText.setScaleX(.96f);
        glideTrailText.setScaleY(.96f);

        glideTrailText.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(70)
            .start();
    }


    void hideGlideTrail() {
        if(glideTrailText!=null) {
            glideTrailText.animate()
                .cancel();
        }

        if(glideTrailPopup!=null) {
            try {
                glideTrailPopup.dismiss();
            } catch(Exception ignored) {
            }
        }

        glideTrailPopup=null;
        glideTrailText=null;
    }


    String glideWordSignature(String word) {
        if(word==null || word.isEmpty())
            return "";

        String lower=
            word.toLowerCase(
                java.util.Locale.ROOT
            );

        StringBuilder out=
            new StringBuilder();

        char last=0;

        for(int i=0;i<lower.length();i++) {
            char ch=lower.charAt(i);

            if(!Character.isLetter(ch))
                continue;

            if(out.length()==0 || ch!=last) {
                out.append(ch);
                last=ch;
            }
        }

        return out.toString();
    }


    int glideEditDistance(
        String a,
        String b
    ) {
        int la=a.length();
        int lb=b.length();

        int[] prev=
            new int[lb+1];
        int[] curr=
            new int[lb+1];

        for(int j=0;j<=lb;j++)
            prev[j]=j;

        for(int i=1;i<=la;i++) {
            curr[0]=i;

            for(int j=1;j<=lb;j++) {
                int cost=
                    a.charAt(i-1)==b.charAt(j-1)
                    ? 0
                    : 1;

                curr[j]=Math.min(
                    Math.min(
                        curr[j-1]+1,
                        prev[j]+1
                    ),
                    prev[j-1]+cost
                );
            }

            int[] swap=prev;
            prev=curr;
            curr=swap;
        }

        return prev[lb];
    }


    String decodeGlideWord(String gesture) {
        if(
            gesture==null ||
            gesture.length()<2
        ) {
            return gesture==null
                ? ""
                : gesture;
        }

        java.util.ArrayList<String> dictionary=
            loadGlideDictionaryBlocking();

        if(
            dictionary==null ||
            dictionary.isEmpty()
        ) {
            return gesture;
        }

        String best="";
        int bestScore=Integer.MAX_VALUE;

        char first=
            gesture.charAt(0);
        char last=
            gesture.charAt(
                gesture.length()-1
            );

        for(int index=0;index<dictionary.size();index++) {
            String word=
                dictionary.get(index);

            if(
                word.isEmpty() ||
                word.charAt(0)!=first
            ) {
                continue;
            }

            String signature=
                glideWordSignature(word);

            if(signature.isEmpty())
                continue;

            int distance=
                glideEditDistance(
                    gesture,
                    signature
                );

            // Prefer the same ending key, but do not require it because a
            // finger can lift close to a neighboring key.
            int endPenalty=
                signature.charAt(
                    signature.length()-1
                )==last
                ? 0
                : 4;

            int lengthPenalty=
                Math.abs(
                    signature.length()-
                    gesture.length()
                );

            // The bundled dictionary is frequency ordered, so a tiny rank
            // penalty breaks ties toward common English words.
            int rankPenalty=
                index/1800;

            int score=
                distance*10 +
                endPenalty +
                lengthPenalty +
                rankPenalty;

            if(score<bestScore) {
                bestScore=score;
                best=word;

                if(
                    distance==0 &&
                    endPenalty==0 &&
                    rankPenalty==0
                ) {
                    break;
                }
            }
        }

        if(best.isEmpty())
            return gesture;

        return best;
    }


    void finishGlideGesture(
        String gesture,
        boolean capitalize
    ) {
        if(
            gesture==null ||
            gesture.length()<2
        ) {
            return;
        }

        final int session=
            ++glideDecodeSession;

        new Thread(
            () -> {
                String decoded=
                    decodeGlideWord(
                        gesture
                    );

                if(
                    decoded==null ||
                    decoded.isEmpty()
                ) {
                    return;
                }

                final String result=
                    capitalize
                    ? decoded.substring(0,1)
                        .toUpperCase(
                            java.util.Locale.ROOT
                        ) +
                      decoded.substring(1)
                    : decoded;

                new android.os.Handler(
                    android.os.Looper.getMainLooper()
                ).post(() -> {
                    if(session!=glideDecodeSession)
                        return;

                    InputConnection ic=
                        getCurrentInputConnection();

                    if(ic==null)
                        return;

                    ic.commitText(
                        result + " ",
                        1
                    );

                    if(
                        shift &&
                        !capsLock
                    ) {
                        shift=false;
                        lastShiftTap=0L;
                        showPage();
                    }
                });
            },
            "KeyKii-Glide-Decode"
        ).start();
    }


    boolean handleGlideKeyTouch(
        View keyView,
        MotionEvent event,
        String action
    ) {
        if(!isGlideLetterAction(action))
            return false;

        int type=
            event.getActionMasked();

        if(type==MotionEvent.ACTION_DOWN) {
            glideTracking=true;
            glideActive=false;
            glideStartShift=
                shift &&
                !capsLock;
            glideDownX=event.getRawX();
            glideDownY=event.getRawY();
            glideGestureLetters.setLength(0);
            appendGlideLetter(action);
            return false;
        }

        if(
            type==MotionEvent.ACTION_MOVE &&
            glideTracking
        ) {
            float dx=
                event.getRawX()-
                glideDownX;
            float dy=
                event.getRawY()-
                glideDownY;

            if(
                !glideActive &&
                (
                    Math.abs(dx)>dp(16) ||
                    Math.abs(dy)>dp(16)
                )
            ) {
                glideActive=true;
                keyView.cancelLongPress();
                dismissKeyPreview();

                resetGlideKeyVisuals();

                String firstLetter=
                    glideLetterAt(
                        event.getRawX(),
                        event.getRawY()
                    );

                if(firstLetter.isEmpty())
                    firstLetter=action;

                highlightGlideLetter(
                    firstLetter
                );

                showGlideTrail(
                    event.getRawX(),
                    event.getRawY()
                );
            }

            if(glideActive) {
                String letter=
                    glideLetterAt(
                        event.getRawX(),
                        event.getRawY()
                    );

                if(!letter.isEmpty()) {
                    appendGlideLetter(letter);
                    highlightGlideLetter(letter);
                }

                moveGlideTrail(
                    event.getRawX(),
                    event.getRawY()
                );

                return true;
            }

            return false;
        }

        if(
            type==MotionEvent.ACTION_UP ||
            type==MotionEvent.ACTION_CANCEL
        ) {
            boolean wasActive=
                glideActive;

            if(wasActive) {
                String letter=
                    glideLetterAt(
                        event.getRawX(),
                        event.getRawY()
                    );

                if(!letter.isEmpty())
                    appendGlideLetter(letter);
            }

            String gesture=
                glideGestureLetters.toString();

            boolean capitalize=
                glideStartShift;

            glideTracking=false;
            glideActive=false;
            glideGestureLetters.setLength(0);
            hideGlideTrail();
            resetGlideKeyVisuals();

            if(
                wasActive &&
                type==MotionEvent.ACTION_UP
            ) {
                lastGlideEndTime=
                    android.os.SystemClock
                        .uptimeMillis();

                finishGlideGesture(
                    gesture,
                    capitalize
                );

                return true;
            }

            return wasActive;
        }

        return false;
    }


    void buildKeyboard() {

        resetGlideKeyVisuals();
        hideGlideTrail();
        glideLetterViews.clear();
        glideLetterActions.clear();

        if(!symbols) {

            if(numberRow) {
                row(new String[]{
                    "1","2","3","4","5",
                    "6","7","8","9","0"
                });
            }

            row(new String[]{
                "q","w","e","r","t",
                "y","u","i","o","p"
            });

            centered(new String[]{
                "a","s","d","f","g",
                "h","j","k","l"
            });

            third(new String[]{
                "z","x","c","v",
                "b","n","m"
            });

        } else if(symbolPage==1) {

            // Gboard-style ?123 page
            row(new String[]{
                "1","2","3","4","5",
                "6","7","8","9","0"
            });

            row(new String[]{
                "@","#","$","_","&",
                "-","+","(",")","/"
            });

            third(new String[]{
                "*","\"","'",":",
                ";","!","?"
            });

        } else {

            // Gboard-style =\< page
            row(new String[]{
                "~","`","|","•","√",
                "π","÷","×","§","∆"
            });

            row(new String[]{
                "£","¢","€","¥","^",
                "°","=","{","}","\\"
            });

            third(new String[]{
                "%","©","®","™",
                "✓","[","]"
            });
        }

        bottom();
    }

    void row(String[] values) {

        LinearLayout r=newRow();

        for(String s:values)
            key(r,s,s,1,false);

        body.addView(r);
    }

    void centered(String[] values) {

        LinearLayout r=newRow();

        spacer(r,.42f);

        for(String s:values)
            key(r,s,s,1,false);

        spacer(r,.42f);

        body.addView(r);
    }

    void third(String[] values) {

        LinearLayout r=newRow();

        if(!symbols) {

            key(
                r,
                capsLock ? "⇪" : "⇧",
                "SHIFT",
                1.05f,
                true
            );

        } else if(symbolPage==1) {

            key(
                r,
                "=\\<",
                "SYM2",
                1.05f,
                true
            );

        } else {

            key(
                r,
                "?123",
                "SYM1",
                1.05f,
                true
            );
        }

        for(String s:values)
            key(r,s,s,1,false);

        key(
            r,
            "⌫",
            "BACK",
            1.05f,
            true
        );

        body.addView(r);
    }

    void bottom() {

        LinearLayout r=newRow();

        if(page==1 && emojiSearchMode) {

            key(
                r,
                symbols ? "ABC" : "?123",
                symbols ? "ABC" : "123",
                1f,
                true
            );

            key(
                r,
                ",",
                ",",
                .72f,
                false
            );

            key(
                r,
                "KeyKii",
                "SPACE",
                3.05f,
                false
            );

            key(
                r,
                ".",
                ".",
                .58f,
                false
            );

            key(
                r,
                "🔍",
                "ENTER",
                1.05f,
                true
            );

            body.addView(r);
            return;
        }

        key(
            r,
            symbols ? "ABC" : "?123",
            symbols ? "ABC" : "123",
            1f,
            true
        );

        if(symbols && symbolPage==2) {
            // Gboard-style second symbol page: < and > live beside space.
            key(
                r,
                "<",
                "<",
                .85f,
                false
            );

            key(
                r,
                "KeyKii",
                "SPACE",
                2.75f,
                false
            );

            key(
                r,
                ">",
                ">",
                .58f,
                false
            );

        } else {
            key(
                r,
                ",",
                ",",
                .85f,
                false
            );

            key(
                r,
                "KeyKii",
                "SPACE",
                2.75f,
                false
            );

            key(
                r,
                ".",
                ".",
                .58f,
                false
            );
        }

        key(
            r,
            enterLabel(),
            "ENTER",
            1.05f,
            true
        );

        body.addView(r);
    }


    LinearLayout newRow() {

        LinearLayout r=
            new LinearLayout(this);

        r.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams p=
            new LinearLayout.LayoutParams(
                -1,
                -2
            );

        p.setMargins(
            0,
            0,
            0,
            dp(1)
        );

        r.setLayoutParams(p);

        return r;
    }

    void spacer(
        LinearLayout r,
        float weight
    ) {

        r.addView(
            new View(this),
            new LinearLayout.LayoutParams(
                0,
                dp(28),
                weight
            )
        );
    }

    void key(
        LinearLayout r,
        String label,
        String action,
        float weight,
        boolean special
    ){

        FrameLayout box=
            new FrameLayout(this);

        TextView main=
            new TextView(this);

        String shown=
            shift &&
            !symbols &&
            label.length()==1
            ? label.toUpperCase()
            : label;

        main.setText(shown);
        main.setTextColor(textColor());
        main.setGravity(Gravity.CENTER);
        main.setIncludeFontPadding(false);
        main.setAllCaps(false);

        boolean mainNumberLabel=
                page==0 &&
                !symbols &&
                label!=null &&
                label.length()==1 &&
                Character.isDigit(
                    label.charAt(0)
                );

        if(label.equals("KeyKii"))
            main.setTextSize(16);

        else if(mainNumberLabel)
            main.setTextSize(15);

        else if(label.length()>2)
            main.setTextSize(13);

        else
            main.setTextSize(18);

        boolean space=
            action.equals("SPACE");

        box.setBackground(
            keyBackground(
                special,
                space
            )
        );

        box.addView(
            main,
            new FrameLayout.LayoutParams(
                -1,
                -1
            )
        );

        String hint=
            (!symbols && page==0)
            ? hintFor(action)
            : "";

        if(!hint.isEmpty()) {

            TextView small=
                new TextView(this);

            small.setText(hint);
            small.setTextColor(
                textColor()
            );

            small.setAlpha(.55f);
            small.setTextSize(8);
            small.setGravity(Gravity.CENTER);

            FrameLayout.LayoutParams hp=
                new FrameLayout.LayoutParams(
                    dp(20),
                    dp(16),
                    Gravity.TOP |
                    Gravity.CENTER_HORIZONTAL
                );

            hp.setMargins(
                0,
                dp(2),
                0,
                0
            );

            box.addView(
                small,
                hp
            );

        }

        // Gboard-style comma key: tap still types comma. Holding it opens
        // a compact shortcut bubble above the key; the center smiley opens emoji.
        box.setOnLongClickListener(v -> {

            // SPACE gestures are handled entirely in onTouch:
            // tap = space, slide = cursor, stationary hold = keyboard picker.
            if(action.equals("SPACE"))
                return true;

            if(action.equals(",") && page==0 && !symbols) {
                dismissKeyPreview();
                showCommaShortcutPopup(v);
                return true;
            }

            if(
                quickPunctuation &&
                action.equals(".") &&
                page==0 &&
                !symbols
            ) {
                dismissKeyPreview();
                showLongPressPopup(
                    v,
                    ".|,|?|!|:|;|@|#|&"
                );
                return true;
            }

            String choices=alternativesFor(action);

            if(choices.isEmpty()) {
                String visible=main.getText()==null
                    ? ""
                    : main.getText().toString();
                choices=alternativesFor(visible);
            }

            if(choices.isEmpty())
                return false;

            dismissKeyPreview();
            showLongPressPopup(v,choices);
            return true;
        });

        if(
            page==0 &&
            !symbols &&
            action!=null &&
            action.length()==1 &&
            Character.isLetter(
                action.charAt(0)
            )
        ) {
            glideLetterViews.add(box);
            glideLetterActions.add(
                action.toLowerCase(
                    java.util.Locale.ROOT
                )
            );
        }

        box.setOnTouchListener((v,e)->{

            if(action.equals("SPACE"))
                return handleSpacebarTouch(v,e);

            if(isGlideLetterAction(action)) {
                boolean glideConsumed=
                    handleGlideKeyTouch(
                        v,
                        e,
                        action
                    );

                if(glideConsumed)
                    return true;
            }

            if(dragChoiceActive) {
                int a=e.getActionMasked();

                if(
                    a==MotionEvent.ACTION_MOVE ||
                    a==MotionEvent.ACTION_UP ||
                    a==MotionEvent.ACTION_CANCEL
                ) {
                    if(
                        a==MotionEvent.ACTION_UP ||
                        a==MotionEvent.ACTION_CANCEL
                    ) {
                        v.animate()
                         .scaleX(1f)
                         .scaleY(1f)
                         .setDuration(45)
                         .start();
                        v.setPressed(false);
                        v.post(() -> {
                            v.setPressed(false);
                            v.jumpDrawablesToCurrentState();
                        });
                        dismissKeyPreview();
                    }

                    return handleDragChoiceTouch(e);
                }
            }

            if(e.getAction()==MotionEvent.ACTION_DOWN) {
                // Soft Gboard-like press feedback. Keep it fast so typing
                // still feels responsive instead of rigid.
                v.animate()
                 .scaleX(1.035f)
                 .scaleY(1.035f)
                 .setDuration(30)
                 .start();

                // Never pop the KeyKii spacebar text. Preview only an
                // actual one-character typing key.
                if(
                    keyPreviewEnabled &&
                    !special &&
                    !action.equals("SPACE") &&
                    shown!=null &&
                    shown.codePointCount(0,shown.length())==1
                ) {
                    showKeyPreview(v,shown);
                }

                if(keySound)
                    playKeySound();

                if(haptic)
                    v.performHapticFeedback(
                        HapticFeedbackConstants.KEYBOARD_TAP
                    );
            }

            if(
                e.getAction()==MotionEvent.ACTION_UP ||
                e.getAction()==MotionEvent.ACTION_CANCEL
            ) {
                v.animate()
                 .scaleX(1f)
                 .scaleY(1f)
                 .setDuration(45)
                 .start();

                dismissKeyPreview();
            }

            if(
                action.equals("BACK") &&
                e.getAction()==MotionEvent.ACTION_DOWN
            ){
                backspaceRepeating=false;
                suppressBackspaceClick=false;
                backspaceSwipeActive=false;
                backspaceGestureStartX=e.getRawX();

                repeatBackspaceHandler.postDelayed(
                    repeatBackspaceRunnable,
                    330
                );
            }

            if(
                action.equals("BACK") &&
                e.getAction()==MotionEvent.ACTION_MOVE &&
                swipeDeleteWord
            ){
                float dx=
                    e.getRawX()-
                    backspaceGestureStartX;

                if(dx<=-dp(42)) {
                    repeatBackspaceHandler.removeCallbacks(
                        repeatBackspaceRunnable
                    );

                    if(!backspaceSwipeActive) {
                        InputConnection ic=
                            getCurrentInputConnection();

                        if(ic!=null)
                            deletePreviousWord(ic);

                        backspaceSwipeActive=true;
                        suppressBackspaceClick=true;

                        if(haptic)
                            v.performHapticFeedback(
                                HapticFeedbackConstants.LONG_PRESS
                            );
                    }

                    return true;
                }
            }

            if(
                action.equals("BACK") &&
                (
                    e.getAction()==MotionEvent.ACTION_UP ||
                    e.getAction()==MotionEvent.ACTION_CANCEL
                )
            ){
                repeatBackspaceHandler.removeCallbacks(
                    repeatBackspaceRunnable
                );

                if(
                    backspaceRepeating ||
                    backspaceSwipeActive
                ) {
                    suppressBackspaceClick=true;
                }

                backspaceRepeating=false;
                backspaceSwipeActive=false;
            }

            return false;
        });

        box.setOnClickListener(v -> {

            if(
                isGlideLetterAction(action) &&
                android.os.SystemClock.uptimeMillis()-
                    lastGlideEndTime<180
            ) {
                return;
            }

            if(
                action.equals("BACK") &&
                suppressBackspaceClick
            ){
                suppressBackspaceClick=false;
                return;
            }

            press(action);

            v.setPressed(false);
            v.post(() -> {
                v.setPressed(false);
                v.jumpDrawablesToCurrentState();
            });
        });

        int effectiveKeyHeight;

        if(hand!=0) {
            effectiveKeyHeight=
                Math.min(
                    keyHeight,
                    39
                );

        } else if(!wideMode) {
            effectiveKeyHeight=
                Math.min(
                    keyHeight,
                    40
                );

        } else {
            // Even the true full-width keyboard keeps the same clean,
            // compact proportions instead of becoming tall.
            effectiveKeyHeight=
                Math.min(
                    keyHeight,
                    42
                );
        }

        boolean mainNumberKey=
                page==0 &&
                !symbols &&
                action!=null &&
                action.length()==1 &&
                Character.isDigit(
                    action.charAt(0)
                );

        if(mainNumberKey)
            effectiveKeyHeight=
                Math.max(
                    34,
                    effectiveKeyHeight-4
                );

        int height=dp(effectiveKeyHeight);

        LinearLayout.LayoutParams p=
            new LinearLayout.LayoutParams(
                0,
                height,
                weight
            );

        p.setMargins(
            dp(1),
            dp(2),
            dp(1),
            dp(2)
        );

        r.addView(box,p);
    }


    void playKeySound() {
        if(audioManager==null)
            return;

        float volume=
            Math.max(
                0.05f,
                Math.min(
                    1f,
                    keySoundVolume/100f
                )
            );

        try {
            audioManager.playSoundEffect(
                android.media.AudioManager.FX_KEY_CLICK,
                volume
            );
        } catch(Exception ignored) {
        }
    }


    void openKeyboardPicker() {
        android.view.inputmethod.InputMethodManager imm=
            (android.view.inputmethod.InputMethodManager)
            getSystemService(INPUT_METHOD_SERVICE);

        if(imm!=null)
            imm.showInputMethodPicker();
    }


    void beginSpaceCursorGesture() {
        spaceStartSelection=-1;
        spaceMinSelection=0;
        spaceMaxSelection=0;
        spaceFallbackStep=0;

        InputConnection ic=getCurrentInputConnection();
        if(ic==null) return;

        try {
            ExtractedTextRequest req=new ExtractedTextRequest();
            ExtractedText et=ic.getExtractedText(req,0);

            if(et!=null && et.text!=null && et.selectionStart>=0) {
                spaceMinSelection=et.startOffset;
                spaceMaxSelection=et.startOffset+et.text.length();
                spaceStartSelection=et.startOffset+et.selectionStart;
            }
        } catch(Exception ignored) {}
    }


    void moveCursorFromSpace(float dx) {
        InputConnection ic=getCurrentInputConnection();
        if(ic==null) return;

        int stepPx=Math.max(dp(14),1);
        int steps=Math.round(dx/(float)stepPx);

        if(spaceStartSelection>=0) {
            int target=spaceStartSelection+steps;
            target=Math.max(spaceMinSelection,Math.min(spaceMaxSelection,target));
            try {
                ic.setSelection(target,target);
                return;
            } catch(Exception ignored) {}
        }

        // Fallback for editors that do not expose selection text.
        int delta=steps-spaceFallbackStep;
        if(delta==0) return;

        int key=delta<0
            ? KeyEvent.KEYCODE_DPAD_LEFT
            : KeyEvent.KEYCODE_DPAD_RIGHT;

        for(int i=0;i<Math.min(Math.abs(delta),12);i++)
            sendKey(ic,key);

        spaceFallbackStep=steps;
    }


    boolean handleSpacebarTouch(View v, MotionEvent e) {
        int a=e.getActionMasked();

        if(a==MotionEvent.ACTION_DOWN) {
            spaceDownX=e.getX();
            spaceCursorDragging=false;
            spacePickerShown=false;
            if(page==1 && emojiSearchMode) {
                if(emojiSearchQuery==null)
                    emojiSearchQuery="";

                emojiSearchCursor=Math.max(
                    0,
                    Math.min(
                        emojiSearchCursor,
                        emojiSearchQuery.length()
                    )
                );

                emojiSearchGestureStart=emojiSearchCursor;
            } else {
                beginSpaceCursorGesture();
            }

            v.animate()
             .scaleX(1.02f)
             .scaleY(1.02f)
             .setDuration(30)
             .start();

            if(haptic)
                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);

            spaceGestureHandler.removeCallbacksAndMessages(null);
            spaceGestureHandler.postDelayed(() -> {
                if(!spaceCursorDragging) {
                    spacePickerShown=true;
                    openKeyboardPicker();
                }
            },650);

            return true;
        }

        if(a==MotionEvent.ACTION_MOVE) {
            float dx=e.getX()-spaceDownX;

            if(Math.abs(dx)>=dp(9)) {
                if(!spaceCursorDragging) {
                    spaceCursorDragging=true;
                    spaceGestureHandler.removeCallbacksAndMessages(null);
                }
                if(page==1 && emojiSearchMode)
                    moveEmojiSearchCursorFromSpace(dx);
                else
                    moveCursorFromSpace(dx);
            }

            return true;
        }

        if(a==MotionEvent.ACTION_UP || a==MotionEvent.ACTION_CANCEL) {
            spaceGestureHandler.removeCallbacksAndMessages(null);

            v.animate()
             .scaleX(1f)
             .scaleY(1f)
             .setDuration(45)
             .start();

            if(
                a==MotionEvent.ACTION_UP &&
                !spaceCursorDragging &&
                !spacePickerShown
            ) {
                press("SPACE");
            }

            spaceCursorDragging=false;
            spacePickerShown=false;
            return true;
        }

        return true;
    }



    boolean canRenderEmoji(String value) {

        if(value==null || value.trim().isEmpty())
            return false;

        if(
            value.equals("□") ||
            value.equals("�")
        )
            return false;

        if(android.os.Build.VERSION.SDK_INT >= 23) {

            try {

                android.graphics.Paint paint=
                    new android.graphics.Paint();

                paint.setTypeface(
                    android.graphics.Typeface.DEFAULT
                );

                return paint.hasGlyph(value);

            } catch(Exception ignored) {}
        }

        return true;
    }



    java.util.ArrayList<String[]> fastEmojiDb=null;
    java.util.HashMap<String,Integer> fastEmojiJump=
        new java.util.HashMap<>();
    android.widget.ListView fastEmojiList=null;


    void loadFastEmojiDb() {

        if(fastEmojiDb!=null)
            return;

        fastEmojiDb=new java.util.ArrayList<>();

        try {

            java.io.BufferedReader r=
                new java.io.BufferedReader(
                    new java.io.InputStreamReader(
                        getAssets().open(
                            "keykii-emojis.txt"
                        ),
                        "UTF-8"
                    )
                );

            String line;

            while((line=r.readLine())!=null) {

                String[] x=line.split("\\t",-1);

                if(x.length<3)
                    continue;

                if(
                    x[0].equalsIgnoreCase(
                        "Component"
                    )
                )
                    continue;

                fastEmojiDb.add(x);
            }

            r.close();

        } catch(Exception ignored) {}
    }


    String fastGroupLabel(String g) {

        if(g.equals("Smileys & Emotion"))
            return "Smileys & Emotions";

        if(g.equals("People & Body"))
            return "People";

        if(g.equals("Animals & Nature"))
            return "Animals & Nature";

        if(g.equals("Food & Drink"))
            return "Food & Drink";

        if(g.equals("Travel & Places"))
            return "Travel & Places";

        return g;
    }



    java.util.HashMap<String,Boolean> emojiGlyphCache=
        new java.util.HashMap<>();

    boolean hasSkinToneModifier(String emoji) {

        if(emoji==null) return false;

        for(int i=0;i<emoji.length();) {

            int cp=emoji.codePointAt(i);

            if(cp>=0x1F3FB && cp<=0x1F3FF)
                return true;

            i+=Character.charCount(cp);
        }

        return false;
    }


    android.graphics.Paint emojiTestPaint=null;

    boolean displayableEmoji(String emoji) {

        if(emoji==null || emoji.trim().isEmpty())
            return false;

        if(
            emoji.equals("□") ||
            emoji.equals("�")
        )
            return false;

        Boolean cached=emojiGlyphCache.get(emoji);

        if(cached!=null)
            return cached;

        boolean ok=true;

        // Android's Character table is a stronger guard than hasGlyph()
        // for newly-added code points that otherwise appear as a blank box.
        for(int i=0;i<emoji.length();) {
            int cp=emoji.codePointAt(i);
            int type=Character.getType(cp);

            if(type==Character.UNASSIGNED) {
                ok=false;
                break;
            }

            i+=Character.charCount(cp);
        }

        if(ok && android.os.Build.VERSION.SDK_INT>=23) {
            try {
                if(emojiTestPaint==null) {
                    emojiTestPaint=new android.graphics.Paint();
                    emojiTestPaint.setTypeface(
                        android.graphics.Typeface.DEFAULT
                    );
                    emojiTestPaint.setTextSize(dp(30));
                }

                ok=emojiTestPaint.hasGlyph(emoji);
            } catch(Exception ignored) {}
        }

        emojiGlyphCache.put(emoji,ok);
        return ok;
    }


    java.util.ArrayList<String> loadEmojiFavorites() {
        java.util.ArrayList<String> out=
            new java.util.ArrayList<>();

        android.content.SharedPreferences prefs=
            getSharedPreferences(
                "keykii_prefs",
                android.content.Context.MODE_PRIVATE
            );

        String raw=prefs.getString(
            "emoji_favorites_v1",
            ""
        );

        if(raw!=null && !raw.isEmpty()) {
            for(String emoji:raw.split("~~K~~")) {
                if(
                    displayableEmoji(emoji) &&
                    !out.contains(emoji)
                ) {
                    out.add(emoji);
                }

                if(out.size()>=48)
                    break;
            }
        }

        return out;
    }


    boolean toggleEmojiFavorite(String emoji) {
        if(emoji==null || emoji.isEmpty())
            return false;

        java.util.ArrayList<String> list=
            loadEmojiFavorites();

        boolean added;

        if(list.contains(emoji)) {
            list.remove(emoji);
            added=false;
        } else {
            list.add(0,emoji);
            added=true;
        }

        while(list.size()>48)
            list.remove(list.size()-1);

        StringBuilder joined=
            new StringBuilder();

        for(String value:list) {
            if(joined.length()>0)
                joined.append("~~K~~");

            joined.append(value);
        }

        getSharedPreferences(
            "keykii_prefs",
            android.content.Context.MODE_PRIVATE
        ).edit()
         .putString(
             "emoji_favorites_v1",
             joined.toString()
         )
         .apply();

        return added;
    }


    void rememberFastRecent(String emoji) {

        if(emoji==null || emoji.isEmpty())
            return;

        android.content.SharedPreferences prefs=
            getSharedPreferences(
                "keykii_prefs",
                android.content.Context.MODE_PRIVATE
            );

        String raw=prefs.getString(
            "fast_recent_v2",
            ""
        );

        java.util.ArrayList<String> list=
            new java.util.ArrayList<>();

        list.add(emoji);

        if(!raw.isEmpty()) {

            for(String old:raw.split("~~K~~")) {

                if(
                    !old.isEmpty() &&
                    !old.equals(emoji) &&
                    list.size()<24
                )
                    list.add(old);
            }
        }

        StringBuilder joined=
            new StringBuilder();

        for(String value:list) {

            if(joined.length()>0)
                joined.append("~~K~~");

            joined.append(value);
        }

        prefs.edit()
            .putString(
                "fast_recent_v2",
                joined.toString()
            )
            .apply();
    }


    java.util.ArrayList<String> loadFastRecent() {

        java.util.ArrayList<String> out=
            new java.util.ArrayList<>();

        android.content.SharedPreferences prefs=
            getSharedPreferences(
                "keykii_prefs",
                android.content.Context.MODE_PRIVATE
            );

        String raw=prefs.getString(
            "fast_recent_v2",
            ""
        );

        if(raw!=null && !raw.isEmpty()) {
            for(String emoji:raw.split("~~K~~")) {
                if(
                    displayableEmoji(emoji) &&
                    !out.contains(emoji)
                ) {
                    out.add(emoji);
                }
            }
        }

        // Keep older recent history from earlier KeyKii versions.
        if(out.isEmpty()) {
            android.content.SharedPreferences legacy=
                getSharedPreferences(
                    "keykii_emoji",
                    MODE_PRIVATE
                );

            String old=legacy.getString("recent","");

            if(old!=null && !old.trim().isEmpty()) {
                for(String emoji:old.trim().split(" ")) {
                    if(
                        displayableEmoji(emoji) &&
                        !out.contains(emoji)
                    ) {
                        out.add(emoji);
                    }
                    if(out.size()>=24) break;
                }
            }
        }

        return out;
    }


    java.util.ArrayList<Object>
    makeFastEmojiRows(String query) {

        loadFastEmojiDb();

        java.util.ArrayList<Object> rows=
            new java.util.ArrayList<>();

        fastEmojiJump.clear();

        String q=query==null
            ? ""
            : query.trim().toLowerCase();

        if(q.isEmpty()) {
            fastEmojiJump.put("Favorites",rows.size());
            rows.add("Favorites");

            java.util.ArrayList<String> favorites=
                loadEmojiFavorites();

            for(int i=0;i<favorites.size();i+=10) {
                java.util.ArrayList<String> row=
                    new java.util.ArrayList<>();

                for(
                    int j=i;
                    j<Math.min(i+10,favorites.size());
                    j++
                ) {
                    row.add(favorites.get(j));
                }

                if(!row.isEmpty())
                    rows.add(row);
            }

            fastEmojiJump.put("Recent emoji",rows.size());
            rows.add("Recent Emoji");

            java.util.ArrayList<String> recent=loadFastRecent();

            for(int i=0;i<recent.size();i+=10) {
                java.util.ArrayList<String> row=
                    new java.util.ArrayList<>();

                for(int j=i;j<Math.min(i+10,recent.size());j++)
                    row.add(recent.get(j));

                if(!row.isEmpty()) rows.add(row);
            }
        }

        java.util.LinkedHashMap<
            String,
            java.util.ArrayList<String>
        > groups=new java.util.LinkedHashMap<>();

        java.util.HashSet<String> seen=
            new java.util.HashSet<>();

        for(String[] x:fastEmojiDb) {

            if(x.length<3) continue;

            String group=x[0];
            String subgroup=x.length>1 ? x[1] : "";
            String emoji=x[2];
            String name=x.length>3 ? x[3] : "";

            if(group.equalsIgnoreCase("Component"))
                continue;

            // Main grid shows the default emoji only.
            // Tone variants are available by long press.
            if(hasSkinToneModifier(emoji))
                continue;

            if(!displayableEmoji(emoji))
                continue;

            if(!q.isEmpty()) {
                String haystack=(
                    group+" "+subgroup+" "+name
                ).toLowerCase();

                if(!emojiSearchMatches(haystack,q))
                    continue;

                group="Search results";
            }

            String unique=group+"\n"+emoji;
            if(!seen.add(unique)) continue;

            java.util.ArrayList<String> list=groups.get(group);

            if(list==null) {
                list=new java.util.ArrayList<>();
                groups.put(group,list);
            }

            list.add(emoji);
        }

        for(
            java.util.Map.Entry<
                String,
                java.util.ArrayList<String>
            > entry:groups.entrySet()
        ) {
            String rawGroup=entry.getKey();

            fastEmojiJump.put(rawGroup,rows.size());

            rows.add(
                rawGroup.equals("Search results")
                ? "Search results"
                : fastGroupLabel(rawGroup)
            );

            java.util.ArrayList<String> list=entry.getValue();

            for(int i=0;i<list.size();i+=10) {
                java.util.ArrayList<String> row=
                    new java.util.ArrayList<>();

                for(int j=i;j<Math.min(i+10,list.size());j++)
                    row.add(list.get(j));

                if(!row.isEmpty()) rows.add(row);
            }
        }

        return rows;
    }



    boolean emojiSearchMatches(String haystack, String q) {

        if(haystack==null || q==null)
            return false;

        haystack=haystack.toLowerCase();
        q=q.trim().toLowerCase();

        if(q.isEmpty())
            return true;

        // Common feeling words should behave like an emoji search, not
        // a raw substring search. This also keeps searches such as
        // "key" from matching monKEY / turKEY.
        if(q.equals("sad") || q.equals("sadness") || q.equals("unhappy")) {
            return haystack.contains("sad") ||
                   haystack.contains("frown") ||
                   haystack.contains("disappoint") ||
                   haystack.contains("worr") ||
                   haystack.contains("plead") ||
                   haystack.contains("tear") ||
                   haystack.contains("cry") ||
                   haystack.contains("anguish") ||
                   haystack.contains("weary") ||
                   haystack.contains("pensive") ||
                   haystack.contains("downcast") ||
                   haystack.contains("sorrow");
        }

        if(q.equals("cry") || q.equals("crying")) {
            return haystack.contains("crying") ||
                   haystack.contains("tear") ||
                   haystack.contains("sob");
        }

        if(q.equals("sick") || q.equals("ill")) {
            return haystack.contains("nauseat") ||
                   haystack.contains("vomit") ||
                   haystack.contains("thermometer") ||
                   haystack.contains("mask") ||
                   haystack.contains("sneez") ||
                   haystack.contains("fever") ||
                   haystack.contains("woozy") ||
                   haystack.contains("sick");
        }

        if(q.equals("happy") || q.equals("happiness")) {
            return haystack.contains("smil") ||
                   haystack.contains("grin") ||
                   haystack.contains("joy") ||
                   haystack.contains("laugh") ||
                   haystack.contains("heart");
        }

        if(q.equals("love")) {
            return haystack.contains("love") ||
                   haystack.contains("heart") ||
                   haystack.contains("kiss");
        }

        if(q.equals("angry") || q.equals("mad")) {
            return haystack.contains("angry") ||
                   haystack.contains("rage") ||
                   haystack.contains("pouting") ||
                   haystack.contains("steam");
        }

        if(q.equals("sleep") || q.equals("sleepy")) {
            return haystack.contains("sleep") ||
                   haystack.contains("tired") ||
                   haystack.contains("drowsy") ||
                   haystack.contains("zzz");
        }

        String normalized=haystack
            .replace('-', ' ')
            .replace('_', ' ')
            .replace('/', ' ')
            .replace(':', ' ');

        String[] tokens=normalized.split("[^a-z0-9]+");
        String[] terms=q.split("\\s+");

        for(String term:terms) {
            if(term.isEmpty()) continue;

            boolean found=false;
            String stem=term.length()>=5
                ? term.substring(0,term.length()-1)
                : term;

            for(String token:tokens) {
                if(token.isEmpty()) continue;

                if(
                    token.equals(term) ||
                    token.startsWith(term) ||
                    (stem.length()>=4 && token.startsWith(stem))
                ) {
                    found=true;
                    break;
                }
            }

            if(!found) return false;
        }

        return true;
    }


    java.util.ArrayList<String>
    findFastEmojiMatches(String query,int limit) {

        loadFastEmojiDb();

        java.util.ArrayList<String> results=
            new java.util.ArrayList<>();

        java.util.HashSet<String> seen=
            new java.util.HashSet<>();

        String q=query==null
            ? ""
            : query.trim().toLowerCase();

        if(q.isEmpty())
            return results;

        for(String[] x:fastEmojiDb) {
            if(x.length<3) continue;

            String group=x[0];
            String subgroup=x.length>1 ? x[1] : "";
            String emoji=x[2];
            String name=x.length>3 ? x[3] : "";

            if(group.equalsIgnoreCase("Component"))
                continue;

            if(hasSkinToneModifier(emoji))
                continue;

            String haystack=(
                group+" "+subgroup+" "+name
            ).toLowerCase();

            if(!emojiSearchMatches(haystack,q))
                continue;

            if(!displayableEmoji(emoji))
                continue;

            if(seen.add(emoji))
                results.add(emoji);

            if(results.size()>=limit)
                break;
        }

        return results;
    }


    void refreshEmojiSearchResults() {

        if(emojiSearchResultsRow==null || emojiSearchResultsScroll==null)
            return;

        emojiSearchResultsRow.removeAllViews();

        String q=emojiSearchQuery==null
            ? ""
            : emojiSearchQuery.trim();

        if(q.isEmpty()) {
            emojiSearchResultsScroll.setVisibility(View.GONE);
            return;
        }

        java.util.ArrayList<String> results=
            findFastEmojiMatches(q,24);

        if(results.isEmpty()) {
            TextView empty=new TextView(this);
            empty.setText("No emoji found");
            empty.setTextSize(14);
            empty.setTextColor(textColor());
            empty.setAlpha(.65f);
            empty.setGravity(Gravity.CENTER_VERTICAL);
            empty.setPadding(dp(12),0,dp(12),0);
            emojiSearchResultsRow.addView(
                empty,
                new LinearLayout.LayoutParams(dp(150),dp(56))
            );
        } else {
            for(String value:results) {
                TextView e=new TextView(this);
                e.setText(value);
                e.setTextSize(28);
                e.setGravity(Gravity.CENTER);
                e.setIncludeFontPadding(false);
                e.setOnClickListener(v -> fastCommitEmoji(value));
                e.setOnLongClickListener(v -> {
                    java.util.ArrayList<String> variants=emojiToneVariants(value);
                    if(variants.size()>1) {
                        showEmojiVariantPopup(v,value);
                        return true;
                    }
                    return false;
                });
                e.setOnTouchListener((v,event) ->
                    handleDragChoiceTouch(event)
                );
                emojiSearchResultsRow.addView(
                    e,
                    new LinearLayout.LayoutParams(dp(52),dp(56))
                );
            }
        }

        emojiSearchResultsScroll.setVisibility(View.VISIBLE);
    }


    void ensureKeyPreview() {

        if(keyPreviewPopup!=null && keyPreviewText!=null)
            return;

        keyPreviewText=new TextView(this);
        keyPreviewText.setGravity(Gravity.CENTER);
        keyPreviewText.setTextSize(28);
        keyPreviewText.setTextColor(textColor());
        keyPreviewText.setIncludeFontPadding(false);
        keyPreviewText.setBackground(
            round(keyColor(false),18,borderColor())
        );

        keyPreviewPopup=new PopupWindow(
            keyPreviewText,
            dp(52),
            dp(64),
            false
        );

        keyPreviewPopup.setTouchable(false);
        keyPreviewPopup.setOutsideTouchable(false);
        keyPreviewPopup.setBackgroundDrawable(
            new android.graphics.drawable.ColorDrawable(
                android.graphics.Color.TRANSPARENT
            )
        );

        if(android.os.Build.VERSION.SDK_INT>=21)
            keyPreviewPopup.setElevation(dp(8));
    }


    void showKeyPreview(View anchor, String text) {

        if(anchor==null || text==null || text.isEmpty())
            return;

        ensureKeyPreview();

        keyPreviewText.setText(text);
        keyPreviewText.setTextColor(textColor());
        keyPreviewText.setBackground(
            round(keyColor(false),18,borderColor())
        );

        if(keyPreviewPopup.isShowing())
            keyPreviewPopup.dismiss();

        int x=(anchor.getWidth()-dp(52))/2;
        int y=-anchor.getHeight()-dp(67);

        keyPreviewPopup.showAsDropDown(anchor,x,y);
    }


    void dismissKeyPreview() {
        if(keyPreviewPopup!=null && keyPreviewPopup.isShowing())
            keyPreviewPopup.dismiss();
    }


    String removeSkinTone(String emoji) {

        StringBuilder out=new StringBuilder();

        for(int i=0;i<emoji.length();) {

            int cp=emoji.codePointAt(i);

            // Fitzpatrick skin-tone modifiers
            if(cp<0x1F3FB || cp>0x1F3FF)
                out.appendCodePoint(cp);

            i+=Character.charCount(cp);
        }

        return out.toString();
    }


    String emojiToneFamilyKey(String emoji) {

        if(emoji==null) return "";

        StringBuilder out=new StringBuilder();

        for(int i=0;i<emoji.length();) {
            int cp=emoji.codePointAt(i);

            // Ignore Fitzpatrick modifiers and text/emoji presentation
            // selectors when matching a base emoji to its tone variants.
            // Unicode stores forms such as ✌️ and ✌🏻 differently.
            if(
                !(cp>=0x1F3FB && cp<=0x1F3FF) &&
                cp!=0xFE0E &&
                cp!=0xFE0F
            ) {
                out.appendCodePoint(cp);
            }

            i+=Character.charCount(cp);
        }

        return out.toString();
    }


    java.util.ArrayList<String>
    emojiToneVariants(String emoji) {

        loadFastEmojiDb();

        java.util.ArrayList<String> out=
            new java.util.ArrayList<>();

        String base=removeSkinTone(emoji);
        String familyKey=emojiToneFamilyKey(emoji);

        if(familyKey.isEmpty() || !displayableEmoji(base))
            return out;

        out.add(base);

        final int[] wanted={
            0x1F3FB,0x1F3FC,0x1F3FD,0x1F3FE,0x1F3FF
        };

        for(int tone:wanted) {
            String found=null;

            for(String[] x:fastEmojiDb) {
                if(x.length<3) continue;

                String candidate=x[2];
                if(!emojiToneFamilyKey(candidate).equals(familyKey))
                    continue;

                int count=0;
                int candidateTone=-1;
                boolean allSameTone=true;

                for(int i=0;i<candidate.length();) {
                    int cp=candidate.codePointAt(i);

                    if(cp>=0x1F3FB && cp<=0x1F3FF) {
                        if(count==0)
                            candidateTone=cp;
                        else if(candidateTone!=cp)
                            allSameTone=false;

                        count++;
                    }

                    i+=Character.charCount(cp);
                }

                // Single-person emoji normally has one tone modifier.
                // Couple/heart/kiss sequences can contain two modifiers.
                // For the compact popup, accept one or more modifiers when
                // every person uses the same requested skin tone. This gives
                // the same default + five-tone selector for multi-person emoji.
                if(
                    count>=1 &&
                    allSameTone &&
                    candidateTone==tone &&
                    displayableEmoji(candidate)
                ) {
                    found=candidate;
                    break;
                }
            }

            if(found!=null && !out.contains(found))
                out.add(found);
        }

        if(out.size()<2)
            out.clear();

        return out;
    }


    void showEmojiVariantPopup(
        View anchor,
        String emoji
    ) {
        java.util.ArrayList<String> variants=
            emojiToneVariants(emoji);

        if(variants.size()<2)
            return;

        showDragChoicePopup(
            anchor,
            variants,
            variants,
            "EMOJI",
            0
        );
    }


    void fastCommitEmoji(String emoji) {

        InputConnection ic=
            getCurrentInputConnection();

        if(ic!=null)
            ic.commitText(emoji,1);

        rememberEmoji(emoji);
        rememberFastRecent(emoji);
    }


    TextView fastEmojiModeButton(
        String text
    ) {

        TextView b=new TextView(this);

        b.setText(text);
        b.setTextSize(18);
        b.setTextColor(textColor());
        b.setGravity(Gravity.CENTER);

        b.setBackground(
            round(
                keyColor(false),
                16,
                borderColor()
            )
        );

        return b;
    }


    void addFastEmojiModeBar() {
        LinearLayout bar=new LinearLayout(this);
        bar.setGravity(Gravity.CENTER);

        TextView abc=fastEmojiModeButton("ABC");
        TextView emoji=fastEmojiModeButton("☺");
        TextView gif=fastEmojiModeButton("GIF");
        TextView sticker=fastEmojiModeButton("▧");
        TextView kao=fastEmojiModeButton(":-)");
        TextView del=fastEmojiModeButton("⌫");

        // Selected emoji tab, similar to Gboard, while keeping KeyKii colors.
        int selectedColor = theme==1
            ? Color.rgb(201,218,255)
            : Color.argb(120,150,180,255);
        if(kaomojiMode)
            kao.setBackground(round(selectedColor,16,borderColor()));
        else
            emoji.setBackground(round(selectedColor,16,borderColor()));

        // GIF/sticker are visual placeholders only until those features exist.
        gif.setAlpha(.35f);
        sticker.setAlpha(.35f);
        gif.setClickable(false);
        sticker.setClickable(false);

        abc.setOnClickListener(v -> {
            page=0;
            symbols=false;
            symbolPage=1;
            shift=false;
            capsLock=false;
            emojiSearchMode=false;
            emojiSearchQuery="";
            kaomojiMode=false;
            showPage();
        });

        emoji.setOnClickListener(v -> {
            kaomojiMode=false;
            emojiSearchMode=false;
            emojiSearchQuery="";
            showPage();
        });

        kao.setOnClickListener(v -> {
            kaomojiMode=true;
            emojiSearchMode=false;
            emojiSearchQuery="";
            showPage();
        });

        del.setOnTouchListener((v,e) -> {
            if(e.getAction()==MotionEvent.ACTION_DOWN) {
                backspaceRepeating=false;
                suppressBackspaceClick=false;
                repeatBackspaceHandler.postDelayed(
                    repeatBackspaceRunnable,
                    330
                );
            }

            if(
                e.getAction()==MotionEvent.ACTION_UP ||
                e.getAction()==MotionEvent.ACTION_CANCEL
            ) {
                repeatBackspaceHandler.removeCallbacks(
                    repeatBackspaceRunnable
                );

                if(backspaceRepeating)
                    suppressBackspaceClick=true;

                backspaceRepeating=false;
            }

            return false;
        });

        del.setOnClickListener(v -> {
            if(suppressBackspaceClick) {
                suppressBackspaceClick=false;
                return;
            }

            InputConnection ic=getCurrentInputConnection();
            if(ic!=null)
                deleteOneBeforeCursor(ic);
        });

        TextView[] buttons={abc,emoji,gif,sticker,kao,del};
        for(TextView b:buttons) {
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(46),1);
            lp.setMargins(dp(2),dp(3),dp(2),dp(3));
            bar.addView(b,lp);
        }

        body.addView(bar,new LinearLayout.LayoutParams(-1,dp(52)));
    }

    void highlightEmojiCategory(TextView[] buttons,int selected) {
        for(int i=0;i<buttons.length;i++) {
            int color=theme==1
                ? Color.rgb(201,218,255)
                : Color.argb(120,150,180,255);
            buttons[i].setBackground(
                i==selected ? round(color,20,Color.TRANSPARENT) : null
            );
        }
    }

    void highlightKaomojiCategory(TextView[] buttons,int selected) {
        for(int i=0;i<buttons.length;i++)
            buttons[i].setBackground(
                i==selected
                ? round(keyColor(false),18,borderColor())
                : null
            );
    }


    void buildEmoji() {

        // SEARCH / BACK ROW
        LinearLayout searchRow=
            new LinearLayout(this);

        searchRow.setGravity(
            Gravity.CENTER_VERTICAL
        );

        TextView back=
            new TextView(this);

        back.setText("←");
        back.setTextSize(23);
        back.setTextColor(textColor());
        back.setGravity(Gravity.CENTER);
        back.setBackground(
            round(
                keyColor(true),
                18,
                Color.TRANSPARENT
            )
        );

        back.setOnClickListener(v -> {
            if(emojiSearchMode || kaomojiMode) {
                emojiSearchMode=false;
                emojiSearchQuery="";
                kaomojiMode=false;
                showPage();
            } else {
                page=0;
                showPage();
            }
        });

        searchRow.addView(
            back,
            new LinearLayout.LayoutParams(
                dp(46),
                dp(42)
            )
        );

        TextView search=
            new TextView(this);

        emojiSearchField=search;

        renderEmojiSearchField();

        search.setTextSize(15);
        search.setTextColor(textColor());
        search.setGravity(
            Gravity.CENTER_VERTICAL
        );

        search.setPadding(
            dp(14),0,dp(8),0
        );

        search.setBackground(
            round(
                keyColor(false),
                17,
                borderColor()
            )
        );

        search.setOnClickListener(v -> {

            if(kaomojiMode) {

                kaomojiMode=false;
                showPage();
                return;
            }

            emojiSearchMode=true;
            emojiSearchCursor=
                emojiSearchQuery==null
                ? 0
                : emojiSearchQuery.length();
            symbols=false;
            symbolPage=1;
            shift=false;
            capsLock=false;
            lastShiftTap=0L;
            showPage();
        });

        searchRow.addView(
            search,
            new LinearLayout.LayoutParams(
                0,
                dp(42),
                1
            )
        );

        TextView close=
            new TextView(this);

        close.setText(
            (emojiSearchMode || kaomojiMode)
            ? "×"
            : "★"
        );
        close.setTextSize(22);
        close.setTextColor(textColor());
        close.setGravity(Gravity.CENTER);
        close.setVisibility(View.VISIBLE);

        close.setOnTouchListener((v,e) -> {

            if(
                emojiSearchMode &&
                emojiSearchQuery!=null &&
                !emojiSearchQuery.isEmpty() &&
                e.getAction()==MotionEvent.ACTION_DOWN
            ) {
                backspaceRepeating=false;
                suppressBackspaceClick=false;
                repeatBackspaceHandler.postDelayed(
                    repeatBackspaceRunnable,
                    330
                );
            }

            if(
                e.getAction()==MotionEvent.ACTION_UP ||
                e.getAction()==MotionEvent.ACTION_CANCEL
            ) {
                repeatBackspaceHandler.removeCallbacks(
                    repeatBackspaceRunnable
                );

                if(backspaceRepeating)
                    suppressBackspaceClick=true;

                backspaceRepeating=false;
            }

            return false;
        });

        close.setOnClickListener(v -> {

            if(!emojiSearchMode && !kaomojiMode) {
                emojiCategory=0;
                showPage();
                return;
            }

            if(suppressBackspaceClick) {
                suppressBackspaceClick=false;
                return;
            }

            // In search, X behaves like backspace. When the field is
            // empty, it exits search and returns to the emoji browser.
            if(
                emojiSearchMode &&
                emojiSearchQuery!=null &&
                !emojiSearchQuery.isEmpty()
            ) {
                eraseEmojiSearchChar();
                refreshEmojiSearchResults();
                return;
            }

            kaomojiMode=false;
            emojiSearchMode=false;
            emojiSearchQuery="";
            showPage();
        });

        searchRow.addView(
            close,
            new LinearLayout.LayoutParams(
                dp(48),
                dp(42)
            )
        );

        body.addView(
            searchRow,
            new LinearLayout.LayoutParams(
                -1,
                dp(46)
            )
        );

        if(emojiSearchMode)
            startEmojiSearchCursor();
        else
            stopEmojiSearchCursor();


        // KAOMOJI
        if(kaomojiMode) {

            buildKaomojiPanel();
            addFastEmojiModeBar();

            return;
        }


        // Gboard-style emoji search: search + live results + real KeyKii keyboard.
        if(emojiSearchMode) {

            emojiSearchResultsScroll=
                new HorizontalScrollView(this);

            emojiSearchResultsScroll.setHorizontalScrollBarEnabled(false);
            emojiSearchResultsScroll.setFillViewport(false);
            emojiSearchResultsScroll.setVisibility(View.GONE);

            emojiSearchResultsRow=
                new LinearLayout(this);

            emojiSearchResultsRow.setOrientation(LinearLayout.HORIZONTAL);
            emojiSearchResultsRow.setGravity(Gravity.CENTER_VERTICAL);

            emojiSearchResultsScroll.addView(emojiSearchResultsRow);

            body.addView(
                emojiSearchResultsScroll,
                new LinearLayout.LayoutParams(-1,dp(60))
            );

            refreshEmojiSearchResults();
            buildKeyboard();
            return;
        }


        // CATEGORY ICONS
        final String[] groups={
            "Favorites",
            "Recent emoji",
            "Smileys & Emotion",
            "People & Body",
            "Animals & Nature",
            "Food & Drink",
            "Travel & Places",
            "Activities",
            "Objects",
            "Symbols",
            "Flags"
        };

        String[] icons={
            "★","🕘","😀","🧑","🐻","🍔",
            "🚗","⚽","💡","❤️","🏳️"
        };

        // One adapter holds every section; tabs only move its scroll position.
        final java.util.ArrayList<Object> rows=makeFastEmojiRows("");
        final java.util.HashMap<String,Integer> sectionStarts=
            new java.util.HashMap<>(fastEmojiJump);
        final TextView[] categoryButtons=new TextView[icons.length];
        final int initialEmojiCategory=
            Math.max(0,Math.min(emojiCategory,groups.length-1));

        HorizontalScrollView hsv=
            new HorizontalScrollView(this);

        hsv.setHorizontalScrollBarEnabled(
            false
        );

        LinearLayout cats=
            new LinearLayout(this);

        cats.setGravity(Gravity.CENTER);

        for(int i=0;i<icons.length;i++) {

            final int index=i;

            TextView b=
                new TextView(this);

            b.setText(icons[i]);
            b.setTextSize(21);
            b.setGravity(Gravity.CENTER);

            categoryButtons[i]=b;

            b.setOnClickListener(v -> {
                emojiCategory=index;
                highlightEmojiCategory(categoryButtons,index);

                hsv.smoothScrollTo(
                    Math.max(0,index*dp(48)-dp(96)),
                    0
                );

                Integer position=sectionStarts.get(groups[index]);
                if(position!=null && fastEmojiList!=null)
                    fastEmojiList.setSelectionFromTop(position,0);
            });

            cats.addView(
                b,
                new LinearLayout.LayoutParams(
                    dp(48),
                    dp(42)
                )
            );
        }

        highlightEmojiCategory(categoryButtons,initialEmojiCategory);
        hsv.addView(cats);

        body.addView(
            hsv,
            new LinearLayout.LayoutParams(
                -1,
                dp(46)
            )
        );

        // Keep the selected category visible instead of snapping the
        // horizontal category strip back to the first icon after rebuild.
        final int selectedEmojiCategory=initialEmojiCategory;
        hsv.post(() -> hsv.scrollTo(
            Math.max(0, selectedEmojiCategory*dp(48)-dp(72)),
            0
        ));


        fastEmojiList=
            new android.widget.ListView(this);

        fastEmojiList.setDivider(null);

        fastEmojiList.setVerticalScrollBarEnabled(
            true
        );

        final Runnable syncEmojiCategory=() -> {
            if(fastEmojiList==null) return;

            int first=fastEmojiList.getFirstVisiblePosition();
            int active=0;
            int latest=-1;

            for(int i=0;i<groups.length;i++) {
                Integer start=sectionStarts.get(groups[i]);

                if(start!=null && start<=first && start>latest) {
                    latest=start;
                    active=i;
                }
            }

            if(emojiCategory!=active) {
                emojiCategory=active;
                highlightEmojiCategory(categoryButtons,active);

                final int categoryToShow=active;
                hsv.post(() -> hsv.smoothScrollTo(
                    Math.max(0,categoryToShow*dp(48)-dp(96)),
                    0
                ));
            }
        };

        fastEmojiList.setOnScrollListener(new android.widget.AbsListView.OnScrollListener() {
            public void onScrollStateChanged(android.widget.AbsListView view,int state) {
                syncEmojiCategory.run();
            }

            public void onScroll(android.widget.AbsListView view,
                                 int first,int visible,int total) {
                syncEmojiCategory.run();
            }
        });

        if(android.os.Build.VERSION.SDK_INT>=23) {
            fastEmojiList.setOnScrollChangeListener(
                (v,scrollX,scrollY,oldScrollX,oldScrollY) ->
                    syncEmojiCategory.run()
            );
        }

        fastEmojiList.setAdapter(
            new android.widget.BaseAdapter() {

                public int getCount() {
                    return rows.size();
                }

                public Object getItem(int p) {
                    return rows.get(p);
                }

                public long getItemId(int p) {
                    return p;
                }

                public View getView(
                    int position,
                    View convertView,
                    android.view.ViewGroup parent
                ) {

                    Object item=
                        rows.get(position);


                    // SECTION LABEL
                    if(item instanceof String) {

                        TextView label=
                            new TextView(
                                KeyKiiService.this
                            );

                        label.setText(
                            (String)item
                        );

                        label.setTextSize(16);
                        label.setTextColor(
                            textColor()
                        );

                        label.setGravity(
                            Gravity.CENTER_VERTICAL
                        );

                        label.setPadding(
                            dp(10),
                            dp(9),
                            dp(4),
                            dp(5)
                        );

                        return label;
                    }


                    // EMOJI ROW
                    LinearLayout row=
                        new LinearLayout(
                            KeyKiiService.this
                        );

                    row.setGravity(
                        Gravity.CENTER
                    );

                    @SuppressWarnings("unchecked")
                    java.util.ArrayList<String>
                        emojis=
                        (java.util.ArrayList<String>)
                        item;

                    for(int i=0;i<10;i++) {

                        TextView e=
                            new TextView(
                                KeyKiiService.this
                            );

                        e.setGravity(
                            Gravity.CENTER
                        );

                        e.setTextSize(25);
                        e.setIncludeFontPadding(false);

                        if(i<emojis.size()) {

                            final String value=
                                emojis.get(i);

                            // Rows are filtered before the adapter is built,
                            // so never replace a real item with an empty cell here.
                            e.setText(value);
                            e.setTypeface(android.graphics.Typeface.DEFAULT);

                            e.setOnClickListener(v ->
                                fastCommitEmoji(value)
                            );

                            e.setOnLongClickListener(v -> {
                                java.util.ArrayList<String> variants=
                                    emojiToneVariants(value);

                                if(variants.size()>1) {
                                    showEmojiVariantPopup(v,value);
                                    return true;
                                }

                                boolean added=
                                    toggleEmojiFavorite(value);

                                android.widget.Toast.makeText(
                                    KeyKiiService.this,
                                    added
                                        ? "Added to favorites"
                                        : "Removed from favorites",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show();

                                showPage();
                                return true;
                            });
                            e.setOnTouchListener((v,event) ->
                                handleDragChoiceTouch(event)
                            );
                        }

                        row.addView(
                            e,
                            new LinearLayout.LayoutParams(
                                0,
                                dp(46),
                                1
                            )
                        );
                    }

                    return row;
                }
            }
        );

        int listHeight=dp(300);

        body.addView(
            fastEmojiList,
            new LinearLayout.LayoutParams(
                -1,
                listHeight
            )
        );

        // Restore the selected section when returning from another panel.
        Integer initialPosition=sectionStarts.get(groups[selectedEmojiCategory]);
        if(initialPosition!=null && initialPosition>0)
            fastEmojiList.post(() -> fastEmojiList.setSelectionFromTop(
                initialPosition,0
            ));

        addFastEmojiModeBar();
    }


    void buildEmojiSlowBackup() {

        // KEYKII_SEARCH_BAR
        LinearLayout searchRow=new LinearLayout(this);
        searchRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView search=new TextView(this);
        search.setText(
            kaomojiMode
            ? "Kaomoji"
            : (
                emojiSearchQuery.isEmpty()
                ? "🔍  Search emoji"
                : "🔍  "+emojiSearchQuery
            )
        );
        search.setTextSize(14);
        search.setTextColor(textColor());
        search.setGravity(Gravity.CENTER_VERTICAL);
        search.setPadding(dp(14),0,dp(10),0);
        search.setBackground(
            round(keyColor(false),16,borderColor())
        );

        search.setOnClickListener(v -> {
            emojiSearchMode=true;
            emojiSearchCursor=
                emojiSearchQuery==null
                ? 0
                : emojiSearchQuery.length();
            showPage();
        });

        searchRow.addView(
            search,
            new LinearLayout.LayoutParams(
                0,dp(40),1
            )
        );

        TextView kaomoji=
            new TextView(this);

        kaomoji.setText(
            kaomojiMode ? "😀" : ":-)"
        );

        kaomoji.setTextSize(16);
        kaomoji.setTextColor(textColor());
        kaomoji.setGravity(Gravity.CENTER);

        kaomoji.setBackground(
            round(
                keyColor(false),
                14,
                borderColor()
            )
        );

        kaomoji.setOnClickListener(v -> {

            kaomojiMode=!kaomojiMode;
            emojiSearchMode=false;
            emojiSearchQuery="";

            showPage();
        });

        searchRow.addView(
            kaomoji,
            new LinearLayout.LayoutParams(
                dp(54),
                dp(40)
            )
        );

        TextView clear=new TextView(this);
        clear.setText("×");
        clear.setTextSize(20);
        clear.setTextColor(textColor());
        clear.setGravity(Gravity.CENTER);

        clear.setOnClickListener(v -> {
            emojiSearchQuery="";
            emojiSearchMode=false;
            showPage();
        });

        searchRow.addView(
            clear,
            new LinearLayout.LayoutParams(
                dp(44),dp(40)
            )
        );

        body.addView(
            searchRow,
            new LinearLayout.LayoutParams(
                -1,dp(44)
            )
        );

        if(kaomojiMode) {
            buildKaomojiPanel();
            return;
        }

        String[] groupNames={
            "Recent",
            "Smileys & Emotion",
            "People & Body",
            "Animals & Nature",
            "Food & Drink",
            "Activities",
            "Travel & Places",
            "Objects",
            "Symbols",
            "Flags"
        };

        String[] icons={
            "🕘",
            "😀",
            "🧑",
            "🐻",
            "🍔",
            "⚽",
            "🚗",
            "💡",
            "❤️",
            "🏳️"
        };

        HorizontalScrollView tabScroll=
            new HorizontalScrollView(this);

        tabScroll.setHorizontalScrollBarEnabled(
            false
        );

        LinearLayout tabs=
            new LinearLayout(this);

        tabs.setOrientation(
            LinearLayout.HORIZONTAL
        );

        tabs.setGravity(Gravity.CENTER);

        for(
            int n=0;
            n<icons.length;
            n++
        ){

            final int category=n;

            TextView tab=
                new TextView(this);

            tab.setText(icons[n]);
            tab.setTextSize(19);
            tab.setGravity(Gravity.CENTER);
            tab.setTextColor(textColor());

            if(n==emojiCategory) {

                tab.setBackground(
                    round(
                        keyColor(false),
                        15,
                        borderColor()
                    )
                );
            }

            tab.setOnClickListener(v -> {

                emojiCategory=category;
                emojiSearchQuery="";
                emojiSearchMode=false;
                showPage();
            });

            LinearLayout.LayoutParams tp=
                new LinearLayout.LayoutParams(
                    dp(48),
                    dp(40)
                );

            tp.setMargins(
                dp(2),
                dp(1),
                dp(2),
                dp(3)
            );

            tabs.addView(
                tab,
                tp
            );
        }

        tabScroll.addView(tabs);

        body.addView(
            tabScroll,
            new LinearLayout.LayoutParams(
                -1,
                dp(45)
            )
        );

        java.util.ArrayList<String> emojis=
            new java.util.ArrayList<>();

        if(emojiCategory==0) {

            String recent=
                getSharedPreferences(
                    "keykii_emoji",
                    MODE_PRIVATE
                ).getString(
                    "recent",
                    ""
                );

            if(
                recent!=null &&
                !recent.trim().isEmpty()
            ){

                for(
                    String e:
                    recent.trim().split(" ")
                ){

                    if(!e.isEmpty())
                        emojis.add(e);
                }
            }

            if(emojis.isEmpty()) {

                String popular=
                    "😭 😂 🥹 🤣 ❤️ 😊 😍 🥰 " +
                    "😘 😎 🔥 ✨ 👍 🙏 😡 🎉";

                for(
                    String e:
                    popular.split(" ")
                )
                    emojis.add(e);
            }

        } else {

            java.util.LinkedHashMap<
                String,
                java.util.ArrayList<String>
            > groups=
                loadEmojiDatabase();

            java.util.ArrayList<String> selected=
                groups.get(
                    groupNames[
                        emojiCategory
                    ]
                );

            if(selected!=null)
                emojis.addAll(selected);
        }

        if(
            emojiSearchQuery!=null &&
            !emojiSearchQuery.trim().isEmpty()
        ){
            emojis.clear();
            emojis.addAll(
                searchEmojiDatabase(emojiSearchQuery)
            );
        }

        // Hide emoji that this phone cannot display.
        // This prevents the blank square boxes.
        for(int i=emojis.size()-1;i>=0;i--) {

            if(!canRenderEmoji(emojis.get(i))) {
                emojis.remove(i);
            }
        }

        // First category = continuous ALL emoji view
        if(
            emojiCategory==0 &&
            (
                emojiSearchQuery==null ||
                emojiSearchQuery.trim().isEmpty()
            )
        ) {

            emojis.clear();
            emojis.addAll(
                loadAllDisplayableEmoji()
            );
        }

        TextView heading=
            new TextView(this);

        heading.setText(
            prettyEmojiGroup(
                groupNames[
                    emojiCategory
                ]
            )
        );

        heading.setTextColor(textColor());
        heading.setTextSize(13);

        heading.setPadding(
            dp(6),
            dp(4),
            dp(4),
            dp(4)
        );

        body.addView(
            heading,
            new LinearLayout.LayoutParams(
                -1,
                dp(30)
            )
        );

        if(emojiSearchMode)
            buildEmojiSearchPad();

        ScrollView scroll=
            new ScrollView(this);

        LinearLayout wrap=
            new LinearLayout(this);

        wrap.setOrientation(
            LinearLayout.VERTICAL
        );

        int columns=
            wideMode ? 8 : 6;

        addEmojiRows(
            wrap,
            emojis,
            columns
        );

        scroll.addView(wrap);

        scroll.setFillViewport(false);
        scroll.setVerticalScrollBarEnabled(true);
        scroll.setNestedScrollingEnabled(true);

        LinearLayout.LayoutParams scrollParams=
            new LinearLayout.LayoutParams(
                -1,
                dp(290)
            );

        scrollParams.setMargins(
            0,
            0,
            0,
            dp(2)
        );

        body.addView(
            scroll,
            scrollParams
        );
    }


    java.util.LinkedHashMap<
        String,
        java.util.ArrayList<String>
    > loadEmojiDatabase() {

        if(emojiGroupsCache!=null)
            return emojiGroupsCache;

        emojiGroupsCache=
            new java.util.LinkedHashMap<>();

        try {

            java.io.BufferedReader reader=
                new java.io.BufferedReader(
                    new java.io.InputStreamReader(
                        getAssets().open(
                            "keykii-emojis.txt"
                        ),
                        "UTF-8"
                    )
                );

            String line;

            while(
                (line=reader.readLine())!=null
            ){

                String[] parts=
                    line.split("\\t");

                if(parts.length<3)
                    continue;

                String group=
                    parts[0].trim();

                String emoji=
                    parts[2].trim();

                if(
                    group.isEmpty() ||
                    emoji.isEmpty() ||
                    group.equalsIgnoreCase(
                        "Component"
                    )
                )
                    continue;

                if(
                    !emojiGroupsCache
                        .containsKey(group)
                ){

                    emojiGroupsCache.put(
                        group,
                        new java.util.ArrayList<String>()
                    );
                }

                emojiGroupsCache
                    .get(group)
                    .add(emoji);
            }

            reader.close();

        } catch(Exception e) {

            java.util.ArrayList<String> fallback=
                new java.util.ArrayList<>();

            String basic=
                "😭 😂 🥹 🤣 ❤️ 😊 😍 🥰 " +
                "😘 😀 😃 😄 😁 😆 😅 😎";

            for(String x:basic.split(" "))
                fallback.add(x);

            emojiGroupsCache.put(
                "Smileys & Emotion",
                fallback
            );
        }

        return emojiGroupsCache;
    }


    void addEmojiRows(
        LinearLayout wrap,
        java.util.ArrayList<String> emojis,
        int columns
    ){

        for(
            int i=0;
            i<emojis.size();
            i+=columns
        ){

            LinearLayout row=
                new LinearLayout(this);

            row.setGravity(Gravity.CENTER);

            for(
                int j=i;
                j<Math.min(
                    i+columns,
                    emojis.size()
                );
                j++
            ){

                final String emoji=
                    emojis.get(j);

                TextView e=
                    new TextView(this);

                e.setText(emoji);
                e.setTextSize(25);
                e.setGravity(Gravity.CENTER);
                e.setClickable(true);

                e.setOnClickListener(v -> {

                    InputConnection ic=
                        getCurrentInputConnection();

                    if(ic!=null) {

                        ic.commitText(
                            emoji,
                            1
                        );

                        rememberEmoji(
                            emoji
                        );
                    }
                });

                row.addView(
                    e,
                    new LinearLayout.LayoutParams(
                        0,
                        dp(48),
                        1
                    )
                );
            }

            int count=
                Math.min(
                    columns,
                    emojis.size()-i
                );

            for(
                int x=count;
                x<columns;
                x++
            ){

                row.addView(
                    new Space(this),
                    new LinearLayout.LayoutParams(
                        0,
                        dp(48),
                        1
                    )
                );
            }

            wrap.addView(row);
        }
    }


    String prettyEmojiGroup(
        String group
    ){

        if(group.equals("Recent"))
            return "Recent emoji";

        if(group.equals("Smileys & Emotion"))
            return "Smileys and emotions";

        if(group.equals("People & Body"))
            return "People and body";

        if(group.equals("Animals & Nature"))
            return "Animals and nature";

        if(group.equals("Food & Drink"))
            return "Food and drink";

        if(group.equals("Travel & Places"))
            return "Travel and places";

        return group;
    }


    void rememberEmoji(
        String emoji
    ) {

        if(
            emoji==null ||
            emoji.trim().isEmpty()
        )
            return;

        SharedPreferences prefs=
            getSharedPreferences(
                "keykii_emoji",
                MODE_PRIVATE
            );

        String old=
            prefs.getString(
                "recent",
                ""
            );

        java.util.ArrayList<String> recent=
            new java.util.ArrayList<>();

        recent.add(emoji);

        if(
            old!=null &&
            !old.trim().isEmpty()
        ) {

            for(
                String item :
                old.trim().split(" ")
            ) {

                if(
                    !item.isEmpty() &&
                    !item.equals(emoji) &&
                    !recent.contains(item)
                ) {

                    recent.add(item);
                }

                if(recent.size()>=40)
                    break;
            }
        }

        StringBuilder result=
            new StringBuilder();

        for(String item:recent) {

            if(result.length()>0)
                result.append(" ");

            result.append(item);
        }

        prefs.edit()
            .putString(
                "recent",
                result.toString()
            )
            .apply();
    }



    class EmojiSearchCursorSpan
        extends android.text.style.ReplacementSpan {

        @Override
        public int getSize(
            android.graphics.Paint paint,
            CharSequence text,
            int start,
            int end,
            android.graphics.Paint.FontMetricsInt fm
        ) {
            return Math.max(dp(2),1);
        }

        @Override
        public void draw(
            android.graphics.Canvas canvas,
            CharSequence text,
            int start,
            int end,
            float x,
            int top,
            int y,
            int bottom,
            android.graphics.Paint paint
        ) {
            int oldColor=paint.getColor();
            float oldWidth=paint.getStrokeWidth();

            paint.setColor(textColor());
            paint.setStrokeWidth(Math.max(dp(1),1));

            float lineX=x+Math.max(dp(1),1);
            canvas.drawLine(
                lineX,
                top+dp(5),
                lineX,
                bottom-dp(5),
                paint
            );

            paint.setColor(oldColor);
            paint.setStrokeWidth(oldWidth);
        }
    }


    void renderEmojiSearchField() {
        if(emojiSearchField==null) return;

        if(kaomojiMode) {
            emojiSearchField.setText("Kaomoji");
            return;
        }

        if(emojiSearchMode) {
            String q=emojiSearchQuery==null ? "" : emojiSearchQuery;
            emojiSearchCursor=Math.max(
                0,
                Math.min(emojiSearchCursor,q.length())
            );

            android.text.SpannableStringBuilder text=
                new android.text.SpannableStringBuilder();

            text.append("🔍  ");
            text.append(q.substring(0,emojiSearchCursor));

            int cursorStart=text.length();
            text.append("\u200B");

            text.setSpan(
                new EmojiSearchCursorSpan(),
                cursorStart,
                cursorStart+1,
                android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            text.append(q.substring(emojiSearchCursor));
            emojiSearchField.setText(text);
            return;
        }

        emojiSearchField.setText("🔍  Search emoji");
    }


    void startEmojiSearchCursor() {
        if(emojiSearchQuery==null)
            emojiSearchQuery="";

        emojiSearchCursor=Math.max(
            0,
            Math.min(emojiSearchCursor,emojiSearchQuery.length())
        );

        renderEmojiSearchField();
    }


    void stopEmojiSearchCursor() {
        // No timer is used. Keeping the caret static avoids typing lag.
    }


    void refreshEmojiSearchField() {
        renderEmojiSearchField();
    }


    void insertEmojiSearchText(String value) {
        if(value==null || value.isEmpty())
            return;

        if(emojiSearchQuery==null)
            emojiSearchQuery="";

        emojiSearchCursor=Math.max(
            0,
            Math.min(emojiSearchCursor,emojiSearchQuery.length())
        );

        emojiSearchQuery=
            emojiSearchQuery.substring(0,emojiSearchCursor) +
            value +
            emojiSearchQuery.substring(emojiSearchCursor);

        emojiSearchCursor+=value.length();
        refreshEmojiSearchField();
    }


    void eraseEmojiSearchChar() {
        if(
            emojiSearchQuery==null ||
            emojiSearchQuery.isEmpty() ||
            emojiSearchCursor<=0
        ) return;

        emojiSearchCursor=Math.min(
            emojiSearchCursor,
            emojiSearchQuery.length()
        );

        int start=emojiSearchQuery.offsetByCodePoints(
            emojiSearchCursor,
            -1
        );

        emojiSearchQuery=
            emojiSearchQuery.substring(0,start) +
            emojiSearchQuery.substring(emojiSearchCursor);

        emojiSearchCursor=start;
        refreshEmojiSearchField();
    }


    void moveEmojiSearchCursorFromSpace(float dx) {
        if(emojiSearchQuery==null)
            emojiSearchQuery="";

        int stepPx=Math.max(dp(14),1);
        int steps=Math.round(dx/(float)stepPx);

        int start=Math.max(
            0,
            Math.min(emojiSearchGestureStart,emojiSearchQuery.length())
        );

        int totalCodePoints=
            emojiSearchQuery.codePointCount(
                0,
                emojiSearchQuery.length()
            );

        int startCodePoint=
            emojiSearchQuery.codePointCount(0,start);

        int targetCodePoint=Math.max(
            0,
            Math.min(totalCodePoints,startCodePoint+steps)
        );

        int target=emojiSearchQuery.offsetByCodePoints(
            0,
            targetCodePoint
        );

        if(target!=emojiSearchCursor) {
            emojiSearchCursor=target;
            refreshEmojiSearchField();
        }
    }

    void toggleShiftState() {

        long now=android.os.SystemClock.uptimeMillis();

        // Caps Lock -> one tap turns it off.
        if(capsLock) {
            capsLock=false;
            shift=false;
            lastShiftTap=0L;
            return;
        }

        // First tap: temporary Shift.
        if(!shift) {
            shift=true;
            capsLock=false;
            lastShiftTap=now;
            return;
        }

        // Second quick tap: Caps Lock. The wider window makes this
        // reliable even after the keyboard view redraws on slower phones.
        if(lastShiftTap>0L && (now-lastShiftTap)<=1000L) {
            shift=true;
            capsLock=true;
            lastShiftTap=0L;
            return;
        }

        // A later tap while temporary Shift is active simply turns Shift off.
        shift=false;
        capsLock=false;
        lastShiftTap=0L;
    }


    boolean handleEmojiSearchKey(String action) {
        if(page!=1 || !emojiSearchMode) return false;

        if(action.equals("BACK")) {
            eraseEmojiSearchChar();
            refreshEmojiSearchResults();
            return true;
        }
        if(action.equals("ENTER")) {
            refreshEmojiSearchResults();
            return true;
        }
        if(action.equals("EMOJI")) {
            emojiSearchMode=false;
            showPage();
            return true;
        }
        if(action.equals("SHIFT")) {
            toggleShiftState();
            showPage();
            return true;
        }
        if(action.equals("123")) {
            symbols=true;
            symbolPage=1;
            shift=false;
            capsLock=false;
            lastShiftTap=0L;
            showPage();
            return true;
        }
        if(action.equals("ABC")) {
            symbols=false;
            symbolPage=1;
            shift=false;
            capsLock=false;
            lastShiftTap=0L;
            showPage();
            return true;
        }
        if(action.equals("SYM2")) {
            symbolPage=2;
            showPage();
            return true;
        }
        if(action.equals("SYM1")) {
            symbolPage=1;
            showPage();
            return true;
        }
        if(action.equals("SPACE")) {
            insertEmojiSearchText(" ");
            refreshEmojiSearchResults();
            return true;
        }
        if(action.length()==1) {
            boolean shifted=shift && !symbols;
            String value=shifted ? action.toUpperCase() : action;
            insertEmojiSearchText(value);

            if(shifted && !capsLock) {
                shift=false;
                lastShiftTap=0L;
                showPage();
            } else {
                refreshEmojiSearchResults();
            }
            return true;
        }
        return false;
    }


    void buildEmojiSearchPad() {

        String[] rows={
            "qwertyuiop",
            "asdfghjkl",
            "zxcvbnm"
        };

        for(String letters:rows) {

            LinearLayout r=newRow();

            for(char c:letters.toCharArray()) {

                String x=String.valueOf(c);

                TextView k=new TextView(this);
                k.setText(x);
                k.setTextSize(13);
                k.setTextColor(textColor());
                k.setGravity(Gravity.CENTER);

                k.setBackground(
                    round(keyColor(false),10,borderColor())
                );

                k.setOnClickListener(v -> {
                    emojiSearchQuery+=x;
                    showPage();
                });

                LinearLayout.LayoutParams lp=
                    new LinearLayout.LayoutParams(
                        0,dp(32),1
                    );

                lp.setMargins(dp(2),dp(2),dp(2),dp(2));
                r.addView(k,lp);
            }

            body.addView(r);
        }

        LinearLayout bottom=newRow();

        TextView erase=new TextView(this);
        erase.setText("⌫");
        erase.setTextSize(17);
        erase.setGravity(Gravity.CENTER);
        erase.setTextColor(textColor());

        erase.setOnClickListener(v -> {

            if(!emojiSearchQuery.isEmpty()) {
                emojiSearchQuery=
                    emojiSearchQuery.substring(
                        0,
                        emojiSearchQuery.length()-1
                    );
            }

            showPage();
        });

        bottom.addView(
            erase,
            new LinearLayout.LayoutParams(
                0,dp(34),1
            )
        );

        TextView close=new TextView(this);
        close.setText("Done");
        close.setGravity(Gravity.CENTER);
        close.setTextColor(textColor());

        close.setOnClickListener(v -> {
            emojiSearchMode=false;
            showPage();
        });

        bottom.addView(
            close,
            new LinearLayout.LayoutParams(
                0,dp(34),2
            )
        );

        body.addView(bottom);
    }




    java.util.ArrayList<String> loadAllDisplayableEmoji() {

        java.util.ArrayList<String> out=
            new java.util.ArrayList<>();

        try {

            java.io.BufferedReader r=
                new java.io.BufferedReader(
                    new java.io.InputStreamReader(
                        getAssets().open("keykii-emojis.txt"),
                        "UTF-8"
                    )
                );

            String line;

            while((line=r.readLine())!=null) {

                String[] x=line.split("\\t",-1);

                if(x.length<3)
                    continue;

                String emoji=x[2].trim();

                if(
                    canRenderEmoji(emoji) &&
                    !out.contains(emoji)
                ) {
                    out.add(emoji);
                }
            }

            r.close();

        } catch(Exception ignored) {}

        return out;
    }


    java.util.ArrayList<String> searchEmojiDatabase(String query) {

        java.util.ArrayList<String> result=
            new java.util.ArrayList<>();

        String q=query.trim().toLowerCase();

        if(q.isEmpty())
            return result;

        try {

            java.io.BufferedReader r=
                new java.io.BufferedReader(
                    new java.io.InputStreamReader(
                        getAssets().open("keykii-emojis.txt"),
                        "UTF-8"
                    )
                );

            String line;

            while((line=r.readLine())!=null) {

                String[] x=line.split("\\t",-1);

                if(x.length<3)
                    continue;

                String group=x[0];
                String subgroup=x[1];
                String emoji=x[2];

                String name=
                    x.length>3 ? x[3] : "";

                String searchable=
                    (group+" "+subgroup+" "+name)
                    .toLowerCase();

                if(
                    emojiSearchMatches(searchable,q) &&
                    !result.contains(emoji)
                ) {
                    result.add(emoji);
                }
            }

            r.close();

        } catch(Exception ignored) {}

        return result;
    }



    java.util.LinkedHashMap<
        String,
        java.util.ArrayList<String>
    > fullKaomoji=null;

    java.util.ArrayList<String>
        fullKaomojiCategories=null;


    void loadFullKaomoji() {

        if(fullKaomoji!=null)
            return;

        fullKaomoji=
            new java.util.LinkedHashMap<>();

        fullKaomojiCategories=
            new java.util.ArrayList<>();

        try {

            java.io.BufferedReader r=
                new java.io.BufferedReader(
                    new java.io.InputStreamReader(
                        getAssets().open(
                            "keykii-kaomoji.txt"
                        ),
                        "UTF-8"
                    )
                );

            String line;

            while((line=r.readLine())!=null) {

                String[] x=
                    line.split("\\t",2);

                if(x.length<2)
                    continue;

                String category=x[0];
                String face=x[1];

                java.util.ArrayList<String>
                    list=fullKaomoji.get(category);

                if(list==null) {

                    list=
                        new java.util.ArrayList<>();

                    fullKaomoji.put(
                        category,
                        list
                    );

                    fullKaomojiCategories.add(
                        category
                    );
                }

                if(!list.contains(face))
                    list.add(face);
            }

            r.close();

        } catch(Exception ignored) {}
    }


    void buildKaomojiPanel() {

        loadFullKaomoji();

        if(
            fullKaomojiCategories==null ||
            fullKaomojiCategories.isEmpty()
        )
            return;

        if(
            kaomojiCategory<0 ||
            kaomojiCategory>=
                fullKaomojiCategories.size()
        )
            kaomojiCategory=0;

        final int initialKaomojiCategory=kaomojiCategory;
        final TextView[] categoryButtons=
            new TextView[fullKaomojiCategories.size()];
        final int[] sectionStarts=
            new int[fullKaomojiCategories.size()];
        final java.util.ArrayList<Object> rows=
            new java.util.ArrayList<>();

        for(int i=0;i<fullKaomojiCategories.size();i++) {
            String category=fullKaomojiCategories.get(i);
            sectionStarts[i]=rows.size();
            rows.add(category);

            java.util.ArrayList<String> faces=fullKaomoji.get(category);
            if(faces==null) continue;
            for(int j=0;j<faces.size();j+=2) {
                java.util.ArrayList<String> pair=
                    new java.util.ArrayList<>();
                pair.add(faces.get(j));
                if(j+1<faces.size()) pair.add(faces.get(j+1));
                rows.add(pair);
            }
        }

        final android.widget.ListView list=
            new android.widget.ListView(this);

        HorizontalScrollView tabsScroll=
            new HorizontalScrollView(this);

        tabsScroll.setHorizontalScrollBarEnabled(
            false
        );

        LinearLayout tabs=
            new LinearLayout(this);

        tabs.setOrientation(
            LinearLayout.HORIZONTAL
        );


        for(
            int i=0;
            i<fullKaomojiCategories.size();
            i++
        ) {

            final int index=i;

            TextView tab=
                new TextView(this);

            tab.setText(
                fullKaomojiCategories.get(i)
            );

            tab.setTextSize(14);
            tab.setTextColor(textColor());
            tab.setGravity(Gravity.CENTER);
            categoryButtons[i]=tab;

            tab.setOnClickListener(v -> {
                kaomojiCategory=index;
                highlightKaomojiCategory(categoryButtons,index);
                list.setSelectionFromTop(sectionStarts[index],0);
            });

            LinearLayout.LayoutParams lp=
                new LinearLayout.LayoutParams(
                    dp(112),
                    dp(42)
                );

            lp.setMargins(
                dp(3),dp(2),
                dp(3),dp(2)
            );

            tabs.addView(tab,lp);
        }

        highlightKaomojiCategory(categoryButtons,initialKaomojiCategory);
        tabsScroll.addView(tabs);

        body.addView(
            tabsScroll,
            new LinearLayout.LayoutParams(
                -1,
                dp(46)
            )
        );

        // Rebuilding the kaomoji page used to visually snap the tab strip
        // back to the first category. Keep the active tab in view.
        final int selectedKaomojiCategory=initialKaomojiCategory;
        tabsScroll.post(() -> tabsScroll.scrollTo(
            Math.max(0, selectedKaomojiCategory*dp(118)-dp(42)),
            0
        ));


        list.setDivider(null);

        list.setVerticalScrollBarEnabled(
            true
        );

        list.setOnScrollListener(new android.widget.AbsListView.OnScrollListener() {
            public void onScrollStateChanged(android.widget.AbsListView view,int state) {}

            public void onScroll(android.widget.AbsListView view,
                                 int first,int visible,int total) {
                int active=0;
                for(int i=1;i<sectionStarts.length;i++) {
                    if(sectionStarts[i]<=first)
                        active=i;
                    else
                        break;
                }
                if(kaomojiCategory!=active) {
                    kaomojiCategory=active;
                    highlightKaomojiCategory(categoryButtons,active);
                }
            }
        });

        list.setAdapter(
            new android.widget.BaseAdapter() {

                final int columns=2;

                public int getCount() {
                    return rows.size();
                }

                public Object getItem(int p) {
                    return rows.get(p);
                }

                public long getItemId(int p) {
                    return p;
                }

                public View getView(
                    int position,
                    View convertView,
                    android.view.ViewGroup parent
                ) {
                    Object item=rows.get(position);
                    if(item instanceof String) {
                        TextView heading=new TextView(KeyKiiService.this);
                        heading.setText((String)item);
                        heading.setTextSize(16);
                        heading.setTextColor(textColor());
                        heading.setGravity(Gravity.CENTER_VERTICAL);
                        heading.setPadding(dp(10),dp(9),dp(4),dp(5));
                        return heading;
                    }

                    @SuppressWarnings("unchecked")
                    java.util.ArrayList<String> pair=
                        (java.util.ArrayList<String>)item;

                    LinearLayout row=
                        new LinearLayout(
                            KeyKiiService.this
                        );

                    row.setGravity(
                        Gravity.CENTER
                    );

                    for(int c=0;c<columns;c++) {
                        TextView face=
                            new TextView(
                                KeyKiiService.this
                            );

                        face.setGravity(
                            Gravity.CENTER
                        );

                        face.setTextSize(15);
                        face.setTextColor(
                            textColor()
                        );

                        face.setSingleLine(true);

                        face.setBackground(
                            round(
                                keyColor(false),
                                14,
                                borderColor()
                            )
                        );


                        if(c<pair.size()) {

                            final String value=
                                pair.get(c);

                            face.setText(value);

                            face.setOnClickListener(v -> {

                                InputConnection ic=
                                    getCurrentInputConnection();

                                if(ic!=null)
                                    ic.commitText(
                                        value,
                                        1
                                    );
                            });
                        }


                        LinearLayout.LayoutParams fp=
                            new LinearLayout.LayoutParams(
                                0,
                                dp(58),
                                1
                            );

                        fp.setMargins(
                            dp(4),dp(4),
                            dp(4),dp(4)
                        );

                        row.addView(face,fp);
                    }

                    return row;
                }
            }
        );


        body.addView(
            list,
            new LinearLayout.LayoutParams(
                -1,
                dp(285)
            )
        );

        if(sectionStarts[selectedKaomojiCategory]>0)
            list.post(() -> list.setSelectionFromTop(
                sectionStarts[selectedKaomojiCategory],0
            ));
    }


    java.util.ArrayList<String> loadClipboardItems(
        SharedPreferences sp,
        String prefix,
        int max
    ) {
        java.util.ArrayList<String> out=
            new java.util.ArrayList<>();

        for(int i=0;i<max;i++) {
            String value=sp.getString(prefix+i,"");

            if(
                value!=null &&
                !value.isEmpty() &&
                !out.contains(value)
            ) {
                out.add(value);
            }
        }

        return out;
    }


    void saveClipboardItems(
        SharedPreferences sp,
        String prefix,
        java.util.ArrayList<String> items,
        int max
    ) {
        SharedPreferences.Editor e=sp.edit();

        for(int i=0;i<max;i++)
            e.remove(prefix+i);

        for(int i=0;i<Math.min(items.size(),max);i++)
            e.putString(prefix+i,items.get(i));

        e.apply();
    }


    void pinClipboardText(String text, boolean currentlyPinned) {
        if(text==null || text.isEmpty())
            return;

        SharedPreferences sp=
            getSharedPreferences(
                "keykii_clipboard",
                MODE_PRIVATE
            );

        java.util.ArrayList<String> pinned=
            loadClipboardItems(sp,"pin",10);

        java.util.ArrayList<String> recent=
            loadClipboardItems(sp,"clip",20);

        pinned.remove(text);
        recent.remove(text);

        if(currentlyPinned) {
            recent.add(0,text);
        } else {
            pinned.add(0,text);
        }

        saveClipboardItems(sp,"pin",pinned,10);
        saveClipboardItems(sp,"clip",recent,20);
    }


    void deleteClipboardText(String text) {
        if(text==null || text.isEmpty())
            return;

        SharedPreferences sp=
            getSharedPreferences(
                "keykii_clipboard",
                MODE_PRIVATE
            );

        java.util.ArrayList<String> pinned=
            loadClipboardItems(sp,"pin",10);

        java.util.ArrayList<String> recent=
            loadClipboardItems(sp,"clip",20);

        pinned.remove(text);
        recent.remove(text);

        saveClipboardItems(sp,"pin",pinned,10);
        saveClipboardItems(sp,"clip",recent,20);
    }


    void clearClipboardHistory() {
        SharedPreferences sp=
            getSharedPreferences(
                "keykii_clipboard",
                MODE_PRIVATE
            );

        SharedPreferences.Editor e=sp.edit();

        // Clear only normal clipboard history.
        // Pinned clips are intentionally preserved.
        for(int i=0;i<20;i++)
            e.remove("clip"+i);

        e.apply();
    }


    TextView clipboardSectionLabel(String text) {
        TextView v=new TextView(this);

        v.setText(text);
        v.setTextColor(textColor());
        v.setTextSize(12);
        v.setAlpha(.65f);
        v.setGravity(Gravity.CENTER_VERTICAL);
        v.setPadding(dp(8),dp(4),dp(4),dp(2));

        return v;
    }


    View clipboardItemView(
        String text,
        boolean pinned
    ) {
        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(8),dp(3),dp(4),dp(3));
        row.setBackground(
            round(
                keyColor(false),
                15,
                borderColor()
            )
        );

        TextView paste=new TextView(this);

        String preview=text
            .replace("\n"," ")
            .replace("\r"," ");

        paste.setText(preview);
        paste.setTextColor(textColor());
        paste.setTextSize(13);
        paste.setGravity(Gravity.CENTER_VERTICAL);
        paste.setMaxLines(2);
        paste.setEllipsize(
            android.text.TextUtils.TruncateAt.END
        );
        paste.setPadding(dp(3),0,dp(6),0);

        final String pasteText=text;

        paste.setOnClickListener(v -> {
            InputConnection ic=
                getCurrentInputConnection();

            if(ic!=null)
                ic.commitText(pasteText,1);
        });

        row.addView(
            paste,
            new LinearLayout.LayoutParams(
                0,
                dp(50),
                1
            )
        );

        TextView pin=new TextView(this);
        pin.setText("📌");
        pin.setTextSize(17);
        pin.setGravity(Gravity.CENTER);
        pin.setTextColor(textColor());
        pin.setAlpha(pinned ? 1f : .35f);
        pin.setContentDescription(
            pinned ? "Unpin clipboard item" : "Pin clipboard item"
        );

        pin.setOnClickListener(v -> {
            pinClipboardText(pasteText,pinned);
            showPage();
        });

        row.addView(
            pin,
            new LinearLayout.LayoutParams(
                dp(42),
                dp(46)
            )
        );

        TextView remove=new TextView(this);
        remove.setText("×");
        remove.setTextSize(20);
        remove.setGravity(Gravity.CENTER);
        remove.setTextColor(textColor());
        remove.setAlpha(.70f);
        remove.setContentDescription("Delete clipboard item");

        remove.setOnClickListener(v -> {
            deleteClipboardText(pasteText);
            showPage();
        });

        row.addView(
            remove,
            new LinearLayout.LayoutParams(
                dp(38),
                dp(46)
            )
        );

        LinearLayout wrapper=
            new LinearLayout(this);

        wrapper.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams rp=
            new LinearLayout.LayoutParams(
                -1,
                dp(56)
            );

        rp.setMargins(
            dp(5),
            dp(3),
            dp(5),
            dp(3)
        );

        wrapper.addView(row,rp);

        return wrapper;
    }


    void buildClipboard() {

        LinearLayout modeRow=new LinearLayout(this);
        modeRow.setGravity(Gravity.CENTER);

        TextView clipboardTab=title("Clipboard");
        TextView shortcutsTab=title("Shortcuts");

        clipboardTab.setTextSize(13);
        shortcutsTab.setTextSize(13);

        clipboardTab.setBackground(
            clipboardShortcutMode
            ? round(keyColor(false),14,borderColor())
            : round(
                accentFillColor(),
                14,
                borderColor()
            )
        );

        shortcutsTab.setBackground(
            clipboardShortcutMode
            ? round(
                accentFillColor(),
                14,
                borderColor()
            )
            : round(keyColor(false),14,borderColor())
        );

        clipboardTab.setOnClickListener(v -> {
            clipboardShortcutMode=false;
            showPage();
        });

        shortcutsTab.setOnClickListener(v -> {
            clipboardShortcutMode=true;
            showPage();
        });

        LinearLayout.LayoutParams tabParams=
            new LinearLayout.LayoutParams(
                0,
                dp(34),
                1
            );

        tabParams.setMargins(
            dp(4),dp(2),dp(4),dp(4)
        );

        modeRow.addView(clipboardTab,tabParams);
        modeRow.addView(shortcutsTab,tabParams);

        body.addView(
            modeRow,
            new LinearLayout.LayoutParams(
                -1,
                dp(40)
            )
        );

        if(clipboardShortcutMode) {
            buildTextShortcuts();
            return;
        }

        ClipboardManager cm=
            (ClipboardManager)
            getSystemService(
                CLIPBOARD_SERVICE
            );

        if(
            cm!=null &&
            cm.hasPrimaryClip()
        ) {
            ClipData clip=cm.getPrimaryClip();

            if(
                clip!=null &&
                clip.getItemCount()>0
            ) {
                CharSequence cs=
                    clip.getItemAt(0)
                        .coerceToText(this);

                if(cs!=null)
                    rememberClip(cs.toString());
            }
        }

        SharedPreferences sp=
            getSharedPreferences(
                "keykii_clipboard",
                MODE_PRIVATE
            );

        java.util.ArrayList<String> pinned=
            loadClipboardItems(sp,"pin",10);

        java.util.ArrayList<String> recent=
            loadClipboardItems(sp,"clip",20);

        LinearLayout header=new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView heading=title("Clipboard");
        heading.setTextSize(14);
        heading.setGravity(Gravity.CENTER_VERTICAL);
        heading.setPadding(dp(8),0,0,0);

        header.addView(
            heading,
            new LinearLayout.LayoutParams(
                0,
                dp(32),
                1
            )
        );

        TextView clear=title("Clear all");
        clear.setTextSize(12);
        clear.setGravity(Gravity.CENTER);
        clear.setAlpha(
            recent.isEmpty()
            ? .35f
            : .85f
        );
        clear.setClickable(
            !recent.isEmpty()
        );

        clear.setOnClickListener(v -> {
            clearClipboardHistory();
            showPage();
        });

        header.addView(
            clear,
            new LinearLayout.LayoutParams(
                dp(72),
                dp(32)
            )
        );

        body.addView(
            header,
            new LinearLayout.LayoutParams(
                -1,
                dp(34)
            )
        );

        android.widget.ScrollView scroll=
            new android.widget.ScrollView(this);

        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(true);

        LinearLayout content=
            new LinearLayout(this);

        content.setOrientation(
            LinearLayout.VERTICAL
        );

        if(!pinned.isEmpty()) {
            content.addView(
                clipboardSectionLabel("📌  Pinned"),
                new LinearLayout.LayoutParams(
                    -1,
                    dp(26)
                )
            );

            for(String text:pinned)
                content.addView(
                    clipboardItemView(text,true)
                );
        }

        if(!recent.isEmpty()) {
            content.addView(
                clipboardSectionLabel("Recent"),
                new LinearLayout.LayoutParams(
                    -1,
                    dp(26)
                )
            );

            for(String text:recent)
                content.addView(
                    clipboardItemView(text,false)
                );
        }

        if(pinned.isEmpty() && recent.isEmpty()) {
            TextView empty=title("Nothing copied yet");
            empty.setAlpha(.60f);

            content.addView(
                empty,
                new LinearLayout.LayoutParams(
                    -1,
                    dp(90)
                )
            );
        }

        scroll.addView(
            content,
            new android.widget.ScrollView.LayoutParams(
                -1,
                -2
            )
        );

        body.addView(
            scroll,
            new LinearLayout.LayoutParams(
                -1,
                dp(300)
            )
        );
    }


    void buildTextShortcuts() {

        SharedPreferences sp=
            getSharedPreferences(
                "keykii_shortcuts",
                MODE_PRIVATE
            );

        LinearLayout header=new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView heading=title("Tap to paste • hold to edit");
        heading.setTextSize(14);
        heading.setGravity(Gravity.CENTER_VERTICAL);
        heading.setPadding(dp(8),0,0,0);

        header.addView(
            heading,
            new LinearLayout.LayoutParams(
                0,
                dp(32),
                1
            )
        );

        TextView manage=title("Manage");
        manage.setTextSize(12);
        manage.setGravity(Gravity.CENTER);
        manage.setAlpha(.85f);

        manage.setOnClickListener(v -> {
            try {
                Intent intent=new Intent();
                intent.setClassName(
                    getPackageName(),
                    "com.keykii.neo.SettingsActivity"
                );
                intent.putExtra(
                    "open_screen",
                    "shortcuts"
                );
                intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                );
                startActivity(intent);
            } catch(Exception ignored) {}
        });

        header.addView(
            manage,
            new LinearLayout.LayoutParams(
                dp(72),
                dp(32)
            )
        );

        body.addView(
            header,
            new LinearLayout.LayoutParams(
                -1,
                dp(34)
            )
        );

        android.widget.ScrollView scroll=
            new android.widget.ScrollView(this);

        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(true);

        LinearLayout content=
            new LinearLayout(this);

        content.setOrientation(
            LinearLayout.VERTICAL
        );

        boolean found=false;

        for(int i=0;i<12;i++) {
            String label=
                sp.getString(
                    "shortcut_label"+i,
                    ""
                );

            String value=
                sp.getString(
                    "shortcut_text"+i,
                    ""
                );

            if(value==null || value.isEmpty())
                continue;

            found=true;

            LinearLayout item=
                new LinearLayout(this);

            item.setOrientation(
                LinearLayout.VERTICAL
            );

            item.setPadding(
                dp(12),
                dp(7),
                dp(12),
                dp(7)
            );

            item.setBackground(
                round(
                    keyColor(false),
                    15,
                    borderColor()
                )
            );

            TextView name=new TextView(this);
            name.setText(
                label==null || label.trim().isEmpty()
                ? "Shortcut"
                : label
            );
            name.setTextColor(textColor());
            name.setTextSize(14);
            name.setTypeface(
                android.graphics.Typeface.DEFAULT,
                android.graphics.Typeface.BOLD
            );

            TextView preview=new TextView(this);
            preview.setText(
                "Pastes: " +
                value
                    .replace("\n"," ")
                    .replace("\r"," ")
            );
            preview.setTextColor(textColor());
            preview.setTextSize(12);
            preview.setAlpha(.72f);
            preview.setMaxLines(2);
            preview.setEllipsize(
                android.text.TextUtils.TruncateAt.END
            );
            preview.setPadding(
                0,
                dp(2),
                0,
                0
            );

            item.addView(name);
            item.addView(preview);

            final String pasteText=value;
            final int shortcutIndex=i;

            item.setOnClickListener(v -> {
                InputConnection ic=
                    getCurrentInputConnection();

                if(ic!=null)
                    ic.commitText(
                        pasteText,
                        1
                    );
            });

            item.setOnLongClickListener(v -> {
                try {
                    Intent intent=new Intent();
                    intent.setClassName(
                        getPackageName(),
                        "com.keykii.neo.SettingsActivity"
                    );
                    intent.putExtra(
                        "open_screen",
                        "shortcuts"
                    );
                    intent.putExtra(
                        "shortcut_index",
                        shortcutIndex
                    );
                    intent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    );
                    startActivity(intent);
                } catch(Exception ignored) {}

                return true;
            });

            LinearLayout.LayoutParams itemParams=
                new LinearLayout.LayoutParams(
                    -1,
                    dp(60)
                );

            itemParams.setMargins(
                dp(5),
                dp(3),
                dp(5),
                dp(3)
            );

            content.addView(
                item,
                itemParams
            );
        }

        if(!found) {
            TextView empty=
                title(
                    "No shortcuts yet — tap Manage to add one"
                );

            empty.setAlpha(.60f);
            empty.setTextSize(12);

            content.addView(
                empty,
                new LinearLayout.LayoutParams(
                    -1,
                    dp(90)
                )
            );
        }

        scroll.addView(
            content,
            new android.widget.ScrollView.LayoutParams(
                -1,
                -2
            )
        );

        body.addView(
            scroll,
            new LinearLayout.LayoutParams(
                -1,
                dp(260)
            )
        );
    }


    void rememberClip(String text) {

        if(
            text==null ||
            text.trim().isEmpty()
        ) return;

        SharedPreferences sp=
            getSharedPreferences(
                "keykii_clipboard",
                MODE_PRIVATE
            );

        java.util.ArrayList<String> pinned=
            loadClipboardItems(sp,"pin",10);

        // Pinned clips stay pinned even if the same text is copied again.
        if(pinned.contains(text))
            return;

        java.util.ArrayList<String> recent=
            loadClipboardItems(sp,"clip",20);

        recent.remove(text);
        recent.add(0,text);

        saveClipboardItems(
            sp,
            "clip",
            recent,
            20
        );
    }


    void buildToolsPanel() {

        TextView heading=
            title("Tools");

        heading.setTextSize(12);
        heading.setAlpha(.62f);

        body.addView(
            heading,
            new LinearLayout.LayoutParams(
                -1,
                dp(26)
            )
        );

        toolsRow(
            new String[]{
                "◀  One-handed",
                "↔  Text editing"
            },
            new Runnable[]{
                () -> activateOneHanded(),
                () -> {
                    page=3;
                    buildShell();
                }
            }
        );

        toolsRow(
            new String[]{
                "▣  Clipboard",
                "☺  Emoji"
            },
            new Runnable[]{
                () -> {
                    page=2;
                    showPage();
                },
                () -> {
                    page=1;
                    emojiCategory=0;
                    emojiSearchMode=false;
                    emojiSearchQuery="";
                    kaomojiMode=false;
                    showPage();
                }
            }
        );

        toolsRow(
            new String[]{
                "↕  Resize",
                "⛶  Full / wide"
            },
            new Runnable[]{
                () -> {
                    page=5;
                    buildShell();
                },
                () -> toggleWideFromTools()
            }
        );

        toolsRow(
            new String[]{
                "◐  Theme",
                "⚙  Settings"
            },
            new Runnable[]{
                () -> openKeyKiiSettings("theme"),
                () -> openKeyKiiSettings("")
            }
        );
    }


    void toolsRow(
        String[] labels,
        Runnable[] actions
    ) {
        LinearLayout row=
            new LinearLayout(this);

        row.setGravity(Gravity.CENTER);

        for(int i=0;i<labels.length;i++) {
            final Runnable action=
                actions[i];

            TextView card=
                new TextView(this);

            card.setText(labels[i]);
            card.setTextColor(textColor());
            card.setTextSize(13);
            card.setGravity(Gravity.CENTER_VERTICAL);
            card.setPadding(
                dp(15),
                0,
                dp(12),
                0
            );

            card.setBackground(
                round(
                    keyColor(false),
                    20,
                    borderColor()
                )
            );

            card.setOnClickListener(v -> {
                if(action!=null)
                    action.run();
            });

            LinearLayout.LayoutParams p=
                new LinearLayout.LayoutParams(
                    0,
                    dp(58),
                    1f
                );

            p.setMargins(
                dp(3),
                dp(4),
                dp(3),
                dp(4)
            );

            row.addView(card,p);
        }

        body.addView(row);
    }


    void activateOneHanded() {
        wideMode=false;
        floating=true;

        if(hand==0) {
            int preferred=
                getSharedPreferences(
                    "keykii_prefs",
                    MODE_PRIVATE
                ).getInt(
                    "one_handed_default",
                    1
                );

            hand=
                preferred==2
                ? 2
                : 1;
        }

        page=0;
        symbols=false;
        symbolPage=1;

        getSharedPreferences(
            "keykii_prefs",
            MODE_PRIVATE
        ).edit()
         .putInt(
             "one_handed_default",
             hand
         )
         .putBoolean(
             "wide_default",
             false
         )
         .apply();

        buildShell();
    }

    void toggleWideFromTools() {
        if(!wideMode) {
            wideMode=true;
            floating=false;
            hand=0;
            page=0;
            symbols=false;
            symbolPage=1;
        } else {
            // Leaving real full width returns to the regular compact layout,
            // not to one-handed mode.
            wideMode=false;
            floating=true;
            hand=0;
            page=0;
        }

        getSharedPreferences(
            "keykii_prefs",
            MODE_PRIVATE
        ).edit()
         .putBoolean(
             "wide_default",
             wideMode
         )
         .putInt(
             "one_handed_default",
             hand
         )
         .apply();

        buildShell();
    }

    void buildResizePanel() {

        TextView heading=
            title("Resize keyboard");

        heading.setTextSize(13);
        heading.setAlpha(.68f);

        body.addView(
            heading,
            new LinearLayout.LayoutParams(
                -1,
                dp(28)
            )
        );

        TextView info=
            title(
                "Height " +
                keyHeight +
                "   •   Bottom gap " +
                floatGap
            );

        info.setTextSize(11);
        info.setAlpha(.58f);

        body.addView(
            info,
            new LinearLayout.LayoutParams(
                -1,
                dp(30)
            )
        );

        toolsRow(
            new String[]{
                "−  Shorter",
                "+  Taller"
            },
            new Runnable[]{
                () -> resizeKeyboardBy(-4,0),
                () -> resizeKeyboardBy(4,0)
            }
        );

        toolsRow(
            new String[]{
                "↓  Lower",
                "↑  Higher"
            },
            new Runnable[]{
                () -> resizeKeyboardBy(0,-16),
                () -> resizeKeyboardBy(0,16)
            }
        );

        toolsRow(
            new String[]{
                "↻  Reset",
                "✓  Done"
            },
            new Runnable[]{
                () -> {
                    keyHeight=46;
                    floatGap=96;

                    getSharedPreferences(
                        "keykii_prefs",
                        MODE_PRIVATE
                    ).edit()
                     .putInt(
                         "key_height",
                         keyHeight
                     )
                     .putInt(
                         "float_gap",
                         floatGap
                     )
                     .apply();

                    page=5;
                    buildShell();
                },
                () -> {
                    page=0;
                    buildShell();
                }
            }
        );
    }


    void resizeKeyboardBy(
        int heightDelta,
        int gapDelta
    ) {
        keyHeight=
            Math.max(
                38,
                Math.min(
                    62,
                    keyHeight+heightDelta
                )
            );

        floatGap=
            Math.max(
                32,
                Math.min(
                    160,
                    floatGap+gapDelta
                )
            );

        getSharedPreferences(
            "keykii_prefs",
            MODE_PRIVATE
        ).edit()
         .putInt(
             "key_height",
             keyHeight
         )
         .putInt(
             "float_gap",
             floatGap
         )
         .apply();

        buildShell();
    }


    void openKeyKiiSettings(
        String target
    ) {
        try {
            Intent intent=
                new Intent();

            intent.setClassName(
                getPackageName(),
                "com.keykii.neo.SettingsActivity"
            );

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            );

            if(
                target!=null &&
                !target.isEmpty()
            ) {
                intent.putExtra(
                    "open_screen",
                    target
                );
            }

            startActivity(intent);

        } catch(Exception ignored) {
        }
    }


    void buildEditing() {

        TextView smartTitle=
            title("Quick actions");

        smartTitle.setTextSize(12);
        smartTitle.setAlpha(.65f);

        body.addView(
            smartTitle,
            new LinearLayout.LayoutParams(
                -1,
                dp(24)
            )
        );

        editRow(
            new String[]{
                "Undo","Redo","Select all","Paste"
            },
            new String[]{
                "UNDO","REDO","SELECT","PASTE"
            }
        );

        editRow(
            new String[]{
                "Cut","Copy","Clipboard","Shortcuts"
            },
            new String[]{
                "CUT","COPY","CLIPBOARD","SHORTCUTS"
            }
        );

        editRow(
            new String[]{
                "←","↑","↓","→"
            },
            new String[]{
                "LEFT","UP","DOWN","RIGHT"
            }
        );

        editRow(
            new String[]{
                "Home","End","⌫","ABC"
            },
            new String[]{
                "HOME","END","BACK","KEYS"
            }
        );
    }

    void editRow(
        String[] labels,
        String[] actions
    ) {

        LinearLayout r=newRow();

        for(int i=0;i<labels.length;i++)
            key(
                r,
                labels[i],
                actions[i],
                1,
                true
            );

        body.addView(r);
    }

    TextView title(String text) {

        TextView v=
            new TextView(this);

        v.setText(text);
        v.setTextColor(textColor());
        v.setTextSize(13);
        v.setGravity(Gravity.CENTER);
        v.setSingleLine(true);

        return v;
    }

    String hintFor(String s){

        switch(s){

            case "q": return "1";
            case "w": return "2";
            case "e": return "3";
            case "r": return "4";
            case "t": return "5";
            case "y": return "6";
            case "u": return "7";
            case "i": return "8";
            case "o": return "9";
            case "p": return "0";

            case "a": return "@";
            case "s": return "#";
            case "d": return "$";
            case "f": return "%";
            case "g": return "&";
            case "h": return "-";
            case "j": return "+";
            case "k": return "(";
            case "l": return ")";

            case "z": return "*";
            case "x": return "\"";
            case "c": return "'";
            case "v": return ":";
            case "b": return ";";
            case "n": return "!";
            case "m": return "?";
            case ",": return "☺";

            default: return "";
        }
    }

    String alternativesFor(String s) {

        if(s==null) return "";

        switch(s) {
            // Actual numeric row: fractions/superscripts.
            case "1": return "1|¹|½|⅓|¼|1⁄5|1⁄6|⅛";
            case "2": return "2|²|⅔|2⁄5";
            case "3": return "3|³|¾|⅜|3⁄5";
            case "4": return "4|⁴|4⁄5";
            case "5": return "5|⁵|⅝|5⁄6";
            case "6": return "6|⁶";
            case "7": return "7|⁷|⅞";
            case "8": return "8|⁸";
            case "9": return "9|⁹";
            case "0": return "0|⁰|°";

            // Letter keyboard: long press still gives the small symbol hint,
            // plus useful accents where appropriate.
            case "q": return "1";
            case "w": return "2";
            case "e": return "é|è|ê|ë|ē|3";
            case "r": return "4";
            case "t": return "5";
            case "y": return "ý|ÿ|6";
            case "u": return "ú|ù|û|ü|ū|7";
            case "i": return "í|ì|î|ï|ī|8";
            case "o": return "ó|ò|ô|ö|õ|ø|ō|9";
            case "p": return "0";
            case "a": return "á|à|â|ä|ã|å|æ|@";
            case "s": return "ß|#";
            case "d": return "$";
            case "f": return "%";
            case "g": return "&";
            case "h": return "-";
            case "j": return "+";
            case "k": return "(";
            case "l": return ")";
            case "z": return "*";
            case "x": return "\"";
            case "c": return "ç|'";
            case "v": return ":";
            case "b": return ";";
            case "n": return "ñ|!";
            case "m": return "?";
            default: return "";
        }
    }


    void dismissDragChoicePopup() {
        if(dragChoicePopup!=null && dragChoicePopup.isShowing())
            dragChoicePopup.dismiss();

        dragChoicePopup=null;
        dragChoiceViews.clear();
        dragChoiceValues.clear();
        dragChoiceIndex=-1;
        dragChoiceActive=false;
        dragChoiceMode="";
    }


    void paintDragChoiceSelection() {
        for(int i=0;i<dragChoiceViews.size();i++) {
            TextView v=dragChoiceViews.get(i);

            if(i==dragChoiceIndex) {
                v.setBackground(
                    round(
                        accentFillColor(),
                        18,
                        Color.TRANSPARENT
                    )
                );
            } else {
                v.setBackground(
                    new android.graphics.drawable.ColorDrawable(
                        Color.TRANSPARENT
                    )
                );
            }
        }
    }


    void updateDragChoiceSelection(float rawX, float rawY) {
        if(!dragChoiceActive) return;

        int best=-1;
        float bestDistance=Float.MAX_VALUE;

        for(int i=0;i<dragChoiceViews.size();i++) {
            TextView v=dragChoiceViews.get(i);
            int[] loc=new int[2];
            v.getLocationOnScreen(loc);

            float left=loc[0]-dp(8);
            float top=loc[1]-dp(10);
            float right=loc[0]+v.getWidth()+dp(8);
            float bottom=loc[1]+v.getHeight()+dp(10);

            if(
                rawX>=left && rawX<=right &&
                rawY>=top && rawY<=bottom
            ) {
                best=i;
                break;
            }

            float cx=loc[0]+v.getWidth()/2f;
            float cy=loc[1]+v.getHeight()/2f;
            float dx=rawX-cx;
            float dy=rawY-cy;
            float d=dx*dx+dy*dy;

            // Gboard lets the finger slide just below the popup as well.
            if(rawY<=bottom+dp(46) && d<bestDistance) {
                bestDistance=d;
                best=i;
            }
        }

        if(best>=0 && best!=dragChoiceIndex) {
            dragChoiceIndex=best;
            paintDragChoiceSelection();
        }
    }


    void commitDragChoice() {
        if(
            dragChoiceIndex<0 ||
            dragChoiceIndex>=dragChoiceValues.size()
        ) {
            dismissDragChoicePopup();
            return;
        }

        String value=dragChoiceValues.get(dragChoiceIndex);
        String mode=dragChoiceMode;

        dismissDragChoicePopup();

        if(mode.equals("TEXT")) {
            InputConnection ic=getCurrentInputConnection();
            if(ic!=null) ic.commitText(value,1);
            return;
        }

        if(mode.equals("EMOJI")) {
            fastCommitEmoji(value);
            return;
        }

        if(mode.equals("COMMA")) {
            if(value.equals("HAND")) {
                if(wideMode)
                    wideMode=false;

                hand=
                    hand==0
                    ? 1
                    : hand==1
                        ? 2
                        : 0;

                getSharedPreferences(
                    "keykii_prefs",
                    MODE_PRIVATE
                ).edit()
                 .putInt(
                     "one_handed_default",
                     hand
                 )
                 .putBoolean(
                     "wide_default",
                     false
                 )
                 .apply();

                buildShell();
                return;
            }

            if(value.equals("EMOJI")) {
                page=1;
                emojiCategory=0;
                emojiSearchMode=false;
                emojiSearchQuery="";
                kaomojiMode=false;
                showPage();
                return;
            }

            if(value.equals("SETTINGS")) {
                try {
                    Intent intent=new Intent();
                    intent.setClassName(
                        getPackageName(),
                        "com.keykii.neo.SettingsActivity"
                    );
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch(Exception ignored) {}
            }
        }
    }


    boolean handleDragChoiceTouch(MotionEvent e) {
        if(!dragChoiceActive) return false;

        int action=e.getActionMasked();

        if(action==MotionEvent.ACTION_MOVE) {
            updateDragChoiceSelection(e.getRawX(),e.getRawY());
            return true;
        }

        if(action==MotionEvent.ACTION_UP) {
            updateDragChoiceSelection(e.getRawX(),e.getRawY());
            commitDragChoice();
            return true;
        }

        if(action==MotionEvent.ACTION_CANCEL) {
            dismissDragChoicePopup();
            return true;
        }

        return false;
    }


    void showDragChoicePopup(
        View anchor,
        java.util.ArrayList<String> labels,
        java.util.ArrayList<String> values,
        String mode,
        int initialIndex
    ) {
        if(anchor==null || labels==null || values==null) return;
        if(labels.isEmpty() || labels.size()!=values.size()) return;

        dismissDragChoicePopup();
        dismissKeyPreview();

        int columns=labels.size()<=6
            ? labels.size()
            : Math.min(6,(labels.size()+1)/2);
        int rows=(labels.size()+columns-1)/columns;
        int cellW=mode.equals("EMOJI") ? dp(50) : dp(52);
        int cellH=mode.equals("EMOJI") ? dp(54) : dp(50);
        int popupWidth=dp(14)+columns*cellW;
        int popupHeight=dp(14)+rows*cellH;

        android.widget.GridLayout grid=
            new android.widget.GridLayout(this);
        grid.setColumnCount(columns);
        grid.setPadding(dp(7),dp(7),dp(7),dp(7));
        grid.setBackground(
            round(keyColor(false),20,borderColor())
        );

        dragChoicePopup=new PopupWindow(
            grid,
            popupWidth,
            popupHeight,
            false
        );

        // Important: keep the current finger gesture on the original key.
        // Selection is made by dragging, not by tapping the popup cells.
        dragChoicePopup.setFocusable(false);
        dragChoicePopup.setTouchable(false);
        dragChoicePopup.setOutsideTouchable(false);
        dragChoicePopup.setBackgroundDrawable(
            new android.graphics.drawable.ColorDrawable(
                Color.TRANSPARENT
            )
        );

        if(android.os.Build.VERSION.SDK_INT>=21)
            dragChoicePopup.setElevation(dp(10));

        dragChoiceViews.clear();
        dragChoiceValues.clear();

        for(int i=0;i<labels.size();i++) {
            TextView option=new TextView(this);
            option.setText(labels.get(i));
            option.setTextSize(mode.equals("EMOJI") ? 28 : 21);
            option.setTextColor(textColor());
            option.setGravity(Gravity.CENTER);
            option.setIncludeFontPadding(false);

            android.widget.GridLayout.LayoutParams lp=
                new android.widget.GridLayout.LayoutParams();
            lp.width=cellW;
            lp.height=cellH;
            grid.addView(option,lp);

            dragChoiceViews.add(option);
            dragChoiceValues.add(values.get(i));
        }

        dragChoiceMode=mode;
        dragChoiceIndex=Math.max(0,Math.min(initialIndex,labels.size()-1));
        dragChoiceActive=true;

        int xOffset=(anchor.getWidth()-popupWidth)/2;
        int yOffset=-anchor.getHeight()-popupHeight-dp(8);

        dragChoicePopup.showAsDropDown(
            anchor,
            xOffset,
            yOffset
        );

        grid.post(() -> paintDragChoiceSelection());
    }


    void showCommaShortcutPopup(View anchor) {
        java.util.ArrayList<String> labels=new java.util.ArrayList<>();
        java.util.ArrayList<String> values=new java.util.ArrayList<>();

        labels.add("↔");
        labels.add("☺");
        labels.add("⚙");

        values.add("HAND");
        values.add("EMOJI");
        values.add("SETTINGS");

        // Center smiley starts selected, just like the Gboard-style popup.
        showDragChoicePopup(anchor,labels,values,"COMMA",1);
    }


    void showLongPressPopup(View anchor, String choices) {
        if(choices==null || choices.trim().isEmpty())
            return;

        java.util.ArrayList<String> options=
            new java.util.ArrayList<>();

        android.graphics.Paint paint=new android.graphics.Paint();
        paint.setTypeface(android.graphics.Typeface.DEFAULT);
        paint.setTextSize(dp(22));

        for(String raw:choices.split("\\|")) {
            String value=raw.trim();
            if(value.isEmpty()) continue;
            if(value.equals("□") || value.equals("�")) continue;

            boolean supported=true;

            for(int i=0;i<value.length();) {
                int cp=value.codePointAt(i);
                if(Character.getType(cp)==Character.UNASSIGNED) {
                    supported=false;
                    break;
                }
                i+=Character.charCount(cp);
            }

            if(
                supported &&
                android.os.Build.VERSION.SDK_INT>=23 &&
                !value.contains("⁄")
            ) {
                try {
                    supported=paint.hasGlyph(value);
                } catch(Exception ignored) {}
            }

            if(supported && !options.contains(value))
                options.add(value);
        }

        if(options.isEmpty()) return;

        showDragChoicePopup(anchor,options,options,"TEXT",0);
    }


    boolean isGraphemeExtend(int cp) {
        int type=Character.getType(cp);

        return
            cp==0xFE0E ||
            cp==0xFE0F ||
            cp==0x20E3 ||
            (cp>=0x1F3FB && cp<=0x1F3FF) ||
            (cp>=0xE0020 && cp<=0xE007F) ||
            type==Character.NON_SPACING_MARK ||
            type==Character.COMBINING_SPACING_MARK ||
            type==Character.ENCLOSING_MARK;
    }


    boolean isRegionalIndicator(int cp) {
        return cp>=0x1F1E6 && cp<=0x1F1FF;
    }


    int lastGraphemeUtf16Length(CharSequence text) {
        if(text==null || text.length()==0)
            return 0;

        int end=text.length();
        int pos=end;

        int cp=Character.codePointBefore(text,pos);
        pos-=Character.charCount(cp);

        // Country flags are pairs of regional-indicator code points.
        if(isRegionalIndicator(cp)) {
            if(pos>0) {
                int prev=Character.codePointBefore(text,pos);
                if(isRegionalIndicator(prev))
                    pos-=Character.charCount(prev);
            }
            return end-pos;
        }

        // Pull variation selectors, skin tones, combining marks, keycap
        // marks and emoji tag characters into the same deletion.
        while(isGraphemeExtend(cp) && pos>0) {
            cp=Character.codePointBefore(text,pos);
            pos-=Character.charCount(cp);
        }

        // Pull complete ZWJ emoji sequences (family, profession, etc.)
        // into one deletion as well.
        while(pos>0) {
            int prev=Character.codePointBefore(text,pos);

            if(prev!=0x200D)
                break;

            pos-=Character.charCount(prev);

            if(pos<=0)
                break;

            cp=Character.codePointBefore(text,pos);
            pos-=Character.charCount(cp);

            while(isGraphemeExtend(cp) && pos>0) {
                cp=Character.codePointBefore(text,pos);
                pos-=Character.charCount(cp);
            }
        }

        return end-pos;
    }


    void deletePreviousWord(
        InputConnection ic
    ) {
        if(ic==null)
            return;

        try {
            CharSequence before=
                ic.getTextBeforeCursor(
                    256,
                    0
                );

            if(
                before==null ||
                before.length()==0
            ) {
                return;
            }

            int end=
                before.length();

            int start=end;

            // Remove spaces directly before the previous word first.
            while(
                start>0 &&
                Character.isWhitespace(
                    before.charAt(start-1)
                )
            ) {
                start--;
            }

            // Then remove the previous word/punctuation chunk.
            while(
                start>0 &&
                !Character.isWhitespace(
                    before.charAt(start-1)
                )
            ) {
                start--;
            }

            int units=end-start;

            if(units>0) {
                ic.deleteSurroundingText(
                    units,
                    0
                );
            }

        } catch(Exception ignored) {
        }
    }


    void deleteOneBeforeCursor(InputConnection ic) {
        if(ic==null)
            return;

        try {
            CharSequence selected=ic.getSelectedText(0);

            if(selected!=null && selected.length()>0) {
                ic.commitText("",1);
                return;
            }
        } catch(Exception ignored) {}

        try {
            CharSequence before=ic.getTextBeforeCursor(64,0);
            int units=lastGraphemeUtf16Length(before);

            if(units>0) {
                ic.deleteSurroundingText(units,0);
                return;
            }
        } catch(Exception ignored) {}

        // Fallback for editors that do not expose surrounding text.
        try {
            if(android.os.Build.VERSION.SDK_INT>=24)
                ic.deleteSurroundingTextInCodePoints(1,0);
            else
                ic.deleteSurroundingText(1,0);
        } catch(Exception ignored) {
            ic.deleteSurroundingText(1,0);
        }
    }


    void press(String action) {

        glideDecodeSession++;

        if(handleEmojiSearchKey(action))
            return;

        InputConnection i=
            getCurrentInputConnection();

        if(i==null) return;

        switch(action) {

            case "BACK":
                deleteOneBeforeCursor(i);
                break;

            case "SPACE":
                smartSpace(i);
                break;

            case "ENTER":
                enter(i);

                if(autoCapitalization) {
                    shift=true;
                    capsLock=false;
                    lastShiftTap=0L;
                    showPage();
                }

                break;

            case "SHIFT":
                toggleShiftState();
                showPage();
                break;

            case "123":
                symbols=true;
                symbolPage=1;
                shift=false;
                capsLock=false;
                showPage();
                break;

            case "ABC":
                symbols=false;
                symbolPage=1;
                shift=false;
                capsLock=false;
                showPage();
                break;

            case "SYM2":
                symbolPage=2;
                showPage();
                break;

            case "SYM1":
                symbolPage=1;
                showPage();
                break;

            case "EMOJI":
                page=1;
                emojiCategory=0;
                emojiSearchMode=false;
                emojiSearchQuery="";
                kaomojiMode=false;
                showPage();
                break;

            case "KEYS":
                page=0;
                buildShell();
                break;

            case "LEFT":
                sendKey(i,KeyEvent.KEYCODE_DPAD_LEFT);
                break;

            case "RIGHT":
                sendKey(i,KeyEvent.KEYCODE_DPAD_RIGHT);
                break;

            case "UP":
                sendKey(i,KeyEvent.KEYCODE_DPAD_UP);
                break;

            case "DOWN":
                sendKey(i,KeyEvent.KEYCODE_DPAD_DOWN);
                break;

            case "HOME":
                sendKey(i,KeyEvent.KEYCODE_MOVE_HOME);
                break;

            case "END":
                sendKey(i,KeyEvent.KEYCODE_MOVE_END);
                break;

            case "SELECT":
                i.performContextMenuAction(
                    android.R.id.selectAll
                );
                break;

            case "COPY":
                i.performContextMenuAction(
                    android.R.id.copy
                );
                break;

            case "PASTE":
                i.performContextMenuAction(
                    android.R.id.paste
                );
                break;

            case "CUT":
                i.performContextMenuAction(
                    android.R.id.cut
                );
                break;

            case "UNDO":
                if(!i.performContextMenuAction(
                    android.R.id.undo
                )) {
                    sendCtrlKey(
                        i,
                        KeyEvent.KEYCODE_Z,
                        false
                    );
                }
                break;

            case "REDO":
                if(!i.performContextMenuAction(
                    android.R.id.redo
                )) {
                    sendCtrlKey(
                        i,
                        KeyEvent.KEYCODE_Z,
                        true
                    );
                }
                break;

            case "CLIPBOARD":
                page=2;
                clipboardShortcutMode=false;
                showPage();
                break;

            case "SHORTCUTS":
                page=2;
                clipboardShortcutMode=true;
                showPage();
                break;

            default:

                String out=
                    shift && !symbols
                    ? action.toUpperCase()
                    : action;

                i.commitText(out,1);

                if(shift && !symbols && !capsLock) {
                    shift=false;
                    lastShiftTap=0L;
                    showPage();
                }
        }
    }

    void smartSpace(InputConnection i) {
        if(i==null)
            return;

        long now=
            android.os.SystemClock.uptimeMillis();

        if(doubleSpacePeriod) {
            try {
                CharSequence before=
                    i.getTextBeforeCursor(
                        3,
                        0
                    );

                if(
                    before!=null &&
                    before.length()>=2 &&
                    before.charAt(before.length()-1)==' '
                ) {
                    char previous=
                        before.charAt(
                            before.length()-2
                        );

                    if(
                        !Character.isWhitespace(previous) &&
                        previous!='.' &&
                        previous!='!' &&
                        previous!='?' &&
                        previous!=',' &&
                        previous!=';' &&
                        previous!=':'
                    ) {
                        i.deleteSurroundingText(
                            1,
                            0
                        );

                        i.commitText(
                            ". ",
                            1
                        );

                        lastSpaceTap=now;

                        if(autoCapitalization) {
                            shift=true;
                            capsLock=false;
                            lastShiftTap=0L;
                            showPage();
                        }

                        return;
                    }
                }
            } catch(Exception ignored) {
            }
        }

        i.commitText(" ",1);
        lastSpaceTap=now;

        if(autoCapitalization) {
            try {
                CharSequence before=
                    i.getTextBeforeCursor(
                        4,
                        0
                    );

                if(before!=null) {
                    String text=
                        before.toString();

                    if(
                        text.endsWith(". ") ||
                        text.endsWith("! ") ||
                        text.endsWith("? ")
                    ) {
                        shift=true;
                        capsLock=false;
                        lastShiftTap=0L;
                        showPage();
                    }
                }
            } catch(Exception ignored) {
            }
        }
    }


    void enter(InputConnection i) {

        EditorInfo e=
            getCurrentInputEditorInfo();

        if(e!=null) {

            int action=
                e.imeOptions &
                EditorInfo.IME_MASK_ACTION;

            if(
                action!=EditorInfo.IME_ACTION_NONE &&
                action!=EditorInfo.IME_ACTION_UNSPECIFIED
            ) {

                i.performEditorAction(action);
                return;
            }
        }

        sendKey(
            i,
            KeyEvent.KEYCODE_ENTER
        );
    }

    String enterLabel() {

        EditorInfo e=
            getCurrentInputEditorInfo();

        if(e==null)
            return "return";

        switch(
            e.imeOptions &
            EditorInfo.IME_MASK_ACTION
        ) {

            case EditorInfo.IME_ACTION_GO:
                return "go";

            case EditorInfo.IME_ACTION_SEARCH:
                return "search";

            case EditorInfo.IME_ACTION_SEND:
                return "send";

            case EditorInfo.IME_ACTION_DONE:
                return "done";

            case EditorInfo.IME_ACTION_NEXT:
                return "next";

            default:
                return "↵";
        }
    }

    void sendCtrlKey(
        InputConnection i,
        int code,
        boolean shiftToo
    ) {
        if(i==null) return;

        long now=
            android.os.SystemClock.uptimeMillis();

        int meta=
            KeyEvent.META_CTRL_ON |
            (shiftToo
                ? KeyEvent.META_SHIFT_ON
                : 0);

        i.sendKeyEvent(
            new KeyEvent(
                now,
                now,
                KeyEvent.ACTION_DOWN,
                code,
                0,
                meta
            )
        );

        i.sendKeyEvent(
            new KeyEvent(
                now,
                now,
                KeyEvent.ACTION_UP,
                code,
                0,
                meta
            )
        );
    }


    void sendKey(
        InputConnection i,
        int code
    ) {

        i.sendKeyEvent(
            new KeyEvent(
                KeyEvent.ACTION_DOWN,
                code
            )
        );

        i.sendKeyEvent(
            new KeyEvent(
                KeyEvent.ACTION_UP,
                code
            )
        );
    }

    int resolvedTheme(SharedPreferences p) {
        if(p==null) return 0;

        if(!p.getBoolean("theme_auto_day_night",false))
            return p.getInt("theme",0);

        int nightMode=
            getResources()
                .getConfiguration()
                .uiMode &
            android.content.res.Configuration.UI_MODE_NIGHT_MASK;

        if(
            nightMode==
            android.content.res.Configuration.UI_MODE_NIGHT_YES
        ) {
            return p.getInt("theme_dark",0);
        }

        return p.getInt("theme_light",1);
    }


    int accentColor() {
        return getSharedPreferences(
            "keykii_prefs",
            MODE_PRIVATE
        ).getInt(
            "accent_color",
            Color.rgb(93,118,171)
        );
    }


    int accentFillColor() {
        int c=accentColor();

        if(theme==1) {
            return Color.rgb(
                (Color.red(c)+255)/2,
                (Color.green(c)+255)/2,
                (Color.blue(c)+255)/2
            );
        }

        return Color.argb(
            105,
            Color.red(c),
            Color.green(c),
            Color.blue(c)
        );
    }


    int themeTransparencyPercent() {
        int value=
            getSharedPreferences(
                "keykii_prefs",
                MODE_PRIVATE
            ).getInt(
                "theme_transparency",
                100
            );

        return Math.max(
            45,
            Math.min(100,value)
        );
    }


    int adjustedAlpha(int baseAlpha) {
        return Math.max(
            0,
            Math.min(
                255,
                Math.round(
                    baseAlpha *
                    themeTransparencyPercent() /
                    100f
                )
            )
        );
    }


    int keyCornerRadius() {
        int value=
            getSharedPreferences(
                "keykii_prefs",
                MODE_PRIVATE
            ).getInt(
                "key_corner_radius",
                15
            );

        return Math.max(
            4,
            Math.min(28,value)
        );
    }


    void applyPanelThemeBackground() {
        if(panel==null) return;

        SharedPreferences p=
            getSharedPreferences(
                "keykii_prefs",
                MODE_PRIVATE
            );

        int surfaceMode=
            p.getInt(
                "theme_surface_mode",
                0
            );

        if(surfaceMode==1 || surfaceMode==2) {
            applyColorPanelBackground(
                p,
                surfaceMode==2
            );
            return;
        }

        String uriText=
            p.getString(
                "theme_image_uri",
                ""
            );

        if(
            surfaceMode!=3 ||
            uriText==null ||
            uriText.isEmpty()
        ) {
            applyPlainPanelBackground();
            return;
        }

        android.graphics.Bitmap bitmap=null;

        try {
            android.net.Uri uri=
                android.net.Uri.parse(uriText);

            android.graphics.BitmapFactory.Options bounds=
                new android.graphics.BitmapFactory.Options();

            bounds.inJustDecodeBounds=true;

            java.io.InputStream boundsIn=
                getContentResolver()
                    .openInputStream(uri);

            if(boundsIn!=null) {
                android.graphics.BitmapFactory
                    .decodeStream(
                        boundsIn,
                        null,
                        bounds
                    );
                boundsIn.close();
            }

            int sample=1;
            int maxSide=1440;

            if(
                bounds.outWidth>0 &&
                bounds.outHeight>0
            ) {
                while(
                    bounds.outWidth/sample>maxSide ||
                    bounds.outHeight/sample>maxSide
                ) {
                    sample*=2;
                }
            }

            android.graphics.BitmapFactory.Options options=
                new android.graphics.BitmapFactory.Options();

            options.inSampleSize=Math.max(1,sample);
            options.inPreferredConfig=
                android.graphics.Bitmap.Config.RGB_565;

            java.io.InputStream in=
                getContentResolver()
                    .openInputStream(uri);

            if(in!=null) {
                bitmap=
                    android.graphics.BitmapFactory
                        .decodeStream(
                            in,
                            null,
                            options
                        );
                in.close();
            }

            if(bitmap==null) {
                applyPlainPanelBackground();
                return;
            }

            android.graphics.drawable.BitmapDrawable image=
                new android.graphics.drawable.BitmapDrawable(
                    getResources(),
                    bitmap
                );

            image.setGravity(Gravity.FILL);

            int overlayBaseAlpha=
                photoKeyBorders()
                ? 64
                : 46;

            int overlayColor=
                theme==1
                ? Color.argb(
                    adjustedAlpha(overlayBaseAlpha),
                    255,255,255
                )
                : Color.argb(
                    adjustedAlpha(overlayBaseAlpha),
                    0,0,0
                );

            GradientDrawable overlay=
                round(
                    overlayColor,
                    24,
                    borderColor()
                );

            android.graphics.drawable.LayerDrawable layers=
                new android.graphics.drawable.LayerDrawable(
                    new Drawable[]{
                        image,
                        overlay
                    }
                );

            panel.setBackground(layers);

            if(android.os.Build.VERSION.SDK_INT>=21) {
                panel.setClipToOutline(true);
                panel.setOutlineProvider(
                    new android.view.ViewOutlineProvider() {
                        @Override
                        public void getOutline(
                            View view,
                            android.graphics.Outline outline
                        ) {
                            outline.setRoundRect(
                                0,
                                0,
                                Math.max(1,view.getWidth()),
                                Math.max(1,view.getHeight()),
                                dp(24)
                            );
                        }
                    }
                );
            }

        } catch(OutOfMemoryError memoryError) {
            applyPlainPanelBackground();

        } catch(Throwable ignored) {
            applyPlainPanelBackground();
        }
    }


    int customThemeColor(
        int color,
        int baseAlpha
    ) {
        return Color.argb(
            adjustedAlpha(baseAlpha),
            Color.red(color),
            Color.green(color),
            Color.blue(color)
        );
    }


    void applyColorPanelBackground(
        SharedPreferences p,
        boolean gradient
    ) {
        if(panel==null) return;

        int start=
            p.getInt(
                "theme_custom_start",
                Color.rgb(93,118,171)
            );

        int end=
            p.getInt(
                "theme_custom_end",
                start
            );

        GradientDrawable bg;

        if(gradient) {
            bg=new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{
                    customThemeColor(start,248),
                    customThemeColor(end,248)
                }
            );
        } else {
            bg=new GradientDrawable();
            bg.setColor(
                customThemeColor(start,248)
            );
        }

        bg.setCornerRadius(dp(24));
        bg.setStroke(
            dp(1),
            borderColor()
        );

        if(android.os.Build.VERSION.SDK_INT>=21) {
            panel.setClipToOutline(false);
            panel.setOutlineProvider(
                android.view.ViewOutlineProvider.BACKGROUND
            );
        }

        panel.setBackground(bg);
    }


    void applyPlainPanelBackground() {
        if(panel==null) return;

        if(android.os.Build.VERSION.SDK_INT>=21) {
            panel.setClipToOutline(false);
            panel.setOutlineProvider(
                android.view.ViewOutlineProvider.BACKGROUND
            );
        }

        panel.setBackground(
            round(
                panelColor(),
                24,
                borderColor()
            )
        );
    }


    int panelColor() {

        if(theme==1)
            return Color.argb(
                adjustedAlpha(235),
                247,245,242
            );

        if(theme==2)
            return Color.argb(
                adjustedAlpha(145),
                30,32,37
            );

        return Color.argb(
            adjustedAlpha(220),
            35,37,42
        );
    }

    int keyColor(boolean special) {

        if(theme==1) {

            return special
                ? Color.rgb(243,238,232)
                : Color.WHITE;
        }

        return special
            ? Color.argb(
                72,235,235,238
            )
            : Color.argb(
                112,235,235,238
            );
    }

    int spaceColor() {
        int c=accentColor();

        if(theme==1) {
            return Color.rgb(
                (Color.red(c)+255*3)/4,
                (Color.green(c)+255*3)/4,
                (Color.blue(c)+255*3)/4
            );
        }

        return Color.argb(
            165,
            Color.red(c),
            Color.green(c),
            Color.blue(c)
        );
    }

    boolean photoThemeActive() {
        SharedPreferences p=
            getSharedPreferences(
                "keykii_prefs",
                MODE_PRIVATE
            );

        return
            p.getInt(
                "theme_surface_mode",
                0
            )==3 &&
            !p.getString(
                "theme_image_uri",
                ""
            ).isEmpty();
    }


    boolean themeKeyBorders() {
        SharedPreferences p=
            getSharedPreferences(
                "keykii_prefs",
                MODE_PRIVATE
            );

        // Existing installs keep their normal boxed keys until the user
        // explicitly changes Key borders in the new Theme preview.
        return p.getBoolean(
            "theme_key_borders",
            true
        );
    }


    boolean photoKeyBorders() {
        SharedPreferences p=
            getSharedPreferences(
                "keykii_prefs",
                MODE_PRIVATE
            );

        if(p.contains("theme_key_borders"))
            return p.getBoolean(
                "theme_key_borders",
                false
            );

        return p.getBoolean(
            "photo_key_borders",
            false
        );
    }


    int textColor() {

        if(photoThemeActive())
            return Color.WHITE;

        return theme==1
            ? Color.rgb(45,45,45)
            : Color.WHITE;
    }

    int borderColor() {

        return theme==1
            ? Color.rgb(230,225,219)
            : Color.argb(
                55,255,255,255
            );
    }

    StateListDrawable keyBackground(
        boolean special,
        boolean space
    ) {

        if(photoThemeActive()) {
            boolean borders=
                photoKeyBorders();

            int normalColor;

            if(!borders && !special && !space) {
                normalColor=
                    Color.TRANSPARENT;

            } else if(space) {
                normalColor=
                    Color.argb(
                        borders ? 105 : 58,
                        245,245,247
                    );

            } else if(special) {
                normalColor=
                    Color.argb(
                        borders ? 92 : 48,
                        245,245,247
                    );

            } else {
                normalColor=
                    Color.argb(
                        92,
                        245,245,247
                    );
            }

            int pressedColor=
                Color.argb(
                    115,
                    245,245,247
                );

            int stroke=
                borders
                ? Color.argb(
                    88,
                    255,255,255
                )
                : Color.TRANSPARENT;

            GradientDrawable normal=
                round(
                    normalColor,
                    keyCornerRadius(),
                    stroke
                );

            GradientDrawable pressed=
                round(
                    pressedColor,
                    keyCornerRadius(),
                    Color.argb(
                        90,
                        255,255,255
                    )
                );

            StateListDrawable state=
                new StateListDrawable();

            state.addState(
                new int[]{
                    android.R.attr.state_pressed
                },
                pressed
            );

            state.addState(
                new int[]{},
                normal
            );

            return state;
        }

        boolean borders=
            themeKeyBorders();

        int normalColor;

        if(
            !borders &&
            !special &&
            !space
        ) {
            normalColor=
                Color.TRANSPARENT;
        } else {
            normalColor=
                space
                ? spaceColor()
                : keyColor(special);
        }

        int pressedColor=
            theme==1
            ? Color.argb(
                borders ? 255 : 88,
                236,231,226
            )
            : Color.argb(
                borders ? 175 : 82,
                245,245,247
            );

        int stroke=
            borders
            ? borderColor()
            : Color.TRANSPARENT;

        GradientDrawable normal=
            round(
                normalColor,
                keyCornerRadius(),
                stroke
            );

        GradientDrawable pressed=
            round(
                pressedColor,
                keyCornerRadius(),
                stroke
            );

        StateListDrawable state=
            new StateListDrawable();

        state.addState(
            new int[]{
                android.R.attr.state_pressed
            },
            pressed
        );

        state.addState(
            new int[]{},
            normal
        );

        return state;
    }


    GradientDrawable round(
        int color,
        int radius,
        int stroke
    ) {

        GradientDrawable g=
            new GradientDrawable();

        g.setColor(color);
        g.setCornerRadius(dp(radius));

        if(stroke!=Color.TRANSPARENT)
            g.setStroke(
                dp(1),
                stroke
            );

        return g;
    }

    int dp(int value) {

        return Math.round(
            value *
            getResources()
                .getDisplayMetrics()
                .density
        );
    }
}
