package com.example.my_mobile_app.api.dto;

public class UpdateAvatarRequest {
    public String avatarUrl;

    public UpdateAvatarRequest() {}

    public UpdateAvatarRequest(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
