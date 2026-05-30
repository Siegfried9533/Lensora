package com.example.my_mobile_app.api;

import com.example.my_mobile_app.model.Category;
import com.example.my_mobile_app.model.PaginatedResponse;
import com.example.my_mobile_app.model.Product;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Maps product + category endpoints in productApi.ts. */
public interface ProductService {

    @GET("products")
    Call<ApiResponse<PaginatedResponse<Product>>> getAllProducts(
            @Query("page") int page,
            @Query("size") int size);

    @GET("products/search")
    Call<ApiResponse<PaginatedResponse<Product>>> searchProducts(
            @Query("searchQuery") String searchQuery,
            @Query("categoryId") String categoryId,
            @Query("page") int page,
            @Query("size") int size);

    @GET("products/{id}")
    Call<ApiResponse<Product>> getProductById(@Path("id") String id);

    @GET("products/category/{categoryId}")
    Call<ApiResponse<PaginatedResponse<Product>>> getProductsByCategory(
            @Path("categoryId") String categoryId,
            @Query("page") int page,
            @Query("size") int size);

    @GET("categories")
    Call<ApiResponse<List<Category>>> getCategories();

    @GET("categories/by-type/{type}")
    Call<ApiResponse<List<Category>>> getCategoriesByType(@Path("type") String type);
}
