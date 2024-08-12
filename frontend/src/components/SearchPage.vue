<template>
  <div id="app">
    <AppHeader/>
    <main class="container">
      <section class="search-results">
        <h2>{{ pageTitle }}</h2>
        <div v-if="products.length > 0" class="item-grid">
          <div v-for="item in products" :key="item.id" class="item-card"
               @click="item ? goToProduct(item.id) : null">
            <img :src="item?.image?.imageUrl || '/path/to/default-image.jpg'"
                 :alt="item?.name || 'Default Alt Text'">
            <div class="item-info">
              <div class="item-name">{{ item?.name || 'Unnamed Item' }}</div>
              <div class="item-seller">
                판매자: <a @click.stop="item ? goToSellerPage(item.userId) : null"
                        :href="item ? '/seller/' + item.userId : '#'"
                        :title="item ? item.userName : 'No Seller'">
                {{ item?.userName || 'Unknown Seller' }}
              </a>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="no-items-message">등록된 상품이 없습니다.</div>
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


<script>
import AppHeader from './AppHeader.vue';
import AppFooter from './AppFooter.vue';
import { ref, computed, onMounted } from 'vue';
const client = require('../client')
import { useRouter, useRoute } from "vue-router";

export default {
  components: { AppHeader, AppFooter },
  setup() {
    const isLoggedIn = ref(false);
    const currentPage = ref(1);
    const totalPages = ref(1);
    const itemsPerPage = 16;
    const products = ref([]); // 초기값을 빈 배열로 설정
    const searchQuery = ref('');
    const searchType = ref('');


    const router = useRouter();
    const route = useRoute();



    const pageTitle = computed(() => {
      if (searchType.value === 'product') {
        return `상품 검색 결과: ${searchQuery.value}`;
      } else if (searchType.value === 'seller') {
        return `판매자 검색 결과: ${searchQuery.value}`;
      } else {
        return ''; // Default return value
      }
    });

    const fetchProducts = async (page) => {
      try {
        let url = '';
        if (searchType.value === 'product') {
          url = `/products/search?page=${page}&size=${itemsPerPage}&productName=${searchQuery.value}`;
        } else if (searchType.value === 'seller') {
          url = `/products/search?page=${page}&size=${itemsPerPage}&nickname=${searchQuery.value}`;
        }
        const response = await client.get(url);
        console.log(response.data);
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


    const goToSellerPage = (userId) => {
      router.push(`/seller/${userId}`);
    };

    const goToProduct = (productId) => {
      alert(`상품 ID ${productId}의 상세 페이지로 이동합니다.`);
      router.push(`/product/detail/${productId}`);
    };

    onMounted(() => {
      searchType.value = route.query.searchType;
      searchQuery.value = route.query.searchQuery;
      fetchProducts(currentPage.value);
    });

    return {
      isLoggedIn,
      searchQuery,
      searchType,
      currentPage,
      totalPages,
      itemsPerPage,
      products,
      pageTitle,
      prevPage,
      nextPage,
      goToProduct,
      goToSellerPage,
    };
  }
};
</script>


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


</style>
