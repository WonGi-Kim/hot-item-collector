package com.sparta.hotitemcollector.domain.cart;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.hotitemcollector.domain.cart.dto.CartItemResponseDto;
import com.sparta.hotitemcollector.domain.security.UserDetailsImpl;
import com.sparta.hotitemcollector.global.common.CommonResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
	private final CartService cartService;

	@PostMapping("/{productId}")
	public ResponseEntity<CommonResponse<CartItemResponseDto>> createCartItem(@PathVariable("productId") Long productId,
		@AuthenticationPrincipal UserDetailsImpl userDetails) {
		CartItemResponseDto responseDto = cartService.createCartItem(userDetails.getUser(), productId);
		CommonResponse<CartItemResponseDto> response = new CommonResponse<>("장바구니에 상품 담기 성공", 201, responseDto);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@DeleteMapping("/{productId}")
	public ResponseEntity<CommonResponse> deleteCartItem(@PathVariable("productId") Long productId,
		@AuthenticationPrincipal UserDetailsImpl userDetails) {
		cartService.deleteCartItem(userDetails.getUser(), productId);
		CommonResponse response = new CommonResponse<>("장바구니 상품 제거 성공", 204, "");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping
	public ResponseEntity<CommonResponse<List<CartItemResponseDto>>> getCartItems(
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int size,
		@AuthenticationPrincipal UserDetailsImpl userDetails) {
		List<CartItemResponseDto> responseDtoList = cartService.getCartItems(page, size, userDetails.getUser());
		CommonResponse<List<CartItemResponseDto>> response = new CommonResponse<>("장바구니 조회 성공", 200, responseDtoList);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
