package com.sparta.hotitemcollector.domain.payment;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.request.PrepareData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse;
import com.sparta.hotitemcollector.domain.cart.CartService;
import com.sparta.hotitemcollector.domain.order.OrderService;
import com.sparta.hotitemcollector.domain.order.OrderStatus;
import com.sparta.hotitemcollector.domain.order.Orders;
import com.sparta.hotitemcollector.domain.orderitem.OrderItem;
import com.sparta.hotitemcollector.domain.payment.dto.OrderPrepareRequestDto;
import com.sparta.hotitemcollector.domain.payment.dto.PaymentRequestDto;
import com.sparta.hotitemcollector.domain.payment.dto.PaymentVerificationDto;
import com.sparta.hotitemcollector.domain.product.entity.Product;
import com.sparta.hotitemcollector.domain.product.service.ProductService;
import com.sparta.hotitemcollector.domain.user.User;
import com.sparta.hotitemcollector.global.exception.CustomException;
import com.sparta.hotitemcollector.global.exception.ErrorCode;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {
	private final PaymentRepository paymentRepository;
	private final ProductService productService;
	private IamportClient iamportClient;

	private final OrderService orderService;
	private final CartService cartService;

	@Value("${REST_API_KEY}")
	private String apiKey;

	@Value("${REST_SECRET_KEY}")
	private String secretKey;

	@Value("${IMP_UID}")
	private String uid;

	@PostConstruct
	public void init() {
		this.iamportClient = new IamportClient(apiKey, secretKey);
	}

	@Transactional
	public Long prepareOrder(User user, OrderPrepareRequestDto requestDto) throws IamportResponseException, IOException {

		Orders order = orderService.createOrder(user, requestDto);

		BigDecimal totalAmount = BigDecimal.ZERO; // 총 결제 금액을 저장할 변수

		for (Long itemId : requestDto.getProductItemList()) {
			// 기존 cartItem에서 제품을 찾는 것에서 제품 id를 바로 입력하도록 변경
			Product product = productService.findById(itemId);

			if (product.getUser().getId().equals(user.getId())) {
				throw new CustomException(ErrorCode.SAME_USER_PRODUCT);
			}

			OrderItem orderItem = orderService.createOrderItem(order, product);

			totalAmount = totalAmount.add(product.getPrice());
		}

		// 하나의 결제 레코드를 생성
		Payment payment = Payment.builder()
				.merchantUid("merchant_" + System.currentTimeMillis())
				.payMethod("card")
				.impUid("imp_" + System.currentTimeMillis()) // 임시 값이며 결제가 끝날 경우 아임포트에서 제공하는 값으로 변경 됨
				.amount(totalAmount) // 총 결제 금액
				.status(OrderStatus.PAID_READY)
				.paidAt(null)
				.order(order)
				.build();
		paymentRepository.save(payment);

		// 아임포트 사전 검증 추가
		iamportClient.postPrepare(createPrepareData(payment));

		return order.getId();
	}

	public PaymentRequestDto getPaymentRequestDataByOrderId(Long orderId) {
		Orders order = orderService.findOrderById(orderId);
		List<Payment> payments = paymentRepository.findByOrderId(orderId);
		BigDecimal totalAmount = payments.stream()
				.map(Payment::getAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		String productName = payments.stream()
				.map(payment -> payment.getOrder().getOrderItems().get(0).getProduct().getName())
				.collect(Collectors.joining(", "));

		return PaymentRequestDto.builder()
			.pg("html5_inicis")
			.payMethod("card")
			.merchantUid(payments.get(0).getMerchantUid())
			.amount(totalAmount)
			.name(productName)
			.buyerName(order.getUserName())
			.buyerAddr(order.getAddress())
			.buyerTel(order.getPhoneNumber())
			.build();
	}

	@Transactional
	public void verifyPayment(PaymentVerificationDto verificationDto) throws IamportResponseException, IOException {
		// Iamport 결제 검증
		IamportResponse<com.siot.IamportRestClient.response.Payment> iamportResponse = iamportClient.paymentByImpUid(verificationDto.getImpUid());
		com.siot.IamportRestClient.response.Payment iamportPayment = iamportResponse.getResponse();

		if (iamportPayment.getAmount().equals(verificationDto.getAmount())) {
			// 결제 완료 처리
			Payment payment = paymentRepository.findByMerchantUid(verificationDto.getMerchantUid())
					.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PAYMENT));

			payment.updatePayment(verificationDto.getImpUid(), OrderStatus.PAID, LocalDateTime.now());
			paymentRepository.save(payment);

			// 해당 Order의 모든 Payment 상태를 변경
			List<Payment> paymentList = paymentRepository.findByOrderId(payment.getOrder().getId());
			paymentList.forEach(p -> {
				p.updatePayment(p.getImpUid(), OrderStatus.PAID, LocalDateTime.now());
			});
			paymentRepository.saveAll(paymentList);

			// 해당 Order의 모든 OrderItem 상태를 변경
			List<OrderItem> orderItemList = orderService.findOrderItemsByOrderId(payment.getOrder().getId());
			orderItemList.forEach(orderItem -> {
				orderItem.updateOrderItemStatus(OrderStatus.PAID);
				productService.updateStatus(orderItem.getProduct().getId());
			});

		} else {
			throw new CustomException(ErrorCode.PAY_AMOUNT_MISMATCH);
		}
	}

	@Transactional
	public boolean cancelPayment(Long orderId, Long userId) throws IamportResponseException, IOException {
		// 유저가 주문의 실제 소유자인지 확인
		Orders order = orderService.findById(orderId);

		if (!order.getUser().getId().equals(userId)) {
			throw new SecurityException("사용자가 이 주문을 취소할 권한이 없습니다.");
		}

		List<Payment> payments = paymentRepository.findByOrderId(orderId);

		if (payments.isEmpty()) {
			throw new CustomException(ErrorCode.NOT_FOUND_PAYMENT);
		}

		for (Payment payment : payments) {
			if (OrderStatus.PAID.equals(payment.getStatus())) {
				CancelData cancelData = new CancelData(payment.getImpUid(), true); // amount를 생략하면 전액 취소

				IamportResponse<com.siot.IamportRestClient.response.Payment> cancelResponse = iamportClient.cancelPaymentByImpUid(cancelData);

				if (cancelResponse.getResponse() != null) {
					// 결제 취소가 성공한 경우
					Payment cancelledPayment = paymentRepository.findByImpUid(payment.getImpUid())
							.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PAYMENT));

					cancelledPayment.updatePayment(OrderStatus.ORDER_CANCEL);
					paymentRepository.save(cancelledPayment);

					// 해당 결제를 한 Order의 모든 OrderItem 상태를 같이 변경 해야 함
					List<OrderItem> orderItemList = orderService.findOrderItemsByOrderId(order.getId());
					orderItemList.forEach(orderItem -> orderItem.updateOrderItemStatus(OrderStatus.ORDER_CANCEL));
				} else {
					throw new RuntimeException("결제 취소 실패: " + cancelResponse.getMessage());
				}
			}
		}
		return true;
	}

	private PrepareData createPrepareData(Payment payment) {
		return new PrepareData(payment.getMerchantUid(),payment.getAmount());
	}

}