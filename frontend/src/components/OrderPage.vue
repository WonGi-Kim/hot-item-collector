<script>
const client = require('../client')
import {ref, computed, onMounted} from 'vue';
import {useRouter} from "vue-router";
import Cookies from "js-cookie";
import AppHeader from './AppHeader.vue';
import AppFooter from './AppFooter.vue';

export default {
  components: { AppHeader, AppFooter },
  setup() {
    const jQueryScript = document.createElement('script');
    jQueryScript.src = 'https://code.jquery.com/jquery-1.12.4.min.js';
    jQueryScript.type = 'text/javascript';
    document.head.appendChild(jQueryScript);

    const IamportScript = document.createElement('script');
    IamportScript.src = 'https://cdn.iamport.kr/js/iamport.payment-1.1.8.js';
    IamportScript.type = 'text/javascript';
    document.head.appendChild(IamportScript);

    IamportScript.onload = () => {
      console.log('Iamport script 로드');
    };

    jQueryScript.onload = () => {
      console.log('jQuery script 로드');
    };

    const shippingInfo = ref({
      name: '',
      phone: '',
      address: ''
    })
    const cartItems = ref([])

    const accessToken = Cookies.get('access_token');

    const loadCartItems = () => {
      // 아까 저장했던 정보들이 storedData로 들어옴.
      const storedData = sessionStorage.getItem('orderData')
      const orderDataArray = JSON.parse(storedData)

      orderDataArray.forEach(orderData => {
        cartItems.value.push({
          productId: orderData.productId,
          productName: orderData.productName,
          price: orderData.price,
          seller: orderData.seller,
          productImage: orderData.productImage
        })
      })
    }

    const totalAmount = computed(() => {
      return cartItems.value.reduce((total, item) => total + item.price, 0)
    })

    const useMyAddress = async () => {
      try {
        // API를 통해 주소 정보를 가져오는 것을 시뮬레이션
        const response = await client.get(`/users/profile/address`, {
          headers: {
            'Authorization': accessToken
          }
        });

        const data = response.data.result;

        // 매핑
        shippingInfo.value = {
          name: data.username || '데이터가 존재하지 않음',
          phone: data.phoneNumber || '데이터가 존재하지 않음',
          address: data.address || '데이터가 존재하지 않음'
        };

        alert('내 주소 정보를 불러왔습니다.')
      } catch (error) {
        alert('주소 정보를 불러오는데 실패했습니다.')
      }
    }

    const router = useRouter();

    const prepareAndPay = async () => {
      if (!shippingInfo.value.name || !shippingInfo.value.phone || !shippingInfo.value.address) {
        alert('배송지 정보를 모두 입력해주세요.');
        return;
      }

      try {

        const orderResponse = await client.post('/prepare/order', {
          productItemList: cartItems.value.map(item => item.productId),
          buyerName: shippingInfo.value.name,
          buyerTel: shippingInfo.value.phone,
          buyerAddr: shippingInfo.value.address
        }, {
          headers: {
            'Authorization': accessToken
          }
        });

        const orderId = orderResponse.data.result;

        const paymentResponse = await client.get(`/prepare/payment?orderId=${orderId}`, {
          headers: {
            'Authorization': accessToken
          }
        });

        const paymentData = paymentResponse.data.result;
        console.info(paymentData);

        const IMP = window.IMP; // 아임포트 제공 javascript library
        IMP.init("imp83178621");

        // 결제 요청
        IMP.request_pay({
          pg: paymentData.pg,
          pay_method: paymentData.payMethod,
          merchant_uid: paymentData.merchantUid,
          name: paymentData.name,
          amount: paymentData.amount,
          buyer_email: paymentData.buyerEmail,
          buyer_name: paymentData.buyerName,
          buyer_tel: paymentData.buyerTel,
          buyer_addr: paymentData.buyerAddr,
          buyer_postcode: paymentData.buyerPostcode
        }, async function (rsp) { // callback
          if (rsp.success) {
            try {
              const verifyResponse = await client.post('/payments/verify', {
                impUid: rsp.imp_uid,
                merchantUid: rsp.merchant_uid,
                amount: rsp.paid_amount
              }, {
                headers: {
                  'Authorization': accessToken
                }
              });

              alert('결제 검증 성공');
              router.push({ name: 'DetailOrder', query: { orderId: orderId } });
              console.log('결제 검증 성공:', verifyResponse.data);
            } catch (error) {
              alert('결제 검증 실패');
              console.log('결제 검증 실패:', error);
            }
          } else {
            alert("결제 실패");
            try {
              // 결제 실패 시 주문 삭제
              const deleteResponse = await client.delete(`/order/${orderId}`, {
                headers: {
                  'Authorization': accessToken
                }
              });

              alert("결제가 실패하여 주문이 취소되었습니다.");
              console.log("주문 삭제 성공:", deleteResponse.data);
            } catch (deleteError) {
              alert("주문 삭제 중 오류가 발생했습니다.");
              console.log("주문 삭제 실패:", deleteError);
            }
            console.log("결제 실패:", rsp);
          }
        });
      } catch (error) {
        alert('주문 준비 또는 결제 요청 중 오류가 발생했습니다.');
        console.log('주문 준비 또는 결제 요청 중 오류:', error);
      }
    };

    onMounted(() => {
      loadCartItems(); // 컴포넌트가 마운트될 때 API 호출
    });

    return {
      shippingInfo,
      cartItems,
      totalAmount,
      useMyAddress,
      prepareAndPay
    }
  }
}
</script>

