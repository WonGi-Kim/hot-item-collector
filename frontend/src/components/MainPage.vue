<template>
  <div id="app">
    <AppHeader />
    <main class="container">
      <section class="hot-top-10">
        <h2>Hot Top 10 Items</h2>
        <ol>
          <li v-for="item in hotTopItems" :key="item.id" @click="goToProduct(item.id)">{{ item.name }}</li>
        </ol>
      </section>
      <section class="new-items">
        <h2>새로 등록된 상품</h2>
        <div v-if="newItems.length > 0" class="item-cards">
          <div v-for="item in newItems" :key="item.id" class="item-card" @click="goToProduct(item.id)">
            <img :src="item.image.imageUrl" :alt="item.name">
            <div class="item-info">
              <div class="item-name">{{ item.name }}</div>
              <div class="seller-info">판매자: <a :href="'/seller/' + item.userId">{{ item.userName }}</a></div>
            </div>
          </div>
        </div>
        <div v-else class="no-items-message">등록된 상품이 없습니다.</div>
        <div v-if="newItems.length > 0" class="view-more-container">
          <button @click="viewMoreNewItems" class="view-more-btn">상품 더보기</button>
        </div>
      </section>
      <section v-if="isLoggedIn" class="followed-users-items">
        <h2>팔로우한 사용자의 상품</h2>
        <div v-if="followedUsersItems.length > 0" class="item-cards">
          <div v-for="item in followedUsersItems" :key="item.id" class="item-card" @click="goToProduct(item.id)">
            <img :src="item.image.imageUrl" :alt="item.name">
            <div class="item-info">
              <div class="item-name">{{ item.name }}</div>
              <div class="seller-info">판매자: <a :href="'/seller/' + item.userId">{{ item.userName }}</a></div>
            </div>
          </div>
        </div>
        <div v-else class="no-items-message">팔로우한 사용자의 상품이 없습니다.</div>
        <div v-if="followedUsersItems.length > 0" class="view-more-container">
          <button @click="viewMoreFollowedItems" class="view-more-btn">상품 더보기</button>
        </div>
      </section>
    </main>
    <AppFooter />
  </div>
</template>

<script>

import AppHeader from './AppHeader.vue';
import AppFooter from './AppFooter.vue';
import {onMounted, ref} from 'vue';
import {useRouter} from "vue-router";

