package com.linkle.controller;

import java.util.List;
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

import com.linkle.domain.dto.AccountHistoryDTO;
import com.linkle.domain.dto.PaymentResponseDTO;
import com.linkle.domain.dto.UserBalanceDTO;
import com.linkle.service.UserBalanceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/balance")
@RequiredArgsConstructor
public class UserBalanceController {

    private final UserBalanceService userBalanceService; // UserService에 잔액 조회/차감 로직 구현

    @PostMapping("/charge-complete")
    public ResponseEntity<?> chargeComplete(@RequestBody Map<String, Object> body) {
        String paymentId = (String) body.get("paymentId");
        Long userId = ((Number) body.get("userId")).longValue();

        log.info("[BACK] chargeComplete 진입 ✅ paymentId={}, userId={}", paymentId, userId);

        // 1. 결제내역 조회
        PaymentResponseDTO payment = userBalanceService.getPaymentInfoV2(paymentId);
        System.out.println("결제 조회 결과: " + payment);

        String memo = "계좌 충전";

        if ("PAID".equals(payment.getStatus())) {
            long amount = payment.getAmount().getTotal();
            long newBalance = userBalanceService.updateBalance(userId, amount, memo);
            return ResponseEntity.ok(Map.of(
                "msg", "충전 성공",
                "balance", newBalance
            ));
        } else {
            return ResponseEntity.badRequest()
                .body("결제 검증 실패: status=" + payment.getStatus());
        }
    }



    @GetMapping("/{userId}/history")
    public ResponseEntity<List<AccountHistoryDTO>> getHistory(@PathVariable Long userId) {
        List<AccountHistoryDTO> historyList = userBalanceService.getHistoryByUserId(userId);
        return ResponseEntity.ok(historyList);
    }


    // 1. 유저 정보 조회 (잔액 포함)
    @GetMapping("/{userId}")
    public ResponseEntity<UserBalanceDTO> getUser(@PathVariable Long userId) {
        UserBalanceDTO user = userBalanceService.getUserById(userId);

        System.out.println(user);


        return ResponseEntity.ok(user);
    }

    // 2. 잔액 차감
    @PatchMapping("/{userId}/balance")
    public ResponseEntity<Map<String, Object>> updateBalance(
        @PathVariable Long userId,
        @RequestBody Map<String, Long> body
    ) {
        long amount = body.getOrDefault("amount", 0L);
        String memo = "출금합니다";
        try {
            long newBalance = userBalanceService.updateBalance(userId, amount,memo);
            return ResponseEntity.ok(Map.of("balance", newBalance));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
}