package com.sparta.hotitemcollector.domain.chat.chatmessage;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findByRoomId(String roomId);

    ChatMessage findFirstByRoomIdOrderByCreatedAtDesc(String roomId);
}
