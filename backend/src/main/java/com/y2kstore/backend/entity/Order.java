package com.y2kstore.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address shippingAddress;

    @OneToOne(mappedBy = "order", fetch = FetchType.LAZY)
    private Payment payment;

    @Column(name = "order_date", insertable = false, updatable = false)
    private LocalDateTime orderDate;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 20)
    private String status = "PROCESSING"; // PROCESSING, SHIPPED, DELIVERED, CANCELLED

    @Column(name = "hidden_from_history", nullable = false)
    private Boolean hiddenFromHistory = false;

    public Order() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Coupon getCoupon() { return coupon; }
    public void setCoupon(Coupon coupon) { this.coupon = coupon; }
    public Address getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(Address shippingAddress) { this.shippingAddress = shippingAddress; }
    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getHiddenFromHistory() { return hiddenFromHistory; }
    public void setHiddenFromHistory(Boolean hiddenFromHistory) { this.hiddenFromHistory = hiddenFromHistory; }

    @Transient
    public String getPaymentMethod() {
        return payment != null ? payment.getMethod() : null;
    }

    @Transient
    public String getPaymentStatus() {
        return payment != null ? payment.getStatus() : null;
    }

    @Transient
    public String getFullName() {
        return user != null ? user.getFullName() : null;
    }

    @Transient
    public String getPhone() {
        return user != null ? user.getPhone() : null;
    }
}
