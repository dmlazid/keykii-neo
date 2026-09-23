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
    boolean suppressNextKeyClick=false;

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

    boolean shift=false;
    boolean capsLock=false;
    long lastShiftTap=0L;
    boolean symbols=false;
    boolean floating=true;
    boolean wideMode=false;

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
                        ic.deleteSurroundingText(1,0);
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

        clipboardListener=()->captureClipboard();

        if(clipboardManager!=null)
            clipboardManager
                .addPrimaryClipChangedListener(
                    clipboardListener
                );
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

        theme=getSharedPreferences(
            "keykii_prefs",
            MODE_PRIVATE
        ).getInt("theme",0);

        root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.TRANSPARENT);

        buildShell();

        return root;
    }

    void loadKeyKiiSettings() {

        SharedPreferences p=
            getSharedPreferences(
                "keykii_prefs",
                MODE_PRIVATE
            );

        theme=p.getInt("theme",0);
        keyHeight=p.getInt("key_height",46);
        floatGap=p.getInt("float_gap",96);
        haptic=p.getBoolean("haptic",false);

        wideMode=p.getBoolean(
            "wide_default",
            false
        );

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
        shift=false;
        capsLock=false;
        lastShiftTap=0L;

        if(root!=null)
            buildShell();
    }

    void buildShell() {

        root.removeAllViews();

        if(wideMode)
            hand=0;

        root.setGravity(
            hand==1 ? Gravity.START :
            hand==2 ? Gravity.END :
            Gravity.CENTER_HORIZONTAL
        );

        int sidePadding=
            wideMode ? 3 : 10;

        int bottomPadding=
            wideMode
            ? 52
            : floatGap;

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

        panel.setPadding(
            dp(8),
            dp(5),
            dp(8),
            dp(8)
        );

        panel.setElevation(dp(10));

        panel.setBackground(
            round(
                panelColor(),
                24,
                borderColor()
            )
        );

        DisplayMetrics d=
            getResources()
                .getDisplayMetrics();

        float ratio;

        if(hand!=0)
            ratio=.66f;

        else if(wideMode)
            ratio=.985f;

        else
            ratio=.86f;

        LinearLayout.LayoutParams p=
            new LinearLayout.LayoutParams(
                (int)(d.widthPixels*ratio),
                LinearLayout.LayoutParams.WRAP_CONTENT
            );

        p.gravity=Gravity.CENTER_HORIZONTAL;

        root.addView(panel,p);

        addHandle();
        addToolbar();

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


    void addToolbar() {

        LinearLayout r=
            new LinearLayout(this);

        r.setGravity(Gravity.CENTER);

        tool(r,"⌨",0);
        tool(r,"☺",1);
        tool(r,"▣",2);
        tool(r,"✎",3);
        tool(r,"◐",4);
        tool(r,"↔",5);

        panel.addView(
            r,
            new LinearLayout.LayoutParams(
                -1,
                dp(44)
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

                showPage();

            } else if(action==4) {

                theme=(theme+1)%3;

                getSharedPreferences(
                    "keykii_prefs",
                    MODE_PRIVATE
                ).edit()
                 .putInt("theme",theme)
                 .apply();

                buildShell();

            } else if(action==5) {

                wideMode=!wideMode;
                hand=0;

                getSharedPreferences(
                    "keykii_prefs",
                    MODE_PRIVATE
                ).edit()
                 .putBoolean(
                     "wide_default",
                     wideMode
                 )
                 .apply();

                buildShell();
            }
        });

        r.addView(
            v,
            new LinearLayout.LayoutParams(
                0,
                dp(44),
                1
            )
        );
    }


    void showPage() {

        body.removeAllViews();

        if(page==0)
            buildKeyboard();

        else if(page==1)
            buildEmoji();

        else if(page==2)
            buildClipboard();

        else
            buildEditing();
    }

    void buildKeyboard() {

        if(!symbols) {

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

        // Gboard-style punctuation key. Emoji is still available
        // from the toolbar, so this slot stays useful for typing.
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
            dp(1),
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

        if(label.equals("KeyKii"))
            main.setTextSize(16);

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

        box.setOnTouchListener((v,e)->{

            if(action.equals("SPACE"))
                return handleSpacebarTouch(v,e);

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
                        dismissKeyPreview();
                    }

                    return handleDragChoiceTouch(e);
                }
            }

            if(e.getAction()==MotionEvent.ACTION_DOWN) {
                suppressNextKeyClick=false;
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
                    !special &&
                    !action.equals("SPACE") &&
                    shown!=null &&
                    shown.codePointCount(0,shown.length())==1
                ) {
                    showKeyPreview(v,shown);
                }

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
                repeatBackspaceHandler.postDelayed(
                    repeatBackspaceRunnable,
                    330
                );
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

                if(backspaceRepeating)
                    suppressBackspaceClick=true;

                backspaceRepeating=false;
            }

            return false;
        });

        box.setOnClickListener(v -> {

            if(suppressNextKeyClick) {
                suppressNextKeyClick=false;
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
        });

        int height=dp(keyHeight);

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
            suppressNextKeyClick=true;
            spaceDownX=e.getX();
            spaceCursorDragging=false;
            spacePickerShown=false;
            beginSpaceCursorGesture();

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
            suppressNextKeyClick=true;
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


    java.util.ArrayList<Object>
    makeFastEmojiCategoryRows(String wantedGroup) {

        loadFastEmojiDb();

        java.util.ArrayList<Object> rows=
            new java.util.ArrayList<>();

        if(wantedGroup==null || wantedGroup.isEmpty())
            wantedGroup="Recent emoji";

        if(wantedGroup.equalsIgnoreCase("Recent emoji")) {
            rows.add("Recent Emoji");
            java.util.ArrayList<String> recent=loadFastRecent();

            for(int i=0;i<recent.size();i+=10) {
                java.util.ArrayList<String> row=
                    new java.util.ArrayList<>();
                for(int j=i;j<Math.min(i+10,recent.size());j++)
                    row.add(recent.get(j));
                if(!row.isEmpty()) rows.add(row);
            }
            return rows;
        }

        rows.add(fastGroupLabel(wantedGroup));

        java.util.ArrayList<String> list=
            new java.util.ArrayList<>();
        java.util.HashSet<String> seen=
            new java.util.HashSet<>();

        for(String[] x:fastEmojiDb) {
            if(x.length<3) continue;

            String group=x[0];
            String emoji=x[2];

            if(!group.equalsIgnoreCase(wantedGroup))
                continue;
            if(group.equalsIgnoreCase("Component"))
                continue;
            if(hasSkinToneModifier(emoji))
                continue;
            if(!displayableEmoji(emoji))
                continue;
            if(!seen.add(emoji))
                continue;

            list.add(emoji);
        }

        for(int i=0;i<list.size();i+=10) {
            java.util.ArrayList<String> row=
                new java.util.ArrayList<>();
            for(int j=i;j<Math.min(i+10,list.size());j++)
                row.add(list.get(j));
            if(!row.isEmpty()) rows.add(row);
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

        java.util.ArrayList<Object> rows=makeFastEmojiRows(q);
        java.util.ArrayList<String> results=new java.util.ArrayList<>();

        for(Object item:rows) {
            if(item instanceof java.util.ArrayList) {
                @SuppressWarnings("unchecked")
                java.util.ArrayList<String> row=(java.util.ArrayList<String>)item;
                for(String value:row) {
                    if(!results.contains(value))
                        results.add(value);
                    if(results.size()>=24)
                        break;
                }
            }
            if(results.size()>=24)
                break;
        }

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

                for(int i=0;i<candidate.length();) {
                    int cp=candidate.codePointAt(i);
                    if(cp>=0x1F3FB && cp<=0x1F3FF) {
                        count++;
                        candidateTone=cp;
                    }
                    i+=Character.charCount(cp);
                }

                // Keep the compact six-choice popup. Complex multi-person
                // combinations stay out of this first selector.
                if(
                    count==1 &&
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
                ic.deleteSurroundingText(1,0);
        });

        TextView[] buttons={abc,emoji,gif,sticker,kao,del};
        for(TextView b:buttons) {
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(46),1);
            lp.setMargins(dp(2),dp(3),dp(2),dp(3));
            bar.addView(b,lp);
        }

        body.addView(bar,new LinearLayout.LayoutParams(-1,dp(52)));
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

        search.setText(
            kaomojiMode
            ? "Kaomoji"
            : (
                emojiSearchQuery==null ||
                emojiSearchQuery.isEmpty()
                ? "🔍  Search emoji"
                : "🔍  "+emojiSearchQuery
            )
        );

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

        close.setText("×");
        close.setTextSize(22);
        close.setTextColor(textColor());
        close.setGravity(Gravity.CENTER);
        close.setVisibility(
            (emojiSearchMode || kaomojiMode)
            ? View.VISIBLE
            : View.INVISIBLE
        );

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
            "Recent emoji",
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
            "🕘","😀","🧑","🐻","🍔",
            "⚽","🚗","💡","❤️","🏳️"
        };

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

            if(index==emojiCategory) {
                int selectedColor = theme==1
                    ? Color.rgb(201,218,255)
                    : Color.argb(120,150,180,255);
                b.setBackground(
                    round(
                        selectedColor,
                        20,
                        Color.TRANSPARENT
                    )
                );
            }

            b.setOnClickListener(v -> {

                emojiCategory=index;

                // Category tap exits Search first.
                if(
                    emojiSearchMode ||
                    (
                        emojiSearchQuery!=null &&
                        !emojiSearchQuery.isEmpty()
                    )
                ) {

                    emojiSearchMode=false;
                    emojiSearchQuery="";
                    showPage();
                    return;
                }

                // Rebuild once so the selected category highlight updates,
                // then KEYKII_INITIAL_EMOJI_JUMP moves to the right section.
                showPage();
            });

            cats.addView(
                b,
                new LinearLayout.LayoutParams(
                    dp(48),
                    dp(42)
                )
            );
        }

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
        final int selectedEmojiCategory=emojiCategory;
        hsv.post(() -> hsv.scrollTo(
            Math.max(0, selectedEmojiCategory*dp(48)-dp(72)),
            0
        ));


        final java.util.ArrayList<Object> rows=
            makeFastEmojiCategoryRows(
                groups[Math.max(0,Math.min(emojiCategory,groups.length-1))]
            );

        fastEmojiList=
            new android.widget.ListView(this);

        fastEmojiList.setDivider(null);

        fastEmojiList.setVerticalScrollBarEnabled(
            true
        );

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

                                return false;
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

        // Category rows are already filtered, so no delayed jump is needed.

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



    void refreshEmojiSearchField() {
        if(emojiSearchField==null) return;
        emojiSearchField.setText(
            emojiSearchQuery==null || emojiSearchQuery.isEmpty()
            ? "🔍  Search emoji"
            : "🔍  "+emojiSearchQuery
        );
    }

    void eraseEmojiSearchChar() {
        if(emojiSearchQuery==null || emojiSearchQuery.isEmpty()) return;
        int end=emojiSearchQuery.length();
        int start=emojiSearchQuery.offsetByCodePoints(end,-1);
        emojiSearchQuery=emojiSearchQuery.substring(0,start);
        refreshEmojiSearchField();
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
            emojiSearchQuery+=" ";
            refreshEmojiSearchField();
            refreshEmojiSearchResults();
            return true;
        }
        if(action.length()==1) {
            boolean shifted=shift && !symbols;
            String value=shifted ? action.toUpperCase() : action;
            emojiSearchQuery+=value;

            if(shifted && !capsLock) {
                shift=false;
                lastShiftTap=0L;
                showPage();
            } else {
                refreshEmojiSearchField();
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

            if(i==kaomojiCategory) {

                tab.setBackground(
                    round(
                        keyColor(false),
                        18,
                        borderColor()
                    )
                );
            }

            tab.setOnClickListener(v -> {

                kaomojiCategory=index;
                showPage();
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
        final int selectedKaomojiCategory=kaomojiCategory;
        tabsScroll.post(() -> tabsScroll.scrollTo(
            Math.max(0, selectedKaomojiCategory*dp(118)-dp(42)),
            0
        ));


        final String category=
            fullKaomojiCategories.get(
                kaomojiCategory
            );

        final java.util.ArrayList<String>
            faces=
            fullKaomoji.get(category);


        android.widget.ListView list=
            new android.widget.ListView(this);

        list.setDivider(null);

        list.setVerticalScrollBarEnabled(
            true
        );


        list.setAdapter(
            new android.widget.BaseAdapter() {

                final int columns=2;

                public int getCount() {

                    return
                        (faces.size()+columns-1)
                        /columns;
                }

                public Object getItem(int p) {
                    return null;
                }

                public long getItemId(int p) {
                    return p;
                }

                public View getView(
                    int position,
                    View convertView,
                    android.view.ViewGroup parent
                ) {

                    LinearLayout row=
                        new LinearLayout(
                            KeyKiiService.this
                        );

                    row.setGravity(
                        Gravity.CENTER
                    );

                    for(int c=0;c<columns;c++) {

                        int index=
                            position*columns+c;

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


                        if(index<faces.size()) {

                            final String value=
                                faces.get(index);

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
    }


    void buildClipboard() {

        body.addView(
            title("Clipboard"),
            new LinearLayout.LayoutParams(
                -1,
                dp(28)
            )
        );

        ClipboardManager cm=
            (ClipboardManager)
            getSystemService(
                CLIPBOARD_SERVICE
            );

        if(
            cm!=null &&
            cm.hasPrimaryClip()
        ){

            ClipData clip=
                cm.getPrimaryClip();

            if(
                clip!=null &&
                clip.getItemCount()>0
            ){

                CharSequence cs=
                    clip.getItemAt(0)
                        .coerceToText(this);

                if(cs!=null)
                    rememberClip(
                        cs.toString()
                    );
            }
        }

        SharedPreferences sp=
            getSharedPreferences(
                "keykii_clipboard",
                MODE_PRIVATE
            );

        boolean found=false;

        for(int n=0;n<6;n++){

            String text=
                sp.getString(
                    "clip"+n,
                    ""
                );

            if(text.isEmpty())
                continue;

            found=true;

            String preview=
                text.length()>42
                ? text.substring(0,42)+"…"
                : text;

            TextView item=
                title(preview);

            item.setBackground(
                round(
                    keyColor(false),
                    14,
                    borderColor()
                )
            );

            final String pasteText=text;

            item.setOnClickListener(v -> {

                InputConnection ic=
                    getCurrentInputConnection();

                if(ic!=null)
                    ic.commitText(
                        pasteText,
                        1
                    );
            });

            LinearLayout.LayoutParams p=
                new LinearLayout.LayoutParams(
                    -1,
                    dp(36)
                );

            p.setMargins(
                dp(5),
                dp(3),
                dp(5),
                dp(3)
            );

            body.addView(item,p);
        }

        if(!found){

            body.addView(
                title("Nothing copied yet"),
                new LinearLayout.LayoutParams(
                    -1,
                    dp(38)
                )
            );
        }
    }

    void rememberClip(String text){

        if(
            text==null ||
            text.trim().isEmpty()
        ) return;

        SharedPreferences sp=
            getSharedPreferences(
                "keykii_clipboard",
                MODE_PRIVATE
            );

        if(
            text.equals(
                sp.getString(
                    "clip0",
                    ""
                )
            )
        ) return;

        SharedPreferences.Editor e=
            sp.edit();

        for(int n=5;n>0;n--){

            e.putString(
                "clip"+n,
                sp.getString(
                    "clip"+(n-1),
                    ""
                )
            );
        }

        e.putString(
            "clip0",
            text
        );

        e.apply();
    }

    void buildEditing() {

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
                "Select","Copy","Paste","Cut"
            },
            new String[]{
                "SELECT","COPY","PASTE","CUT"
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
                        theme==1
                        ? Color.rgb(221,226,239)
                        : Color.argb(90,120,150,220),
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
                if(wideMode) wideMode=false;
                hand=(hand==0) ? 1 : (hand==1 ? 2 : 0);
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
            suppressNextKeyClick=true;
            return true;
        }

        if(action==MotionEvent.ACTION_CANCEL) {
            dismissDragChoicePopup();
            suppressNextKeyClick=true;
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
        suppressNextKeyClick=true;

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


    void press(String action) {

        if(handleEmojiSearchKey(action))
            return;

        InputConnection i=
            getCurrentInputConnection();

        if(i==null) return;

        switch(action) {

            case "BACK":
                i.deleteSurroundingText(1,0);
                break;

            case "SPACE":
                i.commitText(" ",1);
                break;

            case "ENTER":
                enter(i);
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
                showPage();
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

    int panelColor() {

        if(theme==1)
            return Color.argb(
                235,247,245,242
            );

        if(theme==2)
            return Color.argb(
                145,30,32,37
            );

        return Color.argb(
            220,35,37,42
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

        if(theme==1)
            return Color.rgb(
                239,232,221
            );

        return Color.argb(
            145,235,239,242
        );
    }

    int textColor() {

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

        int normalColor=
            space
            ? spaceColor()
            : keyColor(special);

        int pressedColor=
            theme==1
            ? Color.rgb(236,231,226)
            : Color.argb(
                175,245,245,247
            );

        GradientDrawable normal=
            round(
                normalColor,
                15,
                borderColor()
            );

        GradientDrawable pressed=
            round(
                pressedColor,
                15,
                borderColor()
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
