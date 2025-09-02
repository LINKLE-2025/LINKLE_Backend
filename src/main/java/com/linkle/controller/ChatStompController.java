package com.linkle.controller;

import com.linkle.domain.dto.SendMessageRequest;
import com.linkle.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

/**
 * STOMP 엔드포인트
 * 클라이언트는 /app/message.send 로 발행
 * -> Service에서 DB 저장(CHAT_MESSAGE) + 브로커 발행
 * -> 소비자(Consumer)가 /sub/room.{roomId} 로 브로드캐스트
 */
@Controller
@RequiredArgsConstructor
public class ChatStompController {

    private final ChatMessageService messageService;

    /** [STOMP /app/message.send] 실시간 메시지 전송 */
    @MessageMapping("/message.send")
    public void send(SendMessageRequest req) {
        messageService.send(req);
    }
}
