package com.y2kstore.backend.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Keeps an existing local database compatible with the current entities.
 * New installations should still use database_full.sql and seed_full.sql.
 */
@Component
public class DatabaseCompatibilityInitializer implements ApplicationRunner {
    private final JdbcTemplate jdbcTemplate;
    private final boolean enabled;

    public DatabaseCompatibilityInitializer(
            JdbcTemplate jdbcTemplate,
            @Value("${y2kstore.database-compatibility.enabled:true}") boolean enabled
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.enabled = enabled;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }
        addColumnIfMissing("categories", "category_type", "VARCHAR(20) NOT NULL DEFAULT 'GENERAL'");
        addColumnIfMissing("categories", "parent_category_id", "BIGINT");
        addColumnIfMissing("user", "avatar_url", "MEDIUMTEXT");
        addColumnIfMissing("user", "email_verified", "BOOLEAN DEFAULT FALSE");
        addColumnIfMissing("user", "auth_provider", "VARCHAR(30) NOT NULL DEFAULT 'LOCAL'");
        addColumnIfMissing("user", "reset_token", "VARCHAR(120)");
        addColumnIfMissing("user", "reset_token_expires_at", "DATETIME");
        boolean productCreatedAtAdded = addColumnIfMissing("products", "created_at", "DATETIME DEFAULT CURRENT_TIMESTAMP");
        addColumnIfMissing("orders", "hidden_from_history", "BOOLEAN NOT NULL DEFAULT FALSE");
        addColumnIfMissing("reviews", "image_urls", "TEXT");
        addColumnIfMissing("reviews", "video_urls", "TEXT");
        addColumnIfMissing("banner", "title", "VARCHAR(255)");
        addColumnIfMissing("banner", "subtitle", "TEXT");
        addColumnIfMissing("banner", "button_text", "VARCHAR(80)");
        addColumnIfMissing("banner", "sort_order", "INT DEFAULT 0");
        dropColumnIfExists("user", "email_verification_code");
        dropColumnIfExists("user", "email_verification_expires_at");
        ensureEmailVerificationTable();
        addColumnIfMissing("payments", "sepay_transaction_id", "VARCHAR(100) UNIQUE");

