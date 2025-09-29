package com.linkle.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${weather.serviceKey}")
    private String serviceKey;

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentWeather(
        @RequestParam double lat,
        @RequestParam double lon) {

        try {
            // (1) 위경도 → 격자 변환 (기존과 동일)
            String xyUrl = String.format(
                "https://apihub.kma.go.kr/api/typ01/cgi-bin/url/nph-dfs_xy_lonlat?lon=%f&lat=%f&help=0&authKey=%s",
                lon, lat, serviceKey
            );
            String xyResponse = restTemplate.getForObject(xyUrl, String.class);
            String[] lines = xyResponse.trim().split("\n");

            String dataLine = null;
            // 응답의 모든 줄을 확인하여
            for (String line : lines) {
                String trimmedLine = line.trim();
                // 주석(#)으로 시작하지 않고, 비어있지 않은 첫 번째 줄을 데이터 줄로 확정
                if (!trimmedLine.startsWith("#") && !trimmedLine.isEmpty()) {
                    dataLine = trimmedLine;
                    break;
                }
            }

            // 데이터 줄을 찾지 못한 경우 에러 처리
            if (dataLine == null) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("KMA 응답에서 유효한 데이터 라인을 찾을 수 없습니다. 응답: " + xyResponse);
            }

            // 쉼표(,)와 하나 이상의 공백(\s+)을 모두 구분자로 사용하여 파싱
            String[] parts = dataLine.split("[\\s,]+");

            if (parts.length < 4) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("KMA 응답의 데이터가 충분하지 않습니다. 데이터 라인: " + dataLine);
            }

            String nx;
            String ny;

            try {
                // X 좌표는 3번째(인덱스 2), Y 좌표는 4번째(인덱스 3)에 위치
                nx = String.valueOf((int) Double.parseDouble(parts[2]));
                ny = String.valueOf((int) Double.parseDouble(parts[3]));
            } catch (NumberFormatException e) {
                System.err.println("KMA 좌표 변환 API 응답 파싱 실패. 원본 응답: " + xyResponse);
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("KMA 좌표 변환 API의 응답이 올바르지 않습니다.");
            }

            // (2) 기상청 baseDate / baseTime 계산 (수정된 로직)
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
            String baseDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String baseTime;

            int hour = now.getHour();
            // 초단기실황은 매시 45분 이후에 제공되므로, 45분 이전에는 이전 시간의 데이터를 요청해야 함
            if (now.getMinute() < 45) {
                hour = hour - 1;
                if (hour < 0) {
                    // 자정이 지났을 경우, 날짜를 하루 빼고 시간을 23시로 설정
                    baseDate = now.minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                    hour = 23;
                }
            }
            // base_time 포맷은 'HH00' 형식이어야 함
            baseTime = String.format("%02d00", hour);

            String url = UriComponentsBuilder
                .fromHttpUrl("https://apihub.kma.go.kr/api/typ02/openApi/VilageFcstInfoService_2.0/getUltraSrtNcst")
                .queryParam("authKey", serviceKey) // 수정됨: 설정 파일의 serviceKey 사용
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", 10) // 필요한 데이터만 요청 (기온, 강수형태 등)
                .queryParam("dataType", "JSON")
                .queryParam("base_date", baseDate)
                .queryParam("base_time", baseTime) // 수정됨: 동적으로 계산된 baseTime 사용
                .queryParam("nx", nx)
                .queryParam("ny", ny)
                .build(true) // 인코딩된 URI 생성
                .toUriString();

            String result = restTemplate.getForObject(url, String.class);
            if (result == null) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("기상청 응답 없음");
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Weather API 호출 실패: " + e.getMessage());
        }
    }
}