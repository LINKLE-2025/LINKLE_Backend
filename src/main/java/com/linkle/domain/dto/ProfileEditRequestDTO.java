package com.linkle.domain.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// 요청용 (수정용)
public class ProfileEditRequestDTO {

    private String name;
    private String email;
    private String password;    // 요청 시에는 반드시 포함
    private String nickname;
    private String gender;
    private String image;
    private String background;
    private String memo;
    private String accountNumber;
}
