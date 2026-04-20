package com.example.backend.dto;

import lombok.*;

public class ShippingDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ShippingFeeRequest {
        private String toDistrict;
        private String toWard;
        private int weight;
        private long insuranceValue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ShippingFeeResponse {
        private long shippingFee;
        private String message;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Province {
        private String provinceId;
        private String provinceName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class District {
        private String districtId;
        private String districtName;
        private String provinceId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Ward {
        private String wardCode;
        private String wardName;
        private String districtId;
    }
}
