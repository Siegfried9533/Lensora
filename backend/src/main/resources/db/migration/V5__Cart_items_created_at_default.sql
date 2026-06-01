-- Allow cart inserts from application versions that do not explicitly send created_at.
ALTER TABLE cart_items ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;
