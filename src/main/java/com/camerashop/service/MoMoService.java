package com.camerashop.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * MoMo Payment Service (API v2)
 * Documentation: https://developers.momo.vn/v2/#/
 */
@Service
public class MoMoService {

    @Value("${app.momo.partner-code}")
    private String partnerCode;

    @Value("${app.momo.access-key}")
    private String accessKey;

    @Value("${app.momo.secret-key}")
    private String secretKey;

    @Value("${app.momo.url}")
    private String momoUrl;

    @Value("${app.momo.redirect-url}")
    private String redirectUrl;

    @Value("${app.momo.ipn-url}")
    private String ipnUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Request types for MoMo payment
     */
    public enum RequestType {
        CAPTURE_WALLET("captureWallet"),
        PAY_WITH_METHOD("payWithMethod"),
        LINK_AND_PAY("linkAndPay"),
        LINK_AND_PAY_WITH_TOKEN("linkAndPayWithToken");

        private final String value;

        RequestType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * Create MoMo payment URL
     *
     * @param orderId Unique order ID from your system
     * @param amount Amount in VND
     * @param orderInfo Order description
     * @param requestType Payment method type
     * @return MoMo payment URL
     */
    public String createPaymentUrl(String orderId, long amount, String orderInfo, RequestType requestType) {
        try {
            // Generate unique request ID
            String requestId = orderId + "_" + System.currentTimeMillis();

            // Extra data (can be used to store additional info)
            String extraData = "";

            // Build request body for MoMo API
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("partnerCode", partnerCode);
            requestBody.put("accessKey", accessKey);
            requestBody.put("requestId", requestId);
            requestBody.put("amount", amount);
            requestBody.put("orderId", orderId);
            requestBody.put("orderInfo", orderInfo);
            requestBody.put("redirectUrl", redirectUrl);
            requestBody.put("ipnUrl", ipnUrl);
            requestBody.put("extraData", extraData);
            requestBody.put("requestType", requestType.getValue());

            // Generate signature BEFORE adding signature to request
            String signature = generateSignature(requestBody, secretKey);
            requestBody.put("signature", signature);

            // Send request to MoMo API
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    momoUrl + "/v2/gateway/api/create",
                    HttpMethod.POST,
                    request,
                    String.class
            );

            // Parse response
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());

            // Check if successful
            if (jsonResponse.has("payUrl")) {
                return jsonResponse.get("payUrl").asText();
            } else {
                String message = jsonResponse.has("message") ? jsonResponse.get("message").asText() : "Unknown error";
                throw new RuntimeException("MoMo API error: " + message);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to create MoMo payment URL: " + e.getMessage(), e);
        }
    }

    /**
     * Create MoMo payment URL with default CAPTURE_WALLET type
     */
    public String createPaymentUrl(String orderId, long amount, String orderInfo) {
        return createPaymentUrl(orderId, amount, orderInfo, RequestType.CAPTURE_WALLET);
    }

    /**
     * Validate IPN callback from MoMo
     *
     * @param params Parameters from MoMo IPN callback
     * @return true if signature is valid
     */
    public boolean validateIPNCallback(Map<String, String> params) {
        try {
            String receivedSignature = params.get("signature");

            // Build data for signature verification (EXACT order as per MoMo docs)
            Map<String, String> signatureData = new HashMap<>();
            signatureData.put("partnerCode", params.get("partnerCode"));
            signatureData.put("orderId", params.get("orderId"));
            signatureData.put("requestId", params.get("requestId"));
            signatureData.put("amount", params.get("amount"));
            signatureData.put("orderInfo", params.get("orderInfo"));
            signatureData.put("orderType", params.get("orderType"));
            signatureData.put("transId", params.get("transId"));
            signatureData.put("message", params.get("message"));
            signatureData.put("responseTime", params.get("responseTime"));
            signatureData.put("errorCode", params.get("errorCode"));
            signatureData.put("localMessage", params.get("localMessage"));

            // Build raw signature string
            StringBuilder rawData = new StringBuilder();
            rawData.append("partnerCode=").append(params.get("partnerCode"));
            rawData.append("&orderId=").append(params.get("orderId"));
            rawData.append("&requestId=").append(params.get("requestId"));
            rawData.append("&amount=").append(params.get("amount"));
            rawData.append("&orderInfo=").append(params.get("orderInfo"));
            rawData.append("&orderType=").append(params.get("orderType"));
            rawData.append("&transId=").append(params.get("transId"));
            rawData.append("&message=").append(params.get("message"));
            rawData.append("&responseTime=").append(params.get("responseTime"));
            rawData.append("&errorCode=").append(params.get("errorCode"));
            rawData.append("&localMessage=").append(params.get("localMessage"));

            // Calculate expected signature
            String expectedSignature = hmacSHA256(secretKey, rawData.toString());

            return expectedSignature.equals(receivedSignature);

        } catch (Exception e) {
            System.err.println("Error validating MoMo IPN: " + e.getMessage());
            return false;
        }
    }

