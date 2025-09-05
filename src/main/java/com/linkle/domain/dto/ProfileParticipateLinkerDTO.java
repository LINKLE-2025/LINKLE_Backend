package com.linkle.domain.dto;

import java.time.LocalDate;

import com.linkle.domain.entity.LinkerState;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProfileParticipateLinkerDTO {
    private Long linkerId;
    private String name;
    private LinkerState state;
    private String memo;
    private LocalDate participatedDate;

}
