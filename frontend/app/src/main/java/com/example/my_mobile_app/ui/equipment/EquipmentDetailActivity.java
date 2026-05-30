package com.example.my_mobile_app.ui.equipment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.my_mobile_app.R;
import com.example.my_mobile_app.api.ApiClient;
import com.example.my_mobile_app.api.ApiResponse;
import com.example.my_mobile_app.api.AssetService;
import com.example.my_mobile_app.api.CartService;
import com.example.my_mobile_app.api.FavoriteService;
import com.example.my_mobile_app.api.ProductService;
import com.example.my_mobile_app.api.dto.AddToCartRequest;
import com.example.my_mobile_app.model.Asset;
import com.example.my_mobile_app.model.Product;
import com.example.my_mobile_app.ui.BaseActivity;
import com.example.my_mobile_app.ui.checkout.CheckoutActivity;
import com.example.my_mobile_app.util.PriceFormatter;
import com.example.my_mobile_app.util.TokenManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Equipment detail screen — mirrors {@code frontend/app/equipment/[id].tsx}.
 *
 * Receives extras: {@code id} (String), {@code type} ("PRODUCT"|"ASSET").
 * Loads the corresponding product or asset, displays carousel + info,
 * and adds to cart on confirm.
 */
public class EquipmentDetailActivity extends BaseActivity {

    public static final String EXTRA_ID = "id";
    public static final String EXTRA_TYPE = "type";

    private static final SimpleDateFormat DISPLAY_FMT =
            new SimpleDateFormat("dd/MM/yyyy", Locale.US);
    private static final SimpleDateFormat ISO_FMT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
    static { ISO_FMT.setTimeZone(TimeZone.getTimeZone("UTC")); }

    private String itemId;
    private String itemType; // PRODUCT | ASSET
    private Product product;
    private Asset asset;
    private int qty = 1;
    private long startMillis;
    private long endMillis;
    private boolean isFavorite;

