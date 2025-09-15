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
import com.linkle.service.PortOneService;
import com.linkle.service.UserBalanceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/balance")
@RequiredArgsConstructor
public class UserBalanceController {

    private final UserBalanceService userBalanceService; // UserService에 잔액 조회/차감 로직 구현
    private final PortOneService portOneService;

    @PostMapping("/charge-complete")
    public ResponseEntity<?> chargeComplete(@RequestBody Map<String, Object> body) {
        String impUid = (String) body.get("imp_uid");
        int amount = ((Number) body.get("paid_amount")).intValue();
        Long userId = 1L; // 👉 실제는 로그인 세션/JWT에서 userId 가져오기
        String memo = "계좌 충전";
        // 1. 포트원에서 결제 검증
        String token = portOneService.getAccessToken();
        PaymentResponseDTO payment = portOneService.getPaymentInfo(token, impUid);

        if (payment.getAmount() == amount && "paid".equals(payment.getStatus())) {
            // 2. 사용자 포인트 충전 처리
            userBalanceService.updateBalance(userId, amount,memo);
            return ResponseEntity.ok("충전 성공");
        } else {
            return ResponseEntity.badRequest().body("결제 검증 실패");
        }
    }
    
    // 결제 히스토리 조회
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
    @PatchMapping("/{userId}/withdraw")
    public ResponseEntity<Map<String, Object>> updateBalance(
        @PathVariable Long userId,
        @RequestBody Map<String, Object> body
    ) {
        long amount = ((Number) body.getOrDefault("amount", 0L)).longValue();
        String memo = (String) body.getOrDefault("memo", ""); // 메모 추가

        try {
            long newBalance = userBalanceService.updateBalance(userId, amount,memo);
            return ResponseEntity.ok(Map.of("balance", newBalance));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
}