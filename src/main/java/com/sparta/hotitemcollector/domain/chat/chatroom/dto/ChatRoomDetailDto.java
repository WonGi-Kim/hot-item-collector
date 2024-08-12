package com.sparta.hotitemcollector.domain.chat.chatroom.dto;

import com.sparta.hotitemcollector.domain.user.User;
import lombok.Getter;

@Getter
public class ChatRoomDetailDto {
    private String roomId;
    private String roomName;
    private String buyerName;
    private String sellerName;
    private String sellerImage;

    public ChatRoomDetailDto(String roomId, String roomName, String buyerName, String sellerName, String sellerImage) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.buyerName = buyerName;
        this.sellerName = sellerName;
        this.sellerImage = sellerImage;
    }
}
