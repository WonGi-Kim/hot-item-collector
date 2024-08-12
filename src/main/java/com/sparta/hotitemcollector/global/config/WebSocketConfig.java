package com.sparta.hotitemcollector.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketInterceptor webSocketInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost("localhost") // RabbitMQ 호스트 설정
                .setRelayPort(61613) // RabbitMQ STOMP 포트 (기본 포트)
                .setClientLogin("guest") // RabbitMQ 로그인
                .setClientPasscode("guest") // RabbitMQ 비밀번호
                .setSystemLogin("guest") // RabbitMQ 시스템 로그인
                .setSystemPasscode("guest") // RabbitMQ 시스템 비밀번호
                .setVirtualHost("/") // RabbitMQ 가상 호스트
                .setAutoStartup(true);

        registry.setApplicationDestinationPrefixes("/app"); // 앱 경로
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOrigins("*");
//                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketInterceptor);
    }
}
