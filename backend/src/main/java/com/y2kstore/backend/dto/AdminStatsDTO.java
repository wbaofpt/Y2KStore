package com.y2kstore.backend.dto;

import java.math.BigDecimal;

public class AdminStatsDTO {
    private BigDecimal totalRevenue;
    private long totalOrders;
    private long totalProducts;
    private long totalUsers;
    private long pendingOrders;
    private long lowStockProducts;
    private long totalCategories;
    private long totalBanners;
    private long totalPromotions;
    private long totalCoupons;
    private long totalReviews;
    private long totalAddresses;
    private long totalPayments;

    public AdminStatsDTO() {}

    public AdminStatsDTO(BigDecimal totalRevenue, long totalOrders, long totalProducts, long totalUsers,
                         long pendingOrders, long lowStockProducts, long totalCategories, long totalBanners,
                         long totalPromotions, long totalCoupons, long totalReviews, long totalAddresses, long totalPayments) {
        this.totalRevenue = totalRevenue;
        this.totalOrders = totalOrders;
        this.totalProducts = totalProducts;
        this.totalUsers = totalUsers;
        this.pendingOrders = pendingOrders;
        this.lowStockProducts = lowStockProducts;
        this.totalCategories = totalCategories;
        this.totalBanners = totalBanners;
        this.totalPromotions = totalPromotions;
        this.totalCoupons = totalCoupons;
        this.totalReviews = totalReviews;
        this.totalAddresses = totalAddresses;
        this.totalPayments = totalPayments;
    }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }
    public long getTotalProducts() { return totalProducts; }
    public void setTotalProducts(long totalProducts) { this.totalProducts = totalProducts; }
    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
    public long getPendingOrders() { return pendingOrders; }
    public void setPendingOrders(long pendingOrders) { this.pendingOrders = pendingOrders; }
    public long getLowStockProducts() { return lowStockProducts; }
    public void setLowStockProducts(long lowStockProducts) { this.lowStockProducts = lowStockProducts; }
    public long getTotalCategories() { return totalCategories; }
    public void setTotalCategories(long totalCategories) { this.totalCategories = totalCategories; }
    public long getTotalBanners() { return totalBanners; }
    public void setTotalBanners(long totalBanners) { this.totalBanners = totalBanners; }
    public long getTotalPromotions() { return totalPromotions; }
    public void setTotalPromotions(long totalPromotions) { this.totalPromotions = totalPromotions; }
    public long getTotalCoupons() { return totalCoupons; }
    public void setTotalCoupons(long totalCoupons) { this.totalCoupons = totalCoupons; }
    public long getTotalReviews() { return totalReviews; }
    public void setTotalReviews(long totalReviews) { this.totalReviews = totalReviews; }
    public long getTotalAddresses() { return totalAddresses; }
    public void setTotalAddresses(long totalAddresses) { this.totalAddresses = totalAddresses; }
    public long getTotalPayments() { return totalPayments; }
    public void setTotalPayments(long totalPayments) { this.totalPayments = totalPayments; }
}
