package com.sparta.hotitemcollector.domain.product.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.hotitemcollector.domain.follow.Follow;
import com.sparta.hotitemcollector.domain.follow.FollowService;
import com.sparta.hotitemcollector.domain.product.dto.HotProductResponseDto;
import com.sparta.hotitemcollector.domain.product.dto.ProductImageRequestDto;
import com.sparta.hotitemcollector.domain.product.dto.ProductImageResponseDto;
import com.sparta.hotitemcollector.domain.product.dto.ProductRequestDto;
import com.sparta.hotitemcollector.domain.product.dto.ProductResponseDto;
import com.sparta.hotitemcollector.domain.product.dto.ProductSimpleResponseDto;
import com.sparta.hotitemcollector.domain.product.entity.Product;
import com.sparta.hotitemcollector.domain.product.entity.ProductImage;
import com.sparta.hotitemcollector.domain.product.entity.ProductStatus;
import com.sparta.hotitemcollector.domain.product.repository.ProductImageRepository;
import com.sparta.hotitemcollector.domain.product.repository.ProductRepository;
import com.sparta.hotitemcollector.domain.s3.service.S3Service;
import com.sparta.hotitemcollector.domain.user.User;
import com.sparta.hotitemcollector.domain.user.UserService;
import com.sparta.hotitemcollector.domain.user.dto.user.ProfileImageResponseDto;
import com.sparta.hotitemcollector.global.exception.CustomException;
import com.sparta.hotitemcollector.global.exception.ErrorCode;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;
	private final FollowService followService;
	private final ProductImageRepository productImageRepository;
	private final S3Service s3Service;
	private final UserService userService;
	@PersistenceContext
	private final EntityManager entityManager;

	@Transactional
	public ProductResponseDto createProduct(ProductRequestDto requestDto, User user) {

		// Product 엔티티 생성
		Product product = Product.builder()
			.requestDto(requestDto)
			.user(user)
			.build();

		// Product 엔티티 저장
		productRepository.save(product);

		// ProductImage 엔티티 생성 및 저장
		List<ProductImage> productImages = requestDto.getImages().stream()
			.map(dto -> new ProductImage(dto.getFilename(), dto.getImageUrl(), product, user))
			.collect(Collectors.toList());

		// 각 이미지 엔티티를 저장
		for (ProductImage productImage : productImages) {
			productImageRepository.save(productImage);
		}

		// Product 엔티티에서 저장된 이미지를 가져옴
		List<ProductImageResponseDto> imageDtos = productImages.stream()
			.map(image -> new ProductImageResponseDto(image))
			.collect(Collectors.toList());

		ProfileImageResponseDto profileImageDto = new ProfileImageResponseDto(product.getUser()
			.getProfileImage());

		// ProductResponseDto 생성
		ProductResponseDto responseDto = new ProductResponseDto(product, imageDtos, profileImageDto);
		return responseDto;
	}

	@Transactional
	public ProductResponseDto updateProduct(Long productId, ProductRequestDto requestDto, User user) {
		// 제품 정보 조회
		Product product = findById(productId);

		// 사용자 권한 확인
		if (!product.getUser().getId().equals(user.getId())) {
			throw new CustomException(ErrorCode.NOT_SAME_USER);
		}

		// 제품 상태 확인
		if (product.getStatus().equals(ProductStatus.SOLD_OUT)) {
			throw new CustomException(ErrorCode.ALREADY_SOLD_OUT);
		}

		// 제품 업데이트
		product.updateProduct(requestDto);

		// 이미지 처리 로직
		List<ProductImageRequestDto> newImageDtos = requestDto.getImages();
		if (newImageDtos != null && !newImageDtos.isEmpty()) {
			// 기존 이미지와 새로운 이미지를 분리
			List<ProductImage> existingImages = product.getImages();
			List<String> existingImageUrls = existingImages.stream()
				.map(ProductImage::getImageUrl)
				.collect(Collectors.toList());

			// 새로운 이미지 URL만 추가
			for (ProductImageRequestDto newImageDto : newImageDtos) {
				// 새로운 이미지 URL이 기존 이미지 URL에 없는 경우만 추가
				if (!existingImageUrls.contains(newImageDto.getImageUrl())) {
					ProductImage newImage = new ProductImage(newImageDto.getFilename(), newImageDto.getImageUrl(), product, user);
					product.addImage(newImage);
					productImageRepository.save(newImage);
				}
			}
		}

		// 제품 정보 저장
		productRepository.save(product);

		// 제품의 모든 이미지를 다시 조회하여 반환
		List<ProductImageResponseDto> updatedImageDtos = product.getImages().stream()
			.map(ProductImageResponseDto::new)
			.collect(Collectors.toList());

		ProfileImageResponseDto profileImageResponseDto = new ProfileImageResponseDto(product.getUser().getProfileImage());

		// ProductResponseDto 생성 및 반환
		return new ProductResponseDto(product, updatedImageDtos, profileImageResponseDto);
	}

	@Transactional
	public void deleteProduct(Long productId, User user) {
		Product product = findById(productId);

		if (!product.getUser().getId().equals(user.getId())) {
			throw new CustomException(ErrorCode.NOT_SAME_USER);
		}

		// 제품과 연결된 모든 이미지를 S3에서 삭제
		List<ProductImage> images = product.getImages();
		for (ProductImage image : images) {
			s3Service.deleteImage(image.getFilename());
		}

		productRepository.delete(product);
	}

	@Transactional
	public void deleteImage(Long productId, Long imageId, User user) {
		Product product = findById(productId);
		ProductImage productImage = findImageById(imageId);
		if (!productImage.getUser().getId().equals(user.getId())) {
			throw new CustomException(ErrorCode.NOT_SAME_USER);
		}
		product.removeImage(productImage);
		s3Service.deleteImage(productImage.getFilename());
		productImageRepository.delete(productImage);
	}

	@Transactional(readOnly = true)
	public ProductResponseDto getProduct(Long productId) {
		// Product와 관련된 이미지 정보를 가져옴
		Product product = productRepository.findByIdWithImages(productId);

		// Product와 연관된 이미지 정보를 DTO로 변환
		List<ProductImageResponseDto> imageDtos = product.getImages().stream()
			.map(image -> new ProductImageResponseDto(image))
			.collect(Collectors.toList());

		// ProfileImageResponseDto를 생성, 프로필 이미지가 없으면 null 설정
		ProfileImageResponseDto profileImageResponseDto = product.getUser().getProfileImage() != null
			? new ProfileImageResponseDto(product.getUser().getProfileImage())
			: null;

		// ProductResponseDto 생성
		ProductResponseDto responseDto = new ProductResponseDto(product, imageDtos, profileImageResponseDto);
		return responseDto;
	}

	@Transactional(readOnly = true)
	public Page<ProductSimpleResponseDto> getFollowProduct(User user, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
		List<Follow> followList = followService.getAllFollowers(user);

		List<User> followingUsers = followList.stream()
			.map(Follow::getFollowing)
			.collect(Collectors.toList());

		Page<Product> productPage = productRepository.findByRequirement(followingUsers, null, null, null, null, pageable);
		return productPage.map(ProductSimpleResponseDto::new);
	}

	@Transactional(readOnly = true)
	public List<HotProductResponseDto> getHotProduct(int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
		Page<Product> productPage = productRepository.findTop10ByOrderByLikesDesc(pageable);

		return productPage.getContent()
			.stream()
			.map(HotProductResponseDto::new)
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public Page<ProductSimpleResponseDto> getSaleProduct(User user, ProductStatus status, int page,
		int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
		Page<Product> productPage = Page.empty(pageable);

		if (status != null) {
			productPage = productRepository.findByRequirement(null, user, null, null, status, pageable);
		}
		if (status == null) {
			productPage = productRepository.findByRequirement(null, user, null, null, null, pageable);
		}

		return productPage.map(ProductSimpleResponseDto::new);
	}

	@Transactional(readOnly = true)
	public Page<ProductSimpleResponseDto> getSaleYourProduct(Long userId, ProductStatus status, int page, int size) {
		User user = userService.findByUserId(userId);

		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
		Page<Product> productPage = Page.empty(pageable);

		if (status != null) {
			productPage = productRepository.findByRequirement(null, user, null, null, status, pageable);
		}
		if (status == null) {
			productPage = productRepository.findByRequirement(null, user, null, null, null, pageable);
		}

		return productPage.map(ProductSimpleResponseDto::new);
	}

	@Transactional(readOnly = true)
	public Page<ProductSimpleResponseDto> getNewProduct(int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

		Page<Product> productPage = productRepository.findAll(pageable);

		return productPage.map(ProductSimpleResponseDto::new);
	}

	public Product findById(Long productId) {
		return productRepository.findById(productId).orElseThrow(
			() -> new CustomException(ErrorCode.NOT_FOUND_PRODUCT)
		);
	}

	public ProductImage findImageById(Long imageId) {
		return productImageRepository.findById(imageId).orElseThrow(
			() -> new CustomException(ErrorCode.NOT_FOUND_IMAGE)
		);
	}

	public void increaseLikes(Long productId) {
		Product product = findById(productId);
		product.increaseLikes();
		productRepository.save(product);
	}

	public void decreaseLikes(Long productId) {
		Product product = findById(productId);
		product.decreaseLikes();
		productRepository.save(product);
	}

	public void updateStatus(Long productId) {
		Product product = findById(productId);
		product.updateStatus();
		productRepository.save(product);
	}

	public List<Product> findByUserAndStatus(User user, ProductStatus status) {
		return productRepository.findByUserAndStatus(user, status);
	}
}
