package com.example.my_mobile_app.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.my_mobile_app.R;
import com.example.my_mobile_app.api.ApiClient;
import com.example.my_mobile_app.api.ApiResponse;
import com.example.my_mobile_app.api.AssetService;
import com.example.my_mobile_app.api.CartService;
import com.example.my_mobile_app.api.FavoriteService;
import com.example.my_mobile_app.api.ProductService;
import com.example.my_mobile_app.model.Asset;
import com.example.my_mobile_app.model.CartItem;
import com.example.my_mobile_app.model.Category;
import com.example.my_mobile_app.model.Favorite;
import com.example.my_mobile_app.model.PaginatedResponse;
import com.example.my_mobile_app.model.Product;
import com.example.my_mobile_app.model.User;
import com.example.my_mobile_app.ui.BaseActivity;
import com.example.my_mobile_app.ui.auth.LoginActivity;
import com.example.my_mobile_app.ui.cart.CartActivity;
import com.example.my_mobile_app.ui.equipment.EquipmentDetailActivity;
import com.example.my_mobile_app.ui.profile.FavoritesActivity;
import com.example.my_mobile_app.ui.profile.ProfileActivity;
import com.example.my_mobile_app.util.BottomNavHelper;
import com.example.my_mobile_app.util.TokenManager;
import com.example.my_mobile_app.util.UserManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Discovery tab — mirrors {@code frontend/app/(tabs)/index.tsx}.
 *
 * Loads products + assets + categories + favorites in parallel, supports
 * BUY/RENT toggle, category chip filter, search (300ms debounce), and
 * pull-to-refresh. Card click currently shows a Toast TODO until
 * EquipmentDetailActivity (Phase 5) lands.
 */
