package com.y2kstore.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "cart_items", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "variant_id"})
})
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @Column(nullable = false)
    private Integer quantity = 1;

    public CartItem() {}

    public CartItem(User user, ProductVariant variant, Integer quantity) {
        this.user = user;
        this.variant = variant;
        this.quantity = quantity;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public ProductVariant getVariant() { return variant; }
    public void setVariant(ProductVariant variant) { this.variant = variant; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
