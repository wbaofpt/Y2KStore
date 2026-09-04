package com.y2kstore.backend.repository;

import com.y2kstore.backend.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Integer> {
    List<Banner> findAllByOrderByDateBannerDesc();
    List<Banner> findByStatusTrueOrderByDateBannerDesc();
}