public class HomeActivity extends BaseActivity
        implements ProductCardAdapter.OnItemClick, ProductCardAdapter.OnFavoriteClick {

    private static final int DEBOUNCE_MS = 300;

    // State
    private final List<DisplayItem> allItems = new ArrayList<>();
    private final List<DisplayItem> filteredItems = new ArrayList<>();
    private Set<String> favoriteIds = new HashSet<>();
    private String shopMode = "PRODUCT"; // PRODUCT | ASSET
    private String selectedCategoryId = CategoryChipAdapter.ALL_ID;
    private String searchQuery = "";

    // Views
    private RecyclerView rvProducts;
    private RecyclerView rvCategories;
    private SwipeRefreshLayout swipe;
    private MaterialButton btnBuy;
    private MaterialButton btnRent;
    private MaterialButton btnSignIn;
    private EditText inputSearch;
    private TextView cartBadge;
    private TextView txtWelcome;

    private ProductCardAdapter productAdapter;
    private CategoryChipAdapter categoryAdapter;

    private final Handler debounce = new Handler(Looper.getMainLooper());
    private Runnable pendingSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        rvProducts = findViewById(R.id.rv_products);
        rvCategories = findViewById(R.id.rv_categories);
        swipe = findViewById(R.id.swipe_refresh);
        btnBuy = findViewById(R.id.btn_buy);
        btnRent = findViewById(R.id.btn_rent);
        btnSignIn = findViewById(R.id.btn_sign_in);
        inputSearch = findViewById(R.id.input_search);
        cartBadge = findViewById(R.id.txt_cart_badge);
        txtWelcome = findViewById(R.id.txt_home_welcome);
        ImageButton btnCart = findViewById(R.id.btn_cart);
        ImageButton btnFavorites = findViewById(R.id.btn_favorites);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        productAdapter = new ProductCardAdapter(this, filteredItems, favoriteIds, this, this);
        rvProducts.setLayoutManager(new GridLayoutManager(this, 1));
        rvProducts.setAdapter(productAdapter);

        categoryAdapter = new CategoryChipAdapter(category -> {
            selectedCategoryId = category.categoryId;
            categoryAdapter.setSelectedId(selectedCategoryId);
            applyFilter();
        });
        rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        BottomNavHelper.attachTo(this, bottomNav, R.id.nav_discover);

        btnSignIn.setOnClickListener(v -> {
            Intent i = TokenManager.getToken(this) == null
                    ? new Intent(this, LoginActivity.class)
                    : new Intent(this, ProfileActivity.class);
            startActivity(i);
        });
        btnBuy.setOnClickListener(v -> setShopMode("PRODUCT"));
        btnRent.setOnClickListener(v -> setShopMode("ASSET"));
        updateModeButtons();

        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (pendingSearch != null) debounce.removeCallbacks(pendingSearch);
                pendingSearch = () -> {
                    searchQuery = s == null ? "" : s.toString().trim().toLowerCase(Locale.ROOT);
                    applyFilter();
                };
                debounce.postDelayed(pendingSearch, DEBOUNCE_MS);
            }
        });

        swipe.setOnRefreshListener(this::loadAll);

        btnCart.setOnClickListener(v -> {
            if (!requireLoginForAction()) return;
            startActivity(new android.content.Intent(this, CartActivity.class));
        });
        btnFavorites.setOnClickListener(v -> {
            if (!requireLoginForAction()) return;
            startActivity(new Intent(this, FavoritesActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateHeader();
        loadAll();
        refreshCartBadge();
    }

    private void updateHeader() {
        User user = UserManager.getUser(this);
        boolean loggedIn = TokenManager.getToken(this) != null;
        if (loggedIn && user != null && user.userName != null && !user.userName.isEmpty()) {
            txtWelcome.setText(getString(R.string.home_welcome_user, user.userName));
        } else {
            txtWelcome.setText(R.string.home_welcome_guest);
        }
        btnSignIn.setText(loggedIn ? getString(R.string.tab_profile) : getString(R.string.auth_login));
    }

    private void setShopMode(String mode) {
        if (mode.equals(shopMode)) return;
        shopMode = mode;
        selectedCategoryId = CategoryChipAdapter.ALL_ID;
        categoryAdapter.setSelectedId(selectedCategoryId);
        updateModeButtons();
        applyFilter();
    }

    private void updateModeButtons() {
        boolean buy = "PRODUCT".equals(shopMode);
        btnBuy.setBackgroundResource(buy ? R.drawable.bg_segment_active : R.drawable.bg_segment_inactive);
        btnBuy.setTextColor(getColor(buy ? R.color.black : R.color.nav_inactive));
        btnRent.setBackgroundResource(!buy ? R.drawable.bg_segment_active : R.drawable.bg_segment_inactive);
        btnRent.setTextColor(getColor(!buy ? R.color.black : R.color.nav_inactive));
    }

    private void loadAll() {
        swipe.setRefreshing(true);

        final List<Product> productsHolder = new ArrayList<>();
        final List<Asset> assetsHolder = new ArrayList<>();
        final List<Category> categoriesHolder = new ArrayList<>();
        final Set<String> favs = new HashSet<>();

        boolean loggedIn = TokenManager.getToken(this) != null;
        AtomicInteger remaining = new AtomicInteger(loggedIn ? 4 : 3);

        Runnable maybeRender = () -> {
            if (remaining.decrementAndGet() > 0) return;
            allItems.clear();
            for (Product p : productsHolder) allItems.add(DisplayItem.from(p));
            for (Asset a : assetsHolder) allItems.add(DisplayItem.from(a));
            favoriteIds = favs;
            productAdapter.setFavoriteIds(favoriteIds);
            categoryAdapter.setCategories(categoriesHolder);
            applyFilter();
            swipe.setRefreshing(false);
        };

        ProductService ps = ApiClient.get(this).create(ProductService.class);
        AssetService as = ApiClient.get(this).create(AssetService.class);

        ps.getAllProducts(0, 100).enqueue(new Callback<ApiResponse<PaginatedResponse<Product>>>() {
            @Override public void onResponse(@NonNull Call<ApiResponse<PaginatedResponse<Product>>> call,
                                              @NonNull Response<ApiResponse<PaginatedResponse<Product>>> response) {
                ApiResponse<PaginatedResponse<Product>> body = response.body();
                if (body != null && body.success && body.data != null && body.data.content != null) {
                    productsHolder.addAll(body.data.content);
                }
                maybeRender.run();
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<PaginatedResponse<Product>>> call, @NonNull Throwable t) {
                maybeRender.run();
            }
        });

        as.getAllAssets(0, 100).enqueue(new Callback<ApiResponse<PaginatedResponse<Asset>>>() {
            @Override public void onResponse(@NonNull Call<ApiResponse<PaginatedResponse<Asset>>> call,
                                              @NonNull Response<ApiResponse<PaginatedResponse<Asset>>> response) {
                ApiResponse<PaginatedResponse<Asset>> body = response.body();
                if (body != null && body.success && body.data != null && body.data.content != null) {
                    assetsHolder.addAll(body.data.content);
                }
                maybeRender.run();
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<PaginatedResponse<Asset>>> call, @NonNull Throwable t) {
                maybeRender.run();
            }
        });

        ps.getCategories().enqueue(new Callback<ApiResponse<List<Category>>>() {
            @Override public void onResponse(@NonNull Call<ApiResponse<List<Category>>> call,
                                              @NonNull Response<ApiResponse<List<Category>>> response) {
                ApiResponse<List<Category>> body = response.body();
                if (body != null && body.success && body.data != null) {
                    categoriesHolder.addAll(body.data);
                }
                maybeRender.run();
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<List<Category>>> call, @NonNull Throwable t) {
                maybeRender.run();
            }
        });

        if (loggedIn) {
            FavoriteService fs = ApiClient.get(this).create(FavoriteService.class);
            fs.getFavorites().enqueue(new Callback<ApiResponse<List<Favorite>>>() {
                @Override public void onResponse(@NonNull Call<ApiResponse<List<Favorite>>> call,
                                                  @NonNull Response<ApiResponse<List<Favorite>>> response) {
                    ApiResponse<List<Favorite>> body = response.body();
                    if (body != null && body.success && body.data != null) {
                        for (Favorite f : body.data) {
                            String id = "PRODUCT".equals(f.type) ? f.productId : f.assetId;
                            if (id != null) favs.add(id);
                        }
                    }
                    maybeRender.run();
                }
                @Override public void onFailure(@NonNull Call<ApiResponse<List<Favorite>>> call, @NonNull Throwable t) {
                    maybeRender.run();
                }
            });
        }
    }

    private void applyFilter() {
        filteredItems.clear();
        for (DisplayItem it : allItems) {
            if (!shopMode.equals(it.type)) continue;
            if (!CategoryChipAdapter.ALL_ID.equals(selectedCategoryId)
                    && !selectedCategoryId.equals(it.categoryId)) continue;
            if (!searchQuery.isEmpty()) {
                String title = it.title == null ? "" : it.title.toLowerCase(Locale.ROOT);
                String cat = it.categoryName == null ? "" : it.categoryName.toLowerCase(Locale.ROOT);
                if (!title.contains(searchQuery) && !cat.contains(searchQuery)) continue;
            }
            filteredItems.add(it);
        }
        productAdapter.setItems(filteredItems);
    }

    private void refreshCartBadge() {
        if (TokenManager.getToken(this) == null) {
            cartBadge.setVisibility(View.GONE);
            return;
        }
        CartService cs = ApiClient.get(this).create(CartService.class);
        cs.getCartItems().enqueue(new Callback<ApiResponse<List<CartItem>>>() {
            @Override public void onResponse(@NonNull Call<ApiResponse<List<CartItem>>> call,
                                              @NonNull Response<ApiResponse<List<CartItem>>> response) {
                ApiResponse<List<CartItem>> body = response.body();
                int count = 0;
                if (body != null && body.success && body.data != null) {
                    for (CartItem c : body.data) count += c.quantity;
                }
                if (count <= 0) {
                    cartBadge.setVisibility(View.GONE);
                } else {
                    cartBadge.setVisibility(View.VISIBLE);
                    cartBadge.setText(count > 99 ? "99+" : String.valueOf(count));
                }
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<List<CartItem>>> call, @NonNull Throwable t) {
                cartBadge.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onItemClick(DisplayItem item) {
        android.content.Intent i = new android.content.Intent(this, EquipmentDetailActivity.class);
        i.putExtra(EquipmentDetailActivity.EXTRA_ID, item.id);
        i.putExtra(EquipmentDetailActivity.EXTRA_TYPE, item.type);
        startActivity(i);
    }

    @Override
    public void onFavoriteClick(DisplayItem item) {
        if (!requireLoginForAction()) return;
        java.util.HashMap<String, String> body = new java.util.HashMap<>();
        body.put("itemId", item.id);
        body.put("type", item.type);
        FavoriteService fs = ApiClient.get(this).create(FavoriteService.class);
        fs.toggleFavorite(body).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override public void onResponse(@NonNull Call<ApiResponse<Map<String, Object>>> call,
                                              @NonNull Response<ApiResponse<Map<String, Object>>> response) {
                ApiResponse<Map<String, Object>> b = response.body();
                if (b == null || !b.success) return;
                if (favoriteIds.contains(item.id)) favoriteIds.remove(item.id);
                else favoriteIds.add(item.id);
                productAdapter.setFavoriteIds(favoriteIds);
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                showError(getString(R.string.error_update_favorite));
            }
        });
    }
}
