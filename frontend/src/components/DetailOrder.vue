<script>
import {onMounted, ref} from 'vue';
import Cookies from "js-cookie";
const client = require('../client')
import {useRoute, useRouter} from "vue-router";
import AppHeader from './AppHeader.vue';
import AppFooter from './AppFooter.vue';

export default {
  components: {AppFooter, AppHeader},
  setup() {

    // shippingInfo를 객체로 설정
    const shippingInfo = ref({
      name: '',
      phone: '',
      address: ''
    });

    // product를 배열로 설정
    const products = ref([]);
    //const orderData = ref(null);
    const route = useRoute();
    const orderId = route.query.orderId;

    const deliveryStatuses = ref(['결제 완료', '상품 준비중', '배송 중', '배송 완료']);
    const currentStatusIndex = ref(0); // 현재 상태 (배송완료)

    const statusMapping = {
      '결제 완료': 0,
      '상품 준비중': 1,
      '배송 중': 2,
      '배송 완료':3
    };

    // 문자열을 인덱스로 변경
    const getStatusIndex = (status) => {
      return statusMapping[status] !== undefined ? statusMapping[status] : 0;
    };

    const router = useRouter();

    const goToProductDetail = (productId) => {
      alert(`상품 ID ${productId}의 상세 페이지로 이동합니다.`)
      router.push({ name: 'DetailProductPage', params: { productId } });
    }

    const goToSellerDetail = (sellerId) => {
      alert(`판매자 ID ${sellerId}의 상세 페이지로 이동합니다.`)
      // 아직 안되는 듯
      // router.push({ name: 'OtherProfilePage', params: { sellerId } });
    }

    const accessToken = Cookies.get('access_token');

    const fetchOrderDetail = async () => {
      try {
        const response = await client.get(`/order/buy/${orderId}`, {
          headers: {
            'Authorization': accessToken
          }
        });

        const orderDataResponse = response.data.result;
        console.log(response.data);

        if (orderDataResponse) {
          // orderDataResponse.value = orderDataResponse;
          shippingInfo.value = {
            name: orderDataResponse.userName,
            phone: orderDataResponse.phoneNumber,
            address: orderDataResponse.address
          };

          products.value = orderDataResponse.orderItemResponseDtoList.map(item => ({
            id: item.productId,
            name: item.productName,
            price: item.price,
            seller: item.sellerNickname,
            sellerId: item.sellerId,
            image: item.productImage.imageUrl,
            currentStatusIndex: getStatusIndex(item.orderStatus)
          }));
        }
        console.log(products.value)
      } catch (err) {
        console.error('Failed to fetch order details:', err);
      }
    };

    onMounted(() => {
      fetchOrderDetail();
    });

    return {
      shippingInfo,
      products,
      deliveryStatuses,
      currentStatusIndex,
      goToProductDetail,
      goToSellerDetail,
      fetchOrderDetail
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
          <h2>배송지 정보</h2>
          <div class="info-group">
            <label>이름:</label>
            <span>{{ shippingInfo.name }}</span>
          </div>
          <div class="info-group">
            <label>전화번호:</label>
            <span>{{ shippingInfo.phone }}</span>
          </div>
          <div class="info-group">
            <label>주소:</label>
            <span>{{ shippingInfo.address }}</span>
          </div>
        </div>
        <div class="order-summary">
          <h2>주문 상품 정보</h2>
          <div v-for="product in products" :key="product.id" class="product-item">
            <div class="product-content" @click="goToProductDetail(product.id)">
              <img :src="product.image" :alt="product.name" class="product-image">
              <div class="product-info">
                <div>
                  <div class="product-name">{{ product.name }}</div>
                  <a :href="'/seller/' + product.sellerId" class="seller-name"
                     @click.stop="goToSellerDetail(product.sellerId)">{{ product.seller }}</a>
                </div>
                <span>{{ product.price.toLocaleString() }}원</span>
              </div>
            </div>
            <div class="delivery-status">
              <h3>배송 상태</h3>
              <div class="status-bar">
                <div v-for="(status, index) in deliveryStatuses" :key="status"
                     :class="['status-step', { active: index <= product.currentStatusIndex }]">
                  <div class="status-icon">{{ index + 1 }}</div>
                  <div class="status-label">{{ status }}</div>
                </div>
                <pre>현재 상태 인덱스: {{ product.currentStatusIndex }}</pre>
              </div>
            </div>
          </div>
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
  --status-incomplete: #ccc;
  --status-complete: #4CAF50;
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
  padding: 60px 0;
}

.order-details {
  display: flex;
  flex-direction: column;
}

.shipping-info,
.order-summary {
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
}

.info-group {
  margin-bottom: 15px;
}

.info-group label {
  font-weight: bold;
  display: inline-block;
  width: 100px;
}

.product-item {
  display: flex;
  flex-direction: column;
  padding: 20px;
  border: 1px solid var(--card-border);
  border-radius: 5px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.product-item:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  transform: translateY(-5px);
}

.product-content {
  display: flex;
  align-items: center;
}

.product-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  margin-right: 20px;
}

.product-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-grow: 1;
}

.product-name {
  font-weight: bold;
  font-size: 18px;
  margin-bottom: 5px;
}

.seller-name {
  color: var(--main-color);
  text-decoration: none;
  font-weight: bold;
}

.seller-name:hover {
  text-decoration: underline;
}

.delivery-status {
  margin-bottom: 20px;
}

.status-bar {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
}

.status-step {
  flex: 1;
  text-align: center;
  position: relative;
}

.status-step::before {
  content: '';
  height: 3px;
  width: 100%;
  background-color: var(--status-incomplete);
  position: absolute;
  top: 15px;
  left: 50%;
  z-index: 1;
}

.status-step:last-child::before {
  display: none;
}

.status-step.active::before {
  background-color: var(--status-complete);
}

.status-icon {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background-color: var(--status-incomplete);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  position: relative;
  z-index: 2;
}

.status-step.active .status-icon {
  background-color: var(--status-complete);
}

.status-label {
  margin-top: 5px;
  font-size: 12px;
}

</style>