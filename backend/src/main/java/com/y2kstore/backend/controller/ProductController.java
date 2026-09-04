package com.y2kstore.backend.controller;

import com.y2kstore.backend.dto.ProductDTO;
import com.y2kstore.backend.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getProducts(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        return ResponseEntity.ok(productService.getProducts(categoryId, search, minPrice, maxPrice));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Integer id) {
        ProductDTO product = productService.getProductById(id);
        return product != null ? ResponseEntity.ok(product) : ResponseEntity.notFound().build();
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ProductDTO> getProductBySlug(@PathVariable String slug) {
        ProductDTO product = productService.getProductBySlug(slug);
        return product != null ? ResponseEntity.ok(product) : ResponseEntity.notFound().build();
    }

    @GetMapping("/featured")
    public ResponseEntity<List<ProductDTO>> getFeaturedProducts(
            @RequestParam(defaultValue = "combined") String type,
            @RequestParam(defaultValue = "10") Integer limit) {
        return ResponseEntity.ok(productService.getFeaturedProducts(type, limit));
    }

    @GetMapping("/sale")
    public ResponseEntity<List<ProductDTO>> getSaleProducts() {
        return ResponseEntity.ok(productService.getSaleProducts());
    }
}