package com.linkle.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    // 실제 운영에서는 최소 32바이트 이상 복잡한 키를 사용하세요
    private static final String SECRET_KEY = "your-secret-key-your-secret-key-123456";

    // JWT 토큰 만료 시간
    @Value("${jwt.access-token-expiration:900000}")
    private long accessExpiration;
    @Value("${jwt.refresh-token-expiration:1209600000}")
    private long refreshExpiration;

    private final SecretKey key;

    public JwtTokenProvider() {
        // 문자열을 직접 bytes로 변환 → Key 객체 생성
        this.key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    // Access Token 발급
    public String generateAccessToken(String userId) {
        return Jwts.builder()
            .setSubject(userId)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + accessExpiration))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    // Refresh Token 발급
    public String generateRefreshToken(String userId) {
        return Jwts.builder()
            .setSubject(userId)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    // 토큰 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 토큰에서 사용자 ID 추출
    public String getUserIdFromToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody().getSubject();
    }

}
