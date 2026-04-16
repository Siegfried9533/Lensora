-- Seed data for Lensora

-- Categories
INSERT INTO categories (category_name, type)
SELECT 'Camera', 'ASSET'
WHERE NOT EXISTS (
    SELECT 1 FROM categories WHERE category_name = 'Camera' AND type = 'ASSET'
);

INSERT INTO categories (category_name, type)
SELECT 'Lens', 'ASSET'
WHERE NOT EXISTS (
    SELECT 1 FROM categories WHERE category_name = 'Lens' AND type = 'ASSET'
);

INSERT INTO categories (category_name, type)
SELECT 'Accessory', 'PRODUCT'
WHERE NOT EXISTS (
    SELECT 1 FROM categories WHERE category_name = 'Accessory' AND type = 'PRODUCT'
);

-- Users
INSERT INTO users (user_name, password, email, role, trust_score)
SELECT 'admin', '$2a$10$demo.hash.replace.in.real.env', 'admin@lensora.local', 'ADMIN', 100
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'admin@lensora.local'
);

INSERT INTO users (user_name, password, email, role, trust_score)
SELECT 'hoa.nguyen', '$2a$10$demo.hash.replace.in.real.env', 'hoa.nguyen@lensora.local', 'USER', 95
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'hoa.nguyen@lensora.local'
);

INSERT INTO users (user_name, password, email, role, trust_score)
SELECT 'minh.tran', '$2a$10$demo.hash.replace.in.real.env', 'minh.tran@lensora.local', 'USER', 92
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'minh.tran@lensora.local'
);

-- Products
INSERT INTO products (category_id, product_name, brand, description, price, stock_quantity)
SELECT
    (SELECT category_id FROM categories WHERE category_name = 'Accessory' AND type = 'PRODUCT' LIMIT 1),
    'UHS-II SD Card 128GB',
    'SanDisk',
    'High-speed memory card for 4K recording',
    1190000,
    30
WHERE NOT EXISTS (
    SELECT 1 FROM products WHERE product_name = 'UHS-II SD Card 128GB' AND brand = 'SanDisk'
);

INSERT INTO products (category_id, product_name, brand, description, price, stock_quantity)
SELECT
    (SELECT category_id FROM categories WHERE category_name = 'Accessory' AND type = 'PRODUCT' LIMIT 1),
    'Camera Tripod Carbon 1.6m',
    'Ulanzi',
    'Lightweight carbon tripod for travel and studio',
    2590000,
    12
WHERE NOT EXISTS (
    SELECT 1 FROM products WHERE product_name = 'Camera Tripod Carbon 1.6m' AND brand = 'Ulanzi'
);

-- Assets
INSERT INTO assets (category_id, model_name, brand, description, daily_rate, deposit_value, status, serial_number)
SELECT
    (SELECT category_id FROM categories WHERE category_name = 'Camera' AND type = 'ASSET' LIMIT 1),
    'EOS R6 Mark II',
    'Canon',
    '24.2MP full-frame mirrorless camera body',
    650000,
    10000000,
    'AVAILABLE',
    'CAN-R6II-0001'
WHERE NOT EXISTS (
    SELECT 1 FROM assets WHERE serial_number = 'CAN-R6II-0001'
);

INSERT INTO assets (category_id, model_name, brand, description, daily_rate, deposit_value, status, serial_number)
SELECT
    (SELECT category_id FROM categories WHERE category_name = 'Lens' AND type = 'ASSET' LIMIT 1),
    'RF 24-70mm f/2.8L',
    'Canon',
    'Professional standard zoom lens for RF mount',
    420000,
    7000000,
    'AVAILABLE',
    'CAN-RF2470-0001'
WHERE NOT EXISTS (
    SELECT 1 FROM assets WHERE serial_number = 'CAN-RF2470-0001'
);

-- Images (polymorphic by entity_id + type)
INSERT INTO images (entity_id, url, is_primary, type)
SELECT
    (SELECT product_id FROM products WHERE product_name = 'UHS-II SD Card 128GB' AND brand = 'SanDisk' LIMIT 1),
    'https://encrypted-tbn1.gstatic.com/shopping?q=tbn:ANd9GcQxXW0GwBBacRvrJkWFEAtVj-cv67eSUgl-AhUo32OnDTiTbKpyjT8pfgoTH1JTddWgHPXNjjk28nYg2u7BRIg0wqDwG5QT7mreIDPSNeY1l7iU1YN4j9TlMROK0W09Nj3QKwmQ1A&usqp=CAc',
    TRUE,
    'PRODUCT'
