package com.keykii.neo;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;

public class SettingsActivity extends Activity {

    SharedPreferences prefs;
    LinearLayout root;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        prefs=getSharedPreferences(
                "keykii_prefs",
                MODE_PRIVATE
        );

        ScrollView scroll=new ScrollView(this);

        root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(
                dp(24),
                dp(30),
                dp(24),
                dp(50)
        );

        root.setBackgroundColor(
                Color.rgb(247,245,242)
        );

        addTitle("Keyboard settings");

        addHeading("Layout");

        addSwitch(
                "Start in wide mode",
                "wide_default",
                false
        );

        addHeading("Typing");

        addSwitch(
                "Key vibration",
                "haptic",
                false
        );

        addHeading("Key size");

        addSlider(
                "Key height",
                "key_height",
                46,
                34,
                60
        );

        addHeading("Floating keyboard");

        addSlider(
                "Distance from bottom",
                "float_gap",
                96,
                50,
                160
        );

        addHeading("Clipboard");

        Button clear=new Button(this);
        clear.setText("Clear clipboard history");

        clear.setOnClickListener(v -> {
            getSharedPreferences(
                    "keykii_clipboard",
                    MODE_PRIVATE
            ).edit().clear().apply();

            Toast.makeText(
                    this,
                    "Clipboard cleared",
                    Toast.LENGTH_SHORT
            ).show();
        });

        root.addView(clear);

        addHeading("Tip");

        TextView note=new TextView(this);
        note.setText(
                "Close and reopen the keyboard after changing layout settings."
        );
        note.setTextSize(14);
        note.setTextColor(Color.DKGRAY);

        root.addView(note);

        scroll.addView(root);
        setContentView(scroll);
    }

    void addTitle(String value) {
        TextView t=new TextView(this);
        t.setText(value);
        t.setTextSize(30);
        t.setTextColor(Color.rgb(45,45,45));
        root.addView(t);
    }

    void addHeading(String value) {
        Space space=new Space(this);
        root.addView(
                space,
                new LinearLayout.LayoutParams(
                        1,
                        dp(22)
                )
        );

        TextView t=new TextView(this);
        t.setText(value);
        t.setTextSize(20);
        t.setTextColor(Color.rgb(45,45,45));

        root.addView(t);
    }

    void addSwitch(
            String title,
            String key,
            boolean def
    ) {

        Switch sw=new Switch(this);

        sw.setText(title);
        sw.setTextSize(16);
        sw.setGravity(Gravity.CENTER_VERTICAL);

        sw.setChecked(
                prefs.getBoolean(
                        key,
                        def
                )
        );

        sw.setOnCheckedChangeListener(
                (button,checked) ->

                prefs.edit()
                        .putBoolean(
                                key,
                                checked
                        )
                        .apply()
        );

        root.addView(
                sw,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(56)
                )
        );
    }

    void addSlider(
            String title,
            String key,
            int def,
            int min,
            int max
    ) {

        TextView label=new TextView(this);

        label.setText(
                title + ": " +
                prefs.getInt(key,def)
        );

        label.setTextSize(16);
        label.setTextColor(
                Color.rgb(45,45,45)
        );

        root.addView(label);

        SeekBar bar=new SeekBar(this);

        bar.setMax(max-min);

        bar.setProgress(
                prefs.getInt(
                        key,
                        def
                )-min
        );

        bar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(
                            SeekBar seekBar,
                            int progress,
                            boolean fromUser
                    ) {

                        int value=min+progress;

                        prefs.edit()
                                .putInt(
                                        key,
                                        value
                                )
                                .apply();

                        label.setText(
                                title + ": " + value
                        );
                    }

                    @Override
                    public void onStartTrackingTouch(
                            SeekBar seekBar
                    ) {}

                    @Override
                    public void onStopTrackingTouch(
                            SeekBar seekBar
                    ) {}
                }
        );

        root.addView(bar);
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
