package com.example.my_mobile_app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import java.util.Locale;

/** Stores and applies the app language independently from the device language. */
public final class LocaleHelper {

    public static final String LANG_EN = "en";
    public static final String LANG_VI = "vi";

    private static final String PREFS = "app_locale";
    private static final String KEY_LANGUAGE = "language";
    private static final String DEFAULT_LANGUAGE = LANG_VI;

    private LocaleHelper() {}

    public static Context apply(Context context) {
        return wrap(context, getLanguage(context));
    }

    public static String getLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE);
    }

    public static void setLanguage(Context context, String language) {
        if (!LANG_EN.equals(language) && !LANG_VI.equals(language)) return;
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_LANGUAGE, language)
                .apply();
    }

    private static Context wrap(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(new LocaleList(locale));
        } else {
            config.setLocale(locale);
        }
        return context.createConfigurationContext(config);
    }
}
