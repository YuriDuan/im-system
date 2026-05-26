<template>
  <AuthPage v-if="!store.currentUser" @logged-in="onLoggedIn" />
  <div v-else class="app-page">
    <Sidebar
      @select-friend="onSelectFriend"
      @select-group="onSelectGroup"
      @logout="onLogout"
    />
    <ChatPanel ref="chatPanel" />
  </div>
  <ToastNotification />
  <ImageLightbox />
</template>

<script setup>
import { ref, onMounted } from "vue";
import { store, setCurrentUser } from "./store.js";
import { connectSocket } from "./websocket.js";
import AuthPage from "./components/AuthPage.vue";
import Sidebar from "./components/Sidebar.vue";
import ChatPanel from "./components/ChatPanel.vue";
import ToastNotification from "./components/ToastNotification.vue";
import ImageLightbox from "./components/ImageLightbox.vue";

const chatPanel = ref(null);

function onLoggedIn() {
  // AuthPage 已经处理了登录逻辑
}

function onSelectFriend(friend) {
  store.currentMode = "friend";
}

function onSelectGroup(group) {
  store.currentMode = "group";
}

function onLogout() {
  // Sidebar 已经处理了清除逻辑
}

onMounted(() => {
  // 尝试从 localStorage 恢复会话
  const token = localStorage.getItem("im_token");
  const userId = localStorage.getItem("im_userId");
  const username = localStorage.getItem("im_username");
  if (token && userId && username) {
    setCurrentUser({ token, userId: Number(userId), username });
    connectSocket();
  }
});
</script>
