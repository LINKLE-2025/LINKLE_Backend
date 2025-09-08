package com.linkle.domain.dto;

import com.linkle.domain.entity.Friend;
import com.linkle.domain.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchResponseDTO {
    private Long userId;
    private String name;
    private String nickname;
    private String gender;
    private String state;
    private String image;

    public static SearchResponseDTO from(User user, Friend friend) {
        return SearchResponseDTO.builder()
            .userId(user.getUserId())
            .name(user.getName())
            .nickname(user.getNickname())
            .gender(user.getGender())
            .state(friend != null ? friend.getState().name() : "NONE")
            .image(user.getImage())
            .build();
    }
}
