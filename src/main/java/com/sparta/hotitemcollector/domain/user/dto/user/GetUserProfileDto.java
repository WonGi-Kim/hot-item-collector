package com.sparta.hotitemcollector.domain.user.dto.user;

import com.sparta.hotitemcollector.domain.user.User;
import lombok.Getter;

@Getter
public class GetUserProfileDto {
    private Long id;
    private String nickname;
    private String info;
    private ProfileImageResponseDto profileImage;
    private Long followerCount;

    public GetUserProfileDto(User user, ProfileImageResponseDto profileImageResponseDto,Long followerCount) {
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.info = user.getInfo();
        this.profileImage = profileImageResponseDto;
        this.followerCount=followerCount;
    }
}
