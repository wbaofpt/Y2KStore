package com.y2kstore.backend.controller;

import com.y2kstore.backend.dto.ReviewDTO;
import com.y2kstore.backend.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /*
     * =============================================================
     * LẤY REVIEW CỦA SẢN PHẨM
     * =============================================================
     *
     * Chỉ trả review VISIBLE.
     */
    @GetMapping("/products/{id}/reviews")
    public ResponseEntity<List<ReviewDTO>> getProductReviews(
            @PathVariable Integer id
    ) {

        return ResponseEntity.ok(
                reviewService.getProductReviews(id)
        );
    }

    /*
     * =============================================================
     * KIỂM TRA CÓ ĐƯỢC ĐÁNH GIÁ KHÔNG
     * =============================================================
     */
    @GetMapping("/products/{id}/review-eligibility")
    public ResponseEntity<Map<String, Object>> getReviewEligibility(
            @PathVariable Integer id
    ) {

        try {

            return ResponseEntity.ok(
                    reviewService.getReviewEligibility(id)
            );

        } catch (Exception e) {

            return ResponseEntity
                    .status(401)
                    .build();
        }
    }

    /*
     * =============================================================
     * TẠO REVIEW
     * =============================================================
     */
    @PostMapping("/products/{id}/reviews")
    public ResponseEntity<?> createProductReview(
            @PathVariable Integer id,
            @RequestBody ReviewDTO dto
    ) {

        try {

            ReviewDTO createdReview =
                    reviewService.createProductReview(
                            id,
                            dto
                    );

            return ResponseEntity.ok(
                    createdReview
            );

        } catch (RuntimeException e) {

            String message = e.getMessage();

            /*
             * Người dùng chưa đủ điều kiện đánh giá.
             */
            if (message != null
                    && message.contains(
                    "chỉ có thể đánh giá"
            )) {

                return ResponseEntity
                        .status(403)
                        .body(
                                Map.of(
                                        "message",
                                        message
                                )
                        );
            }

            /*
             * Các lỗi khác.
             */
            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    message == null
                                            ? "Có lỗi xảy ra."
                                            : message
                            )
                    );
        }
    }
}