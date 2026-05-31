package com.example.my_mobile_app.api;

import com.example.my_mobile_app.api.dto.CreateOrderRequest;
import com.example.my_mobile_app.model.Order;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

/** Maps order endpoints in orderApi.ts. */
public interface OrderService {

    @POST("orders")
    Call<ApiResponse<Order>> createOrder(@Body CreateOrderRequest body);

    @GET("orders")
    Call<ApiResponse<List<Order>>> getOrders();

    @GET("orders/{orderId}")
    Call<ApiResponse<Order>> getOrderById(@Path("orderId") String orderId);

    @PATCH("orders/{orderId}/cancel")
    Call<ApiResponse<Order>> cancelOrder(@Path("orderId") String orderId);

    @PATCH("orders/{orderId}/status")
    Call<ApiResponse<Order>> updateOrderStatus(
            @Path("orderId") String orderId,
            @Body Map<String, String> body);
}
