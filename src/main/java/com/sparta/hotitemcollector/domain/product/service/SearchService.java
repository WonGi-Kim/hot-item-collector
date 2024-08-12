package com.sparta.hotitemcollector.domain.product.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.hotitemcollector.domain.product.dto.ProductSimpleResponseDto;
import com.sparta.hotitemcollector.domain.product.entity.Product;
import com.sparta.hotitemcollector.domain.product.entity.ProductCategory;
import com.sparta.hotitemcollector.domain.product.repository.ProductRepository;
import com.sparta.hotitemcollector.domain.user.User;
import com.sparta.hotitemcollector.domain.user.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
public class SearchService {

	private final UserService userService;
	private final ProductRepository productRepository;

	@Transactional(readOnly = true)
	public Page<ProductSimpleResponseDto> getSearchProduct(String nickname, String productName,
		ProductCategory category, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
		Page<ProductSimpleResponseDto> productPage = Page.empty(pageable);

		if (nickname != null && !nickname.isEmpty()) {
			List<User> userList = userService.findByNicknameContainingIgnoreCase(nickname);
			if (!userList.isEmpty()) {
				productPage = productRepository.findByRequirement(userList, null, null, null, null, pageable);
			}
		}

		if (productName != null && !productName.isEmpty()) {
			productPage = productRepository.findByRequirement(null, null, productName, null, null, pageable);
		}

		if (category != null) {
			productPage = productRepository.findByRequirement(null, null, null, category, null, pageable);
		}

		return productPage;
	}
}
