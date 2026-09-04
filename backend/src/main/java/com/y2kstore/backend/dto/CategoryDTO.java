package com.y2kstore.backend.dto;

import com.y2kstore.backend.entity.Category;

public class CategoryDTO {
    private Integer id;
    private String name;
    private String description;
    private String categoryType;
    private Integer parentCategoryId;
    private String parentCategoryName;
    private Boolean status;
    private Long productCount;

    public CategoryDTO() {}

    public CategoryDTO(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.description = category.getDescription();
        this.categoryType = category.getCategoryType() == null
                ? "GENERAL"
                : category.getCategoryType().trim().toUpperCase();
        if (category.getParentCategory() != null) {
            this.parentCategoryId = category.getParentCategory().getId();
            this.parentCategoryName = category.getParentCategory().getName();
        }
        this.status = category.getStatus();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategoryType() { return categoryType; }
    public void setCategoryType(String categoryType) { this.categoryType = categoryType; }
    public Integer getParentCategoryId() { return parentCategoryId; }
    public void setParentCategoryId(Integer parentCategoryId) { this.parentCategoryId = parentCategoryId; }
    public String getParentCategoryName() { return parentCategoryName; }
    public void setParentCategoryName(String parentCategoryName) { this.parentCategoryName = parentCategoryName; }
    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }
    public Long getProductCount() { return productCount; }
    public void setProductCount(Long productCount) { this.productCount = productCount; }
}
