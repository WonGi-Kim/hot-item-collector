package com.sparta.hotitemcollector.domain.user.dto.user;

import com.sparta.hotitemcollector.domain.user.ProfileImage;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProfileImageResponseDto {
    private Long id;
    private String filename;
    private String imageUrl;

    public ProfileImageResponseDto(ProfileImage profileImage) {
        this.id= profileImage.getId();
        this.filename= profileImage.getFilename();
        this.imageUrl= profileImage.getImageUrl();
    }
}
