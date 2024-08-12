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
          <button @click="showLoginModal">로그인</button>
          <button @click="showSignupModal">회원가입</button>
        </template>
      </div>
    </div>
  </header>
  <nav class="categories">
    <div class="container">
      <div class="categories-container">
        <a v-for="category in categories" :key="category" @click="searchByCategory(category)"
           class="category-item">{{ category }}</a>
      </div>
    </div>
  </nav>
  <!-- 회원가입 모달 -->
  <!--  <div v-if="showSignupModal" class="modal-overlay" @click.self="showSignupModal = false">-->
  <!--    <div class="modal-container">-->
  <!--      <button class="close-btn" @click="showSignupModal = false">&times;</button>-->
  <!--      <h1>회원가입</h1>-->
  <!--      <form @submit.prevent="register">-->
  <!--        <div class="form-group">-->
  <!--          <label for="auth-signupLoginId">아이디</label>-->
  <!--          <input type="text" id="auth-signupLoginId" v-model="signupLoginId" @input="validateLoginId" required>-->
  <!--          <div class="error" v-if="loginIdError">{{ loginIdError }}</div>-->
  <!--        </div>-->
  <!--        <div class="form-group">-->
  <!--          <label for="auth-signupPassword">비밀번호</label>-->
  <!--          <input type="password" id="auth-signupPassword" v-model="signupPassword" @input="validatePassword" required>-->
  <!--          <div class="error" v-if="passwordError">{{ passwordError }}</div>-->
  <!--        </div>-->
  <!--        <div class="form-group">-->
  <!--          <label for="auth-username">이름</label>-->
  <!--          <input type="text" id="auth-username" v-model="username" required>-->
  <!--        </div>-->
  <!--        <div class="form-group">-->
  <!--          <label for="auth-nickname">닉네임</label>-->
  <!--          <input type="text" id="auth-nickname" v-model="nickname" required>-->
  <!--          <div class="error" v-if="nicknameError">{{ nicknameError }}</div>-->
  <!--        </div>-->
  <!--        <button type="submit" :disabled="!isSignupFormValid">회원가입</button>-->
  <!--      </form>-->
  <!--      <div class="social-login">-->
  <!--        <div class="social-login-divider">-->
  <!--          <span>또는</span>-->
  <!--        </div>-->
  <!--        <button @click="kakaoLogin" class="kakao-login-btn">카카오톡으로 회원가입</button>-->
  <!--      </div>-->
  <!--      <div class="login-link">-->
  <!--        이미 계정이 있으신가요? <a @click="switchToLogin">로그인</a>-->
  <!--      </div>-->
  <!--    </div>-->
  <!--  </div>-->

  <div class="modal" :class="{ active: showModal }">
    <div class="modal-content">
      <button class="close-button" @click="closeModal">&times;</button>
      <h2 class="modalTitle">{{ modalTitle }}</h2>
      <form @submit.prevent="auth">
        <div v-if="isNewUser">
          <div class="form-group">
            <label for="username">이름</label>
            <input type="text" id="username" v-model="username" required>
          </div>
          <div class="form-group">
            <label for="loginId">아이디</label>
            <input type="text" id="loginId" v-model="loginId" required @input="validateLoginId">
          </div>

          <div class="form-group">
            <label for="email">이메일</label>
            <div class="email-verification">
              <div class="email-input-container">
                <input type="email" id="email" v-model="email" required @input="validateEmail">
                <span v-if="isEmailVerified" class="email-status-icon success">&#10004;</span>
                <span v-if="verificationError" class="email-status-icon failure">&#10008;</span>
              </div>
              <button type="button" id="verifiationbutton" @click="sendVerificationCode"
                      :disabled="!isEmailValid || isEmailVerified || isSendingCode|| timer > 0">
                <span v-if="isSendingCode" class="loading-spinner"></span>
                {{ verificationButtonText }}
              </button>
            </div>
            <p class="error" v-if="emailError">{{ emailError }}</p>
            <div v-if="showVerificationCode" class="verification-code">
              <input type="text" v-model="verificationCode" placeholder="인증 코드 입력">
              <button type="button" @click="verifyCode">인증하기</button>
            </div>
            <p class="timer" v-if="timer > 0">
              남은 시간: {{ formatTime(timer) }}
            </p>
            <p class="verification-status" :class="{ success: isEmailVerified, failure: verificationError }">
              {{ verificationStatus }}
            </p>
          </div>

          <div class="form-group">
            <label for="password">비밀번호</label>
            <input type="password" id="password" v-model="password" required @input="validatePassword">
            <p class="error" v-if="passwordError">{{ passwordError }}</p>
          </div>

          <div class="form-group">
            <label for="confirmPassword">비밀번호 확인</label>
            <input type="password" id="confirmPassword" v-model="confirmPassword" required
                   @input="validateConfirmPassword">
            <p class="error" v-if="confirmPasswordError">{{ confirmPasswordError }}</p>
          </div>

        </div>

        <div v-else>
          <div class="form-group">
            <label for="loginId">아이디</label>
            <input type="text" id="loginId" v-model="loginId" required>
            <p class="error" v-if="emailError">{{ emailError }}</p>
          </div>

          <div class="form-group">
            <label for="password">비밀번호</label>
            <input type="password" id="password" v-model="password" required>
            <p class="error" v-if="passwordError">{{ passwordError }}</p>
          </div>
        </div>

        <button type="submit" class="submit-button" :disabled="!isFormValid">{{ submitButtonText }}</button>
        <div v-if="isNewUser">
          <div class="sc-b82c0cba-2 DwtdQ">
            <div class="sc-jXUnUj bMUZyR">
              <div class="sc-kXWJUi gIRbED">
              </div></div><div class="sc-b82c0cba-4 faerXM">
            <div class="sc-grBnJl dBXZuI">
              <span class="sc-hrDJJk bReByP">다른 방법으로 가입하기</span>
            </div>
          </div>
          </div>
        </div>
        <div v-else>
          <div class="sc-b82c0cba-2 DwtdQ">
            <div class="sc-jXUnUj bMUZyR">
              <div class="sc-kXWJUi gIRbED">
              </div></div><div class="sc-b82c0cba-4 faerXM">
            <div class="sc-grBnJl dBXZuI">
              <span class="sc-hrDJJk bReByP">다른 방법으로 로그인하기</span>
            </div>
          </div>
          </div>
        </div>
        <div class="social-buttons">
          <button class="social-button kakao-button" @click="socialLogin('kakao')">
            <img src="@/assets/kakao.png" alt="대체 텍스트">
          </button>
          <button class="social-button google-button" @click="socialLogin('google')">
            <img src="@/assets/google.png" alt="대체 텍스트">
          </button>
        </div>
      </form>
      <p class="error" v-if="error">{{ error }}</p>
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
          <input type="password" id="auth-confirmNewPassword" v-model="confirmNewPassword"
                 @input="validateConfirmNewPassword" required>
          <div class="error" v-if="confirmNewPasswordError">{{ confirmNewPasswordError }}</div>
        </div>
        <button type="submit" :disabled="!isChangePasswordFormValid">비밀번호 변경</button>
        <div v-if="changePasswordError" class="error">{{ changePasswordError }}</div>
        <div v-if="changePasswordSuccess" class="success">{{ changePasswordSuccess }}</div>
      </form>
    </div>
  </div>

