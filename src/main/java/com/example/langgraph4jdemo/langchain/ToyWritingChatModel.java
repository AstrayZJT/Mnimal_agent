package com.example.langgraph4jdemo.langchain;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.FinishReason;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ToyWritingChatModel implements ChatModel {

    @Override
    public ChatResponse doChat(ChatRequest request) {
        String prompt = lastUserMessageText(request.messages());
        String answer;

        if (prompt.startsWith("Write a first draft about")) {
            String topic = extractQuotedTail(prompt, "Write a first draft about");
            answer = "Draft about " + topic + ": concise overview and key points.";
        } else if (prompt.startsWith("Polish this draft:")) {
            String draft = prompt.substring("Polish this draft:".length()).trim();
            answer = "Polished version: " + draft.replace("Draft about ", "");
        } else {
            answer = "Toy model reply: " + prompt;
        }

        return ChatResponse.builder()
                .aiMessage(AiMessage.from(answer))
                .finishReason(FinishReason.STOP)
                .build();
    }

    private String lastUserMessageText(List<ChatMessage> messages) {
        return UserMessage.findLast(messages)
                .map(UserMessage::singleText)
                .orElse("");
    }

    private String extractQuotedTail(String prompt, String prefix) {
        String tail = prompt.substring(prefix.length()).trim();
        if (tail.endsWith(".")) {
            tail = tail.substring(0, tail.length() - 1);
        }
        return tail;
    }
}
