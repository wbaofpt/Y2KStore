package com.y2kstore.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "image_urls", columnDefinition = "TEXT")
    private String imageUrls;

    @Column(name = "video_urls", columnDefinition = "TEXT")
    private String videoUrls;

    // --- Các trường mới bổ sung cho AI & Moderation ---
    @Column(name = "status", length = 50)
    private String status = "VISIBLE"; // Mặc định là VISIBLE, nếu vi phạm sẽ thành FLAGGED

    @Column(name = "ai_checked")
    private Boolean aiChecked = false;

    @Column(name = "ai_score")
    private Float aiScore;

    @Column(name = "ai_reason", columnDefinition = "TEXT")
    private String aiReason;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Review() {}

    // --- Getters và Setters cũ ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getImageUrls() { return imageUrls; }
    public void setImageUrls(String imageUrls) { this.imageUrls = imageUrls; }
    public String getVideoUrls() { return videoUrls; }
    public void setVideoUrls(String videoUrls) { this.videoUrls = videoUrls; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // --- Getters và Setters mới cho AI ---
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getAiChecked() { return aiChecked; }
    public void setAiChecked(Boolean aiChecked) { this.aiChecked = aiChecked; }
    public Float getAiScore() { return aiScore; }
    public void setAiScore(Float aiScore) { this.aiScore = aiScore; }
    public String getAiReason() { return aiReason; }
    public void setAiReason(String aiReason) { this.aiReason = aiReason; }
}