package com.sparta.hotitemcollector.domain.product.dto;

import com.sparta.hotitemcollector.domain.product.entity.ProductImage;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductImageResponseDto {

    private Long id;
    private String filename;
    private String imageUrl;

    public ProductImageResponseDto(ProductImage productImage) {
        this.id = productImage.getId();
        this.filename = productImage.getFilename();
        this.imageUrl = productImage.getImageUrl();
    }
}
