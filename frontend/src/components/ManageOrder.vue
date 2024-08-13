<script>
import {onMounted, ref} from 'vue';
import Cookies from "js-cookie";
const client = require('../client')
import AppHeader from './AppHeader.vue';
import AppFooter from './AppFooter.vue';

export default {
  components: { AppHeader, AppFooter },
  setup() {
    const deliveryStatuses = ref(['결제 대기중', '결제 완료', '상품 준비중', '배송 시작', '배송 중', '배송 완료', '주문 취소'])
    const statusFilter = ref('')
    const orders = ref([]) // order 상태
    const today = new Date().toISOString().split('T')[0]
    const startDate = ref(new Date(new Date().setMonth(new Date().getMonth() - 3)).toISOString().split('T')[0])
    const endDate = ref(today)
    const accessToken = Cookies.get('access_token')

    const formatDate = (date) => {
      return date.split('T')[0]
    }
    const searchOrders = () => {
      client.get('/orders/sell', {
        params: {
          startDate: startDate.value,
          endDate: endDate.value,
          status: statusFilter.value,
        },
        headers: {
          'Authorization': accessToken
        }
      }).then(response => {
        orders.value = response.data.result
      }).catch(error => {
        console.error(error)
      })
    }

    const loadOrders = () => {
      client.get('/orders/sell', {
        headers: {
          'Authorization': accessToken
        }
      }).then(response => {
        orders.value = response.data.result
      }).catch(error => {
        console.error(error)
      })
    }

    const updateStatus = (order) => {

      client.patch(`/orders/sell/${order.orderItemId}`, {
        status: order.orderStatus
      }, {
        headers: {
          'Authorization': accessToken,
          'Content-Type': 'application/json'
        }
      }).then(response => {
        alert(`주문상품 #${order.orderItemId}의 배송상태가 ${order.orderStatus}(으)로 변경되었습니다.`)
        console.log(response)
        searchOrders() // 자동으로 업데이트되도록
      })
    }

    onMounted(() => {
      loadOrders()
    })

    return {
      deliveryStatuses,
      statusFilter,
      orders,
      today,
      startDate,
      endDate,
      searchOrders,
      updateStatus,
      formatDate
    }
  }
}
</script>

<template>
  <div id="app">
    <AppHeader/>
    <main class="container">
      <h1>주문 관리</h1>
      <div class="search-filters">
        <input type="date" v-model="startDate" :max="today">
        <input type="date" v-model="endDate" :max="today">
        <select v-model="statusFilter">
          <option value="">전체</option>
          <option v-for="status in deliveryStatuses" :key="status" :value="status">
            {{ status }}
          </option>
        </select>
        <button @click="searchOrders">검색</button>
      </div>
      <div class="order-cards">
        <div v-for="order in orders" :key="order.id" class="order-card">
          <div class="customer-info">
            <p><strong>주문자:</strong> {{ order.userNickname }}</p>
            <p><strong>연락처:</strong> {{ order.phoneNumber }}</p>
            <p><strong>주소:</strong> {{ order.address }}</p>
            <p><strong>주문일시:</strong> {{formatDate(order.createdAt)}}</p>
          </div>
          <div class="order-details">
            <div class="order-info">
              <img :src="order.productImage.imageUrl" :alt="order.productImage.filename" class="product-image">
              <div>
                <p><strong>상품명:</strong> {{ order.productName }}</p>
                <p><strong>가격:</strong> {{ order.price }}원</p>
              </div>
            </div>
            <div class="status-update">
              <select v-model="order.orderStatus" class="status-dropdown">
                <option v-for="status in deliveryStatuses" :key="status" :value="status">
                  {{ status }}
                </option>
              </select>
              <button @click="updateStatus(order)" class="update-status">배송상태 변경</button>
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

/* Main Content Styles */
main {
  flex-grow: 1;
  padding: 30px 0;
}

.search-filters {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-bottom: 20px;
  gap: 10px;
}

.search-filters input,
.search-filters select {
  padding: 8px;
  border: 1px solid var(--input-border);
  border-radius: 4px;
}

.search-filters button {
  padding: 8px 15px;
  background-color: var(--button-color);
  color: var(--bg-color);
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.search-filters button:hover {
  background-color: var(--hover-color);
}

.order-cards {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-card {
  border: 1px solid var(--card-border);
  border-radius: 8px;
  padding: 15px;
  background-color: var(--bg-color);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.customer-info {
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid var(--card-border);
}

.customer-info p,
.order-info p {
  margin: 5px 0;
}

.order-details {
  display: flex;
  justify-content: space-between;
}

.order-info {
  flex: 1;
  display: flex;
  align-items: center;
}

.product-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 4px;
  margin-right: 15px;
}

.status-update {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-dropdown {
  padding: 8px;
  border: 1px solid var(--input-border);
  border-radius: 4px;
  width: 150px;
}

.update-status {
  padding: 8px 15px;
  background-color: var(--button-color);
  color: var(--bg-color);
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.update-status:hover {
  background-color: var(--hover-color);
}
</style>