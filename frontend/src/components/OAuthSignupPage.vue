<template>
  <div id="app">
    <div class="container">
      <h1>Hot Item Collector</h1>
      <p class="info-text">
        소셜 로그인이 성공적으로 완료되었습니다! 환영합니다.<br><br>
        서비스를 더욱 효과적으로 이용하기 위해 추가 정보가 필요합니다. 아래 두 가지 옵션 중 하나를 선택해 주세요:<br><br>
        1. 기존 계정 연결하기: 이미 Hot Item Collector 계정이 있다면 이 옵션을 선택하세요. 소셜 로그인과 기존 계정을 연동할 수 있습니다.<br>
        2. 새 계정 만들기: Hot Item Collector를 처음 이용하신다면 이 옵션을 선택하여 새 계정을 만들어주세요.<br><br>
        선택하신 옵션에 따라 간단한 추가 정보를 입력하시면 곧 Hot Item Collector의 모든 기능을 이용하실 수 있습니다!
      </p>
      <button @click="showExistingUserModal">기존 계정 연결하기</button>
      <button @click="showNewUserModal">새 계정 만들기</button>
    </div>

    <div class="modal" :class="{ active: showModal }">
      <div class="modal-content">
        <button class="close-button" @click="closeModal">&times;</button>
        <h2>{{ modalTitle }}</h2>
        <form @submit.prevent="submitForm">
          <div v-if="isNewUser">
            <div class="form-group">
              <label for="username">이름</label>
              <input type="text" id="username" v-model="username" required>
            </div>
            <div class="form-group">
              <label for="loginId">아이디</label>
              <input type="text" id="loginId" v-model="loginId" required>
            </div>

            <div class="form-group">
              <label for="email">이메일</label>
              <div class="email-verification">
                <div class="email-input-container">
                  <input type="email" id="email" v-model="email" required @input="validateEmail">
                  <span v-if="isEmailVerified" class="email-status-icon success">&#10004;</span>
                  <span v-if="verificationError" class="email-status-icon failure">&#10008;</span>
                </div>
                <button type="button" @click="sendVerificationCode" :disabled="!isEmailValid || isEmailVerified || isSendingCode|| timer > 0">
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
        </form>
        <p class="error" v-if="error">{{ error }}</p>
      </div>
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  data() {
    return {
      showModal: false,
      isNewUser: false,
      loginId: '',
      email: '',
      password: '',
      confirmPassword: '',
      username: '',
      error: '',
      emailError: '',
      passwordError: '',
      confirmPasswordError: '',
      modalTitle: '',
      submitButtonText: '',
      showVerificationCode: false,
      verificationCode: '',
      timer: 0,
      isEmailVerified: false,
      isSendingCode: false,
      verificationError: '',
      verificationButtonText: '인증 코드 발송',
      verificationStatus: '',
      oauthId: '', // Add property for query parameter
      socialId: '' // Add property for query parameter
    };
  },
  created() {
    // Access query parameters using this.$route.query
    this.oauthId = this.$route.query.oauthId || '';
    this.socialId = this.$route.query.socialId || '';
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
    }
  },
  methods: {
    showExistingUserModal() {
      this.isNewUser = false;
      this.modalTitle = '기존 계정 연결';
      this.submitButtonText = '연결하기';
      this.showModal = true;
    },
    showNewUserModal() {
      this.isNewUser = true;
      this.modalTitle = '새 계정 만들기';
      this.submitButtonText = '계정 생성';
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
    validatePassword() {
      const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,15}$/;
      this.passwordError = passwordRegex.test(this.password) ? '' : '비밀번호는 8~15자의 영문 대/소문자, 숫자, 특수문자를 포함해야 합니다.';
    },
    validateConfirmPassword() {
      this.confirmPasswordError = this.password === this.confirmPassword ? '' : '비밀번호가 일치하지 않습니다.';
    },
    async submitForm() {
      try {
        let response;
        if (this.isNewUser) {
          const response = await axios.post('http://localhost:8080/users/oauth/signup', {
            oauthId: this.oauthId,
            socialId: this.socialId,
            loginId: this.loginId,
            username: this.username,
            email: this.email,
            password: this.password
          });
          console.log('신규 사용자 등록:', response.data);
          alert('새 계정이 생성되었습니다!');
        } else {
          // 기존 계정 연결 로직
          response = await axios.post('http://localhost:8080/users/connect', {
            oauthId: Number(this.oauthId),
            socialId: this.socialId,
            loginId: this.loginId,
            password: this.password
          });
          console.log('기존 계정 연결:', response.data);
          alert('기존 계정과 연결되었습니다!');
        }
        this.closeModal();
        window.location.href = 'http://localhost:8081/';
      } catch (error) {
        if (error.response && error.response.status === 409) {
          this.error = '이미 사용 중인 이메일 주소입니다. 다른 이메일을 사용해주세요.';
        } else {
          this.error = '오류가 발생했습니다. 다시 시도해주세요.';
        }
      }
    },
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
      if (this.timer > 0) return; // 타이머가 0보다 클 경우 함수 종료
      try {
        this.isSendingCode = true;
        this.verificationButtonText = '전송 중...';
        // 이메일 인증 코드 발송 API 호출
        await axios.post('/users/email', { email: this.email });
        console.log('인증 코드 발송:');
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
      this.timer = 300; // 5분 = 300초
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
        // 이메일 인증 코드 확인 API 호출
        const response = await axios.post('/users/email/validate', {
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
};
</script>

<style scoped>
body {
  font-family: 'Roboto', Arial, sans-serif;
  background-color: #f5f5f5;
  margin: 0;
  padding: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
}
.container {
  margin-top: 100px;
  background-color: white;
  padding: 3rem;
  border-radius: 12px;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
  width: 550px;
  text-align: center;
}
h1 {
  color: #ff6600;
  margin-bottom: 1.5rem;
  font-size: 2.8rem;
  font-weight: 700;
}
.info-text {
  color: #555;
  margin-bottom: 2.5rem;
  line-height: 1.8;
  font-size: 1.1rem;
}
button {
  margin: 0.75rem;
  padding: 1rem 2rem;
  background-color: #ff6600;
  color: white;
  border: none;
  border-radius: 50px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 1.1rem;
  font-weight: 600;
  box-shadow: 0 4px 6px rgba(255, 102, 0, 0.2);
}
button:hover {
  background-color: #e65c00;
  transform: translateY(-2px);
  box-shadow: 0 6px 8px rgba(255, 102, 0, 0.3);
}
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
  color: #ff6600;
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
input {
  padding: 0.75rem;
  margin-top: 0.5rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s ease;
  width: 100%;
  box-sizing: border-box;
}
input:focus {
  outline: none;
  border-color: #ff6600;
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
  background-color: #ff6600;
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
  background-color: #e65c00;
  transform: translateY(-2px);
  box-shadow: 0 4px 6px rgba(255, 102, 0, 0.2);
}
h2 {
  color: #ff6600;
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
  padding: 0.75rem 1rem;
  margin: 0;
  font-size: 0.9rem;
  white-space: nowrap;
  flex-shrink: 0;
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
  color: #ff6600;
  margin-top: 0.5rem;
  width: 100%;
  text-align: right;
}
.resend-code {
  background: none;
  border: none;
  color: #ff6600;
  cursor: pointer;
  font-size: 0.9rem;
  text-decoration: underline;
  padding: 0;
  margin: 0;
}
.resend-code:hover {
  color: #e65c00;
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
  border-top: 2px solid #ff6600;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-right: 10px;
}
@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