WHERE NOT EXISTS (
    SELECT 1 FROM images
    WHERE type = 'PRODUCT'
      AND entity_id = (SELECT product_id FROM products WHERE product_name = 'UHS-II SD Card 128GB' AND brand = 'SanDisk' LIMIT 1)
      AND is_primary = TRUE
);

INSERT INTO images (entity_id, url, is_primary, type)
SELECT
    (SELECT product_id FROM products WHERE product_name = 'Camera Tripod Carbon 1.6m' AND brand = 'Ulanzi' LIMIT 1),
    'https://encrypted-tbn3.gstatic.com/shopping?q=tbn:ANd9GcQ8SLx-NTprT8gMGsDQrcAcDKQ5OZkxKi-wZkVtCV26Y_KzEB0AaVAGWox6Xz_9nb0CaVzCRbs6Jbjhch6OJNWm8mONW4UkfnMn9XnyLb7s5OSG3edcsFQNQ82jeEaSyc7mashAXEo&usqp=CAc',
    TRUE,
    'PRODUCT'
WHERE NOT EXISTS (
    SELECT 1 FROM images
    WHERE type = 'PRODUCT'
      AND entity_id = (SELECT product_id FROM products WHERE product_name = 'Camera Tripod Carbon 1.6m' AND brand = 'Ulanzi' LIMIT 1)
      AND is_primary = TRUE
);

INSERT INTO images (entity_id, url, is_primary, type)
SELECT
    (SELECT asset_id FROM assets WHERE serial_number = 'CAN-R6II-0001' LIMIT 1),
    'https://encrypted-tbn2.gstatic.com/shopping?q=tbn:ANd9GcTxEibHKrlkDg5oh4MSA7qDlmJqlhFtALiHY4UKIkIRtGsAHyDwxHy3ub1U425Zi2wiTetqDt0OhZjyGZxRp-g-ktPNqQAxyLDBHxWjhAq6RPQVaKQNx5ZmreYO7v7ldlpn6CoXRj68QdU&usqp=CAc',
    TRUE,
    'ASSET'
WHERE NOT EXISTS (
    SELECT 1 FROM images
    WHERE type = 'ASSET'
      AND entity_id = (SELECT asset_id FROM assets WHERE serial_number = 'CAN-R6II-0001' LIMIT 1)
      AND is_primary = TRUE
);

INSERT INTO images (entity_id, url, is_primary, type)
SELECT
    (SELECT asset_id FROM assets WHERE serial_number = 'CAN-RF2470-0001' LIMIT 1),
    'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQim3XQodrFMsCJkMtAnMTRb_xyWYmr6EAfag&s',
    TRUE,
    'ASSET'
WHERE NOT EXISTS (
    SELECT 1 FROM images
    WHERE type = 'ASSET'
      AND entity_id = (SELECT asset_id FROM assets WHERE serial_number = 'CAN-RF2470-0001' LIMIT 1)
      AND is_primary = TRUE
);

-- Carts
INSERT INTO carts (user_id, product_id, quantity)
SELECT
    (SELECT user_id FROM users WHERE email = 'hoa.nguyen@lensora.local' LIMIT 1),
    (SELECT product_id FROM products WHERE product_name = 'Camera Tripod Carbon 1.6m' AND brand = 'Ulanzi' LIMIT 1),
    1
WHERE NOT EXISTS (
    SELECT 1 FROM carts
    WHERE user_id = (SELECT user_id FROM users WHERE email = 'hoa.nguyen@lensora.local' LIMIT 1)
      AND product_id = (SELECT product_id FROM products WHERE product_name = 'Camera Tripod Carbon 1.6m' AND brand = 'Ulanzi' LIMIT 1)
);

-- Orders
INSERT INTO orders (user_id, order_date, total_amount, shipping_address, status)
SELECT
    (SELECT user_id FROM users WHERE email = 'hoa.nguyen@lensora.local' LIMIT 1),
    NOW(),
    2590000,
    '123 Nguyen Trai, District 1, Ho Chi Minh City',
    'PENDING'
WHERE NOT EXISTS (
    SELECT 1 FROM orders
    WHERE user_id = (SELECT user_id FROM users WHERE email = 'hoa.nguyen@lensora.local' LIMIT 1)
      AND status = 'PENDING'
      AND total_amount = 2590000
);

