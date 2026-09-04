package com.y2kstore.backend.controller;

import com.y2kstore.backend.dto.*;
import com.y2kstore.backend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        try {
            AuthResponse response = authService.login(authRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Login failed"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest regRequest) {
        try {
            authService.register(regRequest);
            return ResponseEntity.ok(Map.of("message", "Registration successful."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/register/request-code")
    public ResponseEntity<?> requestRegistrationCode(@RequestBody Map<String, String> request) {
        try {
            String email = request.getOrDefault("email", "");
            authService.requestRegistrationCode(email);
            return ResponseEntity.ok(Map.of("message", "Mã xác nhận 6 số đã được gửi đến email của bạn."));
        } catch (Exception e) {
            int status = e.getMessage().contains("SMTP") ? 503 : 400;
            return ResponseEntity.status(status).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            authService.forgotPassword(request.getEmail());
            return ResponseEntity.ok(Map.of("message", "Nếu email tồn tại, mã xác nhận sẽ được gửi đến email đó."));
        } catch (Exception e) {
            return ResponseEntity.status(503).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request);
            return ResponseEntity.ok(Map.of("message", "Đặt lại mật khẩu thành công."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/request-email-verification")
    public ResponseEntity<?> requestEmailVerification() {
        try {
            authService.requestEmailVerification();
            return ResponseEntity.ok(Map.of("message", "Mã xác nhận 6 số đã được gửi đến email của bạn."));
        } catch (Exception e) {
            int status = e.getMessage().contains("SMTP") ? 503 : 400;
            return ResponseEntity.status(status).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody Map<String, String> request) {
        try {
            Map<String, Object> payload = authService.verifyEmail(request.get("code"));
            return ResponseEntity.ok(payload);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        try {
            Map<String, Object> payload = authService.getProfile();
            return ResponseEntity.ok(payload);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UserDTO userDTO) {
        try {
            Map<String, Object> payload = authService.updateProfile(userDTO);
            return ResponseEntity.ok(payload);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/addresses")
    public ResponseEntity<?> getMyAddresses() {
        try {
            List<AddressDTO> addresses = authService.getMyAddresses();
            return ResponseEntity.ok(addresses);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
    }

    @PostMapping("/addresses")
    public ResponseEntity<?> createMyAddress(@RequestBody AddressDTO dto) {
        try {
            AddressDTO created = authService.createMyAddress(dto);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/addresses/{id}")
    public ResponseEntity<?> updateMyAddress(@PathVariable Integer id, @RequestBody AddressDTO dto) {
        try {
            AddressDTO updated = authService.updateMyAddress(id, dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            String msg = e.getMessage();
            int status = "Forbidden".equals(msg) ? 403 : 400;
            return ResponseEntity.status(status).body(Map.of("message", msg));
        }
    }

    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<?> deleteMyAddress(@PathVariable Integer id) {
        try {
            authService.deleteMyAddress(id);
            return ResponseEntity.ok(Map.of("message", "Address deleted."));
        } catch (Exception e) {
            String msg = e.getMessage();
            int status = "Forbidden".equals(msg) ? 403 : 400;
            return ResponseEntity.status(status).body(Map.of("message", msg));
        }
    }
}
