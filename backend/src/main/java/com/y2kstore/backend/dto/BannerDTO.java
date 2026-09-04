package com.y2kstore.backend.dto;

import com.y2kstore.backend.entity.Banner;

public class BannerDTO {
    private Integer id;
    private Integer productId;
    private String productName;
    private String image;
    private String title;
    private String subtitle;
    private String buttonText;
    private Integer sortOrder;
    private String dateBanner;
    private Boolean status;

    public BannerDTO() {}

    public BannerDTO(Banner banner) {
        this.id = banner.getId();
        if (banner.getProduct() != null) {
            this.productId = banner.getProduct().getId();
            this.productName = banner.getProduct().getName();
        }
        this.image = banner.getImage();
        this.title = banner.getTitle();
        this.subtitle = banner.getSubtitle();
        this.buttonText = banner.getButtonText();
        this.sortOrder = banner.getSortOrder();
        this.dateBanner = banner.getDateBanner() != null ? banner.getDateBanner().toString() : null;
        this.status = banner.getStatus();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public String getButtonText() { return buttonText; }
    public void setButtonText(String buttonText) { this.buttonText = buttonText; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getDateBanner() { return dateBanner; }
    public void setDateBanner(String dateBanner) { this.dateBanner = dateBanner; }
    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }
}
