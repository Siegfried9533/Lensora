package com.example.my_mobile_app.util;

import android.content.Context;
import android.content.SharedPreferences;

/** SharedPreferences-backed JWT token store. */
public final class TokenManager {

    public static final String PREFS_NAME = "camera_shop_prefs";
    public static final String KEY_TOKEN = "auth_token";

    private TokenManager() {}

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void saveToken(Context ctx, String token) {
        prefs(ctx).edit().putString(KEY_TOKEN, token).apply();
    }

    public static String getToken(Context ctx) {
        return prefs(ctx).getString(KEY_TOKEN, null);
    }

    public static void clear(Context ctx) {
        prefs(ctx).edit().remove(KEY_TOKEN).apply();
    }
}
