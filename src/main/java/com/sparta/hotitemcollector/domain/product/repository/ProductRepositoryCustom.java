package com.sparta.hotitemcollector.domain.product.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sparta.hotitemcollector.domain.product.entity.Product;
import com.sparta.hotitemcollector.domain.product.entity.ProductCategory;
import com.sparta.hotitemcollector.domain.product.entity.ProductStatus;
import com.sparta.hotitemcollector.domain.user.User;

public interface ProductRepositoryCustom {

	public Page<Product> findByRequirement(List<User> users, User user, String productName, ProductCategory category, ProductStatus status, Pageable pageable);

}
