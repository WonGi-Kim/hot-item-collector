package com.sparta.hotitemcollector.domain.user.oauthUser;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuthUserRepository extends JpaRepository<OAuthUser, Long> {
    Optional<OAuthUser> findBySocialTypeAndEmail(SocialType socialType, String socialId);
    Optional<OAuthUser> findByEmail(String email);

    Optional<OAuthUser> findByIdAndSocialId(Long id, String socialId);
}