        ensureRole("ROLE_USER");
        ensureRole("ROLE_ADMIN");
        jdbcTemplate.update("UPDATE orders SET status = 'PROCESSING' WHERE status = 'PENDING'");
        jdbcTemplate.update("UPDATE orders o JOIN payments p ON p.order_id = o.id SET o.status = 'PREPARING' WHERE UPPER(o.status) = 'PROCESSING' AND UPPER(p.method) = 'COD'");
        jdbcTemplate.update("UPDATE payments SET status = 'UNPAID' WHERE status IS NULL OR TRIM(status) = ''");
        jdbcTemplate.update("UPDATE categories SET category_type = UPPER(TRIM(COALESCE(category_type, 'GENERAL')))");
        jdbcTemplate.update("UPDATE categories SET category_type = 'GENERAL', parent_category_id = NULL WHERE category_type NOT IN ('GENERAL', 'TAG')");
        jdbcTemplate.update("UPDATE categories child LEFT JOIN categories parent ON parent.id = child.parent_category_id SET child.parent_category_id = NULL WHERE child.category_type = 'TAG' AND (parent.id IS NULL OR parent.category_type <> 'GENERAL')");
        jdbcTemplate.update("UPDATE categories SET category_type = 'GENERAL', parent_category_id = NULL WHERE name IN ('Áo', 'Quần', 'Váy / Đầm', 'Phụ kiện', 'Giày', 'Túi xách', 'Trang sức', 'Áo khoác', 'Set đồ')");
        jdbcTemplate.update("UPDATE categories SET category_type = 'TAG', parent_category_id = (SELECT id FROM (SELECT id FROM categories WHERE name = 'Quần' LIMIT 1) AS parent_category) WHERE name IN ('Jean', 'Suông', 'Dài', 'Cargo', 'Short', 'Nam')");
        jdbcTemplate.update("UPDATE categories SET category_type = 'TAG', parent_category_id = (SELECT id FROM (SELECT id FROM categories WHERE name = 'Áo' LIMIT 1) AS parent_category) WHERE name IN ('Croptop', 'Oversize', 'Baby tee')");
        jdbcTemplate.update("UPDATE categories SET category_type = 'TAG', parent_category_id = (SELECT id FROM (SELECT id FROM categories WHERE name = 'Váy / Đầm' LIMIT 1) AS parent_category) WHERE name IN ('Xếp ly', 'Mini', 'Hai dây')");
        jdbcTemplate.update("UPDATE categories SET category_type = 'TAG', parent_category_id = (SELECT id FROM (SELECT id FROM categories WHERE name = 'Phụ kiện' LIMIT 1) AS parent_category) WHERE name IN ('Mũ', 'Kính', 'Vòng cổ')");
        jdbcTemplate.update("UPDATE categories SET category_type = 'TAG', parent_category_id = (SELECT id FROM (SELECT id FROM categories WHERE name = 'Giày' LIMIT 1) AS parent_category) WHERE name IN ('Chunky', 'Platform', 'Boot')");
        jdbcTemplate.update("UPDATE categories SET category_type = 'TAG', parent_category_id = (SELECT id FROM (SELECT id FROM categories WHERE name = 'Túi xách' LIMIT 1) AS parent_category) WHERE name IN ('Kẹp nách', 'Mini bag', 'Baguette')");
        jdbcTemplate.update("UPDATE categories SET category_type = 'TAG', parent_category_id = (SELECT id FROM (SELECT id FROM categories WHERE name = 'Trang sức' LIMIT 1) AS parent_category) WHERE name IN ('Dây chuyền', 'Khuyên tai')");
        jdbcTemplate.update("UPDATE categories SET category_type = 'TAG', parent_category_id = (SELECT id FROM (SELECT id FROM categories WHERE name = 'Áo khoác' LIMIT 1) AS parent_category) WHERE name IN ('Jacket', 'Cardigan', 'Outerwear')");
        jdbcTemplate.update("UPDATE categories SET category_type = 'TAG', parent_category_id = (SELECT id FROM (SELECT id FROM categories WHERE name = 'Set đồ' LIMIT 1) AS parent_category) WHERE name IN ('Crop', 'Skirt', 'Sporty')");
        jdbcTemplate.update("DELETE FROM product_categories WHERE product_id IN (SELECT id FROM products WHERE category_id IS NOT NULL) AND category_id IN (SELECT id FROM categories WHERE category_type = 'GENERAL')");
        if (productCreatedAtAdded) {
            seedProductCreatedAtDates();
        }
    }

    private boolean addColumnIfMissing(String table, String column, String definition) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?",
                Integer.class,
                table,
                column
        );
        if (count != null && count == 0) {
            jdbcTemplate.execute("ALTER TABLE " + quote(table) + " ADD COLUMN " + quote(column) + " " + definition);
            return true;
        }
        return false;
    }

    private void dropColumnIfExists(String table, String column) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?",
                Integer.class,
                table,
                column
        );
        if (count != null && count > 0) {
            jdbcTemplate.execute("ALTER TABLE " + quote(table) + " DROP COLUMN " + quote(column));
        }
    }

    private void ensureRole(String roleName) {
        jdbcTemplate.update(
                "INSERT INTO role (role_name) SELECT ? WHERE NOT EXISTS (SELECT 1 FROM role WHERE role_name = ?)",
                roleName,
                roleName
        );
    }

    private void ensureEmailVerificationTable() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS email_verifications ("
                + "id BIGINT AUTO_INCREMENT PRIMARY KEY,"
                + "email VARCHAR(100) NOT NULL,"
                + "purpose VARCHAR(30) NOT NULL,"
                + "code VARCHAR(6) NOT NULL,"
                + "expires_at DATETIME NOT NULL,"
                + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "UNIQUE KEY uk_email_verification (email, purpose)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
    }

    private void seedProductCreatedAtDates() {
        jdbcTemplate.update("UPDATE products SET created_at = DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 35 DAY) WHERE name IN ('Áo croptop Y2K Butterfly','Áo thun oversize Graphic Cyberpunk','Áo hoodie pastel Star','Quần cargo túi hộp Pinky','Quần jeans ống rộng Blue Wash','Quần Jean Nam Suông','Chân váy xếp ly Tennis Đen','Váy mini Silver Sparkle','Giày chunky Y2K Retro White','Sneaker Platform Holo Pink','Túi kẹp nách da bóng Pinky','Mũ len beanie ngôi sao','Kính mát Cyber Y2K Silver','Dây chuyền xích đôi khóa bạc','Khuyên tai trái tim kim loại','Set đồ phối Y2K Crop + Skirt')");
        jdbcTemplate.update("UPDATE products SET created_at = CASE name WHEN 'Áo baby tee Cherry Pop' THEN DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY) WHEN 'Quần short denim Star Stitch' THEN DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY) WHEN 'Váy hai dây Mesh Galaxy' THEN DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY) WHEN 'Túi mini baguette Silver Pixel' THEN DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 DAY) WHEN 'Kính oval Pink Chrome' THEN DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY) WHEN 'Boot platform Moonwalk' THEN DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 9 DAY) WHEN 'Jacket denim Washed Blue' THEN DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 11 DAY) WHEN 'Set đồ Sporty Pink Track' THEN DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 13 DAY) ELSE created_at END WHERE name IN ('Áo baby tee Cherry Pop','Quần short denim Star Stitch','Váy hai dây Mesh Galaxy','Túi mini baguette Silver Pixel','Kính oval Pink Chrome','Boot platform Moonwalk','Jacket denim Washed Blue','Set đồ Sporty Pink Track')");
    }

    private String quote(String identifier) {
        return "`" + identifier + "`";
    }
}
