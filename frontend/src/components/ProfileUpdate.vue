
<style scoped>
:root {
  --main-color: #FF0000;
  --text-color: #333;
  --bg-color: #FFFFFF;
  --hover-color: #FF6666;
  --button-color: #FF4136;
  --input-border: #ccc;
  --modal-bg: rgba(0, 0, 0, 0.5);
}

body {
  font-family: Arial, sans-serif;
  margin: 0;
  padding: 0;
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


/* Edit Profile Styles */
.edit-profile {
  max-width: 600px;
  margin: 40px auto;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 10px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}

.edit-profile h1 {
  color: var(--text-color);
  margin-bottom: 20px;
  font-size: 28px;
  font-weight: bold;
  text-align: center;
}

.edit-profile-form {
  display: grid;
  gap: 15px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  margin-bottom: 5px;
  font-weight: bold;
}

.form-group input,
.form-group textarea {
  padding: 10px;
  border: 1px solid var(--border-color);
  border-radius: 5px;
  font-size: 16px;
}

.form-group input[readonly] {
  background-color: var(--light-gray);
}

.form-group textarea {
  resize: vertical;
  min-height: 100px;
}

/* Submit Button */
.submit-btn {
  padding: 10px 20px;
  background-color: var(--bg-color);
  color: var(--text-color);
  border: 1px solid var(--border-color);
  border-radius: 5px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 16px;
  font-weight: bold;
}

/* Submit Button Hover State */
.submit-btn:hover {
  background-color: var(--hover-color);
}

.profile-image {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  object-fit: cover;
  margin-bottom: 15px;
}

.image-upload {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.profile-image-input {
  display: none;
}

/* Profile Image Label */
.profile-image-label {
  padding: 10px 20px;
  background-color: var(--bg-color);
  color: var(--text-color);
  border: 1px solid var(--border-color);
  border-radius: 5px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 16px;
  font-weight: bold;
}

/* Profile Image Label Hover State */
.profile-image-label:hover {
  background-color: var(--hover-color);
}


</style>

<template>
  <div id="app">
    <AppHeader />
    <main class="container edit-profile">
      <h1>개인정보 수정</h1>
      <form class="edit-profile-form" @submit.prevent="submitForm">
        <div class="image-upload">
          <img :src="userInfo.profileImage || defaultProfileImage" alt="프로필 이미지" class="profile-image">
          <input type="file" id="profile-image-input" class="profile-image-input" @change="onFileChange" accept="image/*">
          <label for="profile-image-input" class="profile-image-label">프로필 사진 변경</label>
        </div>
        <div class="form-group">
          <label for="username">사용자 이름</label>
          <input type="text" id="username" v-model="userInfo.username" readonly>
        </div>
        <div class="form-group">
          <label for="loginId">로그인 ID</label>
          <input type="text" id="loginId" v-model="userInfo.loginId" readonly>
        </div>
        <div class="form-group">
          <label for="nickname">닉네임</label>
          <input type="text" id="nickname" v-model="userInfo.nickname">
        </div>
        <div class="form-group">
          <label for="address">주소</label>
          <input type="text" id="address" v-model="userInfo.address">
        </div>
        <div class="form-group">
          <label for="phoneNumber">전화번호</label>
          <input type="tel" id="phoneNumber" v-model="userInfo.phoneNumber">
        </div>
        <div class="form-group">
          <label for="info">자기소개</label>
          <textarea id="info" v-model="userInfo.info"></textarea>
        </div>
        <button type="submit" class="submit-btn">수정 완료</button>
      </form>
    </main>
    <AppFooter />
  </div>
</template>

<script>
import { ref, onMounted  } from 'vue';
const client = require('../client')
import Cookies from 'js-cookie';
import AppHeader from './AppHeader.vue';
import AppFooter from './AppFooter.vue';
import { useRouter } from 'vue-router';
export default {
  components: { AppHeader, AppFooter },
  setup() {
    const router = useRouter(); // For navigation
    const defaultProfileImage = require('@/assets/user.png');
    const userInfo = ref({
      username: 'johndoe',
      loginId: 'johndoe123',
      nickname: '',
      address: '',
      phoneNumber: '',
      info: '',
      profileImage: null
    });

    const selectedFile = ref(null);

    const fetchUserProfile = async () => {
      try {
        const accessToken = Cookies.get('access_token');
        const response = await client.get('/users/profile', {
          headers: {
            'Authorization': accessToken
          }
        });
        const data = response.data.result;
        console.log(data)
        userInfo.value = {
          username: data.username, // Replace with actual username if available
          loginId: data.loginId, // Replace with actual login ID if available
          nickname: data.nickname,
          address: data.address,
          phoneNumber: data.phoneNumber,
          info: data.info,
          profileImage: data.profileImage ? data.profileImage.imageUrl : defaultProfileImage
        };
      } catch (error) {
        console.error('Failed to fetch user profile:', error);
      }
    };




    const onFileChange = (event) => {
      const file = event.target.files[0];
      if (file) {
        selectedFile.value = file;
        const reader = new FileReader();
        reader.onload = (e) => {
          userInfo.value.profileImage = e.target.result;
        };
        reader.readAsDataURL(file);
      }
    };

    const submitForm = async () => {
      try {
        const accessToken = Cookies.get('access_token');

        const formData = new FormData();
        formData.append('requestDto', new Blob([JSON.stringify({
          nickname: userInfo.value.nickname,
          address: userInfo.value.address,
          phoneNumber: userInfo.value.phoneNumber,
          info: userInfo.value.info
        })], { type: 'application/json' }));

        if (selectedFile.value) {
          formData.append('files', selectedFile.value);
        }

        await client.patch('/users/profile', formData, {
          headers: {
            'Authorization': accessToken,
            'Content-Type': 'multipart/form-data'
          }
        });

        setTimeout(() => {
          alert('개인정보가 성공적으로 수정되었습니다.');
          router.push('/profile');
        }, 500);

      } catch (error) {
        console.error('Failed to update profile:', error);
      }
    };

    onMounted(fetchUserProfile);

    return {
      userInfo,
      defaultProfileImage,
      onFileChange,
      submitForm
    }
  }
}
</script>