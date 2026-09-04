package com.y2kstore.backend.dto;

import com.y2kstore.backend.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderDTO {
    private Integer id;
    private Integer userId;
    private String fullName;
    private String userFullName;
    private String phone;
    private String userPhone;
    private Integer couponId;
    private String couponCode;
    private BigDecimal couponDiscountValue;
    private String promotionName;
    private BigDecimal promotionDiscountPercent;
    private Integer addressId;
    private String addressLabel;
    private String paymentMethod;
    private String paymentStatus;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private String status;

    public OrderDTO() {}

    public OrderDTO(Order order) {
        this.id = order.getId();
        this.orderDate = order.getOrderDate();
        this.totalAmount = order.getTotalAmount();
        this.status = order.getStatus();

        if (order.getUser() != null) {
            this.userId = order.getUser().getId();
            this.fullName = order.getUser().getFullName();
            this.userFullName = this.fullName;
            this.phone = order.getUser().getPhone();
            this.userPhone = this.phone;
        }

        if (order.getCoupon() != null) {
            this.couponId = order.getCoupon().getId();
            this.couponCode = order.getCoupon().getCouponCode();
            this.couponDiscountValue = order.getCoupon().getDiscountValue();
            if (order.getCoupon().getPromotion() != null) {
                this.promotionName = order.getCoupon().getPromotion().getPromotionName();
                this.promotionDiscountPercent = order.getCoupon().getPromotion().getDiscountPercent();
            }
        }

        if (order.getShippingAddress() != null) {
            this.addressId = order.getShippingAddress().getId();
            this.addressLabel = String.join(", ",
                    java.util.stream.Stream.of(
                            order.getShippingAddress().getDetail(),
                            order.getShippingAddress().getWard(),
                            order.getShippingAddress().getDistrict(),
                            order.getShippingAddress().getProvince()
                    ).filter(value -> value != null && !value.isBlank()).toList());
        }

        if (order.getPayment() != null) {
            this.paymentMethod = order.getPayment().getMethod();
            this.paymentStatus = order.getPayment().getStatus();
        }
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getUserFullName() { return userFullName; }
    public void setUserFullName(String userFullName) { this.userFullName = userFullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }
    public Integer getCouponId() { return couponId; }
    public void setCouponId(Integer couponId) { this.couponId = couponId; }
    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
    public BigDecimal getCouponDiscountValue() { return couponDiscountValue; }
    public void setCouponDiscountValue(BigDecimal couponDiscountValue) { this.couponDiscountValue = couponDiscountValue; }
    public String getPromotionName() { return promotionName; }
    public void setPromotionName(String promotionName) { this.promotionName = promotionName; }
    public BigDecimal getPromotionDiscountPercent() { return promotionDiscountPercent; }
    public void setPromotionDiscountPercent(BigDecimal promotionDiscountPercent) { this.promotionDiscountPercent = promotionDiscountPercent; }
    public Integer getAddressId() { return addressId; }
    public void setAddressId(Integer addressId) { this.addressId = addressId; }
    public String getAddressLabel() { return addressLabel; }
    public void setAddressLabel(String addressLabel) { this.addressLabel = addressLabel; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
