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
public class UserAuthDTO {

    private Long userId;
    private String name;
    private String nickname;
    private Integer age;
    private String gender;
    private String image;

}
