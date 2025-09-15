package com.linkle.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponseDTO {
    private String impUid;       // 포트원 결제 UID
    private String merchantUid;  // 상점 주문번호
    private int amount;          // 결제 금액
    private String status;       // 상태 (paid, ready 등)
}
