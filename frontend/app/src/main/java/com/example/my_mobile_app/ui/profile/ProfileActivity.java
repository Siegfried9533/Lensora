package com.example.my_mobile_app.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.my_mobile_app.MainActivity;
import com.example.my_mobile_app.R;
import com.example.my_mobile_app.api.ApiClient;
import com.example.my_mobile_app.api.ApiResponse;
import com.example.my_mobile_app.api.AuthService;
import com.example.my_mobile_app.api.OrderService;
import com.example.my_mobile_app.api.RentalService;
import com.example.my_mobile_app.api.ReviewService;
import com.example.my_mobile_app.model.Order;
import com.example.my_mobile_app.model.Rental;
import com.example.my_mobile_app.model.User;
import com.example.my_mobile_app.ui.BaseActivity;
import com.example.my_mobile_app.ui.auth.LoginActivity;
import com.example.my_mobile_app.ui.cart.CartActivity;
import com.example.my_mobile_app.ui.notifications.NotificationsActivity;
import com.example.my_mobile_app.ui.transactions.TransactionsActivity;
import com.example.my_mobile_app.util.BottomNavHelper;
import com.example.my_mobile_app.util.TokenManager;
import com.example.my_mobile_app.util.UserManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Profile tab: shows cached user, refreshes /auth/me, and handles logout. */
public class ProfileActivity extends BaseActivity {

    private ImageView imgAvatar;
    private TextView txtInitial, txtName, txtEmail;
    private TextView txtOrderCount, txtRentalCount, txtReviewCount;
    private LinearLayout accountMenu, settingsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        BottomNavHelper.attachTo(this, bottomNav, R.id.nav_profile);

        if (TokenManager.getToken(this) == null) {
            showGuestState();
            return;
        }

        findViewById(R.id.profile_content).setVisibility(View.VISIBLE);
        findViewById(R.id.guest_state).setVisibility(View.GONE);

        imgAvatar = findViewById(R.id.img_avatar);
        txtInitial = findViewById(R.id.txt_avatar_initial);
        txtName = findViewById(R.id.txt_user_name);
        txtEmail = findViewById(R.id.txt_user_email);
        txtOrderCount = findViewById(R.id.txt_order_count);
        txtRentalCount = findViewById(R.id.txt_rental_count);
        txtReviewCount = findViewById(R.id.txt_review_count);
        accountMenu = findViewById(R.id.account_menu);
        settingsMenu = findViewById(R.id.settings_menu);

        findViewById(R.id.stat_orders).setOnClickListener(v -> openTransactions(0));
        findViewById(R.id.stat_rentals).setOnClickListener(v -> openTransactions(1));

