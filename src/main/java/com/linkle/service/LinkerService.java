package com.linkle.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.linkle.domain.dto.LinkerDTO;
import com.linkle.domain.entity.Linker;
import com.linkle.domain.entity.LinkerState;
import com.linkle.domain.entity.Participate;
import com.linkle.domain.entity.User;
import com.linkle.repository.LinkerRepository;
import com.linkle.repository.ParticipateRepository;
import com.linkle.repository.UserRepository;
import com.linkle.util.LinkerMapper;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class LinkerService {
    private final LinkerRepository linkerRepository;
    private final S3Client s3Client;
    private final ParticipateRepository participateRepository;
    private final UserRepository userRepository; // User 엔티티 조회용

    public LinkerDTO linkerDetail(Long linkerId){

        return linkerRepository.findById(linkerId)
            .map(LinkerMapper::toDTO)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Linker not found"));
    }

    public List<LinkerDTO> findAll() {
        return linkerRepository.findAll()
            .stream()
            .map(LinkerDTO::fromEntity)
            .toList();
    }

    public LinkerDTO createLinker(LinkerDTO linkerDTO) {
        Linker linker = LinkerMapper.toEntity(linkerDTO);
        linker.setCreatedDate(LocalDateTime.now());
        linker.setState(LinkerState.ACTIVATED);

        Linker savedLInker = linkerRepository.save(linker);

        return LinkerMapper.toDTO(savedLInker);
    }

    // 참여 여부 체크
    public boolean isParticipating(Long linkerId, Long userId) {
        return participateRepository.findByLinker_LinkerIdAndUser_UserId(linkerId, userId).isPresent();
    }

    // 참여 추가
    public void addParticipation(Long linkerId, Long userId) {
        if (isParticipating(linkerId, userId)) return;

        Linker linker = linkerRepository.findById(linkerId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Linker not found"));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Participate p = new Participate();
        p.setLinker(linker);
        p.setUser(user); // User 엔티티 세팅
        p.setParticipatedDate(LocalDate.now());

        participateRepository.save(p);
    }
}
