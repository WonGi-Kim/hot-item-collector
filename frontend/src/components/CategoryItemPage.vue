<script>
import AppHeader from './AppHeader.vue';
import {computed, onMounted, ref} from 'vue';
import {useRoute, useRouter} from "vue-router";
import AppFooter from './AppFooter.vue';

const client = require('../client')
export default {
  components: { AppHeader, AppFooter },
  setup() {
    const isLoggedIn = ref(false);
    const currentPage = ref(1);
    const totalPages = ref(1);
    const itemsPerPage = 8;
    const products = ref([]); // 초기값을 빈 배열로 설정
    const searchQuery = ref('');
    const categoryType = ref('');

    const categories = {
      '식품': 'FOOD',
      '뷰티': 'BEAUTY',
      '패션&주얼리': 'FASHION',
      '공예품': 'CRAFTS',
      '홈리빙': 'HOME_LIVING',
      '반려동물': 'PET'
    };

    const router = useRouter();
    const route = useRoute();

    const pageTitle = computed(() => `${categoryType.value} 상품 목록`);

    const fetchProducts = async (page) => {
      try {
        const url = `/products/search?page=${page}&size=${itemsPerPage}&category=${searchQuery.value}`;
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

    const selectCategory = (category) => {
      searchQuery.value = categories[category] || category;
      currentPage.value = 1;
      fetchProducts(currentPage.value);
    };

    const goToSellerPage = (userId) => {
      router.push(`/seller/${userId}`);
    };

    const goToProduct = (productId) => {
      router.push(`/product/detail/${productId}`);
    };

    onMounted(() => {
      const initialCategory = route.query.searchQuery;
      categoryType.value = initialCategory;
      searchQuery.value = categories[initialCategory] || initialCategory;
      fetchProducts(currentPage.value);
    });

    return {
      isLoggedIn,
      searchQuery,
      currentPage,
      totalPages,
      itemsPerPage,
      products,
      pageTitle,
      prevPage,
      nextPage,
      goToProduct,
      goToSellerPage,
      selectCategory
    };
  }
};
</script>

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
    <AppFooter />
  </div>
</template>

<style scoped>
/* 기본 스타일 추가 */
#app {
  font-family: Arial, sans-serif;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.container {
  width: 80%;
  margin: 0 auto;
  flex: 1;
}

.item-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(250px, 1fr)); /* 4개의 열을 고정하고 각 열이 동일한 비율로 공간을 차지하도록 설정 */
  gap: 16px; /* 아이템 간의 간격 설정 */
}

.item-card {
  display: flex;
  flex-direction: column;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s;
  height: 100%; /* 카드의 높이를 부모의 높이에 맞게 조정 */
}

.item-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.item-card img {
  width: 100%;
  height: 253px; /* 이미지의 고정 높이 설정 */
  object-fit: cover; /* 이미지가 카드에 맞게 잘리도록 설정 */
}

.item-info {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 10px;
  height: 100%; /* info 영역이 카드의 전체 높이를 차지하도록 설정 */
}

.item-name {
  font-weight: bold;
  margin-bottom: 5px;
  overflow: hidden; /* 텍스트가 넘치면 숨김 */
  text-overflow: ellipsis; /* 넘치는 텍스트를 '...'으로 표시 */
  white-space: nowrap; /* 텍스트 줄 바꿈 방지 */
  width: 100%; /* 부모 컨테이너의 너비를 따라감 */
  box-sizing: border-box; /* padding과 border를 width에 포함시킴 */
}

.item-seller {
  font-size: 0.9em;
  color: #555;
  margin-top: auto; /* 판매자 정보를 카드 하단에 배치 */
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
