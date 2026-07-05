package com.example.langgraph4jdemo.auth;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserAccountService userAccountService;
    private final CurrentUserService currentUserService;

    public AuthController(UserAccountService userAccountService, CurrentUserService currentUserService) {
        this.userAccountService = userAccountService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody AuthRequest request, HttpSession session) {
        AppUser user = userAccountService.register(request.username(), request.password(), request.displayName());
        currentUserService.login(session, user);
        return AuthResponse.from(user);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request, HttpSession session) {
        AppUser user = userAccountService.authenticate(request.username(), request.password());
        currentUserService.login(session, user);
        return AuthResponse.from(user);
    }

    @PostMapping("/logout")
    public void logout(HttpSession session) {
        currentUserService.logout(session);
    }

    @GetMapping("/me")
    public AuthResponse me(HttpSession session) {
        return currentUserService.findCurrentUser(session)
                .map(AuthResponse::from)
                .orElse(null);
    }
}
