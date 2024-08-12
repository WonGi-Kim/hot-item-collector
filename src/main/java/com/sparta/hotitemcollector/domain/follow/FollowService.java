package com.sparta.hotitemcollector.domain.follow;

import com.sparta.hotitemcollector.domain.user.ProfileImage;
import com.sparta.hotitemcollector.domain.user.User;
import com.sparta.hotitemcollector.domain.user.dto.user.ProfileImageResponseDto;

import com.sparta.hotitemcollector.domain.user.UserService;

import com.sparta.hotitemcollector.global.exception.CustomException;
import com.sparta.hotitemcollector.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService {
    private final FollowRepository followRepository;
    private final UserService userService;

    @Transactional
    public void createFollow(Long userId, User followerUser) {
        if(userId.equals(followerUser.getId())){
            throw new CustomException(ErrorCode.SELF_FOLLOW);
        }
        // 팔로우 당하는 사람 ERD에서 following_id
        User followingUser = userService.findByUserId(userId);

        if (checkFollowAlready(followerUser, followingUser)) {
            throw new CustomException(ErrorCode.ALREADY_EXIST_FOLLOW);
        }

        Follow newFollow = Follow.builder()
                .follower(followerUser)
                .following(followingUser)
                .build();

        followRepository.save(newFollow);
    }

    @Transactional
    public void deleteFollow(Long userId, User followerUser) {
        User followingUser = userService.findByUserId(userId);

        if (!checkFollowAlready(followerUser, followingUser)) {
            throw new CustomException(ErrorCode.ALREADY_UNFOLLOWED);
        }

        Follow followForDelete = checkFollowExists(followerUser, followingUser);

        followRepository.delete(followForDelete);
    }

    @Transactional(readOnly = true)
    public UserFollowResponseDto getUserFollow(Long userId, User followerUser) {
        User followingUser = userService.findByUserId(userId);
        return new UserFollowResponseDto(checkFollowAlready(followerUser, followingUser));
    }

    @Transactional(readOnly = true)
    public List<GetAllFollowsResponseDto> getFollows(User user) {
        List<Follow> followList = getAllFollowers(user);

        return followList.stream()
            .map(follow -> {
                User followingUser = follow.getFollowing();
                ProfileImage profileImage = followingUser.getProfileImage();
                ProfileImageResponseDto profileImageDto = new ProfileImageResponseDto(profileImage);
                return new GetAllFollowsResponseDto(
                    followingUser.getId(),
                    profileImageDto,
                    followingUser.getInfo(),
                    followingUser.getNickname()
                );
            })
            .toList();
    }

    public boolean checkFollowAlready(User followerUser, User followingUser) {
        return followRepository.existsByFollowerIdAndFollowingId(followerUser.getId(), followingUser.getId());
    }

    public Follow checkFollowExists(User followerUser, User followingUser) {
        return followRepository.findByFollowerIdAndFollowingId(followerUser.getId(), followingUser.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FOLLOW));
    }

    public List<Follow> getAllFollowers(User user) {
        return followRepository.findByFollowerId(user.getId());
    }
}
