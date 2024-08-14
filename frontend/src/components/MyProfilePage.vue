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



/* My Info Styles */
.my-info {
  padding: 30px 0;
}

.profile-section {
  display: flex;
  align-items: flex-start;
  margin-bottom: 50px;
  padding: 30px;
  background-color: #f9f9f9;
  border-radius: 10px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.profile-image {
  width: 250px;
  height: 250px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 30px;
}

.profile-details {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.profile-name {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 10px;
  color: #000000;
}

.profile-bio {
  font-size: 18px;
  margin-bottom: 20px;
  max-width: 600px;
  line-height: 1.6;
}

.profile-stats {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.profile-stats span {
  margin-right: 20px;
  font-size: 18px;
  font-weight: bold;
}

.profile-stats button {
  padding: 10px 20px;
  background-color: var(--main-color);
  color: var(--bg-color);
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s ease;
  font-size: 16px;
}

.profile-stats button:hover {
  background-color: var(--hover-color);
}

.product-section {
  margin-bottom: 30px;
}

.product-section h2 {
  font-size: 24px;
  margin-bottom: 20px;
  color: #000000;
  cursor: pointer;
  transition: color 0.3s ease;
}

.product-section h2:hover {
  color: var(--main-color);
}

.product-list {
  display: grid;
  grid-template-columns: repeat(4, minmax(250px, 1fr));
  gap: 20px;
  margin: 30px 0 60px 0;
}

.product-card {
  display: flex;
  flex-direction: column;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  cursor: pointer;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.product-image {
  width: 100%;
  height: 253px;
  object-fit: cover;
}

.product-info {

  padding: 15px;
  flex-direction: column; /* 자식 요소를 세로로 나열 */
  align-items: flex-start; /* 좌측 정렬 */
}

.product-name {
  font-weight: bold;
  margin-bottom: 5px;
}

.seller-info {
  color: #666;
  font-size: 14px;
  flex-direction: column;

}

.seller-info a {
  color: var(--text-color);
  text-decoration: none;
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

<template>
  <div id="app">
    <AppHeader />
    <main class="container my-info">
      <section class="profile-section">
        <img :src="user.profileImage" alt="프로필 이미지" class="profile-image">
        <div class="profile-details">
          <h1 class="profile-name">{{ user.name }}</h1>
          <p class="profile-bio">{{ user.bio }}</p>
          <div class="profile-stats">
            <span>팔로워: {{ user.followers }}</span>
            <button @click="showFollowers">팔로잉 목록</button>
          </div>
        </div>
      </section>

      <section class="product-section">
        <h2 @click="goToRegisteredProducts">내가 등록한 상품</h2>
        <div v-if="registeredProducts.length > 0" class="product-list">
          <div v-for="product in registeredProducts" :key="product.id" class="product-card" @click="goToProduct(product.id)">
            <img :src="product.image" :alt="product.name" class="product-image">
            <div class="product-info">
              <div class="product-name">{{ product.name }}</div>
            </div>
          </div>
        </div>
        <div v-else class="no-items-message">등록된 상품이 없습니다.</div>
      </section>

      <section class="product-section">
        <h2 @click="goToPurchasedProducts">내가 구매한 상품</h2>
        <div v-if="purchasedProducts.length > 0" class="product-list">
          <div v-for="product in purchasedProducts" :key="product.id" class="product-card" @click="goToYourProduct(product.id)">
            <img :src="product.image" :alt="product.name" class="product-image">
            <div class="product-info">
              <div class="product-name">{{ product.name }}</div>
              <div class="seller-info">판매자: <a :href="'/seller/' + product.sellerId">{{ product.sellerNickname }}</a></div>
            </div>
          </div>
        </div>
        <div v-else class="no-items-message">구매한 상품이 없습니다.</div>
      </section>

      <section class="product-section">
        <h2 @click="goToLikedProducts">내가 좋아요한 상품</h2>
        <div v-if="likedProducts.length > 0" class="product-list">
          <div v-for="product in likedProducts" :key="product.id" class="product-card" @click="goToYourProduct(product.id)">
            <img :src="product.image" :alt="product.name" class="product-image">
            <div class="product-info">
              <div class="product-name">{{ product.name }}</div>
              <div class="seller-info">판매자: <a :href="'/seller/' + product.userId">{{ product.userNickname }}</a></div>
            </div>
          </div>
        </div>
        <div v-else class="no-items-message">좋아요한 상품이 없습니다.</div>
      </section>
    </main>
    <AppFooter />
  </div>
</template>

<script>
import {onMounted, ref} from 'vue'
import AppHeader from './AppHeader.vue';
import AppFooter from './AppFooter.vue';
import Cookies from "js-cookie";
import {useRouter} from 'vue-router';

const client = require('../client')

export default {
  name: 'App',
  components: {AppHeader, AppFooter},
  setup() {
    const router = useRouter(); // For navigation
    const user = ref({
      id: '',
      name: '',
      profileImage: '',
      bio: '',
      followers: 0
    });

    const registeredProducts = ref([]);
    const purchasedProducts = ref([]);
    const likedProducts = ref([]);


    const fetchData = async () => {
      const accessToken = Cookies.get('access_token');
      try {
        // Fetch user profile data
        const userResponse = await client.get('/users/profile', {
          headers: {
            'Authorization': accessToken
          }
        });
        // Check if bio is missing
        if (!userResponse.data.result.info) {
          router.push('/profile/update');
          return; // Exit the function if redirecting
        }
        user.value = {
          id: userResponse.data.result.id,
          name: userResponse.data.result.nickname,
          profileImage: userResponse.data.result.profileImage.imageUrl,
          bio: userResponse.data.result.info,
          followers: userResponse.data.result.followerCount || 0
        };

        // Fetch registered products
        const registeredResponse = await client.get('/products/sale', {
          params: {
            page: 1,
            size: 4
          },
          headers: {
            'Authorization': accessToken
          }
        });
        console.log(registeredResponse.data);
        registeredProducts.value = registeredResponse.data.result.content.map(product => ({
          id: product.id,
          name: product.name,
          image: product.image.imageUrl
        }));

        const purchaseResponse = await client.get(`/orderitems/buy`, {
          params: {
            page: 1,
            size: 4
          },
          headers: {
            'Authorization': accessToken
          }
        });
        console.log(purchaseResponse.data);

        purchasedProducts.value = purchaseResponse.data.result.content.map(product => ({
          id: product.productId,
          name: product.productName,
          orderId: product.orderId,
          sellerId: product.sellerId,
          sellerNickname: product.sellerNickname,
          image: product.productImage.imageUrl
        }));



        const likedResponse = await client.get('/products/like', {
          params: {
            page: 1,
            size: 4
          },
          headers: {
            'Authorization': accessToken
          }
        });
        console.log(likedResponse.data);

        likedProducts.value = likedResponse.data.result.content.map(product => ({
          id: product.id,
          name: product.name,
          userId: product.userId,
          userNickname: product.userName,
          image: product.image.imageUrl
        }));



      } catch (error) {
        console.error('Failed to fetch data:', error);
      }
    };

    onMounted(fetchData);



    const showFollowers = () => {
      router.push(`/profile/follow`);
    }

    const goToProduct = (productId) => {
      router.push(`/product/update/${productId}`);
    }
    const goToYourProduct = (productId) => {
      const orderItem = purchasedProducts.value.find(product => product.id === productId);
      if (orderItem) {
        router.push({name: 'DetailOrder', query: {orderId: orderItem.orderId}});
      } else {
        console.error('주문 아이템을 찾을 수 없습니다.');
      }
    }

    const goToRegisteredProducts = () => {
      router.push(`/product/sale`);
    }

    const goToPurchasedProducts = () => {
      router.push(`/orders/buy`)

      //alert('내가 구매한 상품 목록 페이지로 이동합니다.')
    }

    const goToLikedProducts = () => {
      router.push(`/product/like`);
    }

    return {
      user,
      registeredProducts,
      purchasedProducts,
      likedProducts,
      showFollowers,
      goToProduct,
      goToRegisteredProducts,
      goToPurchasedProducts,
      goToLikedProducts,
      goToYourProduct
    }
  }
}
</script>
