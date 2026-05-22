package com.example.my_mobile_app.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.my_mobile_app.R;
import com.example.my_mobile_app.ui.auth.LoginActivity;
import com.example.my_mobile_app.util.LocaleHelper;
import com.example.my_mobile_app.util.TokenManager;
import com.google.android.material.snackbar.Snackbar;

/** Common Activity base with loading overlay, snackbars, and auth gate. */
public abstract class BaseActivity extends AppCompatActivity {

    private FrameLayout loadingOverlay;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.apply(newBase));
    }

    /** Show a full-screen translucent overlay with a centered spinner. */
    protected void showLoading() {
        if (loadingOverlay != null) return;
        ViewGroup root = findViewById(android.R.id.content);
        if (root == null) return;

        loadingOverlay = new FrameLayout(this);
        loadingOverlay.setBackgroundColor(Color.parseColor("#80000000"));
        loadingOverlay.setClickable(true);
        loadingOverlay.setFocusable(true);

        ProgressBar spinner = new ProgressBar(this);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        loadingOverlay.addView(spinner, lp);

        root.addView(loadingOverlay, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }

    protected void hideLoading() {
        if (loadingOverlay == null) return;
        ViewGroup parent = (ViewGroup) loadingOverlay.getParent();
        if (parent != null) parent.removeView(loadingOverlay);
        loadingOverlay = null;
    }

    protected void showError(String message) {
        showSnackbar(message, getColor(R.color.red_500));
    }

    protected void showSuccess(String message) {
        showSnackbar(message, getColor(R.color.green_500));
    }

    private void showSnackbar(String message, int bgColor) {
        View root = findViewById(android.R.id.content);
        if (root == null || message == null) return;
        Snackbar bar = Snackbar.make(root, message, Snackbar.LENGTH_LONG);
        bar.getView().setBackgroundColor(bgColor);
        bar.setTextColor(Color.WHITE);
        bar.show();
    }

    /**
     * Returns true if a token is present. Otherwise routes to login and
     * finishes the current activity.
     */
    protected boolean requireLogin() {
        if (TokenManager.getToken(this) != null) return true;
        startActivity(new Intent(this, LoginActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
        return false;
    }
}
