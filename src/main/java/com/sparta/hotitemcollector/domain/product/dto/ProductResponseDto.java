package com.sparta.hotitemcollector.domain.product.dto;

import com.sparta.hotitemcollector.domain.product.entity.Product;
import com.sparta.hotitemcollector.domain.product.entity.ProductCategory;

import com.sparta.hotitemcollector.domain.product.entity.ProductStatus;
import com.sparta.hotitemcollector.domain.user.dto.user.ProfileImageResponseDto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;

@Getter
public class ProductResponseDto {

    private Long id;
    private String name;
    private ProductCategory category;
    private List<ProductImageResponseDto> images;
    private BigDecimal price;
    private String info;
    private Long likes;
    private ProductStatus status;
    private Long userId;
    private String nickname;
    private ProfileImageResponseDto profileImage;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public ProductResponseDto(Product product, List<ProductImageResponseDto> images, ProfileImageResponseDto prfileImage) {
        this.id = product.getId();
        this.name = product.getName();
        this.category = product.getCategory();
        this.images = images;
        this.price = product.getPrice();
        this.info = product.getInfo();
        this.likes = product.getLikes();
        this.status=product.getStatus();
        this.userId = product.getUser().getId();
        this.nickname=product.getUser().getNickname();
        this.profileImage=prfileImage;
        this.createdAt = product.getCreatedAt();
        this.modifiedAt = product.getModifiedAt();
    }

    public ProductResponseDto(Product product, List<ProductImageResponseDto> images) {
        this.id = product.getId();
        this.name = product.getName();
        this.category = product.getCategory();
        this.images = images;
        this.price = product.getPrice();
        this.info = product.getInfo();
        this.likes = product.getLikes();
        this.status=product.getStatus();
        this.userId = product.getUser().getId();
        this.nickname=product.getUser().getNickname();
       // this.profileImage=prfileImage;
        this.createdAt = product.getCreatedAt();
        this.modifiedAt = product.getModifiedAt();
    }
}
