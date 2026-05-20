package com.example.my_mobile_app.api;

import com.example.my_mobile_app.model.Asset;
import com.example.my_mobile_app.model.PaginatedResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Maps asset endpoints in productApi.ts. */
public interface AssetService {

    @GET("assets")
    Call<ApiResponse<PaginatedResponse<Asset>>> getAllAssets(
            @Query("page") int page,
            @Query("size") int size);

    @GET("assets/search")
    Call<ApiResponse<PaginatedResponse<Asset>>> searchAssets(
            @Query("searchQuery") String searchQuery,
            @Query("categoryId") String categoryId,
            @Query("status") String status,
            @Query("page") int page,
            @Query("size") int size);

    @GET("assets/{id}")
    Call<ApiResponse<Asset>> getAssetById(@Path("id") String id);
}
