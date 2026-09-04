package com.y2kstore.backend.controller;

import com.y2kstore.backend.dto.CategoryDTO;
import com.y2kstore.backend.service.CategoryService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/areas")
    public ResponseEntity<JsonNode> getVietnameseAreas() {
        JsonNode payload = categoryService.getVietnameseAreas();
        return ResponseEntity.ok(payload);
    }
}