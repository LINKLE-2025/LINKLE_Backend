package com.linkle.domain.dto;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarkerDTO {
    @NotBlank private String title;
    @NotNull @DecimalMin("-90.0")  @DecimalMax("90.0")   private Double lat;
    @NotNull @DecimalMin("-180.0") @DecimalMax("180.0")  private Double lng;
    @NotNull private Long categoryId;
}