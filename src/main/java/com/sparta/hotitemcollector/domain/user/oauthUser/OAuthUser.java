package com.sparta.hotitemcollector.domain.user.oauthUser;

import com.sparta.hotitemcollector.domain.user.User;
import com.sparta.hotitemcollector.global.Timestamped;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@Entity
@RequiredArgsConstructor
public class OAuthUser extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private SocialType socialType; // KAKAO, NAVER, GOOGLE

    private String socialId;

    private String email;


    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;


    @Builder
    public OAuthUser(SocialType socialType, User user, String socialId,String email) {
        this.email = email;
        this.socialType = socialType;
        this.user = user;
        this.socialId = socialId;
    }

    public void updateUser(User user) {
        this.user = user;
    }

}
