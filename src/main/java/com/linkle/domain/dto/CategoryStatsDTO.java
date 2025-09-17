package com.linkle.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryStatsDTO {
    private Long categoryId;
    private Long linkerCount;   // 내가 참여한 링커 수
    private Long postCount;     // 내가 쓴 포스트 수
    private Long chatCount;     // 내가 참여한 채팅방 수
}