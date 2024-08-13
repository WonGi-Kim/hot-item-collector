<script>
import {onMounted, ref} from 'vue'
import {DateTime} from 'luxon'
import Cookies from "js-cookie";
import AppHeader from './AppHeader.vue';
import AppFooter from './AppFooter.vue';
import router from "@/router";

const client = require('../client')

export default {
  components: {AppHeader, AppFooter},
  setup() {
    const startDate = ref('')
    const endDate = ref('')
    const orders = ref([])
    const accessToken = Cookies.get('access_token')
    const groupedOrders = ref({});

    const formatDate = (dateString) => {
      const date = DateTime.fromISO(dateString)
      return date.toFormat('yyyy년 MM월 dd일 (ccc)', {locale: 'ko'})
    }

    const formatPrice = (price) => {
      return new Intl.NumberFormat('ko-KR').format(price)
    }

    const searchPurchases = () => {

      client.get('/orderitems/buy', {
        params: {
          startDate: startDate.value,
          endDate: endDate.value,
        },
        headers: {
          'Authorization': accessToken
        }
      }).then(response => {
        orders.value = response.data.result.content;
        groupedOrders.value = groupOrdersByDate(orders.value);
      }).catch(error => {
        console.error(error)
      })
    }
    const goToOrderDetail = (itemId) => {
      const orderItem = orders.value.find(order => order.productId === itemId);
      if (orderItem) {
        router.push({name: 'DetailOrder', query: {orderId: orderItem.orderId}});
      } else {
        console.error('주문 아이템을 찾을 수 없습니다.');
      }
    }

    const groupOrdersByDate = (orders) => {
      return orders.reduce((grouped, order) => {
        const date = order.createdAt.split('T')[0];
        if (!grouped[date]) {
          grouped[date] = [];
        }
        grouped[date].push(order);
        return grouped;
      }, {});
    }


    onMounted(() => {
          const today = DateTime.now();
          endDate.value = today.toFormat('yyyy-MM-dd');
          startDate.value = today.minus({months: 3}).toFormat('yyyy-MM-dd');
          searchPurchases();
        }
    );


    return {
      startDate,
      endDate,
      orders,
      formatDate,
      formatPrice,
      searchPurchases,
      goToOrderDetail,
      groupedOrders
    };
  }
};

</script>

<template>
  <div id="app">
    <AppHeader/>
    <main class="container purchased-products">
      <h1>구매한 상품 목록</h1>
      <div class="date-range-selector">
        <input type="date" v-model="startDate">
        <span>~</span>
        <input type="date" v-model="endDate">
        <button @click="searchPurchases">검색</button>
      </div>
      <div v-if="Object.keys(groupedOrders).length > 0">
        <div v-for="(orders, date) in groupedOrders" :key="date">
          <div class="purchase-date">{{ formatDate(date) }}</div>
          <div class="products-container">
            <div v-for="item in orders" :key="item.id" class="product-card" @click="goToOrderDetail(item.productId)">
              <img :src="item.productImage.imageUrl" :alt="item.productName" class="product-image">
              <div class="product-info">
                <div class="product-details">
                  <div class="product-name">{{ item.productName }}</div>
                  <div>
                    판매자: <a :href="'/seller/' + item.sellerId" class="seller-name" @click.stop>{{
                      item.sellerNickname
                    }}</a>
                  </div>
                </div>
                <div class="product-price">{{ formatPrice(item.price) }}원</div>
                <div class="product-status">{{ item.orderStatus }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="no-items-message">해당 기간에 등록된 상품이 없습니다.</div>
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
  --category-bg: #f1f1f1;
}

body {
  font-family: Arial, sans-serif;
  margin: 0;
  padding: 0;
  background-color: var(--bg-color);
  color: var(--text-color);
}

#app{
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.container {
  width: 80%;
  margin: 0 auto;
  padding: 0 20px;
  flex: 1;
}

/* Purchased Products Styles */
.purchased-products {
  padding: 20px 0;
}

.date-range-selector {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.date-range-selector input[type="date"] {
  padding: 10px;
  border: 1px solid var(--card-border);
  border-radius: 8px; /* 검색창의 모서리도 둥글게 */
  font-size: 16px; /* 폰트 사이즈 통일 */
  box-sizing: border-box; /* 패딩과 보더를 포함한 박스 사이즈 계산 */
}

.date-range-selector button {
  padding: 10px 20px; /* 버튼의 패딩을 검색창과 일치시킴 */
  width: 130px;
  background-color: var(--button-color);
  color: var(--bg-color);
  border: none;
  border-radius: 8px; /* 버튼 모서리를 둥글게 */
  cursor: pointer;
  font-size: 16px; /* 폰트 사이즈 통일 */
  font-weight: bold; /* 폰트 두께 조정 */
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); /* 버튼에 그림자 추가 */
}

.date-range-selector button:hover {
  background-color: var(--hover-color); /* 호버 시 배경색 변경 */
  transform: scale(1.05); /* 호버 시 버튼 크기 약간 증가 */
}

.date-range-selector button:active {
  transform: scale(0.95); /* 클릭 시 버튼 크기 약간 감소 */
}

.purchase-date {
  font-size: 18px;
  font-weight: bold;
  margin-top: 20px;
  margin-bottom: 10px;
}

.products-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 20px;
}

.product-card {
  border: 1px solid var(--card-border);
  border-radius: 5px;
  padding: 10px;
  transition: box-shadow 0.3s ease;
  display: flex;
  align-items: center;
}

.product-card:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.product-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 5px;
  margin-right: 20px;
  cursor: pointer;
}

.product-info {
  flex-grow: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-details {
  display: flex;
  flex-direction: column;
}

.product-name {
  font-weight: bold;
  font-size: 16px;
  cursor: pointer;
  color: var(--text-color);
  text-decoration: none;
}

.product-name:hover {
  text-decoration: underline;
}

.seller-name {
  color: var(--text-color);
  text-decoration: none;
  font-size: 14px;
}

.seller-name:hover {
  text-decoration: underline;
}

.product-price {
  font-weight: bold;
  font-size: 16px;
  margin-left: auto;
  margin-right: 20px;
}

.product-status {
  font-size: 14px;
  color: #666;
  width: 100px;
  text-align: right;
}

.no-items-message {
  text-align: center;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
  margin: 20px 0;
  color: #666;
  font-style: italic;
}
</style>