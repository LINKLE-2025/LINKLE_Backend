package com.linkle.domain.dto;

import com.linkle.domain.entity.Board;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardDTO {

    private Long boardId;
    private String address;
    private String image;

    // Entity → DTO 변환
    public static BoardDTO fromEntity(Board board) {
        if (board == null) {
            return null;
        }
        return BoardDTO.builder()
            .boardId(board.getBoardId())
            .address(board.getAddress())
            .image(board.getImage())
            .build();
    }
}
