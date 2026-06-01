-- Lý do hủy đơn do khách hàng chọn khi hủy (hoặc do hệ thống đặt khi GHN hủy vận đơn).
ALTER TABLE orders ADD COLUMN cancel_reason VARCHAR(255);
