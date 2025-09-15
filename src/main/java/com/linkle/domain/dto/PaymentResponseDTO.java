package com.linkle.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class PaymentResponseDTO {
    private String id;
    private String status;
    private Amount amount;

    @Data
    public static class Amount {
        private long total;
        private long taxFree;
        private long vat;
        private String currency;
    }
}
