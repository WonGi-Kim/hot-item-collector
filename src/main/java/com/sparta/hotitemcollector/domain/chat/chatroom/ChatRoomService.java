package com.sparta.hotitemcollector.domain.chat.chatroom;

import com.sparta.hotitemcollector.domain.chat.chatroom.dto.ChatRoomDetailDto;
import com.sparta.hotitemcollector.domain.user.User;
import com.sparta.hotitemcollector.domain.user.UserService;
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
    private final UserService userService;

    @Transactional
    public ChatRoomDetailDto createChatRoom(Long buyerId, Long sellerId) {
        // buyerId와 sellerId를 정렬
        Long firstUserId, secondUserId;
        User firstUser, secondUser;

        if (buyerId < sellerId) {
            firstUserId = buyerId;
            secondUserId = sellerId;
            firstUser = userService.findByUserId(buyerId);
            secondUser = userService.findByUserId(sellerId);
        } else {
            firstUserId = sellerId;
            secondUserId = buyerId;
            firstUser = userService.findByUserId(sellerId);
            secondUser = userService.findByUserId(buyerId);
        }

        String roomId = generateRoomId();
        String roomName = generateRoomName(firstUser.getNickname(), secondUser.getNickname());

        // 채팅방이 이미 존재하는지 확인
        Optional<ChatRoom> existingChatRoom = chatRoomRepository.findByBuyerIdAndSellerId(firstUserId, secondUserId);

        ChatRoom chatRoom = existingChatRoom.orElseGet(() -> {
            ChatRoom newChatRoom = ChatRoom.builder()
                    .roomId(roomId)
                    .roomName(roomName)
                    .buyer(firstUser)
                    .seller(secondUser)
                    .build();
            return chatRoomRepository.save(newChatRoom);
        });

        return convertChatRoomToChatRoomDetailDto(chatRoom);
    }

    public List<ChatRoomDetailDto> getAllChatRoomByUser(Long userId) {
        // Buyer 또는 Seller가 해당 닉네임인 채팅방을 모두 조회
        List<ChatRoom> chatRooms = chatRoomRepository.findAllByBuyerIdOrSellerId(userId, userId);

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
        return new ChatRoomDetailDto(
                chatRoom.getRoomId(),
                chatRoom.getRoomName(),
                chatRoom.getBuyer().getLoginId(),
                chatRoom.getSeller().getLoginId(),
                chatRoom.getSeller().getProfileImage().getImageUrl()
        );
    }

}
