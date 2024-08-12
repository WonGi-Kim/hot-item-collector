package com.sparta.hotitemcollector.domain.user.dto.auth;

import com.sparta.hotitemcollector.domain.user.dto.OauthSignupRequestDto;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ConnectAccountRequestDto {
    private Long oauthId;
    private String socialId;
    private String loginId;
    private String password;

    public ConnectAccountRequestDto(OauthSignupRequestDto oauthSignupRequestDto) {
        this.oauthId = oauthSignupRequestDto.getOauthId();
        this.socialId = oauthSignupRequestDto.getSocialId();
        this.loginId = oauthSignupRequestDto.getLoginId();
        this.password = oauthSignupRequestDto.getPassword();
    }
}
