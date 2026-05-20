package com.example.my_mobile_app.model;

/** Mirrors {@code ChatMessage} in chatbotApi.ts. */
public class ChatMessage {
    /** "system" | "user" | "assistant". */
    public String role;
    public String content;

    public ChatMessage() {}

    public ChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }
}
