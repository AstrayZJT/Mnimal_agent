package com.example.langgraph4jdemo.langchain;

import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LangChainConfig {

    @Bean
    public WritingAssistant writingAssistant(ToyWritingChatModel toyWritingChatModel) {
        return AiServices.create(WritingAssistant.class, toyWritingChatModel);
    }
}
