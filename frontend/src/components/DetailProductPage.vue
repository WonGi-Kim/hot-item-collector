<template>
  <div id="app">
   <AppHeader/>

    <main class="container">
      <section class="product-detail">
        <div class="seller-info">
          <img class="seller-photo" :src="product.profileImage.imageUrl || defaultProfileImage" alt="Profile Image" width="70px">
          <div class="seller-name-follow">
            <span class="seller-name">{{ product.nickname }}</span>
            <button v-if="isLoggedIn" class="follow-button" @click="toggleFollow">
              {{ isFollowing ? '팔로우 취소' : '팔로우' }}
            </button>
            <button v-if="isLoggedIn" class="chat-button" @click="createChatRoom">
              채팅하기
            </button>
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
                <button v-if="isLoggedIn" class="like-button" @click="toggleLike">
                  {{ isLiked ? '❤️' : '🤍' }}
                </button>
              </div>
            </div>
            <p class="product-category">{{ product.category }}</p>
            <p class="product-description">{{ product.info }}</p>
            <div class="product-actions-container">
              <p class="product-price">{{ formatPrice(product.price) }}원</p>
              <div class="buy-actions">
                <button v-if="isLoggedIn&product.status !=='SOLD_OUT'" class="add-to-cart" @click="addToCart">장바구니 담기</button>
                <button v-if="isLoggedIn&product.status !=='SOLD_OUT'" class="buy-now" @click="buyNow">구매하기</button>
                <button v-else class="sold-out" disabled>판매완료</button>
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>
    <AppFooter/>
  </div>
<!--카트 모달-->
  <div v-if="showModal" class="modal-overlay" @click.self= "showModal = false">
    <div class="modal-container">
      <button class="close-btn" @click="showModal = false">&times;</button>
      <h1>{{ buttonText }}</h1>
      <button v-if="showGoToCartButton" @click="goToCart">장바구니로 이동하기</button>
    </div>
  </div>

</template>

<script>
import AppHeader from './AppHeader.vue';
import {ref, onMounted, computed} from 'vue'
const client = require('../client')
import {useRoute} from "vue-router";
import AppFooter from "@/components/AppFooter.vue";
import defaultUserImage from "../assets/user.png"
import Cookies from "js-cookie";
import router from "@/router";

