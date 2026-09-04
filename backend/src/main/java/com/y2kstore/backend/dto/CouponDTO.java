package com.y2kstore.backend.dto;

import com.y2kstore.backend.entity.Coupon;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CouponDTO {
    private Integer id;
    private Integer promotionId;
    private String promotionName;
    private BigDecimal promotionDiscountPercent;
    private String couponCode;
    private BigDecimal discountValue;
    private LocalDateTime expireDate;
    private Boolean status;

    public CouponDTO() {}

    public CouponDTO(Coupon coupon) {
        this.id = coupon.getId();
        this.couponCode = coupon.getCouponCode();
        this.discountValue = coupon.getDiscountValue();
        this.expireDate = coupon.getExpireDate();
        this.status = coupon.getStatus();
        if (coupon.getPromotion() != null) {
            this.promotionId = coupon.getPromotion().getId();
            this.promotionName = coupon.getPromotion().getPromotionName();
            this.promotionDiscountPercent = coupon.getPromotion().getDiscountPercent();
        }
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getPromotionId() { return promotionId; }
    public void setPromotionId(Integer promotionId) { this.promotionId = promotionId; }
    public String getPromotionName() { return promotionName; }
    public void setPromotionName(String promotionName) { this.promotionName = promotionName; }
    public BigDecimal getPromotionDiscountPercent() { return promotionDiscountPercent; }
    public void setPromotionDiscountPercent(BigDecimal promotionDiscountPercent) { this.promotionDiscountPercent = promotionDiscountPercent; }
    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
    public LocalDateTime getExpireDate() { return expireDate; }
    public void setExpireDate(LocalDateTime expireDate) { this.expireDate = expireDate; }
    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }
}
