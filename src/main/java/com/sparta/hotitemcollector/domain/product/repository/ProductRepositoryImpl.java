package com.sparta.hotitemcollector.domain.product.repository;

import static com.sparta.hotitemcollector.domain.product.entity.QProduct.product;
import static com.sparta.hotitemcollector.domain.product.entity.QProductImage.productImage;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.hotitemcollector.domain.product.dto.HotProductResponseDto;
import com.sparta.hotitemcollector.domain.product.dto.ProductImageResponseDto;
import com.sparta.hotitemcollector.domain.product.dto.ProductResponseDto;
import com.sparta.hotitemcollector.domain.product.dto.ProductSimpleResponseDto;
import com.sparta.hotitemcollector.domain.product.entity.Product;
import com.sparta.hotitemcollector.domain.product.entity.ProductCategory;
import com.sparta.hotitemcollector.domain.product.entity.ProductImage;
import com.sparta.hotitemcollector.domain.product.entity.ProductStatus;
import com.sparta.hotitemcollector.domain.user.User;
import com.sparta.hotitemcollector.domain.user.dto.user.ProfileImageResponseDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public Page<ProductSimpleResponseDto> findByRequirement(List<User> users, User user, String productName, ProductCategory category, ProductStatus status, Pageable pageable) {

		List<ProductSimpleResponseDto> dtoQueryResults = jpaQueryFactory
			.select(Projections.fields(ProductSimpleResponseDto.class,
					product.id,
					product.name,
					product.status,
					product.user.id.as("userId"),
					product.user.nickname.as("userName")// alias가 dto이름, 접근하는 건 필드 이름
				))
				.from(product)
				.leftJoin(product.user)
				.where(userListEq(users),
					userEq(user),
					productNameContains(productName),
					categoryEq(category),
					statusEq(status))
				.orderBy(product.createdAt.desc())
				.offset(pageable.getOffset())
				.limit(pageable.getPageSize())
				.fetch();

		for (ProductSimpleResponseDto dto : dtoQueryResults) {
			ProductImageResponseDto imageDto = jpaQueryFactory
				.select(Projections.fields(ProductImageResponseDto.class,
					productImage.id,
					productImage.filename,
					productImage.imageUrl))
				.from(productImage)
				.where(productImage.product.id.eq(dto.getId()))
				.fetchFirst();

				dto.setProductImageResponseDto(imageDto);
		}

		Long total = jpaQueryFactory
			.selectFrom(product)
			.where(userListEq(users),
				userEq(user),
				productNameContains(productName),
				categoryEq(category),
				statusEq(status))
			.fetchCount();

		return PageableExecutionUtils.getPage(dtoQueryResults, pageable, () -> total);
	}

	@Override
	public ProductResponseDto findByIdWithImages(Long id){

		ProductResponseDto dtoQueryResult = jpaQueryFactory
			.select(Projections.fields(ProductResponseDto.class,
				product.id,
				product.name,
				product.category,
				product.price,
				product.info,
				product.likes,
				product.status,
				product.user.id.as("userId"),
				product.user.nickname,
				Projections.fields(ProfileImageResponseDto.class,
					product.user.profileImage.id,
					product.user.profileImage.filename,
					product.user.profileImage.imageUrl
					).as("profileImage"),
				product.createdAt,
				product.modifiedAt
				))
			.from(product)
			.leftJoin(product.user)
			.where(product.id.eq(id))
			.orderBy(product.createdAt.desc())
			.fetchFirst();

		List<ProductImageResponseDto> imageDtos = jpaQueryFactory
			.select(Projections.fields(ProductImageResponseDto.class,
				productImage.id,
				productImage.filename,
				productImage.imageUrl))
			.from(productImage)
			.where(productImage.product.id.eq(dtoQueryResult.getId()))
			.fetch();

		dtoQueryResult.setProductImageResponseDto(imageDtos);

		return dtoQueryResult;
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

	public BooleanExpression productNameContains(String productName) {
		if (productName == null || productName.isEmpty()) {
			return null;
		}
		return product.name.contains(productName);
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
