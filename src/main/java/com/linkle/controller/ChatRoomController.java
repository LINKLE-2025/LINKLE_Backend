package com.linkle.controller;

import com.linkle.domain.dto.*;
import com.linkle.domain.entity.ChatPart;
import com.linkle.domain.entity.ChatRoom;
import com.linkle.repository.ChatMessageRepository;
import com.linkle.repository.ChatPartRepository;
import com.linkle.repository.ChatRoomRepository;
import com.linkle.service.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/chat")
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

    /** 그룹/클래스 방 생성 */
    @PostMapping("/room")
    public RoomResponseDTO createRoom(@Valid @RequestBody CreateRoomRequestDTO body,
        HttpServletRequest req) {
        Long me = currentUserId(req);
        return chatRoomService.createRoom(body, me);
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
    public List<MessageResponseDTO> roomMessages(@PathVariable Long roomId,
        @RequestParam(required = false) Long beforeId,
        @RequestParam(defaultValue = "20") int size) {
        return chatMessageService.listMessages(roomId, beforeId, size);
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

    /** 톡 배경화면 */
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
}
