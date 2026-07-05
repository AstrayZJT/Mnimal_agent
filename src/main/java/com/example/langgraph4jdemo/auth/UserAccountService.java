package com.example.langgraph4jdemo.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserAccountService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAccountService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppUser register(String username, String password, String displayName) {
        String normalizedUsername = normalizeUsername(username);
        if (!StringUtils.hasText(normalizedUsername)) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (appUserRepository.existsByUsername(normalizedUsername)) {
            throw new UsernameAlreadyExistsException("用户名已存在");
        }

        AppUser user = new AppUser(
                normalizedUsername,
                passwordEncoder.encode(password),
                StringUtils.hasText(displayName) ? displayName.trim() : normalizedUsername
        );
        return appUserRepository.save(user);
    }

    @Transactional(readOnly = true)
    public AppUser authenticate(String username, String password) {
        AppUser user = appUserRepository.findByUsername(normalizeUsername(username))
                .orElseThrow(() -> new AuthenticationFailedException("用户名或密码错误"));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new AuthenticationFailedException("用户名或密码错误");
        }
        return user;
    }

    @Transactional(readOnly = true)
    public AppUser requireById(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new AuthenticationFailedException("登录状态已失效，请重新登录"));
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim();
    }
}
