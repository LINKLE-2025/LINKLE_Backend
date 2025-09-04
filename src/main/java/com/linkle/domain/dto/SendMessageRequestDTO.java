package com.linkle.domain.dto;

import java.util.Optional;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import com.linkle.domain.entity.MessageType; // TEXT / SYSTEM 등

 import com.fasterxml.jackson.annotation.JsonAlias;
 import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class SendMessageRequestDTO {

    @NotNull
    private Long roomId;

    private MessageType messageType;   // TEXT 기본

    @JsonAlias({"text", "content"})    // ← text/ content 둘 다 허용
    private String text;

    @JsonIgnore
    public String ensuredText() {
        return Optional.ofNullable(text)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .orElseThrow(() -> new IllegalArgumentException("메시지 내용이 비어있습니다."));
    }
}

