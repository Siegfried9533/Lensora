-- Notifications Table Schema
-- For user-specific notifications (order updates, rental return reminders)

CREATE TABLE IF NOT EXISTS notifications (
    notification_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL COMMENT 'ORDER_UPDATE, RENTAL_REMINDER, SYSTEM, PROMOTION',
    reference_id VARCHAR(36) COMMENT 'Order ID or Rental ID this notification relates to',
    reference_type VARCHAR(50) COMMENT 'ORDER or RENTAL',
    is_read BOOLEAN DEFAULT FALSE,
    is_action_required BOOLEAN DEFAULT FALSE COMMENT 'True if user needs to take action',
    action_url VARCHAR(500) COMMENT 'Deep link URL for action',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL,
    expires_at TIMESTAMP NULL COMMENT 'Notification expires after this time',

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_user_unread (user_id, is_read),
    INDEX idx_reference (reference_id, reference_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add notification preferences to users table (optional)
ALTER TABLE users
ADD COLUMN email_notifications BOOLEAN DEFAULT TRUE COMMENT 'Receive email notifications',
ADD COLUMN push_notifications BOOLEAN DEFAULT TRUE COMMENT 'Receive push notifications';
