package com.ra.st.service;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class LogoutService {
    private Set<String> blacklistedTokens = new HashSet<>();

    // Thêm token vào danh sách bị chặn
    public void blacklistToken(String token) {
        blacklistedTokens.add(token);
    }

    // Kiểm tra xem token có bị chặn không
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }
}
