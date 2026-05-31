package com.example.my_mobile_app.api;

import com.example.my_mobile_app.api.dto.AddToCartRequest;
import com.example.my_mobile_app.api.dto.UpdateQuantityRequest;
import com.example.my_mobile_app.model.CartItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/** Maps frontend/services/api/cartApi.ts. */
public interface CartService {

    @GET("cart")
    Call<ApiResponse<List<CartItem>>> getCartItems();

    @POST("cart/add")
    Call<ApiResponse<CartItem>> addToCart(@Body AddToCartRequest body);

    @DELETE("cart/{cartItemId}")
    Call<ApiResponse<Object>> removeFromCart(@Path("cartItemId") String cartItemId);

    @PUT("cart/{cartItemId}/quantity")
    Call<ApiResponse<CartItem>> updateQuantity(
            @Path("cartItemId") String cartItemId,
            @Body UpdateQuantityRequest body);

    @DELETE("cart")
    Call<ApiResponse<Object>> clearCart();
}
