<template>
  <div id="app">
    <AppHeader />
    <div class="main-content">
      <div class="chat-container">
        <div class="chat-list">
          <!-- 채팅방 목록 -->
          <div class="chat-items">
            <div
                v-for="(chat, index) in filteredChatList"
                :key="index"
                :class="['chat-item', { active: activeChatIndex === index }]"
                @click="setActiveChat(index)"
            >
              <img :src="chat.avatar" :alt="chat.sellerName" class="chat-item-avatar" />
              <div class="chat-item-info">
                <div class="chat-item-name">
                  <!-- 현재 사용자와 sellerName이 같다면 buyerName을 사용 -->
                  {{ chat.sellerName === currentUser.valueOf() ? chat.buyerName : chat.sellerName }}
                </div>
                <div class="chat-item-preview">
                  {{ chat.messages.length > 0 ? chat.messages[chat.messages.length - 1].text : 'No messages' }}
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="chat-window" v-if="activeChat">
          <div class="chat-header">
            {{ activeChat.roomName }}
          </div>
          <div class="chat-messages" id="chat-room">
            <div
                v-for="(message, index) in activeChat.messages"
                :key="index"
                :class="['message', message.type]"
            >
              <div class="message-bubble">{{ message.text }}</div>
              <div class="message-time">{{ message.time }}</div>
            </div>
          </div>
          <div class="chat-input">
            <input
                v-model="newMessage"
                @keyup.enter="sendMessage"
                placeholder="메시지를 입력하세요..."
            />
            <button @click="sendMessage">전송</button>
          </div>
        </div>
      </div>
    </div>
    <AppFooter />
  </div>
</template>

<script>
import Cookies from 'js-cookie';
import AppHeader from './AppHeader.vue';
import AppFooter from './AppFooter.vue';
import { ref, computed, onMounted, watch } from 'vue';
const client = require('../client');

export default {
  components: { AppHeader, AppFooter },
  setup() {
    const chatList = ref([]);
    const activeChatIndex = ref(null);
    const newMessage = ref('');
    const searchQuery = ref('');
    const activeChat = computed(() => chatList.value[activeChatIndex.value] || null);
    const filteredChatList = computed(() =>
        chatList.value.filter(chat =>
            chat.roomName.toLowerCase().includes(searchQuery.value.toLowerCase())
        )
    );
    const currentUser = ref('');

    const accessToken = Cookies.get('access_token');
    if (!accessToken) {
      throw new Error('Access token is missing.');
    }

    const loadStompJs = () => {
      return new Promise((resolve, reject) => {
        const stompScript = document.createElement('script');
        stompScript.src = 'https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js';
        stompScript.onload = () => resolve(window.Stomp);
        stompScript.onerror = () => reject(new Error('Failed to load STOMP.js.'));
        document.head.appendChild(stompScript);
      });
    };

    const loadChatLists = async () => {
      try {
        const response = await client.get('/chatrooms/list', {
          headers: {
            Authorization: accessToken,
          },
        });
        chatList.value = response.data.result.map(chat => ({
          roomId: chat.roomId,
          roomName: chat.roomName,
          buyerName: chat.buyerName,
          sellerName: chat.sellerName,
          avatar: chat.sellerImage,
          messages: [],
        }));
      } catch (error) {
        console.error('Failed to load chat rooms:', error);
      }
    };

    const socketConnections = {};

    const connectSocket = async roomId => {
      if (socketConnections[roomId]) {
        console.log(`Socket for room ${roomId} is already connected.`);
        return;
      }

      const Stomp = await loadStompJs();
      const socket = new WebSocket('ws://hotitemcollector.com:8080/ws');
      const stompClient = Stomp.over(socket);

      await new Promise((resolve, reject) => {
        stompClient.connect(
            { Authorization: accessToken },
            () => {
              console.log('Connected to WebSocket.');

              const subscription = stompClient.subscribe(`/topic/${roomId}`, message => {
                try {
                  const chatMessages = JSON.parse(message.body);
                  if (Array.isArray(chatMessages)) {
                    chatMessages.forEach(chatMessage => {
                      addMessageToChat(roomId, chatMessage);
                    });
                  } else {
                    addMessageToChat(roomId, chatMessages);
                  }
                } catch (e) {
                  console.error('Error parsing message body:', e);
                }
              });

              socketConnections[roomId] = {
                stompClient,
                subscription,
              };

              stompClient.send(`/app/join/${roomId}`, {}, JSON.stringify({ roomId }));
              resolve();
            },
            error => {
              console.error('Connection error:', error);
              reject(error);
            }
        );
      });
    };

    const disconnectSocket = roomId => {
      const connection = socketConnections[roomId];
      if (connection) {
        connection.subscription.unsubscribe();
        connection.stompClient.disconnect(() => {
          console.log(`Disconnected from room: ${roomId}`);
        });
        delete socketConnections[roomId];
      }
    };

    const setActiveChat = async index => {
      const newRoomId = chatList.value[index].roomId;
      if (activeChatIndex.value !== null) {
        const previousRoomId = chatList.value[activeChatIndex.value].roomId;
        if (previousRoomId !== newRoomId) {
          disconnectSocket(previousRoomId); // Disconnect the existing socket before connecting to a new one
        }
      }
      activeChatIndex.value = index;
      await connectSocket(newRoomId);
    };

    const fetchUserProfile = async () => {
      try {
        const response = await client.get('/users/profile', {
          headers: {
            Authorization: accessToken,
          },
        });
        currentUser.value = response.data.result.nickname;
      } catch (error) {
        console.error('Failed to fetch user profile:', error);
      }
    };

    const addMessageToChat = (roomId, message) => {
      const chat = chatList.value.find(c => c.roomId === roomId);
      if (chat) {
        const isDuplicate = chat.messages.some(m => m.id === message.id);

        if (!isDuplicate) {
          chat.messages.push({
            id: message.id,
            type: message.sender === currentUser.value ? 'sent' : 'received',
            text: message.message,
            time: new Date(message.timestamp).toLocaleString('ko-KR', {
              year: 'numeric',
              month: '2-digit',
              day: '2-digit',
              hour: '2-digit',
              minute: '2-digit',
            }),
          });
        }
      }
    };

    const sendMessage = () => {
      if (newMessage.value.trim() !== '') {
        const roomId = activeChat.value?.roomId;
        const messageContent = {
          roomId: roomId,
          message: newMessage.value,
        };

        const connection = socketConnections[roomId];
        if (connection && connection.stompClient) {
          connection.stompClient.send('/app/send', {}, JSON.stringify(messageContent));
          newMessage.value = '';
        } else {
          console.error(`No active WebSocket connection or no roomId.`);
        }
      }
    };

    onMounted(() => {
      loadChatLists();
      fetchUserProfile();
    });

    // Watch for changes in activeChatIndex to ensure socket connection is managed
    watch(activeChatIndex, async (newIndex, oldIndex) => {
      if (oldIndex !== null && oldIndex !== newIndex) {
        const previousRoomId = chatList.value[oldIndex].roomId;
        disconnectSocket(previousRoomId);
      }
      if (newIndex !== null) {
        const newRoomId = chatList.value[newIndex].roomId;
        await connectSocket(newRoomId);
      }
    });

    return {
      chatList,
      activeChatIndex,
      activeChat,
      newMessage,
      searchQuery,
      filteredChatList,
      loadStompJs,
      loadChatLists,
      connectSocket,
      disconnectSocket,
      setActiveChat,
      addMessageToChat,
      sendMessage,
      fetchUserProfile,
      currentUser
    };
  },
};
</script>

<style scoped>
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
