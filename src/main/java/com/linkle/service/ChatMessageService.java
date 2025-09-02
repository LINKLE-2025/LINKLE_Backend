package com.linkle.service;

import com.linkle.domain.dto.MessageCreatedEvent;
import com.linkle.domain.dto.MessageResponseDTO;
import com.linkle.domain.dto.SendMessageRequestDTO;
import com.linkle.domain.entity.ChatMessage;
import com.linkle.domain.entity.ChatRoom;
import com.linkle.domain.entity.MessageType;
import com.linkle.domain.entity.User;
import com.linkle.repository.ChatMessageRepository;
import com.linkle.repository.ChatPartRepository;
import com.linkle.repository.ChatRoomRepository;
import com.linkle.repository.UserRepository;
import com.linkle.util.ChatEventProducer;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatPartRepository chatPartRepository;
    private final UserRepository userRepository;
    private final ChatEventProducer eventProducer; // STOMP 발행 유틸

    /**
     * 메시지 발송 + 브로드캐스트
     */
    @Transactional
    public MessageResponseDTO send(SendMessageRequestDTO req, Long senderUserId) {
        ChatRoom room = chatRoomRepository.findById(req.getRoomId())
            .orElseThrow(() -> new EntityNotFoundException("Room not found: " + req.getRoomId()));

        // 방 멤버인지 검증
        chatPartRepository.findByRoom_RoomIdAndUser_UserId(room.getRoomId(), senderUserId)
            .orElseThrow(() -> new IllegalStateException("Not a member of room: " + room.getRoomId()));

        // 내용/타입
        String content = Optional.ofNullable(req.getContent())
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .orElseThrow(() -> new IllegalArgumentException("메시지 내용이 비어있습니다."));
        MessageType type = req.getMessageType() == null ? MessageType.TEXT : req.getMessageType();

        // 보낸 사람(User) 로딩 (TEXT 기준). SYSTEM이면 null 허용하려면 엔티티 nullable=true 필요
        User sender = (type == MessageType.SYSTEM) ? null : userRepository.getReferenceById(senderUserId);

        // 저장
        ChatMessage saved = chatMessageRepository.save(
            ChatMessage.builder()
                .room(room)
                .userId(sender)
                .type(type)
                .text(content)
                .build()
        );

        // 이벤트/응답에 보낼 보낸 사람 요약
        Long senderId = (saved.getUserId() != null) ? saved.getUserId().getUserId() : null;
        String senderName = (saved.getUserId() != null) ? saved.getUserId().getNickname() : null;
        String senderImage = (saved.getUserId() != null) ? saved.getUserId().getImage() : null;

        // 브로드캐스트
        MessageCreatedEvent event = MessageCreatedEvent.builder()
            .messageId(saved.getMessageId())
            .roomId(room.getRoomId())
            .messageType(saved.getType())
            .text(saved.getText())
            .createdDate(saved.getCreatedDate())
            .senderId(senderId)
            .senderName(senderName)
            .senderImage(senderImage)
            .build();
        eventProducer.messageCreated(event);

        // 클라이언트 응답
        return MessageResponseDTO.builder()
            .messageId(saved.getMessageId())
            .roomId(room.getRoomId())
            .messageType(saved.getType())
            .content(saved.getText())
            .createdDate(saved.getCreatedDate())
            .senderId(senderId)
            .senderName(senderName)
            .senderImage(senderImage)
            .build();
    }

    /**
     * 메시지 목록 조회 (최신 → 과거, beforeId 커서)
     */
    @Transactional(readOnly = true)
    public List<MessageResponseDTO> listMessages(Long roomId, Long beforeMessageId, int pageSize) {
        Slice<ChatMessage> slice = (beforeMessageId == null)
            ? chatMessageRepository.findByRoom_RoomIdOrderByMessageIdDesc(roomId, PageRequest.of(0, pageSize))
            : chatMessageRepository.findByRoom_RoomIdAndMessageIdLessThanOrderByMessageIdDesc(
            roomId, beforeMessageId, PageRequest.of(0, pageSize));

        // ManyToOne 기본 fetch(EAGER)라면 추가 로딩 없이 userid 접근 가능.
        // 만약 LAZY로 바꾸면 fetch join 쿼리로 최적화 필요.
        return slice.getContent().stream()
            .map(m -> {
                User su = m.getUserId();
                return MessageResponseDTO.builder()
                    .messageId(m.getMessageId())
                    .roomId(m.getRoom().getRoomId())
                    .messageType(m.getType())
                    .content(m.getText())
                    .createdDate(m.getCreatedDate())
                    .senderId(su != null ? su.getUserId() : null)
                    .senderName(su != null ? su.getNickname() : null)
                    .senderImage(su != null ? su.getImage() : null)
                    .build();
            })
            .toList();
    }
}
