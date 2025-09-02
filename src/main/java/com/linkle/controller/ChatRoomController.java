package com.linkle.controller;

import com.linkle.domain.dto.CreateRoomRequestDTO;
import com.linkle.domain.dto.MemberResponseDTO;
import com.linkle.domain.dto.MessageResponseDTO;
import com.linkle.domain.dto.OpenDmRequestDTO;
import com.linkle.domain.dto.ReadSyncRequestDTO;
import com.linkle.domain.dto.RoomResponseDTO;
import com.linkle.domain.dto.UnreadCountResponseDTO;
import com.linkle.domain.entity.ChatPart;
import com.linkle.repository.ChatMessageRepository;
import com.linkle.repository.ChatPartRepository;
import com.linkle.service.ChatMessageService;
import com.linkle.service.ChatReadService;
import com.linkle.service.ChatRoomService;
import com.linkle.service.UnreadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final ChatReadService chatReadService;
    private final UnreadService unreadService;

    // (멤버 목록만 N+1 방지용 fetch join 필요해서 Repository 직접 사용)
    private final ChatPartRepository chatPartRepository;
    private final ChatMessageRepository chatMessageRepository;

    /** 그룹/클래스 방 생성 */
    @PostMapping("/room")
    public RoomResponseDTO createRoom(@Valid @RequestBody CreateRoomRequestDTO req) {
        Long me = currentUserId();
        return chatRoomService.createRoom(req, me);
    }

    /** 1:1 DM 열기/조회 (서비스에 openDm 구현되어 있다고 가정) */
    @PostMapping("/room/dm")
    public RoomResponseDTO openDm(@Valid @RequestBody OpenDmRequestDTO req) {
        Long me = currentUserId();
        // ChatRoomService에 openDm(OpenDmRequest, meId) 메서드가 있어야 합니다.
        return chatRoomService.openDm(req, me);
    }

    /** 방 단건 조회 (+ 최신 메시지 프리뷰/미확인/멤버수 메타 포함) */
    @GetMapping("/room/{roomId}")
    public RoomResponseDTO getRoom(@PathVariable Long roomId) {
        Long me = currentUserId();
        return chatRoomService.getRoomWithMeta(roomId, me);
    }

    /** 내가 참여 중인 방 목록 */
    @GetMapping("/room")
    public List<RoomResponseDTO> myRooms() {
        Long me = currentUserId();
        return chatRoomService.listMyRooms(me);
    }

    /** 특정 방의 멤버 목록 */
    @GetMapping("/room/{roomId}/members")
    public List<MemberResponseDTO> roomMembers(@PathVariable Long roomId) {
        // N+1 방지용 fetch join 메서드 사용
        List<ChatPart> parts = chatPartRepository.findActiveByRoomIdWithUser(roomId);
        return parts.stream()
            .map(MemberResponseDTO::fromEntity)
            .toList();
    }

    /** 특정 방의 메시지 목록 (최신부터, 커서 beforeId로 더 불러오기) */
    @GetMapping("/room/{roomId}/messages")
    public List<MessageResponseDTO> roomMessages(@PathVariable Long roomId,
        @RequestParam(required = false) Long beforeId,
        @RequestParam(defaultValue = "20") int size) {
        return chatMessageService.listMessages(roomId, beforeId, size);
    }

    /** 읽음 동기화 */
    @PostMapping("/read")
    public void syncRead(@Valid @RequestBody ReadSyncRequestDTO req) {
        Long me = currentUserId();
        chatReadService.syncRead(req, me);
    }

    /** 내 전체/방별 미확인 수 요약 */
    @GetMapping("/unread")
    public UnreadCountResponseDTO unreadSummary() {
        Long me = currentUserId();
        return unreadService.unreadSummary(me);
    }

    // ----------------- helpers -----------------
    private Long currentUserId() {
        // 프로젝트의 보안 구성에 맞게 교체하세요.
        // 예: JWT subject가 숫자 userId면 그대로 파싱, 아니면 CustomPrincipal에서 꺼내기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            try { return Long.parseLong(auth.getName()); } catch (NumberFormatException ignore) {}
        }
        // 데모/개발용 fallback
        return 1L;
    }
}
