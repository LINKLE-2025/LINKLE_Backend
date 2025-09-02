package com.linkle.controller;

import java.util.List;

import com.linkle.domain.dto.UserRequestDTO;
import com.linkle.domain.dto.UserResponseDTO;
import com.linkle.service.ProfileService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final ProfileService profileService;

    // 유저 조회
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUserProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.getUserProfile(userId));
    }

    // 유저 수정
    @PatchMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> updateUserProfile(
        @PathVariable Long userId,
        @RequestBody UserRequestDTO dto) {
        return ResponseEntity.ok(profileService.updateUserProfile(userId, dto));
    }

    // 유저 삭제
    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        profileService.deleteUserProfile(userId);
    }

    // 유저 생성
    @PostMapping
    public UserResponseDTO insertUser(@RequestBody UserRequestDTO dto){
        return profileService.insertUser(dto);
    }

    // 모든 유저 조회
    @GetMapping
    public List<UserResponseDTO> getAllUsers(){
        return profileService.getAllUsers();
    }
}
