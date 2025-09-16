package com.linkle.controller;

import org.springframework.data.domain.Pageable;
import java.util.List;

import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.linkle.domain.dto.ParticipateLinkerResponseDTO;
import com.linkle.domain.dto.SearchUserResponseDTO;
import com.linkle.service.SearchService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/user")
    public ResponseEntity<?> searchByNicknameOrName(
        @RequestParam("word") String word,
        @RequestParam("currentUserId") Long currentUserId,
        @PageableDefault(size = 10) Pageable pageable
    ) {
        word = "%" + word + "%";
        return ResponseEntity.ok(
            searchService.searchUsersWithFriendStatus(word, currentUserId, pageable)
        );
    }

    @GetMapping("/linker")
    public ResponseEntity<?> searchLinkers(
        @RequestParam("word") String word,
        @PageableDefault(size = 10) Pageable pageable
    ) {
        word = "%" + word + "%";
        return ResponseEntity.ok(
            searchService.getAllLinkersByName(word, pageable)
        );
    }

}