    /**
     * Validate redirect callback from MoMo
     *
     * @param params Parameters from redirect callback
     * @return true if signature is valid
     */
    public boolean validateRedirectCallback(Map<String, String> params) {
        try {
            String receivedSignature = params.get("signature");

            // Build raw signature string for redirect (different from IPN)
            StringBuilder rawData = new StringBuilder();
            rawData.append("partnerCode=").append(params.get("partnerCode"));
            rawData.append("&orderId=").append(params.get("orderId"));
            rawData.append("&requestId=").append(params.get("requestId"));
            rawData.append("&amount=").append(params.get("amount"));
            rawData.append("&orderInfo=").append(params.get("orderInfo"));
            rawData.append("&orderType=").append(params.get("orderType"));
            rawData.append("&transId=").append(params.get("transId"));
            rawData.append("&message=").append(params.get("message"));
            rawData.append("&responseTime=").append(params.get("responseTime"));
            rawData.append("&errorCode=").append(params.get("errorCode"));
            rawData.append("&localMessage=").append(params.get("localMessage"));

            // Calculate expected signature
            String expectedSignature = hmacSHA256(secretKey, rawData.toString());

            return expectedSignature.equals(receivedSignature);

        } catch (Exception e) {
            System.err.println("Error validating MoMo redirect: " + e.getMessage());
            return false;
        }
    }

    /**
     * Query MoMo transaction status
     *
     * @param orderId Order ID to query
     * @param requestId Unique request ID
     * @return Transaction status response
     */
    public Map<String, Object> queryTransaction(String orderId, String requestId) {
        try {
            // Build request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("partnerCode", partnerCode);
            requestBody.put("accessKey", accessKey);
            requestBody.put("requestId", requestId);
            requestBody.put("orderId", orderId);
            requestBody.put("lang", "vi");

            // Generate signature
            String rawData = "partnerCode=" + partnerCode +
                           "&accessKey=" + accessKey +
                           "&requestId=" + requestId +
                           "&orderId=" + orderId;
            String signature = hmacSHA256(secretKey, rawData);
            requestBody.put("signature", signature);

            // Send request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    momoUrl + "/v2/gateway/api/query",
                    HttpMethod.POST,
                    request,
                    String.class
            );

            // Parse response
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());

            Map<String, Object> result = new HashMap<>();
            result.put("success", jsonResponse.get("errorCode").asInt() == 0);
            result.put("message", jsonResponse.has("message") ? jsonResponse.get("message").asText() : "");
            result.put("transId", jsonResponse.has("transId") ? jsonResponse.get("transId").asText() : "");
            result.put("amount", jsonResponse.has("amount") ? jsonResponse.get("amount").asLong() : 0);

            return result;

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return error;
        }
    }

    /**
     * Generate HMAC SHA256 signature
     * IMPORTANT: Must use exact parameter order as specified in MoMo docs
     */
    private String generateSignature(Map<String, Object> requestBody, String secretKey) {
        try {
            // Build raw signature string in EXACT order as MoMo docs
            // Order: partnerCode|accessKey|requestId|amount|orderId|orderInfo|redirectUrl|ipnUrl|extraData
            String rawData = "partnerCode=" + requestBody.get("partnerCode") +
                           "&accessKey=" + requestBody.get("accessKey") +
                           "&requestId=" + requestBody.get("requestId") +
                           "&amount=" + requestBody.get("amount") +
                           "&orderId=" + requestBody.get("orderId") +
                           "&orderInfo=" + requestBody.get("orderInfo") +
                           "&redirectUrl=" + requestBody.get("redirectUrl") +
                           "&ipnUrl=" + requestBody.get("ipnUrl") +
                           "&extraData=" + requestBody.get("extraData");

            return hmacSHA256(secretKey, rawData);

        } catch (Exception e) {
            throw new RuntimeException("Error generating signature", e);
        }
    }

    /**
     * Calculate HMAC SHA256
     */
    private String hmacSHA256(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (Exception e) {
            throw new RuntimeException("Error calculating HMAC SHA256", e);
        }
    }
}
