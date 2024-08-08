package com.sparta.hotitemcollector.domain.user.login.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.hotitemcollector.domain.token.TokenService;
import com.sparta.hotitemcollector.domain.user.User;
import com.sparta.hotitemcollector.domain.user.UserRepository;
import com.sparta.hotitemcollector.global.common.CommonResponse;
import com.sparta.hotitemcollector.global.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON 변환을 위한 ObjectMapper

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String loginId = extractUsername(authentication); // 인증 정보에서 Username(email) 추출

        Optional<User> user = userRepository.findByLoginId(loginId);
        if (user.isPresent()) {
            String accessToken = jwtUtil.createAccessToken(user.get().getLoginId(),user.get().getRole()); // JwtService의 createAccessToken을 사용하여 AccessToken 발급
            String refreshToken = jwtUtil.createRefreshToken(user.get().getLoginId(),user.get().getRole()); // JwtService의 createRefreshToken을 사용하여 RefreshToken 발급

            tokenService.findRefreshToken(user.get()).ifPresentOrElse(
                    token -> tokenService.updateToken(token, refreshToken),
                    () -> tokenService.saveToken(user.get(), refreshToken)
            );
            log.info("User found and token updated/saved");
            jwtUtil.sendAccessAndRefreshToken(response,accessToken,refreshToken);

        }


        CommonResponse commonResponse = new CommonResponse<>("로그인 성공",200,null);

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        String jsonResponse = objectMapper.writeValueAsString(commonResponse);
        response.getWriter().write(jsonResponse);
        // 응답 헤더에 AccessToken, RefreshToken 실어서 응답
    }

    private String extractUsername(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }

}