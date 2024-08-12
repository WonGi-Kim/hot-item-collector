package com.sparta.hotitemcollector.domain.chat.chatmessage;

import com.sparta.hotitemcollector.global.Timestamped;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Document(collection = "chat_message")
@NoArgsConstructor
public class ChatMessage {
    @Id
    private String id;
    private String roomId;
    private String message;
    private String sender;

    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public ChatMessage(String id, String roomId, String message, String sender, LocalDateTime createdAt) {
        this.id = id;
        this.roomId = roomId;
        this.message = message;
        this.sender = sender;
        this.createdAt = createdAt;
    }
}
