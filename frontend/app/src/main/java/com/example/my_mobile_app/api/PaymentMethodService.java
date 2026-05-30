package com.example.my_mobile_app.api;

import com.example.my_mobile_app.model.SavedPaymentMethod;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/** Maps the backend {@code PaymentMethodController} at /api/payment-methods. */
public interface PaymentMethodService {

    @GET("payment-methods")
    Call<ApiResponse<List<SavedPaymentMethod>>> getMethods();

    @POST("payment-methods")
    Call<ApiResponse<SavedPaymentMethod>> addMethod(@Body Map<String, Object> body);

    @DELETE("payment-methods/{id}")
    Call<ApiResponse<Void>> deleteMethod(@Path("id") String id);

    @PUT("payment-methods/{id}/default")
    Call<ApiResponse<SavedPaymentMethod>> setDefault(@Path("id") String id);
}
