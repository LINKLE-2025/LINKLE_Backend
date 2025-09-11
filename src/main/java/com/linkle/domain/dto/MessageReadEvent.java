package com.linkle.domain.dto;

import lombok.*;

import java.time.Instant;

/**
 * [브로커 이벤트] 메시지 읽음 이벤트
 * - 구독 경로 예시:
 *   - 방 토픽:   /sub/room.{roomId}
 *   - 유저 토픽: /sub/users.{userId}.room-updates
 * - 프론트 호환을 위해 type / unreadCount / lastMessageDate 를 포함
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageReadEvent {

    /** 이벤트 타입 (프론트에서 includes("READ") 체크) */
    @Builder.Default
    private String type = "ROOM_READ";

    /** 읽음이 발생한 방 */
    private Long roomId;

    /** 읽은 사용자 */
    private Long readerId;

    /** 이 메시지 ID까지 읽음 처리됨 */
    private Long lastReadMessageId;

    /** 서버에서 이벤트가 생성된 시각 */
    private Instant eventDate;

    // ===== 프론트 리스트 갱신 호환 필드 =====
    /** 리스트 핸들러가 바로 쓰는 필드 (READ면 0 고정) */
    private Integer unreadCount;      // ← NEW

    /** 마지막 메시지 시각(문자열 ISO-8601; 선택) */
    private String lastMessageDate;   // ← NEW

    // ===== 선택 메타(방 토픽/읽음 마크용) =====
    /** reader 기준 이 방의 미확인 개수(보통 0으로 떨어짐) */
    private Integer roomUnreadCount;

    /** lastReadMessageId를 읽은 사람 수(읽음 마크 UI에 사용) */
    private Integer readByCount;

    // ---------- 편의 팩토리 ----------
    public static MessageReadEvent of(Long roomId, Long readerId, Long lastReadMessageId) {
        return MessageReadEvent.builder()
            .type("ROOM_READ")
            .roomId(roomId)
            .readerId(readerId)
            .lastReadMessageId(lastReadMessageId)
            .unreadCount(0)                 // 리스트용
            .eventDate(Instant.now())
            .build();
    }

    public static MessageReadEvent of(Long roomId, Long readerId, Long lastReadMessageId,
        Integer roomUnreadCount, Integer readByCount) {
        return MessageReadEvent.builder()
            .type("ROOM_READ")
            .roomId(roomId)
            .readerId(readerId)
            .lastReadMessageId(lastReadMessageId)
            .unreadCount(0)                 // 리스트용
            .roomUnreadCount(roomUnreadCount)
            .readByCount(readByCount)
            .eventDate(Instant.now())
            .build();
    }
}
