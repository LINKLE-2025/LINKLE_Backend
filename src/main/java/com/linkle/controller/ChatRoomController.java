package com.linkle.controller;

import com.linkle.domain.dto.CreateRoomRequest;
import com.linkle.domain.dto.MemberResponse;
import com.linkle.domain.dto.MessageResponse;
import com.linkle.domain.dto.OpenDmRequest;
import com.linkle.domain.dto.ReadSyncRequest;
import com.linkle.domain.dto.RoomResponse;
import com.linkle.domain.dto.UnreadCountResponse;
import com.linkle.service.ChatMessageService;
import com.linkle.service.ChatReadService;
import com.linkle.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Validated
public class ChatRoomController {

    private final ChatRoomService roomService;
    private final ChatMessageService messageService;
    private final ChatReadService readService;

    /** [POST /chat/room] 그룹/클래스 방 생성 (CHATTING_ROOM) */
    @PostMapping("/room")
    public RoomResponse createRoom(@RequestBody CreateRoomRequest req) {
        return roomService.createRoom(req);
    }

    /** [POST /chat/room/dm] 1:1 DM 방 열기/조회 (DM_PAIR + CHATTING_ROOM) */
    @PostMapping("/room/dm")
    public RoomResponse openDm(@RequestBody OpenDmRequest req) {
        return roomService.openDm(req);
    }

    /** [GET /chat/room/{roomId}] 방 상세 (CHATTING_ROOM) */
    @GetMapping("/room/{roomId}")
    public RoomResponse getRoom(@PathVariable Long roomId) {
        return roomService.getRoom(roomId);
    }

    /** [GET /chat/room/{roomId}/members] 방 멤버 목록 (CHAT_PART) */
    @GetMapping("/room/{roomId}/members")
    public Page<MemberResponse> members(@PathVariable Long roomId, Pageable pageable) {
        return roomService.members(roomId, pageable);
    }

    /** [GET /chat/room] 내가 속한 방 목록 (CHAT_PART join CHATTING_ROOM) */
    @GetMapping("/room")
    public Page<RoomResponse> myRooms(Pageable pageable) {
        return roomService.myRooms(pageable);
    }

    /** [POST /chat/room/{roomId}/connect] 방 입장 (CHAT_PART.joined_at 업데이트 + SYSTEM 메시지 생성) */
    @PostMapping("/room/{roomId}/connect")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void connect(@PathVariable Long roomId) {
        roomService.connect(roomId);
    }

    /** [POST /chat/room/{roomId}/disconnect] 방 퇴장 (CHAT_PART.left_at 업데이트 + SYSTEM 메시지 생성) */
    @PostMapping("/room/{roomId}/disconnect")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disconnect(@PathVariable Long roomId) {
        roomService.disconnect(roomId);
    }

    /** [GET /chat/room/{roomId}/messages] 메시지 페이징 조회 (CHAT_MESSAGE) */
    @GetMapping("/room/{roomId}/messages")
    public Page<MessageResponse> messages(@PathVariable Long roomId, Pageable pageable) {
        return messageService.page(roomId, pageable);
    }

    /** [POST /chat/read] 읽음 동기화 (CHAT_PART.last_read_msg_id 업데이트) */
    @PostMapping("/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void syncRead(@RequestBody ReadSyncRequest req) {
        readService.sync(req);
    }

    /** [GET /chat/unread?room_id=] 미확인 개수 조회 (CHAT_MESSAGE vs CHAT_PART.last_read_msg_id) */
    @GetMapping("/unread")
    public UnreadCountResponse unread(@RequestParam("room_id") Long roomId) {
        return readService.unreadCount(roomId);
    }
}
