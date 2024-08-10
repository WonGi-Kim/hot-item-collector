package com.sparta.hotitemcollector.domain.product.repository;

import com.sparta.hotitemcollector.domain.product.entity.Product;
import com.sparta.hotitemcollector.domain.product.entity.ProductCategory;
import com.sparta.hotitemcollector.domain.product.entity.ProductStatus;
import com.sparta.hotitemcollector.domain.user.User;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

	Page<Product> findTop10ByOrderByLikesDesc(Pageable pageable);

	@Query("SELECT p FROM Product p LEFT JOIN FETCH p.images WHERE p.id = :id")
	Product findByIdWithImages(@Param("id") Long id);

	List<Product> findByUserAndStatus(User user, ProductStatus status);

}
