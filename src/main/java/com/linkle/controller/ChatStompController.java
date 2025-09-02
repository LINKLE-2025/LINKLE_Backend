package com.linkle.controller;

import com.linkle.domain.dto.MessageResponseDTO;
import com.linkle.domain.dto.SendMessageRequestDTO;
import com.linkle.service.ChatMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatStompController {

    private final ChatMessageService chatMessageService;

    /**
     * 클라이언트 → /app/message.send 로 전송
     * 서버는 저장 후 브로커로 /sub/room.{roomId} 에 MessageCreatedEvent를 push (서비스 내부)
     * 이 핸들러 자체는 별도 반환을 하지 않음(ACK 필요시 /user/queue 로 응답하도록 확장 가능)
     */
    @MessageMapping("message.send")
    public void handleSend(@Valid @Payload SendMessageRequestDTO req,
        SimpMessageHeaderAccessor headers) {
        Long me = resolveUserId(headers);
        // 저장 & 브로드캐스트 (서비스 내부에서 eventProducer 사용)
        MessageResponseDTO _ignored = chatMessageService.send(req, me);
    }

    // ----------------- helpers -----------------
    private Long resolveUserId(SimpMessageHeaderAccessor headers) {
        // 프로젝트 정책에 맞게 유저 식별 추출
        // 1) STOMP native header에 x-user-id를 심어 보냈다면:
        if (headers != null && headers.getFirstNativeHeader("x-user-id") != null) {
            try {
                return Long.parseLong(headers.getFirstNativeHeader("x-user-id"));
            } catch (NumberFormatException ignore) {}
        }
        // 2) 인증을 Principal로 연동했다면:
        if (headers != null && headers.getUser() != null) {
            try {
                return Long.parseLong(headers.getUser().getName());
            } catch (NumberFormatException ignore) {}
        }
        // 데모/개발용 fallback
        return 1L;
    }
}
