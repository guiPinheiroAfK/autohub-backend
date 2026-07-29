package com.autohub.infrastructure.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String PREFIX = "blacklist:";
    private final StringRedisTemplate redis;

    public void blacklist(String token, long expirationEpochMs) {
        long ttlMs = expirationEpochMs - System.currentTimeMillis();
        if (ttlMs > 0) {
            redis.opsForValue().set(PREFIX + token, "1", Duration.ofMillis(ttlMs));
        }
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redis.hasKey(PREFIX + token));
    }
}
