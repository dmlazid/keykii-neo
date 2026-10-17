package com.keykii.neo;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.inputmethodservice.InputMethodService;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class KeyKiiService extends InputMethodService {

    // KeyKii Morning Cream palette
    private static final int CREAM = Color.rgb(247, 245, 242);      // #F7F5F2
    private static final int KEY_WHITE = Color.rgb(255, 255, 255);  // #FFFFFF
    private static final int PRESSED = Color.rgb(236, 231, 226);    // #ECE7E2
    private static final int ACCENT = Color.rgb(216, 203, 184);     // #D8CBB8
    private static final int TEXT = Color.rgb(45, 45, 45);          // #2D2D2D
    private static final int MUTED = Color.rgb(150, 145, 140);

    private LinearLayout keyboardPanel;

    private boolean shift = false;
    private boolean symbols = false;

    @Override
    public View onCreateInputView() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);

        // Creates the floating appearance around the keyboard
        root.setPadding(dp(10), dp(6), dp(10), dp(10));
        root.setBackgroundColor(Color.TRANSPARENT);

        keyboardPanel = new LinearLayout(this);
        keyboardPanel.setOrientation(LinearLayout.VERTICAL);
        keyboardPanel.setPadding(dp(8), dp(8), dp(8), dp(10));
        keyboardPanel.setElevation(dp(14));

        GradientDrawable panelBg = new GradientDrawable();
        panelBg.setColor(CREAM);
        panelBg.setCornerRadius(dp(28));
        keyboardPanel.setBackground(panelBg);

        LinearLayout.LayoutParams panelParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        panelParams.setMargins(dp(2), dp(2), dp(2), dp(4));

        root.addView(keyboardPanel, panelParams);

        rebuildKeyboard();

        return root;
    }

    private void rebuildKeyboard() {

        if (keyboardPanel == null) return;

        keyboardPanel.removeAllViews();

        addDragHandle();

        if (symbols) {
            buildSymbolKeyboard();
        } else {
            buildLetterKeyboard();
        }
    }

    private void addDragHandle() {

        LinearLayout handleRow = new LinearLayout(this);
        handleRow.setGravity(Gravity.CENTER);

        TextView handle = new TextView(this);

        GradientDrawable handleBg = new GradientDrawable();
        handleBg.setColor(Color.rgb(205, 200, 194));
        handleBg.setCornerRadius(dp(10));

        handle.setBackground(handleBg);

        LinearLayout.LayoutParams handleParams =
                new LinearLayout.LayoutParams(dp(42), dp(4));

        handleParams.setMargins(0, dp(2), 0, dp(10));

        handleRow.addView(handle, handleParams);

        keyboardPanel.addView(handleRow);
    }

    private void buildLetterKeyboard() {

        String[] row1 = {
                "q", "w", "e", "r", "t",
                "y", "u", "i", "o", "p"
        };

        String[] row2 = {
                "a", "s", "d", "f", "g",
                "h", "j", "k", "l"
        };

        String[] row3 = {
                "z", "x", "c", "v",
                "b", "n", "m"
        };

        addLetterRow(row1);
        addCenteredLetterRow(row2);

        LinearLayout third = newRow();

        addKey(third, "⇧", "SHIFT", 1.25f, true, false);

        for (String letter : row3) {
            String shown = shift ? letter.toUpperCase() : letter;
            addKey(third, shown, letter, 1f, false, false);
        }

        addKey(third, "⌫", "BACKSPACE", 1.25f, true, false);

        keyboardPanel.addView(third);

        addBottomRow();
    }

    private void buildSymbolKeyboard() {

        String[] row1 = {
                "1", "2", "3", "4", "5",
                "6", "7", "8", "9", "0"
        };

        String[] row2 = {
                "@", "#", "$", "%", "&",
                "-", "+", "(", ")", "/"
        };

        String[] row3 = {
                "*", "\"", "'", ":",
                ";", "!", "?"
        };

        addActionRow(row1);
        addActionRow(row2);

        LinearLayout third = newRow();

        addKey(third, "ABC", "ABC", 1.30f, true, false);

        for (String key : row3) {
            addKey(third, key, key, 1f, false, false);
        }

        addKey(third, "⌫", "BACKSPACE", 1.30f, true, false);

        keyboardPanel.addView(third);

        addBottomRow();
    }

    private void addLetterRow(String[] letters) {

        LinearLayout row = newRow();

        for (String letter : letters) {

            String shown =
                    shift ? letter.toUpperCase() : letter;

            addKey(
                    row,
                    shown,
                    letter,
                    1f,
                    false,
                    false
            );
        }

        keyboardPanel.addView(row);
    }

    private void addCenteredLetterRow(String[] letters) {

        LinearLayout row = newRow();

        addSpacer(row, 0.5f);

        for (String letter : letters) {

            String shown =
                    shift ? letter.toUpperCase() : letter;

            addKey(
                    row,
                    shown,
                    letter,
                    1f,
                    false,
                    false
            );
        }

        addSpacer(row, 0.5f);

        keyboardPanel.addView(row);
    }

    private void addActionRow(String[] keys) {

        LinearLayout row = newRow();

        for (String key : keys) {
            addKey(
                    row,
                    key,
                    key,
                    1f,
                    false,
                    false
            );
        }

        keyboardPanel.addView(row);
    }

    private void addBottomRow() {

        LinearLayout bottom = newRow();

        if (symbols) {
            addKey(bottom, "ABC", "ABC", 1.30f, true, true);
        } else {
            addKey(bottom, "?123", "123", 1.30f, true, true);
        }

        addKey(bottom, "☺", "EMOJI", 0.75f, false, true);
        addKey(bottom, "◎", "GLOBE", 0.75f, false, true);

        // Signature KeyKii pill-shaped spacebar
        addKey(bottom, "KeyKii", "SPACE", 3.30f, false, true);

        addKey(bottom, ".", ".", 0.80f, false, true);
        addKey(bottom, "↵", "ENTER", 1.20f, true, true);

        keyboardPanel.addView(bottom);
    }

    private LinearLayout newRow() {

        LinearLayout row = new LinearLayout(this);

        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(0, dp(2), 0, dp(2));

        row.setLayoutParams(params);

        return row;
    }

    private void addSpacer(
            LinearLayout row,
            float weight
    ) {

        View spacer = new View(this);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0,
                        dp(52),
                        weight
                );

        row.addView(spacer, params);
    }

    private void addKey(
            LinearLayout row,
            String label,
            String action,
            float weight,
            boolean special,
            boolean pill
    ) {

        TextView key = new TextView(this);

        key.setText(label);
        key.setTextColor(TEXT);
        key.setTextSize(
                label.length() > 3 ? 16 : 21
        );

        key.setGravity(Gravity.CENTER);
        key.setAllCaps(false);
        key.setIncludeFontPadding(false);

        int normalColor =
                special ? Color.rgb(244, 240, 235) : KEY_WHITE;

        float radius =
                pill ? dp(24) : dp(18);

        GradientDrawable normal = new GradientDrawable();
        normal.setColor(normalColor);
        normal.setCornerRadius(radius);
        normal.setStroke(dp(1), Color.argb(25, 45, 45, 45));

        GradientDrawable pressed = new GradientDrawable();
        pressed.setColor(PRESSED);
        pressed.setCornerRadius(radius);
        pressed.setStroke(dp(1), ACCENT);

        StateListDrawable background =
                new StateListDrawable();

        background.addState(
                new int[]{android.R.attr.state_pressed},
                pressed
        );

        background.addState(
                new int[]{},
                normal
        );

        key.setBackground(background);
        key.setElevation(dp(3));

        key.setOnClickListener(
                v -> handleKey(action)
        );

        // Gentle 2dp sink animation when pressed
        key.setOnTouchListener((v, event) -> {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {

                v.animate()
                        .translationY(dp(2))
                        .setDuration(60)
                        .start();

            } else if (
                    event.getAction() == MotionEvent.ACTION_UP ||
                    event.getAction() == MotionEvent.ACTION_CANCEL
            ) {

                v.animate()
                        .translationY(0)
                        .setDuration(80)
                        .start();
            }

            return false;
        });

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0,
                        dp(54),
                        weight
                );

        params.setMargins(
                dp(3),
                dp(3),
                dp(3),
                dp(3)
        );

        row.addView(key, params);
    }

    private void handleKey(String key) {

        InputConnection input =
                getCurrentInputConnection();

        if (input == null) return;

        switch (key) {

            case "BACKSPACE":

                input.deleteSurroundingText(1, 0);
                break;

            case "SPACE":

                input.commitText(" ", 1);
                break;

            case "ENTER":

                handleEnter(input);
                break;

            case "SHIFT":

                shift = !shift;
                rebuildKeyboard();
                break;

            case "123":

                symbols = true;
                shift = false;
                rebuildKeyboard();
                break;

            case "ABC":

                symbols = false;
                shift = false;
                rebuildKeyboard();
                break;

            case "EMOJI":

                input.commitText("😊", 1);
                break;

            case "GLOBE":

                InputMethodManager imm =
                        (InputMethodManager)
                                getSystemService(
                                        Context.INPUT_METHOD_SERVICE
                                );

                if (imm != null) {
                    imm.showInputMethodPicker();
                }

                break;

            default:

                String value = key;

                if (!symbols && shift) {
                    value = key.toUpperCase();
                }

                input.commitText(value, 1);

                // Shift returns to lowercase after one letter
                if (shift && !symbols) {
                    shift = false;
                    rebuildKeyboard();
                }

                break;
        }
    }

    private void handleEnter(InputConnection input) {

        EditorInfo info =
                getCurrentInputEditorInfo();

        if (info != null) {

            int action =
                    info.imeOptions &
                            EditorInfo.IME_MASK_ACTION;

            if (
                    action != EditorInfo.IME_ACTION_NONE &&
                    action != EditorInfo.IME_ACTION_UNSPECIFIED
            ) {

                input.performEditorAction(action);
                return;
            }
        }

        input.sendKeyEvent(
                new KeyEvent(
                        KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_ENTER
                )
        );

        input.sendKeyEvent(
                new KeyEvent(
                        KeyEvent.ACTION_UP,
                        KeyEvent.KEYCODE_ENTER
                )
        );
    }

    private int dp(int value) {

        float density =
                getResources()
                        .getDisplayMetrics()
                        .density;

        return Math.round(
                value * density
        );
    }
}
