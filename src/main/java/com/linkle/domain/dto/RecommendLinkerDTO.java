package com.linkle.domain.dto;

import java.time.LocalDateTime;

import com.linkle.domain.entity.Linker;
import com.linkle.domain.entity.LinkerState;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendLinkerDTO {

    private Long linkerId;
    private String name;
    private String addressName;
    private String address;
    private String addressDetail;
    private Double locationX;
    private Double locationY;
    private LinkerState state;
    private Long categoryId;
    private LocalDateTime createdDate;
    private String memo;

    // Entity → DTO
    public static RecommendLinkerDTO fromEntity(Linker linker) {
        if (linker == null) {
            return null;
        }
        return RecommendLinkerDTO.builder()
            .linkerId(linker.getLinkerId())
            .name(linker.getName())
            .addressName(linker.getAddressName())
            .address(linker.getAddress())
            .addressDetail(linker.getAddressDetail())
            .locationX(linker.getLocationX())
            .locationY(linker.getLocationY())
            .state(linker.getState())
            .categoryId(linker.getCategoryId())
            .createdDate(linker.getCreatedDate())
            .memo(linker.getMemo())
            .build();
    }
}
