package com.linkle.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.linkle.domain.dto.ParticipateLinkerResponseDTO;
import com.linkle.service.RecommendService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendController { private final RestTemplate restTemplate = new RestTemplate();

    private final RecommendService recommendService;

    @GetMapping
    public ResponseEntity<List<ParticipateLinkerResponseDTO>> getRecommend(@RequestParam Long userId) {
        return ResponseEntity.ok(recommendService.getRecommend(userId));
    }

}
