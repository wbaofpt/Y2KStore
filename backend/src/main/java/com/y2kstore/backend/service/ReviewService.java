package com.y2kstore.backend.service;

import com.y2kstore.backend.dto.ReviewDTO;
import com.y2kstore.backend.entity.Product;
import com.y2kstore.backend.entity.Review;
import com.y2kstore.backend.entity.User;
import com.y2kstore.backend.repository.OrderItemRepository;
import com.y2kstore.backend.repository.ProductRepository;
import com.y2kstore.backend.repository.ReviewRepository;
import com.y2kstore.backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ReviewModerationService moderationService;

    public ReviewService(
            ReviewRepository reviewRepository,
            ProductRepository productRepository,
            OrderItemRepository orderItemRepository,
            UserRepository userRepository,
            ReviewModerationService moderationService
    ) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.moderationService = moderationService;
    }

    private User getAuthenticatedUser() {

        Authentication auth =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        return userRepository
                .findByEmail(auth.getName())
                .orElseThrow(() ->
                        new RuntimeException("Chưa đăng nhập!")
                );
    }

    /*
     * =============================================================
     * LẤY REVIEW CHO KHÁCH HÀNG
     * =============================================================
     *
     * Khách hàng CHỈ được nhìn thấy review VISIBLE.
     *
     * FLAGGED / REJECTED không được hiển thị.
     */
    @Transactional(readOnly = true)
    public List<ReviewDTO> getProductReviews(
            Integer productId
    ) {

        return reviewRepository
                .findByProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .filter(r ->
                        "VISIBLE".equals(r.getStatus())
                )
                .map(ReviewDTO::new)
                .collect(Collectors.toList());
    }

    /*
     * =============================================================
     * KIỂM TRA QUYỀN ĐÁNH GIÁ
     * =============================================================
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getReviewEligibility(
            Integer productId
    ) {

        User user = getAuthenticatedUser();

        boolean purchased =
                orderItemRepository.existsReviewablePurchase(
                        user.getId(),
                        productId
                );

        boolean reviewed =
                reviewRepository.existsByUserIdAndProductId(
                        user.getId(),
                        productId
                );

        return Map.of(
                "canReview",
                purchased && !reviewed,

                "purchased",
                purchased,

                "reviewed",
                reviewed
        );
    }

    /*
     * =============================================================
     * TẠO REVIEW
     * =============================================================
     */
    @Transactional
    public ReviewDTO createProductReview(
            Integer productId,
            ReviewDTO dto
    ) {

        /*
         * ---------------------------------------------------------
         * 1. Lấy user đang đăng nhập
         * ---------------------------------------------------------
         */

        User user = getAuthenticatedUser();

        /*
         * ---------------------------------------------------------
         * 2. Kiểm tra sản phẩm
         * ---------------------------------------------------------
         */

        Product product =
                productRepository
                        .findById(productId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Sản phẩm không tồn tại!"
                                )
                        );

        /*
         * ---------------------------------------------------------
         * 3. Kiểm tra người dùng đã mua sản phẩm
         * ---------------------------------------------------------
         */

        if (!orderItemRepository.existsReviewablePurchase(
                user.getId(),
                productId
        )) {

            throw new RuntimeException(
                    "Bạn chỉ có thể đánh giá sản phẩm sau khi đơn hàng đã được giao và thanh toán."
            );
        }

        /*
         * ---------------------------------------------------------
         * 4. Mỗi user chỉ được đánh giá một lần
         * ---------------------------------------------------------
         */

        if (reviewRepository.existsByUserIdAndProductId(
                user.getId(),
                productId
        )) {

            throw new RuntimeException(
                    "Bạn đã đánh giá sản phẩm này rồi."
            );
        }

        /*
         * ---------------------------------------------------------
         * 5. Rating
         * ---------------------------------------------------------
         */

        int rating =
                dto.getRating() == null
                        ? 5
                        : Math.max(
                        1,
                        Math.min(
                                dto.getRating(),
                                5
                        )
                );

        /*
         * ---------------------------------------------------------
         * 6. Lấy TEXT / IMAGE / VIDEO
         * ---------------------------------------------------------
         */

        List<String> imageUrls =
                dto.getImageUrls() == null
                        ? List.of()
                        : dto.getImageUrls();

        List<String> videoUrls =
                dto.getVideoUrls() == null
                        ? List.of()
                        : dto.getVideoUrls();

        /*
         * ---------------------------------------------------------
         * 7. AI KIỂM TRA TEXT + IMAGE
         * ---------------------------------------------------------
         *
         * Video hiện tại chưa gửi trực tiếp cho Moderation API.
         *
         * Video sẽ được lưu nguyên bản.
         * Sau này có thể xử lý frame/video riêng.
         */

        ReviewModerationService.ModerationResult modResult =
                moderationService.checkReview(
                        dto.getComment(),
                        imageUrls
                );

        /*
         * ---------------------------------------------------------
         * 8. TẠO REVIEW
         * ---------------------------------------------------------
         */

        Review review = new Review();

        review.setUser(user);
        review.setProduct(product);
        review.setRating(rating);

        /*
         * Giữ nguyên nội dung người dùng gửi.
         * Không sửa comment.
         */
        review.setComment(dto.getComment());

        /*
         * Giữ nguyên danh sách ảnh.
         */
        review.setImageUrls(
                ReviewDTO.toJson(imageUrls)
        );

        /*
         * Giữ nguyên danh sách video.
         */
        review.setVideoUrls(
                ReviewDTO.toJson(videoUrls)
        );

        /*
         * ---------------------------------------------------------
         * 9. LƯU KẾT QUẢ AI
         * ---------------------------------------------------------
         */

        review.setAiChecked(true);

        review.setAiScore(
                modResult.getScore()
        );

        review.setAiReason(
                modResult.getReason()
        );

        /*
         * ---------------------------------------------------------
         * 10. QUYẾT ĐỊNH STATUS
         * ---------------------------------------------------------
         *
         * SAFE
         *      -> VISIBLE
         *
         * FLAGGED
         *      -> FLAGGED
         *      -> Admin kiểm tra
         *
         * ERROR
         *      -> FLAGGED
         *      -> Admin kiểm tra
         *
         * AI KHÔNG được tự ý REJECT.
         */
        if (modResult.isSafe()) {

            review.setStatus("VISIBLE");

        } else {

            review.setStatus("FLAGGED");
        }

        /*
         * ---------------------------------------------------------
         * 11. LƯU DATABASE
         * ---------------------------------------------------------
         */

        reviewRepository.save(review);

        /*
         * ---------------------------------------------------------
         * 12. TRẢ REVIEW
         * ---------------------------------------------------------
         */

        return new ReviewDTO(review);
    }
}