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

    LinearLayout root, panel, body;

    ClipboardManager clipboardManager;
    ClipboardManager.OnPrimaryClipChangedListener clipboardListener;

    boolean shift=false;
    boolean symbols=false;
    boolean floating=true;

    int symbolPage=1;
    int page=0;
    int theme=0;
    int hand=0;
    int emojiCategory=0;
    int keyHeight=46;
    boolean haptic=false;

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

    void buildShell() {

        root.removeAllViews();

        root.setGravity(
            hand==1 ? Gravity.START :
            hand==2 ? Gravity.END :
            Gravity.CENTER_HORIZONTAL
        );

        // Higher floating position
        root.setPadding(
            dp(12),
            dp(4),
            dp(12),
            dp(floating ? 96 : 72)
        );

        panel=new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(
            dp(9),
            dp(6),
            dp(9),
            dp(9)
        );

        panel.setElevation(dp(10));

        panel.setBackground(
            round(
                panelColor(),
                25,
                borderColor()
            )
        );

        DisplayMetrics d=
            getResources().getDisplayMetrics();

        float ratio;

        if(hand!=0)
            ratio=.62f;
        else if(floating)
            ratio=.76f;
        else
            ratio=.92f;

        LinearLayout.LayoutParams p=
            new LinearLayout.LayoutParams(
                (int)(d.widthPixels*ratio),
                LinearLayout.LayoutParams.WRAP_CONTENT
            );

        p.gravity=
            hand==1 ? Gravity.START :
            hand==2 ? Gravity.END :
            Gravity.CENTER_HORIZONTAL;

        root.addView(panel,p);

        addHandle();
        addToolbar();

        body=new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);

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

        LinearLayout r=new LinearLayout(this);
        r.setGravity(Gravity.CENTER);

        View v=new View(this);

        v.setBackground(
            round(
                Color.argb(115,255,255,255),
                4,
                Color.TRANSPARENT
            )
        );

        LinearLayout.LayoutParams p=
            new LinearLayout.LayoutParams(
                dp(40),
                dp(3)
            );

        p.setMargins(
            0,0,0,dp(5)
        );

        r.addView(v,p);
        panel.addView(r);
    }

    void addToolbar() {

        LinearLayout r=new LinearLayout(this);
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
                dp(30)
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
        v.setTextSize(15);
        v.setGravity(Gravity.CENTER);

        v.setOnClickListener(x -> {

            if(action<=3) {

                page=action;
                showPage();

            } else if(action==4) {

                theme=(theme+1)%3;
                buildShell();

            } else if(action==5) {

                // Real Compact / Wide switch
                floating=!floating;
                hand=0;
                buildShell();
            }
        });

        r.addView(
            v,
            new LinearLayout.LayoutParams(
                0,
                dp(28),
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
            .68f,
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

        FrameLayout box=new FrameLayout(this);

        TextView main=new TextView(this);

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
            main.setTextSize(19);

        boolean space=
            action.equals("SPACE");

        box.setBackground(
            keyBackground(
                special,
                space
            )
        );

        FrameLayout.LayoutParams mainParams=
            new FrameLayout.LayoutParams(
                -1,
                -1
            );

        box.addView(main,mainParams);

        /*
         * Small Gboard-style symbol hint.
         */
        String hint=
            (!symbols && page==0)
            ? hintFor(action)
            : "";

        if(!hint.isEmpty()){

            TextView small=
                new TextView(this);

            small.setText(hint);
            small.setTextColor(textColor());
            small.setAlpha(.62f);
            small.setTextSize(9);
            small.setGravity(Gravity.CENTER);

            FrameLayout.LayoutParams hp=
                new FrameLayout.LayoutParams(
                    dp(18),
                    dp(15),
                    Gravity.TOP | Gravity.RIGHT
                );

            hp.setMargins(
                0,
                dp(2),
                dp(4),
                0
            );

            box.addView(small,hp);

            /*
             * Long press inserts the small symbol.
             */
            box.setOnLongClickListener(v -> {

                InputConnection ic=
                    getCurrentInputConnection();

                if(ic!=null)
                    ic.commitText(
                        hint,
                        1
                    );

                return true;
            });
        }

        box.setOnTouchListener((v,e)->{

            if(e.getAction()==
               MotionEvent.ACTION_DOWN){

                if(haptic)
                    v.performHapticFeedback(
                        HapticFeedbackConstants.KEYBOARD_TAP
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
                 .setDuration(55)
                 .start();
            }

            return false;
        });

        box.setOnClickListener(
            v -> press(action)
        );

        int height=
            label.equals("KeyKii")
            ? dp(keyHeight+5)
            : dp(keyHeight);

        LinearLayout.LayoutParams p=
            new LinearLayout.LayoutParams(
                0,
                height,
                weight
            );

        p.setMargins(
            dp(3),
            dp(4),
            dp(3),
            dp(4)
        );

        r.addView(box,p);
    }

    void buildEmoji() {

        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);

        LinearLayout wrap=new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(
            dp(4),
            dp(3),
            dp(4),
            dp(8)
        );

        // RECENT EMOJI
        String recent=
            getSharedPreferences(
                "keykii_emoji",
                MODE_PRIVATE
            ).getString(
                "recent",
                ""
            );

        if(!recent.trim().isEmpty()) {

            java.util.ArrayList<String> recentList=
                new java.util.ArrayList<>();

            for(String e:recent.trim().split(" ")) {
                if(!e.isEmpty())
                    recentList.add(e);
            }

            addEmojiSection(
                wrap,
                "Recent emoji",
                recentList
            );
        }

        // LOAD COMPLETE UNICODE EMOJI DATABASE
        java.util.LinkedHashMap<
            String,
            java.util.ArrayList<String>
        > groups=
            loadEmojiDatabase();

        for(
            java.util.Map.Entry<
                String,
                java.util.ArrayList<String>
            > entry : groups.entrySet()
        ) {

            addEmojiSection(
                wrap,
                prettyEmojiGroup(
                    entry.getKey()
                ),
                entry.getValue()
            );
        }

        scroll.addView(wrap);

        body.addView(
            scroll,
            new LinearLayout.LayoutParams(
                -1,
                dp(285)
            )
        );
    }


    java.util.LinkedHashMap<
        String,
        java.util.ArrayList<String>
    > loadEmojiDatabase() {

        java.util.LinkedHashMap<
            String,
            java.util.ArrayList<String>
        > result=
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
            ) {

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
                    emoji.isEmpty()
                )
                    continue;

                // Unicode components aren't useful
                // as their own visible emoji section.
                if(
                    group.equalsIgnoreCase(
                        "Component"
                    )
                )
                    continue;

                if(!result.containsKey(group)) {

                    result.put(
                        group,
                        new java.util.ArrayList<String>()
                    );
                }

                result.get(group)
                      .add(emoji);
            }

            reader.close();

        } catch(Exception e) {

            java.util.ArrayList<String> fallback=
                new java.util.ArrayList<>();

            String basic=
                "😭 😂 🥹 🤣 ❤️ 😊 😍 🥰 😘 " +
                "😀 😃 😄 😁 😆 😅 🙂 🙃 😉 " +
                "😎 🤩 🥳 😡 🤬 😱 😴 🤔 🙄 " +
                "👍 👎 👏 🙌 🙏 💪 🔥 ✨ 🎉";

            for(String emoji:basic.split(" "))
                fallback.add(emoji);

            result.put(
                "Smileys & Emotion",
                fallback
            );
        }

        return result;
    }


    void addEmojiSection(
        LinearLayout wrap,
        String section,
        java.util.ArrayList<String> emojis
    ) {

        if(
            emojis==null ||
            emojis.isEmpty()
        )
            return;

        TextView heading=
            new TextView(this);

        heading.setText(section);
        heading.setTextColor(
            textColor()
        );
        heading.setTextSize(13);
        heading.setGravity(
            Gravity.CENTER_VERTICAL
        );

        heading.setPadding(
            dp(7),
            dp(8),
            dp(4),
            dp(5)
        );

        wrap.addView(
            heading,
            new LinearLayout.LayoutParams(
                -1,
                dp(36)
            )
        );

        for(
            int i=0;
            i<emojis.size();
            i+=7
        ) {

            LinearLayout row=
                new LinearLayout(this);

            row.setOrientation(
                LinearLayout.HORIZONTAL
            );

            row.setGravity(
                Gravity.CENTER
            );

            for(
                int j=i;
                j<Math.min(
                    i+7,
                    emojis.size()
                );
                j++
            ) {

                final String emoji=
                    emojis.get(j);

                TextView button=
                    new TextView(this);

                button.setText(emoji);
                button.setTextSize(24);
                button.setGravity(
                    Gravity.CENTER
                );

                button.setOnClickListener(v -> {

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

                LinearLayout.LayoutParams ep=
                    new LinearLayout.LayoutParams(
                        0,
                        dp(47),
                        1
                    );

                ep.setMargins(
                    dp(1),
                    dp(1),
                    dp(1),
                    dp(1)
                );

                row.addView(
                    button,
                    ep
                );
            }

            // Fill empty spaces in last row.
            int missing=
                7-
                Math.min(
                    7,
                    emojis.size()-i
                );

            for(
                int x=0;
                x<missing;
                x++
            ) {

                Space blank=
                    new Space(this);

                row.addView(
                    blank,
                    new LinearLayout.LayoutParams(
                        0,
                        dp(47),
                        1
                    )
                );
            }

            wrap.addView(row);
        }
    }


    String prettyEmojiGroup(
        String group
    ) {

        if(
            group.equals(
                "Smileys & Emotion"
            )
        )
            return "Smileys and emotions";

        if(
            group.equals(
                "People & Body"
            )
        )
            return "People and body";

        if(
            group.equals(
                "Animals & Nature"
            )
        )
            return "Animals and nature";

        if(
            group.equals(
                "Food & Drink"
            )
        )
            return "Food and drink";

        if(
            group.equals(
                "Travel & Places"
            )
        )
            return "Travel and places";

        return group;
    }


    void rememberEmoji(String emoji) {

        if(
            emoji==null ||
            emoji.trim().isEmpty()
        ) return;

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

        java.util.ArrayList<String> list=
            new java.util.ArrayList<>();

        // Put newest emoji first
        list.add(emoji);

        if(old!=null && !old.trim().isEmpty()) {

            for(String e:old.trim().split(" ")) {

                if(
                    !e.isEmpty() &&
                    !e.equals(emoji) &&
                    !list.contains(e)
                ) {
                    list.add(e);
                }

                if(list.size()>=28)
                    break;
            }
        }

        StringBuilder result=
            new StringBuilder();

        for(String e:list) {

            if(result.length()>0)
                result.append(" ");

            result.append(e);
        }

        prefs.edit()
            .putString(
                "recent",
                result.toString()
            )
            .apply();
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
