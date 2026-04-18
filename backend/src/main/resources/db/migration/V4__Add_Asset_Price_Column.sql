-- Ensure assets.price exists for deposit calculation logic in Asset entity.
ALTER TABLE assets
    ADD COLUMN IF NOT EXISTS price DOUBLE PRECISION;
