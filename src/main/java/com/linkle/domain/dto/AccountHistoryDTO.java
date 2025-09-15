package com.linkle.domain.dto;


import lombok.*;

import java.time.LocalDateTime;

import com.linkle.domain.entity.AccountHistory;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountHistoryDTO {

    private Long accountId;
    private Long amount;
    private String memo;
    private LocalDateTime createdDate;
    private Long userId;

    // Entity → DTO 변환
    public static AccountHistoryDTO fromEntity(AccountHistory entity) {
        if (entity == null) return null;
        return AccountHistoryDTO.builder()
            .accountId(entity.getAccountId())
            .amount(entity.getAmount())
            .memo(entity.getMemo())
            .createdDate(entity.getCreatedDate())
            .userId(entity.getUser() != null ? entity.getUser().getUserId() : null)
            .build();
    }
}
