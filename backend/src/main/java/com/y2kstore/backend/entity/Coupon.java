package com.y2kstore.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    @Column(name = "coupon_code", nullable = false, unique = true, length = 50)
    private String couponCode;

    @Column(name = "discount_value", precision = 12, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "expire_date")
    private LocalDateTime expireDate;

    @Column(nullable = false)
    private Boolean status = true;

    public Coupon() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Promotion getPromotion() { return promotion; }
    public void setPromotion(Promotion promotion) { this.promotion = promotion; }
    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
    public LocalDateTime getExpireDate() { return expireDate; }
    public void setExpireDate(LocalDateTime expireDate) { this.expireDate = expireDate; }
    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }
}
