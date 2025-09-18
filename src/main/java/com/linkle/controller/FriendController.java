package com.linkle.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.linkle.domain.dto.FriendDTO;
import com.linkle.domain.dto.FriendResponseDTO;
import com.linkle.domain.entity.Friend;
import com.linkle.repository.UserRepository;
import com.linkle.service.FriendService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/friend")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;
    private final UserRepository userRepository;

    // 친구 요청 보내기(insert)
    @PostMapping
    public FriendDTO createFriend(@RequestBody FriendDTO dto) {
        return friendService.createFriend(dto);
    }

    // 받은 요청 조회
    @GetMapping("/received")
    public List<FriendResponseDTO> getReceivedRequests(@RequestParam("user_id2") Long userId) {
        List<Friend> friends = friendService.getReceivedRequests(userId);
        List<FriendResponseDTO> data = new ArrayList<>();
        for (Friend friend : friends) {
            data.add(FriendResponseDTO.fromEntity(friend, userId));
        }
        return data;
    }

    // 보낸 요청 조회
    @GetMapping("/sent")
    public List<FriendResponseDTO> getSentRequests(@RequestParam("user_id1") Long userId) {
        List<Friend> friends = friendService.getSentRequests(userId);
        List<FriendResponseDTO> data = new ArrayList<>();
        for (Friend friend : friends) {
            data.add(FriendResponseDTO.fromEntity(friend, userId));
        }
        return data;
    }

    // 요청 수락(update)
    @PutMapping("/{friendId}/reception")
    public FriendDTO acceptFriend(@PathVariable Long friendId) {
        return friendService.acceptFriend(friendId);
    }

    // 요청 거절(delete)
    @DeleteMapping("/refusal")
    public void refusalFriend(@RequestBody FriendDTO dto) {
        friendService.refusalFriend(dto);
    }

    // 친구 목록 조회 단순화(fetch join 구조)
    @GetMapping("/{userId}")
    public List<FriendResponseDTO> getFriendList(@PathVariable Long userId) {
        return friendService.getFriendEntities(userId);
    }

    // 친구 삭제
    @DeleteMapping("/{friendId}")
    public void deleteFriend(@PathVariable Long friendId) {
        friendService.deleteFriend(friendId);
    }

    // 친구 관계 조회
    @GetMapping("/relationship")
    public ResponseEntity<?> getFriendRelationship(
        @RequestParam Long userId1,
        @RequestParam Long userId2
    ) {
        Friend friend = friendService.getFriendRelationship(userId1, userId2);

        if (friend == null) {
            return ResponseEntity.ok(Map.of(
                "exists", false,
                "state", "NONE"
            ));
        }

        return ResponseEntity.ok(Map.of(
            "exists", true,
            "friendId", friend.getFriendId(),
            "state", friend.getState(),  // ACCEPTED / PENDING / REJECTED
            "userId1", friend.getUser1().getUserId(),
            "userId2", friend.getUser2().getUserId()
        ));
    }

}
