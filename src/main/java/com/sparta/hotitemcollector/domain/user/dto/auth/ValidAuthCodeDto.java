package com.sparta.hotitemcollector.domain.user.dto.auth;

import lombok.Getter;

@Getter
public class ValidAuthCodeDto {
    private String email;
    private String authCode;
}
