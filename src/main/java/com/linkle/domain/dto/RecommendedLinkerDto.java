package com.linkle.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter @Setter
public class RecommendedLinkerDto {
    private Long linkerId;
    private String name;
    private String memo;
    private Long categoryId;
    private String address;
    private String addressDetail;
    private Double locationX;   // 경도
    private Double locationY;   // 위도
    private Double score;       // 유사도 점수
}