export default {
  components: {AppHeader, AppFooter},
  setup() {
    const isLoggedIn = ref(false);
    const hotTopItems = ref([]);
    const router = useRouter();
    const client = require('../client')

    // API 요청을 통해 데이터를 가져오는 함수
    const fetchHotTopItems = async () => {
      try {
        const response = await client.get('/products/hot', {
          params: {
            page: 1,
            size: 10,
          },
          headers: {
            'Content-Type': 'application/json',
          },
        });

        // 응답 데이터 매핑
        hotTopItems.value = response.data.result.map((item) => ({
          id: item.id,
          name: item.name,
        }));
      } catch (error) {
        console.error('Failed to fetch hot top items', error);
      }
    };

    onMounted(fetchHotTopItems);

    // 신제품 목록 조회
    const fetchNewItems = async () => {
      try {

        const response = await client.get('/products/new',{
          params: {
            page: 1,
            size: 4,
          },
          headers: {
            'Content-Type': 'application/json',
          },
        });

        if (response.data && response.data.result) {
          newItems.value = response.data.result.content;
        } else {
          console.error('Unexpected response format:', response.data);
        }
      } catch (error) {

        console.error('Error fetching new items:', error);
      }
    };

    onMounted(fetchNewItems);

    const newItems = ref([]);

    const followedUsersItems = ref([]);

    function getCookie(name) {
      const value = `; ${document.cookie}`;
      const parts = value.split(`; ${name}=`);
      if (parts.length === 2) {
        return decodeURIComponent(parts.pop().split(';').shift());
      }
      return null;
    }

    const fetchFollowedProducts = async () => {
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

        const response = await client.get('/products/follow', {
          params: {
            page: 1,
            size: 4,
          },
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        followedUsersItems.value = response.data.result.content;
      } catch (error) {
        console.error('Error fetching followed users items:', error);
        if (error.response) {
          console.error('Server responded with:', error.response.data);
        }
      }
    };

    onMounted(fetchFollowedProducts);

    const showLoginModal = ref(false);
    const showSignupModal = ref(false);
    const signupLoginId = ref('');
    const signupPassword = ref('');
    const username = ref('');
    const nickname = ref('');
    const loginId = ref('');
    const password = ref('');
    const loginIdError = ref('');
    const passwordError = ref('');




    const checkLoginStatus = () => {
      const token = getCookie('access_token'); // 쿠키에서 토큰 가져오기
      isLoggedIn.value = Boolean(token); // 토큰이 있으면 로그인 상태로 간주
    };

    onMounted(checkLoginStatus);


    const viewMoreNewItems = () => {
      console.log('View more new items');
      window.location.href = '/new-items';
    };

    const viewMoreFollowedItems = () => {
      console.log('View more followed items');
      window.location.href = '/followed-items';
    };

    const goToProduct = (productId) => {
      router.push(`/product/detail/${productId}`);
    }


    return {
      isLoggedIn,
      hotTopItems,
      newItems,
      followedUsersItems,
      showLoginModal,
      showSignupModal,
      signupLoginId,
      signupPassword,
      username,
      nickname,
      loginId,
      password,
      loginIdError,
      passwordError,
      viewMoreNewItems,
      viewMoreFollowedItems,
      goToProduct,
    };
  },
};
</script>

<style scoped>
:root {
  --main-color: #FF0000;
  --text-color: #333;
  --bg-color: #FFFFFF;
  --hover-color: #FF6666;
  --button-color: #FF4136;
  --footer-bg: #f8f8f8;
  --kakao-color: #FEE500;
  --input-border: #ccc;
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

/* Header Styles */
header {
  background-color: var(--main-color);
  padding: 15px 0;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
}

.logo {
  font-size: 24px;
  font-weight: bold;
  color: var(--bg-color);
  text-decoration: none;
  margin-right: 20px;
}

.search-bar {
  display: flex;
  align-items: stretch;
  flex-grow: 1;
  margin: 10px 0;
  max-width: 600px;
}

.search-bar select,
.search-bar input,
.search-bar button {
  padding: 10px;
  font-size: 16px;
  border: none;
}

.search-bar select {
  border-radius: 5px 0 0 5px;
}

.search-bar input {
  flex-grow: 1;
  min-width: 200px;
}

.search-bar button {
  background-color: var(--button-color);
  color: var(--bg-color);
  cursor: pointer;
  border-radius: 0 5px 5px 0;
  transition: background-color 0.3s ease;
}

.search-bar button:hover {
  background-color: var(--hover-color);
}

.user-actions {
  display: flex;
  align-items: center;
}

.user-actions button {
  margin-left: 10px;
  padding: 10px 20px;
  background-color: transparent;
  color: var(--bg-color);
  border: 2px solid var(--bg-color);
  cursor: pointer;
  border-radius: 5px;
  font-weight: bold;
  transition: all 0.3s ease;
}

.user-actions button:hover {
  background-color: var(--bg-color);
  color: var(--main-color);
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-container {
  background-color: var(--bg-color);
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  max-width: 400px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
}

.modal-container .close-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  font-size: 24px;
  cursor: pointer;
  background: none;
  border: none;
  color: var(--text-color);
  padding: 0;
  width: auto;
}

.modal-container .close-btn:hover {
  color: var(--main-color);
}

.modal-container h1 {
  text-align: center;
  color: var(--text-color);
  margin-bottom: 20px;
}

.modal-container .form-group {
  margin-bottom: 20px;
}

.modal-container label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

.modal-container input {
  width: 100%;
  padding: 10px;
  border: 1px solid var(--input-border);
  border-radius: 4px;
  font-size: 16px;
  box-sizing: border-box;
}

.modal-container .error {
  color: var(--main-color);
  font-size: 14px;
  margin-top: 5px;
}

.modal-overlay button {
  width: 100%;
  padding: 10px;
  background-color: var(--button-color);
  color: var(--bg-color);
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.modal-overlay button:hover {
  background-color: var(--hover-color);
}

.modal-overlay button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.modal-container .social-login {
  margin-top: 20px;
  text-align: center;
}

.modal-container .social-login-divider {
  display: flex;
  align-items: center;
  margin: 15px 0;
}

.modal-container .social-login-divider::before,
.modal-container .social-login-divider::after {
  content: "";
  flex: 1;
  border-bottom: 1px solid var(--input-border);
}

.modal-container .social-login-divider span {
  padding: 0 10px;
  color: var(--text-color);
  font-size: 14px;
}

.modal-container .kakao-login-btn {
  background-color: var(--kakao-color);
  color: #3C1E1E;
  font-weight: bold;
}

.modal-container .kakao-login-btn:hover {
  background-color: #E6D100;
}

.login-link,
.signup-link {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
}

.login-link a,
.signup-link a {
  color: var(--main-color);
  text-decoration: none;
  cursor: pointer;
}

.login-link a:hover,
.signup-link a:hover {
  text-decoration: underline;
}

/* Categories Styles */
.categories {
  background-color: #FFFFFF;
  padding: 15px 0;
}

.categories-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: var(--bg-color);
  border-radius: 5px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.category-item {
  flex: 1;
  text-align: center;
  padding: 15px 0;
  color: var(--text-color);
  text-decoration: none;
  font-weight: bold;
  transition: all 0.3s ease;
  border-right: 1px solid #e0e0e0;
}

.category-item:last-child {
  border-right: none;
}

.category-item:hover {
  background-color: var(--hover-color);
  color: var(--bg-color);
}

/* Hot Top 10 Styles */
.hot-top-10 {
  margin: 30px auto;
  background-color: #f9f9f9;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  max-width: 600px;
  border: 2px solid var(--main-color);
}

.hot-top-10 h2 {
  color: var(--main-color);
  text-align: center;
  margin-bottom: 20px;
}

.hot-top-10 ol {
  padding-left: 0;
  counter-reset: item;
  list-style-type: none;
}

.hot-top-10 li {
  margin-bottom: 15px;
  position: relative;
  padding-left: 40px;
  font-size: 18px;
  border-bottom: 1px solid #e0e0e0;
  padding-bottom: 10px;
}

.hot-top-10 li:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.hot-top-10 li:before {
  content: counter(item);
  counter-increment: item;
  background-color: var(--main-color);
  color: white;
  font-weight: bold;
  width: 24px;
  height: 24px;
  line-height: 24px;
  text-align: center;
  border-radius: 4px;
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
}

.hot-top-10 li a {
  color: var(--text-color);
  text-decoration: none;
  transition: color 0.3s ease;
  display: block;
}

.hot-top-10 li a:hover {
  color: var(--main-color);
}

/* Item Cards Styles */
.item-cards {
  display: grid;
  grid-template-columns: repeat(4, minmax(250px, 1fr));
  gap: 20px;
  margin: 30px 0;
}

.item-card {
  border: 1px solid #ddd;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.item-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.item-card img {
  width: 100%;
  height: 253px;
  object-fit: cover;
}

.item-info {
  padding: 15px;
}

.item-name {
  font-weight: bold;
  margin-bottom: 10px;
}

.seller-info {
  font-size: 14px;
  color: #666;
}

.seller-info a {
  text-decoration: none;
}

/* View More Button Styles */
.view-more-container {
  text-align: center;
  margin-top: 20px;
  margin-bottom: 60px;
}

.view-more-btn {
  background-color: var(--bg-color);
  color: var(--main-color);
  border: 2px solid var(--main-color);
  padding: 10px 20px;
  font-size: 16px;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s ease, color 0.3s ease;
}

.view-more-btn:hover {
  background-color: var(--main-color);
  color: var(--bg-color);
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