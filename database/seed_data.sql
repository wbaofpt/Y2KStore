SET NAMES utf8mb4;
START TRANSACTION;

-- ==========================================
-- 1. ROLE
-- ==========================================
INSERT INTO role (role_name)
SELECT 'ROLE_USER'
WHERE NOT EXISTS (SELECT 1 FROM role WHERE role_name = 'ROLE_USER');

INSERT INTO role (role_name)
SELECT 'ROLE_ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM role WHERE role_name = 'ROLE_ADMIN');

SET @role_user := (SELECT id FROM role WHERE role_name = 'ROLE_USER' LIMIT 1);
SET @role_admin := (SELECT id FROM role WHERE role_name = 'ROLE_ADMIN' LIMIT 1);

-- ==========================================
-- 2. CATEGORIES
-- ==========================================
INSERT INTO categories (name, description, category_type, parent_category_id, status)
SELECT 'Áo', 'Áo thun, croptop, hoodie, sơ mi phong cách Y2K', 'GENERAL', NULL, 1
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Áo');

INSERT INTO categories (name, description, category_type, parent_category_id, status)
SELECT 'Quần', 'Quần cargo, jeans, shorts và quần ống rộng', 'GENERAL', NULL, 1
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Quần');

INSERT INTO categories (name, description, category_type, parent_category_id, status)
SELECT 'Váy / Đầm', 'Váy xếp ly, váy mini và đầm dạo phố', 'GENERAL', NULL, 1
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Váy / Đầm');

INSERT INTO categories (name, description, category_type, parent_category_id, status)
SELECT 'Phụ kiện', 'Mũ, kính, vòng cổ, dây đeo và item phối đồ', 'GENERAL', NULL, 1
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Phụ kiện');

INSERT INTO categories (name, description, category_type, parent_category_id, status)
SELECT 'Giày', 'Sneaker chunky, platform, sandal và giày retro', 'GENERAL', NULL, 1
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Giày');

INSERT INTO categories (name, description, category_type, parent_category_id, status)
SELECT 'Túi xách', 'Túi kẹp nách, tote, mini bag và túi da bóng', 'GENERAL', NULL, 1
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Túi xách');

INSERT INTO categories (name, description, category_type, parent_category_id, status)
SELECT 'Trang sức', 'Vòng cổ, nhẫn, khuyên tai và phụ kiện metallic', 'GENERAL', NULL, 1
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Trang sức');

INSERT INTO categories (name, description, category_type, parent_category_id, status)
SELECT 'Áo khoác', 'Hoodie, jacket, cardigan và outerwear', 'GENERAL', NULL, 1
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Áo khoác');

INSERT INTO categories (name, description, category_type, parent_category_id, status)
SELECT 'Set đồ', 'Combo phối sẵn theo phong cách Y2K', 'GENERAL', NULL, 1
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Set đồ');

SET @cat_ao := (SELECT id FROM categories WHERE name = 'Áo' LIMIT 1);
SET @cat_quan := (SELECT id FROM categories WHERE name = 'Quần' LIMIT 1);
SET @cat_vay := (SELECT id FROM categories WHERE name = 'Váy / Đầm' LIMIT 1);
SET @cat_phukien := (SELECT id FROM categories WHERE name = 'Phụ kiện' LIMIT 1);
SET @cat_giay := (SELECT id FROM categories WHERE name = 'Giày' LIMIT 1);
SET @cat_tui := (SELECT id FROM categories WHERE name = 'Túi xách' LIMIT 1);
SET @cat_trangsuc := (SELECT id FROM categories WHERE name = 'Trang sức' LIMIT 1);
SET @cat_aokhoac := (SELECT id FROM categories WHERE name = 'Áo khoác' LIMIT 1);
SET @cat_setdo := (SELECT id FROM categories WHERE name = 'Set đồ' LIMIT 1);

INSERT INTO categories (name, description, category_type, parent_category_id, status)
SELECT 'Croptop', 'Tag cho áo croptop', 'TAG', @cat_ao, 1
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Croptop' AND parent_category_id = @cat_ao);

INSERT INTO categories (name, description, category_type, parent_category_id, status)
SELECT 'Oversize', 'Tag cho form oversize', 'TAG', @cat_ao, 1
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Oversize' AND parent_category_id = @cat_ao);

INSERT INTO categories (name, description, category_type, parent_category_id, status)
SELECT 'Baby tee', 'Tag cho áo baby tee', 'TAG', @cat_ao, 1
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Baby tee' AND parent_category_id = @cat_ao);

INSERT INTO categories (name, description, category_type, parent_category_id, status)
SELECT 'Jean', 'Tag cho chất liệu jean', 'TAG', @cat_quan, 1
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Jean' AND parent_category_id = @cat_quan);

INSERT INTO categories (name, description, category_type, parent_category_id, status)
SELECT 'Suông', 'Tag cho dáng suông', 'TAG', @cat_quan, 1
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Suông' AND parent_category_id = @cat_quan);

INSERT INTO categories (name, description, category_type, parent_category_id, status)
SELECT 'Dài', 'Tag cho form dài', 'TAG', @cat_quan, 1
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Dài' AND parent_category_id = @cat_quan);

INSERT INTO categories (name, description, category_type, parent_category_id, status)
SELECT 'Cargo', 'Tag cho quần cargo', 'TAG', @cat_quan, 1
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Cargo' AND parent_category_id = @cat_quan);

INSERT INTO categories (name, description, category_type, parent_category_id, status)
SELECT 'Short', 'Tag cho quần short', 'TAG', @cat_quan, 1
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Short' AND parent_category_id = @cat_quan);

INSERT INTO categories (name, description, category_type, parent_category_id, status)
SELECT 'Nam', 'Tag cho item nam', 'TAG', @cat_quan, 1
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Nam' AND parent_category_id = @cat_quan);

INSERT INTO categories (name, description, category_type, parent_category_id, status)
SELECT 'Xếp ly', 'Tag cho váy xếp ly', 'TAG', @cat_vay, 1
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Xếp ly' AND parent_category_id = @cat_vay);

INSERT INTO categories (name, description, category_type, parent_category_id, status)
SELECT 'Mini', 'Tag cho váy / túi mini', 'TAG', @cat_vay, 1
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Mini' AND parent_category_id = @cat_vay);

INSERT INTO categories (name, description, category_type, parent_category_id, status)
SELECT 'Hai dây', 'Tag cho váy hai dây', 'TAG', @cat_vay, 1
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Hai dây' AND parent_category_id = @cat_vay);

INSERT INTO categories (name, description, category_type, parent_category_id, status)
SELECT 'Mũ', 'Tag cho mũ và beanie', 'TAG', @cat_phukien, 1
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Mũ' AND parent_category_id = @cat_phukien);

INSERT INTO categories (name, description, category_type, parent_category_id, status)
SELECT 'Kính', 'Tag cho kính mát', 'TAG', @cat_phukien, 1
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Kính' AND parent_category_id = @cat_phukien);

INSERT INTO categories (name, description, category_type, parent_category_id, status)
SELECT 'Vòng cổ', 'Tag cho vòng cổ', 'TAG', @cat_phukien, 1
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Vòng cổ' AND parent_category_id = @cat_phukien);

INSERT INTO categories (name, description, category_type, parent_category_id, status)
SELECT 'Chunky', 'Tag cho giày chunky', 'TAG', @cat_giay, 1
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Chunky' AND parent_category_id = @cat_giay);

INSERT INTO categories (name, description, category_type, parent_category_id, status)
SELECT 'Platform', 'Tag cho giày platform', 'TAG', @cat_giay, 1
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Platform' AND parent_category_id = @cat_giay);

INSERT INTO categories (name, description, category_type, parent_category_id, status)
SELECT 'Boot', 'Tag cho boot', 'TAG', @cat_giay, 1
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Boot' AND parent_category_id = @cat_giay);

INSERT INTO categories (name, description, category_type, parent_category_id, status)
SELECT 'Kẹp nách', 'Tag cho túi kẹp nách', 'TAG', @cat_tui, 1
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Kẹp nách' AND parent_category_id = @cat_tui);

