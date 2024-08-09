package com.sparta.hotitemcollector.domain.product.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sparta.hotitemcollector.domain.product.dto.HotProductResponseDto;
import com.sparta.hotitemcollector.domain.product.entity.Product;
import com.sparta.hotitemcollector.domain.product.entity.ProductCategory;
import com.sparta.hotitemcollector.domain.product.entity.ProductStatus;
import com.sparta.hotitemcollector.domain.user.User;

public interface ProductRepositoryCustom {

	Page<Product> findByRequirement(List<User> users, User user, String productName, ProductCategory category, ProductStatus status, Pageable pageable);

	// @Query(value = "SELECT new com.sparta.hotitemcollector.domain.product.dto.HotProductResponseDto(p.id, p.name) "
	// 	+ "FROM Product p "
	// 	+ "ORDER BY p.likes DESC")
	Page<HotProductResponseDto> findTop10HotProduct(Pageable pageable);
}
