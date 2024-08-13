<script>
import AppHeader from './AppHeader.vue';
import { ref, computed, onMounted } from 'vue';
const client = require('../client')
import {useRouter} from "vue-router";
import AppFooter from "@/components/AppFooter.vue";

export default {
  components: {AppFooter, AppHeader},
  setup() {
    const isLoggedIn = ref(false);
    const currentPage = ref(1);
    const totalPages = ref(1);
    const itemsPerPage = 16;
    const products = ref([]); // 초기값을 빈 배열로 설정

    const router = useRouter();

    const pageTitle = computed(() => '팔로우 사용자 상품 목록');


    function getCookie(name) {
      const value = `; ${document.cookie}`;
      const parts = value.split(`; ${name}=`);
      if (parts.length === 2) {
        return decodeURIComponent(parts.pop().split(';').shift());
      }
      return null;
    }

    const fetchProducts = async (page) => {
      try {
        let token = getCookie('access_token');
        if (!token) {
          console.error('No token found');
          return;
        }
        // Bearer 접두사 제거
        if (token.startsWith('Bearer ')) {
          token = token.substring(7);
        }

        if (!token) {
          console.error('Token is empty after processing');
          return;
        }

        const url = `/products/follow?page=${page}&size=${itemsPerPage}`;
        const response = await client.get(url,{
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        const data = response.data.result;
        products.value = data.content || [];
        totalPages.value = data.totalPages || 1;
      } catch (error) {
        console.error('Failed to fetch products:', error);
      }
    };

    const prevPage = () => {
      if (currentPage.value > 1) {
        currentPage.value--;
        fetchProducts(currentPage.value);
      }
    };

    const nextPage = () => {
      if (currentPage.value < totalPages.value) {
        currentPage.value++;
        fetchProducts(currentPage.value);
      }
    };

    const goToItemPage = (itemId) => {
      window.location.href = `/products/${itemId}`;
    };

    const goToSellerPage = (userId) => {
      window.location.href = `/seller/${userId}`;
    };

    onMounted(() => {
      fetchProducts(currentPage.value);
    });
    const goToProduct = (productId) => {
      router.push(`/product/detail/${productId}`);
    }

    return {
      isLoggedIn,
      currentPage,
      totalPages,
      itemsPerPage,
      products,
      pageTitle,
      prevPage,
      nextPage,
      goToItemPage,
      goToSellerPage,
      goToProduct
    };
  }
}
</script>

<template>
  <div id="app">
    <AppHeader/>
    <main class="container">
      <section class="search-results">
        <h2>{{ pageTitle }}</h2>
        <div class="item-grid">
          <div v-for="item in products" :key="item.id" class="item-card"
               @click="goToProduct(item.id)">
            <img :src="item.image.imageUrl" :alt="item.name">
            <div class="item-info">
              <div class="item-name">{{ item.name }}</div>
              <div class="item-seller">
                판매자: <a @click.stop="goToSellerPage(item.userId)" :href="'/seller/' + item.userId">{{ item.userName }}</a>
              </div>
            </div>
          </div>
        </div>
        <div class="pagination">
          <button @click="prevPage" :disabled="currentPage === 1">&lt; 이전</button>
          <span class="page-number">{{ currentPage }} / {{ totalPages }}</span>
          <button @click="nextPage" :disabled="currentPage === totalPages">다음 &gt;</button>
        </div>
      </section>
    </main>
    <AppFooter/>
  </div>
</template>

<style scoped>
/* 기본 스타일 추가 */
#app {
  font-family: Arial, sans-serif;
}

.container {
  width: 80%;
  margin: 0 auto;
}

.item-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr); /* 4개의 열을 만들고, 각 열이 동일한 비율로 공간을 차지하도록 설정 */
  gap: 16px; /* 아이템 간의 간격을 설정 */
}

.item-card {
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s;
}

.item-card:hover {
  transform: scale(1.05);
}

.item-card img {
  width: 100%;
  height: auto;
}

.item-info {
  padding: 10px;
}

.item-name {
  font-weight: bold;
}

.item-seller {
  font-size: 0.9em;
  color: #555;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 10px;
}

button {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  background-color: #007bff;
  color: white;
  cursor: pointer;
}

button:disabled {
  background-color: #ddd;
}

footer {
  background-color: #f8f9fa;
  padding: 20px;
  text-align: center;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-container {
  background: white;
  padding: 20px;
  border-radius: 8px;
  width: 400px;
  position: relative;
}

.close-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
}

.form-group {
  margin-bottom: 15px;
}

label {
  display: block;
  margin-bottom: 5px;
}

input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.error {
  color: red;
  font-size: 0.9em;
}
</style>
