package com.y2kstore.backend.service;

import com.y2kstore.backend.dto.CouponDTO;
import com.y2kstore.backend.entity.Coupon;
import com.y2kstore.backend.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Transactional(readOnly = true)
    public CouponDTO validateCoupon(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new RuntimeException("Vui lòng nhập mã giảm giá.");
        }

        Coupon coupon = couponRepository.findByCouponCode(code.trim().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không tồn tại."));

        if (Boolean.FALSE.equals(coupon.getStatus())) {
            throw new RuntimeException("Mã giảm giá đang tắt.");
        }

        if (coupon.getExpireDate() != null && coupon.getExpireDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Mã giảm giá đã hết hạn.");
        }

        return new CouponDTO(coupon);
    }
}