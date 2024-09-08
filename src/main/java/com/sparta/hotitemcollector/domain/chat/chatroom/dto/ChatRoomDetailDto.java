package com.sparta.hotitemcollector.domain.chat.chatroom.dto;

import com.sparta.hotitemcollector.domain.user.User;
import lombok.Getter;

@Getter
public class ChatRoomDetailDto {
    private String roomId;
    private String lastMessage;
    private String buyerLoginId;
    private String sellerLoginId;
    private String sellerImage;

    public ChatRoomDetailDto(String roomId, String lastMessage, String buyerLoginId, String sellerLoginId, String sellerImage) {
        this.roomId = roomId;
        this.lastMessage = lastMessage;
        this.buyerLoginId = buyerLoginId;
        this.sellerLoginId = sellerLoginId;
        this.sellerImage = sellerImage;
    }
}