INSERT INTO categories (name, description, category_type, parent_category_id, status)
SELECT 'Mini bag', 'Tag cho túi mini bag', 'TAG', @cat_tui, 1
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Mini bag' AND parent_category_id = @cat_tui);

INSERT INTO categories (name, description, category_type, parent_category_id, status)
SELECT 'Baguette', 'Tag cho túi baguette', 'TAG', @cat_tui, 1
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Baguette' AND parent_category_id = @cat_tui);

INSERT INTO categories (name, description, category_type, parent_category_id, status)
SELECT 'Dây chuyền', 'Tag cho dây chuyền', 'TAG', @cat_trangsuc, 1
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Dây chuyền' AND parent_category_id = @cat_trangsuc);

INSERT INTO categories (name, description, category_type, parent_category_id, status)
SELECT 'Khuyên tai', 'Tag cho khuyên tai', 'TAG', @cat_trangsuc, 1
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Khuyên tai' AND parent_category_id = @cat_trangsuc);

INSERT INTO categories (name, description, category_type, parent_category_id, status)
SELECT 'Jacket', 'Tag cho áo khoác jacket', 'TAG', @cat_aokhoac, 1
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Jacket' AND parent_category_id = @cat_aokhoac);

INSERT INTO categories (name, description, category_type, parent_category_id, status)
SELECT 'Cardigan', 'Tag cho áo khoác cardigan', 'TAG', @cat_aokhoac, 1
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Cardigan' AND parent_category_id = @cat_aokhoac);

INSERT INTO categories (name, description, category_type, parent_category_id, status)
SELECT 'Outerwear', 'Tag cho áo khoác ngoài', 'TAG', @cat_aokhoac, 1
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Outerwear' AND parent_category_id = @cat_aokhoac);

INSERT INTO categories (name, description, category_type, parent_category_id, status)
SELECT 'Crop', 'Tag cho set crop', 'TAG', @cat_setdo, 1
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Crop' AND parent_category_id = @cat_setdo);

INSERT INTO categories (name, description, category_type, parent_category_id, status)
SELECT 'Skirt', 'Tag cho set skirt', 'TAG', @cat_setdo, 1
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Skirt' AND parent_category_id = @cat_setdo);

INSERT INTO categories (name, description, category_type, parent_category_id, status)
SELECT 'Sporty', 'Tag cho set sporty', 'TAG', @cat_setdo, 1
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Sporty' AND parent_category_id = @cat_setdo);

SET @cat_croptop := (SELECT id FROM categories WHERE name = 'Croptop' AND parent_category_id = @cat_ao LIMIT 1);
SET @cat_oversize := (SELECT id FROM categories WHERE name = 'Oversize' AND parent_category_id = @cat_ao LIMIT 1);
SET @cat_babytee := (SELECT id FROM categories WHERE name = 'Baby tee' AND parent_category_id = @cat_ao LIMIT 1);
SET @cat_jean := (SELECT id FROM categories WHERE name = 'Jean' AND parent_category_id = @cat_quan LIMIT 1);
SET @cat_suong := (SELECT id FROM categories WHERE name = 'Suông' AND parent_category_id = @cat_quan LIMIT 1);
SET @cat_dai := (SELECT id FROM categories WHERE name = 'Dài' AND parent_category_id = @cat_quan LIMIT 1);
SET @cat_cargo := (SELECT id FROM categories WHERE name = 'Cargo' AND parent_category_id = @cat_quan LIMIT 1);
SET @cat_short := (SELECT id FROM categories WHERE name = 'Short' AND parent_category_id = @cat_quan LIMIT 1);
SET @cat_nam := (SELECT id FROM categories WHERE name = 'Nam' AND parent_category_id = @cat_quan LIMIT 1);
SET @cat_xeply := (SELECT id FROM categories WHERE name = 'Xếp ly' AND parent_category_id = @cat_vay LIMIT 1);
SET @cat_mini := (SELECT id FROM categories WHERE name = 'Mini' AND parent_category_id = @cat_vay LIMIT 1);
SET @cat_haiday := (SELECT id FROM categories WHERE name = 'Hai dây' AND parent_category_id = @cat_vay LIMIT 1);
SET @cat_mu := (SELECT id FROM categories WHERE name = 'Mũ' AND parent_category_id = @cat_phukien LIMIT 1);
SET @cat_kinh := (SELECT id FROM categories WHERE name = 'Kính' AND parent_category_id = @cat_phukien LIMIT 1);
SET @cat_vongco := (SELECT id FROM categories WHERE name = 'Vòng cổ' AND parent_category_id = @cat_phukien LIMIT 1);
SET @cat_chunky := (SELECT id FROM categories WHERE name = 'Chunky' AND parent_category_id = @cat_giay LIMIT 1);
SET @cat_platform := (SELECT id FROM categories WHERE name = 'Platform' AND parent_category_id = @cat_giay LIMIT 1);
SET @cat_boot := (SELECT id FROM categories WHERE name = 'Boot' AND parent_category_id = @cat_giay LIMIT 1);
SET @cat_kepnach := (SELECT id FROM categories WHERE name = 'Kẹp nách' AND parent_category_id = @cat_tui LIMIT 1);
SET @cat_minibag := (SELECT id FROM categories WHERE name = 'Mini bag' AND parent_category_id = @cat_tui LIMIT 1);
SET @cat_baguette := (SELECT id FROM categories WHERE name = 'Baguette' AND parent_category_id = @cat_tui LIMIT 1);
SET @cat_daychuyen := (SELECT id FROM categories WHERE name = 'Dây chuyền' AND parent_category_id = @cat_trangsuc LIMIT 1);
SET @cat_khuyentai := (SELECT id FROM categories WHERE name = 'Khuyên tai' AND parent_category_id = @cat_trangsuc LIMIT 1);
SET @cat_jacket := (SELECT id FROM categories WHERE name = 'Jacket' AND parent_category_id = @cat_aokhoac LIMIT 1);
SET @cat_cardigan := (SELECT id FROM categories WHERE name = 'Cardigan' AND parent_category_id = @cat_aokhoac LIMIT 1);
SET @cat_outerwear := (SELECT id FROM categories WHERE name = 'Outerwear' AND parent_category_id = @cat_aokhoac LIMIT 1);
SET @cat_crop := (SELECT id FROM categories WHERE name = 'Crop' AND parent_category_id = @cat_setdo LIMIT 1);
SET @cat_skirt := (SELECT id FROM categories WHERE name = 'Skirt' AND parent_category_id = @cat_setdo LIMIT 1);
SET @cat_sporty := (SELECT id FROM categories WHERE name = 'Sporty' AND parent_category_id = @cat_setdo LIMIT 1);

UPDATE categories
SET category_type = 'GENERAL', parent_category_id = NULL
WHERE name IN ('Áo', 'Quần', 'Váy / Đầm', 'Phụ kiện', 'Giày', 'Túi xách', 'Trang sức', 'Áo khoác', 'Set đồ');

UPDATE categories
SET category_type = 'TAG'
WHERE id IN (@cat_croptop, @cat_oversize, @cat_babytee, @cat_jean, @cat_suong, @cat_dai, @cat_cargo, @cat_short, @cat_nam, @cat_xeply, @cat_mini, @cat_haiday, @cat_mu, @cat_kinh, @cat_vongco, @cat_chunky, @cat_platform, @cat_boot, @cat_kepnach, @cat_minibag, @cat_baguette, @cat_daychuyen, @cat_khuyentai, @cat_jacket, @cat_cardigan, @cat_outerwear, @cat_crop, @cat_skirt, @cat_sporty);

-- ==========================================
-- 3. PROMOTIONS
-- ==========================================
INSERT INTO promotions (promotion_name, discount_percent, start_date, end_date, status)
SELECT 'Summer Launch', 10.00, NOW() - INTERVAL 30 DAY, NOW() + INTERVAL 180 DAY, 1
WHERE NOT EXISTS (SELECT 1 FROM promotions WHERE promotion_name = 'Summer Launch');

