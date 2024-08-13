package com.sparta.hotitemcollector.domain.user.oauthUser;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OAuthUserRepository extends JpaRepository<OAuthUser, Long> {
    Optional<OAuthUser> findBySocialTypeAndEmail(SocialType socialType, String socialId);
    Optional<OAuthUser> findByEmail(String email);

    Optional<OAuthUser> findBySocialId(String SocialId);

    Optional<OAuthUser> findByIdAndSocialId(Long id, String socialId);

    /**
     * 주어진 이메일과 동일한 이메일을 가진 OAuthUser 목록을 찾습니다.
     * User가 null이 아닌 OAuthUser만 포함합니다.
     *
     * @param email    이메일
     * @param socialId 현재 OAuthUser의 socialId (현재 OAuthUser를 제외하기 위해 사용)
     * @return 동일한 이메일을 가진 OAuthUser 목록
     */
    List<OAuthUser> findByEmailAndSocialIdNotAndUserIsNotNull(String email, String socialId);
}
