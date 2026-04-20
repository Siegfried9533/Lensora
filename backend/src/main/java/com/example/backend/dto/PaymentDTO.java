package com.example.backend.dto;

import lombok.*;

public class PaymentDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VNPayRequest {
        private String orderCode;
        private Long amount;
        private String bankCode;
        private String locale;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VNPayResponse {
        private String paymentUrl;
        private String transactionRef;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VNPayCallback {
        private String vnp_TxnRef;
        private String vnp_TransactionNo;
        private String vnp_TransactionStatus;
        private String vnp_BankCode;
        private String vnp_PayDate;
        private String vnp_ResponseCode;
        private String vnp_SecureHash;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentResult {
        private boolean success;
        private String message;
        private String orderCode;
        private Long amount;
        private String transactionRef;
    }
}
