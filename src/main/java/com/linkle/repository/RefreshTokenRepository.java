package com.linkle.repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RefreshTokenRepository {

    @Value("${jwt.refresh-token-expiration:1209600000}")
    private long refreshExpiration;

    private final StringRedisTemplate redisTemplate;

    public RefreshTokenRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Redis에 Refresh Token 저장 (userId를 key로 사용)
    public void save(String userId, String refreshToken) {
        redisTemplate.opsForValue().set("RT:" + userId, refreshToken, refreshExpiration, TimeUnit.MILLISECONDS);
    }

    // userId로 Refresh Token 조회
    public Optional<String> findByUserId(String userId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get("RT:" + userId));
    }

    // userId로 Refresh Token 삭제
    public void delete(String userId) {
        redisTemplate.delete("RT:" + userId);
    }
}