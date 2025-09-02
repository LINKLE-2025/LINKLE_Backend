package com.linkle.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import com.linkle.domain.entity.MessageType; // TEXT / SYSTEM 등

@Getter @Builder
@NoArgsConstructor @AllArgsConstructor
public class SendMessageRequest {

    @NotNull
    private Long roomId;

    @NotNull
    private MessageType messageType; // 보통 TEXT 고정, 시스템은 서버에서만 생성

    @NotBlank
    @Size(max = 2000)
    private String content;

}
