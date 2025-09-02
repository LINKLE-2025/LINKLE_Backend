package com.linkle.domain.dto;

import com.linkle.domain.entity.Participate;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipateDTO {

    private Long id;
    private Long userId;       // User FK
    private Long linkerId;     // Linker FK
    private LocalDate participatedAt;

    // Entity → DTO 변환
    public static ParticipateDTO fromEntity(Participate p) {
        if (p == null)
            return null;
        return ParticipateDTO.builder()
            .id(p.getId())
            .userId(p.getUser() != null ? p.getUser().getUserId() : null)
            .linkerId(p.getLinker() != null ? p.getLinker().getLinkerId() : null)
            .participatedAt(p.getParticipatedAt())
            .build();

    }
}
