package com.example.my_mobile_app.ui.profile;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.my_mobile_app.MainActivity;
import com.example.my_mobile_app.R;
import com.example.my_mobile_app.ui.BaseActivity;
import com.example.my_mobile_app.util.LocaleHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SettingsActivity extends BaseActivity {

    private TextView txtLanguageValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ImageButton btnBack = findViewById(R.id.btn_back);
        LinearLayout rowLanguage = findViewById(R.id.row_language);
        txtLanguageValue = findViewById(R.id.txt_language_value);
        TextView txtVersion = findViewById(R.id.txt_version);

        btnBack.setOnClickListener(v -> finish());
        rowLanguage.setOnClickListener(v -> showLanguageDialog());
        updateLanguageValue();
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            txtVersion.setText(info.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            txtVersion.setText("--");
        }
    }

    private void updateLanguageValue() {
        String language = LocaleHelper.getLanguage(this);
        txtLanguageValue.setText(LocaleHelper.LANG_EN.equals(language)
                ? R.string.settings_language_english
                : R.string.settings_language_vietnamese);
    }

    private void showLanguageDialog() {
        String[] codes = {LocaleHelper.LANG_VI, LocaleHelper.LANG_EN};
        String[] labels = {
                getString(R.string.settings_language_vietnamese),
                getString(R.string.settings_language_english)
        };
        String current = LocaleHelper.getLanguage(this);
        int checked = LocaleHelper.LANG_EN.equals(current) ? 1 : 0;

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.settings_choose_language)
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    String selected = codes[which];
                    dialog.dismiss();
                    if (selected.equals(current)) return;
                    LocaleHelper.setLanguage(this, selected);
                    restartApp();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void restartApp() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
