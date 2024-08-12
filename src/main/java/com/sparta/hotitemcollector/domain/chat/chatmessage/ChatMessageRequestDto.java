package com.sparta.hotitemcollector.domain.chat.chatmessage;

import lombok.Getter;

@Getter
public class ChatMessageRequestDto {
    private String roomId;
    private String message;
}
