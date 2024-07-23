package com.sparta.hotitemcollector.domain.product;

import com.sparta.hotitemcollector.domain.user.User;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByUserIn(List<User> users, Pageable pageable);

    Page<Product> findTop10ByOrderByLikesDesc(Pageable pageable);

    Page<Product> findByUserAndStatus(User user, ProductStatus status, Pageable pageable);
}
