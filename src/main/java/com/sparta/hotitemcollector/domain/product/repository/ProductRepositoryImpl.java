package com.sparta.hotitemcollector.domain.product.repository;

import static com.sparta.hotitemcollector.domain.product.entity.QProduct.product;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.hotitemcollector.domain.product.dto.HotProductResponseDto;
import com.sparta.hotitemcollector.domain.product.entity.Product;
import com.sparta.hotitemcollector.domain.product.entity.ProductCategory;
import com.sparta.hotitemcollector.domain.product.entity.ProductStatus;
import com.sparta.hotitemcollector.domain.user.User;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public Page<Product> findByRequirement(List<User> users, User user, String productName, ProductCategory category, ProductStatus status, Pageable pageable) {

		QueryResults<Product> productQueryResults = jpaQueryFactory
			.selectFrom(product)
			.where(userListEq(users),
				userEq(user),
				productNameEq(productName),
				categoryEq(category),
				statusEq(status))
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetchResults();

		List<Product> productList = productQueryResults.getResults();
		long total = productQueryResults.getTotal();

		return PageableExecutionUtils.getPage(productList, pageable, () -> total);
	}

	@Override
	public Page<HotProductResponseDto> findTop10HotProduct(Pageable pageable){

		QueryResults<HotProductResponseDto> dtoQueryResults = jpaQueryFactory
			.select(Projections.fields(HotProductResponseDto.class,
				product.id,
				product.name
			))
			.from(product)
			.orderBy(product.likes.desc(),
				product.createdAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetchResults();

		List<HotProductResponseDto> hotProductResponseDtos = dtoQueryResults.getResults();
		long total = dtoQueryResults.getTotal();

		return PageableExecutionUtils.getPage(hotProductResponseDtos, pageable, () -> total);
	}

	public BooleanExpression userListEq(List<User> users) {
		if (users == null || users.isEmpty()) {
			return null;
		}
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
