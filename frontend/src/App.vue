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
  <CallPanel />
</template>

<script setup>
import { onMounted, ref } from "vue";
import { connectSocket } from "./websocket.js";
import { setCurrentUser, store } from "./store.js";
import AuthPage from "./components/AuthPage.vue";
import CallPanel from "./components/CallPanel.vue";
import ChatPanel from "./components/ChatPanel.vue";
import ImageLightbox from "./components/ImageLightbox.vue";
import Sidebar from "./components/Sidebar.vue";
import ToastNotification from "./components/ToastNotification.vue";

const chatPanel = ref(null);

function onLoggedIn() {
  // AuthPage 宸茬粡澶勭悊浜嗙櫥褰曢€昏緫
}

function onSelectFriend(friend) {
  store.currentMode = "friend";
}

function onSelectGroup(group) {
  store.currentMode = "group";
}

function onLogout() {
  // Sidebar 宸茬粡澶勭悊浜嗘竻闄ら€昏緫
}

onMounted(() => {
  const token = localStorage.getItem("im_token");
  const userId = localStorage.getItem("im_userId");
  const username = localStorage.getItem("im_username");
  if (token && userId && username) {
    setCurrentUser({ token, userId: Number(userId), username });
    connectSocket();
  }
});
</script>
