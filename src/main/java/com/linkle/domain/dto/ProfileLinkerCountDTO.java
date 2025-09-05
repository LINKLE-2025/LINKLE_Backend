package com.linkle.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProfileLinkerCountDTO {
    private Long categoryId;
    private Long count;
}
