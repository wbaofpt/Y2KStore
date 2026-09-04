package com.y2kstore.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/config")
@CrossOrigin
public class AppConfigController {
    @Value("${y2kstore.page-loading-delay-ms:450}")
    private long pageLoadingDelayMs;

    @GetMapping
    public ResponseEntity<Map<String, Long>> getConfig() {
        return ResponseEntity.ok(Map.of("pageLoadingDelayMs", Math.max(0, pageLoadingDelayMs)));
    }
}