</template>

<script>
import Cookies from "js-cookie";
const client = require('../client')

export default {

  data() {
    return {
      showModal: false,
      isNewUser: false,
      signupModal: false,
      searchType: 'product',
      searchQuery: '',
      isLoggedIn: false,
      categories: ['식품', '뷰티', '패션&주얼리', '공예품', '홈리빙', '반려동물'],
      showChangePasswordModal: false,
      email: '',
      loginId: '',
      timer: 0,
      password: '',
      error: '',
      username: '',
      nickname: '',
      confirmPassword: '',
      confirmPasswordError: '',
      isEmailVerified: false,
      loginIdError: '',
      passwordError: '',
      nicknameError: '',
      modalTitle: '',
      loginError: '',
      emailError: '',
      verificationError: '',
      verificationButtonText: '인증 코드 발송',
      verificationStatus: '',
      currentPassword: '',
      newPassword: '',
      confirmNewPassword: '',
      newPasswordError: '',
      confirmNewPasswordError: '',
      changePasswordError: '',
      showVerificationCode: false,
      submitButtonText: '',
      verificationCode: '',
      changePasswordSuccess: '',
      isSendingCode: false
    };
  },

  computed: {

    isFormValid() {
      if (this.isNewUser) {
        return !this.emailError && !this.passwordError && !this.confirmPasswordError &&
            this.username && this.email && this.password && this.confirmPassword && this.isEmailVerified;
      } else {
        return this.loginId && this.password;
      }
    },
    isEmailValid() {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      return emailRegex.test(this.email);
    },
    isChangePasswordFormValid() {
      return this.currentPassword && this.newPassword && this.confirmNewPassword && !this.newPasswordError && !this.confirmNewPasswordError;
    }
  },
  async created() {
    const urlParams = new URLSearchParams(window.location.search);
    const newAccessToken = urlParams.get('access');
    const newRefreshToken = urlParams.get('refresh');
    if (newAccessToken && newRefreshToken) {
      Cookies.set('access_token', newAccessToken, {expires: 1});
      Cookies.set('refresh_token', newRefreshToken, {expires: 7});
      await this.$router.push('/'); // Redirect to home or desired page
    }

    const accessToken = Cookies.get('access_token');
    const refreshToken = Cookies.get('refresh_token');

    if (accessToken) {
      this.isLoggedIn = true;
    } else if (refreshToken) {
      await this.getRefreshToken();
    } else {
      this.isLoggedIn = false;
    }
  },
  methods: {


    showLoginModal() {
      this.isNewUser = false;
      this.modalTitle = '로그인';
      this.submitButtonText = '로그인';
      this.showModal = true;
    },
    showSignupModal() {
      this.isNewUser = true;
      this.modalTitle = '회원가입';
      this.submitButtonText = '회원가입';
      this.showModal = true;
    },
    closeModal() {
      this.showModal = false;
      this.resetForm();
    },
    validateEmail() {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      this.emailError = emailRegex.test(this.email) ? '' : '유효한 이메일 주소를 입력해주세요.';
    },

    search() {
      this.$router.push({
        name: 'SearchPage',
        query: {searchType: this.searchType, searchQuery: this.searchQuery}
      }).then(() => {
        // 강제 새로고침
        window.location.reload();
      }).catch(err => {
        console.error('Routing error:', err);
      });
    },
    searchByCategory(category) {
      console.log(category);
      this.$router.push({
        name: 'CategoryItemPage',
        query: {searchQuery: category}
      }).then(() => {
        // 강제 새로고침
        window.location.reload();
      }).catch(err => {
        console.error('Routing error:', err);
      });
    },


    async getRefreshToken() {
      const refreshToken = Cookies.get('refresh_token');

      if (!refreshToken) {
        console.error('Refresh token not found');
        this.logout();
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

        const {access, refresh} = response.data.result;
        Cookies.set('access_token', access, {expires: 1});
        Cookies.set('refresh_token', refresh, {expires: 7});
        this.isLoggedIn = true;
        console.log('Access token refreshed successfully');
      } catch (error) {
        console.error('Failed to refresh token:', error.response.data);
        this.logout();
      }
    },
    async logout() {
      const accessToken = Cookies.get('access_token');
      const refreshToken = Cookies.get('refresh_token');

      if (!refreshToken) {
        Cookies.remove('access_token');
        this.isLoggedIn = false;
        return;
      }

      try {
        const response = await client.post('/users/logout', {}, {
          headers: {
            'Authorization': accessToken
          }
        });

        if (response.status === 200) {
          Cookies.remove('access_token');
          Cookies.remove('refresh_token');
          alert('로그아웃 성공');
          this.isLoggedIn = false;
          console.log('로그아웃 성공');
          await this.$router.push('/');
        } else {
          console.error('로그아웃 실패:', response.data);
        }
      } catch (error) {
        console.error('로그아웃 요청 실패:', error.response ? error.response.data : error.message);
      }
    },
    async deleteAccount() {
      if (confirm('정말로 회원 탈퇴를 하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
        if (confirm('정말로 탈퇴하시겠습니까? 이 작업을 계속 진행하면 회원 정보가 삭제됩니다.')) {
          try {
            const accessToken = Cookies.get('access_token');
            const response = await client.patch('/users/withdraw', {}, {
              headers: {
                'Authorization': accessToken
              }
            });

            if (response.status === 200) {
              Cookies.remove('access_token');
              Cookies.remove('refresh_token');
              alert('회원 탈퇴가 완료되었습니다.');
              console.log('회원 탈퇴 성공:', response.data);
              this.isLoggedIn = false;
              await this.$router.push('/');
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
    },
    goToProductRegistration() {
      this.$router.push('/product/upload');
    },
    goToProductManagement() {
      this.$router.push('/product/sale');
    },
    goToOrderManagement() {
      this.$router.push('/orders/sell');
    },
    viewMyInfo() {
      this.$router.push('/profile');
    },
    editProfile() {
      this.$router.push('/profile/update/password');
    },
    goToCart() {
      // 장바구니 이동 함수 구현
      this.$router.push('/cart');
    },


    async auth() {
      try {
        if (this.isNewUser) {
          // 회원가입 로직
          await this.signUp(); // 회원가입 처리 메서드 호출
        } else {
          // 로그인 로직
          await this.login(); // 로그인 처리 메서드 호출
        }
      } catch (error) {
        this.error = error.message;
      }
    },
    async signUp() {
      console.log(this.email);
      try {
        const response = await client.post('/users/signup', {
          loginId: this.loginId,
          password: this.password,
          username: this.username,
          nickname: this.nickname,
          email: this.email
        }, {
          headers: {
            'Content-Type': 'application/json'
          }
        });
        alert('회원가입 성공');
        this.showSignupModal = false;
        console.log('회원가입 성공:', response.data);
        window.location.reload();
      } catch (error) {
        console.error('회원가입 실패:', error.response.data);
        if (error.response && error.response.data) {
          const {error: errorType, message} = error.response.data;
          if (errorType === 'Conflict') {
            if (message.includes('아이디')) {
              this.loginIdError = message;
            } else if (message.includes('닉네임')) {
              this.nicknameError = message;
            }
          }
        }
      }
    }
    ,
    async login() {
      try {
        const response = await client.post('/users/login', {
          loginId: this.loginId,
          password: this.password
        }, {
          headers: {
            'Content-Type': 'application/json'
          }
        });

        const accessToken = response.headers['access'];
        const refreshToken = response.headers['refresh'];

        if (accessToken && refreshToken) {
          Cookies.set('access_token', accessToken, {expires: 1});
          Cookies.set('refresh_token', refreshToken, {expires: 7});
          this.isLoggedIn = true;
          this.showLoginModal = false;
          window.location.reload();
        } else {
          console.error('로그인 성공, 그러나 헤더에 토큰이 없습니다.');
          this.loginError = '로그인 성공, 그러나 헤더에 토큰이 없습니다.';
        }
      } catch (error) {
        console.error('로그인 실패:', error.response ? error.response.data : error.message);
        this.loginError = '로그인에 실패하였습니다.';
      }
    }
    ,
    validateLoginId() {
      const loginIdRegex = /^[a-z0-9]{4,10}$/;
      this.loginIdError = !loginIdRegex.test(this.loginId) ? '아이디는 4~10자의 영문 소문자와 숫자만 사용 가능합니다.' : '';
    }
    ,
    validatePassword() {
      const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,15}$/;
      this.passwordError = !passwordRegex.test(this.password) ? '비밀번호는 8~15자의 영문 대/소문자, 숫자, 특수문자를 포함해야 합니다.' : '';
    },
    validateConfirmPassword() {
      this.confirmPasswordError = this.password === this.confirmPassword ? '' : '비밀번호가 일치하지 않습니다.';
    },
    socialLogin(provider) {
      let url = 'http://hotitemcollector.com:8080';

      switch (provider) {
        case 'kakao':
          url = url+'/oauth2/authorization/kakao';
          break;
        case 'naver':
          url = url+'/oauth2/authorization/naver';
          break;
        case 'google':
          url = url+'/oauth2/authorization/google';
          break;
        default:
          console.error('Unsupported provider');
          return;
      }

      window.location.href = url;
    }
    ,
    validateNewPassword() {
      const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,15}$/;
      this.newPasswordError = !passwordRegex.test(this.newPassword) ? '비밀번호는 8~15자의 영문 대/소문자, 숫자, 특수문자를 포함해야 합니다.' : '';
      this.validateConfirmNewPassword();
    }
    ,
    validateConfirmNewPassword() {
      this.confirmNewPasswordError = this.newPassword !== this.confirmNewPassword ? '새 비밀번호가 일치하지 않습니다.' : '';
    }
    ,
    async changePassword() {
      try {
        const accessToken = Cookies.get('access_token');
        const response = await client.patch('/users/password', {
          oldPassword: this.currentPassword,
          newPassword: this.newPassword
        }, {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': accessToken
          }
        });

        if (response.status === 200) {
          this.changePasswordSuccess = '비밀번호 변경 성공';
          this.showChangePasswordModal = false;
          this.currentPassword = '';
          this.newPassword = '';
          this.confirmNewPassword = '';
          console.log('비밀번호 변경 성공:', response.data);
        } else {
          this.changePasswordError = '비밀번호 변경 실패';
          console.error('비밀번호 변경 실패:', response.data);
        }
      } catch (error) {
        this.changePasswordError = '비밀번호 변경 중 오류가 발생했습니다.';
        console.error('비밀번호 변경 요청 실패:', error.response ? error.response.data : error.message);
      }
    }
    ,
    resetForm() {
      this.loginId = '';
      this.email = '';
      this.password = '';
      this.confirmPassword = '';
      this.username = '';
      this.error = '';
      this.emailError = '';
      this.passwordError = '';
      this.confirmPasswordError = '';
      this.showVerificationCode = false;
      this.verificationCode = '';
      this.timer = 0;
      this.isEmailVerified = false;
      this.verificationError = '';
      this.verificationButtonText = '인증 코드 발송';
      this.verificationStatus = '';
      this.isSendingCode = false;
    },
    async sendVerificationCode() {
      if (this.timer > 0) return;
      try {
        this.isSendingCode = true;
        this.verificationButtonText = '전송 중...';
        const response = await client.post('/users/email', {email: this.email});
        console.log('인증 코드 발송:', response.data);
        this.showVerificationCode = true;
        this.startTimer();
        this.verificationButtonText = '코드 재전송';
        alert('인증 코드가 이메일로 발송되었습니다. 5분 이내에 입력해주세요.');
      } catch (error) {
        console.error('인증 코드 발송 실패:', error);
        this.error = '인증 코드 발송에 실패했습니다. 다시 시도해주세요.';
      } finally {
        this.isSendingCode = false;
      }
    },
    startTimer() {
      this.timer = 300;
      const interval = setInterval(() => {
        this.timer--;
        if (this.timer <= 0) {
          clearInterval(interval);
          this.showVerificationCode = false;
          this.verificationButtonText = '인증 코드 발송';
        }
      }, 1000);
    },
    formatTime(seconds) {
      const minutes = Math.floor(seconds / 60);
      const remainingSeconds = seconds % 60;
      return `${minutes}:${remainingSeconds < 10 ? '0' : ''}${remainingSeconds}`;
    },
    async verifyCode() {
      try {
        const response = await client.post('/users/email/validate', {
          email: this.email,
          authCode: this.verificationCode
        });
        console.log('인증 코드 확인:', response.data);
        this.isEmailVerified = true;
        this.showVerificationCode = false;
        this.verificationError = '';
        this.verificationButtonText = '인증 완료';
        this.verificationStatus = '인증 성공';
        alert('이메일이 성공적으로 인증되었습니다.');
      } catch (error) {
        console.error('인증 코드 확인 실패:', error);
        this.verificationError = '인증 코드가 올바르지 않습니다. 다시 시도해주세요.';
        this.verificationStatus = '인증 실패';
        this.isEmailVerified = false;
      }
    }
  }
}
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
.modalTitle {
  text-align: center;
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

.social-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 20px
}

.social-button {
  border: none;
  background: none;
  padding: 0;
  cursor: pointer;
}

.social-button img {
  display: block;
  border: 1px solid #e1e1e1; /* 이미지에 적용된 테두리 제거 */
  outline: none; /* 포커스 시 테두리 제거 */
  border-radius: 10%
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
.DwtdQ {
  position: relative;
  width: 100%;
  margin-top: 22px;
}
.bMUZyR {
  width: 100%;
  padding: 8px 0px;
}
.gIRbED {
  width: 100%;
  border-bottom: 1px solid rgba(90, 101, 119, 0.15);
}
.faerXM {
  position: absolute;
  top: 50%;
  left: 50%;
  padding: 0px 12px;
  transform: translate(-50%, -50%);
  background-color: rgb(255, 255, 255);
}
.dBXZuI {
  display: flex;
  flex-flow: row;
  gap: 4px;
  max-width: fit-content;
  align-items: center;
}
.bReByP {
  color: #333;
  font: 500 0.875rem / 1.46 "Pretendard Variable", Figtree, "IBM Plex Sans JP", "Pretendard JP Variable";
}
/* .search-bar 내부의 input 스타일 */
.search-bar input {
  padding: 10px;
  margin-top: 0;
  font-size: 16px;
  border: none;
  flex-grow: 1;
  min-width: 200px;
  border-radius: 0; /* 검색바의 다른 input과 차별화된 스타일 */
  width: 0;
}

/* .search-bar 내부의 select 스타일 */
.search-bar select {
  padding: 10px;
  font-size: 16px;
  border: none;
  border-radius: 5px 0 0 5px; /* 검색바의 select 요소만 해당 스타일 적용 */
}

/* .search-bar 내부의 button 스타일 */
.search-bar button {
  padding: 10px;
  font-size: 16px;
  border: none;
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

/* modal */

.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s ease;
}

.modal.active {
  opacity: 1;
  visibility: visible;
}

.modal-content {
  background-color: white;
  padding: 3rem;
  border-radius: 12px;
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.2);
  width: 450px;
  position: relative;
  transform: scale(0.8);
  transition: all 0.3s ease;
  max-height: 90vh;
  overflow-y: auto;
}

.modal.active .modal-content {
  transform: scale(1);
}

.close-button {
  position: absolute;
  top: 15px;
  right: 15px;
  font-size: 24px;
  background: none;
  border: none;
  cursor: pointer;
  color: #999;
  transition: all 0.2s ease;
  width: 30px;
  height: 30px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 50%;
  padding: 0;
}

.close-button:hover {
  color: var(--main-color);
  background-color: #f0f0f0;
}

form {
  display: flex;
  flex-direction: column;
}

label {
  margin-top: 1.5rem;
  font-weight: 600;
  text-align: left;
  color: #333;
}

/* 전역 input 스타일 (검색바 외의 input에 적용) */
input {
  padding: 0.75rem;
  margin-top: 8px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s ease;
  width: 100%;
  box-sizing: border-box;
}

input:focus {
  outline: none;
  border-color: var(--main-color);
}

.error {
  color: #ff3333;
  margin-top: 0.75rem;
  font-size: 0.9rem;
  text-align: left;
}

.form-group {
  margin-bottom: 1.75rem;
}

.submit-button {
  margin-top: 2rem;
  background-color: var(--main-color);
  color: white;
  border: none;
  padding: 1rem;
  border-radius: 8px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  width: 100%;
}

.submit-button:hover {
  background-color: var(--main-color);
  transform: translateY(-2px);
  box-shadow: 0 4px 6px rgba(255, 102, 0, 0.2);
}

h2 {

  color: var(--main-color);
  font-size: 2rem;
  margin-bottom: 2rem;
  font-weight: 700;
}

.email-verification {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.email-verification input {
  flex: 1;
  min-width: 200px;
}

.email-verification button,
.verification-code button {
  background-color: var(--main-color);
  margin-top: 8px;
  border: none;
  color: white;
  padding: 0.75rem 1rem;
  font-size: 0.9rem;
  white-space: nowrap;
  flex-shrink: 0;
  border-radius: 50px;
}

.verification-code {
  margin-top: 1rem;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.verification-code input {
  flex: 0 1 150px;
  min-width: 100px;
  max-width: 150px;
}

.timer {
  font-size: 0.9rem;
  color: var(--main-color);
  margin-top: 0.5rem;
  width: 100%;
  text-align: right;
}

.resend-code {
  background: none;
  border: none;
  color: var(--main-color);
  cursor: pointer;
  font-size: 0.9rem;
  text-decoration: underline;
  padding: 0;
  margin: 0;
}

.resend-code:hover {
  color: var(--hover-color);
}

.verification-status {
  margin-top: 0.5rem;
  font-weight: bold;
}

.verification-status.success {
  color: #4CAF50;
}

.verification-status.failure {
  color: #f44336;
}

.email-input-container {
  position: relative;
  flex: 1;
}

.email-status-icon {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 1.2rem;
}

.email-status-icon.success {
  color: #4CAF50;
}

.email-status-icon.failure {
  color: #f44336;
}

.loading-spinner {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 2px solid #f3f3f3;
  border-top: 2px solid var(--main-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-right: 10px;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
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