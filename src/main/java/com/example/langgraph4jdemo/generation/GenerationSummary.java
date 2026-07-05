package com.example.langgraph4jdemo.generation;

public record GenerationSummary(
        Long id,
        String createdAt,
        String preview,
        String downloadUrl
) {
}
