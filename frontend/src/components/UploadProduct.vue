<script>
import { ref } from 'vue';
import AppHeader from './AppHeader.vue';
import AppFooter from './AppFooter.vue';
const client = require('../client')
import Cookies from 'js-cookie';
import { useRouter } from "vue-router";

export default {
  components: { AppFooter, AppHeader },
  setup() {
    const searchType = ref('product');
    const searchQuery = ref('');
    const categories = ref([
      { displayName: '식품', value: 'FOOD' },
      { displayName: '뷰티', value: 'BEAUTY' },
      { displayName: '패션&주얼리', value: 'FASHION' },
      { displayName: '공예품', value: 'CRAFTS' },
      { displayName: '홈리빙', value: 'HOME_LIVING' },
      { displayName: '반려동물', value: 'PET' }
    ]);

    const images = ref([]);
    const imageFiles = ref([]);
    const productCategory = ref('');
    const productName = ref('');
    const productPrice = ref(0);
    const productDescription = ref('');
    const fileInput = ref(null);
    const router = useRouter();

    const triggerFileInput = () => {
      fileInput.value.click();
    };

    const handleFileUpload = (event) => {
      const files = event.target.files;
      for (let i = 0; i < files.length; i++) {
        const file = files[i];
        imageFiles.value.push(file);

        const reader = new FileReader();
        reader.onload = (e) => {
          images.value.push(e.target.result);
        };
        reader.readAsDataURL(file);
      }
    };

    const removeImage = (index) => {
      images.value.splice(index, 1);
      imageFiles.value.splice(index, 1);
    };

    const submitProduct = async () => {
      try {
        const accessToken = Cookies.get('access_token');
        if (!accessToken) {
          throw new Error('Access token is missing.');
        }

        const formData = new FormData();
        const requestDto = {
          name: productName.value,
          category: productCategory.value,
          price: productPrice.value,
          info: productDescription.value,
        };

        formData.append('requestDto', new Blob([JSON.stringify(requestDto)], { type: 'application/json' }));

        imageFiles.value.forEach((imageFile) => {
          formData.append('files', imageFile);
        });

        const response = await client.post('/products', formData, {
          headers: {
            'Authorization': accessToken,
            'Content-Type': 'multipart/form-data',
          },
        });

        const data = response.data.result;
        console.log(data);

        setTimeout(() => {
          alert('상품이 등록되었습니다.');
          router.push(`/product/update/${data.id}`);
        }, 500);

        // 폼 초기화
        productName.value = '';
        productCategory.value = '';
        productPrice.value = 0;
        productDescription.value = '';
        images.value = [];
        imageFiles.value = [];
      } catch (error) {
        console.error('Failed to submit product:', error);
        alert(`상품 등록에 실패했습니다: ${error.message}`);
      }
    };

    return {
      searchType,
      searchQuery,
      categories,
      images,
      imageFiles,
      productCategory,
      productName,
      productPrice,
      productDescription,
      fileInput,
      triggerFileInput,
      handleFileUpload,
      removeImage,
      submitProduct,
    };
  },
};
</script>


<template>
  <div id="app">
    <AppHeader />
    <main class="container">
      <section class="product-registration">
        <h1>판매 상품 등록</h1>
        <form @submit.prevent="submitProduct">
          <div class="registration-content">
            <div class="image-upload">
              <div class="image-upload-area" @click="triggerFileInput">
                <p>클릭하여 사진 업로드</p>
                <input type="file" ref="fileInput" style="display: none" @change="handleFileUpload" multiple accept="image/*">
              </div>
              <div class="image-preview-container">
                <div class="image-preview">
                  <div v-for="(image, index) in images" :key="index" class="preview-image-container">
                    <img :src="image" class="preview-image">
                    <button class="remove-image" @click="removeImage(index)">×</button>
                  </div>
                </div>
              </div>
            </div>
            <div class="product-details">
              <div class="form-group">
                <label for="category">상품 카테고리</label>
                <select id="category" v-model="productCategory" required>
                  <option value="">카테고리 선택</option>
                  <option v-for="category in categories" :key="category.value" :value="category.value">
                    {{ category.displayName }}
                  </option>
                </select>

              </div>
              <div class="form-group">
                <label for="name">상품명</label>
                <input type="text" id="name" v-model="productName" required>
              </div>
              <div class="form-group">
                <label for="price">가격</label>
                <input type="number" id="price" v-model="productPrice" required min="0" step="100">
              </div>
              <div class="form-group">
                <label for="description">상품 설명</label>
                <textarea id="description" v-model="productDescription" required></textarea>
              </div>
            </div>
          </div>
          <div class="submit-button-container">
            <button type="submit" class="submit-button">상품 등록</button>
          </div>
        </form>
      </section>
    </main>
    <AppFooter />
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

