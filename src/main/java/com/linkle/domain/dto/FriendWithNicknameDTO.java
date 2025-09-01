package com.linkle.domain.dto;

import com.linkle.domain.entity.Friend;
import com.linkle.domain.entity.FriendState;

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
public class FriendWithNicknameDTO {

    private Long friendId;
    private Long userId1;
    private String user1Nickname; // 요청 받은 사용자 닉네임
    private Long userId2;
    private String user2Nickname; // 요청 보낸 사용자 닉네임
    private FriendState state;

    // Entity → DTO 변환 (ID + 닉네임)
    public static FriendWithNicknameDTO fromEntity(Friend friend) {
        if (friend == null) {
            return null;
        }
        return FriendWithNicknameDTO.builder()
            .friendId(friend.getFriendId())
            .userId1(friend.getUser1() != null ? friend.getUser1().getUserId() : null)
            .user1Nickname(friend.getUser1() != null ? friend.getUser1().getNickname() : null)
            .userId2(friend.getUser2() != null ? friend.getUser2().getUserId() : null)
            .user2Nickname(friend.getUser2() != null ? friend.getUser2().getNickname() : null)
            .state(friend.getState())
            .build();
    }
}
