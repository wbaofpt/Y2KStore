package com.y2kstore.backend.controller;

import com.y2kstore.backend.dto.OrderDTO;
import com.y2kstore.backend.dto.OrderItemDTO;
import com.y2kstore.backend.dto.OrderRequest;
import com.y2kstore.backend.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getMyOrders() {
        return ResponseEntity.ok(orderService.getMyOrders());
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<?> getOrderItems(@PathVariable Integer id) {
        try {
            List<OrderItemDTO> items = orderService.getOrderItems(id);
            return ResponseEntity.ok(items);
        } catch (RuntimeException e) {
            if ("Forbidden".equals(e.getMessage())) {
                return ResponseEntity.status(403).build();
            }
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHistoryOrder(@PathVariable Integer id) {
        try {
            orderService.deleteHistoryOrder(id);
            return ResponseEntity.ok(Map.of("message", "Đã ẩn đơn khỏi lịch sử tài khoản."));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("không có quyền")) {
                return ResponseEntity.status(403).body(Map.of("message", e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody OrderRequest orderRequest) {
        try {
            Map<String, Object> result = orderService.placeOrder(orderRequest);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/payment-session")
    public ResponseEntity<?> continuePayment(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(orderService.continuePayment(id));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("không có quyền")) {
                return ResponseEntity.status(403).body(Map.of("message", e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
