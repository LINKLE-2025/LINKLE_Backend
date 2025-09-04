package com.linkle.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.linkle.domain.dto.PostDTO;
import com.linkle.service.PostService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/post")
@Slf4j
public class PostController {

    private final PostService postService;

    @GetMapping
    public List<PostDTO> selectPost(@RequestParam("linkerId")  Long linkerId) {
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
}
