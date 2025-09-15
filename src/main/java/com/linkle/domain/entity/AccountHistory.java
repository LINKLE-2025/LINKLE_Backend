package com.linkle.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import com.linkle.domain.entity.User;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @Column(nullable = false)
    private Long amount; // 금액 (+,-)

    @Column(nullable = false)
    private String memo; // 입금/출금 내용

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate; // 입출금 시간

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // 사용자 연관관계
}
