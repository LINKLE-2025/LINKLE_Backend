package com.linkle.service;

import com.linkle.domain.dto.MessageReadEvent;
import com.linkle.domain.dto.ReadSyncRequest;
import com.linkle.domain.entity.ChatPart;
import com.linkle.repository.ChatMessageRepository;
import com.linkle.repository.ChatPartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatReadService {

    private final ChatPartRepository chatPartRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 읽음 동기화
     * - lastReadMessageId를 증가 방향으로만 갱신
     * - 방별 내 미확인 개수 + 해당 메시지의 읽은 사람 수를 계산해 이벤트로 push
     */
    @Transactional
    public void syncRead(ReadSyncRequest req, Long readerUserId) {
        ChatPart part = chatPartRepository.findByRoom_RoomIdAndUser_UserId(req.getRoomId(), readerUserId)
            .orElseThrow(() -> new IllegalStateException("Not a member of room: " + req.getRoomId()));

        // 증가 방향으로만 갱신
        Long current = part.getLastReadMsgId();
        if (current == null || req.getLastReadMessageId() > current) {
            part.setLastReadMsgId(req.getLastReadMessageId());
        }

        // 읽은 사람 수 (마크/인디케이터용)
        int readBy = (int) chatPartRepository.countReadersOfMessage(req.getRoomId(), req.getLastReadMessageId());

        // 내 방 미확인 수
        int roomUnread = (int) chatMessageRepository.countByRoom_RoomIdAndMessageIdGreaterThan(
            req.getRoomId(), part.getLastReadMsgId() == null ? -1L : part.getLastReadMsgId());

        // 브로드캐스트
        MessageReadEvent evt = MessageReadEvent.of(
            req.getRoomId(), readerUserId, part.getLastReadMsgId(), roomUnread, readBy);
        messagingTemplate.convertAndSend("/sub/room." + req.getRoomId(), evt);
    }
}
