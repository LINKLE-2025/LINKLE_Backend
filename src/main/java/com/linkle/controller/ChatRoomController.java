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
    public List<MessageResponseDTO> roomMessages(
        @PathVariable Long roomId,
        @RequestParam(required = false) Long beforeId,
        @RequestParam(required = false, defaultValue = "20") Integer size,
        @RequestHeader("x-user-id") Long meId  // ★ 추가
    ) {
        int pageSize = (size == null || size <= 0) ? 20 : size;
        return chatMessageService.listMessages(roomId, meId, beforeId, pageSize); // ★ meId 전달
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
    /**채팅방 보이게 하기*/
    @GetMapping("/room/by-linker")
    public List<RoomResponseDTO> roomsByLinker(@RequestParam Long linkerId, HttpServletRequest req) {
        Long me = currentUserId(req);
        return chatRoomService.listRoomsByLinker(linkerId, me);
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
    @GetMapping("/view/color/{name}")
    public ResponseEntity<byte[]> viewColor(@PathVariable String name) throws IOException {
        // 1) 입력 검증: 영문 소문자만 허용하고 화이트리스트로 한 번 더 막기
        String normalized = name.toLowerCase();
        List<String> allowed = List.of("red", "orange", "yellow", "green", "blue", "purple");
        if (!normalized.matches("^[a-z]+$") || !allowed.contains(normalized)) {
            return ResponseEntity.badRequest().build();
        }

        // 2) MinIO Key 구성: color/red.png
        String key = "color/" + normalized + ".png";

        // 3) 다운로드
        byte[] data = profileService.downloadFile(key);

        // 4) 캐시 헤더(선택)
        return ResponseEntity.ok()
            .contentType(resolveMediaType(key))
            .header("Cache-Control", "public, max-age=86400") // 1일 캐시
            .body(data);
    }

    @PostMapping("room/{roomId}/leave")
    public ResponseEntity<Void> leave(
        @PathVariable Long roomId,
        @RequestHeader("x-user-id") Long meId
    ) {
        chatRoomService.leaveRoom(roomId, meId);
        return ResponseEntity.noContent().build(); // 204
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



    /** 방참가하기 */
    @PostMapping("/room/{roomId}/join")
    public ResponseEntity<?> join(@PathVariable Long roomId, HttpServletRequest req) {
        Long me = currentUserId(req);

        System.out.println("💡 [JOIN] 송금하기 백단으로 왔나? userId=" + me);

        // 1. 톡방 정보 조회
        ChatRoom room = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));

        System.out.println("💡 roomType=" + room.getRoomType() +
            ", entryFee=" + room.getEntryFee() +
            ", ownerId=" + room.getOwnerId());

        Long newBalance = null;

        // 2. 클래스 톡방이면 입장료 송금
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
                    // 송금 (참여자 → 방 주인)
                    long afterMe = userBalanceService.updateBalance(
                        me,
                        -fee,
                        (room.getRoomName() != null ? room.getRoomName() : "") + " 입장료 차감"
                    );
                    long afterOwner = userBalanceService.updateBalance(
                        ownerId,
                        fee,
                        (room.getRoomName() != null ? room.getRoomName() : "") + " 입장료 입금"
                    );

                    System.out.println("💡 balance 업데이트 완료: 참여자=" + afterMe + ", 방주인=" + afterOwner);

                    // 참가자 새 잔액 확인
                    newBalance = afterMe;

                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "error", "잔액 부족으로 입장 불가",
                        "need", fee
                    ));
                }
            }
        }

        // 3. 참가 처리
        RoomResponseDTO dto = chatRoomService.joinRoom(roomId, me);

        // 4. 응답 생성
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("msg", "입장 성공");
        response.put("room", dto);
        if (newBalance != null) {
            response.put("balance", newBalance);
        }

        return ResponseEntity.ok(response);
    }

}
