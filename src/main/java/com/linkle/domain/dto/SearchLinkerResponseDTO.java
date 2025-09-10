package com.linkle.domain.dto;

import com.linkle.domain.entity.Friend;
import com.linkle.domain.entity.Linker;
import com.linkle.domain.entity.Post;
import com.linkle.domain.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchLinkerResponseDTO {
    private Long linkerId;
    private String name;
    private Long categoryId;
    private String memo;
    // 채팅방 수
    private Long chatRoomCount;
    // 포스트 수
    private Long postCount;

    public static SearchLinkerResponseDTO from(Linker linker, Long chatRoomCount, Long postCount){
        return SearchLinkerResponseDTO.builder()
            .linkerId(linker.getLinkerId())
            .name(linker.getName())
            .categoryId(linker.getCategoryId())
            .memo(linker.getMemo())
            .chatRoomCount(chatRoomCount)
            .postCount(postCount)
            .build();
    }
}
