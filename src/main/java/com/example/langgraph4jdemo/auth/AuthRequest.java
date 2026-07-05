package com.example.langgraph4jdemo.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRequest(
        @NotBlank @Size(max = 80) String username,
        @NotBlank @Size(min = 6, max = 72) String password,
        @Size(max = 120) String displayName
) {
}
