package com.linkle.service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.linkle.domain.dto.RecommendedLinkerDto;
import com.linkle.domain.entity.Linker;
import com.linkle.domain.dto.LinkerDTO;
import com.linkle.domain.entity.LinkerState;
import com.linkle.repository.FriendRepository;
import com.linkle.repository.LinkerRepository;
import com.linkle.repository.ParticipateByRecommendRepository;
import com.linkle.util.LinkerMapper;

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

        // 중복 방지 → 같은 id로 저장하면 항상 새로운 UUID 생성되므로 delete는 사실상 필요 X
        vectorStore.add(List.of(doc));

        log.info("링커 저장 및 벡터스토어 등록 완료: {} (카테고리: {}, 주소: {})",
            linker.getLinkerId(), categoryName, linker.getAddressName());
    }

    // 추천 로직
    public List<RecommendedLinkerDto> recommend(
        Long myId, double myLat, double myLng, double radiusKm, int topK) {

        // 1. 내 top 카테고리
        List<Integer> myTopCategories = participateByRecommendRepository.findTopCategoriesByUser(myId);
        String myCategoriesText = myTopCategories.stream()
            .map(Object::toString)
            .collect(Collectors.joining(", "));

        // 2. 친구들의 top 카테고리
        List<Long> friendIds = friendRepository.findAllFriendIds(myId);
        List<Integer> friendTopCategories = friendIds.isEmpty() ? List.of()
            : participateByRecommendRepository.findTopCategoriesByFriends(friendIds);
        String friendCategoriesText = friendTopCategories.stream()
            .map(Object::toString)
            .collect(Collectors.joining(", "));

        // 3. AI 검색 쿼리 텍스트 생성
        String profileText = """
        사용자가 자주 참여한 카테고리: %s
        친구들이 자주 참여한 카테고리: %s
    """.formatted(myCategoriesText, friendCategoriesText);

        // 4. 벡터 검색 (AI Embedding)
        List<Document> hits = vectorStore.similaritySearch(profileText);

        // 5. 후보군 필터링
        List<Long> recentIds =
            participateByRecommendRepository.findRecentParticipatedLinkerIds(
                myId, LocalDate.now().minusDays(30));
        List<Long> nearbyIds =
            linkerRepository.findIdsWithinRadius(myLat, myLng, radiusKm);

        // 6. 점수화 + 결과 생성
        List<RecommendedLinkerDto> results = new ArrayList<>();
        for (Document d : hits) {
            Long linkerId = Long.valueOf(d.getMetadata().get("linkerId").toString());
            Optional<Linker> opt = linkerRepository.findById(linkerId);
            if (opt.isEmpty()) continue;

            Linker linker = opt.get();

            // 비활성화 / 반경 외 / 최근 방문 제외
            if (linker.getState() != LinkerState.ACTIVATED) continue;
            if (!nearbyIds.contains(linkerId)) continue;
            if (recentIds.contains(linkerId)) continue;

            double score = d.getScore();

            // 내가 선호하는 카테고리면 가산점
            if (myTopCategories.contains(linker.getCategoryId())) score += 0.2;

            // 친구들이 참여 많이 한 링커면 가산점
            if (!friendIds.isEmpty()) {
                long friendParticipationCount =
                    participateByRecommendRepository.findTopLinkerIdsByFriends(friendIds)
                        .stream().filter(id -> id.equals(linkerId)).count();
                score += 0.05 * friendParticipationCount;
            }

            results.add(RecommendedLinkerDto.from(linker, score));
        }

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