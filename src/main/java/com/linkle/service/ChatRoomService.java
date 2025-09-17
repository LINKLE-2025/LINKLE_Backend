// src/main/java/com/linkle/service/ChatRoomService.java
package com.linkle.service;

import com.linkle.domain.dto.CreateRoomRequestDTO;
import com.linkle.domain.dto.OpenDmRequestDTO;
import com.linkle.domain.dto.RoomResponseDTO;
import com.linkle.domain.entity.*;
import com.linkle.repository.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
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
            // 배경은 숫자(1~6) 아이콘 이름 또는 업로드 key를 themeColor에 저장
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
            .joinedDate(Instant.now())
            .build();
        chatPartRepository.save(ownerPart);

        return toRoomResponseWithMeta(saved, ownerUserId);
    }

    /** 방 배경 업로드 + themeColor를 업로드 key로 교체 */
    @Transactional
    public void updateRoomBackground(Long roomId, MultipartFile background) throws IOException {
        if (background == null || background.isEmpty()) return;

        ChatRoom room = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new EntityNotFoundException("Room not found: " + roomId));

        String original = background.getOriginalFilename();
        String ext = (original != null && original.contains(".")) ? original.substring(original.lastIndexOf('.') + 1) : "png";
        String key = "room_" + roomId + "." + ext;

        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(background.getContentType())
                .build(),
            RequestBody.fromInputStream(background.getInputStream(), background.getSize())
        );

        // 업로드 성공 시 themeColor를 업로드 key로 덮어씀
        room.setThemeColor(key);
        chatRoomRepository.save(room);
    }

    /** 1:1 DM 열기/조회 — DmPair로 단일 방 보장 */
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

        long s = Math.min(meId, targetId);
        long g = Math.max(meId, targetId);

        ChatRoom dmRoom;

        Optional<DmPair> pairOpt = dmPairRepository.findBySmallerIdAndGreaterId(s, g);
        if (pairOpt.isPresent()) {
            dmRoom = pairOpt.get().getRoom();

            var myPartOpt = chatPartRepository.findByRoom_RoomIdAndUser_UserId(dmRoom.getRoomId(), meId);
            if (myPartOpt.isPresent()) {
                ChatPart p = myPartOpt.get();
                if (p.getLeftDate() != null) {
                    p.setLeftDate(null);
                    p.setJoinedDate(Instant.now());
                    chatPartRepository.save(p);
                }
            } else {
                ChatPart mePart = ChatPart.builder()
                    .id(new ChatPartId(dmRoom.getRoomId(), me.getUserId()))
                    .room(dmRoom)
                    .user(me)
                    .alarm(Alarm.ON)
                    .joinedDate(Instant.now())
                    .build();
                chatPartRepository.save(mePart);
            }

            var partnerPartOpt = chatPartRepository.findByRoom_RoomIdAndUser_UserId(dmRoom.getRoomId(), partner.getUserId());
            if (partnerPartOpt.isEmpty()) {
                ChatPart partnerPart = ChatPart.builder()
                    .id(new ChatPartId(dmRoom.getRoomId(), partner.getUserId()))
                    .room(dmRoom)
                    .user(partner)
                    .alarm(Alarm.ON)
                    .joinedDate(Instant.now())
                    .build();
                chatPartRepository.save(partnerPart);
            }
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
                .joinedDate(Instant.now())
                .build();
            ChatPart partnerPart = ChatPart.builder()
                .id(new ChatPartId(dmRoom.getRoomId(), partner.getUserId()))
                .room(dmRoom)
                .user(partner)
                .alarm(Alarm.ON)
                .joinedDate(Instant.now())
                .build();
            chatPartRepository.saveAll(List.of(mePart, partnerPart));

            try {
                dmPairRepository.save(DmPair.builder()
                    .room(dmRoom)
                    .userAId(meId)
                    .userBId(targetId)
                    .build());
            } catch (DataIntegrityViolationException e) {
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
            .map(r -> safeToRoomResponse(r, meId))
            .toList();
    }


    // ============================== 내부 유틸 ==============================
    // 예외 안전: 방 하나가 문제여도 목록 전체가 죽지 않도록 폴백 제공
    private RoomResponseDTO safeToRoomResponse(ChatRoom room, Long meId) {
        try {
            return toRoomResponseWithMeta(room, meId);
        } catch (Exception e) {
            RoomResponseDTO dto = new RoomResponseDTO();
            dto.setRoomId(room.getRoomId());
            dto.setRoomType(room.getRoomType());
            dto.setRoomName(
                (room.getRoomName() != null && !room.getRoomName().isBlank())
                    ? room.getRoomName()
                    : "(알 수 없음)"
            );
            dto.setLastMessagePreview("(정보를 불러오지 못했습니다)");
            // dto.setLastMessageDate(...) 는 타입 충돌 방지를 위해 설정하지 않음 (null)
            dto.setUnreadCount(0);
            return dto;
        }
    }


    private RoomResponseDTO toRoomResponseWithMeta(ChatRoom room, Long meId) {
        if (room.getRoomType() == RoomType.DM) {
            // 1) 활성 파트에서 파트너 먼저 찾기
            List<ChatPart> parts = chatPartRepository.findActiveByRoomIdWithUser(room.getRoomId());
            User partnerUser = null;
            for (ChatPart p : parts) {
                if (!p.getUser().getUserId().equals(meId)) {
                    partnerUser = p.getUser();
                    break;
                }
            }

            // 2) 활성 파트에 없으면, 퇴장 포함 전체 파트에서 찾기
            if (partnerUser == null) {
                List<ChatPart> allParts = chatPartRepository.findAllByRoomIdWithUser(room.getRoomId());
                for (ChatPart p : allParts) {
                    if (!p.getUser().getUserId().equals(meId)) {
                        partnerUser = p.getUser();
                        break;
                    }
                }
            }

            // 3) 그래도 없으면, DmPair 시도하되 "던지지 말고" 옵션 처리
            if (partnerUser == null) {
                var pairOpt = dmPairRepository.findByRoom_RoomId(room.getRoomId());
                if (pairOpt.isPresent()) {
                    var pair = pairOpt.get();
                    Long partnerId = Objects.equals(pair.getUserAId(), meId) ? pair.getUserBId() : pair.getUserAId();
                    partnerUser = userRepository.findById(partnerId).orElse(null);
                }
            }

            // 4) 마지막 폴백: 완전 없으면 의도된 placeholder로 표시(탈퇴/정리된 경우)
            Long partnerIdForDto;
            String partnerNameForDto;
            String partnerImageForDto;
            String partnerNickForDto;
            String partnerGenderForDto;

            if (partnerUser != null) {
                partnerIdForDto   = partnerUser.getUserId();
                partnerNameForDto = partnerUser.getName();
                partnerImageForDto= partnerUser.getImage();
                partnerNickForDto = partnerUser.getNickname();
                partnerGenderForDto = partnerUser.getGender();
            } else {
                // 프론트가 안전하게 처리하도록 0/placeholder 사용
                partnerIdForDto   = 0L;
                partnerNameForDto = "(탈퇴한 사용자)";
                partnerImageForDto= null;
                partnerNickForDto = null;
                partnerGenderForDto = null;
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
                partnerIdForDto,
                partnerNameForDto,
                partnerImageForDto,
                partnerNickForDto,
                partnerGenderForDto,
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
            partner.getGender(),
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
            .filter(p -> p.getLeftDate() == null)   // leftDate 체크 추가
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
            dto.setIsMember(isMember); // 정확한 isMember 전달
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

        var existingOpt = chatPartRepository.findByRoom_RoomIdAndUser_UserId(roomId, userId);

        boolean joinedNow = false;

        if (existingOpt.isPresent()) {
            ChatPart p = existingOpt.get();

            // 이미 참여중이면 아무 것도 하지 않고 조용히 반환 (중복 알림 방지)
            if (p.getLeftDate() == null) {
                return toRoomResponseWithMeta(room, userId);
            }

            // 재참여
            p.setLeftDate(null);
            p.setJoinedDate(Instant.now());
            chatPartRepository.save(p);
            joinedNow = true;

        } else {
            // 신규 참여
            ChatPart newPart = ChatPart.builder()
                .id(new ChatPartId(roomId, userId))
                .room(room)
                .user(userRepository.getReferenceById(userId))
                .alarm(Alarm.ON)
                .joinedDate(Instant.now())
                .build();
            chatPartRepository.save(newPart);
            joinedNow = true;
        }

        //실제로 '지금' 참여(신규/재참여)한 경우에만 시스템 메시지
        if (joinedNow && room.getRoomType() != RoomType.DM) {
            String name = userRepository.getReferenceById(userId).getName();
            chatMessageService.sendSystem(roomId, name + " 님이 입장하였습니다");
        }

        return toRoomResponseWithMeta(room, userId);
    }


    @PersistenceContext
    private EntityManager em;

    /** 방 나가기. 그룹/클래스에서만 시스템 메시지 발행 */
    @Transactional
    public void leaveRoom(Long roomId, Long userId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new EntityNotFoundException("Room not found: " + roomId));

        ChatPart part = chatPartRepository
            .findByRoom_RoomIdAndUser_UserId(roomId, userId)
            .orElseThrow(() -> new IllegalStateException("You are not a member of this room."));

        if (part.getLeftDate() != null) return;

        part.setLeftDate(Instant.now());
        chatPartRepository.save(part);

        if (room.getRoomType() != RoomType.DM) {
            String name = userRepository.getReferenceById(userId).getName();
            chatMessageService.sendSystem(roomId, name + " 님이 퇴장하였습니다");
        }

        // 카운트 정확히 보기 위해 플러시
        em.flush();

        long active = chatPartRepository.countByRoom_RoomIdAndLeftDateIsNull(roomId);
        if (active == 0) {
            // 부모만 삭제하면 ChatPart/ChatMessage/DmPair도 함께 제거됨
            chatRoomRepository.delete(room);
        }
    }

}
