<template>
  <header>
    <div class="container header-content">
      <a href="/" class="logo">Hot Item Collector</a>
      <div class="search-bar">
        <select v-model="searchType">
          <option value="product">상품명</option>
          <option value="seller">판매자명</option>
        </select>
        <input type="text" v-model="searchQuery" placeholder="검색어를 입력하세요">
        <button @click="search">검색</button>
      </div>
      <div class="user-actions">
        <template v-if="isLoggedIn">
          <div class="dropdown">
            <button>상품</button>
            <div class="dropdown-content">
              <a href="#" @click="goToProductRegistration">상품 등록</a>
              <a href="#" @click="goToProductManagement">판매 물품 관리</a>
              <a href="#" @click="goToOrderManagement">주문 관리</a>
            </div>
          </div>
          <div class="dropdown">
            <button>내정보</button>
            <div class="dropdown-content">
              <a href="#" @click="viewMyInfo">내정보 보기</a>
              <a href="#" @click="editProfile">정보 수정</a>
              <a href="#" @click="deleteAccount">회원 탈퇴</a>
              <a href="#" @click="logout">로그아웃</a>
              <a href="#" @click="showChangePasswordModal = true">비밀번호 변경</a>

            </div>
          </div>
          <button @click="goToCart">장바구니</button>
        </template>
        <template v-else>
          <button @click="showLoginModal = true">로그인</button>
          <button @click="showSignupModal = true">회원가입</button>
        </template>
      </div>
    </div>
  </header>
  <nav class="categories">
    <div class="container">
      <div class="categories-container">
        <a v-for="category in categories" :key="category" @click="searchByCategory(category)" class="category-item">{{ category }}</a>
      </div>
    </div>
  </nav>
  <!-- 회원가입 모달 -->
  <div v-if="showSignupModal" class="modal-overlay" @click.self="showSignupModal = false">
    <div class="modal-container">
      <button class="close-btn" @click="showSignupModal = false">&times;</button>
      <h1>회원가입</h1>
      <form @submit.prevent="register">
        <div class="form-group">
          <label for="auth-signupLoginId">아이디</label>
          <input type="text" id="auth-signupLoginId" v-model="signupLoginId" @input="validateLoginId" required>
          <div class="error" v-if="loginIdError">{{ loginIdError }}</div>
        </div>
        <div class="form-group">
          <label for="auth-signupPassword">비밀번호</label>
          <input type="password" id="auth-signupPassword" v-model="signupPassword" @input="validatePassword" required>
          <div class="error" v-if="passwordError">{{ passwordError }}</div>
        </div>
        <div class="form-group">
          <label for="auth-username">이름</label>
          <input type="text" id="auth-username" v-model="username" required>
        </div>
        <div class="form-group">
          <label for="auth-nickname">닉네임</label>
          <input type="text" id="auth-nickname" v-model="nickname" required>
          <div class="error" v-if="nicknameError">{{ nicknameError }}</div>
        </div>
        <button type="submit" :disabled="!isSignupFormValid">회원가입</button>
      </form>
      <div class="social-login">
        <div class="social-login-divider">
          <span>또는</span>
        </div>
        <button @click="kakaoLogin" class="kakao-login-btn">카카오톡으로 회원가입</button>
      </div>
      <div class="login-link">
        이미 계정이 있으신가요? <a @click="switchToLogin">로그인</a>
      </div>
    </div>
  </div>

  <!-- 로그인 모달 -->
  <div v-if="showLoginModal" class="modal-overlay" @click.self="showLoginModal = false">
    <div class="modal-container">
      <button class="close-btn" @click="showLoginModal = false">&times;</button>
      <h1>로그인</h1>
      <form @submit.prevent="login">
        <div class="form-group">
          <label for="auth-loginId">아이디</label>
          <input type="text" id="auth-loginId" v-model="loginId" required>
        </div>
        <div class="form-group">
          <label for="auth-password">비밀번호</label>
          <input type="password" id="auth-password" v-model="password" required>
        </div>
        <button type="submit">로그인</button>
        <div v-if="loginError" class="error">{{ loginError }}</div>
      </form>
      <div class="social-login">
        <div class="social-login-divider">
          <span>또는</span>
        </div>
        <button @click="kakaoLogin" class="kakao-login-btn">카카오톡으로 로그인</button>
      </div>
      <div class="signup-link">
        계정이 없으신가요? <a @click="switchToSignup">회원가입</a>
      </div>
    </div>
  </div>
  <!-- 비밀번호 변경 모달 -->
  <div v-if="showChangePasswordModal" class="modal-overlay" @click.self="showChangePasswordModal = false">
    <div class="modal-container">
      <button class="close-btn" @click="showChangePasswordModal = false">&times;</button>
      <h1>비밀번호 변경</h1>
      <form @submit.prevent="changePassword">
        <div class="form-group">
          <label for="auth-currentPassword">현재 비밀번호</label>
          <input type="password" id="auth-currentPassword" v-model="currentPassword" required>
        </div>
        <div class="form-group">
          <label for="auth-newPassword">새 비밀번호</label>
          <input type="password" id="auth-newPassword" v-model="newPassword" @input="validateNewPassword" required>
          <div class="error" v-if="newPasswordError">{{ newPasswordError }}</div>
        </div>
        <div class="form-group">
          <label for="auth-confirmNewPassword">새 비밀번호 확인</label>
          <input type="password" id="auth-confirmNewPassword" v-model="confirmNewPassword" @input="validateConfirmNewPassword" required>
          <div class="error" v-if="confirmNewPasswordError">{{ confirmNewPasswordError }}</div>
        </div>
        <button type="submit" :disabled="!isChangePasswordFormValid">비밀번호 변경</button>
        <div v-if="changePasswordError" class="error">{{ changePasswordError }}</div>
        <div v-if="changePasswordSuccess" class="success">{{ changePasswordSuccess }}</div>
      </form>
    </div>
  </div>

