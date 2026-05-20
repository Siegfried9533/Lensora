package com.example.my_mobile_app.ui.payment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.my_mobile_app.R;
import com.example.my_mobile_app.ui.BaseActivity;
import com.example.my_mobile_app.ui.home.HomeActivity;
import com.google.android.material.button.MaterialButton;

/** Confirmation screen shown after a failed payment attempt. */
public class PaymentFailedActivity extends BaseActivity {

    public static final String EXTRA_REASON = "reason";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_failed);

        String reason = getIntent().getStringExtra(EXTRA_REASON);
        if (reason != null && !reason.isEmpty()) {
            ((TextView) findViewById(R.id.txt_reason)).setText(reason);
        }

        findViewById(R.id.btn_retry).setOnClickListener(v -> finish());
        ((MaterialButton) findViewById(R.id.btn_home)).setOnClickListener(v -> {
            Intent i = new Intent(this, HomeActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            finishAffinity();
        });
    }
}
