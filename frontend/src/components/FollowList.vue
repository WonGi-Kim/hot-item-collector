<template>
  <div>
    <AppHeader />
    <main class="container following-list">
      <h1>팔로우 목록</h1>
      <div v-for="user in followingUsers" :key="user.id" class="following-item" @click="goToUserProfile(user.id)">
        <img :src="user.profileImage" :alt="user.name" class="following-profile-image">
        <div class="following-info">
          <div class="following-name">{{ user.name }}</div>
          <div class="following-bio">{{ user.bio }}</div>
        </div>
        <button @click.stop="toggleFollow(user)" :class="['toggle-follow', { following: user.isFollowing }]">
          {{ user.isFollowing ? '팔로우 취소' : '팔로우' }}
        </button>
      </div>
    </main>
    <AppFooter />
  </div>
</template>

<script>
import AppHeader from './AppHeader.vue';
import AppFooter from './AppFooter.vue';
import { ref, onMounted } from 'vue';
const client = require('../client')
import { useRouter } from "vue-router";
import Cookies from "js-cookie";

export default {
  components: { AppHeader, AppFooter },
  setup() {
    const router = useRouter();

    const followingUsers = ref([]);

    const toggleFollow = async (user) => {
      const accessToken = Cookies.get('access_token');
      try {
        if (user.isFollowing) {
          // 팔로우 취소 요청
          await client.delete(`/follow/${user.id}`, {
            headers: {
              'Authorization': accessToken
            }
          });
        } else {
          // 팔로우 요청
          await client.post(`/follow/${user.id}`, {}, {
            headers: {
              'Authorization': accessToken
            }
          });
        }
        user.isFollowing = !user.isFollowing;
        console.log('팔로우 상태 변경 성공');
      } catch (error) {
        console.error('팔로우 상태 변경 실패:', error);
      }
    }

    const goToUserProfile = (userId) => {
      alert(`사용자 ID ${userId}의 프로필 페이지로 이동합니다.`);
      router.push('/profile');
    }

    // API 요청을 통해 데이터를 가져오는 함수
    const fetchFollowingUsers = async () => {
      try {
        const accessToken = Cookies.get('access_token');
        const response = await client.get('/follows', {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': accessToken
          }
        });
        console.log(response.data);
        // 응답 데이터 매핑
        followingUsers.value = response.data.result.map((item) => ({
          id: item.userId,
          name: item.userName,
          profileImage: item.profileImage.imageUrl,
          bio: item.profileInfo,
          isFollowing: true // Assume all users in the list are being followed
        }));
      } catch (error) {
        console.error('팔로잉 유저 목록을 가져오는 데 실패했습니다.', error);
      }
    };

    onMounted(fetchFollowingUsers);

    return {
      followingUsers,
      toggleFollow,
      goToUserProfile,
    }
  }
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

/* Following List Styles */
.following-list {
  padding: 30px 0;
  max-width: 800px;
  margin: 0 auto;
}

.following-item {
  display: flex;
  align-items: center;
  padding: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  background-color: var(--bg-color);
}

.following-item:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  transform: translateY(-5px);
  background-color: #f9f9f9;
}

.following-profile-image {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 20px;
}

.following-info {
  flex-grow: 1;
}

.following-name {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 5px;
}

.following-bio {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.toggle-follow {
  padding: 8px 16px;
  background-color: var(--bg-color);
  color: var(--main-color);
  border: 2px solid var(--main-color);
  border-radius: 20px;
  cursor: pointer;
  font-weight: bold;
  transition: all 0.3s ease;
}

.toggle-follow:hover {
  background-color: var(--main-color);
  color: var(--bg-color);
}

.toggle-follow.following {
  background-color: var(--main-color);
  color: var(--bg-color);
}

</style>
