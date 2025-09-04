package com.linkle.util;

import com.linkle.domain.dto.MessageCreatedEvent;
import com.linkle.domain.dto.MessageReadEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 채팅 도메인 이벤트 → STOMP 브로커 발행 어댑터
 *
 * 역할
 * - 서비스 레이어를 브로커/경로/전송방식으로부터 분리
 * - 전송 경로 정책을 한 곳에서 관리 (StompDestinations와 함께 사용)
 * - 향후 전송 수단(예: Redis Pub/Sub, Kafka) 변경 시 여기만 교체
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatEventProducer {

    private final WebSocketBroadcaster ws; // STOMP 세션으로 push 하는 얇은 유틸

    /** 메시지 생성 이벤트 브로드캐스트: /sub/room.{roomId} */
    public void messageCreated(MessageCreatedEvent evt) {
        if (evt == null || evt.getRoomId() == null) return;
        ws.toRoom(evt.getRoomId(), evt);
        log.debug("Broadcasted MessageCreatedEvent roomId={}, messageId={}", evt.getRoomId(), evt.getMessageId());
    }

    /** 메시지 읽음 이벤트 브로드캐스트: /sub/room.{roomId} */
    public void messageRead(MessageReadEvent evt) {
        if (evt == null || evt.getRoomId() == null) return;
        ws.toRoom(evt.getRoomId(), evt);
        log.debug("Broadcasted MessageReadEvent roomId={}, readerId={}, lastReadMessageId={}",
            evt.getRoomId(), evt.getReaderId(), evt.getLastReadMessageId());
    }

}
