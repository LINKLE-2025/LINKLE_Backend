package com.linkle.config;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;


@Configuration
public class HttpConfig {

    // connect-timeout-ms: 클라이언트(Spring) -> 서버(Flask)로 TCP 연결을 맺을 떄까지 기다리는 시간
    // read-timeout-ms: 연결은 성공했지만, 서버가 응답을 보내기 시작하지 않을떄까지 기다리는 시간
    @Bean
    public RestTemplate restTemplate(
        @Value("${app.recommend.connect-timeout-ms:2000}") int connectTimeout,
        @Value("${app.recommend.read-timeout-ms:3000}") int readTimeout) {
        /* HttpComponentsClientHttpRequestFactory
        Spring 프레임워크의 RestTemplate에서 Apache HttpComponents 라이브러리를 사용하여 HTTP 통신을 수행하도록 하는 ClientHttpRequestFactory
        즉, HTTP 통신을 위한 Connenct의 고급 설정을 가능하게 해주는 요소이다.
        */
        HttpClient httpClient = HttpClients.custom().build();
        var factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);

        return new RestTemplate(factory);
    }
}