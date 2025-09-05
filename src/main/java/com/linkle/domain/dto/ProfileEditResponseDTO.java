package com.linkle.domain.dto;

import java.time.LocalDate;
import java.util.List;

import com.linkle.domain.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// 응답용
public class ProfileEditResponseDTO {

    private Long userId;
    private String name;
    private String email;
    private String nickname;
    private String gender;
    private String image;
    private String background;
    private String memo;
    private String accountNumber;
    private LocalDate createdDate;

    // Entity → DTO 변환
    public static ProfileEditResponseDTO fromEntity(User user) {
        if (user == null) return null;
        return ProfileEditResponseDTO.builder()
            .userId(user.getUserId())
            .name(user.getName())
            .email(user.getEmail())
            .nickname(user.getNickname())
            .gender(user.getGender())
            .image(user.getImage())
            .background(user.getBackground())
            .memo(user.getMemo())
            .accountNumber(user.getAccountNumber())
            .createdDate(user.getCreatedDate())
            .build();
    }

    // List<Entity> → List<DTO> 변환
    public static List<ProfileEditResponseDTO> fromEntityList(List<User> users) {
        if (users == null) return List.of(); // null 대신 빈 불변 리스트 반환
        return users.stream()
            .map(ProfileEditResponseDTO::fromEntity)
            .toList();
    }
}