INSERT INTO promotions (promotion_name, discount_percent, start_date, end_date, status)
SELECT 'Flash Sale', 20.00, NOW() - INTERVAL 7 DAY, NOW() + INTERVAL 21 DAY, 1
WHERE NOT EXISTS (SELECT 1 FROM promotions WHERE promotion_name = 'Flash Sale');

INSERT INTO promotions (promotion_name, discount_percent, start_date, end_date, status)
SELECT 'Student Style', 15.00, NOW() - INTERVAL 15 DAY, NOW() + INTERVAL 60 DAY, 1
WHERE NOT EXISTS (SELECT 1 FROM promotions WHERE promotion_name = 'Student Style');

INSERT INTO promotions (promotion_name, discount_percent, start_date, end_date, status)
SELECT 'New Season', 25.00, NOW() - INTERVAL 3 DAY, NOW() + INTERVAL 90 DAY, 1
WHERE NOT EXISTS (SELECT 1 FROM promotions WHERE promotion_name = 'New Season');

SET @promo_summer := (SELECT id FROM promotions WHERE promotion_name = 'Summer Launch' LIMIT 1);
SET @promo_flash := (SELECT id FROM promotions WHERE promotion_name = 'Flash Sale' LIMIT 1);
SET @promo_student := (SELECT id FROM promotions WHERE promotion_name = 'Student Style' LIMIT 1);
SET @promo_new := (SELECT id FROM promotions WHERE promotion_name = 'New Season' LIMIT 1);

-- ==========================================
-- 4. USERS
-- Mật khẩu mẫu cho các tài khoản seed: 123456
-- ==========================================
INSERT INTO user (role_id, password, full_name, email, phone, created_at, avatar_url, email_verified, auth_provider, status)
SELECT @role_admin, '$2a$10$MiS1u1uu8zgy/UHgXNdM3.ED5F/vwIlkww4bKsNoir3cCpzMtJvsG', 'Y2K Admin', 'admin@y2kstore.vn', '0123456789', NOW(), NULL, 0, 'LOCAL', 1
WHERE NOT EXISTS (SELECT 1 FROM user WHERE email = 'admin@y2kstore.vn');

INSERT INTO user (role_id, password, full_name, email, phone, created_at, avatar_url, email_verified, auth_provider, status)
SELECT @role_user, '$2a$10$MiS1u1uu8zgy/UHgXNdM3.ED5F/vwIlkww4bKsNoir3cCpzMtJvsG', 'Quốc Bảo Y2K', 'user@gmail.com', '0987654321', NOW(), NULL, 0, 'LOCAL', 1
WHERE NOT EXISTS (SELECT 1 FROM user WHERE email = 'user@gmail.com');

INSERT INTO user (role_id, password, full_name, email, phone, created_at, avatar_url, email_verified, auth_provider, status)
SELECT @role_user, '$2a$10$MiS1u1uu8zgy/UHgXNdM3.ED5F/vwIlkww4bKsNoir3cCpzMtJvsG', 'Minh Anh', 'minhanh@y2kstore.vn', '0911222333', NOW(), NULL, 0, 'LOCAL', 1
WHERE NOT EXISTS (SELECT 1 FROM user WHERE email = 'minhanh@y2kstore.vn');

INSERT INTO user (role_id, password, full_name, email, phone, created_at, avatar_url, email_verified, auth_provider, status)
SELECT @role_user, '$2a$10$MiS1u1uu8zgy/UHgXNdM3.ED5F/vwIlkww4bKsNoir3cCpzMtJvsG', 'Gia Huy', 'giahuy@y2kstore.vn', '0933444555', NOW(), NULL, 0, 'LOCAL', 1
WHERE NOT EXISTS (SELECT 1 FROM user WHERE email = 'giahuy@y2kstore.vn');

SET @admin_id := (SELECT id FROM user WHERE email = 'admin@y2kstore.vn' LIMIT 1);
SET @user_id_1 := (SELECT id FROM user WHERE email = 'user@gmail.com' LIMIT 1);
SET @user_id_2 := (SELECT id FROM user WHERE email = 'minhanh@y2kstore.vn' LIMIT 1);
SET @user_id_3 := (SELECT id FROM user WHERE email = 'giahuy@y2kstore.vn' LIMIT 1);

-- ==========================================
-- 5. ADDRESSES
-- ==========================================
INSERT INTO address (user_id, province, district, ward, detail, is_default)
SELECT @user_id_1, 'TP. Hồ Chí Minh', 'Quận 1', 'Bến Nghé', '123 Lê Lợi, Quận 1', 1
WHERE NOT EXISTS (SELECT 1 FROM address WHERE user_id = @user_id_1 AND detail = '123 Lê Lợi, Quận 1');

INSERT INTO address (user_id, province, district, ward, detail, is_default)
SELECT @user_id_2, 'Hà Nội', 'Cầu Giấy', 'Dịch Vọng', '45 Trần Thái Tông, Cầu Giấy', 1
WHERE NOT EXISTS (SELECT 1 FROM address WHERE user_id = @user_id_2 AND detail = '45 Trần Thái Tông, Cầu Giấy');

INSERT INTO address (user_id, province, district, ward, detail, is_default)
SELECT @user_id_3, 'Đà Nẵng', 'Hải Châu', 'Hòa Cường', '88 Nguyễn Văn Linh, Hải Châu', 1
WHERE NOT EXISTS (SELECT 1 FROM address WHERE user_id = @user_id_3 AND detail = '88 Nguyễn Văn Linh, Hải Châu');

-- ==========================================
-- 6. PRODUCTS
-- products.status: 0=Nháp, 1=Đang bán, 2=Hết hàng, 3=Sắp về, 4=Ẩn
-- ==========================================
INSERT INTO products (category_id, name, description, price, image, status)
SELECT @cat_ao, 'Áo croptop Y2K Butterfly', 'Áo croptop phong cách Y2K thêu hình bướm cá tính.', 249000.00, '/summer_y2k.png', 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Áo croptop Y2K Butterfly');

INSERT INTO products (category_id, name, description, price, image, status)
SELECT @cat_ao, 'Áo thun oversize Graphic Cyberpunk', 'Áo thun form rộng unisex, hình in đậm chất cyber.', 199000.00, '/summer_y2k.png', 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Áo thun oversize Graphic Cyberpunk');

INSERT INTO products (category_id, name, description, price, image, status)
SELECT @cat_aokhoac, 'Áo hoodie pastel Star', 'Hoodie pastel mềm, form rộng, dễ phối đồ.', 429000.00, '/y2k_banner.png', 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Áo hoodie pastel Star');

INSERT INTO products (category_id, name, description, price, image, status)
SELECT @cat_quan, 'Quần cargo túi hộp Pinky', 'Quần cargo túi hộp màu hồng phấn, cá tính và nổi bật.', 359000.00, '/summer_y2k.png', 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Quần cargo túi hộp Pinky');

INSERT INTO products (category_id, name, description, price, image, status)
SELECT @cat_quan, 'Quần jeans ống rộng Blue Wash', 'Jeans ống rộng wash xanh nhạt hợp phối sneaker.', 389000.00, '/summer_y2k.png', 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Quần jeans ống rộng Blue Wash');

INSERT INTO products (category_id, name, description, price, image, status)
SELECT @cat_quan, 'Quần Jean Nam Suông', 'Quần jean nam dáng suông, dễ phối với áo thun và sneaker.', 419000.00, '/summer_y2k.png', 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Quần Jean Nam Suông');

INSERT INTO products (category_id, name, description, price, image, status)
SELECT @cat_vay, 'Chân váy xếp ly Tennis Đen', 'Chân váy xếp ly cá tính phong cách K-Pop.', 279000.00, '/summer_y2k.png', 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Chân váy xếp ly Tennis Đen');

INSERT INTO products (category_id, name, description, price, image, status)
SELECT @cat_vay, 'Váy mini Silver Sparkle', 'Váy mini ánh bạc nổi bật trong buổi tối.', 319000.00, '/y2k_banner.png', 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Váy mini Silver Sparkle');

