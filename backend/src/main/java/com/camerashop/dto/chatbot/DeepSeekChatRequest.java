package com.camerashop.dto.chatbot;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeepSeekChatRequest {
    private String model;
    private List<DeepSeekMessage> messages;
    private boolean stream;
    private Double temperature;
    @Builder.Default
    private Integer max_tokens = 1024;
}
