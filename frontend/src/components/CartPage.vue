<template>
  <div id="app">
    <AppHeader/>
    <main class="container cart">
      <h1>장바구니</h1>
      <div class="cart-items">
        <div v-for="item in cartItems" :key="item.id" class="cart-item">
          <div class="cart-item-left">
            <input type="checkbox" v-model="item.selected" class="cart-item-checkbox">
            <img :src="item.productImage.imageUrl" :alt="item.productImage.filename" class="cart-item-image">
            <div class="cart-item-details">
              <div class="cart-item-name">{{ item.productName }}</div>
              <div class="cart-item-price">{{ item.price }}원</div>
              <div class="cart-item-seller">판매자: {{ item.seller }}</div>
            </div>
          </div>
          <div class="cart-item-actions">
            <button @click="buyItem(item)">구매하기</button>
            <button @click="removeItem(item)">삭제</button>
          </div>
        </div>
      </div>
      <div class="cart-summary">
        <div class="cart-total">총 금액: {{ totalPrice }}원</div>
        <button class="cart-order-button" @click="orderItems">주문하기</button>
      </div>
    </main>
    <AppFooter/>
  </div>
</template>

<script>
import {ref, computed, onMounted} from 'vue';
const client = require('../client')
import {useRouter} from "vue-router";
import Cookies from "js-cookie";
import AppHeader from './AppHeader.vue';
import AppFooter from './AppFooter.vue';

export default {
  components: { AppHeader, AppFooter },
  setup() {
    const cartItems = ref([]); // 장바구니 항목을 저장할 상태
    const accessToken = Cookies.get('access_token');

    const loadCartItems = () => {

      client.get('/cart', {
        headers: {
          'Authorization': accessToken
        }
      }).then(response => {
        cartItems.value = response.data.result; // 응답 데이터를 상태에 저장
      }).catch(error => {
        console.error(error); // 에러 처리
      });
    };

    const totalPrice = computed(() => {
      return cartItems.value.reduce((total, item) => {
        return item.selected ? total + item.price : total
      }, 0)
    })

    const router = useRouter();

    const buyItem = (item) => {
      const orderData = [item]
      sessionStorage.setItem('orderData', JSON.stringify(orderData));
      router.push({name: 'OrderPage'})
    }

    const removeItem = (item) => {
      const userConfirmed = confirm(`${item.productName}을(를) 장바구니에서 삭제하시겠습니까?`)
      if (!userConfirmed) {
        return;
      }
      client.delete(`/cart/${item.productId}`, {
        headers: {
          'Authorization': accessToken
        }
      }).then(response => {
        if (response.status == 200) {
          window.location.reload();
        }
      }).catch(error => {
        console.error(error);
      })
    }

    const orderItems = () => {
      const orderData = cartItems.value.filter(item => item.selected)
      if (orderData.length > 0) {

        sessionStorage.setItem('orderData', JSON.stringify(orderData));
        router.push({name: 'OrderPage'})
      } else {
        alert('선택한 상품이 없습니다.')
      }
    }

    onMounted(() => {
      loadCartItems(); // 컴포넌트가 마운트될 때 API 호출
    });

    return {
      cartItems,
      totalPrice,
      buyItem,
      removeItem,
      orderItems
    }
  }
}
</script>



<style scoped>
:root {
  --main-color: #FF0000;
  --text-color: #333;
  --bg-color: #FFFFFF;
  --hover-color: #FF6666;
  --button-color: #FF4136;
  --footer-bg: #f8f8f8;
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

/* Cart Styles */
.cart {
  padding: 20px 0;
}

.cart-items {
  margin-bottom: 20px;
}

.cart-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding: 10px;
  border: 1px solid #e0e0e0;
  border-radius: 5px;
}

.cart-item-left {
  display: flex;
  align-items: center;
}

.cart-item-checkbox {
  margin-right: 10px;
}

.cart-item-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  margin-right: 20px;
}

.cart-item-details {
  flex-grow: 1;
}

.cart-item-name {
  font-weight: bold;
  margin-bottom: 5px;
}

.cart-item-price {
  color: var(--main-color);
  font-weight: bold;
  margin-bottom: 5px;
}

.cart-item-seller {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.cart-item-actions {
  display: flex;
  gap: 10px;
}

.cart-item-actions button {
  padding: 5px 10px;
  background-color: var(--button-color);
  color: var(--bg-color);
  border: none;
  border-radius: 3px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.cart-item-actions button:hover {
  background-color: var(--hover-color);
}

.cart-summary {
  background-color: var(--bg-color);
  padding: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 5px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cart-total {
  font-size: 18px;
  font-weight: bold;
}

.cart-order-button {
  padding: 10px 20px;
  background-color: var(--button-color);
  color: var(--bg-color);
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.3s ease;
}

.cart-order-button:hover {
  background-color: var(--hover-color);
}
</style>