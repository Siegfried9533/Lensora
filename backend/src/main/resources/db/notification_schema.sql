-- Lược đồ Bảng Thông báo
-- Dành cho thông báo theo người dùng (cập nhật đơn hàng, nhắc nhở trả thuê)

CREATE TABLE IF NOT EXISTS notifications (
    notification_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL COMMENT 'CẬP_NHẬT_ĐƠN_HÀNG, NHẮC_NHỞ_THUÊ, HỆ_THỐNG, KHUYẾN_MÃI',
    reference_id VARCHAR(36) COMMENT 'ID Đơn hàng hoặc ID Thuê mà thông báo này liên quan đến',
    reference_type VARCHAR(50) COMMENT 'ĐƠN HÀNG hoặc THUÊ',
    is_read BOOLEAN DEFAULT FALSE,
    is_action_required BOOLEAN DEFAULT FALSE COMMENT 'Đúng nếu người dùng cần thực hiện hành động',
    action_url VARCHAR(500) COMMENT 'URL deep link cho hành động',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL,
    expires_at TIMESTAMP NULL COMMENT 'Thông báo hết hạn sau thời điểm này',

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_user_unread (user_id, is_read),
    INDEX idx_reference (reference_id, reference_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Thêm tùy chọn thông báo vào bảng users (tùy chọn)
ALTER TABLE users
ADD COLUMN email_notifications BOOLEAN DEFAULT TRUE COMMENT 'Nhận thông báo qua email',
ADD COLUMN push_notifications BOOLEAN DEFAULT TRUE COMMENT 'Nhận thông báo đẩy';
