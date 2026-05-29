package com.camerashop.dto.chatbot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeepSeekChoice {
    private int index;
    private DeepSeekMessage message;
    private DeepSeekMessage delta;
    private String finish_reason;
}
