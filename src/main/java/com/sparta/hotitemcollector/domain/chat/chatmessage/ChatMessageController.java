package com.sparta.hotitemcollector.domain.chat.chatmessage;

import com.sparta.hotitemcollector.domain.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatMessageController {
    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/send")
    public void sendMessage(@RequestBody ChatMessageRequestDto requestDto) {
        // 현재 인증된 사용자 정보 추출
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String sender = userDetails.getUser().getNickname();

        ChatMessageDto chatMessage = chatMessageService.sendMessage(requestDto.getRoomId(), requestDto.getMessage(), sender);

        // 동적 경로로 메시지 전송
        String destination = "/topic/" + requestDto.getRoomId();
        messagingTemplate.convertAndSend(destination, chatMessage);
    }

    @MessageMapping("/join/{roomId}")
    public void getMessagesByRoomId(@DestinationVariable String roomId) {
        List<ChatMessageDto> messages = chatMessageService.getMessagesByRoomId(roomId);

        // 동적 경로로 메시지 전송
        String destination = "/topic/" + roomId;
        messagingTemplate.convertAndSend(destination, messages);
    }
}