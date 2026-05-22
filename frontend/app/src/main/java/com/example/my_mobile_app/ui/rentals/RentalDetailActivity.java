package com.example.my_mobile_app.ui.rentals;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.my_mobile_app.R;
import com.example.my_mobile_app.api.ApiClient;
import com.example.my_mobile_app.api.ApiResponse;
import com.example.my_mobile_app.api.RentalService;
import com.example.my_mobile_app.model.Rental;
import com.example.my_mobile_app.ui.BaseActivity;
import com.example.my_mobile_app.util.DateUtils;
import com.example.my_mobile_app.util.PriceFormatter;
import com.example.my_mobile_app.util.StatusUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Rental detail screen: calls GET /rentals/{rentalId}. */
public class RentalDetailActivity extends BaseActivity {

    public static final String EXTRA_RENTAL_ID = "rental_id";

    private ImageView imgAsset;
    private TextView txtName, txtBrand, txtStatus, txtPeriod, txtReturnDate;
    private TextView txtDeposit, txtRentTotal, txtPenalty, txtGrandTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requireLogin()) return;
        setContentView(R.layout.activity_rental_detail);

        ImageButton btnBack = findViewById(R.id.btn_back);
        imgAsset = findViewById(R.id.img_asset);
        txtName = findViewById(R.id.txt_asset_name);
        txtBrand = findViewById(R.id.txt_asset_brand);
        txtStatus = findViewById(R.id.txt_rental_status);
        txtPeriod = findViewById(R.id.txt_rental_period);
        txtReturnDate = findViewById(R.id.txt_return_date);
        txtDeposit = findViewById(R.id.txt_deposit);
        txtRentTotal = findViewById(R.id.txt_rent_total);
        txtPenalty = findViewById(R.id.txt_penalty);
        txtGrandTotal = findViewById(R.id.txt_grand_total);

        btnBack.setOnClickListener(v -> finish());
        String rentalId = getIntent().getStringExtra(EXTRA_RENTAL_ID);
        if (rentalId == null || rentalId.isEmpty()) {
            showError(getString(R.string.error_missing_rental_id));
            finish();
            return;
        }
        load(rentalId);
    }

    private void load(String rentalId) {
        showLoading();
        ApiClient.get(this).create(RentalService.class).getRentalById(rentalId)
                .enqueue(new Callback<ApiResponse<Rental>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<Rental>> call,
                                                      @NonNull Response<ApiResponse<Rental>> response) {
                        hideLoading();
                        ApiResponse<Rental> body = response.body();
                        if (body == null || !body.success || body.data == null) {
                            showError(getString(R.string.error_rental_not_found));
                            return;
                        }
                        bind(body.data);
                    }

                    @Override public void onFailure(@NonNull Call<ApiResponse<Rental>> call,
                                                    @NonNull Throwable t) {
                        hideLoading();
                        showError(getString(R.string.error_load_rental_detail));
                    }
                });
    }

    private void bind(Rental rental) {
        txtName.setText(valueOrDash(rental.assetName));
        txtBrand.setText(valueOrDash(rental.assetBrand));

        StatusUtils.Style style = StatusUtils.forRental(this, rental.status);
        txtStatus.setText(style.label);
        txtStatus.setTextColor(getColor(style.colorRes));
        txtStatus.setBackgroundResource(style.chipBgRes);

        String start = DateUtils.formatDisplay(DateUtils.parseIso(rental.startDate));
        String end = DateUtils.formatDisplay(DateUtils.parseIso(rental.endDate));
        txtPeriod.setText(valueOrDash(start) + " - " + valueOrDash(end));
        txtReturnDate.setText(valueOrDash(DateUtils.formatDisplay(DateUtils.parseIso(rental.returnDate))));
        txtDeposit.setText(getString(R.string.rental_deposit_format, PriceFormatter.format(rental.depositFee)));
        txtRentTotal.setText(getString(R.string.rental_fee_format, PriceFormatter.format(rental.totalRentFee)));
        txtPenalty.setText(getString(R.string.rental_penalty_format, PriceFormatter.format(rental.penaltyFee)));
        txtGrandTotal.setText(getString(R.string.rental_total_format,
                PriceFormatter.format(rental.depositFee + rental.totalRentFee + rental.penaltyFee)));

        if (rental.primaryImageUrl != null && !rental.primaryImageUrl.isEmpty()) {
            Glide.with(this).load(rental.primaryImageUrl).into(imgAsset);
        } else {
            imgAsset.setImageDrawable(null);
        }
    }

    private static String valueOrDash(String value) {
        return value == null || value.isEmpty() ? "--" : value;
    }
}