INSERT INTO products (category_id, name, description, price, image, status)
SELECT @cat_giay, 'Giày chunky Y2K Retro White', 'Giày sneaker chunky đế cao, hợp outfit đường phố.', 699000.00, '/summer_y2k.png', 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Giày chunky Y2K Retro White');

INSERT INTO products (category_id, name, description, price, image, status)
SELECT @cat_giay, 'Sneaker Platform Holo Pink', 'Sneaker đế platform phối hồng hologram.', 759000.00, '/y2k_banner.png', 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Sneaker Platform Holo Pink');

INSERT INTO products (category_id, name, description, price, image, status)
SELECT @cat_tui, 'Túi kẹp nách da bóng Pinky', 'Túi kẹp nách da PU bóng loáng, nhỏ gọn và dễ phối.', 199000.00, '/summer_y2k.png', 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Túi kẹp nách da bóng Pinky');

INSERT INTO products (category_id, name, description, price, image, status)
SELECT @cat_phukien, 'Mũ len beanie ngôi sao', 'Mũ len beanie thêu họa tiết ngôi sao Y2K.', 129000.00, '/summer_y2k.png', 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Mũ len beanie ngôi sao');

INSERT INTO products (category_id, name, description, price, image, status)
SELECT @cat_phukien, 'Kính mát Cyber Y2K Silver', 'Kính mát gọng bạc ôm sát mặt, chất cyber futurism.', 159000.00, '/summer_y2k.png', 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Kính mát Cyber Y2K Silver');

INSERT INTO products (category_id, name, description, price, image, status)
SELECT @cat_trangsuc, 'Dây chuyền xích đôi khóa bạc', 'Vòng cổ layer xích kim loại cho outfit cá tính.', 89000.00, '/summer_y2k.png', 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Dây chuyền xích đôi khóa bạc');

INSERT INTO products (category_id, name, description, price, image, status)
SELECT @cat_trangsuc, 'Khuyên tai trái tim kim loại', 'Khuyên tai sáng bóng kiểu metallic.', 79000.00, '/y2k_banner.png', 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Khuyên tai trái tim kim loại');

INSERT INTO products (category_id, name, description, price, image, status)
SELECT @cat_setdo, 'Set đồ phối Y2K Crop + Skirt', 'Bộ set phối sẵn gồm crop top và chân váy.', 499000.00, '/summer_y2k.png', 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Set đồ phối Y2K Crop + Skirt');

INSERT INTO products (category_id, name, description, price, image, status)
SELECT @cat_ao, 'Áo baby tee Cherry Pop', 'Áo baby tee ôm vừa, họa tiết cherry nổi bật.', 189000.00, '/summer_y2k.png', 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Áo baby tee Cherry Pop');

INSERT INTO products (category_id, name, description, price, image, status)
SELECT @cat_quan, 'Quần short denim Star Stitch', 'Quần short denim thêu ngôi sao, dễ phối cùng baby tee.', 259000.00, '/summer_y2k.png', 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Quần short denim Star Stitch');

INSERT INTO products (category_id, name, description, price, image, status)
SELECT @cat_vay, 'Váy hai dây Mesh Galaxy', 'Váy hai dây ánh kim nhẹ cho những buổi đi chơi tối.', 369000.00, '/y2k_banner.png', 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Váy hai dây Mesh Galaxy');

INSERT INTO products (category_id, name, description, price, image, status)
SELECT @cat_tui, 'Túi mini baguette Silver Pixel', 'Túi mini dáng baguette ánh bạc, nhỏ gọn và cá tính.', 229000.00, '/y2k_banner.png', 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Túi mini baguette Silver Pixel');

INSERT INTO products (category_id, name, description, price, image, status)
SELECT @cat_phukien, 'Kính oval Pink Chrome', 'Kính oval gọng chrome hồng, tạo điểm nhấn cho outfit.', 179000.00, '/summer_y2k.png', 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Kính oval Pink Chrome');

INSERT INTO products (category_id, name, description, price, image, status)
SELECT @cat_giay, 'Boot platform Moonwalk', 'Boot platform đế cao, form chắc và dễ phối đồ streetwear.', 829000.00, '/y2k_banner.png', 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Boot platform Moonwalk');

INSERT INTO products (category_id, name, description, price, image, status)
SELECT @cat_aokhoac, 'Jacket denim Washed Blue', 'Jacket denim wash xanh, phom rộng unisex.', 549000.00, '/summer_y2k.png', 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Jacket denim Washed Blue');

INSERT INTO products (category_id, name, description, price, image, status)
SELECT @cat_setdo, 'Set đồ Sporty Pink Track', 'Set áo và quần sporty màu hồng cho outfit năng động.', 599000.00, '/summer_y2k.png', 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Set đồ Sporty Pink Track');

UPDATE products
SET created_at = DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 35 DAY)
WHERE name IN (
    'Áo croptop Y2K Butterfly',
    'Áo thun oversize Graphic Cyberpunk',
    'Áo hoodie pastel Star',
    'Quần cargo túi hộp Pinky',
    'Quần jeans ống rộng Blue Wash',
    'Quần Jean Nam Suông',
    'Chân váy xếp ly Tennis Đen',
    'Váy mini Silver Sparkle',
    'Giày chunky Y2K Retro White',
    'Sneaker Platform Holo Pink',
    'Túi kẹp nách da bóng Pinky',
    'Mũ len beanie ngôi sao',
    'Kính mát Cyber Y2K Silver',
    'Dây chuyền xích đôi khóa bạc',
    'Khuyên tai trái tim kim loại',
    'Set đồ phối Y2K Crop + Skirt'
);

UPDATE products
SET created_at = CASE name
    WHEN 'Áo baby tee Cherry Pop' THEN DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY)
    WHEN 'Quần short denim Star Stitch' THEN DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY)
    WHEN 'Váy hai dây Mesh Galaxy' THEN DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY)
    WHEN 'Túi mini baguette Silver Pixel' THEN DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 DAY)
    WHEN 'Kính oval Pink Chrome' THEN DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY)
    WHEN 'Boot platform Moonwalk' THEN DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 9 DAY)
    WHEN 'Jacket denim Washed Blue' THEN DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 11 DAY)
    WHEN 'Set đồ Sporty Pink Track' THEN DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 13 DAY)
    ELSE created_at
END
WHERE name IN (
    'Áo baby tee Cherry Pop',
    'Quần short denim Star Stitch',
    'Váy hai dây Mesh Galaxy',
    'Túi mini baguette Silver Pixel',
    'Kính oval Pink Chrome',
    'Boot platform Moonwalk',
    'Jacket denim Washed Blue',
    'Set đồ Sporty Pink Track'
);

SET @p1 := (SELECT id FROM products WHERE name = 'Áo croptop Y2K Butterfly' LIMIT 1);
SET @p2 := (SELECT id FROM products WHERE name = 'Áo thun oversize Graphic Cyberpunk' LIMIT 1);
SET @p3 := (SELECT id FROM products WHERE name = 'Áo hoodie pastel Star' LIMIT 1);
SET @p4 := (SELECT id FROM products WHERE name = 'Quần cargo túi hộp Pinky' LIMIT 1);
SET @p5 := (SELECT id FROM products WHERE name = 'Quần jeans ống rộng Blue Wash' LIMIT 1);
SET @p6 := (SELECT id FROM products WHERE name = 'Quần Jean Nam Suông' LIMIT 1);
SET @p7 := (SELECT id FROM products WHERE name = 'Chân váy xếp ly Tennis Đen' LIMIT 1);
SET @p8 := (SELECT id FROM products WHERE name = 'Váy mini Silver Sparkle' LIMIT 1);
SET @p9 := (SELECT id FROM products WHERE name = 'Giày chunky Y2K Retro White' LIMIT 1);
SET @p10 := (SELECT id FROM products WHERE name = 'Sneaker Platform Holo Pink' LIMIT 1);
SET @p11 := (SELECT id FROM products WHERE name = 'Túi kẹp nách da bóng Pinky' LIMIT 1);
SET @p12 := (SELECT id FROM products WHERE name = 'Mũ len beanie ngôi sao' LIMIT 1);
SET @p13 := (SELECT id FROM products WHERE name = 'Kính mát Cyber Y2K Silver' LIMIT 1);
SET @p14 := (SELECT id FROM products WHERE name = 'Dây chuyền xích đôi khóa bạc' LIMIT 1);
SET @p15 := (SELECT id FROM products WHERE name = 'Khuyên tai trái tim kim loại' LIMIT 1);
SET @p16 := (SELECT id FROM products WHERE name = 'Set đồ phối Y2K Crop + Skirt' LIMIT 1);
SET @p17 := (SELECT id FROM products WHERE name = 'Áo baby tee Cherry Pop' LIMIT 1);
SET @p18 := (SELECT id FROM products WHERE name = 'Quần short denim Star Stitch' LIMIT 1);
SET @p19 := (SELECT id FROM products WHERE name = 'Váy hai dây Mesh Galaxy' LIMIT 1);
SET @p20 := (SELECT id FROM products WHERE name = 'Túi mini baguette Silver Pixel' LIMIT 1);
SET @p21 := (SELECT id FROM products WHERE name = 'Kính oval Pink Chrome' LIMIT 1);
SET @p22 := (SELECT id FROM products WHERE name = 'Boot platform Moonwalk' LIMIT 1);
SET @p23 := (SELECT id FROM products WHERE name = 'Jacket denim Washed Blue' LIMIT 1);
SET @p24 := (SELECT id FROM products WHERE name = 'Set đồ Sporty Pink Track' LIMIT 1);

