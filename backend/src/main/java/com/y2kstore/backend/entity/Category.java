package com.y2kstore.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "category_type", nullable = false, length = 20)
    private String categoryType = "GENERAL";

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "parentCategory"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private Category parentCategory;

    @Column(nullable = false)
    private Boolean status = true;

    public Category() {}

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategoryType() { return categoryType; }
    public void setCategoryType(String categoryType) { this.categoryType = categoryType; }
    public Category getParentCategory() { return parentCategory; }
    public void setParentCategory(Category parentCategory) { this.parentCategory = parentCategory; }
    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }
}
