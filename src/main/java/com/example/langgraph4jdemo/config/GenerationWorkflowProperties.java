package com.example.langgraph4jdemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "agent.workflow")
public record GenerationWorkflowProperties(
        Integer scoreThreshold,
        Integer maxRevisionRounds
) {

    public GenerationWorkflowProperties {
        if (scoreThreshold == null || scoreThreshold <= 0) {
            scoreThreshold = 85;
        }
        if (maxRevisionRounds == null || maxRevisionRounds < 0) {
            maxRevisionRounds = 2;
        }
    }
}
