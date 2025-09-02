package com.linkle.service;

import com.linkle.domain.dto.CreateRoomRequest;
import com.linkle.domain.dto.RoomResponse;
import com.linkle.domain.entity.*;
import com.linkle.repository.ChatMessageRepository;
import com.linkle.repository.ChatPartRepository;
import com.linkle.repository.ChatRoomRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatPartRepository chatPartRepository;
    private final ChatMessageRepository chatMessageRepository;

    /**
     * 그룹/클래스 방 생성
     * - DM 생성은 별도 OpenDm 로직 사용
     */
    @Transactional
    public RoomResponse createRoom(CreateRoomRequest req, Long ownerUserId) {
        if (req.getRoomType() == RoomType.DM) {
            throw new IllegalArgumentException("DM은 OpenDmRequest를 사용하세요.");
        }

        ChatRoom room = ChatRoom.builder()
            .roomType(req.getRoomType())
            .roomName(req.getRoomName())
            .description(req.getDescription())
            .memo(req.getMemo())
            .themeColor(String.valueOf(req.getThemeColor())) // Integer -> String
            .entryFee(req.getEntryFee())
            .startDate(req.getStartDate() == null ? null :
                req.getStartDate().atZone(ZoneId.systemDefault()).toInstant())
            .ownerId(ownerUserId)
            .build();

        ChatRoom saved = chatRoomRepository.save(room);

        // 방장 입장(OWNER)
        ChatPart part = ChatPart.builder()
            .id(new ChatPartId(saved.getRoomId(), ownerUserId))
            .room(saved)
            // .user는 @MapsId("userId") 로 매핑되어 있어야 함. User 로딩은 필요 시 fetch join 사용.
            .alarm(Alarm.ACTIVE)
            .build();
        chatPartRepository.save(part);

        return RoomResponse.fromEntity(saved);
    }

    /** 단건 조회 + 메타 채워서 반환 (리스트/상세 공용) */
    @Transactional(readOnly = true)
    public RoomResponse getRoomWithMeta(Long roomId, Long meId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new EntityNotFoundException("Room not found: " + roomId));

        // 최신 메시지
        var lastMsgOpt = chatMessageRepository.findTopByRoom_RoomIdOrderByMessageIdDesc(roomId);
        var lastMsg = lastMsgOpt.orElse(null);

        // 내 미확인 수
        var myPartOpt = chatPartRepository.findByRoom_RoomIdAndUser_UserId(roomId, meId);
        Long lastReadId = myPartOpt.map(ChatPart::getLastReadMsgId).orElse(null);
        int unread = (lastReadId == null)
            ? (int) chatMessageRepository.countByRoom_RoomId(roomId)
            : (int) chatMessageRepository.countByRoom_RoomIdAndMessageIdGreaterThan(roomId, lastReadId);

        // 멤버 수
        int memberCount = (int) chatPartRepository.countByRoom_RoomIdAndLeftDateIsNull(roomId);

        return RoomResponse.fromEntity(room, lastMsg, unread, memberCount);
    }

    /** 내가 속한 방 목록 (탈퇴하지 않은) */
    @Transactional(readOnly = true)
    public List<RoomResponse> listMyRooms(Long meId) {
        List<ChatRoom> rooms = chatRoomRepository.findActiveRoomsByUserId(meId);
        return rooms.stream()
            .map(r -> {
                var lastMsg = chatMessageRepository.findTopByRoom_RoomIdOrderByMessageIdDesc(r.getRoomId()).orElse(null);
                var part = chatPartRepository.findByRoom_RoomIdAndUser_UserId(r.getRoomId(), meId).orElse(null);
                Long lastRead = part == null ? null : part.getLastReadMsgId();
                int unread = (lastRead == null)
                    ? (int) chatMessageRepository.countByRoom_RoomId(r.getRoomId())
                    : (int) chatMessageRepository.countByRoom_RoomIdAndMessageIdGreaterThan(r.getRoomId(), lastRead);
                int members = (int) chatPartRepository.countByRoom_RoomIdAndLeftDateIsNull(r.getRoomId());
                return RoomResponse.fromEntity(r, lastMsg, unread, members);
            })
            .toList();
    }
}
