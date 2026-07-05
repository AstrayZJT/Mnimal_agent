package com.example.langgraph4jdemo.generation;

public record GenerationResponse(
        Long id,
        String finalText,
        String createdAt,
        String downloadUrl
) {
}
