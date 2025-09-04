package com.linkle.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ReadSyncRequestDTO {

    @NotNull(message = "roomId는 필수입니다")
    private Long roomId;

    @NotNull(message = "lastReadMessageId는 필수입니다")
    private Long lastReadMessageId;
}
