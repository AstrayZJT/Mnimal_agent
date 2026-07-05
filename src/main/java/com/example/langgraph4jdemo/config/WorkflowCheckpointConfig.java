package com.example.langgraph4jdemo.config;

import com.example.langgraph4jdemo.generation.GenerationWorkflowState;
import org.bsc.langgraph4j.checkpoint.BaseCheckpointSaver;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.bsc.langgraph4j.checkpoint.PostgresSaver;
import org.bsc.langgraph4j.serializer.std.ObjectStreamStateSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

@Configuration
public class WorkflowCheckpointConfig {

    @Bean
    public ObjectStreamStateSerializer<GenerationWorkflowState> generationWorkflowStateSerializer() {
        return new ObjectStreamStateSerializer<>(GenerationWorkflowState::new);
    }

    @Bean
    public BaseCheckpointSaver generationCheckpointSaver(Environment environment,
                                                         ObjectStreamStateSerializer<GenerationWorkflowState> serializer) throws java.sql.SQLException {
        String datasourceUrl = environment.getProperty("spring.datasource.url", "");
        if (datasourceUrl.startsWith("jdbc:postgresql")) {
            String host = environment.getProperty("DB_HOST", "localhost");
            int port = parseInt(environment.getProperty("DB_PORT"), 5432);
            String database = environment.getProperty("DB_NAME", "agentdemo");
            String username = environment.getProperty("DB_USERNAME", "postgres");
            String password = environment.getProperty("DB_PASSWORD", "");

            if (!StringUtils.hasText(password)) {
                password = "";
            }

            return PostgresSaver.builder()
                    .host(host)
                    .port(port)
                    .database(database)
                    .user(username)
                    .password(password)
                    .stateSerializer(serializer)
                    .createTables(true)
                    .build();
        }

        return new MemorySaver();
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
