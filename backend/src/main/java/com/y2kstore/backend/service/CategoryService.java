package com.y2kstore.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.y2kstore.backend.dto.CategoryDTO;
import com.y2kstore.backend.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findByStatusTrue().stream()
                .map(CategoryDTO::new)
                .collect(Collectors.toList());
    }

    public JsonNode getVietnameseAreas() {
        return RestClient.create("https://provinces.open-api.vn")
                .get()
                .uri("/api/v1/?depth=3")
                .retrieve()
                .body(JsonNode.class);
    }
}