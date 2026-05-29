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
 * Dich vu Thanh toan MoMo (API v2)
 * Tai lieu: https://developers.momo.vn/v2/#/
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
     * Cac loai yeu cau thanh toan MoMo
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
     * Tao URL thanh toan MoMo
     *
     * @param orderId     Ma don hang duy nhat tu he thong cua ban
     * @param amount      So tien VND
     * @param orderInfo   Mo ta don hang
     * @param requestType Loai phuong thuc thanh toan
     * @return URL thanh toan MoMo
     */
    public String createPaymentUrl(String orderId, long amount, String orderInfo, RequestType requestType) {
        try {
            validateMerchantConfig();

            // Tao ma yeu cau duy nhat
            String requestId = orderId + "_" + System.currentTimeMillis();

            // Du lieu bo sung (co the dung de luu thong tin them)
            String extraData = "";

            // Xay dung noi dung yeu cau cho API MoMo
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

            // Tao chu ky TRUOC khi them chu ky vao yeu cau
            String signature = generateSignature(requestBody, secretKey);
            requestBody.put("signature", signature);

            // Gui yeu cau den API MoMo
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    momoUrl + "/v2/gateway/api/create",
                    HttpMethod.POST,
                    request,
                    String.class);

            // Phan tich phan hoi
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());

            if (jsonResponse.has("resultCode") && jsonResponse.get("resultCode").asInt() != 0) {
                int resultCode = jsonResponse.get("resultCode").asInt();
                String message = jsonResponse.has("message") ? jsonResponse.get("message").asText() : "Unknown error";
                System.err.println("LENSORA_MOMO_CREATE_FAILED: resultCode=" + resultCode
                        + ", message=" + message
                        + ", partnerCode=" + partnerCode
                        + ", requestType=" + requestType.getValue()
                        + ", redirectUrl=" + redirectUrl
                        + ", ipnUrl=" + ipnUrl);
                throw new RuntimeException("MoMo trả về resultCode=" + resultCode + ": " + message);
            }

            // Kiem tra thanh cong
            if (jsonResponse.has("payUrl")) {
                return jsonResponse.get("payUrl").asText();
            } else {
                String message = jsonResponse.has("message") ? jsonResponse.get("message").asText() : "Unknown error";
                throw new RuntimeException("Lỗi API MoMo: " + message);
            }

        } catch (Exception e) {
            throw new RuntimeException("Tạo URL thanh toán MoMo thất bại: " + e.getMessage(), e);
        }
    }

    /**
     * Tao URL thanh toan MoMo voi loai mac dinh CAPTURE_WALLET
     */
    public String createPaymentUrl(String orderId, long amount, String orderInfo) {
        return createPaymentUrl(orderId, amount, orderInfo, RequestType.CAPTURE_WALLET);
    }

    /**
     * Xac thuc goi lai IPN tu MoMo
     *
     * @param params Tham so tu goi lai IPN cua MoMo
     * @return true neu chu ky hop le
     */
    // public boolean validateIPNCallback(Map<String, String> params) {
    // try {
    // String receivedSignature = params.get("signature");

    // // Xay dung du lieu xac thuc chu ky (DUNG thu tu theo tai lieu MoMo)
    // Map<String, String> signatureData = new HashMap<>();
    // signatureData.put("partnerCode", params.get("partnerCode"));
    // signatureData.put("orderId", params.get("orderId"));
    // signatureData.put("requestId", params.get("requestId"));
    // signatureData.put("amount", params.get("amount"));
    // signatureData.put("orderInfo", params.get("orderInfo"));
    // signatureData.put("orderType", params.get("orderType"));
    // signatureData.put("transId", params.get("transId"));
    // signatureData.put("message", params.get("message"));
    // signatureData.put("responseTime", params.get("responseTime"));
    // signatureData.put("errorCode", params.get("errorCode"));
    // signatureData.put("localMessage", params.get("localMessage"));

    // // Xay dung chuoi chu ky tho
    // StringBuilder rawData = new StringBuilder();
    // rawData.append("partnerCode=").append(params.get("partnerCode"));
    // rawData.append("&orderId=").append(params.get("orderId"));
    // rawData.append("&requestId=").append(params.get("requestId"));
    // rawData.append("&amount=").append(params.get("amount"));
    // rawData.append("&orderInfo=").append(params.get("orderInfo"));
    // rawData.append("&orderType=").append(params.get("orderType"));
    // rawData.append("&transId=").append(params.get("transId"));
    // rawData.append("&message=").append(params.get("message"));
    // rawData.append("&responseTime=").append(params.get("responseTime"));
    // rawData.append("&errorCode=").append(params.get("errorCode"));
    // rawData.append("&localMessage=").append(params.get("localMessage"));

    // // Tinh chu ky mong doi
    // String expectedSignature = hmacSHA256(secretKey, rawData.toString());

    // return expectedSignature.equals(receivedSignature);

    // } catch (Exception e) {
    // System.err.println("Error validating MoMo IPN: " + e.getMessage());
    // return false;
    // }
    // }

    public boolean validateIPNCallback(Map<String, String> params) {
        try {
            String receivedSignature = params.get("signature");
            if (receivedSignature == null || receivedSignature.isEmpty()) {
                System.err.println("LENSORA_IPN_ERROR: Không tìm thấy signature trong callback của MoMo");
                return false;
            }

            // Dùng getOrDefault để tránh tối đa lỗi NullPointerException nếu MoMo trả thiếu
            // trường
            String amount = params.getOrDefault("amount", "");
            String extraData = params.getOrDefault("extraData", "");
            String message = params.getOrDefault("message", "");
            String orderId = params.getOrDefault("orderId", "");
            String orderInfo = params.getOrDefault("orderInfo", "");
            String orderType = params.getOrDefault("orderType", "");
            String partnerCode = params.getOrDefault("partnerCode", "");
            String payType = params.getOrDefault("payType", "");
            String requestId = params.getOrDefault("requestId", "");
            String responseTime = params.getOrDefault("responseTime", "");
            String resultCode = params.getOrDefault("resultCode", "");
            String transId = params.getOrDefault("transId", "");

            // ĐÚNG QUY TẮC: Ghép chuỗi theo đúng thứ tự bảng chữ cái Alphabet từ A đến Z
            // của Key
            String rawData = "accessKey=" + accessKey +
                    "&amount=" + amount +
                    "&extraData=" + extraData +
                    "&message=" + message +
                    "&orderId=" + orderId +
                    "&orderInfo=" + orderInfo +
                    "&orderType=" + orderType +
                    "&partnerCode=" + partnerCode +
                    "&payType=" + payType +
                    "&requestId=" + requestId +
                    "&responseTime=" + responseTime +
                    "&resultCode=" + resultCode +
                    "&transId=" + transId;

            // In ra Console để bác dễ so sánh chuỗi thô khi debug dòng tiền
            System.out.println("LENSORA_IPN_RAW_DATA: " + rawData);

            // Tính toán chữ ký mong đợi dựa trên SecretKey chung của hệ thống
            String expectedSignature = hmacSHA256(secretKey, rawData);

            // So sánh khớp chữ ký
            boolean isValid = expectedSignature.equalsIgnoreCase(receivedSignature.trim());

            // Log chi tiết khi không khớp chữ ký để dễ debug
            if (!isValid) {
                System.err.println("LENSORA SIGNATURE MISMATCH (IPN) -> \n" +
                        "Raw Data: " + rawData + "\n" +
                        "Expected : " + expectedSignature + "\n" +
                        "Received : " + receivedSignature);
            }
            return isValid;

        } catch (Exception e) {
            System.err.println("Error validating MoMo IPN: " + e.getMessage());
            return false;
        }
    }

    /**
     * Xac thuc goi lai chuyen huong tu MoMo
     *
     * @param params Tham so tu goi lai chuyen huong
     * @return true neu chu ky hop le
     */
    public boolean validateRedirectCallback(Map<String, String> params) {
        try {
            String receivedSignature = params.get("signature");
            if (receivedSignature == null || receivedSignature.isBlank()) {
                System.err.println("LENSORA_REDIRECT_ERROR: Không tìm thấy signature trong callback của MoMo");
                return false;
            }

            // Xay dung chuoi chu ky tho cho chuyen huong (khac voi IPN)
            StringBuilder rawData = new StringBuilder();
            rawData.append("partnerCode=").append(params.getOrDefault("partnerCode", ""));
            rawData.append("&orderId=").append(params.getOrDefault("orderId", ""));
            rawData.append("&requestId=").append(params.getOrDefault("requestId", ""));
            rawData.append("&amount=").append(params.getOrDefault("amount", ""));
            rawData.append("&orderInfo=").append(params.getOrDefault("orderInfo", ""));
            rawData.append("&orderType=").append(params.getOrDefault("orderType", ""));
            rawData.append("&transId=").append(params.getOrDefault("transId", ""));
            rawData.append("&message=").append(params.getOrDefault("message", ""));
            rawData.append("&responseTime=").append(params.getOrDefault("responseTime", ""));
            rawData.append("&errorCode=")
                    .append(params.getOrDefault("errorCode", params.getOrDefault("resultCode", "")));
            rawData.append("&localMessage=").append(params.getOrDefault("localMessage", ""));

            // Tinh chu ky mong doi
            String expectedSignature = hmacSHA256(secretKey, rawData.toString());

            boolean isValid = expectedSignature.equalsIgnoreCase(receivedSignature.trim());
            if (!isValid) {
                System.err.println("LENSORA SIGNATURE MISMATCH (REDIRECT) -> \n"
                        + "Raw Data: " + rawData + "\n"
                        + "Expected : " + expectedSignature + "\n"
                        + "Received : " + receivedSignature);
            }
            return isValid;

        } catch (Exception e) {
            System.err.println("Error validating MoMo redirect: " + e.getMessage());
            return false;
        }
    }

    /**
     * Truy van trang thai giao dich MoMo
     *
     * @param orderId   Ma don hang can truy van
     * @param requestId Ma yeu cau duy nhat
     * @return Phan hoi trang thai giao dich
     */
    public Map<String, Object> queryTransaction(String orderId, String requestId) {
        try {
            // Xay dung noi dung yeu cau
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("partnerCode", partnerCode);
            requestBody.put("accessKey", accessKey);
            requestBody.put("requestId", requestId);
            requestBody.put("orderId", orderId);
            requestBody.put("lang", "vi");

            // Tao chu ky
            String rawData = "partnerCode=" + partnerCode +
                    "&accessKey=" + accessKey +
                    "&requestId=" + requestId +
                    "&orderId=" + orderId;
            String signature = hmacSHA256(secretKey, rawData);
            requestBody.put("signature", signature);

            // Gui yeu cau
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    momoUrl + "/v2/gateway/api/query",
                    HttpMethod.POST,
                    request,
                    String.class);

            // Phan tich phan hoi
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
     * Tao chu ky HMAC SHA256
     * QUAN TRONG: Phai dung dung thu tu tham so theo tai lieu MoMo
     */
    private String generateSignature(Map<String, Object> requestBody, String secretKey) {
        try {
            // Xay dung chuoi chu ky tho theo DUNG thu tu MoMo
            // Thu tu:
            // partnerCode|accessKey|requestId|amount|orderId|orderInfo|redirectUrl|ipnUrl|extraData
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
            throw new RuntimeException("Lỗi tạo chữ ký", e);
        }
    }

    /**
     * Tinh HMAC SHA256
     */
    private String hmacSHA256(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Chuyen sang chuoi hex
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (Exception e) {
            throw new RuntimeException("Lỗi tính HMAC SHA256", e);
        }
    }

    private void validateMerchantConfig() {
        if (partnerCode == null || partnerCode.isBlank()) {
            throw new IllegalStateException("MoMo partnerCode is missing");
        }
        if (accessKey == null || accessKey.isBlank()) {
            throw new IllegalStateException("MoMo accessKey is missing");
        }
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("MoMo secretKey is missing");
        }
        if (momoUrl == null || momoUrl.isBlank()) {
            throw new IllegalStateException("MoMo url is missing");
        }
    }
}
