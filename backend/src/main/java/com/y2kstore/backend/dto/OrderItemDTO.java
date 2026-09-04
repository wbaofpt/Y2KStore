package com.y2kstore.backend.dto;

import com.y2kstore.backend.entity.OrderItem;
import com.y2kstore.backend.entity.ProductVariant;

import java.math.BigDecimal;

public class OrderItemDTO {
    private Integer id;
    private Integer productId;
    private String productName;
    private String productImage;
    private Integer variantId;
    private String variantLabel;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    public OrderItemDTO() {}

    public OrderItemDTO(OrderItem item) {
        this.id = item.getId();
        this.quantity = item.getQuantity();
        this.unitPrice = item.getPrice();
        this.totalPrice = item.getPrice() != null ? item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())) : null;
        ProductVariant variant = item.getVariant();
        if (variant != null) {
            this.variantId = variant.getId();
            this.variantLabel = buildVariantLabel(variant);
            if (variant.getProduct() != null) {
                this.productId = variant.getProduct().getId();
                this.productName = variant.getProduct().getName();
                this.productImage = variant.getProduct().getImage();
            }
        }
    }

    private String buildVariantLabel(ProductVariant variant) {
        StringBuilder sb = new StringBuilder();
        if (variant.getSize() != null && !variant.getSize().isBlank()) sb.append(variant.getSize());
        if (variant.getColor() != null && !variant.getColor().isBlank()) {
            if (sb.length() > 0) sb.append(" / ");
            sb.append(variant.getColor());
        }
        return sb.length() > 0 ? sb.toString() : "Variant #" + variant.getId();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }
    public Integer getVariantId() { return variantId; }
    public void setVariantId(Integer variantId) { this.variantId = variantId; }
    public String getVariantLabel() { return variantLabel; }
    public void setVariantLabel(String variantLabel) { this.variantLabel = variantLabel; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
}
