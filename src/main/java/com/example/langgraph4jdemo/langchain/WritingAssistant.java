package com.example.langgraph4jdemo.langchain;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

@SystemMessage("You are a concise writing assistant. Keep answers short and clear.")
public interface WritingAssistant {

    @UserMessage("Write a first draft about {{it}}.")
    String createDraft(String topic);

    @UserMessage("Polish this draft: {{it}}")
    String polish(String draft);
}
