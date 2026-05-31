package com.camerashop.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class GHNService {

    private static final String VIETNAM_PROVINCES_API = "https://provinces.open-api.vn/api/v1";

    @Value("${app.ghn.api-url}")
    private String ghnApiUrl;

    @Value("${app.ghn.shop-id}")
    private String shopId;

    @Value("${app.ghn.token}")
    private String ghnToken;

    @Value("${app.ghn.district-id}")
    private String defaultDistrictId;

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
     * Creates a shipping order with GHN
     * @param orderCode Your order code
     * @param toName Recipient name
     * @param toPhone Recipient phone
     * @param toAddress Recipient address
     * @param toWard Ward code
     * @param toDistrict District ID
     * @param weight Package weight
     * @param value Order value
     * @return GHN order ID
     */
    public String createShippingOrder(String orderCode, String toName, String toPhone,
                                       String toAddress, String toWard, String toDistrict,
                                       int weight, long value) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Token", ghnToken);
            headers.set("ShopId", shopId);

            Map<String, Object> body = new HashMap<>();
            body.put("payment_type_id", 2); // Recipient pays
            body.put("required_note", "Cho liên hệ trước khi giao");
            body.put("to_name", toName);
            body.put("to_phone", toPhone);
            body.put("to_address", toAddress);
            body.put("to_ward_code", toWard);
            body.put("to_district_id", Integer.parseInt(toDistrict));
            body.put("height", 10);
            body.put("length", 20);
            body.put("width", 15);
            body.put("weight", weight);
            body.put("insurance_value", value);
            body.put("fee_cod", value); // COD amount
            body.put("service_type_id", 2);
            body.put("client_order_code", orderCode);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    ghnApiUrl + "/shipping-order",
                    HttpMethod.POST,
                    request,
                    String.class
            );

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            if (jsonNode.has("data") && jsonNode.get("data").has("order_code")) {
                return jsonNode.get("data").get("order_code").asText();
            }

        } catch (Exception e) {
            // Log error but don't fail the order
            System.err.println("Failed to create GHN shipping order: " + e.getMessage());
        }

        return null;
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
