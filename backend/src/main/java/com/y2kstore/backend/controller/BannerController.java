package com.y2kstore.backend.controller;

import com.y2kstore.backend.dto.BannerDTO;
import com.y2kstore.backend.service.BannerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/banners")
@CrossOrigin
public class BannerController {

    private final BannerService bannerService;

    public BannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @GetMapping
    public ResponseEntity<List<BannerDTO>> getActiveBanners() {
        List<BannerDTO> banners = bannerService.getActiveBanners();
        return ResponseEntity.ok(banners);
    }
}