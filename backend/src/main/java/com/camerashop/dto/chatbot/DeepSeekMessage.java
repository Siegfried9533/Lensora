package com.camerashop.dto.chatbot;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeepSeekMessage {
    private String role;
    private String content;
}
