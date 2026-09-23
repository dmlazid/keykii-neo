package com.keykii.neo;

import android.app.Activity;
import android.content.*;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

public class MainActivity extends Activity {

    LinearLayout root;
    SharedPreferences prefs;

    int cream = Color.rgb(247,245,242);
    int text = Color.rgb(45,45,45);
    int beige = Color.rgb(239,232,221);

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        prefs = getSharedPreferences(
                "keykii_prefs",
                MODE_PRIVATE
        );

        buildScreen();
    }

    void buildScreen() {

        ScrollView scroll = new ScrollView(this);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24),dp(35),dp(24),dp(35));
        root.setBackgroundColor(cream);

        TextView logo = text("KeyKii", 34);
        logo.setGravity(Gravity.CENTER);

        TextView sub = text(
                "Your keyboard, your space.",
                16
        );

        sub.setGravity(Gravity.CENTER);
        sub.setTextColor(Color.rgb(120,115,110));

        root.addView(logo);
        root.addView(sub);

        space(28);

        addButton(
                "Enable KeyKii",
                () -> startActivity(
                        new Intent(
                                Settings.ACTION_INPUT_METHOD_SETTINGS
                        )
                )
        );

        addButton(
                "Choose Keyboard",
                () -> {
                    InputMethodManager imm =
                            (InputMethodManager)
                            getSystemService(
                                    INPUT_METHOD_SERVICE
                            );

                    if(imm != null)
                        imm.showInputMethodPicker();
                }
        );

        section("Appearance");

        addTheme("Glass Dark",0);
        addTheme("Morning Cream",1);
        addTheme("Clear Glass",2);

        section("Keyboard");

        addButton(
                "Keyboard settings",
                () -> toast(
                        "More keyboard settings coming in 2.2"
                )
        );

        section("About");

        TextView version =
                text("KeyKii Neo 2.2",15);

        version.setTextColor(
                Color.rgb(120,115,110)
        );

        root.addView(version);

        scroll.addView(root);
        setContentView(scroll);
    }

    void addTheme(
            String name,
            int theme
    ) {

        boolean selected =
                prefs.getInt(
                        "theme",
                        0
                ) == theme;

        TextView button =
                text(
                        (selected ? "✓  " : "○  ")
                                + name,
                        17
                );

        button.setGravity(
                Gravity.CENTER_VERTICAL
        );

        button.setPadding(
                dp(18),
                0,
                dp(18),
                0
        );

        button.setBackground(
                card(
                        selected
                                ? beige
                                : Color.WHITE
                )
        );

        button.setOnClickListener(v -> {

            prefs.edit()
                    .putInt(
                            "theme",
                            theme
                    )
                    .apply();

            buildScreen();

            toast(
                    name + " selected"
            );
        });

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(58)
                );

        p.setMargins(
                0,
                dp(5),
                0,
                dp(5)
        );

        root.addView(button,p);
    }

    void addButton(
            String name,
            Runnable action
    ) {

        TextView b =
                text(name,17);

        b.setGravity(Gravity.CENTER);
        b.setBackground(card(Color.WHITE));

        b.setOnClickListener(
                v -> action.run()
        );

        LinearLayout.LayoutParams p =
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

        root.addView(b,p);
    }

    void section(String name) {

        space(24);

        TextView t =
                text(name,21);

        t.setPadding(
                dp(3),
                dp(8),
                0,
                dp(8)
        );

        root.addView(t);
    }

    TextView text(
            String s,
            int size
    ) {

        TextView t =
                new TextView(this);

        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(text);

        return t;
    }

    GradientDrawable card(int color) {

        GradientDrawable g =
                new GradientDrawable();

        g.setColor(color);
        g.setCornerRadius(dp(18));

        g.setStroke(
                dp(1),
                Color.rgb(225,220,214)
        );

        return g;
    }

    void space(int h) {

        Space s = new Space(this);

        root.addView(
                s,
                new LinearLayout.LayoutParams(
                        1,
                        dp(h)
                )
        );
    }

    void toast(String s) {

        Toast.makeText(
                this,
                s,
                Toast.LENGTH_SHORT
        ).show();
    }

    int dp(int n) {

        return Math.round(
                n *
                getResources()
                        .getDisplayMetrics()
                        .density
        );
    }
}
