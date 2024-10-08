package com.sparta.hotitemcollector.domain.user;

import com.sparta.hotitemcollector.domain.chat.chatroom.ChatRoom;
import com.sparta.hotitemcollector.domain.user.dto.user.ProfileImageRequestDto;
import com.sparta.hotitemcollector.global.Timestamped;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Entity
@NoArgsConstructor
public class User extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(name = "login_id",nullable = false)
    private String loginId;

    @Column(nullable = false)
    private String email;

    @Column
    private String nickname;

    @Setter
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private UserRole role;

    @Setter
    @Column
    private String address;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "profileImage_id")
     private ProfileImage profileImage;

    @Setter
    @Column
    private String phoneNumber;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private UserStatus userStatus;

    @Column
    private String info;

    // User가 생성한 ChatRoom 리스트 (buyer 혹은 seller일 수 있음)
    @OneToMany(mappedBy = "buyer")
    private List<ChatRoom> buyerChatRooms;

    @OneToMany(mappedBy = "seller")
    private List<ChatRoom> sellerChatRooms;

    @Builder
    public User(String loginId,String password,String email, String username, String nickname,ProfileImage profileImage){
        this.userStatus = UserStatus.NORMAL;
        this.role = UserRole.USER;
        this.loginId = loginId;
        this.email = email;
        this.password = password;
        this.username = username;
        this.nickname = nickname;
        this.profileImage=profileImage;
    }

    public void updateStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
    }

    public void updatePhoneNumber(String phoneNumber) {this.phoneNumber = phoneNumber;}

    public void updateAddress(String address) {this.address = address;}

    public void updateInfo(String info) {this.info = info;}

    public void updatePassword(String password) {this.password = password;}

    public void updateNickname(String nickname) {this.nickname = nickname;}

    public boolean isExist() {
        return this.userStatus == UserStatus.NORMAL;
    }

    public void updateProfileImage(ProfileImage profileImage) {
        this.profileImage=profileImage;
    }
}
