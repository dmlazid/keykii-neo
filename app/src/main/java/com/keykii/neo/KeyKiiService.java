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



    LinearLayout root, panel, body;

    ClipboardManager clipboardManager;
    ClipboardManager.OnPrimaryClipChangedListener clipboardListener;

    boolean shift=false;
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

                InputConnection ic=
                    getCurrentInputConnection();

                if(ic!=null)
                    ic.deleteSurroundingText(
                        1,
                        0
                    );

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
                dp(46)
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

            row(new String[]{
                "1","2","3","4","5",
                "6","7","8","9","0"
            });

            row(new String[]{
                "@","#","$","%","&",
                "-","+","(",")","/"
            });

            third(new String[]{
                "*","\"","'",":",
                ";","!","?"
            });

        } else {

            row(new String[]{
                "~","`","|","•","√",
                "π","÷","×","§","∆"
            });

            row(new String[]{
                "£","€","¥","¢","^",
                "°","=","{","}","\\"
            });

            third(new String[]{
                "[","]","<",">",
                "_","…","±"
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
                "⇧",
                "SHIFT",
                1.05f,
                true
            );

        } else if(symbolPage==1) {

            key(
                r,
                "#+=",
                "SYM2",
                1.05f,
                true
            );

        } else {

            key(
                r,
                "123",
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

        key(
            r,
            symbols ? "ABC" : "123",
            symbols ? "ABC" : "123",
            1f,
            true
        );

        key(
            r,
            "☺",
            "EMOJI",
            .85f,
            true
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

            box.setOnLongClickListener(v -> {

                String choices=
                    alternativesFor(action);

                if(!choices.isEmpty()) {

                    showLongPressPopup(
                        v,
                        choices
                    );
                }

                return true;
            });
        }

        box.setOnTouchListener((v,e)->{

            if(
                action.equals("BACK") &&
                e.getAction()==
                MotionEvent.ACTION_DOWN
            ){

                backspaceRepeating=false;
                suppressBackspaceClick=false;

                repeatBackspaceHandler
                    .postDelayed(
                        repeatBackspaceRunnable,
                        330
                    );
            }

            if(
                action.equals("BACK") &&
                (
                    e.getAction()==
                    MotionEvent.ACTION_UP ||
                    e.getAction()==
                    MotionEvent.ACTION_CANCEL
                )
            ){

                repeatBackspaceHandler
                    .removeCallbacks(
                        repeatBackspaceRunnable
                    );

                if(backspaceRepeating)
                    suppressBackspaceClick=true;

                backspaceRepeating=false;
            }

            if(
                e.getAction()==
                MotionEvent.ACTION_DOWN
            ){

                if(haptic)
                    v.performHapticFeedback(
                        HapticFeedbackConstants
                            .KEYBOARD_TAP
                    );

                v.animate()
                 .scaleX(.96f)
                 .scaleY(.96f)
                 .setDuration(35)
                 .start();
            }

            if(
                e.getAction()==
                MotionEvent.ACTION_UP ||
                e.getAction()==
                MotionEvent.ACTION_CANCEL
            ){

                v.animate()
                 .scaleX(1f)
                 .scaleY(1f)
                 .setDuration(50)
                 .start();
            }

            return false;
        });

        box.setOnClickListener(v -> {

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
            dp(2),
            dp(3),
            dp(2),
            dp(3)
        );

        r.addView(box,p);
    }


    void buildEmoji() {

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
                    searchable.contains(q) &&
                    !result.contains(emoji)
                ) {
                    result.add(emoji);
                }
            }

            r.close();

        } catch(Exception ignored) {}

        return result;
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

            default: return "";
        }
    }

    String alternativesFor(String s) {

        switch(s) {

            case "q": return "1";
            case "w": return "2";
            case "e": return "é|è|ê|ë|ē|3";
            case "r": return "4";
            case "t": return "5";
            case "y": return "ý|ÿ|6";

            case "u":
                return "ú|ù|û|ü|ū|7";

            case "i":
                return "í|ì|î|ï|ī|8";

            case "o":
                return "ó|ò|ô|ö|õ|ø|ō|9";

            case "p": return "0";

            case "a":
                return "á|à|â|ä|ã|å|æ|@";

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

            default:
                return "";
        }
    }


    void showLongPressPopup(
        View anchor,
        String choices
    ) {

        String[] items=choices.split("\\|");

        LinearLayout row=new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);

        row.setPadding(
            dp(5),
            dp(5),
            dp(5),
            dp(5)
        );

        row.setBackground(
            round(
                panelColor(),
                18,
                borderColor()
            )
        );

        final PopupWindow popup=
            new PopupWindow(
                row,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
            );

        for(String item:items) {

            final String value=item;

            TextView key=new TextView(this);

            key.setText(value);
            key.setTextSize(19);
            key.setTextColor(textColor());
            key.setGravity(Gravity.CENTER);

            key.setBackground(
                round(
                    keyColor(false),
                    12,
                    borderColor()
                )
            );

            key.setOnClickListener(v -> {

                InputConnection ic=
                    getCurrentInputConnection();

                if(ic!=null)
                    ic.commitText(value,1);

                popup.dismiss();
            });

            LinearLayout.LayoutParams lp=
                new LinearLayout.LayoutParams(
                    dp(44),
                    dp(44)
                );

            lp.setMargins(
                dp(2),
                dp(2),
                dp(2),
                dp(2)
            );

            row.addView(key,lp);
        }

        popup.setOutsideTouchable(true);

        popup.setBackgroundDrawable(
            new android.graphics.drawable.ColorDrawable(
                Color.TRANSPARENT
            )
        );

        popup.setElevation(dp(10));
        popup.setClippingEnabled(false);

        row.measure(
            View.MeasureSpec.UNSPECIFIED,
            View.MeasureSpec.UNSPECIFIED
        );

        int popupWidth=row.getMeasuredWidth();

        int xOffset=
            anchor.getWidth()/2
            - popupWidth/2;

        int yOffset=
            -anchor.getHeight()
            -row.getMeasuredHeight()
            -dp(8);

        popup.showAsDropDown(
            anchor,
            xOffset,
            yOffset
        );
    }


    void press(String action) {

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
                shift=!shift;
                showPage();
                break;

            case "123":
                symbols=true;
                symbolPage=1;
                shift=false;
                showPage();
                break;

            case "ABC":
                symbols=false;
                symbolPage=1;
                shift=false;
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

                if(shift && !symbols) {
                    shift=false;
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
