package com.y2kstore.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class ReviewModerationService {

    @Value("${gemini.api-key:}")
    private String apiKey;

    // Sử dụng Gemini 2.5 Flash (hoặc gemini-1.5-flash) miễn phí, xử lý tốt cả Text và Image
    private static final String GEMINI_URL_TEMPLATE =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=";

    // Bộ lọc local dùng để bắt nhanh từ ngữ khiếm nhã tiếng Việt
    private static final Set<String> LOCAL_BAD_WORDS = Set.of(
            "lồn", "cặc", "đụ", "địt", "đm", "vcl", "vãi", "cc", "cmm",
            "đéo", "fuck", "ass", "bitch", "shit", "mẹ mày", "con chó"
    );

    private final RestTemplate restTemplate;

    public ReviewModerationService() {
        this.restTemplate = new RestTemplate();
    }

    public ModerationResult checkReview(String comment, List<String> imageUrls) {

        // 1. KIỂM TRA TEXT BẰNG LOCAL FILTER TRƯỚC
        if (comment != null && !comment.trim().isEmpty()) {
            String lowerComment = comment.toLowerCase().replaceAll("\\s+", " ").trim();
            for (String badWord : LOCAL_BAD_WORDS) {
                if (lowerComment.contains(badWord)) {
                    System.out.println("[MODERATION] LOCAL FLAGGED: " + badWord);
                    return new ModerationResult(
                            ModerationStatus.FLAGGED,
                            1.0f,
                            "Phát hiện từ ngữ khiếm nhã / tục tiểu: " + badWord
                    );
                }
            }
        }

        // 2. KIỂM TRA API KEY
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("[MODERATION] Gemini API key chưa được cấu hình!");
            return new ModerationResult(
                    ModerationStatus.ERROR,
                    null,
                    "Không thể kiểm duyệt vì Gemini API key chưa được cấu hình."
            );
        }

        // 3. XÂY DỰNG PROMPT VÀ NỘI DUNG GỬI CHO GEMINI
        List<Object> contentParts = new ArrayList<>();

        String promptInstruction =
                "Bạn là một hệ thống kiểm duyệt nội dung đánh giá sản phẩm thương mại điện tử. " +
                        "Hãy kiểm tra xem nội dung văn bản và hình ảnh đính kèm có chứa yếu tố vi phạm không " +
                        "(như: ngôn từ thù địch, bạo lực, nội dung người lớn/18+, đồi trụy, lừa đảo, hoặc xúc phạm nghiêm trọng). " +
                        "Trả về kết quả duy nhất theo đúng định dạng JSON chuẩn với 2 trường: " +
                        "\"isViolation\" (true hoặc false) và \"reason\" (lý do ngắn gọn bằng tiếng Việt nếu vi phạm).";

        contentParts.add(Map.of("text", promptInstruction));

        if (comment != null && !comment.trim().isEmpty()) {
            contentParts.add(Map.of("text", "Nội dung đánh giá cần kiểm duyệt: " + comment));
        }

        // Thêm hình ảnh nếu có (Gemini hỗ trợ URL hoặc dữ liệu ảnh, ở đây truyền text mô tả hoặc cấu trúc ảnh nếu URL công khai)
        if (imageUrls != null) {
            int count = 0;
            for (String imgUrl : imageUrls) {
                if (imgUrl != null && !imgUrl.isBlank() && count < 4) {
                    contentParts.add(Map.of("text", "Hình ảnh đính kèm số " + (++count) + " có đường dẫn: " + imgUrl));
                }
            }
        }

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", contentParts)
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            System.out.println("[MODERATION] Đang gọi Google Gemini Moderation...");

            ResponseEntity<Map> response = restTemplate.exchange(
                    GEMINI_URL_TEMPLATE + apiKey,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return new ModerationResult(ModerationStatus.ERROR, null, "Gemini trả về phản hồi không hợp lệ.");
            }

            // Phân tích kết quả trả về từ Gemini
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                return new ModerationResult(ModerationStatus.SAFE, 0.0f, "Nội dung an toàn.");
            }

            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            String aiResponseText = (String) parts.get(0).get("text");

            System.out.println("[MODERATION] Gemini Response: " + aiResponseText);

            // Kiểm tra phân tích cơ bản từ câu trả lời của AI
            if (aiResponseText != null && aiResponseText.toLowerCase().contains("\"isviolation\": true")) {
                return new ModerationResult(ModerationStatus.FLAGGED, 0.9f, "AI phát hiện nội dung nghi vấn vi phạm tiêu chuẩn cộng đồng.");
            }

            return new ModerationResult(ModerationStatus.SAFE, 0.0f, "Nội dung không bị AI đánh dấu vi phạm.");

        } catch (Exception e) {
            // In chi tiết lỗi ra console của IntelliJ/Eclipse để xem nguyên nhân thật sự
            System.err.println("[MODERATION] LỖI CHI TIẾT TỪ GEMINI: " + e.getMessage());
            e.printStackTrace();

            return new ModerationResult(
                    ModerationStatus.ERROR,
                    null,
                    "Không thể kết nối hệ thống AI Gemini: " + e.getMessage()
            );
        }
    }

    public enum ModerationStatus {
        SAFE, FLAGGED, ERROR
    }

    public static class ModerationResult {
        private final ModerationStatus status;
        private final Float score;
        private final String reason;

        public ModerationResult(ModerationStatus status, Float score, String reason) {
            this.status = status;
            this.score = score;
            this.reason = reason;
        }

        public ModerationStatus getStatus() { return status; }
        public Float getScore() { return score; }
        public String getReason() { return reason; }
        public boolean isSafe() { return status == ModerationStatus.SAFE; }
        public boolean isFlagged() { return status == ModerationStatus.FLAGGED; }
        public boolean isError() { return status == ModerationStatus.ERROR; }
    }
}