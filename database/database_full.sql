-- ========================================================
-- SCRIPT TAO DATABASE: Y2K_STORE_2026
-- Dong bo voi backend entities va DatabaseCompatibilityInitializer
-- ========================================================

DROP DATABASE IF EXISTS y2k_store_2026;
CREATE DATABASE y2k_store_2026
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE y2k_store_2026;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ==========================================
-- 1. PHAN QUYEN, NGUOI DUNG, DIA CHI
-- ==========================================

CREATE TABLE role (
  id INT AUTO_INCREMENT PRIMARY KEY,
  role_name VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `user` (
  id INT AUTO_INCREMENT PRIMARY KEY,
  role_id INT,
  password VARCHAR(255) NOT NULL,
  full_name VARCHAR(100) NOT NULL,
  email VARCHAR(100) NOT NULL UNIQUE,
  phone VARCHAR(15),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  avatar_url MEDIUMTEXT,
  email_verified BOOLEAN NOT NULL DEFAULT FALSE,
  auth_provider VARCHAR(30) NOT NULL DEFAULT 'LOCAL',
  reset_token VARCHAR(120),
  reset_token_expires_at DATETIME,
  status BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES role(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE address (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  province VARCHAR(100),
  district VARCHAR(100),
  ward VARCHAR(100),
  detail VARCHAR(255),
  is_default BOOLEAN DEFAULT FALSE,
  CONSTRAINT fk_address_user FOREIGN KEY (user_id) REFERENCES `user`(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE email_verifications (
  id INT AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(100) NOT NULL,
  purpose VARCHAR(30) NOT NULL,
  code VARCHAR(6) NOT NULL,
  expires_at DATETIME NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_email_verification (email, purpose)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- 2. DANH MUC, SAN PHAM, BANNER, KHUYEN MAI
-- ==========================================

CREATE TABLE categories (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  description TEXT,
  category_type VARCHAR(20) NOT NULL DEFAULT 'GENERAL',
  parent_category_id INT,
  status BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT fk_category_parent FOREIGN KEY (parent_category_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE products (
  id INT AUTO_INCREMENT PRIMARY KEY,
  category_id INT,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  price DECIMAL(12,2) NOT NULL,
  image TEXT,
  status INT DEFAULT 1,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE product_categories (
  product_id INT NOT NULL,
  category_id INT NOT NULL,
  PRIMARY KEY (product_id, category_id),
  CONSTRAINT fk_product_categories_product FOREIGN KEY (product_id) REFERENCES products(id),
  CONSTRAINT fk_product_categories_category FOREIGN KEY (category_id) REFERENCES categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE product_images (
  id INT AUTO_INCREMENT PRIMARY KEY,
  product_id INT NOT NULL,
  image_url VARCHAR(255) NOT NULL,
  CONSTRAINT fk_product_image_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE product_variants (
  id INT AUTO_INCREMENT PRIMARY KEY,
  product_id INT NOT NULL,
  size VARCHAR(20),
  color VARCHAR(50),
  stock INT NOT NULL DEFAULT 0,
  price DECIMAL(12,2),
  status BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT fk_product_variant_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE promotions (
  id INT AUTO_INCREMENT PRIMARY KEY,
  promotion_name VARCHAR(100) NOT NULL,
  discount_percent DECIMAL(5,2),
  start_date DATETIME,
  end_date DATETIME,
  status BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE product_promotions (
  product_id INT NOT NULL,
  promotion_id INT NOT NULL,
  PRIMARY KEY (product_id, promotion_id),
  CONSTRAINT fk_product_promotion_product FOREIGN KEY (product_id) REFERENCES products(id),
  CONSTRAINT fk_product_promotion_promotion FOREIGN KEY (promotion_id) REFERENCES promotions(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE banner (
  id INT AUTO_INCREMENT PRIMARY KEY,
  product_id INT,
  image TEXT,
  title VARCHAR(180),
  subtitle TEXT,
  button_text VARCHAR(80),
  sort_order INT NOT NULL DEFAULT 0,
  date_banner DATE,
  status BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT fk_banner_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- 3. MA GIAM GIA, DON HANG, THANH TOAN
-- ==========================================

CREATE TABLE coupons (
  id INT AUTO_INCREMENT PRIMARY KEY,
  promotion_id INT,
  coupon_code VARCHAR(50) NOT NULL UNIQUE,
  discount_value DECIMAL(12,2),
  expire_date DATETIME,
  status BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT fk_coupon_promotion FOREIGN KEY (promotion_id) REFERENCES promotions(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE orders (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT,
  coupon_id INT,
  address_id INT,
  order_date DATETIME DEFAULT CURRENT_TIMESTAMP,
  total_amount DECIMAL(12,2) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PROCESSING',
  hidden_from_history BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES `user`(id),
  CONSTRAINT fk_order_coupon FOREIGN KEY (coupon_id) REFERENCES coupons(id),
  CONSTRAINT fk_order_address FOREIGN KEY (address_id) REFERENCES address(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE payments (
  id INT AUTO_INCREMENT PRIMARY KEY,
  order_id INT NOT NULL UNIQUE,
  method VARCHAR(50) NOT NULL,
  amount DECIMAL(12,2) NOT NULL,
  payment_date DATETIME DEFAULT CURRENT_TIMESTAMP,
  status VARCHAR(50) NOT NULL DEFAULT 'UNPAID',
  sepay_transaction_id VARCHAR(100) UNIQUE,
  CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE order_details (
  id INT AUTO_INCREMENT PRIMARY KEY,
  order_id INT NOT NULL,
  variant_id INT NOT NULL,
  quantity INT NOT NULL,
  unit_price DECIMAL(12,2) NOT NULL,
  CONSTRAINT fk_order_detail_order FOREIGN KEY (order_id) REFERENCES orders(id),
  CONSTRAINT fk_order_detail_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE cart_items (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  variant_id INT NOT NULL,
  quantity INT NOT NULL DEFAULT 1,
  UNIQUE KEY uq_cart_user_variant (user_id, variant_id),
  CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES `user`(id),
  CONSTRAINT fk_cart_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- 4. DANH GIA SAN PHAM
-- ==========================================

CREATE TABLE reviews (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  product_id INT NOT NULL,
  rating INT NOT NULL,
  comment TEXT,
  image_urls TEXT,
  video_urls TEXT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT chk_review_rating CHECK (rating >= 1 AND rating <= 5),
  CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES `user`(id),
  CONSTRAINT fk_review_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
