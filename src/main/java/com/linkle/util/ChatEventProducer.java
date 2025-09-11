package com.linkle.util;

import com.linkle.domain.dto.MessageCreatedEvent;
import com.linkle.domain.dto.MessageReadEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatEventProducer {

    private final WebSocketBroadcaster ws;

    /** 메시지 생성 이벤트 브로드캐스트: /sub/room.{roomId} */
    public void messageCreated(MessageCreatedEvent evt) {
        if (evt == null || evt.getRoomId() == null) return;
        ws.toRoom(evt.getRoomId(), evt);
        log.debug("Broadcasted MessageCreatedEvent to room {} -> {}", evt.getRoomId(), evt.getMessageId());
    }

    /** 메시지 읽음 이벤트 브로드캐스트: /sub/room.{roomId} */
    public void messageRead(MessageReadEvent evt) {
        if (evt == null || evt.getRoomId() == null) return;
        ws.toRoom(evt.getRoomId(), evt);
        log.debug("Broadcasted MessageReadEvent to room {} by reader {}", evt.getRoomId(), evt.getReaderId());
    }

    // 유저 단일 토픽으로 메시지 생성 알림
    public void messageCreatedToUsers(MessageCreatedEvent evt, Collection<Long> memberUserIds) {
        if (evt == null || evt.getRoomId() == null) return;
        for (Long uid : memberUserIds) {
            ws.to(StompDestinations.userRoomUpdates(uid), evt);
            log.debug("Sent MessageCreatedEvent to user {} -> room {}", uid, evt.getRoomId());
        }
    }

    // 유저 단일 토픽으로 읽음 알림 (해당 사용자만)
    public void messageReadToUser(MessageReadEvent evt, long userId) {
        ws.to(StompDestinations.userRoomUpdates(userId), evt);
        log.debug("Sent MessageReadEvent to user {} -> room {}", userId, evt.getRoomId());
    }
}
