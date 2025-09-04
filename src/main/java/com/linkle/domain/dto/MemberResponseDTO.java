package com.linkle.domain.dto;

import com.linkle.domain.entity.ChatPart;
import com.linkle.domain.entity.User;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberResponseDTO {

    private Long userId;
    private String name;
    private String image;        // 프로필 이미지

    public static MemberResponseDTO fromEntity(ChatPart part) {
        if (part == null) return null;
        User u = part.getUser();
        return MemberResponseDTO.builder()
            .userId(u.getUserId())
            .name(u.getName())
            .image(u.getImage())
            .build();
    }

}