-- Order items
INSERT INTO order_items (order_id, product_id, quantity, price_at_purchase)
SELECT
        (
                SELECT order_id
                FROM orders
                WHERE user_id = (SELECT user_id FROM users WHERE email = 'hoa.nguyen@lensora.local' LIMIT 1)
                    AND status = 'PENDING'
                    AND total_amount = 2590000
                    AND shipping_address = '123 Nguyen Trai, District 1, Ho Chi Minh City'
                ORDER BY order_id DESC
                LIMIT 1
        ),
    (SELECT product_id FROM products WHERE product_name = 'Camera Tripod Carbon 1.6m' AND brand = 'Ulanzi' LIMIT 1),
    1,
    2590000
WHERE NOT EXISTS (
    SELECT 1 FROM order_items
        WHERE order_id = (
                        SELECT order_id
                        FROM orders
                        WHERE user_id = (SELECT user_id FROM users WHERE email = 'hoa.nguyen@lensora.local' LIMIT 1)
                            AND status = 'PENDING'
                            AND total_amount = 2590000
                            AND shipping_address = '123 Nguyen Trai, District 1, Ho Chi Minh City'
                        ORDER BY order_id DESC
                        LIMIT 1
                )
      AND product_id = (SELECT product_id FROM products WHERE product_name = 'Camera Tripod Carbon 1.6m' AND brand = 'Ulanzi' LIMIT 1)
);

-- Reviews
INSERT INTO reviews (user_id, entity_id, rating, comment, type)
SELECT
    (SELECT user_id FROM users WHERE email = 'minh.tran@lensora.local' LIMIT 1),
    (SELECT asset_id FROM assets WHERE serial_number = 'CAN-R6II-0001' LIMIT 1),
    5,
    'Thiet bi dep, hoat dong on dinh, ho tro nhanh.',
    'ASSET'
WHERE NOT EXISTS (
    SELECT 1 FROM reviews
    WHERE user_id = (SELECT user_id FROM users WHERE email = 'minh.tran@lensora.local' LIMIT 1)
      AND entity_id = (SELECT asset_id FROM assets WHERE serial_number = 'CAN-R6II-0001' LIMIT 1)
      AND type = 'ASSET'
);

-- Repair legacy FK from rentals.user_id -> customers(id) if present
DO $$
DECLARE
    legacy_fk_name text;
BEGIN
    SELECT c.conname
    INTO legacy_fk_name
    FROM pg_constraint c
    JOIN pg_class t ON t.oid = c.conrelid
    JOIN pg_namespace n ON n.oid = t.relnamespace
    JOIN pg_attribute a ON a.attrelid = t.oid AND a.attnum = ANY (c.conkey)
    JOIN pg_class rt ON rt.oid = c.confrelid
    WHERE c.contype = 'f'
      AND n.nspname = 'public'
      AND t.relname = 'rentals'
      AND a.attname = 'user_id'
      AND rt.relname = 'customers'
    LIMIT 1;

    IF legacy_fk_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE rentals DROP CONSTRAINT %I', legacy_fk_name);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint c
        JOIN pg_class t ON t.oid = c.conrelid
        JOIN pg_namespace n ON n.oid = t.relnamespace
        JOIN pg_attribute a ON a.attrelid = t.oid AND a.attnum = ANY (c.conkey)
        JOIN pg_class rt ON rt.oid = c.confrelid
        WHERE c.contype = 'f'
          AND n.nspname = 'public'
          AND t.relname = 'rentals'
          AND a.attname = 'user_id'
          AND rt.relname = 'users'
    ) THEN
        ALTER TABLE rentals
            ADD CONSTRAINT fk_rentals_user
            FOREIGN KEY (user_id) REFERENCES users (user_id);
    END IF;
END $$;

-- Rentals
INSERT INTO rentals (user_id, asset_id, start_date, end_date, return_date, deposit_fee, total_rent_fee, penalty_fee, status)
SELECT
    (SELECT user_id FROM users WHERE email = 'hoa.nguyen@lensora.local' LIMIT 1),
    (SELECT asset_id FROM assets WHERE serial_number = 'CAN-RF2470-0001' LIMIT 1),
    CURRENT_DATE,
    CURRENT_DATE + 3,
    NULL,
    7000000,
    1260000,
    0,
    'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1 FROM rentals
    WHERE user_id = (SELECT user_id FROM users WHERE email = 'hoa.nguyen@lensora.local' LIMIT 1)
      AND asset_id = (SELECT asset_id FROM assets WHERE serial_number = 'CAN-RF2470-0001' LIMIT 1)
      AND status = 'ACTIVE'
);
