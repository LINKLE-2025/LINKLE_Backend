package com.linkle.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.ai.document.Document;
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
    private final VectorStore vectorStore;
    private final ParticipateByRecommendRepository participateByRecommendRepository;
    private final FriendRepository friendRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    /**
     * 두 지점 간 거리 계산 (km)
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371.0; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double rLat1 = Math.toRadians(lat1);
        double rLat2 = Math.toRadians(lat2);

        double a = Math.pow(Math.sin(dLat / 2), 2)
            + Math.cos(rLat1) * Math.cos(rLat2) * Math.pow(Math.sin(dLng / 2), 2);

        double c = 2 * Math.asin(Math.sqrt(a));

        return earthRadius * c;
    }

    /**
     * Linker를 저장하면서 벡터스토어에도 Embedding 등록
     */
    @Transactional
    public void saveLinkerWithEmbedding(Linker linker) {
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

        String content = """
            이름: %s
            메모: %s
            카테고리: %s
            주소명: %s
            주소: %s
            위도: %.6f
            경도: %.6f
            """.formatted(
            linker.getName() != null ? linker.getName() : "",
            linker.getMemo() != null ? linker.getMemo() : "",
            categoryName,
            linker.getAddressName() != null ? linker.getAddressName() : "",
            linker.getAddress() != null ? linker.getAddress() : "",
            linker.getLocationY(),
            linker.getLocationX()
        );

        Document doc = Document.builder()
            .id(UUID.randomUUID().toString())
            .text(content)
            .metadata(Map.of(
                "linkerId", linker.getLinkerId(),
                "lat", linker.getLocationY(),
                "lng", linker.getLocationX(),
                "AddressName", linker.getAddressName(),
                "Address", linker.getAddress()
            ))
            .build();

        vectorStore.add(List.of(doc));

        log.info("링커 저장 및 벡터스토어 등록 완료: {} (카테고리: {}, 주소: {})",
            linker.getLinkerId(), categoryName, linker.getAddressName());
    }

    /**
     * 추천 + 반경 필터링
     */
    public List<RecommendedLinkerDto> recommendByEmbedding(
        Long myId, double myLat, double myLng, double radiusKm, int topK) {

        // radiusKm 기본값 처리 (null이나 0 들어오면 3km로 강제)
        if (radiusKm <= 0) {
            radiusKm = 3.0;
        }

        // 1. 내 프로필 카테고리
        List<Integer> myTopCategories = participateByRecommendRepository.findTopCategoriesByUser(myId);

        String userName = userRepository.findById(myId)
            .map(User::getName)
            .orElse("사용자");

        // 2. 친구 카테고리
        List<Long> friendIds = friendRepository.findAllFriendIds(myId);
        List<Integer> friendTopCategories = friendIds.stream()
            .flatMap(fid -> participateByRecommendRepository.findTopCategoriesByUser(fid).stream())
            .toList();

        // 3. 검색 텍스트
        String text = """
            사용자가 자주 참여한 카테고리: %s
            친구들이 자주 참여한 카테고리: %s
            """.formatted(myTopCategories, friendTopCategories);

        // 4. 벡터스토어 유사도 검색
        List<Document> hits = vectorStore.similaritySearch(text);

        List<Long> linkerIds = hits.stream()
            .map(d -> Long.valueOf(d.getMetadata().get("linkerId").toString()))
            .toList();

        if (linkerIds.isEmpty()) return List.of();

        // 5. 참여/게시글 카운트 조회
        List<Object[]> countsData = participateByRecommendRepository.findAllWithCountsByIds(linkerIds);
        Map<Long, Object[]> countsMap = countsData.stream()
            .collect(Collectors.toMap(row -> ((Number) row[0]).longValue(), row -> row));

        // 6. DTO 변환 + 거리 필터링
        double finalRadiusKm = radiusKm;
        List<RecommendedLinkerDto> results = hits.stream()
            .map(d -> {
                Long linkerId = Long.valueOf(d.getMetadata().get("linkerId").toString());
                Double similarityScore = d.getScore();

                Double lat = (Double) d.getMetadata().get("lat");
                Double lng = (Double) d.getMetadata().get("lng");
                if (lat == null || lng == null) return null;

                double distance = calculateDistance(myLat, myLng, lat, lng);
                if (distance > finalRadiusKm) return null; // 반경 필터링

                Optional<Linker> opt = linkerRepository.findById(linkerId);
                if (opt.isEmpty()) return null;

                Linker linker = opt.get();
                if (linker.getState() != LinkerState.ACTIVATED) return null;

                Object[] row = countsMap.get(linkerId);
                Long chatRoomCount = ((Number) row[4]).longValue();
                Long postCount = ((Number) row[5]).longValue();

                return RecommendedLinkerDto.from(linker, similarityScore, chatRoomCount, postCount, userName);
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
