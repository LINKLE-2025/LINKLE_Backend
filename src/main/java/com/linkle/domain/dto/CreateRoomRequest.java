package com.linkle.domain.dto;

import com.linkle.domain.entity.RoomType; // DM, LIGHT, CLASS
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRoomRequest {

    @NotNull(message = "방 타입은 필수입니다")
    private RoomType roomType;             // LIGHT or CLASS (DM은 OpenDmRequest 사용)

    @NotBlank(message = "채팅방 이름은 필수입니다")
    @Size(max = 100, message = "채팅방 이름은 100자를 넘을 수 없습니다")
    private String roomName;

    @NotBlank(message = "채팅방 설명은 필수입니다")
    @Size(max = 500, message = "설명은 500자를 넘을 수 없습니다")
    private String description;

    @NotBlank(message = "채팅방 메모는 필수입니다")
    @Size(max = 200, message = "메모는 200자를 넘을 수 없습니다")
    private String memo;

    // ====== 팔레트 색상 코드 (1~9) ======
    @NotNull(message = "테마 코드는 필수입니다")
    @Min(value = 1, message = "테마 코드는 1 이상이어야 합니다")
    @Max(value = 9, message = "테마 코드는 9 이하여야 합니다")
    private Integer themeColor;

    // ====== CLASS 전용 옵션 ======
    @PositiveOrZero(message = "입장료는 0원 이상이어야 합니다")
    private Integer entryFee;        // CLASS일 경우 필수

    private LocalDateTime startDate;         // CLASS일 경우 필수

    // ====== 조건부 검증 ======
    @AssertTrue(message = "클래스 방은 시작 일시(startAt)가 필요합니다")
    private boolean isStartAtValidForClass() {
        if (roomType == RoomType.CLASS) {
            return startDate != null;
        }
        return true;
    }

    @AssertTrue(message = "클래스 방은 입장료(0원 이상)를 지정해야 합니다")
    private boolean isFeeValidForClass() {
        if (roomType == RoomType.CLASS) {
            return entryFee != null && entryFee >= 0;
        }
        return true;
    }
}
