package com.sparta.hotitemcollector.domain.product.dto;

import com.sparta.hotitemcollector.domain.product.entity.Product;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor //QueryDSL에서 dto 사용 시 기본 생성자를 필요로 한다
public class HotProductResponseDto {

    private Long id;
    private String name;

    public HotProductResponseDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
