package com.example.my_mobile_app.api;

import com.example.my_mobile_app.api.dto.CreateRentalRequest;
import com.example.my_mobile_app.model.Rental;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Maps rental endpoints in orderApi.ts + rentalApi.ts. */
public interface RentalService {

    @POST("rentals")
    Call<ApiResponse<Rental>> createRental(@Body CreateRentalRequest body);

    @GET("rentals")
    Call<ApiResponse<List<Rental>>> getRentals();

    @GET("rentals/{rentalId}")
    Call<ApiResponse<Rental>> getRentalById(@Path("rentalId") String rentalId);

    @GET("rentals/check-availability")
    Call<ApiResponse<Map<String, Object>>> checkAvailability(
            @Query("assetId") String assetId,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate);

    @POST("rentals/calculate-price")
    Call<ApiResponse<Map<String, Object>>> calculatePrice(@Body Map<String, String> body);

    @POST("rentals/{id}/extend")
    Call<ApiResponse<Rental>> extendRental(
            @Path("id") String id,
            @Body Map<String, String> body);

    @POST("rentals/{id}/return")
    Call<ApiResponse<Rental>> returnRental(
            @Path("id") String id,
            @Body Map<String, String> body);
}
