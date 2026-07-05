package com.example.langgraph4jdemo.generation;

public record GenerationSummary(
        Long id,
        String topic,
        String tone,
        String createdAt,
        String preview,
        String downloadUrl
) {
}