        bindUser(UserManager.getUser(this));
        buildMenus();
        refreshUser();
        refreshStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (txtOrderCount != null && TokenManager.getToken(this) != null) {
            refreshStats();
        }
    }

    private void showGuestState() {
        findViewById(R.id.profile_content).setVisibility(View.GONE);
        findViewById(R.id.guest_state).setVisibility(View.VISIBLE);
        MaterialButton signIn = findViewById(R.id.btn_guest_sign_in);
        signIn.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
    }

    private void refreshUser() {
        ApiClient.get(this).create(AuthService.class).getCurrentUser()
                .enqueue(new Callback<ApiResponse<User>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<User>> call,
                                                      @NonNull Response<ApiResponse<User>> response) {
                        ApiResponse<User> body = response.body();
                        if (body != null && body.success && body.data != null) {
                            UserManager.saveUser(ProfileActivity.this, body.data);
                            bindUser(body.data);
                        }
                    }

                    @Override public void onFailure(@NonNull Call<ApiResponse<User>> call,
                                                    @NonNull Throwable t) {
                        // Cached user is enough for this screen.
                    }
                });
    }

    private void bindUser(User user) {
        if (user == null) return;
        String name = valueOrDash(user.userName);
        txtName.setText(name);
        txtEmail.setText(valueOrDash(user.email));
        String initial = name.equals("--") ? "?" : name.substring(0, 1).toUpperCase();
        txtInitial.setText(initial);
        if (user.avatarUrl != null && !user.avatarUrl.isEmpty()) {
            txtInitial.setVisibility(View.GONE);
            imgAvatar.setVisibility(View.VISIBLE);
            Glide.with(this).load(user.avatarUrl).circleCrop().into(imgAvatar);
        } else {
            txtInitial.setVisibility(View.VISIBLE);
            imgAvatar.setVisibility(View.GONE);
        }
    }

    private void refreshStats() {
        loadOrderCount();
        loadRentalCount();
        loadReviewCount();
    }

    private void loadOrderCount() {
        ApiClient.get(this).create(OrderService.class).getOrders()
                .enqueue(new Callback<ApiResponse<List<Order>>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<List<Order>>> call,
                                                      @NonNull Response<ApiResponse<List<Order>>> response) {
                        ApiResponse<List<Order>> body = response.body();
                        int count = body != null && body.success && body.data != null
                                ? countDeliveredOrders(body.data)
                                : 0;
                        txtOrderCount.setText(String.valueOf(count));
                    }

                    @Override public void onFailure(@NonNull Call<ApiResponse<List<Order>>> call,
                                                    @NonNull Throwable t) {
                        txtOrderCount.setText("0");
                    }
                });
    }

    private void loadRentalCount() {
        ApiClient.get(this).create(RentalService.class).getRentals()
                .enqueue(new Callback<ApiResponse<List<Rental>>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<List<Rental>>> call,
                                                      @NonNull Response<ApiResponse<List<Rental>>> response) {
                        ApiResponse<List<Rental>> body = response.body();
                        int count = body != null && body.success && body.data != null
                                ? countNonCancelledRentals(body.data)
                                : 0;
                        txtRentalCount.setText(String.valueOf(count));
                    }

                    @Override public void onFailure(@NonNull Call<ApiResponse<List<Rental>>> call,
                                                    @NonNull Throwable t) {
                        txtRentalCount.setText("0");
                    }
                });
    }

    private void loadReviewCount() {
        ApiClient.get(this).create(ReviewService.class).getMyReviewCount()
                .enqueue(new Callback<ApiResponse<Integer>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<Integer>> call,
                                                      @NonNull Response<ApiResponse<Integer>> response) {
                        ApiResponse<Integer> body = response.body();
                        int count = body != null && body.success && body.data != null ? body.data : 0;
                        txtReviewCount.setText(String.valueOf(count));
                    }

                    @Override public void onFailure(@NonNull Call<ApiResponse<Integer>> call,
                                                    @NonNull Throwable t) {
                        txtReviewCount.setText("0");
                    }
                });
    }

    private void openTransactions(int tab) {
        Intent intent = new Intent(this, TransactionsActivity.class);
        intent.putExtra(TransactionsActivity.EXTRA_INITIAL_TAB, tab);
        startActivity(intent);
    }

    private void buildMenus() {
        accountMenu.removeAllViews();
        settingsMenu.removeAllViews();

        addMenuRow(accountMenu, R.drawable.ic_user_outline, getString(R.string.profile_personal_info),
                () -> startActivity(new Intent(this, PersonalInfoActivity.class)));
        addMenuRow(accountMenu, R.drawable.ic_box, getString(R.string.profile_address_book),
                () -> startActivity(new Intent(this, AddressBookActivity.class)));
        addMenuRow(accountMenu, R.drawable.ic_heart_outline, getString(R.string.profile_favorites),
                () -> startActivity(new Intent(this, FavoritesActivity.class)));
        addMenuRow(accountMenu, R.drawable.ic_box, getString(R.string.profile_my_equipment),
                () -> startActivity(new Intent(this, MyEquipmentActivity.class)));
        addMenuRow(accountMenu, R.drawable.ic_bell_outline, getString(R.string.profile_notifications),
                () -> startActivity(new Intent(this, NotificationsActivity.class)));

        addMenuRow(settingsMenu, R.drawable.ic_settings, getString(R.string.profile_settings),
                () -> startActivity(new Intent(this, SettingsActivity.class)));
        addMenuRow(settingsMenu, R.drawable.ic_lock_outline, getString(R.string.auth_change_password),
                () -> startActivity(new Intent(this, ChangePasswordActivity.class)));
        addMenuRow(settingsMenu, R.drawable.ic_logout, getString(R.string.auth_logout), this::logout);
    }

    private void addMenuRow(LinearLayout parent, int iconRes, String label, Runnable action) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_profile_menu, parent, false);
        ImageView imgIcon = row.findViewById(R.id.img_menu_icon);
        TextView txtLabel = row.findViewById(R.id.txt_menu_label);
        imgIcon.setImageResource(iconRes);
        txtLabel.setText(label);
        if (getString(R.string.auth_logout).equals(label)) {
            imgIcon.setColorFilter(getColor(R.color.red_500));
            txtLabel.setTextColor(getColor(R.color.red_500));
        }
        row.setOnClickListener(v -> {
            if (action != null) action.run();
            else showError(getString(R.string.profile_screen_coming_soon));
        });
        parent.addView(row);
    }

    private void logout() {
        TokenManager.clear(this);
        UserManager.clear(this);
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    private static String valueOrDash(String value) {
        return value == null || value.isEmpty() ? "--" : value;
    }

    private static int countDeliveredOrders(List<Order> orders) {
        int count = 0;
        for (Order order : orders) {
            if (order != null && isDelivered(order.status)) {
                count++;
            }
        }
        return count;
    }

    private static boolean isDelivered(String status) {
        return status != null && "DELIVERED".equalsIgnoreCase(status.trim());
    }

    private static int countNonCancelledRentals(List<Rental> rentals) {
        int count = 0;
        for (Rental rental : rentals) {
            if (rental != null && !isCancelled(rental.status)) {
                count++;
            }
        }
        return count;
    }

    private static boolean isCancelled(String status) {
        if (status == null) return false;
        String normalized = status.trim();
        return "CANCELLED".equalsIgnoreCase(normalized)
                || "CANCELED".equalsIgnoreCase(normalized);
    }
}
