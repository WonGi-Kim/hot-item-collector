package com.sparta.hotitemcollector.domain.cart;

import com.sparta.hotitemcollector.domain.product.entity.Product;
import com.sparta.hotitemcollector.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
	Optional<CartItem> findByProductIdAndUserId(Long productId, Long userId);

	Page<CartItem> findAllByUserId(Long userId, Pageable pageable);

	CartItem findByUserAndProduct(User user, Product product);
}
