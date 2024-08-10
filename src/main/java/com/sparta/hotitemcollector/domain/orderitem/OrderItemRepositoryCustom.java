package com.sparta.hotitemcollector.domain.orderitem;

import java.time.LocalDateTime;
import java.util.List;

import com.sparta.hotitemcollector.domain.order.OrderStatus;
import com.sparta.hotitemcollector.domain.product.entity.Product;

public interface OrderItemRepositoryCustom {

	List<OrderItem> findAllByRequirement(OrderStatus status, LocalDateTime startDate, LocalDateTime endDate, List<Product> productList);

}
