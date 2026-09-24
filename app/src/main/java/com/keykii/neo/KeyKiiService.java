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

    // Keyboard language layouts plus local word suggestions.
    String activeKeyboardLanguage="en-US";
    boolean wordSuggestions=true;
    LinearLayout predictionStrip=null;
    TextView[] predictionViews=new TextView[3];
    boolean predictionDictionaryLoading=false;
    java.util.ArrayList<String> enabledKeyboardLanguages=
        new java.util.ArrayList<>();

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
    int floatGap=52;

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

    // Quick calculator lives inside Tools and does not run during normal typing.
    String calculatorExpression="";
    String calculatorResult="";
    TextView calculatorExpressionView=null;
    TextView calculatorResultView=null;

    // Gboard-style translator using Google ML Kit on-device translation.
    // Language models download on demand; typed text stays on the device.
    final String[] translatorLanguageNames={
        "English","Filipino (Tagalog)","Cebuano","Spanish","French","German",
        "Italian","Portuguese","Turkish","Chinese","Japanese","Korean",
        "Arabic","Russian","Ukrainian","Dutch","Polish","Romanian",
        "Greek","Hindi","Indonesian","Malay","Thai","Vietnamese",
        "Swedish","Danish","Norwegian","Finnish","Czech","Hungarian",
        "Hebrew","Afrikaans","Albanian","Belarusian","Bengali",
        "Bulgarian","Catalan","Croatian","Estonian","Georgian",
        "Gujarati","Haitian Creole","Icelandic","Irish","Kannada",
        "Latvian","Lithuanian","Macedonian","Persian","Serbian",
        "Slovak","Slovenian","Swahili","Tamil","Telugu","Urdu","Welsh"
    };
    final String[] translatorLanguageCodes={
        "en","tl","ceb","es","fr","de",
        "it","pt","tr","zh","ja","ko",
        "ar","ru","uk","nl","pl","ro",
        "el","hi","id","ms","th","vi",
        "sv","da","no","fi","cs","hu",
        "he","af","sq","be","bn",
        "bg","ca","hr","et","ka",
        "gu","ht","is","ga","kn",
        "lv","lt","mk","fa","sr",
        "sk","sl","sw","ta","te","ur","cy"
    };
    int translatorSourceLanguage=-1;
    int translatorTargetLanguage=1;
    String translatorSourceText="";
    String translatorResult="";
    String translatorStatus="";
    TextView translatorSourceLanguageView=null;
    TextView translatorTargetLanguageView=null;
    EditText translatorSourceView=null;
    TextView translatorResultView=null;
    TextView translatorStatusView=null;
    LinearLayout translatorLanguageList=null;
    boolean translatorLanguageChooser=false;
    boolean translatorChoosingSource=true;
    String translatorLanguageSearch="";
    int translatorCursor=0;
    int translatorRequestId=0;

    // Grammar Fix runs only when opened from Tools.
    String grammarSourceText="";
    String grammarCorrectedText="";
    String grammarStatus="";
    EditText grammarSourceView=null;
    TextView grammarCorrectedView=null;
    TextView grammarStatusView=null;
    int grammarCursor=0;
    int grammarRequestId=0;

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
                } else if(page==8) {
                    translatorBackspace();
                } else if(page==9) {
                    grammarBackspace();
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

    boolean isIncognitoMode() {
        return getSharedPreferences(
            "keykii_prefs",
            MODE_PRIVATE
        ).getBoolean(
            "incognito_mode",
            false
        );
    }


    void toggleIncognitoMode() {
        boolean next=
            !isIncognitoMode();

        getSharedPreferences(
            "keykii_prefs",
            MODE_PRIVATE
        ).edit()
         .putBoolean(
             "incognito_mode",
             next
         )
         .apply();

        voiceToast(
            next
            ? "Incognito on • history saving paused"
            : "Incognito off • normal saving restored"
        );

        buildShell();
    }


    void captureClipboard(){

        if(isIncognitoMode())
            return;

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

        floatGap=keykiiPrefs.getInt(
            "float_gap",
            52
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

        wordSuggestions=keykiiPrefs.getBoolean(
            "word_suggestions",
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

        loadKeyboardLanguagePrefs(
            keykiiPrefs
        );

        if(
            wordSuggestions &&
            isEnglishKeyboardLanguage()
        ) {
            ensurePredictionDictionaryAsync();
        }

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

        if(
            !p.getBoolean(
                "gboard_bottom_inset_2353_migrated",
                false
            )
        ) {
            int oldGap=
                p.getInt(
                    "float_gap",
                    52
                );

            if(oldGap<44) {
                oldGap=52;
            }

            p.edit()
             .putInt(
                 "float_gap",
                 oldGap
             )
             .putBoolean(
                 "gboard_bottom_inset_2353_migrated",
                 true
             )
             .apply();
        }

        floatGap=p.getInt("float_gap",52);
        haptic=p.getBoolean("haptic",false);
        keySound=p.getBoolean("key_sound",false);
        keySoundVolume=p.getInt(
            "key_sound_volume",
            50
        );
        numberRow=p.getBoolean("number_row",false);

        loadKeyboardLanguagePrefs(p);

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

        wordSuggestions=p.getBoolean(
            "word_suggestions",
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

        if(
            wordSuggestions &&
            isEnglishKeyboardLanguage()
        ) {
            ensurePredictionDictionaryAsync();
        }

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


    @Override
    public void onUpdateSelection(
        int oldSelStart,
        int oldSelEnd,
        int newSelStart,
        int newSelEnd,
        int candidatesStart,
        int candidatesEnd
    ) {
        super.onUpdateSelection(
            oldSelStart,
            oldSelEnd,
            newSelStart,
            newSelEnd,
            candidatesStart,
            candidatesEnd
        );

        if(
            root!=null &&
            page==0 &&
            wordSuggestions
        ) {
            new android.os.Handler(
                android.os.Looper.getMainLooper()
            ).post(
                () -> refreshPredictionSuggestions()
            );
        }
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
            hand!=0
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

        if(
            action==9 &&
            isIncognitoMode()
        ) {
            v.setBackground(
                round(
                    accentFillColor(),
                    16,
                    accentColor()
                )
            );
        }

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
                if(
                    page==3 ||
                    page==4 ||
                    page==5 ||
                    page==6 ||
                    page==7 ||
                    page==8 ||
                    page==9
                ) {
                    page=0;
                } else {
                    page=4;
                }

                buildShell();

            } else if(action==8) {

                toggleVoiceTyping();

            } else if(action==9) {

                toggleIncognitoMode();
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

        else if(page==6)
            buildCalculatorPanel();

        else if(page==7)
            buildTextCasePanel();

        else if(page==8)
            buildTranslatorPanel();

        else if(page==9)
            buildGrammarPanel();

        else if(page==10)
            buildSmartTextPanel();

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

            String currentLetter=
                glideLetterAt(
                    event.getRawX(),
                    event.getRawY()
                );

            // A normal tap can move a few pixels naturally. Do not turn that
            // into a glide. Glide only begins after the finger actually
            // reaches a DIFFERENT letter key.
            if(
                !glideActive &&
                !currentLetter.isEmpty() &&
                !currentLetter.equalsIgnoreCase(action) &&
                (
                    Math.abs(dx)>dp(10) ||
                    Math.abs(dy)>dp(10)
                )
            ) {
                glideActive=true;
                keyView.cancelLongPress();
                dismissKeyPreview();

                resetGlideKeyVisuals();

                highlightGlideLetter(action);
                appendGlideLetter(currentLetter);
                highlightGlideLetter(currentLetter);

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

            if(
                wasActive ||
                type==MotionEvent.ACTION_CANCEL
            ) {
                hideGlideTrail();
                resetGlideKeyVisuals();
            }

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

            if(type==MotionEvent.ACTION_CANCEL) {
                lastGlideEndTime=0L;
            }

            return wasActive;
        }

        return false;
    }


    void loadKeyboardLanguagePrefs(
        SharedPreferences prefs
    ) {
        String saved=
            prefs.getString(
                "keyboard_languages",
                "en-US"
            );

        enabledKeyboardLanguages.clear();

        if(saved!=null) {
            for(String part:saved.split(",")) {
                String code=
                    part==null
                    ? ""
                    : part.trim();

                if(
                    !code.isEmpty() &&
                    !enabledKeyboardLanguages.contains(code)
                ) {
                    enabledKeyboardLanguages.add(code);
                }
            }
        }

        if(enabledKeyboardLanguages.isEmpty())
            enabledKeyboardLanguages.add("en-US");

        activeKeyboardLanguage=
            prefs.getString(
                "keyboard_language_active",
                enabledKeyboardLanguages.get(0)
            );

        if(
            activeKeyboardLanguage==null ||
            !enabledKeyboardLanguages.contains(
                activeKeyboardLanguage
            )
        ) {
            activeKeyboardLanguage=
                enabledKeyboardLanguages.get(0);

            prefs.edit()
             .putString(
                 "keyboard_language_active",
                 activeKeyboardLanguage
             )
             .apply();
        }
    }


    boolean isEnglishKeyboardLanguage() {
        return
            "en-US".equals(activeKeyboardLanguage) ||
            "en-GB".equals(activeKeyboardLanguage);
    }


    java.util.Locale keyboardLocale() {
        if("tr".equals(activeKeyboardLanguage))
            return new java.util.Locale("tr");

        if("de".equals(activeKeyboardLanguage))
            return java.util.Locale.GERMAN;

        if("fr".equals(activeKeyboardLanguage))
            return java.util.Locale.FRENCH;

        if("es".equals(activeKeyboardLanguage))
            return new java.util.Locale("es");

        if("pt".equals(activeKeyboardLanguage))
            return new java.util.Locale("pt");

        if("it".equals(activeKeyboardLanguage))
            return java.util.Locale.ITALIAN;

        if("fil".equals(activeKeyboardLanguage))
            return new java.util.Locale("fil");

        if("ceb".equals(activeKeyboardLanguage))
            return new java.util.Locale("ceb");

        return java.util.Locale.ENGLISH;
    }


    String keyboardLanguageLabel() {
        switch(activeKeyboardLanguage) {
            case "en-GB":
                return "English (UK)";
            case "fil":
                return "Filipino";
            case "ceb":
                return "Cebuano";
            case "es":
                return "Español";
            case "fr":
                return "Français";
            case "de":
                return "Deutsch";
            case "tr":
                return "Türkçe";
            case "pt":
                return "Português";
            case "it":
                return "Italiano";
            case "en-US":
            default:
                return "English";
        }
    }


    String[] keyboardTopRow() {
        switch(activeKeyboardLanguage) {
            case "fr":
                return new String[]{
                    "a","z","e","r","t",
                    "y","u","i","o","p"
                };

            case "de":
                return new String[]{
                    "q","w","e","r","t",
                    "z","u","i","o","p"
                };

            case "tr":
                return new String[]{
                    "q","w","e","r","t",
                    "y","u","ı","o","p","ğ","ü"
                };

            default:
                return new String[]{
                    "q","w","e","r","t",
                    "y","u","i","o","p"
                };
        }
    }


    String[] keyboardMiddleRow() {
        switch(activeKeyboardLanguage) {
            case "fr":
                return new String[]{
                    "q","s","d","f","g",
                    "h","j","k","l","m"
                };

            case "es":
                return new String[]{
                    "a","s","d","f","g",
                    "h","j","k","l","ñ"
                };

            case "pt":
                return new String[]{
                    "a","s","d","f","g",
                    "h","j","k","l","ç"
                };

            case "tr":
                return new String[]{
                    "a","s","d","f","g",
                    "h","j","k","l","ş","i"
                };

            default:
                return new String[]{
                    "a","s","d","f","g",
                    "h","j","k","l"
                };
        }
    }


    String[] keyboardBottomLetters() {
        switch(activeKeyboardLanguage) {
            case "fr":
                return new String[]{
                    "w","x","c","v","b","n"
                };

            case "de":
                return new String[]{
                    "y","x","c","v","b","n","m"
                };

            case "tr":
                return new String[]{
                    "z","x","c","v",
                    "b","n","m","ö","ç"
                };

            default:
                return new String[]{
                    "z","x","c","v",
                    "b","n","m"
                };
        }
    }


    void cycleKeyboardLanguage() {
        SharedPreferences prefs=
            getSharedPreferences(
                "keykii_prefs",
                MODE_PRIVATE
            );

        loadKeyboardLanguagePrefs(prefs);

        if(enabledKeyboardLanguages.size()<=1) {
            openKeyKiiSettings(
                "languages"
            );
            return;
        }

        int index=
            enabledKeyboardLanguages.indexOf(
                activeKeyboardLanguage
            );

        index=
            (index+1)%
            enabledKeyboardLanguages.size();

        activeKeyboardLanguage=
            enabledKeyboardLanguages.get(index);

        prefs.edit()
         .putString(
             "keyboard_language_active",
             activeKeyboardLanguage
         )
         .apply();

        shift=false;
        capsLock=false;
        lastShiftTap=0L;

        if(
            wordSuggestions &&
            isEnglishKeyboardLanguage()
        ) {
            ensurePredictionDictionaryAsync();
        }

        showPage();

        voiceToast(
            keyboardLanguageLabel()
        );
    }


    void buildKeyboard() {

        resetGlideKeyVisuals();
        hideGlideTrail();
        glideLetterViews.clear();
        glideLetterActions.clear();

        if(
            !symbols &&
            wordSuggestions &&
            isEnglishKeyboardLanguage() &&
            predictionAllowedForCurrentField()
        ) {
            buildPredictionStrip();
        } else {
            predictionStrip=null;
            predictionViews=
                new TextView[3];
        }

        if(!symbols) {

            if(numberRow) {
                row(new String[]{
                    "1","2","3","4","5",
                    "6","7","8","9","0"
                });
            }

            row(
                keyboardTopRow()
            );

            String[] middle=
                keyboardMiddleRow();

            if(middle.length>=10)
                row(middle);
            else
                centered(middle);

            third(
                keyboardBottomLetters()
            );

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


    boolean predictionAllowedForCurrentField() {
        EditorInfo info=
            getCurrentInputEditorInfo();

        if(info==null)
            return true;

        int inputType=info.inputType;
        int inputClass=
            inputType &
            android.text.InputType.TYPE_MASK_CLASS;

        if(
            inputClass!=
            android.text.InputType.TYPE_CLASS_TEXT
        ) {
            return false;
        }

        int variation=
            inputType &
            android.text.InputType.TYPE_MASK_VARIATION;

        if(
            variation==
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD ||
            variation==
                android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
            variation==
                android.text.InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
        ) {
            return false;
        }

        return true;
    }


    void ensurePredictionDictionaryAsync() {
        if(
            glideDictionary!=null ||
            predictionDictionaryLoading
        ) {
            if(glideDictionary!=null) {
                new android.os.Handler(
                    android.os.Looper.getMainLooper()
                ).post(
                    () -> refreshPredictionSuggestions()
                );
            }

            return;
        }

        predictionDictionaryLoading=true;

        new Thread(
            () -> {
                loadGlideDictionaryBlocking();
                predictionDictionaryLoading=false;

                new android.os.Handler(
                    android.os.Looper.getMainLooper()
                ).post(
                    () -> refreshPredictionSuggestions()
                );
            },
            "KeyKii-Prediction-Dictionary"
        ).start();
    }


    void buildPredictionStrip() {
        predictionStrip=
            new LinearLayout(this);

        predictionStrip.setGravity(
            Gravity.CENTER
        );

        predictionStrip.setPadding(
            dp(2),
            dp(2),
            dp(2),
            dp(2)
        );

        predictionViews=
            new TextView[3];

        for(int i=0;i<3;i++) {
            final int index=i;

            TextView word=
                new TextView(this);

            word.setText("");
            word.setTextColor(textColor());
            word.setTextSize(14);
            word.setTypeface(
                keyboardTypeface()
            );
            word.setGravity(Gravity.CENTER);
            word.setSingleLine(true);
            word.setEllipsize(
                android.text.TextUtils.TruncateAt.END
            );

            word.setBackground(
                round(
                    keyColor(false),
                    14,
                    borderColor()
                )
            );

            word.setOnClickListener(
                v -> {
                    CharSequence value=
                        predictionViews[index]
                            .getText();

                    if(
                        value!=null &&
                        value.length()>0
                    ) {
                        applyPredictionSuggestion(
                            value.toString()
                        );
                    }
                }
            );

            predictionViews[i]=word;

            LinearLayout.LayoutParams p=
                new LinearLayout.LayoutParams(
                    0,
                    dp(36),
                    1f
                );

            p.setMargins(
                dp(3),0,
                dp(3),0
            );

            predictionStrip.addView(
                word,
                p
            );
        }

        body.addView(
            predictionStrip,
            new LinearLayout.LayoutParams(
                -1,
                dp(40)
            )
        );

        predictionStrip.post(
            () -> refreshPredictionSuggestions()
        );
    }


    String currentPredictionWord(
        InputConnection ic
    ) {
        if(ic==null)
            return "";

        try {
            CharSequence before=
                ic.getTextBeforeCursor(
                    64,
                    0
                );

            if(
                before==null ||
                before.length()==0
            ) {
                return "";
            }

            int end=before.length();
            int start=end;

            while(start>0) {
                char ch=
                    before.charAt(start-1);

                if(
                    Character.isLetter(ch) ||
                    ch=='\''
                ) {
                    start--;
                } else {
                    break;
                }
            }

            return before
                .subSequence(start,end)
                .toString();

        } catch(Exception ignored) {
            return "";
        }
    }


    String previousPredictionWord(
        InputConnection ic
    ) {
        if(ic==null)
            return "";

        try {
            CharSequence before=
                ic.getTextBeforeCursor(
                    96,
                    0
                );

            if(before==null)
                return "";

            String text=
                before.toString();

            int end=text.length();

            while(
                end>0 &&
                !Character.isLetter(
                    text.charAt(end-1)
                )
            ) {
                end--;
            }

            if(end<=0)
                return "";

            int start=end;

            while(
                start>0 &&
                Character.isLetter(
                    text.charAt(start-1)
                )
            ) {
                start--;
            }

            return text
                .substring(start,end)
                .toLowerCase(
                    java.util.Locale.ROOT
                );

        } catch(Exception ignored) {
            return "";
        }
    }


    String[] nextWordSeeds(
        String previous
    ) {
        if(previous==null)
            previous="";

        switch(previous) {
            case "i":
                return new String[]{"am","have","will"};
            case "you":
                return new String[]{"are","can","have"};
            case "we":
                return new String[]{"are","can","will"};
            case "they":
                return new String[]{"are","have","will"};
            case "good":
                return new String[]{"morning","luck","job"};
            case "how":
                return new String[]{"are","do","is"};
            case "what":
                return new String[]{"is","are","do"};
            case "can":
                return new String[]{"you","I","we"};
            case "please":
                return new String[]{"send","check","let"};
            case "thank":
                return new String[]{"you","everyone","again"};
            default:
                return new String[]{"the","I","and"};
        }
    }


    boolean predictionNearMatch(
        String a,
        String b
    ) {
        if(a==null || b==null)
            return false;

        int la=a.length();
        int lb=b.length();

        if(Math.abs(la-lb)>1)
            return false;

        int i=0;
        int j=0;
        int edits=0;

        while(i<la && j<lb) {
            if(a.charAt(i)==b.charAt(j)) {
                i++;
                j++;
                continue;
            }

            if(++edits>1)
                return false;

            if(la>lb)
                i++;
            else if(lb>la)
                j++;
            else {
                i++;
                j++;
            }
        }

        if(i<la || j<lb)
            edits++;

        return edits<=1;
    }


    String matchPredictionCase(
        String candidate,
        String typed
    ) {
        if(
            candidate==null ||
            candidate.isEmpty()
        ) {
            return "";
        }

        if(
            typed!=null &&
            typed.length()>1 &&
            typed.equals(
                typed.toUpperCase(
                    keyboardLocale()
                )
            )
        ) {
            return candidate.toUpperCase(
                keyboardLocale()
            );
        }

        if(
            typed!=null &&
            !typed.isEmpty() &&
            Character.isUpperCase(
                typed.charAt(0)
            )
        ) {
            return
                candidate.substring(0,1)
                    .toUpperCase(
                        keyboardLocale()
                    ) +
                candidate.substring(1);
        }

        return candidate;
    }


    void refreshPredictionSuggestions() {
        if(
            predictionViews==null ||
            predictionViews.length!=3
        ) {
            return;
        }

        if(
            page!=0 ||
            symbols ||
            !wordSuggestions ||
            !isEnglishKeyboardLanguage() ||
            !predictionAllowedForCurrentField()
        ) {
            for(TextView view:predictionViews) {
                if(view!=null)
                    view.setText("");
            }
            return;
        }

        InputConnection ic=
            getCurrentInputConnection();

        if(ic==null)
            return;

        String typed=
            currentPredictionWord(ic);

        java.util.ArrayList<String> result=
            new java.util.ArrayList<>();

        if(typed.isEmpty()) {
            String previous=
                previousPredictionWord(ic);

            String[] seeds=
                nextWordSeeds(previous);

            for(String seed:seeds) {
                if(
                    seed!=null &&
                    !seed.isEmpty() &&
                    !result.contains(seed)
                ) {
                    result.add(seed);
                }
            }

        } else {
            if(glideDictionary==null) {
                ensurePredictionDictionaryAsync();
            } else {
                String prefix=
                    typed.toLowerCase(
                        java.util.Locale.ROOT
                    );

                for(String word:glideDictionary) {
                    if(
                        word.startsWith(prefix) &&
                        !result.contains(word)
                    ) {
                        result.add(word);

                        if(result.size()>=3)
                            break;
                    }
                }

                if(
                    result.size()<3 &&
                    prefix.length()>=3
                ) {
                    int checked=0;

                    for(String word:glideDictionary) {
                        if(checked++>=7000)
                            break;

                        if(
                            result.contains(word) ||
                            word.startsWith(prefix)
                        ) {
                            continue;
                        }

                        if(
                            predictionNearMatch(
                                prefix,
                                word
                            )
                        ) {
                            result.add(word);

                            if(result.size()>=3)
                                break;
                        }
                    }
                }
            }
        }

        for(int i=0;i<3;i++) {
            TextView view=
                predictionViews[i];

            if(view==null)
                continue;

            if(i<result.size()) {
                String word=
                    matchPredictionCase(
                        result.get(i),
                        typed
                    );

                view.setText(word);
                view.setAlpha(1f);
            } else {
                view.setText("");
                view.setAlpha(.35f);
            }
        }
    }


    void applyPredictionSuggestion(
        String suggestion
    ) {
        if(
            suggestion==null ||
            suggestion.trim().isEmpty()
        ) {
            return;
        }

        InputConnection ic=
            getCurrentInputConnection();

        if(ic==null)
            return;

        String typed=
            currentPredictionWord(ic);

        try {
            ic.beginBatchEdit();

            if(!typed.isEmpty()) {
                ic.deleteSurroundingText(
                    typed.length(),
                    0
                );
            }

            ic.commitText(
                suggestion+" ",
                1
            );

            ic.endBatchEdit();

        } catch(Exception e) {
            try {
                ic.endBatchEdit();
            } catch(Exception ignored) {
            }
        }

        if(
            shift &&
            !capsLock
        ) {
            shift=false;
            lastShiftTap=0L;
        }

        new android.os.Handler(
            android.os.Looper.getMainLooper()
        ).postDelayed(
            () -> refreshPredictionSuggestions(),
            35
        );
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
            page==0 && !symbols
            ? 1.12f
            : 1f,
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
                page==0 && !symbols
                ? .58f
                : .62f,
                false
            );

            if(
                page==0 &&
                !symbols
            ) {
                key(
                    r,
                    "🌐",
                    "LANG",
                    .68f,
                    true
                );
            }

            key(
                r,
                page==0 && !symbols
                ? keyboardLanguageLabel()
                : "KeyKii",
                "SPACE",
                page==0 && !symbols
                ? 2.80f
                : 2.75f,
                false
            );

            key(
                r,
                ".",
                ".",
                page==0 && !symbols
                ? .58f
                : .58f,
                false
            );
        }

        key(
            r,
            enterLabel(),
            "ENTER",
            page==0 && !symbols
            ? 1.12f
            : 1.05f,
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
            ? label.toUpperCase(
                keyboardLocale()
              )
            : label;

        main.setText(shown);
        main.setTextColor(textColor());
        main.setGravity(Gravity.CENTER);
        main.setIncludeFontPadding(false);
        main.setAllCaps(false);
        main.setTypeface(
            keyboardTypeface()
        );

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

        boolean gboardBottomPlain=
            page==0 &&
            !symbols &&
            (
                action.equals(",") ||
                action.equals("LANG") ||
                action.equals(".")
            );

        if(gboardBottomPlain) {
            box.setBackgroundColor(
                Color.TRANSPARENT
            );
        } else {
            box.setBackground(
                keyBackground(
                    special,
                    space
                )
            );
        }

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
            small.setTypeface(
                keyboardTypeface()
            );
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

        int themeStickerRes=
            themeKeyStickerRes(action);

        if(themeStickerRes!=0) {
            ImageView sticker=
                new ImageView(this);

            android.graphics.drawable.Drawable stickerDrawable=
                getDrawable(themeStickerRes);

            if(stickerDrawable!=null) {
                stickerDrawable=
                    stickerDrawable.mutate();

                stickerDrawable.setTint(
                    accentColor()
                );

                sticker.setImageDrawable(
                    stickerDrawable
                );
            }

            sticker.setAlpha(.88f);
            sticker.setScaleType(
                ImageView.ScaleType.CENTER_INSIDE
            );
            sticker.setClickable(false);
            sticker.setFocusable(false);

            boolean spaceSticker=
                action.equals("SPACE");

            FrameLayout.LayoutParams sp=
                new FrameLayout.LayoutParams(
                    dp(
                        spaceSticker
                            ? 24
                            : 16
                    ),
                    dp(
                        spaceSticker
                            ? 20
                            : 16
                    ),
                    spaceSticker
                        ? (
                            Gravity.CENTER_VERTICAL |
                            Gravity.RIGHT
                          )
                        : (
                            Gravity.TOP |
                            Gravity.RIGHT
                          )
                );

            sp.setMargins(
                0,
                spaceSticker
                    ? 0
                    : dp(2),
                dp(
                    spaceSticker
                        ? 8
                        : 3
                ),
                0
            );

            box.addView(
                sticker,
                sp
            );
        }

        // Gboard-style comma key: tap still types comma. Holding it opens
        // a compact shortcut bubble above the key; the center smiley opens emoji.
        box.setOnLongClickListener(v -> {

            if(page==8 || page==9)
                return false;

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

            if(
                action.equals("SPACE") &&
                page==8 &&
                !translatorLanguageChooser
            )
                return handleTranslatorSpacebarTouch(v,e);

            if(
                action.equals("SPACE") &&
                page==9
            )
                return handleGrammarSpacebarTouch(v,e);

            if(
                action.equals("SPACE") &&
                page!=8 &&
                page!=9
            )
                return handleSpacebarTouch(v,e);

            // Long-press alternative selection owns the gesture once its
            // popup appears. This must run before glide typing; otherwise
            // MOVE/UP can be stolen by glide, leaving the popup stuck and
            // corrupting later key gestures.
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

            if(
                page==0 &&
                isEnglishKeyboardLanguage() &&
                isGlideLetterAction(action)
            ) {
                boolean glideConsumed=
                    handleGlideKeyTouch(
                        v,
                        e,
                        action
                    );

                if(glideConsumed)
                    return true;
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
                swipeDeleteWord &&
                page!=8 &&
                page!=9
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
                lastGlideEndTime>0L &&
                android.os.SystemClock.uptimeMillis()-
                    lastGlideEndTime<70
            ) {
                lastGlideEndTime=0L;
                return;
            }

            if(
                action.equals("BACK") &&
                suppressBackspaceClick
            ){
                suppressBackspaceClick=false;
                return;
            }

            if(isGlideLetterAction(action)) {
                glideTracking=false;
                glideActive=false;
                glideGestureLetters.setLength(0);
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


    int themeKeyStickerRes(
        String action
    ) {
        SharedPreferences p=
            getSharedPreferences(
                "keykii_prefs",
                MODE_PRIVATE
            );

        if(
            p.getInt(
                "theme_surface_mode",
                0
            )!=2
        ) {
            return 0;
        }

        int pack=
            p.getInt(
                "keykii_style_pack",
                -1
            );

        if(
            pack<100 ||
            pack>119 ||
            action==null ||
            action.isEmpty()
        ) {
            return 0;
        }

        boolean left=
            action.equals("q") ||
            action.equals("1") ||
            action.equals("SHIFT");

        boolean right=
            action.equals("p") ||
            action.equals("0") ||
            action.equals("BACK");

        boolean center=
            action.equals("SPACE") ||
            action.equals("5");

        boolean midLeft=
            action.equals("e") ||
            action.equals("a") ||
            action.equals("z");

        boolean midRight=
            action.equals("i") ||
            action.equals("l") ||
            action.equals("m");

        if(
            !left &&
            !right &&
            !center &&
            !midLeft &&
            !midRight
        ) {
            return 0;
        }

        int primary=
            themeDecorPrimaryAssetRes(pack);

        int secondary=
            themeDecorSecondaryAssetRes(pack);

        int tertiary=
            themeDecorTertiaryAssetRes(pack);

        switch(pack) {
            case 100:
                if(right || midRight) return R.drawable.theme_motif_cherry;
                if(center) return R.drawable.theme_motif_heart;
                return R.drawable.theme_motif_flower;

            case 101:
                if(center) return R.drawable.theme_motif_moonstar;
                if(right || midRight) return R.drawable.theme_motif_cloud;
                return R.drawable.theme_motif_star;

            case 102:
                if(center) return R.drawable.theme_motif_cup;
                if(midLeft || midRight) return R.drawable.theme_motif_leaf;
                return R.drawable.theme_motif_bunny;

            case 103:
                if(right || midRight) return R.drawable.theme_motif_cake;
                if(center) return R.drawable.theme_motif_heart;
                return R.drawable.theme_motif_bear;

            case 104:
                if(center) return R.drawable.theme_motif_bow;
                if(midLeft || midRight) return R.drawable.theme_motif_flower;
                return R.drawable.theme_motif_butterfly;

            case 105:
                if(center) return R.drawable.theme_motif_bubble;
                if(midLeft || midRight) return R.drawable.theme_motif_moonstar;
                return R.drawable.theme_motif_star;

            case 106:
                if(center) return R.drawable.theme_motif_heart;
                if(right || midRight) return R.drawable.theme_motif_bow;
                return R.drawable.theme_motif_strawberry;

            case 107:
                if(center) return R.drawable.theme_motif_moonstar;
                if(midLeft || midRight) return R.drawable.theme_motif_star;
                return R.drawable.theme_motif_cloud;

            case 108:
                if(center) return R.drawable.theme_motif_flower;
                if(midLeft || midRight) return R.drawable.theme_motif_leaf;
                return R.drawable.theme_motif_frog;

            case 109:
                if(center) return R.drawable.theme_motif_heart;
                if(right || midRight) return R.drawable.theme_motif_bow;
                return R.drawable.theme_motif_bear;

            case 110:
                if(center) return R.drawable.theme_motif_bow;
                if(midLeft || midRight) return R.drawable.theme_motif_flower;
                return R.drawable.theme_motif_butterfly;

            case 111:
                if(center) return R.drawable.theme_motif_cup;
                if(right || midRight) return R.drawable.theme_motif_cherry;
                return R.drawable.theme_motif_flower;

            case 112:
                if(center) return R.drawable.theme_motif_star;
                if(midLeft || midRight) return R.drawable.theme_motif_moonstar;
                return R.drawable.theme_motif_bubble;

            case 113:
                if(center) return R.drawable.theme_motif_cup;
                if(right || midRight) return R.drawable.theme_motif_bear;
                return R.drawable.theme_motif_bunny;

            case 114:
                if(center) return R.drawable.theme_motif_heart;
                if(right || midRight) return R.drawable.theme_motif_cat;
                return R.drawable.theme_motif_bow;

            case 115:
                if(center) return R.drawable.theme_motif_lotus;
                if(midLeft || midRight) return R.drawable.theme_motif_flower;
                return R.drawable.theme_motif_star;

            case 116:
                if(right || midRight) return R.drawable.theme_motif_butterfly;
                return R.drawable.theme_motif_lotus;

            case 117:
                if(center || midLeft) return R.drawable.theme_motif_heart;
                if(right || midRight) return R.drawable.theme_motif_bow;
                return R.drawable.theme_motif_flower;

            case 118:
                if(center) return R.drawable.theme_motif_heart;
                if(midLeft || midRight) return R.drawable.theme_motif_star;
                return R.drawable.theme_motif_butterfly;

            case 119:
                if(center) return R.drawable.theme_motif_bow;
                if(right || midRight) return R.drawable.theme_motif_gift;
                return R.drawable.theme_motif_snow;
        }

        if(center) return tertiary;
        if(right || midRight) return secondary;
        return primary;
    }


    int themeDecorPrimaryAssetRes(
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


    int themeDecorSecondaryAssetRes(
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


    int themeDecorTertiaryAssetRes(
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


    void drawTintedThemeAsset(
        android.graphics.Canvas canvas,
        int res,
        int tint,
        int alpha,
        float x,
        float y,
        int size
    ) {
        android.graphics.drawable.Drawable drawable=
            getDrawable(res);

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

        int left=
            Math.round(x);

        int top=
            Math.round(y);

        drawable.setBounds(
            left,
            top,
            left+size,
            top+size
        );

        drawable.draw(canvas);
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

        if(isIncognitoMode())
            return;

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

        if(isIncognitoMode())
            return;

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


    java.util.ArrayList<String> loadRecentKaomoji() {
        SharedPreferences sp=
            getSharedPreferences(
                "keykii_kaomoji_recent",
                MODE_PRIVATE
            );

        java.util.ArrayList<String> out=
            new java.util.ArrayList<>();

        for(int i=0;i<20;i++) {
            String value=sp.getString("k"+i,"");

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


    void rememberKaomoji(String value) {
        if(
            value==null ||
            value.isEmpty() ||
            isIncognitoMode()
        ) {
            return;
        }

        SharedPreferences sp=
            getSharedPreferences(
                "keykii_kaomoji_recent",
                MODE_PRIVATE
            );

        java.util.ArrayList<String> recent=
            loadRecentKaomoji();

        recent.remove(value);
        recent.add(0,value);

        while(recent.size()>20)
            recent.remove(recent.size()-1);

        SharedPreferences.Editor e=sp.edit();

        for(int i=0;i<20;i++)
            e.remove("k"+i);

        for(int i=0;i<recent.size();i++)
            e.putString("k"+i,recent.get(i));

        e.apply();

        // Reopening kaomoji starts on the freshly updated Recent section.
        kaomojiCategory=0;
    }


    java.util.ArrayList<String> loadFavoriteKaomoji() {
        SharedPreferences sp=
            getSharedPreferences(
                "keykii_kaomoji_favorites",
                MODE_PRIVATE
            );

        java.util.ArrayList<String> out=
            new java.util.ArrayList<>();

        for(int i=0;i<30;i++) {
            String value=sp.getString("f"+i,"");

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


    boolean toggleFavoriteKaomoji(String value) {
        if(value==null || value.isEmpty())
            return false;

        SharedPreferences sp=
            getSharedPreferences(
                "keykii_kaomoji_favorites",
                MODE_PRIVATE
            );

        java.util.ArrayList<String> favorites=
            loadFavoriteKaomoji();

        boolean added=!favorites.remove(value);

        if(added)
            favorites.add(0,value);

        while(favorites.size()>30)
            favorites.remove(favorites.size()-1);

        SharedPreferences.Editor e=sp.edit();

        for(int i=0;i<30;i++)
            e.remove("f"+i);

        for(int i=0;i<favorites.size();i++)
            e.putString("f"+i,favorites.get(i));

        e.apply();
        return added;
    }


    void buildKaomojiPanel() {

        loadFullKaomoji();

        if(
            fullKaomojiCategories==null ||
            fullKaomojiCategories.isEmpty()
        )
            return;

        // Dynamic sections stay separate from the asset-backed library.
        // 2.36.0 added Recents; 2.36.1 adds user-controlled Favorites.
        fullKaomoji.remove("Recent");
        fullKaomoji.remove("Favorites");
        fullKaomojiCategories.remove("Recent");
        fullKaomojiCategories.remove("Favorites");

        java.util.ArrayList<String> recentKaomoji=
            loadRecentKaomoji();

        java.util.ArrayList<String> favoriteKaomoji=
            loadFavoriteKaomoji();

        int dynamicIndex=0;

        if(!recentKaomoji.isEmpty()) {
            fullKaomoji.put("Recent",recentKaomoji);
            fullKaomojiCategories.add(dynamicIndex++,"Recent");
        }

        if(!favoriteKaomoji.isEmpty()) {
            fullKaomoji.put("Favorites",favoriteKaomoji);
            fullKaomojiCategories.add(dynamicIndex,"Favorites");
        }

        final java.util.HashSet<String> favoriteKaomojiSet=
            new java.util.HashSet<>(favoriteKaomoji);

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

                                rememberKaomoji(value);
                            });

                            // Long-press only manages favorites; it does not
                            // type the kaomoji or interfere with normal taps.
                            face.setOnLongClickListener(v -> {
                                boolean added=
                                    toggleFavoriteKaomoji(value);

                                voiceToast(
                                    added
                                    ? "Added to Kaomoji Favorites"
                                    : "Removed from Kaomoji Favorites"
                                );

                                java.util.ArrayList<String> favorites=
                                    loadFavoriteKaomoji();

                                if(!favorites.isEmpty()) {
                                    kaomojiCategory=
                                        loadRecentKaomoji().isEmpty()
                                        ? 0
                                        : 1;
                                } else {
                                    kaomojiCategory=0;
                                }

                                showPage();
                                return true;
                            });

                            if(favoriteKaomojiSet.contains(value)) {
                                face.setBackground(
                                    round(
                                        accentFillColor(),
                                        14,
                                        borderColor()
                                    )
                                );
                            }
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

        if(isIncognitoMode())
            return;

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
                dp(24)
            )
        );

        final android.widget.ViewFlipper slides=
            new android.widget.ViewFlipper(this);

        LinearLayout mainSlide=
            new LinearLayout(this);

        mainSlide.setOrientation(
            LinearLayout.VERTICAL
        );

        toolsSlideRow(
            mainSlide,
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

        toolsSlideRow(
            mainSlide,
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

        toolsSlideRow(
            mainSlide,
            new String[]{
                "🧮  Calculator",
                "🌐  Translator"
            },
            new Runnable[]{
                () -> {
                    page=6;
                    buildShell();
                },
                () -> {
                    translatorSourceText="";
                    translatorResult="";
                    translatorStatus="";
                    translatorLanguageChooser=false;
                    translatorLanguageSearch="";
                    translatorCursor=0;
                    translatorRequestId++;
                    symbols=false;
                    symbolPage=1;
                    shift=false;
                    page=8;
                    buildShell();
                }
            }
        );

        toolsSlideRow(
            mainSlide,
            new String[]{
                "✓  Grammar Fix"
            },
            new Runnable[]{
                () -> {
                    grammarSourceText="";
                    grammarCorrectedText="";
                    grammarStatus="";
                    grammarCursor=0;
                    grammarRequestId++;
                    symbols=false;
                    symbolPage=1;
                    shift=false;
                    page=9;
                    buildShell();
                }
            }
        );

        LinearLayout moreSlide=
            new LinearLayout(this);

        moreSlide.setOrientation(
            LinearLayout.VERTICAL
        );

        toolsSlideRow(
            moreSlide,
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

        toolsSlideRow(
            moreSlide,
            new String[]{
                "◐  Theme",
                "⚙  Settings"
            },
            new Runnable[]{
                () -> openKeyKiiSettings("theme"),
                () -> openKeyKiiSettings("")
            }
        );

        toolsSlideRow(
            moreSlide,
            new String[]{
                isIncognitoMode()
                    ? "🕶  Incognito ON"
                    : "🕶  Incognito",
                "🔒  Privacy"
            },
            new Runnable[]{
                () -> toggleIncognitoMode(),
                () -> openKeyKiiSettings("privacy")
            }
        );

        toolsSlideRow(
            moreSlide,
            new String[]{
                "Aa  Text case",
                "✦  Smart text"
            },
            new Runnable[]{
                () -> {
                    page=7;
                    buildShell();
                },
                () -> {
                    page=10;
                    buildShell();
                }
            }
        );

        slides.addView(
            mainSlide,
            new android.widget.FrameLayout.LayoutParams(
                -1,
                -2
            )
        );

        slides.addView(
            moreSlide,
            new android.widget.FrameLayout.LayoutParams(
                -1,
                -2
            )
        );

        body.addView(
            slides,
            new LinearLayout.LayoutParams(
                -1,
                -2
            )
        );

        LinearLayout pager=
            new LinearLayout(this);

        pager.setGravity(
            Gravity.CENTER
        );

        TextView previous=
            new TextView(this);

        previous.setText("‹");
        previous.setTextColor(textColor());
        previous.setTextSize(25);
        previous.setGravity(Gravity.CENTER);

        TextView dots=
            new TextView(this);

        dots.setText("●  ○");
        dots.setTextColor(textColor());
        dots.setAlpha(.62f);
        dots.setTextSize(13);
        dots.setGravity(Gravity.CENTER);

        TextView next=
            new TextView(this);

        next.setText("›");
        next.setTextColor(textColor());
        next.setTextSize(25);
        next.setGravity(Gravity.CENTER);

        Runnable refreshPager=
            () -> {
                int index=
                    slides.getDisplayedChild();

                dots.setText(
                    index==0
                    ? "●  ○"
                    : "○  ●"
                );

                previous.setAlpha(
                    index==0
                    ? .28f
                    : 1f
                );

                next.setAlpha(
                    index==slides.getChildCount()-1
                    ? .28f
                    : 1f
                );
            };

        previous.setOnClickListener(v -> {
            if(slides.getDisplayedChild()>0) {
                slides.setInAnimation(
                    this,
                    android.R.anim.slide_in_left
                );

                slides.setOutAnimation(
                    this,
                    android.R.anim.slide_out_right
                );

                slides.showPrevious();
                refreshPager.run();
            }
        });

        next.setOnClickListener(v -> {
            if(
                slides.getDisplayedChild()<
                slides.getChildCount()-1
            ) {
                slides.setInAnimation(
                    this,
                    android.R.anim.slide_in_left
                );

                slides.setOutAnimation(
                    this,
                    android.R.anim.slide_out_right
                );

                slides.showNext();
                refreshPager.run();
            }
        });

        final float[] swipeStartX=
            new float[]{0f};

        slides.setOnTouchListener(
            (v,event) -> {
                int action=
                    event.getActionMasked();

                if(action==MotionEvent.ACTION_DOWN) {
                    swipeStartX[0]=
                        event.getRawX();

                    return true;
                }

                if(action==MotionEvent.ACTION_UP) {
                    float dx=
                        event.getRawX()-
                        swipeStartX[0];

                    if(
                        Math.abs(dx)>=dp(48)
                    ) {
                        if(
                            dx<0 &&
                            slides.getDisplayedChild()<
                            slides.getChildCount()-1
                        ) {
                            slides.setInAnimation(
                                this,
                                android.R.anim.slide_in_left
                            );

                            slides.setOutAnimation(
                                this,
                                android.R.anim.slide_out_right
                            );

                            slides.showNext();

                        } else if(
                            dx>0 &&
                            slides.getDisplayedChild()>0
                        ) {
                            slides.setInAnimation(
                                this,
                                android.R.anim.slide_in_left
                            );

                            slides.setOutAnimation(
                                this,
                                android.R.anim.slide_out_right
                            );

                            slides.showPrevious();
                        }

                        refreshPager.run();
                    }

                    return true;
                }

                if(action==MotionEvent.ACTION_CANCEL)
                    return true;

                return true;
            }
        );

        pager.addView(
            previous,
            new LinearLayout.LayoutParams(
                dp(52),
                dp(34)
            )
        );

        pager.addView(
            dots,
            new LinearLayout.LayoutParams(
                dp(82),
                dp(34)
            )
        );

        pager.addView(
            next,
            new LinearLayout.LayoutParams(
                dp(52),
                dp(34)
            )
        );

        body.addView(
            pager,
            new LinearLayout.LayoutParams(
                -1,
                dp(34)
            )
        );

        refreshPager.run();
    }


    void toolsSlideRow(
        LinearLayout parent,
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
            card.setGravity(
                Gravity.CENTER_VERTICAL
            );

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

            row.addView(
                card,
                p
            );
        }

        parent.addView(
            row,
            new LinearLayout.LayoutParams(
                -1,
                -2
            )
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

    void buildTranslatorPanel() {

        if(translatorLanguageChooser) {
            buildTranslatorLanguageChooser();
            return;
        }

        if(
            translatorSourceText==null ||
            translatorSourceText.isEmpty()
        ) {
            captureSelectedTextForTranslator();
        }

        LinearLayout top=
            new LinearLayout(this);

        top.setGravity(Gravity.CENTER);

        TextView back=
            translatorLanguageButton("←");

        back.setOnClickListener(
            v -> {
                syncTranslatorSourceFromView();
                page=4;
                buildShell();
            }
        );

        translatorSourceLanguageView=
            translatorLanguageButton(
                translatorSourceLanguage<0
                ? "Detect language"
                : translatorLanguageNames[
                    translatorSourceLanguage
                ]
            );

        TextView swap=
            translatorLanguageButton("⇄");

        translatorTargetLanguageView=
            translatorLanguageButton(
                translatorLanguageNames[
                    translatorTargetLanguage
                ]
            );

        translatorSourceLanguageView.setOnClickListener(
            v -> openTranslatorLanguageChooser(true)
        );

        translatorTargetLanguageView.setOnClickListener(
            v -> openTranslatorLanguageChooser(false)
        );

        swap.setOnClickListener(
            v -> swapTranslatorLanguages()
        );

        top.addView(
            back,
            new LinearLayout.LayoutParams(
                dp(46),dp(44)
            )
        );

        LinearLayout.LayoutParams sourceParams=
            new LinearLayout.LayoutParams(
                0,dp(44),1.35f
            );
        sourceParams.setMargins(dp(4),0,dp(4),0);

        top.addView(
            translatorSourceLanguageView,
            sourceParams
        );

        top.addView(
            swap,
            new LinearLayout.LayoutParams(
                dp(50),dp(44)
            )
        );

        LinearLayout.LayoutParams targetParams=
            new LinearLayout.LayoutParams(
                0,dp(44),1f
            );
        targetParams.setMargins(dp(4),0,0,0);

        top.addView(
            translatorTargetLanguageView,
            targetParams
        );

        body.addView(
            top,
            new LinearLayout.LayoutParams(
                -1,dp(48)
            )
        );

        translatorSourceView=
            new EditText(this);

        translatorSourceView.setTextColor(textColor());
        translatorSourceView.setHintTextColor(textColor());
        translatorSourceView.setHint("Type here to translate");
        translatorSourceView.setTextSize(15);
        translatorSourceView.setGravity(
            Gravity.START |
            Gravity.CENTER_VERTICAL
        );
        translatorSourceView.setPadding(
            dp(14),dp(8),dp(14),dp(8)
        );
        translatorSourceView.setSingleLine(false);
        translatorSourceView.setMaxLines(3);
        translatorSourceView.setCursorVisible(true);
        translatorSourceView.setFocusable(true);
        translatorSourceView.setFocusableInTouchMode(true);
        translatorSourceView.setTextIsSelectable(true);
        translatorSourceView.setInputType(
            android.text.InputType.TYPE_CLASS_TEXT |
            android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE |
            android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        );

        if(android.os.Build.VERSION.SDK_INT>=21)
            translatorSourceView.setShowSoftInputOnFocus(false);

        translatorSourceView.setBackground(
            round(
                keyColor(false),
                18,
                borderColor()
            )
        );

        translatorSourceView.setText(
            translatorSourceText==null
            ? ""
            : translatorSourceText
        );

        translatorCursor=
            Math.max(
                0,
                Math.min(
                    translatorCursor,
                    translatorSourceView.length()
                )
            );

        if(
            translatorSourceView.length()>0 &&
            translatorCursor==0
        ) {
            translatorCursor=
                translatorSourceView.length();
        }

        try {
            translatorSourceView.setSelection(
                translatorCursor
            );
        } catch(Exception ignored) {}

        translatorSourceView.setOnClickListener(
            v -> syncTranslatorCursorFromView()
        );

        translatorSourceView.setOnTouchListener(
            (v,event) -> {
                v.post(
                    () -> syncTranslatorCursorFromView()
                );
                return false;
            }
        );

        body.addView(
            translatorSourceView,
            new LinearLayout.LayoutParams(
                -1,dp(72)
            )
        );

        translatorResultView=
            new TextView(this);

        translatorResultView.setTextColor(textColor());
        translatorResultView.setTextSize(12);
        translatorResultView.setAlpha(.72f);
        translatorResultView.setGravity(
            Gravity.CENTER_VERTICAL
        );
        translatorResultView.setPadding(
            dp(12),0,dp(12),0
        );

        body.addView(
            translatorResultView,
            new LinearLayout.LayoutParams(
                -1,dp(30)
            )
        );

        LinearLayout actions=
            new LinearLayout(this);

        actions.setGravity(Gravity.CENTER);

        TextView clear=
            translatorLanguageButton("Clear");

        TextView translate=
            translatorLanguageButton("Translate");

        translate.setBackground(
            round(
                accentFillColor(),
                18,
                accentColor()
            )
        );

        clear.setOnClickListener(
            v -> {
                translatorSourceText="";
                translatorResult="";
                translatorStatus="";
                translatorCursor=0;
                translatorRequestId++;
                updateTranslatorPanel();
                focusTranslatorSource();
            }
        );

        translate.setOnClickListener(
            v -> {
                syncTranslatorSourceFromView();
                translateSelectedTextOnline();
            }
        );

        LinearLayout.LayoutParams actionParams=
            new LinearLayout.LayoutParams(
                0,dp(42),1f
            );
        actionParams.setMargins(
            dp(3),dp(2),dp(3),dp(2)
        );

        actions.addView(
            clear,
            actionParams
        );

        actions.addView(
            translate,
            new LinearLayout.LayoutParams(
                0,dp(42),1f
            )
        );

        body.addView(
            actions,
            new LinearLayout.LayoutParams(
                -1,dp(46)
            )
        );

        translatorStatusView=
            new TextView(this);

        translatorStatusView.setTextColor(textColor());
        translatorStatusView.setTextSize(9);
        translatorStatusView.setAlpha(.55f);
        translatorStatusView.setGravity(Gravity.CENTER);

        body.addView(
            translatorStatusView,
            new LinearLayout.LayoutParams(
                -1,dp(22)
            )
        );

        updateTranslatorPanel();

        translatorSourceView.post(
            () -> focusTranslatorSource()
        );

        buildKeyboard();
    }


    void buildTranslatorLanguageChooser() {

        LinearLayout header=
            new LinearLayout(this);

        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView back=
            translatorLanguageButton("←");

        back.setOnClickListener(
            v -> {
                translatorLanguageChooser=false;
                translatorLanguageSearch="";
                showPage();
            }
        );

        TextView title=
            new TextView(this);

        title.setText(
            translatorChoosingSource
            ? "Translate from"
            : "Translate to"
        );
        title.setTextColor(textColor());
        title.setTextSize(16);
        title.setGravity(Gravity.CENTER);

        header.addView(
            back,
            new LinearLayout.LayoutParams(
                dp(48),dp(42)
            )
        );

        header.addView(
            title,
            new LinearLayout.LayoutParams(
                0,dp(42),1f
            )
        );

        body.addView(
            header,
            new LinearLayout.LayoutParams(
                -1,dp(46)
            )
        );

        TextView search=
            translatorTextBox(
                "🔍  Search languages"
            );

        search.setText(
            translatorLanguageSearch==null ||
            translatorLanguageSearch.isEmpty()
            ? "🔍  Search languages"
            : "🔍  "+translatorLanguageSearch
        );
        search.setTextSize(13);
        search.setGravity(Gravity.CENTER_VERTICAL);

        body.addView(
            search,
            new LinearLayout.LayoutParams(
                -1,dp(46)
            )
        );

        android.widget.ScrollView scroll=
            new android.widget.ScrollView(this);

        translatorLanguageList=
            new LinearLayout(this);

        translatorLanguageList.setOrientation(
            LinearLayout.VERTICAL
        );

        scroll.addView(
            translatorLanguageList,
            new android.widget.ScrollView.LayoutParams(
                -1,-2
            )
        );

        body.addView(
            scroll,
            new LinearLayout.LayoutParams(
                -1,dp(150)
            )
        );

        refreshTranslatorLanguageList();
        buildKeyboard();
    }


    void openTranslatorLanguageChooser(
        boolean source
    ) {
        syncTranslatorSourceFromView();
        translatorLanguageChooser=true;
        translatorChoosingSource=source;
        translatorLanguageSearch="";
        translatorResult="";
        translatorStatus="";
        translatorRequestId++;
        symbols=false;
        symbolPage=1;
        shift=false;
        showPage();
    }


    void refreshTranslatorLanguageList() {
        if(translatorLanguageList==null)
            return;

        translatorLanguageList.removeAllViews();

        String q=
            translatorLanguageSearch==null
            ? ""
            : translatorLanguageSearch
                .trim()
                .toLowerCase(
                    java.util.Locale.ROOT
                );

        if(
            translatorChoosingSource &&
            (
                q.isEmpty() ||
                "detect language".contains(q) ||
                "detect".contains(q)
            )
        ) {
            addTranslatorLanguageChoice(
                "Detect language",
                -1
            );
        }

        for(
            int index=0;
            index<translatorLanguageNames.length;
            index++
        ) {
            String name=
                translatorLanguageNames[index];

            if(
                !q.isEmpty() &&
                !name.toLowerCase(
                    java.util.Locale.ROOT
                ).contains(q)
            ) {
                continue;
            }

            addTranslatorLanguageChoice(name,index);
        }

        if(
            translatorLanguageList.getChildCount()==0
        ) {
            TextView none=
                new TextView(this);

            none.setText("No language found");
            none.setTextColor(textColor());
            none.setAlpha(.52f);
            none.setTextSize(12);
            none.setGravity(Gravity.CENTER);

            translatorLanguageList.addView(
                none,
                new LinearLayout.LayoutParams(
                    -1,dp(46)
                )
            );
        }
    }


    void addTranslatorLanguageChoice(
        String name,
        int index
    ) {
        TextView item=
            new TextView(this);

        boolean selected=
            translatorChoosingSource
            ? translatorSourceLanguage==index
            : translatorTargetLanguage==index;

        item.setText(
            selected
            ? "✓  "+name
            : "    "+name
        );
        item.setTextColor(textColor());
        item.setTextSize(14);
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setPadding(dp(16),0,dp(12),0);

        if(selected) {
            item.setBackground(
                round(
                    accentFillColor(),
                    16,
                    accentColor()
                )
            );
        }

        item.setOnClickListener(
            v -> {
                if(translatorChoosingSource)
                    translatorSourceLanguage=index;
                else
                    translatorTargetLanguage=index;

                translatorLanguageChooser=false;
                translatorLanguageSearch="";
                translatorResult="";
                translatorStatus="";
                translatorRequestId++;
                symbols=false;
                symbolPage=1;
                shift=false;
                showPage();
            }
        );

        LinearLayout.LayoutParams p=
            new LinearLayout.LayoutParams(
                -1,dp(46)
            );
        p.setMargins(dp(3),dp(2),dp(3),dp(2));

        translatorLanguageList.addView(item,p);
    }


    void syncTranslatorCursorFromView() {
        if(translatorSourceView==null)
            return;

        try {
            translatorCursor=
                Math.max(
                    0,
                    translatorSourceView.getSelectionStart()
                );
        } catch(Exception ignored) {}
    }


    void syncTranslatorSourceFromView() {
        if(translatorSourceView==null)
            return;

        translatorSourceText=
            translatorSourceView.getText()==null
            ? ""
            : translatorSourceView
                .getText()
                .toString();

        syncTranslatorCursorFromView();
    }


    void focusTranslatorSource() {
        if(translatorSourceView==null)
            return;

        translatorSourceView.requestFocus();
        translatorSourceView.setCursorVisible(true);

        translatorCursor=
            Math.max(
                0,
                Math.min(
                    translatorCursor,
                    translatorSourceView.length()
                )
            );

        try {
            translatorSourceView.setSelection(
                translatorCursor
            );
        } catch(Exception ignored) {}
    }


    boolean handleTranslatorSpacebarTouch(
        View v,
        MotionEvent e
    ) {
        int action=e.getActionMasked();

        if(action==MotionEvent.ACTION_DOWN) {
            syncTranslatorSourceFromView();
            spaceDownX=e.getX();
            spaceCursorDragging=false;
            spaceStartSelection=translatorCursor;

            v.animate()
             .scaleX(1.02f)
             .scaleY(1.02f)
             .setDuration(30)
             .start();

            return true;
        }

        if(action==MotionEvent.ACTION_MOVE) {
            float dx=e.getX()-spaceDownX;

            if(Math.abs(dx)>=dp(9)) {
                spaceCursorDragging=true;

                int steps=
                    Math.round(
                        dx/(float)Math.max(
                            dp(14),1
                        )
                    );

                translatorCursor=
                    Math.max(
                        0,
                        Math.min(
                            translatorSourceText.length(),
                            spaceStartSelection+steps
                        )
                    );

                focusTranslatorSource();
            }

            return true;
        }

        if(
            action==MotionEvent.ACTION_UP ||
            action==MotionEvent.ACTION_CANCEL
        ) {
            v.animate()
             .scaleX(1f)
             .scaleY(1f)
             .setDuration(45)
             .start();

            if(
                action==MotionEvent.ACTION_UP &&
                !spaceCursorDragging
            ) {
                press("SPACE");
            }

            spaceCursorDragging=false;
            return true;
        }

        return true;
    }


    boolean handleTranslatorKey(
        String action
    ) {
        if(page!=8)
            return false;

        if(action==null)
            return true;

        if(
            action.equals("123") ||
            action.equals("ABC") ||
            action.equals("SYM1") ||
            action.equals("SYM2") ||
            action.equals("SHIFT")
        ) {
            syncTranslatorSourceFromView();
            return false;
        }

        if(action.equals("KEYS")) {
            syncTranslatorSourceFromView();
            translatorLanguageChooser=false;
            translatorLanguageSearch="";
            page=0;
            buildShell();
            return true;
        }

        if(action.equals("BACK")) {
            translatorBackspace();
            return true;
        }

        if(action.equals("ENTER")) {
            if(!translatorLanguageChooser) {
                syncTranslatorSourceFromView();
                translateSelectedTextOnline();
            }
            return true;
        }

        String value=
            action.equals("SPACE")
            ? " "
            : action;

        if(
            value.length()>2 ||
            action.equals("EMOJI") ||
            action.equals("CLIPBOARD") ||
            action.equals("SHORTCUTS")
        ) {
            return true;
        }

        if(
            shift &&
            !symbols &&
            value.length()==1
        ) {
            value=
                value.toUpperCase(
                    java.util.Locale.getDefault()
                );

            if(!capsLock)
                shift=false;
        }

        if(translatorLanguageChooser) {
            if(
                value.equals(" ") ||
                Character.isLetterOrDigit(
                    value.charAt(0)
                )
            ) {
                translatorLanguageSearch+=value;
                showPage();
            }
            return true;
        }

        syncTranslatorSourceFromView();

        if(translatorSourceText==null)
            translatorSourceText="";

        translatorCursor=
            Math.max(
                0,
                Math.min(
                    translatorCursor,
                    translatorSourceText.length()
                )
            );

        if(translatorSourceText.length()<450) {
            translatorSourceText=
                translatorSourceText.substring(
                    0,translatorCursor
                )+
                value+
                translatorSourceText.substring(
                    translatorCursor
                );

            translatorCursor+=value.length();
            translatorResult="";
            translatorStatus="";
            translatorRequestId++;
            updateTranslatorPanel();
            focusTranslatorSource();
        }

        return true;
    }


    void translatorBackspace() {
        if(translatorLanguageChooser) {
            if(
                translatorLanguageSearch!=null &&
                !translatorLanguageSearch.isEmpty()
            ) {
                int end=
                    translatorLanguageSearch.length();

                int start=
                    translatorLanguageSearch.offsetByCodePoints(
                        end,-1
                    );

                translatorLanguageSearch=
                    translatorLanguageSearch.substring(
                        0,start
                    );

                showPage();
            }
            return;
        }

        syncTranslatorSourceFromView();

        if(
            translatorSourceText!=null &&
            !translatorSourceText.isEmpty() &&
            translatorCursor>0
        ) {
            int start=
                translatorSourceText.offsetByCodePoints(
                    translatorCursor,-1
                );

            translatorSourceText=
                translatorSourceText.substring(
                    0,start
                )+
                translatorSourceText.substring(
                    translatorCursor
                );

            translatorCursor=start;
            translatorResult="";
            translatorStatus="";
            translatorRequestId++;
            updateTranslatorPanel();
            focusTranslatorSource();
            return;
        }

        if(
            translatorSourceText==null ||
            translatorSourceText.isEmpty()
        ) {
            InputConnection ic=
                getCurrentInputConnection();

            if(ic!=null)
                deleteOneBeforeCursor(ic);
        }
    }


    void swapTranslatorLanguages() {
        syncTranslatorSourceFromView();

        int source=
            translatorSourceLanguage;

        if(source<0)
            source=0;

        int target=
            translatorTargetLanguage;

        translatorSourceLanguage=target;
        translatorTargetLanguage=source;

        if(
            translatorResult!=null &&
            !translatorResult.isEmpty()
        ) {
            String oldSource=
                translatorSourceText;

            translatorSourceText=
                translatorResult;

            translatorResult=
                oldSource==null
                ? ""
                : oldSource;

            translatorCursor=
                translatorSourceText.length();
        }

        translatorStatus="";
        translatorRequestId++;
        updateTranslatorPanel();
        focusTranslatorSource();
    }


    String normalizeDetectedLanguageCode(
        String code
    ) {
        if(code==null)
            return "";

        String clean=
            code.toLowerCase(
                java.util.Locale.ROOT
            );

        if(clean.equals("fil"))
            return "tl";

        if(clean.startsWith("zh"))
            return "zh";

        int dash=clean.indexOf('-');
        if(dash>0)
            clean=clean.substring(0,dash);

        return clean;
    }


    String strongDetectedLanguageCode(
        String text
    ) {
        if(text==null)
            return "";

        String lower=
            text.toLowerCase(
                java.util.Locale.ROOT
            );

        String clean=
            " "+
            lower.replaceAll(
                "[^\\p{L}\\p{Nd}]+",
                " "
            ).trim()+
            " ";

        String[] cebuanoStrong={
            "gwapa","gwapo","maayo","maayong","buntag","gabii",
            "dili","unsa","ngano","asa","kinsa","kanus","kaayo",
            "nimo","nako","imong","akong","gyud","pud","diri",
            "adto","palihug","amping","gihigugma","gusto"
        };

        String[] filipinoStrong={
            "maganda","magandang","hindi","bakit","saan","sino",
            "kailan","kumusta","kamusta","umaga","gabi","ngayon",
            "bukas","kahapon","mahal","opo","po","salamat",
            "paano","ano","gusto","akin","iyo"
        };

        String[] englishStrong={
            "hello","hi","good","morning","afternoon","evening",
            "beautiful","thanks","thank","please","what","where",
            "why","when","how","you","your","this","that","the",
            "and","is","are","was","were","have","has","can",
            "will","with","from"
        };

        String[] turkishStrong={
            "merhaba","gunaydin","günaydın","teşekkür","tesekkur",
            "evet","hayır","hayir","nasilsin","nasılsın","güzel"
        };

        int ceb=0;
        int fil=0;
        int en=0;
        int tr=0;

        for(String w:cebuanoStrong) {
            if(clean.contains(" "+w+" "))
                ceb++;
        }

        for(String w:filipinoStrong) {
            if(clean.contains(" "+w+" "))
                fil++;
        }

        for(String w:englishStrong) {
            if(clean.contains(" "+w+" "))
                en++;
        }

        for(String w:turkishStrong) {
            if(clean.contains(" "+w+" "))
                tr++;
        }

        // High-value Philippine words should win even in very short phrases.
        if(
            clean.contains(" gwapa ") ||
            clean.contains(" gwapo ") ||
            clean.contains(" dili ") ||
            clean.contains(" unsa ") ||
            clean.contains(" kaayo ") ||
            clean.contains(" maayong ")
        )
            return "ceb";

        if(
            clean.contains(" maganda ") ||
            clean.contains(" magandang ") ||
            clean.contains(" hindi ") ||
            clean.contains(" kumusta ") ||
            clean.contains(" kamusta ")
        )
            return "tl";

        if(tr>=1)
            return "tr";

        if(ceb>=2 && ceb>fil)
            return "ceb";

        if(fil>=2 && fil>=ceb)
            return "tl";

        if(en>=2)
            return "en";

        return "";
    }


    String fallbackDetectedLanguageCode(
        String text
    ) {
        String strong=
            strongDetectedLanguageCode(text);

        if(!strong.isEmpty())
            return strong;

        if(text==null)
            return "en";

        for(
            int offset=0;
            offset<text.length();
        ) {
            int cp=text.codePointAt(offset);

            if(cp>=0x3040 && cp<=0x30FF)
                return "ja";
            if(cp>=0xAC00 && cp<=0xD7AF)
                return "ko";
            if(cp>=0x4E00 && cp<=0x9FFF)
                return "zh";
            if(cp>=0x0600 && cp<=0x06FF)
                return "ar";
            if(cp>=0x0400 && cp<=0x04FF)
                return "ru";
            if(cp>=0x0370 && cp<=0x03FF)
                return "el";
            if(cp>=0x0590 && cp<=0x05FF)
                return "he";
            if(cp>=0x0E00 && cp<=0x0E7F)
                return "th";
            if(cp>=0x0900 && cp<=0x097F)
                return "hi";

            offset+=Character.charCount(cp);
        }

        return "en";
    }


    boolean isTranslatorLanguageSupported(
        String code
    ) {
        if(code==null || code.isEmpty())
            return false;

        for(String supported:translatorLanguageCodes) {
            if(supported.equalsIgnoreCase(code))
                return true;
        }

        return false;
    }


    String translatorDisplayNameForCode(
        String code
    ) {
        if(code==null)
            return "language";

        for(
            int i=0;
            i<translatorLanguageCodes.length;
            i++
        ) {
            if(
                translatorLanguageCodes[i]
                    .equalsIgnoreCase(code)
            ) {
                return translatorLanguageNames[i];
            }
        }

        return code;
    }


    TextView translatorLanguageButton(
        String text
    ) {
        TextView button=
            new TextView(this);

        button.setText(text);
        button.setTextColor(textColor());
        button.setTextSize(13);
        button.setGravity(Gravity.CENTER);
        button.setSingleLine(true);
        button.setBackground(
            round(
                keyColor(false),
                18,
                borderColor()
            )
        );

        return button;
    }


    TextView translatorTextBox(
        String placeholder
    ) {
        TextView box=
            new TextView(this);

        box.setText(placeholder);
        box.setTextColor(textColor());
        box.setTextSize(13);
        box.setGravity(
            Gravity.START |
            Gravity.CENTER_VERTICAL
        );
        box.setPadding(
            dp(14),dp(8),dp(14),dp(8)
        );
        box.setMaxLines(3);
        box.setBackground(
            round(
                keyColor(false),
                18,
                borderColor()
            )
        );

        return box;
    }


    void captureSelectedTextForTranslator() {
        InputConnection ic=
            getCurrentInputConnection();

        if(ic==null)
            return;

        try {
            CharSequence selected=
                ic.getSelectedText(0);

            if(
                selected!=null &&
                selected.length()>0
            ) {
                translatorSourceText=
                    trimTranslatorUtf8(
                        selected.toString(),
                        450
                    );

                translatorCursor=
                    translatorSourceText.length();
            }
        } catch(Exception ignored) {}
    }


    String trimTranslatorUtf8(
        String text,
        int maxBytes
    ) {
        if(text==null)
            return "";

        StringBuilder out=
            new StringBuilder();

        int used=0;

        for(
            int offset=0;
            offset<text.length();
        ) {
            int cp=
                text.codePointAt(offset);

            String piece=
                new String(
                    Character.toChars(cp)
                );

            int bytes;

            try {
                bytes=
                    piece.getBytes("UTF-8").length;
            } catch(Exception e) {
                bytes=piece.length();
            }

            if(used+bytes>maxBytes)
                break;

            out.append(piece);
            used+=bytes;
            offset+=Character.charCount(cp);
        }

        return out.toString();
    }


    void updateTranslatorPanel() {
        if(translatorSourceLanguageView!=null) {
            translatorSourceLanguageView.setText(
                translatorSourceLanguage<0
                ? "Detect language"
                : translatorLanguageNames[
                    translatorSourceLanguage
                ]
            );
        }

        if(translatorTargetLanguageView!=null) {
            translatorTargetLanguageView.setText(
                translatorLanguageNames[
                    translatorTargetLanguage
                ]
            );
        }

        if(translatorSourceView!=null) {
            String wanted=
                translatorSourceText==null
                ? ""
                : translatorSourceText;

            String current=
                translatorSourceView.getText()==null
                ? ""
                : translatorSourceView
                    .getText()
                    .toString();

            if(!current.equals(wanted))
                translatorSourceView.setText(wanted);

            translatorCursor=
                Math.max(
                    0,
                    Math.min(
                        translatorCursor,
                        translatorSourceView.length()
                    )
                );

            try {
                translatorSourceView.setSelection(
                    translatorCursor
                );
            } catch(Exception ignored) {}
        }

        if(translatorResultView!=null) {
            translatorResultView.setText(
                translatorResult==null ||
                translatorResult.isEmpty()
                ? " "
                : translatorResult
            );
        }

        if(translatorStatusView!=null) {
            String status=
                translatorStatus==null
                ? ""
                : translatorStatus;

            if(status.isEmpty())
                status=
                    "On-device translation • Cebuano uses internet fallback";

            translatorStatusView.setText(status);
        }
    }


    void translateSelectedTextOnline() {
        syncTranslatorSourceFromView();

        if(
            translatorSourceText==null ||
            translatorSourceText.trim().isEmpty()
        ) {
            translatorStatus="Type text to translate";
            updateTranslatorPanel();
            return;
        }

        final String sourceText=
            trimTranslatorUtf8(
                translatorSourceText,
                450
            );

        final int request=
            ++translatorRequestId;

        translatorResult="";
        translatorStatus=
            translatorSourceLanguage<0
            ? "Detecting language…"
            : "Preparing translation…";
        updateTranslatorPanel();

        if(translatorSourceLanguage>=0) {
            startMlKitTranslation(
                sourceText,
                translatorLanguageCodes[
                    translatorSourceLanguage
                ],
                translatorLanguageCodes[
                    translatorTargetLanguage
                ],
                request
            );
            return;
        }

        String strongDetected=
            strongDetectedLanguageCode(
                sourceText
            );

        if(!strongDetected.isEmpty()) {
            translatorStatus=
                "Detected "+
                translatorDisplayNameForCode(
                    strongDetected
                );

            updateTranslatorPanel();

            startMlKitTranslation(
                sourceText,
                strongDetected,
                translatorLanguageCodes[
                    translatorTargetLanguage
                ],
                request
            );

            return;
        }

        final com.google.mlkit.nl.languageid.LanguageIdentifier identifier=
            com.google.mlkit.nl.languageid.LanguageIdentification
                .getClient();

        identifier.identifyLanguage(
            sourceText
        ).addOnSuccessListener(
            code -> {
                identifier.close();

                if(request!=translatorRequestId)
                    return;

                String detected=
                    normalizeDetectedLanguageCode(code);

                if(
                    detected==null ||
                    detected.isEmpty() ||
                    detected.equals("und") ||
                    !isTranslatorLanguageSupported(detected)
                ) {
                    detected=
                        fallbackDetectedLanguageCode(
                            sourceText
                        );
                }

                if(
                    !isTranslatorLanguageSupported(
                        detected
                    )
                ) {
                    translatorStatus=
                        "Detected language is not supported";
                    updateTranslatorPanel();
                    return;
                }

                translatorStatus=
                    "Detected "+
                    translatorDisplayNameForCode(
                        detected
                    );
                updateTranslatorPanel();

                startMlKitTranslation(
                    sourceText,
                    detected,
                    translatorLanguageCodes[
                        translatorTargetLanguage
                    ],
                    request
                );
            }
        ).addOnFailureListener(
            error -> {
                identifier.close();

                if(request!=translatorRequestId)
                    return;

                String detected=
                    fallbackDetectedLanguageCode(
                        sourceText
                    );

                startMlKitTranslation(
                    sourceText,
                    detected,
                    translatorLanguageCodes[
                        translatorTargetLanguage
                    ],
                    request
                );
            }
        );
    }


    void startMlKitTranslation(
        String sourceText,
        String sourceCode,
        String targetCode,
        int request
    ) {
        if(
            sourceCode==null ||
            targetCode==null
        ) {
            translatorStatus="Language not supported";
            updateTranslatorPanel();
            return;
        }

        if(
            sourceCode.equalsIgnoreCase(
                targetCode
            )
        ) {
            translatorResult=sourceText;
            translatorStatus=
                "Already "+
                translatorDisplayNameForCode(
                    targetCode
                );
            updateTranslatorPanel();
            return;
        }

        if(
            sourceCode.equalsIgnoreCase("ceb") ||
            targetCode.equalsIgnoreCase("ceb")
        ) {
            startOnlineTranslatorFallback(
                sourceText,
                sourceCode,
                targetCode,
                request
            );
            return;
        }

        String sourceTag=
            com.google.mlkit.nl.translate.TranslateLanguage
                .fromLanguageTag(sourceCode);

        String targetTag=
            com.google.mlkit.nl.translate.TranslateLanguage
                .fromLanguageTag(targetCode);

        if(
            sourceTag==null ||
            targetTag==null
        ) {
            startOnlineTranslatorFallback(
                sourceText,
                sourceCode,
                targetCode,
                request
            );
            return;
        }

        com.google.mlkit.nl.translate.TranslatorOptions options=
            new com.google.mlkit.nl.translate.TranslatorOptions
                .Builder()
                .setSourceLanguage(sourceTag)
                .setTargetLanguage(targetTag)
                .build();

        final com.google.mlkit.nl.translate.Translator translator=
            com.google.mlkit.nl.translate.Translation
                .getClient(options);

        com.google.mlkit.common.model.DownloadConditions conditions=
            new com.google.mlkit.common.model.DownloadConditions
                .Builder()
                .build();

        translatorStatus=
            "Preparing "+
            translatorDisplayNameForCode(sourceCode)+
            " → "+
            translatorDisplayNameForCode(targetCode)+
            "…";
        updateTranslatorPanel();

        translator.downloadModelIfNeeded(
            conditions
        ).addOnSuccessListener(
            unused -> {
                if(request!=translatorRequestId) {
                    translator.close();
                    return;
                }

                translator.translate(
                    sourceText
                ).addOnSuccessListener(
                    translated -> {
                        translator.close();

                        if(request!=translatorRequestId)
                            return;

                        if(
                            translated==null ||
                            translated.trim().isEmpty()
                        ) {
                            translatorStatus=
                                "No translation returned";
                            updateTranslatorPanel();
                            return;
                        }

                        translatorResult=translated;
                        translatorStatus="Translated";
                        updateTranslatorPanel();

                        insertTranslatorTranslation(
                            translated,
                            request
                        );
                    }
                ).addOnFailureListener(
                    error -> {
                        translator.close();

                        if(request!=translatorRequestId)
                            return;

                        startOnlineTranslatorFallback(
                            sourceText,
                            sourceCode,
                            targetCode,
                            request
                        );
                    }
                );
            }
        ).addOnFailureListener(
            error -> {
                translator.close();

                if(request!=translatorRequestId)
                    return;

                startOnlineTranslatorFallback(
                    sourceText,
                    sourceCode,
                    targetCode,
                    request
                );
            }
        );
    }


    void startOnlineTranslatorFallback(
        String sourceText,
        String sourceCode,
        String targetCode,
        int request
    ) {
        translatorStatus=
            sourceCode.equalsIgnoreCase("ceb") ||
            targetCode.equalsIgnoreCase("ceb")
            ? "Translating Cebuano online…"
            : "Trying online translation…";

        updateTranslatorPanel();

        new Thread(
            () -> {
                String result="";
                String status="";
                java.net.HttpURLConnection connection=null;

                try {
                    String encodedText=
                        java.net.URLEncoder.encode(
                            sourceText,
                            "UTF-8"
                        );

                    String encodedSource=
                        java.net.URLEncoder.encode(
                            sourceCode,
                            "UTF-8"
                        );

                    String encodedTarget=
                        java.net.URLEncoder.encode(
                            targetCode,
                            "UTF-8"
                        );

                    java.net.URL url=
                        new java.net.URL(
                            "https://translate.googleapis.com/translate_a/single"+
                            "?client=gtx"+
                            "&dt=t"+
                            "&sl="+encodedSource+
                            "&tl="+encodedTarget+
                            "&q="+encodedText
                        );

                    connection=
                        (java.net.HttpURLConnection)
                        url.openConnection();

                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(10000);
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty(
                        "Accept",
                        "application/json"
                    );
                    connection.setRequestProperty(
                        "User-Agent",
                        "KeyKii-Neo/2.33.3"
                    );

                    int code=
                        connection.getResponseCode();

                    if(code>=200 && code<300) {
                        java.io.BufferedReader reader=
                            new java.io.BufferedReader(
                                new java.io.InputStreamReader(
                                    connection.getInputStream(),
                                    "UTF-8"
                                )
                            );

                        StringBuilder raw=
                            new StringBuilder();

                        String line;
                        while(
                            (line=reader.readLine())!=null
                        ) {
                            raw.append(line);
                        }

                        reader.close();

                        org.json.JSONArray root=
                            new org.json.JSONArray(
                                raw.toString()
                            );

                        org.json.JSONArray segments=
                            root.optJSONArray(0);

                        StringBuilder translated=
                            new StringBuilder();

                        if(segments!=null) {
                            for(
                                int i=0;
                                i<segments.length();
                                i++
                            ) {
                                org.json.JSONArray part=
                                    segments.optJSONArray(i);

                                if(part==null)
                                    continue;

                                String value=
                                    part.optString(0,"");

                                if(value!=null)
                                    translated.append(value);
                            }
                        }

                        result=
                            translated.toString().trim();
                    }

                    if(
                        result==null ||
                        result.isEmpty() ||
                        (
                            !sourceCode.equalsIgnoreCase(
                                targetCode
                            ) &&
                            result.equalsIgnoreCase(
                                sourceText.trim()
                            )
                        )
                    ) {
                        result="";
                        status=
                            "Translation unavailable • try choosing the source language";
                    } else {
                        status="Translated";
                    }

                } catch(Exception e) {
                    result="";
                    status=
                        "Translation failed • check internet";
                } finally {
                    if(connection!=null)
                        connection.disconnect();
                }

                final String finalResult=
                    result==null
                    ? ""
                    : result;

                final String finalStatus=
                    status;

                new android.os.Handler(
                    android.os.Looper.getMainLooper()
                ).post(
                    () -> {
                        if(request!=translatorRequestId)
                            return;

                        translatorResult=
                            finalResult;

                        translatorStatus=
                            finalStatus;

                        updateTranslatorPanel();

                        if(!finalResult.isEmpty()) {
                            insertTranslatorTranslation(
                                finalResult,
                                request
                            );
                        }
                    }
                );
            },
            "KeyKii-Translate-Fallback"
        ).start();
    }


    void insertTranslatorTranslation(
        String translated,
        int request
    ) {
        if(
            request!=translatorRequestId ||
            translated==null ||
            translated.isEmpty()
        ) {
            return;
        }

        InputConnection ic=
            getCurrentInputConnection();

        if(ic!=null) {
            ic.commitText(
                translated,
                1
            );

            translatorStatus="Inserted";
            updateTranslatorPanel();
        }
    }


    void buildGrammarPanel() {

        if(
            grammarSourceText==null ||
            grammarSourceText.isEmpty()
        ) {
            captureSelectedTextForGrammar();
        }

        LinearLayout header=
            new LinearLayout(this);

        header.setGravity(
            Gravity.CENTER_VERTICAL
        );

        TextView back=
            translatorLanguageButton("←");

        back.setOnClickListener(
            v -> {
                syncGrammarSourceFromView();
                page=4;
                buildShell();
            }
        );

        TextView title=
            new TextView(this);

        title.setText("Grammar Fix");
        title.setTextColor(textColor());
        title.setTextSize(16);
        title.setGravity(Gravity.CENTER);

        header.addView(
            back,
            new LinearLayout.LayoutParams(
                dp(48),
                dp(42)
            )
        );

        header.addView(
            title,
            new LinearLayout.LayoutParams(
                0,
                dp(42),
                1f
            )
        );

        body.addView(
            header,
            new LinearLayout.LayoutParams(
                -1,
                dp(46)
            )
        );

        grammarSourceView=
            new EditText(this);

        grammarSourceView.setTextColor(
            textColor()
        );

        grammarSourceView.setHintTextColor(
            textColor()
        );

        grammarSourceView.setHint(
            "Type or select English text to correct"
        );

        grammarSourceView.setTextSize(15);
        grammarSourceView.setGravity(
            Gravity.START |
            Gravity.CENTER_VERTICAL
        );

        grammarSourceView.setPadding(
            dp(14),
            dp(8),
            dp(14),
            dp(8)
        );

        grammarSourceView.setSingleLine(false);
        grammarSourceView.setMaxLines(4);
        grammarSourceView.setCursorVisible(true);
        grammarSourceView.setFocusable(true);
        grammarSourceView.setFocusableInTouchMode(true);
        grammarSourceView.setTextIsSelectable(true);

        grammarSourceView.setInputType(
            android.text.InputType.TYPE_CLASS_TEXT |
            android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE |
            android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        );

        if(android.os.Build.VERSION.SDK_INT>=21)
            grammarSourceView.setShowSoftInputOnFocus(false);

        grammarSourceView.setBackground(
            round(
                keyColor(false),
                18,
                borderColor()
            )
        );

        grammarSourceView.setText(
            grammarSourceText==null
            ? ""
            : grammarSourceText
        );

        grammarCursor=
            Math.max(
                0,
                Math.min(
                    grammarCursor,
                    grammarSourceView.length()
                )
            );

        if(
            grammarSourceView.length()>0 &&
            grammarCursor==0
        ) {
            grammarCursor=
                grammarSourceView.length();
        }

        try {
            grammarSourceView.setSelection(
                grammarCursor
            );
        } catch(Exception ignored) {}

        grammarSourceView.setOnClickListener(
            v -> syncGrammarCursorFromView()
        );

        grammarSourceView.setOnTouchListener(
            (v,event) -> {
                v.post(
                    () -> syncGrammarCursorFromView()
                );
                return false;
            }
        );

        body.addView(
            grammarSourceView,
            new LinearLayout.LayoutParams(
                -1,
                dp(82)
            )
        );

        grammarCorrectedView=
            new TextView(this);

        grammarCorrectedView.setTextColor(
            textColor()
        );

        grammarCorrectedView.setTextSize(13);
        grammarCorrectedView.setGravity(
            Gravity.START |
            Gravity.CENTER_VERTICAL
        );

        grammarCorrectedView.setPadding(
            dp(14),
            dp(8),
            dp(14),
            dp(8)
        );

        grammarCorrectedView.setMaxLines(4);

        grammarCorrectedView.setBackground(
            round(
                keyColor(false),
                18,
                borderColor()
            )
        );

        body.addView(
            grammarCorrectedView,
            new LinearLayout.LayoutParams(
                -1,
                dp(72)
            )
        );

        toolsRow(
            new String[]{
                "✓  Check grammar",
                "↳  Apply correction"
            },
            new Runnable[]{
                () -> {
                    syncGrammarSourceFromView();
                    checkGrammarOnline();
                },
                () -> applyGrammarCorrection()
            }
        );

        grammarStatusView=
            new TextView(this);

        grammarStatusView.setTextColor(
            textColor()
        );

        grammarStatusView.setTextSize(9);
        grammarStatusView.setAlpha(.56f);
        grammarStatusView.setGravity(
            Gravity.CENTER
        );

        body.addView(
            grammarStatusView,
            new LinearLayout.LayoutParams(
                -1,
                dp(24)
            )
        );

        updateGrammarPanel();

        grammarSourceView.post(
            () -> focusGrammarSource()
        );

        buildKeyboard();
    }


    void captureSelectedTextForGrammar() {
        InputConnection ic=
            getCurrentInputConnection();

        if(ic==null)
            return;

        try {
            CharSequence selected=
                ic.getSelectedText(0);

            if(
                selected!=null &&
                selected.length()>0
            ) {
                grammarSourceText=
                    selected.toString();

                if(grammarSourceText.length()>1200)
                    grammarSourceText=
                        grammarSourceText.substring(
                            0,
                            1200
                        );

                grammarCursor=
                    grammarSourceText.length();
            }
        } catch(Exception ignored) {}
    }


    void syncGrammarCursorFromView() {
        if(grammarSourceView==null)
            return;

        try {
            grammarCursor=
                Math.max(
                    0,
                    grammarSourceView.getSelectionStart()
                );
        } catch(Exception ignored) {}
    }


    void syncGrammarSourceFromView() {
        if(grammarSourceView==null)
            return;

        grammarSourceText=
            grammarSourceView.getText()==null
            ? ""
            : grammarSourceView
                .getText()
                .toString();

        syncGrammarCursorFromView();
    }


    void focusGrammarSource() {
        if(grammarSourceView==null)
            return;

        grammarSourceView.requestFocus();
        grammarSourceView.setCursorVisible(true);

        grammarCursor=
            Math.max(
                0,
                Math.min(
                    grammarCursor,
                    grammarSourceView.length()
                )
            );

        try {
            grammarSourceView.setSelection(
                grammarCursor
            );
        } catch(Exception ignored) {}
    }


    void updateGrammarPanel() {
        if(grammarSourceView!=null) {
            String wanted=
                grammarSourceText==null
                ? ""
                : grammarSourceText;

            String current=
                grammarSourceView.getText()==null
                ? ""
                : grammarSourceView
                    .getText()
                    .toString();

            if(!current.equals(wanted))
                grammarSourceView.setText(wanted);

            grammarCursor=
                Math.max(
                    0,
                    Math.min(
                        grammarCursor,
                        grammarSourceView.length()
                    )
                );

            try {
                grammarSourceView.setSelection(
                    grammarCursor
                );
            } catch(Exception ignored) {}
        }

        if(grammarCorrectedView!=null) {
            grammarCorrectedView.setText(
                grammarCorrectedText==null ||
                grammarCorrectedText.isEmpty()
                ? "Corrected text will appear here"
                : grammarCorrectedText
            );

            grammarCorrectedView.setAlpha(
                grammarCorrectedText==null ||
                grammarCorrectedText.isEmpty()
                ? .48f
                : 1f
            );
        }

        if(grammarStatusView!=null) {
            String status=
                grammarStatus==null
                ? ""
                : grammarStatus;

            if(status.isEmpty())
                status=
                    "English grammar check runs only when you tap Check grammar";

            grammarStatusView.setText(
                status
            );
        }
    }


    boolean handleGrammarSpacebarTouch(
        View v,
        MotionEvent e
    ) {
        int action=
            e.getActionMasked();

        if(action==MotionEvent.ACTION_DOWN) {
            syncGrammarSourceFromView();
            spaceDownX=e.getX();
            spaceCursorDragging=false;
            spaceStartSelection=
                grammarCursor;

            v.animate()
             .scaleX(1.02f)
             .scaleY(1.02f)
             .setDuration(30)
             .start();

            return true;
        }

        if(action==MotionEvent.ACTION_MOVE) {
            float dx=
                e.getX()-spaceDownX;

            if(Math.abs(dx)>=dp(9)) {
                spaceCursorDragging=true;

                int steps=
                    Math.round(
                        dx/(float)Math.max(
                            dp(14),
                            1
                        )
                    );

                grammarCursor=
                    Math.max(
                        0,
                        Math.min(
                            grammarSourceText.length(),
                            spaceStartSelection+steps
                        )
                    );

                focusGrammarSource();
            }

            return true;
        }

        if(
            action==MotionEvent.ACTION_UP ||
            action==MotionEvent.ACTION_CANCEL
        ) {
            v.animate()
             .scaleX(1f)
             .scaleY(1f)
             .setDuration(45)
             .start();

            if(
                action==MotionEvent.ACTION_UP &&
                !spaceCursorDragging
            ) {
                press("SPACE");
            }

            spaceCursorDragging=false;
            return true;
        }

        return true;
    }


    boolean handleGrammarKey(
        String action
    ) {
        if(page!=9)
            return false;

        if(action==null)
            return true;

        if(
            action.equals("123") ||
            action.equals("ABC") ||
            action.equals("SYM1") ||
            action.equals("SYM2") ||
            action.equals("SHIFT")
        ) {
            syncGrammarSourceFromView();
            return false;
        }

        if(action.equals("KEYS")) {
            syncGrammarSourceFromView();
            page=0;
            buildShell();
            return true;
        }

        if(action.equals("BACK")) {
            grammarBackspace();
            return true;
        }

        if(action.equals("ENTER")) {
            syncGrammarSourceFromView();
            checkGrammarOnline();
            return true;
        }

        String value=
            action.equals("SPACE")
            ? " "
            : action;

        if(
            value.length()>2 ||
            action.equals("EMOJI") ||
            action.equals("CLIPBOARD") ||
            action.equals("SHORTCUTS")
        ) {
            return true;
        }

        if(
            shift &&
            !symbols &&
            value.length()==1
        ) {
            value=
                value.toUpperCase(
                    java.util.Locale.getDefault()
                );

            if(!capsLock)
                shift=false;
        }

        syncGrammarSourceFromView();

        if(grammarSourceText==null)
            grammarSourceText="";

        grammarCursor=
            Math.max(
                0,
                Math.min(
                    grammarCursor,
                    grammarSourceText.length()
                )
            );

        if(grammarSourceText.length()<1200) {
            grammarSourceText=
                grammarSourceText.substring(
                    0,
                    grammarCursor
                )+
                value+
                grammarSourceText.substring(
                    grammarCursor
                );

            grammarCursor+=value.length();
            grammarCorrectedText="";
            grammarStatus="";
            grammarRequestId++;

            updateGrammarPanel();
            focusGrammarSource();
        }

        return true;
    }


    void grammarBackspace() {
        syncGrammarSourceFromView();

        if(
            grammarSourceText!=null &&
            !grammarSourceText.isEmpty() &&
            grammarCursor>0
        ) {
            int start=
                grammarSourceText.offsetByCodePoints(
                    grammarCursor,
                    -1
                );

            grammarSourceText=
                grammarSourceText.substring(
                    0,
                    start
                )+
                grammarSourceText.substring(
                    grammarCursor
                );

            grammarCursor=start;
            grammarCorrectedText="";
            grammarStatus="";
            grammarRequestId++;

            updateGrammarPanel();
            focusGrammarSource();
            return;
        }

        if(
            grammarSourceText==null ||
            grammarSourceText.isEmpty()
        ) {
            InputConnection ic=
                getCurrentInputConnection();

            if(ic!=null)
                deleteOneBeforeCursor(ic);
        }
    }


    void checkGrammarOnline() {
        syncGrammarSourceFromView();

        if(
            grammarSourceText==null ||
            grammarSourceText.trim().isEmpty()
        ) {
            grammarStatus=
                "Type or select text first";

            updateGrammarPanel();
            return;
        }

        final String sourceText=
            grammarSourceText.length()>1200
            ? grammarSourceText.substring(
                0,
                1200
            )
            : grammarSourceText;

        final int request=
            ++grammarRequestId;

        grammarCorrectedText="";
        grammarStatus=
            "Checking grammar…";
        updateGrammarPanel();

        new Thread(
            () -> {
                java.net.HttpURLConnection connection=null;

                String corrected="";
                String status="";

                try {
                    String data=
                        "language=en-US"+
                        "&text="+
                        java.net.URLEncoder.encode(
                            sourceText,
                            "UTF-8"
                        );

                    byte[] payload=
                        data.getBytes(
                            "UTF-8"
                        );

                    java.net.URL url=
                        new java.net.URL(
                            "https://api.languagetool.org/v2/check"
                        );

                    connection=
                        (java.net.HttpURLConnection)
                        url.openConnection();

                    connection.setConnectTimeout(
                        10000
                    );

                    connection.setReadTimeout(
                        15000
                    );

                    connection.setRequestMethod(
                        "POST"
                    );

                    connection.setDoOutput(true);

                    connection.setRequestProperty(
                        "Content-Type",
                        "application/x-www-form-urlencoded; charset=UTF-8"
                    );

                    connection.setRequestProperty(
                        "Accept",
                        "application/json"
                    );

                    connection.setRequestProperty(
                        "User-Agent",
                        "KeyKii-Neo/2.34.0"
                    );

                    connection.setFixedLengthStreamingMode(
                        payload.length
                    );

                    java.io.OutputStream output=
                        connection.getOutputStream();

                    output.write(payload);
                    output.flush();
                    output.close();

                    int responseCode=
                        connection.getResponseCode();

                    if(
                        responseCode>=200 &&
                        responseCode<300
                    ) {
                        java.io.BufferedReader reader=
                            new java.io.BufferedReader(
                                new java.io.InputStreamReader(
                                    connection.getInputStream(),
                                    "UTF-8"
                                )
                            );

                        StringBuilder raw=
                            new StringBuilder();

                        String line;

                        while(
                            (line=reader.readLine())!=null
                        ) {
                            raw.append(line);
                        }

                        reader.close();

                        org.json.JSONObject root=
                            new org.json.JSONObject(
                                raw.toString()
                            );

                        org.json.JSONArray matches=
                            root.optJSONArray(
                                "matches"
                            );

                        java.util.ArrayList<org.json.JSONObject> usable=
                            new java.util.ArrayList<>();

                        if(matches!=null) {
                            for(
                                int i=0;
                                i<matches.length();
                                i++
                            ) {
                                org.json.JSONObject match=
                                    matches.optJSONObject(i);

                                if(match==null)
                                    continue;

                                org.json.JSONArray replacements=
                                    match.optJSONArray(
                                        "replacements"
                                    );

                                if(
                                    replacements==null ||
                                    replacements.length()==0
                                ) {
                                    continue;
                                }

                                org.json.JSONObject first=
                                    replacements.optJSONObject(0);

                                if(
                                    first==null ||
                                    !first.has("value")
                                ) {
                                    continue;
                                }

                                usable.add(match);
                            }
                        }

                        java.util.Collections.sort(
                            usable,
                            (left,right) ->
                                Integer.compare(
                                    right.optInt(
                                        "offset",
                                        0
                                    ),
                                    left.optInt(
                                        "offset",
                                        0
                                    )
                                )
                        );

                        StringBuilder fixed=
                            new StringBuilder(
                                sourceText
                            );

                        int applied=0;

                        for(
                            org.json.JSONObject match:
                            usable
                        ) {
                            int offset=
                                match.optInt(
                                    "offset",
                                    -1
                                );

                            int length=
                                match.optInt(
                                    "length",
                                    0
                                );

                            org.json.JSONArray replacements=
                                match.optJSONArray(
                                    "replacements"
                                );

                            org.json.JSONObject first=
                                replacements==null
                                ? null
                                : replacements.optJSONObject(0);

                            String replacement=
                                first==null
                                ? ""
                                : first.optString(
                                    "value",
                                    ""
                                );

                            if(
                                offset<0 ||
                                length<0 ||
                                offset>fixed.length() ||
                                offset+length>
                                    fixed.length()
                            ) {
                                continue;
                            }

                            fixed.replace(
                                offset,
                                offset+length,
                                replacement
                            );

                            applied++;
                        }

                        corrected=
                            fixed.toString();

                        status=
                            applied==0
                            ? "No grammar issues found"
                            : applied+
                              (
                                applied==1
                                ? " correction found"
                                : " corrections found"
                              );

                    } else {
                        status=
                            "Grammar service unavailable";
                    }

                } catch(Exception e) {
                    status=
                        "Grammar check failed • check internet";
                } finally {
                    if(connection!=null)
                        connection.disconnect();
                }

                final String finalCorrected=
                    corrected==null
                    ? ""
                    : corrected;

                final String finalStatus=
                    status;

                new android.os.Handler(
                    android.os.Looper.getMainLooper()
                ).post(
                    () -> {
                        if(request!=grammarRequestId)
                            return;

                        grammarCorrectedText=
                            finalCorrected;

                        grammarStatus=
                            finalStatus;

                        updateGrammarPanel();
                    }
                );
            },
            "KeyKii-Grammar"
        ).start();
    }


    void applyGrammarCorrection() {
        if(
            grammarCorrectedText==null ||
            grammarCorrectedText.isEmpty()
        ) {
            voiceToast(
                "Check grammar first"
            );
            return;
        }

        InputConnection ic=
            getCurrentInputConnection();

        if(ic==null)
            return;

        ic.commitText(
            grammarCorrectedText,
            1
        );

        grammarStatus=
            "Applied";

        page=0;
        buildShell();
    }


    void buildTextCasePanel() {

        TextView heading=
            title("Text case");

        heading.setTextSize(12);
        heading.setAlpha(.62f);

        body.addView(
            heading,
            new LinearLayout.LayoutParams(
                -1,
                dp(24)
            )
        );

        TextView info=
            new TextView(this);

        info.setText(
            "Select text in the app, then choose how you want to change it."
        );

        info.setTextColor(
            textColor()
        );

        info.setTextSize(12);
        info.setAlpha(.70f);
        info.setGravity(
            Gravity.CENTER
        );

        info.setPadding(
            dp(12),
            dp(6),
            dp(12),
            dp(8)
        );

        body.addView(
            info,
            new LinearLayout.LayoutParams(
                -1,
                dp(54)
            )
        );

        toolsRow(
            new String[]{
                "ABC  UPPERCASE",
                "abc  lowercase"
            },
            new Runnable[]{
                () -> applySelectedTextCase("upper"),
                () -> applySelectedTextCase("lower")
            }
        );

        toolsRow(
            new String[]{
                "Ab  Title Case",
                "Aa  Sentence case"
            },
            new Runnable[]{
                () -> applySelectedTextCase("title"),
                () -> applySelectedTextCase("sentence")
            }
        );

        TextView note=
            new TextView(this);

        note.setText(
            "The selected text is replaced directly. Nothing is saved."
        );

        note.setTextColor(
            textColor()
        );

        note.setTextSize(11);
        note.setAlpha(.52f);
        note.setGravity(
            Gravity.CENTER
        );

        body.addView(
            note,
            new LinearLayout.LayoutParams(
                -1,
                dp(46)
            )
        );
    }


    void applySelectedTextCase(
        String mode
    ) {
        InputConnection ic=
            getCurrentInputConnection();

        if(ic==null) {
            voiceToast(
                "No text field available"
            );
            return;
        }

        CharSequence selected=null;

        try {
            selected=
                ic.getSelectedText(0);
        } catch(Exception ignored) {
        }

        if(
            selected==null ||
            selected.length()==0
        ) {
            voiceToast(
                "Select some text first"
            );
            return;
        }

        String source=
            selected.toString();

        String converted;

        if("upper".equals(mode)) {
            converted=
                source.toUpperCase(
                    java.util.Locale.getDefault()
                );

        } else if("lower".equals(mode)) {
            converted=
                source.toLowerCase(
                    java.util.Locale.getDefault()
                );

        } else if("title".equals(mode)) {
            converted=
                titleCaseText(source);

        } else {
            converted=
                sentenceCaseText(source);
        }

        ic.commitText(
            converted,
            1
        );

        voiceToast(
            "Text case changed"
        );

        page=0;
        buildShell();
    }


    String titleCaseText(
        String text
    ) {
        if(text==null || text.isEmpty())
            return "";

        String lower=
            text.toLowerCase(
                java.util.Locale.getDefault()
            );

        StringBuilder out=
            new StringBuilder(
                lower.length()
            );

        boolean newWord=true;

        for(int i=0;i<lower.length();i++) {
            char ch=
                lower.charAt(i);

            if(
                Character.isLetter(ch) &&
                newWord
            ) {
                out.append(
                    Character.toUpperCase(ch)
                );

                newWord=false;

            } else {
                out.append(ch);

                if(Character.isLetterOrDigit(ch))
                    newWord=false;
            }

            if(
                Character.isWhitespace(ch) ||
                ch=='-' ||
                ch=='/' ||
                ch=='\\'
            ) {
                newWord=true;
            }
        }

        return out.toString();
    }


    String sentenceCaseText(
        String text
    ) {
        if(text==null || text.isEmpty())
            return "";

        String lower=
            text.toLowerCase(
                java.util.Locale.getDefault()
            );

        StringBuilder out=
            new StringBuilder(
                lower.length()
            );

        boolean capitalizeNext=true;

        for(int i=0;i<lower.length();i++) {
            char ch=
                lower.charAt(i);

            if(
                capitalizeNext &&
                Character.isLetter(ch)
            ) {
                out.append(
                    Character.toUpperCase(ch)
                );

                capitalizeNext=false;

            } else {
                out.append(ch);
            }

            if(
                ch=='.' ||
                ch=='!' ||
                ch=='?' ||
                ch=='\n'
            ) {
                capitalizeNext=true;
            } else if(
                !Character.isWhitespace(ch)
            ) {
                if(!capitalizeNext)
                    capitalizeNext=false;
            }
        }

        return out.toString();
    }


    void buildSmartTextPanel() {

        TextView heading=
            title("Smart text");

        heading.setTextSize(12);
        heading.setAlpha(.62f);

        body.addView(
            heading,
            new LinearLayout.LayoutParams(
                -1,
                dp(24)
            )
        );

        TextView info=
            new TextView(this);

        info.setText(
            "Select text in the app, then use an offline tool. Nothing is uploaded."
        );
        info.setTextColor(textColor());
        info.setTextSize(12);
        info.setAlpha(.70f);
        info.setGravity(Gravity.CENTER);
        info.setPadding(
            dp(12),
            dp(4),
            dp(12),
            dp(6)
        );

        body.addView(
            info,
            new LinearLayout.LayoutParams(
                -1,
                dp(48)
            )
        );

        toolsRow(
            new String[]{
                "✨  Clean spacing",
                "↔  Join lines"
            },
            new Runnable[]{
                () -> applySelectedSmartText("clean"),
                () -> applySelectedSmartText("join")
            }
        );

        toolsRow(
            new String[]{
                "•  Bullet list",
                "1.  Number list"
            },
            new Runnable[]{
                () -> applySelectedSmartText("bullets"),
                () -> applySelectedSmartText("numbers")
            }
        );

        toolsRow(
            new String[]{
                "A-Z  Sort lines",
                "≠  Remove duplicates"
            },
            new Runnable[]{
                () -> applySelectedSmartText("sort"),
                () -> applySelectedSmartText("dedupe")
            }
        );

        toolsRow(
            new String[]{
                "☐  Checklist",
                "123  Text stats"
            },
            new Runnable[]{
                () -> applySelectedSmartText("checklist"),
                () -> applySelectedSmartText("stats")
            }
        );
    }


    void applySelectedSmartText(
        String mode
    ) {
        InputConnection ic=
            getCurrentInputConnection();

        if(ic==null) {
            voiceToast("No text field available");
            return;
        }

        CharSequence selected=null;

        try {
            selected=ic.getSelectedText(0);
        } catch(Exception ignored) {
        }

        if(
            selected==null ||
            selected.length()==0
        ) {
            voiceToast("Select some text first");
            return;
        }

        String source=
            selected.toString();

        if("stats".equals(mode)) {
            String trimmed=source.trim();

            int words=
                trimmed.isEmpty()
                ? 0
                : trimmed.split("\\s+").length;

            int lines=
                source.isEmpty()
                ? 0
                : source.split("\\r?\\n",-1).length;

            voiceToast(
                words+" words • "+
                source.length()+" characters • "+
                lines+" lines"
            );
            return;
        }

        String converted=
            smartTextTransform(
                source,
                mode
            );

        if(converted.equals(source)) {
            voiceToast("Text already looks good");
            return;
        }

        ic.commitText(
            converted,
            1
        );

        voiceToast("Smart text applied");

        page=0;
        buildShell();
    }


    String smartTextTransform(
        String source,
        String mode
    ) {
        if(source==null)
            return "";

        String normalized=
            source
                .replace("\r\n","\n")
                .replace('\r','\n');

        if("join".equals(mode)) {
            return normalized
                .replaceAll("\\s*\\n+\\s*"," ")
                .replaceAll("[\\t ]+"," ")
                .trim();
        }

        String[] rawLines=
            normalized.split("\\n",-1);

        if("clean".equals(mode)) {
            StringBuilder out=
                new StringBuilder();

            boolean previousBlank=false;

            for(String raw:rawLines) {
                String line=
                    raw.trim()
                       .replaceAll("[\\t ]+"," ");

                boolean blank=line.isEmpty();

                if(blank && previousBlank)
                    continue;

                if(out.length()>0)
                    out.append('\n');

                out.append(line);
                previousBlank=blank;
            }

            return out.toString().trim();
        }

        java.util.ArrayList<String> lines=
            new java.util.ArrayList<>();

        for(String raw:rawLines) {
            String line=raw.trim();

            if(!line.isEmpty())
                lines.add(line);
        }

        if("sort".equals(mode)) {
            java.util.Collections.sort(
                lines,
                String.CASE_INSENSITIVE_ORDER
            );

        } else if("dedupe".equals(mode)) {
            java.util.LinkedHashSet<String> unique=
                new java.util.LinkedHashSet<>(lines);

            lines.clear();
            lines.addAll(unique);

        } else if(
            "bullets".equals(mode) ||
            "numbers".equals(mode) ||
            "checklist".equals(mode)
        ) {
            java.util.ArrayList<String> formatted=
                new java.util.ArrayList<>();

            for(int i=0;i<lines.size();i++) {
                String line=
                    stripSmartListPrefix(
                        lines.get(i)
                    );

                if("bullets".equals(mode))
                    formatted.add("• "+line);

                else if("numbers".equals(mode))
                    formatted.add((i+1)+". "+line);

                else
                    formatted.add("☐ "+line);
            }

            lines=formatted;
        }

        StringBuilder out=
            new StringBuilder();

        for(int i=0;i<lines.size();i++) {
            if(i>0)
                out.append('\n');

            out.append(lines.get(i));
        }

        return out.toString();
    }


    String stripSmartListPrefix(
        String line
    ) {
        if(line==null)
            return "";

        return line.replaceFirst(
            "^\\s*(?:(?:[•*\\-☐☑✓])|(?:\\d+[.)]))\\s+",
            ""
        );
    }


    void buildCalculatorPanel() {

        TextView heading=
            title("Calculator");

        heading.setTextSize(12);
        heading.setAlpha(.62f);

        body.addView(
            heading,
            new LinearLayout.LayoutParams(
                -1,
                dp(24)
            )
        );

        LinearLayout display=
            new LinearLayout(this);

        display.setOrientation(
            LinearLayout.VERTICAL
        );

        display.setGravity(
            Gravity.CENTER_VERTICAL
        );

        display.setPadding(
            dp(16),
            dp(8),
            dp(16),
            dp(8)
        );

        display.setBackground(
            round(
                keyColor(false),
                20,
                borderColor()
            )
        );

        calculatorExpressionView=
            new TextView(this);

        calculatorExpressionView.setTextColor(
            textColor()
        );

        calculatorExpressionView.setTextSize(22);
        calculatorExpressionView.setGravity(
            Gravity.END |
            Gravity.CENTER_VERTICAL
        );

        calculatorExpressionView.setSingleLine(
            true
        );

        calculatorResultView=
            new TextView(this);

        calculatorResultView.setTextColor(
            textColor()
        );

        calculatorResultView.setAlpha(.68f);
        calculatorResultView.setTextSize(15);
        calculatorResultView.setGravity(
            Gravity.END |
            Gravity.CENTER_VERTICAL
        );

        calculatorResultView.setSingleLine(
            true
        );

        display.addView(
            calculatorExpressionView,
            new LinearLayout.LayoutParams(
                -1,
                dp(35)
            )
        );

        display.addView(
            calculatorResultView,
            new LinearLayout.LayoutParams(
                -1,
                dp(27)
            )
        );

        LinearLayout.LayoutParams displayParams=
            new LinearLayout.LayoutParams(
                -1,
                dp(66)
            );

        displayParams.setMargins(
            dp(3),
            dp(3),
            dp(3),
            dp(5)
        );

        body.addView(
            display,
            displayParams
        );

        calculatorRow(
            new String[]{"C","⌫","(",")"}
        );

        calculatorRow(
            new String[]{"7","8","9","÷"}
        );

        calculatorRow(
            new String[]{"4","5","6","×"}
        );

        calculatorRow(
            new String[]{"1","2","3","-"}
        );

        calculatorRow(
            new String[]{".","0","=","+"}
        );

        TextView insert=
            new TextView(this);

        insert.setText(
            "Insert result"
        );

        insert.setTextColor(
            textColor()
        );

        insert.setTextSize(14);
        insert.setGravity(
            Gravity.CENTER
        );

        insert.setBackground(
            round(
                accentFillColor(),
                18,
                accentColor()
            )
        );

        insert.setOnClickListener(
            v -> calculatorInsertResult()
        );

        LinearLayout.LayoutParams insertParams=
            new LinearLayout.LayoutParams(
                -1,
                dp(48)
            );

        insertParams.setMargins(
            dp(3),
            dp(6),
            dp(3),
            dp(2)
        );

        body.addView(
            insert,
            insertParams
        );

        updateCalculatorDisplay();
    }


    void calculatorRow(
        String[] labels
    ) {
        LinearLayout row=
            new LinearLayout(this);

        row.setGravity(
            Gravity.CENTER
        );

        for(String label:labels) {
            TextView button=
                new TextView(this);

            button.setText(label);
            button.setTextColor(
                textColor()
            );

            button.setTextSize(18);
            button.setGravity(
                Gravity.CENTER
            );

            boolean operator=
                label.equals("+") ||
                label.equals("-") ||
                label.equals("×") ||
                label.equals("÷") ||
                label.equals("=");

            button.setBackground(
                round(
                    operator
                    ? accentFillColor()
                    : keyColor(false),
                    18,
                    operator
                    ? accentColor()
                    : borderColor()
                )
            );

            button.setOnClickListener(
                v -> calculatorPress(label)
            );

            LinearLayout.LayoutParams p=
                new LinearLayout.LayoutParams(
                    0,
                    dp(46),
                    1f
                );

            p.setMargins(
                dp(3),
                dp(3),
                dp(3),
                dp(3)
            );

            row.addView(
                button,
                p
            );
        }

        body.addView(
            row,
            new LinearLayout.LayoutParams(
                -1,
                -2
            )
        );
    }


    void calculatorPress(
        String key
    ) {
        if(key==null)
            return;

        if(key.equals("C")) {
            calculatorExpression="";
            calculatorResult="";
            updateCalculatorDisplay();
            return;
        }

        if(key.equals("⌫")) {
            if(
                calculatorExpression!=null &&
                !calculatorExpression.isEmpty()
            ) {
                calculatorExpression=
                    calculatorExpression.substring(
                        0,
                        calculatorExpression.length()-1
                    );
            }

            calculatorResult="";
            updateCalculatorDisplay();
            return;
        }

        if(key.equals("=")) {
            calculateCurrentExpression();
            updateCalculatorDisplay();
            return;
        }

        if(calculatorExpression==null)
            calculatorExpression="";

        if(calculatorExpression.length()>=48)
            return;

        calculatorExpression+=key;
        calculatorResult="";
        updateCalculatorDisplay();
    }


    void updateCalculatorDisplay() {
        if(calculatorExpressionView!=null) {
            calculatorExpressionView.setText(
                calculatorExpression==null ||
                calculatorExpression.isEmpty()
                ? "0"
                : calculatorExpression
            );
        }

        if(calculatorResultView!=null) {
            calculatorResultView.setText(
                calculatorResult==null ||
                calculatorResult.isEmpty()
                ? " "
                : "= " + calculatorResult
            );
        }
    }


    void calculateCurrentExpression() {
        if(
            calculatorExpression==null ||
            calculatorExpression.trim().isEmpty()
        ) {
            calculatorResult="";
            return;
        }

        try {
            double value=
                evaluateCalculatorExpression(
                    calculatorExpression
                );

            if(
                Double.isNaN(value) ||
                Double.isInfinite(value)
            ) {
                calculatorResult="Error";
                return;
            }

            java.math.BigDecimal number=
                new java.math.BigDecimal(
                    Double.toString(value)
                ).stripTrailingZeros();

            calculatorResult=
                number.toPlainString();

        } catch(Exception e) {
            calculatorResult="Error";
        }
    }


    double evaluateCalculatorExpression(
        String expression
    ) {
        final String source=
            expression
                .replace("×","*")
                .replace("÷","/")
                .replace("−","-")
                .replace(" ","");

        class Parser {
            int pos=-1;
            int ch;

            void nextChar() {
                ch=
                    ++pos<source.length()
                    ? source.charAt(pos)
                    : -1;
            }

            boolean eat(int value) {
                while(ch==' ')
                    nextChar();

                if(ch==value) {
                    nextChar();
                    return true;
                }

                return false;
            }

            double parse() {
                nextChar();

                double x=
                    parseExpression();

                if(pos<source.length())
                    throw new RuntimeException(
                        "Unexpected"
                    );

                return x;
            }

            double parseExpression() {
                double x=
                    parseTerm();

                while(true) {
                    if(eat('+'))
                        x+=parseTerm();

                    else if(eat('-'))
                        x-=parseTerm();

                    else
                        return x;
                }
            }

            double parseTerm() {
                double x=
                    parseFactor();

                while(true) {
                    if(eat('*'))
                        x*=parseFactor();

                    else if(eat('/'))
                        x/=parseFactor();

                    else
                        return x;
                }
            }

            double parseFactor() {
                if(eat('+'))
                    return parseFactor();

                if(eat('-'))
                    return -parseFactor();

                double x;
                int start=pos;

                if(eat('(')) {
                    x=parseExpression();

                    if(!eat(')'))
                        throw new RuntimeException(
                            "Missing )"
                        );

                } else {
                    while(
                        (ch>='0' && ch<='9') ||
                        ch=='.'
                    ) {
                        nextChar();
                    }

                    if(start==pos)
                        throw new RuntimeException(
                            "Number expected"
                        );

                    x=Double.parseDouble(
                        source.substring(
                            start,
                            pos
                        )
                    );
                }

                return x;
            }
        }

        return new Parser().parse();
    }


    void calculatorInsertResult() {
        if(
            calculatorResult==null ||
            calculatorResult.isEmpty() ||
            calculatorResult.equals("Error")
        ) {
            calculateCurrentExpression();
        }

        if(
            calculatorResult==null ||
            calculatorResult.isEmpty() ||
            calculatorResult.equals("Error")
        ) {
            updateCalculatorDisplay();
            return;
        }

        InputConnection ic=
            getCurrentInputConnection();

        if(ic!=null) {
            ic.commitText(
                calculatorResult,
                1
            );
        }
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
                () -> resizeKeyboardBy(0,-8),
                () -> resizeKeyboardBy(0,8)
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
                    floatGap=52;

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
                24,
                Math.min(
                    80,
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

        if("es".equals(activeKeyboardLanguage)) {
            if(s.equals("a")) return "á|à|ä|@";
            if(s.equals("e")) return "é|è|ë|3";
            if(s.equals("i")) return "í|ì|ï|8";
            if(s.equals("o")) return "ó|ò|ö|9";
            if(s.equals("u")) return "ú|ù|ü|7";
            if(s.equals("n")) return "ñ|!";
        }

        if("fr".equals(activeKeyboardLanguage)) {
            if(s.equals("a")) return "à|â|ä|æ|@";
            if(s.equals("c")) return "ç|'";
            if(s.equals("e")) return "é|è|ê|ë|3";
            if(s.equals("i")) return "î|ï|8";
            if(s.equals("o")) return "ô|ö|œ|9";
            if(s.equals("u")) return "ù|û|ü|7";
        }

        if("de".equals(activeKeyboardLanguage)) {
            if(s.equals("a")) return "ä|@";
            if(s.equals("o")) return "ö|9";
            if(s.equals("u")) return "ü|7";
            if(s.equals("s")) return "ß|#";
        }

        if("tr".equals(activeKeyboardLanguage)) {
            if(s.equals("c")) return "ç|'";
            if(s.equals("g")) return "ğ|&";
            if(s.equals("i")) return "ı|İ|8";
            if(s.equals("o")) return "ö|9";
            if(s.equals("s")) return "ş|#";
            if(s.equals("u")) return "ü|7";
        }

        if("pt".equals(activeKeyboardLanguage)) {
            if(s.equals("a")) return "á|à|â|ã|@";
            if(s.equals("c")) return "ç|'";
            if(s.equals("e")) return "é|ê|3";
            if(s.equals("i")) return "í|8";
            if(s.equals("o")) return "ó|ô|õ|9";
            if(s.equals("u")) return "ú|ü|7";
        }

        if("it".equals(activeKeyboardLanguage)) {
            if(s.equals("a")) return "à|á|@";
            if(s.equals("e")) return "è|é|3";
            if(s.equals("i")) return "ì|í|8";
            if(s.equals("o")) return "ò|ó|9";
            if(s.equals("u")) return "ù|ú|7";
        }

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

        repeatBackspaceHandler.removeCallbacks(
            repeatBackspaceRunnable
        );
        backspaceRepeating=false;
        backspaceSwipeActive=false;
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

        glideDecodeSession++;
        lastGlideEndTime=0L;
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

        // A long press and a glide both start from the same letter touch.
        // Once the chooser opens, cancel glide/backspace state completely
        // so releasing on an alternate character cannot leave KeyKii in a
        // stuck gesture that affects later typing.
        glideTracking=false;
        glideActive=false;
        glideGestureLetters.setLength(0);
        lastGlideEndTime=0L;
        hideGlideTrail();
        resetGlideKeyVisuals();

        repeatBackspaceHandler.removeCallbacks(
            repeatBackspaceRunnable
        );
        backspaceRepeating=false;
        backspaceSwipeActive=false;
        suppressBackspaceClick=false;

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

        if(handleTranslatorKey(action))
            return;

        if(handleGrammarKey(action))
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

            case "LANG":
                cycleKeyboardLanguage();
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
                    ? action.toUpperCase(
                        keyboardLocale()
                      )
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


    int themeKeyStyle() {
        return getSharedPreferences(
            "keykii_prefs",
            MODE_PRIVATE
        ).getInt(
            "theme_key_style",
            0
        );
    }


    int blendThemeColor(
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
                Color.red(a)*ap +
                Color.red(b)*p
            )/100,
            (
                Color.green(a)*ap +
                Color.green(b)*p
            )/100,
            (
                Color.blue(a)*ap +
                Color.blue(b)*p
            )/100
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

        int decorStyle=
            p.getInt(
                "theme_decor_style",
                0
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

        if(decorStyle>0) {
            android.graphics.drawable.LayerDrawable layers=
                new android.graphics.drawable.LayerDrawable(
                    new android.graphics.drawable.Drawable[]{
                        bg,
                        new KeyKiiThemeDecorDrawable(
                            decorStyle,
                            accentColor(),
                            p.getInt(
                                "keykii_style_pack",
                                -1
                            )
                        )
                    }
                );

            panel.setBackground(layers);
        } else {
            panel.setBackground(bg);
        }
    }


    class KeyKiiThemeDecorDrawable
        extends android.graphics.drawable.Drawable {

        final int style;
        final int accent;
        final int pack;
        final android.graphics.Paint paint=
            new android.graphics.Paint(
                android.graphics.Paint.ANTI_ALIAS_FLAG
            );

        KeyKiiThemeDecorDrawable(
            int style,
            int accent,
            int pack
        ) {
            this.style=style;
            this.accent=accent;
            this.pack=pack;
        }


        @Override
        public void draw(
            android.graphics.Canvas canvas
        ) {
            android.graphics.Rect b=getBounds();

            float w=b.width();
            float h=b.height();

            if(w<=0 || h<=0)
                return;

            paint.setStyle(
                android.graphics.Paint.Style.FILL
            );

            int softAccent=
                Color.argb(
                    44,
                    Color.red(accent),
                    Color.green(accent),
                    Color.blue(accent)
                );

            int softWhite=
                Color.argb(
                    theme==1 ? 80 : 42,
                    255,255,255
                );

            if(
                style>=20 &&
                style<=39
            ) {
                drawAestheticPackComposition(
                    canvas,
                    w,
                    h
                );
                return;
            }

            switch(style) {
                case 1:
                    paint.setColor(softAccent);
                    drawHeart(canvas,w*.11f,h*.20f,dp(10),paint);
                    drawHeart(canvas,w*.87f,h*.24f,dp(8),paint);
                    drawHeart(canvas,w*.76f,h*.78f,dp(12),paint);
                    drawHeart(canvas,w*.18f,h*.82f,dp(7),paint);
                    break;

                case 2:
                    paint.setColor(softWhite);
                    drawSpark(canvas,w*.13f,h*.22f,dp(8),paint);
                    drawSpark(canvas,w*.83f,h*.18f,dp(11),paint);
                    drawSpark(canvas,w*.72f,h*.78f,dp(7),paint);
                    drawSpark(canvas,w*.23f,h*.74f,dp(5),paint);
                    break;

                case 3:
                    paint.setStyle(
                        android.graphics.Paint.Style.STROKE
                    );
                    paint.setStrokeWidth(dp(2));
                    paint.setColor(softWhite);
                    canvas.drawCircle(w*.10f,h*.23f,dp(13),paint);
                    canvas.drawCircle(w*.84f,h*.20f,dp(19),paint);
                    canvas.drawCircle(w*.70f,h*.78f,dp(15),paint);
                    canvas.drawCircle(w*.25f,h*.83f,dp(9),paint);
                    paint.setStyle(
                        android.graphics.Paint.Style.FILL
                    );
                    break;

                case 4:
                    paint.setColor(softAccent);
                    paint.setStrokeWidth(dp(2));
                    for(int i=0;i<6;i++) {
                        float y=h*(.10f+i*.17f);
                        canvas.drawLine(
                            w*.02f,y,
                            w*.22f,y-dp(13),
                            paint
                        );
                        canvas.drawLine(
                            w*.78f,y+dp(10),
                            w*.98f,y-dp(4),
                            paint
                        );
                    }
                    break;

                case 5:
                    paint.setColor(
                        Color.argb(
                            theme==1 ? 70 : 30,
                            255,255,255
                        )
                    );
                    canvas.drawCircle(w*.12f,h*.16f,dp(36),paint);
                    canvas.drawCircle(w*.91f,h*.34f,dp(48),paint);
                    canvas.drawCircle(w*.58f,h*.88f,dp(55),paint);
                    break;

                case 6:
                    paint.setStrokeWidth(dp(3));
                    for(int i=0;i<12;i++) {
                        int c=
                            i%2==0
                                ? softAccent
                                : softWhite;
                        paint.setColor(c);
                        float x=w*(.05f+(i%6)*.18f);
                        float y=h*(i<6 ? .17f : .82f);
                        canvas.drawLine(
                            x,y,
                            x+dp((i%3)-1)*6,
                            y+dp(10),
                            paint
                        );
                    }
                    break;

                case 7:
                    paint.setColor(softWhite);
                    drawStar(canvas,w*.11f,h*.20f,dp(10),paint);
                    drawStar(canvas,w*.87f,h*.22f,dp(14),paint);
                    drawStar(canvas,w*.72f,h*.80f,dp(9),paint);
                    drawStar(canvas,w*.24f,h*.76f,dp(6),paint);
                    break;

                case 8:
                    paint.setColor(softAccent);
                    for(int i=0;i<5;i++) {
                        float x=w*(.10f+i*.20f);
                        android.graphics.RectF petal=
                            new android.graphics.RectF(
                                x-dp(5),
                                h*.12f-dp(10),
                                x+dp(5),
                                h*.12f+dp(10)
                            );
                        canvas.save();
                        canvas.rotate(
                            i%2==0 ? 28 : -28,
                            x,
                            h*.12f
                        );
                        canvas.drawOval(petal,paint);
                        canvas.restore();
                    }
                    break;

                case 9:
                    paint.setColor(softWhite);
                    paint.setStrokeWidth(dp(2));
                    drawSnowflake(canvas,w*.12f,h*.20f,dp(10),paint);
                    drawSnowflake(canvas,w*.86f,h*.24f,dp(14),paint);
                    drawSnowflake(canvas,w*.74f,h*.80f,dp(9),paint);
                    break;

                case 10:
                    paint.setColor(softAccent);
                    paint.setStrokeWidth(dp(7));
                    for(float x=-w*.15f;x<w*1.1f;x+=dp(38)) {
                        canvas.drawLine(
                            x,h,
                            x+w*.30f,0,
                            paint
                        );
                    }
                    break;

                case 11:
                    paint.setColor(softWhite);
                    paint.setStrokeWidth(dp(2));
                    float cx=w*.86f;
                    float cy=h*.20f;
                    for(int i=0;i<10;i++) {
                        double a=
                            Math.PI*2*i/10.0;
                        canvas.drawLine(
                            cx,
                            cy,
                            cx+(float)Math.cos(a)*dp(28),
                            cy+(float)Math.sin(a)*dp(28),
                            paint
                        );
                    }
                    break;

                // 2.44.0 aesthetic collection.
                case 20: // Cherry Blossom Love
                    paint.setColor(
                        Color.argb(72,255,142,184)
                    );
                    drawFlower(canvas,w*.08f,h*.18f,dp(11),paint);
                    drawFlower(canvas,w*.91f,h*.20f,dp(14),paint);
                    drawFlower(canvas,w*.78f,h*.82f,dp(10),paint);
                    paint.setColor(
                        Color.argb(60,194,70,105)
                    );
                    drawCherryPair(canvas,w*.20f,h*.80f,dp(9),paint);
                    drawHeart(canvas,w*.56f,h*.12f,dp(6),paint);
                    break;

                case 21: // Blueberry Jelly Sky
                    paint.setColor(
                        Color.argb(72,255,255,255)
                    );
                    drawCloud(canvas,w*.14f,h*.22f,dp(10),paint);
                    drawCloud(canvas,w*.82f,h*.24f,dp(13),paint);
                    paint.setColor(
                        Color.argb(74,255,232,111)
                    );
                    drawStar(canvas,w*.26f,h*.14f,dp(6),paint);
                    drawStar(canvas,w*.72f,h*.78f,dp(8),paint);
                    drawMoon(canvas,w*.90f,h*.78f,dp(12),paint);
                    break;

                case 22: // Matcha Bunny Café
                    paint.setColor(
                        Color.argb(72,95,158,90)
                    );
                    drawLeaf(canvas,w*.10f,h*.20f,dp(12),paint);
                    drawLeaf(canvas,w*.88f,h*.22f,dp(11),paint);
                    drawLeaf(canvas,w*.76f,h*.82f,dp(10),paint);
                    paint.setColor(
                        Color.argb(68,255,255,255)
                    );
                    drawBunny(canvas,w*.18f,h*.80f,dp(11),paint);
                    break;

                case 23: // Peach Teddy Dessert
                    paint.setColor(
                        Color.argb(78,154,92,64)
                    );
                    drawBear(canvas,w*.13f,h*.21f,dp(12),paint);
                    drawBear(canvas,w*.86f,h*.78f,dp(11),paint);
                    paint.setColor(
                        Color.argb(74,255,255,255)
                    );
                    drawSpark(canvas,w*.88f,h*.20f,dp(7),paint);
                    drawHeart(canvas,w*.23f,h*.80f,dp(7),paint);
                    break;

                case 24: // Lilac Butterfly Diary
                    paint.setColor(
                        Color.argb(72,137,78,194)
                    );
                    drawButterfly(canvas,w*.10f,h*.20f,dp(11),paint);
                    drawButterfly(canvas,w*.87f,h*.22f,dp(13),paint);
                    drawButterfly(canvas,w*.75f,h*.80f,dp(10),paint);
                    paint.setColor(softWhite);
                    drawSpark(canvas,w*.28f,h*.14f,dp(6),paint);
                    break;

                case 25: // Midnight Neon Arcade
                    paint.setColor(
                        Color.argb(90,64,235,255)
                    );
                    drawSpark(canvas,w*.10f,h*.17f,dp(8),paint);
                    paint.setColor(
                        Color.argb(82,255,52,226)
                    );
                    drawStar(canvas,w*.88f,h*.20f,dp(11),paint);
                    paint.setStrokeWidth(dp(2));
                    canvas.drawLine(w*.08f,h*.84f,w*.26f,h*.76f,paint);
                    canvas.drawLine(w*.73f,h*.82f,w*.92f,h*.75f,paint);
                    break;

                case 26: // Strawberry Ribbon Milk
                    paint.setColor(
                        Color.argb(78,235,72,122)
                    );
                    drawStrawberry(canvas,w*.11f,h*.20f,dp(10),paint);
                    drawStrawberry(canvas,w*.87f,h*.23f,dp(11),paint);
                    paint.setColor(
                        Color.argb(72,255,255,255)
                    );
                    drawBow(canvas,w*.75f,h*.80f,dp(11),paint);
                    drawHeart(canvas,w*.22f,h*.82f,dp(7),paint);
                    break;

                case 27: // Cloudy Moon Sleep
                    paint.setColor(
                        Color.argb(68,255,255,255)
                    );
                    drawCloud(canvas,w*.12f,h*.20f,dp(12),paint);
                    drawCloud(canvas,w*.84f,h*.22f,dp(14),paint);
                    paint.setColor(
                        Color.argb(76,255,236,138)
                    );
                    drawMoon(canvas,w*.78f,h*.80f,dp(12),paint);
                    drawStar(canvas,w*.24f,h*.80f,dp(7),paint);
                    break;

                case 28: // Mint Frog Garden
                    paint.setColor(
                        Color.argb(74,76,153,80)
                    );
                    drawLeaf(canvas,w*.10f,h*.18f,dp(11),paint);
                    drawLeaf(canvas,w*.90f,h*.20f,dp(12),paint);
                    drawLeaf(canvas,w*.76f,h*.82f,dp(11),paint);
                    paint.setColor(
                        Color.argb(66,255,255,255)
                    );
                    drawFrog(canvas,w*.18f,h*.80f,dp(12),paint);
                    break;

                case 29: // Rosy Bear Picnic
                    paint.setColor(
                        Color.argb(72,139,89,68)
                    );
                    drawBear(canvas,w*.11f,h*.20f,dp(11),paint);
                    drawBear(canvas,w*.88f,h*.23f,dp(12),paint);
                    paint.setColor(
                        Color.argb(72,235,119,145)
                    );
                    drawBow(canvas,w*.76f,h*.80f,dp(10),paint);
                    drawHeart(canvas,w*.22f,h*.80f,dp(7),paint);
                    break;

                case 30: // Lavender Lace Dream
                    paint.setColor(
                        Color.argb(68,132,83,190)
                    );
                    drawButterfly(canvas,w*.10f,h*.20f,dp(10),paint);
                    drawButterfly(canvas,w*.88f,h*.22f,dp(12),paint);
                    paint.setColor(
                        Color.argb(64,255,255,255)
                    );
                    drawFlower(canvas,w*.76f,h*.82f,dp(9),paint);
                    drawSpark(canvas,w*.25f,h*.80f,dp(6),paint);
                    break;

                case 31: // Sakura Cherry Soda
                    paint.setColor(
                        Color.argb(74,255,133,178)
                    );
                    drawFlower(canvas,w*.09f,h*.19f,dp(10),paint);
                    drawFlower(canvas,w*.89f,h*.22f,dp(12),paint);
                    paint.setColor(
                        Color.argb(68,195,65,96)
                    );
                    drawCherryPair(canvas,w*.76f,h*.81f,dp(9),paint);
                    drawCherryPair(canvas,w*.22f,h*.80f,dp(8),paint);
                    break;

                case 32: // Ocean Jelly Star
                    paint.setColor(
                        Color.argb(72,255,255,255)
                    );
                    drawBubbleCluster(canvas,w*.11f,h*.20f,dp(12),paint);
                    drawBubbleCluster(canvas,w*.88f,h*.24f,dp(14),paint);
                    paint.setColor(
                        Color.argb(72,255,235,92)
                    );
                    drawStar(canvas,w*.77f,h*.80f,dp(10),paint);
                    break;

                case 33: // Cozy Cocoa Bunny
                    paint.setColor(
                        Color.argb(70,116,70,50)
                    );
                    drawBunny(canvas,w*.12f,h*.20f,dp(11),paint);
                    drawBear(canvas,w*.88f,h*.22f,dp(11),paint);
                    paint.setColor(
                        Color.argb(64,255,245,232)
                    );
                    drawHeart(canvas,w*.77f,h*.80f,dp(8),paint);
                    drawSpark(canvas,w*.22f,h*.80f,dp(5),paint);
                    break;

                case 34: // Pink Kitty Bow
                    paint.setColor(
                        Color.argb(72,221,72,143)
                    );
                    drawBow(canvas,w*.10f,h*.20f,dp(11),paint);
                    drawBow(canvas,w*.88f,h*.22f,dp(12),paint);
                    paint.setColor(
                        Color.argb(68,255,255,255)
                    );
                    drawCat(canvas,w*.76f,h*.81f,dp(11),paint);
                    drawHeart(canvas,w*.23f,h*.80f,dp(7),paint);
                    break;

                case 35: // Blue Porcelain Bloom
                    paint.setColor(
                        Color.argb(76,40,86,164)
                    );
                    drawFlower(canvas,w*.10f,h*.19f,dp(11),paint);
                    drawFlower(canvas,w*.89f,h*.21f,dp(13),paint);
                    drawFlower(canvas,w*.77f,h*.81f,dp(9),paint);
                    paint.setColor(softWhite);
                    drawSpark(canvas,w*.23f,h*.80f,dp(5),paint);
                    break;

                case 36: // Purple Lotus Watercolor
                    paint.setColor(
                        Color.argb(68,122,75,180)
                    );
                    drawLotus(canvas,w*.12f,h*.20f,dp(13),paint);
                    drawLotus(canvas,w*.86f,h*.23f,dp(12),paint);
                    drawButterfly(canvas,w*.76f,h*.81f,dp(9),paint);
                    break;

                case 37: // Cream Heart Minimal
                    paint.setColor(
                        Color.argb(56,154,118,95)
                    );
                    drawHeart(canvas,w*.10f,h*.20f,dp(8),paint);
                    drawHeart(canvas,w*.89f,h*.22f,dp(9),paint);
                    drawBow(canvas,w*.77f,h*.81f,dp(8),paint);
                    drawHeart(canvas,w*.22f,h*.80f,dp(6),paint);
                    break;

                case 38: // Brown Butterfly Noir
                    paint.setColor(
                        Color.argb(82,190,130,78)
                    );
                    drawButterfly(canvas,w*.10f,h*.20f,dp(11),paint);
                    drawButterfly(canvas,w*.88f,h*.22f,dp(13),paint);
                    drawButterfly(canvas,w*.76f,h*.81f,dp(10),paint);
                    paint.setColor(
                        Color.argb(58,255,255,255)
                    );
                    drawSpark(canvas,w*.24f,h*.80f,dp(6),paint);
                    break;

                case 39: // Snowy Pastel Christmas
                    paint.setColor(
                        Color.argb(82,255,255,255)
                    );
                    paint.setStrokeWidth(dp(2));
                    drawSnowflake(canvas,w*.10f,h*.20f,dp(10),paint);
                    drawSnowflake(canvas,w*.88f,h*.22f,dp(13),paint);
                    drawSnowflake(canvas,w*.76f,h*.80f,dp(9),paint);
                    paint.setColor(
                        Color.argb(64,224,75,92)
                    );
                    drawBow(canvas,w*.23f,h*.80f,dp(8),paint);
                    break;
            }
        }


        void drawMotifAsset(
            android.graphics.Canvas canvas,
            android.graphics.drawable.Drawable drawable,
            float x,
            float y,
            int size
        ) {
            int left=
                Math.round(x);

            int top=
                Math.round(y);

            drawable.setBounds(
                left,
                top,
                left+size,
                top+size
            );

            drawable.draw(canvas);
        }


        void drawAestheticPackComposition(
            android.graphics.Canvas canvas,
            float w,
            float h
        ) {
            int soft=
                blendThemeColor(
                    accent,
                    Color.WHITE,
                    theme==1 ? 30 : 55
                );

            int deep=
                blendThemeColor(
                    accent,
                    Color.BLACK,
                    theme==1 ? 8 : 18
                );

            switch(pack) {
                case 100: // Cherry Blossom Love
                    drawThemeGarland(
                        canvas,w,h,
                        R.drawable.theme_motif_flower,
                        R.drawable.theme_motif_cherry,
                        soft
                    );
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_heart,accent,185,w*.47f,h*.08f,dp(24));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_flower,accent,210,w*.02f,h*.68f,dp(46));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_flower,soft,205,w*.84f,h*.66f,dp(52));
                    break;

                case 101: // Blueberry Jelly Sky
                    drawThemeStars(canvas,w,h,soft);
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_moonstar,soft,220,w*.03f,h*.05f,dp(58));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_cloud,Color.WHITE,185,w*.72f,h*.08f,dp(68));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_cloud,soft,150,w*.16f,h*.72f,dp(54));
                    break;

                case 102: // Matcha Bunny Cafe
                    drawThemeLeafBorder(canvas,w,h,soft);
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_bunny,accent,215,w*.03f,h*.62f,dp(58));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_cup,deep,205,w*.76f,h*.66f,dp(52));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_leaf,accent,175,w*.45f,h*.06f,dp(28));
                    break;

                case 103: // Peach Teddy Dessert
                    drawThemeGrid(canvas,w,h,accent,34,30,30);
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_bear,deep,220,w*.76f,h*.03f,dp(66));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_cake,accent,215,w*.03f,h*.66f,dp(54));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_heart,soft,170,w*.45f,h*.08f,dp(24));
                    break;

                case 104: // Lilac Butterfly Diary
                    drawThemeScallops(canvas,w,h,soft);
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_butterfly,accent,220,w*.02f,h*.06f,dp(50));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_butterfly,soft,205,w*.80f,h*.09f,dp(48));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_bow,accent,190,w*.43f,h*.70f,dp(40));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_flower,soft,185,w*.77f,h*.66f,dp(38));
                    break;

                case 105: // Midnight Neon Arcade
                    drawThemeNeonGrid(canvas,w,h,accent);
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_star,accent,230,w*.03f,h*.08f,dp(36));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_bubble,Color.MAGENTA,185,w*.77f,h*.05f,dp(54));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_moonstar,Color.CYAN,190,w*.76f,h*.68f,dp(42));
                    break;

                case 106: // Strawberry Ribbon Milk
                    drawThemeDots(canvas,w,h,accent,26,20);
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_strawberry,accent,220,w*.02f,h*.06f,dp(48));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_bow,soft,230,w*.78f,h*.05f,dp(58));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_heart,accent,175,w*.46f,h*.08f,dp(24));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_strawberry,soft,200,w*.80f,h*.68f,dp(40));
                    break;

                case 107: // Cloudy Moon Sleep
                    drawThemeStars(canvas,w,h,soft);
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_moonstar,soft,230,w*.03f,h*.03f,dp(68));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_cloud,Color.WHITE,185,w*.60f,h*.09f,dp(78));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_cloud,soft,150,w*.12f,h*.72f,dp(62));
                    break;

                case 108: // Mint Frog Garden
                    drawThemeLeafBorder(canvas,w,h,accent);
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_frog,accent,220,w*.02f,h*.62f,dp(58));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_frog,soft,205,w*.78f,h*.05f,dp(54));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_flower,Color.WHITE,190,w*.46f,h*.06f,dp(34));
                    break;

                case 109: // Rosy Bear Picnic
                    drawThemeGrid(canvas,w,h,deep,38,32,34);
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_bear,deep,220,w*.74f,h*.04f,dp(66));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_bow,accent,210,w*.03f,h*.07f,dp(44));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_heart,soft,180,w*.08f,h*.70f,dp(34));
                    break;

                case 110: // Lavender Lace Dream
                    drawThemeScallops(canvas,w,h,soft);
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_butterfly,accent,220,w*.03f,h*.05f,dp(48));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_butterfly,soft,200,w*.80f,h*.07f,dp(52));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_flower,accent,185,w*.05f,h*.69f,dp(40));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_bow,soft,190,w*.76f,h*.70f,dp(42));
                    break;

                case 111: // Sakura Cherry Soda
                    drawThemeGarland(canvas,w,h,R.drawable.theme_motif_flower,R.drawable.theme_motif_cherry,soft);
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_cup,accent,205,w*.77f,h*.66f,dp(50));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_cherry,deep,190,w*.06f,h*.68f,dp(38));
                    break;

                case 112: // Ocean Jelly Star
                    drawThemeWaves(canvas,w,h,soft);
                    drawThemeBubbles(canvas,w,h,Color.WHITE);
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_star,soft,220,w*.03f,h*.06f,dp(40));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_moonstar,accent,180,w*.78f,h*.68f,dp(42));
                    break;

                case 113: // Cozy Cocoa Bunny
                    drawThemeChecker(canvas,w,h,deep,soft);
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_bunny,soft,220,w*.03f,h*.05f,dp(52));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_bear,accent,215,w*.78f,h*.05f,dp(56));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_cup,deep,205,w*.76f,h*.68f,dp(46));
                    break;

                case 114: // Pink Kitty Bow
                    drawThemeDots(canvas,w,h,soft,28,18);
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_cat,accent,220,w*.75f,h*.03f,dp(64));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_bow,soft,225,w*.02f,h*.05f,dp(50));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_heart,accent,185,w*.08f,h*.70f,dp(34));
                    break;

                case 115: // Blue Porcelain Bloom
                    drawThemeFrame(canvas,w,h,accent,2);
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_flower,accent,220,w*.00f,h*.02f,dp(58));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_lotus,deep,190,w*.78f,h*.04f,dp(58));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_flower,soft,205,w*.78f,h*.68f,dp(48));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_flower,accent,180,w*.02f,h*.70f,dp(42));
                    break;

                case 116: // Purple Lotus Watercolor
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_lotus,accent,215,w*.02f,h*.04f,dp(58));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_lotus,soft,205,w*.78f,h*.04f,dp(64));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_butterfly,accent,185,w*.72f,h*.68f,dp(44));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_lotus,soft,165,w*.08f,h*.70f,dp(40));
                    break;

                case 117: // Cream Heart Minimal
                    drawThemeFrame(canvas,w,h,accent,1);
                    for(int i=0;i<5;i++)
                        drawTintedThemeAsset(canvas,R.drawable.theme_motif_heart,accent,120,w*(.09f+i*.19f),h*.06f,dp(24));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_bow,accent,165,w*.76f,h*.70f,dp(42));
                    break;

                case 118: // Brown Butterfly Noir
                    drawThemeFrame(canvas,w,h,accent,3);
                    drawThemeStars(canvas,w,h,accent);
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_butterfly,accent,230,w*.02f,h*.04f,dp(52));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_butterfly,soft,205,w*.78f,h*.07f,dp(50));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_butterfly,accent,180,w*.74f,h*.68f,dp(40));
                    break;

                case 119: // Snowy Pastel Christmas
                    drawThemeSnow(canvas,w,h,Color.WHITE);
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_snow,Color.WHITE,230,w*.03f,h*.04f,dp(48));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_gift,accent,220,w*.78f,h*.05f,dp(54));
                    drawTintedThemeAsset(canvas,R.drawable.theme_motif_bow,soft,200,w*.07f,h*.68f,dp(42));
                    break;
            }
        }


        void drawThemeGarland(
            android.graphics.Canvas canvas,
            float w,
            float h,
            int a,
            int b,
            int tint
        ) {
            for(int i=0;i<6;i++) {
                int res=i%2==0 ? a : b;
                drawTintedThemeAsset(
                    canvas,res,tint,165,
                    w*(.02f+i*.17f),
                    h*.03f,
                    dp(i%2==0 ? 30 : 26)
                );
            }
        }


        void drawThemeGrid(
            android.graphics.Canvas canvas,
            float w,
            float h,
            int color,
            int stepX,
            int stepY,
            int alpha
        ) {
            paint.setStyle(android.graphics.Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1));
            paint.setColor(
                Color.argb(
                    alpha,
                    Color.red(color),
                    Color.green(color),
                    Color.blue(color)
                )
            );
            for(float x=0;x<w;x+=dp(stepX))
                canvas.drawLine(x,0,x,h,paint);
            for(float y=0;y<h;y+=dp(stepY))
                canvas.drawLine(0,y,w,y,paint);
            paint.setStyle(android.graphics.Paint.Style.FILL);
        }


        void drawThemeDots(
            android.graphics.Canvas canvas,
            float w,
            float h,
            int color,
            int step,
            int alpha
        ) {
            paint.setColor(
                Color.argb(
                    alpha,
                    Color.red(color),
                    Color.green(color),
                    Color.blue(color)
                )
            );
            for(float y=dp(12);y<h;y+=dp(step))
                for(float x=dp(12);x<w;x+=dp(step))
                    canvas.drawCircle(x,y,dp(1.6f),paint);
        }


        void drawThemeStars(
            android.graphics.Canvas canvas,
            float w,
            float h,
            int color
        ) {
            int[] xs={12,28,51,69,88,38,82};
            int[] ys={16,28,11,25,14,78,72};
            for(int i=0;i<xs.length;i++)
                drawTintedThemeAsset(
                    canvas,
                    R.drawable.theme_motif_star,
                    color,
                    135,
                    w*xs[i]/100f,
                    h*ys[i]/100f,
                    dp(i%3==0 ? 18 : 13)
                );
        }


        void drawThemeLeafBorder(
            android.graphics.Canvas canvas,
            float w,
            float h,
            int color
        ) {
            for(int i=0;i<7;i++) {
                drawTintedThemeAsset(
                    canvas,
                    R.drawable.theme_motif_leaf,
                    color,
                    160,
                    w*(.01f+i*.15f),
                    h*.02f,
                    dp(24)
                );
                drawTintedThemeAsset(
                    canvas,
                    R.drawable.theme_motif_leaf,
                    color,
                    120,
                    w*(.06f+i*.14f),
                    h*.82f,
                    dp(20)
                );
            }
        }


        void drawThemeScallops(
            android.graphics.Canvas canvas,
            float w,
            float h,
            int color
        ) {
            paint.setStyle(android.graphics.Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1));
            paint.setColor(
                Color.argb(
                    90,
                    Color.red(color),
                    Color.green(color),
                    Color.blue(color)
                )
            );
            for(float x=dp(8);x<w;x+=dp(18)) {
                canvas.drawCircle(x,dp(10),dp(8),paint);
                canvas.drawCircle(x,h-dp(10),dp(8),paint);
            }
            paint.setStyle(android.graphics.Paint.Style.FILL);
        }


        void drawThemeNeonGrid(
            android.graphics.Canvas canvas,
            float w,
            float h,
            int color
        ) {
            drawThemeGrid(canvas,w,h,color,28,24,70);
            paint.setStrokeWidth(dp(2));
            paint.setColor(
                Color.argb(
                    150,
                    Color.red(color),
                    Color.green(color),
                    Color.blue(color)
                )
            );
            canvas.drawLine(0,h*.22f,w,h*.12f,paint);
            canvas.drawLine(0,h*.82f,w,h*.68f,paint);
        }


        void drawThemeWaves(
            android.graphics.Canvas canvas,
            float w,
            float h,
            int color
        ) {
            paint.setStyle(android.graphics.Paint.Style.STROKE);
            paint.setStrokeWidth(dp(2));
            paint.setColor(
                Color.argb(
                    90,
                    Color.red(color),
                    Color.green(color),
                    Color.blue(color)
                )
            );
            for(int band=0;band<3;band++) {
                android.graphics.Path path=new android.graphics.Path();
                float y=h*(.14f+band*.33f);
                path.moveTo(0,y);
                for(int i=1;i<=8;i++) {
                    float x=w*i/8f;
                    float yy=y+(i%2==0 ? -dp(6) : dp(6));
                    path.lineTo(x,yy);
                }
                canvas.drawPath(path,paint);
            }
            paint.setStyle(android.graphics.Paint.Style.FILL);
        }


        void drawThemeBubbles(
            android.graphics.Canvas canvas,
            float w,
            float h,
            int color
        ) {
            paint.setStyle(android.graphics.Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1));
            paint.setColor(Color.argb(90,Color.red(color),Color.green(color),Color.blue(color)));
            int[] xs={10,23,44,68,87,78,32};
            int[] ys={20,72,13,22,63,82,84};
            for(int i=0;i<xs.length;i++)
                canvas.drawCircle(w*xs[i]/100f,h*ys[i]/100f,dp(4+i%3*2),paint);
            paint.setStyle(android.graphics.Paint.Style.FILL);
        }


        void drawThemeChecker(
            android.graphics.Canvas canvas,
            float w,
            float h,
            int a,
            int b
        ) {
            float size=dp(24);
            for(int r=0;r*size<h;r++) {
                for(int c=0;c*size<w;c++) {
                    int color=(r+c)%2==0 ? a : b;
                    paint.setColor(Color.argb(24,Color.red(color),Color.green(color),Color.blue(color)));
                    canvas.drawRect(c*size,r*size,(c+1)*size,(r+1)*size,paint);
                }
            }
        }


        void drawThemeFrame(
            android.graphics.Canvas canvas,
            float w,
            float h,
            int color,
            int width
        ) {
            paint.setStyle(android.graphics.Paint.Style.STROKE);
            paint.setStrokeWidth(dp(width));
            paint.setColor(Color.argb(150,Color.red(color),Color.green(color),Color.blue(color)));
            canvas.drawRoundRect(
                new android.graphics.RectF(dp(4),dp(4),w-dp(4),h-dp(4)),
                dp(16),dp(16),paint
            );
            paint.setStyle(android.graphics.Paint.Style.FILL);
        }


        void drawThemeSnow(
            android.graphics.Canvas canvas,
            float w,
            float h,
            int color
        ) {
            int[] xs={8,19,31,45,58,72,86,94,25,67};
            int[] ys={12,28,9,22,14,31,10,25,76,80};
            for(int i=0;i<xs.length;i++)
                drawTintedThemeAsset(
                    canvas,
                    R.drawable.theme_motif_snow,
                    color,
                    135,
                    w*xs[i]/100f,
                    h*ys[i]/100f,
                    dp(i%3==0 ? 20 : 14)
                );
        }


        void drawFlower(
            android.graphics.Canvas canvas,
            float cx,
            float cy,
            float size,
            android.graphics.Paint p
        ) {
            for(int i=0;i<5;i++) {
                double a=
                    -Math.PI/2+
                    i*Math.PI*2/5;

                float px=
                    cx+
                    (float)Math.cos(a)*size*.62f;

                float py=
                    cy+
                    (float)Math.sin(a)*size*.62f;

                canvas.drawCircle(
                    px,
                    py,
                    size*.42f,
                    p
                );
            }

            int old=p.getColor();
            p.setColor(
                Color.argb(
                    Math.min(
                        255,
                        Color.alpha(old)+35
                    ),
                    255,236,174
                )
            );
            canvas.drawCircle(
                cx,cy,
                size*.28f,
                p
            );
            p.setColor(old);
        }


        void drawCherryPair(
            android.graphics.Canvas canvas,
            float cx,
            float cy,
            float size,
            android.graphics.Paint p
        ) {
            canvas.drawCircle(
                cx-size*.38f,
                cy+size*.20f,
                size*.42f,
                p
            );

            canvas.drawCircle(
                cx+size*.38f,
                cy+size*.20f,
                size*.42f,
                p
            );

            paint.setStyle(
                android.graphics.Paint.Style.STROKE
            );

            paint.setStrokeWidth(
                Math.max(
                    1f,
                    size*.12f
                )
            );

            canvas.drawLine(
                cx-size*.34f,
                cy-size*.10f,
                cx,
                cy-size*.72f,
                paint
            );

            canvas.drawLine(
                cx+size*.34f,
                cy-size*.10f,
                cx,
                cy-size*.72f,
                paint
            );

            paint.setStyle(
                android.graphics.Paint.Style.FILL
            );
        }


        void drawCloud(
            android.graphics.Canvas canvas,
            float cx,
            float cy,
            float size,
            android.graphics.Paint p
        ) {
            canvas.drawCircle(
                cx-size*.48f,
                cy,
                size*.48f,
                p
            );
            canvas.drawCircle(
                cx,
                cy-size*.20f,
                size*.62f,
                p
            );
            canvas.drawCircle(
                cx+size*.52f,
                cy,
                size*.43f,
                p
            );
            canvas.drawRect(
                cx-size*.85f,
                cy,
                cx+size*.88f,
                cy+size*.42f,
                p
            );
        }


        void drawMoon(
            android.graphics.Canvas canvas,
            float cx,
            float cy,
            float size,
            android.graphics.Paint p
        ) {
            canvas.drawCircle(
                cx,cy,
                size,
                p
            );

            int old=p.getColor();

            p.setColor(
                Color.argb(
                    Math.max(
                        18,
                        Color.alpha(old)/2
                    ),
                    30,35,75
                )
            );

            canvas.drawCircle(
                cx+size*.42f,
                cy-size*.18f,
                size*.92f,
                p
            );

            p.setColor(old);
        }


        void drawLeaf(
            android.graphics.Canvas canvas,
            float cx,
            float cy,
            float size,
            android.graphics.Paint p
        ) {
            android.graphics.RectF oval=
                new android.graphics.RectF(
                    cx-size*.45f,
                    cy-size,
                    cx+size*.45f,
                    cy+size
                );

            canvas.save();
            canvas.rotate(
                -34,
                cx,cy
            );
            canvas.drawOval(
                oval,
                p
            );
            canvas.restore();
        }


        void drawBunny(
            android.graphics.Canvas canvas,
            float cx,
            float cy,
            float size,
            android.graphics.Paint p
        ) {
            canvas.drawCircle(
                cx,cy,
                size*.62f,
                p
            );

            android.graphics.RectF left=
                new android.graphics.RectF(
                    cx-size*.58f,
                    cy-size*1.35f,
                    cx-size*.12f,
                    cy-size*.20f
                );

            android.graphics.RectF right=
                new android.graphics.RectF(
                    cx+size*.12f,
                    cy-size*1.35f,
                    cx+size*.58f,
                    cy-size*.20f
                );

            canvas.drawOval(left,p);
            canvas.drawOval(right,p);
        }


        void drawBear(
            android.graphics.Canvas canvas,
            float cx,
            float cy,
            float size,
            android.graphics.Paint p
        ) {
            canvas.drawCircle(
                cx,cy,
                size*.70f,
                p
            );
            canvas.drawCircle(
                cx-size*.58f,
                cy-size*.52f,
                size*.34f,
                p
            );
            canvas.drawCircle(
                cx+size*.58f,
                cy-size*.52f,
                size*.34f,
                p
            );
        }


        void drawButterfly(
            android.graphics.Canvas canvas,
            float cx,
            float cy,
            float size,
            android.graphics.Paint p
        ) {
            canvas.drawOval(
                new android.graphics.RectF(
                    cx-size,
                    cy-size*.72f,
                    cx-size*.10f,
                    cy+size*.25f
                ),
                p
            );

            canvas.drawOval(
                new android.graphics.RectF(
                    cx+size*.10f,
                    cy-size*.72f,
                    cx+size,
                    cy+size*.25f
                ),
                p
            );

            canvas.drawOval(
                new android.graphics.RectF(
                    cx-size*.78f,
                    cy+size*.02f,
                    cx-size*.08f,
                    cy+size*.72f
                ),
                p
            );

            canvas.drawOval(
                new android.graphics.RectF(
                    cx+size*.08f,
                    cy+size*.02f,
                    cx+size*.78f,
                    cy+size*.72f
                ),
                p
            );

            canvas.drawRect(
                cx-size*.08f,
                cy-size*.45f,
                cx+size*.08f,
                cy+size*.62f,
                p
            );
        }


        void drawStrawberry(
            android.graphics.Canvas canvas,
            float cx,
            float cy,
            float size,
            android.graphics.Paint p
        ) {
            android.graphics.Path path=
                new android.graphics.Path();

            path.moveTo(
                cx-size*.82f,
                cy-size*.48f
            );

            path.quadTo(
                cx,
                cy+size*1.18f,
                cx+size*.82f,
                cy-size*.48f
            );

            path.quadTo(
                cx,
                cy-size*.80f,
                cx-size*.82f,
                cy-size*.48f
            );

            path.close();
            canvas.drawPath(
                path,p
            );

            int old=p.getColor();

            p.setColor(
                Color.argb(
                    Math.min(
                        255,
                        Color.alpha(old)+35
                    ),
                    75,145,72
                )
            );

            drawLeaf(
                canvas,
                cx,
                cy-size*.72f,
                size*.45f,
                p
            );

            p.setColor(old);
        }


        void drawBow(
            android.graphics.Canvas canvas,
            float cx,
            float cy,
            float size,
            android.graphics.Paint p
        ) {
            android.graphics.Path left=
                new android.graphics.Path();

            left.moveTo(cx,cy);
            left.lineTo(
                cx-size,
                cy-size*.62f
            );
            left.lineTo(
                cx-size*.82f,
                cy+size*.62f
            );
            left.close();

            android.graphics.Path right=
                new android.graphics.Path();

            right.moveTo(cx,cy);
            right.lineTo(
                cx+size,
                cy-size*.62f
            );
            right.lineTo(
                cx+size*.82f,
                cy+size*.62f
            );
            right.close();

            canvas.drawPath(left,p);
            canvas.drawPath(right,p);
            canvas.drawCircle(
                cx,cy,
                size*.26f,
                p
            );
        }


        void drawFrog(
            android.graphics.Canvas canvas,
            float cx,
            float cy,
            float size,
            android.graphics.Paint p
        ) {
            canvas.drawCircle(
                cx,cy,
                size*.72f,
                p
            );

            canvas.drawCircle(
                cx-size*.46f,
                cy-size*.58f,
                size*.34f,
                p
            );

            canvas.drawCircle(
                cx+size*.46f,
                cy-size*.58f,
                size*.34f,
                p
            );
        }


        void drawBubbleCluster(
            android.graphics.Canvas canvas,
            float cx,
            float cy,
            float size,
            android.graphics.Paint p
        ) {
            paint.setStyle(
                android.graphics.Paint.Style.STROKE
            );

            paint.setStrokeWidth(
                Math.max(
                    1f,
                    size*.12f
                )
            );

            canvas.drawCircle(
                cx,cy,
                size*.72f,
                p
            );

            canvas.drawCircle(
                cx+size*.82f,
                cy-size*.48f,
                size*.34f,
                p
            );

            canvas.drawCircle(
                cx-size*.76f,
                cy+size*.42f,
                size*.28f,
                p
            );

            paint.setStyle(
                android.graphics.Paint.Style.FILL
            );
        }


        void drawCat(
            android.graphics.Canvas canvas,
            float cx,
            float cy,
            float size,
            android.graphics.Paint p
        ) {
            canvas.drawCircle(
                cx,cy+size*.10f,
                size*.66f,
                p
            );

            android.graphics.Path left=
                new android.graphics.Path();

            left.moveTo(
                cx-size*.60f,
                cy-size*.22f
            );

            left.lineTo(
                cx-size*.52f,
                cy-size
            );

            left.lineTo(
                cx-size*.05f,
                cy-size*.52f
            );

            left.close();
            canvas.drawPath(left,p);

            android.graphics.Path right=
                new android.graphics.Path();

            right.moveTo(
                cx+size*.60f,
                cy-size*.22f
            );

            right.lineTo(
                cx+size*.52f,
                cy-size
            );

            right.lineTo(
                cx+size*.05f,
                cy-size*.52f
            );

            right.close();
            canvas.drawPath(right,p);
        }


        void drawLotus(
            android.graphics.Canvas canvas,
            float cx,
            float cy,
            float size,
            android.graphics.Paint p
        ) {
            for(int i=-2;i<=2;i++) {
                float offset=
                    i*size*.30f;

                android.graphics.RectF petal=
                    new android.graphics.RectF(
                        cx-size*.28f+offset,
                        cy-size*.82f+
                            Math.abs(i)*size*.12f,
                        cx+size*.28f+offset,
                        cy+size*.48f
                    );

                canvas.save();
                canvas.rotate(
                    i*13,
                    cx+offset,
                    cy
                );
                canvas.drawOval(
                    petal,p
                );
                canvas.restore();
            }
        }


        void drawHeart(
            android.graphics.Canvas canvas,
            float cx,
            float cy,
            float size,
            android.graphics.Paint p
        ) {
            android.graphics.Path path=
                new android.graphics.Path();

            path.moveTo(cx,cy+size*.72f);
            path.cubicTo(
                cx-size*1.2f,
                cy-size*.05f,
                cx-size*.65f,
                cy-size*.85f,
                cx,
                cy-size*.25f
            );
            path.cubicTo(
                cx+size*.65f,
                cy-size*.85f,
                cx+size*1.2f,
                cy-size*.05f,
                cx,
                cy+size*.72f
            );
            canvas.drawPath(path,p);
        }


        void drawSpark(
            android.graphics.Canvas canvas,
            float cx,
            float cy,
            float size,
            android.graphics.Paint p
        ) {
            android.graphics.Path path=
                new android.graphics.Path();

            path.moveTo(cx,cy-size);
            path.lineTo(cx+size*.24f,cy-size*.24f);
            path.lineTo(cx+size,cy);
            path.lineTo(cx+size*.24f,cy+size*.24f);
            path.lineTo(cx,cy+size);
            path.lineTo(cx-size*.24f,cy+size*.24f);
            path.lineTo(cx-size,cy);
            path.lineTo(cx-size*.24f,cy-size*.24f);
            path.close();

            canvas.drawPath(path,p);
        }


        void drawStar(
            android.graphics.Canvas canvas,
            float cx,
            float cy,
            float radius,
            android.graphics.Paint p
        ) {
            android.graphics.Path path=
                new android.graphics.Path();

            for(int i=0;i<10;i++) {
                double angle=
                    -Math.PI/2+
                    i*Math.PI/5;

                float r=
                    i%2==0
                        ? radius
                        : radius*.42f;

                float x=
                    cx+(float)Math.cos(angle)*r;
                float y=
                    cy+(float)Math.sin(angle)*r;

                if(i==0)
                    path.moveTo(x,y);
                else
                    path.lineTo(x,y);
            }

            path.close();
            canvas.drawPath(path,p);
        }


        void drawSnowflake(
            android.graphics.Canvas canvas,
            float cx,
            float cy,
            float radius,
            android.graphics.Paint p
        ) {
            for(int i=0;i<3;i++) {
                double a=i*Math.PI/3;
                float dx=
                    (float)Math.cos(a)*radius;
                float dy=
                    (float)Math.sin(a)*radius;
                canvas.drawLine(
                    cx-dx,cy-dy,
                    cx+dx,cy+dy,
                    p
                );
            }
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


    android.graphics.Typeface cachedKeyboardTypeface=null;
    int cachedKeyboardTypefaceStyle=
        Integer.MIN_VALUE;


    android.graphics.Typeface keyboardTypeface() {
        int style=
            getSharedPreferences(
                "keykii_prefs",
                MODE_PRIVATE
            ).getInt(
                "keyboard_font_style",
                0
            );

        if(
            cachedKeyboardTypeface!=null &&
            cachedKeyboardTypefaceStyle==style
        ) {
            return cachedKeyboardTypeface;
        }

        android.graphics.Typeface result;

        switch(style) {
            case 1:
                result=
                    android.graphics.Typeface.create(
                        "sans-serif-rounded",
                        android.graphics.Typeface.NORMAL
                    );
                break;

            case 2:
                result=
                    android.graphics.Typeface.create(
                        "serif",
                        android.graphics.Typeface.NORMAL
                    );
                break;

            case 3:
                result=
                    android.graphics.Typeface.create(
                        "monospace",
                        android.graphics.Typeface.NORMAL
                    );
                break;

            case 4:
                result=
                    android.graphics.Typeface.create(
                        "sans-serif-condensed",
                        android.graphics.Typeface.NORMAL
                    );
                break;

            case 5:
                result=
                    android.graphics.Typeface.create(
                        "cursive",
                        android.graphics.Typeface.NORMAL
                    );
                break;

            case 6:
                result=
                    android.graphics.Typeface.create(
                        "sans-serif-medium",
                        android.graphics.Typeface.NORMAL
                    );
                break;

            case 100:
                result=assetKeyboardTypeface(
                    "fonts/fredoka.ttf"
                );
                break;

            case 101:
                result=assetKeyboardTypeface(
                    "fonts/dynapuff.ttf"
                );
                break;

            case 102:
                result=assetKeyboardTypeface(
                    "fonts/rubik_bubbles.ttf"
                );
                break;

            case 103:
                result=assetKeyboardTypeface(
                    "fonts/patrick_hand.ttf"
                );
                break;

            case 104:
                result=assetKeyboardTypeface(
                    "fonts/lobster.ttf"
                );
                break;

            case 105:
                result=assetKeyboardTypeface(
                    "fonts/bungee.ttf"
                );
                break;

            case 106:
                result=assetKeyboardTypeface(
                    "fonts/press_start_2p.ttf"
                );
                break;

            case 107:
                result=assetKeyboardTypeface(
                    "fonts/cinzel_decorative.ttf"
                );
                break;

            default:
                result=
                    android.graphics.Typeface.create(
                        "sans-serif",
                        android.graphics.Typeface.NORMAL
                    );
                break;
        }

        cachedKeyboardTypefaceStyle=style;
        cachedKeyboardTypeface=result;
        return result;
    }


    android.graphics.Typeface assetKeyboardTypeface(
        String assetPath
    ) {
        try {
            return android.graphics.Typeface.createFromAsset(
                getAssets(),
                assetPath
            );
        } catch(Exception ignored) {
            return android.graphics.Typeface.DEFAULT;
        }
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

        int keyStyle=
            themeKeyStyle();

        if(keyStyle>0) {
            return styledThemeKeyBackground(
                keyStyle,
                special,
                space,
                borders
            );
        }

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


    StateListDrawable styledThemeKeyBackground(
        int style,
        boolean special,
        boolean space,
        boolean borders
    ) {
        int accent=
            accentColor();

        int white=
            Color.rgb(
                255,255,255
            );

        int black=
            Color.rgb(
                10,10,14
            );

        int normalStart;
        int normalEnd;
        int pressedStart;
        int pressedEnd;
        int stroke;
        int strokeWidth=1;

        switch(style) {
            // Neon glow / cyber.
            case 1:
                normalStart=
                    Color.argb(
                        145,
                        12,14,28
                    );

                normalEnd=
                    Color.argb(
                        115,
                        Color.red(accent),
                        Color.green(accent),
                        Color.blue(accent)
                    );

                pressedStart=
                    Color.argb(
                        205,
                        Color.red(accent),
                        Color.green(accent),
                        Color.blue(accent)
                    );

                pressedEnd=
                    Color.argb(
                        185,
                        18,20,34
                    );

                stroke=accent;
                strokeWidth=2;
                break;

            // Frosted glass / crystal.
            case 2:
                normalStart=
                    Color.argb(
                        special || space
                            ? 155
                            : 92,
                        255,255,255
                    );

                normalEnd=
                    Color.argb(
                        special || space
                            ? 110
                            : 58,
                        Color.red(accent),
                        Color.green(accent),
                        Color.blue(accent)
                    );

                pressedStart=
                    Color.argb(
                        190,
                        255,255,255
                    );

                pressedEnd=
                    Color.argb(
                        125,
                        Color.red(accent),
                        Color.green(accent),
                        Color.blue(accent)
                    );

                stroke=
                    Color.argb(
                        190,
                        255,255,255
                    );

                strokeWidth=1;
                break;

            // Kawaii candy / pastel.
            case 3:
                normalStart=
                    blendThemeColor(
                        accent,
                        white,
                        special || space
                            ? 42
                            : 72
                    );

                normalEnd=
                    blendThemeColor(
                        accent,
                        white,
                        special || space
                            ? 22
                            : 58
                    );

                pressedStart=
                    blendThemeColor(
                        accent,
                        white,
                        28
                    );

                pressedEnd=
                    blendThemeColor(
                        accent,
                        white,
                        48
                    );

                stroke=
                    Color.argb(
                        220,
                        255,255,255
                    );

                strokeWidth=2;
                break;

            // Black-red / RGB gamer.
            case 4:
                normalStart=
                    special || space
                        ? Color.argb(
                            205,
                            Color.red(accent),
                            Color.green(accent),
                            Color.blue(accent)
                        )
                        : Color.argb(
                            228,
                            15,15,20
                        );

                normalEnd=
                    Color.argb(
                        190,
                        2,2,5
                    );

                pressedStart=
                    Color.argb(
                        230,
                        Color.red(accent),
                        Color.green(accent),
                        Color.blue(accent)
                    );

                pressedEnd=
                    Color.argb(
                        225,
                        10,10,14
                    );

                stroke=accent;
                strokeWidth=2;
                break;

            // Luxury black + gold / rose.
            case 5:
                normalStart=
                    special || space
                        ? blendThemeColor(
                            accent,
                            black,
                            35
                        )
                        : Color.rgb(
                            24,21,24
                        );

                normalEnd=
                    Color.rgb(
                        5,5,8
                    );

                pressedStart=
                    blendThemeColor(
                        accent,
                        white,
                        12
                    );

                pressedEnd=
                    Color.rgb(
                        18,16,20
                    );

                stroke=accent;
                strokeWidth=2;
                break;

            // Ice / water keys.
            case 6:
                normalStart=
                    Color.argb(
                        225,
                        235,249,255
                    );

                normalEnd=
                    Color.argb(
                        205,
                        Color.red(
                            blendThemeColor(
                                accent,
                                white,
                                58
                            )
                        ),
                        Color.green(
                            blendThemeColor(
                                accent,
                                white,
                                58
                            )
                        ),
                        Color.blue(
                            blendThemeColor(
                                accent,
                                white,
                                58
                            )
                        )
                    );

                pressedStart=
                    blendThemeColor(
                        accent,
                        white,
                        45
                    );

                pressedEnd=
                    Color.rgb(
                        236,248,255
                    );

                stroke=
                    Color.argb(
                        235,
                        255,255,255
                    );

                strokeWidth=2;
                break;

            // Galaxy glow.
            case 7:
                normalStart=
                    Color.argb(
                        special || space
                            ? 195
                            : 150,
                        20,15,47
                    );

                normalEnd=
                    Color.argb(
                        145,
                        Color.red(accent),
                        Color.green(accent),
                        Color.blue(accent)
                    );

                pressedStart=
                    Color.argb(
                        225,
                        Color.red(accent),
                        Color.green(accent),
                        Color.blue(accent)
                    );

                pressedEnd=
                    Color.argb(
                        215,
                        25,15,60
                    );

                stroke=
                    Color.argb(
                        240,
                        Color.red(accent),
                        Color.green(accent),
                        Color.blue(accent)
                    );

                strokeWidth=2;
                break;

            // Love / glossy pink.
            case 8:
                normalStart=
                    blendThemeColor(
                        accent,
                        white,
                        special || space
                            ? 35
                            : 68
                    );

                normalEnd=
                    blendThemeColor(
                        accent,
                        Color.rgb(
                            255,210,232
                        ),
                        special || space
                            ? 20
                            : 55
                    );

                pressedStart=
                    blendThemeColor(
                        accent,
                        white,
                        22
                    );

                pressedEnd=
                    blendThemeColor(
                        accent,
                        white,
                        45
                    );

                stroke=
                    Color.argb(
                        220,
                        255,255,255
                    );

                strokeWidth=2;
                break;

            // Porcelain / floral ceramic.
            case 9:
                normalStart=
                    special || space
                        ? blendThemeColor(accent,white,76)
                        : Color.rgb(250,252,255);
                normalEnd=
                    blendThemeColor(accent,white,90);
                pressedStart=
                    blendThemeColor(accent,white,60);
                pressedEnd=
                    Color.rgb(255,255,255);
                stroke=
                    blendThemeColor(accent,black,12);
                strokeWidth=1;
                break;

            // Cafe / teddy / warm stitched.
            case 10:
                normalStart=
                    special || space
                        ? blendThemeColor(accent,white,38)
                        : blendThemeColor(accent,white,68);
                normalEnd=
                    blendThemeColor(accent,white,54);
                pressedStart=
                    blendThemeColor(accent,white,28);
                pressedEnd=
                    blendThemeColor(accent,white,50);
                stroke=
                    blendThemeColor(accent,black,28);
                strokeWidth=1;
                break;

            // Jelly / sky / ocean translucent.
            case 11:
                normalStart=
                    Color.argb(
                        special || space ? 220 : 170,
                        Color.red(blendThemeColor(accent,white,45)),
                        Color.green(blendThemeColor(accent,white,45)),
                        Color.blue(blendThemeColor(accent,white,45))
                    );
                normalEnd=
                    Color.argb(
                        special || space ? 205 : 145,
                        255,255,255
                    );
                pressedStart=
                    Color.argb(
                        235,
                        Color.red(accent),
                        Color.green(accent),
                        Color.blue(accent)
                    );
                pressedEnd=
                    Color.argb(220,255,255,255);
                stroke=Color.WHITE;
                strokeWidth=2;
                break;

            // Lace / diary / soft floral.
            case 12:
                normalStart=
                    blendThemeColor(accent,white,82);
                normalEnd=
                    blendThemeColor(accent,white,67);
                pressedStart=
                    blendThemeColor(accent,white,48);
                pressedEnd=
                    blendThemeColor(accent,white,68);
                stroke=
                    blendThemeColor(accent,white,22);
                strokeWidth=1;
                break;

            // Candy / ribbon / kitty glossy.
            case 13:
                normalStart=
                    blendThemeColor(accent,white,74);
                normalEnd=
                    blendThemeColor(accent,Color.rgb(255,224,240),45);
                pressedStart=
                    blendThemeColor(accent,white,35);
                pressedEnd=
                    blendThemeColor(accent,white,60);
                stroke=Color.WHITE;
                strokeWidth=2;
                break;

            // Winter / frosted snow.
            case 14:
                normalStart=
                    Color.rgb(250,253,255);
                normalEnd=
                    blendThemeColor(accent,white,82);
                pressedStart=
                    blendThemeColor(accent,white,60);
                pressedEnd=
                    Color.rgb(236,248,255);
                stroke=
                    blendThemeColor(accent,white,42);
                strokeWidth=2;
                break;

            default:
                normalStart=
                    keyColor(special);

                normalEnd=
                    keyColor(special);

                pressedStart=
                    accentFillColor();

                pressedEnd=
                    accentFillColor();

                stroke=
                    borders
                        ? borderColor()
                        : Color.TRANSPARENT;
                break;
        }

        GradientDrawable normal=
            new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{
                    normalStart,
                    normalEnd
                }
            );

        normal.setCornerRadius(
            dp(
                keyCornerRadius()
            )
        );

        if(stroke!=Color.TRANSPARENT) {
            normal.setStroke(
                dp(strokeWidth),
                stroke
            );
        }

        GradientDrawable pressed=
            new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{
                    pressedStart,
                    pressedEnd
                }
            );

        pressed.setCornerRadius(
            dp(
                keyCornerRadius()
            )
        );

        if(stroke!=Color.TRANSPARENT) {
            pressed.setStroke(
                dp(strokeWidth),
                stroke
            );
        }

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
