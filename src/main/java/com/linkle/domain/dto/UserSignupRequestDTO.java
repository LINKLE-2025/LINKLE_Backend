package com.linkle.domain.dto;

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
public class UserSignupRequestDTO {

    private String name;
    private String email;
    private String password;
    private String nickname;
    private Integer age;
    private String gender;

    public User toEntity(String encodedPassword) {
        return User.builder()
            .name(this.name)
            .email(this.email)
            .password(encodedPassword)
            .nickname(this.nickname)
            .age(this.age)
            .gender(this.gender)
            .build();
    }
}
