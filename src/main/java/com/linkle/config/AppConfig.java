package com.linkle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {


    /*
    * RestTemplate는 Spring 에서 제공하는 동기식 Http 클라이언트
    * 다른 서버의 API를 호출할 떄 사용하는 도구/ 현재는 Python API를 호출할때 사용
    * */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}