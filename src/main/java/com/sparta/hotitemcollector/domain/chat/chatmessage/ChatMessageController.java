package com.sparta.hotitemcollector.domain.chat.chatmessage;

import com.sparta.hotitemcollector.domain.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
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
    @MessageMapping("/send/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatMessageDto sendMessage(@DestinationVariable String roomId, @RequestBody ChatMessageRequestDto requestDto) {
        // 현재 인증된 사용자 정보 추출
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String sender = userDetails.getUser().getLoginId();

        ChatMessageDto chatMessage = chatMessageService.sendMessage(roomId, requestDto.getMessage(), sender);
        return chatMessage;
    }

    @MessageMapping("/join/{roomId}")
    @SendTo("/topic/{roomId}")
    public List<ChatMessageDto> getMessagesByRoomId(@DestinationVariable String roomId) {
        return chatMessageService.getMessagesByRoomId(roomId);
    }
}