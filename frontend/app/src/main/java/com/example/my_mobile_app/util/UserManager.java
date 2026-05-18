package com.example.my_mobile_app.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.my_mobile_app.model.User;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Stores the current user as JSON in SharedPreferences. Provides both raw
 * String access (kept for backward compat with Phase 1) and typed
 * {@link User} methods backed by Gson.
 */
public final class UserManager {

    public static final String KEY_USER_JSON = "current_user";

    private static final Gson GSON = new Gson();

    private UserManager() {}

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getApplicationContext()
                .getSharedPreferences(TokenManager.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void saveUserJson(Context ctx, String json) {
        prefs(ctx).edit().putString(KEY_USER_JSON, json).apply();
    }

    public static String getUserJson(Context ctx) {
        return prefs(ctx).getString(KEY_USER_JSON, null);
    }

    public static void saveUser(Context ctx, User user) {
        if (user == null) {
            clear(ctx);
            return;
        }
        saveUserJson(ctx, GSON.toJson(user));
    }

    /** Returns null if no user stored or stored JSON is malformed. */
    public static User getUser(Context ctx) {
        String json = getUserJson(ctx);
        if (json == null) return null;
        try {
            return GSON.fromJson(json, User.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    public static void clear(Context ctx) {
        prefs(ctx).edit().remove(KEY_USER_JSON).apply();
    }
}
