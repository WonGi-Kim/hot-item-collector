package com.sparta.hotitemcollector.domain.chat.chatmessage;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatMessageDto {
    private String id;
    private String roomId;
    private String message;
    private String sender;
    private LocalDateTime timestamp;
}
