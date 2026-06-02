package com.example.my_mobile_app.api;

import com.example.my_mobile_app.api.dto.CreateMoMoPaymentRequest;
import com.example.my_mobile_app.model.District;
import com.example.my_mobile_app.model.PaymentResult;
import com.example.my_mobile_app.model.Province;
import com.example.my_mobile_app.model.ShippingFee;
import com.example.my_mobile_app.model.Ward;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/** Maps frontend/services/api/paymentApi.ts. */
public interface PaymentService {

    /** Returns {payUrl, orderId}. */
    @POST("payment/momo/create")
    Call<ApiResponse<Map<String, String>>> createMoMoPayment(@Body CreateMoMoPaymentRequest body);

    /** Body: {rentalId, orderInfo?}. */
    @POST("payment/momo/create-rental")
    Call<ApiResponse<Map<String, String>>> createMoMoPaymentRental(@Body Map<String, String> body);

    @GET("payment/status/{orderCode}")
    Call<ApiResponse<PaymentResult>> getPaymentStatus(@Path("orderCode") String orderCode);

    /** Body: {toDistrict, toWard, weight, insuranceValue}. */
    @POST("shipping/calculate")
    Call<ApiResponse<ShippingFee>> calculateShippingFee(@Body Map<String, Object> body);

    @GET("shipping/provinces")
    Call<ApiResponse<List<Province>>> getProvinces();

    @GET("shipping/districts/{provinceId}")
    Call<ApiResponse<List<District>>> getDistricts(@Path("provinceId") String provinceId);

    @GET("shipping/wards/{districtId}")
    Call<ApiResponse<List<Ward>>> getWards(@Path("districtId") String districtId);

    @GET("shipping/track/{orderCode}")
    Call<ApiResponse<Map<String, String>>> trackOrder(@Path("orderCode") String orderCode);

    /** Body: {orderId, requestId}. */
    @POST("payment/momo/query")
    Call<ApiResponse<Map<String, Object>>> queryMoMoTransaction(@Body Map<String, String> body);

    /** Test/manual QR flow. Body: {orderCode}. */
    @POST("payment/momo/manual-confirm")
    Call<ApiResponse<PaymentResult>> confirmManualMoMoPayment(@Body Map<String, String> body);
}
