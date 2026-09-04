package com.y2kstore.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "banner")
public class Banner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(columnDefinition = "TEXT")
    private String image;

    @Column(length = 180)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String subtitle;

    @Column(name = "button_text", length = 80)
    private String buttonText;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "date_banner")
    private LocalDate dateBanner;

    @Column(nullable = false)
    private Boolean status = true;

    public Banner() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
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
    public LocalDate getDateBanner() { return dateBanner; }
    public void setDateBanner(LocalDate dateBanner) { this.dateBanner = dateBanner; }
    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }
}
