package com.y2kstore.backend.service;

import com.y2kstore.backend.dto.OrderDTO;
import com.y2kstore.backend.dto.OrderItemDTO;
import com.y2kstore.backend.entity.Order;
import com.y2kstore.backend.repository.OrderItemRepository;
import com.y2kstore.backend.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class EmailService {

    private static final DateTimeFormatter MAIL_DATE_TIME = DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy");

    private final JavaMailSender mailSender;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final String fromAddress;

    public EmailService(JavaMailSender mailSender,
                        OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        @Value("${spring.mail.username:}") String fromAddress) {
        this.mailSender = mailSender;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.fromAddress = fromAddress;
    }

    public void sendPasswordResetCode(String recipient, String code, long expirationMinutes) {
        sendCodeEmail(
                recipient,
                "Mã đặt lại mật khẩu Y2K Store",
                "Đặt lại mật khẩu",
                "Dùng mã dưới đây để xác nhận yêu cầu đặt lại mật khẩu tài khoản Y2K Store.",
                code,
                "Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này.",
                expirationMinutes
        );
    }

    public void sendEmailVerificationCode(String recipient, String code, long expirationMinutes) {
        sendCodeEmail(
                recipient,
                "Mã xác nhận email Y2K Store",
                "Xác nhận email",
                "Nhập mã này trong trang tài khoản để hoàn tất xác nhận email.",
                code,
                "Nếu bạn không yêu cầu xác nhận email, hãy bỏ qua email này.",
                expirationMinutes
        );
    }

    public void sendOrderStatusNotification(String recipient, OrderDTO order, List<OrderItemDTO> items, String status) {
        if (recipient == null || recipient.isBlank() || order == null) {
            return;
        }

        String normalizedStatus = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        String statusLabel = switch (normalizedStatus) {
            case "PREPARING" -> "Đang chuẩn bị";
            case "SHIPPED" -> "Đang vận chuyển";
            case "DELIVERED" -> "Đã giao thành công";
            default -> "Đang được cập nhật";
        };
        String nextStep = switch (normalizedStatus) {
            case "PREPARING" -> "Shop đang kiểm tra và đóng gói sản phẩm cho bạn.";
            case "SHIPPED" -> "Đơn hàng đang trên đường giao, bạn vui lòng để ý điện thoại.";
            case "DELIVERED" -> "Cảm ơn bạn đã mua hàng. Bạn có thể vào tài khoản để đánh giá sản phẩm.";
            default -> "Bạn có thể kiểm tra chi tiết trong trang tài khoản.";
        };

        String subject = "Cập nhật đơn hàng #" + order.getId() + " - " + statusLabel;
        String body = buildOrderStatusBody(order, items, statusLabel, nextStep);
        sendHtml(recipient, subject, layout("Cập nhật đơn hàng", "Đơn hàng #" + order.getId(), body));
    }

    public void sendOrderStatusNotification(String recipient, Integer orderId, String status) {
        if (recipient == null || recipient.isBlank() || orderId == null) {
            return;
        }
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return;
        }
        List<OrderItemDTO> items = orderItemRepository.findByOrderId(orderId).stream()
                .map(OrderItemDTO::new)
                .toList();
        sendOrderStatusNotification(recipient, new OrderDTO(order), items, status);
    }

    private void sendCodeEmail(String recipient, String subject, String title, String intro, String code, String safetyNote, long expirationMinutes) {
        String body = """
                <p style="margin:0 0 18px;color:#475569;line-height:1.75">%s</p>
                <div style="padding:18px 14px;border:1px solid #dbeafe;border-radius:18px;background:linear-gradient(135deg,#eff6ff 0%,#ffffff 100%);text-align:center">
                  <div style="color:#0f766e;font-size:12px;font-weight:900;letter-spacing:.16em;text-transform:uppercase">Mã xác nhận</div>
                  <div style="margin-top:10px;color:#0f172a;font-size:34px;font-weight:900;letter-spacing:8px">%s</div>
                </div>
                <p style="margin:18px 0 0;color:#64748b;font-size:13px;line-height:1.65">Mã có hiệu lực trong <strong>%d phút</strong> và chỉ dùng một lần. %s</p>
                """.formatted(escape(intro), escape(code), expirationMinutes, escape(safetyNote));
        sendHtml(recipient, subject, layout(title, "Mã xác nhận của bạn", body));
    }

    private void sendHtml(String recipient, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            if (fromAddress != null && !fromAddress.isBlank()) {
                helper.setFrom(fromAddress);
            }
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception ex) {
            throw new RuntimeException("Không thể gửi email.", ex);
        }
    }

    private String layout(String title, String heading, String body) {
        return """
                <div style="margin:0;padding:28px;background:linear-gradient(180deg,#f5f7fb 0%%,#eef6ff 100%%);font-family:Arial,sans-serif;color:#111827">
                  <div style="max-width:720px;margin:0 auto;background:#fff;border:1px solid #e5e7eb;border-radius:24px;overflow:hidden;box-shadow:0 18px 45px rgba(15,23,42,.08)">
                    <div style="padding:28px 30px;background:linear-gradient(135deg,#111827 0%%,#0f172a 60%%,#0891b2 140%%);color:#fff">
                      <div style="display:inline-flex;align-items:center;gap:8px;padding:6px 12px;border-radius:999px;background:rgba(103,232,249,.12);color:#67e8f9;font-size:12px;font-weight:800;letter-spacing:1.6px;text-transform:uppercase">Y2K Store</div>
                      <h1 style="margin:12px 0 0;font-size:28px;line-height:1.2">%s</h1>
                      <p style="margin:8px 0 0;color:#cbd5e1;line-height:1.6">Cập nhật đơn hàng và thông tin thanh toán của bạn.</p>
                    </div>
                    <div style="padding:30px">
                      <h2 style="margin:0 0 16px;font-size:22px;line-height:1.25;color:#111827">%s</h2>
                      %s
                      <div style="margin-top:24px;padding-top:18px;border-top:1px solid #e5e7eb;color:#64748b;font-size:12px;line-height:1.7">
                        Email này được gửi tự động từ Y2K Store. Vui lòng không chia sẻ mã xác nhận hoặc thông tin thanh toán cho người khác.
                      </div>
                    </div>
                  </div>
                </div>
                """.formatted(escape(title), escape(heading), body);
    }

    private String buildOrderStatusBody(OrderDTO order, List<OrderItemDTO> items, String statusLabel, String nextStep) {
        return summaryGrid(order, statusLabel)
                + statusCard(statusLabel, nextStep)
                + orderDetailsCard(order)
                + itemsCard(items)
                + totalsCard(order, items)
                + "<p style=\"margin:18px 0 0;color:#64748b;font-size:13px;line-height:1.7\">Bạn có thể mở trang tài khoản để theo dõi trạng thái đơn, tiếp tục thanh toán nếu cần và xem lại lịch sử giao hàng.</p>";
    }

    private String summaryGrid(OrderDTO order, String statusLabel) {
        return """
                <div style="display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:12px;margin-bottom:16px">
                  %s
                  %s
                  %s
                  %s
                </div>
                """.formatted(
                infoTile("Mã đơn", "#" + order.getId(), "bi bi-receipt"),
                infoTile("Trạng thái", statusLabel, "bi bi-truck"),
                infoTile("Ngày đặt", formatDateTime(order.getOrderDate()), "bi bi-calendar3"),
                infoTile("Thanh toán", paymentLabel(order), "bi bi-credit-card")
        );
    }

    private String infoTile(String label, String value, String iconClass) {
        return """
                <div style="padding:16px;border:1px solid #e5e7eb;border-radius:16px;background:#fbfdff">
                  <div style="display:flex;align-items:center;gap:8px;color:#0ea5e9;font-size:12px;font-weight:900;letter-spacing:.08em;text-transform:uppercase"><span style="font-style:normal">%s</span><span>%s</span></div>
                  <div style="margin-top:8px;color:#111827;font-size:18px;font-weight:900;line-height:1.35">%s</div>
                </div>
                """.formatted(iconGlyph(iconClass), escape(label), escape(value));
    }

    private String statusCard(String statusLabel, String nextStep) {
        return """
                <div style="margin-top:6px;border:1px solid #bae6fd;background:linear-gradient(135deg,#f0f9ff 0%,#ffffff 100%);border-radius:18px;padding:18px">
                  <div style="font-size:12px;font-weight:900;text-transform:uppercase;letter-spacing:1px;color:#0369a1">Trạng thái mới</div>
                  <div style="margin-top:6px;font-size:24px;font-weight:900;color:#0f172a">%s</div>
                  <p style="margin:10px 0 0;color:#334155;line-height:1.65">%s</p>
                </div>
                """.formatted(escape(statusLabel), escape(nextStep));
    }

    private String orderDetailsCard(OrderDTO order) {
        return """
                <div style="margin-top:16px;padding:18px;border:1px solid #e5e7eb;border-radius:18px;background:#fff">
                  <div style="font-size:12px;font-weight:900;letter-spacing:.08em;text-transform:uppercase;color:#0ea5e9">Chi tiết đơn hàng</div>
                  <div style="display:grid;gap:10px;margin-top:14px">
                    %s
                    %s
                    %s
                    %s
                    %s
                  </div>
                </div>
                """.formatted(
                detailRow("Người nhận", firstNonBlank(order.getFullName(), order.getUserFullName(), "Khách hàng")),
                detailRow("Số điện thoại", firstNonBlank(order.getPhone(), order.getUserPhone(), "Chưa cập nhật")),
                detailRow("Thanh toán", paymentLabel(order)),
                detailRow("Phương thức", paymentMethodLabel(order.getPaymentMethod())),
                detailRow("Địa chỉ", firstNonBlank(order.getAddressLabel(), "Chưa có địa chỉ giao hàng"))
        );
    }

    private String detailRow(String label, String value) {
        return """
                <div style="display:flex;justify-content:space-between;gap:16px;padding:12px 0;border-bottom:1px dashed #e5e7eb">
                  <span style="color:#64748b;font-size:13px;font-weight:700">%s</span>
                  <strong style="margin:0;color:#111827;font-size:13px;text-align:right;line-height:1.5">%s</strong>
                </div>
                """.formatted(escape(label), escape(value));
    }

    private String itemsCard(List<OrderItemDTO> items) {
        if (items == null || items.isEmpty()) {
            return """
                    <div style="margin-top:16px;padding:18px;border:1px solid #e5e7eb;border-radius:18px;background:#fff">
                      <div style="font-size:12px;font-weight:900;letter-spacing:.08em;text-transform:uppercase;color:#0ea5e9">Sản phẩm trong đơn</div>
                      <p style="margin:14px 0 0;color:#64748b;line-height:1.6">Không có dữ liệu sản phẩm trong đơn.</p>
                    </div>
                    """;
        }

        StringBuilder rows = new StringBuilder();
        for (OrderItemDTO item : items) {
            rows.append("""
                    <div style="display:grid;grid-template-columns:72px minmax(0,1fr) auto;gap:14px;align-items:center;padding:12px;border:1px solid #eef2f7;border-radius:16px;background:#fbfdff">
                      %s
                      <div style="min-width:0">
                        <strong style="display:block;color:#111827;font-size:14px;line-height:1.45">%s</strong>
                        <span style="display:block;margin-top:4px;color:#64748b;font-size:12px;line-height:1.5">%s</span>
                        <span style="display:block;margin-top:4px;color:#64748b;font-size:12px;line-height:1.5">Số lượng: %s</span>
                      </div>
                      <div style="text-align:right">
                        <strong style="display:block;color:#e11d48;font-size:14px;white-space:nowrap">%s</strong>
                        <span style="display:block;margin-top:4px;color:#64748b;font-size:12px;white-space:nowrap">%s/sp</span>
                      </div>
                    </div>
                    """.formatted(
                    itemMedia(item),
                    escape(firstNonBlank(item.getProductName(), "Sản phẩm")),
                    escape(firstNonBlank(item.getVariantLabel(), "Biến thể mặc định")),
                    item.getQuantity() == null ? "0" : item.getQuantity().toString(),
                    escape(formatMoney(item.getTotalPrice())),
                    escape(formatMoney(item.getUnitPrice()))
            ));
        }

        return """
                <div style="margin-top:16px;padding:18px;border:1px solid #e5e7eb;border-radius:18px;background:#fff">
                  <div style="font-size:12px;font-weight:900;letter-spacing:.08em;text-transform:uppercase;color:#0ea5e9">Sản phẩm trong đơn</div>
                  <div style="display:grid;gap:12px;margin-top:14px">%s</div>
                </div>
                """.formatted(rows);
    }

    private String totalsCard(OrderDTO order, List<OrderItemDTO> items) {
        BigDecimal subtotal = BigDecimal.ZERO;
        if (items != null) {
            for (OrderItemDTO item : items) {
                if (item.getTotalPrice() != null) {
                    subtotal = subtotal.add(item.getTotalPrice());
                }
            }
        }
        BigDecimal total = order.getTotalAmount() != null ? order.getTotalAmount() : subtotal;
        return """
                <div style="margin-top:16px;padding:18px;border:1px solid #dbeafe;border-radius:18px;background:linear-gradient(180deg,#eff6ff 0%,#ffffff 100%)">
                  <div style="display:flex;justify-content:space-between;gap:16px;padding:8px 0;color:#334155"><span>Tạm tính</span><strong>%s</strong></div>
                  <div style="display:flex;justify-content:space-between;gap:16px;padding:8px 0;color:#334155"><span>Tổng thanh toán</span><strong style="font-size:20px;color:#e11d48">%s</strong></div>
                </div>
                """.formatted(escape(formatMoney(subtotal)), escape(formatMoney(total)));
    }

    private String itemMedia(OrderItemDTO item) {
        String image = item.getProductImage();
        String alt = firstNonBlank(item.getProductName(), "Sản phẩm");
        if (image != null && (image.startsWith("http://") || image.startsWith("https://"))) {
            return "<img src=\"" + escape(image) + "\" alt=\"" + escape(alt) + "\" style=\"width:72px;height:84px;object-fit:cover;border-radius:12px;background:#f8fafc;border:1px solid #e5e7eb\">";
        }
        return """
                <div style="width:72px;height:84px;display:grid;place-items:center;border-radius:12px;background:linear-gradient(135deg,#ecfeff,#fdf2f8);border:1px solid #e5e7eb;color:#0f766e;font-size:18px;font-weight:900">
                  %s
                </div>
                """.formatted(escape(initials(alt)));
    }

    private String paymentLabel(OrderDTO order) {
        String method = order.getPaymentMethod() == null ? "" : order.getPaymentMethod().toUpperCase(Locale.ROOT);
        String status = order.getPaymentStatus() == null ? "" : order.getPaymentStatus().toUpperCase(Locale.ROOT);
        String methodLabel = paymentMethodLabel(method);
        String statusLabel = switch (status) {
            case "PAID" -> "Đã thanh toán";
            case "PENDING" -> "Chờ thanh toán";
            case "UNPAID" -> "Chưa thanh toán";
            default -> "Đang xử lý";
        };
        return methodLabel + " · " + statusLabel;
    }

    private String paymentMethodLabel(String method) {
        return switch (method == null ? "" : method.toUpperCase(Locale.ROOT)) {
            case "COD" -> "Thanh toán khi nhận hàng";
            case "BANKING" -> "Chuyển khoản MB Bank";
            default -> "COD";
        };
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "—" : value.format(MAIL_DATE_TIME);
    }

    private String formatMoney(BigDecimal value) {
        BigDecimal safeValue = value == null ? BigDecimal.ZERO : value;
        NumberFormat format = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(0);
        return format.format(safeValue.setScale(0, RoundingMode.HALF_UP)) + " đ";
    }

    private String firstNonBlank(String... values) {
        if (values == null) return "";
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private String initials(String value) {
        String[] parts = value == null ? new String[0] : value.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = Math.max(0, parts.length - 2); i < parts.length; i++) {
            if (!parts[i].isBlank()) sb.append(parts[i].charAt(0));
        }
        return sb.length() > 0 ? sb.toString().toUpperCase(Locale.ROOT) : "Y2K";
    }

    private String iconGlyph(String iconClass) {
        return switch (iconClass) {
            case "bi bi-truck" -> "🚚";
            case "bi bi-calendar3" -> "🗓";
            case "bi bi-credit-card" -> "💳";
            default -> "•";
        };
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
