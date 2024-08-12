package com.sparta.hotitemcollector.domain.chat.chatroom;

import com.sparta.hotitemcollector.domain.chat.chatroom.dto.ChatRoomDetailDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;

    @Transactional
    public ChatRoomDetailDto createChatRoom(String buyer, String seller) {
        // roomId는 대화하기 버튼이 눌릴 시 생성
        String roomId = generateRoomId();
        String roomName = generateRoomName(buyer,seller);

        // 채팅방이 이미 존재하는지 확인
        Optional<ChatRoom> existingChatRoom = chatRoomRepository.findByBuyerAndSeller(buyer, seller);

        ChatRoom chatRoom = existingChatRoom.orElseGet(() -> {
            ChatRoom newChatRoom = ChatRoom.builder()
                    .roomId(roomId)
                    .roomName(roomName)
                    .buyer(buyer)
                    .seller(seller)
                    .build();
            return chatRoomRepository.save(newChatRoom);
        });

        return convertChatRoomToChatRoomDetailDto(chatRoom);
    }

    public List<ChatRoomDetailDto> getAllChatRoomByUser(String nickname) {
        List<ChatRoom> chatRooms = chatRoomRepository.findAllByBuyer(nickname);
        return chatRooms.stream()
                .map(this::convertChatRoomToChatRoomDetailDto)
                .collect(Collectors.toList());
    }

    public ChatRoomDetailDto getChatRoom(String roomId) {
        ChatRoom chatRoom = getChatRoomById(roomId);
        return convertChatRoomToChatRoomDetailDto(chatRoom);
    }

    public ChatRoom getChatRoomById(String roomId) {
        return chatRoomRepository.findByRoomId(roomId).orElseThrow(() -> new IllegalArgumentException("ChatRoom not found with roomId: " + roomId));
    }

    private String generateRoomId() {
        return UUID.randomUUID().toString();
    }
    private String generateRoomName(String buyer, String seller) {
        return buyer + " 와 " + seller + " 의 채팅방";
    }

    private ChatRoomDetailDto convertChatRoomToChatRoomDetailDto(ChatRoom chatRoom) {
        return new ChatRoomDetailDto(chatRoom.getRoomId(), chatRoom.getRoomName(), chatRoom.getBuyer(), chatRoom.getSeller());
    }

}
