package com.sparta.hotitemcollector.domain.product.repository;

import static com.sparta.hotitemcollector.domain.product.entity.QProduct.product;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.support.PageableExecutionUtils;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.hotitemcollector.domain.product.dto.FilterQueryDto;
import com.sparta.hotitemcollector.domain.product.entity.Product;
import com.sparta.hotitemcollector.domain.product.entity.ProductCategory;
import com.sparta.hotitemcollector.domain.product.entity.ProductStatus;
import com.sparta.hotitemcollector.domain.user.User;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public Page<Product> findByRequirement(List<User> users, User user, String productName, ProductCategory category, ProductStatus status,  Pageable pageable) {
	// public Page<Product> findByRequirement(FilterQueryDto filterQueryDto, Pageable pageable) {

		List<Product> productList = jpaQueryFactory
			.selectFrom(product)
			.where(userListEq(users),
				userEq(user),
				productNameEq(productName),
				categoryEq(category),
				statusEq(status))
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long contentSize = jpaQueryFactory
			.select(product.count())
			.from(product)
			.where(userListEq(users),
				userEq(user),
				productNameEq(productName),
				categoryEq(category),
				statusEq(status))
			.fetchOne();

		return PageableExecutionUtils.getPage(productList, pageable, () -> contentSize);
	}

	@Override
	public Page<Product> findTop10ByOrderByLikesDesc(Pageable pageable) {
		return null;
	}

	// @Query("SELECT p FROM Product p LEFT JOIN FETCH p.images WHERE p.id = :id")
	@Override
	public Product findByIdWithImages(@Param("id") Long id) {
		return null;
	}

	@Override
	public List<Product> findByUserAndStatus(User user, ProductStatus status) {
		return null;
	}

	public BooleanExpression userListEq(List<User> users) {
		if (users == null || users.isEmpty()) {
			return null;
		}
		//QProduct product = QProduct.product;   ?
		return product.user.in(users);
	}

	public BooleanExpression userEq(User user) {
		if (user == null) {
			return null;
		}
		return product.user.eq(user);
	}

	public BooleanExpression productNameEq(String productName) {
		if (productName == null || productName.isEmpty()) {
			return null;
		}
		return product.name.eq(productName);
	}

	public BooleanExpression categoryEq(ProductCategory category) {
		if (category == null) {
			return null;
		}
		return product.category.eq(category);
	}

	public BooleanExpression statusEq(ProductStatus status) {
		if (status == null) {
			return null;
		}
		return product.status.eq(status);
	}

}
