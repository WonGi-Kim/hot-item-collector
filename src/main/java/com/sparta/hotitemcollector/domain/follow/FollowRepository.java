package com.sparta.hotitemcollector.domain.follow;

import com.sparta.hotitemcollector.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    List<Follow> findByFollowerId(Long id);

    Long countByFollowing(User user);
}
