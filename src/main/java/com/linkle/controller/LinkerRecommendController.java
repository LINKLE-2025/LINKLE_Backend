package com.linkle.controller;

import com.linkle.domain.dto.RecommendedLinkerDto;
import com.linkle.service.LinkerRecommendService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/linkers")
public class LinkerRecommendController {

    private final LinkerRecommendService linkerRecommendService;

    /**
     * 위치 + 관심사 기반 추천
     * GET /api/linkers/recommend?lat=37.5&lng=127.0&userId=1&radiusKm=3&topK=10
     */
    @GetMapping("/recommend")
    public List<RecommendedLinkerDto> recommend(
        @RequestParam("lat") Double lat,
        @RequestParam("lng") Double lng,
        @RequestParam("userId") Long userId,   // 로그인된 사용자 ID
        @RequestParam(defaultValue = "3") Double radiusKm, // 기본 3km
        @RequestParam(defaultValue = "10") Integer topK
    ) {
        return linkerRecommendService.recommendByEmbedding(userId, lat, lng, radiusKm, topK);
    }

    /**
     * 기존 DB에 있는 Linker들을 벡터스토어에 백필
     * POST /api/linkers/backup
     */
    @PostMapping("/backup")
    public ResponseEntity<String> backup() {
        linkerRecommendService.backfillAllLinkersToVectorStore();
        return ResponseEntity.ok("벡터스토어 백필 완료");
    }
}
