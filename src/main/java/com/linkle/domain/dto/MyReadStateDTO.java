// src/main/java/com/linkle/domain/dto/MyReadStateDTO.java
package com.linkle.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 특정 방에서 각 유저의 읽음 상태를 내려줄 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyReadStateDTO {
    private Long userId;            // 유저 ID
    private Long lastReadMessageId; // 마지막 읽은 메시지 ID (없으면 null)
}
