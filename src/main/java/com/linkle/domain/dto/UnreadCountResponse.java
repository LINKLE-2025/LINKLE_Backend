package com.linkle.domain.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * [GET /chat/unread]
 * - 현재 로그인 사용자의 전체 미확인 개수와, 방별 미확인 개수를 함께 반환
 * - 리스트 화면 뱃지(totalUnread)와, 방별 뱃지(rooms[*].unreadCount)에 바로 사용 가능
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnreadCountResponse {

    /** 모든 방의 미확인 합계 */
    private Integer totalUnread;

    /** 방별 미확인 목록 */
    @Builder.Default
    private List<RoomUnread> rooms = new ArrayList<>();

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoomUnread {
        private Long roomId;
        private Integer unreadCount;
    }

    // ======= 팩토리 메서드들 =======

    /** 방별 미확인 Map(roomId -> count)을 한 번에 DTO로 변환 */
    public static UnreadCountResponse fromPerRoom(Map<Long, Integer> perRoom) {
        UnreadCountResponse res = new UnreadCountResponse();
        if (perRoom == null || perRoom.isEmpty()) {
            res.setTotalUnread(0);
            res.setRooms(new ArrayList<>());
            return res;
        }

        int total = 0;
        List<RoomUnread> items = new ArrayList<>(perRoom.size());
        for (Map.Entry<Long, Integer> e : perRoom.entrySet()) {
            int c = e.getValue() == null ? 0 : e.getValue();
            items.add(RoomUnread.builder()
                .roomId(e.getKey())
                .unreadCount(c)
                .build());
            total += c;
        }
        res.setTotalUnread(total);
        res.setRooms(items);
        return res;
    }

    /** 단일 방 미확인 응답(부분 갱신 시 유용) */
    public static UnreadCountResponse forRoom(Long roomId, int unreadCount) {
        return UnreadCountResponse.builder()
            .totalUnread(unreadCount)
            .rooms(List.of(RoomUnread.builder()
                .roomId(roomId)
                .unreadCount(unreadCount)
                .build()))
            .build();
    }

    /** 비어있는 기본 응답 */
    public static UnreadCountResponse empty() {
        return UnreadCountResponse.builder()
            .totalUnread(0)
            .rooms(new ArrayList<>())
            .build();
    }
}
