package com.y2kstore.backend.service;

import com.y2kstore.backend.dto.BannerDTO;
import com.y2kstore.backend.repository.BannerRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BannerService {

    private final BannerRepository bannerRepository;

    public BannerService(BannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    public List<BannerDTO> getActiveBanners() {
        return bannerRepository.findByStatusTrueOrderByDateBannerDesc()
                .stream()
                .map(BannerDTO::new)
                .sorted(Comparator.comparing(BannerDTO::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList());
    }
}