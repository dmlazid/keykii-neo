package com.keykii.neo;

import android.graphics.Color;
import android.graphics.drawable.*;
import android.inputmethodservice.InputMethodService;
import android.util.DisplayMetrics;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;

public class KeyKiiService extends InputMethodService {

    LinearLayout panel;
    boolean shift=false, symbols=false;

    int PANEL=Color.argb(220,35,37,42);
    int KEY=Color.argb(115,235,235,238);
    int SPECIAL=Color.argb(75,235,235,238);
    int PRESS=Color.argb(170,245,245,247);
    int WHITE=Color.WHITE;

    @Override
    public View onCreateInputView(){

        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundColor(Color.TRANSPARENT);
        root.setPadding(dp(12),dp(5),dp(12),dp(32));

        panel=new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(9),dp(6),dp(9),dp(9));

        GradientDrawable bg=new GradientDrawable();
        bg.setColor(PANEL);
        bg.setCornerRadius(dp(25));
        bg.setStroke(dp(1),Color.argb(55,255,255,255));
        panel.setBackground(bg);
        panel.setElevation(dp(10));

        DisplayMetrics d=getResources().getDisplayMetrics();

        LinearLayout.LayoutParams p=
            new LinearLayout.LayoutParams(
                (int)(d.widthPixels*.78f),
                LinearLayout.LayoutParams.WRAP_CONTENT);

        root.addView(panel,p);
        build();
        return root;
    }

    void build(){
        panel.removeAllViews();
        handle();

        if(symbols){
            row(new String[]{"1","2","3","4","5","6","7","8","9","0"});
            row(new String[]{"@","#","$","%","&","-","+","(",")","/"});
            third(new String[]{"*","\"","'",":",";","!","?"});
        }else{
            row(new String[]{"q","w","e","r","t","y","u","i","o","p"});
            centerRow(new String[]{"a","s","d","f","g","h","j","k","l"});
            third(new String[]{"z","x","c","v","b","n","m"});
        }

        bottom();
    }

    void handle(){
        LinearLayout r=new LinearLayout(this);
        r.setGravity(Gravity.CENTER);

        View v=new View(this);

        GradientDrawable g=new GradientDrawable();
        g.setColor(Color.argb(120,255,255,255));
        g.setCornerRadius(dp(5));
        v.setBackground(g);

        LinearLayout.LayoutParams p=
            new LinearLayout.LayoutParams(dp(42),dp(3));

        p.setMargins(0,0,0,dp(7));
        r.addView(v,p);
        panel.addView(r);
    }

    void row(String[] a){
        LinearLayout r=newRow();

        for(String s:a)
            key(r,s,s,1,false);

        panel.addView(r);
    }

    void centerRow(String[] a){
        LinearLayout r=newRow();

        spacer(r,.45f);

        for(String s:a)
            key(r,s,s,1,false);

        spacer(r,.45f);
        panel.addView(r);
    }

    void third(String[] a){
        LinearLayout r=newRow();

        key(r,symbols?"ABC":"⇧",
            symbols?"ABC":"SHIFT",1.05f,true);

        for(String s:a)
            key(r,s,s,1,false);

        key(r,"⌫","BACK",1.05f,true);

        panel.addView(r);
    }

    void bottom(){
        LinearLayout r=newRow();

        key(r,symbols?"ABC":"123",
            symbols?"ABC":"123",1,true);

        key(r,"☺","EMOJI",.7f,true);

        key(r,"KeyKii","SPACE",3.2f,false);

        key(r,"return","ENTER",1.15f,true);

        panel.addView(r);
    }

    LinearLayout newRow(){
        LinearLayout r=new LinearLayout(this);
        r.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams p=
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        p.setMargins(0,dp(1),0,dp(1));
        r.setLayoutParams(p);

        return r;
    }

    void spacer(LinearLayout r,float w){
        View v=new View(this);

        r.addView(v,
            new LinearLayout.LayoutParams(
                0,dp(30),w));
    }

    void key(LinearLayout r,String text,
             String action,float weight,
             boolean special){

        TextView k=new TextView(this);

        k.setText(
            shift && !symbols && text.length()==1
                ? text.toUpperCase():text);

        k.setTextColor(WHITE);
        k.setGravity(Gravity.CENTER);
        k.setIncludeFontPadding(false);
        k.setTextSize(
            text.equals("KeyKii")?14:
            text.length()>1?12:15);

        GradientDrawable normal=new GradientDrawable();
        normal.setColor(special?SPECIAL:KEY);
        normal.setCornerRadius(dp(15));
        normal.setStroke(
            dp(1),
            Color.argb(28,255,255,255));

        GradientDrawable pressed=new GradientDrawable();
        pressed.setColor(PRESS);
        pressed.setCornerRadius(dp(15));

        StateListDrawable state=new StateListDrawable();

        state.addState(
            new int[]{android.R.attr.state_pressed},
            pressed);

        state.addState(new int[]{},normal);

        k.setBackground(state);

        // Visual animation only. NO vibration.
        k.setOnTouchListener((v,e)->{

            if(e.getAction()==MotionEvent.ACTION_DOWN)
                v.animate()
                    .scaleX(.94f)
                    .scaleY(.94f)
                    .setDuration(40).start();

            if(e.getAction()==MotionEvent.ACTION_UP ||
               e.getAction()==MotionEvent.ACTION_CANCEL)
                v.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(60).start();

            return false;
        });

        k.setOnClickListener(v->press(action));

        LinearLayout.LayoutParams p=
            new LinearLayout.LayoutParams(
                0,
                text.equals("KeyKii")?dp(34):dp(30),
                weight);

        p.setMargins(dp(3),dp(3),dp(3),dp(3));

        r.addView(k,p);
    }

    void press(String s){

        InputConnection i=getCurrentInputConnection();

        if(i==null)return;

        switch(s){

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
                build();
                break;

            case "123":
                symbols=true;
                shift=false;
                build();
                break;

            case "ABC":
                symbols=false;
                shift=false;
                build();
                break;

            case "EMOJI":
                i.commitText("☺",1);
                break;

            default:

                String out=
                    shift && !symbols
                        ? s.toUpperCase():s;

                i.commitText(out,1);

                if(shift && !symbols){
                    shift=false;
                    build();
                }
        }
    }

    void enter(InputConnection i){

        EditorInfo e=getCurrentInputEditorInfo();

        if(e!=null){

            int a=e.imeOptions &
                  EditorInfo.IME_MASK_ACTION;

            if(a!=EditorInfo.IME_ACTION_NONE &&
               a!=EditorInfo.IME_ACTION_UNSPECIFIED){

                i.performEditorAction(a);
                return;
            }
        }

        i.sendKeyEvent(
            new KeyEvent(
                KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_ENTER));

        i.sendKeyEvent(
            new KeyEvent(
                KeyEvent.ACTION_UP,
                KeyEvent.KEYCODE_ENTER));
    }

    int dp(int n){
        return Math.round(
            n*getResources()
                .getDisplayMetrics().density);
    }
}