-- product_categories chỉ lưu tag sản phẩm, không lặp danh mục chung.
INSERT INTO product_categories (product_id, category_id)
SELECT @p1, @cat_croptop
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE product_id = @p1 AND category_id = @cat_croptop);

INSERT INTO product_categories (product_id, category_id)
SELECT @p2, @cat_oversize
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE product_id = @p2 AND category_id = @cat_oversize);

INSERT INTO product_categories (product_id, category_id)
SELECT @p3, @cat_outerwear
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE product_id = @p3 AND category_id = @cat_outerwear);

INSERT INTO product_categories (product_id, category_id)
SELECT @p4, @cat_cargo
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE product_id = @p4 AND category_id = @cat_cargo);

INSERT INTO product_categories (product_id, category_id)
SELECT @p5, @cat_jean
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE product_id = @p5 AND category_id = @cat_jean);

INSERT INTO product_categories (product_id, category_id)
SELECT @p5, @cat_dai
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE product_id = @p5 AND category_id = @cat_dai);

INSERT INTO product_categories (product_id, category_id)
SELECT @p6, @cat_jean
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE product_id = @p6 AND category_id = @cat_jean);

INSERT INTO product_categories (product_id, category_id)
SELECT @p6, @cat_nam
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE product_id = @p6 AND category_id = @cat_nam);

INSERT INTO product_categories (product_id, category_id)
SELECT @p6, @cat_suong
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE product_id = @p6 AND category_id = @cat_suong);

INSERT INTO product_categories (product_id, category_id)
SELECT @p6, @cat_dai
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE product_id = @p6 AND category_id = @cat_dai);

INSERT INTO product_categories (product_id, category_id)
SELECT @p7, @cat_xeply
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE product_id = @p7 AND category_id = @cat_xeply);

INSERT INTO product_categories (product_id, category_id)
SELECT @p8, @cat_mini
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE product_id = @p8 AND category_id = @cat_mini);

INSERT INTO product_categories (product_id, category_id)
SELECT @p9, @cat_chunky
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE product_id = @p9 AND category_id = @cat_chunky);

INSERT INTO product_categories (product_id, category_id)
SELECT @p10, @cat_platform
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE product_id = @p10 AND category_id = @cat_platform);

INSERT INTO product_categories (product_id, category_id)
SELECT @p11, @cat_kepnach
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE product_id = @p11 AND category_id = @cat_kepnach);

INSERT INTO product_categories (product_id, category_id)
SELECT @p12, @cat_mu
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE product_id = @p12 AND category_id = @cat_mu);

INSERT INTO product_categories (product_id, category_id)
SELECT @p13, @cat_kinh
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE product_id = @p13 AND category_id = @cat_kinh);

INSERT INTO product_categories (product_id, category_id)
SELECT @p14, @cat_daychuyen
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE product_id = @p14 AND category_id = @cat_daychuyen);

INSERT INTO product_categories (product_id, category_id)
SELECT @p14, @cat_vongco
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE product_id = @p14 AND category_id = @cat_vongco);

INSERT INTO product_categories (product_id, category_id)
SELECT @p15, @cat_khuyentai
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE product_id = @p15 AND category_id = @cat_khuyentai);

INSERT INTO product_categories (product_id, category_id)
SELECT @p16, @cat_crop
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE product_id = @p16 AND category_id = @cat_crop);

INSERT INTO product_categories (product_id, category_id)
SELECT @p16, @cat_skirt
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE product_id = @p16 AND category_id = @cat_skirt);

INSERT INTO product_categories (product_id, category_id)
SELECT @p17, @cat_babytee
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE product_id = @p17 AND category_id = @cat_babytee);

INSERT INTO product_categories (product_id, category_id)
SELECT @p18, @cat_short
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE product_id = @p18 AND category_id = @cat_short);

INSERT INTO product_categories (product_id, category_id)
SELECT @p18, @cat_jean
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE product_id = @p18 AND category_id = @cat_jean);

INSERT INTO product_categories (product_id, category_id)
SELECT @p19, @cat_haiday
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE product_id = @p19 AND category_id = @cat_haiday);

INSERT INTO product_categories (product_id, category_id)
SELECT @p19, @cat_mini
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE product_id = @p19 AND category_id = @cat_mini);

INSERT INTO product_categories (product_id, category_id)
SELECT @p20, @cat_minibag
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE product_id = @p20 AND category_id = @cat_minibag);

INSERT INTO product_categories (product_id, category_id)
SELECT @p20, @cat_baguette
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE product_id = @p20 AND category_id = @cat_baguette);

INSERT INTO product_categories (product_id, category_id)
SELECT @p21, @cat_kinh
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE product_id = @p21 AND category_id = @cat_kinh);

INSERT INTO product_categories (product_id, category_id)
SELECT @p22, @cat_boot
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE product_id = @p22 AND category_id = @cat_boot);

INSERT INTO product_categories (product_id, category_id)
SELECT @p22, @cat_platform
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE product_id = @p22 AND category_id = @cat_platform);

INSERT INTO product_categories (product_id, category_id)
SELECT @p23, @cat_jacket
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE product_id = @p23 AND category_id = @cat_jacket);

INSERT INTO product_categories (product_id, category_id)
SELECT @p23, @cat_outerwear
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE product_id = @p23 AND category_id = @cat_outerwear);

INSERT INTO product_categories (product_id, category_id)
SELECT @p24, @cat_sporty
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE product_id = @p24 AND category_id = @cat_sporty);

INSERT INTO product_categories (product_id, category_id)
SELECT @p24, @cat_crop
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE product_id = @p24 AND category_id = @cat_crop);

INSERT INTO product_categories (product_id, category_id)
SELECT @p24, @cat_skirt
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE product_id = @p24 AND category_id = @cat_skirt);

-- Dọn dữ liệu cũ nếu chạy seed trên database đã có liên kết danh mục chung.
-- Điều kiện dùng cả hai cột khóa để tương thích SQL_SAFE_UPDATES.
DELETE FROM product_categories
WHERE product_id IN (@p1, @p2, @p3, @p4, @p5, @p6, @p7, @p8, @p9, @p10, @p11, @p12, @p13, @p14, @p15, @p16, @p17, @p18, @p19, @p20, @p21, @p22, @p23, @p24)
  AND category_id IN (@cat_ao, @cat_quan, @cat_vay, @cat_phukien, @cat_giay, @cat_tui, @cat_trangsuc, @cat_aokhoac, @cat_setdo);

-- ==========================================
-- 7. VARIANTS
-- ==========================================
INSERT INTO product_variants (product_id, size, color, stock, price, status)
SELECT @p1, 'S', 'Pink', 10, 249000.00, 1
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p1 AND size = 'S' AND color = 'Pink');

