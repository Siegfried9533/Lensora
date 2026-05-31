package com.example.my_mobile_app.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public final class ThemeManager {
    private static final String PREFS = "theme_prefs";
    private static final String KEY_DARK_MODE = "dark_mode";

    private ThemeManager() {}

    public static boolean isDarkMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_DARK_MODE, true);
    }

    public static void setDarkMode(Context context, boolean darkMode) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_DARK_MODE, darkMode)
                .apply();
        applyMode(darkMode);
    }

    public static void applySavedMode(Context context) {
        applyMode(isDarkMode(context));
    }

    private static void applyMode(boolean darkMode) {
        AppCompatDelegate.setDefaultNightMode(darkMode
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }
}
