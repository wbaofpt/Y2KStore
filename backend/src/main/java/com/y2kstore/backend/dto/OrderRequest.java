package com.y2kstore.backend.dto;

import java.util.List;

public class OrderRequest {
    private String paymentMethod;
    private String note;
    private Integer addressId;
    private String couponCode;
    private List<OrderItemRequest> items;

    public OrderRequest() {}

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Integer getAddressId() { return addressId; }
    public void setAddressId(Integer addressId) { this.addressId = addressId; }
    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }
}
