package com.linkle.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.linkle.domain.dto.LinkerDTO;
import com.linkle.service.LinkerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/linker")
@Slf4j
public class LinkerController {
    private final LinkerService linkerService;

    @PostMapping
    public  void create(@RequestBody LinkerDTO linkerDTO,
        UriComponentsBuilder ucb){
        log.info("===== LinkerDTO 수신 =====");
        log.info("링커 아이디: {}", linkerDTO.getLinkerId());
        log.info("이름: {}", linkerDTO.getName());
        log.info("주소 이름: {}", linkerDTO.getAdressName());
        log.info("주소: {}", linkerDTO.getAddress());
        log.info("위도(X): {}", linkerDTO.getLocationX());
        log.info("경도(Y): {}", linkerDTO.getLocationY());
        log.info("상태: {}", linkerDTO.getState());
        log.info("카테고리 ID: {}", linkerDTO.getCategoryId());
        log.info("생성일: {}", linkerDTO.getCreatedDate());
        log.info("메모: {}", linkerDTO.getMemo());

        linkerService.createLinker(linkerDTO);
    }

    @GetMapping
    public ResponseEntity<List<LinkerDTO>> findAll() {
        return ResponseEntity.ok(linkerService.findAll());
    }
}
