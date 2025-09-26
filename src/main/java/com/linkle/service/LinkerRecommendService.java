package com.linkle.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.linkle.domain.dto.RecommendedLinkerDto;
import com.linkle.domain.entity.Linker;
import com.linkle.domain.entity.LinkerState;
import com.linkle.repository.FriendRepository;
import com.linkle.repository.LinkerRepository;
import com.linkle.repository.ParticipateByRecommendRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LinkerRecommendService {

    private final LinkerRepository linkerRepository;
    private final VectorStore vectorStore; // MariaDB용 VectorStore 또는 외부 Vector DB
    private final ParticipateByRecommendRepository participateByRecommendRepository;
    private final FriendRepository friendRepository;

    /**
     * Linker를 저장하면서 벡터스토어에도 Embedding 등록
     */
    @Transactional
    public void saveLinkerWithEmbedding(Linker linker) {
        // categoryId → categoryName 변환
        String categoryName;
        int categoryId = (linker.getCategoryId() != null) ? linker.getCategoryId().intValue() : -1;

        switch (categoryId) {
            case 1: categoryName = "식사"; break;
            case 2: categoryName = "카페"; break;
            case 3: categoryName = "음악"; break;
            case 4: categoryName = "영화"; break;
            case 5: categoryName = "독서"; break;
            case 6: categoryName = "운동"; break;
            case 7: categoryName = "음주"; break;
            case 8: categoryName = "학습"; break;
            case 9: categoryName = "쇼핑"; break;
            case 10: categoryName = "봉사"; break;
            case 11: categoryName = "게임"; break;
            case 12: categoryName = "여행"; break;
            case 13: categoryName = "신한"; break;
            default: categoryName = "기타"; break;
        }

        // 이름 + 메모 + 카테고리명 + 주소까지 포함
        String content = """
            이름: %s
            메모: %s
            카테고리: %s
            주소명: %s
            주소: %s
            """.formatted(
            linker.getName() != null ? linker.getName() : "",
            linker.getMemo() != null ? linker.getMemo() : "",
            categoryName,
            linker.getAddressName() != null ? linker.getAddressName() : "",
            linker.getAddress() != null ? linker.getAddress() : ""
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

        vectorStore.add(List.of(doc));

        log.info("링커 저장 및 벡터스토어 등록 완료: {} (카테고리: {}, 주소: {})",
            linker.getLinkerId(), categoryName, linker.getAddressName());
    }

    /**
     * 순수 임베딩 기반 추천
     */
    public List<RecommendedLinkerDto> recommendByEmbedding(
        Long myId, double myLat, double myLng, double radiusKm, int topK) {

        // 1. 내 프로필 텍스트 생성
        List<Integer> myTopCategories = participateByRecommendRepository.findTopCategoriesByUser(myId);
        // 2. 친구들의 주요 카테고리
        List<Long> friendIds = friendRepository.findAllFriendIds(myId);
        List<Integer> friendTopCategories = friendIds.stream()
            .flatMap(fid -> participateByRecommendRepository.findTopCategoriesByUser(fid).stream())
            .collect(Collectors.toList());

        // 3. 프로필 텍스트 생성
        String profileText = """
    사용자가 자주 참여한 카테고리: %s
    친구들이 자주 참여한 카테고리: %s
    """.formatted(myTopCategories, friendTopCategories);
        //(내가 가장 많이 참여한 카테고리 ID 목록  내 친구들이 자주 참여한 카테고리)

        // 2. 벡터스토어 유사도 검색 (profileText 직접 전달)
        List<Document> hits = vectorStore.similaritySearch(profileText);

        // 3. Document → DTO 변환
        List<RecommendedLinkerDto> results = hits.stream()
            .map(d -> {
                // 검사를 통해 얻은 결과값에서 linkerId를 꺼냄
                Long linkerId = Long.valueOf(d.getMetadata().get("linkerId").toString());
                Double similarityScore = d.getScore(); // 순수 임베딩 점수 를 꺼냄
                Optional<Linker> opt = linkerRepository.findById(linkerId); // 꺼낸 linkerId를 통해서 실제로 있는 linker인지 select

                // 만약 select된 linker가 없으면 null return
                if (opt.isEmpty()) return null;

                //있다면 Linker타입으로 변환
                Linker linker = opt.get();

                // 활성화된 Linker만 추천
                if (linker.getState() != LinkerState.ACTIVATED) return null;

                //추천된 linker와 유사도 점수를 RecommendedLinkerDto에 합치기
                return RecommendedLinkerDto.from(linker, similarityScore);
            })
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(RecommendedLinkerDto::getScore).reversed()) // 유사도점수 높은 순
            .toList();

        return results;
    }


    /**
     * 기존 DB 데이터 → 벡터스토어에 백필
     */
    @Transactional(readOnly = true)
    public void backfillAllLinkersToVectorStore() {
        List<Linker> all = linkerRepository.findAll();

        List<Document> docs = all.stream()
            .map(l -> Document.builder()
                .id(UUID.randomUUID().toString())
                .text("""
                %s %s %s
                """.formatted(
                    l.getName() != null ? l.getName() : "",
                    l.getMemo() != null ? l.getMemo() : "",
                    l.getCategoryId() != null ? l.getCategoryId().toString() : ""
                ))
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
