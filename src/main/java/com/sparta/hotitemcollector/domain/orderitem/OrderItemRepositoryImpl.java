package com.sparta.hotitemcollector.domain.orderitem;

import static com.sparta.hotitemcollector.domain.orderitem.QOrderItem.orderItem;

import java.time.LocalDateTime;
import java.util.List;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.hotitemcollector.domain.order.OrderStatus;
import com.sparta.hotitemcollector.domain.product.entity.Product;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderItemRepositoryImpl implements OrderItemRepositoryCustom {

	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public List<OrderItem> findAllByRequirement(OrderStatus status, LocalDateTime startDate, LocalDateTime endDate, List<Product> productList) {

		List<OrderItem> orderItemList = jpaQueryFactory.
			selectFrom(orderItem)
			.where(statusEq(status),
				orderItem.createdAt.between(startDate, endDate),
				orderItem.product.in(productList))
			.orderBy(orderItem.createdAt.desc())
			.fetch();

		return orderItemList;
	}

	public BooleanExpression statusEq(OrderStatus status) {
		if (status == null) {
			return null;
		}
		return orderItem.status.eq(status);
	}
}
