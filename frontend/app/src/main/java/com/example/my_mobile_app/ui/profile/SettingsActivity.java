package com.example.my_mobile_app.ui.profile;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.my_mobile_app.R;
import com.example.my_mobile_app.ui.BaseActivity;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ImageButton btnBack = findViewById(R.id.btn_back);
        TextView txtVersion = findViewById(R.id.txt_version);

        btnBack.setOnClickListener(v -> finish());
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            txtVersion.setText(info.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            txtVersion.setText("--");
        }
    }
}
