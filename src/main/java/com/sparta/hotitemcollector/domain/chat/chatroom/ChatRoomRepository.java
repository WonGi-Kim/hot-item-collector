package com.sparta.hotitemcollector.domain.chat.chatroom;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByRoomId(String roomId);
    Optional<ChatRoom> findByBuyerIdAndSellerId(Long buyerId, Long sellerId);

    List<ChatRoom> findAllByBuyerIdOrSellerId(Long userId, Long userId1);
}
