package com.linkle.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkle.domain.dto.ParticipateLinkerResponseDTO;
import com.linkle.domain.entity.Linker;
import com.linkle.domain.entity.LinkerState;
import com.linkle.repository.ParticipateRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecommendService {

    private final RestTemplate restTemplate;
    private final ParticipateRepository participateRepository;
    private final ObjectMapper objectMapper;

    @Value("${recommend.flask.base-url}")
    private String flaskBaseUrl;

    @Value("${recommend.flask.recommend-path}")
    private String recommendPath;

    public List<ParticipateLinkerResponseDTO> getRecommend(Long userId) {
        try {
            // Flask 기반 추천 API 불러오기
            String flaskUrl = flaskBaseUrl + recommendPath + "?user_id=" + userId;
            ResponseEntity<String> response = restTemplate.getForEntity(flaskUrl, String.class);

            // 응답 결과
            Map<String, List<Long>> result = objectMapper.readValue(response.getBody(), Map.class);
            List<Long> linkerIds = result.get("linker_ids");

            if (linkerIds == null || linkerIds.isEmpty()) {
                return List.of();
            }

            List<Object[]> results = participateRepository.findAllWithCountsByIds(linkerIds);
            System.out.println(results);
            // row 배열에서 값 꺼내기
            /*
            *row[0] → linker_id
            * row[1] → name
            * row[2] → category_id
            * row[3] → memo
            * row[4] → chatRoomCount
            * row[5] → postCount
            * row[6] -> state
            * row[7] -> address
            * */
            return results.stream()
                .map(row -> {
                    Long linkerId = ((Number) row[0]).longValue();
                    String name = (String) row[1];
                    Long categoryId = ((Number) row[2]).longValue();
                    String memo = (String) row[3];
                    Long chatRoomCount = ((Number) row[4]).longValue();
                    Long postCount = ((Number) row[5]).longValue();
                    LinkerState state = LinkerState.valueOf((String) row[6]);
                    String address = ((String) row[7]);

                    Linker linker = new Linker();
                    linker.setLinkerId(linkerId);
                    linker.setName(name);
                    linker.setCategoryId(categoryId);
                    linker.setMemo(memo);
                    linker.setState((state));
                    linker.setAddress((address));

                    return ParticipateLinkerResponseDTO.from(linker, chatRoomCount, postCount);
                })
                .toList();

        } catch (Exception e) {
            throw new RuntimeException("추천 처리 실패: " + e.getMessage(), e);
        }
    }
}