INSERT INTO product_variants (product_id, size, color, stock, price, status)
SELECT @p1, 'M', 'Pink', 12, 249000.00, 1
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p1 AND size = 'M' AND color = 'Pink');

INSERT INTO product_variants (product_id, size, color, stock, price, status)
SELECT @p2, 'M', 'Black', 16, 199000.00, 1
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p2 AND size = 'M' AND color = 'Black');

INSERT INTO product_variants (product_id, size, color, stock, price, status)
SELECT @p2, 'L', 'Black', 14, 199000.00, 1
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p2 AND size = 'L' AND color = 'Black');

INSERT INTO product_variants (product_id, size, color, stock, price, status)
SELECT @p3, 'M', 'Cream', 11, 429000.00, 1
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p3 AND size = 'M' AND color = 'Cream');

INSERT INTO product_variants (product_id, size, color, stock, price, status)
SELECT @p4, 'M', 'Pink', 15, 359000.00, 1
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p4 AND size = 'M' AND color = 'Pink');

INSERT INTO product_variants (product_id, size, color, stock, price, status)
SELECT @p4, 'L', 'Pink', 12, 359000.00, 1
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p4 AND size = 'L' AND color = 'Pink');

INSERT INTO product_variants (product_id, size, color, stock, price, status)
SELECT @p5, 'M', 'Blue', 13, 389000.00, 1
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p5 AND size = 'M' AND color = 'Blue');

INSERT INTO product_variants (product_id, size, color, stock, price, status)
SELECT @p6, 'S', 'Black', 18, 419000.00, 1
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p6 AND size = 'S' AND color = 'Black');

INSERT INTO product_variants (product_id, size, color, stock, price, status)
SELECT @p6, 'M', 'Black', 16, 419000.00, 1
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p6 AND size = 'M' AND color = 'Black');

INSERT INTO product_variants (product_id, size, color, stock, price, status)
SELECT @p7, 'M', 'Silver', 9, 279000.00, 1
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p7 AND size = 'M' AND color = 'Silver');

INSERT INTO product_variants (product_id, size, color, stock, price, status)
SELECT @p8, 'S', 'Silver', 10, 319000.00, 1
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p8 AND size = 'S' AND color = 'Silver');

INSERT INTO product_variants (product_id, size, color, stock, price, status)
SELECT @p8, 'M', 'Silver', 8, 319000.00, 1
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p8 AND size = 'M' AND color = 'Silver');

INSERT INTO product_variants (product_id, size, color, stock, price, status)
SELECT @p9, '36', 'White', 10, 699000.00, 1
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p9 AND size = '36' AND color = 'White');

INSERT INTO product_variants (product_id, size, color, stock, price, status)
SELECT @p9, '37', 'White', 10, 699000.00, 1
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p9 AND size = '37' AND color = 'White');

INSERT INTO product_variants (product_id, size, color, stock, price, status)
SELECT @p10, '36', 'Pink', 8, 759000.00, 1
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p10 AND size = '36' AND color = 'Pink');

INSERT INTO product_variants (product_id, size, color, stock, price, status)
SELECT @p11, 'Free size', 'Pink', 14, 199000.00, 1
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p11 AND size = 'Free size' AND color = 'Pink');

INSERT INTO product_variants (product_id, size, color, stock, price, status)
SELECT @p12, 'Free size', 'Gray', 20, 129000.00, 1
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p12 AND size = 'Free size' AND color = 'Gray');

INSERT INTO product_variants (product_id, size, color, stock, price, status)
SELECT @p13, 'Free size', 'Silver', 18, 159000.00, 1
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p13 AND size = 'Free size' AND color = 'Silver');

INSERT INTO product_variants (product_id, size, color, stock, price, status)
SELECT @p14, 'Free size', 'Silver', 30, 89000.00, 1
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p14 AND size = 'Free size' AND color = 'Silver');

INSERT INTO product_variants (product_id, size, color, stock, price, status)
SELECT @p15, 'Free size', 'Silver', 24, 79000.00, 1
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p15 AND size = 'Free size' AND color = 'Silver');

INSERT INTO product_variants (product_id, size, color, stock, price, status)
SELECT @p16, 'M', 'Black', 14, 499000.00, 1
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p16 AND size = 'M' AND color = 'Black');

INSERT INTO product_variants (product_id, size, color, stock, price, status)
SELECT @p16, 'L', 'Black', 12, 499000.00, 1
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p16 AND size = 'L' AND color = 'Black');

INSERT INTO product_variants (product_id, size, color, stock, price, status)
SELECT @p17, 'S', 'Red', 12, 189000.00, 1
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p17 AND size = 'S' AND color = 'Red');

INSERT INTO product_variants (product_id, size, color, stock, price, status)
SELECT @p17, 'M', 'Red', 10, 189000.00, 1
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p17 AND size = 'M' AND color = 'Red');

INSERT INTO product_variants (product_id, size, color, stock, price, status)
SELECT @p18, 'M', 'Blue', 14, 259000.00, 1
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p18 AND size = 'M' AND color = 'Blue');

INSERT INTO product_variants (product_id, size, color, stock, price, status)
SELECT @p19, 'S', 'Silver', 9, 369000.00, 1
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p19 AND size = 'S' AND color = 'Silver');

INSERT INTO product_variants (product_id, size, color, stock, price, status)
SELECT @p20, 'Free size', 'Silver', 16, 229000.00, 1
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p20 AND size = 'Free size' AND color = 'Silver');

INSERT INTO product_variants (product_id, size, color, stock, price, status)
SELECT @p21, 'Free size', 'Pink', 18, 179000.00, 1
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p21 AND size = 'Free size' AND color = 'Pink');

INSERT INTO product_variants (product_id, size, color, stock, price, status)
SELECT @p22, '37', 'Black', 8, 829000.00, 1
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p22 AND size = '37' AND color = 'Black');

INSERT INTO product_variants (product_id, size, color, stock, price, status)
SELECT @p23, 'M', 'Blue', 11, 549000.00, 1
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p23 AND size = 'M' AND color = 'Blue');

INSERT INTO product_variants (product_id, size, color, stock, price, status)
SELECT @p24, 'M', 'Pink', 13, 599000.00, 1
WHERE NOT EXISTS (SELECT 1 FROM product_variants WHERE product_id = @p24 AND size = 'M' AND color = 'Pink');

UPDATE product_variants
SET price = 419000.00
WHERE product_id = @p6 AND size IN ('S', 'M') AND color = 'Black';

UPDATE product_variants
SET price = 279000.00
WHERE product_id = @p7 AND size = 'M' AND color = 'Silver';

SET @v1 := (SELECT pv.id FROM product_variants pv JOIN products p ON p.id = pv.product_id WHERE p.name = 'Áo croptop Y2K Butterfly' AND pv.size = 'M' AND pv.color = 'Pink' LIMIT 1);
SET @v2 := (SELECT pv.id FROM product_variants pv JOIN products p ON p.id = pv.product_id WHERE p.name = 'Quần cargo túi hộp Pinky' AND pv.size = 'M' AND pv.color = 'Pink' LIMIT 1);
SET @v3 := (SELECT pv.id FROM product_variants pv JOIN products p ON p.id = pv.product_id WHERE p.name = 'Giày chunky Y2K Retro White' AND pv.size = '36' AND pv.color = 'White' LIMIT 1);
SET @v4 := (SELECT pv.id FROM product_variants pv JOIN products p ON p.id = pv.product_id WHERE p.name = 'Túi kẹp nách da bóng Pinky' AND pv.size = 'Free size' AND pv.color = 'Pink' LIMIT 1);
SET @v11 := (SELECT pv.id FROM product_variants pv JOIN products p ON p.id = pv.product_id WHERE p.name = 'Mũ len beanie ngôi sao' AND pv.size = 'Free size' AND pv.color = 'Gray' LIMIT 1);

-- ==========================================
-- 8. IMAGES, PRODUCT PROMOTIONS, BANNERS
-- ==========================================
INSERT INTO product_images (product_id, image_url)
SELECT @p1, '/summer_y2k.png'
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p1 AND image_url = '/summer_y2k.png');

