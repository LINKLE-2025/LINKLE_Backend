package com.linkle.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // CSRF 비활성화 (POST 허용)
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 적용
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Preflight 허용
                .requestMatchers("/api/**").permitAll()                 // API 모두 허용 (개발 단계)
                .anyRequest().permitAll()
            )
            .httpBasic(basic -> basic.disable())  // 기본 인증 비활성
            .formLogin(form -> form.disable());   // 로그인 폼 비활성
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(List.of(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "http://192.168.0.129:*",   // ✅ 실제 프론트엔드 개발 IP
            "http://192.168.0.128:*",   // ✅ 실제 프론트엔드 개발 IP
            "https://localhost:*",
            "https://127.0.0.1:*",
            "https://192.168.0.129:*",   // ✅ HTTPS도 허용 (백엔드 SSL일 경우)
            "https://192.168.0.128:*",   // ✅ HTTPS도 허용 (백엔드 SSL일 경우)
            "http://192.168.0.156:*",   // ✅ HTTPS도 허용 (백엔드 SSL일 경우)
            "https://192.168.0.156:*"   // ✅ HTTPS도 허용 (백엔드 SSL일 경우)
        ));
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
