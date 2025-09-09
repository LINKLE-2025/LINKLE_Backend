package com.linkle.domain.dto;

import com.linkle.domain.entity.Friend;
import com.linkle.domain.entity.FriendState;
import com.linkle.domain.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendResponseDTO {
    private Long friendId;
    private Long userId1;
    private Long userId2;
    private String name;
    private String nickname;
    private FriendState state;
    private String image;
    private String gender;



    public static FriendResponseDTO fromEntity(Friend friend, Long currentUserId) {
        if (friend == null) return null;

        User other = friend.getUser1().getUserId().equals(currentUserId)
            ? friend.getUser2()
            : friend.getUser1();

        return FriendResponseDTO.builder()
            .friendId(friend.getFriendId())
            .userId1(friend.getUser1().getUserId())
            .userId2(friend.getUser2().getUserId())
            .name(other.getName())
            .nickname(other.getNickname())
            .state(friend.getState())
            .image(other.getImage())
            .gender(other.getGender())
            .build();
    }
}
