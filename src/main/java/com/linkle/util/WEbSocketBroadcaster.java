package com.linkle.util;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/** STOMP 브로커로 메시지 push */
@Component
@RequiredArgsConstructor
public class WebSocketBroadcaster {

    private final SimpMessagingTemplate template;

    /** 방 구독자들에게 브로드캐스트 */
    public void toRoom(Long roomId, Object payload) {
        template.convertAndSend(StompDestinations.room(roomId), payload);
    }

    /** 특정 사용자 개인 큐로 전송(선택) */
    public void toUser(Long userId, Object payload) {
        template.convertAndSend(StompDestinations.user(userId), payload);
    }

    /** 임의 목적지로 전송 */
    public void to(String destination, Object payload) {
        template.convertAndSend(destination, payload);
    }
}
