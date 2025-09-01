package com.linkle.util;

import com.linkle.domain.dto.LinkerDTO;
import com.linkle.domain.entity.Linker;

public class LinkerMapper {

    private LinkerMapper() {
        // 객체 생성 방지
    }

    // DTO → Entity
    public static Linker toEntity(LinkerDTO dto) {
        if (dto == null) return null;

        return Linker.builder()
            .linkerId(dto.getLinkerId())
            .name(dto.getName())
            .adressName(dto.getAdressName())
            .address(dto.getAddress())
            .locationX(dto.getLocationX())
            .locationY(dto.getLocationY())
            .state(dto.getState())
            .categoryId(dto.getCategoryId())
            .createdDate(dto.getCreatedDate())
            .memo(dto.getMemo())
            .build();
    }

    // Entity → DTO
    public static LinkerDTO toDTO(Linker entity) {
        if (entity == null) return null;

        return LinkerDTO.builder()
            .linkerId(entity.getLinkerId())
            .name(entity.getName())
            .adressName(entity.getAdressName())
            .address(entity.getAddress())
            .locationX(entity.getLocationX())
            .locationY(entity.getLocationY())
            .state(entity.getState())
            .categoryId(entity.getCategoryId())
            .createdDate(entity.getCreatedDate())
            .memo(entity.getMemo())
            .build();
    }
}
