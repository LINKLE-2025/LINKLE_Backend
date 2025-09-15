package com.linkle.service;

import com.linkle.domain.dto.MessageCreatedEvent;
import com.linkle.domain.dto.MessageResponseDTO;
import com.linkle.domain.dto.SendMessageRequestDTO;
import com.linkle.domain.entity.*;
import com.linkle.repository.ChatMessageRepository;
import com.linkle.repository.ChatPartRepository;
import com.linkle.repository.ChatRoomRepository;
import com.linkle.repository.UserRepository;
import com.linkle.repository.DmPairRepository;
import com.linkle.util.ChatEventProducer;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatPartRepository chatPartRepository;
    private final UserRepository userRepository;
    private final ChatEventProducer eventProducer;
    private final DmPairRepository dmPairRepository;

    /** 일반 메시지 전송 (DM: 보낸 사람/상대 모두 자동 복구) */
    @Transactional
    public MessageResponseDTO send(SendMessageRequestDTO req, Long senderUserId) {
        ChatRoom room = chatRoomRepository.findById(req.getRoomId())
            .orElseThrow(() -> new EntityNotFoundException("Room not found: " + req.getRoomId()));

        Instant now = Instant.now();

        // --- 1) 보낸 사람 파트 보장/복구 ---
        var myPartOpt = chatPartRepository.findByRoom_RoomIdAndUser_UserId(room.getRoomId(), senderUserId);
        if (myPartOpt.isEmpty() || myPartOpt.get().getLeftDate() != null) {
            if (room.getRoomType() == RoomType.DM) {
                if (myPartOpt.isPresent()) {
                    ChatPart p = myPartOpt.get();
                    p.setLeftDate(null);
                    p.setJoinedDate(now);                 // ★ 재입장 갱신
                    chatPartRepository.save(p);
                } else {
                    ChatPart newPart = ChatPart.builder()
                        .id(new ChatPartId(room.getRoomId(), senderUserId))
                        .room(room)
                        .user(userRepository.getReferenceById(senderUserId))
                        .alarm(Alarm.ON)
                        .joinedDate(now)                   // ★ 최초 참여 시간
                        .build();
                    chatPartRepository.save(newPart);
                }
            } else {
                throw new IllegalStateException("Not a member of room: " + room.getRoomId());
            }
        }

        // --- 2) DM이면 상대 파트도 보장/복구 ---
        if (room.getRoomType() == RoomType.DM) {
            DmPair pair = dmPairRepository.findByRoom_RoomId(room.getRoomId())
                .orElseThrow(() -> new IllegalStateException("DM pair not found for room: " + room.getRoomId()));
            Long partnerId = pair.getUserAId().equals(senderUserId) ? pair.getUserBId() : pair.getUserAId();

            var partnerPartOpt = chatPartRepository.findByRoom_RoomIdAndUser_UserId(room.getRoomId(), partnerId);
            if (partnerPartOpt.isPresent()) {
                ChatPart pp = partnerPartOpt.get();
                if (pp.getLeftDate() != null) {
                    pp.setLeftDate(null);
                    pp.setJoinedDate(now);               // ★ 자동 복귀 시 갱신
                    chatPartRepository.save(pp);
                }
            } else {
                ChatPart partnerPart = ChatPart.builder()
                    .id(new ChatPartId(room.getRoomId(), partnerId))
                    .room(room)
                    .user(userRepository.getReferenceById(partnerId))
                    .alarm(Alarm.ON)
                    .joinedDate(now)                     // ★ 최초 참여 시간
                    .build();
                chatPartRepository.save(partnerPart);
            }
        }

        // --- 3) 메시지 저장/브로드캐스트 ---
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

        eventProducer.messageCreated(event);
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

    /** 시스템 메시지(입장/퇴장 등) */
    @Transactional
    public MessageResponseDTO sendSystem(Long roomId, String text) {
        ChatRoom room = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new EntityNotFoundException("Room not found: " + roomId));

        ChatMessage saved = chatMessageRepository.save(
            ChatMessage.builder()
                .room(room)
                .userId(null)
                .type(MessageType.SYSTEM)
                .text(text)
                .build()
        );

        MessageCreatedEvent event = MessageCreatedEvent.builder()
            .messageId(saved.getMessageId())
            .roomId(room.getRoomId())
            .messageType(MessageType.SYSTEM)
            .text(saved.getText())
            .createdDate(saved.getCreatedDate())
            .senderId(null)
            .senderName(null)
            .senderImage(null)
            .build();

        eventProducer.messageCreated(event);
        var memberUserIds = chatPartRepository.findUserIdsByRoomId(room.getRoomId());
        eventProducer.messageCreatedToUsers(event, memberUserIds);

        return MessageResponseDTO.builder()
            .messageId(saved.getMessageId())
            .roomId(room.getRoomId())
            .messageType(MessageType.SYSTEM)
            .text(saved.getText())
            .createdDate(saved.getCreatedDate())
            .senderId(null)
            .senderName(null)
            .senderImage(null)
            .build();
    }

    /** joinedDate를 고려한 메시지 목록 조회 */
    @Transactional(readOnly = true)
    public List<MessageResponseDTO> listMessages(Long roomId, Long meId, Long beforeMessageId, int pageSize) {
        Instant joinedAt = chatPartRepository.findByRoom_RoomIdAndUser_UserId(roomId, meId)
            .map(ChatPart::getJoinedDate)
            .orElse(null);

        Slice<ChatMessage> slice;
        if (joinedAt == null) {
            slice = (beforeMessageId == null)
                ? chatMessageRepository.findByRoom_RoomIdOrderByMessageIdDesc(roomId, PageRequest.of(0, pageSize))
                : chatMessageRepository.findByRoom_RoomIdAndMessageIdLessThanOrderByMessageIdDesc(
                roomId, beforeMessageId, PageRequest.of(0, pageSize));
        } else {
            slice = (beforeMessageId == null)
                ? chatMessageRepository.findByRoom_RoomIdAndCreatedDateAfterOrderByMessageIdDesc(
                roomId, joinedAt, PageRequest.of(0, pageSize))
                : chatMessageRepository.findByRoom_RoomIdAndCreatedDateAfterAndMessageIdLessThanOrderByMessageIdDesc(
                roomId, joinedAt, beforeMessageId, PageRequest.of(0, pageSize));
        }

        return slice.getContent().stream()
            .map(m -> {
                User su = m.getUserId(); // SYSTEM이면 null
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