<template>
  <div id="app">
    <AppHeader/>
    <main class="container">
      <div class="order-details">
        <div class="shipping-info">
          <h2>
            배송지 입력
            <button class="use-my-address" @click="useMyAddress">내 주소 배송</button>
          </h2>
          <div class="input-group">
            <label for="name">이름</label>
            <input type="text" id="name" v-model="shippingInfo.name">
          </div>
          <div class="input-group">
            <label for="phone">전화번호</label>
            <input type="tel" id="phone" v-model="shippingInfo.phone">
          </div>
          <div class="input-group">
            <label for="address">주소</label>
            <input type="text" id="address" v-model="shippingInfo.address">
          </div>
        </div>
        <div class="order-summary">
          <h2>주문 상품 정보</h2>
          <div v-for="product in cartItems" :key="product.id" class="product-item">
            <img :src="product.productImage.imageUrl" :alt="product.productName" class="product-image">
            <div class="product-info">
              <h3>{{ product.productName }}</h3>
              <p>가격: {{ product.price }}원</p>
              <p>판매자: {{ product.seller }}</p>
            </div>
          </div>
          <div class="total-amount">
            <!--             최종 결제금액: {{ totalAmount.toLocaleString() }}원-->
            최종 결제금액: {{ totalAmount }}원
          </div>
          <button class="checkout-button" @click="prepareAndPay">결제하기</button>
        </div>
      </div>
    </main>
    <AppFooter/>
  </div>
</template>

<style scoped>
:root {
  --main-color: #FF0000;
  --text-color: #333;
  --bg-color: #FFFFFF;
  --hover-color: #FF6666;
  --button-color: #FF4136;
  --footer-bg: #f8f8f8;
  --card-border: #e0e0e0;
  --input-border: #ccc;
  --section-border: #ddd;
}

body {
  font-family: Arial, sans-serif;
  margin: 0;
  padding: 0;
  background-color: var(--bg-color);
  color: var(--text-color);
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* Main Content Styles */
main {
  flex-grow: 1;
  padding: 60px 0; /* 상하 패딩 더 증가 */
}

.order-details {
  display: flex;
  flex-direction: column;
}

.shipping-info, .order-summary {
  width: 100%;
  margin-bottom: 30px;
  border: 1px solid var(--section-border);
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

h2 {
  font-size: 24px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.input-group {
  margin-bottom: 15px;
}

.input-group label {
  display: block;
  margin-bottom: 5px;
}

.input-group input {
  width: 100%;
  padding: 10px;
  border: 1px solid var(--input-border);
  border-radius: 5px;
}

.use-my-address {
  background-color: var(--button-color);
  color: var(--bg-color);
  border: none;
  padding: 10px 15px;
  cursor: pointer;
  border-radius: 5px;
  font-weight: bold;
  transition: background-color 0.3s ease;
}

.use-my-address:hover {
  background-color: var(--hover-color);
}

.product-item {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding: 10px;
  border: 1px solid var(--card-border);
  border-radius: 5px;
}

.product-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  margin-right: 20px;
}

.product-info h3 {
  margin: 0 0 10px 0;
}

.total-amount {
  font-size: 20px;
  font-weight: bold;
  margin-top: 20px;
  text-align: right;
}

.checkout-button {
  display: block;
  width: 100%;
  padding: 15px;
  background-color: var(--button-color);
  color: var(--bg-color);
  border: none;
  border-radius: 5px;
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
  transition: background-color 0.3s ease;
  margin-top: 20px;
}

.checkout-button:hover {
  background-color: var(--hover-color);
}

</style>