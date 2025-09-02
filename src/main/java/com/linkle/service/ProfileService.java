package com.linkle.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.linkle.domain.dto.UserRequestDTO;
import com.linkle.domain.dto.UserResponseDTO;
import com.linkle.domain.entity.User;
import com.linkle.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;

    // 유저 조회
    public UserResponseDTO getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        return UserResponseDTO.fromEntity(user);
    }

    // 유저 수정
    public UserResponseDTO updateUserProfile(Long userId, UserRequestDTO dto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        user.setName(dto.getName());
        user.setNickname(dto.getNickname());
        user.setImage(dto.getImage());
        user.setMemo(dto.getMemo());
        user.setGender(dto.getGender());
        userRepository.save(user);
        return UserResponseDTO.fromEntity(user);
    }

    // 유저 삭제
    public void deleteUserProfile(Long userId) {
        userRepository.deleteById(userId);
    }

    // 유저 삽입
    public UserResponseDTO insertUser(UserRequestDTO dto) {
        User user = User.builder()
            .name(dto.getName())
            .email(dto.getEmail())
            .password(dto.getPassword())
            .nickname(dto.getNickname())
            .gender(dto.getGender())
            .balance(0)
            .createdDate(LocalDate.now())
            .build();

        userRepository.save(user);
        return UserResponseDTO.fromEntity(user);
    }

    // 모든 유저 조회
    public List<UserResponseDTO> getAllUsers() {
        List<User> userEntities = userRepository.findAll();
        List<UserResponseDTO> userDtoList = new ArrayList<>();
        for (User userEntity : userEntities) {
            UserResponseDTO userDto = UserResponseDTO.fromEntity(userEntity);
            userDtoList.add(userDto);
        }
        return userDtoList;
    }

}
