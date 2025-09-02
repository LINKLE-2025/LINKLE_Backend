package com.linkle.domain.dto;

import com.linkle.domain.entity.Friend;
import com.linkle.domain.entity.FriendState;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendDTO {

    private Long friendId;
    private Long userId1; // 요청 받은 사용자 ID
    private Long userId2; // 요청 보낸 사용자 ID
    private FriendState state;

    // Entity → DTO 변환
    public static FriendDTO fromEntity(Friend friend) {
        if (friend == null) {
            return null;
        }
        return FriendDTO.builder()
            .friendId(friend.getFriendId())
            .userId1(friend.getUser1() != null ? friend.getUser1().getUserId() : null)
            .userId2(friend.getUser2() != null ? friend.getUser2().getUserId() : null)
            .state(friend.getState())
            .build();
    }
}
