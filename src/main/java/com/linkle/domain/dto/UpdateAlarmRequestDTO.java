package com.linkle.domain.dto;

import com.linkle.domain.entity.Alarm; // ACTIVE / MUTE
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateAlarmRequestDTO {

    @NotNull(message = "roomId는 필수입니다")
    private Long roomId;

    @NotNull(message = "알림 상태는 필수입니다")
    private Alarm alarm; // ACTIVE or MUTE
}
