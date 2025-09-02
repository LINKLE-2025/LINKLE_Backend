package com.linkle.service;

import com.linkle.domain.dto.MessageResponse;
import com.linkle.domain.dto.MessageCreatedEvent;
import com.linkle.domain.dto.SendMessageRequest;
import com.linkle.domain.entity.ChatMessage;
import com.linkle.domain.entity.ChatRoom;
import com.linkle.domain.entity.MessageType;
import com.linkle.repository.ChatMessageRepository;
import com.linkle.repository.ChatPartRepository;
import com.linkle.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatPartRepository chatPartRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 메시지 발송 + 브로드캐스트
     */
    @Transactional
    public MessageResponse send(SendMessageRequest req, Long senderUserId) {
        ChatRoom room = chatRoomRepository.findById(req.getRoomId())
            .orElseThrow(() -> new EntityNotFoundException("Room not found: " + req.getRoomId()));

        // (선택) 방 멤버인지 검증
        chatPartRepository.findByRoom_RoomIdAndUser_UserId(room.getRoomId(), senderUserId)
            .orElseThrow(() -> new IllegalStateException("Not a member of room: " + room.getRoomId()));

        // SYSTEM 메시지는 서버 내부 로직에서만 생성하도록 가정
        MessageType type = req.getMessageType() == null ? MessageType.TEXT : req.getMessageType();

        ChatMessage saved = chatMessageRepository.save(
            ChatMessage.builder()
                .room(room)
                .senderId(senderUserId)      // sender 엔티티/프록시는 엔티티 설계에 맞춰 세팅
                .messageType(type)
                .text(req.getContent())
                .build()
        );

        // 브로드캐스트
        MessageCreatedEvent event = MessageCreatedEvent.forBroadcast(saved);
        messagingTemplate.convertAndSend("/sub/room." + room.getRoomId(), event);

        return MessageResponse.fromEntity(saved);
    }

    /**
     * 메시지 목록 조회 (최신부터 페이지). beforeId(커서) 있으면 그 이전 메시지.
     */
    @Transactional(readOnly = true)
    public List<MessageResponse> listMessages(Long roomId, Long beforeMessageId, int pageSize) {
        Slice<ChatMessage> slice;
        if (beforeMessageId == null) {
            slice = chatMessageRepository.findByRoom_RoomIdOrderByMessageIdDesc(roomId, PageRequest.of(0, pageSize));
        } else {
            slice = chatMessageRepository.findByRoom_RoomIdAndMessageIdLessThanOrderByMessageIdDesc(roomId, beforeMessageId, PageRequest.of(0, pageSize));
        }
        return slice.stream().map(MessageResponse::fromEntity).toList();
    }
}
