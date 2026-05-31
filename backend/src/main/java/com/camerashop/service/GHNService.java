package com.camerashop.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GHNService {

    private static final Logger log = LoggerFactory.getLogger(GHNService.class);

    private static final String VIETNAM_PROVINCES_API = "https://provinces.open-api.vn/api/v1";

    @Value("${app.ghn.api-url}")
    private String ghnApiUrl;

    @Value("${app.ghn.shop-id}")
    private String shopId;

    @Value("${app.ghn.token}")
    private String ghnToken;

    @Value("${app.ghn.district-id}")
    private String defaultDistrictId;

    @Value("${app.ghn.from-name:}")
    private String fromName;

    @Value("${app.ghn.from-phone:}")
    private String fromPhone;

    @Value("${app.ghn.from-address:}")
    private String fromAddress;

    @Value("${app.ghn.from-ward-code:}")
    private String fromWardCode;

    @Value("${app.ghn.from-district-id:0}")
    private String fromDistrictId;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Calculates shipping fee for an order
     * @param toDistrict District ID of the recipient
     * @param toWard Ward code of the recipient
     * @param weight Package weight in grams
     * @param insuranceValue Declared value for insurance
     * @return Shipping fee in VND
     */
    public long calculateShippingFee(String toDistrict, String toWard, int weight, long insuranceValue) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Token", ghnToken);
            headers.set("ShopId", shopId);

            Map<String, Object> body = new HashMap<>();
            body.put("service_type_id", 2); // Standard delivery
            body.put("to_district_id", Integer.parseInt(toDistrict));
            body.put("to_ward_code", toWard);
            body.put("height", 10); // Default dimensions in cm
            body.put("length", 20);
            body.put("width", 15);
            body.put("weight", weight);
            body.put("insurance_value", insuranceValue);
            body.put("coupon", null);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    ghnApiUrl + "/shipping-order/available-services",
                    HttpMethod.POST,
                    request,
                    String.class
            );

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            if (jsonNode.has("data") && jsonNode.get("data").has("total")) {
                return jsonNode.get("data").get("total").asLong();
            }

            // Fallback: calculate based on distance
            return calculateDefaultShippingFee(toDistrict, weight);

        } catch (Exception e) {
            // Fallback to default calculation
            return calculateDefaultShippingFee(toDistrict, weight);
        }
    }

    /**
     * Tao van don thuc te tren GHN (endpoint v2/shipping-order/create).
     * Nem ngoai le co thong diep ro rang khi thieu cau hinh hoac thieu dia chi,
     * de tang goi (OrderService) tu quyet dinh chuyen thanh canh bao ma khong
     * lam fail don hang noi bo.
     *
     * @param clientOrderCode  ma don hang noi bo (client_order_code)
     * @param toName           ten nguoi nhan
     * @param toPhone          so dien thoai nguoi nhan
     * @param toAddress        so nha / ten duong
     * @param toWardCode       ma phuong/xa (GHN ward_code)
     * @param toDistrictId     ma quan/huyen (GHN district_id)
     * @param weight           tong khoi luong goi hang (gram)
     * @param insuranceValue   gia tri khai gia bao hiem (VND)
     * @param codAmount        so tien thu ho COD (0 neu da thanh toan truoc)
     * @param items            danh sach san pham: moi phan tu can name/quantity/weight
     * @return ma van don GHN (order_code)
     */
    public String createShippingOrder(String clientOrderCode, String toName, String toPhone,
                                       String toAddress, String toWardCode, String toDistrictId,
                                       int weight, long insuranceValue, long codAmount,
                                       List<Map<String, Object>> items) {
        ensureConfigured();
        if (toDistrictId == null || toDistrictId.isBlank() || "0".equals(toDistrictId.trim())) {
            throw new IllegalArgumentException("Thiếu mã quận/huyện (district_id) để tạo vận đơn GHN");
        }
        if (toWardCode == null || toWardCode.isBlank()) {
            throw new IllegalArgumentException("Thiếu mã phường/xã (ward_code) để tạo vận đơn GHN");
        }

        int districtId;
        try {
            districtId = Integer.parseInt(toDistrictId.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Mã quận/huyện (district_id) không hợp lệ: " + toDistrictId);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", ghnToken);
        headers.set("ShopId", shopId);

        Map<String, Object> body = new HashMap<>();
        // payment_type_id: 1 = Shop tra phi (don da thanh toan truoc), 2 = Nguoi nhan tra (COD)
        body.put("payment_type_id", codAmount > 0 ? 2 : 1);
        body.put("required_note", "KHONGCHOXEMHANG"); // enum hop le cua GHN v2
        // Dia chi lay hang: chi gui khi da cau hinh day du. Neu shop tren GHN chua khai bao
        // dia chi va khong gui from_*, GHN se tra FROM_ADDRESS_CONVERT_FAIL.
        applyFromAddress(body);
        body.put("to_name", toName);
        body.put("to_phone", toPhone);
        body.put("to_address", toAddress);
        body.put("to_ward_code", toWardCode);
        body.put("to_district_id", districtId);
        body.put("height", 10);
        body.put("length", 20);
        body.put("width", 15);
        body.put("weight", Math.max(1, weight));
        body.put("insurance_value", insuranceValue);
        if (codAmount > 0) {
            body.put("cod_amount", codAmount);
        }
        body.put("service_type_id", 2);
        body.put("client_order_code", clientOrderCode);
        body.put("items", buildItems(items, weight));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl() + "/v2/shipping-order/create",
                    HttpMethod.POST,
                    request,
                    String.class
            );

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            if (jsonNode.has("data") && jsonNode.get("data").has("order_code")) {
                return jsonNode.get("data").get("order_code").asText();
            }
            throw new RuntimeException("GHN không trả về mã vận đơn (order_code)");

        } catch (RestClientResponseException e) {
            String message = extractGhnMessage(e.getResponseBodyAsString());
            log.error("GHN create shipping order failed for {} (HTTP {}): {}",
                    clientOrderCode, e.getRawStatusCode(), message);
            throw new RuntimeException("GHN từ chối tạo vận đơn: " + message, e);
        } catch (RuntimeException e) {
            log.error("GHN create shipping order failed for {}: {}", clientOrderCode, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("GHN create shipping order failed for {}: {}", clientOrderCode, e.getMessage());
            throw new RuntimeException("Không thể kết nối GHN để tạo vận đơn: " + e.getMessage(), e);
        }
    }

    /**
     * Huy van don tren GHN (endpoint v2/switch-status/cancel).
     * @param orderCode ma van don GHN can huy
     */
    public void cancelShippingOrder(String orderCode) {
        ensureConfigured();
        if (orderCode == null || orderCode.isBlank()) {
            throw new IllegalArgumentException("Thiếu mã vận đơn GHN để hủy");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", ghnToken);
        headers.set("ShopId", shopId);

        Map<String, Object> body = new HashMap<>();
        body.put("order_codes", List.of(orderCode));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.exchange(
                    baseUrl() + "/v2/switch-status/cancel",
                    HttpMethod.POST,
                    request,
                    String.class
            );
            log.info("GHN shipping order {} cancelled", orderCode);
        } catch (RestClientResponseException e) {
            String message = extractGhnMessage(e.getResponseBodyAsString());
            log.error("GHN cancel shipping order {} failed (HTTP {}): {}",
                    orderCode, e.getRawStatusCode(), message);
            throw new RuntimeException("GHN từ chối hủy vận đơn: " + message, e);
        } catch (Exception e) {
            log.error("GHN cancel shipping order {} failed: {}", orderCode, e.getMessage());
            throw new RuntimeException("Không thể kết nối GHN để hủy vận đơn: " + e.getMessage(), e);
        }
    }

    /**
     * Kiem tra GHN da duoc cau hinh (co token + shopId hop le) hay chua.
     * Moi truong dev thuong de trong token/shopId=0, khi do bo qua goi GHN.
     */
    public boolean isConfigured() {
        return ghnToken != null && !ghnToken.isBlank()
                && shopId != null && !shopId.isBlank() && !"0".equals(shopId.trim());
    }

    private void ensureConfigured() {
        if (!isConfigured()) {
            throw new IllegalStateException(
                    "GHN chưa được cấu hình (thiếu APP_GHN_TOKEN hoặc APP_GHN_SHOP_ID)");
        }
    }

    /** Bo dau '/' cuoi de tranh URL bi double-slash khi noi voi duong dan. */
    private String baseUrl() {
        if (ghnApiUrl != null && ghnApiUrl.endsWith("/")) {
            return ghnApiUrl.substring(0, ghnApiUrl.length() - 1);
        }
        return ghnApiUrl;
    }

    /**
     * Dinh kem dia chi lay hang (from_*) vao payload tao van don khi da cau hinh day du.
     * GHN yeu cau ca 5 truong (name/phone/address/ward_code/district_id) thi moi nhan;
     * thieu mot truong se khong gui de tranh payload nua voi.
     */
    private void applyFromAddress(Map<String, Object> body) {
        boolean hasName = fromName != null && !fromName.isBlank();
        boolean hasPhone = fromPhone != null && !fromPhone.isBlank();
        boolean hasAddress = fromAddress != null && !fromAddress.isBlank();
        boolean hasWard = fromWardCode != null && !fromWardCode.isBlank();
        boolean hasDistrict = fromDistrictId != null && !fromDistrictId.isBlank()
                && !"0".equals(fromDistrictId.trim());
        if (!(hasName && hasPhone && hasAddress && hasWard && hasDistrict)) {
            return;
        }
        int districtId;
        try {
            districtId = Integer.parseInt(fromDistrictId.trim());
        } catch (NumberFormatException e) {
            log.warn("app.ghn.from-district-id khong hop le: {} - bo qua dia chi lay hang", fromDistrictId);
            return;
        }
        body.put("from_name", fromName);
        body.put("from_phone", fromPhone);
        body.put("from_address", fromAddress);
        body.put("from_ward_code", fromWardCode);
        body.put("from_district_id", districtId);
    }

    private List<Map<String, Object>> buildItems(List<Map<String, Object>> items, int totalWeight) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (items != null) {
            for (Map<String, Object> item : items) {
                Map<String, Object> ghnItem = new HashMap<>();
                ghnItem.put("name", item.getOrDefault("name", "Sản phẩm"));
                Object qty = item.get("quantity");
                ghnItem.put("quantity", qty instanceof Number ? ((Number) qty).intValue() : 1);
                Object w = item.get("weight");
                ghnItem.put("weight", w instanceof Number ? ((Number) w).intValue() : 500);
                result.add(ghnItem);
            }
        }
        if (result.isEmpty()) {
            // GHN bat buoc co it nhat 1 item
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("name", "Đơn hàng Lensora");
            fallback.put("quantity", 1);
            fallback.put("weight", Math.max(1, totalWeight));
            result.add(fallback);
        }
        return result;
    }

    private String extractGhnMessage(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "không có phản hồi";
        }
        try {
            JsonNode node = objectMapper.readTree(responseBody);
            if (node.has("message")) {
                return node.get("message").asText();
            }
        } catch (Exception ignored) {
            // tra ve raw body neu khong parse duoc
        }
        return responseBody;
    }

    /**
     * Gets order tracking information
     * @param orderCode GHN order code
     * @return Tracking status
     */
    public String getOrderStatus(String orderCode) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Token", ghnToken);

            Map<String, Object> body = new HashMap<>();
            body.put("order_code", orderCode);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    ghnApiUrl + "/shipping-order/detail",
                    HttpMethod.POST,
                    request,
                    String.class
            );

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            if (jsonNode.has("data") && jsonNode.get("data").has("status")) {
                return jsonNode.get("data").get("status").asText();
            }

        } catch (Exception e) {
            System.err.println("Failed to get GHN order status: " + e.getMessage());
        }

        return "UNKNOWN";
    }

    /**
     * Gets all provinces from GHN
     * @return List of provinces
     */
    public JsonNode getProvinces() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Token", ghnToken);

            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    ghnApiUrl + "/master-data/province",
                    HttpMethod.GET,
                    request,
                    String.class
            );

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            if (jsonNode.has("data") && hasItems(jsonNode.get("data"))) {
                return jsonNode.get("data");
            }

        } catch (Exception e) {
            System.err.println("Failed to get provinces: " + e.getMessage());
        }

        return getOpenApiProvinces();
    }

    /**
     * Gets districts by province
     * @param provinceId Province ID
     * @return List of districts
     */
    public JsonNode getDistricts(String provinceId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Token", ghnToken);

            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    ghnApiUrl + "/master-data/district?province_id=" + provinceId,
                    HttpMethod.GET,
                    request,
                    String.class
            );

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            if (jsonNode.has("data") && hasItems(jsonNode.get("data"))) {
                return jsonNode.get("data");
            }

        } catch (Exception e) {
            System.err.println("Failed to get districts: " + e.getMessage());
        }

        return getOpenApiDistricts(provinceId);
    }

    /**
     * Gets wards by district
     * @param districtId District ID
     * @return List of wards
     */
    public JsonNode getWards(String districtId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Token", ghnToken);

            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    ghnApiUrl + "/master-data/ward?district_id=" + districtId,
                    HttpMethod.GET,
                    request,
                    String.class
            );

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            if (jsonNode.has("data") && hasItems(jsonNode.get("data"))) {
                return jsonNode.get("data");
            }

        } catch (Exception e) {
            System.err.println("Failed to get wards: " + e.getMessage());
        }

        return getOpenApiWards(districtId);
    }

    private boolean hasItems(JsonNode node) {
        return node != null && node.isArray() && node.size() > 0;
    }

    private JsonNode getOpenApiProvinces() {
        ArrayNode result = objectMapper.createArrayNode();
        try {
            String response = restTemplate.getForObject(VIETNAM_PROVINCES_API + "/?depth=1", String.class);
            JsonNode provinces = objectMapper.readTree(response);
            if (!provinces.isArray()) {
                return result;
            }

            for (JsonNode province : provinces) {
                ObjectNode item = result.addObject();
                item.put("ProvinceID", province.path("code").asText());
                item.put("ProvinceName", province.path("name").asText());
            }
        } catch (Exception e) {
            System.err.println("Failed to get fallback provinces: " + e.getMessage());
        }
        return result;
    }

    private JsonNode getOpenApiDistricts(String provinceId) {
        ArrayNode result = objectMapper.createArrayNode();
        try {
            String response = restTemplate.getForObject(
                    VIETNAM_PROVINCES_API + "/p/" + provinceId + "?depth=2",
                    String.class);
            JsonNode districts = objectMapper.readTree(response).path("districts");
            if (!districts.isArray()) {
                return result;
            }

            for (JsonNode district : districts) {
                ObjectNode item = result.addObject();
                item.put("DistrictID", district.path("code").asText());
                item.put("DistrictName", district.path("name").asText());
                item.put("ProvinceID", district.path("province_code").asText(provinceId));
            }
        } catch (Exception e) {
            System.err.println("Failed to get fallback districts: " + e.getMessage());
        }
        return result;
    }

    private JsonNode getOpenApiWards(String districtId) {
        ArrayNode result = objectMapper.createArrayNode();
        try {
            String response = restTemplate.getForObject(
                    VIETNAM_PROVINCES_API + "/d/" + districtId + "?depth=2",
                    String.class);
            JsonNode wards = objectMapper.readTree(response).path("wards");
            if (!wards.isArray()) {
                return result;
            }

            for (JsonNode ward : wards) {
                ObjectNode item = result.addObject();
                item.put("WardCode", ward.path("code").asText());
                item.put("WardName", ward.path("name").asText());
                item.put("DistrictID", ward.path("district_code").asText(districtId));
            }
        } catch (Exception e) {
            System.err.println("Failed to get fallback wards: " + e.getMessage());
        }
        return result;
    }

    /**
     * Default shipping fee calculation (fallback)
     */
    private long calculateDefaultShippingFee(String districtId, int weight) {
        // Base fee: 30,000 VND for first 500g
        long baseFee = 30000;

        // Additional fee per 500g: 10,000 VND
        int additionalWeight = Math.max(0, weight - 500);
        long additionalFee = (additionalWeight / 500 + 1) * 10000;

        // Distance fee (simplified - in reality would use actual district)
        long distanceFee = 10000;

        return baseFee + additionalFee + distanceFee;
    }
}
