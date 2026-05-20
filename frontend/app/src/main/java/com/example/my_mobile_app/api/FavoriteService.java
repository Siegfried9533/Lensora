package com.example.my_mobile_app.api;

import com.example.my_mobile_app.model.Favorite;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/** Maps frontend/services/api/favoriteApi.ts. */
public interface FavoriteService {

    @GET("favorites")
    Call<ApiResponse<List<Favorite>>> getFavorites();

    /** Body: {itemId, type}. Returns {action, favorite?}. */
    @POST("favorites/toggle")
    Call<ApiResponse<Map<String, Object>>> toggleFavorite(@Body Map<String, String> body);

    @GET("favorites/check")
    Call<ApiResponse<Map<String, Boolean>>> isFavorite(
            @Query("itemId") String itemId,
            @Query("type") String type);
}
