package com.y2kstore.backend.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.y2kstore.backend.entity.Review;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReviewDTO {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {};

    private Integer id;
    private Integer userId;
    private String userFullName;
    private String userAvatarUrl;
    private Integer productId;
    private String productName;
    private Integer rating;
    private String comment;
    private List<String> imageUrls = new ArrayList<>();
    private List<String> videoUrls = new ArrayList<>();
    private LocalDateTime createdAt;

    // --- CÁC TRƯỜNG MỚI BỔ SUNG CHO TÍNH NĂNG KIỂM DUYỆT AI ---
    private String status;       // Trạng thái hiển thị: VISIBLE (Công khai), FLAGGED (Nghi vấn), HIDDEN (Đã ẩn)
    private Boolean aiChecked;   // Đánh dấu xem AI đã quét qua nội dung này chưa
    private Float aiScore;       // Điểm rủi ro/vi phạm do AI chấm
    private String aiReason;     // Lý do chi tiết vi phạm do AI trả về

    public ReviewDTO() {}

    public ReviewDTO(Review review) {
        this.id = review.getId();
        this.rating = review.getRating();
        this.comment = review.getComment();
        this.imageUrls = parseList(review.getImageUrls());
        this.videoUrls = parseList(review.getVideoUrls());
        this.createdAt = review.getCreatedAt();

        // --- Map thông tin AI từ Entity sang DTO ---
        this.status = review.getStatus();
        this.aiChecked = review.getAiChecked();
        this.aiScore = review.getAiScore();
        this.aiReason = review.getAiReason();

        if (review.getUser() != null) {
            this.userId = review.getUser().getId();
            this.userFullName = review.getUser().getFullName();
            this.userAvatarUrl = review.getUser().getAvatarUrl();
        }
        if (review.getProduct() != null) {
            this.productId = review.getProduct().getId();
            this.productName = review.getProduct().getName();
        }
    }

    // --- Getters và Setters cho các thuộc tính cũ ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getUserFullName() { return userFullName; }
    public void setUserFullName(String userFullName) { this.userFullName = userFullName; }
    public String getUserAvatarUrl() { return userAvatarUrl; }
    public void setUserAvatarUrl(String userAvatarUrl) { this.userAvatarUrl = userAvatarUrl; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    public List<String> getVideoUrls() { return videoUrls; }
    public void setVideoUrls(List<String> videoUrls) { this.videoUrls = videoUrls; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // --- Getters và Setters cho các thuộc tính AI mới ---
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getAiChecked() { return aiChecked; }
    public void setAiChecked(Boolean aiChecked) { this.aiChecked = aiChecked; }
    public Float getAiScore() { return aiScore; }
    public void setAiScore(Float aiScore) { this.aiScore = aiScore; }
    public String getAiReason() { return aiReason; }
    public void setAiReason(String aiReason) { this.aiReason = aiReason; }

    public static String toJson(List<String> values) {
        try {
            List<String> safeValues = values == null ? List.of() : values.stream()
                                                                   .filter(value -> value != null && !value.isBlank())
                                                                   .limit(8)
                                                                   .toList();
            return OBJECT_MAPPER.writeValueAsString(safeValues);
        } catch (Exception ex) {
            return "[]";
        }
    }

    private static List<String> parseList(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return OBJECT_MAPPER.readValue(json, STRING_LIST);
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }
}