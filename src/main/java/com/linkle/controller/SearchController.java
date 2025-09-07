package com.linkle.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.linkle.domain.dto.SearchResponseDTO;
import com.linkle.service.SearchService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/user")
    public ResponseEntity<List<SearchResponseDTO>> searchByNicknameOrName(
        @RequestParam("word") String word,
        @RequestParam("currentUserId") Long currentUserId
    ) {
        List<SearchResponseDTO> dtos = searchService.searchUsersWithFriendStatus(word, currentUserId);
        return ResponseEntity.ok(dtos);
    }


}
