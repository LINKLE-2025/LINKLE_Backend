package com.linkle.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final String serviceKey = "QkxHc6vrSYKMR3Or65mCfg"; // TODO: 환경변수 또는 yml에 분리하세요

    @GetMapping("/current")
    public ResponseEntity<String> getCurrentWeather(
        @RequestParam double lat,
        @RequestParam double lon) {

        try {
            // (1) 위경도 → 격자 변환
            String xyUrl = String.format(
                "https://apihub.kma.go.kr/api/typ01/cgi-bin/url/nph-dfs_xy_lonlat?lon=%f&lat=%f&help=0&authKey=%s",
                lon, lat, serviceKey
            );

            RestTemplate restTemplate = new RestTemplate();
            String xyResponse = restTemplate.getForObject(xyUrl, String.class);

            // 파싱 (예: "lon lat x y\n127.5 36.5 60 127")
            String[] lines = xyResponse.trim().split("\n");
            String[] parts = lines[1].split("\\s+");
            String x = parts[2];
            String y = parts[3];

            // (2) 발표시간 (yyyyMMddHHmm)
            String tmfc = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));

            // (3) 기상청 실황 조회 API 호출
            String url = String.format(
                "https://apihub.kma.go.kr/api/typ01/cgi-bin/url/nph-dfs_odam_grd?tmfc=%s&x=%s&y=%s&vars=T1H,PTY&authKey=%s",
                tmfc, x, y, serviceKey
            );

            String result = restTemplate.getForObject(url, String.class);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Weather API 호출 실패: " + e.getMessage());
        }
    }
}