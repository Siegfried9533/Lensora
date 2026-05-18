package com.example.my_mobile_app.api;

import com.example.my_mobile_app.api.dto.ChangePasswordRequest;
import com.example.my_mobile_app.api.dto.ForgotPasswordRequest;
import com.example.my_mobile_app.api.dto.LoginRequest;
import com.example.my_mobile_app.api.dto.RegisterRequest;
import com.example.my_mobile_app.api.dto.ResetPasswordRequest;
import com.example.my_mobile_app.api.dto.UpdateAvatarRequest;
import com.example.my_mobile_app.model.AuthResponse;
import com.example.my_mobile_app.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

/** Maps frontend/services/api/authApi.ts. */
public interface AuthService {

    @POST("auth/login")
    Call<ApiResponse<AuthResponse>> login(@Body LoginRequest body);

    @POST("auth/register")
    Call<ApiResponse<AuthResponse>> register(@Body RegisterRequest body);

    @GET("auth/me")
    Call<ApiResponse<User>> getCurrentUser();

    @PUT("auth/avatar")
    Call<ApiResponse<User>> updateAvatar(@Body UpdateAvatarRequest body);

    @POST("auth/change-password")
    Call<ApiResponse<Void>> changePassword(@Body ChangePasswordRequest body);

    @POST("auth/verify-email")
    Call<ApiResponse<Void>> verifyEmail(@Query("token") String token);

    @POST("auth/resend-verification")
    Call<ApiResponse<Void>> resendVerificationEmail(@Body ForgotPasswordRequest body);

    @POST("auth/forgot-password")
    Call<ApiResponse<Void>> forgotPassword(@Body ForgotPasswordRequest body);

    @POST("auth/reset-password")
    Call<ApiResponse<Void>> resetPassword(@Body ResetPasswordRequest body);
}
