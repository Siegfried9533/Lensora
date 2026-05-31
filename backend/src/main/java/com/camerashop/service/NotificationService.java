package com.camerashop.service;

import com.camerashop.dto.NotificationDTO;
import com.camerashop.entity.Notification;
import com.camerashop.entity.Order;
import com.camerashop.entity.Rental;
import com.camerashop.entity.User;
import com.camerashop.exception.ResourceNotFoundException;
import com.camerashop.repository.NotificationRepository;
import com.camerashop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Tạo thông báo mới cho người dùng
     */
    @Transactional
    public NotificationDTO.NotificationResponse createNotification(
            String userId,
            String title,
            String message,
            Notification.NotificationType type,
            String referenceId,
            Notification.ReferenceType referenceType,
            Boolean isActionRequired,
            String actionUrl
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + userId));

        Notification notification = Notification.builder()
                .notificationId(UUID.randomUUID().toString())
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .isActionRequired(isActionRequired != null ? isActionRequired : false)
                .actionUrl(actionUrl)
                .build();

        // Đặt thời hạn cho một số loại thông báo nhất định
        if (type == Notification.NotificationType.PROMOTION) {
            notification.setExpiresAt(LocalDateTime.now().plusDays(7));
        } else if (type == Notification.NotificationType.RENTAL_REMINDER) {
            notification.setExpiresAt(LocalDateTime.now().plusDays(1));
        }

        notificationRepository.save(notification);

        return toResponse(notification);
    }

    /**
     * Tạo thông báo cho thay đổi trạng thái đơn hàng
     */
    @Transactional
    public void notifyOrderStatusChange(Order order, String oldStatus, String newStatus) {
        String title = getOrderStatusTitle(newStatus);
        String message = getOrderStatusMessage(order, oldStatus, newStatus);

        createNotification(
                order.getUser().getUserId(),
                title,
                message,
                Notification.NotificationType.ORDER_UPDATE,
                order.getOrderId(),
                Notification.ReferenceType.ORDER,
                requiresAction(newStatus),
                null
        );
    }

    /**
     * Tạo thông báo nhắc nhở trả thiết bị thuê
     */
    @Transactional
    public void notifyRentalReturnReminder(Rental rental, long daysUntilReturn) {
        String title = "Nhắc nhở trả thiết bị thuê";
        String message = String.format(
                "Thiết bị thuê %s của bạn sẽ đến hạn trong %d ngày. Vui lòng trả trước %s để tránh phí phạt.",
                rental.getAsset().getModelName(),
                daysUntilReturn,
                rental.getEndDate()
        );

        createNotification(
                rental.getUser().getUserId(),
                title,
                message,
                Notification.NotificationType.RENTAL_REMINDER,
                rental.getRentalId(),
                Notification.ReferenceType.RENTAL,
                true,
                null
        );
    }

    /**
     * Tạo thông báo thuê quá hạn
     */
    @Transactional
    public void notifyRentalOverdue(Rental rental, long daysOverdue) {
        String title = "Thuê quá hạn!";
        String message = String.format(
                "Thiết bị thuê %s của bạn đã quá hạn %d ngày. Vui lòng trả ngay để tránh phí phụ thu.",
                rental.getAsset().getModelName(),
                daysOverdue
        );

        createNotification(
                rental.getUser().getUserId(),
                title,
                message,
                Notification.NotificationType.RENTAL_OVERDUE,
                rental.getRentalId(),
                Notification.ReferenceType.RENTAL,
                true,
                null
        );
    }

    /**
     * Tạo thông báo thanh toán thành công
     */
    @Transactional
    public void notifyPaymentSuccess(Order order, double amount) {
        String title = "Thanh toán thành công";
        String message = String.format(
                "Thanh toán ₫%,d cho đơn hàng %s đã được xác nhận.",
                (long) amount,
                order.getOrderId()
        );

        createNotification(
                order.getUser().getUserId(),
                title,
                message,
                Notification.NotificationType.PAYMENT_SUCCESS,
                order.getOrderId(),
                Notification.ReferenceType.ORDER,
                false,
                null
        );
    }

    /**
     * Lấy thông báo của người dùng có phân trang
     */
    @Transactional(readOnly = true)
    public Page<NotificationDTO.NotificationResponse> getUserNotifications(
            String email,
            int page,
            int size
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Notification> notifications = notificationRepository.findByUserUserId(user.getUserId(), pageRequest);

        return notifications.map(this::toResponse);
    }

    /**
     * Lấy thông báo chưa đọc của người dùng
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO.NotificationResponse> getUnreadNotifications(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        List<Notification> notifications = notificationRepository.findByUserUserIdAndIsReadFalse(user.getUserId());
        return notifications.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Đếm số thông báo chưa đọc
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        return notificationRepository.countByUserUserIdAndIsReadFalse(user.getUserId());
    }

    /**
     * Đánh dấu một thông báo đã đọc
     */
    @Transactional
    public NotificationDTO.NotificationResponse markAsRead(String notificationId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo: " + notificationId));

        if (!notification.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Thông báo không thuộc về người dùng này");
        }

        notification.markAsRead();
        notificationRepository.save(notification);

        return toResponse(notification);
    }

    /**
     * Đánh dấu tất cả thông báo đã đọc cho người dùng
     */
    @Transactional
    public int markAllAsRead(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        return notificationRepository.markAllAsRead(user.getUserId(), LocalDateTime.now());
    }

    /**
     * Xoa mot thong bao
     */
    @Transactional
    public void deleteNotification(String notificationId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo: " + notificationId));

        if (!notification.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Thông báo không thuộc về người dùng này");
        }

        notificationRepository.delete(notification);
    }

    /**
     * Công việc định kỳ: Gửi nhắc nhở trả thuê trước 2 ngày
     */
    @Scheduled(cron = "0 0 9 * * ?") // Mỗi ngày lúc 9 giờ sáng
    @Transactional
    public void sendRentalReminders() {
        LocalDateTime twoDaysFromNow = LocalDateTime.now().plusDays(2);

        // Cần có phương thức RentalRepository để tìm đơn thuê đến hạn trong 2 ngày
        // Hiện tại đây là placeholder cho công việc định kỳ
        System.out.println("Running scheduled rental reminder job...");
    }

    /**
     * Công việc định kỳ: Dọn dẹp thông báo cũ
     */
    @Scheduled(cron = "0 0 2 * * ?") // Mỗi ngày lúc 2 giờ sáng
    @Transactional
    public void cleanupOldNotifications() {
        // Xóa thông báo đã hết hạn
        int deletedExpired = notificationRepository.deleteExpired(LocalDateTime.now());

        // Xóa thông báo đã đọc cũ hơn 30 ngày
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        int deletedOld = notificationRepository.deleteOldReadNotifications(thirtyDaysAgo);

        System.out.println("Cleanup: deleted " + deletedExpired + " expired and " + deletedOld + " old read notifications");
    }

    /**
     * Tạo thông báo cho người dùng bằng email (chuyển email thành userId)
     */
    @Transactional
    public NotificationDTO.NotificationResponse createNotificationForUser(
            String email,
            String title,
            String message,
            Notification.NotificationType type,
            String referenceId,
            Notification.ReferenceType referenceType,
            Boolean isActionRequired,
            String actionUrl
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        return createNotification(
                user.getUserId(),
                title,
                message,
                type,
                referenceId,
                referenceType,
                isActionRequired,
                actionUrl
        );
    }

    /**
     * Lấy thông báo hệ thống/phát sóng (không cần xác thực, giới hạn 10 mới nhất)
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO.NotificationResponse> getSystemNotifications() {
        List<Notification> notifications = notificationRepository.findByTypeInAndExpiresAtAfterOrExpiresAtIsNull(
                Arrays.asList(Notification.NotificationType.SYSTEM, Notification.NotificationType.PROMOTION),
                LocalDateTime.now(),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        // Loại bỏ trùng lặp theo tiêu đề để tránh hiển thị cùng một broadcast cho mọi người dùng
        Map<String, Notification> uniqueByTitle = new java.util.LinkedHashMap<>();
        for (Notification n : notifications) {
            uniqueByTitle.putIfAbsent(n.getTitle(), n);
        }
        return uniqueByTitle.values().stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Kiểm tra xem đã có thông báo quá hạn cho đơn thuê chưa
     */
    @Transactional(readOnly = true)
    public boolean hasOverdueNotification(String rentalId) {
        List<Notification> existing = notificationRepository.findByReferenceIdAndReferenceType(
                rentalId, Notification.ReferenceType.RENTAL
        );
        return existing.stream().anyMatch(n -> n.getType() == Notification.NotificationType.RENTAL_OVERDUE);
    }

    /**
     * Tạo thông báo chào mừng cho người dùng mới
     */
    @Transactional
    public void createWelcomeNotification(String userId) {
        createNotification(
                userId,
                "Chào mừng đến với Lensora!",
                "Cảm ơn bạn đã tham gia Lensora! Hãy khám phá bộ sưu tập máy ảnh, ống kính và thiết bị cao cấp của chúng tôi.",
                Notification.NotificationType.SYSTEM,
                null,
                null,
                false,
                null
        );
    }

    // Các phương thức hỗ trợ

    private NotificationDTO.NotificationResponse toResponse(Notification notification) {
        return NotificationDTO.NotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .userId(notification.getUser().getUserId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType().name())
                .referenceId(notification.getReferenceId())
                .referenceType(notification.getReferenceType() != null ? notification.getReferenceType().name() : null)
                .isRead(notification.getIsRead())
                .isActionRequired(notification.getIsActionRequired())
                .actionUrl(notification.getActionUrl())
                .deepLinkUrl(notification.getDeepLinkUrl())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }

    private String getOrderStatusTitle(String status) {
        switch (status) {
            case "PENDING": return "Đã đặt hàng";
            case "CONFIRMED": return "Đã xác nhận đơn hàng";
            case "PROCESSING": return "Đang xử lý đơn hàng";
            case "SHIPPED": return "Đơn hàng đã giao cho vận chuyển";
            case "DELIVERED": return "Đơn hàng đã giao";
            case "CANCELLED": return "Đơn hàng đã bị hủy";
            default: return "Cập nhật trạng thái đơn hàng";
        }
    }

    private String getOrderStatusMessage(Order order, String oldStatus, String newStatus) {
        switch (newStatus) {
            case "PENDING":
                return String.format("Đơn hàng %s đã được đặt và đang chờ xác nhận.", order.getOrderId());
            case "CONFIRMED":
                return String.format("Đơn hàng %s đã được xác nhận. Chúng tôi đang chuẩn bị giao hàng.", order.getOrderId());
            case "PROCESSING":
                return String.format("Đơn hàng %s đang được chuẩn bị giao.", order.getOrderId());
            case "SHIPPED":
                return String.format("Đơn hàng %s đã được giao cho vận chuyển và đang trên đường đến!", order.getOrderId());
            case "DELIVERED":
                return String.format("Đơn hàng %s đã được giao. Chúc bạn sử dụng vui vẻ!", order.getOrderId());
            case "CANCELLED":
                return String.format("Đơn hàng %s đã bị hủy.", order.getOrderId());
            default:
                return String.format("Trạng thái đơn hàng %s đã được cập nhật thành %s.", order.getOrderId(), newStatus);
        }
    }

    private boolean requiresAction(String status) {
        return "CANCELLED".equals(status) || "PENDING".equals(status);
    }
}
