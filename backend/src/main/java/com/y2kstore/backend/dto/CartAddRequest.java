package com.y2kstore.backend.dto;

public class CartAddRequest {
    private Integer variantId;
    private Integer quantity = 1;

    public Integer getVariantId() { return variantId; }
    public void setVariantId(Integer variantId) { this.variantId = variantId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
