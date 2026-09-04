package com.y2kstore.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(nullable = false, length = 50)
    private String method;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_date", insertable = false, updatable = false)
    private LocalDateTime paymentDate;

    @Column(nullable = false, length = 50)
    private String status = "UNPAID";

    @Column(name = "sepay_transaction_id", length = 100, unique = true)
    private String sepayTransactionId;

    public Payment() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSepayTransactionId() { return sepayTransactionId; }
    public void setSepayTransactionId(String sepayTransactionId) { this.sepayTransactionId = sepayTransactionId; }
}
