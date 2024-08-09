package com.sparta.hotitemcollector.domain.user;

import com.sparta.hotitemcollector.domain.user.dto.user.ProfileImageRequestDto;
import com.sparta.hotitemcollector.global.Timestamped;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Setter
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

    @Setter
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "profileImage_id")
     private ProfileImage profileImage;

    @Setter
    @Column
    private String phoneNumber;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private UserStatus userStatus;

    @Setter
    @Column
    private String info;

    @Builder
    public User(String loginId,String password, String username, String nickname,ProfileImage profileImage){
        this.userStatus = UserStatus.NORMAL;
        this.role = UserRole.USER;
        this.loginId = loginId;
        this.password = password;
        this.username = username;
        this.nickname = nickname;
        this.profileImage=profileImage;
    }

    public void updateStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
    }

    public boolean isExist() {
        return this.userStatus == UserStatus.NORMAL;
    }

    public void updateProfileImage(ProfileImage profileImage) {
        this.profileImage=profileImage;
    }
}
