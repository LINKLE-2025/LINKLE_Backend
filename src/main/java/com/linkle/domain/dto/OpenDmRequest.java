package com.linkle.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OpenDmRequest {

    @NotNull(message = "DM 대상 사용자 ID는 필수입니다")
    private Long targetUserId;
}
