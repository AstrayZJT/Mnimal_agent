package com.example.langgraph4jdemo.generation;

public record GenerationResponse(
        Long id,
        String topic,
        String audience,
        String tone,
        String notes,
        String draftText,
        String finalText,
        String archivePath,
        String createdAt,
        String downloadUrl
) {
}
