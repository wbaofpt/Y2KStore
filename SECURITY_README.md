# Security Y2K Store - Nội Dung Thuyết Trình

## 1. Mục tiêu
> Security giúp hệ thống xác định đúng người dùng, giới hạn quyền truy cập và bảo vệ các thao tác nhạy cảm như tài khoản và đơn hàng.

Ba lớp chính của hệ thống là:

- Xác thực: người dùng có đúng là chủ tài khoản không?
- Phân quyền: người dùng được phép làm gì?
- Bảo vệ nghiệp vụ: thao tác có đúng quy tắc của cửa hàng không?

## 2. Luồng đăng nhập

```text
Nhập email + mật khẩu
  -> POST /api/auth/login
  -> AuthenticationManager xác thực
  -> BCrypt kiểm tra mật khẩu
  -> AuthService tạo JWT
  -> Frontend lưu JWT
  -> Gửi JWT ở các request tiếp theo
```

Request riêng tư gửi token:

```http
Authorization: Bearer <JWT>
```

JWT chứa email, role, thời gian tạo và thời gian hết hạn.

## 3. Phân quyền bằng gì?

Role được lưu trong database:

- `ROLE_USER`: người dùng thông thường.
- `ROLE_ADMIN`: quản trị viên.

Quan hệ dữ liệu:

```text
user.role_id -> role.id -> role.role_name
```

Trong Spring Security:

```java
.requestMatchers("/api/admin/**").hasRole("ADMIN")
```

`hasRole("ADMIN")` thực tế kiểm tra authority `ROLE_ADMIN`.

Backend không tin role trong localStorage, tên nút hoặc URL frontend.

## 4. Luồng kiểm tra request

```text
Bearer JWT
  -> JwtAuthenticationFilter đọc token
  -> JwtTokenProvider kiểm tra chữ ký và thời hạn
  -> Tải user mới nhất từ database
  -> Lấy status và role
  -> Đưa Authentication vào SecurityContext
  -> SecurityConfig kiểm tra quyền endpoint
  -> Controller xử lý nghiệp vụ
```

Service còn kiểm tra ownership. Ví dụ User A không thể xem, sửa hoặc hủy đơn hàng của User B.

## 5. Các hàm Security chính

### `SecurityConfig.java`

| Hàm | Tác dụng |
|---|---|
| `passwordEncoder()` | Tạo BCrypt để hash và kiểm tra mật khẩu. |
| `authenticationManager(...)` | Cung cấp cơ chế xác thực đăng nhập. |
| `securityFilterChain(...)` | Khai báo API public, API cần đăng nhập và API admin. |
| `corsConfigurationSource()` | Giới hạn origin, method và header được phép. |

Luật quan trọng:

```java
.requestMatchers("/api/admin/**").hasRole("ADMIN")
.requestMatchers("/api/cart/**").authenticated()
.requestMatchers("/api/orders/**").authenticated()
.anyRequest().authenticated()
```

### `JwtAuthenticationFilter.java`

| Hàm | Tác dụng |
|---|---|
| `doFilterInternal(...)` | Chạy trước controller để xác thực JWT ở mỗi request. |
| `getJwtFromRequest(...)` | Lấy token từ header Bearer. |
| `setAuthentication(...)` | Lưu user và authority vào SecurityContext. |

### `JwtTokenProvider.java`

| Hàm | Tác dụng |
|---|---|
| `generateToken(...)` | Tạo JWT sau khi đăng nhập thành công. |
| `getUsernameFromToken(...)` | Lấy email từ claim `sub`. |
| `getAllClaimsFromToken(...)` | Đọc claims và kiểm tra chữ ký. |
| `isTokenExpired(...)` | Kiểm tra token hết hạn. |
| `validateToken(...)` | Kiểm tra token đúng user và còn hạn. |
| `getSigningKey()` | Tạo khóa HMAC từ JWT secret. |

### `CustomUserDetailsService.java`

| Hàm | Tác dụng |
|---|---|
| `loadUserByUsername(...)` | Tìm user trong database và lấy role. |
| `new SimpleGrantedAuthority(...)` | Chuyển role thành authority cho Spring Security. |

Nếu không có role, hệ thống dùng mặc định `ROLE_USER`.

### `AuthService.java`

