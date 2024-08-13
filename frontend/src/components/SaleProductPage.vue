<script>
import { ref } from 'vue';
import AppHeader from "@/components/AppHeader.vue";
import AppFooter from "@/components/AppFooter.vue";
import Cookies from "js-cookie";
const client = require('../client')
import {useRouter} from "vue-router";

export default {
  components: {AppFooter, AppHeader},
  setup() {
    const searchType = ref('product');
    const searchQuery = ref('');
    const categories = ref(['식품', '뷰티', '패션&주얼리', '공예품', '홈리빙', '반려동물']);
    const filter = ref('all');
    const currentPage = ref(1);
    const products = ref([]);
    const totalPages = ref(1);
    const itemsPerPage = 12;
    const router = useRouter();

    const fetchProducts = async (filterType, page) => {
      const accessToken = Cookies.get('access_token');
      let status;

      switch (filterType) {
        case 'selling':
          status = 'ON_SALE';
          break;
        case 'sold':
          status = 'SOLD_OUT';
          break;
        default:
          status = null;
      }

      try {
        const params = status ? { status } : {};
        const response = await client.get(`/products/sale?page=${page}&size=${itemsPerPage}`, {
          headers: {
            'Authorization': accessToken
          },
          params
        });
        const data = response.data.result;
        products.value = data.content || [];
        totalPages.value = data.totalPages || 1;
      } catch (error) {
        console.error('Error fetching products:', error);
      }
    };

    const setFilter = (filterType) => {
      filter.value = filterType;
      currentPage.value = 1;
      fetchProducts(filterType, currentPage.value);
    };

    const prevPage = () => {
      if (currentPage.value > 1) {
        currentPage.value--;
        fetchProducts(filter.value, currentPage.value);
      }
    };

    const nextPage = () => {
      if (currentPage.value < totalPages.value) {
        currentPage.value++;
        fetchProducts(filter.value, currentPage.value);
      }
    };

    const goToProduct = (productId) => {
      router.push(`/product/update/${productId}`);
    };

    const goToProductManagement = () => {
      router.push('/orders/sell');
    };

    const goToProductRegistration = () => {
      router.push('/product/upload');
    };

    // Initial fetch
    fetchProducts(filter.value, currentPage.value);

    return {
      searchType,
      searchQuery,
      categories,
      filter,
      currentPage,
      totalPages,
      products,
      setFilter,
      prevPage,
      nextPage,
      goToProduct,
      goToProductManagement,
      goToProductRegistration
    };
  }
}
</script>

<template>
  <div id="app">
    <AppHeader />

    <main class="container product-list">
      <div class="product-list-header">
        <h1 class="product-list-title">판매 상품 목록</h1>
        <div class="product-list-actions">
          <button @click="goToProductManagement">주문 관리</button>
          <button @click="goToProductRegistration">상품 등록</button>
        </div>
      </div>
      <div class="product-status-filter">
        <button :class="{ active: filter === 'all' }" @click="setFilter('all')">전체</button>
        <button :class="{ active: filter === 'selling' }" @click="setFilter('selling')">판매중</button>
        <button :class="{ active: filter === 'sold' }" @click="setFilter('sold')">판매완료</button>
      </div>
      <div class="product-grid">
        <div v-for="product in products" :key="product.id" class="product-card" @click="goToProduct(product.id)">
          <img :src="product.image.imageUrl" :alt="product.name" class="product-image">
          <div class="product-info">
            <div class="product-name">{{ product.name }}</div>
            <span :class="['product-status', product.status === 'ON_SALE' ? 'status-selling' : 'status-sold']">
              {{ product.status === 'ON_SALE' ? '판매중' : '판매완료' }}
            </span>
          </div>
        </div>
      </div>
      <div class="pagination">
        <button @click="prevPage" :disabled="currentPage === 1">&lt; 이전</button>
        <span>{{ currentPage }} / {{ totalPages }}</span>
        <button @click="nextPage" :disabled="currentPage === totalPages">다음 &gt;</button>
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
  --status-sold: #4CAF50;
  --status-selling: #2196F3;
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

/* Product List Styles */
.product-list {
  padding: 20px 0;
}

.product-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.product-list-title {
  font-size: 24px;
  font-weight: bold;
}

.product-list-actions button {
  padding: 10px 20px;
  background-color: var(--button-color);
  color: var(--bg-color);
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 16px;
  margin-left: 10px;
  transition: background-color 0.3s ease;
}

.product-list-actions button:hover {
  background-color: var(--hover-color);
}

.product-status-filter {
  margin-bottom: 20px;
}

.product-status-filter button {
  padding: 8px 16px;
  background-color: var(--bg-color);
  color: var(--text-color);
  border: 1px solid var(--card-border);
  border-radius: 5px;
  cursor: pointer;
  font-size: 14px;
  margin-right: 10px;
  transition: all 0.3s ease;
}

.product-status-filter button.active {
  background-color: var(--button-color);
  color: var(--bg-color);
  border-color: var(--button-color);
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.product-card {
  border: 1px solid var(--card-border);
  border-radius: 5px;
  overflow: hidden;
  transition: box-shadow 0.3s ease;
}

.product-card:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.product-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.product-info {
  padding: 15px;
}

.product-name {
  font-weight: bold;
  margin-bottom: 5px;
}

.product-id {
  font-size: 12px;
  color: #666;
  margin-bottom: 5px;
}

.product-status {
  display: inline-block;
  padding: 3px 8px;
  border-radius: 3px;
  font-size: 12px;
  font-weight: bold;
}

.status-selling {
  background-color: red;
  color: white;
}

.status-sold {
  background-color: red;
  color: white;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 20px;
}

.pagination button {
  padding: 8px 12px;
  background-color: var(--bg-color);
  color: var(--text-color);
  border: 1px solid var(--card-border);
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
}

.pagination button:hover:not(:disabled) {
  background-color: var(--hover-color);
  color: var(--bg-color);
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination span {
  margin: 0 10px;
  font-size: 16px;
}

</style>