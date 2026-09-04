package com.y2kstore.backend.service;

import com.y2kstore.backend.dto.PaymentSessionDTO;
import com.y2kstore.backend.entity.Order;
import com.y2kstore.backend.entity.OrderItem;
import com.y2kstore.backend.entity.Payment;
import com.y2kstore.backend.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PaymentGatewayService {

    private static final Pattern ORDER_CODE_PATTERN =
            Pattern.compile("(?i)(?:\\by2k\\s*#?\\s*|#\\s*)(\\d+)\\b");

    private final PaymentRepository paymentRepository;
    private final String mbBankId;
    private final String mbBankAccountNumber;
    private final String mbBankAccountName;
    private final String mbBankQrTemplate;
    private final String sepayWebhookApiKey;
    private final String sepayBankAccount;

    public PaymentGatewayService(
            PaymentRepository paymentRepository,
            @Value("${mbbank.bank-id:970422}") String mbBankId,
            @Value("${mbbank.account-number:}") String mbBankAccountNumber,
            @Value("${mbbank.account-name:Y2K STORE}") String mbBankAccountName,
            @Value("${mbbank.qr-template:compact2}") String mbBankQrTemplate,
            @Value("${sepay.webhook.api-key:}") String sepayWebhookApiKey,
            @Value("${sepay.bank-account:}") String sepayBankAccount) {
        this.paymentRepository = paymentRepository;
        this.mbBankId = mbBankId;
        this.mbBankAccountNumber = mbBankAccountNumber;
        this.mbBankAccountName = mbBankAccountName;
        this.mbBankQrTemplate = mbBankQrTemplate;
        this.sepayWebhookApiKey = sepayWebhookApiKey;
        this.sepayBankAccount = sepayBankAccount;
    }

    public PaymentSessionDTO createPaymentSession(Order order, Payment payment, List<OrderItem> orderItems) {
        String method = normalizeMethod(payment.getMethod());
        if ("COD".equals(method)) return null;
        if ("BANKING".equals(method)) return createMbBankQr(order, payment);
        throw new RuntimeException("Phuong thuc thanh toan chua duoc ho tro.");
    }

    private PaymentSessionDTO createMbBankQr(Order order, Payment payment) {
        requireConfigured(mbBankAccountNumber, "MBBANK_ACCOUNT_NUMBER");
        long amount = toVndAmount(payment.getAmount());
        String content = "Thanh toan don Y2K #" + order.getId();
        String qrUrl = "https://img.vietqr.io/image/"
                + encodePath(mbBankId) + "-" + encodePath(mbBankAccountNumber)
                + "-" + encodePath(mbBankQrTemplate) + ".png?amount=" + amount
                + "&addInfo=" + encodeQuery(content)
                + "&accountName=" + encodeQuery(mbBankAccountName);

        return new PaymentSessionDTO(
                "BANKING", "PENDING", null, qrUrl, null,
                "Quet QR MB Bank va chuyen dung noi dung: " + content
                        + ". Shop se xac nhan thanh toan sau khi nhan tien.");
    }

    @Transactional
    public void handleSepayWebhook(Map<String, Object> payload, String authorization) {
        if (!isValidApiKey(authorization)) {
            throw new SecurityException("SePay webhook khong hop le.");
        }
        if (!"in".equalsIgnoreCase(textValue(payload.get("transferType")))) return;

        String accountNumber = textValue(payload.get("accountNumber"));
        if (sepayBankAccount != null && !sepayBankAccount.isBlank()
                && !sepayBankAccount.equals(accountNumber)) return;

        String transactionId = firstNonBlank(textValue(payload.get("id")),
                textValue(payload.get("referenceCode")));
        // SePay sends the transfer description as transactionContent.
        String content = firstNonBlank(textValue(payload.get("transactionContent")),
                firstNonBlank(textValue(payload.get("content")),
                        textValue(payload.get("description"))));
        BigDecimal amount = toAmount(firstNonNull(payload.get("transferAmount"), payload.get("amount")));
        if (transactionId == null || content == null || amount == null) return;

        Matcher matcher = ORDER_CODE_PATTERN.matcher(content);
        if (!matcher.find()) return;

        Integer orderId = Integer.valueOf(matcher.group(1));
        paymentRepository.findByOrderId(orderId).ifPresent(payment -> {
            if (!"BANKING".equalsIgnoreCase(payment.getMethod())
                    || "PAID".equalsIgnoreCase(payment.getStatus())
                    || payment.getOrder() == null
                    || "CANCELLED".equalsIgnoreCase(payment.getOrder().getStatus())
                    || payment.getSepayTransactionId() != null
                    || payment.getAmount() == null
                    || payment.getAmount().compareTo(amount) != 0) return;

            payment.setSepayTransactionId(transactionId);
            payment.setStatus("PAID");
            paymentRepository.save(payment);
        });
    }

    @Transactional(readOnly = true)
    public String getOrderPaymentStatus(Integer orderId) {
        return paymentRepository.findByOrderId(orderId)
                .map(Payment::getStatus)
                .orElseThrow(() -> new RuntimeException("Khong tim thay thanh toan cua don hang."));
    }

    private boolean isValidApiKey(String authorization) {
        if (sepayWebhookApiKey == null || sepayWebhookApiKey.isBlank()
                || authorization == null) return false;
        String value = authorization.trim();
        if (value.regionMatches(true, 0, "Apikey ", 0, 7)) {
            value = value.substring(7).trim();
        }
        return MessageDigest.isEqual(value.getBytes(StandardCharsets.UTF_8),
                sepayWebhookApiKey.trim().getBytes(StandardCharsets.UTF_8));
    }

    private String textValue(Object value) {
        if (value == null) return null;
        String text = String.valueOf(value).trim();
        return text.isBlank() ? null : text;
    }

    private String firstNonBlank(String first, String second) {
        return first != null ? first : second;
    }

    private BigDecimal toAmount(Object value) {
        if (value == null) return null;
        try {
            return new BigDecimal(String.valueOf(value)).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Object firstNonNull(Object first, Object second) {
        return first != null ? first : second;
    }

    private long toVndAmount(BigDecimal amount) {
        return amount == null ? 0 : amount.setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    private String normalizeMethod(String method) {
        return String.valueOf(method == null ? "COD" : method).trim().toUpperCase();
    }

    private void requireConfigured(String value, String envName) {
        if (value == null || value.isBlank()) {
            throw new RuntimeException("Chua cau hinh " + envName + " cho cong thanh toan.");
        }
    }

    private String encodeQuery(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private String encodePath(String value) {
        return encodeQuery(value).replace("+", "%20");
    }
}
