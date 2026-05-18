package com.example.my_mobile_app.ui.profile;

import android.os.Bundle;
import android.widget.ImageButton;

import com.example.my_mobile_app.R;
import com.example.my_mobile_app.ui.BaseActivity;

public class HelpActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());
    }
}
