package com.example.langgraph4jdemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "langchain4j.open-ai.chat-model")
public record OpenAiChatModelProperties(
        String baseUrl,
        String apiKey,
        String modelName,
        Boolean logRequests,
        Boolean logResponses
) {

    public OpenAiChatModelProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
        }
        if (modelName == null || modelName.isBlank()) {
            modelName = "qwen-plus";
        }
        if (logRequests == null) {
            logRequests = Boolean.TRUE;
        }
        if (logResponses == null) {
            logResponses = Boolean.TRUE;
        }
    }
}
