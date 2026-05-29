package com.camerashop.dto.chatbot;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDTO {
    private String role;
    @NotBlank(message = "Nội dung tin nhắn không được để trống")
    private String content;
}
