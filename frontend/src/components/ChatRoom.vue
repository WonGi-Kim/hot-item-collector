<template>
  <div id="app">
    <AppHeader v-if="activeChat" />
    <div v-if="activeChat" class="main-content">
      <div class="chat-container">
        <div class="chat-list">
          <div class="chat-search">
            <input v-model="searchQuery" placeholder="채팅방 검색..." />
            <button @click="filterChats">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
                <path d="M11.742 10.344a6.5 6.5 0 1 0-1.397 1.398h-.001c.03.04.062.078.098.115l3.85 3.85a1 1 0 0 0 1.415-1.414l-3.85-3.85a1.007 1.007 0 0 0-.115-.1zM12 6.5a5.5 5.5 0 1 1-11 0 5.5 5.5 0 0 1 11 0z"/>
              </svg>
            </button>
          </div>
          <div class="chat-items">
            <div
                v-for="(chat, index) in filteredChatList"
                :key="index"
                :class="['chat-item', { active: activeChatIndex === index }]"
                @click="setActiveChat(index)"
            >
              <img :src="chat.avatar || 'default-avatar-url.png'" :alt="chat.name" class="chat-item-avatar" />
              <div class="chat-item-info">
                <div class="chat-item-name">{{ chat.sellerName }}</div>
              </div>
            </div>
          </div>
        </div>
        <div v-if="activeChat" class="chat-window">
          <div class="chat-header" @click="showUserModal">
            <img :src="activeChat.avatar || 'default-avatar-url.png'" :alt="activeChatName" class="chat-header-avatar" />
            {{ activeChatName }}
          </div>
          <div class="chat-messages">
            <div v-for="(message, index) in activeChat.messages" :key="index" :class="['message', message.type]">
              <div class="message-bubble">{{ message.text }}</div>
              <div class="message-time">{{ message.time }}</div>
            </div>
          </div>
          <div class="chat-input">
            <input v-model="newMessage" @keyup.enter="sendMessage" placeholder="메시지를 입력하세요..." />
            <button @click="sendMessage">전송</button>
          </div>
        </div>
      </div>
    </div>
    <div class="modal" v-if="showModal">
      <div class="modal-content">
        <div class="modal-profile">
          <img :src="activeChat?.avatar || 'default-avatar-url.png'" :alt="activeChatName" />
          <div class="modal-profile-info">
            <h2>{{ activeChatName }}</h2>
            <p>{{ activeChat?.bio }}</p>
          </div>
        </div>
        <div class="modal-buttons">
          <button class="confirm" @click="goToUserProfile">상세 프로필</button>
          <button class="cancel" @click="closeModal">닫기</button>
        </div>
      </div>
    </div>
    <AppFooter v-if="activeChat" />
  </div>
</template>

<script>
import Cookies from "js-cookie";

const client = require('../client')
import { ref, computed, onMounted } from 'vue'
import AppHeader from './AppHeader.vue';
import AppFooter from './AppFooter.vue';

export default {
  components: {AppFooter, AppHeader},
  setup() {
    const chatList = ref([]) // 초기 데이터는 빈 배열로 설정

    const activeChatIndex = ref(0)
    const newMessage = ref('')
    const searchQuery = ref('')
    const showModal = ref(false)

    const activeChat = computed(() => chatList.value[activeChatIndex.value])
    const activeChatName = computed(() => activeChat.value.roomName)

    const filteredChatList = computed(() => {
      return chatList.value.filter(chat => {
        return chat.sellerName && chat.roomName.toLowerCase().includes(searchQuery.value.toLowerCase())
      });
    })

    const setActiveChat = (index) => {
      activeChatIndex.value = index
    }

    const sendMessage = () => {
      if (newMessage.value.trim() !== '') {
        const now = new Date()
        const time = now.toLocaleString('ko-KR', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit'
        })
        activeChat.value.messages.push({
          type: 'sent',
          text: newMessage.value,
          time: time
        })
        newMessage.value = ''

        // Simulate a response
        setTimeout(() => {
          const responseTime = new Date()
          const responseTimeString = responseTime.toLocaleString('ko-KR', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
          })
          activeChat.value.messages.push({
            type: 'received',
            text: '네, 알겠습니다. 더 궁금한 점이 있으면 말씀해 주세요.',
            time: responseTimeString
          })
        }, 1000)
      }
    }

    const filterChats = () => {
      // Filtering is handled by the computed property 'filteredChatList'
    }

    const showUserModal = () => {
      showModal.value = true
    }

    const closeModal = () => {
      showModal.value = false
    }

    const goToUserProfile = () => {
      // Typically use router here
      alert(`${activeChatName.value}의 상세 프로필로 이동합니다.`)
      closeModal()
    }

    const loadChatLists = async () => {
      try {
        const accessToken = Cookies.get('access_token');
        if (!accessToken) {
          throw new Error('Access token is missing.');
        }
        const response = await client.get(`/chatrooms/list`, {
          headers: {
            'Authorization': accessToken
          }
        });

        // 서버에서 받아온 데이터를 chatList에 반영
        chatList.value = response.data.result.map(chat => ({
          roomName: chat.roomName,
          buyerName: chat.buyerName,
          sellerName: chat.sellerName,
          avatar: chat.sellerImage,
          messages: [] // 초기 메시지 목록은 빈 배열로 설정
        }));

      } catch (error) {
        console.error('Failed to load chat rooms:', error)
      }
    }

    onMounted(() =>{
      loadChatLists();
    })

    return {
      chatList,
      activeChatIndex,
      activeChat,
      activeChatName,
      newMessage,
      searchQuery,
      showModal,
      filteredChatList,
      setActiveChat,
      sendMessage,
      filterChats,
      showUserModal,
      closeModal,
      goToUserProfile,
      loadChatLists
    }
  }
}
</script>

