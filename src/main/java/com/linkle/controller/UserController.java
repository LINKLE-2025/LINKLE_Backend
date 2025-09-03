package com.linkle.controller;

import java.io.IOException;
import java.util.List;

import com.linkle.domain.dto.UserLinkerCountDTO;
import com.linkle.domain.dto.UserParticipateLinkerDTO;
import com.linkle.domain.dto.UserRequestDTO;
import com.linkle.domain.dto.UserResponseDTO;

import com.linkle.service.ProfileService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final ProfileService profileService;

    // 유저 조회
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUserProfile(@PathVariable Long userId) {
        UserResponseDTO dto = profileService.getUserProfile(userId);
        return ResponseEntity.ok(dto);
    }

    //링커 조회
    @PostMapping("/linker/list")
    public ResponseEntity<List<UserParticipateLinkerDTO>> getUserLinker(@RequestBody UserResponseDTO res) {
        return ResponseEntity.ok(profileService.getUserLineker(res.getUserId()));
    }

    //링커 통계 조회
    @PostMapping("/linker/history")
    public ResponseEntity<List<UserLinkerCountDTO>> getUserCount(@RequestBody UserResponseDTO res) {
        return ResponseEntity.ok(profileService.getUserCount(res.getUserId()));
    }

    // 유저 수정
    // MediaType은 기본 제공되는 파일 타입들이 존재
    // RequestPart는 일반적으로 RequestParm은 JSON 형태로 가져오지만 Media 형태는 문자열이 아니기 때문에 이를 해결하기 위해 RequestPart를 활용함
    @PatchMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponseDTO> updateUserProfile(
        @PathVariable Long userId,
        @RequestPart(value = "dto") UserRequestDTO dto,
        @RequestPart(value = "profile", required = false) MultipartFile profile,
        @RequestPart(value = "background", required = false) MultipartFile background) throws IOException {

        // 프로필 이미지
        if (profile != null && !profile.isEmpty()) {
            dto.setImage(profileService.uploadProfileImage(profile, userId));
        }
        // 프로필 배경이미지
        if (background != null && !background.isEmpty()) {
            dto.setBackground(profileService.uploadBackgroundImage(background, userId));
        }
        return ResponseEntity.ok(profileService.updateUserProfile(userId, dto));
    }

    // 유저 삭제
    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        profileService.deleteUserProfile(userId);
    }

    // 유저 생성
    @PostMapping
    public UserResponseDTO insertUser(@RequestBody UserRequestDTO dto) {
        return profileService.insertUser(dto);
    }

    // 모든 유저 조회
    @GetMapping
    public List<UserResponseDTO> getAllUsers() {
        return profileService.getAllUsers();
    }

    // 미디어 타입 판별
    private MediaType resolveMediaType(String key) {
        if (key != null && key.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        } else if (key != null && (key.endsWith(".jpg") || key.endsWith(".jpeg"))) {
            return MediaType.IMAGE_JPEG;
        } else {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }


    // 프로필 이미지 조회
    @GetMapping("/view/profile/{userId}")
    public ResponseEntity<byte[]> viewProfile(@PathVariable Long userId) throws IOException {
        UserResponseDTO dto = profileService.getUserProfile(userId);
        String key = dto.getImage();
        byte[] data = profileService.downloadFile(key);
        return ResponseEntity.ok()
            .contentType(resolveMediaType(key))
            .body(data);
    }

    // 배경이미지 조회
    @GetMapping("/view/background/{userId}")
    public ResponseEntity<byte[]> viewBackground(@PathVariable Long userId) throws IOException {
        UserResponseDTO dto = profileService.getUserProfile(userId);
        String key = dto.getBackground();
        byte[] data = profileService.downloadFile(key);
        return ResponseEntity.ok()
            .contentType(resolveMediaType(key))
            .body(data);
    }


}
