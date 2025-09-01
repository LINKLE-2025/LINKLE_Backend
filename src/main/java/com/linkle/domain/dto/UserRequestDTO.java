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
// 요청용 (회원가입/수정용)
public class UserRequestDTO {

    private String name;
    private String email;
    private String password;    // 요청 시에는 반드시 포함
    private String nickname;
    private String gender;
    private String image;
    private String background;
    private String memo;
    private Integer accountNumber;
}
