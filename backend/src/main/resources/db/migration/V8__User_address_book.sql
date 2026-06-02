-- Sổ địa chỉ giao hàng: mỗi user lưu nhiều địa chỉ và chọn 1 địa chỉ mặc định,
-- để không phải nhập lại mỗi lần đặt hàng. Lưu kèm ID GHN (district_id, ward_code)
-- để tính phí vận chuyển mà không cần tra cứu lại.
CREATE TABLE IF NOT EXISTS user_addresses (
    address_id      VARCHAR(255) PRIMARY KEY,
    user_id         VARCHAR(255) NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    recipient_name  VARCHAR(255) NOT NULL,
    recipient_phone VARCHAR(255) NOT NULL,
    province_id     VARCHAR(255),
    province_name   VARCHAR(255),
    district_id     VARCHAR(255),
    district_name   VARCHAR(255),
    ward_code       VARCHAR(255),
    ward_name       VARCHAR(255),
    street          VARCHAR(255),
    note            VARCHAR(255),
    is_default      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP(6),
    updated_at      TIMESTAMP(6)
);

CREATE INDEX IF NOT EXISTS idx_user_addresses_user ON user_addresses(user_id);
