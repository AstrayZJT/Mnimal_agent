package com.example.langgraph4jdemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(String generatedOutputDir, int historyLimit) {

    public AppProperties {
        if (generatedOutputDir == null || generatedOutputDir.isBlank()) {
            generatedOutputDir = "generated-output";
        }
        if (historyLimit <= 0) {
            historyLimit = 20;
        }
    }
}
