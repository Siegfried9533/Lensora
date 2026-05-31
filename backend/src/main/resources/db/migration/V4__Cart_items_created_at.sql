-- Track insertion time so cart items can be displayed newest first.
ALTER TABLE cart_items ADD COLUMN IF NOT EXISTS created_at TIMESTAMP(6);

UPDATE cart_items SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL;

ALTER TABLE cart_items ALTER COLUMN created_at SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_cart_items_user_created_at ON cart_items(user_id, created_at DESC);
