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
        // 더 이상 예외를 던지지 않음. 방 멤버가 아니면 조용히 무시.
        Optional<ChatPart> partOpt =
            chatPartRepository.findByRoom_RoomIdAndUser_UserId(req.getRoomId(), readerUserId);

        if (partOpt.isEmpty()) {
            // 로그만 남기고 종료 (운영 안정성)
            // log.debug("syncRead ignored: user {} not a member of room {}", readerUserId, req.getRoomId());
            return;
        }

        ChatPart part = partOpt.get();

        // (옵션) lastReadMessageId가 해당 방의 메시지인지 검증하고, 아니면 무시
        Optional<ChatMessage> optMsg = Optional.empty();
        if (req.getLastReadMessageId() != null) {
            optMsg = chatMessageRepository.findById(req.getLastReadMessageId())
                .filter(m -> m.getRoom().getRoomId().equals(req.getRoomId()));
        }

        // 증가 방향으로만 갱신
        if (req.getLastReadMessageId() != null) {
            Long current = part.getLastReadMsgId();
            if (current == null || req.getLastReadMessageId() > current) {
                // 메시지가 해당 방 소속이 아니면 갱신하지 않음
                if (optMsg.isPresent()) {
                    part.setLastReadMsgId(req.getLastReadMessageId());
                }
            }
        }

        // 이벤트 구성 (roomUnreadCount/readByCount는 필요 시 계산해서 넣거나 0 유지)
        MessageReadEvent evt = MessageReadEvent.of(
            req.getRoomId(),
            readerUserId,
            part.getLastReadMsgId(),
            0, // roomUnreadCount (선택)
            0  // readByCount (선택)
        );
        // 프론트 시간 폴백 보강
        optMsg.ifPresent(m -> evt.setLastMessageDate(m.getCreatedDate().toString()));

        // 방 토픽 브로드캐스트(방 UI)
        eventProducer.messageRead(evt);
        // 유저 단일 토픽에도 발행 (리스트 갱신)
        eventProducer.messageReadToUser(evt, readerUserId);
    }
}
