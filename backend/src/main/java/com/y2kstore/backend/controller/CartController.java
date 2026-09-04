package com.y2kstore.backend.controller;

import com.y2kstore.backend.dto.CartAddRequest;
import com.y2kstore.backend.dto.CartItemDTO;
import com.y2kstore.backend.dto.CartUpdateRequest;
import com.y2kstore.backend.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<List<CartItemDTO>> getCart() {
        List<CartItemDTO> items = cartService.getCart();
        return ResponseEntity.ok(items);
    }

    @PostMapping
    public ResponseEntity<?> addToCart(@RequestBody CartAddRequest request) {
        try {
            CartItemDTO itemDTO = cartService.addToCart(request);
            return ResponseEntity.ok(Map.of("message", "Đã thêm vào giỏ hàng!", "item", itemDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{variantId}")
    public ResponseEntity<?> updateQuantity(@PathVariable Integer variantId, @RequestBody CartUpdateRequest request) {
        try {
            CartItemDTO updatedItem = cartService.updateQuantity(variantId, request);
            return ResponseEntity.ok(updatedItem);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{variantId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Integer variantId) {
        try {
            cartService.removeFromCart(variantId);
            return ResponseEntity.ok(Map.of("message", "Đã xóa khỏi giỏ hàng!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping
    public ResponseEntity<?> clearCart() {
        try {
            cartService.clearCart();
            return ResponseEntity.ok(Map.of("message", "Đã xóa toàn bộ giỏ hàng!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}