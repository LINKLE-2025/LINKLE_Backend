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
import com.linkle.domain.entity.User;
import com.linkle.repository.ChatRoomRepository;
import com.linkle.repository.FriendRepository;
import com.linkle.repository.LinkerRepository;
import com.linkle.repository.ParticipateByRecommendRepository;
import com.linkle.repository.UserRepository;

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
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

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

    public List<RecommendedLinkerDto> recommendByEmbedding(
        Long myId, double myLat, double myLng, double radiusKm, int topK) {

        // 1. 내 프로필 텍스트 생성
        List<Integer> myTopCategories = participateByRecommendRepository.findTopCategoriesByUser(myId);

        // 닉네임, 카테고리 불러오기
        String userName = userRepository.findById(myId)
            .map(User::getName)
            .orElse("사용자");

        // 2. 친구들의 주요 카테고리
        List<Long> friendIds = friendRepository.findAllFriendIds(myId);
        List<Integer> friendTopCategories = friendIds.stream()
            .flatMap(fid -> participateByRecommendRepository.findTopCategoriesByUser(fid).stream())
            .toList();

        // 3. 프로필 텍스트 생성
        String profileText = """
        사용자가 자주 참여한 카테고리: %s
        친구들이 자주 참여한 카테고리: %s
        """.formatted(myTopCategories, friendTopCategories);

        // 4. 벡터스토어 유사도 검색
        List<Document> hits = vectorStore.similaritySearch(profileText);

        // 5. linkerId 목록 추출
        List<Long> linkerIds = hits.stream()
            .map(d -> Long.valueOf(d.getMetadata().get("linkerId").toString()))
            .toList();

        if (linkerIds.isEmpty()) return List.of();

        // 6. chatRoomCount, postCount 한 번에 조회
        List<Object[]> countsData = participateByRecommendRepository.findAllWithCountsByIds(linkerIds);
        Map<Long, Object[]> countsMap = countsData.stream()
            .collect(Collectors.toMap(row -> ((Number) row[0]).longValue(), row -> row));

        // 7. DTO 변환
        List<RecommendedLinkerDto> results = hits.stream()
            .map(d -> {
                Long linkerId = Long.valueOf(d.getMetadata().get("linkerId").toString());
                Double similarityScore = d.getScore();

                Optional<Linker> opt = linkerRepository.findById(linkerId);
                if (opt.isEmpty()) return null;

                Linker linker = opt.get();
                if (linker.getState() != LinkerState.ACTIVATED) return null;

                Object[] row = countsMap.get(linkerId);
                Long chatRoomCount = ((Number) row[4]).longValue();
                Long postCount = ((Number) row[5]).longValue();

                return RecommendedLinkerDto.from(linker, similarityScore, chatRoomCount, postCount,userName);
            })
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(RecommendedLinkerDto::getScore).reversed())
            .limit(topK)
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
