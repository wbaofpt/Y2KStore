package com.y2kstore.backend.dto;

import com.y2kstore.backend.entity.Product;
import com.y2kstore.backend.entity.ProductImage;
import com.y2kstore.backend.entity.ProductVariant;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ProductDTO {
    private Integer id;
    private String name;
    private String description;
    private BigDecimal price;
    private String image;
    private Integer status;
    private Integer categoryId;
    private String categoryName;
    private List<Integer> categoryIds = new ArrayList<>();
    private List<String> categoryNames = new ArrayList<>();
    private Integer stock;
    private BigDecimal minVariantPrice;
    private BigDecimal maxVariantPrice;
    private String promotionName;
    private BigDecimal promotionDiscountPercent;
    private List<Integer> promotionIds = new ArrayList<>();
    private List<String> promotionNames = new ArrayList<>();
    private BigDecimal ratingAverage;
    private Long reviewCount;
    private Long soldCount;
    private LocalDateTime createdAt;
    private List<ProductImageDTO> images = new ArrayList<>();
    private List<ProductVariantDTO> variants = new ArrayList<>();

    public ProductDTO() {}

    public ProductDTO(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.image = product.getImage();
        this.status = product.getStatus();
        this.createdAt = product.getCreatedAt();
        Set<Integer> ids = new LinkedHashSet<>();
        Set<String> names = new LinkedHashSet<>();
        if (product.getCategory() != null) {
            this.categoryId = product.getCategory().getId();
            this.categoryName = product.getCategory().getName();
            if (this.categoryId != null) ids.add(this.categoryId);
            if (this.categoryName != null) names.add(this.categoryName);
        }
        if (product.getCategories() != null) {
            product.getCategories().forEach(category -> {
                if (category.getId() != null) ids.add(category.getId());
                if (category.getName() != null) names.add(category.getName());
            });
        }
        this.categoryIds = new ArrayList<>(ids);
        this.categoryNames = new ArrayList<>(names);
    }

    public ProductDTO(Product product, List<ProductImage> productImages, List<ProductVariant> productVariants) {
        this(product);
        if (productImages != null) {
            for (ProductImage image : productImages) {
                this.images.add(new ProductImageDTO(image));
            }
        }
        if (productVariants != null) {
            BigDecimal min = null;
            BigDecimal max = null;
            int totalStock = 0;
            for (ProductVariant variant : productVariants) {
                ProductVariantDTO dto = new ProductVariantDTO(variant);
                this.variants.add(dto);
                if (variant.getStock() != null) totalStock += variant.getStock();
                BigDecimal price = variant.getPrice() != null ? variant.getPrice() : product.getPrice();
                if (price != null) {
                    if (min == null || price.compareTo(min) < 0) min = price;
                    if (max == null || price.compareTo(max) > 0) max = price;
                }
            }
            this.stock = totalStock;
            this.minVariantPrice = min;
            this.maxVariantPrice = max;
        }
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public List<Integer> getCategoryIds() { return categoryIds; }
    public void setCategoryIds(List<Integer> categoryIds) { this.categoryIds = categoryIds; }
    public List<String> getCategoryNames() { return categoryNames; }
    public void setCategoryNames(List<String> categoryNames) { this.categoryNames = categoryNames; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public BigDecimal getMinVariantPrice() { return minVariantPrice; }
    public void setMinVariantPrice(BigDecimal minVariantPrice) { this.minVariantPrice = minVariantPrice; }
    public BigDecimal getMaxVariantPrice() { return maxVariantPrice; }
    public void setMaxVariantPrice(BigDecimal maxVariantPrice) { this.maxVariantPrice = maxVariantPrice; }
    public String getPromotionName() { return promotionName; }
    public void setPromotionName(String promotionName) { this.promotionName = promotionName; }
    public BigDecimal getPromotionDiscountPercent() { return promotionDiscountPercent; }
    public void setPromotionDiscountPercent(BigDecimal promotionDiscountPercent) { this.promotionDiscountPercent = promotionDiscountPercent; }
    public List<Integer> getPromotionIds() { return promotionIds; }
    public void setPromotionIds(List<Integer> promotionIds) { this.promotionIds = promotionIds; }
    public List<String> getPromotionNames() { return promotionNames; }
    public void setPromotionNames(List<String> promotionNames) { this.promotionNames = promotionNames; }
    public BigDecimal getRatingAverage() { return ratingAverage; }
    public void setRatingAverage(BigDecimal ratingAverage) { this.ratingAverage = ratingAverage; }
    public Long getReviewCount() { return reviewCount; }
    public void setReviewCount(Long reviewCount) { this.reviewCount = reviewCount; }
    public Long getSoldCount() { return soldCount; }
    public void setSoldCount(Long soldCount) { this.soldCount = soldCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<ProductImageDTO> getImages() { return images; }
    public void setImages(List<ProductImageDTO> images) { this.images = images; }
    public List<ProductVariantDTO> getVariants() { return variants; }
    public void setVariants(List<ProductVariantDTO> variants) { this.variants = variants; }
}
