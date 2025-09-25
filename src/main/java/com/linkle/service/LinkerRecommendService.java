package com.linkle.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.linkle.domain.dto.RecommendedLinkerDto;
import com.linkle.domain.entity.Linker;
import com.linkle.domain.dto.LinkerDTO;
import com.linkle.repository.LinkerRepository;
import com.linkle.util.LinkerMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Service
@RequiredArgsConstructor
@Slf4j
public class LinkerRecommendService {

    private final LinkerRepository linkerRepository;
    private final VectorStore vectorStore; // MariaDB용 VectorStore 또는 외부 Vector DB

    /**
     * Linker를 저장하면서 벡터스토어에도 Embedding 등록
     */
    @Transactional
    public void saveLinkerWithEmbedding(Linker linker) {
        String content = """
                %s %s %s
                """.formatted(
            linker.getName() != null ? linker.getName() : "",
            linker.getMemo() != null ? linker.getMemo() : "",
            linker.getCategoryId() != null ? linker.getCategoryId().toString() : ""
        );

        Document doc = Document.builder()
            .id(UUID.randomUUID().toString())
            .text(content)
            .metadata(Map.of(
                "linkerId", linker.getLinkerId(),
                "lat", linker.getLocationY(),   // 위도
                "lng", linker.getLocationX()    // 경도
            ))
            .build();

        vectorStore.delete(List.of(doc.getId())); // 중복 방지
        vectorStore.add(List.of(doc));

        log.info("링커 저장 및 벡터스토어 등록 완료: {}", linker.getLinkerId());
    }

    /**
     * 위치 + 관심사 기반 추천
     */
    public List<RecommendedLinkerDto> recommend(
        double myLat, double myLng,
        double radiusKm, String myInterests, int topK) {

        // 1. 반경 내 후보군 조회
        List<Long> candidateIds = linkerRepository.findIdsWithinRadius(myLat, myLng, radiusKm);
        if (candidateIds.isEmpty()) return List.of();

        // 2. 관심사 기반 벡터 검색
        List<Document> hits = vectorStore.similaritySearch(myInterests);

        // 3. 후보군과 교집합 필터링
        List<RecommendedLinkerDto> results = new ArrayList<>();
        for (Document d : hits) {
            Object linkerIdObj = d.getMetadata().get("linkerId");
            if (linkerIdObj == null) continue;
            Long linkerId = Long.valueOf(linkerIdObj.toString());

            if (candidateIds.contains(linkerId)) {
                linkerRepository.findById(linkerId).ifPresent(linker -> {
                    RecommendedLinkerDto dto = RecommendedLinkerDto.builder()
                        .linkerId(linker.getLinkerId())
                        .name(linker.getName())
                        .memo(linker.getMemo())
                        .categoryId(linker.getCategoryId())
                        .address(linker.getAddress())
                        .addressDetail(linker.getAddressDetail())
                        .locationX(linker.getLocationX())
                        .locationY(linker.getLocationY())
                        .score(d.getScore())  // 점수 저장
                        .build();
                    results.add(dto);
                });
            }
        }

        // 4. 상위 topK 반환
        return results.stream()
            .sorted(Comparator.comparing(RecommendedLinkerDto::getScore).reversed())
            .limit(topK)
            .toList();
    }

    // 기존 DB 데이터 → 벡터스토어에 넣기
    @Transactional(readOnly = true)
    public void backfillAllLinkersToVectorStore() {
        List<Linker> all = linkerRepository.findAll();

        List<Document> docs = all.stream()
            .map(l -> Document.builder()
                // UUID는 무조건 랜덤 UUID만 사용 (MariaDB VectorStore 제약)
                .id(UUID.randomUUID().toString())
                // 검색용 텍스트 (이름 + 메모 + 카테고리ID)
                .text("""
                %s %s %s
                """.formatted(
                    l.getName() != null ? l.getName() : "",
                    l.getMemo() != null ? l.getMemo() : "",
                    l.getCategoryId() != null ? l.getCategoryId().toString() : ""
                ))
                // LinkerId는 metadata에만 넣음
                .metadata(Map.of(
                    "linkerId", l.getLinkerId(),
                    "lat", l.getLocationY(),
                    "lng", l.getLocationX()
                ))
                .build())
            .toList();

        vectorStore.add(docs);
        log.info("{}개의 링커를 벡터스토어에 백필 완료", docs.size());
    }
}