package com.sparta.hotitemcollector.domain.chat.chatroom;

import com.sparta.hotitemcollector.domain.user.User;
import com.sparta.hotitemcollector.global.Timestamped;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class ChatRoom extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String roomId;
    private String roomName;
    @ManyToOne
    @JoinColumn(name = "buyer_id")
    private User buyer;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;


    @Builder
    public ChatRoom(String roomId,String roomName, User buyer, User seller) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.buyer = buyer;
        this.seller = seller;
    }
}
