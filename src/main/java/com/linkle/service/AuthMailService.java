package com.linkle.service;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

import org.springframework.mail.javamail.MimeMessageHelper;

@Service
@RequiredArgsConstructor
public class AuthMailService {

    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;
    private static final Duration TTL = Duration.ofMinutes(5);  // 5분 TTL

    public void sendMail(String to, String subject, String text) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // 발신자 설정 (이메일, 이름)
            helper.setFrom("linkle.team@gmail.com", "LINKLE");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException("메일 전송 실패", e);
        }
    }

    // 코드 저장 (해시 + TTL)
    public void saveCode(String email, String rawCode) {
        String key = keyOf(email);
        String hashed = hash(rawCode);  // 해시 저장
        redisTemplate.opsForValue().set(key, hashed, TTL);  // 5분 TTL
    }

    // 코드 검증 및 삭제
    public boolean verifyCode(String email, String input) {
        String key = keyOf(email);
        String saved = redisTemplate.opsForValue().get(key);  // 저장된 해시
        if (saved == null || !slowEquals(saved, hash(input))) return false;  // null 또는 불일치
        redisTemplate.delete(key);  // 검증 성공 시 삭제
        return true;
    }

    // Redis 키 생성
    private String keyOf(String email) {
        return "verify:" + email.trim().toLowerCase();  // 이메일 소문자 변환 및 공백 제거
    }

    // 코드 해시 함수 (SHA-256 + Base64)
    private String hash(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(md.digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    // 타이밍 공격 방지 비교 (상수 시간 비교)
    private boolean slowEquals(String a, String b) {
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) result |= a.charAt(i) ^ b.charAt(i);
        return result == 0;
    }
}