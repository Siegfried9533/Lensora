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
import com.example.my_mobile_app.model.User;
import com.example.my_mobile_app.ui.BaseActivity;
import com.example.my_mobile_app.ui.auth.LoginActivity;
import com.example.my_mobile_app.ui.cart.CartActivity;
import com.example.my_mobile_app.ui.notifications.NotificationsActivity;
import com.example.my_mobile_app.util.BottomNavHelper;
import com.example.my_mobile_app.util.TokenManager;
import com.example.my_mobile_app.util.UserManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Profile tab: shows cached user, refreshes /auth/me, and handles logout. */
public class ProfileActivity extends BaseActivity {

    private ImageView imgAvatar;
    private TextView txtInitial, txtName, txtEmail, txtTrustScore, txtTrustLabel;
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
        txtTrustScore = findViewById(R.id.txt_trust_score);
        txtTrustLabel = findViewById(R.id.txt_trust_label);
        accountMenu = findViewById(R.id.account_menu);
        settingsMenu = findViewById(R.id.settings_menu);

        bindUser(UserManager.getUser(this));
        buildMenus();
        refreshUser();
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
        String score = String.valueOf((int) user.trustScore);
        txtTrustScore.setText(score);
        txtTrustLabel.setText("Trust Score: " + score);
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

    private void buildMenus() {
        accountMenu.removeAllViews();
        settingsMenu.removeAllViews();

        addMenuRow(accountMenu, R.drawable.ic_user_outline, getString(R.string.profile_personal_info),
                () -> startActivity(new Intent(this, PersonalInfoActivity.class)));
        addMenuRow(accountMenu, R.drawable.ic_heart_outline, getString(R.string.profile_favorites),
                () -> startActivity(new Intent(this, FavoritesActivity.class)));
        addMenuRow(accountMenu, R.drawable.ic_box, getString(R.string.profile_my_equipment),
                () -> startActivity(new Intent(this, MyEquipmentActivity.class)));
        addMenuRow(accountMenu, R.drawable.ic_bell_outline, "Notifications",
                () -> startActivity(new Intent(this, NotificationsActivity.class)));

        addMenuRow(settingsMenu, R.drawable.ic_settings, getString(R.string.profile_settings),
                () -> startActivity(new Intent(this, SettingsActivity.class)));
        addMenuRow(settingsMenu, R.drawable.ic_lock_outline, getString(R.string.auth_change_password),
                () -> startActivity(new Intent(this, ChangePasswordActivity.class)));
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
            else showError("Màn hình này đang được triển khai");
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
}
