package com.linkle.service;

import com.linkle.domain.dto.CreateRoomRequestDTO;
import com.linkle.domain.dto.OpenDmRequestDTO;
import com.linkle.domain.dto.RoomResponseDTO;
import com.linkle.domain.entity.*;
import com.linkle.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;

import java.time.Instant;
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
    private final LinkerRepository linkerRepository;
    private final ChatMessageService chatMessageService;

    // DM 단일방 보장용
    private final DmPairRepository dmPairRepository;

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
        var linkerRef = (req.getLinkerId() != null) ? linkerRepository.getReferenceById(req.getLinkerId()) : null;

        ChatRoom room = ChatRoom.builder()
            .roomType(req.getRoomType())
            .roomName(req.getRoomName())
            .description(req.getDescription())
            .memo(req.getMemo())
            .themeColor(String.valueOf(req.getThemeColor()))
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
            .joinedDate(Instant.now()) // ★ 최초 참여 시점 기록
            .build();
        chatPartRepository.save(ownerPart);

        return toRoomResponseWithMeta(saved, ownerUserId);
    }

    /** 1:1 DM 열기/조회 — DmPair로 단일 방 보장 */
    @Transactional
    public RoomResponseDTO openDm(OpenDmRequestDTO req, Long meId) {
        Long targetId = req.getTargetUserId();
        if (Objects.equals(meId, targetId)) {
            throw new IllegalArgumentException("자기 자신과의 DM은 생성할 수 없습니다.");
        }

        // 사용자 로딩
        User me = userRepository.findById(meId)
            .orElseThrow(() -> new EntityNotFoundException("Current user not found: " + meId));
        User partner = userRepository.findById(targetId)
            .orElseThrow(() -> new EntityNotFoundException("Target user not found: " + targetId));

        long s = Math.min(meId, targetId);
        long g = Math.max(meId, targetId);

        ChatRoom dmRoom;

        // 1) pair 조회 (이미 있으면 그 방 재사용)
        Optional<DmPair> pairOpt = dmPairRepository.findBySmallerIdAndGreaterId(s, g);
        if (pairOpt.isPresent()) {
            dmRoom = pairOpt.get().getRoom();

            // 내가 과거에 나갔으면 복구, 없으면 파트 생성
            var myPartOpt = chatPartRepository.findByRoom_RoomIdAndUser_UserId(dmRoom.getRoomId(), meId);
            if (myPartOpt.isPresent()) {
                ChatPart p = myPartOpt.get();
                if (p.getLeftDate() != null) {
                    p.setLeftDate(null);
                    p.setJoinedDate(Instant.now());   // 재입장 시 갱신
                    chatPartRepository.save(p);
                }
            } else {
                ChatPart mePart = ChatPart.builder()
                    .id(new ChatPartId(dmRoom.getRoomId(), me.getUserId()))
                    .room(dmRoom)
                    .user(me)
                    .alarm(Alarm.ON)
                    .joinedDate(Instant.now())        // 신규 생성 시각
                    .build();
                chatPartRepository.save(mePart);
            }

            // 상대 파트도 보장(이상 상태 대비)
            var partnerPartOpt = chatPartRepository.findByRoom_RoomIdAndUser_UserId(dmRoom.getRoomId(), partner.getUserId());
            if (partnerPartOpt.isEmpty()) {
                ChatPart partnerPart = ChatPart.builder()
                    .id(new ChatPartId(dmRoom.getRoomId(), partner.getUserId()))
                    .room(dmRoom)
                    .user(partner)
                    .alarm(Alarm.ON)
                    .joinedDate(Instant.now())        // ★ 누락 대비
                    .build();
                chatPartRepository.save(partnerPart);
            }
        } else {
            // 2) 없으면 새 방 + pair 생성 (경합 시 유니크 예외 처리)
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
                .joinedDate(Instant.now())            // ★ 최초 참여
                .build();
            ChatPart partnerPart = ChatPart.builder()
                .id(new ChatPartId(dmRoom.getRoomId(), partner.getUserId()))
                .room(dmRoom)
                .user(partner)
                .alarm(Alarm.ON)
                .joinedDate(Instant.now())            // ★ 최초 참여
                .build();
            chatPartRepository.saveAll(List.of(mePart, partnerPart));

            try {
                dmPairRepository.save(DmPair.builder()
                    .room(dmRoom)
                    .userAId(meId)
                    .userBId(targetId)
                    .build()); // @PrePersist에서 smaller/greater 세팅
            } catch (DataIntegrityViolationException e) {
                // 경쟁 상황: 기존 pair 재조회하여 그 방 사용
                dmRoom = dmPairRepository.findBySmallerIdAndGreaterId(s, g)
                    .map(DmPair::getRoom)
                    .orElse(dmRoom);
            }
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
            // 1) 활성 파트에서 파트너 찾아보기
            List<ChatPart> parts = chatPartRepository.findActiveByRoomIdWithUser(room.getRoomId());
            User partnerUser = null;
            for (ChatPart p : parts) {
                if (!p.getUser().getUserId().equals(meId)) {
                    partnerUser = p.getUser();
                    break;
                }
            }

            // 2) 못 찾았으면 DmPair로 복구해서 User 로드 (상대가 나간 상태 등)
            if (partnerUser == null) {
                DmPair pair = dmPairRepository.findByRoom_RoomId(room.getRoomId())
                    .orElseThrow(() -> new IllegalStateException("DM pair not found for room " + room.getRoomId()));
                Long partnerId = Objects.equals(pair.getUserAId(), meId) ? pair.getUserBId() : pair.getUserAId();
                partnerUser = userRepository.findById(partnerId)
                    .orElseThrow(() -> new EntityNotFoundException("Partner user not found: " + partnerId));
            }

            var lastMsg = chatMessageRepository
                .findTopByRoom_RoomIdOrderByMessageIdDesc(room.getRoomId())
                .orElse(null);
            var myPartOpt = chatPartRepository
                .findByRoom_RoomIdAndUser_UserId(room.getRoomId(), meId);
            Long lastReadId = myPartOpt.map(ChatPart::getLastReadMsgId).orElse(null);
            int unread = computeUnread(room.getRoomId(), lastReadId);

            return RoomResponseDTO.fromDm(
                room,
                partnerUser.getUserId(),
                partnerUser.getName(),
                partnerUser.getImage(),
                partnerUser.getNickname(),
                lastMsg,
                unread
            );
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
            partner.getNickname(),
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

    @Transactional(readOnly = true)
    public List<RoomResponseDTO> listRoomsByLinker(Long linkerId, Long meId) {
        List<ChatRoom> rooms = chatRoomRepository.findByLinker_LinkerIdOrderByCreatedDateDesc(linkerId);

        return rooms.stream()
            .filter(r -> r.getRoomType() != RoomType.DM)
            .map(r -> toRoomResponseAllowNonMember(r, meId))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<RoomResponseDTO> listRoomsByLinkerAndType(Long linkerId, RoomType type, Long meId) {
        List<ChatRoom> rooms = chatRoomRepository.findRoomsByLinkerIdAndOptionalType(linkerId, type);
        return rooms.stream()
            .filter(r -> r.getRoomType() != RoomType.DM)
            .map(r -> toRoomResponseAllowNonMember(r, meId))
            .toList();
    }

    private RoomResponseDTO toRoomResponseAllowNonMember(ChatRoom room, Long meId) {
        boolean isMember = chatPartRepository
            .findByRoom_RoomIdAndUser_UserId(room.getRoomId(), meId)
            .isPresent();

        var lastMsg = chatMessageRepository
            .findTopByRoom_RoomIdOrderByMessageIdDesc(room.getRoomId())
            .orElse(null);

        int members = (int) chatPartRepository
            .countByRoom_RoomIdAndLeftDateIsNull(room.getRoomId());

        int unread = 0;
        if (isMember) {
            Long lastReadId = chatPartRepository
                .findByRoom_RoomIdAndUser_UserId(room.getRoomId(), meId)
                .map(ChatPart::getLastReadMsgId)
                .orElse(null);
            unread = computeUnread(room.getRoomId(), lastReadId);
        }

        RoomResponseDTO dto = RoomResponseDTO.fromEntity(room, lastMsg, unread, members);
        try {
            var linker = room.getLinker();
            var lid = (linker != null ? linker.getLinkerId() : null);
            dto.setLinkerId(lid);
            dto.setIsMember(isMember);
        } catch (Exception ignore) {}
        return dto;
    }

    /** 방 참여(재참여 포함). 그룹/클래스에서만 시스템 메시지 발행 */
    @Transactional
    public RoomResponseDTO joinRoom(Long roomId, Long userId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new EntityNotFoundException("Room not found: " + roomId));

        if (room.getRoomType() == RoomType.DM) {
            throw new IllegalArgumentException("DM room cannot be joined via this API.");
        }

        var existing = chatPartRepository.findByRoom_RoomIdAndUser_UserId(roomId, userId);
        if (existing.isPresent()) {
            ChatPart p = existing.get();
            if (p.getLeftDate() != null) {
                p.setLeftDate(null);                 // 재참여
                p.setJoinedDate(Instant.now());      // ★ 재입장 시간 갱신
                chatPartRepository.save(p);
            }
        } else {
            ChatPart newPart = ChatPart.builder()
                .id(new ChatPartId(roomId, userId))
                .room(room)
                .user(userRepository.getReferenceById(userId))
                .alarm(Alarm.ON)
                .joinedDate(Instant.now())           // ★ 신규 참여 시점
                .build();
            chatPartRepository.save(newPart);
        }

        // SYSTEM 입장 메시지 (그룹/클래스만)
        if (room.getRoomType() != RoomType.DM) {
            String name = userRepository.getReferenceById(userId).getName();
            chatMessageService.sendSystem(roomId, name + " 님이 입장하였습니다");
        }

        return toRoomResponseWithMeta(room, userId);
    }

    /** 방 나가기. 그룹/클래스에서만 시스템 메시지 발행 */
    @Transactional
    public void leaveRoom(Long roomId, Long userId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new EntityNotFoundException("Room not found: " + roomId));

        ChatPart part = chatPartRepository
            .findByRoom_RoomIdAndUser_UserId(roomId, userId)
            .orElseThrow(() -> new IllegalStateException("You are not a member of this room."));

        if (part.getLeftDate() != null) return; // 이미 나간 상태면 무시

        part.setLeftDate(Instant.now());
        chatPartRepository.save(part);

        // SYSTEM 퇴장 메시지 (그룹/클래스만)
        if (room.getRoomType() != RoomType.DM) {
            String name = userRepository.getReferenceById(userId).getName();
            chatMessageService.sendSystem(roomId, name + " 님이 퇴장하였습니다");
        }

        // 남은 멤버 0명이면 방 삭제
        long active = chatPartRepository.countByRoom_RoomIdAndLeftDateIsNull(roomId);
        if (active == 0) {
            chatRoomRepository.delete(room);
        }
    }
}
