package com.linkle.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.linkle.domain.dto.PaymentResponseDTO;

@Service
public class PortOneService {

    private final RestTemplate restTemplate = new RestTemplate();

    // 🔑 포트원 관리자 콘솔에서 발급받은 REST API Key/Secret
    private final String apiKey = "YOUR_REST_API_KEY";
    private final String apiSecret = "YOUR_REST_API_SECRET";

    /**
     * Access Token 발급
     */
    public String getAccessToken() {
        String url = "https://api.iamport.kr/users/getToken";

        Map<String, String> body = new HashMap<>();
        body.put("imp_key", apiKey);
        body.put("imp_secret", apiSecret);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        Map responseBody = (Map) response.getBody().get("response");

        return (String) responseBody.get("access_token");
    }

    /**
     * imp_uid로 결제 정보 조회
     */
    public PaymentResponseDTO getPaymentInfo(String token, String impUid) {
        String url = "https://api.iamport.kr/payments/" + impUid;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        Map responseBody = (Map) response.getBody().get("response");

        return new PaymentResponseDTO(
            (String) responseBody.get("imp_uid"),
            (String) responseBody.get("merchant_uid"),
            ((Number) responseBody.get("amount")).intValue(),
            (String) responseBody.get("status")
        );
    }
}
