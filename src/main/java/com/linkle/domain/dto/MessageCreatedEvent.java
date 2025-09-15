package com.linkle.domain.dto;

import com.linkle.domain.entity.ChatMessage;
import com.linkle.domain.entity.MessageType;
import com.linkle.domain.entity.User;
import lombok.*;

import java.time.Instant;

/**
 * [브로커 이벤트] 메시지 생성 이벤트
 * - 저장 직후 STOMP 등으로 브로드캐스트할 페이로드
 * - 구독 경로 예: /sub/room.{roomId}, /sub/users.{userId}.room-updates
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageCreatedEvent {

    @Builder.Default
    private String type = "MESSAGE_CREATED";   //

    // ===== 기본 식별/본문 =====
    private Long messageId;
    private Long roomId;
    private MessageType messageType;   // TEXT / SYSTEM ...
    private String text;
    private Instant createdDate;

    // ===== 보낸 사람(시스템이면 null 가능) =====
    private Long senderId;
    private String senderName;
    private String senderImage;

    // ===== 리스트 갱신 호환 필드 =====
    private Integer unreadCount;   //
    private String lastMessageDate; // ISO-8601 string (선택)

    // ------------------------------------------------------------------
    // 팩토리: 엔티티 -> 이벤트 변환
    // ------------------------------------------------------------------
    public static MessageCreatedEvent fromEntity(ChatMessage msg, int unreadCount) {
        if (msg == null) return null;

        MessageCreatedEvent.MessageCreatedEventBuilder b = MessageCreatedEvent.builder()
            .messageId(msg.getMessageId())
            .roomId(msg.getRoom().getRoomId())
            .messageType(msg.getType())
            .text(msg.getText())
            .createdDate(msg.getCreatedDate())
            .unreadCount(unreadCount)
            .lastMessageDate(msg.getCreatedDate().toString());

        User sender = msg.getUserId();
        if (sender != null) {
            b.senderId(sender.getUserId())
                .senderName(sender.getName())
                .senderImage(sender.getImage());
        }
        return b.build();
    }

    /** 브로드캐스트 편의 메서드 */
    public static MessageCreatedEvent forBroadcast(ChatMessage saved, int unreadCount) {
        return fromEntity(saved, unreadCount);
    }
}
