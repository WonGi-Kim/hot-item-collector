package com.sparta.hotitemcollector.domain.chat.chatroom;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByRoomId(String roomId);
    Optional<ChatRoom> findByBuyerAndSeller(String buyer, String seller);
    List<ChatRoom> findAllByBuyer(String buyer);

    List<ChatRoom> findAllByBuyerOrSeller(String nickname, String nickname1);
}
