package com.example.langgraph4jdemo;

import com.example.langgraph4jdemo.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class LangGraph4jDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(LangGraph4jDemoApplication.class, args);
    }
}