| Hàm | Tác dụng |
|---|---|
| `login(...)` | Xác thực email/mật khẩu và phát hành JWT. |
| `register(...)` | Kiểm tra OTP, hash mật khẩu và tạo user. |
| `requestRegistrationCode(...)` | Sinh và gửi OTP đăng ký. |
| `forgotPassword(...)` | Tạo mã đặt lại mật khẩu có thời hạn. |
| `resetPassword(...)` | Kiểm tra mã và lưu mật khẩu mới bằng BCrypt. |
| `currentUser()` | Lấy user hiện tại từ SecurityContext. |
| `ownedAddress(...)` | Kiểm tra địa chỉ có thuộc user hiện tại không. |

### `AuthController.java`

| Endpoint | Tác dụng |
|---|---|
| `POST /api/auth/login` | Đăng nhập và nhận JWT. |
| `POST /api/auth/register` | Đăng ký sau khi OTP hợp lệ. |
| `POST /api/auth/forgot-password` | Yêu cầu mã quên mật khẩu. |
| `POST /api/auth/reset-password` | Đặt lại mật khẩu. |
| `GET /api/auth/profile` | Lấy thông tin user đang đăng nhập. |

### Frontend `auth.js`

| Hàm/getter | Tác dụng |
|---|---|
| `login(...)` | Gọi API đăng nhập và lưu phiên. |
| `persistSession()` | Lưu JWT và gắn Bearer token cho Axios. |
| `initAuth()` | Khôi phục phiên khi mở lại trang. |
| `fetchProfile()` | Tải lại user từ backend; lỗi 401 thì logout. |
| `logout()` | Xóa JWT, user và header Authorization. |
| `isAdmin` | Kiểm tra role để điều hướng giao diện admin. |

## 6. Mã lỗi bảo mật

- `401 Unauthorized`: thiếu token, token sai/hết hạn hoặc tài khoản bị khóa.
- `403 Forbidden`: đã đăng nhập nhưng không đủ quyền, ví dụ user gọi API admin.

Khi admin khóa user, filter đọc lại `status = false` từ database nên JWT cũ cũng không còn quyền hoạt động.

## 7. Mật khẩu và OTP

- Mật khẩu được hash bằng BCrypt, không lưu dạng rõ.
- OTP được tạo bằng SecureRandom.
- OTP có thời hạn và chỉ dùng một lần.
- OTP sai hoặc hết hạn sẽ bị từ chối.
- OTP được xóa sau khi sử dụng hoặc khi gửi email thất bại.

## 8. Web không có mạng

- Nếu frontend, backend và database cùng máy hoặc cùng mạng LAN: vẫn có thể chạy bằng IP nội bộ.
- Nếu mất hoàn toàn mạng: không thể đăng nhập, gọi API, lưu đơn, gửi OTP hoặc nhận webhook SePay.
- Lý do: hệ thống hoạt động theo mô hình `Frontend -> Backend -> Database`.
- Muốn offline hoàn toàn cần PWA, IndexedDB và cơ chế đồng bộ khi có mạng.

## 9. Câu hỏi thực tế

### Vì sao không chỉ phân quyền ở frontend?

Vì frontend có thể bị sửa bằng DevTools. Người dùng vẫn có thể gọi API trực tiếp, nên backend phải kiểm tra quyền.

### Sửa role trong localStorage có được làm admin không?

Không. Backend lấy role từ JWT đã ký và database.

### JWT đúng nhưng user bị khóa thì sao?

Request tiếp theo vẫn bị từ chối vì backend kiểm tra lại trạng thái user.

### Vì sao dùng `hasRole("ADMIN")` nhưng role là `ROLE_ADMIN`?

Spring Security tự thêm tiền tố `ROLE_` khi dùng `hasRole`.

### Có thể gọi API bằng Postman không?

Có. Đăng nhập lấy JWT rồi thêm header:

```http
Authorization: Bearer <JWT>
```

### JWT có an toàn tuyệt đối không?

Không. Cần HTTPS, bảo vệ secret, thời hạn hợp lý và cân nhắc cookie HttpOnly khi triển khai production.

## 10. Kết luận

> Y2K Store dùng BCrypt để bảo vệ mật khẩu, JWT để xác thực request và Role để phân quyền. Backend luôn kiểm tra lại user, trạng thái tài khoản, role và quyền sở hữu dữ liệu. Vì vậy frontend chỉ là lớp giao diện, còn backend là lớp bảo mật cuối cùng.
