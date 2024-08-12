package com.sparta.hotitemcollector.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.hotitemcollector.domain.security.UserDetailsServiceImpl;
import com.sparta.hotitemcollector.domain.token.TokenService;
import com.sparta.hotitemcollector.domain.user.UserRepository;
import com.sparta.hotitemcollector.domain.user.login.filter.CustomJsonUsernamePasswordAuthenticationFilter;
import com.sparta.hotitemcollector.domain.user.login.handler.LoginFailureHandler;
import com.sparta.hotitemcollector.domain.user.login.handler.LoginSuccessHandler;
import com.sparta.hotitemcollector.domain.user.oauth2.handler.OAuth2LoginFailureHandler;
import com.sparta.hotitemcollector.domain.user.oauth2.handler.OAuth2LoginSuccessHandler;
import com.sparta.hotitemcollector.domain.user.oauth2.service.CustomOAuth2UserService;
import com.sparta.hotitemcollector.global.jwt.JwtAuthenticationFilter;
import com.sparta.hotitemcollector.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    @Value("${SECURITY_PERMITTED_URLS}")
    private String permittedUrlsString;

    private final UserDetailsServiceImpl UserDetailsServiceImpl;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final TokenService tokenService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

        // Convert the comma-separated URLs string to a List
        List<String> permittedUrls = Arrays.stream(permittedUrlsString.split(","))
                .map(String::trim)
                .collect(Collectors.toList());

        // CSRF 비활성화
        http.csrf((csrf) -> csrf.disable());

        // 세션을 생성하지 않음
        http.sessionManagement((sessionManagement) ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        // 요청 권한 설정
        http.authorizeHttpRequests((authorizeHttpRequests) ->
                authorizeHttpRequests
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/profile/**").permitAll()
                        .requestMatchers(permittedUrls.toArray(new String[0])).permitAll()
                        .requestMatchers("/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/prepare/order").permitAll()
                        .requestMatchers(HttpMethod.GET, "/prepare/payment").permitAll()
                        .anyRequest().authenticated()
        );

        // 소셜 로그인 설정
        http.oauth2Login(oauth2Login -> oauth2Login
                .loginProcessingUrl("/login/oauth2/code/*") // 추가됨!
                .userInfoEndpoint(userInfoEndpoint ->
                        userInfoEndpoint.userService(customOAuth2UserService)
                )
                .successHandler(oAuth2LoginSuccessHandler)
                .failureHandler(oAuth2LoginFailureHandler)

        );

        // 커스텀 필터 추가
        http.addFilterAfter(customJsonUsernamePasswordAuthenticationFilter(), LogoutFilter.class);
        http.addFilterBefore(jwtAuthenticationProcessingFilter(), CustomJsonUsernamePasswordAuthenticationFilter.class);

        // JWT 인증 필터 추가
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(UserDetailsServiceImpl);
        return new ProviderManager(provider);
    }

    @Bean
    public LoginSuccessHandler loginSuccessHandler() {
        return new LoginSuccessHandler(jwtUtil, userRepository,tokenService);
    }

    @Bean
    public LoginFailureHandler loginFailureHandler() {
        return new LoginFailureHandler();
    }

    @Bean
    public CustomJsonUsernamePasswordAuthenticationFilter customJsonUsernamePasswordAuthenticationFilter() {
        CustomJsonUsernamePasswordAuthenticationFilter customFilter
                = new CustomJsonUsernamePasswordAuthenticationFilter(objectMapper);
        customFilter.setAuthenticationManager(authenticationManager());
        customFilter.setAuthenticationSuccessHandler(loginSuccessHandler());
        customFilter.setAuthenticationFailureHandler(loginFailureHandler());
        return customFilter;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationProcessingFilter() {
        return new JwtAuthenticationFilter(jwtUtil, UserDetailsServiceImpl);
    }


}
