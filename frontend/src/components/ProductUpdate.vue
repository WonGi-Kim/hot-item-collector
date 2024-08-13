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

/* Product Detail Styles */
.product-detail {
  display: flex;
  flex-direction: column;
  padding: 30px 0;
}

.seller-info {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.seller-photo {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 15px;
}

.seller-name-follow {
  display: flex;
  align-items: center;
  gap: 15px;
}

.seller-name {
  font-size: 18px;
  font-weight: bold;
}


.product-info {
  display: flex;
  gap: 100px;
}

.product-image-container {
  flex: 1;
  max-width: 500px;
  position: relative;
  aspect-ratio: 1 / 1;
  overflow: hidden;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

.image-nav-button {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
  border: none;
  padding: 10px;
  cursor: pointer;
  font-size: 20px;
  transition: background-color 0.3s ease;
}

.image-nav-button:hover {
  background-color: rgba(0, 0, 0, 0.7);
}

.image-nav-button.prev {
  left: 10px;
}

.image-nav-button.next {
  right: 10px;
}

.product-details {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.product-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.product-name {
  font-size: 24px;
  font-weight: bold;
}

.like-actions {
  display: flex;
  align-items: center;
}

.like-count {
  font-size: 16px;
  margin-right: 10px;
}

.product-category {
  font-size: 16px;
  color: #666;
  margin-bottom: 20px;
}

.product-description {
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 20px;
  flex-grow: 1;
}

.product-actions-container {
  display: flex;
  flex-direction: column;
  align-items: stretch;
}

.product-price {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 15px;
  color: var(--main-color);
  text-align: right;
}

.buy-actions {
  display: flex;
  gap: 10px;
}

.edit-now,.delete-now,.sold-out {
  padding: 12px 24px;
  font-size: 16px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s ease;
  flex: 1;
}


.edit-now,.delete-now {
  background-color: var(--main-color);
  color: var(--bg-color);
}

.edit-now:hover {
  background-color: var(--hover-color);
}
</style>

<template>
  <div id="app">
    <AppHeader />
    <main class="container">
      <section class="product-detail">
        <div class="seller-info">
          <img class="seller-photo" :src="product.profileImage.imageUrl || defaultProfileImage" alt="Profile Image" width="70px">
          <div class="seller-name-follow">
            <span class="seller-name">{{ product.nickname }}</span>
          </div>
        </div>
        <div class="product-info">
          <div class="product-image-container">
            <img :src="currentImage" :alt="product.name" class="product-image">
            <button class="image-nav-button prev" @click="prevImage" v-if="product.images.length > 1">&lt;</button>
            <button class="image-nav-button next" @click="nextImage" v-if="product.images.length > 1">&gt;</button>
          </div>
          <div class="product-details">
            <div class="product-header">
              <h1 class="product-name">{{ product.name }}</h1>
              <div class="like-actions">
                <span class="like-count">{{ product.likes }} 좋아요</span>
              </div>
            </div>
            <p class="product-category">{{ product.category }}</p>
            <p class="product-description">{{ product.info }}</p>
            <div class="product-actions-container">
              <p class="product-price">{{ formatPrice(product.price) }}원</p>
              <div class="buy-actions">
                <button v-if="product.status !== 'SOLD_OUT'" class="edit-now" :key="product.id" @click="editProduct(product.id)">상품정보 수정</button>
                <button v-else class="sold-out" disabled>판매완료</button>
                <button class="delete-now" @click="deleteProduct">상품정보 삭제</button>
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>
    <AppFooter />
  </div>
</template>
<script>
import { ref, computed, onMounted  } from 'vue';
import AppHeader from './AppHeader.vue';
import AppFooter from './AppFooter.vue';
import {useRoute, useRouter} from 'vue-router';
const client = require('../client')
import defaultProfile from "../assets/user.png";
import Cookies from "js-cookie";

export default {
  components: {AppFooter, AppHeader},

  setup() {
    const route = useRoute(); // useRoute를 통해 현재 라우트에 접근
    const productId = route.params.productId; // 라우트 파라미터에서 productId를 가져옴
    const currentImageIndex = ref(0);
    const router = useRouter();

    // 기본 프로필 이미지 URL
    const defaultProfileImage = defaultProfile;

    const categoryMap = {
      FOOD: '식품',
      BEAUTY: '뷰티',
      FASHION: '패션&주얼리',
      CRAFTS: '공예품',
      HOME_LIVING: '홈리빙',
      PET: '반려동물'
    };

    // 상품 상세 정보 (실제로는 API에서 가져와야 함)
    const product = ref({
      id: null,
      name: '',
      category: '',
      images: [],
      price: 0,
      info: '',
      likes: 0,
      profileImage: {
        id: null,
        filename: '',
        imageUrl: defaultProfileImage
      }
    });

    const currentImage = computed(() => {
      return product.value.images.length > 0 ? product.value.images[currentImageIndex.value].imageUrl : '';
    });

    const formatPrice = (price) => {
      return price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",")
    }

    const prevImage = () => {
      if (currentImageIndex.value > 0) {
        currentImageIndex.value--
      } else {
        currentImageIndex.value = product.value.images.length - 1
      }
    }

    const nextImage = () => {
      if (currentImageIndex.value < product.value.images.length - 1) {
        currentImageIndex.value++
      } else {
        currentImageIndex.value = 0
      }
    }

    const fetchProduct = async () => {
      if (productId) {
        try {
          const response = await client.get(`/products/${productId}`);
          console.log(response.data.result);

          product.value = response.data.result;

          // 카테고리 한글 변환
          if (product.value.category && categoryMap[product.value.category]) {
            product.value.category = categoryMap[product.value.category];
          }

          // 프로필 이미지가 null일 경우 기본 이미지 설정
          if (!product.value.profileImage || !product.value.profileImage.imageUrl) {
            product.value.profileImage = {
              id: null,
              filename: '',
              imageUrl: defaultProfileImage
            };
          }
        } catch (error) {
          console.error('Failed to fetch product data:', error);
        }
      } else {
        console.error('Product ID is missing in route parameters');
      }
    };

    onMounted(fetchProduct);

    const deleteProduct = async() =>{
      if (confirm('정말 삭제하시겠습니까?')) {
      if (productId) {
        const accessToken = Cookies.get('access_token');
        try{
           const response = await client.delete(`/products/${productId}`,{
             headers: {
               'Authorization': accessToken
             }
           });
           console.log(response.data);
          router.push('/product/sale');
        }catch (error){
        console.error('Failed to fetch product data:', error);
      }
      }else {
        console.error('Product ID is missing in route parameters');
      }
    }}

    const editProduct = (productId) => {
      alert(`상품 ID ${productId}의 수정 페이지로 이동합니다.`)
      router.push(`/product/edit/${productId}`);
    }

    return {
      product,
      currentImage,
      formatPrice,
      prevImage,
      nextImage,
      deleteProduct,
      editProduct
    }
  }
}
</script>