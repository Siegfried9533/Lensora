package com.example.my_mobile_app.api;

import com.example.my_mobile_app.model.Notification;
import com.example.my_mobile_app.model.PaginatedResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Maps frontend/services/api/notificationApi.ts. */
public interface NotificationService {

    @GET("notifications")
    Call<ApiResponse<PaginatedResponse<Notification>>> getNotifications(
            @Query("page") int page,
            @Query("size") int size);

    @GET("notifications/unread")
    Call<ApiResponse<List<Notification>>> getUnreadNotifications();

    @GET("notifications/unread/count")
    Call<ApiResponse<Map<String, Integer>>> getUnreadCount();

    @POST("notifications/{id}/read")
    Call<ApiResponse<Notification>> markAsRead(@Path("id") String notificationId);

    @POST("notifications/read-all")
    Call<ApiResponse<Map<String, Integer>>> markAllAsRead();

    @DELETE("notifications/{id}")
    Call<ApiResponse<Void>> deleteNotification(@Path("id") String notificationId);

    @GET("notifications/system")
    Call<ApiResponse<List<Notification>>> getSystemNotifications();
}
