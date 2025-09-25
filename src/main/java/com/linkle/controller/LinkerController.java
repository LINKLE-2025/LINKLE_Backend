package com.linkle.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.linkle.domain.dto.LinkerDTO;
import com.linkle.domain.dto.ParticipateDTO;
import com.linkle.service.LinkerRecommendService;
import com.linkle.service.LinkerService;
import com.linkle.util.LinkerMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/linker")
@Slf4j
public class LinkerController {
    private final LinkerService linkerService;
    private final LinkerRecommendService linkerRecommendService;

    @GetMapping("/{id}")
    public LinkerDTO linkerDetail(@PathVariable("id") Long linkerId){
        return  linkerService.linkerDetail(linkerId);
    }


    @PostMapping
    public  void create(@RequestBody LinkerDTO linkerDTO,
        UriComponentsBuilder ucb){
        log.info("===== LinkerDTO 수신 =====");
        log.info("링커 아이디: {}", linkerDTO.getLinkerId());
        log.info("이름: {}", linkerDTO.getName());
        log.info("상호 이름: {}", linkerDTO.getAddressName());
        log.info("주소: {}", linkerDTO.getAddress());
        log.info("주소 앞자리만: {}", linkerDTO.getAddressDetail());
        log.info("위도(Y): {}", linkerDTO.getLocationY());
        log.info("경도(X): {}", linkerDTO.getLocationX());
        log.info("상태: {}", linkerDTO.getState());
        log.info("카테고리 ID: {}", linkerDTO.getCategoryId());
        log.info("생성일: {}", linkerDTO.getCreatedDate());
        log.info("메모: {}", linkerDTO.getMemo());

        // 1) DB에 저장
        LinkerDTO saved = linkerService.createLinker(linkerDTO);

        // 2) Entity로 변환해서 벡터스토어에 저장
        linkerRecommendService.saveLinkerWithEmbedding(LinkerMapper.toEntity(saved));
    }

    // 🔹 참여 여부 체크
    @GetMapping("/participate/check")
    public ResponseEntity<ParticipateDTO> checkParticipation(
        @RequestParam Long linkerId,
        @RequestParam Long userId) {

        boolean participating = linkerService.isParticipating(linkerId, userId);

        ParticipateDTO dto = ParticipateDTO.builder()
            .linkerId(linkerId)
            .userId(userId)
            .participating(participating)
            .build();

        return ResponseEntity.ok(dto);
    }

    // 🔹 참여하기
    @PostMapping("/participate")
    public ResponseEntity<String> participate(@RequestBody ParticipateDTO request) {
        linkerService.addParticipation(request.getLinkerId(), request.getUserId());
        return ResponseEntity.ok("참여 완료");
    }
    // 🔹 링커 만료일 연장
    @PostMapping("/{id}/extend")
    public ResponseEntity<String> extendLinker(@PathVariable("id") Long linkerId) {
        linkerService.extendLinkerCreatedDate(linkerId);
        return ResponseEntity.ok("연장 완료");
    }

    @GetMapping
    public ResponseEntity<List<LinkerDTO>> findAll() {
        return ResponseEntity.ok(linkerService.findAll());
    }
}
