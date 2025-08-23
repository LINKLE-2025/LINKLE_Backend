package com.linkle.service;

import org.springframework.stereotype.Service;

import com.linkle.dto.SampleUserRequestDTO;
import com.linkle.dto.SampleUserResponseDTO;
import com.linkle.entity.SampleUserEntity;
import com.linkle.repository.SampleUserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SampleUserService {
	private final SampleUserRepository sampleUserRepository;

	public SampleUserResponseDTO register(SampleUserRequestDTO dto) {
		SampleUserEntity user = SampleUserEntity.builder()
			.username(dto.getUsername())
			.email(dto.getEmail())
			.password(dto.getPassword()) // 비밀번호 암호화 생략
			.build();

		SampleUserEntity saved = sampleUserRepository.save(user);

		return SampleUserResponseDTO.builder()
			.id(saved.getId())
			.username(saved.getUsername())
			.email(saved.getEmail())
			.build();
	}
}