    private ViewPager2 vpImages;
    private LinearLayout dots;
    private TextView txtBrand, txtTitle, txtPrice, txtPriceUnit, txtDescription;
    private TextView txtRentalTotal, txtQty;
    private LinearLayout rentalSection, qtySection, specsList, specsContainer;
    private MaterialButton btnAction, btnAddCart, btnStartDate, btnEndDate;
    private ImageButton btnFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipment_detail);

        itemId = getIntent().getStringExtra(EXTRA_ID);
        itemType = getIntent().getStringExtra(EXTRA_TYPE);
        if (itemId == null || itemType == null) {
            showError(getString(R.string.error_missing_item_info));
            finish();
            return;
        }

        vpImages = findViewById(R.id.vp_images);
        dots = findViewById(R.id.dots);
        txtBrand = findViewById(R.id.txt_brand);
        txtTitle = findViewById(R.id.txt_title);
        txtPrice = findViewById(R.id.txt_price);
        txtPriceUnit = findViewById(R.id.txt_price_unit);
        txtDescription = findViewById(R.id.txt_description);
        txtRentalTotal = findViewById(R.id.txt_rental_total);
        txtQty = findViewById(R.id.txt_qty);
        rentalSection = findViewById(R.id.rental_section);
        qtySection = findViewById(R.id.qty_section);
        specsList = findViewById(R.id.specs_list);
        specsContainer = findViewById(R.id.specs_container);
        btnAction = findViewById(R.id.btn_action);
        btnAddCart = findViewById(R.id.btn_add_cart);
        btnStartDate = findViewById(R.id.btn_start_date);
        btnEndDate = findViewById(R.id.btn_end_date);
        btnFavorite = findViewById(R.id.btn_favorite);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        btnFavorite.setOnClickListener(v -> toggleFavorite());

        // Default rental dates: today → today + 1 day.
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0);
        startMillis = c.getTimeInMillis();
        c.add(Calendar.DAY_OF_MONTH, 1);
        endMillis = c.getTimeInMillis();

        btnStartDate.setOnClickListener(v -> pickDate(true));
        btnEndDate.setOnClickListener(v -> pickDate(false));
        findViewById(R.id.btn_qty_minus).setOnClickListener(v -> changeQty(-1));
        findViewById(R.id.btn_qty_plus).setOnClickListener(v -> changeQty(1));

        btnAction.setOnClickListener(v -> {
            if ("PRODUCT".equals(itemType)) {
                buyNow();
            } else {
                rentNow();
            }
        });
        btnAddCart.setOnClickListener(v -> addToCart());

        loadItem();
    }

    private void loadItem() {
        showLoading();
        if ("PRODUCT".equals(itemType)) {
            ApiClient.get(this).create(ProductService.class).getProductById(itemId)
                    .enqueue(new Callback<ApiResponse<Product>>() {
                        @Override public void onResponse(@NonNull Call<ApiResponse<Product>> call, @NonNull Response<ApiResponse<Product>> response) {
                            hideLoading();
                            ApiResponse<Product> body = response.body();
                            if (body == null || !body.success || body.data == null) {
                                showError(getString(R.string.error_product_not_found));
                                finish();
                                return;
                            }
                            product = body.data;
                            bindProduct();
                        }
                        @Override public void onFailure(@NonNull Call<ApiResponse<Product>> call, @NonNull Throwable t) {
                            hideLoading();
                            showError(getString(R.string.error_load_product));
                            finish();
                        }
                    });
        } else {
            ApiClient.get(this).create(AssetService.class).getAssetById(itemId)
                    .enqueue(new Callback<ApiResponse<Asset>>() {
                        @Override public void onResponse(@NonNull Call<ApiResponse<Asset>> call, @NonNull Response<ApiResponse<Asset>> response) {
                            hideLoading();
                            ApiResponse<Asset> body = response.body();
                            if (body == null || !body.success || body.data == null) {
                                showError(getString(R.string.error_asset_not_found));
                                finish();
                                return;
                            }
                            asset = body.data;
                            bindAsset();
                        }
                        @Override public void onFailure(@NonNull Call<ApiResponse<Asset>> call, @NonNull Throwable t) {
                            hideLoading();
                            showError(getString(R.string.error_load_asset));
                            finish();
                        }
                    });
        }
        refreshFavoriteState();
    }

    private void bindProduct() {
        setImages(product.imageUrls, product.primaryImageUrl);
        txtBrand.setText(product.brand == null ? "" : product.brand.toUpperCase(Locale.ROOT));
        txtTitle.setText(product.productName);
        txtPrice.setText(PriceFormatter.format(product.price));
        txtPriceUnit.setVisibility(View.GONE);
        txtDescription.setText(product.description == null ? "" : product.description);
        rentalSection.setVisibility(View.GONE);
        qtySection.setVisibility(View.VISIBLE);
        renderSpecs(new String[][]{
                {getString(R.string.equipment_brand), product.brand},
                {getString(R.string.equipment_category), product.categoryName},
                {getString(R.string.equipment_stock), String.valueOf(product.stockQuantity)},
        });
        btnAddCart.setVisibility(View.VISIBLE);
        btnAddCart.setEnabled(true);
        btnAction.setEnabled(true);
        btnAction.setText(R.string.action_buy_now);
    }

    private void bindAsset() {
        setImages(asset.imageUrls, asset.primaryImageUrl);
        txtBrand.setText(asset.brand == null ? "" : asset.brand.toUpperCase(Locale.ROOT));
        txtTitle.setText(asset.modelName);
        txtPrice.setText(PriceFormatter.format(asset.dailyRate));
        txtPriceUnit.setVisibility(View.VISIBLE);
        txtDescription.setText(asset.description == null ? "" : asset.description);
        rentalSection.setVisibility(View.VISIBLE);
        qtySection.setVisibility(View.GONE);
        renderSpecs(new String[][]{
                {getString(R.string.equipment_brand), asset.brand},
                {getString(R.string.equipment_category), asset.categoryName},
                {getString(R.string.equipment_status), asset.status},
        });
        updateDateButtons();
        btnAddCart.setVisibility(View.GONE);
        btnAction.setText(R.string.action_rent_now);
        if (!"AVAILABLE".equals(asset.status)) {
            btnAction.setEnabled(false);
            btnAction.setText(R.string.equipment_unavailable);
        } else {
            btnAction.setEnabled(true);
        }
    }

    private void renderSpecs(String[][] specs) {
        specsList.removeAllViews();
        boolean any = false;
        for (String[] kv : specs) {
            if (kv[1] == null || kv[1].isEmpty()) continue;
            any = true;
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rp.topMargin = 12;
            row.setLayoutParams(rp);

            TextView label = new TextView(this);
            label.setText(kv[0]);
            label.setTextColor(getColor(R.color.text_muted));
            label.setTextSize(14);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            row.addView(label, lp);

            TextView value = new TextView(this);
            value.setText(kv[1]);
            value.setTextColor(getColor(R.color.white));
            value.setTextSize(14);
            row.addView(value, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            specsList.addView(row);
        }
        specsContainer.setVisibility(any ? View.VISIBLE : View.GONE);
    }

    private void setImages(List<String> urls, String primary) {
        List<String> all = new ArrayList<>();
        if (urls != null) all.addAll(urls);
        if (all.isEmpty() && primary != null && !primary.isEmpty()) all.add(primary);

        vpImages.setAdapter(new ImagePagerAdapter(all));

        dots.removeAllViews();
        if (all.size() > 1) {
            for (int i = 0; i < all.size(); i++) {
                View dot = LayoutInflater.from(this).inflate(R.layout.item_image_dot, dots, false);
                dot.setBackgroundResource(i == 0 ? R.drawable.bg_dot_active : R.drawable.bg_dot_inactive);
                dots.addView(dot);
            }
            vpImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override public void onPageSelected(int position) {
                    for (int i = 0; i < dots.getChildCount(); i++) {
                        dots.getChildAt(i).setBackgroundResource(
                                i == position ? R.drawable.bg_dot_active : R.drawable.bg_dot_inactive);
                    }
                }
            });
        }
    }

    private void changeQty(int delta) {
        int newQty = qty + delta;
        if (newQty < 1) return;
        if (product != null && product.stockQuantity > 0 && newQty > product.stockQuantity) {
            showError(getString(R.string.error_out_of_stock));
            return;
        }
        qty = newQty;
        txtQty.setText(String.valueOf(qty));
    }

    private void pickDate(boolean isStart) {
        long initial = isStart ? startMillis : endMillis;
        long minDate = isStart ? today() : (startMillis + 24L * 3600 * 1000);

        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.from(minDate))
                .build();

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(isStart
                        ? getString(R.string.equipment_select_start_date)
                        : getString(R.string.equipment_select_return_date))
                .setSelection(initial)
                .setCalendarConstraints(constraints)
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            if (isStart) {
                startMillis = selection;
                if (endMillis <= startMillis) {
                    endMillis = startMillis + 24L * 3600 * 1000;
                }
            } else {
                if (selection <= startMillis) {
                    showError(getString(R.string.error_return_after_start));
                    return;
                }
                endMillis = selection;
            }
            updateDateButtons();
        });
        picker.show(getSupportFragmentManager(), "date_picker");
    }

    private long today() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private void updateDateButtons() {
        btnStartDate.setText(getString(R.string.equipment_start_date_format,
                DISPLAY_FMT.format(new Date(startMillis))));
        btnEndDate.setText(getString(R.string.equipment_end_date_format,
                DISPLAY_FMT.format(new Date(endMillis))));
        if (asset != null) {
            long days = Math.max(1, (endMillis - startMillis) / (24L * 3600 * 1000));
            double total = asset.dailyRate * days;
            txtRentalTotal.setText(getString(R.string.equipment_rental_total_format,
                    (int) days, PriceFormatter.format(total)));
        }
    }

    private void refreshFavoriteState() {
        if (TokenManager.getToken(this) == null) return;
        FavoriteService fs = ApiClient.get(this).create(FavoriteService.class);
        fs.isFavorite(itemId, itemType).enqueue(new Callback<ApiResponse<Map<String, Boolean>>>() {
            @Override public void onResponse(@NonNull Call<ApiResponse<Map<String, Boolean>>> call, @NonNull Response<ApiResponse<Map<String, Boolean>>> response) {
                ApiResponse<Map<String, Boolean>> body = response.body();
                if (body == null || !body.success || body.data == null) return;
                Boolean fav = body.data.get("isFavorite");
                if (fav == null) fav = body.data.get("favorite");
                isFavorite = fav != null && fav;
                btnFavorite.setImageResource(isFavorite ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<Map<String, Boolean>>> call, @NonNull Throwable t) {}
        });
    }

    private void toggleFavorite() {
        if (TokenManager.getToken(this) == null) {
            showError(getString(R.string.error_login_required));
            return;
        }
        Map<String, String> body = new HashMap<>();
        body.put("itemId", itemId);
        body.put("type", itemType);
        ApiClient.get(this).create(FavoriteService.class).toggleFavorite(body)
                .enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<Map<String, Object>>> call, @NonNull Response<ApiResponse<Map<String, Object>>> response) {
                        ApiResponse<Map<String, Object>> b = response.body();
                        if (b == null || !b.success) return;
                        isFavorite = !isFavorite;
                        btnFavorite.setImageResource(isFavorite ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
                    }
                    @Override public void onFailure(@NonNull Call<ApiResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                        showError(getString(R.string.error_update_favorite));
                    }
                });
    }

    private void buyNow() {
        if (TokenManager.getToken(this) == null) {
            showError(getString(R.string.error_login_required));
            return;
        }
        if (product == null) return;
        if (product.stockQuantity > 0 && qty > product.stockQuantity) {
            showError(getString(R.string.error_out_of_stock));
            return;
        }

        Intent i = new Intent(this, CheckoutActivity.class);
        i.putExtra(CheckoutActivity.EXTRA_MODE, CheckoutActivity.MODE_PRODUCT);
        i.putExtra(CheckoutActivity.EXTRA_ITEM_ID, product.productId);
        i.putExtra(CheckoutActivity.EXTRA_ITEM_NAME, product.productName);
        i.putExtra(CheckoutActivity.EXTRA_ITEM_PRICE, product.price);
        i.putExtra(CheckoutActivity.EXTRA_ITEM_QUANTITY, qty);
        startActivity(i);
    }

    private void rentNow() {
        if (TokenManager.getToken(this) == null) {
            showError(getString(R.string.error_login_required));
            return;
        }
        if (asset == null) return;
        if (endMillis <= startMillis) {
            showError(getString(R.string.error_return_after_start));
            return;
        }

        Intent i = new Intent(this, CheckoutActivity.class);
        i.putExtra(CheckoutActivity.EXTRA_MODE, CheckoutActivity.MODE_RENTAL);
        i.putExtra(CheckoutActivity.EXTRA_ITEM_ID, asset.assetId);
        i.putExtra(CheckoutActivity.EXTRA_ITEM_NAME, asset.modelName);
        i.putExtra(CheckoutActivity.EXTRA_ITEM_PRICE, asset.dailyRate);
        i.putExtra(CheckoutActivity.EXTRA_RENTAL_START_MILLIS, startMillis);
        i.putExtra(CheckoutActivity.EXTRA_RENTAL_END_MILLIS, endMillis);
        startActivity(i);
    }

    private void addToCart() {
        if (TokenManager.getToken(this) == null) {
            showError(getString(R.string.error_login_required));
            return;
        }
        AddToCartRequest req;
        if ("PRODUCT".equals(itemType)) {
            req = new AddToCartRequest(itemId, "PRODUCT", qty);
        } else {
            if (endMillis <= startMillis) {
                showError(getString(R.string.error_return_after_start));
                return;
            }
            req = new AddToCartRequest(itemId, "ASSET", 1,
                    ISO_FMT.format(new Date(startMillis)),
                    ISO_FMT.format(new Date(endMillis)));
        }
        btnAction.setEnabled(false);
        btnAddCart.setEnabled(false);
        ApiClient.get(this).create(CartService.class).addToCart(req)
                .enqueue(new Callback<ApiResponse<com.example.my_mobile_app.model.CartItem>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<com.example.my_mobile_app.model.CartItem>> call, @NonNull Response<ApiResponse<com.example.my_mobile_app.model.CartItem>> response) {
                        btnAction.setEnabled(true);
                        btnAddCart.setEnabled(true);
                        ApiResponse<com.example.my_mobile_app.model.CartItem> b = response.body();
                        if (b == null || !b.success) {
                            showError(b != null && b.message != null
                                    ? b.message
                                    : getString(R.string.error_add_to_cart));
                            return;
                        }
                        showSuccess(getString(R.string.success_added_to_cart));
                        finish();
                    }
                    @Override public void onFailure(@NonNull Call<ApiResponse<com.example.my_mobile_app.model.CartItem>> call, @NonNull Throwable t) {
                        btnAction.setEnabled(true);
                        btnAddCart.setEnabled(true);
                        showError(getString(R.string.error_server_connection));
                    }
                });
    }

    /** Trivial Glide-powered ViewPager2 adapter for image URLs. */
    private class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.VH> {
        private final List<String> urls;
        ImagePagerAdapter(List<String> urls) { this.urls = urls; }

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView iv = new ImageView(parent.getContext());
            iv.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return new VH(iv);
        }

        @Override public void onBindViewHolder(@NonNull VH holder, int position) {
            String url = urls.get(position);
            Glide.with(holder.itemView.getContext()).load(url).into((ImageView) holder.itemView);
        }

        @Override public int getItemCount() { return urls == null ? 0 : urls.size(); }

        class VH extends RecyclerView.ViewHolder {
            VH(View v) { super(v); }
        }
    }
}
