package com.linkle.domain.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.linkle.domain.entity.Linker;
import com.linkle.domain.entity.LinkerState;

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
public class LinkerDTO {

    private Long linkerId;
    private String name;
    private String addressName;
    private String address;
    private String addressDetail;
    private Double locationX;
    private Double locationY;
    private LinkerState state;
    private Long categoryId;
    private LocalDateTime createdDate;
    private String memo;

    // Entity → DTO
    public static LinkerDTO fromEntity(Linker linker) {
        if (linker == null) {
            return null;
        }
        return LinkerDTO.builder()
            .linkerId(linker.getLinkerId())
            .name(linker.getName())
            .addressName(linker.getAddressName())
            .address(linker.getAddress())
            .addressDetail(linker.getAddressDetail())
            .locationX(linker.getLocationX())
            .locationY(linker.getLocationY())
            .state(linker.getState())
            .categoryId(linker.getCategoryId())
            .createdDate(linker.getCreatedDate())
            .memo(linker.getMemo())
            .build();
    }

    // List<Entity> → List<DTO>
    public static List<LinkerDTO> fromEntityList(List<Linker> linkers) {
        if (linkers == null) {
            return Collections.emptyList(); // ✅ null 대신 빈 리스트 반환
        }
        return linkers.stream()
            .map(LinkerDTO::fromEntity)
            .collect(Collectors.toList());
    }

    // DTO → Entity (필요시 추가)
    public Linker toEntity() {
        Linker linker = new Linker();
        linker.setLinkerId(this.linkerId);
        linker.setName(this.name);
        linker.setAddressName(this.addressName);
        linker.setAddress(this.address);
        linker.setAddressDetail(this.addressDetail);
        linker.setLocationX(this.locationX);
        linker.setLocationY(this.locationY);
        linker.setState(this.state);
        linker.setCategoryId(this.categoryId);
        linker.setCreatedDate(this.createdDate);
        linker.setMemo(this.memo);
        return linker;
    }
}
