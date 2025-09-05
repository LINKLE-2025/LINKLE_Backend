package com.linkle.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.linkle.domain.dto.PostDTO;
import com.linkle.domain.dto.ProfilePostDTO;
import com.linkle.domain.entity.Post;
import com.linkle.service.PostService;
import com.linkle.service.ProfileService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/post")
@Slf4j
public class PostController {

    private final PostService postService;
    private final ProfileService profileService;

    @GetMapping("/{postId}")
    public PostDTO getPost(@PathVariable Long postId) {
        return postService.findById(postId);
    }

    @GetMapping(params = "linkerId")
    public List<PostDTO> selectPostList(@RequestParam("linkerId")  Long linkerId) {
        List<PostDTO> postList = postService.findByLinkerLinkerId(linkerId);
        return postList;
    }
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostDTO> createPost(
        @RequestParam("linkerId") Long linkerId,
        @RequestParam("content") String content,
        @RequestParam("image") MultipartFile file
    ) throws IOException {

        // TODO: 로그인 붙이면 SecurityContext에서 userId 가져오기
         Long userId = 1L;

        log.info("링커아이디 :  " + linkerId);

        PostDTO dto = new PostDTO();
        dto.setLinkerId(linkerId);
        dto.setMemo(content);
        dto.setUserId(userId);

        PostDTO created = postService.createPost(dto, file);
        return ResponseEntity.ok(created);
    }

    private MediaType resolveMediaType(String key) {
        if (key != null && key.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        } else if (key != null && (key.endsWith(".jpg") || key.endsWith(".jpeg"))) {
            return MediaType.IMAGE_JPEG;
        } else {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

        @GetMapping("/{postId}/image")
        public ResponseEntity<byte[]> viewPostImage(@PathVariable Long postId) throws IOException {
            PostDTO dto = postService.findById(postId);
            if (dto == null || dto.getImage() == null) {
                return ResponseEntity.notFound().build();
            }
            String key = dto.getImage();                 // 외부 저장 키 (예: minio/s3 key)
            byte[] data = profileService.downloadFile(key);

            return ResponseEntity.ok()
                .contentType(resolveMediaType(key))
                .body(data);
        }

    // 유저 포스트 조회
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ProfilePostDTO>> getUserPosts(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.getProfilePost(userId));
    }
}
