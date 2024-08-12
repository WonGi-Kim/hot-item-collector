package com.sparta.hotitemcollector.domain.chat.chatmessage;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public ChatMessageDto sendMessage(String roomId, String message, String sender) {
        ChatMessage chatMessage = ChatMessage.builder()
                .roomId(roomId)
                .message(message)
                .sender(sender)
                .createdAt(LocalDateTime.now())
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        return convertToDto(savedMessage);
    }

    public List<ChatMessageDto> getMessagesByRoomId(String roomId) {
        List<ChatMessage> messages = chatMessageRepository.findByRoomId(roomId);
        return messages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ChatMessageDto convertToDto(ChatMessage chatMessage) {
        return ChatMessageDto.builder()
                .id(chatMessage.getId())
                .roomId(chatMessage.getRoomId())
                .message(chatMessage.getMessage())
                .sender(chatMessage.getSender())
                .timestamp(chatMessage.getCreatedAt())
                .build();
    }
}
