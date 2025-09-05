package com.linkle.domain.dto;

import java.time.LocalDateTime;

import com.linkle.domain.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfilePostDTO {
    private Long postId;
    private String image;
    private String memo;
    private LocalDateTime createdDate;
    private Long userId;


    // Entity → DTO
    public static ProfilePostDTO fromEntity(Post post) {
        if (post == null) return null;
        return ProfilePostDTO.builder()
            .postId(post.getPostId())
            .image(post.getImage())
            .memo(post.getMemo())
            .createdDate(post.getCreatedDate())
            .userId(post.getUser().getUserId())
            .build();
    }
}
