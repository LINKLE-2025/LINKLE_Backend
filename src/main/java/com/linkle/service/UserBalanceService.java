package com.linkle.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.linkle.domain.dto.AccountHistoryDTO;
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

    public UserBalanceDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));

        return new UserBalanceDTO(user.getUserId(), user.getBalance());
    }

    /**
     * 잔액 차감 (amount < 0: 차감, amount > 0: 충전)
     */
    public long updateBalance(Long userId, long amount) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));

        long newBalance = user.getBalance() + amount;
        if (newBalance < 0) {
            throw new IllegalArgumentException("잔액이 부족합니다.");
        }

        user.setBalance(newBalance);
        userRepository.save(user);

        // 💡 입출금 내역 기록 남기기
        String memo = amount > 0 ? "입금" : "출금";
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
}