package com.keykii.neo;

import android.app.Activity;
import android.content.*;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

public class SettingsActivity extends Activity {

    LinearLayout root;
    SharedPreferences prefs;

    @Override
    public void onCreate(Bundle b){

        super.onCreate(b);

        prefs=getSharedPreferences(
            "keykii_prefs",
            MODE_PRIVATE
        );

        build();
    }

    void build(){

        ScrollView scroll=
            new ScrollView(this);

        root=new LinearLayout(this);
        root.setOrientation(
            LinearLayout.VERTICAL
        );

        root.setPadding(
            dp(24),
            dp(30),
            dp(24),
            dp(40)
        );

        root.setBackgroundColor(
            Color.rgb(247,245,242)
        );

        TextView title=text(
            "Keyboard settings",
            30
        );

        root.addView(title);

        addSection("Layout");

        addSwitch(
            "Start with wide keyboard",
            "wide_default",
            false
        );

        addSwitch(
            "Key vibration",
            "haptic",
            false
        );

        addSection("Key size");

        addSlider(
            "Key height",
            "key_height",
            28,
            24,
            40
        );

        addSection("Floating position");

        addSlider(
            "Height above bottom",
            "float_gap",
            96,
            50,
            150
        );

        addSection("Clipboard");

        TextView clear=
            button("Clear clipboard history");

        clear.setOnClickListener(v->{

            getSharedPreferences(
                "keykii_clipboard",
                MODE_PRIVATE
            ).edit().clear().apply();

            Toast.makeText(
                this,
                "Clipboard history cleared",
                Toast.LENGTH_SHORT
            ).show();
        });

        root.addView(clear);

        scroll.addView(root);
        setContentView(scroll);
    }

    void addSection(String name){

        Space space=new Space(this);

        root.addView(
            space,
            new LinearLayout.LayoutParams(
                1,
                dp(22)
            )
        );

        root.addView(
            text(name,20)
        );
    }

    void addSwitch(
        String label,
        String key,
        boolean def
    ){

        Switch sw=new Switch(this);

        sw.setText(label);
        sw.setTextSize(16);

        sw.setChecked(
            prefs.getBoolean(
                key,
                def
            )
        );

        sw.setPadding(
            dp(12),
            0,
            dp(12),
            0
        );

        sw.setOnCheckedChangeListener(
            (button,on)->

            prefs.edit()
                .putBoolean(
                    key,
                    on
                )
                .apply()
        );

        LinearLayout.LayoutParams p=
            new LinearLayout.LayoutParams(
                -1,
                dp(56)
            );

        p.setMargins(
            0,
            dp(5),
            0,
            dp(5)
        );

        root.addView(sw,p);
    }

    void addSlider(
        String label,
        String key,
        int def,
        int min,
        int max
    ){

        TextView title=
            text(label,16);

        root.addView(title);

        SeekBar bar=
            new SeekBar(this);

        bar.setMax(max-min);

        bar.setProgress(
            prefs.getInt(
                key,
                def
            )-min
        );

        bar.setOnSeekBarChangeListener(
            new SeekBar.OnSeekBarChangeListener(){

                public void onProgressChanged(
                    SeekBar b,
                    int progress,
                    boolean user
                ){

                    prefs.edit()
                        .putInt(
                            key,
                            min+progress
                        )
                        .apply();
                }

                public void onStartTrackingTouch(
                    SeekBar b
                ){}

                public void onStopTrackingTouch(
                    SeekBar b
                ){}
            }
        );

        root.addView(bar);
    }

    TextView button(String value){

        TextView t=text(value,16);

        t.setGravity(Gravity.CENTER);

        GradientDrawable g=
            new GradientDrawable();

        g.setColor(Color.WHITE);
        g.setCornerRadius(dp(18));

        g.setStroke(
            dp(1),
            Color.rgb(225,220,214)
        );

        t.setBackground(g);

        t.setLayoutParams(
            new LinearLayout.LayoutParams(
                -1,
                dp(56)
            )
        );

        return t;
    }

    TextView text(
        String value,
        int size
    ){

        TextView t=
            new TextView(this);

        t.setText(value);
        t.setTextSize(size);

        t.setTextColor(
            Color.rgb(45,45,45)
        );

        return t;
    }

    int dp(int n){

        return Math.round(
            n*
            getResources()
                .getDisplayMetrics()
                .density
        );
    }
}
