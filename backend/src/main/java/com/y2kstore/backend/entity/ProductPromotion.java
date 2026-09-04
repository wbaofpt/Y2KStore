package com.y2kstore.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "product_promotions")
public class ProductPromotion {
    @EmbeddedId
    private ProductPromotionId id = new ProductPromotionId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("promotionId")
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    public ProductPromotion() {}

    public ProductPromotionId getId() { return id; }
    public void setId(ProductPromotionId id) { this.id = id; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Promotion getPromotion() { return promotion; }
    public void setPromotion(Promotion promotion) { this.promotion = promotion; }

    @Embeddable
    public static class ProductPromotionId implements java.io.Serializable {
        @Column(name = "product_id")
        private Integer productId;

        @Column(name = "promotion_id")
        private Integer promotionId;

        public ProductPromotionId() {}

        public Integer getProductId() { return productId; }
        public void setProductId(Integer productId) { this.productId = productId; }
        public Integer getPromotionId() { return promotionId; }
        public void setPromotionId(Integer promotionId) { this.promotionId = promotionId; }
    }
}
