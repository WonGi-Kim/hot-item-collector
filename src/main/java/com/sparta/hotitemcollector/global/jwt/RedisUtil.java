package com.sparta.hotitemcollector.global.jwt;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Configuration
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisTemplate<String, Object> redisBlackListTemplate;

    // Initialize serializers in constructor
    public RedisUtil(RedisTemplate<String, Object> redisTemplate, RedisTemplate<String, Object> redisBlackListTemplate) {
        this.redisTemplate = redisTemplate;
        this.redisBlackListTemplate = redisBlackListTemplate;
        initializeSerializers();
    }

    private void initializeSerializers() {
        Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        redisTemplate.setValueSerializer(jsonSerializer);
        redisBlackListTemplate.setValueSerializer(jsonSerializer);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisBlackListTemplate.setKeySerializer(new StringRedisSerializer());
    }

    public <T> void set(String key, T object, int expire) {
        redisTemplate.opsForValue().set(key, object, expire, TimeUnit.MINUTES);
    }

    public <T> T get(String key, Class<T> clazz) {
        return clazz.cast(redisTemplate.opsForValue().get(key));
    }

    public boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public <T> void setBlackList(String key, T object, Long milliSeconds) {
        redisBlackListTemplate.opsForValue().set(key, object, milliSeconds, TimeUnit.MILLISECONDS);
    }

    public <T> T getBlackList(String key, Class<T> clazz) {
        return clazz.cast(redisBlackListTemplate.opsForValue().get(key));
    }

    public boolean deleteBlackList(String key) {
        return Boolean.TRUE.equals(redisBlackListTemplate.delete(key));
    }

    public boolean hasKeyBlackList(String key) {
        return Boolean.TRUE.equals(redisBlackListTemplate.hasKey(key));
    }

    public void deleteAll() {
        redisTemplate.delete(Objects.requireNonNull(redisTemplate.keys("*")));
    }

    // Email Authentication Code Operations
    private static final String AUTH_CODE_PREFIX = "authCode:";

    public void saveAuthCode(String email, String authCode, long expireMinutes) {
        set(AUTH_CODE_PREFIX + email, authCode, (int) expireMinutes);
    }

    public String getAuthCode(String email) {
        return get(AUTH_CODE_PREFIX + email, String.class);
    }

    public boolean validateAuthCode(String email, String authCode) {
        String storedCode = getAuthCode(email);
        return authCode != null && authCode.equals(storedCode);
    }

    public boolean deleteAuthCode(String email) {
        return delete(AUTH_CODE_PREFIX + email);
    }
}
