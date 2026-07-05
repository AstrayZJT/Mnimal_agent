package com.example.langgraph4jdemo.auth;

import com.example.langgraph4jdemo.web.SessionKeys;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CurrentUserService {

    private final UserAccountService userAccountService;

    public CurrentUserService(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    public Optional<AppUser> findCurrentUser(HttpSession session) {
        if (session == null) {
            return Optional.empty();
        }
        Object id = session.getAttribute(SessionKeys.CURRENT_USER_ID);
        if (id instanceof Long userId) {
            return Optional.of(userAccountService.requireById(userId));
        }
        return Optional.empty();
    }

    public AppUser requireCurrentUser(HttpSession session) {
        return findCurrentUser(session)
                .orElseThrow(() -> new AuthenticationFailedException("请先登录"));
    }

    public void login(HttpSession session, AppUser user) {
        session.setAttribute(SessionKeys.CURRENT_USER_ID, user.getId());
    }

    public void logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
    }
}
