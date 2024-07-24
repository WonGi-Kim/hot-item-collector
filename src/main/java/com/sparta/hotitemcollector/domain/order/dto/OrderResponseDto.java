package com.sparta.hotitemcollector.domain.order.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
public class OrderResponseDto {
	private Long id;
	private String userName;
	private String address;
	private String phoneNumber;
	private LocalDateTime createdAt;

	private List<OrderItemResponseDto> orderItemResponseDtoList;

	@Builder
	public OrderResponseDto(Long id, String userName, String address, String phoneNumber, LocalDateTime createdAt,
		List<OrderItemResponseDto> orderItemResponseDtoList) {
		this.id = id;
		this.userName = userName;
		this.address = address;
		this.phoneNumber = phoneNumber;
		this.createdAt = createdAt;

		this.orderItemResponseDtoList = orderItemResponseDtoList;
	}

}
