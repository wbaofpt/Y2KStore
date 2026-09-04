package com.y2kstore.backend.controller;

import com.y2kstore.backend.dto.CouponDTO;
import com.y2kstore.backend.service.CouponService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/coupons")
@CrossOrigin
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateCoupon(@RequestParam String code) {
        try {
            CouponDTO couponDTO = couponService.validateCoupon(code);
            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "coupon", couponDTO
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}