package com.example.langgraph4jdemo.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String root(HttpSession session) {
        return isLoggedIn(session) ? "redirect:/app" : "redirect:/login";
    }

    @GetMapping("/login")
    public String login(HttpSession session) {
        return isLoggedIn(session) ? "redirect:/app" : "forward:/login.html";
    }

    @GetMapping("/register")
    public String register(HttpSession session) {
        return isLoggedIn(session) ? "redirect:/app" : "forward:/register.html";
    }

    @GetMapping("/app")
    public String app(HttpSession session) {
        return isLoggedIn(session) ? "forward:/app.html" : "redirect:/login";
    }

    private boolean isLoggedIn(HttpSession session) {
        return session != null && session.getAttribute(SessionKeys.CURRENT_USER_ID) != null;
    }
}
