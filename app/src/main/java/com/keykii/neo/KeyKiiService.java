package com.keykii.neo;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.inputmethodservice.InputMethodService;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class KeyKiiService extends InputMethodService {

    // Glass Float palette
    private static final int PANEL_BG = Color.argb(205, 28, 31, 37);
    private static final int PANEL_BORDER = Color.argb(70, 255, 255, 255);
    private static final int HANDLE = Color.argb(130, 255, 255, 255);

    private static final int KEY_BG = Color.argb(120, 255, 255, 255);
    private static final int KEY_BG_ALT = Color.argb(90, 255, 255, 255);
    private static final int KEY_BG_PRESSED = Color.argb(165, 255, 255, 255);

    private static final int SPACE_BG = Color.argb(185, 92, 231, 255);
    private static final int SPACE_BG_PRESSED = Color.argb(210, 120, 239, 255);

    private static final int TEXT = Color.argb(245, 255, 255, 255);
    private static final int SUBTEXT = Color.argb(185, 255, 255, 255);

    private LinearLayout keyboardPanel;
    private TextView previewText;

    private boolean shift = false;
    private boolean symbols = false;

    private final StringBuilder typedBuffer = new StringBuilder();

    @Override
    public View onCreateInputView() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundColor(Color.TRANSPARENT);
        root.setPadding(dp(14), dp(6), dp(14), dp(24));

        keyboardPanel = new LinearLayout(this);
        keyboardPanel.setOrientation(LinearLayout.VERTICAL);
        keyboardPanel.setPadding(dp(12), dp(8), dp(12), dp(12));
        keyboardPanel.setElevation(dp(12));

        GradientDrawable panelBg = new GradientDrawable();
        panelBg.setColor(PANEL_BG);
        panelBg.setCornerRadius(dp(26));
        panelBg.setStroke(dp(1), PANEL_BORDER);
        keyboardPanel.setBackground(panelBg);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        int panelWidth = (int) (dm.widthPixels * 0.82f);

        LinearLayout.LayoutParams panelParams =
                new LinearLayout.LayoutParams(
                        panelWidth,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        panelParams.gravity = Gravity.CENTER_HORIZONTAL;

        root.addView(keyboardPanel, panelParams);

        rebuildKeyboard();

        return root;
    }

    private void rebuildKeyboard() {
        if (keyboardPanel == null) return;

        keyboardPanel.removeAllViews();

        addHandle();
        addPreviewArea();
        addSuggestionRow();

        if (symbols) {
            buildSymbolLayout();
        } else {
            buildLetterLayout();
        }
    }

    private void addHandle() {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER);

        View handle = new View(this);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(HANDLE);
        bg.setCornerRadius(dp(10));
        handle.setBackground(bg);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(dp(56), dp(4));
        p.setMargins(0, dp(2), 0, dp(10));

        row.addView(handle, p);
        keyboardPanel.addView(row);
    }

    private void addPreviewArea() {
        previewText = new TextView(this);
        previewText.setTextColor(TEXT);
        previewText.setTextSize(16);
        previewText.setGravity(Gravity.CENTER);
        previewText.setSingleLine(true);
        previewText.setText(getPreviewText());

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        p.setMargins(dp(6), 0, dp(6), dp(8));

        keyboardPanel.addView(previewText, p);
    }

    private void addSuggestionRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);

        String[] suggestions = getSuggestions();

        for (String suggestion : suggestions) {
            addSuggestionChip(row, suggestion);
        }

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        p.setMargins(0, 0, 0, dp(8));
        row.setLayoutParams(p);

        keyboardPanel.addView(row);
    }

    private String[] getSuggestions() {
        String current = getCurrentWord();

        if (current.isEmpty()) {
            return new String[]{"I", "The", "I'm"};
        }

        String cap =
                current.substring(0, 1).toUpperCase() +
                        current.substring(1).toLowerCase();

        return new String[]{cap, current, current + "!"};
    }

    private String getCurrentWord() {
        String text = typedBuffer.toString().trim();
        if (text.isEmpty()) return "";

        int lastSpace = text.lastIndexOf(' ');
        if (lastSpace == -1) return text;

        return text.substring(lastSpace + 1);
    }

    private void addSuggestionChip(
            LinearLayout row,
            String text
    ) {
        TextView chip = new TextView(this);
        chip.setText(text);
        chip.setTextColor(TEXT);
        chip.setTextSize(15);
        chip.setGravity(Gravity.CENTER);
        chip.setSingleLine(true);

        GradientDrawable normal = new GradientDrawable();
        normal.setColor(Color.argb(100, 255, 255, 255));
        normal.setCornerRadius(dp(18));
        normal.setStroke(dp(1), Color.argb(40, 255, 255, 255));

        GradientDrawable pressed = new GradientDrawable();
        pressed.setColor(Color.argb(150, 255, 255, 255));
        pressed.setCornerRadius(dp(18));
        pressed.setStroke(dp(1), Color.argb(80, 255, 255, 255));

        StateListDrawable state = new StateListDrawable();
        state.addState(new int[]{android.R.attr.state_pressed}, pressed);
        state.addState(new int[]{}, normal);

        chip.setBackground(state);

        chip.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            commitSuggestion(text);
        });

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        0,
                        dp(38),
                        1f
                );

        p.setMargins(dp(4), 0, dp(4), 0);

        row.addView(chip, p);
    }

    private void buildLetterLayout() {
        addLetterRow(new String[]{"q","w","e","r","t","y","u","i","o","p"}, false);
        addLetterRow(new String[]{"a","s","d","f","g","h","j","k","l"}, true);

        LinearLayout row3 = newRow();

        addKey(row3, "⇧", "SHIFT", 1.15f, true, true, false);

        for (String letter : new String[]{"z","x","c","v","b","n","m"}) {
            addKey(
                    row3,
                    shift ? letter.toUpperCase() : letter,
                    letter,
                    1f,
                    false,
                    false,
                    false
            );
        }

        addKey(row3, "⌫", "BACKSPACE", 1.15f, true, true, false);

        keyboardPanel.addView(row3);

        addBottomRow(false);
    }

    private void buildSymbolLayout() {
        addSymbolRow(new String[]{"1","2","3","4","5","6","7","8","9","0"});
        addSymbolRow(new String[]{"@","#","$","%","&","-","+","(",")","/"});

        LinearLayout row3 = newRow();

        addKey(row3, "ABC", "ABC", 1.15f, true, true, false);

        for (String key : new String[]{"*","\"","'",":",";","!","?"}) {
            addKey(row3, key, key, 1f, false, false, false);
        }

        addKey(row3, "⌫", "BACKSPACE", 1.15f, true, true, false);

        keyboardPanel.addView(row3);

        addBottomRow(true);
    }

    private void addLetterRow(
            String[] letters,
            boolean centered
    ) {
        LinearLayout row = newRow();

        if (centered) addSpacer(row, 0.55f);

        for (String letter : letters) {
            addKey(
                    row,
                    shift ? letter.toUpperCase() : letter,
                    letter,
                    1f,
                    false,
                    false,
                    false
            );
        }

        if (centered) addSpacer(row, 0.55f);

        keyboardPanel.addView(row);
    }

    private void addSymbolRow(String[] keys) {
        LinearLayout row = newRow();

        for (String key : keys) {
            addKey(row, key, key, 1f, false, false, false);
        }

        keyboardPanel.addView(row);
    }

    private void addBottomRow(boolean inSymbols) {
        LinearLayout row = newRow();

        addKey(
                row,
                inSymbols ? "ABC" : "123",
                inSymbols ? "ABC" : "123",
                0.95f,
                true,
                true,
                false
        );

        addKey(row, "☺", "EMOJI", 0.75f, false, true, false);

        addKey(row, "KeyKii", "SPACE", 3.35f, false, true, true);

        addKey(row, "return", "ENTER", 1.10f, true, true, false);

        keyboardPanel.addView(row);
    }

    private LinearLayout newRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        p.setMargins(0, dp(2), 0, dp(2));
        row.setLayoutParams(p);

        return row;
    }

    private void addSpacer(
            LinearLayout row,
            float weight
    ) {
        View spacer = new View(this);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        0,
                        dp(42),
                        weight
                );

        row.addView(spacer, p);
    }

    private void addKey(
            LinearLayout row,
            String label,
            String action,
            float weight,
            boolean special,
            boolean pill,
            boolean isSpace
    ) {
        TextView key = new TextView(this);
        key.setText(label);
        key.setTextColor(TEXT);
        key.setGravity(Gravity.CENTER);
        key.setAllCaps(false);
        key.setIncludeFontPadding(false);

        if (label.equals("KeyKii")) {
            key.setTextSize(15);
        } else if (label.equals("return") || label.equals("ABC")) {
            key.setTextSize(14);
        } else {
            key.setTextSize(18);
        }

        float radius = pill ? dp(20) : dp(18);
        key.setBackground(makeKeyBackground(special, isSpace, radius));
        key.setElevation(dp(2));

        key.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                v.animate()
                        .translationY(dp(2))
                        .scaleX(0.98f)
                        .scaleY(0.98f)
                        .setDuration(55)
                        .start();
            } else if (
                    event.getAction() == MotionEvent.ACTION_UP ||
                    event.getAction() == MotionEvent.ACTION_CANCEL
            ) {
                v.animate()
                        .translationY(0)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(75)
                        .start();
            }
            return false;
        });

        key.setOnClickListener(v -> handleKey(action));

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        0,
                        dp(42),
                        weight
                );

        p.setMargins(dp(4), dp(4), dp(4), dp(4));
        row.addView(key, p);
    }

    private StateListDrawable makeKeyBackground(
            boolean special,
            boolean isSpace,
            float radius
    ) {
        int normalColor;
        int pressedColor;

        if (isSpace) {
            normalColor = SPACE_BG;
            pressedColor = SPACE_BG_PRESSED;
        } else if (special) {
            normalColor = KEY_BG_ALT;
            pressedColor = KEY_BG_PRESSED;
        } else {
            normalColor = KEY_BG;
            pressedColor = KEY_BG_PRESSED;
        }

        GradientDrawable normal = new GradientDrawable();
        normal.setColor(normalColor);
        normal.setCornerRadius(radius);
        normal.setStroke(dp(1), Color.argb(35, 255, 255, 255));

        GradientDrawable pressed = new GradientDrawable();
        pressed.setColor(pressedColor);
        pressed.setCornerRadius(radius);
        pressed.setStroke(dp(1), Color.argb(80, 255, 255, 255));

        StateListDrawable state = new StateListDrawable();
        state.addState(new int[]{android.R.attr.state_pressed}, pressed);
        state.addState(new int[]{}, normal);

        return state;
    }

    private void handleKey(String key) {
        InputConnection input = getCurrentInputConnection();
        if (input == null) return;

        switch (key) {
            case "BACKSPACE":
                input.deleteSurroundingText(1, 0);
                if (typedBuffer.length() > 0) {
                    typedBuffer.deleteCharAt(typedBuffer.length() - 1);
                }
                updatePreview();
                break;

            case "SPACE":
                input.commitText(" ", 1);
                typedBuffer.append(" ");
                updatePreview();
                break;

            case "ENTER":
                handleEnter(input);
                typedBuffer.append("\n");
                updatePreview();
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
                input.commitText("☺", 1);
                typedBuffer.append("☺");
                updatePreview();
                break;

            default:
                String value = key;

                if (!symbols && shift) {
                    value = key.toUpperCase();
                }

                input.commitText(value, 1);
                typedBuffer.append(value);
                updatePreview();

                if (shift && !symbols) {
                    shift = false;
                    rebuildKeyboard();
                }
                break;
        }
    }

    private void commitSuggestion(String word) {
        InputConnection input = getCurrentInputConnection();
        if (input == null) return;

        input.commitText(word + " ", 1);
        typedBuffer.append(word).append(" ");
        updatePreview();
    }

    private void updatePreview() {
        if (previewText != null) {
            previewText.setText(getPreviewText());
        }
    }

    private String getPreviewText() {
        String value = typedBuffer.toString();

        if (value.trim().isEmpty()) {
            return "type softly";
        }

        value = value.replace("\n", " ");

        if (value.length() > 28) {
            return value.substring(value.length() - 28);
        }

        return value;
    }

    private void handleEnter(InputConnection input) {
        EditorInfo info = getCurrentInputEditorInfo();

        if (info != null) {
            int action = info.imeOptions & EditorInfo.IME_MASK_ACTION;

            if (
                    action != EditorInfo.IME_ACTION_NONE &&
                    action != EditorInfo.IME_ACTION_UNSPECIFIED
            ) {
                input.performEditorAction(action);
                return;
            }
        }

        input.sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)
        );
        input.sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER)
        );
    }

    private int dp(int value) {
        return Math.round(
                value * getResources().getDisplayMetrics().density
        );
    }
}
