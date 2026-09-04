package com.y2kstore.backend.dto;

import com.y2kstore.backend.entity.CartItem;
import com.y2kstore.backend.entity.ProductVariant;

import java.math.BigDecimal;

public class CartItemDTO {
    private Integer id;
    private Integer variantId;
    private Integer productId;
    private String productName;
    private String productImage;
    private String variantLabel;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private Integer quantity;
    private Integer stock;

    public CartItemDTO() {}

    public CartItemDTO(CartItem cartItem) {
        this.id = cartItem.getId();
        this.quantity = cartItem.getQuantity();
        ProductVariant variant = cartItem.getVariant();
        if (variant != null) {
            this.variantId = variant.getId();
            this.stock = variant.getStock();
            this.variantLabel = buildVariantLabel(variant);
            this.unitPrice = variant.getPrice() != null ? variant.getPrice() : (variant.getProduct() != null ? variant.getProduct().getPrice() : null);
            if (variant.getProduct() != null) {
                this.productId = variant.getProduct().getId();
                this.productName = variant.getProduct().getName();
                this.productImage = variant.getProduct().getImage();
            }
            this.totalPrice = this.unitPrice != null ? this.unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity())) : null;
        }
    }

    private String buildVariantLabel(ProductVariant variant) {
        StringBuilder sb = new StringBuilder();
        if (variant.getSize() != null && !variant.getSize().isBlank()) {
            sb.append(variant.getSize());
        }
        if (variant.getColor() != null && !variant.getColor().isBlank()) {
            if (sb.length() > 0) sb.append(" / ");
            sb.append(variant.getColor());
        }
        return sb.length() > 0 ? sb.toString() : "Variant #" + variant.getId();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getVariantId() { return variantId; }
    public void setVariantId(Integer variantId) { this.variantId = variantId; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }
    public String getVariantLabel() { return variantLabel; }
    public void setVariantLabel(String variantLabel) { this.variantLabel = variantLabel; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
}
