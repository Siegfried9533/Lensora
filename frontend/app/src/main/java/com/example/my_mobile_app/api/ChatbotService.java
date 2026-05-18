package com.example.my_mobile_app.api;

import com.example.my_mobile_app.model.ChatMessage;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Non-streaming chatbot endpoint (chatbotApi.ts -> chatNonStream).
 * Streaming uses raw OkHttp in Phase 7 — not declared here.
 */
public interface ChatbotService {

    /** Body: {messages: List<ChatMessage>}. Returns string in data field. */
    @POST("chatbot/chat-sync")
    Call<ApiResponse<String>> chatNonStream(@Body Map<String, List<ChatMessage>> body);
}
