package com.linkle.service;

import com.linkle.domain.dto.MessageReadEvent;
import com.linkle.domain.dto.ReadSyncRequestDTO;
import com.linkle.domain.entity.ChatPart;
import com.linkle.domain.entity.ChatMessage;
import com.linkle.repository.ChatMessageRepository;
import com.linkle.repository.ChatPartRepository;
import com.linkle.util.ChatEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatReadService {

    private final ChatPartRepository chatPartRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatEventProducer eventProducer;

    @Transactional
    public void syncRead(ReadSyncRequestDTO req, Long readerUserId) {
        ChatPart part = chatPartRepository
            .findByRoom_RoomIdAndUser_UserId(req.getRoomId(), readerUserId)
            .orElseThrow(() -> new IllegalStateException("Not a member of room: " + req.getRoomId()));

        // (권장) lastReadMessageId가 해당 방의 메시지인지 검증
        Optional<ChatMessage> optMsg = chatMessageRepository
            .findById(req.getLastReadMessageId())
            .filter(m -> m.getRoom().getRoomId().equals(req.getRoomId()));

        // 증가 방향으로만 갱신
        Long current = part.getLastReadMsgId();
        if (current == null || req.getLastReadMessageId() > current) {
            part.setLastReadMsgId(req.getLastReadMessageId());
        }

        // 읽은 사람 수 (UI 마크용) — 필요 없다면 0으로 두어도 됨
        // long readBy = chatPartRepository.countReadersOfMessage(req.getRoomId(), part.getLastReadMsgId());

        // 이벤트 구성
        MessageReadEvent evt = MessageReadEvent.of(
            req.getRoomId(),
            readerUserId,
            part.getLastReadMsgId(),
            0,              // roomUnreadCount (선택)
            0               // readByCount (선택)
        );
        // 선택: lastMessageDate 채우기 (프론트의 when 폴백에 도움)
        optMsg.ifPresent(m -> evt.setLastMessageDate(m.getCreatedDate().toString()));

        // 방 토픽 브로드캐스트(방 안 UI용)
        eventProducer.messageRead(evt);

        // ✅ 유저 단일 토픽에도 발행 (리스트 갱신용; 프론트는 type/unreadCount로 처리)
        eventProducer.messageReadToUser(evt, readerUserId);
    }
}
