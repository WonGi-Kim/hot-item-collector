package com.sparta.hotitemcollector.domain.product.entity;

import com.sparta.hotitemcollector.domain.product.dto.ProductImageRequestDto;
import com.sparta.hotitemcollector.domain.user.User;
import com.sparta.hotitemcollector.global.Timestamped;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Entity
@Table(name = "product_image")
@RequiredArgsConstructor
public class ProductImage extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "file_name")
    private String filename;
    @Column(name = "image_url")
    private String imageUrl;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public ProductImage(String filename, String imageUrl, Product product, User user) {
        this.filename = filename;
        this.imageUrl = imageUrl;
        this.product = product;
        this.user = user;
    }

    public void updateProductImage(ProductImageRequestDto imageDto) {
        this.filename = imageDto.getFilename();
        this.imageUrl = imageDto.getImageUrl();
    }

    public void setProduct(Product product) {
        this.product = product;
        if (!product.getImages().contains(this)) {
            product.getImages().add(this);  // 부모 엔티티의 리스트에 자식 엔티티 추가
        }
    }
}
