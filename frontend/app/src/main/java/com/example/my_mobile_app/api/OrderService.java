package com.example.my_mobile_app.api;

import com.example.my_mobile_app.api.dto.CreateOrderRequest;
import com.example.my_mobile_app.model.Order;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
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
}
