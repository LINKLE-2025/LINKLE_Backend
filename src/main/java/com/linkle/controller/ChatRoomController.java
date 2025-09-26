// src/main/java/com/linkle/controller/ChatRoomController.java
package com.linkle.controller;

import com.linkle.domain.dto.*;
import com.linkle.domain.entity.ChatPart;
import com.linkle.domain.entity.ChatRoom;
import com.linkle.repository.ChatMessageRepository;
import com.linkle.repository.ChatPartRepository;
import com.linkle.repository.ChatRoomRepository;
import com.linkle.service.*;
import com.linkle.util.StompDestinations;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final ChatReadService chatReadService;
    private final UnreadService unreadService;

    private final ChatPartRepository chatPartRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;

    // S3/MinIO 다운로드 재사용
    private final ProfileService profileService;
    private final UserBalanceService userBalanceService;

    /** 그룹/클래스 방 생성 (JSON) - 기존 그대로 유지 */
    @PostMapping("/room")
    public RoomResponseDTO createRoom(@Valid @RequestBody CreateRoomRequestDTO body,
        HttpServletRequest req) {
        Long me = currentUserId(req);
        return chatRoomService.createRoom(body, me);
    }

    /** 그룹/클래스 방 생성 (멀티파트 + 배경 이미지 업로드) */
    @PostMapping(value = "/room", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RoomResponseDTO createRoomWithBackground(
        @RequestPart("dto") @Valid CreateRoomRequestDTO body,
        @RequestPart(value = "background", required = false) MultipartFile background,
        HttpServletRequest req
    ) throws IOException {
        Long me = currentUserId(req);
        // 1) 우선 방 생성
        RoomResponseDTO dto = chatRoomService.createRoom(body, me);
        // 2) 배경이 왔다면 업로드 + 반영
        if (background != null && !background.isEmpty()) {
            chatRoomService.updateRoomBackground(dto.getRoomId(), background);
            // 최신 상태로 재조회
            dto = chatRoomService.getRoomWithMeta(dto.getRoomId(), me);
        }
        return dto;
    }

    /** 1:1 DM 열기/조회 */
    @PostMapping("/room/dm")
    public RoomResponseDTO openDm(@Valid @RequestBody OpenDmRequestDTO body,
        HttpServletRequest req) {
        Long me = currentUserId(req);
        return chatRoomService.openDm(body, me);
    }

    /** 방 단건 조회 */
    @GetMapping("/room/{roomId}")
    public RoomResponseDTO getRoom(@PathVariable Long roomId,
        HttpServletRequest req) {
        Long me = currentUserId(req);
        return chatRoomService.getRoomWithMeta(roomId, me);
    }

    /** 내가 참여 중인 방 목록 */
    @GetMapping("/room")
    public List<RoomResponseDTO> myRooms(HttpServletRequest req) {
        Long me = currentUserId(req);
        return chatRoomService.listMyRooms(me);
    }

    /** 특정 방의 멤버 목록 */
    @GetMapping("/room/{roomId}/members")
    public List<MemberResponseDTO> roomMembers(@PathVariable Long roomId) {
        List<ChatPart> parts = chatPartRepository.findActiveByRoomIdWithUser(roomId);
        return parts.stream().map(MemberResponseDTO::fromEntity).toList();
    }

    /** 특정 방의 메시지 목록 */
    @GetMapping("/room/{roomId}/messages")
    public List<MessageResponseDTO> roomMessages(
        @PathVariable Long roomId,
        @RequestParam(required = false) Long beforeId,
        @RequestParam(required = false, defaultValue = "20") Integer size,
        @RequestHeader("x-user-id") Long meId
    ) {
        int pageSize = (size == null || size <= 0) ? 20 : size;
        return chatMessageService.listMessages(roomId, meId, beforeId, pageSize);
    }

    /** 방의 멤버별 마지막 읽음 상태(초기 로딩용) */
    @GetMapping("/room/{roomId}/reads")
    public List<MyReadStateDTO> roomReads(@PathVariable Long roomId) {
        return chatReadService.listRoomReads(roomId);
    }


    /** 읽음 동기화 */
    @PostMapping("/read")
    public void syncRead(@Valid @RequestBody ReadSyncRequestDTO body,
        HttpServletRequest req) {
        Long me = currentUserId(req);
        chatReadService.syncRead(body, me);

    }

    /** 내 전체/방별 미확인 수 요약 */
    @GetMapping("/unread")
    public UnreadCountResponseDTO unreadSummary(HttpServletRequest req) {
        Long me = currentUserId(req);
        return unreadService.unreadSummary(me);
    }

    /** 특정 링커의 모든 방(내가 멤버가 아닐 수도 있음) */
    @GetMapping("/room/by-linker")
    public List<RoomResponseDTO> roomsByLinker(@RequestParam Long linkerId, HttpServletRequest req) {
        Long me = currentUserId(req);
        return chatRoomService.listRoomsByLinker(linkerId, me);
    }

    /** 톡 배경화면(업로드 key 또는 색상 아이콘 파일)을 스트리밍 */
    @GetMapping("/view/background/{roomId}")
    public ResponseEntity<byte[]> viewRoomBackground(@PathVariable Long roomId) throws IOException {
        Optional<ChatRoom> room = chatRoomService.findById(roomId);
        if (room.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        String key = room.get().getThemeColor();
        byte[] data = profileService.downloadFile(key);

        return ResponseEntity.ok()
            .contentType(resolveMediaType(key))
            .body(data);
    }

    @GetMapping("/view/color/{name}")
    public ResponseEntity<byte[]> viewColor(@PathVariable String name) throws IOException {
        String normalized = name.toLowerCase();
        List<String> allowed = List.of("red", "orange", "yellow", "green", "blue", "purple");
        if (!normalized.matches("^[a-z]+$") || !allowed.contains(normalized)) {
            return ResponseEntity.badRequest().build();
        }
        String key = "color/" + normalized + ".png";
        byte[] data = profileService.downloadFile(key);
        return ResponseEntity.ok()
            .contentType(resolveMediaType(key))
            .header("Cache-Control", "public, max-age=86400")
            .body(data);
    }

    /** 방 배경 교체 전용 (이미 생성된 방) */
    @PatchMapping(value = "/room/{roomId}/background", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RoomResponseDTO updateRoomBackground(
        @PathVariable Long roomId,
        @RequestPart("background") MultipartFile background,
        HttpServletRequest req
    ) throws IOException {
        chatRoomService.updateRoomBackground(roomId, background);
        return chatRoomService.getRoomWithMeta(roomId, currentUserId(req));
    }

    @PostMapping("room/{roomId}/leave")
    public ResponseEntity<Void> leave(
        @PathVariable Long roomId,
        @RequestHeader("x-user-id") Long meId
    ) {
        chatRoomService.leaveRoom(roomId, meId);
        return ResponseEntity.noContent().build();
    }

    // ---- helpers ----
    private MediaType resolveMediaType(String key) {
        String k = key.toLowerCase();
        if (k.endsWith(".png"))  return MediaType.IMAGE_PNG;
        if (k.endsWith(".jpg") || k.endsWith(".jpeg")) return MediaType.IMAGE_JPEG;
        if (k.endsWith(".gif"))  return MediaType.IMAGE_GIF;
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    private Long currentUserId(HttpServletRequest req) {
        String h = req.getHeader("x-user-id");
        if (h != null && !h.isBlank()) return Long.parseLong(h);
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            try { return Long.parseLong(auth.getName()); } catch (Exception ignore) {}
        }
        return 1L; // 개발용 기본값
    }

    /** 방참가하기 — (기존 그대로) */
    @PostMapping("/room/{roomId}/join")
    public ResponseEntity<?> join(@PathVariable Long roomId, HttpServletRequest req) {
        Long me = currentUserId(req);
        ChatRoom room = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));

        Long newBalance = null;
        if (room.getRoomType() != null && "CLASS".equalsIgnoreCase(String.valueOf(room.getRoomType()))) {
            int fee = room.getEntryFee() != null ? room.getEntryFee() : 0;
            if (fee > 0) {
                Long ownerId = room.getOwnerId();
                if (ownerId == null) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "error", "방 주인이 설정되지 않은 클래스 방입니다."
                    ));
                }
                try {
                    long afterMe = userBalanceService.updateBalance(
                        me, -fee, (room.getRoomName() != null ? room.getRoomName() : "") + " 입장료 차감"
                    );
                    long afterOwner = userBalanceService.updateBalance(
                        ownerId, fee, (room.getRoomName() != null ? room.getRoomName() : "") + " 입장료 입금"
                    );
                    newBalance = afterMe;
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "error", "잔액 부족으로 입장 불가",
                        "need", fee
                    ));
                }
            }
        }

        RoomResponseDTO dto = chatRoomService.joinRoom(roomId, me);
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("msg", "입장 성공");
        response.put("room", dto);
        if (newBalance != null) response.put("balance", newBalance);
        return ResponseEntity.ok(response);
    }
}
