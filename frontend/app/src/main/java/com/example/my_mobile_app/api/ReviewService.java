package com.example.my_mobile_app.api;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ReviewService {

    @GET("reviews/me/count")
    Call<ApiResponse<Integer>> getMyReviewCount();
}
