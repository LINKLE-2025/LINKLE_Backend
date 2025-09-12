package com.linkle.service;

import org.springframework.stereotype.Service;

import com.linkle.domain.dto.UserBalanceDTO;
import com.linkle.domain.entity.User;
import com.linkle.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserBalanceService {

    private final UserRepository userRepository;

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
        return newBalance;
    }
}