</template>

<script setup>
import { ref, computed, onMounted  } from 'vue';
import Cookies from 'js-cookie';
import router from "@/router";

const client = require('../client')

const searchType = ref('product');
const searchQuery = ref('');
const isLoggedIn = ref(false);
const categories = ref(['식품', '뷰티', '패션&주얼리', '공예품', '홈리빙', '반려동물']);
const showChangePasswordModal = ref(false);

const showLoginModal = ref(false);
const showSignupModal = ref(false);
const signupLoginId = ref('');
const signupPassword = ref('');
const loginId = ref('');
const password = ref('');
const username = ref('');
const nickname = ref('');
const loginIdError = ref('');
const passwordError = ref('');
const nicknameError = ref('');
const loginError = ref('');
const isSignupFormValid = computed(() => !loginIdError.value && !passwordError.value && signupLoginId.value && signupPassword.value && username.value && nickname.value);
// 비밀번호 변경 관련 상태
const currentPassword = ref('');
const newPassword = ref('');
const confirmNewPassword = ref('');
const newPasswordError = ref('');
const confirmNewPasswordError = ref('');
const changePasswordError = ref('');
const changePasswordSuccess = ref('');
const isChangePasswordFormValid = computed(() =>
    currentPassword.value &&
    newPassword.value &&
    confirmNewPassword.value &&
    !newPasswordError.value &&
    !confirmNewPasswordError.value
);

async function getrefreshToken() {
  const refreshToken = Cookies.get('refresh_token');

  if (!refreshToken) {
    console.error('Refresh token not found');
    logout();
    return;
  }

  try {
    const response = await client.post('/users/refresh', {
      refresh: refreshToken
    }, {
      headers: {
        'Content-Type': 'application/json'
      }
    });

    // 성공적으로 새로운 토큰을 받은 경우
    const { access, refresh } = response.data.result;

    // 새로운 토큰을 쿠키에 저장
    Cookies.set('access_token', access, { expires: 1 });
    Cookies.set('refresh_token', refresh, { expires: 7 });

    // 로그인 상태 업데이트
    isLoggedIn.value = true;

    console.log('Access token refreshed successfully');
  } catch (error) {
    console.error('Failed to refresh token:', error.response.data);
    // refresh token이 만료되었거나 잘못된 경우, 로그아웃 처리를 할 수 있습니다.
    logout();
  }
}

// Check login status on mount
onMounted(async () => {
  const accessToken = Cookies.get('access_token');
  const refreshToken = Cookies.get('refresh_token');

  if (accessToken) {
    // 토큰이 유효한지 확인 (서버로 검증 요청을 보내거나 클라이언트에서 간단히 검사)
    // 예를 들어, accessToken이 유효한 경우
    isLoggedIn.value = true;
  } else if (refreshToken) {
    // accessToken이 없는 경우 refreshToken으로 accessToken 재발급
    await getrefreshToken();
  } else {
    isLoggedIn.value = false;
  }
});