<style>
/* The CSS styles remain the same */
:root {
  --main-color: #FF0000;
  --text-color: #333;
  --bg-color: #FFFFFF;
  --hover-color: #FF6666;
  --button-color: #FFCCCB;
  --button-hover-color: #FF6666;
  --footer-bg: #f8f8f8;
  --chat-bg: #f0f0f0;
  --chat-sent: #e6f3ff;
  --chat-received: #f5f5f5;
  --border-color: #ddd;
  --chat-header-bg: #f0f0f0;
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

.top-bar {
  background-color: var(--main-color);
  color: var(--bg-color);
  padding: 10px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.top-bar h1 {
  margin: 0;
  font-size: 1.5em;
}

.top-bar nav a {
  color: var(--bg-color);
  text-decoration: none;
  margin-left: 20px;
}

.top-bar nav a:hover {
  color: var(--hover-color);
}

.main-content {
  flex: 1;
  justify-content: center;
  display: flex;
  padding: 20px;
}

.chat-container {
  display: flex;
  height: 700px;
  width: 1100px;
  max-width: 100%;
  border: 1px solid var(--border-color);
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}

.chat-list {
  width: 30%;
  background-color: var(--chat-bg);
  border-right: 1px solid var(--border-color);
  overflow-y: auto;
}

.chat-search {
  display: flex;
  padding: 10px;
  border-bottom: 1px solid var(--border-color);
}

.chat-search input {
  flex: 1;
  padding: 5px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
}

.chat-search button {
  background-color: var(--button-color);
  border: none;
  padding: 5px 10px;
  margin-left: 5px;
  border-radius: 4px;
  cursor: pointer;
}

.chat-search button:hover {
  background-color: var(--button-hover-color);
}

.chat-items {
  padding: 10px;
}

.chat-item {
  display: flex;
  align-items: center;
  padding: 10px;
  cursor: pointer;
  border-bottom: 1px solid var(--border-color);
}

.chat-item:hover {
  background-color: var(--hover-color);
}

.chat-item.active {
  background-color: var(--hover-color);
}

.chat-item-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin-right: 10px;
}

.chat-item-info {
  display: flex;
  flex-direction: column;
}

.chat-item-name {
  font-weight: bold;
}

.chat-item-preview {
  color: #666;
}

.chat-window {
  width: 70%;
  display: flex;
  flex-direction: column;
}

.chat-header {
  background-color: var(--chat-header-bg);
  padding: 10px;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  cursor: pointer;
}

.chat-header-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin-right: 10px;
}

.chat-messages {
  flex: 1;
  padding: 10px;
  overflow-y: auto;
  background-color: var(--chat-bg);
}

.message {
  margin-bottom: 10px;
}

.message.sent {
  text-align: right;
}

.message.received {
  text-align: left;
}

.message-bubble {
  display: inline-block;
  padding: 10px;
  border-radius: 10px;
}

.message.sent .message-bubble {
  background-color: var(--chat-sent);
}

.message.received .message-bubble {
  background-color: var(--chat-received);
}

.message-time {
  font-size: 0.8em;
  color: #999;
}

.chat-input {
  display: flex;
  padding: 10px;
  border-top: 1px solid var(--border-color);
}

.chat-input input {
  flex: 1;
  padding: 10px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
}

.chat-input button {
  background-color: var(--button-color);
  border: none;
  padding: 10px;
  margin-left: 5px;
  border-radius: 4px;
  cursor: pointer;
}

.chat-input button:hover {
  background-color: var(--button-hover-color);
}

.bottom-bar {
  background-color: var(--footer-bg);
  color: #666;
  padding: 10px 20px;
  text-align: center;
  border-top: 1px solid var(--border-color);
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-content {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  width: 80%;
  max-width: 500px;
  text-align: center;
}

.modal-profile {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}

.modal-profile img {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  margin-right: 20px;
}

.modal-profile-info {
  text-align: left;
}

.modal-buttons {
  display: flex;
  justify-content: space-between;
}

.modal-buttons .confirm,
.modal-buttons .cancel {
  background-color: var(--button-color);
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
}

.modal-buttons .confirm:hover,
.modal-buttons .cancel:hover {
  background-color: var(--button-hover-color);
}
</style>
