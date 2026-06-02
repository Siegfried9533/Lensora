package com.example.my_mobile_app.api;

import com.example.my_mobile_app.model.Address;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/** Maps the backend {@code AddressController} at /api/addresses. */
public interface AddressService {

    @GET("addresses")
    Call<ApiResponse<List<Address>>> getAddresses();

    @POST("addresses")
    Call<ApiResponse<Address>> addAddress(@Body Map<String, Object> body);

    @PUT("addresses/{id}")
    Call<ApiResponse<Address>> updateAddress(@Path("id") String id, @Body Map<String, Object> body);

    @DELETE("addresses/{id}")
    Call<ApiResponse<Void>> deleteAddress(@Path("id") String id);

    @PUT("addresses/{id}/default")
    Call<ApiResponse<Address>> setDefault(@Path("id") String id);
}
