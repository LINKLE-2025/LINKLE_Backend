package com.linkle.controller;

import java.util.ArrayList;
import java.util.List;

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
import com.linkle.domain.dto.UserResponseDTO;
import com.linkle.domain.entity.User;
import com.linkle.repository.UserRepository;
import com.linkle.service.FriendService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/friend")
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
    public List<UserResponseDTO> getReceivedRequests(@RequestParam("user_id2") Long userId) {
        List<Long> friend = friendService.getReceivedRequests(userId);
        List<UserResponseDTO> data = new ArrayList<>();
        for (Long usersId : friend) {
            User user = userRepository.findById(usersId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));
            data.add(UserResponseDTO.fromEntity(user));
        }
        return data;
    }

    // 보낸 요청 조회
    @GetMapping("/sent")
    public List<UserResponseDTO> getSentRequests(@RequestParam("user_id1") Long userId) {
        List<Long> friend = friendService.getSentRequests(userId);
        List<UserResponseDTO> data = new ArrayList<>();
        for (Long usersId : friend) {
            User user = userRepository.findById(usersId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));
            data.add(UserResponseDTO.fromEntity(user));
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

    // 친구 목록 조회
    @GetMapping("/{userId}")
    public List<UserResponseDTO> getFriendList(@PathVariable Long userId) {
        List<Long> friend = friendService.getFriendList(userId);
        List<UserResponseDTO> data = new ArrayList<>();
        for (Long usersId : friend) {
            User user = userRepository.findById(usersId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));
            data.add(UserResponseDTO.fromEntity(user));
        }
        return data;
    }

    // 친구 삭제
    @DeleteMapping("/{friendId}")
    public void deleteFriend(@PathVariable Long friendId){
        friendService.deleteFriend(friendId);
    }

}