function goToProductRegistration() {
  // 상품 등록 함수 구현
  router.push('/product/upload');


}

function goToProductManagement() {
  // 판매 물품 관리 함수 구현
  router.push('/product/sale');
}

function goToOrderManagement() {
  // 주문 관리 함수 구현
  router.push('/orders/sell');
}

function viewMyInfo() {
  // 내정보 보기 함수 구현
  router.push('/profile');
}

function editProfile() {
  // 정보 수정 함수 구현
  router.push('/profile/update/password');
}



async function logout() {
  const accessToken = Cookies.get('access_token');
  const refreshToken = Cookies.get('refresh_token');

  if (!refreshToken) {
    Cookies.remove('access_token');
    Cookies.remove('refresh_token');
    isLoggedIn.value = false;
    return;
  }


  try {
    // Send logout request to server
    const response = await client.post('/users/logout', {}, {
      headers: {
        'Authorization': accessToken
      }
    });

    if (response.status === 200) {
      // Clear tokens from cookies
      Cookies.remove('access_token');
      Cookies.remove('refresh_token');
      alert('로그아웃 성공');

      // Update login state
      isLoggedIn.value = false;

      console.log('로그아웃 성공');
      window.location.reload()
      router.push('/');
    } else {
      console.error('로그아웃 실패:', response.data);
    }
  } catch (error) {
    console.error('로그아웃 요청 실패:', error.response ? error.response.data : error.message);
  }
}


async function deleteAccount() {
  // First confirmation dialog
  if (confirm('정말로 회원 탈퇴를 하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
    // Second confirmation dialog
    if (confirm('정말로 탈퇴하시겠습니까? 이 작업을 계속 진행하면 회원 정보가 삭제됩니다.')) {
      try {
        const accessToken = Cookies.get('access_token');
        console.log(accessToken);

      const response = await client.patch('/users/withdraw', {}, {
        headers: {
          'Authorization': accessToken
        }
      });
          console.log(response.data);

        if (response.status === 200) {
          // Clear tokens from cookies
          Cookies.remove('access_token');
          Cookies.remove('refresh_token');

          alert('회원 탈퇴가 완료되었습니다.');
          console.log('회원 탈퇴 성공:', response.data);

          // Update login state
          isLoggedIn.value = false;

          window.location.reload()
          // Optionally redirect to the home page or login page
          router.push('/');
        } else {
          console.error('회원 탈퇴 실패:', response.data);
          alert('회원 탈퇴에 실패하였습니다.');
        }
      } catch (error) {
        console.error('회원 탈퇴 요청 실패:', error.response ? error.response.data : error.message);
        alert('회원 탈퇴 중 오류가 발생했습니다.');
      }
    }
  }
}

function goToCart() {
  // 장바구니 이동 함수 구현
  router.push('/cart');
}

async function register() {
  try {
    const response = await client.post('/users/signup', {
      loginId: signupLoginId.value,
      password: signupPassword.value,
      username: username.value,
      nickname: nickname.value
    }, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
    alert('회원가입 성공');

    setTimeout(() => {
      showSignupModal.value = false;
    }, 500);

    console.log('회원가입 성공:', response.data);

    // 추가적으로 회원가입 성공 시 수행할 작업을 여기에 추가합니다.
    window.location.reload()
  } catch (error) {
    console.error('회원가입 실패:', error.response.data);
    // 서버 응답에서 에러 메시지를 처리
    if (error.response && error.response.data) {
      const { error: errorType, message } = error.response.data;
      if (errorType === 'Conflict') {
        if (message.includes('아이디')) {
          loginIdError.value = message;
        } else if (message.includes('닉네임')) {
          nicknameError.value = message;
        }
      }
    }
  }
}

async function login() {
  // 로그인 로직 구현
  {
    try {
      const response = await client.post('/users/login', {
        loginId: loginId.value,
        password: password.value,
      }, {
        headers: {
          'Content-Type': 'application/json'
        }
      });
      console.log('로그인 성공:', response.data);

      // 쿠키에 토큰 저장
      const {access, refresh} = response.data.result;
      Cookies.set('access_token', access, {expires: 1}); // access_token을 1일 동안 쿠키에 저장
      Cookies.set('refresh_token', refresh, {expires: 7}); // refresh_token을 7일 동안 쿠키에 저장


      // 로그인 성공 처리
      isLoggedIn.value = true;
      showLoginModal.value = false;
      // 로그인 후 추가 작업이 있다면 여기에 추가
      window.location.reload()
    } catch (error) {
      console.error('로그인 실패:', error.response.data);
      loginError.value = '로그인에 실패하였습니다.';
    }
  }
}

function validateLoginId() {
  const loginIdRegex = /^[a-z0-9]{4,10}$/
  if (!loginIdRegex.test(signupLoginId.value)) {
    loginIdError.value = '아이디는 4~10자의 영문 소문자와 숫자만 사용 가능합니다.'
  } else {
    loginIdError.value = ''
  }
}

function validatePassword() {
  const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,15}$/
  if (!passwordRegex.test(signupPassword.value)) {
    passwordError.value = '비밀번호는 8~15자의 영문 대/소문자, 숫자, 특수문자를 포함해야 합니다.'
  } else {
    passwordError.value = ''
  }
}

function switchToSignup() {
  showLoginModal.value = false;
  showSignupModal.value = true;
}

function switchToLogin() {
  showSignupModal.value = false;
  showLoginModal.value = true;
}

function kakaoLogin() {
  // 카카오 로그인 로직 구현
}

function validateNewPassword() {
  const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,15}$/
  if (!passwordRegex.test(newPassword.value)) {
    newPasswordError.value = '비밀번호는 8~15자의 영문 대/소문자, 숫자, 특수문자를 포함해야 합니다.'
  } else {
    newPasswordError.value = ''
  }
  validateConfirmNewPassword();
}

