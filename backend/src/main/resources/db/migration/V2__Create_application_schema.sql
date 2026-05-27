CREATE TABLE IF NOT EXISTS users (
    user_id VARCHAR(255) PRIMARY KEY,
    user_name VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    provider VARCHAR(255),
    provider_id VARCHAR(255) UNIQUE,
    avatar_url VARCHAR(255),
    email_verified BOOLEAN NOT NULL,
    role VARCHAR(255) NOT NULL,
    trust_score INTEGER NOT NULL,
    created_at TIMESTAMP(6),
    updated_at TIMESTAMP(6)
);

CREATE TABLE IF NOT EXISTS categories (
    category_id VARCHAR(255) PRIMARY KEY,
    category_name VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS products (
    product_id VARCHAR(255) PRIMARY KEY,
    category_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    brand VARCHAR(255) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    price BIGINT NOT NULL,
    stock_quantity INTEGER NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(category_id),
    CONSTRAINT fk_products_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS assets (
    asset_id VARCHAR(255) PRIMARY KEY,
    category_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    model_name VARCHAR(255) NOT NULL,
    brand VARCHAR(255) NOT NULL,
    daily_rate BIGINT NOT NULL,
    status VARCHAR(255) NOT NULL,
    serial_number VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_assets_category FOREIGN KEY (category_id) REFERENCES categories(category_id),
    CONSTRAINT fk_assets_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS product_images (
    image_id VARCHAR(255) PRIMARY KEY,
    product_id VARCHAR(255) NOT NULL,
    url VARCHAR(255) NOT NULL,
    is_primary BOOLEAN NOT NULL,
    CONSTRAINT fk_product_images_product FOREIGN KEY (product_id) REFERENCES products(product_id)
);

CREATE TABLE IF NOT EXISTS asset_images (
    image_id VARCHAR(255) PRIMARY KEY,
    asset_id VARCHAR(255) NOT NULL,
    url VARCHAR(255) NOT NULL,
    is_primary BOOLEAN NOT NULL,
    CONSTRAINT fk_asset_images_asset FOREIGN KEY (asset_id) REFERENCES assets(asset_id)
);

CREATE TABLE IF NOT EXISTS orders (
    order_id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    order_date TIMESTAMP(6) NOT NULL,
    total_amount BIGINT NOT NULL,
    shipping_address VARCHAR(500) NOT NULL,
    status VARCHAR(255) NOT NULL,
    payment_method VARCHAR(255) NOT NULL,
    payment_status VARCHAR(255),
    ghn_order_id VARCHAR(255),
    shipping_fee BIGINT,
    shipped_date TIMESTAMP(6),
    delivered_date TIMESTAMP(6),
    cancelled_date TIMESTAMP(6),
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS order_items (
    order_item_id VARCHAR(255) PRIMARY KEY,
    order_id VARCHAR(255) NOT NULL,
    product_id VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    price_at_purchase BIGINT NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(order_id),
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(product_id)
);

CREATE TABLE IF NOT EXISTS rentals (
    rental_id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    asset_id VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    return_date DATE,
    deposit_fee BIGINT NOT NULL,
    total_rent_fee BIGINT NOT NULL,
    penalty_fee BIGINT,
    status VARCHAR(255) NOT NULL,
    shipping_address VARCHAR(255),
    payment_method VARCHAR(255),
    shipping_fee BIGINT,
    CONSTRAINT fk_rentals_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_rentals_asset FOREIGN KEY (asset_id) REFERENCES assets(asset_id)
);

CREATE TABLE IF NOT EXISTS payment_transactions (
    transaction_id VARCHAR(255) PRIMARY KEY,
    transaction_ref VARCHAR(255) NOT NULL UNIQUE,
    order_code VARCHAR(255) NOT NULL,
    amount DOUBLE PRECISION NOT NULL,
    status VARCHAR(255) NOT NULL,
    payment_method VARCHAR(255) NOT NULL,
    order_id VARCHAR(255),
    rental_id VARCHAR(255),
    bank_code VARCHAR(255),
    bank_tran_no VARCHAR(255),
    card_type VARCHAR(255),
    response_code VARCHAR(255),
    response_message VARCHAR(255),
    transaction_date TIMESTAMP(6) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_payment_transactions_order FOREIGN KEY (order_id) REFERENCES orders(order_id),
    CONSTRAINT fk_payment_transactions_rental FOREIGN KEY (rental_id) REFERENCES rentals(rental_id)
);

CREATE TABLE IF NOT EXISTS cart_items (
    cart_item_id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    product_id VARCHAR(255),
    asset_id VARCHAR(255),
    quantity INTEGER NOT NULL,
    type VARCHAR(255) NOT NULL,
    CONSTRAINT fk_cart_items_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT fk_cart_items_asset FOREIGN KEY (asset_id) REFERENCES assets(asset_id)
);

CREATE TABLE IF NOT EXISTS favorites (
    favorite_id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    product_id VARCHAR(255),
    asset_id VARCHAR(255),
    type VARCHAR(255) NOT NULL,
    CONSTRAINT fk_favorites_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_favorites_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT fk_favorites_asset FOREIGN KEY (asset_id) REFERENCES assets(asset_id),
    CONSTRAINT uk_favorites_user_product UNIQUE (user_id, product_id),
    CONSTRAINT uk_favorites_user_asset UNIQUE (user_id, asset_id)
);

CREATE TABLE IF NOT EXISTS notifications (
    notification_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    reference_id VARCHAR(36),
    reference_type VARCHAR(50),
    is_read BOOLEAN NOT NULL,
    is_action_required BOOLEAN NOT NULL,
    action_url VARCHAR(500),
    created_at TIMESTAMP(6) NOT NULL,
    read_at TIMESTAMP(6),
    expires_at TIMESTAMP(6),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE INDEX IF NOT EXISTS idx_user_id ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_user_unread ON notifications(user_id, is_read);
CREATE INDEX IF NOT EXISTS idx_reference ON notifications(reference_id, reference_type);
CREATE INDEX IF NOT EXISTS idx_created_at ON notifications(created_at);

CREATE TABLE IF NOT EXISTS email_verification_tokens (
    id VARCHAR(255) PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP(6) NOT NULL,
    used BOOLEAN NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_email_verification_tokens_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id VARCHAR(255) PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id VARCHAR(255) NOT NULL,
    expiry_date TIMESTAMP(6) NOT NULL,
    used BOOLEAN NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS reviews (
    review_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    user_id VARCHAR(255),
    entity_id BIGINT,
    rating INTEGER,
    comment VARCHAR(255),
    type VARCHAR(255),
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS images (
    image_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    entity_id BIGINT,
    url VARCHAR(1000) NOT NULL,
    is_primary BOOLEAN NOT NULL,
    type VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS carts (
    cart_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    user_id BIGINT,
    product_id BIGINT,
    quantity INTEGER
);
