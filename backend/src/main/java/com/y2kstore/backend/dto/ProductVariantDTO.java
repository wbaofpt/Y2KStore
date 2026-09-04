package com.y2kstore.backend.dto;

import com.y2kstore.backend.entity.ProductVariant;

import java.math.BigDecimal;

public class ProductVariantDTO {
    private Integer id;
    private String size;
    private String color;
    private Integer stock;
    private BigDecimal price;
    private Boolean status;
    private String label;

    public ProductVariantDTO() {}

    public ProductVariantDTO(ProductVariant variant) {
        this.id = variant.getId();
        this.size = variant.getSize();
        this.color = variant.getColor();
        this.stock = variant.getStock();
        this.price = variant.getPrice();
        this.status = variant.getStatus();
        StringBuilder sb = new StringBuilder();
        if (size != null && !size.isBlank()) sb.append(size);
        if (color != null && !color.isBlank()) {
            if (sb.length() > 0) sb.append(" / ");
            sb.append(color);
        }
        this.label = sb.length() > 0 ? sb.toString() : "Variant #" + variant.getId();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
}
