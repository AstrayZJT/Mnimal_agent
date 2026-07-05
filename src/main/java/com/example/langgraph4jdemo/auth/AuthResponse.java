package com.example.langgraph4jdemo.auth;

public record AuthResponse(
        Long id,
        String username,
        String displayName
) {
    public static AuthResponse from(AppUser user) {
        return new AuthResponse(user.getId(), user.getUsername(), user.getDisplayName());
    }
}
