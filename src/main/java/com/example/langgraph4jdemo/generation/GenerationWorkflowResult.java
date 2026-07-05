package com.example.langgraph4jdemo.generation;

public record GenerationWorkflowResult(
        String threadId,
        String draftText,
        String finalText,
        Integer qualityScore,
        Integer revisionCount,
        String feedback,
        String revisionAdvice,
        String traceLog
) {
}
