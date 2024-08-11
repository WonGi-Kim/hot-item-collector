package com.sparta.hotitemcollector.domain.product.dto;

import com.sparta.hotitemcollector.domain.product.entity.Product;
import com.sparta.hotitemcollector.domain.product.entity.ProductStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductSimpleResponseDto {

    private Long id;
    private String name;
    private ProductImageResponseDto image;
    private ProductStatus status;
    private Long userId;
    private String userName;

    //Projections.fields -> 직접 필드에 맵핑하기 때문에 기본생성자 이외 필요 x ?
    public ProductSimpleResponseDto(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.image = product.getImages().isEmpty() ? null //
            : new ProductImageResponseDto(product.getImages().get(0)); //
        this.status=product.getStatus();
        this.userId = product.getUser().getId();
        this.userName = product.getUser().getNickname();
    }

    public void setProductImageResponseDto(ProductImageResponseDto imageResponseDto){
        this.image=imageResponseDto;
    }
}

