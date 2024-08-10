package com.sparta.hotitemcollector.domain.orderitem;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.sparta.hotitemcollector.TestConfig;
import com.sparta.hotitemcollector.domain.order.OrderStatus;
import com.sparta.hotitemcollector.domain.product.entity.Product;
import com.sparta.hotitemcollector.domain.product.repository.ProductRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestConfig.class)
class QueryTest {

	@Autowired
	OrderItemRepository orderItemRepository;
	@Autowired
	ProductRepository productRepository;

	@Test
	@DisplayName("0. orderItemRepository에서 findByOrderId를 테스트한다")
	void test0() {
		//given
		Long id = 2L;

		// when
		long before = System.currentTimeMillis();
		List<OrderItem> orderItems = orderItemRepository.findByOrderId(id);
		long after = System.currentTimeMillis();

		// then
		assertNotNull(orderItems);
		System.out.println("총 걸린 시간 : " + (after - before) + "ms");
		System.out.println("====================================");
	}

	@Test
	@DisplayName("1. findAllByStatusAndCreatedAtBetweenAndProductInOrderByCreatedAtDesc를 테스트한다")
	void test1() {
		//given
		LocalDateTime startDate = LocalDateTime.of(2023, 1, 1, 0, 0);
		LocalDateTime endDate = LocalDateTime.of(2024, 12, 31, 0, 0);
		List<Long> ids = new ArrayList<>();
		for (long i = 1; i <= 10; i++) {
			ids.add(i);
		}
		List<Product> productList = new ArrayList<>();
		productList = productRepository.findAllById(ids);

		// when
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>> 메소드 시작 >>>>>>>>>>>>>>>>>>>>>>>>>");
		long before = System.currentTimeMillis();
		// List<OrderItem> orderItems = orderItemRepository.findAllByStatusAndCreatedAtBetweenAndProductInOrderByCreatedAtDesc(
		// 	OrderStatus.SHIPMENT_START,
		// 	startDate,
		// 	endDate,
		// 	productList
		// );
		List<OrderItem> orderItems = orderItemRepository.findAllByRequirement(
			null,
			startDate,
			endDate,
			productList
		);

		long after = System.currentTimeMillis();
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>> 메소드 종료 >>>>>>>>>>>>>>>>>>>>>>>>>");

		// then
		System.out.println("총 걸린 시간 : " + (after - before) + "ms");
		System.out.println("====================================");
	}

	@Test
	@DisplayName("2. findOrderItemPageByUserId를 테스트한다")
	void test2() {
		//given
		LocalDateTime startDate = LocalDateTime.of(2023, 1, 1, 0, 0);
		LocalDateTime endDate = LocalDateTime.of(2024, 12, 31, 0, 0);
		Pageable pageable = PageRequest.of(1, 16, Sort.by(Sort.Direction.DESC, "createdAt"));

		// when
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>> 메소드 시작 >>>>>>>>>>>>>>>>>>>>>>>>>");
		long before = System.currentTimeMillis();
		long userId = 1L;
		Page<OrderItem> orderItemPage1 = orderItemRepository.findOrderItemPageByUserId(userId, startDate, endDate, pageable);
		long after = System.currentTimeMillis();
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>> 메소드 종료 >>>>>>>>>>>>>>>>>>>>>>>>>");

		// then
		System.out.println("총 걸린 시간 : " + (after - before) + "ms");
		System.out.println("====================================");
	}

	@Test
	@DisplayName("3. findByOrderId를 테스트한다")
	void test3() {
		//given
		Long id = 2L;

		// when
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>> 메소드 시작 >>>>>>>>>>>>>>>>>>>>>>>>>");
		long before = System.currentTimeMillis();
		List<OrderItem> orderItemList = orderItemRepository.findByOrderId(id);
		long after = System.currentTimeMillis();
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>> 메소드 종료 >>>>>>>>>>>>>>>>>>>>>>>>>");

		// then
		System.out.println("총 걸린 시간 : " + (after - before) + "ms");
		System.out.println("====================================");
	}

}
