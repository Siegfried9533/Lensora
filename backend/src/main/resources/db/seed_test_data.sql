-- ============================================================================
-- seed_test_data.sql
-- Manual PostgreSQL wrapper for Lensora test data.
--
-- Usage from backend/src/main/resources/db:
--   psql postgresql://postgres:<password>@localhost:5433/LensoraDB -f seed_test_data.sql
--
-- The canonical seed content lives in Flyway migration V10.
-- ============================================================================

\ir migration/V10__Seed_test_data.sql
