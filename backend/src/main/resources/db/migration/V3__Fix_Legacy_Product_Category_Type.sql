-- Fix legacy schema drift where products.category_id was created as VARCHAR
-- while current entities expect BIGINT foreign key to categories(category_id).

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'products'
          AND column_name = 'category_id'
          AND data_type = 'character varying'
    ) THEN
        ALTER TABLE products
            ALTER COLUMN category_id TYPE BIGINT
            USING CASE
                WHEN category_id ~ '^[0-9]+$' THEN category_id::BIGINT
                ELSE NULL
            END;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint c
        JOIN pg_class t ON t.oid = c.conrelid
        JOIN pg_namespace n ON n.oid = t.relnamespace
        JOIN pg_attribute a ON a.attrelid = t.oid AND a.attnum = ANY (c.conkey)
        JOIN pg_class rt ON rt.oid = c.confrelid
        WHERE c.contype = 'f'
          AND n.nspname = 'public'
          AND t.relname = 'products'
          AND a.attname = 'category_id'
          AND rt.relname = 'categories'
    ) THEN
        ALTER TABLE products
            ADD CONSTRAINT fk_products_category
            FOREIGN KEY (category_id) REFERENCES categories (category_id);
    END IF;
END $$;
