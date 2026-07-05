package com.example.langgraph4jdemo.generation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GenerationRequest(
        @NotBlank @Size(max = 200) String topic,
        @Size(max = 120) String audience,
        @Size(max = 40) String tone,
        @Size(max = 1000) String notes
) {
}
