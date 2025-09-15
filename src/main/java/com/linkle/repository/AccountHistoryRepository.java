package com.linkle.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.linkle.domain.entity.AccountHistory;

public interface AccountHistoryRepository extends JpaRepository<AccountHistory, Long> {
    List<AccountHistory> findByUser_UserIdOrderByCreatedDateDesc(Long userId);
}