export default {
  components: {AppFooter, AppHeader},
  props: {
    productId: {
      type: String,
      required: true
    }
  },
  setup() {
    const isLoggedIn = ref(true)
    const isFollowing = ref(false)
    const isLiked = ref(false)
    const currentImageIndex = ref(0)
    const route = useRoute(); // useRoute를 통해 현재 라우트에 접근
    const productId = route.params.productId; // 라우트 파라미터에서 productId를 가져옴
    const accessToken = Cookies.get('access_token')
    const showModal = ref(false);
    const showGoToCartButton = ref(false);
    const cartError = ref(false)
    const buttonText = ref('')
    const errorMessage = ref('')

    // 기본 프로필 이미지 URL
    const defaultProfileImage = defaultUserImage;

    const checkLoginStatus = () => {
      isLoggedIn.value = Boolean(accessToken); // 토큰이 있으면 로그인 상태로 간주
    };
    onMounted(checkLoginStatus);

    const categoryMap = {
      FOOD: '식품',
      BEAUTY: '뷰티',
      FASHION: '패션&주얼리',
      CRAFTS: '공예품',
      HOME_LIVING: '홈리빙',
      PET: '반려동물'
    };

    // Product 초기화
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
      },
      userId: null
    });

    const fetchProduct = async () => {
      try {
        const response = await client.get(`/products/${productId}`, {
          headers: {
            'Content-Type': 'application/json',
          },
        });

        // product 데이터 설정
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

        try{
          const responseFollow = await client.get(`/follow/${product.value.userId}`, {
            headers: {
              'Authorization': accessToken
            }
          });
          isFollowing.value = responseFollow.data.result.userFollow
          console.log("팔로우 여부 불러오기 완료")
        } catch(error) {
          console.error(error)
        }
      } catch (error) {
        console.error('Error fetching product:', error);
      }
    };

    const fetchLikeStatus = async () => {
      try {
        const response = await client.get(`/likes/${productId}`, {
          headers: {
            'Authorization': accessToken
          }
        });
        isLiked.value = response.data.result.userLike
      } catch(error) {
        console.error(error)
      }
    }



    onMounted(() => {
      fetchProduct();
      fetchLikeStatus();
    });

    const currentImage = computed(() => {
      return product.value.images.length > 0 ? product.value.images[currentImageIndex.value].imageUrl : '';
    });


    const follow = async () => {
      try {
        await client.post(`/follow/${product.value.userId}`, {}, {
          headers: {
            'Authorization': accessToken
          }
        });
        isFollowing.value = true;
        console.log('팔로우 성공')
      } catch (error) {
        console.error('팔로우 실패:', error);
        buttonText.value ='자신은 팔로우할 수 없습니다.'
        showGoToCartButton.value = false
        showModal.value = true
      }
    };

    const unfollow = async () => {
      try {
        await client.delete(`/follow/${product.value.userId}`, {
          headers: {
            'Authorization': accessToken
          }
        });
        isFollowing.value = false;
        console.log('팔로우 취소 성공');
      } catch (error) {
        console.error('팔로우 취소 실패:', error);
      }
    };

    const toggleFollow = async () => {
      if (isFollowing.value) {
        await unfollow();
      } else {
        await follow();
      }
    }

    const toggleLike = async () => {

      try {
        await client.post(`/likes/${productId}`, {}, {
          headers: {
            'Authorization': accessToken
          }
        });
        isLiked.value = !isLiked.value;
        product.value.likes += isLiked.value ? 1 : -1;
      } catch (error) {
        console.error('Error toggling like:', error);
      }
     }

    const prevImage = () => {
      currentImageIndex.value = (currentImageIndex.value - 1 + product.value.images.length) % product.value.images.length
      currentImage.value = product.value.images[currentImageIndex.value]
    }

    const nextImage = () => {
      currentImageIndex.value = (currentImageIndex.value + 1) % product.value.images.length
      currentImage.value = product.value.images[currentImageIndex.value]
    }

    const goToCart = () => {
      router.push({name: 'CartPage'})
    }

    const addToCart = () => {

      client.post(`/cart/${productId}`, {},{
        headers: {
          'Content-Type':'application/json',
          'Authorization':accessToken
        },
      }).then(response => {
        console.log(response)
        buttonText.value = '장바구니에 상품을 담았습니다.'
        cartError.value = false
        showModal.value = true
        showGoToCartButton.value = true

      }).catch(error => {
        /*에러가 있는 상황*/
         cartError.value = true

        if (error.response.data.message){
          errorMessage.value = error.response.data.message
        }

        if (errorMessage.value === 'SAME USER PRODUCT'){
           buttonText.value = '자신이 판매하는 상품을 구매할 수 없습니다.'
          showGoToCartButton.value = false
        } else if (errorMessage.value === 'ALREADY_EXIST_CARTITEM'){
          buttonText.value = '이미 카트에 존재하는 상품입니다.'
          showGoToCartButton.value = true
        } else {
          buttonText.value = '상품을 장바구니에 담는 중 오류가 발생했습니다.'
          showGoToCartButton.value = false
        }
        showModal.value = true
        console.error(error); // 에러 처리
      })
    }

    const buyNow = async () => {
      await fetchProduct();

      const orderData = {
        productId: product.value.id,
        productName: product.value.name,
        price: product.value.price,
        seller: product.value.nickname,
        productImage: product.value.images[0]
      };

      sessionStorage.setItem('orderData', JSON.stringify([orderData]))
      router.push({name: 'OrderPage'})
    }

    const formatPrice = (price) => {
      return price.toLocaleString()
    }

    const switchToCart = () => {
      showModal.value = false
      showModal.value = true
    }

    const createChatRoom = async () => {
      try {
        const response = await client.post(
            '/chatroom',
            {
              sellerId: product.value.userId, // ChatRoomCreationDto에 담을 데이터
            },
            {
              headers: {
                'Authorization': accessToken,
                'Content-Type': 'application/json',
              },
            }
        );

        const chatRoomId = response.data.result.chatRoomId;
        // 채팅방 생성 성공 후, 생성된 채팅방으로 리다이렉트
        router.push({ name: 'ChatRoom', params: { chatRoomId } });
      } catch (error) {
        console.error('채팅방 생성 실패:', error);
      }
    };

    return {
      isLoggedIn,
      product,
      currentImage,
      isFollowing,
      isLiked,
      showModal,
      formatPrice,
      toggleFollow,
      toggleLike,
      prevImage,
      nextImage,
      addToCart,
      goToCart,
      buyNow,
      switchToCart,
      buttonText,
      showGoToCartButton,
      createChatRoom
    }
  },
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
  --kakao-color: #FEE500;
  --input-border: #ccc;
}

body {
  font-family: Arial, sans-serif;
  margin: 0;
  padding: 0;
  background-color: var(--bg-color);
  color: var(--text-color);
}
#app {
  font-family: Arial, sans-serif;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.container {
  width: 80%;
  margin: 0 auto;
  padding: 0;
  flex: 1;
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

.follow-button,.chat-button {
  padding: 5px 15px;
  background-color: var(--main-color);
  color: var(--bg-color);
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.follow-button:hover,.chat-button:hover {
  background-color: var(--hover-color);
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

.like-button {
  background-color: transparent;
  border: none;
  cursor: pointer;
  font-size: 24px;
  padding: 0;
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

.add-to-cart,
.buy-now ,.sold-out{
  padding: 12px 24px;
  font-size: 16px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s ease;
  flex: 1;
}

.add-to-cart {
  background-color: #f0f0f0;
  color: var(--text-color);
}

.add-to-cart:hover {
  background-color: #e0e0e0;
}


.buy-now {
  background-color: var(--main-color);
  color: var(--bg-color);
}

.buy-now:hover {
  background-color: var(--hover-color);
}

</style>
