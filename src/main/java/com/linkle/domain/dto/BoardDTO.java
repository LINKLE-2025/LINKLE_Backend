package com.linkle.domain.dto;

import com.linkle.domain.entity.Board;

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
