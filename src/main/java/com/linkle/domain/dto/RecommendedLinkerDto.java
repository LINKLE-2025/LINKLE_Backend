package com.linkle.domain.dto;

import com.linkle.domain.entity.Linker;
import com.linkle.domain.entity.LinkerState;

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
    private LinkerState state; // 링커 활성 상태

    // 정적 팩토리 메서드 추가
    public static RecommendedLinkerDto from(Linker linker, double score) {
        return RecommendedLinkerDto.builder()
            .linkerId(linker.getLinkerId())
            .name(linker.getName())
            .memo(linker.getMemo())
            .categoryId(linker.getCategoryId())
            .address(linker.getAddress())
            .addressDetail(linker.getAddressDetail())
            .locationX(linker.getLocationX())
            .locationY(linker.getLocationY())
            .state(linker.getState())
            .score(score)
            .build();
    }
}