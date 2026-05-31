package com.camerashop.dto.chatbot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeepSeekChatResponse {
    private String id;
    private String object;
    private long created;
    private String model;
    private List<DeepSeekChoice> choices;
}
