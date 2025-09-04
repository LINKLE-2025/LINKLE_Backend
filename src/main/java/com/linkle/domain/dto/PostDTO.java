package com.linkle.domain.dto;

import com.linkle.domain.entity.Post;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDTO {

    private Long postId;
    private String image;
    private String memo;
    private LocalDateTime createdDate;
    private Long userId;
    private Long linkerId;

    // Entity → DTO 변환
    public static PostDTO fromEntity(Post post) {
        if (post == null) return null;
        return PostDTO.builder()
            .postId(post.getPostId())
            .image(post.getImage())
            .memo(post.getMemo())
            .createdDate(post.getCreatedDate())
            .userId(post.getUser() != null ? post.getUser().getUserId() : null)
            .linkerId(post.getLinker() != null ? post.getLinker().getLinkerId() : null)
            .build();
    }

    // List<Entity> → List<DTO> 변환
    public static List<PostDTO> fromEntityList(List<Post> posts) {
        if (posts == null) return null;
        return posts.stream()
            .map(PostDTO::fromEntity)
            .collect(Collectors.toList());
    }
}
