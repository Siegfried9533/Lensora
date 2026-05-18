package com.camerashop.controller;

import com.camerashop.dto.ApiResponse;
import com.camerashop.dto.ShippingDTO;
import com.camerashop.service.GHNService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shipping")
@CrossOrigin(origins = "*")
public class ShippingController {

    @Autowired
    private GHNService ghnService;

    /**
     * Tinh phi van chuyen
     */
    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse> calculateShippingFee(@RequestBody ShippingDTO.ShippingFeeRequest request) {
        try {
            long shippingFee = ghnService.calculateShippingFee(
                    request.getToDistrict(),
                    request.getToWard(),
                    request.getWeight(),
                    request.getInsuranceValue()
            );

            ShippingDTO.ShippingFeeResponse response = ShippingDTO.ShippingFeeResponse.builder()
                    .shippingFee(shippingFee)
                    .message("Tính phí vận chuyển thành công")
                    .build();

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Tính phí vận chuyển thất bại: " + e.getMessage()));
        }
    }

    /**
     * Lay tat ca tinh/thanh pho
     */
    @GetMapping("/provinces")
    public ResponseEntity<ApiResponse> getProvinces() {
        try {
            JsonNode provinces = ghnService.getProvinces();
            List<ShippingDTO.Province> provinceList = new ArrayList<>();

            if (provinces.isArray()) {
                for (JsonNode province : provinces) {
                    provinceList.add(ShippingDTO.Province.builder()
                            .provinceId(province.has("ProvinceID") ? province.get("ProvinceID").asText() : "")
                            .provinceName(province.has("ProvinceName") ? province.get("ProvinceName").asText() : "")
                            .build());
                }
            }

            return ResponseEntity.ok(ApiResponse.success(provinceList));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lấy danh sách tỉnh/thành phố thất bại: " + e.getMessage()));
        }
    }

    /**
     * Lay quan/huyen theo tinh/thanh pho
     */
    @GetMapping("/districts/{provinceId}")
    public ResponseEntity<ApiResponse> getDistricts(@PathVariable String provinceId) {
        try {
            JsonNode districts = ghnService.getDistricts(provinceId);
            List<ShippingDTO.District> districtList = new ArrayList<>();

            if (districts.isArray()) {
                for (JsonNode district : districts) {
                    districtList.add(ShippingDTO.District.builder()
                            .districtId(district.has("DistrictID") ? district.get("DistrictID").asText() : "")
                            .districtName(district.has("DistrictName") ? district.get("DistrictName").asText() : "")
                            .provinceId(district.has("ProvinceID") ? district.get("ProvinceID").asText() : "")
                            .build());
                }
            }

            return ResponseEntity.ok(ApiResponse.success(districtList));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lấy danh sách quận/huyện thất bại: " + e.getMessage()));
        }
    }

    /**
     * Lay phuong/xa theo quan/huyen
     */
    @GetMapping("/wards/{districtId}")
    public ResponseEntity<ApiResponse> getWards(@PathVariable String districtId) {
        try {
            JsonNode wards = ghnService.getWards(districtId);
            List<ShippingDTO.Ward> wardList = new ArrayList<>();

            if (wards.isArray()) {
                for (JsonNode ward : wards) {
                    wardList.add(ShippingDTO.Ward.builder()
                            .wardCode(ward.has("WardCode") ? ward.get("WardCode").asText() : "")
                            .wardName(ward.has("WardName") ? ward.get("WardName").asText() : "")
                            .districtId(ward.has("DistrictID") ? ward.get("DistrictID").asText() : "")
                            .build());
                }
            }

            return ResponseEntity.ok(ApiResponse.success(wardList));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lấy danh sách phường/xã thất bại: " + e.getMessage()));
        }
    }

    /**
     * Lay trang thai theo doi don hang
     */
    @GetMapping("/track/{orderCode}")
    public ResponseEntity<ApiResponse> trackOrder(@PathVariable String orderCode) {
        try {
            String status = ghnService.getOrderStatus(orderCode);

            Map<String, String> result = new HashMap<>();
            result.put("orderCode", orderCode);
            result.put("status", status);

            return ResponseEntity.ok(ApiResponse.success(result));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Theo dõi đơn hàng thất bại: " + e.getMessage()));
        }
    }
}
