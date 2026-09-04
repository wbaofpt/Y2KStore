package com.y2kstore.backend.dto;

public class OrderItemRequest {
    private Integer variantId;
    private Integer quantity;

    public OrderItemRequest() {}

    public Integer getVariantId() { return variantId; }
    public void setVariantId(Integer variantId) { this.variantId = variantId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
