package com.sparta.hotitemcollector.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class OauthSignupRequestDto {

    private Long oauthId;

    private String email;

    private String socialId;
    @NotBlank(message = "사용자 ID는 필수 입력 사항입니다.")
    @Pattern(regexp = "^[a-z0-9]{4,10}$",
            message = "아이디는 최소 4자 이상, 10자 이하이며 알파벳 소문자(a~z), 숫자(0~9)로 구성되어야 합니다.")
    private String loginId;
    @NotBlank(message = "비밀번호는 필수 입력 사항입니다.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,15}$",
            message = "최소 8자 이상, 15자 이하이며 알파벳 대소문자(az, AZ), 숫자(0~9),특수문자를 각각 1자 이상 포함해야 합니다.")
    private String password;
    @NotBlank(message = "이름은 필수 입력 사항입니다.")
    private String username;
}
