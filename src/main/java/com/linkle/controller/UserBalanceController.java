package com.linkle.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.linkle.domain.dto.UserBalanceDTO;
import com.linkle.service.UserBalanceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/balance")
@RequiredArgsConstructor
public class UserBalanceController {

    private final UserBalanceService userBalanceService; // UserService에 잔액 조회/차감 로직 구현

    // 1. 유저 정보 조회 (잔액 포함)
    @GetMapping("/{userId}")
    public ResponseEntity<UserBalanceDTO> getUser(@PathVariable Long userId) {
        UserBalanceDTO user = userBalanceService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    // 2. 잔액 차감
    @PatchMapping("/{userId}/balance")
    public ResponseEntity<Map<String, Object>> updateBalance(
        @PathVariable Long userId,
        @RequestBody Map<String, Long> body
    ) {
        long amount = body.getOrDefault("amount", 0L);
        try {
            long newBalance = userBalanceService.updateBalance(userId, amount);
            return ResponseEntity.ok(Map.of("balance", newBalance));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
}