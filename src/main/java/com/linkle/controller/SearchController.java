package com.linkle.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.linkle.domain.dto.SearchLinkerResponseDTO;
import com.linkle.domain.dto.SearchUserResponseDTO;
import com.linkle.service.SearchService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/user")
    public ResponseEntity<List<SearchUserResponseDTO>> searchByNicknameOrName(
        @RequestParam("word") String word,
        @RequestParam("currentUserId") Long currentUserId
    ) {
        List<SearchUserResponseDTO> dtos = searchService.searchUsersWithFriendStatus(word, currentUserId);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/linker")
    public ResponseEntity<List<SearchLinkerResponseDTO>> searchLinkers(@RequestParam("word") String word) {
        word = "%" + word + "%";
        System.out.println(word);
        List<SearchLinkerResponseDTO> result = searchService.getAllLinkersByName(word);
        return ResponseEntity.ok(result);
    }

}
