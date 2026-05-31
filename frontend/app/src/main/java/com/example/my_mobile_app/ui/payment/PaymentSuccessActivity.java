package com.example.my_mobile_app.ui.payment;

import android.content.Intent;
import android.os.Bundle;

import com.example.my_mobile_app.R;
import com.example.my_mobile_app.ui.BaseActivity;
import com.example.my_mobile_app.ui.home.HomeActivity;
import com.example.my_mobile_app.ui.orders.OrderDetailActivity;
import com.example.my_mobile_app.ui.rentals.RentalDetailActivity;
import com.google.android.material.button.MaterialButton;

/** Confirmation screen shown after a successful order/rental purchase. */
public class PaymentSuccessActivity extends BaseActivity {

    public static final String EXTRA_ORDER_ID = "order_id";
    public static final String EXTRA_RENTAL_ID = "rental_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_success);

        MaterialButton btnViewOrder = findViewById(R.id.btn_view_order);
        MaterialButton btnHome = findViewById(R.id.btn_home);
        String rentalId = getIntent().getStringExtra(EXTRA_RENTAL_ID);
        if (rentalId != null && !rentalId.isEmpty()) {
            btnViewOrder.setText(R.string.payment_view_rental);
        }

        btnViewOrder.setOnClickListener(v -> {
            String orderId = getIntent().getStringExtra(EXTRA_ORDER_ID);
            if (orderId != null && !orderId.isEmpty()) {
                startActivity(new Intent(this, OrderDetailActivity.class)
                        .putExtra(OrderDetailActivity.EXTRA_ORDER_ID, orderId));
            } else if (rentalId != null && !rentalId.isEmpty()) {
                startActivity(new Intent(this, RentalDetailActivity.class)
                        .putExtra(RentalDetailActivity.EXTRA_RENTAL_ID, rentalId));
            } else {
                goHome();
            }
        });
        btnHome.setOnClickListener(v -> goHome());
    }

    private void goHome() {
        Intent i = new Intent(this, HomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
