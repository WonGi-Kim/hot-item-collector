package com.sparta.hotitemcollector.domain.user.oauth2.handler;

import com.sparta.hotitemcollector.domain.token.Token;
import com.sparta.hotitemcollector.domain.token.TokenService;
import com.sparta.hotitemcollector.domain.user.User;
import com.sparta.hotitemcollector.domain.user.oauth2.CustomOAuth2User;
import com.sparta.hotitemcollector.domain.user.oauthUser.OAuthUser;
import com.sparta.hotitemcollector.domain.user.oauthUser.OAuthUserRepository;
import com.sparta.hotitemcollector.global.jwt.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
//@Transactional
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtService;
    private final TokenService tokenService;
    private final OAuthUserRepository oAuthUserRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("OAuth2 Login 성공!");
        try {
            CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
            log.info(oAuth2User.getEmail());
            OAuthUser oAuthUser = oAuthUserRepository.findByEmail(oAuth2User.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("이메일에 해당하는 OAuthUser가 없습니다."));

            // User가 null인 경우 회원가입 페이지로 리다이렉트
            if (oAuthUser.getUser() == null) {
                String redirectUrl = "http://localhost:8081/oauth2/signup?oauthId=" + oAuthUser.getId() + "&socialId=" + oAuthUser.getSocialId();
                response.sendRedirect(redirectUrl);

            } else {
                loginSuccess(request,response, oAuthUser); // 로그인에 성공한 경우 access, refresh 토큰 생성
            }
        } catch (Exception e) {
            throw e;
        }

    }

    // TODO : 소셜 로그인 시에도 무조건 토큰 생성하지 말고 JWT 인증 필터처럼 RefreshToken 유/무에 따라 다르게 처리해보기
    private void loginSuccess(HttpServletRequest request, HttpServletResponse response, OAuthUser oAuthUser) throws IOException {
        log.info("소셜로그인 성공");
        User user = oAuthUser.getUser();
        String accessToken = jwtService.createAccessToken(user.getLoginId(),user.getRole());
        String refreshToken = jwtService.createRefreshToken(user.getLoginId(),user.getRole());


        Optional<Token> optionalToken = tokenService.findRefreshToken(user);
        if(optionalToken.isPresent()){
            if (!optionalToken.get().getRefreshToken().equals(refreshToken)) {
                tokenService.updateToken(optionalToken.get(),refreshToken);
            }
        }else{
            tokenService.saveToken(user, refreshToken);
        }
        jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);
        // 최초 로그인이 아닌 경우 로그인 성공 페이지로 이동
        String redirectURL = UriComponentsBuilder.fromUriString("http://localhost:8081/")
                .queryParam("access", "Bearer "+accessToken)
                .queryParam("refresh", refreshToken)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUriString();
        getRedirectStrategy().sendRedirect(request, response, redirectURL);


//        if(user.getNickname().isEmpty()){
//            response.sendRedirect("http://localhost:8081/oauth2/signup2");
//            jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);
//            return;
//        }

//        response.sendRedirect("http://localhost:8081/");
//        jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);

    }
}
