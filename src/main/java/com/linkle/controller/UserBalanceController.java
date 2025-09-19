
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

    /**
     * PC QR 결제 등에서 프론트 redirect 불가 시,
     * 서버에서 바로 결제 상태 확인하고 DB 반영
     */
    @PostMapping("/charge")
    public ResponseEntity<?> charge(@RequestBody Map<String, Object> body) {
        String paymentId = (String) body.get("paymentId");
        Object userIdObj = body.get("userId");
        Long userId;

        if (userIdObj instanceof Number n) {
            userId = n.longValue();
        } else if (userIdObj instanceof String s) {
            userId = Long.parseLong(s);
        } else {
            throw new IllegalArgumentException("userId 값이 잘못되었습니다.");
        }

        log.info("[BACK] charge 진입 ✅ paymentId={}, userId={}", paymentId, userId);

        // 1️⃣ 결제 상태 조회
        PaymentResponseDTO payment = userBalanceService.getPaymentInfoV2(paymentId);
        log.info("결제 조회 결과: {}", payment);

        if (!"PAID".equals(payment.getStatus())) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "msg", "결제 미완료",
                    "status", payment.getStatus()
                ));
        }

        // 2️⃣ DB에 잔액 반영
        long amount = payment.getAmount().getTotal();
        String memo = "계좌 충전";
        long newBalance = userBalanceService.updateBalance(userId, amount, memo);

        return ResponseEntity.ok(Map.of(
            "msg", "충전 성공",
            "balance", newBalance
        ));
    }



    // 결제 히스토리 조회
    @GetMapping("/{userId}/history")
    public ResponseEntity<List<AccountHistoryDTO>> getHistory(@PathVariable Long userId) {
        List<AccountHistoryDTO> historyList = userBalanceService.getHistoryByUserId(userId);
        return ResponseEntity.ok(historyList);
    }


    // 1. 유저 정보 조회 (잔액, 계좌번호)
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
        String memo = (String) body.getOrDefault("memo", "");
        try {
            long newBalance = userBalanceService.updateBalance(userId, amount, memo);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "balance", newBalance
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    // 3. 계좌 정보 수정
    @PatchMapping("/{userId}/update")
    public ResponseEntity<Map<String, Object>> updateAccount(
        @PathVariable Long userId,
        @RequestBody Map<String, Object> body
    ) {
        try {
            String accountNumber = (String) body.get("accountNumber");
            Integer bankId = (body.get("bankId") != null) ? ((Number) body.get("bankId")).intValue() : null;

            if (accountNumber == null || bankId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "accountNumber와 bankId 모두 필요합니다."));
            }

            UserBalanceDTO updatedUser = userBalanceService.updateAccountInfo(userId, accountNumber, bankId);

            return ResponseEntity.ok(Map.of(
                "msg", "계좌 정보 수정 성공",
                "user", updatedUser
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "서버 에러: " + e.getMessage()));
        }
    }


    // 4. 다른 사용자에게 송금
    @PatchMapping("/{fromUserId}/transfer/{toUserId}")
    public ResponseEntity<Map<String, Object>> transfer(
        @PathVariable Long fromUserId,
        @PathVariable Long toUserId,
        @RequestBody Map<String, Object> body
    ) {
        long amount = ((Number) body.getOrDefault("amount", 0L)).longValue();
        String memo = (String) body.getOrDefault("memo", "송금");

        if (amount <= 0) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "송금 금액은 0보다 커야 합니다."));
        }

        try {
            // 송신자 출금
            userBalanceService.updateBalance(fromUserId, -amount, "송금 → " + toUserId);

            // 수신자 입금
            long newBalanceReceiver = userBalanceService.updateBalance(toUserId, amount, "입금 ← " + fromUserId);

            return ResponseEntity.ok(Map.of(
                "msg", "송금 성공",
                "receiverBalance", newBalanceReceiver
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

}