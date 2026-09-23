package com.keykii.neo;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.inputmethodservice.InputMethodService;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.LinearLayout;
import android.widget.TextView;

public class KeyKiiService extends InputMethodService {

    private boolean shift = false;
    private boolean symbols = false;

    private final int cream = Color.rgb(247, 242, 232);
    private final int keyColor = Color.rgb(255, 252, 246);
    private final int textColor = Color.rgb(55, 52, 48);

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public View onCreateInputView() {
        return buildKeyboard();
    }

    private LinearLayout buildKeyboard() {
        LinearLayout keyboard = new LinearLayout(this);
        keyboard.setOrientation(LinearLayout.VERTICAL);
        keyboard.setPadding(dp(4), dp(4), dp(4), dp(4));
        keyboard.setBackgroundColor(cream);

        if (symbols) {
            addRow(keyboard, new String[]{"1","2","3","4","5","6","7","8","9","0"});
            addRow(keyboard, new String[]{"@","#","$","%","&","*","-","+","(",")"});
            addRow(keyboard, new String[]{"_","\"","'",";",":","/","?","!","."});
        } else {
            addRow(keyboard, new String[]{"Q","W","E","R","T","Y","U","I","O","P"});
            addRow(keyboard, new String[]{"A","S","D","F","G","H","J","K","L"});
            addRow(keyboard, new String[]{"Z","X","C","V","B","N","M"});
        }

        LinearLayout bottom = new LinearLayout(this);
        bottom.setGravity(Gravity.CENTER);
        bottom.setWeightSum(5);

        addKey(bottom, "⇧", 1.0f, 56);
        addKey(bottom, ",", 0.8f, 56);
        addKey(bottom, "SPACE", 2.4f, 56);
        addKey(bottom, ".", 0.8f, 56);
        addKey(bottom, "⌫", 1.0f, 56);

        keyboard.addView(bottom, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(60)));

        LinearLayout last = new LinearLayout(this);
        last.setGravity(Gravity.CENTER);
        last.setWeightSum(3);

        addKey(last, "123", 1.0f, 56);
        addKey(last, "😊", 1.0f, 56);
        addKey(last, "↵", 1.0f, 56);

        keyboard.addView(last, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(60)));

        return keyboard;
    }

    private void addRow(LinearLayout keyboard, String[] keys) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER);
        row.setWeightSum(keys.length);

        for (String key : keys) {
            addKey(row, key, 1.0f, 56);
        }

        keyboard.addView(row, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(60)));
    }

    private void addKey(LinearLayout row, String text, float weight, int height) {
        TextView key = new TextView(this);

        key.setText(text);
        key.setTextSize(text.equals("SPACE") ? 13 : 18);
        key.setTextColor(textColor);
        key.setGravity(Gravity.CENTER);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(keyColor);
        bg.setCornerRadius(dp(11));
        key.setBackground(bg);

        key.setOnClickListener(v -> handleKey(text));

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(0, dp(height), weight);

        params.setMargins(dp(2), dp(2), dp(2), dp(2));
        row.addView(key, params);
    }

    private void handleKey(String key) {
        InputConnection input = getCurrentInputConnection();
        if (input == null) return;

        if (key.equals("⌫")) {
            input.deleteSurroundingText(1, 0);

        } else if (key.equals("SPACE")) {
            input.commitText(" ", 1);

        } else if (key.equals("↵")) {
            input.performEditorAction(EditorInfo.IME_ACTION_DONE);

        } else if (key.equals("⇧")) {
            shift = !shift;
            refreshKeyboard();

        } else if (key.equals("123")) {
            symbols = !symbols;
            refreshKeyboard();

        } else if (key.equals("😊")) {
            input.commitText("😊", 1);

        } else {
            String value = shift ? key.toUpperCase() : key.toLowerCase();
            input.commitText(value, 1);

            if (shift && !symbols) {
                shift = false;
                refreshKeyboard();
            }
        }
    }

    private void refreshKeyboard() {
        setInputView(buildKeyboard());
    }
}

