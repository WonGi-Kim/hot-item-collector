package com.sparta.hotitemcollector.domain.chat.chatroom.dto;

import lombok.Getter;

@Getter
public class ChatRoomDetailDto {
    private String roomId;
    private String roomName;
    private String buyer;
    private String seller;

    public ChatRoomDetailDto(String roomId, String roomName, String buyer, String seller) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.buyer = buyer;
        this.seller = seller;
    }
}
