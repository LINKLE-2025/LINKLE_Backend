package com.linkle.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.linkle.domain.dto.SendMessageRequestDTO;
import com.linkle.service.ChatMessageService;

@Controller
@RequiredArgsConstructor
public class ChatStompController {

    private final ChatMessageService chatMessageService;

    @MessageMapping("message.send")            // 클라: /app/message.send
    public void handleSend(@Payload SendMessageRequestDTO req,
        @Header(name = "x-user-id", required = false) String uidHeader) {
        Long me = (uidHeader == null || uidHeader.isBlank())
            ? 1L : Long.parseLong(uidHeader);
        chatMessageService.send(req, me);     // 저장 + 브로드캐스트
    }
}
