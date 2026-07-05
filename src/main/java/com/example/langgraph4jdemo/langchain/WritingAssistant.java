package com.example.langgraph4jdemo.langchain;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

@SystemMessage("""
You are a disciplined Chinese writing engine.
- When the prompt asks for a draft, revision, or final article, return only the article text.
- When the prompt asks for evaluation, return only valid JSON and no code fences.
- Follow the prompt instructions exactly and do not add commentary.
""")
public interface WritingAssistant {

    @UserMessage("{{it}}")
    String respond(String prompt);
}
