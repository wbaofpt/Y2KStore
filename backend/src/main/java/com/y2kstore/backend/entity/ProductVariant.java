package com.y2kstore.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "product_variants")
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(length = 20)
    private String size;

    @Column(length = 50)
    private String color;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Boolean status = true;

    public ProductVariant() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
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
}
