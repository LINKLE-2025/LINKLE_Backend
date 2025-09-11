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

        chatPartRepository.findByRoom_RoomIdAndUser_UserId(room.getRoomId(), senderUserId)
            .orElseThrow(() -> new IllegalStateException("Not a member of room: " + room.getRoomId()));

        MessageType type = (req.getMessageType() == null) ? MessageType.TEXT : req.getMessageType();

        //  프록시로 User 채우기 (엔티티 필드는 userId 유지)
        User sender = userRepository.getReferenceById(senderUserId);

        // text만 사용
        String body = req.ensuredText();

        ChatMessage saved = chatMessageRepository.save(
            ChatMessage.builder()
                .room(room)
                .userId(sender)        // 필드명 userId 유지
                .type(type)
                .text(body)            // DB에도 text
                .build()
        );

        Long sid = saved.getUserId().getUserId();
        String sname = saved.getUserId().getName();
        String simg = saved.getUserId().getImage();

        // 브로드캐스트 이벤트도 text
        MessageCreatedEvent event = MessageCreatedEvent.builder()
            .messageId(saved.getMessageId())
            .roomId(room.getRoomId())
            .messageType(saved.getType())
            .text(saved.getText())
            .createdDate(saved.getCreatedDate())
            .senderId(sid)
            .senderName(sname)
            .senderImage(simg)
            .build();
        eventProducer.messageCreated(event);

        // REST 응답도 text로 맞출 거면 .text(...) 사용
        return MessageResponseDTO.builder()
            .messageId(saved.getMessageId())
            .roomId(room.getRoomId())
            .messageType(saved.getType())
            .text(saved.getText())        // ← content 말고 text로 통일
            .createdDate(saved.getCreatedDate())
            .senderId(sid)
            .senderName(sname)
            .senderImage(simg)
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
