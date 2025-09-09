package com.linkle.service;

import com.linkle.domain.dto.CreateRoomRequestDTO;
import com.linkle.domain.dto.OpenDmRequestDTO;
import com.linkle.domain.dto.RoomResponseDTO;
import com.linkle.domain.entity.*;
import com.linkle.repository.ChatMessageRepository;
import com.linkle.repository.ChatPartRepository;
import com.linkle.repository.ChatRoomRepository;
import com.linkle.repository.LinkerRepository;
import com.linkle.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatPartRepository chatPartRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final LinkerRepository  linkerRepository;

    // ===== 이미지 저장소 =====
    private final S3Client s3Client;
    @Value("${minio.bucket}")
    private String bucketName;

    /** 그룹/클래스 방 생성 (DM은 OpenDmRequest 사용) */
    @Transactional
    public RoomResponseDTO createRoom(CreateRoomRequestDTO req, Long ownerUserId) {
        if (req.getRoomType() == RoomType.DM) {
            throw new IllegalArgumentException("DM은 OpenDmRequest를 사용하세요.");
        }
        Linker linkerRef = null;
        if (req.getLinkerId() != null) {
            // 존재 검증을 하고 싶으면 findById + orElseThrow 사용
            linkerRef = linkerRepository.getReferenceById(req.getLinkerId());
        }


        ChatRoom room = ChatRoom.builder()
            .roomType(req.getRoomType())
            .roomName(req.getRoomName())
            .description(req.getDescription())
            .memo(req.getMemo())
            .themeColor(String.valueOf(req.getThemeColor())) // 숫자코드 or S3 키 문자열
            .entryFee(req.getEntryFee())
            .startDate(req.getStartDate() == null ? null :
                req.getStartDate().atZone(ZoneId.systemDefault()).toInstant())
            .ownerId(ownerUserId)
            .linker(linkerRef)
            .build();

        ChatRoom saved = chatRoomRepository.save(room);

        ChatPart ownerPart = ChatPart.builder()
            .id(new ChatPartId(saved.getRoomId(), ownerUserId))
            .room(saved)
            .user(userRepository.getReferenceById(ownerUserId))
            .alarm(Alarm.ON)
            .build();
        chatPartRepository.save(ownerPart);

        return toRoomResponseWithMeta(saved, ownerUserId);
    }

    /** 1:1 DM 열기/조회 */
    @Transactional
    public RoomResponseDTO openDm(OpenDmRequestDTO req, Long meId) {
        Long targetId = req.getTargetUserId();
        if (Objects.equals(meId, targetId)) {
            throw new IllegalArgumentException("자기 자신과의 DM은 생성할 수 없습니다.");
        }

        User me = userRepository.findById(meId)
            .orElseThrow(() -> new EntityNotFoundException("Current user not found: " + meId));
        User partner = userRepository.findById(targetId)
            .orElseThrow(() -> new EntityNotFoundException("Target user not found: " + targetId));

        List<ChatRoom> myRooms = chatRoomRepository.findActiveRoomsByUserId(meId);
        Optional<ChatRoom> existingDm = myRooms.stream()
            .filter(r -> r.getRoomType() == RoomType.DM)
            .filter(r -> chatPartRepository.findActiveByRoomIdWithUser(r.getRoomId())
                .stream().anyMatch(cp -> cp.getUser().getUserId().equals(targetId)))
            .findFirst();

        ChatRoom dmRoom;
        if (existingDm.isPresent()) {
            dmRoom = existingDm.get();
        } else {
            dmRoom = chatRoomRepository.save(ChatRoom.builder()
                .roomType(RoomType.DM)
                .roomName(null)
                .ownerId(null)
                .build());

            ChatPart mePart = ChatPart.builder()
                .id(new ChatPartId(dmRoom.getRoomId(), me.getUserId()))
                .room(dmRoom)
                .user(me)
                .alarm(Alarm.ON)
                .build();

            ChatPart partnerPart = ChatPart.builder()
                .id(new ChatPartId(dmRoom.getRoomId(), partner.getUserId()))
                .room(dmRoom)
                .user(partner)
                .alarm(Alarm.ON)
                .build();

            chatPartRepository.saveAll(List.of(mePart, partnerPart));
        }

        return toDmRoomResponse(dmRoom, me.getUserId(), partner);
    }

    /** 방 단건 조회 + 메타 포함 */
    @Transactional(readOnly = true)
    public RoomResponseDTO getRoomWithMeta(Long roomId, Long meId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new EntityNotFoundException("Room not found: " + roomId));
        return toRoomResponseWithMeta(room, meId);
    }

    @Transactional(readOnly = true)
    public Optional<ChatRoom> findById(Long roomId) {
        return chatRoomRepository.findById(roomId);
    }


    /** 내가 참여 중인 방 목록 */
    @Transactional(readOnly = true)
    public List<RoomResponseDTO> listMyRooms(Long meId) {
        List<ChatRoom> rooms = chatRoomRepository.findActiveRoomsByUserId(meId);
        return rooms.stream()
            .map(r -> toRoomResponseWithMeta(r, meId))
            .toList();
    }

    // ============================== 내부 유틸 ==============================

    private RoomResponseDTO toRoomResponseWithMeta(ChatRoom room, Long meId) {
        if (room.getRoomType() == RoomType.DM) {
            List<ChatPart> parts = chatPartRepository.findActiveByRoomIdWithUser(room.getRoomId());
            ChatPart partnerPart = parts.stream()
                .filter(p -> !p.getUser().getUserId().equals(meId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("DM 파트너를 찾을 수 없습니다."));
            User partner = partnerPart.getUser();
            return toDmRoomResponse(room, meId, partner);
        } else {
            var lastMsg = chatMessageRepository
                .findTopByRoom_RoomIdOrderByMessageIdDesc(room.getRoomId())
                .orElse(null);
            var myPartOpt = chatPartRepository
                .findByRoom_RoomIdAndUser_UserId(room.getRoomId(), meId);
            Long lastReadId = myPartOpt.map(ChatPart::getLastReadMsgId).orElse(null);
            int unread = computeUnread(room.getRoomId(), lastReadId);
            int members = (int) chatPartRepository.countByRoom_RoomIdAndLeftDateIsNull(room.getRoomId());
            return RoomResponseDTO.fromEntity(room, lastMsg, unread, members);
        }
    }

    private RoomResponseDTO toDmRoomResponse(ChatRoom room, Long meId, User partner) {
        var lastMsg = chatMessageRepository
            .findTopByRoom_RoomIdOrderByMessageIdDesc(room.getRoomId())
            .orElse(null);
        var myPartOpt = chatPartRepository
            .findByRoom_RoomIdAndUser_UserId(room.getRoomId(), meId);
        Long lastReadId = myPartOpt.map(ChatPart::getLastReadMsgId).orElse(null);
        int unread = computeUnread(room.getRoomId(), lastReadId);

        return RoomResponseDTO.fromDm(
            room,
            partner.getUserId(),
            partner.getName(),
            partner.getImage(),
            lastMsg,
            unread
        );
    }

    private int computeUnread(Long roomId, Long lastReadId) {
        if (lastReadId == null) {
            return (int) chatMessageRepository.countByRoom_RoomId(roomId);
        }
        return (int) chatMessageRepository
            .countByRoom_RoomIdAndMessageIdGreaterThan(roomId, lastReadId);
    }
}
