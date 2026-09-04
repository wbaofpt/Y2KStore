package com.y2kstore.backend.dto;

import com.y2kstore.backend.entity.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentDTO {
    private Integer id;
    private Integer orderId;
    private Integer userId;
    private String customerName;
    private String method;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private String status;

    public PaymentDTO() {}

    public PaymentDTO(Payment payment) {
        this.id = payment.getId();
        this.method = payment.getMethod();
        this.amount = payment.getAmount();
        this.paymentDate = payment.getPaymentDate();
        this.status = payment.getStatus();
        if (payment.getOrder() != null) {
            this.orderId = payment.getOrder().getId();
            if (payment.getOrder().getUser() != null) {
                this.userId = payment.getOrder().getUser().getId();
                this.customerName = payment.getOrder().getUser().getFullName();
            }
        }
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
