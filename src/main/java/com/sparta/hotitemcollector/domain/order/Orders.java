package com.sparta.hotitemcollector.domain.order;

import java.util.ArrayList;
import java.util.List;

import com.sparta.hotitemcollector.domain.orderitem.OrderItem;
import com.sparta.hotitemcollector.domain.payment.Payment;
import com.sparta.hotitemcollector.domain.user.User;
import com.sparta.hotitemcollector.global.Timestamped;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Entity
@NoArgsConstructor
public class Orders extends Timestamped {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column
	private Long id;

	@Column(name = "user_name", nullable = false)
	private String userName;

	@Column(name = "address", nullable = false)
	private String address;

	@Column(name = "phone_number", nullable = false)
	private String phoneNumber;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
	private Payment payment;

	@Builder
	public Orders(User user, String address, String phoneNumber, String userName) {
		this.user = user;
		this.address = address;
		this.phoneNumber = phoneNumber;
		this.userName = userName;
	}

	public void addOrderItem(OrderItem orderItem) {
		orderItems.add(orderItem);
	}

}
