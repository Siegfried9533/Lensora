-- Migration cơ sở V1
--
-- Schema hiện đang được tạo bởi Hibernate (spring.jpa.hibernate.ddl-auto=update).
-- File này tồn tại để thiết lập baseline Flyway để các thay đổi schema trong tương lai có thể
-- được phiên bản hóa thành V2__*, V3__*, v.v. Khi schema ban đầu ổn định, hãy xuất nó
-- (ví dụ: `pg_dump --schema-only`) vào file này và chuyển ddl-auto sang 'validate'.

SELECT 1;
