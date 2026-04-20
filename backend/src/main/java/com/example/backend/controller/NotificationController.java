package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.NotificationDTO;
import com.example.backend.entity.Notification;
import com.example.backend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    private ResponseEntity<ApiResponse> unauthorized() {
        return ResponseEntity.status(401)
                .body(ApiResponse.error("Unauthorized - please login"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (userDetails == null) return unauthorized();
        try {
            Page<NotificationDTO.NotificationResponse> notifications =
                    notificationService.getUserNotifications(userDetails.getUsername(), page, size);

            Map<String, Object> response = new HashMap<>();
            response.put("content", notifications.getContent());
            response.put("page", notifications.getNumber());
            response.put("size", notifications.getSize());
            response.put("totalElements", notifications.getTotalElements());
            response.put("totalPages", notifications.getTotalPages());
            response.put("first", notifications.isFirst());
            response.put("last", notifications.isLast());

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to get notifications: " + e.getMessage()));
        }
    }

    @GetMapping("/unread")
    public ResponseEntity<ApiResponse> getUnreadNotifications(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) return unauthorized();
        try {
            List<NotificationDTO.NotificationResponse> notifications =
                    notificationService.getUnreadNotifications(userDetails.getUsername());

            return ResponseEntity.ok(ApiResponse.success(notifications));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to get unread notifications: " + e.getMessage()));
        }
    }

    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) return unauthorized();
        try {
            long count = notificationService.getUnreadCount(userDetails.getUsername());

            Map<String, Long> response = new HashMap<>();
            response.put("count", count);

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to get unread count: " + e.getMessage()));
        }
    }

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse> markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String notificationId
    ) {
        if (userDetails == null) return unauthorized();
        try {
            NotificationDTO.NotificationResponse notification =
                    notificationService.markAsRead(notificationId, userDetails.getUsername());

            return ResponseEntity.ok(ApiResponse.success(notification));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to mark as read: " + e.getMessage()));
        }
    }

    @PostMapping("/read-all")
    public ResponseEntity<ApiResponse> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) return unauthorized();
        try {
            int count = notificationService.markAllAsRead(userDetails.getUsername());

            Map<String, Integer> response = new HashMap<>();
            response.put("markedCount", count);

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to mark all as read: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse> deleteNotification(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String notificationId
    ) {
        if (userDetails == null) return unauthorized();
        try {
            notificationService.deleteNotification(notificationId, userDetails.getUsername());

            return ResponseEntity.ok(ApiResponse.success("Notification deleted successfully"));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to delete notification: " + e.getMessage()));
        }
    }

    @PostMapping("/test")
    public ResponseEntity<ApiResponse> createTestNotification(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body
    ) {
        if (userDetails == null) return unauthorized();
        try {
            String type = body.getOrDefault("type", "SYSTEM");
            String title = body.getOrDefault("title", "Test Notification");
            String message = body.getOrDefault("message", "This is a test notification");

            Notification.NotificationType notificationType = Notification.NotificationType.valueOf(type);

            NotificationDTO.NotificationResponse notification = notificationService.createNotificationForUser(
                    userDetails.getUsername(),
                    title,
                    message,
                    notificationType,
                    null,
                    null,
                    false,
                    null
            );

            return ResponseEntity.ok(ApiResponse.success(notification));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to create test notification: " + e.getMessage()));
        }
    }

    @GetMapping("/system")
    public ResponseEntity<ApiResponse> getSystemNotifications() {
        try {
            List<NotificationDTO.NotificationResponse> notifications =
                    notificationService.getSystemNotifications();
            return ResponseEntity.ok(ApiResponse.success(notifications));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to get system notifications: " + e.getMessage()));
        }
    }
}