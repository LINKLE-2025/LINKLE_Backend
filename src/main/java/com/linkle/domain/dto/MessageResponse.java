package com.linkle.domain.dto;

import com.linkle.domain.entity.ChatMessage;
import com.linkle.domain.entity.MessageType;
import com.linkle.domain.entity.User;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MessageResponse
 * - [GET /chat/room/{id}/messages] 목록 조회 응답
 * - [SUB /sub/room.{roomId}] 실시간 브로드캐스트 응답 (메시지 생성 시 push)
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponse {

    // ===== 기본 식별/본문 =====
    private Long messageId;
    private Long roomId;
    private MessageType messageType;    // TEXT / SYSTEM ...
    private String content;
    private Instant createdDate;

    // ===== 보낸 사람 (SYSTEM이면 null 가능) =====
    private Long senderId;
    private String senderName;
    private String senderImage;

    // ===== 부가 정보 (서비스에서 계산해서 세팅) =====
    private Integer readCount;          // 읽은 인원 수(선택)

    // ----------------------------------------------------------------------
    // 팩토리: 공통 변환
    // ----------------------------------------------------------------------
    public static MessageResponse fromEntity(ChatMessage msg) {
        if (msg == null) return null;

        MessageResponse.MessageResponseBuilder b = MessageResponse.builder()
            .messageId(msg.getMessageId())
            .roomId(msg.getRoom().getRoomId())
            .messageType(msg.getType())
            .content(msg.getText())                 //
            .createdDate(msg.getCreatedDate());     //

        // sender가 있을 때만 채움 (SYSTEM 메시지는 null일 수 있음)
        if (msg.getUser() != null) {
            User u = msg.getUser();
            b.senderId(u.getUserId())
                .senderName(u.getName())
                .senderImage(u.getImage());
        }

        return b.build();
    }

    /**
     * 목록 조회 응답에서 "읽은 인원 수"까지 포함해 내려줄 때 사용.
     */
    public static MessageResponse fromEntity(ChatMessage msg, Integer readCount) {
        MessageResponse base = fromEntity(msg);
        if (base != null && readCount != null) {
            base.setReadCount(readCount);
        }
        return base;
    }

    /**
     * 실시간 브로드캐스트 전용 팩토리.
     * - 메시지 저장 직후 STOMP 브로커로 그대로 push 할 때 사용
     * - 사용 예: convertAndSend("/sub/room." + roomId, MessageResponse.forBroadcast(saved))
     */
    public static MessageResponse forBroadcast(ChatMessage saved) {
        // 보통 목록과 동일 스펙으로 push하면 클라이언트 재사용이 쉬움
        return fromEntity(saved);
    }

    // ----------------------------------------------------------------------
    // 유틸: 리스트 변환
    // ----------------------------------------------------------------------
    public static List<MessageResponse> fromEntityList(List<ChatMessage> messages) {
        if (messages == null) return null;
        return messages.stream()
            .map(MessageResponse::fromEntity)
            .collect(Collectors.toList());
    }

    public static List<MessageResponse> fromEntityList(List<ChatMessage> messages, List<Integer> readCounts) {
        if (messages == null) return null;
        // readCounts가 null이거나 길이가 다르면 fromEntity만 사용
        if (readCounts == null || readCounts.size() != messages.size()) {
            return fromEntityList(messages);
        }
        return messages.stream()
            .map(m -> fromEntity(m, readCounts.get(messages.indexOf(m))))
            .collect(Collectors.toList());
    }
}
