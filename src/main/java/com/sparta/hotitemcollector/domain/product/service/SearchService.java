package com.sparta.hotitemcollector.domain.product.service;

import com.sparta.hotitemcollector.domain.product.dto.FilterQueryDto;
import com.sparta.hotitemcollector.domain.product.repository.ProductRepository;
import com.sparta.hotitemcollector.domain.product.dto.ProductSimpleResponseDto;
import com.sparta.hotitemcollector.domain.product.entity.Product;
import com.sparta.hotitemcollector.domain.product.entity.ProductCategory;
import com.sparta.hotitemcollector.domain.user.User;
import com.sparta.hotitemcollector.domain.user.UserService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final UserService userService;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Page<ProductSimpleResponseDto> getSearchProduct(String nickname, String productName,
        ProductCategory category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Product> productPage = Page.empty(pageable);

        if (nickname != null && !nickname.isEmpty()) {
            List<User> userList = userService.findByNicknameContainingIgnoreCase(nickname);
            if (!userList.isEmpty()) {
               // FilterQueryDto filterQueryDto = new FilterQueryDto(userList);
               productPage = productRepository.findByRequirement(userList, null, null, null, null, pageable);
            }
        }

        if (productName != null && !productName.isEmpty()) {
            // FilterQueryDto filterQueryDto = new FilterQueryDto(productName);
            productPage = productRepository.findByRequirement(null, null, productName, null, null, pageable);
        }

        if (category != null) {
            FilterQueryDto filterQueryDto = new FilterQueryDto(category);
           productPage = productRepository.findByRequirement(null, null, null, category, null, pageable);
        }

        return productPage.map(ProductSimpleResponseDto::new);
    }
}
