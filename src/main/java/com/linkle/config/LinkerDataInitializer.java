package com.linkle.config;

import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.linkle.domain.entity.Linker;
import com.linkle.domain.entity.LinkerState;
import com.linkle.repository.LinkerRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@RequiredArgsConstructor
public class LinkerDataInitializer implements CommandLineRunner {

    private final LinkerRepository linkerRepository;
    private static final Logger log = LoggerFactory.getLogger(LinkerDataInitializer.class);

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 이름 기준으로 이미 존재하는지 확인
        boolean exists = linkerRepository.existsByName("신한 금융SW 아카데미");

        if (!exists) {
            Linker linker = Linker.builder()
                .name("신한 금융SW 아카데미")
                .address("서울 마포구 월드컵북로 4길 77")
                .addressName("퍼스트존")
                .addressDetail("서울특별시 마포구")
                .locationX(126.922630667157)
                .locationY(37.5593459381013)
                .categoryId(13L)
                .state(LinkerState.ACTIVATED)
                .createdDate(LocalDateTime.of(2099, 12, 1, 0, 0, 0))
                .memo("신한 금융SW 아카데미입니다.")
                .build();

            linkerRepository.save(linker);
            log.info("✅ 기본 링커 생성 완료");
        } else {
            log.info("ℹ️ 기본 링커 이미 존재, 생성하지 않음");
        }
    }
}
