package com.linkle.domain.dto;

import com.linkle.domain.entity.Reply;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplyDTO {

    private Long replyId;
    private String content;
    private LocalDate createdAt;
    private Long userId;
    private Long boardId;

    // Entity → DTO 변환
    public static ReplyDTO fromEntity(Reply reply) {
        if (reply == null) return null;
        return ReplyDTO.builder()
            .replyId(reply.getReplyId())
            .content(reply.getContent())
            .createdAt(reply.getCreatedAt())
            .userId(reply.getUser() != null ? reply.getUser().getUserId() : null)
            .boardId(reply.getBoard() != null ? reply.getBoard().getBoardId() : null)
            .build();
    }

    // List<Entity> → List<DTO> 변환
    public static List<ReplyDTO> fromEntityList(List<Reply> replies) {
        if (replies == null) return null;
        return replies.stream()
            .map(ReplyDTO::fromEntity)
            .collect(Collectors.toList());
    }
}
