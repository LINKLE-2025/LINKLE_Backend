package com.linkle.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.util.CollectionUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final Environment env;

    // ---- WS props ----
    @Value("${ws.endpoint:/ws-stomp}")
    private String endpoint;

    @Value("${ws.app-prefixes:/app}")
    private String[] appPrefixes;

    @Value("${ws.broker-prefixes:/sub,/queue}")
    private String[] brokerPrefixes;

    @Value("${ws.with-sockjs:false}")
    private boolean withSockJs;

    public WebSocketConfig(Environment env) {
        this.env = env;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker(brokerPrefixes);
        registry.setApplicationDestinationPrefixes(appPrefixes);
        log.info("[WS] brokerPrefixes={}, appPrefixes={}",
            String.join(",", brokerPrefixes), String.join(",", appPrefixes));
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // cors.allowed-origins 를 명시적으로 지정한 경우에만 적용
        List<String> origins = Binder.get(env)
            .bind("cors.allowed-origins", Bindable.listOf(String.class))
            .orElse(null);

        var reg = registry.addEndpoint(endpoint);

        if (!CollectionUtils.isEmpty(origins)) {
            reg.setAllowedOriginPatterns(origins.toArray(String[]::new));
            log.info("[WS] endpoint={} allowedOrigins={}", endpoint, origins);
        } else {
            // 지정이 없으면 setAllowedOriginPatterns 호출 X → same-origin만 허용됨 (보안 우선)
            log.warn("[WS] cors.allowed-origins not set. Only same-origin will be allowed for {}", endpoint);
        }

        if (withSockJs) {
            reg.withSockJS();
            log.info("[WS] SockJS enabled");
        }
    }
}
