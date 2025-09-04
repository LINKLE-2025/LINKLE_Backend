package com.linkle.domain.dto;

import lombok.*;

import java.time.Instant;

/**
 * [브로커 이벤트] 메시지 읽음 이벤트
 * - 사용자가 특정 방에서 lastReadMessageId를 갱신했을 때 브로드캐스트
 * - 구독 경로 예시: /sub/room.{roomId}
 * - 클라이언트는 readerId가 자신이면 내 읽음 포인터/뱃지 갱신,
 *   아니면 상대 읽음 마크(읽음자 수 등) 갱신에 활용
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageReadEvent {

    /** 읽음이 발생한 방 */
    private Long roomId;

    /** 읽은 사용자 */
    private Long readerId;

    /** 이 메시지 ID까지 읽음 처리됨 */
    private Long lastReadMessageId;

    /** 서버에서 이벤트가 생성된 시각 */
    private Instant eventDate;

    // ===== 선택 메타(서비스에서 계산해 채움; 필요 없으면 null) =====
    /** reader 기준 이 방의 미확인 개수(보통 0으로 떨어짐) */
    private Integer roomUnreadCount;

    /** lastReadMessageId를 읽은 사람 수(읽음 마크 UI에 사용) */
    private Integer readByCount;

    // ---------- 편의 팩토리 ----------
    public static MessageReadEvent of(Long roomId, Long readerId, Long lastReadMessageId) {
        return MessageReadEvent.builder()
            .roomId(roomId)
            .readerId(readerId)
            .lastReadMessageId(lastReadMessageId)
            .eventDate(Instant.now())
            .build();
    }

    public static MessageReadEvent of(Long roomId, Long readerId, Long lastReadMessageId,
        Integer roomUnreadCount, Integer readByCount) {
        return MessageReadEvent.builder()
            .roomId(roomId)
            .readerId(readerId)
            .lastReadMessageId(lastReadMessageId)
            .roomUnreadCount(roomUnreadCount)
            .readByCount(readByCount)
            .eventDate(Instant.now())
            .build();
    }
}