INSERT INTO product_images (product_id, image_url)
SELECT @p1, '/y2k_banner.png'
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p1 AND image_url = '/y2k_banner.png');

INSERT INTO product_images (product_id, image_url)
SELECT @p4, '/summer_y2k.png'
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p4 AND image_url = '/summer_y2k.png');

INSERT INTO product_images (product_id, image_url)
SELECT @p9, '/summer_y2k.png'
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p9 AND image_url = '/summer_y2k.png');

INSERT INTO product_images (product_id, image_url)
SELECT @p11, '/y2k_banner.png'
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p11 AND image_url = '/y2k_banner.png');

INSERT INTO product_images (product_id, image_url)
SELECT @p16, '/summer_y2k.png'
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p16 AND image_url = '/summer_y2k.png');

INSERT INTO product_images (product_id, image_url)
SELECT @p17, '/summer_y2k.png'
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p17 AND image_url = '/summer_y2k.png');

INSERT INTO product_images (product_id, image_url)
SELECT @p19, '/y2k_banner.png'
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p19 AND image_url = '/y2k_banner.png');

INSERT INTO product_images (product_id, image_url)
SELECT @p22, '/y2k_banner.png'
WHERE NOT EXISTS (SELECT 1 FROM product_images WHERE product_id = @p22 AND image_url = '/y2k_banner.png');

INSERT INTO product_promotions (product_id, promotion_id)
SELECT @p1, @promo_summer
WHERE NOT EXISTS (SELECT 1 FROM product_promotions WHERE product_id = @p1 AND promotion_id = @promo_summer);

INSERT INTO product_promotions (product_id, promotion_id)
SELECT @p2, @promo_summer
WHERE NOT EXISTS (SELECT 1 FROM product_promotions WHERE product_id = @p2 AND promotion_id = @promo_summer);

INSERT INTO product_promotions (product_id, promotion_id)
SELECT @p4, @promo_flash
WHERE NOT EXISTS (SELECT 1 FROM product_promotions WHERE product_id = @p4 AND promotion_id = @promo_flash);

INSERT INTO product_promotions (product_id, promotion_id)
SELECT @p7, @promo_student
WHERE NOT EXISTS (SELECT 1 FROM product_promotions WHERE product_id = @p7 AND promotion_id = @promo_student);

INSERT INTO product_promotions (product_id, promotion_id)
SELECT @p9, @promo_flash
WHERE NOT EXISTS (SELECT 1 FROM product_promotions WHERE product_id = @p9 AND promotion_id = @promo_flash);

INSERT INTO product_promotions (product_id, promotion_id)
SELECT @p10, @promo_flash
WHERE NOT EXISTS (SELECT 1 FROM product_promotions WHERE product_id = @p10 AND promotion_id = @promo_flash);

INSERT INTO product_promotions (product_id, promotion_id)
SELECT @p11, @promo_summer
WHERE NOT EXISTS (SELECT 1 FROM product_promotions WHERE product_id = @p11 AND promotion_id = @promo_summer);

INSERT INTO product_promotions (product_id, promotion_id)
SELECT @p12, @promo_student
WHERE NOT EXISTS (SELECT 1 FROM product_promotions WHERE product_id = @p12 AND promotion_id = @promo_student);

INSERT INTO product_promotions (product_id, promotion_id)
SELECT @p13, @promo_flash
WHERE NOT EXISTS (SELECT 1 FROM product_promotions WHERE product_id = @p13 AND promotion_id = @promo_flash);

INSERT INTO product_promotions (product_id, promotion_id)
SELECT @p14, @promo_new
WHERE NOT EXISTS (SELECT 1 FROM product_promotions WHERE product_id = @p14 AND promotion_id = @promo_new);

INSERT INTO product_promotions (product_id, promotion_id)
SELECT @p15, @promo_new
WHERE NOT EXISTS (SELECT 1 FROM product_promotions WHERE product_id = @p15 AND promotion_id = @promo_new);

INSERT INTO product_promotions (product_id, promotion_id)
SELECT @p16, @promo_flash
WHERE NOT EXISTS (SELECT 1 FROM product_promotions WHERE product_id = @p16 AND promotion_id = @promo_flash);

INSERT INTO product_promotions (product_id, promotion_id)
SELECT @p17, @promo_summer
WHERE NOT EXISTS (SELECT 1 FROM product_promotions WHERE product_id = @p17 AND promotion_id = @promo_summer);

INSERT INTO product_promotions (product_id, promotion_id)
SELECT @p19, @promo_student
WHERE NOT EXISTS (SELECT 1 FROM product_promotions WHERE product_id = @p19 AND promotion_id = @promo_student);

INSERT INTO product_promotions (product_id, promotion_id)
SELECT @p22, @promo_flash
WHERE NOT EXISTS (SELECT 1 FROM product_promotions WHERE product_id = @p22 AND promotion_id = @promo_flash);

INSERT INTO product_promotions (product_id, promotion_id)
SELECT @p24, @promo_new
WHERE NOT EXISTS (SELECT 1 FROM product_promotions WHERE product_id = @p24 AND promotion_id = @promo_new);

INSERT INTO banner (product_id, image, date_banner, status)
SELECT @p1, '/y2k_banner.png', CURDATE(), 1
WHERE NOT EXISTS (SELECT 1 FROM banner WHERE product_id = @p1 AND image = '/y2k_banner.png');

INSERT INTO banner (product_id, image, date_banner, status)
SELECT @p4, '/summer_y2k.png', CURDATE() - INTERVAL 1 DAY, 1
WHERE NOT EXISTS (SELECT 1 FROM banner WHERE product_id = @p4 AND image = '/summer_y2k.png');

INSERT INTO banner (product_id, image, date_banner, status)
SELECT @p9, '/summer_y2k.png', CURDATE() - INTERVAL 2 DAY, 1
WHERE NOT EXISTS (SELECT 1 FROM banner WHERE product_id = @p9 AND image = '/summer_y2k.png');

INSERT INTO banner (product_id, image, date_banner, status)
SELECT @p16, '/y2k_banner.png', CURDATE() - INTERVAL 3 DAY, 1
WHERE NOT EXISTS (SELECT 1 FROM banner WHERE product_id = @p16 AND image = '/y2k_banner.png');

UPDATE banner SET title = 'Summer Y2K Drop', subtitle = 'Item nổi bật cho outfit mùa hè.', button_text = 'Mua ngay', sort_order = 1
WHERE product_id = @p1 AND image = '/y2k_banner.png';
UPDATE banner SET title = 'Color Pop Streetwear', subtitle = 'Năng lượng màu sắc, phối nhanh mỗi ngày.', button_text = 'Khám phá', sort_order = 2
WHERE product_id = @p4 AND image = '/summer_y2k.png';
UPDATE banner SET title = 'Sneaker Platform', subtitle = 'Tăng điểm nhấn cho mọi outfit.', button_text = 'Xem giày', sort_order = 3
WHERE product_id = @p9 AND image = '/summer_y2k.png';
UPDATE banner SET title = 'Set đồ Y2K', subtitle = 'Phối sẵn, lên đồ nhanh, vẫn đúng chất riêng.', button_text = 'Xem set đồ', sort_order = 4
WHERE product_id = @p16 AND image = '/y2k_banner.png';

INSERT INTO banner (product_id, image, title, subtitle, button_text, sort_order, date_banner, status)
SELECT @p17, '/summer_y2k.png', 'Cherry Pop', 'Baby tee nổi bật cho outfit hàng ngày.', 'Xem áo', 5, CURDATE() - INTERVAL 4 DAY, 1
WHERE NOT EXISTS (SELECT 1 FROM banner WHERE product_id = @p17 AND image = '/summer_y2k.png');

INSERT INTO banner (product_id, image, title, subtitle, button_text, sort_order, date_banner, status)
SELECT @p22, '/y2k_banner.png', 'Platform Night', 'Boot đế cao cho những bước đi thật khác biệt.', 'Xem giày', 6, CURDATE() - INTERVAL 5 DAY, 1
WHERE NOT EXISTS (SELECT 1 FROM banner WHERE product_id = @p22 AND image = '/y2k_banner.png');

