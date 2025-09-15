package com.linkle.domain.dto;

import com.linkle.domain.entity.Linker;
import com.linkle.domain.entity.LinkerState;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParticipateLinkerResponseDTO {
    private Long linkerId;
    private String name;
    private Long categoryId;
    private String memo;
    // 채팅방 수
    private Long chatRoomCount;
    // 포스트 수
    private Long postCount;
    // linker 상태
    private LinkerState state;
    // linker 주소
    private String address;

    public static ParticipateLinkerResponseDTO from(Linker linker, Long chatRoomCount, Long postCount){
        return ParticipateLinkerResponseDTO.builder()
            .linkerId(linker.getLinkerId())
            .name(linker.getName())
            .categoryId(linker.getCategoryId())
            .memo(linker.getMemo())
            .chatRoomCount(chatRoomCount)
            .postCount(postCount)
            .state(linker.getState())
            .address(linker.getAddress())
            .build();
    }
}
