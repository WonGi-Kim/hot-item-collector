package com.sparta.hotitemcollector.global.config;

import com.sparta.hotitemcollector.domain.chat.chatroom.ChatRoomService;
import com.sparta.hotitemcollector.domain.security.UserDetailsServiceImpl;
import com.sparta.hotitemcollector.global.exception.CustomException;
import com.sparta.hotitemcollector.global.exception.ErrorCode;
import com.sparta.hotitemcollector.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final ChatRoomService chatRoomService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);


        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");
            if (token != null && token.startsWith(JwtUtil.BEARER_PREFIX)) {
                token = jwtUtil.substringToken(token); // "Bearer " 접두사 제거
                try {
                    if (jwtUtil.validateToken(token)) {
                        String username = jwtUtil.getLoginIdFromToken(token);
                        UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(username);

                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
                        securityContext.setAuthentication(authentication);
                        SecurityContextHolder.setContext(securityContext);

                        accessor.setUser(authentication); // Authentication 객체를 설정
                        accessor.getSessionAttributes().put("SPRING_SECURITY_CONTEXT", securityContext);
                    } else {
                        throw new CustomException(ErrorCode.INVALID_TOKEN);
                    }
                } catch (Exception e) {
                    throw new CustomException(ErrorCode.INVALID_TOKEN);
                }
            } else {
                throw new CustomException(ErrorCode.HEADER_NOT_FOUND);
            }
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();  // 예: "/topic/{roomId}"
            if (destination != null && destination.startsWith("/topic/")) {
                String roomIdInMessage = destination.substring("/topic/".length()); // "/topic/" 뒤의 부분만 추출
                accessor.getSessionAttributes().put("roomId", roomIdInMessage);
            }
        } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            // DISCONNECT 요청 처리
            String roomId = (String) accessor.getSessionAttributes().get("roomId");
            log.info("Room ID {} retrieved from session", roomId);

            if (roomId != null) {
                // roomId를 사용하여 필요한 로직 처리
                chatRoomService.updateLastMessage(roomId);
            } else {
                log.warn("Room ID not found in session");
            }
        }
        return message;
    }
}