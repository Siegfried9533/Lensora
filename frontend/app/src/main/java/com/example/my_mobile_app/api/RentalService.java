package com.example.my_mobile_app.api;

import com.example.my_mobile_app.api.dto.CreateRentalRequest;
import com.example.my_mobile_app.model.Rental;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/** Maps rental endpoints in orderApi.ts + rentalApi.ts. */
public interface RentalService {

    @POST("rentals")
    Call<ApiResponse<Rental>> createRental(@Body CreateRentalRequest body);

    @GET("rentals")
    Call<ApiResponse<List<Rental>>> getRentals();

    @GET("rentals/{rentalId}")
    Call<ApiResponse<Rental>> getRentalById(@Path("rentalId") String rentalId);
}
