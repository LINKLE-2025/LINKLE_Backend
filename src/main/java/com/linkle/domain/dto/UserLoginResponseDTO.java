package com.linkle.domain.dto;

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
public class UserLoginResponseDTO {

    private String accessToken;
    private String refreshToken;

    private Long userId;
    private String email;
    private String nickname;

}
