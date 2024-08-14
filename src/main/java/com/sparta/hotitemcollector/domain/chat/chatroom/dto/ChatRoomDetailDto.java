package com.sparta.hotitemcollector.domain.chat.chatroom.dto;

import com.sparta.hotitemcollector.domain.user.User;
import lombok.Getter;

@Getter
public class ChatRoomDetailDto {
    private String roomId;
    private String roomName;
    private String buyerLoginId;
    private String sellerLoginId;
    private String sellerImage;

    public ChatRoomDetailDto(String roomId, String roomName, String buyerLoginId, String sellerLoginId, String sellerImage) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.buyerLoginId = buyerLoginId;
        this.sellerLoginId = sellerLoginId;
        this.sellerImage = sellerImage;
    }
}