function validateConfirmNewPassword() {
  if (newPassword.value !== confirmNewPassword.value) {
    confirmNewPasswordError.value = '새 비밀번호가 일치하지 않습니다.'
  } else {
    confirmNewPasswordError.value = ''
  }
}

async function changePassword() {
  try {
    const accessToken = Cookies.get('access_token');
    const response = await client.patch('/users/password', {
      oldPassword: currentPassword.value,
      newPassword: newPassword.value,
    }, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': accessToken
      }
    });
    console.log('비밀번호 변경 성공:', response.data);
    alert('비밀번호가 성공적으로 변경되었습니다.');
    changePasswordSuccess.value = '비밀번호가 성공적으로 변경되었습니다.';
    changePasswordError.value = '';
    // 비밀번호 변경 성공 후 모달 닫기
    setTimeout(() => {
      showChangePasswordModal.value = false;
      // 입력 필드 초기화
      currentPassword.value = '';
      newPassword.value = '';
      confirmNewPassword.value = '';
      changePasswordSuccess.value = '';
    }, 2000);
  } catch (error) {
    console.error('비밀번호 변경 실패:', error.response.data);
    changePasswordError.value = '비밀번호 변경에 실패했습니다. 현재 비밀번호를 확인해주세요.';
    changePasswordSuccess.value = '';
  }
}
// searchByCategory function
const searchByCategory = (category) => {
  console.log(category);
  router.push({
    name: 'CategoryItemPage',
    query: { searchQuery: category }
  }).then(() => {
    // 강제 새로고침
    window.location.reload();
  }).catch(err => {
    console.error('Routing error:', err);
  });
};

// search function
const search = () => {

  router.push({
    name: 'SearchPage',
    query: { searchType: searchType.value, searchQuery: searchQuery.value }
  }).then(() => {
    // 강제 새로고침
    window.location.reload();
  }).catch(err => {
    console.error('Routing error:', err);
  });
};


</script>

<style>
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


/* Dropdown Menu Styles */
.dropdown {
  position: relative;
  display: inline-block;
}

.dropdown-content {
  display: none;
  position: absolute;
  right: 0;
  background-color: var(--bg-color);
  min-width: 160px;
  box-shadow: 0px 8px 16px 0px rgba(0, 0, 0, 0.2);
  z-index: 1;
  border-radius: 5px;
}

.dropdown-content a {
  color: var(--text-color);
  padding: 12px 16px;
  text-decoration: none;
  display: block;
  transition: background-color 0.3s ease;
}

.dropdown-content a:hover {
  background-color: var(--hover-color);
  color: var(--bg-color);
}

.dropdown:hover .dropdown-content {
  display: block;
}
</style>