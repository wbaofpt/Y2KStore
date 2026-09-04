# Thay đổi cơ sở dữ liệu - 15/07/2026

Tài liệu này mô tả các thay đổi database mới nhất để phù hợp với backend, frontend và dữ liệu seed hiện tại.

## 1. Danh mục chung và danh mục sản phẩm

- Thêm `categories.category_type` để phân biệt:
  - `GENERAL`: danh mục chung như `Áo`, `Quần`, `Giày`, `Túi xách`.
  - `TAG`: danh mục sản phẩm con như `Jean`, `Suông`, `Dài`, `Croptop`.
- Thêm `categories.parent_category_id` để thể hiện quan hệ cha-con giữa danh mục chung và danh mục sản phẩm.
- `products.category_id` tiếp tục lưu danh mục chung của sản phẩm.
- `product_categories` dùng để lưu nhiều tag con của sản phẩm.
- Ví dụ:
  - Sản phẩm `Quần Jean Nam Suông` có `category_id` là `Quần`.
  - Các tag con có thể là `Jean`, `Nam`, `Suông`, `Dài`.
- Backend/admin đã được cập nhật để:
  - chọn danh mục chung riêng,
  - chọn tag con theo đúng danh mục cha,
  - chặn gán tag không thuộc danh mục cha đã chọn.

## 2. Tài khoản và xác thực

- Thêm `user.avatar_url` kiểu `MEDIUMTEXT` để lưu avatar người dùng.
- Thêm `user.email_verified` để đánh dấu email đã xác nhận.
- Giữ `user.auth_provider` để tương thích dữ liệu cũ; tài khoản mới sử dụng đăng nhập nội bộ `LOCAL`.
- Thêm `user.reset_token` và `user.reset_token_expires_at` cho chức năng quên mật khẩu.
- Thêm bảng `email_verifications` để lưu mã OTP tạm thời cho đăng ký và xác nhận email.
- Xóa hai cột tạm `user.email_verification_code` và `user.email_verification_expires_at` vì không còn dùng.
- Bảng này có `email`, `purpose`, `code`, `expires_at`, `created_at` và khóa duy nhất theo `email + purpose`.
- Mã đăng ký chỉ được gửi sau khi nhập email; tài khoản chỉ được tạo khi mã đúng và còn hạn.
- Tài khoản đăng ký bằng web mặc định không có avatar; email được đánh dấu đã xác nhận sau khi nhập đúng mã OTP.
- Người dùng có thể thay avatar từ trang tài khoản.
- Admin có thể chỉnh avatar, email_verified và trạng thái người dùng.

## 3. Địa chỉ giao hàng

- Mỗi người dùng được lưu tối đa 4 địa chỉ.
- Một địa chỉ có thể đặt làm mặc định.
- Khi tạo hoặc cập nhật địa chỉ, bắt buộc có tỉnh/thành phố, quận/huyện, phường/xã và địa chỉ cụ thể.
- Trang tài khoản chỉ hiển thị các địa chỉ đã lưu, không hiển thị ô trống.

## 4. Banner trang chủ

- Thêm `banner.title` để lưu tiêu đề banner.
- Thêm `banner.subtitle` để lưu mô tả ngắn.
- Thêm `banner.button_text` để lưu nội dung nút hành động.
- Thêm `banner.sort_order` để quản lý thứ tự hiển thị.
- Trang quản trị có thể chỉnh nội dung banner, ảnh, sản phẩm liên kết, ngày hiển thị, trạng thái và thứ tự.
- Banner trên trang chủ tự động luân phiên.

## 5. Đánh giá sản phẩm

- Dữ liệu đánh giá trả về avatar và tên người đánh giá.
- Người dùng chỉ được đánh giá sản phẩm sau khi đã mua sản phẩm đó.
- Mỗi người dùng chỉ được đánh giá một lần cho mỗi sản phẩm.

## 5.1. Ngày tạo sản phẩm và Hàng mới

- Thêm `products.created_at` kiểu `DATETIME DEFAULT CURRENT_TIMESTAMP`.
- Backend trả `createdAt` trong `ProductDTO`.
- API `/api/products/featured?type=new` chỉ lấy sản phẩm được tạo trong 14 ngày gần nhất.
- Trang chủ và Shop dùng `createdAt` để hiển thị khu **Hàng mới** và nhãn `NEW`.
- Admin Products có cột/ngày tạo và có thể chỉnh ngày tạo khi thêm hoặc sửa sản phẩm.

## 5.2. Bật/tắt tương thích database khi khởi động

- `backend/src/main/resources/application.properties` có cấu hình `y2kstore.database-compatibility.enabled=true`.
- Đặt thành `false` để `DatabaseCompatibilityInitializer` không tự kiểm tra/thêm column, tạo bảng tương thích, cập nhật dữ liệu mẫu hoặc seed ngày tạo sản phẩm khi backend khởi động.
- Giữ `true` cho môi trường local cần tự đồng bộ database cũ với entity hiện tại.

## 6. Cấu hình email và quên mật khẩu

- Hệ thống không còn hỗ trợ đăng nhập bằng Google.
- Backend dùng Gmail SMTP qua `spring-boot-starter-mail` để gửi mã OTP.
- Mã quên mật khẩu và mã xác nhận email đều gồm 6 chữ số, có hiệu lực 15 phút và chỉ dùng một lần.
- Mã xác nhận đăng ký được lưu ở `email_verifications`, không lưu mã tạm trong bảng `user`.
- Cấu hình bằng biến môi trường `MAIL_USERNAME` và `MAIL_APP_PASSWORD`; không lưu App Password trong Git.
- API không trả mã OTP trực tiếp về frontend.
- Backend có lớp tương thích tự bù các cột còn thiếu và tự tạo `ROLE_USER`/`ROLE_ADMIN` nếu database cũ chưa có.

## 7. Cách cập nhật database

### Nếu đang dùng database cũ

Khởi động lại backend để `DatabaseCompatibilityInitializer` tự bổ sung các cột còn thiếu, bao gồm hai cột xác nhận email.

### Nếu tạo database mới

Chạy theo thứ tự:

1. `database/database_full.sql`
2. `database/seed_data.sql`

Hai file này đã được đồng bộ theo schema mới và dữ liệu mẫu mới.
