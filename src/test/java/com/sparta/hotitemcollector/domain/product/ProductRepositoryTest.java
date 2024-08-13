package com.sparta.hotitemcollector.domain.product;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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
import com.sparta.hotitemcollector.domain.product.dto.HotProductResponseDto;
import com.sparta.hotitemcollector.domain.product.dto.ProductResponseDto;
import com.sparta.hotitemcollector.domain.product.entity.Product;
import com.sparta.hotitemcollector.domain.product.entity.ProductCategory;
import com.sparta.hotitemcollector.domain.product.entity.ProductStatus;
import com.sparta.hotitemcollector.domain.product.repository.ProductRepository;
import com.sparta.hotitemcollector.domain.user.User;
import com.sparta.hotitemcollector.domain.user.UserRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestConfig.class)
public class ProductRepositoryTest {

	@Autowired
	ProductRepository productRepository;
	@Autowired
	UserRepository userRepository;


	@Test
	@DisplayName("1. findByRequirement를 테스트한다")
	void test1() {
		//given
		Pageable pageable = PageRequest.of(1, 16, Sort.by(Sort.Direction.DESC, "createdAt"));
		List<User> userList = new ArrayList<>();
		userList.add(userRepository.findById(1L).orElse(null));
		userList.add(userRepository.findById(2L).orElse(null));
		User user = userRepository.findById(1L).orElse(null);
		String productName = "똥";
		ProductCategory category = ProductCategory.PET;
		ProductStatus status = ProductStatus.SOLD_OUT;

		// when
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>> 메소드 시작 >>>>>>>>>>>>>>>>>>>>>>>>>");
		long before = System.currentTimeMillis();
		// Page<Product> productPage = productRepository.findByRequirement(null, user, productName, null, null,  pageable);
		long after = System.currentTimeMillis();
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>> 메소드 종료 >>>>>>>>>>>>>>>>>>>>>>>>>");

		// then
		System.out.println("총 걸린 시간 : " + (after - before) + "ms");
		System.out.println("====================================");
	}

	/*@Test
	@DisplayName("2. findTop10ByOrderByLikesDesc를 테스트한다")
	void test2() {
		//given
		Pageable pageable = PageRequest.of(1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

		// when
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>> 메소드 시작 >>>>>>>>>>>>>>>>>>>>>>>>>");
		long before = System.currentTimeMillis();
		Page<Product> productPage = productRepository.findTop10ByOrderByLikesDesc(pageable);
		long after = System.currentTimeMillis();
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>> 메소드 종료 >>>>>>>>>>>>>>>>>>>>>>>>>");

		// then
		System.out.println("총 걸린 시간 : " + (after - before) + "ms");
		System.out.println("====================================");
	}*/

	@Test
	@DisplayName("22. findTop10ByOrderByLikesDesc를 테스트한다")
	void test22() {
		//given
		Pageable pageable = PageRequest.of(1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

		// when
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>> 메소드 시작 >>>>>>>>>>>>>>>>>>>>>>>>>");
		long before = System.currentTimeMillis();
		Page<HotProductResponseDto> productPage = productRepository.findTop10HotProduct(pageable);
		long after = System.currentTimeMillis();
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>> 메소드 종료 >>>>>>>>>>>>>>>>>>>>>>>>>");

		// then
		System.out.println("총 걸린 시간 : " + (after - before) + "ms");
		System.out.println("====================================");
	}


	/*@Test
	@DisplayName("3. findByIdWithImages를 테스트한다")
	void test3() {
		//given
		Long id = 1L;

		// when
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>> 메소드 시작 >>>>>>>>>>>>>>>>>>>>>>>>>");
		long before = System.currentTimeMillis();
		Product product = productRepository.findByIdWithImages(id);
		long after = System.currentTimeMillis();
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>> 메소드 종료 >>>>>>>>>>>>>>>>>>>>>>>>>");

		// then
		System.out.println("총 걸린 시간 : " + (after - before) + "ms");
		System.out.println("====================================");
	}*/

	@Test
	@DisplayName("3. findAll을 테스트한다")
	void test4() {
		//given

		// when
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>> 메소드 시작 >>>>>>>>>>>>>>>>>>>>>>>>>");
		long before = System.currentTimeMillis();
		List<Product> productList = productRepository.findAll();
		long after = System.currentTimeMillis();
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>> 메소드 종료 >>>>>>>>>>>>>>>>>>>>>>>>>");

		// then
		System.out.println("총 걸린 시간 : " + (after - before) + "ms");
		assertNotNull(productList);
		System.out.println("====================================");
	}

}
