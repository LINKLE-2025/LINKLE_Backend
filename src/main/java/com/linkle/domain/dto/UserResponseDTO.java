package com.linkle.domain.dto;

import com.linkle.domain.entity.User;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// 응답용
public class UserResponseDTO {

    private Long userId;
    private String name;
    private String email;
    private String nickname;
    private String gender;
    private String image;
    private String background;
    private String memo;
    private Integer accountNumber;
    private LocalDate createdAt;

    // Entity → DTO 변환
    public static UserResponseDTO fromEntity(User user) {
        if (user == null) return null;
        return UserResponseDTO.builder()
            .userId(user.getUserId())
            .name(user.getName())
            .email(user.getEmail())
            .nickname(user.getNickname())
            .gender(user.getGender())
            .image(user.getImage())
            .background(user.getBackground())
            .memo(user.getMemo())
            .accountNumber(user.getAccountNumber())
            .createdAt(user.getCreatedAt())
            .build();
    }

    // List<Entity> → List<DTO> 변환
    public static List<UserResponseDTO> fromEntityList(List<User> users) {
        if (users == null) return null;
        return users.stream()
            .map(UserResponseDTO::fromEntity)
            .collect(Collectors.toList());
    }
}
