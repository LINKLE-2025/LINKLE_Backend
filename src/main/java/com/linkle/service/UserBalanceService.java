package com.linkle.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.linkle.domain.dto.AccountHistoryDTO;
import com.linkle.domain.dto.PaymentResponseDTO;
import com.linkle.domain.dto.UserBalanceDTO;
import com.linkle.domain.entity.AccountHistory;
import com.linkle.domain.entity.User;
import com.linkle.repository.AccountHistoryRepository;
import com.linkle.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserBalanceService {
    private final UserRepository userRepository;
    private final AccountHistoryRepository accountHistoryRepository;

    @Value("${PORTONE_API_SECRET}")
    private String apiSecret;


    public UserBalanceDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));

        return UserBalanceDTO.builder()
            .userId(user.getUserId())
            .balance(user.getBalance() != null ? user.getBalance() : 0L)
            .accountNumber(user.getAccountNumber() != null ? user.getAccountNumber() : "")
            .bankId(user.getBankId() != null ? user.getBankId() : 0) // null이면 0으로 처리
            .build();
    }

    /**
     * 잔액 차감 (amount < 0: 차감, amount > 0: 충전)
     */
    public long updateBalance(Long userId, long amount, String memo) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));

        long newBalance = user.getBalance() + amount;
        if (newBalance < 0) {
            throw new IllegalArgumentException("잔액이 부족합니다.");
        }

        user.setBalance(newBalance);
        userRepository.save(user);

        // 💡 입출금 내역 기록 남기기
        AccountHistory history = AccountHistory.builder()
            .amount(amount)
            .memo(memo)
            .createdDate(LocalDateTime.now())
            .user(user)
            .build();
        accountHistoryRepository.save(history);

        return newBalance;
    }

    /**
     * 유저별 입출금 내역 조회
     */
    public List<AccountHistoryDTO> getHistoryByUserId(Long userId) {
        List<AccountHistory> histories = accountHistoryRepository.findByUser_UserIdOrderByCreatedDateDesc(userId);
        return histories.stream()
            .map(AccountHistoryDTO::fromEntity)
            .toList();
    }

    /**
     * V2 결제 조회
     */
    public PaymentResponseDTO getPaymentInfoV2(String paymentId) {
        System.out.println("PortOne API Secret 확인: " + apiSecret); // ✅ 값 제대로 들어오는지 확인

        WebClient client = WebClient.builder()
            .baseUrl("https://api.portone.io")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "PortOne " + apiSecret) // ✅ 시크릿 토큰
            .build();

        return client.get()
            .uri("/payments/" + paymentId)
            .retrieve()
            .bodyToMono(PaymentResponseDTO.class)
            .block();
    }


    public UserBalanceDTO updateAccountInfo(Long userId, String accountNumber, int bankId) {
        // 1️⃣ DB에서 해당 유저 조회
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        // 2️⃣ 계좌 정보 업데이트
        user.setAccountNumber(accountNumber);
        user.setBankId(bankId);

        userRepository.save(user);

        // 3️⃣ DTO 반환
        return UserBalanceDTO.builder()
            .userId(user.getUserId())
            .balance(user.getBalance())
            .accountNumber(user.getAccountNumber())
            .bankId(user.getBankId())
            .build();
    }
}