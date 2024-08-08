package com.sparta.hotitemcollector.domain.user.oauth2.userInfo;

import java.util.Map;

public class GoogleOAuth2UserInfo extends OAuth2UserInfo {


    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getNickname()  {
        return getAttributeValue("nickname");
    }

    @Override
    public String getImageUrl()  {
        return getAttributeValue("profile_image");
    }

    @Override
    public String getEmail()  {
        return getAttributeValue("email");
    }

    private String getAttributeValue(String key) {
        Object value = attributes.get(key);
        return value != null ? value.toString() : null;
    }
}

