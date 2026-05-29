package com.camerashop.service;

import com.camerashop.dto.chatbot.*;
import com.camerashop.dto.CategoryDTO;
import com.camerashop.dto.ProductDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    @Value("${deepseek.base-url:https://api.deepseek.com}")
    private String deepseekBaseUrl;

    @Value("${deepseek.model:deepseek-chat}")
    private String deepseekModel;

    @Value("${deepseek.api-key:}")
    private String deepseekApiKey;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private HttpURLConnection openDeepSeekConnection(int connectTimeout, int readTimeout) throws Exception {
        URL url = new URL(deepseekBaseUrl + "/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + deepseekApiKey);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setConnectTimeout(connectTimeout);
        conn.setReadTimeout(readTimeout);
        return conn;
    }

    private String cachedSystemPrompt = null;
    private long lastPromptUpdate = 0;
    private static final long CACHE_TTL_MS = 60000;

    public String buildSystemPrompt() {
        long now = System.currentTimeMillis();
        if (cachedSystemPrompt != null && (now - lastPromptUpdate) < CACHE_TTL_MS) {
            return cachedSystemPrompt;
        }

        try {
            List<CategoryDTO> categories = categoryService.getAllCategories();
            List<ProductDTO> products = productService.getAllProducts(PageRequest.of(0, 10)).getContent();

            StringBuilder sb = new StringBuilder();
            sb.append("Bạn là trợ lý ảo của CameraShop - cửa hàng thiết bị camera hàng đầu. Nhiệm vụ của bạn là tư vấn nhanh, gọn, lẹ cho khách hàng bằng tiếng Việt đơn giản, dễ hiểu.\n\n");

            sb.append("THÔNG TIN HỆ THỐNG:\n");

            sb.append("- Danh mục: ");
            sb.append(categories.stream()
                    .map(CategoryDTO::getCategoryName)
                    .collect(Collectors.joining(", ")));
            sb.append("\n");

            sb.append("- Sản phẩm nổi bật:\n");
            for (ProductDTO p : products) {
                sb.append(String.format("  + %s (%s) - %,dđ - còn %d chiếc\n",
                        p.getProductName(), p.getBrand(), p.getPrice(), p.getStockQuantity()));
            }

            sb.append("\nDỊCH VỤ:\n");
            sb.append("- Mua thiết bị mới/đã qua sử dụng\n");
            sb.append("- Thuê thiết bị theo ngày\n");

            sb.append("\nCHÍNH SÁCH:\n");
            sb.append("- Bảo hành 12-24 tháng\n");
            sb.append("- Đổi trả trong 7 ngày\n");
            sb.append("- Giao hàng 2-5 ngày, miễn phí đơn >5tr\n");
            sb.append("- Thanh toán: MoMo, COD, trả góp 0% đơn >10tr\n");

            sb.append("\nQUY TẮC TRẢ LỜI:\n");
            sb.append("1. Ngắn gọn, tối đa 2-3 câu\n");
            sb.append("2. Dùng từ ngữ đơn giản, dễ hiểu\n");
            sb.append("3. Ưu tiên tư vấn sản phẩm và dịch vụ của shop\n");
            sb.append("4. Nếu không biết, hướng dẫn khách liên hệ hotline\n");
            sb.append("5. Luôn lịch sự, thân thiện\n");

            cachedSystemPrompt = sb.toString();
            lastPromptUpdate = now;
            return cachedSystemPrompt;
        } catch (Exception e) {
            return buildDefaultSystemPrompt();
        }
    }

    private String buildDefaultSystemPrompt() {
        return "Bạn là trợ lý ảo của CameraShop. Trả lời ngắn gọn, tối đa 2-3 câu, bằng tiếng Việt đơn giản, dễ hiểu.\n\n" +
               "Dịch vụ: mua và thuê thiết bị camera.\n" +
               "Chính sách: bảo hành 12-24 tháng, đổi trả 7 ngày, giao hàng 2-5 ngày, miễn phí ship đơn >5tr.\n" +
               "Thanh toán: MoMo, COD, trả góp 0% đơn >10tr.";
    }

    private List<DeepSeekMessage> buildMessages(ChatRequest request) {
        String systemPrompt = buildSystemPrompt();

        List<DeepSeekMessage> messages = new ArrayList<>();
        messages.add(DeepSeekMessage.builder().role("system").content(systemPrompt).build());

        if (request.getMessages() != null) {
            for (ChatMessageDTO msg : request.getMessages()) {
                messages.add(DeepSeekMessage.builder()
                        .role(msg.getRole())
                        .content(msg.getContent())
                        .build());
            }
        }

        return messages;
    }

    public void streamChat(ChatRequest request, OutputStream outputStream) throws Exception {
        List<DeepSeekMessage> messages = buildMessages(request);

        DeepSeekChatRequest deepseekRequest = DeepSeekChatRequest.builder()
                .model(deepseekModel)
                .messages(messages)
                .stream(true)
                .temperature(0.7)
                .max_tokens(1024)
                .build();

        HttpURLConnection conn = openDeepSeekConnection(10000, 120000);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(objectMapper.writeValueAsBytes(deepseekRequest));
            os.flush();
        }

        int status = conn.getResponseCode();
        if (status >= 400) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                String err = br.lines().collect(Collectors.joining("\n"));
                throw new RuntimeException("Lỗi DeepSeek (" + status + "): " + err);
            }
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
             OutputStream out = outputStream) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                if (!line.startsWith("data: ")) continue;

                String data = line.substring(6).trim();
                if ("[DONE]".equals(data)) break;

                try {
                    DeepSeekChatResponse chunk = objectMapper.readValue(data, DeepSeekChatResponse.class);
                    if (chunk.getChoices() != null && !chunk.getChoices().isEmpty()) {
                        DeepSeekChoice choice = chunk.getChoices().get(0);
                        if (choice.getDelta() != null && choice.getDelta().getContent() != null) {
                            out.write(choice.getDelta().getContent().getBytes(StandardCharsets.UTF_8));
                            out.flush();
                        }
                    }
                } catch (Exception e) {
                    // Bo qua dong bi loi dinh dang
                }
            }
        }
    }

    public String chatNonStream(ChatRequest request) throws Exception {
        List<DeepSeekMessage> messages = buildMessages(request);

        DeepSeekChatRequest deepseekRequest = DeepSeekChatRequest.builder()
                .model(deepseekModel)
                .messages(messages)
                .stream(false)
                .temperature(0.7)
                .max_tokens(1024)
                .build();

        HttpURLConnection conn = openDeepSeekConnection(10000, 60000);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(objectMapper.writeValueAsBytes(deepseekRequest));
            os.flush();
        }

        int status = conn.getResponseCode();
        if (status >= 400) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                String err = br.lines().collect(Collectors.joining("\n"));
                throw new RuntimeException("Lỗi DeepSeek (" + status + "): " + err);
            }
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String responseBody = reader.lines().collect(Collectors.joining("\n"));
            DeepSeekChatResponse response = objectMapper.readValue(responseBody, DeepSeekChatResponse.class);
            if (response.getChoices() != null && !response.getChoices().isEmpty()) {
                DeepSeekMessage message = response.getChoices().get(0).getMessage();
                return message != null ? message.getContent() : "";
            }
            return "";
        }
    }
}
