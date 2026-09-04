CREATE TABLE IF NOT EXISTS role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    category_type VARCHAR(20) NOT NULL DEFAULT 'GENERAL',
    parent_category_id BIGINT,
    status TINYINT(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS email_verifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    purpose VARCHAR(30) NOT NULL,
    code VARCHAR(6) NOT NULL,
    expires_at DATETIME NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_email_verification (email, purpose)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS promotions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    promotion_name VARCHAR(100) NOT NULL,
    discount_percent DECIMAL(5,2),
    start_date DATETIME,
    end_date DATETIME,
    status TINYINT(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `user` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    status INT DEFAULT 1,
    avatar_url MEDIUMTEXT,
    email_verified BOOLEAN DEFAULT FALSE,
    auth_provider VARCHAR(30) NOT NULL DEFAULT 'LOCAL',
    reset_token VARCHAR(120),
    reset_token_expires_at DATETIME,
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES role(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE IF NOT EXISTS address (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    province VARCHAR(100),
    district VARCHAR(100),
    ward VARCHAR(100),
    detail VARCHAR(255),
    is_default BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_address_user FOREIGN KEY (user_id) REFERENCES `user`(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(12,2) NOT NULL,
    image TEXT,
    status INT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product_categories (
    product_id BIGINT,
    category_id BIGINT,
    PRIMARY KEY (product_id, category_id),
    CONSTRAINT fk_product_categories_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_product_categories_category FOREIGN KEY (category_id) REFERENCES categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    CONSTRAINT fk_product_images_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product_variants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    size VARCHAR(20),
    color VARCHAR(50),
    stock INT NOT NULL DEFAULT 0,
    price DECIMAL(12,2),
    status TINYINT(1) DEFAULT 1,
    UNIQUE KEY uq_product_variant (product_id, size, color),
    CONSTRAINT fk_product_variants_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS banner (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT,
    image TEXT,
    title VARCHAR(180),
    subtitle TEXT,
    button_text VARCHAR(80),
    sort_order INT DEFAULT 0,
    date_banner DATE,
    status TINYINT(1) DEFAULT 1,
    CONSTRAINT fk_banner_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product_promotions (
    product_id BIGINT,
    promotion_id BIGINT,
    PRIMARY KEY (product_id, promotion_id),
    CONSTRAINT fk_product_promotions_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_product_promotions_promotion FOREIGN KEY (promotion_id) REFERENCES promotions(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    image_urls TEXT,
    video_urls TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_review_user_product (user_id, product_id),
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES `user`(id),
    CONSTRAINT fk_reviews_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    variant_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    UNIQUE KEY uq_cart_user_variant (user_id, variant_id),
    CONSTRAINT fk_cart_items_user FOREIGN KEY (user_id) REFERENCES `user`(id),
    CONSTRAINT fk_cart_items_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS coupons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    promotion_id BIGINT,
    coupon_code VARCHAR(50) UNIQUE NOT NULL,
    discount_value DECIMAL(12,2),
    expire_date DATETIME,
    status TINYINT(1) DEFAULT 1,
    CONSTRAINT fk_coupons_promotion FOREIGN KEY (promotion_id) REFERENCES promotions(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    coupon_id BIGINT,
    address_id BIGINT,
    order_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(12,2) NOT NULL,
    status VARCHAR(50) DEFAULT 'PROCESSING',
    hidden_from_history BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES `user`(id),
    CONSTRAINT fk_orders_coupon FOREIGN KEY (coupon_id) REFERENCES coupons(id),
    CONSTRAINT fk_orders_address FOREIGN KEY (address_id) REFERENCES address(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS order_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    variant_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_order_details_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_order_details_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    method VARCHAR(50) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    payment_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) DEFAULT 'UNPAID',
    sepay_transaction_id VARCHAR(100) UNIQUE,
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
