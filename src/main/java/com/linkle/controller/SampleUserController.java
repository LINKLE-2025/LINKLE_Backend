package com.linkle.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.linkle.dto.SampleUserRequestDTO;
import com.linkle.dto.SampleUserResponseDTO;
import com.linkle.service.SampleUserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class SampleUserController {
	private final SampleUserService sampleUserService;

	@PostMapping("/register")
	public ResponseEntity<SampleUserResponseDTO> register(@RequestBody SampleUserRequestDTO dto) {
		SampleUserResponseDTO result = sampleUserService.register(dto);
		return ResponseEntity.ok(result);
	}
}
