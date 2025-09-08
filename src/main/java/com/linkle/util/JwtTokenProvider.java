package com.linkle.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    // 실제 운영에서는 최소 32바이트 이상 복잡한 키를 사용하세요
    private static final String SECRET_KEY = "your-secret-key-your-secret-key-123456";
    private static final long EXPIRATION = 1000L * 60 * 60; // 1시간

    private final SecretKey key;

    public JwtTokenProvider() {
        // 문자열을 직접 bytes로 변환 → Key 객체 생성
        this.key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String email) {
        return Jwts.builder()
            .setSubject(email)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
            .signWith(key, SignatureAlgorithm.HS256) // ✅ SecretKey 객체 사용
            .compact();
    }
}
