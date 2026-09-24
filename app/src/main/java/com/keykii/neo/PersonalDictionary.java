package com.keykii.neo;

import android.content.Context;
import android.content.SharedPreferences;

final class PersonalDictionary {

    static final int MAX_WORDS = 100;

    private static final String PREFS =
            "keykii_personal_dictionary";
    private static final String KEY_WORDS =
            "words_v1";

    private PersonalDictionary() {
    }

    static java.util.ArrayList<String> load(
            Context context
    ) {
        SharedPreferences prefs =
                context.getSharedPreferences(
                        PREFS,
                        Context.MODE_PRIVATE
                );

        java.util.Set<String> stored =
                prefs.getStringSet(
                        KEY_WORDS,
                        java.util.Collections.emptySet()
                );

        java.util.ArrayList<String> words =
                new java.util.ArrayList<>();

        if (stored != null) {
            for (String value : stored) {
                String cleaned = normalize(value);

                if (
                        !cleaned.isEmpty() &&
                        !containsIgnoreCase(
                                words,
                                cleaned
                        )
                ) {
                    words.add(cleaned);
                }
            }
        }

        java.util.Collections.sort(
                words,
                String.CASE_INSENSITIVE_ORDER
        );

        return words;
    }

    static String normalize(String value) {
        if (value == null)
            return "";

        String word =
                value.trim()
                        .replace('\u2019', '\'');

        if (
                word.isEmpty() ||
                word.length() > 32
        ) {
            return "";
        }

        if (
                !Character.isLetter(word.charAt(0)) ||
                !Character.isLetter(
                        word.charAt(word.length() - 1)
                )
        ) {
            return "";
        }

        for (int i = 0; i < word.length(); i++) {
            char ch = word.charAt(i);

            if (
                    !Character.isLetter(ch) &&
                    ch != '\''
            ) {
                return "";
            }
        }

        return word;
    }

    static boolean add(
            Context context,
            String value
    ) {
        String cleaned = normalize(value);

        if (cleaned.isEmpty())
            return false;

        java.util.ArrayList<String> words =
                load(context);

        if (containsIgnoreCase(words, cleaned))
            return true;

        if (words.size() >= MAX_WORDS)
            return false;

        words.add(cleaned);
        save(context, words);
        return true;
    }

    static boolean replace(
            Context context,
            String oldValue,
            String newValue
    ) {
        String cleaned = normalize(newValue);

        if (cleaned.isEmpty())
            return false;

        java.util.ArrayList<String> words =
                load(context);

        int oldIndex = -1;

        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);

            if (
                    oldValue != null &&
                    word.equalsIgnoreCase(oldValue)
            ) {
                oldIndex = i;
                continue;
            }

            if (word.equalsIgnoreCase(cleaned))
                return false;
        }

        if (oldIndex >= 0)
            words.set(oldIndex, cleaned);
        else {
            if (words.size() >= MAX_WORDS)
                return false;

            words.add(cleaned);
        }

        save(context, words);
        return true;
    }

    static void remove(
            Context context,
            String value
    ) {
        if (value == null)
            return;

        java.util.ArrayList<String> words =
                load(context);

        for (int i = words.size() - 1; i >= 0; i--) {
            if (words.get(i).equalsIgnoreCase(value))
                words.remove(i);
        }

        save(context, words);
    }

    private static boolean containsIgnoreCase(
            java.util.ArrayList<String> words,
            String candidate
    ) {
        for (String word : words) {
            if (word.equalsIgnoreCase(candidate))
                return true;
        }

        return false;
    }

    private static void save(
            Context context,
            java.util.ArrayList<String> words
    ) {
        java.util.LinkedHashSet<String> set =
                new java.util.LinkedHashSet<>();

        for (String value : words) {
            String cleaned = normalize(value);

            if (
                    !cleaned.isEmpty() &&
                    set.size() < MAX_WORDS
            ) {
                set.add(cleaned);
            }
        }

        context.getSharedPreferences(
                        PREFS,
                        Context.MODE_PRIVATE
                )
                .edit()
                .putStringSet(
                        KEY_WORDS,
                        new java.util.LinkedHashSet<>(set)
                )
                .apply();
    }
}