.search-bar select {
  padding: 10px;
  font-size: 16px;
  border: none;
  border-radius: 5px 0 0 5px;
}

.search-bar input {
  padding: 10px;
  font-size: 16px;
  border: none;
  flex-grow: 1;
  min-width: 200px;
}

.search-bar button {
  padding: 10px 20px;
  font-size: 16px;
  background-color: var(--button-color);
  color: var(--bg-color);
  border: none;
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

/* Dropdown Styles */
.dropdown {
  position: relative;
  display: inline-block;
}

.dropdown-content {
  display: none;
  position: absolute;
  background-color: #f9f9f9;
  min-width: 160px;
  box-shadow: 0px 8px 16px 0px rgba(0,0,0,0.2);
  z-index: 1;
}

.dropdown:hover .dropdown-content {
  display: block;
}

.dropdown-content a {
  color: black;
  padding: 12px 16px;
  text-decoration: none;
  display: block;
}

.dropdown-content a:hover {
  background-color: #f1f1f1;
}

/* Categories Styles */
.categories {
  background-color: #f1f1f1;
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

/* Product Registration Styles */
.product-registration {
  padding: 30px 0;
}

.product-registration h1 {
  font-size: 24px;
  margin-bottom: 20px;
}

.registration-content {
  display: flex;
  gap: 30px;
}

.image-upload {
  flex: 1;
  margin-bottom: 20px;
  max-width: 50%;
}

.image-upload-area {
  border: 2px dashed #ccc;
  border-radius: 5px;
  padding: 10px;
  text-align: center;
  cursor: pointer;
  transition: background-color 0.3s ease;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}

.image-upload-area:hover {
  background-color: #f0f0f0;
}

.image-preview-container {
  width: 100%;
  height: 300px;
  overflow-x: auto;
  white-space: nowrap;
}

.image-preview {
  display: inline-flex;
  gap: 10px;
  padding-bottom: 10px;
}

.preview-image-container {
  position: relative;
  display: inline-block;
  width: 200px;
  height: 200px;
  overflow: hidden;
  border-radius: 5px;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-image {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: rgba(255, 255, 255, 0.7);
  border: none;
  border-radius: 50%;
  width: 30px;
  height: 30px;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-details {
  flex: 1;
  max-width: 50%;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

.form-group select,
.form-group input,
.form-group textarea {
  width: 100%;
  padding: 10px;
  font-size: 16px;
  border: 1px solid #ccc;
  border-radius: 5px;
}

.form-group textarea {
  height: 150px;
  resize: vertical;
}

.submit-button-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.submit-button {
  background-color: var(--main-color);
  color: var(--bg-color);
  border: none;
  padding: 10px 20px;
  font-size: 18px;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.submit-button:hover {
  background-color: var(--hover-color);
}

/* Footer Styles */
footer {
  background-color: var(--footer-bg);
  padding: 30px 0;
  margin-top: auto;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
}

.footer-links {
  display: flex;
  gap: 20px;
}

.footer-links a {
  color: var(--text-color);
  text-decoration: none;
  transition: color 0.3s ease;
}

.footer-links a:hover {
  color: var(--main-color);
}

.footer-copyright {
  margin-top: 20px;
  text-align: center;
  width: 100%;
  font-size: 14px;
  color: #666;
}

/* Scrollbar Styles */
.image-preview-container::-webkit-scrollbar {
  height: 8px;
}

.image-preview-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.image-preview-container::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 4px;
}

.image-preview-container::-webkit-scrollbar-thumb:hover {
  background: #555;
}
</style>