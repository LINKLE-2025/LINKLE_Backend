package com.linkle.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * STOMP 브로커로 메시지를 push하는 얇은 어댑터.
 * - 목적지 문자열은 StompDestinations를 통해 일관되게 생성
 * - 서비스는 전송 방식/경로를 몰라도 됨
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketBroadcaster {

    private final SimpMessagingTemplate template;

    /** 방 구독자들에게 브로드캐스트: /sub/room.{roomId} */
    public void toRoom(Long roomId, Object payload) {
        template.convertAndSend(StompDestinations.room(roomId), payload);
        if (log.isDebugEnabled()) log.debug("Broadcast to room {} -> {}", roomId, payload.getClass().getSimpleName());
    }

    /** 방 브로드캐스트(헤더 포함) */
    public void toRoom(Long roomId, Object payload, @Nullable MessageHeaders headers) {
        if (headers == null) {
            toRoom(roomId, payload);
        } else {
            template.convertAndSend(StompDestinations.room(roomId), payload, headers);
            if (log.isDebugEnabled()) log.debug("Broadcast to room {} (with headers)", roomId);
        }
    }

    /** 특정 사용자 개인 큐로 전송: /queue/user.{userId} (우리 프로젝트 컨벤션) */
    public void toUser(Long userId, Object payload) {
        template.convertAndSend(StompDestinations.user(userId), payload);
        if (log.isDebugEnabled()) log.debug("Send to user {} -> {}", userId, payload.getClass().getSimpleName());
    }

    /** 임의 목적지로 전송 (특수 채널 필요 시) */
    public void to(String destination, Object payload) {
        template.convertAndSend(destination, payload);
        if (log.isDebugEnabled()) log.debug("Send to {} -> {}", destination, payload.getClass().getSimpleName());
    }

    // 참고: Spring의 "user destination" 표준(/user/queue/...)을 쓰고 싶다면 아래도 제공 가능
    // public void toSpringUser(String userName, String userQueue, Object payload) {
    //     template.convertAndSendToUser(userName, userQueue, payload);
    // }
}