UPDATE user
SET auth_provider = 'LOCAL', email_verified = FALSE, avatar_url = NULL
WHERE email IN ('admin@y2kstore.vn', 'user@gmail.com', 'minhanh@y2kstore.vn', 'giahuy@y2kstore.vn');

-- ==========================================
-- 9. COUPONS
-- ==========================================
INSERT INTO coupons (promotion_id, coupon_code, discount_value, expire_date, status)
SELECT @promo_summer, 'WELCOME10', 10.00, NOW() + INTERVAL 120 DAY, 1
WHERE NOT EXISTS (SELECT 1 FROM coupons WHERE coupon_code = 'WELCOME10');

INSERT INTO coupons (promotion_id, coupon_code, discount_value, expire_date, status)
SELECT @promo_flash, 'FLASH20', 20.00, NOW() + INTERVAL 30 DAY, 1
WHERE NOT EXISTS (SELECT 1 FROM coupons WHERE coupon_code = 'FLASH20');

INSERT INTO coupons (promotion_id, coupon_code, discount_value, expire_date, status)
SELECT @promo_student, 'STUDENT15', 15.00, NOW() + INTERVAL 60 DAY, 1
WHERE NOT EXISTS (SELECT 1 FROM coupons WHERE coupon_code = 'STUDENT15');

INSERT INTO coupons (promotion_id, coupon_code, discount_value, expire_date, status)
SELECT @promo_new, 'VIP25', 25.00, NOW() + INTERVAL 90 DAY, 1
WHERE NOT EXISTS (SELECT 1 FROM coupons WHERE coupon_code = 'VIP25');

SET @coupon_welcome := (SELECT id FROM coupons WHERE coupon_code = 'WELCOME10' LIMIT 1);
SET @coupon_flash := (SELECT id FROM coupons WHERE coupon_code = 'FLASH20' LIMIT 1);

-- ==========================================
-- 10. REVIEWS, CART, ORDERS
-- ==========================================
INSERT INTO reviews (user_id, product_id, rating, comment, created_at)
SELECT @user_id_1, @p1, 5, 'Áo mặc đẹp, form ổn, màu rất Y2K.', NOW() - INTERVAL 2 DAY
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id = @user_id_1 AND product_id = @p1 AND comment = 'Áo mặc đẹp, form ổn, màu rất Y2K.');

INSERT INTO reviews (user_id, product_id, rating, comment, created_at)
SELECT @user_id_2, @p9, 5, 'Giày lên chân êm, phối đồ rất nổi.', NOW() - INTERVAL 1 DAY
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id = @user_id_2 AND product_id = @p9 AND comment = 'Giày lên chân êm, phối đồ rất nổi.');

INSERT INTO reviews (user_id, product_id, rating, comment, created_at)
SELECT @user_id_3, @p11, 4, 'Túi nhỏ gọn, hợp đi chơi.', NOW() - INTERVAL 3 DAY
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id = @user_id_3 AND product_id = @p11 AND comment = 'Túi nhỏ gọn, hợp đi chơi.');

INSERT INTO reviews (user_id, product_id, rating, comment, created_at)
SELECT @user_id_1, @p16, 5, 'Set đồ mặc lên là có outfit sẵn.', NOW() - INTERVAL 5 DAY
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id = @user_id_1 AND product_id = @p16 AND comment = 'Set đồ mặc lên là có outfit sẵn.');

INSERT INTO cart_items (user_id, variant_id, quantity)
SELECT @user_id_1, @v1, 1
WHERE NOT EXISTS (SELECT 1 FROM cart_items WHERE user_id = @user_id_1 AND variant_id = @v1);

INSERT INTO cart_items (user_id, variant_id, quantity)
SELECT @user_id_1, @v3, 1
WHERE NOT EXISTS (SELECT 1 FROM cart_items WHERE user_id = @user_id_1 AND variant_id = @v3);

INSERT INTO cart_items (user_id, variant_id, quantity)
SELECT @user_id_2, @v2, 2
WHERE NOT EXISTS (SELECT 1 FROM cart_items WHERE user_id = @user_id_2 AND variant_id = @v2);

INSERT INTO cart_items (user_id, variant_id, quantity)
SELECT @user_id_3, @v4, 1
WHERE NOT EXISTS (SELECT 1 FROM cart_items WHERE user_id = @user_id_3 AND variant_id = @v4);

INSERT INTO orders (user_id, coupon_id, order_date, total_amount, status)
SELECT @user_id_1, @coupon_welcome, '2026-07-10 10:00:00', 608000.00, 'PROCESSING'
WHERE NOT EXISTS (SELECT 1 FROM orders WHERE user_id = @user_id_1 AND total_amount = 608000.00 AND order_date = '2026-07-10 10:00:00');

INSERT INTO orders (user_id, coupon_id, order_date, total_amount, status)
SELECT @user_id_2, @coupon_flash, '2026-07-11 09:30:00', 828000.00, 'PROCESSING'
WHERE NOT EXISTS (SELECT 1 FROM orders WHERE user_id = @user_id_2 AND total_amount = 828000.00 AND order_date = '2026-07-11 09:30:00');

INSERT INTO orders (user_id, coupon_id, order_date, total_amount, status)
SELECT @user_id_1, NULL, '2026-07-12 14:20:00', 199000.00, 'SHIPPED'
WHERE NOT EXISTS (SELECT 1 FROM orders WHERE user_id = @user_id_1 AND total_amount = 199000.00 AND order_date = '2026-07-12 14:20:00');

SET @order1 := (SELECT id FROM orders WHERE user_id = @user_id_1 AND total_amount = 608000.00 LIMIT 1);
SET @order2 := (SELECT id FROM orders WHERE user_id = @user_id_2 AND total_amount = 828000.00 LIMIT 1);
SET @order3 := (SELECT id FROM orders WHERE user_id = @user_id_1 AND total_amount = 199000.00 LIMIT 1);

INSERT INTO order_details (order_id, variant_id, quantity, unit_price)
SELECT @order1, @v1, 1, 249000.00
WHERE NOT EXISTS (SELECT 1 FROM order_details WHERE order_id = @order1 AND variant_id = @v1);

INSERT INTO order_details (order_id, variant_id, quantity, unit_price)
SELECT @order1, @v2, 1, 359000.00
WHERE NOT EXISTS (SELECT 1 FROM order_details WHERE order_id = @order1 AND variant_id = @v2);

INSERT INTO order_details (order_id, variant_id, quantity, unit_price)
SELECT @order2, @v3, 1, 699000.00
WHERE NOT EXISTS (SELECT 1 FROM order_details WHERE order_id = @order2 AND variant_id = @v3);

INSERT INTO order_details (order_id, variant_id, quantity, unit_price)
SELECT @order2, @v11, 1, 129000.00
WHERE NOT EXISTS (SELECT 1 FROM order_details WHERE order_id = @order2 AND variant_id = @v11);

INSERT INTO order_details (order_id, variant_id, quantity, unit_price)
SELECT @order3, @v4, 1, 199000.00
WHERE NOT EXISTS (SELECT 1 FROM order_details WHERE order_id = @order3 AND variant_id = @v4);

INSERT INTO payments (order_id, method, amount, payment_date, status)
SELECT @order1, 'COD', 608000.00, '2026-07-10 10:05:00', 'UNPAID'
WHERE NOT EXISTS (SELECT 1 FROM payments WHERE order_id = @order1);

INSERT INTO payments (order_id, method, amount, payment_date, status)
SELECT @order2, 'BANKING', 828000.00, '2026-07-11 09:35:00', 'PAID'
WHERE NOT EXISTS (SELECT 1 FROM payments WHERE order_id = @order2);

INSERT INTO payments (order_id, method, amount, payment_date, status)
SELECT @order3, 'COD', 199000.00, '2026-07-12 14:25:00', 'UNPAID'
WHERE NOT EXISTS (SELECT 1 FROM payments WHERE order_id = @order3);

COMMIT;



