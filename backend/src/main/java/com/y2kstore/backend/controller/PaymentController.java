package com.y2kstore.backend.controller;

import com.y2kstore.backend.service.PaymentGatewayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin
public class PaymentController {

    private final PaymentGatewayService paymentGatewayService;

    public PaymentController(PaymentGatewayService paymentGatewayService) {
        this.paymentGatewayService = paymentGatewayService;
    }

    @PostMapping("/sepay/webhook")
    public ResponseEntity<?> handleSepayWebhook(
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            paymentGatewayService.handleSepayWebhook(payload, authorization);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (SecurityException ex) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", ex.getMessage()));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    @GetMapping("/orders/{orderId}/status")
    public ResponseEntity<?> getOrderPaymentStatus(@PathVariable Integer orderId) {
        try {
            return ResponseEntity.ok(Map.of("status", paymentGatewayService.getOrderPaymentStatus(orderId)));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }
}
