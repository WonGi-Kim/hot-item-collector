package com.sparta.hotitemcollector.domain.product.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sparta.hotitemcollector.domain.product.dto.HotProductResponseDto;
import com.sparta.hotitemcollector.domain.product.dto.ProductResponseDto;
import com.sparta.hotitemcollector.domain.product.dto.ProductSimpleResponseDto;
import com.sparta.hotitemcollector.domain.product.entity.Product;
import com.sparta.hotitemcollector.domain.product.entity.ProductCategory;
import com.sparta.hotitemcollector.domain.product.entity.ProductStatus;
import com.sparta.hotitemcollector.domain.user.User;

public interface ProductRepositoryCustom {

	Page<ProductSimpleResponseDto> findByRequirement(List<User> users, User user, String productName, ProductCategory category, ProductStatus status, Pageable pageable);

	ProductResponseDto findByIdWithImages(Long id);

	Page<HotProductResponseDto> findTop10HotProduct(Pageable pageable);
}
