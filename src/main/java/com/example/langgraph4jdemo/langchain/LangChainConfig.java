package com.example.langgraph4jdemo.langchain;

import com.example.langgraph4jdemo.config.OpenAiChatModelProperties;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class LangChainConfig {

    @Bean
    public ChatModel chatModel(OpenAiChatModelProperties properties) {
        if (!StringUtils.hasText(properties.apiKey())) {
            throw new IllegalStateException("OPENAI_API_KEY is required for real model calls");
        }

        return OpenAiChatModel.builder()
                .baseUrl(properties.baseUrl())
                .apiKey(properties.apiKey())
                .modelName(properties.modelName())
                .logRequests(properties.logRequests())
                .logResponses(properties.logResponses())
                .build();
    }

    @Bean
    public WritingAssistant writingAssistant(ChatModel chatModel) {
        return AiServices.create(WritingAssistant.class, chatModel);
    }
}
