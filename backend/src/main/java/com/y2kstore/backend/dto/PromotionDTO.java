package com.y2kstore.backend.dto;

import com.y2kstore.backend.entity.Promotion;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PromotionDTO {
    private Integer id;
    private String promotionName;
    private BigDecimal discountPercent;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean status;

    public PromotionDTO() {}

    public PromotionDTO(Promotion promotion) {
        this.id = promotion.getId();
        this.promotionName = promotion.getPromotionName();
        this.discountPercent = promotion.getDiscountPercent();
        this.startDate = promotion.getStartDate();
        this.endDate = promotion.getEndDate();
        this.status = promotion.getStatus();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getPromotionName() { return promotionName; }
    public void setPromotionName(String promotionName) { this.promotionName = promotionName; }
    public BigDecimal getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(BigDecimal discountPercent) { this.discountPercent = discountPercent; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }
}
