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

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatPartRepository chatPartRepository;
    private final UserRepository userRepository;
    private final ChatEventProducer eventProducer;

    @Transactional
    public MessageResponseDTO send(SendMessageRequestDTO req, Long senderUserId) {
        ChatRoom room = chatRoomRepository.findById(req.getRoomId())
            .orElseThrow(() -> new EntityNotFoundException("Room not found: " + req.getRoomId()));

        chatPartRepository.findByRoom_RoomIdAndUser_UserId(room.getRoomId(), senderUserId)
            .orElseThrow(() -> new IllegalStateException("Not a member of room: " + room.getRoomId()));

        MessageType type = (req.getMessageType() == null) ? MessageType.TEXT : req.getMessageType();

        User sender = userRepository.getReferenceById(senderUserId);
        String body = req.ensuredText();

        ChatMessage saved = chatMessageRepository.save(
            ChatMessage.builder()
                .room(room)
                .userId(sender)
                .type(type)
                .text(body)
                .build()
        );

        // 이벤트 DTO
        MessageCreatedEvent event = MessageCreatedEvent.builder()
            .messageId(saved.getMessageId())
            .roomId(room.getRoomId())
            .messageType(saved.getType())
            .text(saved.getText())
            .createdDate(saved.getCreatedDate())
            .senderId(sender.getUserId())
            .senderName(sender.getName())
            .senderImage(sender.getImage())
            .build();

        // 방 토픽 브로드캐스트
        eventProducer.messageCreated(event);

        // ✅ 유저 단일 토픽 발행 (리스트 갱신)
        var memberUserIds = chatPartRepository.findUserIdsByRoomId(room.getRoomId());
        eventProducer.messageCreatedToUsers(event, memberUserIds);

        return MessageResponseDTO.builder()
            .messageId(saved.getMessageId())
            .roomId(room.getRoomId())
            .messageType(saved.getType())
            .text(saved.getText())
            .createdDate(saved.getCreatedDate())
            .senderId(sender.getUserId())
            .senderName(sender.getName())
            .senderImage(sender.getImage())
            .build();
    }

    @Transactional(readOnly = true)
    public List<MessageResponseDTO> listMessages(Long roomId, Long beforeMessageId, int pageSize) {
        Slice<ChatMessage> slice = (beforeMessageId == null)
            ? chatMessageRepository.findByRoom_RoomIdOrderByMessageIdDesc(roomId, PageRequest.of(0, pageSize))
            : chatMessageRepository.findByRoom_RoomIdAndMessageIdLessThanOrderByMessageIdDesc(
            roomId, beforeMessageId, PageRequest.of(0, pageSize));

        return slice.getContent().stream()
            .map(m -> {
                User su = m.getUserId();
                return MessageResponseDTO.builder()
                    .messageId(m.getMessageId())
                    .roomId(m.getRoom().getRoomId())
                    .messageType(m.getType())
                    .text(m.getText())
                    .createdDate(m.getCreatedDate())
                    .senderId(su != null ? su.getUserId() : null)
                    .senderName(su != null ? su.getName() : null)
                    .senderImage(su != null ? su.getImage() : null)
                    .build();
            })
            .toList();
    }
}
