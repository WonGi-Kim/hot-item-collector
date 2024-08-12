package com.sparta.hotitemcollector.domain.order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.hotitemcollector.domain.cart.CartService;
import com.sparta.hotitemcollector.domain.order.dto.OrderItemBySellerResponseDto;
import com.sparta.hotitemcollector.domain.order.dto.OrderItemResponseDto;
import com.sparta.hotitemcollector.domain.order.dto.OrderResponseDto;
import com.sparta.hotitemcollector.domain.order.dto.OrderStatusRequestDto;
import com.sparta.hotitemcollector.domain.orderitem.OrderItem;
import com.sparta.hotitemcollector.domain.orderitem.OrderItemRepository;
import com.sparta.hotitemcollector.domain.payment.dto.OrderPrepareRequestDto;
import com.sparta.hotitemcollector.domain.product.entity.Product;
import com.sparta.hotitemcollector.domain.product.entity.ProductStatus;
import com.sparta.hotitemcollector.domain.product.service.ProductService;
import com.sparta.hotitemcollector.domain.user.User;
import com.sparta.hotitemcollector.global.exception.CustomException;
import com.sparta.hotitemcollector.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final ProductService productService;
	private final CartService cartService;

	@Transactional(readOnly = true)
	public Page<OrderResponseDto> getOrdersAllByBuyer(int page, int size, LocalDateTime startDate, LocalDateTime endDate, User user) {
		Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
		Pageable pageable = PageRequest.of(page, size, sort);
		Page<Orders> orderPage = orderRepository.findAllByUserIdAndCreatedAtBetween(user.getId(), startDate, endDate, pageable);

		return orderPage.map(OrderResponseDto::new);
	}

	//구매자가 산 오더아이템 목록
	@Transactional(readOnly = true)
	public Page<OrderItemResponseDto> getOrderItemsAllByBuyer(int page, int size, LocalDateTime startDate, LocalDateTime endDate, User user) {
		Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
		Pageable pageable = PageRequest.of(page, size, sort);

		Page<OrderItem> orderItemPage = orderItemRepository.findOrderItemPageByUserId(user.getId(), startDate, endDate, pageable);

		return orderItemPage.map(OrderItemResponseDto::new);
	}

	@Transactional(readOnly = true)
	public OrderResponseDto getOrderByBuyer(Long orderId, User user) {
		Orders order = findOrderById(orderId);

		if (!user.getId().equals(order.getUser().getId())) {
			throw new CustomException(ErrorCode.NOT_SAME_USER);
		}

		return new OrderResponseDto(order);
	}

	@Transactional(readOnly = true)
	public List<OrderItemBySellerResponseDto> getOrdersAllBySeller(LocalDateTime startDate, LocalDateTime endDate, String status, User user) {

		List<OrderItem> orderItemList = new ArrayList<>();
		List<Product> productList = productService.findByUserAndStatus(user, ProductStatus.SOLD_OUT);

		if (status == null || status.isEmpty()) {
			orderItemList = orderItemRepository.findAllByRequirement(null, startDate, endDate, productList);
		} else {
			orderItemList = orderItemRepository.findAllByRequirement(OrderStatus.fromString(status), startDate, endDate, productList);
		}

		return orderItemList
			.stream()
			.map(OrderItemBySellerResponseDto::new)
			.collect(Collectors.toList());
	}

	@Transactional
	public void updateStatus(Long orderItemId, OrderStatusRequestDto orderStatusRequestDto, User user) {
		OrderItem orderItem = findOrderItemById(orderItemId);

		if (!user.getId().equals(orderItem.getProduct().getUser().getId())) {
			throw new CustomException(ErrorCode.NOT_SAME_USER);
		}
		OrderStatus status = OrderStatus.fromString(orderStatusRequestDto.getStatus());
		orderItem.updateOrderItemStatus(status);

	}

	@Transactional
	public Orders createOrder(User user, OrderPrepareRequestDto requestDto) {

		Orders order = Orders.builder()
			.user(user)
			.address(requestDto.getBuyerAddr())
			.phoneNumber(requestDto.getBuyerTel())
			.userName(requestDto.getBuyerName())
			.build();

		orderRepository.save(order);
		return order;
	}

	@Transactional
	public OrderItem createOrderItem(Orders order, Product product) {
		OrderItem orderItem = OrderItem.builder()
			.order(order)
			.product(product)
			.status(OrderStatus.ORDERED)
			.build();

		orderItemRepository.save(orderItem);
		order.addOrderItem(orderItem);

		return orderItem;
	}

	public Orders findOrderById(Long orderId) {
		return orderRepository.findById(orderId).orElseThrow(
			() -> new CustomException(ErrorCode.NOT_FOUND_ORDER)
		);
	}

	public OrderItem findOrderItemById(Long orderItemId) {
		return orderItemRepository.findById(orderItemId).orElseThrow(
			() -> new CustomException(ErrorCode.NOT_FOUND_ORDERITEM)
		);
	}

	public List<OrderItem> findOrderItemsByOrderId(Long orderId) {
		return orderItemRepository.findByOrderId(orderId);
	}

	public Orders findById(Long orderId) {
		return orderRepository.findById(orderId)
				.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ORDER));

	}

	public List<OrderItem> findOrderItemListByOrderId(Long orderId) {
		return orderItemRepository.findOrderItemListByOrderId(orderId);
	}
}
