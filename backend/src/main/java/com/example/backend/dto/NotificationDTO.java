package com.example.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

public class NotificationDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationResponse {
        private String notificationId;
        private String userId;
        private String title;
        private String message;
        private String type;
        private String referenceId;
        private String referenceType;
        private Boolean isRead;
        private Boolean isActionRequired;
        private String actionUrl;
        private String deepLinkUrl;
        private LocalDateTime createdAt;
        private LocalDateTime readAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationSummary {
        private String notificationId;
        private String title;
        private String type;
        private Boolean isRead;
        private LocalDateTime createdAt;
        private String deepLinkUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnreadCountResponse {
        private long count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarkAsReadRequest {
        private String notificationId;
    }
}
