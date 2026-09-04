package com.y2kstore.backend.dto;

public class PaymentSessionDTO {
    private String provider;
    private String status;
    private String paymentUrl;
    private String qrCode;
    private String deeplink;
    private String message;

    public PaymentSessionDTO() {}

    public PaymentSessionDTO(String provider, String status, String paymentUrl, String qrCode, String deeplink, String message) {
        this.provider = provider;
        this.status = status;
        this.paymentUrl = paymentUrl;
        this.qrCode = qrCode;
        this.deeplink = deeplink;
        this.message = message;
    }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPaymentUrl() { return paymentUrl; }
    public void setPaymentUrl(String paymentUrl) { this.paymentUrl = paymentUrl; }
    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    public String getDeeplink() { return deeplink; }
    public void setDeeplink(String deeplink) { this.deeplink = deeplink; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
