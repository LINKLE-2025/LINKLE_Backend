package com.linkle.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.linkle.domain.dto.LinkerDTO;
import com.linkle.domain.entity.Linker;
import com.linkle.domain.entity.LinkerState;
import com.linkle.repository.LinkerRepository;
import com.linkle.util.LinkerMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LinkerService {
    private final LinkerRepository linkerRepository;

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
}
