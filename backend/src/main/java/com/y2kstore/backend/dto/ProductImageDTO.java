package com.y2kstore.backend.dto;

import com.y2kstore.backend.entity.ProductImage;

public class ProductImageDTO {
    private Integer id;
    private String imageUrl;

    public ProductImageDTO() {}

    public ProductImageDTO(ProductImage image) {
        this.id = image.getId();
        this.imageUrl = image.getImageUrl();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
