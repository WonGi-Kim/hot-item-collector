package com.sparta.hotitemcollector.domain.user;

import com.sparta.hotitemcollector.domain.s3.service.ImageService;
import com.sparta.hotitemcollector.domain.s3.service.S3Service;
import com.sparta.hotitemcollector.domain.security.UserDetailsImpl;
import com.sparta.hotitemcollector.domain.user.dto.OauthSignupRequestDto;
import com.sparta.hotitemcollector.domain.user.dto.auth.ValidAuthCodeDto;
import com.sparta.hotitemcollector.domain.user.dto.auth.*;
import com.sparta.hotitemcollector.domain.user.dto.user.*;
import com.sparta.hotitemcollector.global.common.CommonResponse;
import com.sparta.hotitemcollector.global.email.EmailMessage;
import com.sparta.hotitemcollector.global.email.EmailPostDto;
import com.sparta.hotitemcollector.global.email.EmailResponseDto;
import com.sparta.hotitemcollector.global.email.EmailService;
import jakarta.validation.Valid;

import java.io.IOException;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final ImageService imageService;
    private final S3Service s3Service;
    private final EmailService emailService;

    @PostMapping("/signup")
    public ResponseEntity<CommonResponse> signup(@Valid @RequestBody SignupRequestDto requestDto) {
        userService.signup(requestDto);
        CommonResponse response = new CommonResponse<>("회원가입 성공", 201, "");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/oauth/signup")
    public ResponseEntity<CommonResponse> oauthSignup(@Valid @RequestBody OauthSignupRequestDto requestDto) {
        userService.oauthSignup(requestDto);
        CommonResponse response = new CommonResponse<>("회원가입 성공", 201, "");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

//    @PostMapping("/login")
//    public ResponseEntity<CommonResponse> login(@RequestBody LoginReqeustDto requestDto) {
//        LoginResponseDto responseDto = userService.login(requestDto);
//        CommonResponse response = new CommonResponse<>("로그인 성공",200,responseDto);
//        return new ResponseEntity<>(response, HttpStatus.OK);
//    }

    @PostMapping("/connect")
    public ResponseEntity<CommonResponse> connectAccount(@RequestBody ConnectAccountRequestDto requestDto) {
        try {
            userService.connectAccount(requestDto);
            CommonResponse response = new CommonResponse<>("소셜로그인 연결 성공", 201, "");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            // 로그 기록 및 예외 처리
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 임시 비밀번호 발급
    @PostMapping("/password")
    public ResponseEntity sendPasswordMail(@RequestBody EmailPostDto emailPostDto) {
        EmailMessage emailMessage = EmailMessage.builder()
                .to(emailPostDto.getEmail())
                .subject("[SAVIEW] 임시 비밀번호 발급")
                .build();

        emailService.sendPasswordResetEmail(emailMessage);

        return ResponseEntity.ok().build();
    }

    // 회원가입 이메일 인증 - 요청 시 body로 인증번호 반환하도록 작성하였음
    @PostMapping("/email")
    public ResponseEntity sendJoinMail(@RequestBody EmailPostDto emailPostDto) {
        EmailMessage emailMessage = EmailMessage.builder()
                .to(emailPostDto.getEmail())
                .subject("[HotItemCollector] 이메일 인증을 위한 인증 코드 발송")
                .build();

        String code = emailService.sendAuthCodeEmail(emailMessage);

        EmailResponseDto emailResponseDto = new EmailResponseDto();
        emailResponseDto.setCode(code);

        return ResponseEntity.ok(emailResponseDto);
    }

    @PostMapping("/email/validate")
    public ResponseEntity<CommonResponse> validateAuthCode(@RequestBody ValidAuthCodeDto validAuthCodeDto) {
        emailService.validateAuthCode(validAuthCodeDto.getEmail(), validAuthCodeDto.getAuthCode());
        CommonResponse response = new CommonResponse<>("이메일 인증 성공", 200, "");
        return new ResponseEntity<>(response, HttpStatus.OK);

    }


    @PostMapping("/confirm/password")
    public ResponseEntity<CommonResponse> confirmPassword(@RequestBody ConfirmPasswordDto requestDto,
                                                          @AuthenticationPrincipal UserDetailsImpl userDetails) {
        userService.confirmPassword(requestDto, userDetails.getUser());
        CommonResponse response = new CommonResponse<>("비밀번호 확인 성공", 200, "");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<CommonResponse> logout(@RequestHeader("Authorization") String accessToken,
                                                 @AuthenticationPrincipal UserDetailsImpl userDetails) {
        userService.logout(accessToken, userDetails.getUser());
        CommonResponse response = new CommonResponse<>("로그아웃 성공", 200, "");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/withdraw")
    public ResponseEntity<CommonResponse> withdraw(@RequestHeader("Authorization") String accessToken,
                                                   @AuthenticationPrincipal UserDetailsImpl userDetails) {
        userService.withdraw(accessToken, userDetails.getUser());
        CommonResponse response = new CommonResponse<>("회원탈퇴 성공", 204, "");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<CommonResponse> refreshToken(@RequestBody RefreshRequestDto refreshRequestDto) {
        LoginResponseDto responseDto = userService.refreshToken(refreshRequestDto);

        CommonResponse response = new CommonResponse<>("access 토큰 재발급 성공", 200, responseDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/profile")
    public ResponseEntity<CommonResponse> updateProfile(@RequestPart("requestDto") ProfileRequestDto requestDto,
                                                        @AuthenticationPrincipal UserDetailsImpl userDetails, @RequestPart(value = "files", required = false) MultipartFile file)
            throws IOException {
        if (file != null) {
            // 파일 유효성 검사
            imageService.validateFile(file);
            // S3에 파일 업로드
            ProfileImageRequestDto image = s3Service.uploadFile(file);
            requestDto.addImage(image);
        }
        ProfileResponseDto responseDto = userService.updateProfile(requestDto, userDetails.getUser());
        CommonResponse response = new CommonResponse<>("회원 정보 수정 성공", 200, responseDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/password")
    public ResponseEntity<CommonResponse> updatePassword(@Valid @RequestBody updatePasswordRequestDto requestDto,
                                                         @AuthenticationPrincipal UserDetailsImpl userDetails) {
        userService.updatePassword(requestDto, userDetails.getUser());
        CommonResponse response = new CommonResponse<>("비밀번호 수정 성공", 200, "");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<CommonResponse> getUserProfile(@PathVariable Long userId) {
        GetUserProfileDto profile = userService.getUserProfile(userId);
        CommonResponse response = new CommonResponse<>("유저 프로필 조회 성공", 200, profile);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/profile")
    public ResponseEntity<CommonResponse> getMyProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        GetMyProfileDto profile = userService.getMyProfile(userDetails.getUser());
        CommonResponse response = new CommonResponse<>("마이 프로필 조회 성공", 200, profile);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/profile/address")
    public ResponseEntity<CommonResponse> getUserAddress(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserAddressDto profile = userService.getUserAddress(userDetails.getUser());
        CommonResponse response = new CommonResponse<>("유저 주소 조회 성공", 200, profile);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
