package com.linkle.domain.dto;

import com.linkle.domain.entity.Friend;
import com.linkle.domain.entity.FriendState;
import com.linkle.domain.entity.User;

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
public class FriendDTO {

    private Long friendId;
    private Long userId1; // 요청 받은 사용자 ID
    private Long userId2; // 요청 보낸 사용자 ID
    private FriendState state;

    /*
     * 1. FROM (Entity → DTO)
     *   - Entity는 Database와 직접 연결되는 객체
     *   - 따라서 Controller/Service에서 바로 사용하지 않고 DTO로 변환하여 사용
     *   - 변환 과정에서 비밀번호 같은 민감한 정보는 제외할 수 있어 보안 측면에서도 안전
     *
     * 2. TO (DTO → Entity)
     *   - DTO는 주로 클라이언트에서 전달받은 요청 데이터를 담는 객체
     *   - 하지만 DB에 저장하거나 JPA가 관리하려면 반드시 Entity 형태여야 함
     *   - 따라서 JSON → DTO → Entity 변환 과정을 거쳐 Repository로 저장
     */


    // Entity → DTO 변환
    // DB에서 불러온 엔티티를 API 응답용 DTO로 바꿀 때 사용
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
    // DTO -> Entity 변환
    // API 요청에서 넘어온 DTO를 DB에 저장할 Entity로 바꿀 때 사용
    public Friend toEntity(User user1, User user2) {
        return Friend.builder()
            .friendId(this.friendId)
            .user1(user1)
            .user2(user2)
            .state(this.state)
            .build();
    }

}
